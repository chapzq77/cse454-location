package sf.filler.regex;

import java.util.Map;
import java.util.List;

import sf.SFConstants;
import sf.SFEntity;
import sf.filler.Filler;
import sf.retriever.CorefProvider;

/**
 * Needs "tokens", "meta",
 * @author xiaoling
 *
 */
public class RegexLocationFiller extends Filler {

	public RegexLocationFiller() {
		for (String slot : SFConstants.slotNames) {
			slotNames.add(slot);
		}
	}
	
	@Override
	public void predict(SFEntity mention, Map<String, String> annotations,
			CorefProvider sentenceCoref) {
		boolean per = isPER(mention);
		boolean org = isORG(mention);
		if (!(per || org))
			return;

		// check if the name is mentioned.
		String tokens = annotations.get(SFConstants.TOKENS);
		if ((per && !containsName(mention, tokens, sentenceCoref)) ||
				(org && !containsOrg(mention, tokens)))
			return;
			
		// find locations, if any exist, in tokens
		List<String> locations = extractLocations(annotations, tokens);
		if (locations.size() == 0)
			return;
		
		String filename = getFilename(annotations);
		if (per) {
			if (mentionsRegex(tokens, SFConstants.BIRTH_REGEX)) {
				for (String location : locations) {
					if (isCountry(location.toLowerCase()) && !mention.ignoredSlots.contains(SFConstants.slotNames[0]))
						mention.addAnswer(SFConstants.slotNames[0], location, filename);
					else if (isStateProv(location.toLowerCase()) && !mention.ignoredSlots.contains(SFConstants.slotNames[1]))
						mention.addAnswer(SFConstants.slotNames[1], location, filename);
					else if (!mention.ignoredSlots.contains(SFConstants.slotNames[2]))
						mention.addAnswer(SFConstants.slotNames[2], location, filename);
				}
			}
			if (mentionsRegex(tokens, SFConstants.DEATH_REGEX)) {
				for (String location : locations) {
					if (isCountry(location.toLowerCase()) && !mention.ignoredSlots.contains(SFConstants.slotNames[3]))
						mention.addAnswer(SFConstants.slotNames[3], location, filename);
					else if (isStateProv(location.toLowerCase()) && !mention.ignoredSlots.contains(SFConstants.slotNames[4]))
						mention.addAnswer(SFConstants.slotNames[4], location, filename);
					else if (!mention.ignoredSlots.contains(SFConstants.slotNames[5]))
						mention.addAnswer(SFConstants.slotNames[5], location, filename);
				}
			}
		}
		if (org) {
			if (mentionsRegex(tokens, SFConstants.HQ_REGEX)) {
				for (String location : locations) {
					if (isCountry(location.toLowerCase()) && !mention.ignoredSlots.contains(SFConstants.slotNames[6]))
						mention.addAnswer(SFConstants.slotNames[6], location, filename);
					else if (isStateProv(location.toLowerCase()) && !mention.ignoredSlots.contains(SFConstants.slotNames[7]))
						mention.addAnswer(SFConstants.slotNames[7], location, filename);
					else if (!mention.ignoredSlots.contains(SFConstants.slotNames[8]))
						mention.addAnswer(SFConstants.slotNames[8], location, filename);
				}
			}
		}
	}
	
}
