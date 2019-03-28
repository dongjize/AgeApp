import argparse
import os
import time

import tensorflow as tf


# TODO
def run_training():
    pass


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("--learning_rate", "--lr", type=float, default=1e-3, help="Init learning rate")
    parser.add_argument("--weight_decay", type=float, default=1e-5, help="Set 0 to disable weight decay")
    parser.add_argument("--model_path", type=str, default="./models", help="Path to save models")
    parser.add_argument("--log_path", type=str, default="./train_log", help="Path to save logs")
    parser.add_argument("--epoch", type=int, default=6, help="Epoch")
    parser.add_argument("--images", type=str, default="./data/train", help="Path of tfrecords")
    parser.add_argument("--batch_size", type=int, default=128, help="Batch size")
    parser.add_argument("--keep_prob", type=float, default=0.8, help="Used by dropout")
    parser.add_argument("--cuda", default=False, action="store_true",
                        help="Set this flag will use cuda when testing.")
    args = parser.parse_args()
    if not args.cuda:
        os.environ['CUDA_VISIBLE_DEVICES'] = ''
    run_training()  # TODO
