import os
from os import listdir
from os.path import isfile, join, basename

lines = [line.strip() for line in open('ITSC2012results.txt')]
total = len(lines)
result = {}

## READ RESULT.TXT -> DICTIONARY

for index, line in enumerate(lines):
    line_split = line.split(":")
    file_id = line_split[0]
    file_boxes = line_split[1]
    print file_id
    box_entries = file_boxes.split(";")

    result_entry = []
    for box_entry in box_entries:
        if len(box_entry) > 0:
            box_entry_parts = box_entry.strip().rstrip().split(' ')
            print '---------%d----------' % len(box_entry_parts)
            validated = 1
            if len(box_entry_parts) > 5:
                validated = box_entry_parts[5]
            if validated == '1':
                x_min = box_entry_parts[0]
                x_max = box_entry_parts[1]
                y_min = box_entry_parts[2]
                y_max = box_entry_parts[3]

                pos = (x_min, x_max, y_min, y_max)
                result_entry.append(pos)

    result[file_id] = result_entry

print result['tme46_019506']


# Get list of files
#dataset = '/home/robotbase/DataDrive/Dataset/motorway/tmeMotorwayDataset_daylight/tme12/Right/'
#onlyfiles = [join(dataset, f) for f in listdir(dataset) if isfile(join(dataset, f))]

onlyfiles = []
for dirname, dirnames, filenames in os.walk('/home/robotbase/DataDrive/Dataset/motorway/dataset'):
    # print path to all filenames.
    for filename in filenames:
        onlyfiles.append(os.path.join(dirname, filename))


dataid = []
for path in onlyfiles:
    filename = basename(path)
    id  =  filename.split('-')[0]
    path_split = path.split('/')
    dir = path_split[ len(path_split) - 3 ]
    dataid.append(dir + '_' + id)

# Save to annotations.txt
fileOutput = open('annotations.txt', 'w')

total_samples = 0
for idx, id in enumerate(dataid):

    if result.has_key(id):

        num_accept = 0

        temp_output = ''
        for result_val in result[id]:
            w = int(result_val[2]) - int(result_val[0])
            h = int(result_val[3]) - int(result_val[1])

            if w < 50:
                continue
            temp_output = temp_output + result_val[0] + ' '
            temp_output = temp_output + result_val[1] + ' '
            temp_output = temp_output + str(w) + ' '
            temp_output = temp_output + str(h) + ' '
            num_accept += 1

        output = onlyfiles[idx] + ' '
        output = output + str( num_accept ) + ' '
        output = output + temp_output
        total_samples += num_accept
        fileOutput.write(output + '\n' )

fileTotal = open(str("annotations-size.txt"), 'w')
fileTotal.write(str(total_samples))
# for key, value in result.iteritems():
#     print key
#     print value


