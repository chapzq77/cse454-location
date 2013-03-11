package sf.filler.tree;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;

import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.Tree;

import sf.SFConstants;
import sf.SFEntity;
import sf.retriever.CorefProvider;

public class TregexOrgPlaceOfHeadquartersFiller extends BaseTregexFiller {

	private static final String countryOfHeadquarters = "org:country_of_headquarters";
	private static final String stateOfHeadquarters = "org:stateorprovince_of_headquarters";
	private static final String cityOfHeadquarters = "org:city_of_headquarters";

	public TregexOrgPlaceOfHeadquartersFiller() {
		slotNames.add(countryOfHeadquarters);
		slotNames.add(stateOfHeadquarters);
		slotNames.add(cityOfHeadquarters);
	}
	
	@Override
	public void predict(SFEntity mention, Map<String, String> annotations,
			CorefProvider sentenceCoref) {
		String tokens = annotations.get(SFConstants.TOKENS);
		if (!isORG(mention))
			return;
		if (!containsName(mention, tokens, sentenceCoref))
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
		
		ArrayList<String> places = new ArrayList<String>();
		
		// Try to match for Place's Mention
		StringBuilder possessiveMatch = new StringBuilder("NP < (NNP > (NP < POS) >> (NP");
		for(String mentionToken : mention.mentionString.split(" ")) {
			possessiveMatch.append(" << /" + mentionToken + "/");
		}
		possessiveMatch.append("))");
		places.addAll(getMatchNames(possessiveMatch.toString(), t, annotations, sentenceCoref));
		
		//places.addAll(getMatchNames("NNPS|NNP >> (NP << /headquarters/ < PP) > (NP > PP)", t));
		
		for(String placeName : places) {
			if(isCountry(placeName)) {
				if(!mention.ignoredSlots.contains(countryOfHeadquarters)) {
					mention.addAnswer(countryOfHeadquarters, placeName, filename);
				}
			} else if(isStateProv(placeName)) {
				if(!mention.ignoredSlots.contains(stateOfHeadquarters)) {
					mention.addAnswer(stateOfHeadquarters, placeName, filename);
				}
			} else {
				if(!mention.ignoredSlots.contains(cityOfHeadquarters)) {
					mention.addAnswer(cityOfHeadquarters, placeName, filename);
				}
			}
		}
	}
}
