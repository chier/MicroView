package geotools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;

public class Snippet {
	private static void conn(String dbtype, String host, String port,  
			String database, String userName, String password) {  
		//	        Map<String, Object> params = new HashMap<String, Object>();  
		// params.put(PostgisNGDataStoreFactory.DBTYPE.key, "postgis");    // 两种代码方式  
		// params.put(PostgisNGDataStoreFactory.HOST.key, "localhost");  
		// params.put(PostgisNGDataStoreFactory.PORT.key, new Integer(5432));  
		// params.put(PostgisNGDataStoreFactory.DATABASE.key, "postgis");  
		// params.put(PostgisNGDataStoreFactory.SCHEMA.key, "public");  
		// params.put(PostgisNGDataStoreFactory.USER.key, "postgres");  
		// params.put(PostgisNGDataStoreFactory.PASSWD.key, "root");
		Map<String,Object> params = new HashMap<String,Object>();
		params.put( "dbtype", "postgis");
		params.put( "host", "192.168.1.100");
		params.put( "port", 5432);
		params.put( "schema", "public");
		params.put( "database", "her_business");
		params.put( "user", "Allenaya");
		params.put( "passwd", "truth");

		try {
			//DataStore在 gt-api-13.1.jar 包中
			//DataStoreFinder 在 gt-main-13.1.jar 包中
			DataStore pgDatastore = DataStoreFinder.getDataStore(params);
			if (pgDatastore != null) {  
				System.out.println("系统连接到位于：" + host + "的空间数据库" + database  
						+ "成功！");  
			} else {  
				System.out.println("系统连接到位于：" + host + "的空间数据库" + database  
						+ "失败！请检查相关参数");  
			}  
		} catch (IOException e) {  
			e.printStackTrace();  
			System.out.println("系统连接到位于：" + host + "的空间数据库" + database  
					+ "失败！请检查相关参数");  
		}  

	}
	
	//2.1 查询 
	//SimpleFeature gt-opengis-13.1.jar
	public static ArrayList<SimpleFeature> queryMethod(
			DataStore pgDatastore,
			String filterStr,  
            String layerName) {  
        //pgDatastore为上文连接数据库获取相当于AE中的workspace  
        //SimpleFeatureSource相当于AE中的featureClass  
		//SimpleFeatureSource gt-djbc-postgis-13.1.jar
        SimpleFeatureSource featureSource = null;
		try {
			featureSource = pgDatastore.getFeatureSource(layerName);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
        ArrayList<SimpleFeature> featureList = new ArrayList<SimpleFeature>();  
        if(featureSource==null)  
            return featureList;  
        try {  
            Filter filter;
            //CQL gt-cql-13.1.jar
            filter = CQL.toFilter(filterStr); // filterStr形式 如  name='武汉大学' or code like 'tt123%'  
            SimpleFeatureCollection result = featureSource.getFeatures(filter);  
  
            FeatureIterator<SimpleFeature> itertor = result.features();  
            while (itertor.hasNext()) {
                SimpleFeature feature = itertor.next();
                featureList.add(feature);
            }  
            itertor.close();
            return featureList;  
        } catch (CQLException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        } catch (IOException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        }  
        return null;  
    }
}

