package sf.eval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sf.SFEntity;

public class MistakeBreakdown {

	public static final String TEST_DATA_PATH = "test-data/training-2010/";
	public static final String KEY_FILE = TEST_DATA_PATH + "annotations.gold";
	public static final String PRED_FILE = TEST_DATA_PATH + "annotations.pred";
	public static final String QUERY_FILE = TEST_DATA_PATH + "queries.xml";
	public static final String CORPUS_PATH = "corpus-samples/only-answers/";
	public static final String META_FILE = CORPUS_PATH + "sentences.meta";
	public static final String TEXT_FILE = CORPUS_PATH + "sentences.text";
	
	// from key:	query:slot:answer --> trigger:doc
	static Map<String, String> keyAnswers = new HashMap<String, String>();
	
	// from pred:	query:slot:answer --> answer:doc
	static Map<String, String> predAnswers = new HashMap<String, String>();
	
	// relevant documents:	doc --> set(answer)
	static Map<String, Set<String>> relevantDocs = new HashMap<String, Set<String>>();
	
	// relevant sentence ids:	doc --> List(sentence id)
	static Map<String, List<String>> relSentByDoc = new HashMap<String, List<String>>();
	
	// answers per sentence:	sentence id --> set(answer)
	static Map<String, Set<String>> sentAnswers = new HashMap<String, Set<String>>();
	
	// relevant sentences:	sentence id --> text
	static Map<String, String> sentText = new HashMap<String, String>();
	
	static sf.query.QueryReader queryReader = new sf.query.QueryReader();
	
	// query id --> query name
	static Map<String, String> queryName = new HashMap<String, String>();
	
	
	public static void main(String[] args) throws IOException {
		
		queryReader.readFrom(QUERY_FILE);
		for (SFEntity query : queryReader.queryList) {
			queryName.put(query.queryId, query.mentionString);
		}
		
		// Read in key judgements
		BufferedReader keyReader = null;
		try {
		    keyReader = new BufferedReader(new FileReader(new File(KEY_FILE)));
		} catch (FileNotFoundException e) {
		    System.out.println ("Unable to open key file");
		    System.exit (1);
		}
		String line;
		while ((line = keyReader.readLine()) != null) {
			String[] fields = line.split("\t", 11);
		    if (fields.length != 11) {
		    	System.out.println ("Invalid line in key file:");
		    	System.out.println (line);
		    	continue;
		    }
		    int judgement = Integer.parseInt(fields[10]);
		    if (judgement != 1)
		    	continue;
		    String queryId = fields[1];
		    String slotName = fields[3];
		    String docId = fields[4];
		    String answer = fields[8];
		    String trigger = fields[7];
		    
		    String key = queryId + ":" + slotName + ":" + answer;
		    String value = trigger + ":" + docId;
		    keyAnswers.put(key, value);
		}
		
		// Read in predictions
		BufferedReader predReader = null;
		try {
		    predReader = new BufferedReader(new FileReader(new File(PRED_FILE)));
		} catch (FileNotFoundException e) {
		    System.out.println ("Unable to open prediction file");
		    System.exit (1);
		}
		while ((line = predReader.readLine()) != null) {
			String[] fields = line.trim().split("\\s+", 5);
		    if (fields.length < 4 || fields.length > 5) {
		    	System.out.println ("Invalid line in response file:  " + fields.length + "fields");
		    	System.out.println (line);
		    	continue;
		    }
		    String docId = fields[3];
		    if (docId.equals("NIL"))
		    	continue;
		    String queryId = fields[0];
		    String slotName = fields[1];
		    String answer = fields.length == 5? fields[4] : "NIL";
		    
		    String key = queryId + ":" + slotName + ":" + answer;
		    if (keyAnswers.containsKey(key))
		    	keyAnswers.remove(key);
		    else {
		    	String value = answer + ":" + docId;
		    	predAnswers.put(key, value);
		    }
		}
		
		// determine relevant documents
		for (String key : keyAnswers.keySet()) {
			String[] ansDoc = keyAnswers.get(key).split(":", 2);
			if (relevantDocs.containsKey(ansDoc[1])) {
				relevantDocs.get(ansDoc[1]).add(ansDoc[0]);
			} else {
				Set<String> answers = new HashSet<String>();
				answers.add(ansDoc[0]);
				relevantDocs.put(ansDoc[1], answers);
			}
		}
		for (String key : predAnswers.keySet()) {
			String[] ansDoc = predAnswers.get(key).split(":", 2);
			if (relevantDocs.containsKey(ansDoc[1])) {
				relevantDocs.get(ansDoc[1]).add(ansDoc[0]);
			} else {
				Set<String> answers = new HashSet<String>();
				answers.add(ansDoc[0]);
				relevantDocs.put(ansDoc[1], answers);
			}			
		}
		
		// determine relevant sentences
		BufferedReader metaReader = null;
		try {
		    metaReader = new BufferedReader(new FileReader(new File(META_FILE)));
		} catch (FileNotFoundException e) {
		    System.out.println ("Unable to open sentences.meta file");
		    System.exit (1);
		}
		while ((line = metaReader.readLine()) != null) {
			String[] fields = line.split("\t");
		    if (fields.length != 5) {
		    	System.out.println ("Invalid line in meta file:");
		    	System.out.println (line);
		    	continue;
		    }
		    String doc = fields[2];
		    if (relevantDocs.containsKey(doc)) {
		    	String sid = fields[0];
		    	if (relSentByDoc.containsKey(doc)) {
		    		relSentByDoc.get(doc).add(sid);
		    	} else {
		    		List<String> sids = new ArrayList<String>();
		    		sids.add(sid);
		    		relSentByDoc.put(doc, sids);
		    	}
		    	
		    	if (sentAnswers.containsKey(sid)) {
		    		sentAnswers.get(sid).addAll(relevantDocs.get(doc));
		    	} else {
		    		sentAnswers.put(sid, relevantDocs.get(doc));
		    	}
		    }
		}
		
		// Collect relevant sentences
		BufferedReader textReader = null;
		try {
		    textReader = new BufferedReader(new FileReader(new File(TEXT_FILE)));
		} catch (FileNotFoundException e) {
		    System.out.println ("Unable to open sentences.text file");
		    System.exit (1);
		}
		while ((line = textReader.readLine()) != null) {		
			String[] fields = line.split("\\s+", 2);
			if (fields.length != 2) {
		    	System.out.println ("Invalid line in text file:");
		    	System.out.println (line);
		    	continue;
			}
			String sid = fields[0];
			String text = fields[1];
			if (sentAnswers.containsKey(sid)) {
				boolean found = false;
				for (String ans : sentAnswers.get(sid)) {
					if (text.contains(ans)) {
						found = true;
						break;
					}
				}
				if (found) {
					sentText.put(sid, text);
				}
			}
		}
		
		// Print Incorrect answers
		Set<String> delete = new HashSet<String>();
		System.out.println("Incorrect answers: (Affect both recall and precision)");
		for (String keyKey : keyAnswers.keySet()) {
			for (String predKey : predAnswers.keySet()) {
				String[] keyKeyParts = keyKey.split(":");
				String[] predKeyParts = predKey.split(":");
				if (keyKeyParts[0].equals(predKeyParts[0]) &&
						keyKeyParts[1].equals(predKeyParts[1]) &&
						keyKeyParts[2].equals(predKeyParts[2])) {
					System.out.println("\tQuery: " + queryName.get(keyKeyParts[0])); 
					System.out.println("\tSlot: " + keyKeyParts[1] + ":" + keyKeyParts[2]);
					System.out.println("\tKey's answer: " + keyKeyParts[3]);
					System.out.println("\tFound in sentences:");
					String[] keyAns = keyAnswers.get(keyKey).split(":",2);
					for (String sid : relSentByDoc.get(keyAns[1])) {
						if (sentText.containsKey(sid) && sentText.get(sid).contains(keyAns[0])) {
							System.out.println("\t\t" + sentText.get(sid));
						}
					}
					System.out.println("\tFiller's answer: " + predKeyParts[3]);
					System.out.println("\tFound in sentences:");
					String[] predAns = predAnswers.get(predKey).split(":",2);
					for (String sid : relSentByDoc.get(predAns[1])) {
						if (sentText.containsKey(sid) && sentText.get(sid).contains(predAns[0])) {
							System.out.println("\t\t" + sentText.get(sid));
						}
					}
					System.out.println();
					delete.add(keyKey);
					delete.add(predKey);
				}	
			}
		}
		for (String key : delete) {
			if (keyAnswers.containsKey(key)) {
				keyAnswers.remove(key);
			}
			if (predAnswers.containsKey(key)) {
				predAnswers.remove(key);
			}
		}
		System.out.println();
		
		// Print missed/not found answers
		System.out.println("Answers in key not found by filler: (Affect recall)");
		for (String key : keyAnswers.keySet()) {
			String[] keyParts = key.split(":");
			System.out.println("\tQuery: " + queryName.get(keyParts[0]));
			System.out.println("\tSlot: " + keyParts[1] + ":" + keyParts[2]);
			System.out.println("\tKey's answer: " + keyParts[3]);
			System.out.println("\tFound in sentences:");
			String[] keyAns = keyAnswers.get(key).split(":",2);
			for (String sid : relSentByDoc.get(keyAns[1])) {
				if (sentText.containsKey(sid) && sentText.get(sid).contains(keyAns[0])) {
					System.out.println("\t\t" + sentText.get(sid));
				}
			}
			System.out.println();
		}
		System.out.println();
		
		// Print spurious answers
		System.out.println("Answers not in key found by filler: (Affect precision)");
		for (String key : predAnswers.keySet()) {
			String[] keyParts = key.split(":");
			System.out.println("\tQuery: " + queryName.get(keyParts[0]));
			System.out.println("\tSlot: " + keyParts[1] + ":" + keyParts[2]);
			System.out.println("\tFiller's answer: " + keyParts[3]);
			System.out.println("\tFound in sentences:");
			String[] keyAns = predAnswers.get(key).split(":",2);
			for (String sid : relSentByDoc.get(keyAns[1])) {
				if (sentText.containsKey(sid) && sentText.get(sid).contains(keyAns[0])) {
					System.out.println("\t\t" + sentText.get(sid));
				}
			}
			System.out.println();			
		}
	}
}
