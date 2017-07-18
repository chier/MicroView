package her.dao.impl;

import her.bean.JSONBean;
import her.bean.JSONBean.Status;
import her.utils.DBUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdentifyDaoImpl {
	private JSONBean jsonBean;

	public IdentifyDaoImpl() {
		this.jsonBean = new JSONBean();
	}

	public JSONBean getJsonBean() {
		return jsonBean;
	}

	/**
	 * sql图层Identify标识查询
	 * @param name
	 * @param type
	 * @param sqlTable
	 * @param lon
	 * @param lat
	 */
	public void sqlLayerIdentExecute(String name, String type, String sqlTable,
			String lon, String lat) {
		Connection connection = DBUtils.getConnection();
		//3785 Web墨卡托坐标 ; 4326 WGS84
		
		String sqlString = "SELECT "+(sqlTable.indexOf("rid ")!=-1?(sqlTable.indexOf("as rid")==-1?"rid,":"t.rid,"):"")
				+" "+(sqlTable.indexOf("time ")!=-1?(sqlTable.indexOf("as time")==-1?"record_time,":"t.time,"):"")
				+" ST_Value(t.rast, foo.pt_geom) As b1pval"
				+" FROM ("+sqlTable+") t CROSS JOIN (SELECT ST_SetSRID(ST_Point("+lon+", "+lat+"), 3785) As pt_geom) As foo";
		System.out.println(sqlString);
		try {
			PreparedStatement preparedStatement = connection
					.prepareStatement(sqlString);
			ResultSet rs = preparedStatement.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int colcount = rsmd.getColumnCount();//取得全部列数
			
			List<Map<String, String>> data = new ArrayList<Map<String, String>>();// 数据

			while (rs.next()) {
				Map<String, String> map = new HashMap<String, String>();
				for(int i=1;i<=colcount;i++){
					String colname = rsmd.getColumnName(i);//取得全部列名
					map.put(colname, rs.getString(colname));
				}

				data.add(map);
			}
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("name", name);
			result.put("data", data);
			this.jsonBean.setResult(result);
			this.jsonBean.setStatus(Status.SUCCESS);
			preparedStatement.close();
			connection.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			this.jsonBean.setStatus(Status.FAILED);
			this.jsonBean.setMsg(e.getMessage());
		}
	}
	/**
	 * sql图层Identify标识查询
	 * @param name
	 * @param type
	 * @param sqlTable
	 * @param lon
	 * @param lat
	 */
	public void sqlLayerIdentify(String rastField, String sqlTable, String lon, String lat) 
	{
		Connection connection = DBUtils.getConnection();
		//3785 Web墨卡托坐标 ; 4326 WGS84
		
		String sqlString = "SELECT"
				+" ST_Value(t."+rastField+", foo.pt_geom) As b1pval,t.*"
				+" FROM ("+sqlTable+") t CROSS JOIN (SELECT ST_SetSRID(ST_Point("+lon+", "+lat+"), 4326) As pt_geom) As foo";
		System.out.println(sqlString);
		try {
			PreparedStatement preparedStatement = connection
					.prepareStatement(sqlString);
			ResultSet rs = preparedStatement.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int colcount = rsmd.getColumnCount();//取得全部列数
			
			List<Map<String, String>> data = new ArrayList<Map<String, String>>();// 数据

			while (rs.next()) {
				Map<String, String> map = new HashMap<String, String>();
				for(int i=1;i<=colcount;i++){
					if(rsmd.getColumnTypeName(i).equals("raster") 
					  || rsmd.getColumnTypeName(i).equals("geometry")
					  || rsmd.getColumnTypeName(i).equals("bytea"))
					{
						continue;
					}
					String colname = rsmd.getColumnName(i);//取得列名
					map.put(colname, rs.getString(colname));
				}

				data.add(map);
			}
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("data", data);
			this.jsonBean.setResult(result);
			this.jsonBean.setStatus(Status.SUCCESS);
			preparedStatement.close();
			connection.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			this.jsonBean.setStatus(Status.FAILED);
			this.jsonBean.setMsg(e.getMessage());
		}
	}
	/**
	 * 累加数据图层Identify标识查询
	 * @param lon
	 * @param lat
	 */
	public void policyAggrIdentExecute(String lon, String lat) {
		Connection connection = DBUtils.getConnection();
		String sqlTable = "select p.id,p.code,p.zone,p.premium,c.geom from policy_aggr_test p,county_400w c where p.code=c.code";
		String sqlString = "SELECT t.id,t.code,t.zone,t.premium,ST_AsGeoJSON(t.geom) as geojson"
				+" FROM ("+sqlTable+") t"
		+"  WHERE 'POINT("+lon+" "+lat+")'::geometry=t.geom or ST_Intersects('POINT("+lon+" "+lat+")'::geometry, t.geom)";
		System.out.println(sqlString);
		try {
			PreparedStatement preparedStatement = connection
					.prepareStatement(sqlString);
			ResultSet rs = preparedStatement.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int colcount = rsmd.getColumnCount();//取得全部列数
			
			List<Map<String, String>> data = new ArrayList<Map<String, String>>();// 数据

			while (rs.next()) {
				Map<String, String> map = new HashMap<String, String>();
				for(int i=1;i<=colcount;i++){
					String colname = rsmd.getColumnName(i);//取得全部列名
					map.put(colname, rs.getString(colname));
				}

				data.add(map);
			}
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("name", "");
			result.put("data", data);
			this.jsonBean.setResult(result);
			this.jsonBean.setStatus(Status.SUCCESS);
			preparedStatement.close();
			connection.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			this.jsonBean.setStatus(Status.FAILED);
			this.jsonBean.setMsg(e.getMessage());
		}
	}
	
}
