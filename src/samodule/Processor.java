package samodule;

import java.util.LinkedList;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;


public class Processor {

	// Alter those three parameters for testing:
	static String main_folder = "C:\\Users\\Lefteris\\Documents\\NetBeansProjects\\sentiment-analysis\\src\\main\\resources\\";		// the path to the "resources" folder 
	static String test_dataset = "Cisco";						// available options for demo: goethe, Liebherr, Cisco
	static boolean useSlidingWindowForTraining = false;				// if set to "true", only the last 1,000 documents will be used for the training of the ensemble classifier
	
	public static void main(String[] args) throws Exception {
		
            LinkedList<String> lt = getData(test_dataset);				// read some data
            SentimentAnalyser analyser = new SentimentAnalyser(main_folder, useSlidingWindowForTraining, test_dataset);
            for (String lt1 : lt) {
                System.out.println(analyser.getPolarity(lt1)); // any text may be passed as an argument here
                //System.out.println(i+"\t"+out);
            }
	}
	
	private static LinkedList<String> getData(String f){
		LinkedList<String> all_tweets = new LinkedList<String>();
		DataSource ds;
		Instances data;
		try {
                    ds = new DataSource(main_folder+"test_sets/"+f+".arff");

                    data =  ds.getDataSet();
                    for (int i=0; i < data.numInstances(); i++){
                        all_tweets.add(data.get(i).stringValue(0));
                    }
                    return all_tweets;
		} catch (Exception e) {
                    System.out.println("File not found.");
                    return null;
		}
		
	}
}