cse454-location
===============

This program extracts location information about people or organizations from
documents. It is intended to operate on the data in the [TAC KBP Challenge][tackbp].

Requirements
------------

 * Java 7
 * Ant

How to Use
----------

You will need to run the main class `cli.Extractor`, and provide it with the
following arguments:

 * A *test set*. The set contains a list of queries, which contain people
   and organizations to find information about, in a file named queries.xml.
   It also contains a list of expected contents for the slots in the
   annotations.gold file. Several test sets are provided in the `test-data`
   directory.
 * A *list of slot filler classes* to use, in order to extract the information.
   If not provided, all the fillers will be used.
 * A *corpus* or *corpus sample*. The slot fillers will evaluate the text in
   this corpus to fill the slots in the queries. Several samples from the
   full KBP corpus are provided in the `corpus-samples` directory.
 * *What to do*: run the queries and/or evaluate the filler's predicted
   annotations against the expected "gold" annotations from the test set.

Run `cli.Extractor` to see a list of arguments which you can provide. It will
probably look like this:

	run          - Run the evaluator.
	eval         - Evaluate the results.
	-v           - Print verbose output.
	-m			 - Print mistake breakdown.
	-limit n     - Limit to n sentences. If n == 0, the number of sentences is not limited.
	
	-corpus x    - Use data source x, which is the path to the folder containing
	               sentences.txt files. This path may be 
	               relative to the corpus-samples/ 
	               directory in your repository, which includes the following:
	               * large-corpus
	               * only-answers
	               * unfiltered-corpus
	               * warmup
	tr-homes     - Use class sf.filler.tree.TregexPerPlacesOfResidenceFiller
	tr-hq        - Use class sf.filler.tree.TregexOrgPlaceOfHeadquartersFiller
	reg          - Use class sf.filler.regex.RegexLocationFiller
	tr-birth     - Use class sf.filler.tree.TregexPerPlaceOfBirthFiller
	tr-death     - Use class sf.filler.tree.TregexPerPlaceOfDeathFiller
	(class name) - Use the filler with the given class name.
	all-fillers  - Use all fillers in filterAbbrevs.
	-test-set x  - Use the queries and annotations in the given directory x.
	               This path may be relative to the test-data/
	               directory in your repository, which includes the following:
	               * eval-2010
	               * training-2010
	               * warmup

Here is an example that runs the slot fillers over a small corpus,
using queries from the 2010 KBP training data. The `reg` means that only the
regular expression slot filler will be run. The working directory must be
the root folder of your checked-out copy of this repository.

	java -cp bin;libs/stanford-tregex.jar;libs/junit-4.10.jar cli.Extractor run eval reg -corpus only-answers -test-set training-2010 -v

If you're using Eclipse, you should create a *Run Configuration* using
`cli.Extractor` as the main class.
In this case, the example arguments would be:

	run eval reg -corpus only-answers -test-set training-2010 -v

You may use Ant for building and running stuff too, but its build file is out
of date.

 * `ant` will compile the project.

Code Organization
-----------------

Directories in the Repository

 * src - Source code.
 * corpus-samples - Contains subdirectories which have samples from the full corpus.
 * data - Extra data used by the slot fillers.
 * libs - JARs of external libraries used by this application.
 * test-data - Contains subdirectories which have queries and expected "gold" annotations, for testing the program performance.


Contributors
------------

 * Alex Cartmell
 * Jeff Booth
 * Tim Plummer

[tackbp]: http://www.nist.gov/tac/2012/KBP/index.html "TAC KBP Challenge"
