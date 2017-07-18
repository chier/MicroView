package her.dao.impl;

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
import her.dao.JsonBeanDao;
import her.utils.CreateTempFile;
import her.utils.CreateTempFile.ImageFormat;

public class HazardDaoImpl {
	private JsonBeanDao jsDao;
	public HazardDaoImpl()
	{
		jsDao = new JsonBeanDao();
	}

	public JSONBean queryAllDisasterType(){
		String sqlString = "select dt.*,dc.name_cn as disaster_type,dc.name_en as disaster_type_en,dc.detail_table from (select c.disaster_type_id from her_disaster_event_catelog c group by c.disaster_type_id) dt,dic_disaster_type dc where dt.disaster_type_id=dc.id";

		JSONBean jsonBean = jsDao.queryTable(sqlString);
		return jsonBean;
	}

	public JSONBean queryEventByType(String disastertypeid, String topnum){
		String sqlString = "select c.* from her_disaster_event_catelog c where c.disaster_type_id="+disastertypeid
				+ " order by begin_time desc";
		if(topnum != null && topnum != "" && topnum != "0")
			sqlString += " limit " + topnum;

		JSONBean jsonBean = jsDao.queryTable(sqlString);
		String countSql = "select count(*) as count from her_disaster_event_catelog c where c.disaster_type_id="+disastertypeid;
		jsonBean.setDataCount(jsDao.queryCount(countSql));
		return jsonBean;
	}
	
	public JSONBean queryEventByTypePage(String disastertypeid, String startRow, String count) {
		String sqlString = "select c.* from her_disaster_event_catelog c where c.disaster_type_id=" + disastertypeid
				+ " order by begin_time desc"
				+ " limit " + count + " offset " + startRow;
		JSONBean jsonBean = jsDao.queryTable(sqlString);
		String countSql = "select count(*) as count from her_disaster_event_catelog c where c.disaster_type_id="+disastertypeid;
		jsonBean.setDataCount(jsDao.queryCount(countSql));
		return jsonBean;
	}
	
	public JSONBean queryDisasterHazardMapping(String disastertypeid)
	{
		String sqlString = "select c.* from her_disaster_hazards_mapping c where c.disaster_type_id="+disastertypeid;
		JSONBean jsonBean = jsDao.queryTable(sqlString);

		return jsonBean;
	}
	
	public JSONBean getDetailsInfo(String eventids, String tablename, String fields)
	{
		String sqlString = "select "+fields+" from "+tablename+" where event_id in ("+eventids+")";
		JSONBean jsonBean = jsDao.queryTable(sqlString);

		return jsonBean;
	}
	
	public JSONBean queryhazardinfo(String hazardtable, String hazardfieldtype, String starttime, String endtime, String renderersql)
	{
		String sqlString = "select *"+(renderersql==""?"":(","+renderersql))+" from "+hazardtable+" where record_time between '"+starttime+"' and '"+endtime+"'";
		JSONBean jsonBean = null;
		if(hazardfieldtype.equalsIgnoreCase("raster"))
		{
			jsonBean = jsDao.queryRasterTable(sqlString);
		}
		else {
			jsonBean = jsDao.queryTable(sqlString);
		}
		
		return jsonBean;
	}
	
	public JSONBean queryTestBean(String fields, String from, String where, String other) {
		JSONBean jsonBean = null;
		String excludeFields = "rast,geom";//多个字段用,隔开
//		jsonBean = jsDao.queryTableWithColumnInfo(fields, from, where, other, excludeFields);
		jsonBean = jsDao.queryTableWithColumnInfo2(fields, from, where, other, excludeFields);
		return jsonBean;
	}
	
	public JSONBean queryHazardData(String fields, String from, String where, String other) {
		JSONBean jsonBean = null;
		String excludeFields = "rast,geom";//多个字段用,隔开
		jsonBean = jsDao.queryTableWithColumnInfo2(fields, from, where, other, excludeFields);
		
		return jsonBean;
	}
	
	public JSONBean translation_disaster(String disasterids)
	{
		JSONBean jsonBean = null;
		String sqlString = "";
		sqlString = "select * from dic_disaster_type where disaster_id in ("+disasterids+")";
		jsonBean = jsDao.queryTable(sqlString);
		return jsonBean;
	}
	
	public List<Map<String, String>> getSQLStructure(String sql)
	{
		List<Map<String, String>> columnInfos = null;
		try {
			columnInfos = jsDao.getTableStructure_list(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return columnInfos;
	}
}
