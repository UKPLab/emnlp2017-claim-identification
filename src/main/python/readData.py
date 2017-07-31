#! /usr/bin/python

import sys,numpy as np
np.random.seed(1337)  # for reproducibility


def read(fn,word2index={},startIndex=0):
    #word2index = {}
    x = []
    y = []
    curIndex = startIndex
    if startIndex is None:
        maxIndex = max(word2index.values())+1
    for line in open(fn):
        line = line.strip()
	#print "|%s|"%line
	try:
          sentence,label = line.split("\t")
	except ValueError:
	  label = line.split("\t")[0]
	  sentence = ""
        lx = []
        y.append(int(label))
        for word in sentence.split():
            if word not in word2index:
                if startIndex is not None:
                    word2index[word] = curIndex
                    curIndex += 1
                else: # this is for dev and test
                    word2index[word] = maxIndex
            index = word2index[word]
            lx.append(index)
        x.append(lx)
    return np.array(x),np.array(y),word2index

if __name__ == "__main__":

    word2index = {}
    train_x,train_y,word2index = read(sys.argv[1],word2index)
    dev_x,dev_y,_ = read(sys.argv[2],word2index,None)
    test_x,test_y,_ = read(sys.argv[3],word2index,None)
    print train_x.shape
    print train_x[100]
