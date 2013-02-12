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
public class RegexPerCityOfBirthFiller extends Filler {

	public RegexPerCityOfBirthFiller() {
		slotName = "per:city_of_birth";
	}
	
	@Override
	public void predict(SFEntity mention, Map<String, String> annotations) {
		// the query needs to be a PER type.
		if (!isPER(mention))
			return;

		// check if the person's last name is mentioned.
		String tokens = annotations.get(SFConstants.TOKENS);
		if (!containsName(mention, tokens))
			return;
		
		// find location, if it exists, in tokens
		String location = extractLocation(annotations, tokens);
		if (location == null)
			return;
		
		// check if birth is mentioned
		if (!mentionsRegex(tokens, SFConstants.BIRTH_REGEX))
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
