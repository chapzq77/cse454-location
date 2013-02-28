package sf.filler;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
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
		// do case-insensitive match only if name is long
		// (otherwise White and white, Rose and rose, etc match)
		String regex = (lastName.length() > 5 ? "(?i)" : "") + "\\b" + lastName + "\\b";
		return mentionsRegex(tokens, regex);
	}
	
	protected boolean containsOrg(SFEntity mention, String tokens) {
		return tokens.contains(mention.mentionString);
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
	
	// adds answer/count to ongoing list.
	// TODO: can probably improve on this design.
	protected void addAnswer(SFEntity mention, Map<String, String> annotations, String location, String slotName) {
		
		// at least one answer exists for this slot
		if (mention.answers.containsKey(slotName)) {
			
			// this Answer is already in list, increment count and add the doc
			boolean found = false;
			for (SFEntity.SingleAnswer ans : mention.answers.get(slotName)) {
				if (ans.answer.equals(location)) {
					ans.doc.add(getFilename(annotations));
					ans.count++;
					found = true;
					break;
				}
			}
			
			// this Answer is not in list, add it with a count of 1
			if (!found) {
				SFEntity.SingleAnswer ans = new SFEntity.SingleAnswer();
				ans.answer = location;
				ans.doc = new HashSet<String>();
				ans.doc.add(getFilename(annotations));
				ans.count = 1;
				mention.answers.get(slotName).add(ans);
			}
			
		// no answers exist for this slot yet
		} else {
			// create SingleAnswer
			SFEntity.SingleAnswer ans = new SFEntity.SingleAnswer();
			ans.answer = location;
			ans.doc = new HashSet<String>();
			ans.doc.add(getFilename(annotations));
			ans.count = 1;
			
			// create List of Answers containing SingleAnswer
			List<SFEntity.SingleAnswer> answers = new ArrayList<SFEntity.SingleAnswer>();
			answers.add(ans);
			
			// put in Map
			mention.answers.put(slotName, answers);
		}
	}
	
	protected void addAnswer(SFEntity mention, Map<String, String> annotations, String location) {
		addAnswer(mention, annotations, location, this.slotName);
	}
}
