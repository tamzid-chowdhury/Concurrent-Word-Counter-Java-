import java.nio.file.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.Scanner;
import java.util.TreeMap;
import java.io.*; 
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class WordCounter {

        // The following are the ONLY variables we will modify for grading.
        // The rest of your code must run with no changes.
        public static final Path FOLDER_OF_TEXT_FILES  = Paths.get("texts"); // path to the folder where input text files are located
        public static final Path WORD_COUNT_TABLE_FILE = Paths.get("singlethread.txt"); // path to the output plain-text (.txt) file
        public static int  NUMBER_OF_THREADS;   // max. number of threads to spawn 
    	static private Comparator<File> fileName;
        static {
            fileName = new Comparator<File>(){
                @Override
                public int compare(final File f1, final File f2){
                    return f1.getName().compareTo(f2.getName());
                }
            };
        }
        
    	//will contain total counts
    	static TreeMap<String,Integer> totalCount = new TreeMap<String,Integer>();
    	static File[] textFiles = FOLDER_OF_TEXT_FILES.toFile().listFiles(); 
    	@SuppressWarnings("unchecked")
		static TreeMap<String,Integer>[] inputMaps = (TreeMap<String, Integer>[]) new TreeMap[textFiles.length]; //will contain counts for each file
        static int count = 0; 
    	
    	
        public static void main(final String[] args) throws IOException {
            // your implementation of how to run the WordCounter as a stand-alone multi-threaded program
			final long start = System.currentTimeMillis();
			Scanner sc = new Scanner(System.in);
			System.out.print("How many threads: "); 

        	int numOfThreads = sc.nextInt();
        	   	   	
        	Arrays.sort(textFiles, fileName); //sort files by fileName for printing output at the end  
        	final ExecutorService executor = Executors.newFixedThreadPool(numOfThreads);
        	
        	
        	//SINGLE THREADED EXECUTION
        	if(numOfThreads == 0 || numOfThreads == 1) {
            	for(int i = 0; i < textFiles.length; i++) {
            		final File inputFile = textFiles[i];
            		getWordCount(inputFile,i); 
            	}
            	System.out.println(printTable(inputMaps,totalCount,textFiles));      	  
            	Files.writeString(WORD_COUNT_TABLE_FILE, printTable(inputMaps, totalCount, textFiles));  
            	final long end = System.currentTimeMillis();
            	final float sec = (end - start); System.out.println("Single Threaded: " + sec + " milliseconds");
        	}
        	
        	//MULTI THREADED EXECUTION
        	else {        
                for (; count < textFiles.length; count++) {
                   
                    final Runnable reader = new Reader(count);
                    executor.execute(reader);
                  }
                executor.shutdown();
                while (!executor.isTerminated()) {

					
                }
                System.out.println(printTable(inputMaps,totalCount,textFiles));  
                Files.writeString(WORD_COUNT_TABLE_FILE, printTable(inputMaps, totalCount, textFiles));        		
            	final long end = System.currentTimeMillis();
            	final float sec = (end - start); System.out.println("MultiThreaded: " + sec + " milliseconds");
			}
			
			sc.close();

        	
        }
        
        public static synchronized void getWordCount(final File inputFile, final int index) throws FileNotFoundException{
    		final TreeMap<String,Integer> wordCount = new TreeMap<String,Integer>();
    		final Scanner scRead = new Scanner(inputFile);
        	while(scRead.hasNext()) {
        		String word = scRead.next();
        		word = word.toLowerCase();
        		word = word.replaceAll("[.,:;!?]","");
        		if(wordCount.containsKey(word)) {
        			wordCount.put(word, wordCount.get(word)+1);
        		}
        		else {
        			wordCount.put(word, 1);
        		}
        		if(totalCount.containsKey(word)) {
        			totalCount.put(word, totalCount.get(word)+1);
        		}
        		else {
        			totalCount.put(word, 1);
        		}
        	}
        	scRead.close();
        	inputMaps[index] = wordCount;
        	
        }
        
        public static String printTable(final TreeMap<String,Integer>[] inputs, final TreeMap<String,Integer> total, final File[] textFiles) {
        	String table = "";
        	final int longestWord = longestWordLength(total);
        	String header = String.format("%"+(longestWord+1)+"s"," ");
        	for(int i = 0; i < textFiles.length; i++) {
        		header += textFiles[i].getName().replace(".txt","") + " ";
        	}
        	header += "total";
        	table += header + "\n"; 
        	final NavigableSet<String> words = totalCount.navigableKeySet();
        	for(final String word: words) {
        		String row = String.format("%-"+(longestWord+1)+"s",word);
        		for(int i = 0; i < inputs.length; i++) {
        			row += String.format("%-"+(textFiles[i].getName().replace(".txt","").length()+1)+"s",inputs[i].getOrDefault(word, 0));
        		}
        		row += total.getOrDefault(word, 0);
        		table += row + "\n";
        	}
        	
        	return table; 
        }
        
        public static int longestWordLength(final TreeMap<String,Integer> totalCount) {
        	int longest = 0; 
        	final NavigableSet<String> words = totalCount.navigableKeySet();
        	for(final String word: words) {
        		if(word.length() > longest) {
        			longest = word.length();
        		}
        	}
        	return longest; 
        }
        
        static class Reader implements Runnable {
        	private final int i; 
        	
        	public Reader(final int i) {
        		this.i = i; 
        	}

			@Override
			public void run() {
				try {
					getWordCount(textFiles[i],i);
				} catch (final FileNotFoundException e) {
					e.printStackTrace();
				}
				
			}
        	
        }
    }

