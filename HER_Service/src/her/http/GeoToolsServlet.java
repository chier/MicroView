package her.http;

import her.bean.JSONBean;
import her.dao.impl.DomainDaoImpl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;

public class GeoToolsServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		this.doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		
		String action = request.getParameter("action");
		String jsonValueString = "";
		if(action.toLowerCase().equalsIgnoreCase("geteventcolumnlist"))
		{
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
					 SimpleFeatureSource featureSource = pgDatastore.getFeatureSource("city_400w");
				}
			} catch (IOException e) {  
				e.printStackTrace();  
				System.out.println("系统连接失败！请检查相关参数");  
			}
		}
		
		out.println(jsonValueString);
		out.flush();
		out.close();
	}
}
