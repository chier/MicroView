package baidusearch;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 通过调用百度api查询地址获取经纬度
 * @author chen
 *
 */

public class SearchBYBaidu {
	private  String ak = "170102705114fade68a4280bae210cc3";//baidu api开发中心申请的
	/**
	 * 
	 * @param address 需要查询的地址
	 * @param region 指定区域内查询
	 */
	public JSONObject SearchPlace(String gps, String address, String region, String pageNum){
		/*
          q(query)
                            检索关键字，周边检索和矩形区域内检索支持多个关键字并集检索，
                            不同关键字间以$符号分隔，最多支持10个关键字检索。如:”银行$酒店”。 
          region 
                              检索区域（市级以上行政区域），如果取值为“全国”或某省份，则返回指定区域的POI及数量。  
          output
                             输出格式为json或者xml 
          scope 
                             检索结果详细程度。取值为1 或空，则返回基本信息；取值为2，返回检索POI详细信息 
          page_size 
                             范围记录数量，默认为10条记录，最大返回20条。多关键字检索时，返回的记录数为关键字个数*page_size。 
          ak
                               用户的访问密钥，必填项。v2之前该属性为key。 
		 */
		URL url;
		JSONObject jsonbaidu = null;
		JSONObject json = null;
		try {
			url = new URL("http://api.map.baidu.com/place/v2/search?query="
					+address+"&region="+region+"&output=json&scope=2&page_size=10&page_num="+pageNum+"&ak="+ak);
			URLConnection urlConnection = url.openConnection();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
			StringBuffer sBuffer = new StringBuffer();
			String res = null;
			while ((res = bufferedReader.readLine()) != null) {
				sBuffer.append(res);
			}
			jsonbaidu = new JSONObject(sBuffer.toString());
			if("84".equalsIgnoreCase(gps)){
				json = transformBDTo84(jsonbaidu);
			}
			else if ("02".equalsIgnoreCase(gps)) {
				json = transformBDTo02(jsonbaidu);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}
	/**
	 * 
	 * @param jsonObject 待转换坐标系的数据
	 * @return
	 */
	private JSONObject transformBDTo84(JSONObject jsonObject) {
		try {
			JSONArray results = jsonObject.getJSONArray("results");
			//			StringBuffer stringBuffer = new StringBuffer("{ \"type\": \"Point\",\"coordinates\": [");
			PositionUtil positionUtil = new PositionUtil();
			if(results.length() > 0 && ((JSONObject)results.get(0)).has("location")){

				int len = results.length();
				for (int i = 0; i < len; i++) {
					JSONObject jObject = (JSONObject) results.get(i);
					double lon = (double) jObject.getJSONObject("location").get("lng");
					double lat = (double) jObject.getJSONObject("location").get("lat");

					Gps gps02 = positionUtil.bd09_To_Gcj02(lat, lon);
					Gps gps84 = positionUtil.gcj_To_Gps84(gps02.getWgLat(), gps02.getWgLon());

					jObject.getJSONObject("location").put("lng", gps84.getWgLon());
					jObject.getJSONObject("location").put("lat", gps84.getWgLat());
					//				if (i == results.length()-1) {
					//					stringBuffer = stringBuffer.append( " [" +gps84.toString() +"]");
					//				}else {
					//					stringBuffer = stringBuffer.append( " [" +gps84.toString() +"],");
					//				}
				}
			}
			//			stringBuffer.append(" ] }");
			return jsonObject;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	private JSONObject transformBDTo02(JSONObject jsonObject) {
		try {
			JSONArray results = jsonObject.getJSONArray("results");
			//			StringBuffer stringBuffer = new StringBuffer("{ \"type\": \"Point\",\"coordinates\": [");
			PositionUtil positionUtil = new PositionUtil();
			if(results.length() > 0 && ((JSONObject)results.get(0)).has("location")){
				int len = results.length();
				for (int i = 0; i < len; i++) {
					JSONObject jObject = (JSONObject) results.get(i);
					double lon = (double) jObject.getJSONObject("location").get("lng");
					double lat = (double) jObject.getJSONObject("location").get("lat");

					Gps gps02 = positionUtil.bd09_To_Gcj02(lat, lon);

					jObject.getJSONObject("location").put("lng", gps02.getWgLon());
					jObject.getJSONObject("location").put("lat", gps02.getWgLat());
					//				if (i == results.length()-1) {
					//					stringBuffer = stringBuffer.append( " [" +gps84.toString() +"]");
					//				}else {
					//					stringBuffer = stringBuffer.append( " [" +gps84.toString() +"],");
					//				}
				}
			}
			//			stringBuffer.append(" ] }");
			return jsonObject;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}
}

