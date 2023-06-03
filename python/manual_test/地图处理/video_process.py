import cv2
import os
import logging

log = logging.getLogger()  # 创建Logger实例
log.setLevel(logging.DEBUG)
console = logging.StreamHandler()
formatter = logging.Formatter('%(asctime)s - %(filename)s - %(levelname)s - %(message)s')
console.setFormatter(formatter)
log.addHandler(console)

image_base_path = "/Users/yuwanglin/Desktop/"
image_output_path = "/Users/yuwanglin/PycharmProjects/pythonProject/地图处理/image/"


def get_images(file_name):
    if not os.path.exists(image_output_path):
        os.makedirs(image_output_path)
    cap = cv2.VideoCapture(file_name)
    # 帧率(frames per second)
    fps = cap.get(cv2.CAP_PROP_FPS)
    # 总帧数(frames)
    frames = cap.get(cv2.CAP_PROP_FRAME_COUNT)
    time_len = frames / fps
    log.info("帧数：" + str(fps))
    log.info("总帧数：" + str(frames))
    log.info("视屏总时长：" + "{0:.2f}".format(time_len) + "秒")
    step = int(frames / time_len)
    total_output_num = 0
    frame_times = -step
    while cap.isOpened():
        frame_times = frame_times + step *  5
        if frame_times >= frames:
            log.info("视频读取完毕,一共输出:%d", total_output_num)
            break
        cap.set(cv2.CAP_PROP_POS_FRAMES, frame_times)
        success, frame = cap.read()
        if not success:
            log.info("读取失败:%d", frame_times)
            break
        log.info("输出当前帧:" + str(frame_times))
        cv2.imencode('.jpg', frame)[1].tofile(image_output_path + str(frame_times) + ".jpg")
        total_output_num += 1


get_images(image_base_path + '末日1.mp4')
