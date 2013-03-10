package sf;

import util.FileUtil;

/**
 * 
 * @author Xiao Ling
 */

public class ErrorAnalysis {
	public static void main(String[] args) {
		if ( args.length < 2 ) {
			System.err.println( "Missing args." );
			System.err.println( "You need to provide the label and the gold " +
					"annotation file to read correct answers from." );
		}
		getLabels(args[0], args[1]);
	}
	/**
	 * print out all the correct answers for the slot
 	 * @param slot
 	 */
	public static void getLabels(String slot, String labelFile) {
		String[] lines = FileUtil.getTextFromFile(labelFile).split(
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

