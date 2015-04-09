#!/usr/bin/python

# Parse server_sample.out

import sys, getopt, logging, pdb
from collections import deque

logger = logging.getLogger(__name__)

# assumed dataset format
# index=    0             1               2             3          4        5
#        <empty>, <event_iteration>, <timestamp>, <sensor_type>, <axis>, <value>
def parseRawDataSet(inputfile, sensorType, windowSize, axis):
  tapTotal = 250
  noneTotal = 250
  motionTotal = 0
  fin = open(inputfile, 'r')
  outputfile = 'weka_dataset.csv'
  print("Output file is " + outputfile)
  fout = open(outputfile, 'w')

  Lines = fin.readlines()

  noneCount = 0
  tapCount = 0
  motionCount = 0
  for i in range(len(Lines)):
    line = Lines[i]
    if i == 0:  
      # print header
      fout.write(line)
    else:  
      cols = line.split(',')
      if "none" in cols[6] and noneCount < noneTotal:
        outputLine = ','.join(cols)
        fout.write(outputLine)
        noneCount += 1
      elif "tap" in cols[6] and tapCount < tapTotal:
        outputLine = ','.join(cols)
        fout.write(outputLine)
        tapCount += 1
      elif "motion" in cols[6] and motionCount < motionTotal:
        outputLine = ','.join(cols)
        fout.write(outputLine)
        motionCount += 1

  fout.close()
  fin.close()

def main(argv):
  inputfile = ''
  outputfile = ''
  window = 20
  try:
    opts, args = getopt.getopt(argv,"hi:o:l:w:")
  except getopt.GetoptError:
    print 'attrTimeParser.py -i <inputfile> -w <windowSize>'
    sys.exit(2)
  for opt, arg in opts:
    if opt == '-h':
      print 'attrTimeParser.py -i <inputfile> -w <windowSize>'
      sys.exit()
    elif opt in ("-i", "--ifile"):
      inputfile = arg
    elif opt in ("-o", "--ofile"):
      outputfile = arg
    elif opt in ("-w"):
      window = int(arg)
    elif opt in ("-l"):
      loglevel = arg
       # assuming loglevel is bound to the string value obtained from the
	     # command line argument. Convert to upper case to allow the user to
	     # specify --log=DEBUG or --log=debug
      numeric_level = getattr(logging, loglevel.upper(), None)
      if not isinstance(numeric_level, int):
        raise ValueError('Invalid log level: %s' % loglevel)
      logging.basicConfig(level=numeric_level)
	     
  logger.info('Input file is "' + inputfile)	  	    
  logger.info('Window is ' + str(window))
  parseRawDataSet(inputfile, 'acc', window, 'z')
   
   
   
if __name__ == "__main__":
  main(sys.argv[1:])
   

