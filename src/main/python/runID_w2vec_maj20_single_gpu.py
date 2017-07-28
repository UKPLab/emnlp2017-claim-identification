#! /usr/bin/python

import os
import sys

path=sys.argv[1]
#filename = filter(lambda x: x!="",path.split("/"))[-2]
dataSets = sorted(list(set([x.split(".")[0] for x in os.listdir(path)])))

#print dataSets; sys.exit(1)

source=sys.argv[2]
fold=sys.argv[3]
index=int(sys.argv[4])

mode="-rand"
#mode="-word2vec"

ofile="/PATH/outputs_INDOMAIN_rand/final_%s_CrossDomain_OUTPUTS_novel_majority20_%s_%s_%d.dat"%(mode,source,fold,index+1)

def readMaxLength(fn):
    for line in open(fn):
        line = line.strip()
        if line.startswith("max sentence length:"):
            return line.split()[-1]

tmpFile="/PATH/tmp/id_status%s-%s-%d.dat"%(source,fold,index+1)
embeddingFile="wordvecs/GoogleNews-vectors-negative300.bin"
embeddingFile="/PATH/GoogleNews-vectors-negative300.bin"

sys.stderr.write("Processing %s - %s - %d\n"%(source,fold,index+1))
sys.stderr.flush()
saveFile = "/PATH/models_id/"+source+"_"+fold+"_"+"%d"%(index+1)+".p"
  #if not os.path.isfile(saveFile):
cmd = "python /PATH/kim/process_data_se_WithDevel.py %s %s/%s/%s_%d.train %s/%s/%s.test %s/%s/%s.dev %s > %s"%(embeddingFile,path,source,fold,index+1,path,source,fold,path,source,fold,saveFile,tmpFile)
os.system(cmd)
maxL = readMaxLength("%s"%tmpFile)
sys.stderr.write("\tLearning\n")
sys.stderr.flush()
os.system('echo "###%s-%s-%d###" > %s'%(source,fold,index+1,ofile))
gpusupport="""cudaDir="/PATH/cudnn/cuda"
export LD_LIBRARY_PATH=${cudaDir}/lib64:${LD_LIBRARY_PATH}
export CPATH=${cudaDir}/include:${CPATH}
export LIBRARY_PATH=${cudaDir}/lib64:${LD_LIBRARY_PATH}
  """
os.system(gpusupport)
cmd2 = "THEANO_FLAGS=mode=FAST_RUN,device=gpu,floatX=float32,optimizer_including=cudnn,base_compiledir=/PATH/r_temp/id_randomtemp%s%s%d python /home/se55gyhe/kim/conv_net_sentence_withDevelSet_fixed.py -nonstatic %s %s %s >> %s"%(source,fold,index+1,mode,str(maxL),saveFile,ofile)
os.system("echo \"%s\" >> /PATH/id_foo.bar"%(cmd2))
os.system(cmd2)
    
