import cv2

lines = [line for line in open('Datasets/IBUG/keypoints.txt')]
paths = [line for line in open('Datasets/IBUG/images.txt')]
keypoints_refine = open('Datasets/IBUG/keypoints_reduce.txt', 'w')

for index in range(0, len(lines)):
    print str(index) + '/' + str(len(lines))
    keypoints = lines[index].split(' ')
    num_of_landmark = 68
    # img = cv2.imread(paths[index].strip(), cv2.CV_LOAD_IMAGE_COLOR)

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
        keypoints_refine.write(i + ' ')
    for i in cur_keypoint_y:
        keypoints_refine.write(i + ' ')
    keypoints_refine.write('\n')
    # cv2.imshow("image", img)
    # cv2.waitKey(10)