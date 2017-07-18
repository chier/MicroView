package her.dao.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jxl.read.biff.BiffException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.python.antlr.PythonParser.varargslist_return;
import org.python.antlr.ast.alias.asname_descriptor;

import her.bean.JSONBean;
import her.bean.JSONBean.Status;
import her.dao.JsonBeanDao;
import her.utils.DBUtils;
import her.utils.GlobalFuns;
import her.utils.WebConfigUtils;

public class DomainDaoImpl {
	private JsonBeanDao jsDao;
	// 限制文件的上传大小
	private int maxPostSize = 100 * 1024 * 1024;

	public DomainDaoImpl()
	{
		jsDao = new JsonBeanDao();
	}

	/**
	 * @param client 是否使用 client库
	 */
	public DomainDaoImpl(Boolean client)
	{
		if(client)
		{
			jsDao = new JsonBeanDao(DBUtils.getClientDBConnection());
		}
		else {
			jsDao = new JsonBeanDao();
		}
	}

	public JSONBean getColumnList(String tablename){
		String sqlString = "";
		if (tablename==null || tablename.isEmpty()) {
			sqlString = "select dt.* from dic_columns dt";
		}
		else {
			sqlString = "select dt.* from dic_columns dt where tablename like '%_"+tablename+"_%'";
		}
		JSONBean jsonBean = jsDao.queryTable(sqlString);
		return jsonBean;
	}

	public JSONBean getQueryCondition(String col, String where, String id){
		String sqlString = "";
		if (col==null || col.isEmpty()) {
			sqlString = "select con_field,con_alias from her_query_condition_catalog where id="+id;
		}
		else {
			sqlString = "select con_table from her_query_condition_catalog where id="+id;
			JSONBean table = jsDao.queryTable(sqlString);
			@SuppressWarnings("unchecked")
			List<Map<String, String>> t_data = (List<Map<String, String>>)((Map<String, Object>) table.getResult()).get("data");
			//			System.out.println(t_data.get(0).get("con_table"));
			sqlString = "select distinct "+col+" from "+t_data.get(0).get("con_table");
			if(where != null && !where.isEmpty())
			{
				sqlString += " where " + where;
			}

		}
		JSONBean jsonBean = jsDao.queryTable(sqlString);
		return jsonBean;
	}

	public JSONBean getQueryCondition_h(){
		String sqlString = "select * from her_query_condition_h order by rank";
		JSONBean jsonBean = jsDao.queryTable(sqlString);
		return jsonBean;
	}

	//her_query_condition_item_h
	public JSONBean getQueryConditionItem_h(String retrunField, String whereField){
		String sqlString = String.format("select distinct %s from her_query_condition_item_h where 1=1 %s order by %s", retrunField, whereField, retrunField);
		JSONBean jsonBean = jsDao.queryTable(sqlString);
		return jsonBean;
	}

	public JSONBean getRegion_xzqh(String code){
		String sqlString = "select * from geo_id_xzqh_name_ssx where xzqh_id like '"+code+"'";
		JSONBean jsonBean = jsDao.queryTable(sqlString);
		return jsonBean;
	}

	public JSONBean querySQLSelectBean(String fields, String from, String where, String other) {
		JSONBean jsonBean = null;
		String excludeFields = "rast";//多个字段用,隔开
		jsonBean = jsDao.queryTableWithColumnInfo2(fields, from, where, other, excludeFields);

		return jsonBean;
	}

	public JSONBean getSQLStructure(String sql) {
		JSONBean jsonBean = null;
		jsonBean = jsDao.getTableStructure(sql);

		return jsonBean;
	}
	
	public JSONBean getFieldsInfo(String tablename){
		String sqlString = "";
		sqlString = "select dt.* from dic_fields_info dt where tablename in ("+tablename+") order by rank";
		JSONBean jsonBean = jsDao.queryTable(sqlString);
		return jsonBean;
	}

	public JSONBean uploadAttachment(HttpServletRequest request, String refer_tables, String where) throws FileUploadException
	{
		JSONBean jsonBean = new JSONBean();
		//保存文件到服务器中
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(4096);
		ServletFileUpload upload = new ServletFileUpload(factory);
		//中文文件名处理的代码,setheaderencoding就可以了
		upload.setHeaderEncoding("utf-8");
		upload.setSizeMax(maxPostSize);
		String filename = "";

		List<FileItem> fileItems = upload.parseRequest(request);
		Iterator<FileItem> iter = fileItems.iterator();
		while (iter.hasNext()) {
			FileItem item = (FileItem) iter.next();
			if (!item.isFormField()) {
				String value = item.getName();
				int start = value.lastIndexOf("\\");
				filename = value.substring(start + 1);

				//获取二进制数据流
				//				inStream = item.getInputStream();
				if(item.getSize() == 0)
				{
					jsonBean.setResult(filename);
					jsonBean.setStatus(Status.FAILED);

					return jsonBean;
				}

				try {
					String filePath = WebConfigUtils.getValue("attachmentpath");
					String savename = GlobalFuns.getUUID();
					String filetype = filename.substring(filename.lastIndexOf("."));
					savename += filetype;
					//数据库中记录
					JsonBeanDao dao = new JsonBeanDao();
					int attachment_id = dao.insertActionHasID(String.format("insert into her_attachment(name, filename, path, type) values('%s', '%s', '%s', '%s')", savename,filename,filePath,filetype), false);
					int newrow = dao.updateAction(String.format("update %s set attachment_id=%s where %s",refer_tables,attachment_id,where), true);
					if(newrow != -1)
					{
						//上产服务器文件
						File uploadFile = new File(filePath.concat("/").concat(savename)); 
						item.write(uploadFile); //写入本地文件

						jsonBean.setResult(newrow+","+savename+","+filename);
						jsonBean.setStatus(Status.SUCCESS);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return jsonBean;
	}

	public void downloadAttachment( HttpServletResponse response, String attachment_id)
	{
		try
		{   
			JsonBeanDao dao = new JsonBeanDao();
			JSONBean jb = dao.queryTable("select * from her_attachment where id="+attachment_id);
			@SuppressWarnings("unchecked")
			List<Map<String, String>> t_data = (List<Map<String, String>>)((Map<String, Object>) jb.getResult()).get("data");
			String downFilename = t_data.get(0).get("name");
			String filename = t_data.get(0).get("filename");
			String filePath = t_data.get(0).get("path");
			//			response.setContentType("text/plain");
			//			response.setHeader("Location", filename);
			//			response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
			//			OutputStream outputStream = response.getOutputStream();
			//			InputStream inputStream = new FileInputStream(filePath+downFilename);
			//			byte[] buffer = new byte[1024];
			//			int i = -1;
			//			while ((i = inputStream.read(buffer)) != -1) {
			//				outputStream.write(buffer, 0, i);
			//			}
			//			outputStream.flush();
			//			outputStream.close();
			//			inputStream.close();
			/*读取文件*/
			File file = new File(filePath+"\\"+downFilename);
			/*如果文件存在*/
			if (file.exists()) {
				filename = URLEncoder.encode(filename, "utf-8");
				response.reset();
				response.setContentType("application/x-msdownload");
				response.addHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
				int fileLength = (int) file.length();
				response.setContentLength(fileLength);
				/*如果文件长度大于0*/
				if (fileLength != 0) {
					/*创建输入流*/
					InputStream inStream = new FileInputStream(file);
					byte[] buf = new byte[4096];
					/*创建输出流*/
					ServletOutputStream servletOS = response.getOutputStream();
					int readLength;
					while (((readLength = inStream.read(buf)) != -1)) {
						servletOS.write(buf, 0, readLength);
					}
					inStream.close();
					servletOS .flush();
					servletOS .close();
				}
			}
		}
		catch(FileNotFoundException e1)
		{
			System.out.println("没有找到您要的文件");
		}
		catch(Exception e)
		{
			System.out.println("系统错误，请及时与管理员联系");
		}
	}

	public JSONBean uploadAttachment_client(HttpServletRequest request, String refer_tables, String where) throws FileUploadException
	{
		JSONBean jsonBean = new JSONBean();
		//保存文件到服务器中
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(4096);
		ServletFileUpload upload = new ServletFileUpload(factory);
		//中文文件名处理的代码,setheaderencoding就可以了
		upload.setHeaderEncoding("utf-8");
		upload.setSizeMax(maxPostSize);
		String filename = "";

		List<FileItem> fileItems = upload.parseRequest(request);
		Iterator<FileItem> iter = fileItems.iterator();
		while (iter.hasNext()) {
			FileItem item = (FileItem) iter.next();
			if (!item.isFormField()) {
				String value = item.getName();
				int start = value.lastIndexOf("\\");
				filename = value.substring(start + 1);

				//获取二进制数据流
				//				inStream = item.getInputStream();
				if(item.getSize() == 0)
				{
					jsonBean.setResult(filename);
					jsonBean.setStatus(Status.FAILED);

					return jsonBean;
				}

				try {
					String filePath = WebConfigUtils.getValue("attachmentpath");
					String savename = GlobalFuns.getUUID();
					String filetype = filename.substring(filename.lastIndexOf("."));
					savename += filetype;
					//数据库中记录
					JsonBeanDao dao = new JsonBeanDao(DBUtils.getClientDBConnection());
					int attachment_id = dao.insertActionHasID(String.format("insert into client_attachment(name, filename, path, type) values('%s', '%s', '%s', '%s')", savename,filename,filePath,filetype), false);
					int newrow = dao.updateAction(String.format("update %s set attachment_id=%s where %s",refer_tables,attachment_id,where), true);
					if(newrow != -1)
					{
						//上产服务器文件
						File uploadFile = new File(filePath.concat("/").concat(savename)); 
						item.write(uploadFile); //写入本地文件

						jsonBean.setResult(newrow+","+savename+","+filename);
						jsonBean.setStatus(Status.SUCCESS);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return jsonBean;
	}

	public void downloadAttachment_client( HttpServletResponse response, String attachment_id)
	{
		try
		{   
			JsonBeanDao dao = new JsonBeanDao(DBUtils.getClientDBConnection());
			JSONBean jb = dao.queryTable("select * from client_attachment where id="+attachment_id);
			@SuppressWarnings("unchecked")
			List<Map<String, String>> t_data = (List<Map<String, String>>)((Map<String, Object>) jb.getResult()).get("data");
			String downFilename = t_data.get(0).get("name");
			String filename = t_data.get(0).get("filename");
			String filePath = t_data.get(0).get("path");
			//			response.setContentType("text/plain");
			//			response.setHeader("Location", filename);
			//			response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
			//			OutputStream outputStream = response.getOutputStream();
			//			InputStream inputStream = new FileInputStream(filePath+downFilename);
			//			byte[] buffer = new byte[1024];
			//			int i = -1;
			//			while ((i = inputStream.read(buffer)) != -1) {
			//				outputStream.write(buffer, 0, i);
			//			}
			//			outputStream.flush();
			//			outputStream.close();
			//			inputStream.close();
			/*读取文件*/
			File file = new File(filePath+"\\"+downFilename);
			/*如果文件存在*/
			if (file.exists()) {
				filename = URLEncoder.encode(filename, "utf-8");
				response.reset();
				response.setContentType("application/x-msdownload");
				response.addHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
				int fileLength = (int) file.length();
				response.setContentLength(fileLength);
				/*如果文件长度大于0*/
				if (fileLength != 0) {
					/*创建输入流*/
					InputStream inStream = new FileInputStream(file);
					byte[] buf = new byte[4096];
					/*创建输出流*/
					ServletOutputStream servletOS = response.getOutputStream();
					int readLength;
					while (((readLength = inStream.read(buf)) != -1)) {
						servletOS.write(buf, 0, readLength);
					}
					inStream.close();
					servletOS .flush();
					servletOS .close();
				}
			}
		}
		catch(FileNotFoundException e1)
		{
			System.out.println("没有找到您要的文件");
		}
		catch(Exception e)
		{
			System.out.println("系统错误，请及时与管理员联系");
		}
	}

	public void deleteAttachment()
	{
		
	}
	
	public JSONBean queryAttachment()
	{
		JSONBean jsonBean = new JSONBean();
		return jsonBean;
	}

	/**
	 * @param type
	 * @param transdata
	 * @return
	 */
	public JSONBean translation_disaster(String type, String transdata) {
		String inString = "";
		String sqlString = "";
		if (type.equalsIgnoreCase("table")) {
			inString = "'" + transdata.replaceAll(",", "','") + "'";
			sqlString = "select titlename,tablename from dic_tables_info where tablename in ("+inString+")";
		}
		else if (type.equalsIgnoreCase("field")) {
			inString = "'" + transdata.replaceAll(",", "','") + "'";
			sqlString = "select fieldname,titlename,is_visible,unit from dic_fields_info where tablename in ("+inString+")";
		}

		JSONBean jsonBean = jsDao.queryTable(sqlString);
		return jsonBean;
	}

	public JSONBean translation_client(String tabletype, String transdata) {
		String inString = "";
		String sqlString = "";
		if (tabletype.equalsIgnoreCase("table")) {
			inString = "'" + transdata.replaceAll(",", "','") + "'";
			sqlString = "select titlename,tablename from dic_tables_info where tablename in ("+inString+")";
		}
		else if (tabletype.equalsIgnoreCase("field")) {
			inString = "'" + transdata.replaceAll(",", "','") + "'";
			//			if (datatype.equalsIgnoreCase("expo")) {
			//				sqlString = "select * from client_expo_record_type_setting where record_type_id in ("+inString+")";
			//			}
			//			else if (datatype.equalsIgnoreCase("risk")) {
			//				sqlString = "select * from client_risk_rl_record_type_setting where record_type_id in ("+inString+")";
			//			}
			inString = "'" + transdata.replaceAll(",", "','") + "'";
			sqlString = "select tablename,fieldname,titlename_en,titlename,is_visible,unit,\"desc\" from dic_fields_info where tablename in ("+inString+")";
		}

		JSONBean jsonBean = jsDao.queryTable(sqlString);
		return jsonBean;
	}

	public JSONBean translation_element(String db, String table, String field, String values)
	{
		JSONBean jsonBean = null;
		String sqlString = "";
		if(db.equalsIgnoreCase("client")){
			sqlString = "select * from client_dic_element where table_n="+table+",field_n="+field+",value_n in ("+values+")";
			jsonBean = jsDao.queryTable(sqlString);
		}
		else if (db.equalsIgnoreCase("disaster")) {
			sqlString = "select * from dic_element_info where table_n="+table+",field_n="+field+",value_n in ("+values+")";
			jsonBean = jsDao.queryTable(sqlString);
		}
		return jsonBean;
	}

	public JSONBean queryAttribute(String fields, String from, String where, String other)
	{
		//		String c_sql = "select "+fields+" from "+from+" where "+where+" limit 1";
		//		String n_fields = "";
		//		String core_ts = GlobalFuns.findCoreTable(from);
		//		try {
		//			List<Map<String, String>> columns = jsDao.getTableStructure_list(c_sql, false);
		//			int len = columns.size();
		//			for (int i = 0; i < len; i++) {
		//				if(columns.get(i).get("columnType").equalsIgnoreCase("raster")
		//				|| columns.get(i).get("columnType").equalsIgnoreCase("geometry")){
		//					continue;
		//				}
		//				n_fields += columns.get(i).get("columnName") + ",";
		//			}
		//			if(!n_fields.isEmpty())fields = n_fields.substring(0, n_fields.length()-1);
		//		} catch (SQLException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		//		String sql = "select "+fields+" from "+from+" where "+where+" "+other;

		JSONBean jsonBean = new JSONBean();
		jsonBean = jsDao.queryTableWithColumnInfo3(fields, from, where, other, "raster,geometry");
		return jsonBean;
	}
	public JSONBean getMaxMinDate(String datefield, String sql)
	{
		String sqlString = "SELECT * from user_timeinterval('"+sql.replaceAll("'", "''")+"','"+datefield+"')";
//		String sqlString = "SELECT max("+datefield+") as maxtime, min("+datefield+") as mintime from ("+sql+") t";
		JSONBean jsonBean = new JSONBean();
		jsonBean = jsDao.queryTable(sqlString);
		return jsonBean;
	}

	public JSONBean getDefaultSetting(String pageid)
	{
		String sqlString = "SELECT * from client_default_setting where pageid='"+pageid+"'";
		JSONBean jsonBean = new JSONBean();
		jsonBean = jsDao.queryTable(sqlString);
		return jsonBean;
	}

	public JSONBean upLoadTempData(HttpServletRequest request, String policy_type, String topic) throws IOException, FileUploadException, BiffException, SQLException
	{
		//保存文件到服务器中
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(4096);
		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setSizeMax(maxPostSize);
		InputStream inStream = null;
		String filename = "";

		List<FileItem> fileItems = upload.parseRequest(request);
		Iterator<FileItem> iter = fileItems.iterator();
		while (iter.hasNext()) {
			FileItem item = (FileItem) iter.next();
			if (!item.isFormField()) {
				filename = item.getName();
				System.out.println(filename);//记录文件名
				//获取二进制数据流
				inStream = item.getInputStream();
			}
		}
		WriteExcelToDBImpl excel2DB= new WriteExcelToDBImpl();
		JSONBean res = excel2DB.excel2DB(
				inStream,
				policy_type,
				filename,
				"anonymous",
				topic);

		return res;
	}

}
