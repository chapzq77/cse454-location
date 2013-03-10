import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import sf.filler.Filler;
import sf.filler.regex.RegexLocationFiller;
import sf.filler.tree.TregexOrgPlaceOfHeadquartersFiller;
import sf.filler.tree.TregexPerPlaceOfBirthFiller;
import sf.filler.tree.TregexPerPlaceOfDeathFiller;
import sf.filler.tree.TregexPerPlacesOfResidenceFiller;

/**
 * Processes command-line arguments.
 * @author Jeffrey Booth
 */
public class Args {
	// Filler abbreviations for the command-line interface.
	// NOTE: You should add your fillers here!
	public static Map<String, Class<? extends Filler>> FILLER_ABBREVS;
	static {
		FILLER_ABBREVS = new HashMap<String, Class<? extends Filler>>();
		//          Abbrev.     Class
		FILLER_ABBREVS.put("reg",      RegexLocationFiller.class );
		FILLER_ABBREVS.put("tr-hq",    TregexOrgPlaceOfHeadquartersFiller.class );
		FILLER_ABBREVS.put("tr-birth", TregexPerPlaceOfBirthFiller.class );
		FILLER_ABBREVS.put("tr-death", TregexPerPlaceOfDeathFiller.class );
		FILLER_ABBREVS.put("tr-homes", TregexPerPlacesOfResidenceFiller.class );
	}
	
	// Abbreviations for data sources.
	public static Map<String, String> DATA_SRC_ABBREVS;
	public static String DEFAULT_DATA_SRC = "sample";
	static {
		DATA_SRC_ABBREVS = new HashMap<String, String>();
		DATA_SRC_ABBREVS.put("filtered", "data/large-corpus/");
		DATA_SRC_ABBREVS.put("sample", "data/unfiltered-corpus/");
		DATA_SRC_ABBREVS.put("full", "/kbp/data/09nw/");
	}
	
	public boolean run, eval, verbose;
	public long limit;
	public Set<Class<? extends Filler>> fillers;
	private PrintStream out;
	public String dataSrc;
	
	public void usage() {
		// Display initial arguments.
		out.println("Arguments:\n" +
	                "\n" +
				    "run          - Run the evaluator.\n" +
	                "eval         - Evaluate the results.\n" +
				    "-v           - Print verbose output.\n" +
				    "-limit n     - Limit to n sentences. If n == 0, " + 
	                    "the number of sentences is not limited.\n");
		
		// Display data sources.
		out.println("-data x      - Use data source x, which is either " +
		                "the path to the folder containing\n" +
				    "               sentences.txt files, or one of the " +
		                "following:");
		for ( Map.Entry<String, String> entry :
			DATA_SRC_ABBREVS.entrySet() ) {
			out.printf( "  %-10s - Use %s\n", entry.getKey(),
					entry.getValue() );
		}
		
		// Display filler names.
		for ( Map.Entry<String, Class<? extends Filler>> entry :
			FILLER_ABBREVS.entrySet() ) {
			out.printf( "%-12s - Use %s\n", entry.getKey(), entry.getValue() );
		}
		out.println("(class name) - Use the filler with the given class name.");
		out.println("all-fillers  - Use all fillers in filterAbbrevs.");
	}
	
	@SuppressWarnings("unchecked")
	public Args( String[] args, PrintStream out ) {
		dataSrc = DATA_SRC_ABBREVS.get( DEFAULT_DATA_SRC );
		
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
					fillers.addAll( FILLER_ABBREVS.values() );
				else if ( arg.equals("-v") )
					verbose = true;
				else if ( arg.equals("-limit") ) {
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
				// Pick the data source
				else if ( arg.equals("-data") ) {
					idx++;
					try {
						dataSrc = DATA_SRC_ABBREVS.get(args[idx]);
						if ( dataSrc == null ) {
							dataSrc = args[idx];
						}
						if ( !(new File(dataSrc)).isDirectory() ) {
							throw new IllegalArgumentException(
									dataSrc + " must be a directory.");
						}
					} catch( ArrayIndexOutOfBoundsException ex ) {
						throw new IllegalArgumentException(
								"Missing path to data, or path abbrev.");
					}
				}
				// If a class abbrev was provided, use it as a filler.
				else if ( ( filler = FILLER_ABBREVS.get( arg ) ) != null )
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

	@Override
	public String toString() {
		return "run=" + run + "\neval=" + eval +
			   "\nlimit=" + limit + "\nfillers=" + fillers +
			   "\ndataSrc=" + dataSrc;
	}
}
