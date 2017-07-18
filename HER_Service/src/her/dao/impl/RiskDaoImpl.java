package her.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import her.bean.JSONBean;
import her.bean.JSONBean.Status;
import her.dao.JavaPythonDao;
import her.dao.JsonBeanDao;
import her.model.InsertException;
import her.utils.DBUtils;
import her.utils.GlobalFuns;
import her.utils.GlobalVariable;

public class RiskDaoImpl {
	private JsonBeanDao jsDao;

	public RiskDaoImpl() {
		jsDao = new JsonBeanDao();
	}

	/**
	 * @param client 是否使用 client库
	 */
	public RiskDaoImpl(Boolean client)
	{
		if(client)
		{
			jsDao = new JsonBeanDao(DBUtils.getClientDBConnection());
		}
		else {
			jsDao = new JsonBeanDao();
		}
	}

	public JSONBean getFunctionList() {
		String sqlString = "select f.*,p.name_en as peril from her_risk_func f,dic_peril p where f.peril_id=p.id";
		JSONBean jsonBean = jsDao.queryTable(sqlString);
		// System.out.println(sqlString);
		// try {
		// PreparedStatement preparedStatement = connection
		// .prepareStatement(sqlString);
		// ResultSet rs = preparedStatement.executeQuery();
		// ResultSetMetaData rsmd = rs.getMetaData();
		// int colcount = rsmd.getColumnCount();//取得全部列数
		//
		// List<Map<String, String>> data = new ArrayList<Map<String,
		// String>>();// 数据
		//
		// while (rs.next()) {
		// Map<String, String> map = new HashMap<String, String>();
		// for(int i=1;i<=colcount;i++){
		// String colname = rsmd.getColumnName(i);//取得全部列名
		// map.put(colname, rs.getString(colname));
		// }
		//
		// data.add(map);
		// }
		// Map<String, Object> result = new HashMap<String, Object>();
		// result.put("data", data);
		// jsonBean.setResult(result);
		// jsonBean.setStatus(Status.SUCCESS);
		// preparedStatement.close();
		// connection.close();
		//
		// } catch (SQLException e) {
		// jsonBean.setStatus(Status.FAILED);
		// jsonBean.setMsg(e.getMessage());
		// }

		return jsonBean;
	}

	// //older
	// public JSONBean checkFunction(String argString, String argNum) {
	// String sqlString = "select f.*"
	// +"  from"
	// +"    (select f.id from her_risk_func f,her_risk_func_characteristic a,her_risk_func_char_mapping m "
	// +"      where f.id=m.fun_id and a.id=m.char_id and f.arg_num="+argNum+" and a.id in ("+argString+") "
	// +"      group by f.id having count(*) = "+argNum+") t,her_expo_fun f"
	// +"  where t.id=f.id";
	//
	// JSONBean jsonBean = jsDao.queryTable(sqlString);
	//
	// return jsonBean;
	// }

	// 根据易损性属性参数选择函数
	public JSONBean checkFunction_(String charString, String peril) {
		String sqlString = "select f.*,p.name_en as peril"
				+ "  from"
				+ "  (select f.*,string_agg(to_char(m.char_id,'FM9999'),',') as char_ids from her_risk_func f,(select * from her_risk_func_char_mapping order by char_id desc) m where f.id=m.func_id group by f.id) fm, dic_peril_type p"
				+ "  where fm.char_ids = '" + charString + "' and p.name_en='"
				+ peril + "'";

		JSONBean jsonBean = jsDao.queryTable(sqlString);

		return jsonBean;
	}

	public JSONBean getFunctionListWithCharacteristic_() {
		String sqlString = "select f.*,string_agg(to_char(m.char_id,'FM9999'),',') as char_ids from her_risk_func f,(select * from her_risk_func_char_mapping order by char_id desc) m where f.id=m.func_id group by f.id";
		JSONBean jsonBean = jsDao.queryTable(sqlString);

		return jsonBean;
	}

	public JSONBean getFunctionCharacteristic_() {
		String sqlString = "select * from her_risk_func_characteristic";
		JSONBean jsonBean = jsDao.queryTable(sqlString);

		return jsonBean;
	}

	public JSONBean queryFunctionCharacteristic_(String fields) {
		String sqlString = "select "
				+ fields
				+ ",p.name_en as peril,per.peril_id from client_expo_policy_per_risk per,dic_peril p "
				+ " where per.peril_id=p.id " + " group by " + fields
				+ ",p.name_en,per.peril_id";

		JSONBean jsonBean = jsDao.queryTable(sqlString);

		return jsonBean;
	}

	public JSONBean getFunctionCharacteristicHeader() {
		String sqlString = "select distinct property_field from her_risk_func_characteristic";
		JSONBean jsonBean = jsDao.queryTable(sqlString);

		return jsonBean;
	}

	public JSONBean getFunctionCharacteristic() {
		JSONBean jsonBean = new JSONBean();
		// String sqlString =
		// "select distinct property_field from her_risk_func_characteristic";
		// JSONBean jsonBean = jsDao.queryTable(sqlString);
		// @SuppressWarnings("unchecked")
		// List<Map<String, String>> head_field = (List<Map<String,
		// String>>)((Map<String, Object>) jsonBean.getResult()).get("data");
		// int colcount = head_field.size();
		// List<String> head_map = new ArrayList<String>();
		// for(int i=0;i<colcount;i++){
		// head_map.add(i, head_field.get(i).get("property_field"));
		// }
		List<Map<String, String>> new_t = new ArrayList<Map<String, String>>();
		String sql = "select * from her_risk_func_characteristic order by func_id";
		JSONBean jsonBean_t = jsDao.queryTable(sql);
		@SuppressWarnings("unchecked")
		List<Map<String, String>> whole_t = (List<Map<String, String>>) ((Map<String, Object>) jsonBean_t
				.getResult()).get("data");
		int count = whole_t.size();
		String current_func_id = "";

		Map<String, String> map = null;
		for (int i = 0; i < count; i++) {
			if (!current_func_id
					.equalsIgnoreCase(whole_t.get(i).get("func_id"))) {
				current_func_id = whole_t.get(i).get("func_id");
				if (map != null)
					new_t.add(map);
				map = new HashMap<String, String>();
				map.put("func_id", whole_t.get(i).get("func_id"));
				map.put(whole_t.get(i).get("property_field"), whole_t.get(i)
						.get("property"));
			} else {
				map.put(whole_t.get(i).get("property_field"), whole_t.get(i)
						.get("property"));
			}
		}
		new_t.add(map);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("data", new_t);
		jsonBean.setResult(result);
		jsonBean.setStatus(Status.SUCCESS);

		return jsonBean;
	}

	public JSONBean getEventList() {
		String sqlString = "select event_id,name_cat as event from her_disaster_event_catelog";
		JSONBean jsonBean = jsDao.queryTable(sqlString);

		return jsonBean;
	}

	public JSONBean calculatePolicy(String dataJsonStr, String fieldJsonStr,
			List<Map<String, String>> event_list) {
		JSONBean jsonResultBean = new JSONBean();
		List<Map<String, String>> data = new ArrayList<Map<String, String>>();// 数据
		try {
			JSONArray field_dataArray = new JSONArray(fieldJsonStr);
			JSONObject dataJsonObj = new JSONObject(dataJsonStr);
			if (((String) dataJsonObj.get("action"))
					.equalsIgnoreCase("querypolicybytree")) {
				String corp = dataJsonObj.has("corp") ? (String) dataJsonObj
						.get("corp") : "";
						String corp_branch = dataJsonObj.has("corp_branch") ? (String) dataJsonObj
								.get("corp_branch") : "";

								String year_uw = dataJsonObj.has("year_uw") ? (String) dataJsonObj
										.get("year_uw") : "";

										ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl();
										// 获取所要计算的保单
										JSONBean jsonPolicyBean = exposureDaoImpl.queryPolicyByTreeAll(
												corp, corp_branch, year_uw);
										@SuppressWarnings("unchecked")
										List<Map<String, String>> policy_data = (List<Map<String, String>>) ((Map<String, Object>) jsonPolicyBean
												.getResult()).get("data");

										int colcount = policy_data.size();
										for (int i = 0; i < colcount; i++) {// 循环保单
											Map<String, String> policy_map = policy_data.get(i);

											String func_id = "";

											for (int j = 0; j < field_dataArray.length(); j++) {// 循环函数
												JSONObject jobj = (JSONObject) field_dataArray.get(j);// 易损性属性
												String str_field = "";
												String str_policy = "";
												@SuppressWarnings("rawtypes")
												Iterator it = jobj.keys();
												while (it.hasNext()) {
													String key = it.next().toString();
													if (!key.equalsIgnoreCase("func")
															&& !key.equalsIgnoreCase("func_sql")
															&& !key.equalsIgnoreCase("func_id")
															&& !key.equalsIgnoreCase("loss_rate_table")
															&& !key.equalsIgnoreCase("peril")
															&& !key.equalsIgnoreCase("peril_id")) {
														str_field += jobj.has(key) ? String
																.valueOf(jobj.get(key)) : "null";
																str_policy += String.valueOf(policy_map.get(key
																		.toLowerCase()));
													}
												}

												if (str_field.toLowerCase().equalsIgnoreCase(
														str_policy.toLowerCase())) {
													// data.add(calculateLossRate(policy_map,
													// (String)jobj.get("func_id"), event_map));
													func_id = (String) jobj.get("func_id");
													break;
												}

												// Connection connection = DBUtils.getConnection();
												// String sql_func =
												// "select f.id as func_id,f.arg_num,f.func_sql,loss_rate_table,i.* "
												// +" from her_risk_func_peril_mapping m,dic_peril_index i,her_risk_func f "
												// +" where m.func_id = "+funcID+" and m.peril_index_id=i.id and m.func_id=f.id";
												//
												// String sql_policy =
												// "select policy_id,sum_insured,premium,lon,lat,location "
												// +" from client_expo_policy_per_risk "
												// +" where policy_id = "+funcID+" ";
												// System.out.println(sql_func);
												// System.out.println(sql_policy);
											}

											int eventcount = event_list.size();
											for (int e = 0; e < eventcount; e++) {// 循环事件
												Map<String, String> event_map = event_list.get(e);
												if (func_id.isEmpty()) {
													Map<String, String> new_policy = new HashMap<String, String>();
													new_policy.putAll(policy_map);// 复制
													new_policy.put("event_id",
															event_map.get("event_id"));
													new_policy.put("event", event_map.get("event"));
													new_policy.put("func_id", "--");
													new_policy.put("lossrate", "--");
													new_policy.put("loss", "--");
													data.add(new_policy);
												} else {
													data.add(calculateLossRate(policy_map, func_id,
															event_map));
												}
											}
										}
			}

			Map<String, Object> result = new HashMap<String, Object>();
			result.put("data", data);
			jsonResultBean.setResult(result);
			jsonResultBean.setStatus(Status.SUCCESS);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			jsonResultBean.setStatus(Status.FAILED);
			jsonResultBean.setMsg(e.getMessage());
		}

		return jsonResultBean;
	}

	private Map<String, String> calculateLossRate(Map<String, String> policy,
			String func_id, Map<String, String> eventmap) {
		Map<String, String> result = new HashMap<String, String>();
		Connection connection = DBUtils.getConnection();

		String sql = "select * from her_risk_func where id=" + func_id;

		try {
			PreparedStatement preparedStatement = connection
					.prepareStatement(sql);
			ResultSet rs = preparedStatement.executeQuery();

			if (rs.next()) {
				String loss_rate_table = rs.getString("loss_rate_table");
				if (loss_rate_table != null && !loss_rate_table.isEmpty()) {
					// String getValueSql =
					// "SELECT id,ST_Value(t.rast, foo.pt_geom) AS lossrate,event_id,event"
					// +" FROM (select l.id,l.event_id,c.name_cat as event,ST_Transform(l.rast,4326) as rast from "+loss_rate_table+" l,her_disaster_event_catelog c where l.event_id=c.event_id) t CROSS JOIN (SELECT ST_SetSRID(ST_Point("+policy.get("lon")+", "+policy.get("lat")+"), 4326) As pt_geom) As foo";
					// // +" WHERE event_id='"+event_id+"'";

					// System.out.println(getValueSql);
					PreparedStatement ps_value = null;
					ResultSet rs_value = null;
					String getValueSql = "SELECT id,ST_Value(t.rast, foo.pt_geom) AS lossrate,event_id"
							+ " FROM (select l.id,l.event_id,ST_Transform(l.rast,4326) as rast from "
							+ loss_rate_table
							+ " l) t CROSS JOIN (SELECT ST_SetSRID(ST_Point("
							+ policy.get("lon")
							+ ", "
							+ policy.get("lat")
							+ "), 4326) As pt_geom) As foo"
							+ " WHERE event_id='"
							+ eventmap.get("event_id")
							+ "'";
					ps_value = connection.prepareStatement(getValueSql);
					rs_value = ps_value.executeQuery();
					if (rs_value.next()) {
						result.putAll(policy);// 复制
						result.put("event_id", rs_value.getString("event_id"));
						result.put("event", eventmap.get("event"));
						result.put("func_id", func_id);
						result.put(
								"lossrate",
								String.format("%.4s",
										rs_value.getString("lossrate")));
						double premium = result.containsKey("premium") ? Double
								.parseDouble(result.get("premium")) : 0;
								double lossrate = rs_value.getString("lossrate") != null ? Double
										.parseDouble(rs_value.getString("lossrate"))
										: 0;
										result.put("loss",
												String.format("%.4f", premium * lossrate));
					}

					// while (rs_value.next()) {
					// Map<String, String> new_policy = new HashMap<String,
					// String>();
					// new_policy.putAll(policy);//复制
					// new_policy.put("event_id",
					// rs_value.getString("event_id"));
					// new_policy.put("event", rs_value.getString("event"));
					// new_policy.put("func_id", func_id);
					// new_policy.put("lossrate", String.format("%.4f",
					// rs_value.getString("lossrate")));
					// String premium = new_policy.get("premium");
					// String lossrate = new_policy.get("lossrate");
					// new_policy.put("loss", String.format("%.4f",
					// Double.parseDouble(premium==null?"0":premium)*Double.parseDouble(lossrate==null?"0":lossrate)));
					// result.add(new_policy);
					// }
					ps_value.close();
					rs_value.close();
				}

			}

			preparedStatement.close();
			rs.close();
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	public JSONBean calculatePolicyByExp(String dataJsonStr, String fieldJsonStr)
			throws JSONException, SQLException {
		JSONBean jsonResultBean = new JSONBean();// 返回数据
		Map<String, String> funcMap = new HashMap<String, String>();// 函数字典
		List<Map<String, String>> data = new ArrayList<Map<String, String>>();// 数据
		Connection connection = DBUtils.getConnection();
		JavaPythonDao pythonDao = new JavaPythonDao();

		String sql_func = "select * from her_risk_func";

		PreparedStatement ps_func = connection.prepareStatement(sql_func);
		ResultSet rs_func = ps_func.executeQuery();
		while (rs_func.next()) {// 录入函数字典
			funcMap.put(rs_func.getString("id"), rs_func.getString("func_exp"));
		}
		rs_func.close();
		ps_func.close();

		PreparedStatement ps_pi = null;
		ResultSet rs_pi = null;
		try {
			JSONArray field_dataArray = new JSONArray(fieldJsonStr);
			JSONObject dataJsonObj = new JSONObject(dataJsonStr);
			if (((String) dataJsonObj.get("action"))
					.equalsIgnoreCase("querypolicybytree")) {
				String corp = dataJsonObj.has("corp") ? (String) dataJsonObj
						.get("corp") : "";
						String corp_branch = dataJsonObj.has("corp_branch") ? (String) dataJsonObj
								.get("corp_branch") : "";
								String year_uw = dataJsonObj.has("year_uw") ? (String) dataJsonObj
										.get("year_uw") : "";

										String policySql = "select p.* " + " from"
												+ " client_expo_policy_per_risk p";
										String whereString = "1 = 1";
										if (corp != null && !corp.isEmpty()) {
											whereString += " and p.corp='" + corp + "'";
										}
										if (corp_branch != null && !corp_branch.isEmpty()) {
											whereString += " and p.corp_branch='" + corp_branch + "'";
										}
										if (year_uw != null && !year_uw.isEmpty()) {
											whereString += " and p.year_uw=" + year_uw;
										}

										if (!whereString.isEmpty()) {
											policySql = policySql + " where " + whereString;
										} else {
											policySql = policySql + " where 1=0";
										}

										JSONBean policyJsonBean = jsDao.queryTable(policySql);
										@SuppressWarnings("unchecked")
										List<Map<String, String>> policy_data = (List<Map<String, String>>) ((Map<String, Object>) policyJsonBean
												.getResult()).get("data");

										int colcount = policy_data.size();
										for (int i = 0; i < colcount; i++) {// 循环保单
											Map<String, String> policy_map = policy_data.get(i);
											String func_id = "";
											int func_count = field_dataArray.length();
											for (int j = 0; j < func_count; j++) {// 循环函数
												JSONObject jobj = (JSONObject) field_dataArray.get(j);// 易损性属性
												String str_field = "";// 函数易损性属性
												String str_policy = "";// 保单易损性属性
												@SuppressWarnings("rawtypes")
												Iterator it = jobj.keys();
												while (it.hasNext()) {
													String key = it.next().toString();
													if (!key.equalsIgnoreCase("func")
															&& !key.equalsIgnoreCase("func_sql")
															&& !key.equalsIgnoreCase("func_id")
															&& !key.equalsIgnoreCase("loss_rate_table")
															&& !key.equalsIgnoreCase("peril")
															&& !key.equalsIgnoreCase("peril_id")) {
														str_field += jobj.has(key) ? String
																.valueOf(jobj.get(key)) : "null";
																str_policy += String.valueOf(policy_map.get(key
																		.toLowerCase()));
													}
												}

												if (str_field.toLowerCase().equalsIgnoreCase(
														str_policy.toLowerCase()))// 判断匹配函数
												{
													func_id = (String) jobj.get("func_id");
													break;
												}

											}
											String policyPISql = String
													.format("select pi.*,c.name_cat as event from client_expo_policy_peril_index pi,her_disaster_event_catelog c where pi.event_id=c.event_id and pi.policy_id='%s'",
															policy_map.get("policy_id"));
											//					System.out.println(policyPISql);
											ps_pi = connection.prepareStatement(policyPISql);
											rs_pi = ps_pi.executeQuery();
											ResultSetMetaData piMetaData = rs_pi.getMetaData();
											int columnCount = piMetaData.getColumnCount();
											while (rs_pi.next()) {
												Map<String, String> new_policy = new HashMap<String, String>();
												Map<String, Object> pi_mapMap = new HashMap<String, Object>();
												new_policy.putAll(policy_map);// 复制
												for (int j = 1; j <= columnCount; j++) {
													if (piMetaData.getColumnLabel(j).equalsIgnoreCase(
															"event"))
														continue;
													if(piMetaData.getColumnType(j) == Types.DOUBLE
															||piMetaData.getColumnType(j) == Types.FLOAT
															||piMetaData.getColumnType(j) == Types.INTEGER
															||piMetaData.getColumnType(j) == Types.NUMERIC
															||piMetaData.getColumnType(j) == Types.SMALLINT
															)//数值类型数据均转换为double类型
													{
														pi_mapMap.put(piMetaData.getColumnLabel(j), rs_pi.getString(j)==null?"null":rs_pi
																.getDouble(piMetaData.getColumnLabel(j)));
													}
													else {
														pi_mapMap.put(piMetaData.getColumnLabel(j), rs_pi
																.getString(piMetaData.getColumnLabel(j)));
													}
												}
												new_policy.put("event_id", rs_pi.getString("event_id"));
												new_policy.put("event", rs_pi.getString("event"));
												new_policy.put("func_id", func_id);

												if (funcMap.get(func_id) == null
														|| funcMap.get(func_id).isEmpty()) {
													new_policy.put("lossrate", "--");
													new_policy.put("loss", "--");
												} else {
													double premium = new_policy.containsKey("premium") ? Double
															.parseDouble(new_policy.get("premium")) : 0;
															JSONObject jsonObj = new JSONObject(pi_mapMap);
															String jsonString = jsonObj.toString();

															System.out.println(jsonString);
															System.out.println(funcMap.get(func_id));
															String lossrateStr = pythonDao.mathExp(jsonString,
																	funcMap.get(func_id));
															new_policy.put("lossrate", lossrateStr
																	.equalsIgnoreCase("--") ? lossrateStr
																			: String.format("%.4s", lossrateStr));
															new_policy
															.put("loss",
																	lossrateStr.equalsIgnoreCase("--") ? lossrateStr
																			: String.format(
																					"%.4f",
																					premium
																					* Double.parseDouble(lossrateStr)));
												}

												data.add(new_policy);
											}
										}

			}
			rs_pi.close();
			ps_pi.close();
			connection.close();

			Map<String, Object> result = new HashMap<String, Object>();
			result.put("data", data);
			jsonResultBean.setResult(result);
			jsonResultBean.setStatus(Status.SUCCESS);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			rs_pi.close();
			ps_pi.close();
			connection.close();

			jsonResultBean.setStatus(Status.FAILED);
			jsonResultBean.setMsg(e.getMessage());
		}

		return jsonResultBean;
	}

	//==============================================================================
	// 分库后代码
	//==============================================================================
	public JSONBean getQueryCondition_rl(){
		String sqlString = "select * from client_risk_rl_query_condition order by rank";
		JSONBean jsonBean = jsDao.queryTable(sqlString);
		return jsonBean;
	}

	public JSONBean getQueryCondition_rh(){
		String sqlString = "select * from client_risk_rh_query_condition order by rank";
		JSONBean jsonBean = jsDao.queryTable(sqlString);
		return jsonBean;
	}

	public JSONBean getQueryCondition_rm(){
		String sqlString = "select * from client_risk_rm_query_condition order by rank";
		JSONBean jsonBean = jsDao.queryTable(sqlString);
		return jsonBean;
	}

	public JSONBean getQueryConditionItemColumn_rl(){
		String sqlString = "select * from client_risk_rl_index_table";
		JSONBean jsonBean = jsDao.getTableStructure(sqlString);
		return jsonBean;
	}

	public JSONBean getQueryConditionItemColumn_rh(){
		String sqlString = "select * from client_risk_rh_index_table";
		JSONBean jsonBean = jsDao.getTableStructure(sqlString);
		return jsonBean;
	}

	public JSONBean getQueryConditionItemColumn_rm(){
		String sqlString = "select * from client_risk_rm_index_table";
		JSONBean jsonBean = jsDao.getTableStructure(sqlString);
		return jsonBean;
	}

	//her_query_condition_item_r
	public JSONBean getQueryConditionItem_rl(String retrunField, String whereField){
		String sqlString = String.format("select distinct %s from client_risk_rl_index_table where 1=1 %s", retrunField, whereField);
		JSONBean jsonBean = jsDao.queryTable(sqlString);
		return jsonBean;
	}

	public JSONBean getQueryConditionItem_rh(String retrunField, String whereField){
		String sqlString = String.format("select distinct %s from client_risk_rh_index_table where 1=1 %s", retrunField, whereField);
		JSONBean jsonBean = jsDao.queryTable(sqlString);
		return jsonBean;
	}

	public JSONBean getQueryConditionItem_rm(String retrunField, String whereField){
		String sqlString = String.format("select distinct %s from client_risk_rm_index_table where 1=1 %s", retrunField, whereField);
		JSONBean jsonBean = jsDao.queryTable(sqlString);
		return jsonBean;
	}

	public JSONBean writeQueryCondition_rl(String values){
		jsDao.deleteAction("DELETE FROM client_risk_rl_query_condition", false);
		String sqlString = String.format("insert into client_risk_rl_query_condition (field, field_alias, condition_col, required, rank, multi, default_show, condition_col_type) values%s", values);
		System.out.println(sqlString);
		int r = jsDao.insertAction(sqlString, true);
		JSONBean jsonBean = new JSONBean();
		if(r != -1){
			jsonBean.setStatus(Status.SUCCESS);
		}
		else{
			jsonBean.setStatus(Status.FAILED);
		}
		return jsonBean;
	}

	public JSONBean writeQueryCondition_rh(String values){
		jsDao.deleteAction("DELETE FROM client_risk_rh_query_condition", false);
		String sqlString = String.format("insert into client_risk_rh_query_condition (field, field_alias, condition_col, required, rank, multi, default_show, condition_col_type) values%s", values);
		System.out.println(sqlString);
		int r = jsDao.insertAction(sqlString, true);
		JSONBean jsonBean = new JSONBean();
		if(r != -1){
			jsonBean.setStatus(Status.SUCCESS);
		}
		else{
			jsonBean.setStatus(Status.FAILED);
		}
		return jsonBean;
	}

	public JSONBean writeQueryCondition_rm(String values){
		jsDao.deleteAction("DELETE FROM client_risk_rm_query_condition", false);
		String sqlString = String.format("insert into client_risk_rm_query_condition (field, field_alias, condition_col, required, rank, multi, default_show, condition_col_type) values%s", values);
		System.out.println(sqlString);
		int r = jsDao.insertAction(sqlString, true);
		JSONBean jsonBean = new JSONBean();
		if(r != -1){
			jsonBean.setStatus(Status.SUCCESS);
		}
		else{
			jsonBean.setStatus(Status.FAILED);
		}
		return jsonBean;
	}

	public JSONBean querySQLSelectBean(String fields, String from, String where, String other) {
		JSONBean jsonBean = null;
		String excludeFields = "rast,location,geom";//多个字段用,隔开
		//		jsonBean = jsDao.queryTableWithColumnInfo(fields, from, where, other, excludeFields);
		jsonBean = jsDao.queryTableWithColumnInfo2(fields, from, where, other, excludeFields);

		return jsonBean;
	}

	public JSONBean querySQLSelectBean_lh(String fields, String from, String where, String other) throws SQLException {
		JSONBean jsonBean = null;
		String excludeFields = "rast,location,geom";//多个字段用,隔开
		String rl_table = from;

		String rl_core_table = GlobalFuns.findCoreTable2(from);
		String rh_table = "client_risk_rh_data" + rl_core_table.substring(rl_core_table.lastIndexOf("_"));
		String e_table = "client_expo_policy" + rl_core_table.substring(rl_core_table.lastIndexOf("_"));
		String rl_field = "";
		String rh_field = "";
		String e_field = "";

		boolean rh_exist = true;
		
		Connection conn = DBUtils.getClientDBConnection();
		
		ResultSet rse = null;
		//判断各个表是否存在
		rse = conn.getMetaData().getTables(null, null, rh_table, null);
		if (rse.next()){
			//Table exist 
			rh_exist = true;
		}
		else {
			rh_exist = false;
		}
				
		String sql = "select column_name,table_name from information_schema.columns "
				+ " WHERE table_schema='public' "
				+ " and table_name in ('"+rl_core_table+"','"+rh_table+"','"+e_table+"')";

		ResultSet rs = conn.prepareStatement(sql).executeQuery();
		while (rs.next()) {
			if(rl_core_table.equalsIgnoreCase(rs.getString("table_name"))){
				rl_field += "rl." + rs.getString("column_name") + " as rl_" + rs.getString("column_name") + ",";
			}
			else if(rh_table.equalsIgnoreCase(rs.getString("table_name"))){
				rh_field += "rh." + rs.getString("column_name") + " as rh_" + rs.getString("column_name") + ",";
			}
			else if(e_table.equalsIgnoreCase(rs.getString("table_name"))){
				e_field += "e." + rs.getString("column_name") + " as e_" + rs.getString("column_name") + ",";
			}
		}
		System.out.println(rl_field);
		if(!rl_field.isEmpty())
			rl_field = rl_field.substring(0, rl_field.length()-1);
		if(!rh_field.isEmpty())
			rh_field = rh_field.substring(0, rh_field.length()-1);
		if(!e_field.isEmpty())
			e_field = e_field.substring(0, e_field.length()-1);

		String from_where = " rh.event_id in (select distinct event_id from "+rl_core_table+")";
		String rlh_sql = "";
		if(rh_exist){
			rlh_sql = String.format("(select %s,%s from %s rh left join %s rl on (rh.policy_id=rl.policy_id and rh.location_id=rl.location_id and rh.event_id=rl.event_id) where %s)",
					rl_field,
					rh_field,
					rh_table,
					rl_core_table,
					from_where
					);
		}
		else{
			rlh_sql = String.format("(select %s from %s rh)",
					rl_field,
					rl_core_table
					);
		}
		String all_sql = String.format("(select %s,rlh.* from %s rlh left join %s e on (rlh.rh_policy_id=e.policy_id and rlh.rh_location_id=e.location_id)) too", 
				e_field,
				rlh_sql,
				e_table
				);
		System.out.println(all_sql);
		jsonBean = jsDao.queryTableWithColumnInfo2(fields, all_sql, where, other, excludeFields);

		return jsonBean;
	}

	public JSONBean querySQLSelectAllInOne(String fields, String from, String where, String other) throws SQLException {
		JSONBean jsonBean = null;
		String excludeFields = "rast,location,geom";//多个字段用","隔开
		String table = GlobalFuns.findCoreTable(from);
		String typerecord = table.substring(table.lastIndexOf("_")+1);
		String rl_table = "client_risk_rl_data_"+typerecord;
		String rh_table = "client_risk_rh_data_"+typerecord;
		String rm_table = "client_risk_rm_data_"+typerecord;
		boolean rl_exist = true;
		boolean rh_exist = true;
		boolean rm_exist = true;

		String rid = "CONCAT(e.rid,rl.rid,'_','_',rh.rid,'_',rm.rid) AS rid,";
		String e_field = "";
		String rl_field = "";
		String rh_field = "";
		String rm_field = "";
		if(!fields.isEmpty()){
			fields = fields + ",";
		}

		Connection conn = DBUtils.getClientDBConnection();
		ResultSet rse = null;
		//判断各个表是否存在
		rse = conn.getMetaData().getTables(null, null, rl_table, null);
		if (rse.next()){
			//Table exist 
			rl_exist = true;
		}
		else {
			rl_exist = false;
		}
		rse = conn.getMetaData().getTables(null, null, rh_table, null);
		if (rse.next()){
			//Table exist 
			rh_exist = true;
		}
		else {
			rh_exist = false;
		}
		rse = conn.getMetaData().getTables(null, null, rm_table, null);
		if (rse.next()){
			//Table exist 
			rm_exist = true;
		}
		else {
			rm_exist = false;
		}
		
		String sql = "select column_name,table_name from information_schema.columns "
				+ " WHERE table_schema='public' "
				+ " and table_name in ('"+table+"','"+rl_table+"','"+rh_table+"','"+rm_table+"')";

		ResultSet rs = conn.prepareStatement(sql).executeQuery();
		while (rs.next()) {
			if(table.equalsIgnoreCase(rs.getString("table_name"))){
				e_field += "e." + rs.getString("column_name") + " as e_" + rs.getString("column_name") + ",";
//				if ("rid".equalsIgnoreCase(rs.getString("column_name"))) {
//					rid += "e.rid"+"||'_'";
//				}
			}
			else if(rl_table.equalsIgnoreCase(rs.getString("table_name"))){
				rl_field += "rl." + rs.getString("column_name") + " as rl_" + rs.getString("column_name") + ",";
//				if ("rid".equalsIgnoreCase(rs.getString("column_name"))) {
//					rid += "rl.rid"+"||'_'";
//				}
			}
			else if(rh_table.equalsIgnoreCase(rs.getString("table_name"))){
				rh_field += "rh." + rs.getString("column_name") + " as rh_" + rs.getString("column_name") + ",";
//				if ("rid".equalsIgnoreCase(rs.getString("column_name"))) {
//					rid += "rh.rid"+"||'_'";
//				}
			}
			else if(rm_table.equalsIgnoreCase(rs.getString("table_name"))){
				rm_field += "rm." + rs.getString("column_name") + " as rm_" + rs.getString("column_name") + ",";
//				if ("rid".equalsIgnoreCase(rs.getString("column_name"))) {
//					rid += "rm.rid"+"||'_'";
//				}
			}
		}

//		if(!rid.isEmpty()){
//			rid = rid.substring(0, rid.lastIndexOf("||")) + " as rid,";
//		}
		rs.close();
		conn.close();

		rm_table = "select rm0.*,t.hazard_id from "
		        + rm_table + " rm0,"
				+ String.format(GlobalVariable.dbling_disater_db, "select id,hazard_ids from risk_func_table", "\"id\" integer,hazard_id character varying")
				+ " where rm0.func_id=t.id";

		String all_in_one_sql = "select %s %s %s %s %s %s ROW_NUMBER() OVER()"
				+ " from (%s) rm"
				+ " left join %s rl on rm.policy_id=rl.policy_id and rm.location_id=rl.location_id and rm.event_id = rl.event_id "
				+ " left join %s rh on rm.policy_id=rh.policy_id and rm.location_id=rh.location_id and rm.event_id = rh.event_id and rm.hazard_id=rh.hazard_id "
				+ " left join %s e on e.policy_id=rm.policy_id and e.location_id=rm.location_id ";

		jsonBean = jsDao.queryTableWithColumnInfo2(" * ", "("+String.format(all_in_one_sql,rid, rm_field,rl_field,rh_field,e_field,fields,rm_table,rl_table,rh_table,table)+") foo", where, other, excludeFields);

		return jsonBean;
	}

	/**
	 * 提取H值，生成Rh数据
	 * @param e_sql
	 * @param h_sql
	 * @param spatial_field
	 * @param h_columnInfos
	 * @return
	 * @throws SQLException
	 * @throws InsertException
	 */
	public boolean extractValue_Rh(
			String e_sql, 
			String h_sql, 
			String spatial_field,
			List<Map<String, String>> h_columnInfos
			) throws SQLException, InsertException
	{
		boolean result = false;
		List<String> excludeField = Arrays.asList(
				"row_number",
				"render_rast",
				"extent",
				"wkt");
		h_sql = h_sql.toLowerCase();
		e_sql = e_sql.toLowerCase();
		String h_from_sql = h_sql.substring(h_sql.indexOf(" from "));
		String h_table = GlobalFuns.findCoreTable(h_sql);
		String e_table = GlobalFuns.findCoreTable(e_sql);
		String h_field_sql = "";
		String h_field_dblink_sql = "";
		int length = h_columnInfos.size();
		for (int i = 0; i < length; i++) {
			if (!excludeField.contains(h_columnInfos.get(i).get("columnName"))) {
				h_field_sql += h_columnInfos.get(i).get("columnName") + ",";
				h_field_dblink_sql += h_columnInfos.get(i).get("columnName") + " ";
				h_field_dblink_sql +=(h_columnInfos.get(i).get("columnType").equalsIgnoreCase("serial")?"integer":h_columnInfos.get(i).get("columnType")) + ",";
			}
		}
		if (h_field_sql.endsWith(",")) {
			h_field_sql = h_field_sql.substring(0,
					h_field_sql.length() - 1);// 删除最后一个逗号
		}
		if (h_field_dblink_sql.endsWith(",")) {
			h_field_dblink_sql = h_field_dblink_sql.substring(0,
					h_field_dblink_sql.length() - 1);// 删除最后一个逗号
		}

		String h_dblink_sql = "select * from " + String.format(
				GlobalVariable.dbling_disater_db, 
				("select "+h_field_sql+h_from_sql).replace("'", "''"),
				h_field_dblink_sql
				);

		Connection conn = DBUtils.getClientDBConnection();
		conn.setAutoCommit(false);

		Map<String, String> hazardidMap = new HashMap<String, String>();
		String hazardid_sql = "select * from " + String.format(
				GlobalVariable.dbling_disater_db, 
				"select tablename,fieldname,hazard_id FROM dic_fields_info",
				"tablename character varying,fieldname character varying,hazard_id character varying"
				);
		ResultSet hazardid_rs = conn.prepareStatement(hazardid_sql).executeQuery();
		conn.commit();
		while (hazardid_rs.next()) {
			hazardidMap.put(
					hazardid_rs.getString("tablename")+"_"+hazardid_rs.getString("fieldname"), 
					hazardid_rs.getString("hazard_id"));
		}

		Statement ps = conn.createStatement();
		if (spatial_field.equalsIgnoreCase("rast")) {
			String sqlString = "SELECT"
					+" ST_Value(t."+spatial_field+", foo.geom) As value,t.event_id,foo.location_id,foo.policy_id"
					+" FROM ("+h_dblink_sql+") t CROSS JOIN ("+e_sql+") As foo";
			//			System.out.println(sqlString);

			ResultSet rs = conn.prepareStatement(sqlString).executeQuery();
			conn.commit();
			int upload_i = 0;//记录上传行数
			while (rs.next()) {
				upload_i++;
				String valueString = String.format(
						"'%s','%s','%s','%s','%s','%s','%s'",
						rs.getString("policy_id"),
						rs.getString("location_id"),
						rs.getString("event_id"),
						rs.getString("value"),
						spatial_field,
						h_table,
						hazardidMap.get(h_table+"_"+"rast"));//hazard_id目前测试默认设置3
				String insert_sql = String.format(
						"insert into client_risk_rh_data_%s(policy_id,location_id,event_id,field_value,field_name,field_table,hazard_id) values(%s);",
						e_table.substring(e_table.lastIndexOf("_")+1), valueString);
				ps.executeUpdate(insert_sql);
				if (upload_i % 500 == 0) {
					// 500条记录提交一次
					try {
						conn.commit();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						throw new InsertException("第"+upload_i+"行:\n"+e.getMessage());
					}
					System.out.println("已成功提交" + upload_i + "行!");
				}
			}
			if (upload_i % 500 != 0) {
				// 不够500条的再提交一次（其实不用判断，直接提交就可以，不会重复提交的）
				try {
					conn.commit();
				} catch (SQLException e) {
					e.printStackTrace();
					throw new InsertException("第"+upload_i+"行:\n"+e.getMessage());
				}
				System.out.println("已成功提交" + upload_i + "行!");
			}
		}
		else if (spatial_field.equalsIgnoreCase("geom")) {
			String sqlString = "SELECT foo.location_id,foo.policy_id,t.*"
					+" FROM ("+h_dblink_sql+") t CROSS JOIN ("+e_sql+") As foo"
					+" WHERE foo.geom=t.geom or ST_Intersects(foo.geom, t.geom)";
			List<String> noField = Arrays.asList(
					"row_number",
					"filename",
					"gid",
					"geom",
					"location_id",
					"policy_id",
					"id",
					"event_id_val",
					"event_id");
			ResultSet rs = conn.prepareStatement(sqlString).executeQuery();
			ResultSetMetaData rsMD = rs.getMetaData();
			conn.commit();
			int upload_i = 0;//记录上传行数
			while (rs.next()) {
				upload_i++;
				int size = rsMD.getColumnCount();
				for (int i = 1; i <= size; i++) {
					if (!noField.contains(rsMD.getColumnName(i))) {
						String valueString = String.format(
								"'%s','%s','%s','%s','%s','%s','%s'",
								rs.getString("policy_id"),
								rs.getString("location_id"),
								rs.getString("event_id"),
								rs.getString(rsMD.getColumnName(i)),
								rsMD.getColumnName(i),
								h_table,
								hazardidMap.get(h_table+"_"+rsMD.getColumnName(i)));//hazard_id目前测试默认设置3
						String insert_sql = String.format(
								"insert into client_risk_rh_data_%s(policy_id,location_id,event_id,field_value,field_name,field_table,hazard_id) values(%s);",
								e_table.substring(e_table.lastIndexOf("_")+1), valueString);
						ps.executeUpdate(insert_sql);
						if (upload_i % 1000 == 0) {
							// 1000条记录提交一次
							try {
								conn.commit();
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								ps.close();
								conn.close();
								throw new InsertException("第"+upload_i+"行:\n"+e.getMessage());
							}
							System.out.println("已成功提交" + upload_i + "行!");
						}
					}
				}
			}
			if (upload_i % 1000 != 0) {
				// 不够1000条的再提交一次（其实不用判断，直接提交就可以，不会重复提交的）
				try {
					conn.commit();
				} catch (SQLException e) {
					e.printStackTrace();
					ps.close();
					conn.close();
					throw new InsertException("第"+upload_i+"行:\n"+e.getMessage());
				}
				System.out.println("已成功提交" + upload_i + "行!");
			}
		}
		//		System.out.println("执行去重。");
		//		//去重
		//		ps.execute("select user_del_repeat('client_risk_rh_data_"+e_table.substring(e_table.lastIndexOf("_")+1)+"','policy_id, location_id, event_id, field_value, field_name, field_table, hazard_id')");
		//		conn.commit();
		//		System.out.println("去重完成。");
		ps.close();
		conn.close();
		System.out.println("执行完成。");

		result = true;
		return result;
	}
	
	public boolean caculateLossrateFromFunc_Rm(String e_sql, String propertys) throws SQLException, InsertException{
		boolean result = false;
		String e_table = GlobalFuns.findCoreTable(e_sql);
		String func_sql = "select id as func_id, no, hazard_ids, expo_type_ids, func_exp, arg_num, func_points, description from risk_func_table";
		String func_fields_sql = "func_id integer,no character varying,hazard_ids character varying,expo_type_ids character varying,func_exp text,arg_num integer,func_points text,description text";
		String func_dblink_sql = String.format(
				GlobalVariable.dbling_disater_db, 
				func_sql,
				func_fields_sql
				);
		String re_sql = "select r.*,"+propertys+",concat_ws(',',"+propertys+") eti from client_risk_rh_data"+e_table.substring(e_table.lastIndexOf("_"))+" r LEFT JOIN ("+e_sql+") e ON r.policy_id=e.policy_id and r.location_id=e.location_id";
		String sql = String.format(
				"select rh.*, t.* from (%s) rh,%s where rh.hazard_id = t.hazard_ids and rh.eti = t.expo_type_ids ",
				re_sql,
				func_dblink_sql
				);
		//		System.out.println(sql);
		JavaPythonDao pythonDao = new JavaPythonDao();
		Connection conn = DBUtils.getClientDBConnection();
		conn.setAutoCommit(false);
		Statement ps = conn.createStatement();
		ResultSet rs = conn.prepareStatement(sql).executeQuery();

		Date now = new Date();

		try {
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			ps.close();
			conn.close();
			throw new InsertException("查询函数错误:\n"+e.getMessage());
		}

		System.out.println("关联读取函数时间："+((new Date()).getTime() - now.getTime()));
		now = new Date();

		int upload_i = 0;//记录上传行数
		while (rs.next()) {
			upload_i++;
			double lossrate = 0;//损失率

			String lr_value = pythonDao.mathExpCMD(
					"{\"he1\":"+rs.getString("field_value")+"}",
					rs.getString("func_exp"));

			lossrate = (lr_value==null||lr_value.isEmpty()||lr_value.equalsIgnoreCase("--"))?-999:Double.parseDouble(lr_value);

			String valueString = String.format(
					"'%s','%s','%s',%s,'%s','%s',%s",
					rs.getString("policy_id"),
					rs.getString("location_id"),
					rs.getString("event_id"),
					rs.getString("func_id"),
					rs.getString("field_name"),
					rs.getString("field_table"),
					lossrate==-999?"null":lossrate);
			String insert_sql = String.format(
					"insert into client_risk_rm_data_%s(policy_id,location_id,event_id,func_id,field_name,field_table,loss_rate) values(%s);",
					e_table.substring(e_table.lastIndexOf("_")+1), valueString);

			ps.executeUpdate(insert_sql);
			if (upload_i % 500 == 0) {
				// 500条记录提交一次
				try {
					conn.commit();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					ps.close();
					conn.close();
					throw new InsertException("第"+upload_i+"行:\n"+e.getMessage());
				}
				System.out.println("已成功提交" + upload_i + "行!");

				System.out.println("Pytyon计算第" + upload_i + "条数据时间："+((new Date()).getTime() - now.getTime()));
				now = new Date();
			}
		}
		if (upload_i % 500 != 0) {
			// 不够500条的再提交一次（其实不用判断，直接提交就可以，不会重复提交的）
			try {
				conn.commit();
			} catch (SQLException e) {
				e.printStackTrace();
				ps.close();
				conn.close();
				throw new InsertException("第"+upload_i+"行:\n"+e.getMessage());
			}
			System.out.println("已成功提交" + upload_i + "行!");

			System.out.println("Pytyon计算第" + upload_i + "条数据时间："+((new Date()).getTime() - now.getTime()));
			now = new Date();
		}
		//去重
		//		ps.execute("select user_del_repeat('client_risk_rm_data_"+e_table.substring(e_table.lastIndexOf("_")+1)+"','policy_id, location_id, event_id, func_id, field_name, field_table, loss_rate')");
		//		try {
		//			conn.commit();
		//		} catch (SQLException e) {
		//			e.printStackTrace();
		//			ps.close();
		//			conn.close();
		//			throw new InsertException("去重复数据错误:\n"+e.getMessage());
		//		}
		//		
		//		System.out.println("去重数据时间："+((new Date()).getTime() - now.getTime()));

		ps.close();
		conn.close();
		System.out.println("执行完成。");
		result = true;
		return result;
	}
	
	//不适用python计算
	public boolean caculateLossrateFromFunc_Rm2(String e_sql, String propertys) throws SQLException, InsertException{
		boolean result = false;
		String e_table = GlobalFuns.findCoreTable(e_sql);
		String func_sql = "select id as func_id, no, hazard_ids, expo_type_ids, func_exp, arg_num, func_points, description from risk_func_table";
		String func_fields_sql = "func_id integer,no character varying,hazard_ids character varying,expo_type_ids character varying,func_exp text,arg_num integer,func_points text,description text";
		String func_dblink_sql = String.format(
				GlobalVariable.dbling_disater_db, 
				func_sql,
				func_fields_sql
				);
		String re_sql = "select r.*,"+propertys+",concat_ws(',',"+propertys+") eti from client_risk_rh_data"+e_table.substring(e_table.lastIndexOf("_"))+" r LEFT JOIN ("+e_sql+") e ON r.policy_id=e.policy_id and r.location_id=e.location_id";
		String sql = String.format(
				"select rh.*, t.*,her_riskcal ('{\"he1\":' || rh.field_value || '}',t.func_exp) AS loss_rate from (%s) rh,%s where rh.hazard_id = t.hazard_ids and rh.eti = t.expo_type_ids ",
				re_sql,
				func_dblink_sql
				);
		System.out.println(sql);
		Connection conn = DBUtils.getClientDBConnection();
		conn.setAutoCommit(false);
		Statement ps = conn.createStatement();
		ResultSet rs = conn.prepareStatement(sql).executeQuery();

		Date now = new Date();

		try {
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			ps.close();
			conn.close();
			throw new InsertException("数据计算错误:\n"+e.getMessage());
		}

		System.out.println("数据计算时间："+((new Date()).getTime() - now.getTime()));
		now = new Date();

		int upload_i = 0;//记录上传行数
		while (rs.next()) {
			upload_i++;

			String valueString = String.format(
					"'%s','%s','%s',%s,'%s','%s',%s",
					rs.getString("policy_id"),
					rs.getString("location_id"),
					rs.getString("event_id"),
					rs.getString("func_id"),
					rs.getString("field_name"),
					rs.getString("field_table"),
					rs.getString("loss_rate"));
			String insert_sql = String.format(
					"insert into client_risk_rm_data_%s(policy_id,location_id,event_id,func_id,field_name,field_table,loss_rate) values(%s);",
					e_table.substring(e_table.lastIndexOf("_")+1), valueString);

			ps.executeUpdate(insert_sql);
			if (upload_i % 500 == 0) {
				// 500条记录提交一次
				try {
					conn.commit();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					ps.close();
					conn.close();
					throw new InsertException("第"+upload_i+"行:\n"+e.getMessage());
				}
				System.out.println("已成功提交" + upload_i + "行!");
			}
		}
		if (upload_i % 500 != 0) {
			// 不够500条的再提交一次（其实不用判断，直接提交就可以，不会重复提交的）
			try {
				conn.commit();
			} catch (SQLException e) {
				e.printStackTrace();
				ps.close();
				conn.close();
				throw new InsertException("第"+upload_i+"行:\n"+e.getMessage());
			}
			System.out.println("已成功提交" + upload_i + "行!");
		}
		//去重
		//		ps.execute("select user_del_repeat('client_risk_rm_data_"+e_table.substring(e_table.lastIndexOf("_")+1)+"','policy_id, location_id, event_id, func_id, field_name, field_table, loss_rate')");
		//		try {
		//			conn.commit();
		//		} catch (SQLException e) {
		//			e.printStackTrace();
		//			ps.close();
		//			conn.close();
		//			throw new InsertException("去重复数据错误:\n"+e.getMessage());
		//		}
		//		
		//		System.out.println("去重数据时间："+((new Date()).getTime() - now.getTime()));

		ps.close();
		conn.close();

		System.out.println("数据录入时间："+((new Date()).getTime() - now.getTime()));
		now = new Date();
		System.out.println("执行完成。");
		result = true;
		return result;
	}
}
