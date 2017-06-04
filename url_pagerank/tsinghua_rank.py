# coding: utf8
import numpy as np
import networkx as nx
from share import *
import datetime
# 词项与ID的对应
# map_filename = "node.map.utf8"
# 链接关系图

"""
PageRank算法实现
•输入： 万维网链接结构图G的链接关系记录文件D，
参数α， 迭代次数M；
"""

# 跳出参数
alpha = 0.15
# 迭代次数
TN = 30
# 临时变量,记录无出链接节点的PageRank值之和
S = 0.0
# map_dict = {}
word = []
# 出度分析
out_degree = {}
# 入度分析
in_degree = {}
# pagerank值
pr_dict = {}
i_dict = {}

begin = datetime.datetime.now()
"""
with open(map_filename) as map_in:
    lines = map_in.readlines()
for line in lines:
    line = line.replace('\r', '')
    line = line.replace('\n', '')
    words = line.split('-->')
    word.append(int(words[1]))
    map_dict[words[1]] = words[0]
    # out_degree.setdefault(words[1], 0)

print max(word), min(word), len(word), max(word) - min(word)

print "map_dict:", len(map_dict)
# print "out_degree:", len(out_degree)
# print word
"""

"""
从数据图中读取
:return:
"""


with open(graph_file) as graph_in:
    lines = graph_in.readlines()
    print len(lines)
for line in lines:
    line = line.replace('\r', '')
    line = line.replace('\n', '')
    words = line.split(':')
    # print words
    src_node = words[0]
    out_degree.setdefault(src_node, 0)
    in_degree.setdefault(src_node, 0)
    if len(words[1]) > 0:
        dst_nodes = words[1].split(',')
        # print dst_nodes
        for dst in dst_nodes:
            if dst != '':
                # 出度
                out_degree[src_node] += 1
                # 入度
                out_degree.setdefault(dst, 0)
                in_degree.setdefault(dst, 0)
                in_degree[dst] += 1


N = len(out_degree)
print N, len(in_degree)
for key in out_degree.keys():
    pr_dict[key] = float(1)/float(N)
    i_dict[key] = alpha/float(N)
    if out_degree[key] == 0:
        S += pr_dict[key]


print S

print datetime.datetime.now() - begin


sums = sum(pr_dict.values())
for k in xrange(TN):

    print k, datetime.datetime.now() - begin, sums, S
    sums = 0.0
    for line in lines:
        line = line.replace('\r', '')
        line = line.replace('\n', '')
        words = line.split(':')
        # print words
        i = words[0]
        # print src_node
        dst_nodes = words[1].split(',')
        for j in dst_nodes:
            if j == '':
                continue
            i_dict[j] += (1.0 - alpha) * pr_dict[i]/out_degree[i]

    for n in out_degree.keys():
        # print key, pr_dict[key], i_dict[key]
        pr_dict[n] = i_dict[n] + (1.0 - alpha) * S / float(N)
        i_dict[n] = alpha/float(N)
        sums += pr_dict[n]

    S = 0
    for n in out_degree.keys():
        if out_degree[n] == 0:
            S += pr_dict[n]

sorted_dict = sorted(pr_dict.items(), lambda x, y: cmp(x[1], y[1]), reverse=True)
print sorted_dict[0]

print datetime.datetime.now() - begin

with open(title_file) as title_in:
    title_lines = title_in.readlines()
with open(anchor_file) as anchor_in:
    anchor_lines = anchor_in.readlines()
with open(url_file) as url_in:
    url_lines = url_in.readlines()

print len(title_lines), len(anchor_lines), len(url_lines)


with open(pagerank_log, 'wb') as out:
    l = 0
    for i in sorted_dict:
        l += 1
        index = int(i[0])
        try:
            anchor = anchor_lines[index].replace('\n', '')
            title = title_lines[index].replace('\n', '')
            url = url_lines[index].replace('\n', '')
            dirs = url.replace('http://news.tsinghua.edu.cn', '')
            out.writelines(dirs + '\t' + str(i[1]) + '\t' + anchor + '\t' + title + '\n')
            if l < 11:
                print index, url, i[1], anchor, title
        except Exception, e:
            print index, i, e


end = datetime.datetime.now()

print end - begin



