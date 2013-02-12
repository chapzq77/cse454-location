package sf.filler.regex;

import java.util.Map;

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
			
		// find location, if it exists, in tokens
		String location = extractLocation(annotations, tokens);
		if (location == null)
			return;
		
		// check if headquarters is mentioned
		if (!mentionsRegex(tokens, SFConstants.HQ_REGEX))
			return;
		
		// check if location is a city
		if (isCountry(location.toLowerCase()) || isStateProv(location.toLowerCase()))
			return;
		
		SFEntity.SingleAnswer ans = new SFEntity.SingleAnswer();
		ans.answer = location;
		ans.doc = getFilename(annotations);
		mention.answers.put(slotName, ans);
	}
	
}
