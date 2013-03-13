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
		if ((per && !containsPerName(mention, tokens)) ||//, sentenceCoref) == null) ||
				(org && !containsOrg(mention, tokens)))
			return;
		
		// find locations, if any exist, in tokens
		List<String> locations = extractLocations(annotations, tokens);
		if (locations.size() == 0)
			return;
		
		String filename = getFilename(annotations);
		if (per) {
			// per place of birth
			if (mentionsRegex(tokens, SFConstants.BIRTH_REGEX)) {
				for (String location : locations) {
					if (isCountry(location.toLowerCase()) && !mention.ignoredSlots.contains(SFConstants.slotNames[0]))
						mention.addAnswer(SFConstants.slotNames[0], location, filename);
					else if (isStateProv(location.toLowerCase()) && !mention.ignoredSlots.contains(SFConstants.slotNames[1]))
						mention.addAnswer(SFConstants.slotNames[1], stateFromAbbr(location), filename);
					else if (!mention.ignoredSlots.contains(SFConstants.slotNames[2]))
						mention.addAnswer(SFConstants.slotNames[2], location, filename);
				}
			}
			// per place of death
			if (mentionsRegex(tokens, SFConstants.DEATH_REGEX)) {
				for (String location : locations) {
					if (isCountry(location.toLowerCase()) && !mention.ignoredSlots.contains(SFConstants.slotNames[3]))
						mention.addAnswer(SFConstants.slotNames[3], location, filename);
					else if (isStateProv(location.toLowerCase()) && !mention.ignoredSlots.contains(SFConstants.slotNames[4]))
						mention.addAnswer(SFConstants.slotNames[4], stateFromAbbr(location), filename);
					else if (!mention.ignoredSlots.contains(SFConstants.slotNames[5]))
						mention.addAnswer(SFConstants.slotNames[5], location, filename);
				}
			}
		}
		if (org) {
			// org place of headquarters
			boolean hqReg = mentionsRegex(tokens, SFConstants.HQ_REGEX);
			String orgName = mention.mentionString;
			if (orgName.length() > 20)
				orgName = orgName.substring(0,20);
			if (hqReg || tokens.contains("'s") || tokens.contains("in")) {
				for (String location : locations) {
					String locRegex = "(?i)" + location + " 's .*" + orgName + "|" +
							"(" + orgName + "|organization|company).{0,30} in .{0,20}" + location + "|" +
							orgName + ".{0,20} at .{0,25} in " + location + "|" +
							location + " (organization|company|business) " + orgName + "|" + 
							"^\\d*\\sin " + location;
					if (hqReg || mentionsRegex(tokens, locRegex)) {
						int conf = (tokens.contains(location + "-based")) ? 2 : 1;
						if (isCountry(location.toLowerCase()) && !mention.ignoredSlots.contains(SFConstants.slotNames[6]))
							mention.addAnswer(SFConstants.slotNames[6], location, filename, conf);
						else if (isStateProv(location.toLowerCase()) && !mention.ignoredSlots.contains(SFConstants.slotNames[7]))
							mention.addAnswer(SFConstants.slotNames[7], stateFromAbbr(location), filename, conf);
						else if (!mention.ignoredSlots.contains(SFConstants.slotNames[8]))
							mention.addAnswer(SFConstants.slotNames[8], location, filename, conf);
					}
				}
			}
		}
	}
	
}
