package sf.filler.regex;

import java.util.Map;
import java.util.List;

import sf.SFConstants;
import sf.SFEntity;
import sf.filler.Filler;
import tackbp.KbEntity.EntityType;

/**
 * Needs "tokens", "meta",
 * @author xiaoling
 *
 */
public class RegexLocationFiller extends Filler {

	public RegexLocationFiller() {
		slotName = "regex_all_loc";
	}
	
	@Override
	public void predict(SFEntity mention, Map<String, String> annotations) {
		boolean per = isPER(mention);
		boolean org = isORG(mention);
		if (!(per || org))
			return;

		// check if the name is mentioned.
		String tokens = annotations.get(SFConstants.TOKENS);
		if ((per && !containsName(mention, tokens)) || (org && !containsOrg(mention, tokens)))
			return;
			
		// find locations, if any exist, in tokens
		List<String> locations = extractLocations(annotations, tokens);
		if (locations.size() == 0)
			return;
			
		if (per) {
			if (mentionsRegex(tokens, SFConstants.BIRTH_REGEX)) {
				for (String location : locations) {
					if (isCountry(location.toLowerCase()))
						addAnswer(mention, annotations, location, SFConstants.slotNames[0]);
					else if (isStateProv(location.toLowerCase()))
						addAnswer(mention, annotations, location, SFConstants.slotNames[1]);
					else
						addAnswer(mention, annotations, location, SFConstants.slotNames[2]);
				}
			}
			if (mentionsRegex(tokens, SFConstants.DEATH_REGEX)) {
				for (String location : locations) {
					if (isCountry(location.toLowerCase()))
						addAnswer(mention, annotations, location, SFConstants.slotNames[3]);
					else if (isStateProv(location.toLowerCase()))
						addAnswer(mention, annotations, location, SFConstants.slotNames[4]);
					else
						addAnswer(mention, annotations, location, SFConstants.slotNames[5]);
				}
			}
		}
		if (org) {
			if (mentionsRegex(tokens, SFConstants.HQ_REGEX)) {
				for (String location : locations) {
					if (isCountry(location.toLowerCase()))
						addAnswer(mention, annotations, location, SFConstants.slotNames[6]);
					else if (isStateProv(location.toLowerCase()))
						addAnswer(mention, annotations, location, SFConstants.slotNames[7]);
					else
						addAnswer(mention, annotations, location, SFConstants.slotNames[8]);
				}
			}
		}
	}
	
}
