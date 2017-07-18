package her.utils;

import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

public class JsonUtils {

	/**
	 * 
	 * @param request
	 * @return
	 */
	public JSONObject RequestJson2JavaJson(HttpServletRequest request) {
		try {
			String encoding = request.getCharacterEncoding();
			if (encoding == null) {
				encoding = "UTF-8";
			}
			InputStream ins = request.getInputStream();
			InputStreamReader insr = new InputStreamReader(ins, encoding);

			int readed = 0;
			int size = request.getContentLength();
			char[] buf = new char[size];

			while (readed < size) {
				int nextSize = size - readed;
				int nextReaded = insr.read(buf, readed, nextSize);
				if (nextReaded != -1) {
					readed += nextReaded;
				} else {
					break;
				}
			}
			System.out.println("Request JSON Value =" + String.valueOf(buf));
			JSONObject jsonReq = new JSONObject(String.valueOf(buf));
			return jsonReq;
		} catch (Exception e) {
			
		}
		return new JSONObject();
	}
}
