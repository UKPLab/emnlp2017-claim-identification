import cPickle
import os
from numpy import average

from confusion_matrix import ConfusionMatrix


# deprecated, use Java implementation instead:
# de.tudarmstadt.ukp.dkpro.argumentation.crossdomainclaims.experiments.results.TableReportCreator

def confusion_matrices_from_report(path):
    f = file(path, 'rb')
    report = cPickle.load(f)
    f.close()
    matrices = []
    for e in report:
        matrices.append(e['confusion_matrix'])
    return matrices


def confusion_matrices_from_directory(path):
    matrices = []
    for i in os.listdir(path):
        if i.endswith(".csv"):
            matrices.append(ConfusionMatrix(file=path + '/' + i))
    return matrices


# Determine scores from python experiment
# report_path = '/Users/zemes/DEVELOPMENT/Experiments/Sufficiency/Results-CNN-test/report_CNN-test1.bin'
# conf_matrices = confusion_matrices_from_report(report_path)
# labels = ['noFlaw', 'sufficiency']

# Determine scores from java experiment
# conf_path = '/PATH-TO-DATA//results/claims-indomain-results-matrices/Stab201X_structure'

# list all directories
input_dir = "/tmp/outcd"

# if False, the macro F1 score is reported
# report_f1_claim = True

feature_map = {'embeddings-sentiment-lexical-dictionary-syntax-structure-discourse': 'All features',
               'embeddings': 'Embedd',
               # 'dictionary': 'Dictionary',
               # 'sentiment': 'Sentiment',
               'lexical': 'Lexical',
               'syntax': 'Syntax',
               'discourse': 'Discourse',
               'structure': 'Structure',
               # 'embeddings-syntax-sentiment-lexical-structure-discourse': 'All--dict',
               'embeddings-syntax-sentiment-lexical-dictionary-discourse': 'All - struct',
               # 'embeddings-syntax-lexical-dictionary-structure-discourse': 'All--senti',
               'embeddings-syntax-sentiment-lexical-dictionary-structure': 'All - disc',
               'embeddings-syntax-sentiment-dictionary-structure-discourse': 'All - lex',
               'syntax-sentiment-lexical-dictionary-structure-discourse': 'All - embedd',
               'embeddings-sentiment-lexical-dictionary-structure-discourse': 'All - syntax',
               # 'CNN1': 'CNN1'
               'allNone': 'Majority bsl',
               'CNN-BIDIR': 'Bi-LSTM',
               'CNN-LSTM': 'LSTM',
               'CNN-rand': 'CNN-R',
               'CNN1': 'CNN'
               }

data_map = {'Peldszus2015en': 'MT',
            'Reed2008': 'VG',
            'Stab201X': 'PE',
            'OrBiran2011-wd': 'WTP',
            'OrBiran2011-lj': 'OC',
            'Habernal2015': 'WD'
            }


def in_domain():
    # for storing f1 +- stddev
    collected_metrics = dict()
    collected_metrics_claim = dict()
    # only for raw f1 scores
    collected_metrics_f1_raw = dict()
    collected_metrics_f1_claim_raw = dict()

    # out_format = "$%.1f \pm %.1f$"
    out_format = "$%.1f$"

    for experiment_dir_name in os.listdir(input_dir):
        experiment_dir = input_dir + "/" + experiment_dir_name
        print(experiment_dir)

        if str(experiment_dir_name).count('_') == 2:
            print("Cross-domain")
            continue

        if str(experiment_dir_name).count('_') == 1:
            print("In domain")

        # extract data name and feature set
        split1 = experiment_dir_name.split("_")
        data_name_training = data_map.get(split1[0])
        features = feature_map.get(split1[1])

        print("Data name:", data_name_training, "Features", features)

        conf_matrices = confusion_matrices_from_directory(experiment_dir)
        labels = ['Claim', 'None']

        accuracy = []
        macro_f1 = []
        precision = []
        recall = []
        f1_claim = []
        f1_noclaim = []

        for cm in conf_matrices:
            # print(cm)
            accuracy += [cm.a() * 100]
            macro_f1 += [cm.macro_f_impl1() * 100]
            precision += [cm.p() * 100]
            recall += [cm.r() * 100]
            f1_claim += [cm.f(labels[0]) * 100]
            f1_noclaim += [cm.f(labels[1]) * 100]

        # avg_acc = out_format % (average(accuracy), std(accuracy))
        # avg_f1 = out_format % (average(macro_f1), std(macro_f1))
        avg_f1 = out_format % (average(macro_f1))
        # avg_p = out_format % (average(precision), std(precision))
        # avg_r = out_format % (average(recall), std(recall))
        avg_f1_c = out_format % (average(f1_claim))
        # avg_f1_nc = out_format % (average(f1_noclaim), std(f1_noclaim))

        if features not in collected_metrics.keys():
            collected_metrics[features] = dict()
            collected_metrics_claim[features] = dict()
        if data_name_training not in collected_metrics[features]:
            collected_metrics[features][data_name_training] = dict()
            collected_metrics_claim[features][data_name_training] = dict()

        collected_metrics_claim[features][data_name_training] = avg_f1_c
        collected_metrics[features][data_name_training] = avg_f1

        # create an empty vector for storing f1 scores for this method
        if features not in collected_metrics_f1_raw:
            collected_metrics_f1_raw[features] = []
            collected_metrics_f1_claim_raw[features] = []

        collected_metrics_f1_claim_raw[features].append(average(f1_claim))
        collected_metrics_f1_raw[features].append(average(macro_f1))

    print(collected_metrics)
    print(collected_metrics_claim)
    print(collected_metrics_f1_raw)
    print(collected_metrics_f1_claim_raw)

    # not print to LaTeX table

    sorted_rows = sorted(feature_map.values())
    sorted_cols = sorted(data_map.values())

    print ('''\\begin{table*}[ht]
    \\begin{small}
    \\begin{tabular}{p{1.6cm}|rr|rr|rr|rr|rr|rr|rr} \\toprule
    feature set & ''' + " & & ".join(sorted_cols) + " & & Avg. & " + '''\\\\ \midrule''')
    for row in sorted_rows:
        print(row + " & " + " & ".join(
            [collected_metrics[row].get(col, '') + " & " +  # macroF1 score
             collected_metrics_claim[row].get(col, '')  # claim F1
             for col in sorted_cols]) +
              " & " +
              # "$%.1f \pm %.1f$" % (average(collected_metrics_f1_raw.get(row)), std(
              #     collected_metrics_f1_raw.get(row))) +
              # "$%.1f \pm %.1f$" % (average(collected_metrics_f1_claim_raw.get(row)), std(
              #     collected_metrics_f1_claim_raw.get(row))) +
              out_format % (average(collected_metrics_f1_raw.get(row))) + " & " +
              out_format % (average(collected_metrics_f1_claim_raw.get(row))) +
              " \\\\ ")

    print ('''\end{tabular}
    \end{small}
    \caption{In-domain experiments, 10-fold cross validation. For each dataset (column head) we show
    two scores: \emph{Macro $F_1$} score (left-hand column) and $F_1$ score for claims (right-hand
    column).}
    \label{tab:results-in-domain-CV}
    \end{table*}''')


def cross_domain():
    # map: {feature_name_str: {train_str: {test_str, f1_float}}}
    feature_train_test_f1_map = dict()
    # initialize (all feature_name_str, all train_str)
    for key in feature_map.values():
        feature_train_test_f1_map[key] = dict()
        for data in data_map.values():
            feature_train_test_f1_map.get(key)[data] = dict()
    feature_train_test_f1_map.pop('Majority bsl')

    for experiment_dir_name in os.listdir(input_dir):
        experiment_dir = input_dir + "/" + experiment_dir_name
        print(experiment_dir)

        if str(experiment_dir_name).count('_') == 1:
            continue

        if str(experiment_dir_name).count('_') == 2:
            print("Cross-domain")

        # extract data name and feature set
        split1 = experiment_dir_name.split("_")
        data_name_training = data_map.get(split1[0])
        data_name_test = data_map.get(split1[1])
        features = feature_map.get(split1[2])

        if features:
            # print(
            #     "Data name training:", data_name_training, "Data name test:", data_name_test,
            #     "Features",
            #     features)

            conf_matrices = confusion_matrices_from_directory(experiment_dir)
            if len(conf_matrices) != 1:
                raise Exception('Only one file with confusion matrix expected')

            labels = ['Claim', 'None']

            cm = conf_matrices[0]

            accuracy = cm.a() * 100
            macro_f1 = cm.macro_f_impl1() * 100
            precision = cm.p() * 100
            recall = cm.r() * 100
            f1_claim = cm.f(labels[0]) * 100
            f1_noclaim = cm.f(labels[1]) * 100

            feature_train_test_f1_map[features][data_name_training][data_name_test] = (
            macro_f1, f1_claim)

    sorted_test_set_names = sorted(feature_train_test_f1_map.values()[0].keys())

    # print table title
    print('\\begin{table*}[ht]')
    print('\\begin{small}')
    print('\\begin{center}')
    print('\\begin{tabular}{p{2cm}|rr|rr|rr|rr|rr|rr|rr}')
    print('Train $\downarrow$ Test $\\rightarrow$ & ' +
          ' & '.join('\multicolumn{2}{c}{\\textbf{' + x + '}}' for x in sorted_test_set_names) +
          ' & \multicolumn{2}{c}{\\textbf{Avg}} \\\\ \hline')

    for feature, results in sorted(feature_train_test_f1_map.iteritems()):
        # print feature
        # print results
        # print results_claim

        # feat average
        feat_avg = []
        feat_avg_claim = []
        for train, test in sorted(results.iteritems()):
            for macro, claim in test.values():
                # print(feature, train, macro, claim)
                feat_avg.append(macro)
                feat_avg_claim.append(claim)

        # print(feat_avg)

        print('\multicolumn{13}{c|}{\centering{\emph{' + feature +
              "}}} & %.1f & %.1f \\\\" % (average(feat_avg), average(feat_avg_claim)))

        for train, test in sorted(results.iteritems()):
            # print train
            # print test
            # print test.values()

            row_cells = [train]

            row_macro = []
            row_claim = []

            for test_key in sorted_test_set_names:
                if test_key == train:
                    row_cells.append('--')
                    row_cells.append('--')
                else:
                    macro, claim = test[test_key]
                    # print("train", train, "test_key", test_key)
                    row_cells.append("%.1f" % macro)
                    row_cells.append("%.1f" % claim)

                    row_macro.append(macro)
                    row_claim.append(claim)

            # two last columns are averages
            row_cells.append("%.1f" % average(row_macro))
            row_cells.append("%.1f" % average(row_claim))

            print(" & ".join(row_cells) + " \\\\")



            # print(train + ' & ' + ' & '.join(
            #     ["%.1f & %.1f" % (
            #     test[test_set], results_claim[train][test_set]) if test_set in test else "--" for
            #      test_set in
            #      sorted(data_map.values())]
            # ) + " & %.1f" % average(test.values()) +
            #       " & %.1f" % average(results_claim[train].values()) +
            #       ' \\\\')
        print ' \hline'

    print('\end{tabular}')
    print('\end{center}')
    print('\end{small}')
    print('\caption{Cross-domain experiments. For each test dataset (column head) we show two scores: \emph{Macro $F_1$} score (left-hand column) and $F_1$ score for claims (right-hand column).}')
    print('\label{tab:results-cross-domain}')
    print('\end{table*}')


if __name__ == '__main__':
    # in_domain()
    cross_domain()
