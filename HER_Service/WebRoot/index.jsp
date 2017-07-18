<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<base href="<%=basePath%>">

<title>进度条</title>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<!-- js文件和代码 -->
<script type="text/javascript" src="js/progress.js"></script>
<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->
</head>

<body id="go" onload="go();" style="top: 200px; hight: 800px">
	<div
		style="top: 145px; width: expression(body.clientWidth); z-index: 100; border: none;">
		<%int i = 1; %>
		
		<script type="text/javascript">
			function onclick() {
				//在表单中添加一个隐藏元素，目的是测试是否获得动态的用户名，以便在Servlet中接收并传递到数据库中  
		        var f = document.uploadForm.photo.value ;  
		        //用元素的id获得该元素的值，从而进行判断选择的文件是否合法  
		        var file = document.uploadForm.p.value ;  
		        if(file==null||file==""){  
		            alert("你还没有选择任何文件，不能上传!") ;  
		            return ;  
		        }  
		        if(file.lastIndexOf(".")==-1){  
		            alert("路径不正确!") ;  
		            return ;  
		        }  
		        var allImgExt = ".jpg|.jpeg|.gif|.bmp|.png|" ;  
		        var extName = file.substring(file.lastIndexOf(".")) ;  
		        if(allImgExt.indexOf(extName+"|")==-1){  
		              
		            errMsg="该文件类型不允许上传。请上传 "+allImgExt+" 类型的文件，当前文件类型为"+extName;  
		            alert(errMsg);  
		            return;  
		        }  
		        document.uploadForm.submit() ;  
			}
		</script>
		<form action="/servlet/FileUploaded" method="post"
			enctype="multipart/form-data">
			请选择上传的图片或文件:<input type="file" name="fileName" /><input type="submit"
				value="上传" />
		</form>
		<table border="0" align="center" cellpadding="0" cellspacing="0">
			<br>
			<br>
			<tr>
				<td height="20" colspan="3" valign="top">
					<center>
						<h2>数据正在处理中，请稍候……</h2>
					</center>
				</td>
			</tr>
			<tr>
				<td colspan="3" valign="top">
					<table align="center">
						<tr>
							<td align="center">
								<div id="progressBar"
									style="padding: 2px; border: solid green 1px; visibility: hidden"
									mce_style="padding:2px;border:solid green 1px;visibility:hidden"
									align="left">
									<div style="width: 800px">
										<c:forEach begin="1" end="50" step="1">
											<span id="block<%=i++ %>" style="width: 2%;"></span>
										</c:forEach>
									</div>
								</div>
							</td>
							<td align="center" id="finish"></td>
						</tr>
						<tr>
							<td align="center" id="complete"></td>
						</tr>
					</table>
				</td>
			</tr>
		</table>
	</div>
</body>
</html>
