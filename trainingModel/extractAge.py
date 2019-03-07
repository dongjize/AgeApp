import os
import re

"""
Extract age information from jpg files and generate label files.
"""



PROJECT_PATH = os.path.dirname(os.path.dirname(__file__))

path = PROJECT_PATH + "/trainingModel/utkface-new/test/"


img = []
label = []

for file in os.listdir(path):
    print(file)
    file_path = os.path.join(path, file)
    print(file_path)
    for imgs in os.listdir(file_path):
        print(imgs)



    # if os.path.isdir(file_img):
    # img = img + os.listdir(file_img)
num = len(img)
for i in range(num):
    imgs = img[i]
    age = imgs.split("_")[0]
    label = label + [age]
assert len(img) == len(label)

print("trained img:", len(img), "......trained label", len(label))

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


# generate label
def generate_label(path):
    img = []
    label = []
    for file in os.listdir(path):
        file_img = os.path.join(path, file)
        if os.path.isdir(file_img):
            img = img + os.listdir(file_img)
    num = len(img)
    for i in range(num):
        imgs = img[i]
        name1 = imgs.split(".")[-2]  # get elements before .jpg
        name2 = name1.split('%')[-1]  # get label
        label = label + [int(name2)]
    assert len(img) == len(label)

    print("train img:", len(img), "......train label", len(label))






# if __name__ == '__main__':
#
#     # generate_label(path)
#
#     rename_file(path)
