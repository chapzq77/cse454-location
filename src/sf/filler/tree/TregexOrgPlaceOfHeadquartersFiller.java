package sf.filler.tree;

import java.io.IOException;
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
import sf.filler.Filler;
import sf.retriever.CorefProvider;

public class TregexOrgPlaceOfHeadquartersFiller extends Filler {

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
		
		TregexPattern p = TregexPattern.compile("NNPS|NNP >> (NP << /headquarters/ < PP) > (NP > PP)");
		TregexMatcher m = p.matcher(t);
		
		Tree parent = null;
		List<String> places = new ArrayList<String>();
		StringBuilder place = new StringBuilder();
		if(m.find()) {
			parent = m.getMatch().parent();
			place.append(m.getMatch().firstChild().value());
			while(m.find()) {
				if(m.getMatch().parent().equals(parent)) {
					place.append(" ");
					place.append(m.getMatch().firstChild().value());
				} else {
					places.add(place.toString());
					place = new StringBuilder();
					place.append(m.getMatch().firstChild().value());
				}
			}
		}
		places.add(place.toString());
		
		// not very good, need a way to combine multiple word place names
		// also check for LOCATION tags
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
