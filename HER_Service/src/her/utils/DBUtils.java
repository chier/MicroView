package her.utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DBUtils {
	private static Log log = LogFactory.getLog(DBUtils.class);
	
	private static String DRIVER = "";
	private static String HOST = "";
	private static String PORT = "";
	private static String DBNAME = "";
	private static String USERNAME = "Allenaya";
	private static String PASSWORD = "truth";
	
	private static String Client_DRIVER = "";
	private static String Client_HOST = "";
	private static String Client_PORT = "";
	private static String Client_DBNAME = "";
	private static String Client_USERNAME = "";
	private static String Client_PASSWORD = "";
	
	
	private static String ORACLE_DRIVER = "";
	private static String ORACLE_HOST = "";
	private static String ORACLE_PORT = "";
	private static String ORACLE_DBNAME = "";
	private static String ORACLE_USERNAME = "";
	private static String ORACLE_PASSWORD = "";
	
	static{
		InputStream inputStream = DBUtils.class.getResourceAsStream("/her/res/DBParam.properties");
		Properties prop = new Properties();
		try {
			prop.load(inputStream);
			DRIVER = prop.getProperty("driver");
			HOST = prop.getProperty("host");
			PORT = prop.getProperty("port");
			DBNAME = prop.getProperty("dbname");
			
			Client_DRIVER = prop.getProperty("client_driver");
			Client_HOST = prop.getProperty("client_host");
			Client_PORT = prop.getProperty("client_port");
			Client_DBNAME = prop.getProperty("client_dbname");
			Client_USERNAME = prop.getProperty("client_username");
			Client_PASSWORD = prop.getProperty("client_password");
			
			
			ORACLE_DRIVER = prop.getProperty("oracle_driver");
			ORACLE_HOST = prop.getProperty("oracle_host");
			ORACLE_PORT = prop.getProperty("oracle_port");
			ORACLE_DBNAME = prop.getProperty("oracle_dbname");
			ORACLE_USERNAME = prop.getProperty("oracle_username");
			ORACLE_PASSWORD = prop.getProperty("oracle_password");
			
		} catch (IOException e) {
			log.info("加载数据库配置文件错误...");
			
		}
	}
	
	public static String getValue(String key){
		String returnString = "";
		InputStream inputStream = DBUtils.class.getResourceAsStream("/her/res/DBParam.properties");
		Properties prop = new Properties();
		try {
			prop.load(inputStream);
			returnString= prop.getProperty(key);
		} catch (IOException e) {
			log.info("DBParam 配置文件错误...");
		}
		
		return returnString;
	}
	
	/**
	 * 
	 * @return java.sql.Connection
	 */
	public static Connection getConnection(){
		return getConnection("oracle");
	}
	
	/**
	 * 
	 * @return java.sql.Connection
	 */
	public static Connection getConnection(String dbType){
		Connection connection = null;
		//System.out.println(DRIVER + ";"+HOST + ";"+ PORT+ ";"+DBNAME + ";"+USERNAME + ";"+ PASSWORD+ ";");
		try {
			
			if(dbType == null  || "".equals(dbType) || "oracle".equalsIgnoreCase(dbType)){
				Class.forName(ORACLE_DRIVER);//指定连接类型jdbc:postgresql://
//		        String url = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DBNAME;
		        String url = "jdbc:oracle:thin:@//" + ORACLE_HOST + ":" 
		        			+ORACLE_PORT+"/" + ORACLE_DBNAME +"";
		        connection = DriverManager.getConnection(url, ORACLE_USERNAME, ORACLE_PASSWORD);
			}
			if("postgresql".equalsIgnoreCase(dbType)){
				try {
		            Class.forName(DRIVER);//指定连接类型jdbc:postgresql://
		            String url = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DBNAME;
		            connection = DriverManager.getConnection(url, USERNAME, PASSWORD);
				}catch(Exception e){
					log.info("获取数据库连接异常...");
					e.printStackTrace();
				}
			}
		}catch(Exception e){
			log.info("获取数据库连接异常...");
			e.printStackTrace();
		}
		return connection;
	}
	
	
	public static void close(ResultSet rs){
		if(rs != null){
			try {
				rs.close();
			} catch (SQLException e) {
				log.info("关闭ResultSet异常...");
			}
		}
	}

	public static void close(Statement stmt){
		if(stmt != null){
			try {
				stmt.close();
			} catch (SQLException e) {
				log.info("关闭Statement异常...");
			}
		}
	}
	
	public static void close(PreparedStatement pstmt){
		if(pstmt != null){
			try {
				pstmt.close();
			} catch (SQLException e) {
				log.info("关闭PreparedStatement异常...");
			}
		}
	}
	
	public static void close(Connection connection){
		if(connection != null){
			try {
				connection.close();
			} catch (SQLException e) {
				log.info("关闭Connection异常...");
			}
		}
	}
	
	/**
	 * 连接用户数据库
	 * @return java.sql.Connection
	 */
	public static Connection getClientDBConnection(){
		Connection connection = null;
		System.out.println("用户数据库："+Client_DRIVER + ";"+Client_HOST + ";"+ Client_PORT+ ";"+Client_DBNAME + ";"+Client_USERNAME + ";"+ Client_PASSWORD+ ";");
		try {  
            Class.forName(Client_DRIVER);//指定连接类型jdbc:postgresql://
            String url = "jdbc:postgresql://" + Client_HOST + ":" + Client_PORT + "/" + Client_DBNAME;
            connection = DriverManager.getConnection(url, Client_USERNAME, Client_PASSWORD);
		}catch(Exception e){
			log.info("获取用户数据库连接异常...");
			e.printStackTrace();
		}
		return connection;
	}
	
	public static void closeAll(Connection conn, PreparedStatement ps,
			ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
