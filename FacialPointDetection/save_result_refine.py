import os
import cv2
lines = [line for line in open('/home/robotbase/DataDrive/Dataset/Kaggle/list.txt')]
result_lines = [line for line in open('./save_result_kaggle.txt')]
result_refine = open('./save_result_kaggle_refine.txt', 'a')
num_of_landmark = 68

list_result = []

cv2.namedWindow("img", cv2.WINDOW_NORMAL)
total_size = len(result_lines)

index = 2161
while index < total_size:
    print "Process index: " + str(index)
    words = result_lines[index].split('_')
    img_idx = int(words[0])
    img_path = lines[img_idx].strip()
    img = cv2.imread(img_path, cv2.CV_LOAD_IMAGE_COLOR)

    keypoints = words[2].split(' ')
    for i in range(0, num_of_landmark):
        pos = (int(float(keypoints[i])), int(float(keypoints[i + num_of_landmark])))
        cv2.circle(img, pos, 3, (0, 255, 0), -1)

    cv2.imshow("img", img)

    isPress = False
    isSave = False
    while isPress == False:
        k = cv2.waitKey(0)

        if k == 1048676:
            print 'Press d'
            isPress = True
        elif k == 1048673:
            print 'Press a'
            isPress = True
            list_result.append(result_lines[index])
        elif k == 1048690:
            print 'Press r'
            isPress = True
            if len(list_result) > 0:
                list_result.pop()
            index -= 2
        elif k == 1048687:
            print 'Press o'
            isPress = True
            isSave = True
            print 'Last position: ' + str(index)
            print 'Start saving result to files'
            for result in list_result:
                result_refine.write(result)
        else:
            print k
    print "List Result:"
    print len(list_result)
    if isSave:
        break
    index += 1