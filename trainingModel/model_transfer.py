import tensorflow as tf
import os


def restore_from_source(sess, source_path):
    s_saver = tf.train.Saver()
    ckpt = tf.train.get_checkpoint_state(source_path)
    if ckpt and ckpt.model_checkpoint_path:
        s_saver.restore(sess, ckpt.model_checkpoint_path)
        print("restore and continue training!")
        return sess
    else:
        raise IOError("Not found source model")
