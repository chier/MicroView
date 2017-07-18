package her.dao.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import jnr.ffi.Struct.int16_t;
import jxl.read.biff.BiffException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.naming.java.javaURLContextFactory;
import org.json.JSONException;
import org.python.antlr.PythonParser.else_clause_return;
import org.python.antlr.PythonParser.funcdef_return;
import org.python.antlr.PythonParser.return_stmt_return;
import org.python.antlr.PythonParser.varargslist_return;

import sun.tools.jar.resources.jar;
import her.bean.JSONBean;
import her.bean.JSONBean.Status;
import her.dao.JsonBeanDao;
import her.model.InsertException;
import her.utils.CsvFileParser;
import her.utils.CsvReader;
import her.utils.DBUtils;
import her.utils.GlobalFuns;

/**
 * @author ZB
 *
 */
public class UploadDaoImpl {
	// 限制文件的上传大小
	private int maxPostSize = 100 * 1024 * 1024;
	private String Record_type_setting_field = "record_type, record_type_id, record_type_desc, field_name, field_title, "
			+ "field_type, field_unit, field_desc, field, id";
	private String Record_type_setting_table = "client_expo_record_type_setting";
	private String Index_table = "client_expo_index_table";
	private String Data_table_prefix = "client_expo_policy_";
	private String fileType = "";

	/**
	 * @param fy FileType
	 */
	public UploadDaoImpl(String fy)
	{
		fileType = fy;
		switch (fy) {
		case FileType.EXPO_Per:
		case FileType.EXPO_Agg:
			Record_type_setting_field = "record_type, record_type_id, record_type_discription, field_name, field_title, "
					+ "field_type, field_unit, field_discription, field, id";
			Record_type_setting_table = "client_expo_record_type_setting";
			Index_table = "client_expo_index_table";
			Data_table_prefix = "client_expo_policy_";
			break;
		case FileType.Rl:
			Record_type_setting_field = "record_type, record_type_id, record_type_discription, field_name, field_title, "
					+ "field_type, field_unit, field_discription, field, id";
			Record_type_setting_table = "client_risk_rl_record_type_setting";
			Index_table = "client_risk_rl_index_table";
			Data_table_prefix = "client_risk_rl_data_";
			break;
		case FileType.Rm:
			Record_type_setting_field = "record_type, record_type_id, record_type_discription, field_name, field_title, "
					+ "field_type, field_unit, field_discription, field, id";
			Record_type_setting_table = "client_risk_rm_record_type_setting";
			Index_table = "client_risk_rm_index_table";
			Data_table_prefix = "client_risk_rm_data_";
			break;
		case FileType.Rv:
			Record_type_setting_field = "record_type, record_type_id, record_type_discription, field_name, field_title, "
					+ "field_type, field_unit, field_discription, field, id";
			Record_type_setting_table = "client_risk_rv_record_type_setting";
			Index_table = "client_risk_rv_index_table";
			Data_table_prefix = "client_risk_rv_data_";
			break;
		default:
			break;
		}
	}

	public JSONBean upLoadExposureData(HttpServletRequest request, String policy_type, String topic) throws IOException, FileUploadException, BiffException, SQLException
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
				//                    try {
				//                        item.write(new File(uploadPath + name));//写入本地文件
				//                       // SaveFile s = new SaveFile();
				//                       // s.saveFile(name);
				//                    } catch (Exception e) {
				//                        e.printStackTrace();
				//                    }
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

	public JSONBean upLoadCSVView(HttpServletRequest request) throws IOException, FileUploadException, BiffException, SQLException
	{
		JSONBean jsonBean = new JSONBean();
		int viewDataCount = 15;
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
				System.out.println("预览文件："+filename);//记录文件名
				//获取二进制数据流
				inStream = item.getInputStream();
			}
		}
		if (inStream != null) {
			// 实例解析器CSVReader
			CsvReader reader = new CsvReader(inStream, Charset.forName("utf-8"));
			reader.readHeaders();//读取第一行作为表头
			String recordtype =  reader.getHeaders()[0];
			System.out.println(recordtype);
			//			Connection con = DBUtils.getClientDBConnection();
			//			ResultSet rs = con.getMetaData().getTables(null, null, "client_expo_policy_"+recordtype, null);  
			//			if (rs.next())
			//yourTable exist 
			reader.readHeaders();//读取第二行作为表头
			if (!recordtype.equalsIgnoreCase("100")) {
				reader.readHeaders();//读取第三行作为表头
			}
			String[] headers = reader.getHeaders();
			List<Map<String, String>> data = new ArrayList<Map<String, String>>();// 数据
			while (reader.readRecord()) { //逐行读入除表头的数据
				if(viewDataCount<=0)
				{
					//视图默认只读50行数据，大于50行退出
					break;
				}
				int headerLength = headers.length;
				Map<String, String> map = new HashMap<String, String>();
				for (int i = 0; i < headerLength; i++) {
					map.put(headers[i], reader.get(i));//录入数据到map
				}
				data.add(map);//将map放入data
				viewDataCount--;
			}
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("data", data);
			result.put("recordtype", recordtype);
			jsonBean.setResult(result);

			reader.close();
		}
		else{
			jsonBean.setResult(null);
		}

		return jsonBean;
	}

	public JSONBean upLoadCSV(HttpServletRequest request) throws IOException, FileUploadException, BiffException, SQLException, ParseException, InsertException
	{
		JSONBean jsonBean = new JSONBean();
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
				System.out.println("上传文件："+filename);//记录文件名
				//获取二进制数据流
				inStream = item.getInputStream();
			}
		}

		if (inStream != null) {
			// 实例解析器CSVReader
			CsvReader reader = new CsvReader(inStream, Charset.forName("utf-8"));
			reader.readHeaders();//读取第一行
			String recordtype = reader.getHeaders()[0].toLowerCase();

			int recordCount = -1;
			if(recordtype.equalsIgnoreCase("100")){
				//100表
				//读取第二行作为表头
				reader.readHeaders();
				String[] headers = reader.getHeaders();

				//在配置表中(client_dic_record_type_code)重新创建配置信息
				updateType100Table(headers, reader);
			}
			else {
				//读取第二行作为数据描述信息 agg/per, edata_source, ins_com, lob1, lob2, lob3, edata_type, table_name
				reader.readHeaders();
				//创建索引信息 创建表
				String[] indexInfos = reader.getHeaders();
				if("agg".equalsIgnoreCase(indexInfos[0])){
					fileType = FileType.EXPO_Agg;
				}
				else{
					fileType = FileType.EXPO_Per;
				}
				Map<String, String> tablefields = createIndexAndDataTable(indexInfos, recordtype);
				//读取第三行作为表头
				reader.readHeaders();
				String[] headers = reader.getHeaders();

				//设置记录读取值，为了重复使用
				//reader.setIsRecordData(true);

				//其他类型表 
				Connection conn = DBUtils.getClientDBConnection();
				try {
					conn.setAutoCommit(false);
				} catch (SQLException e) {
					e.printStackTrace();
					throw new InsertException(e.getMessage());
				}
				Statement ps = conn.createStatement();
				//记录上传次数编号
				ps.execute("INSERT INTO client_data_import_record(import_time, import_user, import_file, import_table) VALUES (now(),'-', '"+filename+"', '"+Data_table_prefix+recordtype+"') returning import_id");

				conn.commit();
				ResultSet rs = ps.getResultSet();
				int importid = 0;
				if (rs.next()) {
					importid = rs.getInt(1);
				}
				try {
					//插入上传数据
					recordCount = updateTypeOtherTable(conn,ps,headers,tablefields,recordtype,importid,reader);
				} catch (InsertException e) {
					e.printStackTrace();
					throw new InsertException(e.getMessage());
				}
				finally{
					rs.close();
					ps.close();
					conn.close();
					reader.close();
				}
			}

			Map<String, Object> result = new HashMap<String, Object>();
			result.put("info", "录入数据量："+recordCount);
			jsonBean.setResult(result);
		}
		else{
			jsonBean.setResult(null);
		}

		return jsonBean;
	}

	public String upLoadCSVRecord100(HttpServletRequest request) throws FileUploadException, IOException, SQLException, InsertException{
		//保存文件到服务器中
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(4096);
		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setSizeMax(maxPostSize);
		InputStream inStream = null;
		String filename = "";
		List<FileItem> fileItems = upload.parseRequest(request);
		Iterator<FileItem> iter = fileItems.iterator();
		String recordTypeID = "";
		while (iter.hasNext()) {
			FileItem item = (FileItem) iter.next();
			if (!item.isFormField()) {
				filename = item.getName();
				System.out.println("上传文件："+filename);//记录文件名
				//获取二进制数据流
				inStream = item.getInputStream();
			}
		}

		if (inStream != null) {
			// 实例解析器CSVReader
			CsvReader reader = new CsvReader(inStream, Charset.forName("utf-8"));
			reader.readHeaders();//读取第一行
			String recordtype = reader.getHeaders()[0].toLowerCase();

			if(recordtype.equalsIgnoreCase("100")){
				//100表
				//读取第二行作为表头
				reader.readHeaders();
				String[] headers = reader.getHeaders();

				//在配置表中(client_dic_record_type_code)重新创建配置信息
				recordTypeID = updateType100Table(headers, reader);
			}
		}

		return recordTypeID;
	}

	public Map<String, String> upLoadCSVRecordOther(HttpServletRequest request) throws FileUploadException, IOException, SQLException, InsertException, ParseException {
		Map<String, String> result = new HashMap<String, String>();
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
				System.out.println("上传文件："+filename);//记录文件名
				//获取二进制数据流
				inStream = item.getInputStream();
			}
		}

		if (inStream != null) {
			// 实例解析器CSVReader
			CsvReader reader = new CsvReader(inStream, Charset.forName("utf-8"));
			reader.readHeaders();//读取第一行
			String recordtype = reader.getHeaders()[0].toLowerCase();
			result.put("recordtype_id", recordtype);
			int recordCount = -1;

			//读取第二行作为数据描述信息 agg/per, edata_source, ins_com, lob1, lob2, lob3, edata_type, table_name
			reader.readHeaders();
			//创建索引信息 创建表
			String[] indexInfos = reader.getHeaders();
			if("agg".equalsIgnoreCase(indexInfos[0])){
				fileType = FileType.EXPO_Agg;
			}
			else{
				fileType = FileType.EXPO_Per;
			}
			Map<String, String> tablefields = createIndexAndDataTable(indexInfos, recordtype);
			//读取第三行作为表头
			reader.readHeaders();
			String[] headers = reader.getHeaders();

			//设置记录读取值，为了重复使用
			//reader.setIsRecordData(true);

			//其他类型表 
			Connection conn = DBUtils.getClientDBConnection();
			try {
				conn.setAutoCommit(false);
			} catch (SQLException e) {
				e.printStackTrace();
				throw new InsertException(e.getMessage());
			}
			Statement ps = conn.createStatement();
			//记录上传次数编号
			ps.execute("INSERT INTO client_data_import_record(import_time, import_user, import_file, import_table) VALUES (now(),'-', '"+filename+"', '"+Data_table_prefix+recordtype+"') returning import_id");

			conn.commit();
			ResultSet rs = ps.getResultSet();
			int importid = 0;
			if (rs.next()) {
				importid = rs.getInt(1);
			}
			result.put("import_id", String.valueOf(importid));
			result.put("import_table", Data_table_prefix+recordtype);
			try {
				//插入上传数据
				recordCount = updateTypeOtherTable(conn,ps,headers,tablefields,recordtype,importid,reader);
			} catch (InsertException e) {
				e.printStackTrace();
				throw new InsertException(e.getMessage());
			}
			finally{
				rs.close();
				ps.close();
				conn.close();
				reader.close();
			}

			result.put("count", String.valueOf(recordCount));
		}

		return result;
	}

	/**
	 * 创建创建类型表以及表索引信息
	 * @param indexInfos
	 * @param recordtype
	 * @return 返回表头字段信息
	 * @throws IOException
	 * @throws SQLException
	 * @throws InsertException 
	 */
	private Map<String, String> createIndexAndDataTable(String[] indexInfos, String recordtype) throws IOException, SQLException, InsertException
	{
		Map<String, String> tablefields = new HashMap<String, String>();
		//判断是否有信息
		Connection connection = DBUtils.getClientDBConnection();
		Statement ps = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, 
				ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = null;

		rs = ps.executeQuery("SELECT * FROM "+Record_type_setting_table+" WHERE record_type_id='"+recordtype+"'");

		String fieldsString = "";
		//添加import_id,attachment_id字段，为系统使用 
		fieldsString += GlobalFuns.createSQLField("import_id", "int")+",";
		tablefields.put("import_id", "integer");
		fieldsString += GlobalFuns.createSQLField("attachment_id", "int")+",";
		tablefields.put("attachment_id", "integer");
		if (fileType.equalsIgnoreCase(FileType.EXPO_Per)) {
			fieldsString += GlobalFuns.createSQLField("geom", "geom")+",";
			tablefields.put("geom", "geometry");
		}
		else if (fileType.equalsIgnoreCase(FileType.EXPO_Agg)) {
			fieldsString += GlobalFuns.createSQLField("geom_id", "string")+",";
			tablefields.put("geom_id", "character varying");
		}

		if (!rs.next()) {
			//--------------------------
			//配置信息不存在，需要录入  ***********************
			throw new InsertException("该类型表字段结构信息不存在，请录入编号为 "+recordtype+" 的字段结构信息");
		}
		rs.previous();//返回首个指针
		while (rs.next()) { //逐行读入除表头的数据
			fieldsString += GlobalFuns.createSQLField(rs.getString("field"), rs.getString("field_type"))+",";
			tablefields.put(rs.getString("field"), rs.getString("field_type"));
		}
		// 删除最后一个逗号
		fieldsString = fieldsString.substring(0,
				fieldsString.length() - 1);

		rs = connection.getMetaData().getTables(null, null, Data_table_prefix+recordtype, null);
		if (rs.next()){
			//Table exist 
		}
		else {
			//未创建该类型数据表
			System.out.println("select user_create_table('"+Data_table_prefix+recordtype
					+"','"+fieldsString
					+"');");
			ps.execute(
					"select user_create_table('"+Data_table_prefix+recordtype
					+"','"+fieldsString
					+"');");
			if (fileType.equalsIgnoreCase(FileType.EXPO_Per)) {
				//添加Rh数据表
				String rh_fields = "policy_id character varying,location_id character varying,event_id character varying,field_value character varying,field_name character varying,field_table character varying,hazard_id character varying";
				ps.execute(
						"select user_create_table('client_risk_rh_data_"+recordtype
						+"','"+rh_fields
						+"');");
				String rm_fields = "policy_id character varying,location_id character varying,event_id character varying,func_id integer,field_name character varying,field_table character varying,loss_rate numeric";
				ps.execute(
						"select user_create_table('client_risk_rm_data_"+recordtype
						+"','"+rm_fields
						+"');");
				//添加Rm数据表
				//				ps.execute(
				//						"select user_create_table('"+Data_table_prefix+recordtype
				//						+"','"+fieldsString
				//						+"');");
			}
		}

		rs = ps.executeQuery("select * from "+Index_table+" where record_type_id = '" + recordtype + "'");
		if (rs.next()) {
			//数据存在，不执行
		}
		else {
			//数据不存在，记录信息
			String headerinsert = "'per',";
			if (indexInfos.length > 0) {
				if ("agg".equalsIgnoreCase(indexInfos[0])) {
					headerinsert = "'agg',";
				}
				else {
					headerinsert = "'per',";
				}
			}
			for (int i = 1; i < 10; i++) {
				if(i < indexInfos.length){
					headerinsert += "'"+(indexInfos[i].isEmpty()?"-":indexInfos[i])+"',";
				}
				else {
					headerinsert += "'-',";
				}
			}

			String headerinsertSql = String.format("INSERT INTO "+Index_table+"("
					+ "per_agg, property1, property2, property3, property4, "
					+ "property5, property6, property7, property8, property9, "
					+ "table_name_db, record_type_id) VALUES (%s %s, %s)",headerinsert,"'"+Data_table_prefix+recordtype+"'","'"+recordtype+"'");
			//更新表索引描述信息信息
			ps.executeUpdate(headerinsertSql);
			if (fileType.equalsIgnoreCase(FileType.EXPO_Per)) {
				//添加E配置信息的同时，添加Rh,Rm配置信息
				String headerinsertSql_rh = String.format("INSERT INTO client_risk_rh_index_table("
						+ "per_agg, property1, property2, property3, property4, "
						+ "property5, property6, property7, property8, property9, "
						+ "table_name_db, record_type_id) VALUES (%s %s, %s)",headerinsert,"'client_risk_rh_data_"+recordtype+"'","'"+recordtype+"'");
				ps.executeUpdate(headerinsertSql_rh);
				String headerinsertSql_rm = String.format("INSERT INTO client_risk_rm_index_table("
						+ "per_agg, property1, property2, property3, property4, "
						+ "property5, property6, property7, property8, property9, "
						+ "table_name_db, record_type_id) VALUES (%s %s, %s)",headerinsert,"'client_risk_rm_data_"+recordtype+"'","'"+recordtype+"'");
				ps.executeUpdate(headerinsertSql_rm);
			}
		}

		rs.close();
		ps.close();
		connection.close();

		return tablefields;
	}

	/**
	 * 返回 新创建的 record_type_id
	 * @throws InsertException 
	 */
	private String updateType100Table(String[] headers, CsvReader reader) throws SQLException, IOException, InsertException
	{
		Statement ps = null;
		Connection conn = null;
		String sql = null;

		conn = DBUtils.getClientDBConnection(); // 调用链接数据库的文件
		conn.setAutoCommit(false);
		ps = conn.createStatement();

		String fieldString = "field,";//加入field字段
		int upload_i = 0;//记录上传行数
		int headerLength = headers.length;
		for (int i = 0; i < headerLength; i++) {
			if(Record_type_setting_field.contains(headers[i]))
			{
				fieldString += headers[i]+",";//录入field sql
			}
		}

		if (fieldString.endsWith(",")) {
			fieldString = fieldString.substring(0,
					fieldString.length() - 1);// 删除最后一个逗号
		}

		ArrayList<String> remebers = new ArrayList<String>();

		String[] uploadheaders = fieldString.split(",");
		String record_type_id = "";
		while (reader.readRecord()) {
			upload_i++;
			if(GlobalFuns.containSpecialChar(reader.get("field_name"))){
				String fn = reader.get("field_name");
				ps.clearBatch();//清空批处理
				ps.close();
				conn.close();
				reader.close();
				throw new InsertException("field_name 中第"+upload_i+"行 \""+fn+"\" 包含非法字符。注意：字段只允许字母、数字和下划线的组合。");
			}
			
			record_type_id = reader.get("record_type_id").toLowerCase();
			
			if (!remebers.contains(record_type_id)) {
				remebers.add(record_type_id);
				//删除原有记录
				ps.executeUpdate("DELETE FROM "+Record_type_setting_table+" WHERE record_type_id='"+record_type_id+"'");
			}

			String valueString = "";
			
			int length = uploadheaders.length;
			for (int j = 0; j < length; j++) {
				if (uploadheaders[j].equalsIgnoreCase("field")) {
//					String relfield = GlobalFuns.stringFilter(reader.get("field_name"));
					String relfield = reader.get("field_name");
					valueString += "'" + relfield.toLowerCase() + "',";
				}
				else if (uploadheaders[j].equalsIgnoreCase("record_type_id")) {
					//					String record_type = reader.get("record_type_id");
					valueString += "'" + record_type_id + "',";
				}
				else if (uploadheaders[j].toLowerCase().equalsIgnoreCase("field_title")) {
					String ft = reader.get(uploadheaders[j]);
					valueString += "'" + (ft==null||ft.isEmpty()?reader.get("field_name"):ft) + "',";
				}
				else {
					valueString += "'" + reader.get(uploadheaders[j]) + "',";
				}
			}
			if (valueString.endsWith(",")) {
				valueString = valueString.substring(0,
						valueString.length() - 1);// 删除最后一个逗号
			}

			sql = String.format(
					"insert into "+Record_type_setting_table+"(%s) values(%s);",
					fieldString, valueString);
			System.out.println(sql);
			ps.executeUpdate(sql);
		}

		conn.commit();
		System.out.println("已成功提交" + upload_i + "行!");
		ps.close();
		conn.close();
		return record_type_id;
	}

	private int updateTypeOtherTable(
			Connection conn, 
			Statement ps, 
			String[] headers,
			Map<String, String> tablefields,
			String recordtype, 
			int importid, 
			CsvReader reader) throws IOException, ParseException, InsertException
	{
		String sql = null;

		String fieldString = "import_id,";//加上录入编号字段
		String relfieldString = "import_id,";//加上录入编号字段
		//加上地理位置字段
		if (fileType.equalsIgnoreCase(FileType.EXPO_Per)) {
			fieldString += "geom,";
			relfieldString += "geom,";
		}
		else if (fileType.equalsIgnoreCase(FileType.EXPO_Agg)) {
			fieldString += "geom_id,";
			relfieldString += "geom_id,";
		}
		int upload_i = 0;//记录上传行数
		int headerLength = headers.length;
		for (int i = 0; i < headerLength; i++) {
			//			String relfield = GlobalFuns.stringFilter(headers[i]).toLowerCase();
			String relfield = headers[i].toLowerCase();
			if(tablefields.containsKey(relfield))
			{
				fieldString += headers[i]+",";
				relfieldString += relfield+",";//录入field sql
			}
		}

		fieldString = fieldString.substring(0,
				fieldString.length() - 1);// 删除最后一个逗号
		relfieldString = relfieldString.substring(0,
				relfieldString.length() - 1);// 删除最后一个逗号

		if(fieldString.equalsIgnoreCase("import_id")
				||fieldString.equalsIgnoreCase("import_id,geom")
				||fieldString.equalsIgnoreCase("import_id,geom_id")){
			return 0;
		}

		String[] uploadheaders = fieldString.split(",");
		while (reader.readRecord()) {   //逐行读入除表头的数据
			upload_i++;
			String valueString = "";

			int length = uploadheaders.length;
			for (int j = 0; j < length; j++) {
				if (uploadheaders[j].equalsIgnoreCase("import_id")) {
					valueString += importid + ",";//首先加上录入编号
				}else if (uploadheaders[j].equalsIgnoreCase("geom")) {
					String lon = reader.get("lon").trim();
					String lat = reader.get("lat").trim();
					if (lon.isEmpty() || lat.isEmpty()) {
						valueString += "null,";
					}
					else{
						valueString += "ST_SetSRID(ST_Point("+reader.get("lon")+", "+reader.get("lat")+"),4326)" + ",";
					}

					//					relfieldString = relfieldString.replace("geom,", "ST_Point("+reader.get("lon")+", "+reader.get("lat")+") as geom,");
				}else if (uploadheaders[j].equalsIgnoreCase("geom_id")) {
					valueString += "user_getgeomid('"+reader.get("code")+"'),";//首先加上录入编号
				}else {
					String value = reader.get(uploadheaders[j]).trim();
					//					if (tablefields.get(uploadheaders[j].toLowerCase()) == Types.DECIMAL
					//						||tablefields.get(uploadheaders[j].toLowerCase()) == Types.INTEGER
					//						||tablefields.get(uploadheaders[j].toLowerCase()) == Types.DOUBLE
					//						||tablefields.get(uploadheaders[j].toLowerCase()) == Types.BIGINT
					//						||tablefields.get(uploadheaders[j].toLowerCase()) == Types.FLOAT
					//						||tablefields.get(uploadheaders[j].toLowerCase()) == Types.NUMERIC
					//						||tablefields.get(uploadheaders[j].toLowerCase()) == Types.SMALLINT
					//						) {//数值类型
					//					String relfield = GlobalFuns.stringFilter(uploadheaders[j]).toLowerCase();
					String relfield = uploadheaders[j].toLowerCase();
					String dataType = tablefields.get(relfield);
					if (dataType.equalsIgnoreCase("int")
							||dataType.equalsIgnoreCase("number")
							||dataType.equalsIgnoreCase("double")
							||dataType.equalsIgnoreCase("float")
							) {//数值类型
						//new DecimalFormat().parse(value).doubleValue()
						value = GlobalFuns.isThousandPointsType(value)?String.valueOf(new DecimalFormat().parse(value).doubleValue()):((value.equalsIgnoreCase("-")||value.isEmpty())?"null":value);
						valueString += value + ",";
					}
					else if (dataType.equalsIgnoreCase("date")
							||dataType.equalsIgnoreCase("timestamp")) {
						valueString += ((value.equalsIgnoreCase("-")||value.isEmpty())?"null,":"'" + value.replaceAll("'", "''") + "',");
					}
					else {//非数值类型
						valueString += "'" + value.replaceAll("'", "''") + "',";	
					}
				}
			}
			if (valueString.endsWith(",")) {
				valueString = valueString.substring(0,
						valueString.length() - 1);// 删除最后一个逗号
			}

			sql = String.format(
					"insert into "+Data_table_prefix+recordtype+"(%s) values(%s);",
					relfieldString, valueString);
			System.out.println(sql);
			try {
				ps.execute(sql);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new InsertException("第"+upload_i+"行:\n"+e.getMessage());
			}

			//			if (upload_i % 500 == 0) {
			//				// 500条记录提交一次
			//				try {
			//					conn.commit();
			//				} catch (SQLException e) {
			//					// TODO Auto-generated catch block
			//					e.printStackTrace();
			//					throw new InsertException("第"+upload_i+"行:\n"+e.getMessage());
			//				}
			//				System.out.println("已成功提交" + upload_i + "行!");
			//			}
		} 

		//		if (upload_i % 500 != 0) {
		//			// 不够500条的再提交一次（其实不用判断，直接提交就可以，不会重复提交的）
		//			try {
		//				conn.commit();
		//			} catch (SQLException e) {
		//				e.printStackTrace();
		//				throw new InsertException("第"+upload_i+"行:\n"+e.getMessage());
		//			}
		//			System.out.println("已成功提交" + upload_i + "行!");
		//		}
		try {
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new InsertException("第"+upload_i+"行:\n"+e.getMessage());
		}
		System.out.println("已成功提交" + upload_i + "行!");

		return upload_i;
	}

	//	将一个数字转换为有千分位的格式：
	//	NumberFormat numberFormat1 = NumberFormat.getNumberInstance();
	//	System.out.println(numberFormat1.format(11122.33)); //结果是11,122.33
	//
	//	NumberFormat numberFormat2 = NumberFormat.getNumberInstance();
	//	numberFormat2.setGroupingUsed(false); //设置了以后不会有千分位，如果不设置，默认是有的
	//	System.out.println(numberFormat2.format(11122.33)); //结果是11122.33 
	//
	//	将一个可能包含千分位的数字转换为不含千分位的形式：
	//	String amount1 = "13,000.00";
	//	double d1 = new DecimalFormat().parse(amount1).doubleValue(); //这里使用的是parse，不是format
	//	System.out.println(String.valueOf(d1)); //结果是13000.00	

	public class FileType {
		// these are static instead of final so they can be changed in unit test
		// isn't visible outside this class and is only accessed once during
		// CsvReader construction
		public static final String EXPO_Per = "Exposure-per";

		public static final String EXPO_Agg = "Exposure-agg";

		public static final String Rl = "Risk_Loss";

		public static final String Rv = "Risk_Visual";

		public static final String Rm = "Risk_Model";
	}
}
