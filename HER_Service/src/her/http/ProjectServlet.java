package her.http;

import her.bean.JSONBean;
import her.bean.JSONBean.Status;
import her.dao.impl.DomainDaoImpl;
import her.dao.impl.ProjectDaoImpl;
import her.utils.GlobalVariable;
import her.utils.JsonUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProjectServlet extends HttpServlet {
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
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		
		//为图片存储临时文件配置地址
		GlobalVariable.filePath = this.getServletConfig().getServletContext().getRealPath("tempfile/");
		
		String action = request.getParameter("action");
		String jsonValueString = "";
		if(action.toLowerCase().equalsIgnoreCase("getprojectlist"))
		{
			String pagenum = request.getParameter("pagenum");
			ProjectDaoImpl projectDao = new ProjectDaoImpl(true);
			JSONBean jsonBean = projectDao.getProjectList(pagenum);
			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("getprojectlistbykey"))
		{
			String key = request.getParameter("key");
			String pagenum = request.getParameter("pagenum");
			ProjectDaoImpl projectDao = new ProjectDaoImpl(true);
			JSONBean jsonBean = projectDao.getProjectListByKey(key, pagenum);
			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("saveproject"))
		{
			JSONBean jsonBean = null;
			String data = request.getParameter("data");
			JSONObject json = null;
			try {
				json = new JSONObject(data);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				jsonBean = new JSONBean();
				jsonBean.setMsg(e1.getMessage());
				jsonBean.setStatus(Status.FAILED);
			}
	        ProjectDaoImpl projectDao = new ProjectDaoImpl(true);
			try {
				jsonBean = projectDao.saveProject(
						json.getString("id"), 
						json.getString("pname"), 
						json.getString("pcreateman"), 
						json.getString("pdesc"), 
						new JSONArray(json.getString("children")));
			} catch (SQLException | JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				jsonBean = new JSONBean();
				jsonBean.setMsg(e.getMessage());
				jsonBean.setStatus(Status.FAILED);
			}

			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("deleteproject"))
		{
			String project_id = request.getParameter("project_id");
			ProjectDaoImpl projectDao = new ProjectDaoImpl(true);
			JSONBean jsonBean = projectDao.deleteProject(project_id);
			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("getprojectdata"))
		{
			String project_id = request.getParameter("project_id");
			ProjectDaoImpl projectDao = new ProjectDaoImpl(true);
			JSONBean jsonBean = projectDao.getProjectData(project_id);
			jsonValueString = jsonBean.toJSONString();
		}
		
		out.write(jsonValueString);
		out.flush();
		out.close();
	}
}
