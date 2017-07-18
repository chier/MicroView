package her.http;

import her.bean.JSONBean;
import her.bean.JSONBean.Status;
import her.dao.impl.DomainDaoImpl;
import her.dao.impl.DownloadDaoImpl;
import her.dao.impl.ExposureDaoImpl;
import her.dao.impl.UploadDaoImpl;
import her.dao.impl.UploadDaoImpl.FileType;
import her.model.InsertException;
import her.utils.GlobalVariable;
import her.utils.WebConfigUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import jxl.read.biff.BiffException;

import org.apache.commons.fileupload.FileUploadException;
import org.json.JSONException;
import org.json.JSONObject;

public class ExposureServlet extends HttpServlet {
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
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/json; charset=UTF-8");
		
		//为图片存储临时文件配置地址
		GlobalVariable.filePath = this.getServletConfig().getServletContext().getRealPath("tempfile/");
//		System.out.println(WebConfigUtils.getValue("tempfilepath").trim().isEmpty());
//		System.out.println(GlobalVariable.imgPath);
		PrintWriter out = response.getWriter();
		String action = request.getParameter("action");
		String jsonValueString = "";

		//查询标的表单树菜单 逐单
		if(action.toLowerCase().equalsIgnoreCase("createsearchtreeper"))
		{
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl();
			String treeString = "";
			try {
				treeString = exposureDaoImpl.createTreeXmlPer();
			} catch (TransformerFactoryConfigurationError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			jsonValueString = treeString;
//			treeString = treeString.replaceAll("\"", "\'");
//			treeString = treeString.replaceAll("\n", "");
//			jsonValueString = "{\"status\":\"SUCCESS\",\"result\":\""+treeString+"\"}";
			System.out.println(jsonValueString);
		}
		//查询标的-树菜单参数
		else if(action.toLowerCase().equalsIgnoreCase("querypolicybytree"))
		{
			String corp = request.getParameter("corp");
			String corp_branch = request.getParameter("corp_branch");
			String disaster_type = request.getParameter("disaster_type");
			String year_uw = request.getParameter("year_uw");
			String startRow = request.getParameter("startRow");
			String count = request.getParameter("count");
			
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl();
			JSONBean jsonBean = exposureDaoImpl.queryPolicyByTree(corp, corp_branch, disaster_type, year_uw, startRow, count);
			jsonValueString = jsonBean.toJSONString();
		}
		//查询标的-模糊查询
		else if(action.toLowerCase().equalsIgnoreCase("querypolicybykeys"))
		{
			String key = request.getParameter("key");
			String startRow = request.getParameter("startRow");
			String count = request.getParameter("count");
			
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl();
			JSONBean jsonBean = exposureDaoImpl.queryPolicyByKeys(key==null?"":key, startRow, count);
			jsonValueString = jsonBean.toJSONString();
		}
		//查询标的损失信息
		else if(action.toLowerCase().equalsIgnoreCase("querypolicyloss"))
		{
			String policy_id = request.getParameter("policy_id");
			
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl();
			JSONBean jsonBean = exposureDaoImpl.queryPolicyLossByID(policy_id);
			jsonValueString = jsonBean.toJSONString();
		}
		//Test
		else if(action.toLowerCase().equalsIgnoreCase("getpolicyaggr_test"))
		{
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl();
			JSONBean jsonBean = exposureDaoImpl.getPolicyAggr_Test();
			jsonValueString = jsonBean.toJSONString();
		}
		//Test
		else if(action.toLowerCase().equalsIgnoreCase("uploadpolicy"))
		{
			String policy_type = request.getParameter("policy_type");
			String topic = request.getParameter("topic");
			UploadDaoImpl uploadDaoImpl = new UploadDaoImpl("");
			JSONBean jsonBean = null;
			try {
				jsonBean = uploadDaoImpl.upLoadExposureData(request, policy_type, topic);
			} catch (BiffException | FileUploadException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			jsonValueString = jsonBean.toJSONString();
		}
		//Test
		else if(action.toLowerCase().equalsIgnoreCase("upscaling"))
		{
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl();
			JSONBean jsonBean = exposureDaoImpl.upScaling("county");
			jsonValueString = jsonBean.toJSONString();
		}
		//Test
		else if(action.toLowerCase().equalsIgnoreCase("downscaling"))
		{
//			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl();
//			JSONBean jsonBean = exposureDaoImpl.getPolicyAggr_Test();
//			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("querycatalog"))
		{
			String policy_type = request.getParameter("policy_type");
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl();
			JSONBean jsonBean = exposureDaoImpl.getPolicyCatalog(policy_type);
			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("getrecord"))
		{
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl(true);
			JSONBean jsonBean = exposureDaoImpl.getRecord();
			jsonValueString = jsonBean.toJSONString();
			
		}
		else if(action.toLowerCase().equalsIgnoreCase("querypolicy_aggr"))
		{
			String cat_id = request.getParameter("cat_id");
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl();
			JSONBean jsonBean = exposureDaoImpl.getPolicy_Aggr(cat_id);
			jsonValueString = jsonBean.toJSONString();
		}
		else if (action.toLowerCase().equalsIgnoreCase("editpolicy_save")) {
			String cat_id = request.getParameter("cat_id");
			String policy_id_value = request.getParameter("policy_id_value");
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl();
			JSONBean jsonBean = null;
			try {
				jsonBean = exposureDaoImpl.savePolicy(cat_id, policy_id_value);
			} catch (JSONException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			jsonValueString = jsonBean.toJSONString();
		}
		else if (action.toLowerCase().equalsIgnoreCase("editpolicy_saveas")) {
			
		}
		else if (action.toLowerCase().equalsIgnoreCase("query_condition")) {//此方法弃用
			String column = request.getParameter("column");
			String where = request.getParameter("where");
			DomainDaoImpl domainDaoImpl = new DomainDaoImpl();
			JSONBean jsonBean = null;
			jsonBean = domainDaoImpl.getQueryCondition(column, where, "1");
			jsonValueString = jsonBean.toJSONString();
		}
		//查询条件
		else if(action.toLowerCase().equalsIgnoreCase("getquerycondition_e"))
		{
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl(true);
			JSONBean jsonBean = exposureDaoImpl.getQueryCondition_e();
			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("getqueryconditionitemcolumns_e"))
		{
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl(true);
			JSONBean jsonBean = exposureDaoImpl.getQueryConditionItemColumn_e();
			jsonValueString = jsonBean.toJSONString();
		}
		else if (action.toLowerCase().equalsIgnoreCase("getqueryconditionitem_e")) {
			String retrunField = request.getParameter("retrunField");
			String whereField = request.getParameter("whereField");
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl(true);
			JSONBean jsonBean = exposureDaoImpl.getQueryConditionItem_e(retrunField, whereField);
			jsonValueString = jsonBean.toJSONString();
		}
		else if (action.toLowerCase().equalsIgnoreCase("setquerycondition_e")) {
			String values = request.getParameter("values");
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl(true);
			JSONBean jsonBean = exposureDaoImpl.writeQueryCondition(values);
			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("querypolicy_e"))
		{
			String client = request.getParameter("client");
			String fields = request.getParameter("fields");
			String from = request.getParameter("from");
			String where = request.getParameter("where");
			String other = request.getParameter("other");
			String sql_inherit = request.getParameter("sql_inherit");
			String tag = request.getParameter("tag");
			boolean isclient = client==null || client.equalsIgnoreCase("true");
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl(isclient);
			JSONBean jsonBean = null;
			if(sql_inherit == null || sql_inherit.isEmpty()){
				jsonBean = exposureDaoImpl.querySQLSelectBean(fields,from,where,other);
			}
			else{
				jsonBean = exposureDaoImpl.querySQLSelectBean(sql_inherit);
			}
			Map<String, Object> result = (Map<String, Object>) jsonBean.getResult();
			if(result == null){
				 result = new HashMap<String, Object>();
				 result.put("tag", tag);
				 jsonBean.setResult(result);
			}
			else {
				result.put("tag", tag);
			}
			jsonValueString = jsonBean.toJSONString();
		}
		
		else if(action.toLowerCase().equalsIgnoreCase("uploadattachment"))
		{
			String refer_tables = request.getParameter("refer_tables");
			String where = request.getParameter("where");
			DomainDaoImpl domainDaoImpl = new DomainDaoImpl();
			JSONBean jsonBean = null;
			try {
				jsonBean = domainDaoImpl.uploadAttachment_client(request, refer_tables, where);
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
			domainDaoImpl.downloadAttachment_client(response, attachment_id);
		}
		else if(action.toLowerCase().equalsIgnoreCase("uploaddata_e"))
		{
			String act = request.getParameter("act");
			UploadDaoImpl uploadDaoImpl = new UploadDaoImpl(FileType.EXPO_Per);
			JSONBean jsonBean = null;
			try {
				if (act.equalsIgnoreCase("view")) {
					jsonBean = uploadDaoImpl.upLoadCSVView(request);
				}
				else if (act.equalsIgnoreCase("data")) {
					jsonBean = uploadDaoImpl.upLoadCSV(request);
				}
				
				jsonBean.setStatus(Status.SUCCESS);
			} catch (BiffException | FileUploadException | SQLException | ParseException | InsertException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				jsonBean = new JSONBean();
				jsonBean.setMsg(e.getMessage());
				jsonBean.setStatus(Status.FAILED);
			} 
			
			if(null != jsonBean)
				jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("downloaddata_e"))
		{
			String fields = request.getParameter("fields");
			String from = request.getParameter("from");
			String where = request.getParameter("where");
			String sql = "select "+fields+" from "+from+" where "+where;
			DownloadDaoImpl downloadDao = new DownloadDaoImpl();
			try {
				downloadDao.downloadPolicyData(response, sql, "Exposure.csv");
			} catch ( SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(action.toLowerCase().equalsIgnoreCase("getscalingindex_e"))
		{
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl();
			JSONBean jsonBean = exposureDaoImpl.getScalingIndex();
			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("downscaling_e"))
		{
			String needback = request.getParameter("needback");
			String datasql = request.getParameter("datasql");
			String datafield = request.getParameter("datafield");
			String ratetable = request.getParameter("ratetable");
			String ratefield = request.getParameter("ratefield");
			String suffix = request.getParameter("suffix");
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl(true);
			JSONBean jsonBean = new JSONBean();
			if ("1".equalsIgnoreCase(needback)) {
				try {
					String sql = exposureDaoImpl.downScalingData(
							datasql, 
							datafield, 
							ratetable,
							ratefield, 
							suffix);
					System.out.println(sql);
					jsonBean.setResult(sql);
					jsonBean.setStatus(Status.SUCCESS);
				} catch (SQLException | InsertException | ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					exposureDaoImpl.close();
					jsonBean.setMsg(e.getMessage());
					jsonBean.setStatus(Status.FAILED);
				}	
			}
			else {
				try {
					exposureDaoImpl.downScalingData(
							datasql, 
							datafield, 
							ratetable,
							ratefield, 
							suffix);
					jsonBean.setStatus(Status.SUCCESS);
				} catch (SQLException | InsertException | ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					exposureDaoImpl.close();
					jsonBean.setMsg(e.getMessage());
					jsonBean.setStatus(Status.FAILED);
				}
			}
			if(null != jsonBean)
				jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("statisticalanalysis"))
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
//			String h_sql = request.getParameter("h_sql");
//			String h_spatial_f = request.getParameter("h_spatial_f");
//			String e_sql = request.getParameter("e_sql");
//			String e_spatial_f = request.getParameter("e_spatial_f");
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl(true);
			JSONBean jsonBean = null;
			try {
				jsonBean = exposureDaoImpl.statisticalAnalysis(h_sql, h_spatial_f, e_sql, e_spatial_f,other);
			} catch (InsertException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(null != jsonBean)
				jsonValueString = jsonBean.toJSONString();
		}
//		else if(action.toLowerCase().equalsIgnoreCase("statisticalcomputer"))
//		{
//			String tag = request.getParameter("tag");
//			String other = request.getParameter("other");
//			JSONObject argsObj = null;
//			try {
//				argsObj = new JSONObject(tag);
//			} catch (JSONException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			String h_sql = "";
//			try {
//				h_sql = argsObj.getString("h_sql");
//			} catch (JSONException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			String h_spatial_f = "";
//			try {
//				h_spatial_f = argsObj.getString("h_spatial_f");
//			} catch (JSONException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			String e_sql = "";
//			try {
//				e_sql = argsObj.getString("e_sql");
//			} catch (JSONException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			String e_spatial_f = "";
//			try {
//				e_spatial_f = argsObj.getString("e_spatial_f");
//			} catch (JSONException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			String h_f = "";
//			try {
//				h_f = argsObj.getString("h_f");
//			} catch (JSONException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			String e_f = "";
//			try {
//				e_f = argsObj.getString("e_f");
//			} catch (JSONException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			String method = "";
//			try {
//				method = argsObj.getString("method");
//			} catch (JSONException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			String merge = null;
//			try {
//				merge = argsObj.getString("merge");
//			} catch (JSONException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			if(merge == null){
//				merge = "true";
//			}
//			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl(true);
//			JSONBean jsonBean = null;
//			try {
//				jsonBean = exposureDaoImpl.statisticalComputer(h_sql, h_spatial_f, e_sql, e_spatial_f, h_f, e_f, method, merge, other);
//			} catch (SQLException | InsertException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			if(null != jsonBean)
//				jsonValueString = jsonBean.toJSONString();
//		}
		
		out.write(jsonValueString);
		out.flush();
		out.close();
	}
}
