# 运行环境

* Ubuntu 16.04
* Tomcat 8
* openjdk 1.8.0_121
* Eclipse 3.8.1

# 使用方法

## 生成索引

* 将爬下来的文件链接到项目根目录下。
```
sudo ln -s /home/root/Downloads/files/ /home/root/searcher/
```

* 将链接分析后的结果放在该文件夹下，每行一条链接，路径、pagerank、链接名用制表符隔开。

* 将项目导入Eclipse，运行`Indexer.java`。

## 部署

* 运行`sh setup.sh`完成项目部署。

* 从`localhost:8080/searcher`访问服务。
