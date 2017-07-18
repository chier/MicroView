package her.http;

import her.bean.JSONBean;
import her.dao.impl.DomainDaoImpl;
import her.utils.GlobalVariable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;
import org.json.JSONObject;
import org.python.antlr.PythonParser.else_clause_return;

import com.kenai.jaffl.mapper.FromNativeContext;
import com.sun.org.apache.bcel.internal.generic.Select;

import baidusearch.SearchBYBaidu;

public class DomainServlet extends HttpServlet {
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
		
		//为图片存储临时文件配置地址
		GlobalVariable.filePath = this.getServletConfig().getServletContext().getRealPath("tempfile/");
		
		request.setCharacterEncoding("UTF-8");
		String action = request.getParameter("action");
		String jsonValueString = "";
		if(action.toLowerCase().equalsIgnoreCase("geteventcolumnlist"))
		{
			String tablename = request.getParameter("tablename");
//			String tablename = "event";
			DomainDaoImpl domainDaoImpl = new DomainDaoImpl();
			JSONBean jsonBean = domainDaoImpl.getColumnList(tablename);
			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("uploadattachment"))
		{
			String refer_tables = request.getParameter("refer_tables");
			String where = request.getParameter("where");
			DomainDaoImpl domainDaoImpl = new DomainDaoImpl();
			JSONBean jsonBean = null;
			try {
				jsonBean = domainDaoImpl.uploadAttachment(request, refer_tables, where);
			} catch (FileUploadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("downloadattachment"))
		{
			String attachment_id = request.getParameter("attachment_id");
			
			DomainDaoImpl domainDaoImpl = new DomainDaoImpl();
			domainDaoImpl.downloadAttachment(response, attachment_id);
		}
		else if(action.toLowerCase().equalsIgnoreCase("getsqlcolumns"))
		{
			String sql = request.getParameter("sql");
			String client = request.getParameter("client");
			DomainDaoImpl domainDaoImpl = null;
			if (client != null && client.equalsIgnoreCase("true")) {
				domainDaoImpl = new DomainDaoImpl(true);
			}
			else {
				domainDaoImpl = new DomainDaoImpl();
			}
			if(sql.toLowerCase().indexOf(" from ") == -1){
				sql = "select * from " + sql;
			}
			JSONBean jsonBean = domainDaoImpl.getSQLStructure(sql);
			jsonValueString = jsonBean.toJSONString();
			System.out.println(jsonValueString);
		}
		else if(action.toLowerCase().equalsIgnoreCase("gettablecolumns"))
		{
			String table = request.getParameter("table");
			String client = request.getParameter("client");
			DomainDaoImpl domainDaoImpl = null;
			if (client != null && client.equalsIgnoreCase("true")) {
				domainDaoImpl = new DomainDaoImpl(true);
			}
			else {
				domainDaoImpl = new DomainDaoImpl();
			}

			JSONBean jsonBean = domainDaoImpl.getFieldsInfo(table);
			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("search"))
		{
			String key = request.getParameter("key");
			String pagenum = request.getParameter("pagenum");
			String region = request.getParameter("region");
			String gps = request.getParameter("gps");
			System.out.println(key+":"+region);
			SearchBYBaidu searchBaidu = new SearchBYBaidu();
			JSONObject jsonObject = searchBaidu.SearchPlace(gps, key, region, pagenum);
			if(jsonObject != null)
				jsonValueString = jsonObject.toString();
		}
		else if (action.toLowerCase().equalsIgnoreCase("translation")) {
			String dbtype = request.getParameter("db");
			String transtype = request.getParameter("type");
			String transdata = request.getParameter("data");
			DomainDaoImpl domainDaoImpl = null;
			JSONBean jsonBean = null;
			if (dbtype.equalsIgnoreCase("disaster")) {
				domainDaoImpl = new DomainDaoImpl();
				jsonBean = domainDaoImpl.translation_disaster(transtype, transdata);
			}
			else if (dbtype.equalsIgnoreCase("client")) {
				domainDaoImpl = new DomainDaoImpl(true);
				jsonBean = domainDaoImpl.translation_client(transtype, transdata);
			}
			else {
				domainDaoImpl = new DomainDaoImpl();
			}
			if(jsonBean != null)
				jsonValueString = jsonBean.toJSONString();
		}
		else if (action.toLowerCase().equalsIgnoreCase("translation_element")) {
			String dbtype = request.getParameter("db");
			String table = request.getParameter("table");
			String field = request.getParameter("field");
			String values = request.getParameter("values");
			DomainDaoImpl domainDaoImpl = null;
			JSONBean jsonBean = null;
			if (dbtype.equalsIgnoreCase("disaster")) {
				domainDaoImpl = new DomainDaoImpl();
				jsonBean = domainDaoImpl.translation_element(dbtype, table, field, values);
			}
			else if (dbtype.equalsIgnoreCase("client")) {
				domainDaoImpl = new DomainDaoImpl(true);
				jsonBean = domainDaoImpl.translation_element(dbtype, table, field, values);
			}
			else {
				domainDaoImpl = new DomainDaoImpl();
			}
			if(jsonBean != null)
				jsonValueString = jsonBean.toJSONString();
		}
		else if (action.toLowerCase().equalsIgnoreCase("queryattribute")) {
			String db = request.getParameter("tag");
			String fields = request.getParameter("fields");
			String from = request.getParameter("from");
			String where = request.getParameter("where");
			String other = request.getParameter("other");
			DomainDaoImpl domainDaoImpl = null;
			JSONBean jsonBean = null;
			if(db.equalsIgnoreCase("Client")){
				domainDaoImpl = new DomainDaoImpl(true);
				jsonBean = domainDaoImpl.queryAttribute(fields, from, where, other);
			}
			else {
				domainDaoImpl = new DomainDaoImpl();
				jsonBean = domainDaoImpl.queryAttribute(fields, from, where, other);
			}
			if(jsonBean != null)
				jsonValueString = jsonBean.toJSONString();
		}
		else if (action.toLowerCase().equalsIgnoreCase("getmaxmindate")) {
			String db = request.getParameter("db");
			String sql = request.getParameter("sql");
			String datefield = request.getParameter("datefield");
			DomainDaoImpl domainDaoImpl = null;
			JSONBean jsonBean = null;
			if(db.toLowerCase().equalsIgnoreCase("client")){
				domainDaoImpl = new DomainDaoImpl(true);
				jsonBean = domainDaoImpl.getMaxMinDate( datefield,sql );
			}
			else {
				domainDaoImpl = new DomainDaoImpl();
				jsonBean = domainDaoImpl.getMaxMinDate( datefield,sql );
			}
			if(jsonBean != null)
				jsonValueString = jsonBean.toJSONString();
		}
		else if (action.toLowerCase().equalsIgnoreCase("getdefaultsetting")) {
			String pageid = request.getParameter("pageid");
			DomainDaoImpl domainDaoImpl = null;
			JSONBean jsonBean = null;
			domainDaoImpl = new DomainDaoImpl(true);
			jsonBean = domainDaoImpl.getDefaultSetting(pageid);
			if(jsonBean != null)
				jsonValueString = jsonBean.toJSONString();
		}
		else if (action.toLowerCase().equalsIgnoreCase("proxy")) {
			String urlStr = request.getParameter("url");
			String tStr = request.getParameter("t");
			String callbackStr = request.getParameter("callback");
			String params = "";
			if(tStr != null){
				params += "?t="+tStr;
			}
			if(callbackStr != null){
				params += "&callback="+callbackStr;
			}
			URL url;
			try {
				url = new URL(urlStr+params);
				URLConnection urlConnection = url.openConnection();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
				StringBuffer sBuffer = new StringBuffer();
				String res = null;
				while ((res = bufferedReader.readLine()) != null) {
					sBuffer.append(res);
				}
				jsonValueString = sBuffer.toString();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		out.write(jsonValueString);
		out.flush();
		out.close();
	}
}
