package her.http;

import her.bean.JSONBean;
import her.bean.JSONBean.Msg;
import her.bean.JSONBean.Status;
import her.dao.JavaPythonDao;
import her.dao.impl.DomainDaoImpl;
import her.dao.impl.IdentifyDaoImpl;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.python.antlr.PythonParser.else_clause_return;

public class IdentifyServlet extends HttpServlet {

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
		JSONBean jsonBean = new JSONBean();
		String action = request.getParameter("action");
		if(action.equalsIgnoreCase("sqllayerident"))
		{
			String name = request.getParameter("name");
			String type = request.getParameter("type");
			String sqlTable = request.getParameter("sqlTable");
			String lon = request.getParameter("lon");
			String lat = request.getParameter("lat");
//			System.out.println(name+";"+type+";"+sqlTable+";"+lon+";"+lat);
			
			if (name != null && type != null && sqlTable != null && lon != null && lat != null) {
				IdentifyDaoImpl identifyImpl = new IdentifyDaoImpl();
				identifyImpl.sqlLayerIdentExecute(name, type, sqlTable, lon, lat);
				jsonBean = identifyImpl.getJsonBean();
			} else {
				jsonBean.setMsg(Msg.PARAMETER_ERROR);
				jsonBean.setStatus(Status.FAILED);
			}
			System.out.println(jsonBean.toJSONString());
		}
		else if(action.equalsIgnoreCase("sqlIdentify"))
		{
			String sqlTable = request.getParameter("sqlTable");
			String sqlField = request.getParameter("sqlField");
			String lon = request.getParameter("lon");
			String lat = request.getParameter("lat");
			if (sqlTable != null && lon != null && lat != null) {
				IdentifyDaoImpl identifyImpl = new IdentifyDaoImpl();
				identifyImpl.sqlLayerIdentify(sqlField, sqlTable, lon, lat);
				jsonBean = identifyImpl.getJsonBean();
			} else {
				jsonBean.setMsg(Msg.PARAMETER_ERROR);
				jsonBean.setStatus(Status.FAILED);
			}
			System.out.println(jsonBean.toJSONString());
		}
		else if(action.equalsIgnoreCase("policyaggrident"))
		{
			String lon = request.getParameter("lon");
			String lat = request.getParameter("lat");
			if (lon != null && lat != null) {
				IdentifyDaoImpl identifyImpl = new IdentifyDaoImpl();
				identifyImpl.policyAggrIdentExecute(lon, lat);
				jsonBean = identifyImpl.getJsonBean();
			} else {
				jsonBean.setMsg(Msg.PARAMETER_ERROR);
				jsonBean.setStatus(Status.FAILED);
			}
			System.out.println(jsonBean.toJSONString());
		}
		else if(action.toLowerCase().equalsIgnoreCase("identify_sql"))
		{
			String fields = request.getParameter("fields");
			String from = request.getParameter("from");
			String where = request.getParameter("where");
			String other = request.getParameter("other");
			DomainDaoImpl domainImpl = new DomainDaoImpl();
			jsonBean = domainImpl.querySQLSelectBean(fields,from,where,other);
		}
		else if(action.toLowerCase().equalsIgnoreCase("identify_chart"))
		{
			String sql = request.getParameter("sql");
			String xField = request.getParameter("xField");
			String yField = request.getParameter("yField");
//			String sql = "SELECT wnd,recordtime FROM eve_ty_bst_cma_sti_pnw where id < 1000;";
			JavaPythonDao pythonImpl = new JavaPythonDao();
			jsonBean = pythonImpl.buildChart(sql,xField,yField,"subplot");
		}
		
		// 输出到response
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		out.write(jsonBean.toJSONString());
		out.flush();
		out.close();
	}

}
