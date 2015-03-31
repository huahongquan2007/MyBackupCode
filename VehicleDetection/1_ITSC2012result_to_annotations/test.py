import os

for dirname, dirnames, filenames in os.walk('/home/robotbase/DataDrive/Dataset/motorway/dataset'):
    # print path to all filenames.
    for filename in filenames:
        print os.path.join(dirname, filename)