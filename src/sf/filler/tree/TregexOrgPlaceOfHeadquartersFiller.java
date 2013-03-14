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
		String mentionContext = containsName(mention, tokens, sentenceCoref);
		if (mentionContext == null)
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
		
		String mentionNP = "(NP";
		for(String mentionToken : mentionContext.split(" ")) {
			mentionToken = mentionToken.replaceAll("[/\\)\\(\\:]", "");
			mentionNP += " << /(" + mentionToken + ")|(" + mentionToken.toLowerCase() + ")/";
		}
		mentionNP += ")";

		// Try to match for Place's Mention
		//String actualMention = "(NP < /" + tokens.split(" ")[mentionIndex] + "/)";
		places.addAll(getMatchNames("NP < (NNP > (NP < POS) >> " + mentionNP + ")", t, annotations, sentenceCoref));
		
		// Try to match Place-based Mention
		places.addAll(getMatchNames("ADJP < /based/", t, annotations, sentenceCoref));

		// Try to match adjective phrases
		places.addAll(getMatchNames("JJ > " + mentionNP, t, annotations, sentenceCoref));

		// Try to match verbed in Place
		places.addAll(getMatchNames("NP > (PP < (IN < /^in$/) > (VP < (VBN < /^((based)|(located)|(headquartered)|(centered)|(active))$/)))", t, annotations, sentenceCoref));

		// These don't work so well. (Matches other arbitrary information, doesn't match locations, etc.)
		
		//places.addAll(getMatchNames("NP >> (PP < (IN < /^in$/) $-- " + mentionNP + ")", t, annotations, sentenceCoref));
		
		// Try to match parenthetical information
		//places.addAll(getMatchNames("NP ,, (/,/ , " + mentionNP + ")", t, annotations, sentenceCoref));
		
		//places.addAll(getMatchNames("NP < (NNPS|NNP >> (NP << /headquarters/ < PP) > (NP > PP))", t, annotations, sentenceCoref));
		
		
		for(String placeName : places) {
			if ( placeName == null ) continue;

			if(isCountry(placeName)) {
				if(!mention.ignoredSlots.contains(countryOfHeadquarters)) {
					mention.addAnswer(countryOfHeadquarters, placeName, filename);
				}
			} else if(isUSState(placeName)) {
				if(!mention.ignoredSlots.contains(countryOfHeadquarters)) {
					mention.addAnswer(countryOfHeadquarters, "US", filename);
				}
				if(!mention.ignoredSlots.contains(stateOfHeadquarters)) {
					mention.addAnswer(stateOfHeadquarters, placeName, filename);
				}
			} else if(isCanadianProvince(placeName)) {
				if(!mention.ignoredSlots.contains(countryOfHeadquarters)) {
					mention.addAnswer(countryOfHeadquarters, "Canada", filename);
				}
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
