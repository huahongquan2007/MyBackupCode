import os

negative = open('negatives.txt', 'w')
onlyfiles = []
for dirname, dirnames, filenames in os.walk('/home/robotbase/VehicleDetectionOutput'):
    # print path to all filenames.
    for filename in filenames:
        onlyfiles.append(os.path.join(dirname, filename))
        negative.write(os.path.join(dirname, filename) + '\n')

print len(onlyfiles)

