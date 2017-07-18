package her.dao.impl;

import her.bean.JSONBean;
import her.bean.TyphoonRecord;
import her.bean.JSONBean.Msg;
import her.bean.JSONBean.Status;
import her.utils.DBUtils;

import java.sql.ResultSet;

import java.sql.Statement;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;



public class TyphoonListDaoImpl {
	//用来存放台风信息的列表
	private List<TyphoonRecord> list ;
	//sql语句
	private String sqlString;
	//自定义JSONBean对象，对象构建和返回json串
	private JSONBean jsonBean;
	
	//初始化函数
	public TyphoonListDaoImpl(){
		this.list = new  ArrayList<TyphoonRecord>();
		this.sqlString = "";
		this.jsonBean = new JSONBean();
	}
	
	
	public JSONBean getJsonBean() {
		return jsonBean;
	}

	private void ExecuteSQL() {
		Connection conn = DBUtils.getConnection();
		try {
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(this.sqlString);
			while (rs.next()) {
				TyphoonRecord tr = new TyphoonRecord();
				tr.setDisaster_id(rs.getString("disaster_id"));
				tr.setTyp_id(rs.getString("typ_id"));
				tr.setInt_num(rs.getString("int_num"));
				tr.setChina_num(rs.getString("china_num"));
				tr.setTcn(rs.getString("tcn"));
				tr.setStart_time(rs.getString("start_time"));
				tr.setEnd_time(rs.getString("end_time"));
				tr.setLandfall_category(rs.getString("landfall_category"));
				 tr.setLandfall_time(rs.getString("landfall_time") ==
				 null?"null":rs.getString("landfall_time"));
				tr.setLandfall_cp(rs.getString("landfall_cp"));
				this.list.add(tr);
			}
			statement.close();
			conn.close();
			this.jsonBean.setStatus(Status.SUCCESS);
			this.jsonBean.setResult(this.list);
		} catch (Exception e) {
			// TODO: handle exception
			this.jsonBean.setStatus(Status.FAILED);
			this.jsonBean.setMsg(Msg.EXECUTE_SQL_EXECPTION);

		}
	}

	/*
	 * 1代表通过英文名进行查询2代表通过中文名进行查询3代表通过时间进行查询4代表通过中国编号进行查询5代表通过国际编号进行查询6通过台风id进行查询
	 */
	public void ExecuteQuery(int method, String param) {
		switch (method) {
		case 1:
			this.GetListByNameEn(param);
			break;
		case 2:
			this.GetListByNameCn(param);
			break;
		case 3:
			this.GetListByDate(param);
			break;
		case 4:
			this.GetListByChianNum(param);
			break;
		case 5:
			this.GetListByIntNum(param);
			break;
		case 6:
			this.GetListByTypId(param);
			break;
		default:
			break;
		}
	}

	// 通过英文名获得列表
	private void GetListByNameEn(String name_en) {

		this.sqlString = String.format(
				"select * from eve_typhoon_record where name_en = '%s'",
				name_en);
		this.ExecuteSQL();
	}

	// 通过中文名获得列表
	private void GetListByNameCn(String name_cn) {

		this.sqlString = String.format(
				"select * from eve_typhoon_record where name_cn = '%s'",
				name_cn);
		this.ExecuteSQL();
	}

	// 通过中国编号时行查询
	private void GetListByChianNum(String china_num) {
		this.sqlString = String.format(
				"select * from eve_typhoon_record where china_num = '%s'",
				china_num);
		this.ExecuteSQL();
	}

	// 通过国际编号时行查询
	private void GetListByIntNum(String int_num) {
		this.sqlString = String.format(
				"select * from eve_typhoon_record where int_num = '%s'",
				int_num);
		this.ExecuteSQL();
	}

	// 通过台风id时行查询
	private void GetListByTypId(String typ_id) {
		this.sqlString = String.format(
				"select * from eve_typhoon_record where typ_id = '%s'", typ_id);
		this.ExecuteSQL();
	}

	private void GetListByDate(String date) {
		this.sqlString = String
				.format("select * from eve_typhoon_record where date_trunc('day',start_time) = timestamp'%s' or date_trunc('day',end_time) = timestamp'%s'",
						date, date);
		this.ExecuteSQL();
	}

}
