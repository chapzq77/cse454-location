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
public class RegexPerStateOrProvinceOfBirthFiller extends Filler {

	public RegexPerStateOrProvinceOfBirthFiller() {
		slotName = "per:stateorprovince_of_birth";
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
		
		// find locations, if any exist, in tokens
		List<String> locations = extractLocations(annotations, tokens);
		if (locations.size() == 0)
			return;
		
		// check if birth is mentioned
		if (!mentionsRegex(tokens, SFConstants.BIRTH_REGEX))
			return;
		
		// Add state/prov locations to answers
		for (String location : locations) {
			if (isStateProv(location.toLowerCase()))
				addAnswer(mention, annotations, location);
		}
	}
	
}
