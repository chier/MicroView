package her.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import her.bean.JSONBean;
import her.dao.JsonBeanDao;
import her.utils.DBUtils;

public class AutoActionDaoImpl {
	private JsonBeanDao jsDao;
	public AutoActionDaoImpl()
	{
		jsDao = new JsonBeanDao();
	}
	public void autoWritePolicyPerilIndex() throws SQLException
	{
		//提取保单表
		String policySql = "select policy_id,lon,lat from client_expo_policy_per_risk";
		JSONBean policyJsonBean = jsDao.queryTable(policySql);
		//提取危险性指标表
		String perilIndexSql = "select id,name_en,tablename,field from dic_peril_index";
		JSONBean perilIndexJsonBean = jsDao.queryTable(perilIndexSql);
		
		Connection conn = DBUtils.getConnection();
		
		@SuppressWarnings("unchecked")
		List<Map<String, String>> policy_data = (List<Map<String, String>>)((Map<String, Object>) policyJsonBean.getResult()).get("data");
		@SuppressWarnings("unchecked")
		List<Map<String, String>> perilIndex_data = (List<Map<String, String>>)((Map<String, Object>) perilIndexJsonBean.getResult()).get("data");
		int policyCount = policy_data.size();

		PreparedStatement policyPiPS= conn.prepareStatement("select * from client_expo_policy_peril_index");
		ResultSet policyPiRS = null;

		PreparedStatement piValuePS = null;
		ResultSet piValueRS = null;
		for(int i=0;i<policyCount;i++){//循环保单
			String policy_id = policy_data.get(i).get("policy_id");
			//查询保单危险性指标表中保单信息是否存在，存在删除
			String countSql = "select count(*) as count from client_expo_policy_peril_index where policy_id='"+policy_id+"'";
			int policypiCount = jsDao.queryCount(countSql);
			if(policypiCount > 0)//将原有记录删除
			{
				String deletePolicyPiSql = "delete from client_expo_policy_peril_index where policy_id='"+policy_id+"'";
				jsDao.deleteAction(deletePolicyPiSql);
			}
			
			policyPiRS = policyPiPS.executeQuery();
			int perilIndexCount = perilIndex_data.size();
			for (int j = 0; j < perilIndexCount; j++) {//循环危险性指标
				String colname = perilIndex_data.get(j).get("id");
				colname = "he"+colname;
				System.out.println(j+":"+colname);
				String pitable = perilIndex_data.get(j).get("tablename");
				String pifield = perilIndex_data.get(j).get("field");
				String lon = policy_data.get(i).get("lon");
				String lat = policy_data.get(i).get("lat");
//				policyPiRS.findColumn(colname) > 0
				if (jsDao.isExistColumn(policyPiRS, colname)) {
					
				}
				else
				{
					//保单危险性指标表中添加危险性指标字段
					String addColSql = "alter table client_expo_policy_peril_index add "+colname+" numeric";
					System.out.println(addColSql);
					PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(addColSql);
					pstmt.execute();
					pstmt.close();
				}
				
				//获取危险性指标值
				String pivalueSql = String.format("SELECT id,event_id,ST_Value(t.%s, foo.pt_geom) AS value"
						+" FROM %s t CROSS JOIN (SELECT ST_SetSRID(ST_Point(%s, %s), 4326) As pt_geom) As foo",
						pifield,pitable,lon,lat);
				System.out.println(pivalueSql);
				piValuePS = conn.prepareStatement(pivalueSql);
				piValueRS = piValuePS.executeQuery();
				
				while (piValueRS.next()) {//循环每个事件一个值
					String insertPISql = String.format("insert into client_expo_policy_peril_index (policy_id,event_id,%s) values('%s','%s',%s)",
							colname,policy_id,piValueRS.getString("event_id"),piValueRS.getString("value")
							);
					System.out.println(insertPISql);
					jsDao.insertAction(insertPISql, true);//写入保单危险性数据值
				}
			}
		}
		
		if(policyPiRS != null)policyPiRS.close();
		if(policyPiPS != null)policyPiPS.close();
		if(piValueRS != null)piValueRS.close();
		if(piValuePS != null)piValuePS.close();
		
	}
}
