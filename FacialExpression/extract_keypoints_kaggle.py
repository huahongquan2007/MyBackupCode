lines = [line for line in open('/home/robotbase/DataDrive/Dataset/Kaggle/list.txt')]
result_lines = [line for line in open('./save_result_kaggle_refine.txt')]

emotion_output = open('./emotion_output.txt', 'w')
keypoint_output = open('./keypoint_output.txt', 'w')
path_output = open('./path_output.txt', 'w')
for index, result in enumerate(result_lines):

    words = result.split('_')
    print index
    print words
    index = int(words[0])
    img_path = lines[index]
    emotion = img_path.split('_')[1].split('.')[0]
    print emotion
    path_output.write(img_path.strip() + '\n')
    emotion_output.write(emotion + '\n')
    keypoint_output.write(words[2].strip() + '\n')
