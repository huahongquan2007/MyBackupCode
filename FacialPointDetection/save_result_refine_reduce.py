lines = [line for line in open('save_result_kaggle_refine.txt')]

output = open('save_result_kaggle_reduce.txt', 'w')

selected_keypoints = [3, 9, 15, 18, 20, 22, 23, 25, 27, 37, 40, 43, 46, 33, 35, 49, 52, 55, 58]

for index, line in enumerate(lines):

    words = line.strip().split('_')
    output.write(words[0] + '_' + words[1] + '_')
    keypoints = words[2].split(' ')
    print keypoints
    for i, key in enumerate(keypoints):
        if i in selected_keypoints:
            output.write(key + ' ')
    output.write('\n')