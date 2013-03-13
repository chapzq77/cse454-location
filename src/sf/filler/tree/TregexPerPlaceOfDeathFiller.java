package sf.filler.tree;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.Tree;

import sf.SFConstants;
import sf.SFEntity;
import sf.retriever.CorefProvider;

public class TregexPerPlaceOfDeathFiller extends BaseTregexFiller {

	private static final String countryOfDeath = "per:country_of_death";
	private static final String stateOfDeath = "per:stateorprovince_of_death";
	private static final String cityOfDeath = "per:city_of_death";

	public TregexPerPlaceOfDeathFiller() {
		slotNames.add(countryOfDeath);
		slotNames.add(stateOfDeath);
		slotNames.add(cityOfDeath);
	}
	
	@Override
	public void predict(SFEntity mention, Map<String, String> annotations,
			CorefProvider sentenceCoref) {
		String tokens = annotations.get(SFConstants.TOKENS);
		if (!isPER(mention))
			return;
		if (containsName(mention, tokens, sentenceCoref) == null)
			return;
		
		String cjtext = annotations.get(SFConstants.CJ);
		String filename = getFilename(annotations);
		Tree t = null;
		
		try {
			t = new PennTreeReader(new StringReader(cjtext)).readTree();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<String> places = getMatchNames("NNP|NNPS >> (NP >> (PP < (IN < /^(in|at)$/) > (VP < (VBD|VBN < /^died$/))))", t, annotations, sentenceCoref);
		
		// TODO not very good, need a way to combine multiple word place names
		// TODO also check for LOCATION tags
		for(String placeName : places) {
			if ( placeName == null ) continue;
			if(isCountry(placeName)) {
				if(!mention.ignoredSlots.contains(countryOfDeath)) {
					mention.addAnswer(countryOfDeath, placeName, filename);
				}
			} else if(isStateProv(placeName)) {
				if(!mention.ignoredSlots.contains(stateOfDeath)) {
					mention.addAnswer(stateOfDeath, placeName, filename);
				}
			} else {
				if(!mention.ignoredSlots.contains(cityOfDeath)) {
					mention.addAnswer(cityOfDeath, placeName, filename);
				}
			}
		}
	}

}
