package her.dao;

import her.bean.JSONBean;
import her.bean.JSONBean.Status;
import her.utils.CreateTempFile;
import her.utils.DBUtils;
import her.utils.CreateTempFile.ImageFormat;
import her.utils.GlobalFuns;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jnr.ffi.Struct.int16_t;

import org.python.antlr.PythonParser.return_stmt_return;
import org.python.antlr.PythonParser.varargslist_return;

public class JsonBeanDao {
	private Connection connection = null;
	public JsonBeanDao(Connection conn)
	{
		connection = conn;
	}
	public JsonBeanDao()
	{
		connection = DBUtils.getConnection();
	}
	
	public int queryCount(String sql) {
		int count = 0;
		System.out.println(sql);
		try {
			PreparedStatement preparedStatement = connection
					.prepareStatement(sql);
			ResultSet rs = preparedStatement.executeQuery();
			rs.next();
			count = Integer.parseInt(rs.getString("count"));
			preparedStatement.close();
			connection.close();

		} catch (SQLException e) {

		}

		return count;
	}
	
	public int queryCount(String sql, boolean isclose) {
		int count = 0;
		try {
			PreparedStatement preparedStatement = connection
					.prepareStatement(sql);
			ResultSet rs = preparedStatement.executeQuery();
			rs.next();
			count = Integer.parseInt(rs.getString("count"));
			preparedStatement.close();
			if(isclose)
				connection.close();

		} catch (SQLException e) {

		}

		return count;
	}

	public JSONBean queryTable(String sql) {
		JSONBean jsonBean = new JSONBean();
		sql = sql.replace(";", "").trim();
		try {
//			String colname = "";
//			
//			String fieldString = sql.substring(sql.toLowerCase().indexOf("select")+6, sql.toLowerCase().indexOf(" from ")).toLowerCase();
//			String starStr = "";
//			if (fieldString.indexOf("*") != -1) {
//				String temp = fieldString.substring(0, fieldString.indexOf("*")+1);
//				if(temp.lastIndexOf(",") != -1){
//					starStr = temp.substring(temp.lastIndexOf(",")+1);
//				}
//				else {
//					starStr = temp.substring(temp.lastIndexOf(" ")+1);
//				}
//				
//				//查询字段，屏蔽rast字段
//				String metaSQL = "select * from " + sql.substring(sql.toLowerCase().indexOf(" from "));
//				metaSQL = sql.toLowerCase().substring(0, sql.lastIndexOf(" "));
//				if(metaSQL.endsWith("offset") || metaSQL.endsWith("limit")){
//					metaSQL = metaSQL.substring(0, metaSQL.lastIndexOf(" limit")) + " limit 1";
//				}
//				else {
//					metaSQL = sql + " limit 1";
//				}
//
//				PreparedStatement metaDStatement = connection.prepareStatement(metaSQL);
//				ResultSet metaDRS = metaDStatement.executeQuery();
//				ResultSetMetaData metaDRSmd = metaDRS.getMetaData();
//				int colcount = metaDRSmd.getColumnCount();// 取得全部列数
//				for (int i = 1; i <= colcount; i++) {
//					colname += metaDRSmd.getColumnName(i) + ",";// 取得全部列名
//				}
//				if(!colname.isEmpty())
//					colname = colname.substring(0, colname.length()-1);
//			}
//			sql = sql.replace(starStr, colname);
			
			System.out.println(sql);
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			ResultSet rs = preparedStatement.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int colcount = rsmd.getColumnCount();// 取得全部列数

			List<Map<String, String>> data = new ArrayList<Map<String, String>>();// 数据
			while (rs.next()) {
				Map<String, String> map = new HashMap<String, String>();
				for (int i = 1; i <= colcount; i++) {
					String colname = rsmd.getColumnName(i);// 取得全部列名
					map.put(colname, rs.getString(colname));
				}
				data.add(map);
			}
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("data", data);
			jsonBean.setResult(result);
			jsonBean.setStatus(Status.SUCCESS);
			preparedStatement.close();
			connection.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			jsonBean.setStatus(Status.FAILED);
			jsonBean.setMsg(e.getMessage());
		}

		return jsonBean;
	}
	
	public JSONBean getTableStructure(String sql) {
		JSONBean jsonBean = new JSONBean();

		System.out.println(sql);
		try {
			PreparedStatement preparedStatement = connection
					.prepareStatement(sql);
			ResultSetMetaData resultSetMetaData = preparedStatement
					.getMetaData();
			List<Map<String, String>> data = new ArrayList<Map<String, String>>();
			for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
				Map<String, String> structureMap = new HashMap<String, String>();
				structureMap.put("columnName", resultSetMetaData.getColumnName(i + 1));
				structureMap.put("columnType",resultSetMetaData.getColumnTypeName(i + 1));
				data.add(structureMap);
			}
 
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("data", data);
			jsonBean.setResult(result);
			jsonBean.setStatus(Status.SUCCESS);
			preparedStatement.close();
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			jsonBean.setStatus(Status.FAILED);
			jsonBean.setMsg(e.getMessage());
		}

		return jsonBean;
	}
	
	public List<Map<String, String>> getTableStructure_list(String sql) throws SQLException {
		List<Map<String, String>> data = null;
		PreparedStatement preparedStatement = null;
		System.out.println(sql);
		try {
			preparedStatement = connection.prepareStatement(sql);
			ResultSetMetaData resultSetMetaData = preparedStatement
					.getMetaData();
			data = new ArrayList<Map<String, String>>();
			for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
				Map<String, String> structureMap = new HashMap<String, String>();
				structureMap.put("columnName", resultSetMetaData.getColumnName(i + 1));
				structureMap.put("columnType",resultSetMetaData.getColumnTypeName(i + 1));
				data.add(structureMap);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
		}
		finally{
			preparedStatement.close();
			connection.close();
		}
		return data;
	}
	
	public List<Map<String, String>> getTableStructure_list(String sql, Boolean isclose) throws SQLException {
		List<Map<String, String>> data = null;
		PreparedStatement preparedStatement = null;
		System.out.println(sql);
		try {
			preparedStatement = connection.prepareStatement(sql);
			ResultSetMetaData resultSetMetaData = preparedStatement
					.getMetaData();
			data = new ArrayList<Map<String, String>>();
			for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
				Map<String, String> structureMap = new HashMap<String, String>();
				structureMap.put("columnName", resultSetMetaData.getColumnName(i + 1));
				structureMap.put("columnType",resultSetMetaData.getColumnTypeName(i + 1));
				data.add(structureMap);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
		}
		finally{
			preparedStatement.close();
			if(isclose)
				connection.close();
		}
		return data;
	}

	public JSONBean queryTableWithColumnInfo(String field, String from, String where,String other,String excludeFields) {
		JSONBean jsonBean = new JSONBean();

		System.out.println("select "+field+" from "+from+" where "+where+other);
		try {
			PreparedStatement distinctPS = null;
			ResultSet distinctRS = null;
			PreparedStatement preparedStatement = connection
					.prepareStatement("select "+field+" from "+from+" where "+where+other);
			ResultSet rs = preparedStatement.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int colcount = rsmd.getColumnCount();// 取得全部列数
			List<Map<String, String>> data = new ArrayList<Map<String, String>>();// 数据
			List<Map<String, Object>> columns = new ArrayList<Map<String, Object>>();// 数据
			for (int i = 1; i <= colcount; i++) {
				String colname = rsmd.getColumnName(i);// 取得全部列名
				if(excludeFields.indexOf(colname.toLowerCase()) != -1)
				{
					continue;
				}
				Map<String, Object> column = new HashMap<String, Object>();
				//				Map<String, String> type = new HashMap<String, String>();
				//				Map<String, String> distinct = new HashMap<String, String>();
				column.put("field", colname);
				column.put("type", getTypeString(rsmd.getColumnType(i)));
				if(column.get("type").toString().equalsIgnoreCase("text")
						||column.get("type").toString().equalsIgnoreCase("date"))
				{
					String colfield = colname;
					String field_as = field + ",";//后面加","用于拆分，每个字段独立格式“ as field,”
					if(field_as.indexOf(" as " + colname + ",") != -1)
					{
						String temp = field_as.substring(0, field_as.indexOf(" as " + colname + ","));
						if(temp.lastIndexOf(",") != -1)
						{
							colfield = temp.substring(temp.lastIndexOf(",")+1);
						}
						else
						{
							colfield = temp.substring(temp.lastIndexOf(" "));
						}
					}
					System.out.println("select distinct "+colfield+" from "+from+" where "+where);
					distinctPS = connection.prepareStatement(
							"select distinct "+colfield+" from "+from+" where "+where,
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_UPDATABLE);
					distinctRS = distinctPS.executeQuery();
					if(distinctRS.last())
					{
						int rowCount = distinctRS.getRow();
						if(rowCount < 50)
						{
							List<String> dsList = new ArrayList<String>();
							distinctRS.first();
							do
							{
								dsList.add(distinctRS.getString(1));
							}
							while (distinctRS.next());
							column.put("distinct", dsList);
						}
						else {
							distinctRS.close();
							distinctPS.close();
						}
					}
					
				}

				columns.add(column);
			}
			while (rs.next()) {
				Map<String, String> map = new HashMap<String, String>();
				for (int i = 1; i <= colcount; i++) {
					String colname = rsmd.getColumnName(i);// 取得全部列名
					if(excludeFields.indexOf(colname) != -1)
					{
						continue;
					}
					map.put(colname, rs.getString(colname));
				}

				data.add(map);
			}
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("data", data);
			result.put("columns", columns);
			jsonBean.setResult(result);
			String nolimintOther = other.substring(0, other.lastIndexOf("limit"));
			jsonBean.setDataCount(queryCount("select count(*) from "+from+" where "+where+nolimintOther));
			jsonBean.setStatus(Status.SUCCESS);
			
			if(distinctRS!=null)distinctRS.close();
			if(distinctPS!=null)distinctPS.close();
			rs.close();
			preparedStatement.close();
			connection.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			jsonBean.setStatus(Status.FAILED);
			jsonBean.setMsg(e.getMessage());
		}

		return jsonBean;
	}
	
	public JSONBean queryTableWithColumnInfo2(String field, String from, String where,String other,String excludeFields) {
		JSONBean jsonBean = new JSONBean();
		String sql = "select " + field + " from " + from + " where " + where + other;
		sql = GlobalFuns.dblingUse(sql);
		System.out.println(sql);
		try {
//			PreparedStatement distinctPS = null;
//			ResultSet distinctRS = null;
			PreparedStatement preparedStatement = connection
					.prepareStatement(sql);
			ResultSet rs = preparedStatement.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int colcount = rsmd.getColumnCount();// 取得全部列数
			List<Map<String, String>> data = new ArrayList<Map<String, String>>();// 数据
			List<Map<String, Object>> columns = new ArrayList<Map<String, Object>>();// 数据
			for (int i = 1; i <= colcount; i++) {
				String colname = rsmd.getColumnName(i);// 取得全部列名
				if(excludeFields.indexOf(colname.toLowerCase()) != -1)
				{
					continue;
				}
				Map<String, Object> column = new HashMap<String, Object>();
				//				Map<String, String> type = new HashMap<String, String>();
				//				Map<String, String> distinct = new HashMap<String, String>();
				column.put("field", colname);
				column.put("type", getTypeString(rsmd.getColumnType(i)));

				columns.add(column);
			}
//			int count = 0;
			while (rs.next()) {
				Map<String, String> map = new HashMap<String, String>();
				for (int i = 1; i <= colcount; i++) {
					String colname = rsmd.getColumnName(i);// 取得全部列名
					if(excludeFields.indexOf(colname) != -1)
					{
						continue;
					}
					map.put(colname, rs.getString(colname));
				}

				data.add(map);
//				count++;
			}
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("data", data);
			result.put("columns", columns);
			jsonBean.setResult(result);
			String nolimintOther = "";
			if(other.toLowerCase().lastIndexOf(" limit ") != -1){
				nolimintOther = other.substring(0, other.toLowerCase().lastIndexOf(" limit "));
			}
			else{
				nolimintOther = other;
			}
			if(nolimintOther.toLowerCase().lastIndexOf(" order by ") != -1){
				nolimintOther = nolimintOther.substring(0, nolimintOther.toLowerCase().lastIndexOf(" order by "));
			}
			jsonBean.setDataCount(queryCount("select count(*) from "+from+" where "+where+nolimintOther));
//			jsonBean.setDataCount(count);
			jsonBean.setStatus(Status.SUCCESS);
			
//			if(distinctRS!=null) distinctRS.close();
//			if(distinctPS!=null) distinctPS.close();
			rs.close();
			preparedStatement.close();
			connection.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			jsonBean.setStatus(Status.FAILED);
			jsonBean.setMsg(e.getMessage());
		}

		return jsonBean;
	}
	
	public JSONBean queryTableWithColumnInfo3(String field, String from, String where,String other,String excludeTypes) {
		JSONBean jsonBean = new JSONBean();
		String sql = "select " + field + " from " + from + " where " + where + other;
		sql = GlobalFuns.dblingUse(sql);
		System.out.println(sql);
		try {
			PreparedStatement preparedStatement = connection
					.prepareStatement(sql);
			ResultSet rs = preparedStatement.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int colcount = rsmd.getColumnCount();// 取得全部列数
			List<Map<String, String>> data = new ArrayList<Map<String, String>>();// 数据
			List<Map<String, Object>> columns = new ArrayList<Map<String, Object>>();// 数据
			String fieldString = "";
			for (int i = 1; i <= colcount; i++) {
				String colname = rsmd.getColumnName(i);// 取得全部列名
				String coltype = rsmd.getColumnTypeName(i);// 取得列类型
				if(excludeTypes.indexOf(coltype.toLowerCase()) != -1)
				{
					continue;
				}
				Map<String, Object> column = new HashMap<String, Object>();
				fieldString += colname + ",";
				column.put("field", colname);
				column.put("type", coltype);

				columns.add(column);
			}

			while (rs.next()) {
				Map<String, String> map = new HashMap<String, String>();
				for (int i = 1; i <= colcount; i++) {
					String colname = rsmd.getColumnName(i);// 取得全部列名
					if(fieldString.contains(colname + ","))
					{
						map.put(colname, rs.getString(colname));
					}
				}

				data.add(map);
			}
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("data", data);
			result.put("columns", columns);
			jsonBean.setResult(result);
			String nolimintOther = "";
			if(other.toLowerCase().lastIndexOf(" limit ") != -1){
				nolimintOther = other.substring(0, other.toLowerCase().lastIndexOf(" limit "));
			}
			else{
				nolimintOther = other;
			}
			if(nolimintOther.toLowerCase().lastIndexOf(" order by ") != -1){
				nolimintOther = nolimintOther.substring(0, nolimintOther.toLowerCase().lastIndexOf(" order by "));
			}
			jsonBean.setDataCount(queryCount("select count(*) from "+from+" where "+where+nolimintOther));
			jsonBean.setStatus(Status.SUCCESS);
			
			rs.close();
			preparedStatement.close();
			connection.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			jsonBean.setStatus(Status.FAILED);
			jsonBean.setMsg(e.getMessage());
		}

		return jsonBean;
	}

	public JSONBean queryTableWithSQL(String sql, String excludeFields)
	{
		JSONBean jsonBean = new JSONBean();
		sql = GlobalFuns.dblingUse(sql);
		System.out.println(sql);
		try {
			PreparedStatement preparedStatement = connection
					.prepareStatement(sql);
			ResultSet rs = preparedStatement.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int colcount = rsmd.getColumnCount();// 取得全部列数
			List<Map<String, String>> data = new ArrayList<Map<String, String>>();// 数据
			List<Map<String, Object>> columns = new ArrayList<Map<String, Object>>();// 数据
			for (int i = 1; i <= colcount; i++) {
				String colname = rsmd.getColumnName(i);// 取得全部列名
				if(excludeFields.indexOf(colname.toLowerCase()) != -1)
				{
					continue;
				}
				Map<String, Object> column = new HashMap<String, Object>();
				//				Map<String, String> type = new HashMap<String, String>();
				//				Map<String, String> distinct = new HashMap<String, String>();
				column.put("field", colname);
				column.put("type", getTypeString(rsmd.getColumnType(i)));

				columns.add(column);
			}
			int count = 0;
			while (rs.next()) {
				Map<String, String> map = new HashMap<String, String>();
				for (int i = 1; i <= colcount; i++) {
					String colname = rsmd.getColumnName(i);// 取得全部列名
					if(excludeFields.indexOf(colname) != -1)
					{
						continue;
					}
					map.put(colname, rs.getString(colname));
				}

				data.add(map);
				count++;
			}
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("data", data);
			result.put("columns", columns);
			jsonBean.setResult(result);
			jsonBean.setDataCount(count);
			jsonBean.setStatus(Status.SUCCESS);

			rs.close();
			preparedStatement.close();
			connection.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			jsonBean.setStatus(Status.FAILED);
			jsonBean.setMsg(e.getMessage());
		}

		return jsonBean;
	}
	
	private String getTypeString(int typeid)
	{
		String rString = "";
		switch (typeid) {
		case Types.VARCHAR:
		case Types.CHAR:
		case Types.LONGNVARCHAR:
		case Types.LONGVARCHAR:
		case Types.NVARCHAR:
			rString = "text";
			break;
		case Types.DECIMAL:
		case Types.DOUBLE:
		case Types.FLOAT:
		case Types.INTEGER:
		case Types.NUMERIC:
		case Types.SMALLINT:
			rString = "number";
			break;
		case Types.DATE:
		case Types.TIME:
//		case Types.TIME_WITH_TIMEZONE:  // zzl
		case Types.TIMESTAMP:
//		case Types.TIMESTAMP_WITH_TIMEZONE:  // zzl
			rString = "date";
			break;
		default:
			rString = "object";
			break;
		}
		return rString;
	}
	
	public Map<String, String> getTableColumnTypeMapping(String tablename)
	{
		if(tablename.contains(".")){
			tablename = tablename.substring(tablename.indexOf(".")+1);
		}
		String sql = "SELECT schemaname,relname,attname,typname "
				+ "FROM pg_class C,pg_attribute A,pg_type T,pg_tables Ta "
				+ "WHERE C .oid = attrelid AND atttypid = T .oid AND attnum > 0 AND ta.tablename = relname "
				+ "AND relname = '"+tablename+"'";
		Map<String, String> structureMap = new HashMap<String, String>();// 用来存储数据结构
		try {
			Statement statement = connection.createStatement();
			statement.executeQuery(sql);
			ResultSet rs = statement.getResultSet();
			
			while (rs.next()) {
				structureMap.put(
						rs.getString("attname"),
						rs.getString("typname")
						);
			}
			statement.close();
			connection.close();
		} catch (Exception e) {
			try {
				connection.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return structureMap;
	}

	public JSONBean queryRasterTable(String sql) {
		JSONBean jsonBean = new JSONBean();
		String imageSession = "";

		System.out.println(sql);
		try {
			PreparedStatement preparedStatement = connection
					.prepareStatement(sql);
			Statement statement = connection.createStatement();
			statement.executeQuery(sql);
			ResultSetMetaData resultSetMetaData = preparedStatement
					.getMetaData();
			ResultSet rs = statement.getResultSet();// 查询结果数据集

			Map<String, String> structureMap = new HashMap<String, String>();// 用来存储数据结构
			for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
				structureMap.put(resultSetMetaData.getColumnName(i + 1),
						resultSetMetaData.getColumnTypeName(i + 1));

			}
			List<Map<String, String>> data = new ArrayList<Map<String, String>>();// 数据

			while (rs.next()) {
				Map<String, String> map = new HashMap<String, String>();

				for (String s : structureMap.keySet()) {
					if (/*
					 * structureMap.get(s).equals("raster") ||
					 * structureMap.get(s) == "geometry"||
					 */structureMap.get(s).equals("bytea")) {
						map.put(s, CreateTempFile.CreateTempImgFile(
								rs.getBytes(s), ImageFormat.PNG));
						imageSession = map.get(s);
					}// 不输出原始的raster和geometry类型
					else if (!structureMap.get(s).equals("raster")
							&& !structureMap.get(s).equals("geometry")) {
						map.put(s, rs.getString(s));
					}
				}
				data.add(map);

			}
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("structure", structureMap);
			result.put("data", data);
			jsonBean.setResult(result);
			jsonBean.setStatus(Status.SUCCESS);
			jsonBean.setImageSession(imageSession);
			statement.close();
			preparedStatement.close();
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			jsonBean.setStatus(Status.FAILED);
			jsonBean.setMsg(e.getMessage());
		}
		System.out.println(jsonBean.toJSONString());
		return jsonBean;
	}


	/**
	 * @param sql "update students set Age='Age' where Name='Name'"
	 * @return
	 */
	public int updateAction(String sql, boolean closeConnect) {
		int i = 0;
		// String sql = "update students set Age='" + student.getAge() + "' where Name='" + student.getName() + "'";
		PreparedStatement pstmt;
		try {
			System.out.println(sql);
			pstmt = (PreparedStatement) connection.prepareStatement(sql);
			// pstmt.setString(1, student.getName());
			// pstmt.setString(2, student.getSex());
			// pstmt.setString(3, student.getAge());
			i = pstmt.executeUpdate();
			pstmt.close();
			if(closeConnect)
				connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return i;
	}

	/**
	 * @param sql "insert into students (Name,Sex,Age) values(?,?,?)"
	 * @return
	 */
	public int insertAction(String sql, boolean closeConnect) {
		int i = 0;
		// String sql = "insert into students (Name,Sex,Age) values(?,?,?)";
		PreparedStatement pstmt;
		try {
			pstmt = (PreparedStatement) connection.prepareStatement(sql);
			i = pstmt.executeUpdate();
			System.out.println("resutl: " + i);
			pstmt.close();
			if(closeConnect)
				connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return i;
	}
	
	/**
	 * 该方法要求插入的数据表中含有id主键字段
	 * @param sql "insert into students (Name,Sex,Age) values(?,?,?)"
	 * @return 新插入数据的id号
	 */
	public int insertActionHasID(String sql, boolean closeConnect) {
		int id = -1;
		// String sql = "insert into students (Name,Sex,Age) values(?,?,?)";
		Statement ps = null;
		try {
			System.out.println(sql.concat(" returning id"));
			ps = connection.createStatement();
			ResultSet rs = ps.executeQuery(sql.concat(" returning id"));
			if(rs.next())
			{
				id = rs.getInt(1);//获取新加入的id值
			}
			
			System.out.println("result row_id: " + id);
			ps.close();
			if(closeConnect)
				connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;
	}

	/**
	 * @param sql "delete from students where Name='name'"
	 * @return
	 */
	public int deleteAction(String sql){
		int i = 0;
		//	    String sql = "delete from students where Name='" + name + "'";
		PreparedStatement pstmt;
		try {
			pstmt = (PreparedStatement) connection.prepareStatement(sql);
			i = pstmt.executeUpdate();
			System.out.println("resutl: " + i);
			pstmt.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return i;
	}
	
	/**
	 * @param sql "delete from students where Name='name'"
	 * @param closeConnect 是否执行完毕之后关闭链接
	 * @return
	 */
	public int deleteAction(String sql, boolean closeConnect){
		int i = 0;
		//	    String sql = "delete from students where Name='" + name + "'";
		PreparedStatement pstmt;
		try {
			pstmt = (PreparedStatement) connection.prepareStatement(sql);
			i = pstmt.executeUpdate();
			System.out.println("resutl: " + i);
			pstmt.close();
			if(closeConnect)
				connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return i;
	}
	
	/**
	 * 判断查询结果集中是否存在某列
	 * @param rs 查询结果集
	 * @param columnName 列名
	 * @return true 存在; false 不存咋
	 */
	public boolean isExistColumn(ResultSet rs, String columnName) {
		try {
			if (rs.findColumn(columnName) > 0 ) {
				return true;
			} 
		}
		catch (SQLException e) {
			return false;
		}

		return false;
	}
	
	
//	public String CheckGeomID(String table) throws SQLException
//	{
//		String tableSQL = table;
//		PreparedStatement preparedStatement = null;
//		System.out.println("select * from " + table);
//		try {
//			preparedStatement = connection.prepareStatement("select * from " + table + " limit 1");
//			ResultSetMetaData resultSetMetaData = preparedStatement
//					.getMetaData();//dic_geom_info
//			for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
//				if("geom_id".equalsIgnoreCase(resultSetMetaData.getColumnName(i + 1))){
//					tableSQL = String.format("(select t1.*,t2.geom from %s t1 left join dic_geom_info t2 on t1.geom_id=t2.geom_id)", table);
//				    break;
//				}
//			}
//
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//		}
//		finally{
//			preparedStatement.close();
//		}
//		return tableSQL;
//	}
}
