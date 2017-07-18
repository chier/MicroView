package her.http;

import her.bean.JSONBean;
import her.bean.JSONBean.Status;
import her.dao.impl.ScalingImpl;
import her.dao.impl.UploadDaoImpl;
import her.dao.impl.WriteCSVToDBImpl;
import her.dao.impl.WriteExcelToDBImpl;

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
@WebServlet("/FileUpload")
public class FileUpload extends HttpServlet {
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
	public FileUpload() {
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
		
		long start = System.currentTimeMillis(); //获取最初时间
		System.out.println("数据开始接收");
		processRequest(request, response);//上传excel
		long end=System.currentTimeMillis(); //获取运行结束时间
		System.out.println("数据导入结束，共用时间： "+(end-start)+"ms");
	}

	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String type = request.getParameter("upload_type");
		System.out.println("执行上传!");
		response.setContentType("application/json;charset=UTF-8");
		PrintWriter out = response.getWriter();

		JSONObject jsono = new JSONObject();
		UploadDaoImpl uploadDaoImpl = new UploadDaoImpl("");
		try {
			if("e_record".equalsIgnoreCase(type)){
				String recordTypeID = uploadDaoImpl.upLoadCSVRecord100(request);
				if(recordTypeID.isEmpty()){
					jsono.put("status", "0");
					jsono.put("msg", "非字段结构表（100表）！");
				}
				else {
					jsono.put("status", "1");
					jsono.put("url_type", "GET");
					jsono.put("url", "servlet/client_query?action=get_e_it_catalog");
				}
			}
			else if("e_data".equalsIgnoreCase(type)){
				Map<String, String> data = uploadDaoImpl.upLoadCSVRecordOther(request);
				if(data.isEmpty()){
					jsono.put("status", "0");
					jsono.put("msg", "表数据上传失败！");
				}
				else {
					jsono.put("status", "1");
					jsono.put("url_type", "GET");
					jsono.put("url", "client_query?action=get_e_data_catalog");
					jsono.put("import_id", data.get("import_id"));
					jsono.put("data_count", data.get("count"));
				}
			}
		} catch (FileUploadException e) {
			try {
				jsono.put("status", "0");
				jsono.put("msg", e.getMessage());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			throw new RuntimeException(e);
		} catch (Exception e) {
			try {
				jsono.put("status", "0");
				jsono.put("msg", e.getMessage());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			throw new RuntimeException(e);
		} finally {
			out.write(jsono.toString());
			out.close();
		}
	}

}
