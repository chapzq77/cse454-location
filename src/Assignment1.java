import java.io.*;
import java.util.*;

import tackbp.KbpConstants;
import sf.SFConstants;
import sf.SFEntity;
import sf.SFEntity.SingleAnswer;
import sf.eval.SFScore;
import sf.filler.Filler;

import sf.filler.regex.*;
import sf.filler.tree.*;

import sf.retriever.CorefIndex;
import sf.retriever.CorefProvider;
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
	public static void run(Args args) throws InstantiationException, IllegalAccessException {
		// read the queries
		sf.query.QueryReader queryReader = new sf.query.QueryReader();
		queryReader.readFrom(SFConstants.queryFile);
		
		// Construct fillers
		Filler[] fillers = new Filler[ args.fillers.size() ];
		int i = 0;
		for ( Class<? extends Filler> filler : args.fillers ) {
			fillers[ i++ ] = filler.newInstance(); 
		}
		
		StringBuilder answersString = new StringBuilder();
		
		String basePath = args.dataSrc;
		
		// initialize the corpus
		// FIXME replace the list by a generic class with an input of slot
		// name and an output of all the relevant files from the answer file
		try( ProcessedCorpus corpus = new ProcessedCorpus( basePath );
		     CorefIndex corefIndex =
		    		 new CorefIndex( basePath + "documents.coref" ) ) {
			
			// Predict annotations
			Map<String, String> annotations = null;
			int c = 0;
			while (corpus.hasNext()) {
				// Get next annotation
				annotations = corpus.next();
				if (++c % 1000 == 0) {
					System.out.println("finished reading " + c + " lines");
				}
				
				String[] sentenceArticle = annotations.get(
						SFConstants.ARTICLE_IDS ).split("\t");
				
				// Advance index to current document ID.
				long desiredDocId = Long.parseLong( sentenceArticle[1] );
				corefIndex.nextDoc( desiredDocId );
				
				// Get a coreference provider for the current sentence.
				long sentenceId = Long.parseLong( sentenceArticle[0] );
				CorefProvider sentenceCoref =
						corefIndex.getSentenceProvider( sentenceId );
				if ( args.verbose && c % 1000 == 0 ) {
					System.out.println("Sentence " + sentenceId + ": " +
							annotations.get( SFConstants.TEXT ) );
					System.out.println("Coreference mentions: " +
							sentenceCoref.all());
				}

				// For each query and filler, attempt to fill the slot.
				for (SFEntity query : queryReader.queryList) {
					for ( Filler filler : fillers ) {
						filler.predict(query, annotations, sentenceCoref);
					}
				}
				
				// Exit if we have exceeded the limit.
				if ( args.limit > 0 && c >= args.limit )
					break;
			}
			
			// Collect answers
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
								slotName, "MyRun", ans.doc,
								ans.answer));
					} else {
						answersString.append(String.format(
								"%s\t%s\t%s\t%s\t%s\n", query.queryId,
								slotName, "MyRun", "NIL", ""));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		FileUtil.writeTextToFile(answersString.toString(),
				SFConstants.outFile);
	}
	
	public static void main(String[] argsList) throws Exception {
		Args args = new Args(argsList, System.err);
		System.out.println("Arguments:\n" + args);

		// The slot filling pipeline
		if (args.run)
			run(args);
		
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
		if (args.eval)
			SFScore.main(new String[] {SFConstants.outFile,
					                   SFConstants.labelFile}); 
	}
}
