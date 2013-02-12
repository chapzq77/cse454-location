package sf;

import tackbp.KbpConstants;

public class SFConstants {

	// data types/extensions
	public static final String STANFORDNER = "stanfordner";

	public static final String DEPS_STANFORD_CC_PROCESSED = "depsStanfordCCProcessed";

	public static final String CJ = "cj";

	public static final String STANFORDPOS = "stanfordpos";

	public static final String TOKEN_SPANS = "tokenSpans";

	public static final String TOKENS = "tokens";

	public static final String TEXT = "text";

	public static final String META = "meta";

	// file prefix of the data files
	public static final String prefix = "sentences";

	// a default list of data types
	public static final String[] dataTypes = {META,TEXT, TOKENS, TOKEN_SPANS, STANFORDPOS, CJ, DEPS_STANFORD_CC_PROCESSED, STANFORDNER};

	// slot filling queries
	public static final String queryFile = KbpConstants.truncatedRootPath + "sf/tac_2010_kbp_evaluation_slot_filling_queries.xml";

	// slot filling output file
	public static final String outFile = KbpConstants.truncatedRootPath + "sf/sf.pred";

	public static final String labelFile = KbpConstants.truncatedRootPath + "sf/sf.gold";
	
	public static final String DATA_DIR = "data/";
	public static final String COUNTRIES_FILE = DATA_DIR + "countries.txt";
	public static final String STATES_FILE = DATA_DIR + "states.txt";
	public static final String PROVINCES_FILE = DATA_DIR + "provinces.txt";
	
	public static final String BIRTH_REGEX = "born|originally from";
	public static final String DEATH_REGEX = "death|died|dead |passed away|assassinated|killed|murdered";
	public static final String HQ_REGEX = "headquarter|located|based";
}
