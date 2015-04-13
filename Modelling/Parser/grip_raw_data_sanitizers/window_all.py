#!/usr/bin/python

# Parse server_sample.out

import sys, getopt, logging, pdb, math
from collections import deque

logger = logging.getLogger(__name__)

sensorVals1 = deque([]) #queue of values for sensor 1
sensorVals2 = deque([]) #queue of values for sensor 2
sensorVals3 = deque([]) #queue of values for sensor 3
sensorVals4 = deque([]) #queue of values for sensor 4
sensorVals5 = deque([]) #queue of values for sensor 5
sensorVals6 = deque([]) #queue of values for sensor 6
sensorVals7 = deque([]) #queue of values for sensor 7
sensorVals8 = deque([]) #queue of values for sensor 8
sensorVals9 = deque([]) #queue of values for sensor 9
sensorVals10 = deque([]) #queue of values for sensor 10
sensorVals11 = deque([]) #queue of values for sensor 11
sensorVals12 = deque([]) #queue of values for sensor 12
sensorVals13 = deque([]) #queue of values for sensor 13
sensorVals14 = deque([]) #queue of values for sensor 14
sensorVals15 = deque([]) #queue of values for sensor 15
sensorVals16 = deque([]) #queue of values for sensor 16

def resetSensorValueArrays():
  sensorVals1.clear()
  sensorVals2.clear()
  sensorVals3.clear()
  sensorVals4.clear()
  sensorVals5.clear()
  sensorVals6.clear()
  sensorVals7.clear()
  sensorVals8.clear()
  sensorVals9.clear()
  sensorVals10.clear()
  sensorVals11.clear()
  sensorVals12.clear()
  sensorVals13.clear()
  sensorVals14.clear()
  sensorVals15.clear()
  sensorVals16.clear()

def slideSensorValueArrays():
  sensorVals1.popleft()
  sensorVals2.popleft()
  sensorVals3.popleft()
  sensorVals4.popleft()
  sensorVals5.popleft()
  sensorVals6.popleft()
  sensorVals7.popleft()
  sensorVals8.popleft()
  sensorVals9.popleft()
  sensorVals10.popleft()
  sensorVals11.popleft()
  sensorVals12.popleft()
  sensorVals13.popleft()
  sensorVals14.popleft()
  sensorVals15.popleft()
  sensorVals16.popleft()

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

def meanLeft():
  left_sum = 0
  for val in sensorVals1:
    left_sum += int(val)
  for val in sensorVals2:
    left_sum += int(val)
  for val in sensorVals3:
    left_sum += int(val)
  for val in sensorVals4:
    left_sum += int(val)
  for val in sensorVals5:
    left_sum += int(val)
  for val in sensorVals6:
    left_sum += int(val)
  for val in sensorVals7:
    left_sum += int(val)
  for val in sensorVals8:
    left_sum += int(val)

  return left_sum / (8 * len(sensorVals1))

def meanRight():
  right_sum = 0
  for val in sensorVals9:
    right_sum += int(val)
  for val in sensorVals10:
    right_sum += int(val)
  for val in sensorVals11:
    right_sum += int(val)
  for val in sensorVals12:
    right_sum += int(val)
  for val in sensorVals13:
    right_sum += int(val)
  for val in sensorVals14:
    right_sum += int(val)
  for val in sensorVals15:
    right_sum += int(val)
  for val in sensorVals16:
    right_sum += int(val)

  return right_sum / (8 * len(sensorVals9))

def sideNormalizedVarianceSensor(sensorId):
  variance = 0
  mean_left = meanLeft()
  len_left = len(sensorVals1)
  mean_right = meanRight()
  len_right = len(sensorVals9)

  sum_sensor = 0
  if (sensorId == 1):
    for val in sensorVals1:
      sum_sensor += math.pow(int(val) - mean_left, 2)
    variance = sum_sensor/len_left

  elif (sensorId == 2):
    for val in sensorVals2:
      sum_sensor += math.pow(int(val) - mean_left, 2)
    variance = sum_sensor/len_left

  elif (sensorId == 3):
    for val in sensorVals3:
      sum_sensor += math.pow(int(val) - mean_left, 2)
    variance = sum_sensor/len_left
    
  elif (sensorId == 4):
    for val in sensorVals4:
      sum_sensor += math.pow(int(val) - mean_left, 2)
    variance = sum_sensor/len_left

  elif (sensorId == 5):
    for val in sensorVals5:
      sum_sensor += math.pow(int(val) - mean_left, 2)
    variance = sum_sensor/len_left

  elif (sensorId == 6):
    for val in sensorVals6:
      sum_sensor += math.pow(int(val) - mean_left, 2)
    variance = sum_sensor/len(sensorVals6)

  elif (sensorId == 7):
    for val in sensorVals7:
      sum_sensor += math.pow(int(val) - mean_left, 2)
    variance = sum_sensor/len_left

  elif (sensorId == 8):
    for val in sensorVals8:
      sum_sensor += math.pow(int(val) - mean_left, 2)
    variance = sum_sensor/len_left

  elif (sensorId == 9):
    for val in sensorVals9:
      sum_sensor += math.pow(int(val) - mean_right, 2)
    variance = sum_sensor/len_right

  elif (sensorId == 10):
    for val in sensorVals8:
      sum_sensor += math.pow(int(val) - mean_right, 2)
    variance = sum_sensor/len_right

  elif (sensorId == 11):
    for val in sensorVals8:
      sum_sensor += math.pow(int(val) - mean_right, 2)
    variance = sum_sensor/len_right

  elif (sensorId == 12):
    for val in sensorVals8:
      sum_sensor += math.pow(int(val) - mean_right, 2)
    variance = sum_sensor/len_right

  elif (sensorId == 13):
    for val in sensorVals8:
      sum_sensor += math.pow(int(val) - mean_right, 2)
    variance = sum_sensor/len_right

  elif (sensorId == 14):
    for val in sensorVals8:
      sum_sensor += math.pow(int(val) - mean_right, 2)
    variance = sum_sensor/len_right

  elif (sensorId == 15):
    for val in sensorVals8:
      sum_sensor += math.pow(int(val) - mean_right, 2)
    variance = sum_sensor/len_right

  elif (sensorId == 16):
    for val in sensorVals8:
      sum_sensor += math.pow(int(val) - mean_right, 2)
    variance = sum_sensor/len_right

  return variance
# assumed dataset format
# index=    0             1               2             3          4        5
#        <empty>, <event_iteration>, <timestamp>, <sensor_type>, <axis>, <value>
def parseRawDataSet(inputfile, sensorType, windowSize, axis):
  sensor1 = 1
  sensor2 = 2
  interval = 20
  inputfileCols = inputfile.split('.')
  fin = open(inputfile, 'r')
  outputfile = inputfileCols[0] + '_windowed_' + str(windowSize) +'.csv'
  feature_out_file = inputfileCols[0] + '_featured_window_'+ str(windowSize) +'.csv'
  print("Output file is " + outputfile)
  print("Feature output file is " + feature_out_file)
  fout = open(outputfile, 'w')
  fout_feature = open(feature_out_file, 'w')

  Lines = fin.readlines()

  window_start_time = 0
  outputLine = ""
  feature_out_line = ''
  for i in range(len(Lines)):
    line = Lines[i]
    cols = line.split(',')

    if i == 0:
      # print header
      outputLine = "timestamp,sensorId,t0,t1,t2,t3,t4,t5,t6,t7,t8,t9,t10,t11,t12,t13,t14,t15,t16,t17,t18,t19,t20,t21,t22,t23,t24,t25,t26,t27,t28,t29\n"
      fout.write(outputLine)
      outputLine = ""
      feature_out_line = "timestamp,meanLeft,meanRight,varS1,varS2,varS3,varS4,varS5,varS6,varS7,varS8,varS9,varS10,varS11,varS12,varS13,varS14,varS15,varS16\n"
      fout_feature.write(feature_out_line)
      feature_out_line = ""
    else:  
      # DEBUG print("count start with values = " + cols[0] + "," + cols[1] + "," + cols[2])
      if (len(sensorVals1) == windowSize  ):
        # output first
        outputLine += window_start_time + ","
        outputLine += str(1) + "," 
        outputLine += ','.join(sensorVals1) + '\n'
        outputLine += window_start_time + ","
        outputLine += str(2) + "," 
        outputLine +=  ','.join(sensorVals2) + '\n'
        outputLine += window_start_time + ","
        outputLine += str(3) + "," 
        outputLine +=  ','.join(sensorVals3) + '\n'
        outputLine += window_start_time + ","
        outputLine += str(4) + "," 
        outputLine +=  ','.join(sensorVals4) + '\n'
        outputLine += window_start_time + ","
        outputLine += str(5) + "," 
        outputLine +=  ','.join(sensorVals5) + '\n'
        outputLine += window_start_time + ","
        outputLine += str(6) + "," 
        outputLine +=  ','.join(sensorVals6) + '\n'
        outputLine += window_start_time + ","
        outputLine += str(7) + "," 
        outputLine +=  ','.join(sensorVals7) + '\n'
        outputLine += window_start_time + ","
        outputLine += str(8) + "," 
        outputLine +=  ','.join(sensorVals8) + '\n'
        outputLine += window_start_time + ","
        outputLine += str(9) + "," 
        outputLine +=  ','.join(sensorVals9) + '\n'
        outputLine += window_start_time + ","
        outputLine += str(10) + "," 
        outputLine +=  ','.join(sensorVals10) + '\n'
        outputLine += window_start_time + ","
        outputLine += str(11) + "," 
        outputLine +=  ','.join(sensorVals11) + '\n'
        outputLine += window_start_time + ","
        outputLine += str(12) + "," 
        outputLine +=  ','.join(sensorVals12) + '\n'
        outputLine += window_start_time + ","
        outputLine += str(13) + "," 
        outputLine +=  ','.join(sensorVals13) + '\n'
        outputLine += window_start_time + ","
        outputLine += str(14) + "," 
        outputLine +=  ','.join(sensorVals14) + '\n'
        outputLine += window_start_time + ","
        outputLine += str(15) + "," 
        outputLine +=  ','.join(sensorVals15) + '\n'
        outputLine += window_start_time + ","
        outputLine += str(16) + "," 
        outputLine +=  ','.join(sensorVals16) + '\n'
        # print(outputLine)
        fout.write(outputLine)

        feature_out_line += window_start_time + ","
        feature_out_line += str(meanLeft()) + ','
        feature_out_line += str(meanRight()) + ','
        feature_out_line += str(sideNormalizedVarianceSensor(1)) + ','
        feature_out_line += str(sideNormalizedVarianceSensor(2)) + ','
        feature_out_line += str(sideNormalizedVarianceSensor(3)) + ','
        feature_out_line += str(sideNormalizedVarianceSensor(4)) + ','
        feature_out_line += str(sideNormalizedVarianceSensor(5)) + ','
        feature_out_line += str(sideNormalizedVarianceSensor(6)) + ','
        feature_out_line += str(sideNormalizedVarianceSensor(7)) + ','
        feature_out_line += str(sideNormalizedVarianceSensor(8)) + ','
        feature_out_line += str(sideNormalizedVarianceSensor(9)) + ','
        feature_out_line += str(sideNormalizedVarianceSensor(10)) + ','
        feature_out_line += str(sideNormalizedVarianceSensor(11)) + ','
        feature_out_line += str(sideNormalizedVarianceSensor(12)) + ','
        feature_out_line += str(sideNormalizedVarianceSensor(13)) + ','
        feature_out_line += str(sideNormalizedVarianceSensor(14)) + ','
        feature_out_line += str(sideNormalizedVarianceSensor(15)) + ','
        feature_out_line += str(sideNormalizedVarianceSensor(16)) + ','
        feature_out_line += '\n'
        fout_feature.write(feature_out_line)
        
        # then pop
        slideSensorValueArrays()
        outputLine = ""
        feature_out_line = ""
        # then append again
        window_start_time = cols[0]
        appendSensorValue(1, cols[1])
        appendSensorValue(2, cols[2])
        appendSensorValue(3, cols[3])
        appendSensorValue(4, cols[4])
        appendSensorValue(5, cols[5])
        appendSensorValue(6, cols[6])
        appendSensorValue(7, cols[7])
        appendSensorValue(8, cols[8])
        appendSensorValue(9, cols[9])
        appendSensorValue(10, cols[10])
        appendSensorValue(11, cols[11])
        appendSensorValue(12, cols[12])
        appendSensorValue(13, cols[13])
        appendSensorValue(14, cols[14])
        appendSensorValue(15, cols[15])
        appendSensorValue(16, str(int(cols[16])))
      else:
        window_start_time = cols[0]
        appendSensorValue(1, cols[1])
        appendSensorValue(2, cols[2])
        appendSensorValue(3, cols[3])
        appendSensorValue(4, cols[4])
        appendSensorValue(5, cols[5])
        appendSensorValue(6, cols[6])
        appendSensorValue(7, cols[7])
        appendSensorValue(8, cols[8])
        appendSensorValue(9, cols[9])
        appendSensorValue(10, cols[10])
        appendSensorValue(11, cols[11])
        appendSensorValue(12, cols[12])
        appendSensorValue(13, cols[13])
        appendSensorValue(14, cols[14])
        appendSensorValue(15, cols[15])
        appendSensorValue(16, str(int(cols[16])))

          # DEBUG print("count reset with values = " + cols[0] + "," + cols[1] + "," + cols[2])
  fout.close()
  fin.close()

def main(argv):
  inputfile = ''
  outputfile = ''
  window = 50
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
   

