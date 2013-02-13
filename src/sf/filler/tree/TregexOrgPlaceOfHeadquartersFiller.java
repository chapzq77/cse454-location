package sf.filler.tree;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;

import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

import sf.SFConstants;
import sf.SFEntity;
import sf.filler.Filler;

public class TregexOrgPlaceOfHeadquartersFiller extends Filler {

	private static final String countryOfHeadquarters = "org:country_of_headquarters";
	private static final String stateOfHeadquarters = "org:stateorprovince_of_headquarters";
	private static final String cityOfHeadquarters = "org:city_of_headquarters";

	public TregexOrgPlaceOfHeadquartersFiller() {
		slotName = countryOfHeadquarters;
	}
	
	@Override
	public void predict(SFEntity mention, Map<String, String> annotations) {
		String tokens = annotations.get(SFConstants.TOKENS);
		if (!isORG(mention))
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
		
		TregexPattern p = TregexPattern.compile("NNPS|NNP >> (NP << /headquarters/ < PP) > (NP > PP)");
		TregexMatcher m = p.matcher(t);
		
		ArrayList<String> possiblePlaces = new ArrayList<String>();
		while(m.find()) {
			possiblePlaces.add(m.getMatch().firstChild().value());
		}
		
		// not very good, need a way to combine multiple word place names
		// also check for LOCATION tags
		for(String place : possiblePlaces) {
			if(isCountry(place)) {
				SFEntity.SingleAnswer country = new SFEntity.SingleAnswer();
				country.answer = place;
				country.doc = getFilename(annotations);
				mention.answers.put(countryOfHeadquarters, country);
			} else if(isStateProv(place)) {
				SFEntity.SingleAnswer state = new SFEntity.SingleAnswer();
				state.answer = place;
				state.doc = getFilename(annotations);
				mention.answers.put(stateOfHeadquarters, state);
			} else {
				SFEntity.SingleAnswer city = new SFEntity.SingleAnswer();
				city.answer = place;
				city.doc = getFilename(annotations);
				mention.answers.put(cityOfHeadquarters, city);
			}
		}
	}
}
