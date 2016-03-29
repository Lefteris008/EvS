///*
// * Copyright (C) 2016 Lefteris Paraskevas
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//package preprocessingmodule;
//
//import utilities.Config;
//import dsretriever.TweetsRetriever;
//import dsretriever.Tweet;
//import dsretriever.MongoHandler;
//import com.mongodb.MongoException;
//import edmodule.EDMethodPicker;
//import java.io.BufferedReader;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
//import java.nio.charset.Charset;
//import java.nio.file.Files;
//import java.nio.file.LinkOption;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.StandardOpenOption;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Scanner;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import preprocessingmodule.nlp.Tokenizer;
//import preprocessingmodule.language.LangUtils;
//import preprocessingmodule.nlp.stopwords.StopWords;
//import evs.EvS;
//import samodule.EmoticonsAnnotator;
//import samodule.SentimentAnalyzer;
//import samodule.SentimentAnnotator;
//import utilities.Utilities;
//
///**
// *
// * @author  Lefteris Paraskevas
// * @version 2016.03.16_1214
// */
//public class PreProcessor {
//    
//    public final static boolean showMongoLogging = false;
//    
//    /**
//     * @param args the command line arguments
//     * @throws java.io.IOException
//     */
//    public final static void main(String[] args) throws IOException {
//        
//        if(!showMongoLogging) {
//            //Stop reporting logging information
//            Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
//            mongoLogger.setLevel(Level.SEVERE);
//        }
//
//        int choice;
//        Config config = new Config(); //Create the configuration object
//        
//        System.out.println("Select one of the following options");
//        System.out.println("1.\tTest a tweet");
//        System.out.println("\tGet a specific tweet from MongoDB Store and test the preprocessing procedure.\n");
//        System.out.println("2.\tApply Event Detection");
//        System.out.println("\tApply your desired ED algorithm.\n");
//        System.out.println("3.\tApply Event Detection combining Sentiment Analysis");
//        System.out.println("\tMain option of project.\n");
//        System.out.print("Your choice: ");
//        
//        Scanner keyboard = new Scanner(System.in);
//        choice = keyboard.nextInt();
//        
//        switch(choice) {
//            case 1: {
//                MongoHandler mongoDB = new MongoHandler(config);
//                try {
//                    //Establish the connection
//                    mongoDB.connectToMongoDB();
//
//                    System.out.print("Type in the ID you want to search for: ");
//                    long id = keyboard.nextLong();
//
//                    //Get tweet and print its data
//                    System.out.println("Test search for tweet with ID: '"+ id + "'");
//                    Tweet tweet = mongoDB.getATweetByIdFromMongoDBStore(id);
//                    tweet.printTweetData();
//
//                    //Sentiment part
//                    SentimentAnalyzer.initAnalyzer(true);
//                    System.out.println("Sentiment: " + SentimentAnalyzer.getSentiment(SentimentAnalyzer.getSentimentOfSentence(tweet.getText())));
//                    SentimentAnalyzer.postActions(true);
//
//                    //Preprocess part
//                    StopWords sw = new StopWords(config);
//                    sw.loadStopWords(LangUtils.getLangISOFromString(tweet.getLanguage())); //Load stopwords
//                    Tokenizer tk = new Tokenizer(config, tweet.getText(), sw);
//                    tk.textTokenizingTester();
//                    break;
//                } catch(MongoException e) {
//                    System.out.println("Error: Can't establish a connection with MongoDB");
//                    Logger.getLogger(PreProcessor.class.getName()).log(Level.SEVERE, null, e);
//                    mongoDB.closeMongoConnection(); //Close DB
//                } 
//            } case 2: {
//                EDMethodPicker.selectEDMethod(config);
//                break;
//            } case 3: {
//                EvS saVed = new EvS(config);
//                
//                break;
//            } case 4: {
//                MongoHandler mongo = new MongoHandler(config);
//                mongo.connectToMongoDB();
//                List<Tweet> tweets = mongo.retrieveAllTweetsFiltered();
//                Utilities.printMessageln("Starting calculating sentiment annotations...");
//                countSentimentAnnotatedTweets(config, mongo, tweets);
//                mongo.closeMongoConnection();
//                break;
//            } case 5: {
//                MongoHandler mongo = new MongoHandler(config);
//                mongo.connectToMongoDB();
//                List<Tweet> tweets = mongo.retrieveAllTweetsFiltered();
//                int i = 0;
//                Utilities.printMessageln("Starting calculating sentiment.");
//                SentimentAnalyzer.initAnalyzer(true);
//                int sentiment;
//                for(Tweet tweet : tweets) {
//                    if(!mongo.tweetHasSentiment(tweet.getID(), config.getStanfordSentimentName())) {
//                        sentiment = SentimentAnalyzer.getSentimentOfSentence(tweet.getText());
//                        mongo.updateSentiment(tweet.getID(), sentiment, config.getStanfordSentimentName());
//                    }
//                }
//                SentimentAnalyzer.postActions(true);
//                mongo.closeMongoConnection();
//                break;
//            } case 6: {
//                List<String> lines = new ArrayList<>();
//                MongoHandler mongo = new MongoHandler(config);
//                mongo.connectToMongoDB();
//                List<Tweet> tweets = new ArrayList<>(mongo.retrieveAllTweetsFiltered());
//                mongo.closeMongoConnection();
//                int posEmot, negEmot;
//                for(Tweet tweet : tweets) {
//                    posEmot = tweet.getPositiveEmoticonFlag();
//                    negEmot = tweet.getNegativeEmoticonFlag();
//                    lines.add("\"" + tweet.getText().replace("\n", " ").replace("\r", " ").replace("\"", "'")
//                            + "\"," + posEmot + "," + negEmot + "," + tweet.getID() + ",?");
//                }
//                String path = "C:\\sentiment\\train_emoticons_ids.arff";
//                Path filePath = Paths.get(path);
//                Files.write(filePath, lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
//                break;
//            } case 7 : {
//                MongoHandler mongo = new MongoHandler(config);
//                mongo.connectToMongoDB();
//                List<Tweet> tweets = new ArrayList<>(mongo.retrieveAllTweetsFiltered());
//                EmoticonsAnnotator emAn = new EmoticonsAnnotator(config);
//                int negativeEmoticon, positiveEmoticon;
//                Utilities.printMessageln("Calculating emoticon info.");
//                for(Tweet tweet : tweets) {
//                    Tokenizer tk = new Tokenizer(config, tweet.getText());
//                    negativeEmoticon = (emAn.containsNegativeEmoticon(tk.getCleanTokens()) ? 1 : 0);
//                    positiveEmoticon = (emAn.containsPositiveEmoticon(tk.getCleanTokens()) ? 1 : 0);
//                    mongo.updateTweetWithEmoticonInfo(tweet.getID(), positiveEmoticon, negativeEmoticon);
//                }
//                mongo.closeMongoConnection();
//                break;
//            } case 8: {
//                List<String> lines = new ArrayList<>();
//                EmoticonsAnnotator emAn = new EmoticonsAnnotator(config);
//                int negativeEmoticon, positiveEmoticon;
//                String path = "C:\\sanders\\train.arff";
//                BufferedReader in = new BufferedReader(new FileReader(path));
//                String line;
//                for(int i = 0; i < 6; i++) {
//                    in.readLine();
//                }
//                try {
//                    while((line = in.readLine()) != null) {
//                        String tmpLine = line.substring(1, line.indexOf(("\""), 2));
//                        Tokenizer tk = new Tokenizer(config, tmpLine);
//                        negativeEmoticon = (emAn.containsNegativeEmoticon(tk.getCleanTokens()) ? 1 : 0);
//                        positiveEmoticon = (emAn.containsPositiveEmoticon(tk.getCleanTokens()) ? 1 : 0);
//                        lines.add("\"" + tmpLine + "\"," + positiveEmoticon + "," 
//                                + negativeEmoticon + ",0," + line.substring(line.indexOf(("\""), 2) + 2));
//                    }
//                } catch(StringIndexOutOfBoundsException e) {
//                    
//                }
//                Path filePath = Paths.get("C:\\sentiment\\test_with_emoticons.txt");
//                Files.write(filePath, lines);
//                in.close();
//                break;
//            } case 9: {
//                MongoHandler mongo = new MongoHandler(config);
//                mongo.connectToMongoDB();
//                List<Tweet> tweets = mongo.retrieveAllTweetsFiltered();
//                SentimentAnnotator sA = new SentimentAnnotator(config, tweets, mongo);
//                sA.annotateWithNaiveBayesSentiment();
//                mongo.closeMongoConnection();
//                break;
//            } case 10: {
//                String header = "C:\\sentiment\\10.03.2016\\header.txt";
//                BufferedReader headerIn = new BufferedReader(new FileReader(header));
//                String line;
//                List<String> headerLines = new ArrayList<>();
//                while((line = headerIn.readLine()) != null) {
//                    headerLines.add(line);
//                }
//                
//                String path = "C:\\sentiment\\10.03.2016\\test.txt";
//                BufferedReader in = new BufferedReader(new FileReader(path));
//                int counter = 0;
//                int masterCounter = 0;
//                List<String> lines = new ArrayList<>();
//                List<String> filenames = new ArrayList<String>() {{
//                    add("01"); add("02"); add("03"); add("04"); add("05");
//                    add("06"); add("07"); add("08"); add("09"); add("10");
//                    add("11"); add("12"); add("13"); add("14"); add("15");
//                    add("16"); add("17"); add("18"); add("19"); add("20");
//                    add("21"); add("22"); add("23"); add("24"); add("25");
//                    add("26"); add("27"); add("28"); add("29"); add("30");
//                    add("31");
//                }};
//                int i = 0;
//                while((line = in.readLine()) != null) {
//                    if(counter != 19390) {
//                        lines.add(line);
//                        masterCounter++;
//                        counter++;
//                    } else {
//                        counter = 0;
//                        Path filename = Paths.get("C:\\sentiment\\10.03.2016\\test\\" + filenames.get(i) + ".arff");
//                        i++;
//                        if (!Files.exists(filename, LinkOption.NOFOLLOW_LINKS))
//                            Files.createFile(filename);
//                        Files.write(filename, headerLines, StandardOpenOption.APPEND);
//                        Files.write(filename, lines, StandardOpenOption.APPEND);
//                        lines = new ArrayList<>();
//                        lines.add(line);
//                        counter++;
//                        masterCounter++;
//                    }
//                }
//                Path filename = Paths.get("C:\\sentiment\\10.03.2016\\test\\" + filenames.get(i) + ".arff");
//                if (!Files.exists(filename, LinkOption.NOFOLLOW_LINKS))
//                    Files.createFile(filename);
//                Files.write(filename, headerLines, StandardOpenOption.APPEND);
//                Files.write(filename, lines, StandardOpenOption.APPEND);
//                break;
//            } default : {
//                System.out.println("Wrong choice. Exiting now...");
//            }
//        }
//    }
//    
//    public final static List<Integer> getSentiment() throws FileNotFoundException, IOException {
//        List<Integer> sentiments = new ArrayList<>();
//        String filePath = "C:\\Users\\Lefteris\\Desktop\\output.txt";
//        BufferedReader in = new BufferedReader(new FileReader(filePath));
//        String line;
//        while((line = in.readLine()) != null) {
//            if(line.equals("01:00")) {
//                sentiments.add(0);
//            } else {
//                sentiments.add(1);
//            }
//        }
//        return sentiments;
//    }
//    
//    /**
//     * Method to extract the ground truth events and then retrieve historical tweets by their IDs.
//     * @param config A configuration object
//     * @param mongoDB A MongoDB object
//     */
//    public final static void retrieveByID(Config config, MongoHandler mongoDB) {
//        List<String> event1List = Utilities.extractTweetIDsFromFile(config, "fa_cup");
//        List<String> event2List = Utilities.extractTweetIDsFromFile(config, "super_tuesday");
//        List<String> event3List = Utilities.extractTweetIDsFromFile(config, "us_elections");
//
//        new TweetsRetriever().retrieveTweetsById(event1List, mongoDB, config, "FA Cup");
//        new TweetsRetriever().retrieveTweetsById(event2List, mongoDB, config, "Super Tuesday");
//        new TweetsRetriever().retrieveTweetsById(event3List, mongoDB, config, "US Elections");
//    }
//    
//    /**
//     * Method that retrieves real-time tweets by querying the API with specific search terms.
//     * @param config A configuration object
//     * @param mongoDB A mongoDB object
//     * @param keywords A String array with the search terms
//     */
//    public final static void retrieveByStreamingAPI(Config config, MongoHandler mongoDB, String[] keywords) {
//        new TweetsRetriever().retrieveTweetsWithStreamingAPI(keywords, mongoDB, config); //Run the streamer
//    }
//    
//    private static void countSentimentAnnotatedTweets(Config config, MongoHandler mongo, List<Tweet> tweets) {
//        int counter = 0;
//        for(Tweet tweet : tweets) {
//            if(mongo.tweetHasSentiment(tweet.getID(), config.getStanfordSentimentName())) {
//                counter++;
//            }
//        }
//        Utilities.printMessageln("Total sentiment annotated tweets: " + counter);
//    }
//    
//    
//}
