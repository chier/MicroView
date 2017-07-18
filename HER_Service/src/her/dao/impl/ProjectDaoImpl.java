package her.dao.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import her.bean.JSONBean;
import her.bean.JSONBean.Status;
import her.dao.JsonBeanDao;
import her.utils.DBUtils;

public class ProjectDaoImpl {
	private JsonBeanDao jsDao;
		
	public ProjectDaoImpl()
	{
		jsDao = new JsonBeanDao();
	}
	
	/**
	 * @param client 是否使用 client库
	 */
	public ProjectDaoImpl(Boolean client)
	{
		if(client)
		{
			jsDao = new JsonBeanDao(DBUtils.getClientDBConnection());
		}
		else {
			jsDao = new JsonBeanDao();
		}
	}

	public JSONBean getProjectList(String pagenum){
		String sqlString = "SELECT *,ROW_NUMBER() OVER() num FROM client_project limit 10 offset "+Integer.valueOf(pagenum)*10+";";
		int rowCount= jsDao.queryCount("SELECT count(*) FROM client_project", false);
		JSONBean jsonBean = jsDao.queryTable(sqlString);
		jsonBean.setDataCount(rowCount);
		return jsonBean;
	}
	
	public JSONBean getProjectListByKey(String key, String pagenum){
		String sqlString = "SELECT *,ROW_NUMBER() OVER() num FROM client_project where pname like '%"+key+"%' limit 10 offset "+Integer.valueOf(pagenum)*10+";";
		int rowCount= jsDao.queryCount("SELECT count(*) FROM client_project", false);
		JSONBean jsonBean = jsDao.queryTable(sqlString);
		jsonBean.setDataCount(rowCount);
		return jsonBean;
	}
	
	public JSONBean getProjectData(String id){
		String sqlString = "select * from client_project_data d where pid = "+id+"";
		
		JSONBean jsonBean = jsDao.queryTable(sqlString);
		return jsonBean;
	}

	public JSONBean queryProjectsByKey(String key, String pagenum){
		String sqlString = "select t.*,d.* from client_project t left join client_project_data d on t.id=d.pid where t.pname like '%"+key+"%' limit 10 offset "+pagenum+"";
		int rowCount= jsDao.queryCount("select t.*,d.* from client_project t left join client_project_data d on t.id=d.pid where t.pname like '%"+key+"%'", false);
		JSONBean jsonBean = jsDao.queryTable(sqlString);
		jsonBean.setDataCount(rowCount);
		return jsonBean;
	}
	public JSONBean saveProject(
			String id, 
			String pname, 
			String pcreateman, 
			String pdesc, 
			JSONArray layers) throws SQLException, JSONException{
		JSONBean jsonBean = new JSONBean();
		Statement ps = null;
		Connection conn = null;

		conn = DBUtils.getClientDBConnection(); // 调用链接数据库的文件
		conn.setAutoCommit(false);
		ps = conn.createStatement();
		if("-1".equalsIgnoreCase(id)){
			//数据库中记录
			JsonBeanDao dao = new JsonBeanDao(DBUtils.getClientDBConnection());
			int project_id = dao.insertActionHasID(String.format("insert into client_project(pname, pcreatetime, pcreateman, pdesc) values('%s', '%s', '%s', '%s')", pname,"Now()",pcreateman,pdesc), false);
			id = Integer.toString(project_id);
		}
		else {
			ps.executeUpdate(String.format("UPDATE client_project SET pname='%s', pcreatetime='%s', pcreateman='%s', pdesc='%s' WHERE id=%s",pname,"Now()",pcreateman,pdesc,id));
			conn.commit();
		}
		
		ps.executeUpdate("DELETE FROM client_project_data WHERE pid="+id);
		conn.commit();
		int len = layers.length();
		for (int i = 0; i < len; i++) {
//			String starttime = ((JSONObject)layers.get(i)).getString("starttime");
//			String endtime = ((JSONObject)layers.get(i)).getString("endtime");
			String insertSQL = String.format("insert into client_project_data(pid, layername, layerid, layertype, businesstype, datasql, timefield, groupname,colormap_style,layersql,attributesql) values(%s, '%s', '%s', '%s','%s', '%s', '%s', '%s', '%s', '%s', '%s');", 
					id,
					((JSONObject)layers.get(i)).getString("layername").replaceAll("'", "''"),
					((JSONObject)layers.get(i)).getString("layerid").replaceAll("'", "''"),
					((JSONObject)layers.get(i)).getString("layertype").replaceAll("'", "''"),
					((JSONObject)layers.get(i)).getString("businesstype").replaceAll("'", "''"),
					((JSONObject)layers.get(i)).getString("datasql").replaceAll("'", "''"),
					((JSONObject)layers.get(i)).getString("timefield").replaceAll("'", "''"),
					((JSONObject)layers.get(i)).has("groupname")?((JSONObject)layers.get(i)).getString("groupname").replaceAll("'", "''"):"",
					((JSONObject)layers.get(i)).has("colormap_style")?((JSONObject)layers.get(i)).getString("colormap_style").replaceAll("'", "''"):"",
					((JSONObject)layers.get(i)).has("layersql")?((JSONObject)layers.get(i)).getString("layersql").replaceAll("'", "''"):"",
					((JSONObject)layers.get(i)).has("attributesql")?((JSONObject)layers.get(i)).getString("attributesql").replaceAll("'", "''"):""
					);
			System.out.println(insertSQL);
			ps.executeUpdate(insertSQL);
		}
		conn.commit();
		
		jsonBean.setResult(id);
		jsonBean.setStatus(Status.SUCCESS);
		
		ps.close();
		conn.close();
		
		return jsonBean;
	}
	
	public JSONBean deleteProject(String id){
		JSONBean jsonBean = new JSONBean();
		String sql = "delete from client_project where id=" + id;
		int r_id = jsDao.deleteAction(sql);
		jsonBean.setResult(Integer.toString(r_id));
		jsonBean.setStatus(Status.SUCCESS);
		return jsonBean;
	}

	public JSONBean queryProjectByUserID(String userID){
		
		JSONBean jsonBean = null;
		return jsonBean;
	}

	public JSONBean getSQLStructure(String sql) {
		JSONBean jsonBean = null;
		jsonBean = jsDao.getTableStructure(sql);
		
		return jsonBean;
	}
	
}
