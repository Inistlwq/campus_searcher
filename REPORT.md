---
title: 校园搜索引擎
date: 2017-06-03
fontsize: 12pt
author:
- 计41  张盛豪  2014011450
- 计43  李明杰  2014011351
categories:
- 搜索引擎
tags: 
- 搜索引擎
- 课程
- 实验
password: 01c6023dacda00474e342dc0ba449ac06eaf44c7
---

# 内容
<!--more-->

综合运用搜索引擎体系结构和核心算法方面的知识，基于开源资源搭建搜索引擎

# 开源搜索引擎工具资源：
- Heritrix 1.14.4
- Lucene 4.0
- 分词工具： IK Analyzer 2012
- Html解析：Jsoup 1.7.2
- PDF 解析：pdfbox 1.8.1
- Doc 解析：poi 3.16
- 前端服务：apache+tomcat (http://tomcat.apache.org/) 

# 实验要求
- 抓取清华校内绝大部分网页资源以及大部分在线万维网文本资源（含M.S.office文档、pdf文档等，约20-30万个文件）
- 实现基于概率模型的内容排序算法；
- 实现基于HTML结构的分域权重计算，并应用到搜索结果排序中；
- 实现基于PageRank的链接结构分析功能，并应用到搜索结果排序中；
- 采用便于用户信息交互的Web界面。



# 实现流程

## 爬虫爬取清华校内网页数据

### Heritrix抓取对象

[Heritrix环境搭建教程](https://www.ibm.com/developerworks/cn/opensource/os-cn-heritrix/)

-   清华新闻网网页（不包括图书馆）资源
-   种子http://news.tsinghua.edu.cn/
-   使用正则表达式对URL进行过滤
    -   过滤无关页面，过滤无关格式文件，，保留html页面和pdf,word文档，去除奇怪链接：

        ```
        .*(?i)\.(mso|tar|txt|asx|asf|bz2|mpe?g|MPE?G| tiff?|gif|GIF|png|PNG|ico|ICO|css|sit|eps|wmf|zip|pptx?|xlsx?|gz|rpm|tgz|mov|MOV|exe|jpe?g|JPE?G|bmp|BMP|rar|RAR|jar|JAR|ZIP|zip|gz|GZ|wma|WMA|rm|RM|rmvb|RMVB|avi|AVI|swf|SWF|mp3|MP3|wmv|WMV|ps|PS|d|dd|yyyy|nivo-nextNav|xiao_ming)$
        ```

    -   禁止抓取图书馆资源：  `[\S]*lib.tsinghua.edu.cn[\S]*；[\S]*166.111.120.[\S]*`
    -   只抓取清华新闻网的数据 `[\S]*news.tsinghua.edu.cn[\S]*`；
-   Module设置，参考PPT中的设置进行

### Heritrix 抓取过程中遇到的问题

#### 页面剔除问题

在一开始的抓取过程中发现了部分奇怪的页面，如图[Heritrix 异常页面]所示，发现了很多以`yyyy.MM.dd、yyyy.M.d、MM.dd.yyyy、a.nivo-nextNav` 为结尾的奇怪链接，后来经过分析清华新闻网的源码，发现这些链接都是js代码里的内容，虽然已经在配置中设定为不从css,js等域中获取超链接，但是仍然会得到这样的url, 因此后来在正则表达式中加上了`d|dd|yyyy|nivo-nextNav|xiao_ming`字段进行过滤，最终得到的页面有52769个，共2.3G

![Heritrix 异常页面](https://d2ppvlu71ri8gs.cloudfront.net/items/2z072c1W2R3H130W3E18/Image%202017-06-04%20at%208.37.02%20下午.png)

####  Heritrix 加速问题

在抓取页面过程中，第一次尝试时发现抓取速度特别慢，爬取整整一个晚上只能获取300MB的数据，通过查询相关资料尝试了很多加速方法，最终在博客[Heritrix提高抓取效率的若干尝试](http://blog.csdn.net/yangding_/article/details/41122977) {http://blog.csdn.net/yangding_/article/details/41122977} 找到Heritrix抓取速度特别慢的原因：heritrix在抓取时一般只运行了一个线程。这是因为在默认的情况下，Heritrix使用`HostnameQueueAssignmentPolicy`来产生key值，而这个策略是用hostname作为key值的，因此一个域名下的所有链接都会被放到同一个线程中去。如果对Heritrix分配URI时的策略进行改进，利用ELFhash算法把url尽量平均分部到各个队列中去，就能够用较多的线程同时抓取一个域名下的网页，速度将得到大大的提高。


```java
@Override
//重写 getClassKey()方法
public String getClassKey(CrawlController controller, CandidateURI cauri) {
    String uri = cauri.getURI().toString();
    long hash = ELFHash(uri);//利用 ELFHash 算法为 uri 分配 Key 值 
    String a = Long.toString(hash % 50);//取模 50，对应 50 个线程
    return a;
}
public long ELFHash(String str)
{
    long hash = 0;
    long x = 0;
    for(int i = 0; i < str.length(); i++) {
        hash = (hash << 4) + str.charAt(i);//将字符中的每个元素依次按前四位与上 
        if((x = hash & 0xF0000000L) != 0)//个元素的低四位想与
        {
            hash ^= (x >> 24);//长整的高四位大于零，折回再与长整后四位异或
            hash &= ~x; 
        }
    }
    return (hash & 0x7FFFFFFF); 
}
```

按照该教程完成代码的修改之后，重新运行，抓取速度得到了大幅提升，4个半小时的时间完成了清华新闻网的页面抓取工作。

![未修改hash函数时爬取速度](https://d2ppvlu71ri8gs.cloudfront.net/items/0t3D2z3R3L430c1O2E1O/Image%202017-05-24%20at%201.27.19%20上午.png)

![修改代码后的爬取速度明显加快](https://d2ppvlu71ri8gs.cloudfront.net/items/0G3I1S1X3Z0H3Q300O08/Image%202017-05-24%20at%202.00.48%20下午.png)

## 数据清洗及PageRank计算

在实验过程中我们需要使用页面的PageRank值来对网页进行打分，因此需要对抓取的数据计算其PageRank值，由于Heritrix在抓取时就已经有了`crawl.log`文件用于记录抓取到的网页链接及访问信息，因此在计算PageRank时直接使用了该文件的记录信息。

预处理及PageRank计算

```
├── clean_page.py : 页面清洗，
├── parse_graph_title_anchor.py ： 
├── share.py :  一些共有的文件名记录，html页面处理函数
└── tsinghua_rank.py ： 计算PageRank值，结果保存到pagerank.txt中
└── crawl.log : Heritrix 抓取的结果记录
```

需要注意的是，由于 Heritrix 在抓取带 GET 请求的网页时，存储文件的文件名和网址URL并不能一一对应(其去掉了问号，挪动了文件类型的位置)，且单从文件名并不能找到对应的 URL，所以第一步分析Heritrix爬取日志是必要且是必须的。通过分析日志，得到了URL到文件名的双向映射，同时删除了 404网页，将网页个数减少到 52205个页面。

在提取链接、标题、anchor时一开始使用的是BeautifulSoup进行提取，后来发现这种方式太慢，于是手动使用正则表达式进行提取。

```python
href_pattern = re.compile(r'<a href=[\"\']([^\"\']*?\.(html|pdf|doc|docx))[\"\'].+?>(.+?)</a>', re.S)
html_pattern = re.compile(r'<a href=[\"\']([^\"\']*?\.html)[\"\']', re.S)
title_pattern = re.compile(r'<title>(.+?)</title>', re.I | re.M | re.S)
```

计算完成之后的PageRank值如下所示：

1.  排名前10的页面
    
    ```
    /publish/thunews/index.html 0.0563122679453 首页  清华大学新闻网
/publish/thunews/9652/index.html    0.0448806058384 更多 &#8250;  清华大学新闻网 - 图说清华
/publish/thunewsen/index.html   0.0384040176156 ENGLISH Tsinghua University News
/publish/thunews/9650/index.html    0.0257207878312 媒体清华    清华大学新闻网 - 媒体清华
/publish/thunews/10303/index.html   0.0257202750476 综合新闻    清华大学新闻网 - 综合新闻
/publish/thunews/9656/index.html    0.0250196913473 清华人物    清华大学新闻网 - 清华人物
/publish/thunews/9649/index.html    0.02500709101   要闻聚焦    清华大学新闻网 - 要闻聚焦
/publish/thunews/9657/index.html    0.0249803912605 新闻合集    清华大学新闻网 - 新闻合集
/publish/thunews/10304/index.html   0.0249798815124 新闻排行    清华大学新闻网 - 新闻排行
/publish/thunews/9655/index.html    0.0243158953181 专题新闻    清华大学新闻网 - 专题新闻
    ```



2.  新闻页前10
    
    ```
    /publish/thunews/9648/2017/20170520203232435687344/20170520203232435687344_.html    0.00197507591613    邱勇出席第二届中以创新论坛：畅谈国际创新创业教育合作  邱勇出席第二届中以创新论坛：畅谈国际创新创业教育合作
/publish/thunews/9648/2017/20170518115011788320647/20170518115011788320647_.html    0.00197507591613    清华医学院程功研究组揭示寨卡病毒感染暴发机制  清华医学院程功研究组揭示寨卡病毒感染暴发机制
/publish/thunews/9648/2017/20170519190126950804131/20170519190126950804131_.html    0.00197507591613    邱勇会见以色列总统鲁文·里夫林·接受以色列特拉维夫大学荣誉博士学位   邱勇会见以色列总统鲁文·里夫林·接受以色列特拉维夫大学荣誉博士学位
/publish/thunews/9648/2017/20170522184445768862282/20170522184445768862282_.html    0.00197507591613    清华大学新闻与传播学院举办纪念成立15周年系列活动   清华大学新闻与传播学院举办纪念成立15周年系列活动
/publish/thunews/9648/2017/20170515184412579525281/20170515184412579525281_.html    0.00197507591613    清华大学全球可持续发展研究院正式揭牌成立    清华大学全球可持续发展研究院正式揭牌成立
/publish/thunews/9648/2017/20170516121327519550489/20170516121327519550489_.html    0.00197507591613    清华微电子所钱鹤、吴华强课题组在基于新型忆阻器阵列的类脑计算取得重大突破    清华微电子所钱鹤、吴华强课题组在基于新型忆阻器阵列的类脑计算取得重大突破
/publish/thunews/9652/2017/20170307133841881904789/20170307133841881904789_.html    0.00193350555685    【组图】最美三月女生节 浪漫创意盈满“幅”   【组图】最美三月女生节  浪漫创意盈满“幅”
/publish/thunews/9652/2017/20170314142112142471080/20170314142112142471080_.html    0.00193350555685    【组图】春到绿茵场 马杯足球赛正酣   【组图】春到绿茵场 马杯足球赛正酣
/publish/thunews/9945/2017/20170524164751321376872/20170524164751321376872_.html    0.00143223231223     5月18日，以“一带一路低碳前行”为主题的第十二届世界低碳城市联盟大会暨低碳城市发展论坛在三亚召开。 ... 2017-05-24  清华共同主办第十二届世界低碳城市联盟大会
/publish/thunews/9945/2017/20170524113049223303463/20170524113049223303463_.html    0.00143197048315     2017年“共和国的脊梁——科学大师名校宣传工程”汇演在重庆大学启动。清华大学原创话剧《马兰花开》被 ... 2017-05-24  清华原创话剧《马兰花开》在科学大师名校宣传工程汇演上首演
/publish/thunews/9945/2017/20170522171134178522272/20170522171134178522272_.html    0.00143197048315     5月19日晚，清华大学巅峰对话第二十期物理分论坛在清华大学举行。本次活动邀请了2015年诺贝尔物理学 ... 2017-05-23  诺贝尔物理学奖得主梶田隆章做客“巅峰对话”
/publish/thunews/9945/2017/20170524093408535456498/20170524093408535456498_.html    0.00143197048315     5月18日—21日，清华大学第一附属医院党委书记类延旭和副院长朱栓立带领一附院医疗分队走进昆明市东川 ... 2017-05-24  清华大学第一附属医院走进昆明健康义诊
    ```

由PageRank计算结果可以发现，正常的新闻页面的PageRank值大多在$10^{-6} ~10^{-4}$之间，如果直接将该PageRank值与BM25算法的得分相乘会导致PageRank值高的页面，即使关键词出现次数少，也会在最终排名中特别靠前，为了减少其影响，在将PageRank值应用到Score计算时对PageRank值进行压缩$$newPageRank = 16 + ln(PageRank)$$

## 构建索引及倒排索引

### 文档解析

### Html解析

网页文件元素十分丰富。实验中使用Jsoup工具包解析网页，抽取title标签的文本内容作为文档的的标题域；抽取p、span、td、div、li、a标签的文本内容作为文档的内容域；a标签的内容表示页面链出的内容，也作为一个域单独索引；h1-h6标签的文本内容表示页面内的小标题，拿出来作为一个域；此外，进入页面的链接有着和页面标题相似的作用，单独成为一个域。

### PDF解析

PDF的元素不易区分，实验中使用pdfbox解析文件获得内容域，直接以文件名作为标题域。

### Doc解析

Doc文件与PDF文件类似，实验中使用POI包解析文件获得内容域，直接以文件名作为标题域。需要注意的是，POI工具解析.doc文件.docx文件的方法并不一样，在实验中，我们为此耽误了不少时间。

## 检索

### 修改图片搜索框架

在实验开始，我们修改了图片搜索的框架，进行如下操作。

1. 对查询进行分词后获得token列表。
2. 对每一个token的倒排索引，只需满足一个域的文档即认为是属于该token的文档
3. 满足所有token的文档才能作为整个查询的文档进行评分
4. 对每个token的每个域计算BM25并求和，最后加上页面的PageRank值。加上PageRank值而非相乘，可以避免索引页面总排在最前面而在评分相差不大时获得优势

### 使用MultiFieldQueryParser

通过修改框架的方式获得了很大的自由空间，但实现上效率很低，搜索结果用时很长。由此，我们使用MultiFieldQueryParser替代自己实现的SimpleQuery、SimpleSimilarity、SimpleScorer等类。为了使用BM25评分，Lucene也改为4.0版本，相应地，IK Analyzer也修改了版本。至此，我们使用Lucene提供的BM25Simlarity计算BM25评分。

### 分域权重

我们抽取1000个文档进行测试，给各个域赋予不同的权重，作为boosts参数传给MultiFieldQueryParser。在使用整体数据进行测试的过程中，我们也进行了相应调整，最后确定了100、25、35、1、0.001的一组权重。

### 文档摘要

呈现文档时，我们对文档内容抽取摘要进行展示。建立所有token在文档内容中的位置构成的集合，从前开始，并呈现token前后的30个字符；若两个token临近则连续输出。

```java
public static String genAbstract(List<String> tokens, String content) {
    int maxLength = 300;
    int range = 30;
    String result = "";
    content = content.trim();
    List<Integer> startPositions = new ArrayList<Integer>();
    List<Integer> endPositions = new ArrayList<Integer>();
    for (String t : tokens) {
        String token = new String(t);
        int colonIndex = token.indexOf(':');
        if (colonIndex >= 0) {
            token = token.split(":")[1];
        }
        int pos = 0;
        Pattern pattern = Pattern.compile(token, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);

        int num = 0;
        while (matcher.find(pos) && ++num < maxLength / range) {
            pos = matcher.start();
            startPositions.add(pos);
            endPositions.add(pos + token.length());
            ++pos;
        }
    }
    Collections.sort(startPositions);
    Collections.sort(endPositions);
    int i = 0;
    int size = startPositions.size();
    while (i < size) {
        int pos = startPositions.get(i);
        int end = endPositions.get(i);
        int ptr;
        for (ptr = pos; ptr >= pos - range; --ptr) {
            if (ptr < 0 || stopChar.contains(content.charAt(ptr))) {
                ++ptr;
                break;
            }
        }
        result += content.subSequence(ptr, pos);
        result += "<em>";
        result += content.subSequence(pos, end);
        result += "</em>";
        ++i;
        while (i < size) {
            pos = startPositions.get(i);
            if (end > pos) {
                result += "<em>";
                result += content.subSequence(end, endPositions.get(i));
                result += "</em>";
                pos = end;
                end = endPositions.get(i);
                ++i;
            } else {
                if (pos == end) {
                    result += content.subSequence(end, pos);
                    end = endPositions.get(i);
                    result += "<em>";
                    result += content.subSequence(pos, end);
                    result += "</em>";
                    ++i;
                } else if (pos - end < range) {
                    result += content.subSequence(end, pos);
                    end = endPositions.get(i);
                    result += "<em>";
                    result += content.subSequence(pos, end);
                    result += "</em>";
                    ++i;
                    if (result.length() > maxLength - range) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        for (ptr = end; ptr < end + range; ++ptr) {
            if (ptr >= content.length()
                    || stopChar.contains(content.charAt(ptr))) {
                break;
            }
        }
        result += content.subSequence(end, ptr) + "... ";

        if (result.length() > maxLength) {
            break;
        }
    }
    return result;
}
```

但这种方法并不总能正确呈现查询词的位置。之后，我们使用Lucene自带的highlight进行处理，一些原来认为毫无关系的文档也能看到相关性。

# 实验结果

![刘奕群 陈旭](https://d2ppvlu71ri8gs.cloudfront.net/items/2l102l3S3A421J3M2d2B/Image%202017-06-04%20at%208.33.17%20下午.png)

![超算](https://d2ppvlu71ri8gs.cloudfront.net/items/3X183g2t120S0T1U2X30/Image%202017-06-04%20at%2010.16.57%20下午.png)

![长文本搜索](https://d2ppvlu71ri8gs.cloudfront.net/items/1v3u1A1D27233b0V1p0D/Image%202017-06-04%20at%2010.20.50%20下午.png)

# 心得体会

实验开始，我们完成了对图像搜索框架的修改，并实现了摘要提取，费尽周章进行调试，但是效果始终不理想。一个是响应速度慢，需要十几秒，一个是显示的摘要大多时候表现不出和查询的相关性。

这个时候，我们采用了Lucene自带的MultiFieldQueryParser进行查询，显著缩短了查询时间，增强了查询体验；同时，Lucene框架内的highlights提供了更加友好的摘要，使得一些标题好像根本不相关的页面也表现出了相关性。与此同时，原有的代码被删减近半。

痛惜之余，我们也感受到了开源社区的优越性，反复造轮子的过程是对时间的浪费，多使用已有的工具包可以获得更好的效果。回过头来看，似乎所有的工作在最后一天下午又重新做起了。

在重新构建框架的过程中也进一步发现了很多有用的开源工具，也发现了很多可进一步扩展的功能，不过由于前期反复造轮子耗时过多导致最后时间不够没能进一步实现。

