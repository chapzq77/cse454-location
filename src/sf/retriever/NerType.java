package sf.retriever;

/**
 * The type of an entity, as determined by the Stanford Named Entity Recognizer.
 * 
 * @author Jeffrey Booth
 */
public enum NerType {
	LOCATION, DATE, TIME,
	PERSON, ORGANIZATION, MONEY, NUMBER, ORDINAL, DURATION, PERCENT, SET,
	MISC, O
}
