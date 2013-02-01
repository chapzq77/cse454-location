package sf;

import util.FileUtil;

/**
 * 
 * @author Xiao Ling
 */

public class ErrorAnalysis {
	public static void main(String[] args) {
		getLabels("per:country_of_birth");
	}
	/**
	 * print out all the correct answers for the slot
 	 * @param slot
 	 */
	public static void getLabels(String slot) {
		String[] lines = FileUtil.getTextFromFile(SFConstants.labelFile).split(
				"\n");
		for (String line : lines) {
			String[] fields = line.split("\t");
			// Note: a set of files whose names begin with "eng" are ignored in this assignment.
 			if (fields[3].equals(slot) && fields[10].equals("1") && !fields[4].startsWith("eng")) {
				System.out.println(String.format("%s\t%s\t%s\t%s", fields[1],
						fields[3], fields[4], fields[8]));
			}
		}
	}
}

