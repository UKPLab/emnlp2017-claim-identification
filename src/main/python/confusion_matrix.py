import pandas as pd
import theano
from theano import tensor as tensor


class ConfusionMatrix:

    matrix = None
    header = None
    size = None

    def __init__(self, **kwargs):
        if 'file' in kwargs:
            d = pd.read_csv(kwargs['file'], header=0, delimiter=",", quoting=3)
            header = []
            for x in d:
                if not 'ID' in x:
                    header.append(str(x).replace(" (pred.)", "").replace('"', ''))

            matrix = [[0.0 for i in range(len(header))] for j in range(len(header))]
            count_x = 0
            for x in d:
                if 'ID' not in x:
                    for i in range(len(d[x])):
                        matrix[i][count_x-1] = float(d[x][i].replace('"', ''))
                count_x += 1
            self.matrix = matrix
            self.header = header
            self.size = len(header)

        else:
            x = tensor.vector('x')
            classes = tensor.scalar('n_classes')
            one_h = tensor.eq(x.dimshuffle(0, 'x'), tensor.arange(classes).dimshuffle('x', 0))
            one_hot = theano.function([x, classes], one_h)
            y = tensor.matrix('y')
            y_predictions = tensor.matrix('y_predictions')
            conf_mat = tensor.dot(y.T, y_predictions)
            confusion_matrix = theano.function(inputs=[y, y_predictions], outputs=conf_mat)
            self.matrix = confusion_matrix(one_hot(kwargs['test'], len(kwargs['header'])), one_hot(kwargs['predictions'], len(kwargs['header'])))
            self.header = kwargs['header']
            self.size = len(kwargs['header'])

    def __str__(self):
        string = '"ID"'
        for x in self.header:
            string = string + ';"' + x + ' (pred.)"'
        string += '\n'
        for x in range(self.size):
            string += '"' + self.header[x] + ' (act.)"'
            for y in range(self.size):
                string += ';"' + str(self.matrix[x][y]) + '"'
            string += '\n'
        return string

    def index_of(self, category):
        for x in range(self.size):
            if category in self.header[x]:
                return x
        return -1

    def fp(self, category=None):
        if not category:
            fp = 0.0
            for x in self.header:
                fp += self.fp(x)
            return fp
        else:
            index = self.index_of(category)
            false_positives = 0.0
            for x in range(self.size):
                if x != index:
                    false_positives += self.matrix[x][index]
            return false_positives

    def tp(self, category=None):
        if not category:
            tp = 0.0
            for x in self.header:
                tp += self.tp(x)
            return tp
        else:
            index = self.index_of(category)
            return self.matrix[index][index]

    def fn(self, category=None):
        if not category:
            fn = 0.0
            for x in self.header:
                fn += self.fn(x)
            return fn
        else:
            index = self.index_of(category)
            false_negative = 0.0
            for x in range(self.size):
                if x != index:
                    false_negative += self.matrix[index][x]
            return false_negative

    def f(self, category):
        tp = self.tp(category)
        fp = self.fp(category)
        fn = self.fn(category)
        result = (2 * tp) / (2 * tp + fp + fn)
        # print("TP:", tp, "FP:", fp, "FN:", fn, "F1:", result)
        return result

    def p(self, category=None):
        if not category:
            p_sum = 0.0
            for h in self.header:
                p_sum += self.p(h)
            return p_sum / len(self.header)
        else:
            if (self.tp(category) + self.fp(category)) == 0.0:
                return 0.0
            return self.tp(category) / (self.tp(category) + self.fp(category))

    def r(self, category=None):
        if not category:
            r_sum = 0.0
            for h in self.header:
                r_sum += self.r(h)
            return r_sum / len(self.header)
        else:
            return self.tp(category) / (self.tp(category) + self.fn(category))

    def macro_p(self):
        sum_p = 0.0
        for x in self.header:
            sum_p += self.p(x)
        return sum_p / self.size

    def macro_r(self):
        sum_r = 0.0
        for x in self.header:
            sum_r += self.r(x)
        return sum_r / self.size

    def macro_f_impl1(self):
        macro_r = self.macro_r()
        macro_p = self.macro_p()
        return 2.0 * (macro_r * macro_p) / (macro_r + macro_p)

    def macro_f_impl2(self):
        sum_f = 0.0
        for x in self.header:
            sum_f += self.f(x)
        return sum_f / self.size

    def sum(self):
        s = 0.0
        for x in range(self.size):
            for y in range(self.size):
                s += self.matrix[x][y]
        return s

    def a(self):
        return self.tp() / self.sum()
