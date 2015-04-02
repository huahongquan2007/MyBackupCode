import cv2
import random

# img = LoadImage("/home/robotbase/DataDrive/Dataset/motorway/tmeMotorwayDataset_daylight/tme08/Right/010730-R.png")
# NamedWindow("opencv")
# ShowImage("opencv",img)
# WaitKey(0)


lines = [line.strip() for line in open('annotations.txt')]

#random.shuffle(lines)

for index, line in enumerate(lines):
    line_split = line.split(' ')

    img = cv2.imread(line_split[0])
    for i in range(0, int(line_split[1]) ):
        [x, y, w, h] = line_split[2 + 4 * i: 2 + 4 * i + 4]
        [x, y, w, h] = [ int(x), int(y), int(w), int(h)]
        # cv2.rectangle(img, (x, y), (x + w, y + h), (255, 0, 0))
    cv2.imshow("opencv", img)
    cv2.waitKey(80)