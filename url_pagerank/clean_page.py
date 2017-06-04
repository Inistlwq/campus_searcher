# coding:utf8
import json
crawl_log = "crawl.log"
clean_log = 'url.log'

# 页面的map数据
map_dict = {}
# 页面的出链文件
graph_dict = {}
page_dict = {}


def get_url_parent():
    with open(crawl_log) as log_in:
        lines = log_in.readlines()

    count = 0
    with open(clean_log, 'w') as out:
        # 清洗网页
        for i in xrange(0, len(lines)):
            line = ' '.join(filter(lambda x: x, lines[i].split(' ')))
            cols = line.split(" ")
            http_code = cols[1]
            http1 = cols[3]
            http2 = cols[5]
            if http_code != '200':
                continue
            if cols[2] == '0' or cols[2] == '-':
                # pass
                continue

            if http1.endswith(('.html', '.pdf', '.doc', '.docx', '.html')):
                page_dict[http1] = 1
                # page_dict[http2] = 1
                graph_dict.setdefault(http2, [])
                graph_dict[http2].append(http1)
                count += 1
                out.writelines("%s\n" % (http1))

            # print cols
    print "parse finished", count, 'pages'


def save_graph():
    with open('news.graph', 'w') as out:
        for key in graph_dict.keys():
            out.write(key + ' ')
            for i in graph_dict[key]:
                out.write(i + ',')
            out.write('\n')
    print len(graph_dict)


if __name__ == '__main__':
    get_url_parent()
    # save_graph()

    print len(page_dict)
    """
    jsObj = json.dumps(page_dict)
    with open('dict.log', 'w') as out:
        out.write(jsObj)
    """
