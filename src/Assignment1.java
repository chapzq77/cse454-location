import java.io.IOException;
import java.util.Map;

import tackbp.KbpConstants;
import sf.SFConstants;
import sf.SFEntity;
import sf.SFEntity.SingleAnswer;
import sf.eval.SFScore;
import sf.filler.Filler;

import sf.filler.regex.RegexLocationFiller;

import sf.retriever.ProcessedCorpus;
import util.FileUtil;

/**
 * CSE 454 Assignment 1 main class. Java 7 required.
 * 
 * In the main method, a pipeline is run as follows: 1) Read the queries. 2) For
 * each query, retrieve relevant documents. In this assignment, only the
 * documents containing an answer will be returned to save running time. In
 * practice, a search-engine-style retriever will be used. Iterate through all
 * the sentences returned and the slot filler will applied to extract answers.
 * 3) Spit out answers and evaluate them against the labels.
 * 
 * In this assignment, you only need to write a new class for the assigned slots
 * implementing the <code>sf.filler.Filler</code> interface. An example class on
 * birthdate is implemented in <code>sf.filler.RegexPerDateOfBirthFiller.java</code>.
 * 
 * @author Xiao Ling
 */

public class Assignment1 {
	public static void main(String[] args) throws Exception {
		// if the first 2 steps are run
		boolean run = true;
		// if the 3rd step is run
		boolean eval = true;
		if (args.length == 1) {
			if (args[0].equals("run")) {
				run = true;
				eval = false;
			} else if (args[0].equals("eval")) {
				run = false;
				eval = true;
			}
		}

		// The slot filling pipeline
		if (run) {
			// read the queries
			sf.query.QueryReader queryReader = new sf.query.QueryReader();
			queryReader.readFrom(SFConstants.queryFile);

			// initialize the filler
			Filler filler = new RegexLocationFiller();

			StringBuilder answersString = new StringBuilder();
			// initialize the corpus
			// FIXME replace the list by a generic class with an input of slot
			// name and an output of all the relevant files from the answer file
			try( ProcessedCorpus corpus = new ProcessedCorpus( KbpConstants.truncatedDocPath ) ) {
				Map<String, String> annotations = null;
				int c = 0;
				while (corpus.hasNext()) {
					annotations = corpus.next();
					if (c++ % 100000 == 0) {
						System.err.print("finished reading " + c + " lines\r");
					}

					// for each query, find out if the slot can be filled
					for (SFEntity query : queryReader.queryList) {
						// apply the filler to the sentences with its
						// annotations
						filler.predict(query, annotations);
					}
				}
				for (String slotName : SFConstants.slotNames) {
					// for each query, print out the answer, or NIL if nothing is found
					for (SFEntity query : queryReader.queryList) {
						if (query.answers.containsKey(slotName)) {
							// The output file format
							// Column 1: query id
							// Column 2: the slot name
							// Column 3: a unique run id for the submission
							// Column 4: NIL, if the system believes no
							// information is learnable for this slot. Or, 
							// a single docid which supports the slot value
							// Column 5: a slot value
							SingleAnswer ans = query.answers.get(slotName).get(0);
							for (SingleAnswer a : query.answers.get(slotName)) {
								if (a.count > ans.count) // choose answer with highest count
									ans = a;
							}
							answersString.append(String.format(
									"%s\t%s\t%s\t%s\t%s\n", query.queryId,
									slotName, filler.getClass().getName(), ans.doc,
									ans.answer));
						} else {
							answersString.append(String.format(
									"%s\t%s\t%s\t%s\t%s\n", query.queryId,
									slotName, filler.getClass().getName(), "NIL", ""));
						}
					}
				}
				
				// Old version that uses filler.slotName
				
				// // for each query, print out the answer, or NIL if nothing is found
				// for (SFEntity query : queryReader.queryList) {
					// if (query.answers.containsKey(filler.slotName)) {
						// // The output file format
						// // Column 1: query id
						// // Column 2: the slot name
						// // Column 3: a unique run id for the submission
						// // Column 4: NIL, if the system believes no
						// // information is learnable for this slot. Or, 
						// // a single docid which supports the slot value
						// // Column 5: a slot value
						// SingleAnswer ans = query.answers.get(filler.slotName).get(0);
						// for (SingleAnswer a : query.answers.get(filler.slotName)) {
							// if (a.count > ans.count) // choose answer with highest count
								// ans = a;
						// }
						// answersString.append(String.format(
								// "%s\t%s\t%s\t%s\t%s\n", query.queryId,
								// filler.slotName, filler.getClass().getName(), ans.doc,
								// ans.answer));
					// } else {
						// answersString.append(String.format(
								// "%s\t%s\t%s\t%s\t%s\n", query.queryId,
								// filler.slotName, filler.getClass().getName(), "NIL", ""));
					// }
				// }
			} catch (Exception e) {
				e.printStackTrace();
			}
			FileUtil.writeTextToFile(answersString.toString(),
					SFConstants.outFile);
		}
		if (eval) {
			// Evaluate against the gold standard labels
			// The label file format (11 fields):
			// 1. NA
			// 2. query id
			// 3. NA
			// 4. slot name
			// 5. from which doc
			// 6., 7., 8. NA
			// 9. answer string
			// 10. equiv. class for the answer in different strings
			// 11. judgement. Correct ones are labeled as 1. 
			try {
				SFScore.main(new String[] { SFConstants.outFile, SFConstants.labelFile});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
