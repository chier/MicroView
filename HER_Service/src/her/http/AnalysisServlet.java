package her.http;

import her.bean.JSONBean;
import her.dao.impl.AnalysisDaoImpl;
import her.model.InsertException;
import her.utils.GlobalVariable;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

public class AnalysisServlet extends RasterSessionBaseServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		this.doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/xml;charset=utf-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		
		//为图片等文件存储临时文件配置地址
		GlobalVariable.filePath = this.getServletConfig().getServletContext().getRealPath("tempfile/");
		
		request.setCharacterEncoding("UTF-8");
		String action = request.getParameter("action");
		String jsonValueString = "";
		if(action.toLowerCase().equalsIgnoreCase("statisticalcomputer"))
		{
			String tag = request.getParameter("tag");
			String other = request.getParameter("other");
			JSONObject argsObj = null;
			try {
				argsObj = new JSONObject(tag);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String h_sql = "";
			try {
				h_sql = argsObj.getString("h_sql");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String h_spatial_f = "";
			try {
				h_spatial_f = argsObj.getString("h_spatial_f");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String e_sql = "";
			try {
				e_sql = argsObj.getString("e_sql");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String e_spatial_f = "";
			try {
				e_spatial_f = argsObj.getString("e_spatial_f");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String h_f = "";
			try {
				h_f = argsObj.getString("h_f");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String e_f = "";
			try {
				e_f = argsObj.getString("e_f");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String type = null;
			try {
				type = argsObj.getString("type");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String method = "";
			try {
				method = argsObj.getString("method");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String merge = null;
			try {
				merge = argsObj.getString("merge");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if(merge == null){
				merge = "true";
			}
			AnalysisDaoImpl analysisDaoImpl = new AnalysisDaoImpl(true);
			JSONBean jsonBean = null;
			try {
				jsonBean = analysisDaoImpl.statisticalComputer(h_sql, h_spatial_f, e_sql, e_spatial_f, h_f, e_f, type, method, merge, other);
			} catch (SQLException | InsertException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(null != jsonBean)
				jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("querydisaster_returntime"))
		{
			String rtid = request.getParameter("rtid");
			AnalysisDaoImpl analysisDaoImpl = new AnalysisDaoImpl(true);
			JSONBean jsonBean = null;
			jsonBean = analysisDaoImpl.returnTimeQueryDisaster(rtid);
			if(null != jsonBean)
				jsonValueString = jsonBean.toJSONString();
		}
		//正问题
		else if(action.toLowerCase().equalsIgnoreCase("querydata_returntime"))
		{
			String type = request.getParameter("type");
			String rtid = request.getParameter("rtid");
			String disaster_ids = request.getParameter("disaster_ids");
			String statistic_field = request.getParameter("statistic_field");
			String where = request.getParameter("where");
			AnalysisDaoImpl analysisDaoImpl = new AnalysisDaoImpl(true);
			JSONBean jsonBean = null;
			jsonBean = analysisDaoImpl.returnTimeQueryData(type, rtid, disaster_ids, statistic_field,where);
			if(null != jsonBean)
				jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("querydisaster_returntime_cons"))
		{
			String rtid = request.getParameter("rtid");
			AnalysisDaoImpl analysisDaoImpl = new AnalysisDaoImpl(true);
			JSONBean jsonBean = null;
			jsonBean = analysisDaoImpl.returnTimeQueryDisaster_cons(rtid);
			if(null != jsonBean)
				jsonValueString = jsonBean.toJSONString();
			
			System.out.println(rtid);
			System.out.println(jsonValueString);
		}
		//反问题
		else if(action.toLowerCase().equalsIgnoreCase("querydata_returntime_cons"))
		{
			String type = request.getParameter("type");
			String rtid = request.getParameter("rtid");
			String disaster_ids = request.getParameter("disaster_ids");
			String st1 = request.getParameter("statistic_field1");
			String st2 = request.getParameter("statistic_field2");
			String starttime = request.getParameter("expo_starttime");
			String endtime = request.getParameter("expo_endtime");
			String where = request.getParameter("where");
			AnalysisDaoImpl analysisDaoImpl = new AnalysisDaoImpl(true);
			JSONBean jsonBean = null;
			jsonBean = analysisDaoImpl.returnTimeQueryData_cons(type, rtid, disaster_ids, st1, st2, starttime, endtime, where);
			if(null != jsonBean)
				jsonValueString = jsonBean.toJSONString();
		}
		//反问题对比
		else if(action.toLowerCase().equalsIgnoreCase("querycomparedata_returntime_cons"))
		{
			String type = request.getParameter("type");
			String rtid = request.getParameter("rtid");
			String disaster_ids = request.getParameter("disaster_ids");
			String st = request.getParameter("statistic_field");
			String starttime = request.getParameter("expo_starttime");
			String endtime = request.getParameter("expo_endtime");
			String where = request.getParameter("where");
			AnalysisDaoImpl analysisDaoImpl = new AnalysisDaoImpl(true);
			JSONBean jsonBean = null;
			jsonBean = analysisDaoImpl.returnTimeQueryCompareData_cons(type, rtid, disaster_ids, st, starttime, endtime, where);
			if(null != jsonBean)
				jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("querydata_common"))
		{
			String fields = request.getParameter("fields");
			String from = request.getParameter("from");
			String where = request.getParameter("where");
			String other = request.getParameter("other");
			AnalysisDaoImpl analysisDaoImpl = new AnalysisDaoImpl(true);
			JSONBean jsonBean = analysisDaoImpl.commonTableQuery(fields, from, where, other);
			if(null != jsonBean)
				jsonValueString = jsonBean.toJSONString();
		}
		else if (action.toLowerCase().equalsIgnoreCase("get_downscale_vector_list")) {
			AnalysisDaoImpl analysisDaoImpl = new AnalysisDaoImpl();
			JSONBean jsonBean = analysisDaoImpl.getDownscaleVectorList();
			
			if(null != jsonBean)
				jsonValueString = jsonBean.toJSONString();
		}
		else if (action.toLowerCase().equalsIgnoreCase("data_downscale")) {
			String sql = request.getParameter("sql");
			String tag = request.getParameter("tag");//返回携带参数
			AnalysisDaoImpl analysisDaoImpl = new AnalysisDaoImpl();
			JSONBean jsonBean = analysisDaoImpl.dataDownscale(request,sql,tag);
			
			if(null != jsonBean)
				jsonValueString = jsonBean.toJSONString();
		}
		else if (action.toLowerCase().equalsIgnoreCase("report_menu")) {
			String type = request.getParameter("type");//actual,history
			AnalysisDaoImpl analysisDaoImpl = new AnalysisDaoImpl(true);
			JSONBean jsonBean = analysisDaoImpl.getReportParam(type);
			if(null != jsonBean)
				jsonValueString = jsonBean.toJSONString();
		}
		else if (action.toLowerCase().equalsIgnoreCase("report")) {
			response.setContentType("text/html;charset=utf-8");
			String type = request.getParameter("type");//actual,history
			String id = request.getParameter("id");
			String func_sql = request.getParameter("func_sql");
			String sql = "";
			if(type.equalsIgnoreCase("actual")){
				sql = func_sql;
			}
			else if(type.equalsIgnoreCase("history")){
				sql = "select result as html from client_report_history where id="+id;
			}
			AnalysisDaoImpl analysisDaoImpl = new AnalysisDaoImpl(true);
			Map<String, String> report = analysisDaoImpl.queryReport(sql);
			jsonValueString = report.get("htmlurl");
			this.SetSession(request, "imagefilename", report.get("htmlurl"));
		}
		out.write(jsonValueString);
		out.flush();
		out.close();
	}
}
