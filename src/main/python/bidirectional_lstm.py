'''Train a Bidirectional LSTM for sentence classification.
Taken from here: https://github.com/fchollet/keras/blob/master/examples/ 
'''

from __future__ import print_function
import numpy as np,sys
np.random.seed(1337)  # for reproducibility

from keras.preprocessing import sequence
from keras.models import Model
from keras.layers import Dense, Dropout, Embedding, LSTM, Input, merge
from keras.callbacks import ModelCheckpoint
import readData as rd

def threshold(x):
  if x>0.5: return 1
  else: return 0

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


# this is the placeholder tensor for the input sequences
sequence = Input(shape=(maxlen,), dtype='int32')
# this embedding layer will transform the sequences of integers
# into vectors of size 128
embedded = Embedding(max_features, 128, input_length=maxlen)(sequence)

# apply forwards LSTM
forwards = LSTM(256)(embedded)
# apply backwards LSTM
backwards = LSTM(256, go_backwards=True)(embedded)

# concatenate the outputs of the 2 LSTMs
merged = merge([forwards, backwards], mode='concat', concat_axis=-1)
after_dp = Dropout(0.5)(merged)
if doReShape:
  ounits = 2
  activation = "softmax"
else:
  ounits = 1
  activation = "sigmoid"
output = Dense(ounits, activation=activation)(after_dp)

model = Model(input=sequence, output=output)

# try using different optimizers and different optimizer configs
model.compile('adam', 'binary_crossentropy', metrics=['accuracy'])

print('Train...X')
weightsPath = "/tmp/weights2.hdf5"
checkpointer = ModelCheckpoint(filepath=weightsPath, verbose=1, save_best_only=True)
model.fit(X_train, y_train, batch_size=batch_size, nb_epoch=5,
          validation_data=(X_dev, y_dev),callbacks=[checkpointer])
# need to load the best weights
model.load_weights(weightsPath)
scoreBest, accBest = model.evaluate(X_test, y_test,
                            batch_size=batch_size)

print('Test score:', scoreBest)
print('Test accuracy:', accBest)
pTest = model.predict_on_batch(X_test)
pDev = model.predict_on_batch(X_dev)
predsTest = []
predsDev = []
for i in xrange(len(pTest)):
  if doReShape: pr = str(np.argmax(pTest[i]))
  else: pr = str(threshold(pTest[i][0]))
  predsTest.append( pr )
for i in xrange(len(pDev)):
  if doReShape: pr = str(np.argmax(pDev[i]))
  else: pr = str(threshold(pDev[i][0]))
  predsDev.append( pr )
print("TEST:"+" ".join(predsTest))
print("DEV:"+" ".join(predsDev))

