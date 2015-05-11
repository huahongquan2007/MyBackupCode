lines = [line for line in open('save_result_kaggle_refine.txt')]

output = open('save_result_kaggle_reduce.txt', 'w')

selected_keypoints = [3, 9, 15, 18, 20, 22, 23, 25, 27, 37, 40, 43, 46, 33, 35, 49, 52, 55, 58]

num_of_landmark = 68

for index, line in enumerate(lines):

    words = line.strip().split('_')
    output.write(words[0] + '_' + words[1] + '_')
    keypoints = words[2].split(' ')
    selected_keypoints = [3, 9, 15, 18, 20, 22, 23, 25, 27, 37, 40, 43, 46, 33, 35, 49, 52, 55, 58]
    cur_keypoint_x = []
    cur_keypoint_y = []
    for i in range(0, num_of_landmark):
        if i + 1 in selected_keypoints:
            cur_keypoint_x.append(keypoints[i])
            cur_keypoint_y.append(keypoints[i + num_of_landmark])
            pos = (int(float(keypoints[i])), int(float(keypoints[i + num_of_landmark])))
            # cv2.circle(img, pos, 3, (0, 255, 0), -1)

    for i in cur_keypoint_x:
        output.write(i + ' ')
    for i in cur_keypoint_y:
        output.write(i + ' ')
    output.write('\n')