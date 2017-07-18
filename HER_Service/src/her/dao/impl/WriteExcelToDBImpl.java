package her.dao.impl;

import her.bean.JSONBean;
import her.dao.JsonBeanDao;
import her.utils.DBUtils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.python.antlr.PythonParser.varargslist_return;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import jxl.*;
import jxl.read.biff.BiffException;

public class WriteExcelToDBImpl {
	private Map<String, String> fieldMap = null;
	private Map<String, String> typeMap = null;
	private String intable = "";

	
	/**
	 * @param is
	 * @param type
	 * @param filename
	 * @param uploadman
	 * @param topic
	 * @return
	 * @throws BiffException
	 * @throws IOException
	 * @throws SQLException
	 */
	public JSONBean excel2DB(InputStream is, String type, String filename, String uploadman, String topic) 
			throws BiffException, IOException,SQLException {
		JSONBean result = null;
		Workbook workbook = Workbook.getWorkbook(is);
		Sheet sheet = workbook.getSheet(0);

		int colums = sheet.getColumns(); // 得到列数
		int rows = sheet.getRows(); // 得到行数

		String mappingxml = "Excel2DB_Aggr.xml";
		if(type.equalsIgnoreCase("aggr"))
		{
			mappingxml = "Excel2DB_Aggr.xml";
		}
		else if(type.equalsIgnoreCase("per")){
			mappingxml = "Excel2DB_Per.xml";
		}
		excel2dbmap(mappingxml);//加载字段映射配置文件
		// String fieldString = "";
		//
		// for (int i = 0; i < colums; i++) {
		// Cell cell0 = sheet.getCell(i, 0);// 取第0行表头信息
		// String field = cell0.getContents();
		// fieldString += field.trim() + ",";
		// }
		//
		int index = 0;
		Connection conn = null;
		Statement ps = null;
		ResultSet rs = null;
		String sqlString = "";
		try {
			conn = DBUtils.getClientDBConnection(); // 调用链接数据库的文件
			conn.setAutoCommit(false);
			ps = conn.createStatement();
			String cat_sql = String.format(
					"insert into client_expo_policy_catalog(topic,modify_time,upload_man,p_type,tablename,resource) values('%s','%s','%s','%s','%s','%s') returning id", 
					topic,"now()",uploadman,type,intable,filename);
			System.out.println(cat_sql);
			//存入catalog
//			ps.executeUpdate(cat_sql, Statement.RETURN_GENERATED_KEYS);
			rs = ps.executeQuery(cat_sql);
			if(rs.next())
			{
				String cat_id = rs.getString(1);//获取新加入的id值
				for (int i = 1; i < rows; i++) // 循环进行读写
				{
					String fieldString = "";
					String valueString = "";
					for (int j = 0; j < colums; j++) {
						Cell cell = sheet.getCell(j, i);
						String field = sheet.getCell(j, 0).getContents().trim();
						String value = cell.getContents();
						if (value == null || value.isEmpty()) {
							continue;
						}

						if(fieldMap.containsKey(field))
						{
							//======================
							// 随机经纬度
							//======================
							Random random = new Random();
							if(field.equalsIgnoreCase("lon"))
							{
								double lon = random.nextFloat()*30 + 80.0;
								value = String.valueOf(lon);
							}
							else if(field.equalsIgnoreCase("lat"))
							{
								double lat = random.nextFloat()*34 + 5.0;
								value = String.valueOf(lat);
							}
							//======================
							// 随机经纬度 END
							//======================

							if(fieldMap.get(field).isEmpty())
							{//数据库中没有该字段
								continue;
							}

							if(typeMap.get(field).equalsIgnoreCase("String"))
							{
								valueString += "'" + value + "',";
							}
							else if(typeMap.get(field).equalsIgnoreCase("Number")){
								valueString += (isNumeric(value)?value:"null") + ",";
							}

							fieldString += fieldMap.get(field) + ",";// 取第0行表头信息
						}
					}
					
//					if (fieldString.endsWith(",")) {
//						fieldString = fieldString.substring(0,
//								fieldString.length() - 1);// 删除最后一个逗号
//					}
//					if (valueString.endsWith(",")) {
//						valueString = valueString.substring(0,
//								valueString.length() - 1);// 删除最后一个逗号
//					}
					fieldString += "cat_id";//添加目录编号
					valueString += cat_id;
					
					index = i;

					sqlString = String.format(
							"insert into %s(%s) values(%s)", 
							intable,
							fieldString,
							valueString);

					ps.executeUpdate(sqlString);

					if (index % 500 == 0) {
						// 500条记录提交一次
						conn.commit();
						System.out.println("已成功提交" + index + "行!");
					}	
				}
				result = getInData(intable, cat_id);
			}
			if (index % 500 != 0) {
				// 不够500条的再提交一次（其实不用判断，直接提交就可以，不会重复提交的）
				conn.commit();
				System.out.println("已成功提交" + index + "行!");
			}
			rs.close();
			ps.close();
			conn.close();
		} catch (Exception ex) {
			System.out.println("导入第" + index + "条时出错");
			System.out.println("出错的sql语句是：" + sqlString);
			System.out.println("错误信息：");
			ex.printStackTrace();
			try {
				if (conn != null) {
					conn.rollback();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		workbook.close();
		return result;
	}

	private boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("-?[0-9]+.?[0-9]+");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}

	/**
	 * 将XML文件生成Document
	 * @return Map<String, String>
	 */
	private void excel2dbmap(String mappingxml)
	{
		fieldMap = new HashMap<String, String>();
		typeMap = new HashMap<String, String>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		Document doc = null;//xml文件对应的document
		//	    Element root = null;//xml文件的根结点
		try {
			builder = factory.newDocumentBuilder();
			InputStream xml_is = WriteExcelToDBImpl.class.getResourceAsStream("/her/res/"+mappingxml);
			doc = builder.parse(xml_is);
			//			root = doc.getDocumentElement();
			intable = ((Element)doc.getElementsByTagName("table").item(0)).getAttribute("name");
			NodeList list = doc.getElementsByTagName("map");
			int length = list.getLength();
			for (int i = 0; i < length; i++) {
				Element e = (Element) list.item(i);
				if (e == null) {
					break;
				}
				String from = e.getAttribute("from");
				String to = e.getAttribute("to");
				String type = e.getAttribute("type");
				fieldMap.put(from, to);
				typeMap.put(from, type);
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private JSONBean getInData(String tablename, String cat_id)
	{
		JsonBeanDao jsonDao = new JsonBeanDao();
		JSONBean jsonBean = jsonDao.queryTable("select * from "+tablename+" where cat_id="+cat_id);
		return jsonBean;
	}
}
