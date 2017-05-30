<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
    request.setCharacterEncoding("utf-8");
    response.setCharacterEncoding("utf-8");
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
    String rootPath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/files/";
    String thuPath = "http://news.tsinghua.edu.cn/"; 
    String currentQuery=(String) request.getAttribute("currentQuery");
    int currentPage=(Integer) request.getAttribute("currentPage");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>校园搜索|<%=currentQuery%></title>
    <style>
        .abstract em {
            color:#F00
        }
    </style>
</head>
<body style="margin:0; padding:0">
    <div id="header" style="margin:0 auto; background-color:#5C307D; height:100%; width:100%; padding:20px">
        <img src="<%=path%>/static/img/logo.png" style="margin-top:20px; margin-bottom:20px"/>
        <form id="form1" name="form1" method="get" action="Server">
            <div style="float:left">
                <input name="query" value="<%=currentQuery%>" type="text" size="70" style="width:400px; float:left"/>
                <input type="submit" name="Submit" value="查询" style="width:80px; float:right"/>
            </div>
        </form>
        <div style="font: 0px/0px sans-serif;clear: both;display: block"> </div> 
    </div>

    <div id="content"  style="margin-left:50px; margin-right:100px; margin-top:50px">
        <Table style="left: 0px; width: 594px;">
        <% 
            String[] titles=(String[]) request.getAttribute("titles");
          	String[] paths=(String[]) request.getAttribute("paths");
          	String[] absts=(String[]) request.getAttribute("absts");
          	String[] types=(String[]) request.getAttribute("types");
          	if(titles!=null && titles.length>0){
          		for(int i=0;i<titles.length;i++){%>
          		<p>
          		<tr><h3><a href="<%=thuPath + paths[i]%>"><%=(currentPage-1)*10+i+1%>. <%=titles[i] %></a></h3></tr>
          		<tr><p class="abstract"><%=absts[i]%></p></tr>
          		<tr><a href="<%=rootPath + paths[i]%>">快照</a></tr>
          		</p>
          		<%}; %>
          	<%}else{ %>
          		<p><tr><h3>No such result</h3></tr></p>
          	<%}; %>
      	</Table>
  	<div>
  	<div id="footer">
	<%if(currentPage>1){ %>
		<a href="Server?query=<%=currentQuery%>&page=<%=currentPage-1%>">上一页</a>
	<%}; %>
	<%for (int i=Math.max(1,currentPage-5);i<currentPage;i++){%>
		<a href="Server?query=<%=currentQuery%>&page=<%=i%>"><%=i%></a>
	<%}; %>
	    <strong><%=currentPage%></strong>
	<%for (int i=currentPage+1;i<=currentPage+5;i++){ %>
		<a href="Server?query=<%=currentQuery%>&page=<%=i%>"><%=i%></a>
	<%}; %>
	    <a href="Server?query=<%=currentQuery%>&page=<%=currentPage+1%>">下一页</a>
	</div>
</body>
