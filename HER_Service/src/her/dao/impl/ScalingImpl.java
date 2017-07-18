package her.dao.impl;

import her.bean.JSONBean;
import her.bean.JSONBean.Status;
import her.utils.CreateTempFile;
import her.utils.DBUtils;
import her.utils.CreateTempFile.ImageFormat;

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

import org.python.antlr.PythonParser.else_clause_return;
import org.python.antlr.PythonParser.return_stmt_return;

public class ScalingImpl {
	private JSONBean jsonBean;
	private String imageSession;
	
	public ScalingImpl() {
		this.jsonBean = new JSONBean();
		this.imageSession = "";
	}

	public JSONBean getJsonBean() {
		return jsonBean;
	}
	public String getImageSession() {
		return imageSession;
	}
	
	/**
	 * @param policy_sql
	 * @param action
	 */
	public JSONBean upScaling(String policy_sql,String action)
	{
		if(action.equalsIgnoreCase("lonlat"))
		{
			
		}
		else if(action.equalsIgnoreCase("code"))
		{
			String to_raster_sql = "select st_asraster(st_setsrid(c.geom,4326),r.rast,'64BF',c.value) as rast from ("+policy_sql+") c,grid_10_test r where r.rid=1";
			System.out.println(to_raster_sql);
			String algebra_sql = "select ST_MapAlgebra(t1.rast,1,t2.rast,1,'[rast1]*[rast2]') as rast from ("+to_raster_sql+") t1,grid_10_test t2 where t2.rid=1";
			System.out.println(algebra_sql);
			String sql = "SELECT ST_AsPNG(ST_ColorMap(r.rast,1,"
					+"'100%  248  2      255   255\n"
					+" 80%    0      0       253  255\n"
					+" 50%    95    179  249   255\n"
					+" 30%    62    185  58     255\n"
					+" 20%    166  241  141  255\n"
					+"  0%     166  241  141  225\n"
					+"  nv       255 255 255  0')) as render_rast, user_extent(r.rast) as extent "
					+"  FROM (select st_transform(st_union(t.rast),3785) as rast from ("+algebra_sql+") t) r";
			Connection connection = DBUtils.getConnection();
			try {
				PreparedStatement preparedStatement = connection
						.prepareStatement(sql);
				Statement statement = connection.createStatement();
				System.out.println(sql);
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
						if (/*structureMap.get(s).equals("raster") || structureMap.get(s) == "geometry"|| */structureMap.get(s).equals("bytea")) {
							map.put(s, CreateTempFile.CreateTempImgFile(rs.getBytes(s), ImageFormat.PNG));
							this.imageSession = map.get(s);
						}//如果是raster或者geometry或者bytea类型，则转换成base64编码
						else{
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
				// TODO Auto-generated catch block
				this.jsonBean.setStatus(Status.FAILED);
				this.jsonBean.setMsg(e.getMessage());
			}
		}
		
		return jsonBean;
	}
}
