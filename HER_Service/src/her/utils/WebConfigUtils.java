package her.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WebConfigUtils {
	private static Log log = LogFactory.getLog(WebConfigUtils.class);

	public static String getValue(String key){
		String returnString = "";
		InputStream inputStream = DBUtils.class.getResourceAsStream("/her/res/WebConfig.properties");
		Properties prop = new Properties();
		try {
			prop.load(inputStream);
			returnString= prop.getProperty(key);
		} catch (IOException e) {
			log.info("WebConfig配置文件错误...");
		}
		
		return returnString;
	}
}
