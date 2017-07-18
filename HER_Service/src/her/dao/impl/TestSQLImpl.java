package her.dao.impl;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import her.bean.JSONBean;
import her.bean.JSONBean.Status;
import her.utils.CreateTempFile;
import her.utils.DBUtils;
import her.utils.GlobalFuns;
import her.utils.CreateTempFile.ImageFormat;
import her.utils.GlobalVariable;

public class TestSQLImpl {
	private String sqlString;
	private JSONBean jsonBean;
	private String imageSession;
	
	public TestSQLImpl(String sqlString) {
		this.sqlString = GlobalFuns.dblingUse(sqlString);
		this.jsonBean = new JSONBean();
		this.imageSession = "";
	}

	public JSONBean getJsonBean() {
		return jsonBean;
	}
	public String getImageSession() {
		return imageSession;
	}
	public void Execute(String db) {
		Connection connection = null;
		if (db != null && db.equalsIgnoreCase("client")) {
			connection = DBUtils.getClientDBConnection();
		} else {
			connection = DBUtils.getConnection();
		}
		System.out.println(this.sqlString);
		try {
//			PreparedStatement preparedStatement = connection
//					.prepareStatement(this.sqlString);
			Statement statement = connection.createStatement();
			statement.executeQuery(this.sqlString);
			
			ResultSet rs = statement.getResultSet();// 查询结果数据集
			ResultSetMetaData resultSetMetaData = rs.getMetaData();
			Map<String, String> structureMap = new HashMap<String, String>();// 用来存储数据结构
			for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
				structureMap.put(resultSetMetaData.getColumnName(i + 1),
						resultSetMetaData.getColumnTypeName(i + 1));

			}
			List<Map<String, String>> data = new ArrayList<Map<String, String>>();// 数据

			while (rs.next()) {
				Map<String, String> map = new HashMap<String, String>();

				for (String s : structureMap.keySet()) {
					if (/*structureMap.get(s).equals("raster") || structureMap.get(s) == "geometry"|| */structureMap.get(s).equals("bytea")) {
						map.put(s, CreateTempFile.CreateTempImgFile(rs.getBytes(s), ImageFormat.PNG));
						this.imageSession = map.get(s);
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
			this.jsonBean.setResult(result);
			this.jsonBean.setStatus(Status.SUCCESS);
			statement.close();
			connection.close();

		} catch (SQLException e) {
			e.printStackTrace();
			this.jsonBean.setStatus(Status.FAILED);
			this.jsonBean.setMsg(e.getMessage());
		}
	}

	
	
	
	
}
