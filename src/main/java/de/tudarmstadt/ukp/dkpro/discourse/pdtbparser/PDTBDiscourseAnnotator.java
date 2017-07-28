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

package de.tudarmstadt.ukp.dkpro.discourse.pdtbparser;

import de.tudarmstadt.ukp.dkpro.core.api.discourse.DiscourseDumpWriter;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.discourse.pdtb.*;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import nu.xom.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Wrapper for discourse parser introduced in:
 * <p/>
 * Lin, Z., Ng, H. T., & Kan, M.-Y. (2014). A PDTB-styled end-to-end discourse parser.
 * Natural Language Engineering, 20(02), 151–184. doi:10.1017/S1351324912000307
 * <p/>
 * The parser is written in Ruby, is launched using JRuby.
 * <p/>
 * NOTE: experimental
 *
 * @author Ivan Habernal
 */
public class PDTBDiscourseAnnotator
        extends JCasAnnotator_ImplBase
{
    private static final String VAL = "value";
    protected PDTBParserWrapper wrapper;

    /**
     * Will completely ignore rebuttal annotations
     */
    public static final String PARAM_VERBOSE = "verbose";
    @ConfigurationParameter(name = PARAM_VERBOSE, mandatory = true, defaultValue = "false")
    private boolean verbose;

    @Override public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            wrapper = new PDTBParserWrapper();
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {
        String document = prepareFileForPDTB(extractParagraphs(aJCas));
        File tmpFileIn = null;
        File tmpFileOut = null;
        try {
            String documentId = DocumentMetaData.get(aJCas).getDocumentId();
            tmpFileIn = writeToTempFile(document, documentId);
            tmpFileOut = File.createTempFile("out" + documentId + "_", ".xml");
            wrapper.run(tmpFileIn, tmpFileOut);

            Builder builder = new Builder();
            try {
                Document xmlDoc = builder.build(tmpFileOut);

                annotateWithPDTBRelations(aJCas, xmlDoc, this.verbose);
                //                String xml = FileUtils.readFileToString(tmpFileOut);
            }
            catch (ParsingException e) {
                System.err.println(e.toString());
            }
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
        finally {
            FileUtils.deleteQuietly(tmpFileIn);
            FileUtils.deleteQuietly(tmpFileOut);
        }
    }

    public static void annotateWithPDTBRelations(JCas jCas, Document xmlDocument, boolean verbose)
            throws IOException
    {
        List<Paragraph> paragraphs = new ArrayList<>(JCasUtil.select(jCas, Paragraph.class));
        //        for (Paragraph p : paragraphs) {
        //            System.out.println(p.getBegin());
        //            System.out.println(p.getEnd());
        //            System.out.println(p.getCoveredText());
        //        }

        Elements paragraphsXml = xmlDocument.getRootElement().getChildElements("p");

        if (paragraphs.size() == paragraphsXml.size()) {
            for (int i = 0; i < paragraphs.size(); i++) {
                Paragraph paragraph = paragraphs.get(i);
                Element xmlParagraph = paragraphsXml.get(i);

                // match tokens in xml so that they correspond exactly to original tokens,
                // if possible
                recreateTokensInXMLParagraph(paragraph, xmlParagraph);

                // annotate
                annotateParagraph(paragraph, xmlParagraph, jCas, verbose);
            }
        }
        else {
            throw new IOException(
                    "Paragraphs in XML (" + paragraphsXml.size() + ") and jCas (" + paragraphs
                            .size() + ") do not match");
        }
    }

    private static void annotateParagraph(Paragraph paragraph, Element xmlParagraph, JCas jCas, boolean verbose)
    {
        Elements elements = xmlParagraph.getChildElements();
        if (elements.size() == 0) {
            System.err.println("No tokens found in paragraph" + xmlParagraph.toXML());
            return;
        }

        List<Token> tokens = JCasUtil.selectCovered(Token.class, paragraph);
        Elements xmlTokens = xmlParagraph.getChildElements("token");

        if (xmlTokens.size() != tokens.size()) {
            System.err.println("Could not map PDTB parse output back to the current paragraph:\n"
                    + paragraph.getCoveredText() +
                    "\nPDTB Parser returned the following:\n" + xmlParagraph.toXML());
            return;
        }

        List<DiscourseArgument> discourseArguments = new ArrayList<>();
        List<DiscourseConnective> discourseConnectives = new ArrayList<>();
        List<DiscourseAttribution> discourseAttributions = new ArrayList<>();

        // relation id and true=explicit, false=implicit
        Map<Integer, Boolean> discourseRelations = new HashMap<>();

        int elementsPosition = 0;
        int tokensPosition = 0;
        //        while (tokensPosition < tokens.size()) {
        if (verbose) {
            System.out.println(xmlParagraph.toXML());
        }

        while (elementsPosition < elements.size()) {
            Token token = null;
            if (tokensPosition < tokens.size()) {
                token = tokens.get(tokensPosition);
            }

            Token prevToken = null;
            if (tokensPosition > 0) {
                prevToken = tokens.get(tokensPosition - 1);
            }

            Element element = elements.get(elementsPosition);

            if (verbose) {
                System.out.println(element.toXML());
            }

            switch (element.getLocalName()) {
            case "token": {
                // we do nothing here
                tokensPosition++;
                break;
            }
            case "rel": {
                // we have some relation etc. - do the annotation here!
                Integer relationId = Integer.valueOf(element.getAttributeValue("id"));
                boolean isBegin = element.getAttributeValue("marker").equals("begin");

                // <rel type='exp' id='0' arg='1' marker='begin' />
                // or
                // <rel type='exp' id='0' connectiveType='Contrast' marker='begin' />

                // is connective?
                String connectiveType = element.getAttributeValue("connectiveType");
                if (connectiveType != null) {
                    // are we opening a connective?
                    if (isBegin) {
                        if (token == null) {
                            throw new IllegalStateException(
                                    "Cannot begin discourse relation without token");
                        }

                        DiscourseConnective discourseConnective = new DiscourseConnective(jCas);
                        discourseConnective.setConnectiveType(connectiveType);
                        discourseConnective.setParentRelationId(relationId);

                        // set begin to the current token
                        discourseConnective.setBegin(token.getBegin());

                        // and add to the temporary list
                        discourseConnectives.add(discourseConnective);
                    }
                    else {
                        // look-up corresponding connective
                        DiscourseConnective existingConnective = findLastConnective(
                                discourseConnectives, relationId, connectiveType);

                        // set end from the previous token
                        if (prevToken == null) {
                            throw new IllegalStateException("No preceding token");
                        }
                        existingConnective.setEnd(prevToken.getEnd());
                    }
                }
                else {
                    // we have a discourse argument
                    boolean isExplicit = element.getAttributeValue("type").equals("exp");
                    if (discourseRelations.containsKey(relationId)
                            && discourseRelations.get(relationId) != isExplicit) {
                        throw new IllegalStateException(
                                "Relation " + relationId + " explicitness is inconsistent");
                    }
                    else {
                        // put id and relation type to the map
                        discourseRelations.put(relationId, isExplicit);
                    }

                    int argumentNumber = Integer.valueOf(element.getAttributeValue("arg"));

                    String argumentType = element.getAttributeValue("t");

                    if (isBegin) {
                        if (token == null) {
                            throw new IllegalStateException(
                                    "Cannot begin discourse argument without token");
                        }

                        DiscourseArgument discourseArgument = new DiscourseArgument(jCas);
                        discourseArgument.setParentRelationId(relationId);
                        discourseArgument.setArgumentNumber(argumentNumber);
                        discourseArgument.setArgumentType(argumentType);

                        // set begin to current token
                        discourseArgument.setBegin(token.getBegin());

                        // add to the temporary list
                        discourseArguments.add(discourseArgument);
                    }
                    else {
                        // set end from the previous token
                        if (prevToken == null) {
                            throw new IllegalStateException("No preceding token");
                        }

                        DiscourseArgument existingArgument = findDiscourseArgument(
                                discourseArguments, relationId, argumentNumber);

                        if (existingArgument != null) {
                            existingArgument.setEnd(prevToken.getEnd());
                        }
                    }
                }

                break;
            }
            case "attr": {
                // we have some relation etc. - do the annotation here!
                boolean isBegin = element.getAttributeValue("marker").equals("begin");
                Integer attrId = Integer.valueOf(element.getAttributeValue("id"));

                if (isBegin) {
                    if (token == null) {
                        throw new IllegalStateException(
                                "Cannot begin discourse attribute without token");
                    }
                    DiscourseAttribution attribution = new DiscourseAttribution(jCas);
                    attribution.setBegin(token.getBegin());
                    attribution.setAttributeId(attrId);

                    // add to the temporary list
                    discourseAttributions.add(attribution);
                }
                else {
                    if (prevToken == null) {
                        throw new IllegalStateException("No preceding token");
                    }

                    DiscourseAttribution attribution = findDiscourseAttribution(
                            discourseAttributions, attrId);
                    if (attribution != null) {
                        attribution.setEnd(prevToken.getEnd());
                    }
                }

                break;
            }
            default:
                throw new IllegalArgumentException("Unknown element: " + element.getLocalName());
            }

            // and always move to the next element
            elementsPosition++;
        }

        // so what we have?
        if (verbose) {
            System.out.println("===== Discourse arguments");
            for (DiscourseArgument discourseArgument : discourseArguments) {
                System.out.println(discourseArgument);
                if (discourseArgument.getEnd() > 0) {
                    System.out.println(discourseArgument.getCoveredText());
                }
                else {
                    System.out.println("Missing end tag??");
                }
                System.out.println("---");
            }

            System.out.println("===== Discourse connectives");
            for (DiscourseConnective connective : discourseConnectives) {
                System.out.println(connective);
                System.out.println(connective.getCoveredText());
                System.out.println("---");
            }

            System.out.println("===== Discourse attributions");
            for (DiscourseAttribution attribution : discourseAttributions) {
                System.out.println(attribution);
                if (attribution.getEnd() > 0) {
                    System.out.println(attribution.getCoveredText());
                }
                System.out.println("---");
            }

            System.out.println("Explicit/implicit discourseRelations");
            System.out.println(discourseRelations);
        }
        for (Map.Entry<Integer, Boolean> entry : discourseRelations.entrySet()) {
            int relationId = entry.getKey();
            boolean isExplicit = entry.getValue();

            // find arg1
            DiscourseArgument arg1 = null;
            for (DiscourseArgument discourseArgument : discourseArguments) {
                if (discourseArgument.getParentRelationId() == relationId &&
                        discourseArgument.getArgumentNumber() == 1) {
                    arg1 = discourseArgument;
                    break;
                }
            }

            // find arg2
            DiscourseArgument arg2 = null;
            for (DiscourseArgument discourseArgument : discourseArguments) {
                if (discourseArgument.getParentRelationId() == relationId &&
                        discourseArgument.getArgumentNumber() == 2) {
                    arg2 = discourseArgument;
                    break;
                }
            }

            if (arg1 != null && arg2 != null) {
                // check if they are properly annotated
                if (arg1.getEnd() > 0 && arg2.getEnd() > 0) {
                    DiscourseRelation discourseRelation;

                    // create either explicit or implicit relation
                    if (isExplicit) {
                        discourseRelation = new ExplicitDiscourseRelation(jCas);

                        // so try to find all relevant connectives
                        List<DiscourseConnective> connectives = new ArrayList<>();
                        for (DiscourseConnective connective : discourseConnectives) {
                            if (connective.getParentRelationId() == relationId) {
                                connectives.add(connective);
                            }
                        }

                        // sort ascending
                        Collections.sort(connectives, new Comparator<DiscourseConnective>()
                        {
                            @Override public int compare(DiscourseConnective o1,
                                    DiscourseConnective o2)
                            {
                                return Integer.compare(o1.getBegin(), o2.getBegin());
                            }
                        });

                        if (connectives.size() > 2) {
                            System.err.println("More than 2 connectives: " + connectives);
                        }

                        // add the first connective
                        if (connectives.size() == 1 || connectives.size() == 2) {
                            DiscourseConnective connective1 = connectives.get(0);
                            ((ExplicitDiscourseRelation) discourseRelation)
                                    .setDiscourseConnective1(connective1);
                            connective1.addToIndexes();
                        }

                        // add the second connective
                        if (connectives.size() == 2) {
                            DiscourseConnective connective2 = connectives.get(0);
                            ((ExplicitDiscourseRelation) discourseRelation)
                                    .setDiscourseConnective2(connective2);
                            connective2.addToIndexes();
                        }

                    }
                    else {
                        discourseRelation = new ImplicitDiscourseRelation(jCas);
                    }
                    discourseRelation.setRelationId(relationId);
                    discourseRelation.setArg1(arg1);
                    discourseRelation.setArg2(arg2);

                    int begin =
                            arg1.getBegin() < arg2.getBegin() ? arg1.getBegin() : arg2.getBegin();
                    int end = arg1.getEnd() > arg2.getEnd() ? arg1.getEnd() : arg2.getEnd();

                    discourseRelation.setBegin(begin);
                    discourseRelation.setEnd(end);

                    arg1.addToIndexes();
                    arg2.addToIndexes();
                    discourseRelation.addToIndexes();
                }
                else {
                    System.err.println("Both arguments must be properly closed");
                }
            }
            else {
                System.err.println("Cannot find both arguments to relation " + relationId);
            }
        }

        // annotate all non-null Attributions
        for (DiscourseAttribution attribution : discourseAttributions) {
            if (attribution.getEnd() > 0) {
                attribution.addToIndexes();
            }
        }

        if (verbose) {
            for (DiscourseRelation discourseRelation : JCasUtil
                    .select(jCas, DiscourseRelation.class)) {
                DiscourseDumpWriter.debugRelation(discourseRelation);
            }
        }
    }

    private static DiscourseAttribution findDiscourseAttribution(
            List<DiscourseAttribution> list,
            Integer attrId)
    {
        DiscourseAttribution result = null;

        for (DiscourseAttribution attribution : list) {
            if (attrId.equals(attribution.getAttributeId())) {
                // we found it
                if (result != null) {
                    throw new IllegalStateException(
                            "Two same attributions are in the list with attrID " + attrId);
                }

                result = attribution;
            }
        }

        if (result == null) {
            System.out.println("Cannot look-up opening attribution for relationId: " + attrId);
        }

        return result;
    }

    /**
     * Looks-up existing discourse argument from the list given the parameters
     *
     * @param list           list
     * @param relationId     relation id
     * @param argumentNumber argument number
     * @return discourse argument (never null)
     * @throws java.lang.IllegalStateException if the discourse argument is not in list or there
     *                                         are more than one with with the parameters
     */
    private static DiscourseArgument findDiscourseArgument(
            List<DiscourseArgument> list, Integer relationId, Integer argumentNumber)
    {
        DiscourseArgument result = null;

        for (DiscourseArgument argument : list) {
            if (relationId.equals(argument.getParentRelationId()) && argumentNumber.equals(
                    argument.getArgumentNumber())) {
                // we found it
                if (result != null) {
                    throw new IllegalStateException(
                            "Two same arguments are in the list with relationId " + relationId
                                    + " and argumentNumber " + argumentNumber);
                }

                result = argument;
            }
        }

        if (result == null) {
            System.out.println("Cannot look-up opening argument for relationId: " + relationId
                    + ", argumentNumber: " + argumentNumber);
            //            System.out.println(list);
            //            throw new IllegalArgumentException("No such argument found in list.");
        }

        return result;
    }

    /**
     * Looks-up existing connective from the list given the parameters
     *
     * @param list           list
     * @param relationId     relation id
     * @param connectiveType connective type
     * @return existing discourse connective from the list
     * @throws java.lang.IllegalStateException if the discourse connective is not in list or there
     *                                         are more than one with with the parameters
     */
    static DiscourseConnective findLastConnective(List<DiscourseConnective> list,
            Integer relationId, String connectiveType)
            throws IllegalStateException
    {
        List<DiscourseConnective> connectives = new ArrayList<>();

        for (DiscourseConnective connective : list) {
            if (relationId.equals(connective.getParentRelationId()) && connectiveType
                    .equals(connective.getConnectiveType())) {
                // we found it
                //                if (result != null) {
                //                    throw new IllegalStateException(
                //                            "Two same connectives are in the list with relationId " + relationId
                //                                    + " and connectiveType " + connectiveType);
                //                }

                connectives.add(connective);
            }
        }

        if (connectives.size() > 1) {
            System.out.println("Multiple connectives found: " + connectives);
        }

        return connectives.get(connectives.size() - 1);
    }

    /**
     * Tries to match tokens from original annotations to the tokens that appear as xml
     * elements in XML paragraph
     *
     * @param paragraph    original text paragraph
     * @param xmlParagraph xml paragraph; will be augmented (removing/adding elements)
     */
    private static void recreateTokensInXMLParagraph(Paragraph paragraph, Element xmlParagraph)
    {
        Elements elements = xmlParagraph.getChildElements();
        List<Token> tokens = JCasUtil.selectCovered(Token.class, paragraph);

        if (elements.size() == 0) {
            //            System.err.println("No tokens found in paragraph" + xmlParagraph.toXML());
            return;
        }

        //        System.out.println(elements);

        List<Element> newElements = new ArrayList<>();

        int currentPositionElements = 0;
        int currentPositionTokens = 0;
        while (currentPositionElements < elements.size()) {
            Element element = elements.get(currentPositionElements);

            // mapping tokens
            if (element.getLocalName().equals("token") && currentPositionTokens < tokens.size()) {

                String currentTokenOrig = tokens.get(currentPositionTokens).getCoveredText();
                String currentToken = mapOrigTokenToPDTBXML(
                        tokens.get(currentPositionTokens).getCoveredText());

                String nextTokenOrig = null;
                String nextToken = null;
                if (currentPositionTokens < tokens.size() - 1) {
                    nextTokenOrig = tokens.get(currentPositionTokens + 1).getCoveredText();
                    nextToken = mapOrigTokenToPDTBXML(
                            tokens.get(currentPositionTokens + 1).getCoveredText());
                }

                String currentTokenXML = elements.get(currentPositionElements).getAttributeValue(
                        VAL);

                String nextTokenXML = null;
                if (currentPositionElements < elements.size() - 1) {
                    nextTokenXML = elements.get(currentPositionElements + 1).getAttributeValue(VAL);
                }

                if (currentToken.equals(currentTokenXML)) {
                    // we're good
                    newElements.add(new Element(element));
                }
                else if (currentToken.equals(currentTokenXML + nextTokenXML)) {
                    // was split in xml
                    // remove attribute
                    Element mergedElement = new Element("token");
                    mergedElement.addAttribute(new Attribute(VAL, currentTokenOrig));
                    newElements.add(mergedElement);
                    currentPositionElements++;
                }
                else if ((currentToken + nextToken).equals(currentTokenXML)) {
                    // was split in annotations
                    Element newElement1 = new Element("token");
                    newElement1.addAttribute(new Attribute(VAL, currentTokenOrig));
                    newElements.add(newElement1);

                    Element newElement2 = new Element("token");
                    newElement2.addAttribute(new Attribute(VAL, nextTokenOrig));
                    newElements.add(newElement2);

                    currentPositionTokens++;
                }
                else {
                    System.err.println(
                            "Unknown state (curr): " + currentToken + " -> " + currentTokenXML);
                    System.err
                            .println("Unknown state (next): " + nextToken + " -> " + nextTokenXML);
                }

                currentPositionTokens++;
            }
            else {
                newElements.add(new Element(element));
            }

            currentPositionElements++;
        }

        xmlParagraph.removeChildren();
        for (Element element : newElements) {
            xmlParagraph.appendChild(element);
        }

        //        System.out.println(newElements);
    }

    private static String mapOrigTokenToPDTBXML(String s)
    {
        final Map<String, String> mappingOrigToPDTB = new HashMap<>();
        mappingOrigToPDTB.put("(", "-LRB-");
        mappingOrigToPDTB.put(")", "-RRB-");
        mappingOrigToPDTB.put("–", "--");
        mappingOrigToPDTB.put("\"", "''");
        mappingOrigToPDTB.put("favour", "favor");
        mappingOrigToPDTB.put("’", "'");
        mappingOrigToPDTB.put("‘", "`");
        mappingOrigToPDTB.put("/", "\\/");
        mappingOrigToPDTB.put("travelled", "traveled");
        mappingOrigToPDTB.put("colour", "color");
        mappingOrigToPDTB.put("“", "``");
        mappingOrigToPDTB.put("—", "--");
        mappingOrigToPDTB.put("…", "...");
        mappingOrigToPDTB.put("”", "''");
        mappingOrigToPDTB.put("[", "-LRB-");
        mappingOrigToPDTB.put("]", "-RRB-");
        mappingOrigToPDTB.put("______", "------");
        mappingOrigToPDTB.put("_______", "-------");
        mappingOrigToPDTB.put("*", "\\*");
        mappingOrigToPDTB.put("**", "\\*\\*");
        mappingOrigToPDTB.put("***", "\\*\\*\\*");
        mappingOrigToPDTB.put("behaviour", "behavior");
        mappingOrigToPDTB.put("practise", "practice");
        mappingOrigToPDTB.put("―", "--");

        return mappingOrigToPDTB.containsKey(s) ? mappingOrigToPDTB.get(s) : s;
    }

    /**
     * Returns a list of paragraphs that contains a list of sentences
     *
     * @param jCas jcas
     * @return string
     */
    private static List<String> extractParagraphs(JCas jCas)
    {
        List<String> result = new ArrayList<>();
        for (Paragraph paragraph : JCasUtil.select(jCas, Paragraph.class)) {

            // joint tokens again with spaces
            List<String> tokens = new ArrayList<>();
            for (Token token : JCasUtil.selectCovered(Token.class, paragraph)) {
                tokens.add(token.getCoveredText());
            }
            result.add(StringUtils.join(tokens, " "));
        }

        return result;
    }

    /**
     * Each sentence on line, paragraphs separated by en empty line
     *
     * @param paragraphsAndSentences document
     * @return string
     */
    private static String prepareFileForPDTB(List<String> paragraphsAndSentences)
    {
        return StringUtils.join(paragraphsAndSentences, "\n\n");
    }

    /**
     * Creates a temporary files, writes the text into it and returns the files
     *
     * @param text text
     * @return file
     * @throws IOException
     */
    private static File writeToTempFile(String text, String documentId)
            throws IOException
    {
        File result = File.createTempFile("tmp" + documentId + "_", ".txt");
        FileUtils.writeStringToFile(result, text);
        return result;
    }

    @Override public void collectionProcessComplete()
            throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        try {
            wrapper.clean();
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }


    /*
    public static Document readXMLWithJSoupNormalizing(InputStream inputStream)
            throws IOException, SAXException
    {
        org.jsoup.nodes.Document parse = Jsoup.parse(inputStream, "utf-8", "");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(outputStream);
        ps.print(parse.toString());

        //        System.out.println(parse.outerHtml());

        return PDTBDiscourseAnnotator.readXML(new ByteArrayInputStream(outputStream.toByteArray()));
    }
    */

    /*
    public static void printXMLDocument(Document doc, OutputStream out)
            throws IOException
    {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            transformer.transform(new DOMSource(doc),
                    new StreamResult(new OutputStreamWriter(out, "UTF-8")));
        }
        catch (TransformerException e) {
            throw new IOException(e);
        }
    }
    */

    public static void main(String[] args)
            throws Exception
    {
        String path = "/home/user-ukp/research/data/argumentation/argumentation-gold-prelabeled";
        String outputDir = "/home/user-ukp/research/data/argumentation/argumentation-gold-prelabeled-discourse";

        SimplePipeline.runPipeline(CollectionReaderFactory.createReaderDescription(
                        XmiReader.class,
                        XmiReader.PARAM_LENIENT, false,
                        XmiReader.PARAM_SOURCE_LOCATION, path,
                        //                        XmiReader.PARAM_PATTERNS, XmiReader.INCLUDE_PREFIX + "*.xmi"),
                        //                        XmiReader.PARAM_PATTERNS, XmiReader.INCLUDE_PREFIX + "1021.xmi"),
                        //                                        XmiReader.PARAM_PATTERNS, XmiReader.INCLUDE_PREFIX + "1037.xmi"),
                        //                                        XmiReader.PARAM_PATTERNS, XmiReader.INCLUDE_PREFIX + "3556.xmi"),
                        //                                        XmiReader.PARAM_PATTERNS, XmiReader.INCLUDE_PREFIX + "1487.xmi"),
                        XmiReader.PARAM_PATTERNS, XmiReader.INCLUDE_PREFIX + "*.xmi"),
                AnalysisEngineFactory.createEngineDescription(PDTBDiscourseAnnotator.class),
                AnalysisEngineFactory.createEngineDescription(
                        XmiWriter.class,
                        XmiWriter.PARAM_TARGET_LOCATION, outputDir
                )
        );
    }

    public static void main2(String[] args)
            throws Exception
    {
        final String corpusFilePathTrain = "/home/user-ukp/research/data/argumentation/argumentation-gold-prelabeled-discourse-test";
        SimplePipeline.runPipeline(
                CollectionReaderFactory.createReaderDescription(XmiReader.class,
                        XmiReader.PARAM_LENIENT, false,
                        XmiReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
                        XmiReader.PARAM_PATTERNS, XmiReader.INCLUDE_PREFIX + "*.xmi"
                ),
                AnalysisEngineFactory.createEngineDescription(DiscourseDumpWriter.class)
        );
    }
}
