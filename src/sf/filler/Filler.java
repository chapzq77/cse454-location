package sf.filler;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;

import java.io.BufferedReader;
import java.io.FileReader;

import sf.SFConstants;
import sf.SFEntity;
import sf.retriever.CorefMention;
import sf.retriever.CorefProvider;
import tackbp.KbEntity.EntityType;

/**
 *
 * @author Xiao Ling
 */

public abstract class Filler {
	public List<String> slotNames = new ArrayList<String>();
	public abstract void predict(SFEntity mention, Map<String, String> annotations, CorefProvider sentenceCoref);
	
	protected boolean isPER(SFEntity mention) {
		return (!mention.ignoredSlots.containsAll(slotNames) && mention.entityType == EntityType.PER);
	}
	
	protected boolean isORG(SFEntity mention) {
		return (!mention.ignoredSlots.containsAll(slotNames) && mention.entityType == EntityType.ORG);
	}
	
	protected String containsName(SFEntity mention, String tokens, CorefProvider sentenceCoref) {
		Collection<CorefMention> sentenceMentions = sentenceCoref.all();
		for(CorefMention coref : sentenceMentions) {
			if(mention.mentionString.equals(coref.entity.repMention.entity.fullName)) {
				return coref.mentionSpan;
			}
		}
		return tokens.contains(mention.mentionString) ? mention.mentionString : null;
	}
	
	protected boolean containsPerName(SFEntity mention, String tokens) {
		String[] names = mention.mentionString.split(" ");
		String lastName = names[names.length - 1];
		// do case-insensitive match only if name is long
		// (otherwise White and white, Rose and rose, etc match)
		String regex = (lastName.length() > 5 ? "(?i)" : "") + "\\b" + lastName + "\\b";
		return mentionsRegex(tokens, regex);	
	}
	
	protected boolean containsOrg(SFEntity mention, String tokens) {
		String name = mention.mentionString.replaceAll("-", " ");
		if (name.length() > 20)
			name = name.substring(0, 20);
		return tokens.toLowerCase().replaceAll("-", " ").contains(name.toLowerCase());
	}
	
	protected List<String> extractLocations(Map<String, String> annotations, String tokens) {
		List<String> locs = new ArrayList<String>();
		String[] namedEnts = annotations.get(SFConstants.STANFORDNER).split("\\s+");
		String[] tokensArr = tokens.split("\\s+");

		for (int i = 0; i < namedEnts.length; i++) {
			if (namedEnts[i].equals("LOCATION")) {
				String location = tokensArr[i];
				while ((++i < namedEnts.length && namedEnts[i].equals("LOCATION")) || 
						(i+1 < namedEnts.length && tokensArr[i].equals(",") && 
								namedEnts[i+1].equals("LOCATION") &&
								(tokensArr[i+1].length() == 2 || tokensArr[i+1].endsWith(".")))) {
					if (namedEnts[i].equals("LOCATION")) {
						location += " " + tokensArr[i];
					} else {
						String state = tokensArr[i+1];
						if (state.length() == 3 && state.endsWith("."))
							state = state.substring(0,2);
						location += ", " + state;
						if (!state.endsWith(".") || (state.length() == 4 && state.charAt(1) == '.'))
							locs.add(state);
						i++;
						break;
					}	
				}
				locs.add(location);
			}
		}

		// bug in NER, doesn't understand "Tokyo-based" as a location
		for (int i = 0; i < tokensArr.length; i++) {
			if (tokensArr[i].contains("-based")) {
				String loc = tokensArr[i].substring(0, tokensArr[i].length() - 6);
				if (loc.length() <= 4 && i > 1 && 
						namedEnts[i-2].equals("LOCATION") && tokensArr[i-1].equals(",")) {
					if (loc.length() == 3 && loc.endsWith("."))
						loc = loc.substring(0,2);
					locs.add(tokensArr[i-2] + ", " + loc);
				}
				locs.add(loc);
			}
		}
		
		return locs;
	}
	
	protected String getFilename(Map<String, String> annotations) {
		String[] meta = annotations.get(SFConstants.META).split("\t");
		return meta[2];
	}
	
	protected boolean mentionsRegex(String tokens, String regex) {
		Pattern pat = Pattern.compile(regex);
		return pat.matcher(tokens).find();
	}
	
	protected static Set<String> countries;
	protected void loadCountries() {
		countries = new HashSet<String>();
		try {
			String line;
			BufferedReader br = new BufferedReader(new FileReader(SFConstants.COUNTRIES_FILE));
			while ((line = br.readLine()) != null) {
				countries.add(line.toLowerCase());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected boolean isCountry(String location) {
		if ( countries == null ) loadCountries();
		return countries.contains(location.toLowerCase());
	}
	
	protected boolean isStateProv(String location) {
		return isUSState(location) || isCanadianProvince(location);
	}
	
	protected static Set<String> states;
	protected void loadStates() {
		states = new HashSet<String>();
		try {
			String line;
			BufferedReader br = new BufferedReader(new FileReader(SFConstants.STATES_FILE));
			while ((line = br.readLine()) != null) {
				states.add(line.toLowerCase());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected boolean isUSState(String location) {
		if ( states == null ) loadStates();
		return states.contains(location.toLowerCase());
	}
	
	protected static Set<String> provinces;
	protected void loadProvinces() {
		provinces = new HashSet<String>();
		try {
			String line;
			BufferedReader br = new BufferedReader(new FileReader(SFConstants.PROVINCES_FILE));
			while ((line = br.readLine()) != null) {
				provinces.add(line.toLowerCase());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected boolean isCanadianProvince(String location) {
		if ( provinces == null ) loadProvinces();
		return provinces.contains(location.toLowerCase());
	}
	
	protected static Map<String, String> stateAbbrevs;
	protected void loadStateAbbrevs() {
		stateAbbrevs = new HashMap<String, String>();
		try {
			String line;
			BufferedReader br = new BufferedReader(new FileReader(SFConstants.STATE_ABBREVS_FILE));
			while ((line = br.readLine()) != null) {
				String[] parts = line.split("\\s+", 2);
				stateAbbrevs.put(parts[0].toLowerCase(), parts[1]);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String stateFromAbbr(String abbr) {
		if (abbr.length() > 2) 
			return abbr;
		if ( stateAbbrevs == null ) loadStateAbbrevs();
		return stateAbbrevs.get(abbr.toLowerCase());
	}
}
