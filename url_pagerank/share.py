import re

root_dir = '/Users/alexzhangch/Documents/git_project/NewsFile/mirror/news.tsinghua.edu.cn'
root_url = 'http://news.tsinghua.edu.cn'

log_file = 'error.log'
graph_file = 'tsinghua.graph'
anchor_file = "anchor.log"
title_file = 'title.log'
url_file = 'url.log'
pagerank_log = 'pagerank.log'
MAX_COUNT = 5000000


def remove_js_css(content):
    """ remove the the javascript and the stylesheet and the comment content
     (<script>....</script> and <style>....</style> <!-- xxx -->) """
    r = re.compile(r'''<script.*?</script>''', re.I | re.M | re.S)
    s = r.sub('', content)
    r = re.compile(r'''<style.*?</style>''', re.I | re.M | re.S)
    s = r.sub('', s)
    r = re.compile(r'''<!--.*?-->''', re.I | re.M | re.S)
    s = r.sub('', s)
    r = re.compile(r'''<meta.*?>''', re.I | re.M | re.S)
    s = r.sub('', s)
    r = re.compile(r'''<ins.*?</ins>''', re.I | re.M | re.S)
    s = r.sub('', s)
    r = re.compile(r'''<embed.*?>''', re.I | re.M | re.S)
    s = r.sub('', s)

    r = re.compile(r'''<img.*?>''', re.I | re.M | re.S)
    s = r.sub('', s)


    return s
