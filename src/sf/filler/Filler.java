package sf.filler;

import java.util.Collection;
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
	
	protected boolean containsName(SFEntity mention, String tokens, CorefProvider sentenceCoref) {
		Collection<CorefMention> sentenceMentions = sentenceCoref.all();
		for(CorefMention coref : sentenceMentions) {
			if(mention.mentionString.equals(coref.entity.repMention.entity.fullName)) {
				return true;
			}
		}
		return tokens.contains(mention.mentionString);
		/*
		String[] names = mention.mentionString.split(" ");
		String lastName = names[names.length - 1];
		// do case-insensitive match only if name is long
		// (otherwise White and white, Rose and rose, etc match)
		String regex = (lastName.length() > 5 ? "(?i)" : "") + "\\b" + lastName + "\\b";
		return mentionsRegex(tokens, regex);*/
	}
	
	protected boolean containsOrg(SFEntity mention, String tokens) {
		String name = mention.mentionString;
		if (name.length() > 20)
			name = name.substring(0, 20);
		return tokens.toLowerCase().contains(name.toLowerCase());
	}
	
	protected List<String> extractLocations(Map<String, String> annotations, String tokens) {
		List<String> locs = new ArrayList<String>();
		String[] namedEnts = annotations.get(SFConstants.STANFORDNER).split("\\s+"); // splitting by \t wasnt working...
		String[] tokensArr = tokens.split("\\s+");
		
		for (int i = 0; i < namedEnts.length; i++) {
			if (namedEnts[i].equals("LOCATION")) {
				String location = tokensArr[i];
				while (++i < namedEnts.length && namedEnts[i].equals("LOCATION")) { // Ex, "United States" -> "LOCATION LOCATION"
					location += " " + tokensArr[i];
				}
				locs.add(location);
			}
		}
		
		// bug in NER, doesn't understand "Tokyo-based" as a location
		for (int i = 0; i < tokensArr.length; i++) {
			if (tokensArr[i].contains("-based")) {
				locs.add(tokensArr[i].substring(0, tokensArr[i].length() - 6));
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
	
	protected boolean isCountry(String location) {
		Set<String> countries = new HashSet<String>();
		try {
			String line;
			BufferedReader br = new BufferedReader(new FileReader(SFConstants.COUNTRIES_FILE));
			while ((line = br.readLine()) != null) {
				countries.add(line.toLowerCase());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return countries.contains(location);
	}
	
	protected boolean isStateProv(String location) {
		Set<String> stateProvs = new HashSet<String>();
		try {
			String line;
			BufferedReader br = new BufferedReader(new FileReader(SFConstants.STATES_FILE));
			while ((line = br.readLine()) != null) {
				stateProvs.add(line.toLowerCase());
			}
			br = new BufferedReader(new FileReader(SFConstants.PROVINCES_FILE));
			while ((line = br.readLine()) != null) {
				stateProvs.add(line.toLowerCase());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return stateProvs.contains(location);
	}
}
