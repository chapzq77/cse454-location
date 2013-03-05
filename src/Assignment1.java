import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
	public static Map<String, Class<? extends Filler>> fillerAbbrevs;
	
	// Filler abbreviations for the command-line interface.
	// NOTE: You should add your fillers here!
	static {
		fillerAbbrevs = new HashMap<String, Class<? extends Filler>>();
		//          Abbrev.     Class
		fillerAbbrevs.put("reg",      RegexLocationFiller.class );
		fillerAbbrevs.put("tr-hq",    TregexOrgPlaceOfHeadquartersFiller.class );
		fillerAbbrevs.put("tr-birth", TregexPerPlaceOfBirthFiller.class );
		fillerAbbrevs.put("tr-death", TregexPerPlaceOfDeathFiller.class );
		fillerAbbrevs.put("tr-homes", TregexPerPlacesOfResidenceFiller.class );
	}
	
	/**
	 * Processes arguments.
	 * @author Jeffrey Booth
	 */
	static class Args {
		public boolean run, eval;
		public long limit;
		public Set<Class<? extends Filler>> fillers;
		private PrintStream out;
		
		public void usage() {
			out.println("Arguments:\n" +
		                "\n" +
					    "run          - Run the evaluator\n" +
		                "eval         - Evaluate the results.\n" +
					    "limit n      - Limit to n sentences. If n == 0, " + 
		                    "the number of sentences is not limited.\n");
			for ( Map.Entry<String, Class<? extends Filler>> entry :
				fillerAbbrevs.entrySet() ) {
				out.printf( "%-12s - Use %s\n", entry.getKey(), entry.getValue() );
			}
			out.println("(class name) - Use the filler with the given class name.");
			out.println("all-fillers  - Use all fillers in filterAbbrevs.");
		}
		
		@SuppressWarnings("unchecked")
		public Args( String[] args, PrintStream out ) {
			try {
				this.out = out;
				
				fillers = new HashSet<Class<? extends Filler>>();
				int idx = 0;
				Class<? extends Filler> filler = null;
				while ( idx < args.length ) {
					String arg = args[ idx ];
					if ( arg.equals("run") )
						run = true;
					else if ( arg.equals("eval") )
						eval = true;
					else if ( arg.equals("all-fillers") )
						fillers.addAll( fillerAbbrevs.values() );
					else if ( arg.equals("limit") ) {
						idx++;
						try {
							limit = Long.parseLong( args[idx] );
							if ( limit < 0 ) {
								throw new IllegalArgumentException(
										"Sentence limit must be positive.");
							}
						} catch( NumberFormatException ex ) {
							throw new IllegalArgumentException(
									"Sentence limit must be an integer.");
						} catch( ArrayIndexOutOfBoundsException ex ) {
							throw new IllegalArgumentException(
									"Missing number of sentences to limit to.");
						}
					}
					// If a class abbrev was provided, use it as a filler.
					else if ( ( filler = fillerAbbrevs.get( arg ) ) != null )
						fillers.add( filler );
					else {
						// If a filler's full class name was given, use it too.
						try {
							Class<?> cls = Class.forName( arg );
							if ( Filler.class.isAssignableFrom( cls ) ) {
								fillers.add( (Class<? extends Filler>)
										     Class.forName(arg) );
							} else {
								throw new IllegalArgumentException(
										"Class " + arg +
										" does not extend Filler.");
							}
						} catch( ClassNotFoundException ex ) {
							throw new IllegalArgumentException(
									"Missing class " + arg + ".");
						}
					}
					idx++;
				}
				
				if ( fillers.size() == 0 ) {
					throw new IllegalArgumentException(
							"No fillers specified.");
				}
			} catch( IllegalArgumentException ex ) {
				out.println( ex.getMessage() + "\n" );
				usage();
				throw ex;
			}
		}
	}
	
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
		
		// initialize the corpus
		// FIXME replace the list by a generic class with an input of slot
		// name and an output of all the relevant files from the answer file
		try( ProcessedCorpus corpus = new ProcessedCorpus( KbpConstants.truncatedDocPath ) ) {
			// TODO: provide document ID
			CorefIndex corefIndex = new CorefIndex(0);
			
			// Predict annotations
			Map<String, String> annotations = null;
			int c = 0;
			while (corpus.hasNext()) {
				// Get next annotation
				annotations = corpus.next();
				if (++c % 1000 == 0) {
					System.err.print("finished reading " + c + " lines\r");
				}

				// for each query and filler, attempt to fill the slot.
				// TODO: provide sentence ID
				CorefProvider sentenceCoref = corefIndex.getSentenceIndex(0);
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
