package her.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Administrator
 *
 */
public class GlobalFuns {
	public static String getUUID(){  
		return  java.util.UUID.randomUUID().toString().replaceAll("-", "");  
	}

	// 过滤特殊字符  
	public static String stringFilter(String str) throws PatternSyntaxException {    
		// 只允许字母和数字       
		// String   regEx  =  "[^a-zA-Z0-9]";                     
		// 清除掉所有特殊字符  
		String regEx="[`~!@#$%^&*()+-=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";  
		Pattern p = Pattern.compile(regEx);     
		Matcher m = p.matcher(str);
		return m.replaceAll("").replaceAll("\\s*", "");     
	}

	public static boolean containSpecialChar(String str){
		if(str.replaceAll("^[A-Za-z0-9_]+$", "").length() == 0)
			return false;
		else 
			return true;
	}

	//是否为千分单位格式字符串
	public static boolean isThousandPointsType(String str){
		if (str.isEmpty()) {
			return false;
		}
		String s = str.replaceAll("-", "");
		Pattern pattern = Pattern.compile("([0-9][,]?)*"); 
		Matcher isNum = pattern.matcher(s);
		if( !isNum.matches() ){
			return false; 
		} 
		return true; 
	}

	/**
	 * 从复合查询语句(sql)中查出核心表名
	 * @param sql
	 * @return T1,T2,...
	 */
	public static String findCoreTable(String sql){
		if (sql.toLowerCase().contains(" from ")) {
			String from_sql = sql.substring(sql.toLowerCase().indexOf(" from ")+6);
			if(from_sql.toLowerCase().startsWith("(")){
				return findCoreTable(from_sql);
			}else {
				if(sql.toLowerCase().contains(" in ")){
					sql = sql.substring(0, sql.toLowerCase().indexOf(" in "));
				}
				String[] tables = sql.split(",");
				String table = "";
				for (int i = 0; i < tables.length; i++) {
					String temp = tables[i].substring(tables[i].toLowerCase().indexOf(" from ")+6);
					String t = temp.substring(0, temp.indexOf(" ")==-1?temp.length():temp.indexOf(" "));
					if (t.endsWith(")")) {
						t = t.substring(0, t.length() - 1);
					}
					table += t + ",";
				}
				if(!table.isEmpty())
					table = table.substring(0, table.length()-1);

				return table;
			}
		}
		else {
			return sql;
		}
	}

	public static String findCoreTable2(String sql){
		Pattern p = Pattern.compile("(.*from\\s)(\\w*)(.*)"); 
		Matcher m = p.matcher(sql); 
		if(m.find()){ 
			return m.group(2); 
		}else{ 
			return sql;
		}
	}

	public static String createSQLField(String fieldname, String type)
	{
		String sqlfield = fieldname.toLowerCase();
		switch (type) {
		case "string":
			sqlfield = fieldname.toLowerCase() + " character varying";
			break;
		case "text":
			sqlfield = fieldname.toLowerCase() + " text";
			break;
		case "number":
			sqlfield = fieldname.toLowerCase() + " numeric";
			break;
		case "int":
			sqlfield = fieldname.toLowerCase() + " integer";
			break;
		case "date":
			sqlfield = fieldname.toLowerCase() + " date";
			break;
		case "time":
			sqlfield = fieldname.toLowerCase() + " time";
			break;
		case "timestamp":
			sqlfield = fieldname.toLowerCase() + " timestamp";
			break;
		case "geom":
			sqlfield = fieldname.toLowerCase() + " geometry";
			break;
		case "rast":
			sqlfield = fieldname.toLowerCase() + " raster";
			break;
		default:
			break;
		}
		return sqlfield;
	}

	public static String dblingUse(String sql)
	{
		if(sql.toLowerCase().contains("dblink(")){
			String dbname = "";
			String[] dbinfo;
			if(sql.toLowerCase().contains(" dbname=")){
				dbname = sql.substring(sql.indexOf(" dbname=")+8);
				dbname = dbname.substring(0,dbname.indexOf(" "));
				dbinfo = GlobalVariable.db_use.get(dbname).split(",");//[host],[port],[user],[pw]
			}
			else {
				return sql;
			}
			if(sql.toLowerCase().contains("[host]")){
				sql = sql.replace("[host]", dbinfo[0]);
			}
			if(sql.toLowerCase().contains("[port]")){
				sql = sql.replace("[port]", dbinfo[1]);
			}
			if(sql.toLowerCase().contains("[user]")){
				sql = sql.replace("[user]", dbinfo[2]);
			}
			if (sql.toLowerCase().contains("[password]")) {
				sql = sql.replace("[password]", dbinfo[3]);
			}
		}
		return sql;
	}
}
