package her.http;

import her.bean.JSONBean;
import her.bean.RasterRain;
import her.bean.JSONBean.Msg;
import her.bean.JSONBean.Status;
import her.dao.impl.TestSQLImpl;
import her.utils.GlobalVariable;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class TestInSQL extends RasterSessionBaseServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		this.doPost(request, response);
	}

	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String sqlString = request.getParameter("sql");
		String db = request.getParameter("db");
		String tag = request.getParameter("tag");//携带参数
		String callback = request.getParameter("callback");

		
		//为图片存储临时文件配置地址
		GlobalVariable.filePath = this.getServletConfig().getServletContext().getRealPath("tempfile/");
		System.out.println(sqlString);
		System.out.println(tag);
		JSONBean jsonBean = new JSONBean();
		if (sqlString != null) {
			TestSQLImpl tSqlImpl = new TestSQLImpl(sqlString);
			tSqlImpl.Execute(db);
			jsonBean = tSqlImpl.getJsonBean();
			//返回携带参数
			if(jsonBean != null && tag != null){
				Map<String, Object> result;
				if(jsonBean.getResult() != null){
					result = (Map<String, Object>)jsonBean.getResult();
					result.put("tag", tag);
				}
				else{
					result = new HashMap<String, Object>();
					result.put("tag", tag);
					jsonBean.setResult(result);
				}
			}
			this.SetSession(request, "imagefilename", tSqlImpl.getImageSession());
		} else {// 如果没有sql命令，则不进行查询，返回一个空的json字符串
			jsonBean.setMsg(Msg.PARAMETER_ERROR);
			jsonBean.setStatus(Status.FAILED);
		}
		response.addHeader( "Access-Control-Allow-Origin", "*" ); 
		response.addHeader( "Access-Control-Allow-Methods", "POST" ); 
		response.addHeader( "Access-Control-Max-Age", "1000" );
		String outString = jsonBean.toJSONString();
		outString = outString.replaceAll(">", "大于");
		outString = outString.replaceAll("<", "小于");
		if(callback != null && !"".equals(callback)){
			outString = callback + "(" + outString + ")";
		}
		// 输出到response
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/json; charset=UTF-8");  
		PrintWriter out = response.getWriter();
		out.write(outString);
		out.flush();
		out.close();
	}

}
