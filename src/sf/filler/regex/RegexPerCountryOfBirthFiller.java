package sf.filler.regex;

import java.io.BufferedReader;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sf.SFConstants;
import sf.SFEntity;
import sf.filler.Filler;
import tackbp.KbEntity.EntityType;

/**
 * Needs "tokens", "meta",
 * @author xiaoling
 *
 */
public class RegexPerCountryOfBirthFiller extends Filler {
	private static final String DATA_DIR = "data/";
	private static final String COUNTRIES_FILE = DATA_DIR + "countries.txt";
	private List<String> countries;
	private Pattern countryPattern;
	private Pattern bornPattern;

	public RegexPerCountryOfBirthFiller() {
		slotName = "per:country_of_birth";

		countries = new ArrayList<String>();
		String countryPatternText = "";
		try ( FileReader fr = new FileReader( COUNTRIES_FILE );
			  BufferedReader br = new BufferedReader( fr ) ) {
			String line;
			while ( ( line = br.readLine() ) != null ) {
				countries.add(line);
				if ( countryPatternText.length() > 0 ) {
					countryPatternText += "|";
				}
				countryPatternText += Pattern.quote( line );
			}
		} catch ( Exception e ) {
			throw new RuntimeException( e );
		}

		// Create a pattern to match any country
		countryPattern = Pattern.compile( "(" + countryPatternText + ")" );

		// ... and the word "born"
		bornPattern = Pattern.compile( "[Bb][Oo][Rr][Nn]" );
	}
	
	@Override
	public void predict(SFEntity mention, Map<String, String> annotations) {
		// the query needs to be a PER type.
		if (mention.ignoredSlots.contains(slotName)
				|| mention.entityType != EntityType.PER) {
			return;
		}

		// get the filename of this sentence.
		String[] meta = annotations.get(SFConstants.META).split("\t");
		String filename = meta[2];

		String[] names = mention.mentionString.split(" ");
		String first = names[0];
		String last = null;
		if (names.length > 1) {
			last = names[names.length - 1];
		} else {
			last = first;
		}

		// Create a pattern for the person's name.
		String fullName = mention.mentionString;
		String patternName =
			"(" + Pattern.quote(fullName) +
			"|" + Pattern.quote(last) + ")";

		// check if the entity is mentioned.
		String tokens = annotations.get(SFConstants.TOKENS);
		Matcher personMatcher = Pattern.compile(patternName).matcher(tokens);
		if (!personMatcher.find()) {
			return;
		}

		// See if "born" is mentioned.
		Matcher bornMatcher = bornPattern.matcher( tokens );
		if ( bornMatcher.find() ) {
			//System.out.println("Found 'born' in sentence: " + tokens);
			Matcher matcher = countryPattern.matcher( tokens );
			if ( matcher.find() ) {
				String country = matcher.group( 1 ).trim();
				System.out.println("--> Found country " + country + " in sentence " + tokens);

				// Record answer
				SFEntity.SingleAnswer ans = new SFEntity.SingleAnswer();
				ans.answer = country;
				ans.doc = filename;
				mention.answers.put(slotName, ans);
			}
		}

		/*
		// Create a pattern for the person's name.
		String fullName = mention.mentionString;
		String patternName =
			"(" + Pattern.quote(fullName) +
			"|" + Pattern.quote(first) +
			"|" + Pattern.quote(last) + ")";

		// Create some patterns to match example sentences.
		Pattern patternDashBorn = Pattern.compile("(.*)-born");
		Pattern patternWasBorn = Pattern.compile(patternName + " was born (in|on) (.+?)\\p{Punct}");

		PatternMatcher pm = new PatternMatcher( mention, filename, tokens );
		pm.match( patternDashBorn, 1 );
		pm.match(  patternWasBorn, 3 );
		*/
	}

	// Matches patterns, and puts the answers into slots.
	class PatternMatcher {
		private SFEntity mention;
		private String filename;
		private String tokens;

		public PatternMatcher( SFEntity mention, String filename, String tokens ) {
			this.filename = filename;
			this.mention  = mention;
			this.tokens   = tokens;
		}
		
		public boolean match( Pattern pattern, int group ) {
			Matcher matcher = pattern.matcher(tokens);
			boolean found = matcher.find();
			if ( found ) {
				SFEntity.SingleAnswer ans = new SFEntity.SingleAnswer();
				ans.answer = matcher.group( group ).trim();
				ans.doc = filename;
				mention.answers.put(slotName, ans);
			}
			return found;
		}
	}
}
