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

public class TregexPerPlaceOfDeathFiller extends Filler {

	private static final String countryOfDeath = "per:country_of_death";
	private static final String stateOfDeath = "per:stateorprovince_of_death";
	private static final String cityOfDeath = "per:city_of_death";

	public TregexPerPlaceOfDeathFiller() {
		slotNames.add(countryOfDeath);
		slotNames.add(stateOfDeath);
		slotNames.add(cityOfDeath);
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
		
		TregexPattern p = TregexPattern.compile("NNP|NNPS >> (NP >> (PP < (IN < /^(in|at)$/) > (VP < (VBD|VBN < /^died$/))))");
		TregexMatcher m = p.matcher(t);
		
		ArrayList<String> possiblePlaces = new ArrayList<String>();
		while(m.find()) {
			possiblePlaces.add(m.getMatch().firstChild().value());
		}
		
		// TODO not very good, need a way to combine multiple word place names
		// TODO also check for LOCATION tags
		for(String place : possiblePlaces) {
			if(isCountry(place)) {
				addAnswer(mention, annotations, place, countryOfDeath);
			} else if(isStateProv(place)) {
				addAnswer(mention, annotations, place, stateOfDeath);
			} else {
				addAnswer(mention, annotations, place, cityOfDeath);
			}
		}
	}

}
