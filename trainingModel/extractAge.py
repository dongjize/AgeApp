import os

"""
Extract age information from jpg files and generate label files.
"""



PROJECT_PATH = os.path.dirname(os.path.dirname(__file__))

img_path = PROJECT_PATH + "/trainingModel/utkface-new/test/"


def extract_age(path):
    img = []
    img_path = []
    age_label = []

    for file in os.listdir(path):
        # print(file)
        file_path = os.path.join(path, file)
        # print(file_path)
        if os.path.isfile(file_path):
            img += [file]
            img_path += [file_path]
        # print(img)

    num = len(img)
    for i in range(num):
        imgs = img[i]
        age = imgs.split("_")[0]
        age_label += [age]
    assert len(img) == len(age_label)

    print(img)
    print(age_label)
    print(img_path)
    print("Train imgs:", len(img), "......Train labels", len(age_label))

#
# # change file name
# def rename_file(path):
#     for file in os.listdir(path):
#         file_path = os.path.join(path, file)
#         if os.path.isdir(file_path):
#             for imgs in os.listdir(file_path):
#                 img = os.path.splitext(imgs)
#                 newname = img[0] + '%' + file + img[1]
#                 os.rename(file_path + "\\" + imgs, file_path + "\\" + newname)
#     print(".....done.......")


if __name__ == '__main__':

    extract_age(img_path)

    # rename_file(path)
