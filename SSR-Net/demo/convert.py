from keras.models import Model
from keras.layers import *
import os
import tensorflow as tf
from SSRNET_model import SSR_net


def keras_to_tensorflow(keras_model, output_dir, model_name, out_prefix="output_", log_tensorboard=True):
    if os.path.exists(output_dir) == False:
        os.mkdir(output_dir)
    out_nodes = []
    for i in range(len(keras_model.outputs)):
        out_nodes.append(out_prefix + str(i + 1))
        tf.identity(keras_model.output[i], out_prefix + str(i + 1))

    sess = K.get_session()

    from tensorflow.python.framework import graph_util, graph_io

    init_graph = sess.graph.as_graph_def()

    main_graph = graph_util.convert_variables_to_constants(sess, init_graph, out_nodes)

    graph_io.write_graph(main_graph, output_dir, name=model_name, as_text=False)

    if log_tensorboard:
        from tensorflow.python.tools import import_pb_to_tensorboard

        import_pb_to_tensorboard.import_to_tensorboard(os.path.join(output_dir, model_name), output_dir)


# weight_file = "../pre-trained/morph2/ssrnet_3_3_3_64_1.0_1.0/ssrnet_3_3_3_64_1.0_1.0.h5"
weight_file = "../pre-trained/wiki/ssrnet_3_3_3_64_1.0_1.0/ssrnet_3_3_3_64_1.0_1.0.h5"
# weight_file = "../pre-trained/imdb/ssrnet_3_3_3_64_1.0_1.0/ssrnet_3_3_3_64_1.0_1.0.h5"
img_size = 64
stage_num = [3, 3, 3]
lambda_local = 1
lambda_d = 1
model = SSR_net(img_size, stage_num, lambda_local, lambda_d)()
model.load_weights(weight_file)

output_dir = os.path.join(os.getcwd(), "out")
keras_to_tensorflow(model, output_dir=output_dir, model_name="ssr_model.pb")
print("MODEL SAVED")
