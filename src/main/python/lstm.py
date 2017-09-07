'''Trains a LSTM for sentence classification. 
Taken from here: https://github.com/fchollet/keras/blob/master/examples/
'''
from __future__ import print_function
import numpy as np,sys,time
np.random.seed(1337)  # for reproducibility

from keras.preprocessing import sequence
from keras.utils import np_utils
from keras.models import Sequential
from keras.layers import Dense, Dropout, Activation, Embedding
from keras.layers import LSTM, SimpleRNN, GRU
from keras.callbacks import ModelCheckpoint

def reshape(x):
    m = len(x)
    x_mod = np.zeros((m,2))
    for i in xrange(m):
        if x[i] == 0:
            x_mod[i,:] = np.array([1,0])
        else:
            x_mod[i,:] = np.array([0,1])
    return x_mod

max_features = 20000
maxlen = 80  # cut texts after this number of words (among top max_features most common words)
batch_size = 32

print('Loading data...')

newData=True
import readData as rd
if newData:
  word2index = {}
  X_train,y_train,word2index = rd.read(sys.argv[1],word2index=word2index,startIndex=1)
  X_dev,y_dev,_ = rd.read(sys.argv[2],word2index,None)
  X_test,y_test,_ = rd.read(sys.argv[3],word2index,None)
  doReShape=False
  if doReShape:
	y_train = reshape(y_train)
	y_test = reshape(y_test)
	y_dev = reshape(y_dev)

print('Pad sequences (samples x time)')
X_train = sequence.pad_sequences(X_train, maxlen=maxlen)
X_test = sequence.pad_sequences(X_test, maxlen=maxlen)
X_dev = sequence.pad_sequences(X_dev, maxlen=maxlen)
print('X_train shape:', X_train.shape)
print('X_test shape:', X_test.shape)
print(X_train[0,:])

print('Build model...')
model = Sequential()
model.add(Embedding(max_features, 128, input_length=maxlen, dropout=0.2))
dropoutR = 0.2
dropoutR = 0.0
model.add(LSTM(256, dropout_W=dropoutR, dropout_U=dropoutR))  # try using a GRU instead, for fun
if doReShape:
  ounits = 2
  activation = "softmax"
else:
  ounits = 1
  activation = "sigmoid"
model.add(Dense(ounits))
model.add(Activation(activation))

# try using different optimizers and different optimizer configs
model.compile(loss='binary_crossentropy',
              optimizer='adam',
              metrics=['accuracy'])

print('Train...')
# save the best weights. could also do early stopping
rtime = int(round(time.time()))
weightsPath = "/tmp/weights%d.hdf5"%rtime
checkpointer = ModelCheckpoint(filepath=weightsPath, verbose=1, save_best_only=True)
model.fit(X_train, y_train, batch_size=batch_size, nb_epoch=5,
          validation_data=(X_dev, y_dev),callbacks=[checkpointer])
score, acc = model.evaluate(X_test, y_test,
                            batch_size=batch_size)
# need to load the best weights
model.load_weights(weightsPath)
scoreBest, accBest = model.evaluate(X_test, y_test,
                            batch_size=batch_size)

print('Test score:', scoreBest)
print('Test accuracy:', accBest)
predsTest = map(str,model.predict_classes(X_test,verbose=0))
predsVal = map(str,model.predict_classes(X_dev,verbose=0))
print("TEST:"+" ".join(predsTest))
print("DEV:"+" ".join(predsVal))

