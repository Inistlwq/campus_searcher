<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
	request.setCharacterEncoding("utf-8");
	System.out.println(request.getCharacterEncoding());
	response.setCharacterEncoding("utf-8");
	System.out.println(response.getCharacterEncoding());
	String path = request.getContextPath();
	String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
	System.out.println(path);
	System.out.println(basePath);
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html style="height:100%" xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<title>校园搜索</title>
	<style>
	    p {
	        font-size:50px;
	        color:#FFF
	    }
	</style>
</head>
<body style="background-color:#5C307D; text-align:center; height:100%">
	<div style="margin:0 auto; width: 500px">
		<img src="<%=path%>/static/img/logo.png" style="margin-top:100px; margin-bottom:20px"/>
		<form id="form1" name="form1" method="get" action="servlet/Server">
			<div style="float:left">
				<input name="query" type="text" size="50" style="width:400px; float:left"/>
				<input type="submit" name="Submit" value="搜索" style="width:80px; float:right"/>
			</div>
		</form>
		<p style="margin-top:80px"><i>Search</i></p>
        <p><i>the Campus</i></p>
	</div>
</body>
</html>
