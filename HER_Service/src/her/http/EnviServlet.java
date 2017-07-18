package her.http;

import her.bean.JSONBean;
import her.bean.JSONBean.Status;
import her.dao.impl.DomainDaoImpl;
import her.dao.impl.EnviDaoImpl;
import her.utils.GlobalVariable;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;

public class EnviServlet extends HttpServlet {
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
		
		//为图片存储临时文件配置地址
		GlobalVariable.filePath = this.getServletConfig().getServletContext().getRealPath("tempfile/");
		
		String action = request.getParameter("action");
		String jsonValueString = "";
		if(action.toLowerCase().equalsIgnoreCase("getenvironmentlist"))
		{
			EnviDaoImpl enviDao = new EnviDaoImpl();
			JSONBean jsonBean = enviDao.getEnvironmentList();
			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("gettileinfos"))
		{
			String groupname = request.getParameter("groupname");
			EnviDaoImpl enviDao = new EnviDaoImpl();
			JSONBean jsonBean = enviDao.getTileInfos(groupname);
			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("querytilebyext"))
		{
			String tablename = request.getParameter("tablename");
			String ext = request.getParameter("ext");
			String queryID = request.getParameter("queryid");
			EnviDaoImpl enviDao = new EnviDaoImpl();
			JSONBean jsonBean = enviDao.queryTileByEXT(tablename, ext);
			if(jsonBean != null && queryID != null){
				Map<String, Object> result = (Map<String, Object>) jsonBean.getResult();
				result.put("queryid", queryID);
			}
			
			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("querytilebyext2"))
		{
			String tablename = request.getParameter("tablename");
			String xmin = request.getParameter("xmin");
			String ymin = request.getParameter("ymin");
			String xmax = request.getParameter("xmax");
			String ymax = request.getParameter("ymax");
			String queryID = request.getParameter("queryid");
			EnviDaoImpl enviDao = new EnviDaoImpl();
			JSONBean jsonBean = enviDao.queryTileByEXT(tablename,xmin,ymin,xmax,ymax);
			if(jsonBean != null && queryID != null){
				Map<String, Object> result = (Map<String, Object>) jsonBean.getResult();
				if(result == null)
				{
					result = new HashMap<String, Object>();
					result.put("data", null);
					result.put("queryid", queryID);
					
					jsonBean.setResult(result);
					jsonBean.setStatus(Status.SUCCESS);
				}
				else
					result.put("queryid", queryID);
			}

			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("gettile"))
		{

		}
		
		out.write(jsonValueString);
		out.flush();
		out.close();
	}
}
