package sf.filler;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.BufferedReader;
import java.io.FileReader;

import sf.SFConstants;
import sf.SFEntity;
import tackbp.KbEntity.EntityType;

/**
 *
 * @author Xiao Ling
 */

public abstract class Filler {
	public String slotName = null;
	public abstract void predict(SFEntity mention, Map<String, String> annotations);
	
	protected boolean isPER(SFEntity mention) {
		return (!mention.ignoredSlots.contains(slotName) && mention.entityType == EntityType.PER);
	}
	
	protected boolean isORG(SFEntity mention) {
		return (!mention.ignoredSlots.contains(slotName) && mention.entityType == EntityType.ORG);
	}
	
	protected boolean containsName(SFEntity mention, String tokens) {
		String[] names = mention.mentionString.split(" ");
		String lastName = names[names.length - 1];
		return tokens.contains(lastName);
	}
	
	protected boolean containsOrg(SFEntity mention, String tokens) {
		return tokens.contains(mention.mentionString);
	}
	
	protected String extractLocation(Map<String, String> annotations, String tokens) {
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
			return null;
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
		
		return location;
	}
	
	protected String getFilename(Map<String, String> annotations) {
		String[] meta = annotations.get(SFConstants.META).split("\t");
		return meta[2];
	}
	
	protected boolean mentionsRegex(String tokens, String regex) {
		Pattern pat = Pattern.compile(regex);
		return pat.matcher(tokens).find();
	}
	
	protected boolean isCountry(String location) {
		String countries = "";
		try {
			String line;
			BufferedReader br = new BufferedReader(new FileReader(SFConstants.COUNTRIES_FILE));
			while ((line = br.readLine()) != null) {
				if (countries.length() > 0) {
					countries += "|";
				}
				countries += Pattern.quote(line.toLowerCase());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return countries.contains(location);
	}
	
	protected boolean isStateProv(String location) {
		String stateProvs = "";
		try {
			String line;
			BufferedReader br = new BufferedReader(new FileReader(SFConstants.STATES_FILE));
			while ((line = br.readLine()) != null) {
				if (stateProvs.length() > 0) {
					stateProvs += "|";
				}
				stateProvs += Pattern.quote(line.toLowerCase());
			}
			br = new BufferedReader(new FileReader(SFConstants.PROVINCES_FILE));
			while ((line = br.readLine()) != null) {
				stateProvs += "|";
				stateProvs += Pattern.quote(line.toLowerCase());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return stateProvs.contains(location);
	}

}
