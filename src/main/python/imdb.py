'''Trains a LSTM on the IMDB sentiment classification task.
The dataset is actually too small for LSTM to be of any advantage
compared to simpler, much faster methods such as TF-IDF+LogReg.
Notes:
- RNNs are tricky. Choice of batch size is important,
choice of loss and optimizer is critical, etc.
Some configurations won't converge.
- LSTM loss decrease patterns during training can be quite different
from what you see with CNNs/MLPs/etc.
'''
from __future__ import print_function
import numpy as np,sys,time
np.random.seed(1337)  # for reproducibility

from keras.preprocessing import sequence
from keras.utils import np_utils
from keras.models import Sequential
from keras.layers import Dense, Dropout, Activation, Embedding
from keras.layers import LSTM, SimpleRNN, GRU
from keras.datasets import imdb
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
(X_train, y_train), (X_test, y_test) = imdb.load_data(nb_words=max_features,test_split=0.2)
X_dev = X_test
y_dev = y_test

#print(y_train.shape)
#print(y_train[0:10]); 
#y_train = reshape(y_train)
#y_test = reshape(y_test)
#print(y_train.shape)
#print(y_train[0:10,:]);
#print(X_train.shape)
#print(X_train[0])
#print(type(X_train[0]))
#print(len(X_train), 'train sequences')
#print(len(X_test), 'test sequences')

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
#np.set_printoptions(threshold=np.inf)
#print(np.array_str(X_train))
#print(np.array_str(y_train))
#print(np.array_str(X_dev))
#print(np.array_str(y_dev))
#print(np.array_str(X_test))
#print(np.array_str(y_test))
#sys.exit(1)

print('Pad sequences (samples x time)')
X_train = sequence.pad_sequences(X_train, maxlen=maxlen)
X_test = sequence.pad_sequences(X_test, maxlen=maxlen)
X_dev = sequence.pad_sequences(X_dev, maxlen=maxlen)
print('X_train shape:', X_train.shape)
print('X_test shape:', X_test.shape)
print(X_train[0,:])
#sys.exit(1)

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
#print('Test score:', score)
#print('Test accuracy:', acc)
print('Test score:', scoreBest)
print('Test accuracy:', accBest)
predsTest = map(str,model.predict_classes(X_test,verbose=0))
predsVal = map(str,model.predict_classes(X_dev,verbose=0))
print("TEST:"+" ".join(predsTest))
print("DEV:"+" ".join(predsVal))

