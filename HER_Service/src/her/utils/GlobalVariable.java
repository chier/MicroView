package her.utils;

import java.util.HashMap;
import java.util.Map;

public class GlobalVariable extends DBUtils{
	public static String filePath = "";
	public static String dbling_disater_db = "dblink('hostaddr=[HOST] port=[PORT] dbname=[DB] user=Allenaya password=truth', '%s') t(%s)";
	public static String dbling_client_db = "dblink('hostaddr=[HOST] port=[PORT] dbname=[DB] user=[USER] password=[PW]', '%s') t(%s)";
	public static Map<String, String> db_use = new HashMap<String, String>();
	static {
		db_use.put("Disaster_database", getValue("host")+","+getValue("port")+","+"Allenaya,truth");
		db_use.put("Client_database", getValue("client_host")+","+getValue("client_port")+","+getValue("client_username")+","+getValue("client_password"));
		dbling_disater_db = dbling_disater_db.replace("[HOST]", getValue("host"));
		dbling_disater_db = dbling_disater_db.replace("[PORT]", getValue("port"));
		dbling_disater_db = dbling_disater_db.replace("[DB]", getValue("dbname"));
		
		dbling_client_db = dbling_client_db.replace("[HOST]", getValue("client_host"));
		dbling_client_db = dbling_client_db.replace("[PORT]", getValue("client_port"));
		dbling_client_db = dbling_client_db.replace("[DB]", getValue("client_dbname"));
		dbling_client_db = dbling_client_db.replace("[USER]", getValue("client_username"));
		dbling_client_db = dbling_client_db.replace("[PW]", getValue("client_password"));
	}
}
