package sf.filler.tree;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

import sf.SFConstants;
import sf.SFEntity;
import sf.SFEntity.SingleAnswer;
import sf.filler.Filler;

public class TregexPerPlaceOfBirthFiller extends Filler {

	private static final String countryOfBirth = "per:country_of_birth";
	private static final String stateOfBirth = "per:stateorprovince_of_birth";
	private static final String cityOfBirth = "per:city_of_birth";

	public TregexPerPlaceOfBirthFiller() {
		slotName = countryOfBirth;
	}
	
	@Override
	public void predict(SFEntity mention, Map<String, String> annotations) {
		String tokens = annotations.get(SFConstants.TOKENS);
		if (!isPER(mention))
			return;
		if (!containsName(mention, tokens))
			return;
		
		String cjtext = annotations.get(SFConstants.CJ);
		Tree t = null;
		
		try {
			t = new PennTreeReader(new StringReader(cjtext)).readTree();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		TregexPattern p = TregexPattern.compile("NNP|NNPS >> (NP > (PP < (IN < /^in$/) > (VP < (VBD|VBN < /born/))))");
		TregexMatcher m = p.matcher(t);
		
		ArrayList<String> possiblePlaces = new ArrayList<String>();
		while(m.find()) {
			possiblePlaces.add(m.getMatch().firstChild().value());
		}
		
		// TODO not very good, need a way to combine multiple word place names
		// TODO also check for LOCATION tags
		for(String place : possiblePlaces) {
			if(isCountry(place)) {
				SFEntity.SingleAnswer country = new SFEntity.SingleAnswer();
				country.answer = place;
				country.doc = getFilename(annotations);
				mention.answers.put(countryOfBirth, country);
			} else if(isStateProv(place)) {
				SFEntity.SingleAnswer state = new SFEntity.SingleAnswer();
				state.answer = place;
				state.doc = getFilename(annotations);
				mention.answers.put(stateOfBirth, state);
			} else {
				SFEntity.SingleAnswer city = new SFEntity.SingleAnswer();
				city.answer = place;
				city.doc = getFilename(annotations);
				mention.answers.put(cityOfBirth, city);
			}
		}
	}

}
