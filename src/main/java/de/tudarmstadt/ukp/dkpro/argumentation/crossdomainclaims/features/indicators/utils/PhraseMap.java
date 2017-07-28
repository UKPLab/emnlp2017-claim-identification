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

package de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.features.indicators.utils;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.*;

/**
 * @author Christian Stab
 */
public class PhraseMap
{

    private Set<String> phrases;

    private boolean caseSensitive;

    public PhraseMap(String path, boolean caseSensitive)
    {
        phrases = new HashSet<String>();
        this.caseSensitive = caseSensitive;
        try {
            File file = new File(path);
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    phrases.add((this.caseSensitive) ? line : line.toLowerCase());
                }
                br.close();
            } else {
                // fallback to resource
                InputStream resourceAsStream = this.getClass().getClassLoader()
                        .getResourceAsStream(path);
                if (resourceAsStream == null) {
                    throw new RuntimeException(new IOException("Cannot find resource " + path));
                }

                List<String> lines = IOUtils.readLines(resourceAsStream);
                for (String line : lines) {
                    phrases.add((this.caseSensitive) ? line : line.toLowerCase());
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, Integer> getPhrases(Collection<Token> tokens)
    {
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        int position = -1;
        for (String con : phrases) {
            if ((position = containsTokenSequence(tokens, con, caseSensitive)) > -1) {
                result.put(con, position);
            }

        }
        return result;
    }

    public Set<String> getPhrases()
    {
        return phrases;
    }

    public static boolean startsWithUpperCase(Collection<Token> tokens, int index)
    {
        Token t = (Token) tokens.toArray()[index];
        return Character.isUpperCase(t.getCoveredText().charAt(0));
    }

    private int containsTokenSequence(Collection<Token> tokens, String sequence,
            boolean caseSensitive)
    {
        String[] split = (caseSensitive) ? sequence.split(" ") : sequence.toLowerCase().split(" ");
        int position = 0;
        int conIndex = 0;
        int sequenceIndex = 0;
        for (Token t : tokens) {
            String tokenText = (caseSensitive) ?
                    t.getCoveredText() :
                    t.getCoveredText().toLowerCase();
            if (tokenText.equals(split[sequenceIndex])) {
                if (sequenceIndex == 0)
                    conIndex = position;
                sequenceIndex++;
            }
            else
                sequenceIndex = 0;
            if (sequenceIndex == split.length) {
                return conIndex;
            }
            position++;
        }

        return -1;
    }

}
