package her.dao.impl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import jxl.read.biff.BiffException;

import org.apache.commons.fileupload.FileUploadException;
import org.python.antlr.PythonParser.else_clause_return;
import org.python.antlr.PythonParser.name_or_print_return;

import her.bean.JSONBean;
import her.bean.JSONBean.Msg;
import her.bean.JSONBean.Status;
import her.dao.JsonBeanDao;
import her.model.InsertException;
import her.utils.CreateTempFile;
import her.utils.DBUtils;
import her.utils.GlobalFuns;
import her.utils.GlobalVariable;
import her.utils.CreateTempFile.ImageFormat;

public class AnalysisDaoImpl {
	private JsonBeanDao jsDao;
	// 限制文件的上传大小
	private int maxPostSize = 100 * 1024 * 1024;
		
	public AnalysisDaoImpl()
	{
		jsDao = new JsonBeanDao(); 
	}
	
	/**
	 * @param client 是否使用 client库
	 */
	public AnalysisDaoImpl(Boolean client)
	{
		if(client)
		{
			jsDao = new JsonBeanDao(DBUtils.getClientDBConnection());
		}
		else {
			jsDao = new JsonBeanDao();
		}
	}
	
	/**
	 * @param h_sql 空间条件
	 * @param h_spatial_f
	 * @param e_sql 统计数据
	 * @param e_spatial_f
	 * @param h_f
	 * @param e_f
	 * @param type 统计类型
	 * @param method 附加计算方式
	 * @param merge 同名合并
	 * @param other 分页条件
	 * @return
	 * @throws SQLException
	 * @throws InsertException
	 */
	public JSONBean statisticalComputer(
			String h_sql,
			String h_spatial_f,
			String e_sql,
			String e_spatial_f,
			String h_f, 
			String e_f, 
			String type,
			String method,
			String merge,
			String other) throws SQLException, InsertException{
		String h_sql_t = "";

		String h_table = GlobalFuns.findCoreTable(h_sql);//空间条件 原表
		if(h_table.toLowerCase().indexOf("client_") == -1 || h_table.indexOf("Disaster_database") != -1){
			JsonBeanDao jsonBeanDao = new JsonBeanDao(DBUtils.getConnection());
			Map<String, String> h_column_type_map = jsonBeanDao.getTableColumnTypeMapping(h_table);
			String h_field_dblink_sql = "";
			String h_field = "";
			for (Entry<String, String> m :h_column_type_map.entrySet())  {  
				h_field += m.getKey()+",";
				h_field_dblink_sql += GlobalFuns.createSQLField(
						"h_"+m.getKey(),
						getPGTypeString(m.getValue())
						)+",";
			}

			// 删除最后一个逗号
			h_field_dblink_sql = h_field_dblink_sql.substring(0,
					h_field_dblink_sql.length() - 1);
			// 删除最后一个逗号
			h_field = h_field.substring(0,
					h_field.length() - 1);
			h_sql_t = "select * from " + String.format(
					GlobalVariable.dbling_disater_db, 
					("select "+h_field+h_sql.substring(h_sql.toLowerCase().indexOf(" from "))).replace("'", "''"),
					h_field_dblink_sql
					);
		}
		else {
			h_sql_t = h_sql;
		}
		String e_sql_t = "";
		String e_table = GlobalFuns.findCoreTable(e_sql);
		if(e_table.toLowerCase().indexOf("client_") == -1){
			JsonBeanDao jsonBeanDao = new JsonBeanDao(DBUtils.getConnection());
			Map<String, String> e_column_type_map = jsonBeanDao.getTableColumnTypeMapping(e_table);
			String e_field_dblink_sql = "";
			String e_field = "";
			for (Entry<String, String> m2 :e_column_type_map.entrySet())  {  
				e_field += m2.getKey()+",";
				e_field_dblink_sql += GlobalFuns.createSQLField(
						m2.getKey(),
						getPGTypeString(m2.getValue())
						)+",";
			} 
			// 删除最后一个逗号
			e_field_dblink_sql = e_field_dblink_sql.substring(0,
					e_field_dblink_sql.length() - 1);
			// 删除最后一个逗号
			e_field = e_field.substring(0,
					e_field.length() - 1);
			e_sql_t = "select * from " + String.format(
					GlobalVariable.dbling_disater_db, 
					("select "+e_field+e_sql.substring(e_sql.toLowerCase().indexOf(" from "))).replace("'", "''"),
					e_field_dblink_sql
					);
		}
		else {
			e_sql_t = e_sql;
		}
//		if(spatial_f2.equalsIgnoreCase("point") && spatial_f1.equalsIgnoreCase("rast")){
//			sql = String.format("SELECT st_value(t1.%s, st_transform(t2.geom, st_srid(t1.%s))) as rast_value,t2.* FROM (%s) t1,(%s) t2", spatial_f1,spatial_f1,t1_sql,t2_sql);
//		}
//		else if(spatial_f2.equalsIgnoreCase("point") && !spatial_f1.equalsIgnoreCase("rast")){
//			sql = String.format("SELECT * FROM (%s) t1,(%s) t2 where ST_Intersects(t1.%s,t2.geom)", t1_sql,t2_sql,spatial_f1);
//		}
//		else {
//			sql = String.format("SELECT * FROM (%s) t1,(%s) t2 where ST_Intersects(t1.%s,t2.%s)", t1_sql,t2_sql,spatial_f1,spatial_f2);
//		}
//		String sql = String.format("SELECT * FROM (%s) t1,(%s) t2 where ST_Intersects(t1.%s,t2.%s)", t1_sql,t2_sql,spatial_f1,spatial_f2);
		
		String sql = "";
		switch (type) {
		case "polygon_point":
			if(h_table.toLowerCase().indexOf("client_") == -1){
				sql = String.format("SELECT * FROM (%s) t1,(%s) t2 where ST_Intersects(t1.h_geom,t2.geom)", h_sql_t,e_sql_t);
			}
			else {
				sql = String.format("SELECT * FROM (%s) t1,(%s) t2 where ST_Intersects(t1.geom,t2.geom)", h_sql_t,e_sql_t);
			}
			break;
		case "polygon_polygon":
			if(h_table.toLowerCase().indexOf("client_") == -1){
				sql = String.format("SELECT * FROM (%s) t1,(%s) t2 where ST_Intersects(t1.h_geom,t2.geom)", h_sql_t,e_sql_t);
			}
			else {
				sql = String.format("SELECT * FROM (%s) t1,(%s) t2 where ST_Intersects(t1.geom,t2.geom)", h_sql_t,e_sql_t);
			}
			break;
		case "rast_point":
			if(h_table.toLowerCase().indexOf("client_") == -1){
				sql = String.format("SELECT st_value(t1.h_rast, st_transform(t2.geom, st_srid(t1.h_rast))) as rast_value,t2.* FROM (%s) t1,(%s) t2", h_sql_t,e_sql_t);
			}
			else {
				sql = String.format("SELECT st_value(t1.rast, st_transform(t2.geom, st_srid(t1.rast))) as rast_value,t2.* FROM (%s) t1,(%s) t2", h_sql_t,e_sql_t);
			}
			break;
		case "rast_polygon":
			if(h_table.toLowerCase().indexOf("client_") == -1){
				sql = String.format("SELECT * FROM (%s) t1,(%s) t2 where ST_Intersects(t1.h_rast,t2.geom)", h_sql_t,e_sql_t);
			}
			else {
				sql = String.format("SELECT * FROM (%s) t1,(%s) t2 where ST_Intersects(t1.rast,t2.geom)", h_sql_t,e_sql_t);
			}
			break;

		default:
			break;
		}
//		if(spatial_f2.equalsIgnoreCase("point") && spatial_f1.equalsIgnoreCase("rast")){
//			if(table1.toLowerCase().indexOf("client_") == -1){
//				sql = String.format("SELECT st_value(t1.h_%s, st_transform(t2.geom, st_srid(t1.h_%s))) as rast_value,t2.* FROM (%s) t1,(%s) t2", spatial_f1,spatial_f1,t1_sql,t2_sql);
//			}
//			else {
//				sql = String.format("SELECT st_value(t1.%s, st_transform(t2.geom, st_srid(t1.%s))) as rast_value,t2.* FROM (%s) t1,(%s) t2", spatial_f1,spatial_f1,t1_sql,t2_sql);
//			}
//			
//		}
//		else if(spatial_f2.equalsIgnoreCase("point") && !spatial_f1.equalsIgnoreCase("rast")){
//			if(table1.toLowerCase().indexOf("client_") == -1){
//				sql = String.format("SELECT * FROM (%s) t1,(%s) t2 where ST_Intersects(t1.h_%s,t2.geom)", t1_sql,t2_sql,spatial_f1);
//			}
//			else {
//				sql = String.format("SELECT * FROM (%s) t1,(%s) t2 where ST_Intersects(t1.%s,t2.geom)", t1_sql,t2_sql,spatial_f1);
//			}
//		}
//		else {
//			sql = String.format("SELECT * FROM (%s) t1,(%s) t2 where ST_Intersects(t1.%s,t2.%s)", t1_sql,t2_sql,spatial_f1,spatial_f2);
//		}
		String s_sql = "";
		String result_f = "";
		switch (type) {
		case "polygon_point":
		case "polygon_polygon":
			if(h_table.toLowerCase().indexOf("client_") == -1){
				h_f = "h_" + h_f + ",h_" + h_spatial_f;
			}
			if("false".equalsIgnoreCase(merge)){
				result_f = h_f = "h_rid," + h_f;
			}
			else {
				result_f = "array_agg(h_rid)," + h_f;
			}
			result_f = result_f.replace("h_" + h_spatial_f, "ST_AsEWKT(h_" + h_spatial_f + ") as wkt");
			break;
		case "rast_point":
		case "rast_polygon":
			result_f = h_f;
			break;

		default:
			break;
		}
//		
//		if("geom".equalsIgnoreCase(spatial_f1)){
//			if(table1.toLowerCase().indexOf("client_") == -1){
//				f1 = "h_" + f1;
//			}
//			if("false".equalsIgnoreCase(merge)){
//				result_f = f1 = "h_rid," + f1;
//			}
//			else {
//				result_f = "array_agg(h_rid)," + f1;
//			}
//		}
//		else if ("rast".equalsIgnoreCase(spatial_f1)) {
//			result_f = f1;
//		}

		if ("sum".equalsIgnoreCase(method)) {
			s_sql = String.format("SELECT %s,count(*),sum(%s::NUMERIC) FROM (%s) t group by %s", result_f,e_f,sql,h_f);
		}
		else if ("max".equalsIgnoreCase(method)) {
			s_sql = String.format("SELECT %s,count(*),max(%s::NUMERIC) FROM (%s) t group by %s", result_f,e_f,sql,h_f);
		}
		else if ("min".equalsIgnoreCase(method)) {
			s_sql = String.format("SELECT %s,count(*),min(%s::NUMERIC) FROM (%s) t group by %s", result_f,e_f,sql,h_f);
		}
		else if ("avg".equalsIgnoreCase(method)) {
			s_sql = String.format("SELECT %s,count(*),avg(%s::NUMERIC) FROM (%s) t group by %s", result_f,e_f,sql,h_f);
		}
		else if ("vari".equalsIgnoreCase(method)) {
			//to_number(%s, '9999999999999.9999999999999')
			s_sql = String.format("SELECT %s,count(*),variance(%s::NUMERIC) FROM (%s) t group by %s", result_f,e_f,sql,h_f);
		}
		int count = jsDao.queryCount("select count(*) from ("+s_sql+") t", false);
		JSONBean jsonBean = jsDao.queryTable(s_sql+other);
		jsonBean.setDataCount(count);
		return jsonBean;
	}
	
	private String getPGTypeString(String typeStr)
	{
		String rString = "text";
		switch (typeStr) {
		case "text":
		case "varchar":
			rString = "text";
			break;
		case "numeric":
			rString = "number";
			break;
		case "int4":
			rString = "int";
			break;
		case "date":
			rString = "date";
			break;
		case "geometry":
			rString = "geom";
			break;
		case "raster":
			rString = "rast";
			break;
		}
		return rString;
	}

	public JSONBean commonTableQuery(String fields, String from, String where, String other)
	{
		String sql = "select " + fields + " from " + from + " where " + where + other;
		JSONBean jsonBean = jsDao.queryTable(sql);
		return jsonBean;
	}
	
	public JSONBean getDownscaleVectorList()
	{
		String sql = "select * from dic_downscale_index";
		JSONBean jsonBean = jsDao.queryTable(sql);
		return jsonBean;
	}
	
	public JSONBean dataDownscale(HttpServletRequest request, String sql, String tag)
	{
		Connection connection = DBUtils.getConnection();

//		if (dsdb != null && dsdb.equalsIgnoreCase("client")) {
//			JsonBeanDao jsonBeanDao = new JsonBeanDao(DBUtils.getClientDBConnection());
//
//			Map<String, String> column_type_map = jsonBeanDao.getTableColumnTypeMapping(GlobalFuns.findCoreTable(ds_sql));
//			String field_dblink_sql = "";
//			String field = "";
//			for (Entry<String, String> m :column_type_map.entrySet())  {  
//				field += m.getKey()+",";
//				field_dblink_sql += GlobalFuns.createSQLField(
//						m.getKey(),
//						getPGTypeString(m.getValue())
//						)+",";
//			}
//			// 删除最后一个逗号
//			field_dblink_sql = field_dblink_sql.substring(0,
//					field_dblink_sql.length() - 1);
//			// 删除最后一个逗号
//			field = field.substring(0,
//					field.length() - 1);
//
//			ds_sql = String.format(
//					GlobalVariable.dbling_client_db, 
//					("select "+field+ds_sql.substring(ds_sql.toLowerCase().indexOf(" from "))).replace("'", "''"),
//					field_dblink_sql
//					);
//		}
		String sqlString = GlobalFuns.dblingUse(sql);
		System.out.println(sqlString);
		JSONBean jsonBean = new JSONBean();
		try {
			PreparedStatement preparedStatement = connection
					.prepareStatement(sqlString);
			Statement statement = connection.createStatement();
			statement.executeQuery(sqlString);
			ResultSetMetaData resultSetMetaData = preparedStatement
					.getMetaData();
			ResultSet rs = statement.getResultSet();// 查询结果数据集

			Map<String, String> structureMap = new HashMap<String, String>();// 用来存储数据结构
			for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
				structureMap.put(resultSetMetaData.getColumnName(i + 1),
						resultSetMetaData.getColumnTypeName(i + 1));

			}
			List<Map<String, String>> data = new ArrayList<Map<String, String>>();// 数据
			HttpSession session = request.getSession();
			while (rs.next()) {
				Map<String, String> map = new HashMap<String, String>();

				for (String s : structureMap.keySet()) {
					if (/*structureMap.get(s).equals("raster") || structureMap.get(s) == "geometry"|| */structureMap.get(s).equals("bytea")) {
						map.put(s, CreateTempFile.CreateTempImgFile(rs.getBytes(s), ImageFormat.PNG));

						if (session.isNew()) {
							session.setAttribute("imagefilename"+rs.getRow(), map.get(s));
						} else {
							session.invalidate();
							session = request.getSession();
							session.setAttribute("imagefilename"+rs.getRow(), map.get(s));
						}
					}//如果是raster或者geometry或者bytea类型，则转换成base64编码
					else if(!structureMap.get(s).equals("raster") && !structureMap.get(s).equals("geometry")){
						map.put(s, rs.getString(s));
					}
				}
				data.add(map);

			}
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("structure", structureMap);
			result.put("data", data);
			if(tag != null){
				result.put("tag", tag);
			}
			jsonBean.setResult(result);
			jsonBean.setStatus(Status.SUCCESS);
			statement.close();
			connection.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			jsonBean.setStatus(Status.FAILED);
			jsonBean.setMsg(e.getMessage());
		}
		return jsonBean;
	}
	
	//=======================================
	// 回归期分析-正问题
	//=======================================
	public JSONBean returnTimeQueryDisaster(String rtid)
	{
		JSONBean jsonBean = jsDao.queryTable("select distinct(split_part(event_id, '_',1)) as disaster_id from public.client_risk_rm_data_"+rtid);
		return jsonBean;
	}
	
	public JSONBean returnTimeQueryData(String type, String rtid, String disaster_ids, String statistic_field, String where)
	{
		JSONBean jsonBean = new JSONBean();
		String[] disaster_idArr = disaster_ids.split(",");
		List<Map<String, Object>> data_list = new ArrayList<Map<String, Object>>();
		for (String disaster_id : disaster_idArr) {
			Map<String, Object> result = null;
			if(type.equalsIgnoreCase("Timeline")){
				result = queryData_Timeline(rtid, disaster_id, statistic_field,where);
			}
			else {
				result = queryData_ReturnTime(rtid, disaster_id, statistic_field,where);
			}
			data_list.add(result);
		}
		jsonBean.setResult(data_list);
		jsonBean.setStatus(Status.SUCCESS);
		return jsonBean;
	}
	
	private Map<String, Object> queryData_Timeline(String rtid, String disaster_id, String statistic_field, String where)
	{
		Connection conn = DBUtils.getClientDBConnection();
		
		//year
		String sql_year = "select max(substring(split_part(event_id, '_',3), 1,4)::float)-min(substring(split_part(event_id, '_',3), 1,4)::float)+1 as year from public.client_risk_rm_data_"+rtid;
		ResultSet rs = null;
		double year = 50;
		try {
			rs = conn.prepareStatement(sql_year).executeQuery();
			if(rs.next()){
				year = rs.getDouble("year");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//rp
		String sql_rpvalue = "select "+year+" / ROW_NUMBER() OVER()  as num, round(r.loss_rate,4) as loss_rate "
				+ "from "
				+ "("
				+ "SELECT rm.event_id,sum(loss_rate * expo."+statistic_field+")/sum(expo."+statistic_field+") as loss_rate "
				+ "FROM client_risk_rm_data_"+rtid+" rm, "
				+ "client_expo_policy_"+rtid+" expo "
				+ "where rm.policy_id = expo.policy_id and rm.location_id = expo.location_id "
				+ "and rm.loss_rate > 0 "
				+ "and rm.event_id like '"+disaster_id+"%' "
				+ "group by event_id "
				+ "order by loss_rate desc "
				+ "limit 5 "
				+ ") r";
		List<Map<String, String>> rp_value = new ArrayList<Map<String, String>>();// 数据
		try {
			rs = conn.prepareStatement(sql_rpvalue).executeQuery();
			while(rs.next()){
				Map<String, String> rp_map = new HashMap<String, String>();
				rp_map.put("label", "("+rs.getString("loss_rate")+","+String.format("%.2f",rs.getDouble("num"))+"/"+year+")");
				rp_map.put("y", rs.getString("loss_rate"));
				rp_value.add(rp_map);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//data
		String sql_data = "SELECT rm.event_id,sum(loss_rate * expo."+statistic_field+")/sum(expo."+statistic_field+") as loss_rate "
				+ "FROM client_risk_rm_data_"+rtid+" rm,"
				+ "client_expo_policy_"+rtid+" expo "
				+ "where rm.policy_id = expo.policy_id "
				+ "and rm.location_id = expo.location_id "
				+ "and rm.loss_rate > 0 "
				+ "and rm.event_id like '"+disaster_id+"%' "
				+ (where.isEmpty()?"":"and "+where+" ")
				+ "group by rm.event_id "
				+ "order by rm.event_id";
		System.out.println(sql_data);
		List<Map<String, Object>> data_list = new ArrayList<Map<String, Object>>();// 数据
		try {
			rs = conn.prepareStatement(sql_data).executeQuery();
			while(rs.next()){
				Map<String, Object> data_map = new HashMap<String, Object>();
				data_map.put("x", rs.getString("event_id"));
				data_map.put("y", rs.getString("loss_rate"));
				
				//赋属性
				Map<String, String> attributes = new HashMap<String, String>();
				attributes.put("event_id", rs.getString("event_id"));
				attributes.put("loss_rate", rs.getString("loss_rate"));
				data_map.put("attributes", attributes);
				
				data_list.add(data_map);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("record_type", rtid);
		result.put("year", year);
		result.put("disaster", disaster_id);
		result.put("rp", rp_value);
		result.put("data", data_list);
		
		return result;
	}
	
	private Map<String, Object> queryData_ReturnTime(String rtid, String disaster_id, String statistic_field, String where)
	{
		Connection conn = DBUtils.getClientDBConnection();
		
		//year
		String sql_year = "select max(substring(split_part(event_id, '_',3), 1,4)::float)-min(substring(split_part(event_id, '_',3), 1,4)::float)+1 as year from public.client_risk_rm_data_"+rtid;
		ResultSet rs = null;
		double year = 50;
		try {
			rs = conn.prepareStatement(sql_year).executeQuery();
			if(rs.next()){
				year = rs.getDouble("year");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//rp
		String sql_rpvalue = "select "+year+" / ROW_NUMBER() OVER()  as num, round(r.loss_rate,4) as loss_rate "
				+ "from "
				+ "("
				+ "SELECT rm.event_id,sum(loss_rate * expo."+statistic_field+")/sum(expo."+statistic_field+") as loss_rate "
				+ "FROM client_risk_rm_data_"+rtid+" rm, "
				+ "client_expo_policy_"+rtid+" expo "
				+ "where rm.policy_id = expo.policy_id and rm.location_id = expo.location_id "
				+ "and rm.loss_rate > 0 "
				+ "and rm.event_id like '"+disaster_id+"%' "
				+ "group by event_id "
				+ "order by loss_rate desc "
				+ "limit 5 "
				+ ") r";
		List<Map<String, String>> rp_value = new ArrayList<Map<String, String>>();// 数据
		try {
			rs = conn.prepareStatement(sql_rpvalue).executeQuery();
			while(rs.next()){
				Map<String, String> rp_map = new HashMap<String, String>();
				rp_map.put("label", "("+rs.getString("loss_rate")+","+String.format("%.2f",rs.getDouble("num"))+"/"+year+")");
				rp_map.put("y", rs.getString("loss_rate"));
				rp_value.add(rp_map);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//data
		String sql_data = "SELECT "+year+" / ROW_NUMBER() OVER() as rp_year, r.loss_rate "
				+ "FROM "
				+ "("
				+ "SELECT rm.event_id,sum(loss_rate * expo."+statistic_field+")/sum(expo."+statistic_field+") as loss_rate "
				+ "FROM public.client_risk_rm_data_"+rtid+" rm,client_expo_policy_"+rtid+" expo "
				+ "where rm.policy_id = expo.policy_id and  rm.location_id = expo.location_id "
				+ "and rm.loss_rate > 0 "
				+ "and rm.event_id like '%0%' "
				+ (where.isEmpty()?"":"and "+where+" ")
				+ "group by event_id "
				+ "order by loss_rate desc "
				+ ") r "
				+ "order by rp_year asc ";
		System.out.println(sql_data);
		List<Map<String, Object>> data_list = new ArrayList<Map<String, Object>>();// 数据
		try {
			rs = conn.prepareStatement(sql_data).executeQuery();
			while(rs.next()){
				Map<String, Object> data_map = new HashMap<String, Object>();
				data_map.put("x", rs.getString("rp_year"));
				data_map.put("y", rs.getString("loss_rate"));
				
				//赋属性
				Map<String, String> attributes = new HashMap<String, String>();
				attributes.put("rp_year", rs.getString("rp_year"));
				attributes.put("loss_rate", rs.getString("loss_rate"));
				data_map.put("attributes", attributes);
				
				data_list.add(data_map);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("record_type", rtid);
		result.put("year", year);
		result.put("disaster", disaster_id);
		result.put("rp", rp_value);
		result.put("data", data_list);
		
		return result;
	}
	
	//=======================================
	// 回归期分析-反问题
	//=======================================
	public JSONBean returnTimeQueryDisaster_cons(String rtid)
	{
		JSONBean jsonBean = jsDao.queryTable("select distinct(split_part(event_id, '_',1)) as disaster_id from public.client_risk_rl_data_"+rtid);
		return jsonBean;
	}
	
	public JSONBean returnTimeQueryData_cons(String type, String rtid, String disaster_ids, 
			String sf1, String sf2, String starttime, String endtime, String where)
	{
		JSONBean jsonBean = new JSONBean();
		String[] disaster_idArr = disaster_ids.split(",");
		List<Map<String, Object>> data_list = new ArrayList<Map<String, Object>>();
		for (String disaster_id : disaster_idArr) {
			Map<String, Object> result = null;
			if(type.equalsIgnoreCase("Timeline")){
				result = queryData_Timeline_cons(rtid, disaster_id, sf1, sf2, starttime, endtime, where);
			}
			else {
				result = queryData_ReturnTime_cons(rtid, disaster_id,  sf1, sf2, starttime, endtime, where);
			}
			data_list.add(result);
		}
		jsonBean.setResult(data_list);
		jsonBean.setStatus(Status.SUCCESS);
		return jsonBean;
	}
	
	private Map<String, Object> queryData_Timeline_cons(
			String rtid, 
			String disaster_id, 
			String sf1,
			String sf2, 
			String starttime,
			String endtime,
			String where)
	{
		Connection conn = DBUtils.getClientDBConnection();
		
		//year
		String sql_year = "select max(substring(split_part(event_id, '_',3), 1,4)::float)-min(substring(split_part(event_id, '_',3), 1,4)::float)+1 as year from public.client_risk_rl_data_"+rtid;
		ResultSet rs = null;
		double year = 50;
		try {
			rs = conn.prepareStatement(sql_year).executeQuery();
			if(rs.next()){
				year = rs.getDouble("year");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//rp
		String sql_rpvalue = "select "+year+" / ROW_NUMBER() OVER()  as num, round(r.loss_rate,4) as loss_rate "
				+ "from "
				+ "("
				+ "SELECT rl.event_id,rl.sumofloss/sum(expo."+sf2+") as loss_rate "
				+ "FROM (select event_id,sum("+sf1+") as sumofloss, split_part(event_id, '_',3)::date as date from public.client_risk_rl_data_"+rtid+" group by event_id) rl, "
				+ "client_expo_policy_"+rtid+" expo "
				+ "where expo."+endtime+" >= rl.date and expo."+starttime+" <= rl.date "
				+ "and rl.event_id like '"+disaster_id+"%' "
				+ "group by rl.event_id,rl.sumofloss "
				+ "order by loss_rate desc nulls last "
				+ "limit 5 "
				+ ") r";
		List<Map<String, String>> rp_value = new ArrayList<Map<String, String>>();// 数据
		try {
			rs = conn.prepareStatement(sql_rpvalue).executeQuery();
			while(rs.next()){
				Map<String, String> rp_map = new HashMap<String, String>();
				rp_map.put("label", "("+rs.getString("loss_rate")+","+String.format("%.2f",rs.getDouble("num"))+"/"+year+")");
				rp_map.put("y", rs.getString("loss_rate"));
				rp_value.add(rp_map);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//data
		String sql_data = "SELECT rl.event_id,rl.sumofloss/sum(expo."+sf2+") as loss_rate "
				+ "FROM (select event_id,sum("+sf1+") as sumofloss, split_part(event_id, '_',3)::date as date from public.client_risk_rl_data_"+rtid+" group by event_id) rl,"
				+ "client_expo_policy_"+rtid+" expo "
				+ "where expo."+endtime+" >= rl.date and expo."+starttime+" <= rl.date "
				+ "and rl.event_id like '"+disaster_id+"%' "
				+ (where.isEmpty()?"":"and "+where+" ")
				+ "group by rl.event_id,rl.sumofloss "
				+ "order by rl.event_id";
		System.out.println(sql_data);
		List<Map<String, Object>> data_list = new ArrayList<Map<String, Object>>();// 数据
		try {
			rs = conn.prepareStatement(sql_data).executeQuery();
			while(rs.next()){
				Map<String, Object> data_map = new HashMap<String, Object>();
				data_map.put("x", rs.getString("event_id"));
				data_map.put("y", rs.getString("loss_rate"));
				
				//赋属性
				Map<String, String> attributes = new HashMap<String, String>();
				attributes.put("event_id", rs.getString("event_id"));
				attributes.put("loss_rate", rs.getString("loss_rate"));
				data_map.put("attributes", attributes);
				
				data_list.add(data_map);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("record_type", rtid);
		result.put("year", year);
		result.put("disaster", disaster_id);
		result.put("rp", rp_value);
		result.put("data", data_list);
		
		return result;
	}
	
	private Map<String, Object> queryData_ReturnTime_cons(
			String rtid, 
			String disaster_id,  
			String sf1, 
			String sf2, 
			String starttime,
			String endtime,
			String where)
	{
		Connection conn = DBUtils.getClientDBConnection();
		
		//year
		String sql_year = "select max(substring(split_part(event_id, '_',3), 1,4)::float)-min(substring(split_part(event_id, '_',3), 1,4)::float)+1 as year from public.client_risk_rl_data_"+rtid;
		ResultSet rs = null;
		double year = 50;
		try {
			rs = conn.prepareStatement(sql_year).executeQuery();
			if(rs.next()){
				System.out.println(rs.getDouble("year"));
				System.out.println(rs.getString("year"));
				year = rs.getDouble("year");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//rp
		String sql_rpvalue = "select "+year+" / ROW_NUMBER() OVER() as num, round(r.loss_rate,4) as loss_rate "
				+ "from "
				+ "("
				+ "SELECT rl.event_id,rl.sumofloss/sum(expo."+sf2+") as loss_rate "
				+ "FROM (select event_id,sum("+sf1+") as sumofloss, split_part(event_id, '_',3)::date as date from public.client_risk_rl_data_"+rtid+" group by event_id) rl, "
				+ "client_expo_policy_"+rtid+" expo "
				+ "where expo."+endtime+" >= rl.date and expo."+starttime+" <= rl.date "
				+ "and rl.event_id like '"+disaster_id+"%' "
				+ "group by rl.event_id,rl.sumofloss "
				+ "order by loss_rate desc nulls last "
				+ "limit 5 "
				+ ") r";
		List<Map<String, String>> rp_value = new ArrayList<Map<String, String>>();// 数据
		try {
			rs = conn.prepareStatement(sql_rpvalue).executeQuery();
			while(rs.next()){
				Map<String, String> rp_map = new HashMap<String, String>();
				rp_map.put("label", "("+rs.getString("loss_rate")+","+String.format("%.2f",rs.getDouble("num"))+"/"+year+")");
				rp_map.put("y", rs.getString("loss_rate"));
				rp_value.add(rp_map);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//data
		String sql_data = "SELECT "+year+" / ROW_NUMBER() OVER() as rp_year, r.loss_rate "
				+ "FROM "
				+ "("
				+ "SELECT rl.event_id,rl.sumofloss/sum(expo."+sf2+") as loss_rate "
				+ "FROM (select event_id,sum("+sf1+") as sumofloss, split_part(event_id, '_',3)::date as date from public.client_risk_rl_data_"+rtid+" group by event_id) rl, "
				+ "client_expo_policy_"+rtid+" expo "
				+ "where expo."+endtime+" >= rl.date and expo."+starttime+" <= rl.date "
				+ "and rl.event_id like '"+disaster_id+"%' "
				+ (where.isEmpty()?"":"and "+where+" ")
				+ "group by rl.event_id,rl.sumofloss "
				+ "order by loss_rate desc nulls last "
				+ ") r "
				+ "order by rp_year asc ";
		System.out.println(sql_data);
		List<Map<String, Object>> data_list = new ArrayList<Map<String, Object>>();// 数据
		try {
			rs = conn.prepareStatement(sql_data).executeQuery();
			while(rs.next()){
				Map<String, Object> data_map = new HashMap<String, Object>();
				data_map.put("x", rs.getString("rp_year"));
				data_map.put("y", rs.getString("loss_rate"));
				
				//赋属性
				Map<String, String> attributes = new HashMap<String, String>();
				attributes.put("rp_year", rs.getString("rp_year"));
				attributes.put("loss_rate", rs.getString("loss_rate"));
				data_map.put("attributes", attributes);
				
				data_list.add(data_map);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("record_type", rtid);
		result.put("year", year);
		result.put("disaster", disaster_id);
		result.put("rp", rp_value);
		result.put("data", data_list);
		
		return result;
	}
	//=======================================
	// 对比数据-反问题
	//=======================================
	public JSONBean returnTimeQueryCompareData_cons(String type, String rtid, String disaster_ids, 
			String sumfield, String starttime, String endtime, String where)
	{
		JSONBean jsonBean = new JSONBean();
		String[] disaster_idArr = disaster_ids.split(",");
		List<Map<String, Object>> data_list = new ArrayList<Map<String, Object>>();
		for (String disaster_id : disaster_idArr) {
			Map<String, Object> result = null;
			if(type.equalsIgnoreCase("Timeline")){
				result = queryCompareData_Timeline_cons(rtid, disaster_id, sumfield, starttime, endtime, where);
			}
			else {
				result = queryCompareData_ReturnTime_cons(rtid, disaster_id, sumfield, starttime, endtime, where);
			}
			data_list.add(result);
		}
		jsonBean.setResult(data_list);
		jsonBean.setStatus(Status.SUCCESS);
		return jsonBean;
	}
	
	private Map<String, Object> queryCompareData_ReturnTime_cons(
			String rtid, 
			String disaster_id, 
			String sumfield,
			String starttime,
			String endtime,
			String where)
	{
		Connection conn = DBUtils.getClientDBConnection();
		
		//year
		String sql_year = "select max(extract(year from "+endtime+")) - min(extract(year from "+starttime+")) as year from public.client_expo_policy_"+rtid;
		ResultSet rs = null;
		double year = 50;
		try {
			rs = conn.prepareStatement(sql_year).executeQuery();
			if(rs.next()){
				year = rs.getDouble("year");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//rp
		String sql_rpvalue = "select "+year+" / ROW_NUMBER() OVER()  as num, round(r.loss_rate,4) as loss_rate "
				+ "from "
				+ "("
				+ "SELECT rm.event_id, sum(loss_rate * expo."+sumfield+")/sum(expo."+sumfield+") as loss_rate "
				+ "FROM public.client_risk_rm_data_"+rtid+" rm,"
				+ "client_expo_policy_"+rtid+" expo "
				+ "where rm.policy_id = expo.policy_id and rm.location_id = expo.location_id "
				+ "and rm.loss_rate > 0 "
				+ "and rm.event_id like '"+disaster_id+"%'"
				+ "and split_part(rm.event_id, '_',3)::date > expo."+starttime+" and split_part(rm.event_id, '_',3)::date < expo."+endtime+" "
				+ "group by event_id "
				+ "limit 5 "
				+ ") r";
		List<Map<String, String>> rp_value = new ArrayList<Map<String, String>>();// 数据
		try {
			rs = conn.prepareStatement(sql_rpvalue).executeQuery();
			while(rs.next()){
				Map<String, String> rp_map = new HashMap<String, String>();
				rp_map.put("label", "("+rs.getString("loss_rate")+","+String.format("%.2f",rs.getDouble("num"))+"/"+year+")");
				rp_map.put("y", rs.getString("loss_rate"));
				rp_value.add(rp_map);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//data
		String sql_data = "SELECT "+year+" / ROW_NUMBER() OVER() as rp_year, r.loss_rate "
				+ "FROM "
				+ "("
				+ "  SELECT rm.event_id, sum(loss_rate * expo."+sumfield+")/sum(expo."+sumfield+") as loss_rate "
				+ "  FROM public.client_risk_rm_data_"+rtid+" rm, "
				+ "       client_expo_policy_"+rtid+" expo "
				+ "  where rm.policy_id = expo.policy_id and  rm.location_id = expo.location_id "
				+ "        and rm.loss_rate > 0 "
				+ "        and rm.event_id like '"+disaster_id+"%' "
				+ "        and split_part(rm.event_id, '_',3)::date > expo."+starttime+" and split_part(rm.event_id, '_',3)::date < expo."+endtime+" "
				+          (where.isEmpty()?"":"and "+where+" ")
				+ "  group by event_id "
				+ "  order by loss_rate desc nulls last "
				+ ") r "
				+ "order by rp_year asc";
		
		System.out.println(sql_data);
		List<Map<String, Object>> data_list = new ArrayList<Map<String, Object>>();// 数据
		try {
			rs = conn.prepareStatement(sql_data).executeQuery();
			while(rs.next()){
				Map<String, Object> data_map = new HashMap<String, Object>();
				data_map.put("x", rs.getString("rp_year"));
				data_map.put("y", rs.getString("loss_rate"));
				
				//赋属性
				Map<String, String> attributes = new HashMap<String, String>();
				attributes.put("rp_year", rs.getString("rp_year"));
				attributes.put("loss_rate", rs.getString("loss_rate"));
				data_map.put("attributes", attributes);
				
				data_list.add(data_map);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("record_type", rtid);
		result.put("year", year);
		result.put("disaster", disaster_id);
		result.put("rp", rp_value);
		result.put("data", data_list);
		
		return result;
	}
	
	private Map<String, Object> queryCompareData_Timeline_cons(
			String rtid, 
			String disaster_id,  
			String sumfield,
			String starttime,
			String endtime,
			String where)
	{
		Connection conn = DBUtils.getClientDBConnection();
		
		//year
		String sql_year = "select max(extract(year from "+endtime+")) - min(extract(year from "+starttime+")) as year from public.client_expo_policy_"+rtid;
		ResultSet rs = null;
		double year = 50;
		try {
			rs = conn.prepareStatement(sql_year).executeQuery();
			if(rs.next()){
				year = rs.getDouble("year");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//rp
		String sql_rpvalue = "select "+year+" / ROW_NUMBER() OVER() as num, round(r.loss_rate,4) as loss_rate "
				+ "from "
				+ "("
				+ "SELECT rm.event_id,sum(loss_rate * expo."+sumfield+")/sum(expo."+sumfield+") as loss_rate "
				+ "FROM  public.client_risk_rm_data_"+rtid+" rm, "
				+ "client_expo_policy_"+rtid+" expo "
				+ "where rm.policy_id = expo.policy_id and rm.location_id = expo.location_id "
				+ "and rm.loss_rate > 0 "
				+ "and rm.event_id like '"+disaster_id+"%' "
				+ "and split_part(rm.event_id, '_',3)::date > expo."+starttime+" and split_part(rm.event_id, '_',3)::date < expo."+endtime+" "
				+ "group by event_id "
				+ "order by loss_rate desc nulls last "
				+ "limit 5 "
				+ ") r";
		System.out.println(sql_rpvalue);
		List<Map<String, String>> rp_value = new ArrayList<Map<String, String>>();// 数据
		try {
			rs = conn.prepareStatement(sql_rpvalue).executeQuery();
			while(rs.next()){
				Map<String, String> rp_map = new HashMap<String, String>();
				rp_map.put("label", "("+rs.getString("loss_rate")+","+String.format("%.2f",rs.getDouble("num"))+"/"+year+")");
				rp_map.put("y", rs.getString("loss_rate"));
				rp_value.add(rp_map);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//data
		String sql_data = "SELECT rm.event_id,sum(loss_rate * expo."+sumfield+")/sum(expo."+sumfield+") as loss_rate "
				+ "FROM public.client_risk_rm_data_"+rtid+" rm, "
				+ "client_expo_policy_ans expo "
				+ "where rm.policy_id = expo.policy_id and rm.location_id = expo.location_id "
				+ "and rm.loss_rate > 0 "
				+ "and rm.event_id like '"+disaster_id+"%' "
				+ "and split_part(rm.event_id, '_',3)::date > expo."+starttime+" and split_part(rm.event_id, '_',3)::date < expo."+endtime+" "
				+ "and rm.event_id like '"+disaster_id+"%' "
				+ (where.isEmpty()?"":"and "+where+" ")
				+ "group by event_id ";
		System.out.println(sql_data);
		List<Map<String, Object>> data_list = new ArrayList<Map<String, Object>>();// 数据
		try {
			rs = conn.prepareStatement(sql_data).executeQuery();
			while(rs.next()){
				Map<String, Object> data_map = new HashMap<String, Object>();
				data_map.put("x", rs.getString("event_id"));
				data_map.put("y", rs.getString("loss_rate"));
				
				//赋属性
				Map<String, String> attributes = new HashMap<String, String>();
				attributes.put("event_id", rs.getString("event_id"));
				attributes.put("loss_rate", rs.getString("loss_rate"));
				data_map.put("attributes", attributes);
				
				data_list.add(data_map);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("record_type", rtid);
		result.put("year", year);
		result.put("disaster", disaster_id);
		result.put("rp", rp_value);
		result.put("data", data_list);
		
		return result;
	}

	//============================================================================================
	//============================================================================================
	
	//=======================================
	// 报表
	//=======================================
	public Map<String, String> queryReport(String sqlString)
	{
		Connection connection = DBUtils.getClientDBConnection();
		String htmlString = "";
		Map<String, String> map = new HashMap<String, String>();
		PreparedStatement preparedStatement;
		try {
			preparedStatement = connection
					.prepareStatement(sqlString);
			ResultSet rs = preparedStatement.executeQuery();
			
			rs.next();
			htmlString = rs.getString("html");

			if (sqlString != null) {
				map.put("htmlurl", CreateTempFile.CreateTempHtmlFile(htmlString));
			}
			preparedStatement.close();
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return map;
	}
	
	public JSONBean getReportParam(String type)
	{
		String sql = "";
		if(type.equalsIgnoreCase("actual")){
			sql = "select id,func_name,func_sql,param,param_name from client_report_actual order by func_name";
		}
		else if(type.equalsIgnoreCase("history")){
			sql = "select id,func_name,func_sql,param,param_name,create_time,create_person from client_report_history order by func_name,param_name,create_time";
		}

		JSONBean jsonBean = jsDao.queryTable(sql);
		return jsonBean;
	}
}
