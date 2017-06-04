# coding=utf-8
# 网页数据的根目录
from bs4 import BeautifulSoup
import share
from share import *
import datetime
import sys
reload(sys)
import re
# 避免编码出错
sys.setdefaultencoding("utf-8")


# href_patten = re.compile(r'href=[\"\']([^\"\']*?)[\"\']', re.S)
href_pattern = re.compile(r'<a href=[\"\']([^\"\']*?\.(html|pdf|doc|docx))[\"\']', re.S)
href_pattern = re.compile(r'<a href=[\"\']([^\"\']*?\.(html|pdf|doc|docx))[\"\'].+?>(.+?)</a>', re.S)
html_pattern = re.compile(r'<a href=[\"\']([^\"\']*?\.html)[\"\']', re.S)
title_pattern = re.compile(r'<title>(.+?)</title>', re.I | re.M | re.S)


# 标题字典
url_title_dict = {}
url_anchor_dict = {}
# 链接关系字典
graph_dict = {}
url_num_dict = {}
log_list = []
with open(url_file) as url_in:
    url_list = url_in.readlines()

dir_list = []

count = 0
for url in url_list:
    count += 1
    if count > MAX_COUNT:
        break
    url = url.replace('http://news.tsinghua.edu.cn', '')
    url = url.replace('\n', '')
    dir_list.append(url)

print len(dir_list)

for index, diri in enumerate(dir_list):
    url_title_dict[index] = 'null'
    url_anchor_dict[index] = 'null'
    graph_dict.setdefault(index, [])
    url_num_dict[diri] = index

print max(graph_dict.keys())
print len(url_title_dict), len(url_num_dict)

start = datetime.datetime.now()

# for i in xrange(0, len(dir_list)):
for i in xrange(0, len(dir_list)):
    if i % 500 == 0:
        print i, datetime.datetime.now() - start
    diri = dir_list[i]
    # print root_url + dir_list[i]

    if not diri.endswith(('.html', 'htm')):
        # url_title_dict[diri] = 'null'
        # pdf,word,等文档
        continue

    try:
        # 打开网页文件
        with open(root_dir + diri) as page_input:
            html = page_input.read()
            html = share.remove_js_css(html)
            # soup = BeautifulSoup(html, 'html.parser')
    except Exception, e:
        print 'open failed', e
        continue
    ans_list = re.findall(href_pattern, html)
    for ans in ans_list:
        href = ans[0]
        href = href.replace('http://news.tsinghua.edu.cn', '')
        if href.find('http:') == 0:
            continue
        try:
            index = url_num_dict[href]
        except Exception, e:
            log_list.append('index error')
            log_list.append(e)
        try:
            anchor = ans[2]
            r = re.compile(r'''<.*?>''', re.I | re.M | re.S)
            anchor = r.sub('', anchor)
            r = re.compile(r'''\s+''', re.I | re.M | re.S)
            anchor = r.sub(' ', anchor)
            anchor = anchor.replace('\r', '')
            anchor = anchor.replace('\n', '')
            anchor = anchor.replace('\t', '')
            if anchor == '' or anchor == ' ':
                anchor = 'null'
            url_anchor_dict[index] = anchor
            if url_title_dict[index] == 'null':
                url_title_dict[index] = anchor
            graph_dict[i].append(index)
        except Exception, e:
            log_list.append('ignore')
            log_list.append(e)

    title_list = re.findall(title_pattern, html)
    if title_list:
        title = title_list[0]
        title = title.replace('\n', '')
        title = title.replace('\t', '')
        url_title_dict[i] = title

    """
    try:
        # 获取页面标题
        title = soup.find('title').get_text()
        # print title
        url_title_dict[i] = title
    except Exception, e:
        print 'title', e, diri

    try:
        # 获取页面超链接
        a_list = soup.find_all('a')
        for a in a_list:
            href = a.get('href')
            if href:
                href = href.replace('http://news.tsinghua.edu.cn', '')
                # print href
                if href in dir_list:
                    anchor = a.get_text()
                    anchor = anchor.replace('\r', '')
                    anchor = anchor.replace('\n', '')
                    if anchor == '' or anchor == ' ':
                        anchor = 'NULL'
                    index = url_num_dict[href]
                    url_anchor_dict[index] = anchor
                    url_title_dict[index] = anchor
                    graph_dict[i].append(index)
                    # print anchor, href
    except Exception, e:
        print 'link', e, diri
    """


print datetime.datetime.now() - start


try:
    with open(graph_file, 'w') as out:
        for key in graph_dict.keys():
            out.write(str(key) + ':')
            for i in graph_dict[key]:
                out.write(str(i) + ',')
            out.write('\n')
except Exception, e:
    print 'save graph failed', e

print datetime.datetime.now() - start

try:
    with open(title_file, 'w') as title_out:
        with open(anchor_file, 'w') as anchor_out:
            for i in xrange(0, len(url_title_dict)):
                # print key, url_title_dict[key], url_anchor_dict[key]
                # title_out.writelines("%d\t%s\t%s\n" % (i, dir_list[i], url_title_dict[i]))
                # anchor_out.writelines("%d\t%s\t%s\n" % (i, dir_list[i], url_anchor_dict[i]))
                title_out.writelines("%s\n" % (url_title_dict[i]))
                anchor_out.writelines("%s\n" % (url_anchor_dict[i]))
except Exception, e:
    print 'save title、anchor failed', e

print len(graph_dict)

print datetime.datetime.now() - start


with open(log_file, 'w') as log:
    for error in log_list:
        log.writelines(error)
        log.writelines('\n')
