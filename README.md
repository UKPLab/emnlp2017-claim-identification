#  What is the Essence of a Claim? Cross-Domain Claim Identification

Source code repository for our EMNLP paper on cross-domain claim identification

Please use the following citation:

```
@InProceedings{Daxenberger.et.al.2017.EMNLP,
  title     = {What is the Essence of a Claim? Cross-Domain Claim Identification},
  author    = {Daxenberger, Johannes and Eger, Steffen and Habernal, Ivan and
               Stab, Christian and Gurevych, Iryna},
  booktitle = {Proceedings of the 2017 Conference on Empirical Methods
               in Natural Language Processing (EMNLP)},
  pages     = {to appear},
  month     = sep,
  year      = {2017},
  address   = {Copenhagen, Denmark},
  publisher = {Association for Computational Linguistics},
  url       = {https://arxiv.org/abs/1704.07203}
}
```

> **Abstract:** Argument mining has become a popular research area in NLP. It typically includes the identification of argumentative components, e.g. claims, as the central component of an argument. We perform a qualitative analysis across six different datasets and show that these appear to conceptualize claims quite differently. To learn about the consequences of such different conceptualizations of claim for practical applications, we carried out extensive experiments using state-of-the-art feature-rich and deep learning systems, to identify claims in a cross-domain fashion. While the divergent perception of claims in different datasets is indeed harmful to cross-domain classification, we show that there are shared properties on the lexical level as well as system configurations that can help to overcome these gaps.

Contact person: Johannes Daxenberger, daxenberger@ukp.informatik.tu-darmstadt.de

https://www.ukp.tu-darmstadt.de/

https://www.tu-darmstadt.de/


Don't hesitate to send us an e-mail or report an issue, if something is broken (and it shouldn't be) or if you have further questions.

> This repository contains experimental software and is published for the sole purpose of giving additional background details on the respective publication.

## How to Run

The experimental code can be found in `src/main/java` and `src/main/python`. Please refer to the respective README files for individual folders.

## Data

All data used for the experiments can be found in a simple sentence-based format in `src/main/python`. If you need the source data for the experiments in `src/main/java`, please contact the authors.

## Requirements

* Java 1.7 and higher, Maven (for Java-based experiments)
* Python 2.7 and virtualenv (for Python-based experiments)
* tested on 64-bit Linux versions and Mac OS
* 16 GB RAM
* Python 2.7
* High-performance Compute Cluster or any large-scale machine for running all experiments in parallel
