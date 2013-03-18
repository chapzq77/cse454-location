import os
import sys
from sys import argv
import xml.etree.ElementTree as xml

if len(argv) < 3:
	print >> sys.stderr, "Missing file to split, and number of splits to make."
	sys.exit(1)

inputFile = argv[1]
splits = int(argv[2])

tree = xml.parse(os.path.join(inputFile, 'queries.xml'))
rootElement = tree.getroot()
queries = rootElement.findall("query")
if queries == None:
	sys.exit(2)

splitsize = len(queries) / splits
if len(queries) % splits > 0:
	splitsize += 1

offset = 0
for i in xrange(splits):
	root = xml.Element('kbpslotfill')
	for child in queries[offset:offset+splitsize]:
		root.append(child)
	offset += splitsize
	newdir = "%s-%d" % (inputFile, i)
	os.mkdir(newdir)
	xml.ElementTree(root).write(os.path.join(newdir, "queries.xml"))
