package sf.filler.regex;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	private String STATES = "Alabama|Alaska|Arizona|Arkansas|California|Colorado|Connecticut|Delaware|District of Columbia|Florida|Georgia|Hawaii|Idaho|Illinois|Indiana|Iowa|Kansas|Kentucky|Louisiana|Maine|Maryland|Massachusetts|Michigan|Minnesota|Mississippi|Missouri|Montana|Nebraska|Nevada|New Hampshire|New Jersey|New Mexico|New York|North Carolina|North Dakota|Ohio|Oklahoma|Oregon|Pennsylvania|Rhode Island|South Carolina|South Dakota|Tennessee|Texas|Utah|Vermont|Virginia|Washington|West Virginia|Wisconsin|Wyoming";

	public RegexPerStateOrProvinceOfDeathFiller() {
		slotName = "per:stateorprovince_of_death";
	}
	
	@Override
	public void predict(SFEntity mention, Map<String, String> annotations) {
		// the query needs to be a PER type.
		if (mention.ignoredSlots.contains(slotName)
				|| mention.entityType != EntityType.PER) {
			return;
		}

		// check if the entity is mentioned.
		String tokens = annotations.get(SFConstants.TOKENS);
		if (!tokens.contains(mention.mentionString)) {
			return;
		}

		// get the filename of this sentence.
		String[] meta = annotations.get(SFConstants.META).split("\t");
		String filename = meta[2];

		String[] names = mention.mentionString.split(" ");
		String first = names[0];
		String last = null;
		if (names.length > 1) {
			last = names[names.length - 1];
		} else {
			last = first;
		}

		// check for us states
		Pattern patternLocation = Pattern.compile("(" + STATES + ")");

		Pattern patternDied = Pattern.compile("died|attacked|killed|murdered");

		// todo: reference parrot sketch
		if(!patternDied.matcher(tokens).find())
			return;

		Matcher matcher = patternLocation.matcher(tokens);
		if(matcher.find()) {
			SFEntity.SingleAnswer ans = new SFEntity.SingleAnswer();
			ans.answer = matcher.group(1).trim();
			ans.doc = filename;
			mention.answers.put(slotName, ans);
		}
	}
	
}
