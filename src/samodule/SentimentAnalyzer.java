/*
 * Copyright (C) 2016 Lefteris Paraskevas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package samodule;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015_12_03_1656_planet2
 */
public class SentimentAnalyzer {
    private static StanfordCoreNLP pipeline;
    private static PrintStream errorStream;

    /**
     * Initializes the Stanford Core NLP for Sentiment Analysis.
     * @param hideLogging Boolean variable to indicate show/hide Error Stream.
     */
    public static void initAnalyzer(boolean hideLogging) {
        //Hide Error Stream
        if(hideLogging) {
            errorStream = System.err;
            System.setErr(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) {}
            }));
        }
        
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        pipeline = new StanfordCoreNLP(props);
    }

    /**
     * Returns the sentiment of a given String inputSentence in the English language.
     * @param inputSentence A String containing the sentence to be processed.
     * @return An integer in the range of [0-4] -the higher, the more positive.
     */
    public final static int getSentimentOfSentence(String inputSentence) {
        int mainSentiment = 0;
        if (inputSentence != null && inputSentence.length() > 0) {
            int longest = 0;
            Annotation annotation = pipeline.process(inputSentence);
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
                int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                String partText = sentence.toString();
                if (partText.length() > longest) {
                    mainSentiment = sentiment;
                    longest = partText.length();
                }
            }
        }
        return mainSentiment;
    }

    /**
     * Returns a String detailing the sentiment method getSentimentFromSentence() returned.
     * @param sentiment An integer in the range of [0-4].
     * @return A String with the description of the input sentiment.
     */
    public final static String getSentiment(int sentiment) {
        switch(sentiment) {
            case 0 : {
                return "Very Negative";
            } case 1 : {
                return "Negative";
            } case 2 : {
                return "Neutral";
            } case 3 : {
                return "Positive";
            } case 4 : {
                return "Very Positive";
            } default : {
                return "Input Sentiment Out of Bounds";
            }
        }
    }
    
    /**
     * Restores Error Stream in case it was previously shut down.
     * @param hideLogging Boolean variable to indicate show/hide logging information.
     */
    public final static void postActions(boolean hideLogging) {
        //Restore Error Stream
        if(hideLogging) {
            System.setErr(errorStream);
        }
    }
}
