#!/usr/bin/python

# Parse server_sample.out

import sys, getopt, logging, pdb
from collections import deque

logger = logging.getLogger(__name__)

# assumed dataset format
# index=    0             1               2             3          4        5
#        <empty>, <event_iteration>, <timestamp>, <sensor_type>, <axis>, <value>
def parseRawDataSet(inputfile, sensorType, windowSize, axis):
   fin = open(inputfile, 'r')
   outputfile = 'parsed_output.csv'
   logger.info("Output file is " + outputfile)
   fout = open(outputfile, 'w')
   heading = ['eventIndex']
   for i in range(0,windowSize):
      heading.append('t' + str(i))
   fout.write(",".join(heading) + ',tapType\n')

   Lines = fin.readlines()
   
   windowIndex = 0
   eventIter = ''
   outputQueue = deque([])
   for i in range(len(Lines)):
      line = Lines[i]
      if i != 0:  # ignore the first line
         cols = line.split(',')
         if cols[3] == sensorType and cols[4] == axis:
            if windowIndex == 0:
               eventIter = cols[1].strip() # want to record the event iteration number
            outputQueue.append(cols[5].strip())
            windowIndex+= 1

            if (windowIndex == windowSize):
               # window is full - need to output
               outputLine = eventIter + ','
               outputLine += ','.join(outputQueue)
               outputLine += ',notap' '\n'
               
               # logger.info(outputLine)
               fout.write(outputLine)
               
               # reset window index and 
               windowIndex -= 1 
               eventIter = str(int(eventIter) + 1)
               outputQueue.popleft()


   searchWord = 'BENCHMARK'

   counter = 0 #counter to keep track of first benchmark data line
   time = 0
   timestampPrev = 0
   timestampCurr = 0
   
   # for i in range(len(Lines)):
   #     if searchWord in Lines[i]:
   #         if counter != 1: #don't write first benchmark data line
   #             #fout.write(Lines[i])
   #             cols = Lines[i].split(',')
   #             colTimestamp = cols[0]
   #             timestampCurr = colTimestamp.split()[1];
   #             if counter == 0:
   #                 cols.insert(1, "Time (s)")
   #             if counter == 2:
   #                 cols.insert(1, time)
   #                 timestampPrev = timestampCurr
   #             elif counter > 2:
   #                 time = ( (int(timestampCurr) - int(timestampPrev)) / float(1000) ) + float(time)
   #                 timestampPrev = timestampCurr
   #                 cols.insert(1, time)
				   
   #             fout.write(','.join(str(v) for v in cols))
   #         counter += 1
		
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
   

