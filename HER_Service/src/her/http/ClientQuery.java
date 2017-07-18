package her.http;

import her.bean.JSONBean;
import her.bean.JSONBean.Status;
import her.dao.impl.ExposureDaoImpl;
import her.dao.impl.ScalingImpl;
import her.dao.impl.UploadDaoImpl;
import her.dao.impl.WriteCSVToDBImpl;
import her.dao.impl.WriteExcelToDBImpl;
import her.utils.GlobalVariable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import jxl.read.biff.BiffException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.python.antlr.PythonParser.else_clause_return;

/**
 * Servlet implementation class FileUploaded
 */
@WebServlet("/ClientQuery")
public class ClientQuery extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
	 * @param request servlet request
	 * @param response servlet response
	 */
	// 定义文件的上传路径

	private String uploadPath = "d://a//";

	// 限制文件的上传大小
	private int maxPostSize = 100 * 1024 * 1024;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ClientQuery() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void destroy() {
		super.destroy();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		this.doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
		response.setHeader("Access-Control-Allow-Headers", "POWERED-BY-MENGXIANHUI");
		response.setHeader("Access-Control-Max-Age", "30");
		
		//为图片存储临时文件配置地址
		GlobalVariable.filePath = this.getServletConfig().getServletContext().getRealPath("tempfile/");

		PrintWriter out = response.getWriter();
		String action = request.getParameter("action");
		String jsonValueString = "";
		if(action.toLowerCase().equalsIgnoreCase("get_e_it_catalog"))//expo_index_table catalog
		{
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl(true);

			jsonValueString = exposureDaoImpl.getExpoIndexTableCatalog();
		}
		else if(action.toLowerCase().equalsIgnoreCase("delete_e_it"))//expo_index_table delete
		{
			String rtid = request.getParameter("rtid");
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl(true);

			jsonValueString = exposureDaoImpl.deleteExpoIndexTable(rtid);
		}
		else if(action.toLowerCase().equalsIgnoreCase("get_e_it_header"))//expo_index_table header
		{
			String rtid = request.getParameter("rtid");
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl(true);

			jsonValueString = exposureDaoImpl.getExpoIndexTableHeader(rtid);
		}
		else if(action.toLowerCase().equalsIgnoreCase("get_e_it_data"))//expo_index_table data
		{//配合 bootstrap table 使用
			String rtid = request.getParameter("rtid");
			String order = request.getParameter("order");
			String limit = request.getParameter("limit");
			String offset = request.getParameter("offset");
			String sort = request.getParameter("sort");
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl(true);

			jsonValueString = exposureDaoImpl.getExpoIndexTableData(rtid,sort,order,limit,offset);
		}
		else if(action.toLowerCase().equalsIgnoreCase("get_e_data_catalog"))//expo_data catalog
		{
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl(true);

			jsonValueString = exposureDaoImpl.getExpoDataCatalog();
		}
		else if(action.toLowerCase().equalsIgnoreCase("get_e_data"))//expo_data
		{//配合 bootstrap table 使用
			String dt = request.getParameter("dt");
			String importid = request.getParameter("importid");
			String order = request.getParameter("order");
			String limit = request.getParameter("limit");
			String offset = request.getParameter("offset");
			String sort = request.getParameter("sort");
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl(true);

			jsonValueString = exposureDaoImpl.getExpoData(dt,importid,sort,order,limit,offset);
		}
		else if(action.toLowerCase().equalsIgnoreCase("get_e_data_header"))//expo_data header
		{
			String rtid = request.getParameter("rtid");
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl(true);

			jsonValueString = exposureDaoImpl.getExpoDataHeader(rtid);
		}
		else if(action.toLowerCase().equalsIgnoreCase("get_e_data_property"))//expo_data property
		{
			String rtid = request.getParameter("rtid");
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl(true);

			jsonValueString = exposureDaoImpl.getExpoDataProperty(rtid);
		}
		else if(action.toLowerCase().equalsIgnoreCase("get_e_data_geojson"))//expo_data GeoJson
		{
			String dt = request.getParameter("dt");
			String importid = request.getParameter("importid");
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl(true);

			try {
				jsonValueString = exposureDaoImpl.getExpoDataGeoJson(dt, importid);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(action.toLowerCase().equalsIgnoreCase("get_e_data_latlng"))//expo_data GeoJson
		{
			String dt = request.getParameter("dt");
			String importid = request.getParameter("importid");
			ExposureDaoImpl exposureDaoImpl = new ExposureDaoImpl(true);

			try {
				jsonValueString = exposureDaoImpl.getExpoDataLatlng(dt, importid);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		out.write(jsonValueString);
		out.flush();
		out.close();
	}
}
