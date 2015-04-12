#!/usr/bin/python

# Parse server_sample.out

import sys, getopt, logging, pdb, math
from collections import deque

logger = logging.getLogger(__name__)

sensorVals1 = [] #list of values for sensor 1
sensorVals2 = [] #list of values for sensor 1
sensorVals3 = [] #list of values for sensor 1
sensorVals4 = [] #list of values for sensor 1
sensorVals5 = [] #list of values for sensor 1
sensorVals6 = [] #list of values for sensor 1
sensorVals7 = [] #list of values for sensor 1
sensorVals8 = [] #list of values for sensor 1
sensorVals9 = [] #list of values for sensor 1
sensorVals10 = [] #list of values for sensor 1
sensorVals11 = [] #list of values for sensor 1
sensorVals12 = [] #list of values for sensor 1
sensorVals13 = [] #list of values for sensor 1
sensorVals14 = [] #list of values for sensor 1
sensorVals15 = [] #list of values for sensor 1
sensorVals16 = [] #list of values for sensor 1

def resetSensorValueArrays():
  del sensorVals1[:]
  del sensorVals2[:]
  del sensorVals3[:]
  del sensorVals4[:]
  del sensorVals5[:]
  del sensorVals6[:]
  del sensorVals7[:]
  del sensorVals8[:]
  del sensorVals9[:]
  del sensorVals10[:]
  del sensorVals11[:]
  del sensorVals12[:]
  del sensorVals13[:]
  del sensorVals14[:]
  del sensorVals15[:]
  del sensorVals16[:]


def appendSensorValue(sensorId, sensorVal):
  if (sensorId == 1):
    sensorVals1.append(sensorVal)
  elif (sensorId == 2):
    sensorVals2.append(sensorVal)
  elif (sensorId == 3):
    sensorVals3.append(sensorVal)
  elif (sensorId == 4):
    sensorVals4.append(sensorVal)
  elif (sensorId == 5):
    sensorVals5.append(sensorVal)
  elif (sensorId == 6):
    sensorVals6.append(sensorVal)
  elif (sensorId == 7):
    sensorVals7.append(sensorVal)
  elif (sensorId == 8):
    sensorVals8.append(sensorVal)
  elif (sensorId == 9):
    sensorVals9.append(sensorVal)
  elif (sensorId == 10):
    sensorVals10.append(sensorVal)
  elif (sensorId == 11):
    sensorVals11.append(sensorVal)
  elif (sensorId == 12):
    sensorVals12.append(sensorVal)
  elif (sensorId == 13):
    sensorVals13.append(sensorVal)
  elif (sensorId == 14):
    sensorVals14.append(sensorVal)
  elif (sensorId == 15):
    sensorVals15.append(sensorVal)
  elif (sensorId == 16):
    sensorVals16.append(sensorVal)

# assumed dataset format
# index=    0             1               2             3          4        5
#        <empty>, <event_iteration>, <timestamp>, <sensor_type>, <axis>, <value>
def parseRawDataSet(inputfile, sensorType, windowSize, axis):
  sensor1 = 1
  sensor2 = 2
  interval = 20
  inputfileCols = inputfile.split('.')
  fin = open(inputfile, 'r')
  outputfile = inputfileCols[0] + '_sanitized_all.csv'
  print("Output file is " + outputfile)
  fout = open(outputfile, 'w')

  Lines = fin.readlines()

  startTime1 = 0
  timer1 = 0
  sensorCounter = 0
  outputLine = ""
  for i in range(len(Lines)):
    line = Lines[i]
    cols = line.split(',')
    if i == 0:
      # print header
      outputLine = "timestamp, s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15, s16n\n"
      fout.write(outputLine)
      outputLine = ""
    elif i == 1:  
      # DEBUG print("count start with values = " + cols[0] + "," + cols[1] + "," + cols[2])
      startTime1 = int(cols[2])
      appendSensorValue(int(cols[0]), int(cols[1]))
    else:  
      if int(cols[0]) == sensor1:
        timer1 = int(cols[2]) - startTime1
        if timer1 < interval:
          # store the sensor value here 
          appendSensorValue(int(cols[0]), int(cols[1]))
        else:
          # print("elapsed time " + str(timer1))
          # calculate average sensor value
          mean1 = sum(sensorVals1) / len(sensorVals1)
          mean2 = sum(sensorVals2) / len(sensorVals2)
          mean3 = sum(sensorVals3) / len(sensorVals3)
          mean4 = sum(sensorVals4) / len(sensorVals4)
          mean5 = sum(sensorVals5) / len(sensorVals5)
          mean6 = sum(sensorVals6) / len(sensorVals6)
          mean7 = sum(sensorVals7) / len(sensorVals7)
          mean8 = sum(sensorVals8) / len(sensorVals8)
          mean9 = sum(sensorVals9) / len(sensorVals9)
          mean10 = sum(sensorVals10) / len(sensorVals10)
          mean11 = sum(sensorVals11) / len(sensorVals11)
          mean12 = sum(sensorVals12) / len(sensorVals12)
          mean13 = sum(sensorVals13) / len(sensorVals13)
          mean14 = sum(sensorVals14) / len(sensorVals14)
          mean15 = sum(sensorVals15) / len(sensorVals15)
          mean16 = sum(sensorVals16) / len(sensorVals16)
          
          # debugOut = "sensor 6: "
          # for val in sensorVals6:
          #   debugOut += str(val) + "," 
          # debugOut += "\n"
          # print(debugOut)

          outputLine += str(startTime1) + ","
          outputLine += str(mean1) + "," + str(mean2) + "," + str(mean3) + "," + str(mean4) + "," + str(mean5) + "," + str(mean6) + "," + str(mean7) + "," + str(mean8) + ","
          outputLine += str(mean9) + "," + str(mean10) + "," + str(mean11) + "," + str(mean12) + "," + str(mean13) + "," + str(mean14) + "," + str(mean15) + "," + str(mean16)
          outputLine += "\n"
          fout.write(outputLine)
          resetSensorValueArrays()
          startTime1 = int(cols[2])
          appendSensorValue(int(cols[0]), int(cols[1]))
          outputLine = ""

          # DEBUG print("count reset with values = " + cols[0] + "," + cols[1] + "," + cols[2])
      else:
        appendSensorValue(int(cols[0]), int(cols[1]))
            
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
   

