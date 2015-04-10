#!/usr/bin/python

# Parse server_sample.out

import sys, getopt, logging, pdb, math
from collections import deque

logger = logging.getLogger(__name__)

# assumed dataset format
# index=    0             1               2             3          4        5
#        <empty>, <event_iteration>, <timestamp>, <sensor_type>, <axis>, <value>
def parseRawDataSet(inputfile, sensorType, windowSize, axis):
  sensor1 = 1
  sensor2 = 2
  interval = 20
  fin = open(inputfile, 'r')
  outputfile = 'sanitized.csv'
  print("Output file is " + outputfile)
  fout = open(outputfile, 'w')

  Lines = fin.readlines()

  startTime1 = 0
  timer1 = 0
  sensorVals1 = [] #list of values for sensor 1
  for i in range(len(Lines)):
    line = Lines[i]
    if i == 0 or i == 1:  
      # print header
      fout.write(line)
    else:  
      cols = line.split(',')
      if int(cols[0]) == sensor1:
        if startTime1 == 0:
          # fout.write(outputLine)
          startTime1 = int(cols[2])
          sensorVals1 = []
          # outputLine = ','.join(cols)
        else:
          timer1 = int(cols[2]) - startTime1
          if timer1 < interval:
            # store the sensor value here 
            sensorVals1.append(int(cols[1]))
          else:
            print("elapsed time " + str(timer1))
            # calculate average sensor value
            mean1 = sum(sensorVals1) / len(sensorVals1)
            cols[1] = str(mean1)
            outputLine = ','.join(cols)
            fout.write(outputLine)
            startTime1 = 0
            
      # if "none" in cols[6] and noneCount < noneTotal:
      #   outputLine = ','.join(cols)
      #   fout.write(outputLine)
      #   noneCount += 1
      # elif "tap" in cols[6] and tapCount < tapTotal:
      #   outputLine = ','.join(cols)
      #   fout.write(outputLine)
      #   tapCount += 1
      # elif "motion" in cols[6] and motionCount < motionTotal:
      #   outputLine = ','.join(cols)
      #   fout.write(outputLine)
      #   motionCount += 1

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
   

