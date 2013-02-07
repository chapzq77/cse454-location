package sf.filler.regex;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.BufferedReader;
import java.io.FileReader;

import sf.SFConstants;
import sf.SFEntity;
import sf.filler.Filler;
import tackbp.KbEntity.EntityType;

/**
 * Needs "tokens", "meta",
 * @author xiaoling
 *
 */
public class RegexPerCountryOfDeathFiller extends Filler {
	private static final String DATA_DIR = "data/";
	private static final String COUNTRIES_FILE = DATA_DIR + "countries.txt";
	//private static final String STATES_FILE = DATA_DIR + "states.txt";
	//private static final String PROVINCES_FILE = DATA_DIR + "provinces.txt";

	public RegexPerCountryOfDeathFiller() {
		slotName = "per:country_of_death";
	}
	
	@Override
	public void predict(SFEntity mention, Map<String, String> annotations) {
		// the query needs to be a PER type.
		if (mention.ignoredSlots.contains(slotName)
				|| mention.entityType != EntityType.PER) {
			return;
		}

		// check if the person's last name is mentioned.
		String tokens = annotations.get(SFConstants.TOKENS);
		String[] names = mention.mentionString.split(" ");
		String lastName = names[names.length - 1];
		if (!tokens.contains(lastName)) {
			return;
		}
		
		// check if a location is mentioned.
		String[] namedEnts = annotations.get(SFConstants.STANFORDNER).split("\\s+"); // splitting by \t wasnt working...
		int locStartIdx = -1;
		int locEndIdx = -1;
		for (int i = 0; i < namedEnts.length; i++) {
			if (namedEnts[i].equals("LOCATION")) {
				locStartIdx = i;
				locEndIdx = i;
				while (++i < namedEnts.length && namedEnts[i].equals("LOCATION")) { // Ex, "United States" -> "LOCATION LOCATION"
					locEndIdx++;
				}
				break;
			}
		}
		if (locStartIdx == -1) {
			return;
		}
		
		// Extract location from tokens
		String location = "";
		String[] tokensArr = tokens.split("\\s+");
		for (int i = locStartIdx; i <= locEndIdx; i++) {
			if (location.length() > 0) {
				location += " ";
			}
			location += tokensArr[i];
		}
			
		// get the filename of this sentence.
		String[] meta = annotations.get(SFConstants.META).split("\t");
		String filename = meta[2];
		
		// check if death is mentioned
		Pattern deathPat = Pattern.compile("death|died|dead |passed away|assassinated|killed|murdered");
		if(!deathPat.matcher(tokens).find()) {
			return;
		}
		
		// read in list of countries
		String countries = "";
		try {
			String line;
			BufferedReader br = new BufferedReader(new FileReader(COUNTRIES_FILE));
			while ((line = br.readLine()) != null) {
				if (countries.length() > 0) {
					countries += "|";
				}
				countries += Pattern.quote(line.toLowerCase());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		// read in list of states/provinces
		// String stateProvs = "";
		// try {
			// String line;
			// BufferedReader br = new BufferedReader(new FileReader(STATES_FILE));
			// while ((line = br.readLine()) != null) {
				// if (stateProvs.length() > 0) {
					// stateProvs += "|";
				// }
				// stateProvs += Pattern.quote(line.toLowerCase());
			// }
			// br = new BufferedReader(new FileReader(PROVINCES_FILE));
			// while ((line = br.readLine()) != null) {
				// stateProvs += "|";
				// stateProvs += Pattern.quote(line.toLowerCase());
			// }
		// } catch (Exception e) {
			// throw new RuntimeException(e);
		// }
		
		// check if location is a country
		if (!countries.contains(location.toLowerCase())) {
			return;
		}
		
		// check if location is a state/province
		// if (!stateProvs.contains(location.toLowerCase())) {
			// return;
		// }
		
		// sentence contains last name, death, and a country. winner
		SFEntity.SingleAnswer ans = new SFEntity.SingleAnswer();
		ans.answer = location;
		ans.doc = filename;
		mention.answers.put(slotName, ans);
	}
	
}
