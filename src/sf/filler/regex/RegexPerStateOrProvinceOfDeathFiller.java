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
public class RegexPerStateOrProvinceOfDeathFiller extends Filler {

	public RegexPerStateOrProvinceOfDeathFiller() {
		slotName = "per:stateorprovince_of_death";
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
		
		// check if death is mentioned
		if (!mentionsRegex(tokens, SFConstants.DEATH_REGEX))
			return;
		
		// check if location is a state/prov
		if (!isStateProv(location.toLowerCase()))
			return;
		
		// add to answers
		addAnswer(mention, annotations, location);
	}
	
}
