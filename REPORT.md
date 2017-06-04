---
title: 校园搜索引擎
date: 2017-05-07
fontsize: 12pt
author: 计41  张盛豪  2014011450
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
- 抓取工具： Heritrix (http://crawler.archive.org/)
- 索引构建及检索工具：Lucene (http://lucene.apache.org/)
- 分词工具：ICTCLAS分词工具(http://ictclas.org/)或庖丁解牛分词包(http://code.google.com/p/paoding/)
- 前端服务：apache+tomcat (http://tomcat.apache.org/) 

# 重点

开源搜索引擎运行流程

# 难点：
- 资源抓取控制：Heritrix文件格式/IP地址过滤器设置、存储中的编码问题等。
- 网页预处理：网页内容编码（可以过滤掉除GBK和UTF-8以外的内容）、无关内容过滤等。
- 链接结构分析:PageRank计算，Anchor信息重定位
- PDF，M.S. office文档解析。
- 确定结果相关性计算公式。
- 检索结果正确性验证。

# 实验要求
- 抓取清华校内绝大部分网页资源以及大部分在线万维网文本资源（含M.S.office文档、pdf文档等，约20-30万个文件）
- 实现基于概率模型的内容排序算法；
    - 图片检索实验已经让大家实现对查询不分词的BM25模型，建议改写框架相或查找开源资源在其之上进行加工。
- 实现基于HTML结构的分域权重计算，并应用到搜索结果排序中；
    - Title, Anchor, Keyword, content, h1-h6等，如何确定权重？
    - 建立小规模测试集合，进行参数调节
- 实现基于PageRank的链接结构分析功能，并应用到搜索结果排序中；
    - 离线计算PageRank，如何与在线更新的结果整合？
- 采用便于用户信息交互的Web界面。
    - 可以考虑尝试实现查询扩展、查询纠错等功能。
    - 是否可能设计与主流搜索引擎展现方式不同的界面？



# 实现流程

## 爬虫爬取清华校内网页数据

### Heritrix抓取对象

[Heritrix环境搭建教程](https://www.ibm.com/developerworks/cn/opensource/os-cn-heritrix/)

- 清华校内网页（不包括图书馆）资源
- 种子至少包含http://news.tsinghua.edu.cn/
- 使用正则表达式对URL进行过滤
    - 过滤无关页面，过滤无关格式文件，，保留html页面和pdf,word文档，去除奇怪链接：  .*(?i)\.(mso|tar|txt|asx|asf|bz2|mpe?g|MPE?G| tiff?|gif|GIF|png|PNG|ico|ICO|css|sit|eps|wmf|zip|pptx?|xlsx?|gz|rpm|tgz|mov|MOV|exe|jpe?g|JPE?G|bmp|BMP|rar|RAR|jar|JAR|ZIP|zip|gz|GZ|wma|WMA|rm|RM|rmvb|RMVB|avi|AVI|swf|SWF|mp3|MP3|wmv|WMV|ps|PS|d|dd|yyyy|nivo-nextNav|xiao_ming)$
    - 禁止抓取图书馆资源：  [\S]*lib.tsinghua.edu.cn[\S]*；[\S]*166.111.120.[\S]*

### Heritrix 抓取过程中遇到的问题

#### 页面剔除问题

在抓取过程中发现了部分

####  Heritrix 加速问题



## 构建索引

## 倒排索引

## 检索


[多线程](http://blog.csdn.net/yangding_/article/details/41122977)


```
public String getClassKey(CrawlController controller, CandidateURI cauri) {
        String uri = cauri.getUURI().toString();
        long hash = ELFHash(uri);   // //利用 ELFHash 算法为 uri 分配 Key 值
        String a = Long.toString(hash % 50); //取模 50，对应 50 个线程
        return a;
    
    }
    
    public long ELFHash(String str) {
        long hash = 0;
        long x = 0;
        for (int i = 0; i < str.length(); i++) {
            hash = (hash << 4) + str.charAt(i); //将字符中的每个元素依次按前四位与上个元素的低四位相与
            if ((x = hash & 0xF0000000L) != 0) {
                hash ^= (x >> 24);  //长整的高四位大于零，折回再与长整后四位异或
                hash &= ~x;
            }
    
        }
        return (hash & 0x7FFFFFFF);
    }
```

```
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
