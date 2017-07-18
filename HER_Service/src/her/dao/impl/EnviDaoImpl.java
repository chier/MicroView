package her.dao.impl;

import her.bean.JSONBean;
import her.dao.JsonBeanDao;
import her.utils.DBUtils;

public class EnviDaoImpl {
	private JsonBeanDao jsDao;
		
	public EnviDaoImpl()
	{
		jsDao = new JsonBeanDao();
	}
	
	/**
	 * @param client 是否使用 client库
	 */
	public EnviDaoImpl(Boolean client)
	{
		if(client)
		{
			jsDao = new JsonBeanDao(DBUtils.getClientDBConnection());
		}
		else {
			jsDao = new JsonBeanDao();
		}
	}

	public JSONBean getEnvironmentList(){
		String sqlString = "SELECT * FROM dic_envi_catalog ORDER BY classtype,name;";
		
		JSONBean jsonBean = jsDao.queryTable(sqlString);
		return jsonBean;
	}
	
	public JSONBean getTileInfos(String groupname){
		String sqlString = "SELECT * FROM dic_tiles_info where groupname='"+groupname+"'";
		
		JSONBean jsonBean = jsDao.queryTable(sqlString);
		return jsonBean;
	}

	public JSONBean queryTileByEXT(String tablename,String ext){
		String sqlString = String.format("select rid from %s t where ST_Intersects(ST_Transform(ST_GeomFromText('%s',4326),st_srid(rast)), t.rast)", tablename,ext);
		JSONBean jsonBean = jsDao.queryTable(sqlString);
		return jsonBean;
	}
	
	public JSONBean queryTileByEXT(String tablename,String xmin,String ymin,String xmax,String ymax){
		String sqlString = String.format("select rid from %s t where user_rect_intersects(%s,%s,%s,%s,extent_minx,extent_miny,extent_maxx,extent_maxy)", tablename,xmin,ymin,xmax,ymax);
		JSONBean jsonBean = jsDao.queryTable(sqlString);
		return jsonBean;
	}
	
	
}
