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
public class RegexOrgCityOfHeadquartersFiller extends Filler {

	public RegexOrgCityOfHeadquartersFiller() {
		slotName = "org:city_of_headquarters";
	}
	
	@Override
	public void predict(SFEntity mention, Map<String, String> annotations) {
		// the query needs to be an ORG type.
		if (!isORG(mention))
			return;

		// check if the organization's name is mentioned.
		String tokens = annotations.get(SFConstants.TOKENS);
		if (!containsOrg(mention, tokens))
			return;
			
		// find locations, if any exist, in tokens
		List<String> locations = extractLocations(annotations, tokens);
		if (locations.size() == 0)
			return;
		
		// check if headquarters is mentioned
		if (!mentionsRegex(tokens, SFConstants.HQ_REGEX))
			return;
		
		// Add city locations to answers
		for (String location : locations) {
			if (!isCountry(location.toLowerCase()) && !isStateProv(location.toLowerCase()))
				addAnswer(mention, annotations, location);
		}
	}
	
}
