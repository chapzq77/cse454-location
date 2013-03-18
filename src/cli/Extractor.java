package cli;
import java.io.*;
import java.util.*;

import tackbp.KbpConstants;
import sf.SFConstants;
import sf.SFEntity;
import sf.SFEntity.SingleAnswer;
import sf.eval.MistakeBreakdown;
import sf.eval.SFScore;
import sf.filler.Filler;

import sf.filler.regex.*;
import sf.filler.tree.*;

import sf.retriever.CorefEntity;
import sf.retriever.CorefIndex;
import sf.retriever.CorefMention;
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

public class Extractor {
	// TODO: actually calculate number of sentences in each corpus.
	protected static long ESTIMATED_SENTENCE_COUNT = 27350000;

	/**
	 * Formats a duration of time for display.
	 * 
	 * @param nanoseconds The duration, in nanoseconds, to display.
	 * @return The time in days:hours:minutes:seconds format.
	 */
	protected static String formatTime(long nanoseconds) {
		double seconds = nanoseconds / 1000000000.;
		boolean negative = seconds < 0;
		if ( negative ) seconds *= -1;

		int minutes = (int)(seconds / 60);
		seconds -= minutes * 60;

		int hours = minutes / 60;
		minutes -= hours * 60;

		int days = hours / 24;
		hours -= days * 24;

		return String.format("%s%d:%02d:%02d:%02.3f", negative ? "-" : "",
			days, hours, minutes, seconds);
	}
	
	/**
	 * Groups of fillers to compare.
	 */
	public static Map<String, Set<Class<? extends Filler>>> fillerGroups;
	static {
		fillerGroups = new HashMap<String, Set<Class<? extends Filler>>>();
		
		Set<Class<? extends Filler>> regexSet =
				new HashSet<Class<? extends Filler>>();
		regexSet.add( RegexLocationFiller.class );
		fillerGroups.put( "reg", regexSet );
		
		Set<Class<? extends Filler>> treeSet =
				new HashSet<Class<? extends Filler>>();
		treeSet.add( TregexOrgPlaceOfHeadquartersFiller.class );
		treeSet.add( TregexPerPlaceOfBirthFiller.class );
		treeSet.add( TregexPerPlaceOfDeathFiller.class );
		treeSet.add( TregexPerPlacesOfResidenceFiller.class );
		fillerGroups.put( "tree", treeSet );
		
		Set<Class<? extends Filler>> allSet =
				new HashSet<Class<? extends Filler>>();
		allSet.addAll( treeSet );
		allSet.addAll( regexSet );
		fillerGroups.put( "all", allSet );
	}
	
	protected static Map<SFEntity, Map<String, List<SingleAnswer>>>
		mergeAnswers( Map<Filler, Map<SFEntity, Map<String,
				List<SingleAnswer>>>> fillerAnswers,
				Set<Class<? extends Filler>> fillerClasses ) {
		Map<SFEntity, Map<String, List<SingleAnswer>>> results =
				new HashMap<SFEntity, Map<String, List<SingleAnswer>>>();
		System.out.println("======");
		System.out.println( fillerAnswers );
		
		for ( Map.Entry<Filler, Map<SFEntity, Map<String, List<SingleAnswer>>>>
				entry : fillerAnswers.entrySet() ) {
			if ( !fillerClasses.contains( entry.getKey().getClass() ) )
				continue;
			for ( Map.Entry<SFEntity, Map<String, List<SingleAnswer>>> sfEntry :
				entry.getValue().entrySet() ) {
				
				// Load entity, and make sure there's a place for it in the
				// results.
				SFEntity entity = sfEntry.getKey();
				Map<String, List<SingleAnswer>> allAnswersMap =
						results.get( entity );
				if ( allAnswersMap == null ) {
					allAnswersMap = new HashMap<String, List<SingleAnswer>>();
					results.put( entity, allAnswersMap );
				}
				
				// Load the answers
				for ( Map.Entry<String, List<SingleAnswer>> answerEntry :
					sfEntry.getValue().entrySet() ) {
					String slot = answerEntry.getKey();
					List<SingleAnswer> answers = answerEntry.getValue();
					
					List<SingleAnswer> allAnswers = allAnswersMap.get( slot );
					if ( allAnswers == null ) {
						allAnswers = new ArrayList<SingleAnswer>();
						allAnswersMap.put( slot, allAnswers );
					}
					
					// Merge the answers.
					for ( SingleAnswer answer : answers ) {
						// Attempt to find an existing matching answer.
						SingleAnswer match = null;
						for ( SingleAnswer candidate : allAnswers ) {
							if ( candidate.answer.equals(answer.answer) ) {
								match = candidate;
								break;
							}
						}
						
						if ( match == null ) {
							// The answer doesn't exist, so add a copy of it.
							match = new SingleAnswer();
							match.answer = answer.answer;
							match.doc    = answer.doc;
							match.count  = answer.count;
							allAnswers.add( match );
						} else {
							// Update the existing answer.
							if ( match.doc == null )
								match.doc = answer.doc;
							match.count += answer.count;
						}
					}
				}
			}
		}
		
		System.out.println(results);
			
		return results;
	}
	
	protected static Map<Filler, Map<SFEntity, Map<String, List<SingleAnswer>>>>
			predict(sf.query.QueryReader queryReader, Args args,
			ProcessedCorpus corpus, CorefIndex corefIndex, Filler[] fillers ) {
		// This map contains the answers to queries that the different fillers
		// provide. It is used to segregate results by filler type in order to
		// compare filler accuracy.
		Map<Filler, Map<SFEntity, Map<String, List<SingleAnswer>>>>
			fillerAnswers = new HashMap<Filler, Map<SFEntity,
			Map<String, List<SingleAnswer>>>>();
		
		// Initialize our answers
		for ( Filler f : fillers ) {
			fillerAnswers.put( f, new HashMap<SFEntity,
					Map<String, List<SingleAnswer>>>() );
		}
		
		long startTime = System.nanoTime();
		
		// Predict annotations
		Map<String, String> annotations = null;
		int c = 0;
		while (corpus.hasNext()) {
			// Get next annotation
			annotations = corpus.next();
			c++;
			if ( c < args.skip ) {
				if ( c % 100000 == 0 ) {
					System.out.println("Skipped " + c + " sentences.");
				}
				continue;
			}

			// Report status
			if (c % 1000 == 0) {
				long elapsed = System.nanoTime() - startTime;
				long estTime = (long)( elapsed *
						(double) ( ESTIMATED_SENTENCE_COUNT / (double) c - 1 ));
				System.out.println("===== Read " + c + " lines in " +
						formatTime(elapsed) + ", remaining time " +
						formatTime(estTime));
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

			// Report coreference information
			if ( args.verbose && c % 1000 == 0 ) {
				System.out.println("Sentence " + sentenceId + ": " +
						annotations.get( SFConstants.TEXT ) );
				System.out.println("Coreference mentions: " +
						sentenceCoref.all());
				Set<CorefEntity> entities = new HashSet<CorefEntity>();
				for ( CorefMention mention : sentenceCoref.all() ) {
					entities.add( mention.entity );
				}
				System.out.println("Coreference entities: " + entities);
			}

			// For each query and filler, attempt to fill the slot.
			for (SFEntity query : queryReader.queryList) {
				for ( Map.Entry<Filler, Map<SFEntity, Map<String,
						List<SingleAnswer>>>> entry : fillerAnswers.entrySet() ) {
					// Swap out the query answers for the filler-specific
					// answers.
					query.answers = entry.getValue().get( query );
					if ( query.answers == null ) {
						query.answers = new HashMap<String,
								List<SingleAnswer>>();
						entry.getValue().put( query, query.answers );
					}
					
					// Have the filler fill them in.
					Filler filler = entry.getKey();
					filler.predict(query, annotations, sentenceCoref);
				}
			}
			
			// Exit if we have exceeded the limit.
			if ( args.limit > 0 && c >= args.limit )
				break;
		}
		
		return fillerAnswers;
	}
	
	protected static void saveAnswers( Map<Filler, Map<SFEntity, Map<String,
			List<SingleAnswer>>>> fillerAnswers, Args args ) throws Exception {
		// Join the slotnames filled by the fillers
		Set<String> slotNames = new HashSet<String>();
		for ( Filler filler : fillerAnswers.keySet() ) {
			slotNames.addAll( filler.slotNames );
		}
		
		// Collect answers
		for ( Map.Entry<String, Set<Class<? extends Filler>>> fillerGroup :
			fillerGroups.entrySet() ) {
			// Unpack entry.
			String groupTitle = fillerGroup.getKey();
			Set<Class<? extends Filler>> fillerClasses =
					fillerGroup.getValue();
			
			// Merge answers from all items in the filler group.
			Map<SFEntity, Map<String, List<SingleAnswer>>> mergedAnswers =
				mergeAnswers( fillerAnswers, fillerClasses ); 
			
			// Open output filename
			String resultsFileName = "annotations.pred";
			if ( !groupTitle.equals("all") ) {
				resultsFileName += "." + groupTitle;
			}
			File resultsFile = new File( args.testSet, resultsFileName );
			PrintStream ps = new PrintStream(
					new FileOutputStream( resultsFile.getPath() ),
					true, "UTF8" );
			// TODO: should close ps on error.
			
			for ( Map.Entry<SFEntity, Map<String, List<SingleAnswer>>>
					queryEntry : mergedAnswers.entrySet() ) {
				SFEntity query = queryEntry.getKey();
				Map<String, List<SingleAnswer>> answerMap =
						queryEntry.getValue();
				for (String slotName : slotNames) {
					// for each query, print out the answer, or NIL if
					// nothing is found
					if (answerMap.containsKey(slotName)) {
						// The output file format
						// Column 1: query id
						// Column 2: the slot name
						// Column 3: a unique run id for the submission
						// Column 4: NIL, if the system believes no
						// information is learnable for this slot. Or, 
						// a single docid which supports the slot value
						// Column 5: a slot value
						SingleAnswer ans = answerMap.get(slotName).get(0);
						for (SingleAnswer a : answerMap.get(slotName)) {
							// choose answer with highest count
							if (a.count > ans.count)
								ans = a;
						}
						ps.printf("%s\t%s\t%s\t%s\t%s\n", query.queryId,
								slotName, "MyRun", ans.doc,
								ans.answer);
					} else {
						ps.printf("%s\t%s\t%s\t%s\t%s\n", query.queryId,
								slotName, "MyRun", "NIL", "");
					}
				}
			}
			
			ps.close();
		}
	}

	/**
	 * Run the slot filler over the corpus, and write out the results.
	 * 
	 * @param args Arguments to control slot filler operation.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static void run(Args args) throws InstantiationException, IllegalAccessException {
		// read the queries
		sf.query.QueryReader queryReader = new sf.query.QueryReader();
		queryReader.readFrom( new File( args.testSet, "queries.xml" )
			.getPath() );
		
		// Construct fillers
		int i = 0;
		Filler[] fillers = new Filler[args.fillers.size()];
		for ( Class<? extends Filler> filler : args.fillers ) {
			fillers[ i++ ] = filler.newInstance();;
			
		}
		
		String basePath = args.corpus.getPath();
		
		// initialize the corpus
		// FIXME replace the list by a generic class with an input of slot
		// name and an output of all the relevant files from the answer file
		ProcessedCorpus corpus = null;
		CorefIndex corefIndex = null;
		try {
			// Open corpus and corpus index.
			corpus = new ProcessedCorpus( basePath );
			corefIndex = new CorefIndex( basePath );
			
			Map<Filler, Map<SFEntity, Map<String, List<SingleAnswer>>>>
				fillerAnswers;
			
			// Predict slots.
			fillerAnswers = predict(queryReader, args, corpus, corefIndex,
						fillers );
						
			// Save the resulting answers.
			saveAnswers( fillerAnswers, args );
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// TODO: handle errors more intelligently:
			try {
				if ( corpus != null ) corpus.close();
				if ( corefIndex != null ) corefIndex.close();
			} catch ( IOException e ) {
				throw new RuntimeException(e);
			}
		}
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
		if (args.eval) {
			String fileName = "annotations.pred";
			if ( args.fillGroup != null ) {
				System.out.println( "=============== Fill group: " +
						args.fillGroup + " ===============" );
				fileName += "." + args.fillGroup;
			}
			String goldAnnotationPath =
					new File( args.testSet, "annotations.gold" ).getPath();
			String predictedAnnotationPath =
					new File( args.testSet, fileName ).getPath();
			SFScore.main(new String[] { predictedAnnotationPath,
										goldAnnotationPath,
										"anydoc" });
		}
		
		if (args.breakdown) {
			String goldAnnotationPath =
					new File( args.testSet, "annotations.gold" ).getPath();
			String predictedAnnotationPath =
					new File( args.testSet, "annotations.pred" ).getPath();
			String queryFilePath = 
					new File( args.testSet, "queries.xml" ).getPath();
			String sentMetaPath = 
					new File( args.corpus, "sentences.meta" ).getPath();
			String sentTextPath = 
					new File( args.corpus, "sentences.text" ).getPath();
			MistakeBreakdown.main(new String[] { predictedAnnotationPath,
												 goldAnnotationPath,
												 queryFilePath,
												 sentMetaPath,
												 sentTextPath });
		}
	}
}
