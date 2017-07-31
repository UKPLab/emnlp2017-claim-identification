# How to run the programs

## Kim's CNN

This directory contains an adaptation of the code in https://github.com/yoonkim/CNN_sentence.
The adaptation allows to specify train, dev, and test sets yourself.

You basically have to run

``python2 process_data_se_WithDevel.py <train> <test> <dev> > <temporaryFile>``

Then you read out the max length from `<temporaryFile>` and run

``python conv_net_sentence_withDevelSet_fixed.py -nonstatic -rand <maxLength> <storeFile.pickle>``

The model's predictions on the test set will be output on the command line. 

See also `runID_w2vec_maj20_single_gpu.py`

## BiLSTM, LSTM

We took an implementation of an LSTM and BiLSTM directly from here: https://github.com/fchollet/keras/blob/master/examples/imdb_lstm.py
Our implementations (found in `imdb_bidirectional_lstm.py` and `imdb.py`) are straightforward adaptations of this code.

# Data format

The data format is as simple as it can get: tokenized sentence, tab, label (binary currently). An example is

``First of all , as I mentioned above students should have a right to choose what they want to study .    1``
``Discuss both harms and good of advertising and give your opinion        0``

As you can see, one example (sentence+label) per line.
 
