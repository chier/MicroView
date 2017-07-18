package her.http;

import her.bean.JSONBean;
import her.bean.JSONBean.Status;
import her.dao.JavaPythonDao;
import her.dao.impl.AutoActionDaoImpl;
import her.dao.impl.DomainDaoImpl;
import her.dao.impl.DownloadDaoImpl;
import her.dao.impl.ExposureDaoImpl;
import her.dao.impl.HazardDaoImpl;
import her.dao.impl.RiskDaoImpl;
import her.dao.impl.UploadDaoImpl;
import her.dao.impl.UploadDaoImpl.FileType;
import her.model.InsertException;
import her.utils.GlobalVariable;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jxl.read.biff.BiffException;

import org.apache.commons.fileupload.FileUploadException;
import org.json.JSONException;

public class RiskServlet extends HttpServlet {
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
		PrintWriter out = response.getWriter();
		
		//为图片存储临时文件配置地址
		GlobalVariable.filePath = this.getServletConfig().getServletContext().getRealPath("tempfile/");
		
		String action = request.getParameter("action");
		String jsonValueString = "";
		if(action.toLowerCase().equalsIgnoreCase("checkfunction"))
		{
//			String args = request.getParameter("args");
//			String argNum = request.getParameter("argNum");
//			RiskDaoImpl riskDaoImpl = new RiskDaoImpl();
//			JSONBean jsonBean = riskDaoImpl.checkFunction(args, argNum);
//			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("getfunctionlist"))
		{
			RiskDaoImpl riskDaoImpl = new RiskDaoImpl();
			JSONBean jsonBean = riskDaoImpl.getFunctionList();
			jsonValueString = jsonBean.toJSONString();
			System.out.println(jsonValueString);
		}
		else if(action.toLowerCase().equalsIgnoreCase("getfunctionlistchar"))
		{
//			RiskDaoImpl riskDaoImpl = new RiskDaoImpl();
//			JSONBean jsonBean = riskDaoImpl.getFunctionListWithCharacteristic();
//			jsonValueString = jsonBean.toJSONString();
//			System.out.println(jsonValueString);
		}
		else if(action.toLowerCase().equalsIgnoreCase("getfunctioncharacteristic"))
		{
			RiskDaoImpl riskDaoImpl = new RiskDaoImpl();
			JSONBean jsonBean = riskDaoImpl.getFunctionCharacteristic();
			jsonValueString = jsonBean.toJSONString();
			System.out.println(jsonValueString);
		}
		else if(action.toLowerCase().equalsIgnoreCase("getfunctioncharacteristicheader"))
		{
			RiskDaoImpl riskDaoImpl = new RiskDaoImpl();
			JSONBean jsonBean = riskDaoImpl.getFunctionCharacteristicHeader();
			jsonValueString = jsonBean.toJSONString();
			System.out.println(jsonValueString);
		}
		else if(action.toLowerCase().equalsIgnoreCase("queryfunctioncharacteristic"))
		{
//			String fields = request.getParameter("fields");
//			RiskDaoImpl riskDaoImpl = new RiskDaoImpl();
//			JSONBean jsonBean = riskDaoImpl.queryFunctionCharacteristic(fields);
//			jsonValueString = jsonBean.toJSONString();
//			System.out.println(jsonValueString);
		}
		else if(action.toLowerCase().equalsIgnoreCase("calculatepolicy"))
		{
			String cal_data = request.getParameter("data_policy");
			String cal_field = request.getParameter("data_field");

			RiskDaoImpl riskDaoImpl = new RiskDaoImpl();
			JSONBean eventJsonBean = riskDaoImpl.getEventList();
			@SuppressWarnings("unchecked")
			JSONBean jsonBean = riskDaoImpl.calculatePolicy(
					cal_data,
					cal_field,
					(List<Map<String, String>>)((Map<String, Object>) eventJsonBean.getResult()).get("data")
					);
			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("autoaction"))
		{
			Date s_now = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//日期格式 
			System.out.println("开始："+dateFormat.format(s_now));
			AutoActionDaoImpl aaDaoImpl = new AutoActionDaoImpl();
			try {
				aaDaoImpl.autoWritePolicyPerilIndex();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Date e_now = new Date();
			System.out.println("结束："+dateFormat.format(e_now));
			System.out.println("用时(秒)："+(e_now.getTime()-s_now.getTime())/1000);
		}
		else if(action.toLowerCase().equalsIgnoreCase("calculatepolicy_exp"))
		{
			String cal_data = request.getParameter("data_policy");
			String cal_field = request.getParameter("data_field");
			RiskDaoImpl riskDaoImpl = new RiskDaoImpl();
			JSONBean jsonBean = new JSONBean();
			try {
				jsonBean = riskDaoImpl.calculatePolicyByExp(cal_data, cal_field);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			jsonValueString = jsonBean.toJSONString();
		}
		//查询条件
		else if(action.toLowerCase().equalsIgnoreCase("getquerycondition_rl"))
		{
			RiskDaoImpl riskDaoImpl = new RiskDaoImpl(true);
			JSONBean jsonBean = riskDaoImpl.getQueryCondition_rl();
			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("getquerycondition_rh"))
		{
			RiskDaoImpl riskDaoImpl = new RiskDaoImpl(true);
			JSONBean jsonBean = riskDaoImpl.getQueryCondition_rh();
			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("getquerycondition_rm"))
		{
			RiskDaoImpl riskDaoImpl = new RiskDaoImpl(true);
			JSONBean jsonBean = riskDaoImpl.getQueryCondition_rm();
			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("getquerycondition_rall"))
		{
			//使用E部分的查询条件
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl(true);
			JSONBean jsonBean = exposureDaoImpl.getQueryCondition_e();
			jsonValueString = jsonBean.toJSONString();
		}
		else if (action.toLowerCase().equalsIgnoreCase("getqueryconditionitem_rl")) {
			String retrunField = request.getParameter("retrunField");
			String whereField = request.getParameter("whereField");
			RiskDaoImpl riskDaoImpl = new RiskDaoImpl(true);
			JSONBean jsonBean = riskDaoImpl.getQueryConditionItem_rl(retrunField, whereField);
			jsonValueString = jsonBean.toJSONString();
		}
		else if (action.toLowerCase().equalsIgnoreCase("getqueryconditionitem_rh")) {
			String retrunField = request.getParameter("retrunField");
			String whereField = request.getParameter("whereField");
			RiskDaoImpl riskDaoImpl = new RiskDaoImpl(true);
			JSONBean jsonBean = riskDaoImpl.getQueryConditionItem_rh(retrunField, whereField);
			jsonValueString = jsonBean.toJSONString();
		}
		else if (action.toLowerCase().equalsIgnoreCase("getqueryconditionitem_rm")) {
			String retrunField = request.getParameter("retrunField");
			String whereField = request.getParameter("whereField");
			RiskDaoImpl riskDaoImpl = new RiskDaoImpl(true);
			JSONBean jsonBean = riskDaoImpl.getQueryConditionItem_rm(retrunField, whereField);
			jsonValueString = jsonBean.toJSONString();
		}
		else if (action.toLowerCase().equalsIgnoreCase("getqueryconditionitem_rall")) {
			//用E部分查询条件
			String retrunField = request.getParameter("retrunField");
			String whereField = request.getParameter("whereField");
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl(true);
			JSONBean jsonBean = exposureDaoImpl.getQueryConditionItem_e(retrunField, whereField);
			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("getqueryconditionitemcolumns_rl"))
		{
			RiskDaoImpl riskDaoImpl = new RiskDaoImpl(true);
			JSONBean jsonBean = riskDaoImpl.getQueryConditionItemColumn_rl();
			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("getqueryconditionitemcolumns_rh"))
		{
			RiskDaoImpl riskDaoImpl = new RiskDaoImpl(true);
			JSONBean jsonBean = riskDaoImpl.getQueryConditionItemColumn_rh();
			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("getqueryconditionitemcolumns_rm"))
		{
			RiskDaoImpl riskDaoImpl = new RiskDaoImpl(true);
			JSONBean jsonBean = riskDaoImpl.getQueryConditionItemColumn_rm();
			jsonValueString = jsonBean.toJSONString();
		}
		else if (action.toLowerCase().equalsIgnoreCase("setquerycondition_rl")) {
			String values = request.getParameter("values");
			RiskDaoImpl riskDaoImpl = new RiskDaoImpl(true);
			JSONBean jsonBean = riskDaoImpl.writeQueryCondition_rl(values);
			jsonValueString = jsonBean.toJSONString();
		}
		else if (action.toLowerCase().equalsIgnoreCase("setquerycondition_rh")) {
			String values = request.getParameter("values");
			RiskDaoImpl riskDaoImpl = new RiskDaoImpl(true);
			JSONBean jsonBean = riskDaoImpl.writeQueryCondition_rh(values);
			jsonValueString = jsonBean.toJSONString();
		}
		else if (action.toLowerCase().equalsIgnoreCase("setquerycondition_rm")) {
			String values = request.getParameter("values");
			RiskDaoImpl riskDaoImpl = new RiskDaoImpl(true);
			JSONBean jsonBean = riskDaoImpl.writeQueryCondition_rm(values);
			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("querysql_r"))
		{
			String fields = request.getParameter("fields");
			String from = request.getParameter("from");
			String where = request.getParameter("where");
			String other = request.getParameter("other");
			RiskDaoImpl riskDaoImpl = new RiskDaoImpl(true);
			JSONBean jsonBean = riskDaoImpl.querySQLSelectBean(fields,from,where,other);
			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("querysql_r_lh"))
		{
			String fields = request.getParameter("fields");
			String from = request.getParameter("from");
			String where = request.getParameter("where");
			String other = request.getParameter("other");
			String tag = request.getParameter("tag"); 
			System.out.println(from);
			RiskDaoImpl riskDaoImpl = new RiskDaoImpl(true);
			JSONBean jsonBean = null;
			try {
				jsonBean = riskDaoImpl.querySQLSelectBean_lh(fields,from,where,other);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(null == jsonBean){
				jsonBean = new JSONBean();
				Map<String, Object> result = new HashMap<String, Object>();
				result.put("tag", tag);
				jsonBean.setResult(result);
				jsonBean.setStatus(Status.FAILED);
			}
			else {
				Map<String, Object> result = (Map<String, Object>) jsonBean.getResult();
				if(result == null){
					 result = new HashMap<String, Object>();
					 if(tag != null)
						 result.put("tag", tag);
					 jsonBean.setResult(result);
				}
				else {
					 if(tag != null)
						 result.put("tag", tag);
				}
			}
				
			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("get_rv_func"))
		{
			String fields = " ROW_NUMBER () OVER (), description, hazard_types, expo_types, hazard_ids, expo_type_ids, arg_num, metadata,func_points,id ";
			String from = "risk_func_table";
			String where = request.getParameter("where");;
			String other = request.getParameter("other");
			RiskDaoImpl riskDaoImpl = new RiskDaoImpl();
			JSONBean jsonBean = riskDaoImpl.querySQLSelectBean(fields,from,where,other);
			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("querysql_rall"))
		{
			String fields = request.getParameter("fields");
			String from = request.getParameter("from");
			String where = request.getParameter("where");
			String other = request.getParameter("other");
			String tag = request.getParameter("tag"); 
			RiskDaoImpl riskDaoImpl = new RiskDaoImpl(true);
			JSONBean jsonBean = null;
			try {
				jsonBean = riskDaoImpl.querySQLSelectAllInOne(fields,from,where,other);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(null == jsonBean){
				jsonBean = new JSONBean();
				Map<String, Object> result = new HashMap<String, Object>();
				if(tag != null)
					result.put("tag", tag);
				jsonBean.setResult(result);
				jsonBean.setStatus(Status.FAILED);
			}
			else {
				if(tag != null){
					Map<String, Object> result = (Map<String, Object>) jsonBean.getResult();
					if(result != null)
						result.put("tag", tag);
				}
			}
				
			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("uploaddata_rl"))
		{
			String act = request.getParameter("act");
			UploadDaoImpl uploadDaoImpl = new UploadDaoImpl(FileType.Rl);
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
		else if(action.toLowerCase().equalsIgnoreCase("downloaddata_r"))
		{
			String fields = request.getParameter("fields");
			String from = request.getParameter("from");
			String where = request.getParameter("where");
			String sql = "select "+fields+" from "+from+" where "+where;
			DownloadDaoImpl downloadDao = new DownloadDaoImpl();
			try {
				downloadDao.downloadPolicyData(response, sql, "RiskLoss.csv");
			} catch ( SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(action.toLowerCase().equalsIgnoreCase("extractvalue_rh"))
		{
			String e_sql = request.getParameter("e_sql");
			String h_sql = request.getParameter("h_sql");
//			if(e_sql.toLocaleLowerCase().indexOf("update ") != -1 ||
//					e_sql.toLocaleLowerCase().indexOf("insert ") != -1 ||
//					e_sql.toLocaleLowerCase().indexOf("delete ") != -1 ||
//					h_sql.toLocaleLowerCase().indexOf("update ") != -1 ||
//					h_sql.toLocaleLowerCase().indexOf("insert ") != -1 ||
//					h_sql.toLocaleLowerCase().indexOf("delete ") != -1)
//				{
//					Alert.show("非法SQL语句！");
//					return;
//				}
			String spatial_field = request.getParameter("spatial_field");
			HazardDaoImpl hazardDaoImpl = new HazardDaoImpl();
			List<Map<String, String>> columnInfos = hazardDaoImpl.getSQLStructure(h_sql);
			RiskDaoImpl riskDaoImpl = new RiskDaoImpl(true);
			JSONBean jsonBean = new JSONBean();
			try {
				if(riskDaoImpl.extractValue_Rh(e_sql, h_sql, spatial_field, columnInfos)){
					jsonBean.setStatus(Status.SUCCESS);
				}
			} catch (SQLException | InsertException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				jsonBean.setMsg(e.getMessage());
				jsonBean.setStatus(Status.FAILED);
			}
			if(null != jsonBean)
				jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("caculatelossrate_rm"))
		{
			String e_sql = request.getParameter("e_sql");
			String propertys = request.getParameter("propertys");
			RiskDaoImpl riskDaoImpl = new RiskDaoImpl(true);
			JSONBean jsonBean = new JSONBean();
			try {
				if(riskDaoImpl.caculateLossrateFromFunc_Rm2(e_sql, propertys)){
					jsonBean.setStatus(Status.SUCCESS);
				}
			} catch (SQLException | InsertException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				jsonBean.setMsg(e.getMessage());
				jsonBean.setStatus(Status.FAILED);
			}
			if(null != jsonBean)
				jsonValueString = jsonBean.toJSONString();
		}
		
		out.write(jsonValueString);
		out.flush();
		out.close();
	}
}
