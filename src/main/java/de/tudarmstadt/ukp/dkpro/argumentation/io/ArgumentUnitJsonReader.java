/*
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.tudarmstadt.ukp.dkpro.argumentation.io;

import com.google.common.base.Joiner;
import de.tudarmstadt.ukp.dkpro.argumentation.types.Claim;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.languagetool.LanguageToolSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.io.TCReaderSingleLabel;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationSequence;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.javatuples.Pair;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

/**
 * @author Judith Eckle-Kohler
 * @author Roland Kluge
 * 
 * assumes as input the file 20140120_dump__after_overlapping_annotations.json
 * where the metadata header has been removed manually
 * 
 * add sentence based "claim" / "no-claim" annotations
 *
 */
public class ArgumentUnitJsonReader 
extends JCasCollectionReader_ImplBase
implements TCReaderSingleLabel

{
	
    public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;

    public static final String PARAM_INPUT_FILE = "inputFile";
    @ConfigurationParameter(name = PARAM_INPUT_FILE, mandatory = true, description = "JSON input file")
    private File inputFile;

    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = true, description = "two-letter language code")
    private String language;

    public static final String PARAM_ANNOTATOR = "annotator";
    @ConfigurationParameter(name = PARAM_ANNOTATOR, mandatory = true, description = "The annotator whose annotations shall be included")
    private String annotator;
    
    public static final String PARAM_PATH_MODEL_SENTENCE = "sentenceModePath";
    @ConfigurationParameter(name = PARAM_PATH_MODEL_SENTENCE, mandatory = true, description = "Path to OpenNLP sentence model")
    private String sentenceModePath;
    
    public static final String PARAM_PATH_MODEL_TOKEN = "tokenModePath";
    @ConfigurationParameter(name = PARAM_PATH_MODEL_TOKEN, mandatory = true, description = "Path to OpenNLP tokenizer model")
    private String tokenModePath;
   

    private List<String> labels;
    private List<String> texts;
    
    private int rawTextIndex = 0;
    private List<String> rawTexts;
    private List<List<String>> p_labels;
    private List<List<Pair<Integer, Integer>>> indexes;
    
    SentenceModel sentenceModel;
    TokenizerModel tokenModel;
    
    public static final String CLAIM = "claim";
    public static final String RESTATEMENT = "claim-re";

    public static final String SUPPORT = "support";
    public static final String SUPPORT_PRE = "support-pre";
    public static final String SUPPORT_POST = "support-post";

    public static final String ATTACK = "attack";
    public static final String ATTACK_PRE = "attack-pre";
    public static final String ATTACK_POST = "attack-post";

    public static final String PREMISE = "premise";
    public static final String PREMISE_PRE = "premise-pre";
    public static final String PREMISE_POST = "premise-post";

    public static final String ARGUMENT = "argument";

    public static final String ARGUMENT_UNIT = "arg-unit";
    
    public static final String HIGH = "high";
    public static final String MEDIUM = "medium";
    public static final String LOW = "low";
	
    /**
     * Per-corpus keys
     */
    public static final String CORPUS_METADATA_FLAG = "corpus_metadata";
    public static final String PREPROCESSING_DATE = "preprocessing_date";
    public static final String POSTPROCESSING_DATE = "postprocessing_date";
    public static final String SEGMENTER = "segmenter";

    /**
     * Per-document keys
     */
    public static final String FILE = "file";
    public static final String PARAGRAPH_COUNT = "num_paragraphs";
    public static final String SENTENCE_COUNT = "num_sentences";
    public static final String TEXT = "text";
    public static final String TOKEN_COUNT = "num_tokens";
    public static final String URL = "url";
    public static final String USER_ANNOTATIONS = "user_annotations";

    /**
     * Per-annotator keys 
     */
    public static final String ARGUMENTATION_UNITS = "arg_units";
    public static final String NOTES = "notes";
    public static final String ANNOTATOR = "annotator";
    public static final String APPROVED = "approved";

    public static final String VIEW_ORIGINAL_HTML = "OriginalHtmlView";
    /**
     * The index of the first marked token in HTML.
     * 
     * The first span with class 'token' has this index.
     */
    public static final int FIRST_TOKEN_IDX = 1;

    /**
     * The index of the first marked sentence in HTML.
     * 
     * The first span with class 'sentence' has this index.
     */
    public static final int FIRST_SENTENCE_IDX = 1;
    
    private int offset;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        // read input file with texts (= argument units) and labels
        labels = new ArrayList<String>();
        texts = new ArrayList<String>();
        
        this.rawTexts = new ArrayList<String>();
        this.p_labels = new ArrayList<List<String>>();
        this.indexes = new ArrayList<List<Pair<Integer,Integer>>>();

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream sentenceModelInput = classLoader.getResourceAsStream(sentenceModePath);
		InputStream tokenModelInput = classLoader.getResourceAsStream(tokenModePath);

        try {
        	sentenceModel  = new SentenceModel(sentenceModelInput);
        	tokenModel = new TokenizerModel(tokenModelInput);
        }
        catch (IOException e) {
          e.printStackTrace();
        }
        finally {
          if (sentenceModel != null) {
            try {
            	sentenceModelInput.close();
            	tokenModelInput.close();
            }
            catch (IOException e) {
            }
          }
        }
        
        Iterator<Map<String, Object>> documentsIterator;
        Map<String, Integer> labelCount = new HashMap<String, Integer>();
        
        try {
            String inputString = FileUtils.readFileToString(this.inputFile);
            JSONParser jsonParser = new JSONParser();
            @SuppressWarnings("unchecked")
            ArrayList<Map<String, Object>> jsonTexts = new ArrayList<Map<String, Object>>(
                    (List<Map<String, Object>>) jsonParser.parse(inputString));
            documentsIterator = jsonTexts.iterator();
            
            while (documentsIterator.hasNext()) {
            	Map<String, Object> jsonData = documentsIterator.next();
//            	org.json.JSONObject jsonObject = new org.json.JSONObject(jsonData);
//                String string = jsonObject.toString();
            	@SuppressWarnings("unchecked")
            	List<Map<String, Object>> userAnnotations = (List<Map<String, Object>>) jsonData
                    .get(USER_ANNOTATIONS);
            	if (userAnnotations == null) continue;
            	for (Map<String, Object> userAnnotation : userAnnotations) {
	                String annotator = (String) userAnnotation.get(ANNOTATOR);
	                if (annotator.equals(this.annotator)) {
	                	
	                	String htmlText = (String) jsonData.get(TEXT);
	                	org.jsoup.nodes.Document cleanedText = Jsoup.parse(htmlText);
	                	String rawDocumentText = cleanedText.text();
	                	
	                	// raw Text to list
	                	this.rawTexts.add(rawDocumentText);
	                	//System.out.println(rawDocumentText.substring(0, 100));
	                	List<String> subParas = new ArrayList<String>();
	                	List<String> subLabels = new ArrayList<String>();
	                	List<Pair<Integer, Integer>> subIndexes = new ArrayList<>();
	                	
	                	Map<Integer, Token> idxToTokenMapping = this
	                        .createIndexToTokenMapping(rawDocumentText);
	
	                	@SuppressWarnings("unchecked")
	                	List<String> argUnits = (List<String>) userAnnotation
	                        .get(ARGUMENTATION_UNITS);
	
	                	for (String argUnit : argUnits) {
	                		//System.out.println("au: " +argUnit);  
	                		String cleanedArgUnit = argUnit.replaceAll("\\s+", "");
	                		Matcher matcher = getRecognitionPattern().matcher(
	                            cleanedArgUnit);
	                		if (!matcher.matches()) {
	                			this.getLogger()
	                                .warn(String
	                                        .format("argument unit %s does not match the expected pattern %s",
	                                                cleanedArgUnit, 
	                                                        getRecognitionPattern().pattern()));
	                		}
	                		else {
	                			// **************************************************
	                			// coordinates of an argument unit:
	                			String label = matcher.group(1);
	                			String stringIndices = matcher.group(3).replaceAll("^,", "");
	            				List<Integer> indices = parseIntList(
	                                stringIndices, ",");
	
	            				int firstIndex = Collections.min(indices);
	            				Token firstToken = idxToTokenMapping.get(firstIndex);
	
	            				int lastIndex = Collections.max(indices);
	            				Token lastToken = idxToTokenMapping.get(lastIndex);
	            				
	            				//String text = getArgunitText(firstIndex, lastIndex);
	            				// *****************************************************
	            				
	            				String generalizedLabel = getGeneralizedLabel(label);
	            				                            
	         	               // Read argument unit as dummy Paragraph annotation to get the text
	            				JCas dummyJCas = JCasFactory.createJCas();
	            				dummyJCas.setDocumentText(rawDocumentText);
	            				
	            				subParas.add(rawDocumentText.substring(firstToken.getBegin(), lastToken.getEnd()));
	            				//System.out.println(rawDocumentText.substring(firstToken.getBegin(), lastToken.getEnd()));
	            				Pair<Integer, Integer> index = new Pair<Integer, Integer>(firstToken.getBegin(), lastToken.getEnd());
	            				subIndexes.add(index);
	            				subLabels.add(generalizedLabel);
	   
	            				Paragraph para = new Paragraph(dummyJCas, firstToken.getBegin(), lastToken.getEnd());
	            				// raw Text -> paras
	            				// 
	            				//System.out.println("argument unit text: " +para.getCoveredText());
	            				
	            				texts.add(para.getCoveredText());
	            				labels.add(generalizedLabel);
	            				if (labelCount.containsKey(generalizedLabel)) labelCount.put(generalizedLabel, labelCount.get(generalizedLabel) + 1);
	            				else labelCount.put(generalizedLabel, 1);
	            			
	            				
	            				//System.out.println("annotator: " +annotator);         

	            				System.out.println("label: " +label +" general label: " +generalizedLabel); 
	            				        				
	                		} // matching was ok
	                	} // for argUnit : argUnits
	                	this.indexes.add(subIndexes);
	                	this.p_labels.add(subLabels);
	                } // if annotator.equals(this.annotator)
            } // for user annotation
        } // while hasNext
        }
        catch (final IOException e) {
            throw new ResourceInitializationException(e);
        }
        catch (final ParseException e) {
            throw new ResourceInitializationException(e);
        } 
        catch (UIMAException e) {
        	throw new ResourceInitializationException(e);		
        }
        offset = 0;
        System.out.println("\n\n\n");    
        for (String labelKey : labelCount.keySet())
        	System.out.println(labelKey + ": " + labelCount.get(labelKey));
        System.out.println("\n\n\n");    
        System.out.println("number of AUs: " +texts.size());
    }

	private String getGeneralizedLabel(String label) {
		String result = null;
		if (label.startsWith("claim")) {
			result = "claim";
		} else if (label.startsWith("support")) {
			result = "support";			
		} else if (label.startsWith("attack")) {
			result = "attack";	
		}		
		return result;
	}
	


	@Override
	public boolean hasNext() throws IOException, CollectionException {
		return rawTextIndex < rawTexts.size();
	}

	@Override
	public Progress[] getProgress() {
        return new Progress[] { new ProgressImpl(offset, texts.size(), "argunits") };
	}

	@Override
	public String getTextClassificationOutcome(JCas jcas)
			throws CollectionException {
		return labels.get(offset);
	}

	@Override
	public void getNext(JCas aJCas) throws IOException, CollectionException {

        System.out.println("Next: " + rawTextIndex + " of " + (rawTexts.size()-1));
        // setting the document text
        String rawText = rawTexts.get(rawTextIndex);
        aJCas.setDocumentLanguage(language);
        aJCas.setDocumentText(rawText);
        
	     // segmentation into words
        Tokenizer tokenizer = new TokenizerME(tokenModel);
        Span tokenSpans[] = tokenizer.tokenizePos(rawText);
        for(Span tokenSpan : tokenSpans){
        	Token token = new Token(aJCas, tokenSpan.getStart(), tokenSpan.getEnd());
			token.addToIndexes();
		}
        
        // segmentation into sentences
        SentenceDetectorME sentenceDetector = new SentenceDetectorME(sentenceModel);
        Span sentences[] = sentenceDetector.sentPosDetect(rawText);
        
		for (Span sentenceSpan : sentences) {
			int start = sentenceSpan.getStart();
			int end = sentenceSpan.getEnd();
						
			Sentence sentence = new Sentence(aJCas, start, end);
			TextClassificationUnit unit = new TextClassificationUnit(aJCas, start, end);
			sentence.addToIndexes();
			unit.addToIndexes();


			List<Pair<Integer, Integer>> indexes = this.indexes.get(rawTextIndex);
			List<String> labels = this.p_labels.get(rawTextIndex);

			int labelIndex = 0;
			for (String label : labels) {
				Pair<Integer, Integer> index = indexes.get(labelIndex);

				// label covers part of sentence
				// label-begin inside sentence OR label-end inside sentence
				if ((start <= index.getValue0() && end >= index.getValue0())
						|| (start <= index.getValue1() && end >= index.getValue1())) {
					if (label.startsWith("claim")) {
						Claim claim = new Claim(aJCas, start, end);
						claim.addToIndexes();
					}
				}
				// label outside sentence
				else if (start > index.getValue1() || end < index.getValue0()) {
					// ignore
				}
				labelIndex++;
			}
		}
        
        // as we are creating more than one CAS out of a single file, we need to have different
        // document titles and URIs for each CAS
        // otherwise, serialized CASes will be overwritten
        DocumentMetaData dmd = DocumentMetaData.create(aJCas);
        dmd.setDocumentTitle("Argunit" + rawTextIndex);
        dmd.setDocumentUri("Argunit" + rawTextIndex);
        dmd.setDocumentId(String.valueOf(rawTextIndex));
        TextClassificationSequence sequence = new TextClassificationSequence(
				aJCas, 
				0, 
				rawText.length()
				);
        sequence.addToIndexes();
        rawTextIndex++;
        offset++;
	}
	
    protected final Map<Integer, Token> createIndexToTokenMapping(final String rawDocumentText)
            throws UIMAException
    {
            final JCas dummyJCas = JCasFactory.createJCas();
            dummyJCas.setDocumentText(rawDocumentText);
            dummyJCas.setDocumentLanguage(this.language);
            SimplePipeline.runPipeline(dummyJCas,
                    createEngineDescription(LanguageToolSegmenter.class));

            final Map<Integer, Token> idxToTokenMapping = mapIndexToAnnotation(
                    dummyJCas, Token.class, FIRST_TOKEN_IDX);
            return idxToTokenMapping;
        }
    
    /**
     * Creates a mapping from indexed position to annotation at that position
     * 
     * @param jCas the CAS 
     * @param type the type to build the index for
     * @param firstIndex the index of the first token (to distinguish between 0-based and 1-based indexing)
     * @return the index mapping
     */
    public static <T extends Annotation> Map<Integer, T> mapIndexToAnnotation(final JCas jCas,
            final Class<T> type, final int firstIndex)
    {
        final Map<Integer, T> mapping = new HashMap<Integer, T>();

        int index = firstIndex;
        for (final T annotation : JCasUtil.select(jCas, type)) {
            mapping.put(index, annotation);
            ++index;
        }

        return mapping;
    }

    public static Pattern getRecognitionPattern()
    {
        final String joinedLabels = Joiner.on("|").join(getOriginalLabels());
        final String joinedConfidences = Joiner.on("|").join(Arrays.asList(HIGH, MEDIUM, LOW));

        final Pattern pattern = Pattern.compile(String.format("\\[\"(%s)\",\"(%s)\"((,\\d+)+)\\]",
                joinedLabels, joinedConfidences));

        return pattern;
    }
    
    /**
     * Returns a list of labels that may appear in the raw corpus
     * 
     * @return the raw labels
     */
    public static List<String> getOriginalLabels()
    {
        return Arrays
                .asList(CLAIM, RESTATEMENT, SUPPORT_PRE, SUPPORT_POST, ATTACK_PRE, ATTACK_POST);
    }
    
    /**
     * Converts a comma-separated string to a list of integers
     * 
     * @param indexList the string
     * @return a list of integers
     */
    public static List<Integer> parseIntList(final String indexList, final String separator)
    {
        final List<String> indicesAsString = Arrays.asList(indexList.split(separator));
        final List<Integer> indices = new ArrayList<Integer>();
        for (final String indexAsString : indicesAsString) {
            indices.add(Integer.valueOf(indexAsString));
        }
        return indices;
    }

}
