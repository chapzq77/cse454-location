package sf;

import tackbp.KbpConstants;

public class SFConstants {

	// data types/extensions
	public static final String ARTICLE_IDS                = "articleIDs";
	public static final String CJ                         = "cj";
	public static final String DEPS_STANFORD_CC_PROCESSED = "depsStanfordCCProcessed";
	public static final String META                       = "meta";
	public static final String STANFORDLEMMA              = "stanfordlemma";
	public static final String STANFORDNER                = "stanfordner";
	public static final String STANFORDPOS                = "stanfordpos";
	public static final String TEXT                       = "text";
	public static final String TOKENS                     = "tokens";
	public static final String TOKEN_SPANS                = "tokenSpans";
	public static final String WIKI                       = "wikification";

	// file prefix of the data files
	public static final String prefix = "sentences";

	// a default list of data types
	public static final String[] dataTypes = {ARTICLE_IDS,CJ,DEPS_STANFORD_CC_PROCESSED,META,STANFORDLEMMA,STANFORDNER,STANFORDPOS,TEXT,TOKENS,TOKEN_SPANS,WIKI};

	// slot filling queries
	public static final String queryFile = KbpConstants.truncatedRootPath + "sf/tac_2010_kbp_evaluation_slot_filling_queries.xml";

	// slot filling output file
	public static final String outFile = KbpConstants.truncatedRootPath + "sf/sf.pred";

	public static final String labelFile = KbpConstants.truncatedRootPath + "sf/sf.gold";
	
	public static final String DATA_DIR = "data/";
	public static final String COUNTRIES_FILE = DATA_DIR + "countries.txt";
	public static final String STATES_FILE = DATA_DIR + "states.txt";
	public static final String PROVINCES_FILE = DATA_DIR + "provinces.txt";
	
	public static final String BIRTH_REGEX = "(?i)born|originally from";
	public static final String DEATH_REGEX = "(?i)death|died|dead |passed away|assassinated|killed|murdered";
	public static final String HQ_REGEX = "(?i)headquarter|located|based";
	
	public static final String[] slotNames = {"per:country_of_birth","per:stateorprovince_of_birth","per:city_of_birth",
											"per:country_of_death","per:stateorprovince_of_death","per:city_of_death",
											"org:country_of_headquarters","org:stateorprovince_of_headquarters","org:city_of_headquarters"};
}
