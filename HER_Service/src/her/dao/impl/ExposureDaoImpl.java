package her.dao.impl;

import her.bean.JSONBean;
import her.bean.JSONBean.Status;
import her.dao.JsonBeanDao;
import her.model.InsertException;
import her.utils.DBUtils;
import her.utils.GlobalFuns;
import her.utils.GlobalVariable;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.python.antlr.PythonParser.else_clause_return;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Administrator
 *
 */
public class ExposureDaoImpl {
	private JsonBeanDao jsDao;
	public ExposureDaoImpl()
	{
		jsDao = new JsonBeanDao();
	}

	/**
	 * @param client 是否使用 client库
	 */
	public ExposureDaoImpl(Boolean client)
	{
		if(client)
		{
			jsDao = new JsonBeanDao(DBUtils.getClientDBConnection());
		}
		else {
			jsDao = new JsonBeanDao();
		}
	}

	/**
	 * 创建树目录
	 * @return
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 */
	public String createTreeXmlPer() throws TransformerFactoryConfigurationError, TransformerException
	{
		Connection connection = DBUtils.getConnection();
		Document document = null;
		String result = "";

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.newDocument();
		} catch (ParserConfigurationException e) {
			System.out.println(e.getMessage());
		}
		Element root = document.createElement("node");//创建根节点
		document.appendChild(root);

		String sql = "select distinct corp from client_expo_policy_per_risk";
		System.out.println(sql);
		try {
			PreparedStatement preparedStatement = connection
					.prepareStatement(sql);
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {
				String[] values_corp = new String[]{rs.getString("corp")};
				Element element = createNodePer(document, values_corp);;
				element.setAttribute("label", rs.getString("corp"));//增加属性

				root.appendChild(element);
			}

			preparedStatement.close();
			connection.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			try {
				connection.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		if (document != null) {
			StringWriter strWtr = new StringWriter();
			TransformerFactory tf = TransformerFactory.newInstance();  
			Transformer transformer = tf.newTransformer();  
			DOMSource source = new DOMSource(document);  
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");  
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");//设置文档的换行与缩进   
			StreamResult strResult = new StreamResult(strWtr);  
			transformer.transform(source, strResult);
			result = strResult.getWriter().toString();
		}

		return result;
	}

	private Element createNodePer(
			Document document,
			String[] values) throws SQLException
	{
		Element parent = document.createElement("node");
		Connection connection = DBUtils.getConnection();
		String sql;

		if(values.length == 1)
		{
			sql = "select distinct corp_branch from client_expo_policy_per_risk where corp='" + values[0] + "'";
			System.out.println(sql);
			PreparedStatement preparedStatement_branch = connection
					.prepareStatement(sql);
			ResultSet rs_branch = preparedStatement_branch.executeQuery();
			while (rs_branch.next()) {
				String[] values_branch = new String[]{values[0].toString(), rs_branch.getString("corp_branch")};
				Element element_corp_branch = createNodePer(document, values_branch);
				element_corp_branch.setAttribute("label", rs_branch.getString("corp_branch"));//增加属性

				parent.appendChild(element_corp_branch);
			}
			preparedStatement_branch.close();
		}
		else if(values.length == 2)
		{
			sql = "select distinct disaster_type from client_expo_policy_per_risk where corp='" + values[0] + "' and corp_branch='" + values[1] + "'";
			System.out.println(sql);
			PreparedStatement preparedStatement_peril = connection
					.prepareStatement(sql);
			ResultSet rs_disaster_type = preparedStatement_peril.executeQuery();
			while (rs_disaster_type.next()) {
				String[] values_disaster_type = new String[]{values[0].toString(), values[1].toString(), rs_disaster_type.getString("disaster_type")};
				Element element_disaster_type = createNodePer(document, values_disaster_type);
				element_disaster_type.setAttribute("label", rs_disaster_type.getString("disaster_type"));//增加属性

				parent.appendChild(element_disaster_type);
			}
			preparedStatement_peril.close();
		}
		else if(values.length == 3)
		{
			sql = "select distinct year_uw from client_expo_policy_per_risk where corp='" + values[0] + "' and corp_branch='" + values[1] + "' and disaster_type='" + values[2] + "' order by year_uw desc";
			System.out.println(sql);
			PreparedStatement preparedStatement_year = connection
					.prepareStatement(sql);
			ResultSet rs_year = preparedStatement_year.executeQuery();
			while (rs_year.next()) {
				Element element_year = document.createElement("node");
				element_year.setAttribute("label", rs_year.getString("year_uw"));//增加属性
				parent.appendChild(element_year);
			}
			preparedStatement_year.close();
		}

		connection.close();

		return parent;
	}

	/**
	 * 关键字查询保单
	 * @param key
	 * @param startRow
	 * @param count
	 * @return
	 */
	public JSONBean queryPolicyByKeys(
			String key, String startRow, String count) {
		String sqlString = "select p.*,r.name_en as peril,r.name_cn as peril_cn "
				+" from"
				+" client_expo_policy_per_risk p,dic_peril r ";

		if(!key.isEmpty())
		{
			sqlString += " where p.peril_id = r.id";
			sqlString += " or p.insured like '%"+key+"%'";
			sqlString += " or p.address like '%"+key+"%'";
			sqlString += " or p.corp like '%"+key+"%'";
			sqlString += " or p.corp_branch like '%"+key+"%'";
		}
		else 
		{
			sqlString += " where 1=0" ;
		}
		//		sqlString += " order by begin_time desc";
		sqlString += " limit " + count + " offset " + startRow;
		JSONBean jsonBean = jsDao.queryTable(sqlString);
		String countSql = "select count(*) as count " + sqlString.substring(sqlString.indexOf("from"));
		jsonBean.setDataCount(jsDao.queryCount(countSql));
		return jsonBean;
	}

	/**
	 * 树菜单查询保单
	 * @param corp
	 * @param corp_branch
	 * @param disaster_type
	 * @param year_uw
	 * @param startRow
	 * @param count
	 * @return
	 */
	public JSONBean queryPolicyByTree(
			String corp, 
			String corp_branch,
			String disaster_type,
			String year_uw,
			String startRow, 
			String count) {
		String sqlString = "select p.*,r.name_en as peril,r.name_cn as peril_cn "
				+" from"
				+" client_expo_policy_per_risk p,dic_peril r ";
		String whereString = "p.peril_id = r.id";
		if(corp != null && !corp.isEmpty())
		{
			whereString += " and p.corp='"+corp+"'";
		}
		if(corp_branch != null && !corp_branch.isEmpty())
		{
			whereString += " and p.corp_branch='"+corp_branch+"'";
		}
		if(disaster_type != null && !disaster_type.isEmpty())
		{
			whereString += " and p.disaster_type='"+disaster_type+"'";
		}
		if(year_uw != null && !year_uw.isEmpty())
		{
			whereString += " and p.year_uw="+year_uw;
		}

		if(!whereString.isEmpty())
		{
			sqlString = sqlString + " where " + whereString;
		}
		else 
		{
			sqlString = sqlString + " where 1=0" ;
		}
		sqlString += " limit " + count + " offset " + startRow;

		JSONBean jsonBean = jsDao.queryTable(sqlString);
		String countSql = "select count(*) as count " + sqlString.substring(sqlString.indexOf("from"));
		jsonBean.setDataCount(jsDao.queryCount(countSql));
		return jsonBean;
	}

	public JSONBean queryPolicyByTreeAll(
			String corp, 
			String corp_branch,
			String year_uw) {
		String sqlString = "select p.* "
				+" from"
				+" client_expo_policy_per_risk p";
		String whereString = "1 = 1";
		if(corp != null && !corp.isEmpty())
		{
			whereString += " and p.corp='"+corp+"'";
		}
		if(corp_branch != null && !corp_branch.isEmpty())
		{
			whereString += " and p.corp_branch='"+corp_branch+"'";
		}
		if(year_uw != null && !year_uw.isEmpty())
		{
			whereString += " and p.year_uw="+year_uw;
		}

		if(!whereString.isEmpty())
		{
			sqlString = sqlString + " where " + whereString;
		}
		else 
		{
			sqlString = sqlString + " where 1=0" ;
		}

		JSONBean jsonBean = jsDao.queryTable(sqlString);
		return jsonBean;
	}

	/**
	 * 查询保单损失数据
	 * @param policy_id
	 * @return
	 */
	public JSONBean queryPolicyLossByID(String policy_id)
	{
		Connection connection = DBUtils.getConnection();
		//		String sql = "select l.*,c.name_cat as event "
		//				+" from client_expo_policy_loss l,her_disaster_event_catelog c "
		//				+" where l.event_id=c.event_id and l.policy_id='"+policy_id+"'";
		JSONBean jsonBean = new JSONBean();
		try {
			//			String sql_hazard = "select l.*,p.lon,p.lat,c.name_cat as event,c.disaster_type,c.disaster_type_id,m.peril_type,m.peril_table,m.peril_field,m.peril_field_type,'null' as peril_value "
			//					+" from client_expo_policy_loss l,client_expo_policy_per_risk p,her_disaster_event_catelog c,her_disaster_peril_mapping m "
			//					+" where l.event_id=c.event_id and l.policy_id='"+policy_id+"' and l.policy_id=p.policy_id and c.disaster_type_id=m.disaster_type_id";
			String sql_loss = "select l.*,p.lon,p.lat,c.name_cat as event,c.disaster_type,c.disaster_type_id "
					+" from client_expo_policy_loss l,client_expo_policy_per_risk p,her_disaster_event_catelog c "
					+" where l.event_id=c.event_id and l.policy_id='"+policy_id+"' and l.policy_id=p.policy_id";

			System.out.println(sql_loss);

			PreparedStatement ps_loss = connection
					.prepareStatement(sql_loss);
			PreparedStatement ps_peril = null;
			PreparedStatement ps_value = null;
			ResultSet rs_loss = ps_loss.executeQuery();
			ResultSetMetaData rsmd = rs_loss.getMetaData();
			int colcount = rsmd.getColumnCount();//取得全部列数

			List<Map<String, String>> data = new ArrayList<Map<String, String>>();// 数据
			while (rs_loss.next()) {
				Map<String, String> map_loss = new HashMap<String, String>();
				for(int i=1;i<=colcount;i++){
					String colname = rsmd.getColumnName(i);//取得全部列名
					map_loss.put(colname, rs_loss.getString(colname));
				}

				String sql_peril = "select p.name_cn as peril_type,p.table as peril_table,p.field as peril_field,p.field_type as peril_field_type"
						+ " from her_disaster_peril_mapping m,dic_peril_index p"
						+ " where m.peril_index_id=p.id and m.disaster_type_id="+rs_loss.getString("disaster_type_id");

				System.out.println(sql_peril);

				ps_peril = connection.prepareStatement(sql_peril);
				ResultSet rs_peril = ps_peril.executeQuery();

				while (rs_peril.next()) {

					String sql_value = "select ST_Value(t."+rs_peril.getString("peril_field")+", foo.pt_geom) as value"
							+ " from "+rs_peril.getString("peril_table")+" t CROSS JOIN (SELECT ST_SetSRID(ST_Point("+rs_loss.getString("lon")+", "+rs_loss.getString("lat")+"), 4326) as pt_geom) as foo"
							+ " where t.event_id='"+rs_loss.getString("event_id")+"'";

					System.out.println(sql_value);

					ps_value = connection.prepareStatement(sql_value);
					ResultSet rs_value = ps_value.executeQuery();
					if(rs_value.next())
					{	
						map_loss.put(rs_peril.getString("peril_type")+"_dp", rs_value.getString("value"));
					}
					else {
						map_loss.put(rs_peril.getString("peril_type")+"_dp", "null");
					}
				}

				data.add(map_loss);
			}
			if(ps_loss != null)ps_loss.close();
			if(ps_peril != null)ps_peril.close();
			if(ps_value != null)ps_value.close();
			connection.close();

			Map<String, Object> result = new HashMap<String, Object>();
			result.put("data", data);
			jsonBean.setResult(result);
			jsonBean.setStatus(Status.SUCCESS);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			try {
				connection.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return jsonBean;
	}
	
	public JSONBean getRecord()
	{
		String sqlString = "SELECT DISTINCT record_type_id,record_type FROM client_expo_record_type_setting";
		JSONBean jsonBean = jsDao.queryTable(sqlString);
		return jsonBean;
	}

	public JSONBean getPolicyAggr_Test()
	{
		Connection connection = DBUtils.getConnection();
		JSONBean jsonBean = new JSONBean();
		try {

			String sql = "select * "
					+" from policy_aggr_test";

			PreparedStatement ps = connection
					.prepareStatement(sql);

			ResultSet rs = ps.executeQuery();
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
			if(ps != null)ps.close();
			if(rs != null)rs.close();
			connection.close();

			Map<String, Object> result = new HashMap<String, Object>();
			result.put("data", data);
			jsonBean.setResult(result);
			jsonBean.setStatus(Status.SUCCESS);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			try {
				connection.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return jsonBean;
	}

	public JSONBean downScaling()
	{
		JSONBean jsonBean = new JSONBean();
		//		ScalingImpl scale = new ScalingImpl();
		//		jsonBean = scale.upScaling("select p.id,p.geom_id,p.zone,p.premium as value,c.geom from policy_aggr_test p,county_400w c where p.geom_id=c.geom_id", "geom_id");
		return jsonBean;
	}

	/**
	 * 暴露数据降尺度
	 * @param level
	 * @return
	 */
	public JSONBean upScaling(String level)
	{
		String vector = "county_400w";
		if(level.equalsIgnoreCase("city"))
		{
			vector = "county";
		}
		else if (level.equalsIgnoreCase("province")) {
			vector = "province_400w";
		}
		else if (level.equalsIgnoreCase("county")) {
			vector = "county_400w";
		}
		else if (level.equalsIgnoreCase("grid10")) {
			vector = "county";
		}
		else if (level.equalsIgnoreCase("grid1")) {
			vector = "county";
		}
		JSONBean jsonBean = new JSONBean();
		ScalingImpl scale = new ScalingImpl();
		jsonBean = scale.upScaling("select p.id,p.code,p.zone,p.premium as value,c.geom from policy_aggr_test p,"+vector+" c where p.code=c.code", "code");
		return jsonBean;
	}

	public JSONBean getPolicyCatalog(String type)
	{
		String sql = "SELECT * FROM client_expo_policy_catalog where p_type='"+type+"'";
		JSONBean jsonBean = jsDao.queryTable(sql);
		return jsonBean;
	}

	public JSONBean getPolicy_Aggr(String cat_id)
	{
		String sql = "SELECT * FROM client_expo_policy_aggr_test where cat_id="+cat_id;
		JSONBean jsonBean = jsDao.queryTable(sql);
		return jsonBean;
	}

	public JSONBean savePolicy(String cat_id, String json) throws JSONException, SQLException
	{
		Connection conn = DBUtils.getConnection();
		JSONBean jsonBean = new JSONBean();
		conn.setAutoCommit(false);
		Statement ps = null;
		ps = conn.createStatement();

		ResultSet rs = ps.executeQuery("update client_expo_policy_catalog set modify_time='now()' where id="+cat_id+" returning tablename");
		String table = "";
		if(rs.next())
		{
			table = rs.getString("tablename");
		}
		if (table.isEmpty()) {
			jsonBean.setResult(null);
			jsonBean.setStatus(Status.FAILED);

			return jsonBean;
		}

		//		JSONObject dataJsonObj = new JSONObject(json);
		JSONArray dataJsonObj = new JSONArray(json);
		int data_count = dataJsonObj.length();
		for (int i = 0; i < data_count; i++) {// 循环要更新的数据
			JSONObject jobj = (JSONObject) dataJsonObj.get(i);
			String set_sql = "";
			String id = "";
			Iterator it = jobj.keys();
			while (it.hasNext()) {
				String key = it.next().toString();
				if(!key.equalsIgnoreCase("id"))
				{
					set_sql += key + "=" + jobj.getString(key);
				}
				else {
					id = jobj.getString(key);
				}
			}
			String sql = String.format(
					"update %s set %s where id=%s", 
					table,set_sql,id);

			ps.executeUpdate(sql);
		}
		conn.commit();
		ps.close();
		conn.close();
		jsonBean.setResult(null);
		jsonBean.setStatus(Status.SUCCESS);

		return jsonBean;
	}

	//==============================================================================
	// 分库后代码
	//==============================================================================
	public JSONBean getQueryCondition_e(){
		String sqlString = "select * from client_expo_query_condition order by rank";
		JSONBean jsonBean = jsDao.queryTable(sqlString);
		return jsonBean;
	}

	public JSONBean getQueryConditionItemColumn_e(){
		String sqlString = "select * from client_expo_index_table";
		JSONBean jsonBean = jsDao.getTableStructure(sqlString);
		return jsonBean;
	}

	//her_query_condition_item_h
	public JSONBean getQueryConditionItem_e(String retrunField, String whereField){
		String sqlString = String.format("select distinct %s from client_expo_index_table where 1=1 %s", retrunField, whereField);
		JSONBean jsonBean = jsDao.queryTable(sqlString);
		return jsonBean;
	}

	public JSONBean writeQueryCondition(String values){
		jsDao.deleteAction("DELETE FROM client_expo_query_condition", false);
		String sqlString = String.format("insert into client_expo_query_condition (field, field_alias, condition_col, required, rank, multi, default_show, condition_col_type) values%s", values);
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
	
	public JSONBean querySQLSelectBean(String sql) {
		JSONBean jsonBean = null;
		String excludeFields = "rast,location,geom";//多个字段用,隔开
//		jsonBean = jsDao.queryTableWithColumnInfo(fields, from, where, other, excludeFields);
		jsonBean = jsDao.queryTableWithSQL(sql, excludeFields);
		return jsonBean;
	}

	private Connection conn = null;
	private Statement ps = null;
	public void close(){
		if (ps != null) {
			try {
				ps.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public JSONBean getScalingIndex(){
		String sqlString = "select * from expo_scaling_index";
		return jsDao.queryTable(sqlString);
	}

	public String downScalingData(String dataSQL, String data_field, String rate_table, String rate_field, String suffix) throws SQLException, InsertException, ParseException{
		conn = DBUtils.getClientDBConnection();
		ps = conn.createStatement();
		conn.setAutoCommit(false);

		//--------------
		// 获取recordtype及监测是否存在存储数据表
		// 存在删除计算行政区数据，不存在创建表
		//--------------
		String e_table = GlobalFuns.findCoreTable(dataSQL);
		String e_table_front = e_table.substring(0, e_table.lastIndexOf("_"));
		String typerecord = e_table.substring(e_table.lastIndexOf("_")+1);
		Map<String, String> tablefields = new HashMap<String, String>();
		//		ResultSet rs = ps.executeQuery(String.format("scaling.%s_%s_%s_%s", e_table_front,toscale,suffix,typerecord));
		ResultSet rs = conn.getMetaData().getTables(null, null, String.format("scaling.%s_%s_%s", e_table_front,suffix,typerecord), null);
		conn.commit();
		if (rs.next()) {
			//--------------------------
			//表存在，删除数据
			ps.executeUpdate(String.format(
					"delete %s where scaling_field='%s'",
					String.format("scaling.%s_%s_%s", e_table_front,suffix,typerecord),
					data_field
					));
			conn.commit();
		}
		else{
			//-------------create
			//表不存在，创建表
			String scaling_data_fields = "";
			//获取表字段结构
			ResultSet rs_create = ps.executeQuery("select * from "+e_table+" limit 1 offset 0");
			conn.commit();
			ResultSetMetaData rsMetaData_create = rs_create.getMetaData();
			int length = rsMetaData_create.getColumnCount();
			for (int i = 1; i <= length; i++) {
				if ("rid".equalsIgnoreCase(rsMetaData_create.getColumnName(i))) {
					continue;
				}
				scaling_data_fields += GlobalFuns.createSQLField( 
						rsMetaData_create.getColumnName(i),
						getTypeString(rsMetaData_create.getColumnType(i))
						) + ",";
				tablefields.put(rsMetaData_create.getColumnName(i), getTypeString(rsMetaData_create.getColumnType(i)));
			}
			if (scaling_data_fields.endsWith(",")) {
				scaling_data_fields += "scaling_field character varying";
				tablefields.put("scaling_field", "string");
				//创建表
				ps.execute(
						"select user_create_table('scaling','"+String.format("%s_%s_%s", e_table_front,suffix,typerecord)
						+"','"+scaling_data_fields
						+"');");
				//创建查询索引
//				ps.execute(
//						"select user_insert_scalingindex('"+String.format("scaling.%s_%s_%s", e_table_front,suffix,typerecord)
//						+"','"+typerecord
//						+"');");
				conn.commit();
			}
			rs_create.close();
		}
		rs.close();

		//-----------fromdata
		// 查询数据
		//--
		ResultSet rs_fromdata = ps.executeQuery(dataSQL);
		conn.commit();
		//存储 rs_fromdata 到 list
		List<Map<String, String>> fromdata = new ArrayList<Map<String,String>>();
		ResultSetMetaData rs_fromdata_meta = rs_fromdata.getMetaData();
		while (rs_fromdata.next()) {
			Map<String, String> fd_map = new HashMap<String, String>();
			for (int i = 1; i <= rs_fromdata_meta.getColumnCount(); i++) {
				fd_map.put(rs_fromdata_meta.getColumnName(i), rs_fromdata.getString(rs_fromdata_meta.getColumnName(i)));
			}
			fromdata.add(fd_map);
		}
		rs_fromdata.close();

		int size = fromdata.size();
		for (int i = 0; i < size; i++) {
			String geom_id = fromdata.get(i).get("geom_id");
			String fromValue = fromdata.get(i).get(data_field);
			String valueMaster = "";
			String fieldString = "";
			for (Map.Entry<String, String> entry : tablefields.entrySet()) {
				fieldString += entry.getKey() + ",";;
				if ("scaling_field".equalsIgnoreCase(entry.getKey())) {
					valueMaster += "'" + data_field + "',";
				}
				else if (data_field.equalsIgnoreCase(entry.getKey())) {
					valueMaster += "data_field_value" + ",";
				}
				else if ("geom_id".equalsIgnoreCase(entry.getKey())) {
					valueMaster += "'" + "geom_id_value" + "',";
				}
				else {
					String dataType = entry.getValue();
					String value = fromdata.get(i).get(entry.getKey());
					if (value == null) {
						value = "-";
					}
					if (dataType.equalsIgnoreCase("int")
							||dataType.equalsIgnoreCase("number")
							||dataType.equalsIgnoreCase("double")
							||dataType.equalsIgnoreCase("float")
							) {//数值类型
//						value = GlobalFuns.isThousandPointsType(value)?String.valueOf(new DecimalFormat().parse(value).doubleValue()):((value.equalsIgnoreCase("-")||value.isEmpty())?"null":value);
						value = (value.equalsIgnoreCase("-")||value.isEmpty())?"null":value.replaceAll(",", "");
						
						valueMaster += value + ",";
					}
					else if (dataType.equalsIgnoreCase("date")
							||dataType.equalsIgnoreCase("timestamp")) {
						valueMaster += ((value.equalsIgnoreCase("-")||value.isEmpty())?"null,":"'" + value + "',");
					}
					else if (dataType.equalsIgnoreCase("geom")) {
						valueMaster += ((value.equalsIgnoreCase("-")||value.isEmpty())?"null,": value + ",");
					}
					else {//非数值类型
						valueMaster += "'" + value + "',";
					}
				}
			}

			if (valueMaster.endsWith(",")) {
				valueMaster = valueMaster.substring(0,
						valueMaster.length() - 1);// 删除最后一个逗号
			}
			if (fieldString.endsWith(",")) {
				fieldString = fieldString.substring(0,
						fieldString.length() - 1);// 删除最后一个逗号
			}

			//-----------ratedata
			// 查询数据 比率数据表
			//--
			String ratedata_sql = "select to_geom_id,"+rate_field+" from "+rate_table+" where from_geom_id like '"+geom_id+"%'";

			//-----------
			// 降尺度计算
			//--
			String calculate_sql = String.format("select t.to_geom_id,t.%s*%s as value from (%s) t",rate_field,fromValue,ratedata_sql);
			//-----------insert
			// 结果记录
			//--
			Connection conn_disaster = DBUtils.getConnection();
			Statement ps_disaster = null;
			ResultSet rs_disaster = null;
			try {
				ps_disaster = conn_disaster.createStatement();
				System.out.println(calculate_sql);
				rs_disaster = ps_disaster.executeQuery(calculate_sql);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if (ps_disaster != null) {
					ps_disaster.close();
				}
				conn_disaster.close();
				throw new InsertException(e.getMessage());
			}

			int upload_i = 0;//记录上传行数
			while (rs_disaster.next()) {
				upload_i++;
				String valueString = valueMaster.replace("data_field_value", rs_disaster.getString("value"));
				valueString = valueString.replace("geom_id_value", rs_disaster.getString("to_geom_id"));
				String insert_sql = String.format(
						"insert into "+String.format("scaling.%s_%s_%s", e_table_front,suffix,typerecord)+"(%s) values(%s);",
						fieldString, valueString);
				System.out.println(insert_sql);
				ps.executeUpdate(insert_sql);
				if (upload_i % 500 == 0) {
					// 500条记录提交一次
					try {
						conn.commit();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						if (ps_disaster != null) {
							ps_disaster.close();
						}
						conn_disaster.close();
						throw new InsertException("计算结果第"+upload_i+"行:\n"+e.getMessage());
					}

					System.out.println("已成功计算" + upload_i + "行!");
				}
			}
			if (upload_i % 500 != 0) {
				// 不够500条的再提交一次（其实不用判断，直接提交就可以，不会重复提交的）
				try {
					conn.commit();
				} catch (SQLException e) {
					e.printStackTrace();
					throw new InsertException("计算结果第"+upload_i+"行:\n"+e.getMessage());
				}
				finally{
					if (ps_disaster != null) {
						ps_disaster.close();
					}
					conn_disaster.close();
				}
				System.out.println("已成功计算" + upload_i + "行!");
			}
		}

		ps.close();
		conn.close();

		return String.format("scaling.%s_%s_%s", e_table_front,suffix,typerecord);
	}

	

	public JSONBean statisticalAnalysis(
			String sql1,
			String spatial_f1,
			String sql2,
			String spatial_f2,
			String other) throws InsertException, SQLException{
		String t1_sql = "";
		String table1 = GlobalFuns.findCoreTable(sql1);
		if(table1.toLowerCase().indexOf("client_") == -1){
			JsonBeanDao jsonBeanDao = new JsonBeanDao(DBUtils.getConnection());
			Map<String, String> column_type_map1 = jsonBeanDao.getTableColumnTypeMapping(table1);
			String field_dblink_sql1 = "";
			String field1 = "";
			for (Entry<String, String> m :column_type_map1.entrySet())  {  
				field1 += m.getKey()+",";
				field_dblink_sql1 += GlobalFuns.createSQLField(
						m.getKey(),
						getPGTypeString(m.getValue())
						)+",";
			}
			// 删除最后一个逗号
			field_dblink_sql1 = field_dblink_sql1.substring(0,
					field_dblink_sql1.length() - 1);
			// 删除最后一个逗号
			field1 = field1.substring(0,
					field1.length() - 1);
			t1_sql = "select * from " + String.format(
					GlobalVariable.dbling_disater_db, 
					("select "+field1+sql1.substring(sql1.toLowerCase().indexOf(" from "))).replace("'", "''"),
					field_dblink_sql1
					);
		}
		else {
			t1_sql = sql1;
		}
		String t2_sql = "";
		String table2 = GlobalFuns.findCoreTable(sql2);
		if(table2.toLowerCase().indexOf("client_") == -1){
			JsonBeanDao jsonBeanDao = new JsonBeanDao(DBUtils.getConnection());
			Map<String, String> h_column_type_map2 = jsonBeanDao.getTableColumnTypeMapping(table2);
			String field_dblink_sql2 = "";
			String field2 = "";
			for (Entry<String, String> m2 :h_column_type_map2.entrySet())  {  
				field2 += m2.getKey()+",";
				field_dblink_sql2 += GlobalFuns.createSQLField(
						m2.getKey(),
						getPGTypeString(m2.getValue())
						)+",";
			} 
			// 删除最后一个逗号
			field_dblink_sql2 = field_dblink_sql2.substring(0,
					field_dblink_sql2.length() - 1);
			// 删除最后一个逗号
			field2 = field2.substring(0,
					field2.length() - 1);
			t2_sql = "select * from " + String.format(
					GlobalVariable.dbling_disater_db, 
					("select "+field2+sql2.substring(sql2.toLowerCase().indexOf(" from "))).replace("'", "''"),
					field_dblink_sql2
					);
		}
		else {
			t2_sql = sql2;
		}

//		String sql = String.format("SELECT * FROM (%s) t1,(%s) t2 where ST_Intersects(t1.%s,t2.%s)", t1_sql,t2_sql,spatial_f1,spatial_f2);
		String sql = "";
		if(spatial_f2.equalsIgnoreCase("point") && spatial_f1.equalsIgnoreCase("rast")){
			sql = String.format("SELECT st_value(t1.%s, st_transform(t2.geom, st_srid(t1.%s))) as rast_value,t2.* FROM (%s) t1,(%s) t2", spatial_f1,spatial_f1,t1_sql,t2_sql);
		}
		else if(spatial_f2.equalsIgnoreCase("point") && !spatial_f1.equalsIgnoreCase("rast")){
			sql = String.format("SELECT * FROM (%s) t1,(%s) t2 where ST_Intersects(t1.%s,t2.geom)", t1_sql,t2_sql,spatial_f1);
		}
		else {
			sql = String.format("SELECT * FROM (%s) t1,(%s) t2 where ST_Intersects(t1.%s,t2.%s)", t1_sql,t2_sql,spatial_f1,spatial_f2);
		}
		int count = jsDao.queryCount("select count(*) from ("+sql+") t", false);
		JSONBean jsonBean = jsDao.queryTable(sql+other);
		jsonBean.setDataCount(count);
		return jsonBean;
	}
	
	public String getExpoIndexTableCatalog()
	{
		String sqlString = "select distinct record_type_id as rtid"
				+" from"
				+" client_expo_record_type_setting";

//		if(!key.isEmpty())
//		{
//			sqlString += " where p.peril_id = r.id";
//			sqlString += " or p.insured like '%"+key+"%'";
//			sqlString += " or p.address like '%"+key+"%'";
//			sqlString += " or p.corp like '%"+key+"%'";
//			sqlString += " or p.corp_branch like '%"+key+"%'";
//		}
//		else 
//		{
//			sqlString += " where 1=0" ;
//		}
//		//		sqlString += " order by begin_time desc";
//		sqlString += " limit " + count + " offset " + startRow;
		JSONBean jsonBean = jsDao.queryTable(sqlString);
		return jsonBean.toJSONString();
	}
	
	public String getExpoIndexTableHeader(String rtid)
	{
		String sqlString = "select record_type, record_type_desc, field_name, field_title, field_type, field_unit, field_desc, field"
				+" from"
				+" client_expo_record_type_setting"
				+" limit 1";

		JSONBean jsonBean = jsDao.getTableStructure(sqlString);
		return jsonBean.toJSONString();
	}
	
	public String deleteExpoIndexTable(String rtid)
	{
		String sqlString = "delete"
				+" from"
				+" client_expo_record_type_setting"
				+" where record_type_id='"+rtid+"'";

		JSONBean jsonBean = new JSONBean();
		int r = jsDao.deleteAction(sqlString);
		if(r != -1){
			Map<String, String> result = new HashMap<String, String>();
			result.put("record_type_id", rtid);
			result.put("delrows", String.valueOf(r));
			jsonBean.setResult(result);
			jsonBean.setStatus(Status.SUCCESS);
		}
		else {
			jsonBean.setStatus(Status.FAILED);
		}
		return jsonBean.toJSONString();
	}
	
	//search: undefined, sort: undefined, order: "asc", limit: 10, offset: 0
	public String getExpoIndexTableData(String rtid, String sort, String order, String limit, String offset)
	{
		String orderby = "";
		if(sort != null && !sort.isEmpty()){
			orderby = " order by " + sort + " " + (order==null||order.isEmpty()?"asc":order);
		}
		String sqlString = "select record_type, record_type_desc, field_name, field_title, field_type, field_unit, field_desc, field"
				+" from"
				+" client_expo_record_type_setting"
				+" where record_type_id ='" + rtid + "'"
				+orderby
				+" limit " + limit + " offset " + offset;
		String sqlString_count = "select count(*) as count"
				+" from"
				+" client_expo_record_type_setting"
				+" where record_type_id ='" + rtid + "'";
//		if(!key.isEmpty())
//		{
//			sqlString += " where p.peril_id = r.id";
//			sqlString += " or p.insured like '%"+key+"%'";
//			sqlString += " or p.address like '%"+key+"%'";
//			sqlString += " or p.corp like '%"+key+"%'";
//			sqlString += " or p.corp_branch like '%"+key+"%'";
//		}
//		else 
//		{
//			sqlString += " where 1=0" ;
//		}
//		//		sqlString += " order by begin_time desc";
//		sqlString += " limit " + count + " offset " + startRow;
		JSONBean jsonBean = null;
		int count = jsDao.queryCount(sqlString_count, false);
		jsonBean = jsDao.queryTable(sqlString);
		jsonBean.setDataCount(count);
		return jsonBean.toJSONString();
	}
	
	public String getExpoDataCatalog()
	{
		String sqlString = 
				"SELECT "
				+ " i.* "
				+ "FROM "
				+ " ("
				+ "  SELECT "
				+ "   DISTINCT record_type_id AS rtid "
				+ "  FROM "
				+ "   client_expo_record_type_setting "
				+ " ) s, "
				+ " ("
				+ "  SELECT "
				+ "   *, SUBSTRING (import_table FROM (LENGTH (import_table) - POSITION ('_' IN reverse(import_table)) + 1)+1 FOR LENGTH (import_table)) AS rtid "
				+ "  FROM "
				+ "   client_data_import_record "
				+ "  WHERE import_table like '%_expo_%' "
				+ " ) i "
				+ "WHERE "
				+ " s.rtid = i.rtid "
				+ "ORDER BY "
				+ " i.rtid";
		
		JSONBean jsonBean = jsDao.queryTable(sqlString);
		return jsonBean.toJSONString();
	}
	
	public String getExpoData(String dt, String importid, String sort, String order, String limit, String offset)
	{
		String orderby = "";
		String limitStr = "";
		if(sort != null && !sort.isEmpty()){
			orderby = " order by " + sort + " " + (order==null||order.isEmpty()?"asc":order);
		}
		if(limit != null && !limit.isEmpty()){
			limitStr = " limit " + limit + " offset " + offset;
		}
		String sqlString = "select *"
				+" from"
				+" " + dt
				+((importid==null||importid.isEmpty())?"":" where import_id = " + importid + "")
				+orderby
				+limitStr;
		String sqlString_count = "select count(*) as count"
				+" from"
				+" " + dt
				+((importid==null||importid.isEmpty())?"":" where import_id = " + importid + "");
		JSONBean jsonBean = null;
		int count = jsDao.queryCount(sqlString_count, false);
		jsonBean = jsDao.queryTable(sqlString);
		jsonBean.setDataCount(count);
		return jsonBean.toJSONString();
	}
	
	public String getExpoDataHeader(String rtid)
	{
		String sqlString = "select record_type, record_type_desc, field_name, field_title, field_type, field_unit, field_desc, field"
				+" from"
				+" client_expo_record_type_setting"
				+" where record_type_id ='" + rtid + "'";

		JSONBean jsonBean = jsDao.queryTable(sqlString);
		return jsonBean.toJSONString();
	}
	
	public String getExpoDataProperty(String rtid)
	{
		String sqlString = "select property1, property2, property3, property4, property5, property6, "
				+ "property7, property8, property9, table_name_db, per_agg"
				+" from"
				+" client_expo_index_table"
				+" where record_type_id ='" + rtid + "'";

		JSONBean jsonBean = jsDao.queryTable(sqlString);
		return jsonBean.toJSONString();
	}
	
	public String getExpoDataGeoJson(String dt, String importid ) throws SQLException
	{
		Connection conn = DBUtils.getClientDBConnection();
		Statement ps = null;
		ps = conn.createStatement();
		String sqlhead = "select * "
				+" from"
				+" dic_fields_info"
				+" where tablename ='" + dt + "'";
		ResultSet rs = ps.executeQuery(sqlhead);
		String fields = "";
		if(rs.next())
		{
//			fields = rs.getString("titlename") == ""?rs.getString("fieldname"):(rs.getString("fieldname") + " as " + rs.getString("titlename"));
			fields = rs.getString("fieldname");
		}

		ps.close();
		conn.close();
		
		String sqlString = "select *"
				+" from"
				+" " + dt
				+((importid==null||importid.isEmpty())?"":" where import_id = " + importid + "");
		String sql = "SELECT row_to_json(fc) As json"
				+" FROM ( SELECT 'FeatureCollection' As type, array_to_json(array_agg(f)) As features"
				+" FROM (SELECT 'Feature' As type"
				+" , ST_AsGeoJSON(lg.geom)::json As geometry"
				+" , row_to_json((SELECT l FROM (SELECT "+fields+") As l"
				+" )) As properties"
				+" FROM ("+sqlString+") As lg   ) As f )  As fc";
		JSONBean jsonBean = null;

		jsonBean = jsDao.queryTable(sql);
		return jsonBean.toJSONString();
	}

	public String getExpoDataLatlng(String dt, String importid ) throws SQLException
	{
		String sqlString = "select *"
				+" from"
				+" " + dt
				+((importid==null||importid.isEmpty())?"":" where import_id = " + importid + "");
		String sql = "SELECT ST_AsLatLonText(geom,'DD.DDD') as latlng"
				+" FROM ("+sqlString+") As lg";
		JSONBean jsonBean = null;

		jsonBean = jsDao.queryTable(sql);
		return jsonBean.toJSONString();
	}

	/**
	 * -7    BIT    
	 * -6    TINYINT    
	 * -5    BIGINT    
	 * -4    LONGVARBINARY     
	 * -3    VARBINARY    
	 * -2    BINARY    
	 * -1    LONGVARCHAR    
	 * 0    NULL    
	 * 1    CHAR    
	 * 2    NUMERIC    
	 * 3    DECIMAL    
	 * 4    INTEGER    
	 * 5    SMALLINT    
	 * 6    FLOAT    
	 * 7    REAL    
	 * 8    DOUBLE    
	 * 12    VARCHAR    
	 * 91    DATE    
	 * 92    TIME    
	 * 93    TIMESTAMP    
	 * 1111     OTHER 
	 */
	private String getTypeString(int typeid)
	{
		String rString = "";
		switch (typeid) {
		case Types.VARCHAR:
		case Types.CHAR:
		case Types.LONGNVARCHAR:
		case Types.LONGVARCHAR:
		case Types.NVARCHAR:
			rString = "text";
			break;
		case Types.DECIMAL:
		case Types.DOUBLE:
		case Types.FLOAT:
		case Types.NUMERIC:
			rString = "number";
			break;
		case Types.INTEGER:
		case Types.SMALLINT:
			rString = "int";
			break;
		case Types.DATE:
			rString = "date";
			break;
		case Types.OTHER:
			rString = "geom";
			break;
		default:
			rString = "object";
			break;
		}
		return rString;
	}
	
	private String getPGTypeString(String typeStr)
	{
		String rString = "text";
		switch (typeStr) {
		case "text":
		case "varchar":
			rString = "text";
			break;
		case "numeric":
			rString = "number";
			break;
		case "int4":
			rString = "int";
			break;
		case "date":
			rString = "date";
			break;
		case "geometry":
			rString = "geom";
			break;
		case "raster":
			rString = "rast";
			break;
		}
		return rString;
	}
}
