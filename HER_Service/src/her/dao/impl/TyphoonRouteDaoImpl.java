package her.dao.impl;

import her.bean.JSONBean;
import her.bean.TyphoonPoint;
import her.bean.JSONBean.Msg;
import her.bean.JSONBean.Status;
import her.utils.DBUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;




public class TyphoonRouteDaoImpl {
	private List<TyphoonPoint> pointList;
	private JSONBean jsonBean;
	private String typ_id = "";
	private String sqlString;

	public TyphoonRouteDaoImpl(String typ_id){
		this.typ_id = typ_id;
		this.Init();
	}
	private void Init(){
		this.jsonBean = new JSONBean();
		this.sqlString = "";
		this.pointList = new ArrayList<TyphoonPoint>();
	}
	
	
	public JSONBean getJsonBean() {
		return jsonBean;
	}

	public List<TyphoonPoint> GetPointList() {
		return pointList;
	}

	public void setPointList(List<TyphoonPoint> pointList) {
		this.pointList = pointList;
	}



	public void ExecuteQuery() {
		if (this.typ_id == "") {
			this.jsonBean.setStatus(Status.FAILED);
			this.jsonBean.setMsg(Msg.PARAMETER_ERROR);
			return;
		}
		this.sqlString = String.format(
				"select %s from eve_typhoon_path where typ_id = '%s' ",
				TyphoonPoint.GetFieldsString(), this.typ_id);
		this.ExecuteSQL();
	}
	
	private void ExecuteSQL(){
		// 如果typ_id为空串则直接返回

		try {
			Connection connection = DBUtils.getConnection();
			Statement s = connection.createStatement();
			ResultSet rs = s.executeQuery(this.sqlString);
			while(rs.next()){
				TyphoonPoint point = new TyphoonPoint();
				
				point.setWkt(rs.getString("wkt"));
				point.setTyp_id(rs.getString("typ_id"));
				point.setRecord_time(rs.getString("record_time"));
				point.setCategory(rs.getString("category"));
				point.setMove_dir(rs.getString("move_dir"));			
				point.setMove_speed(rs.getString("move_speed"));
				point.setMax_windspeed(rs.getString("max_windspeed"));
				point.setAws(rs.getString("max_windspeed"));
				point.setMws(rs.getString("mws"));
				point.setRadius7(rs.getString("radius7"));
				point.setRadius10(rs.getString("radius10"));
				this.pointList.add(point);
			}
			s.close();
			connection.close();
			this.jsonBean.setStatus(Status.SUCCESS);
			this.jsonBean.setResult(this.pointList);
		} catch (SQLException e) {
			// TODO: handle exception
			this.jsonBean.setStatus(Status.FAILED);
			this.jsonBean.setMsg(Msg.EXECUTE_SQL_EXECPTION);
		}

	}
}
