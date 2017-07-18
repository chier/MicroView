package her.http;

import her.bean.JSONBean;
import her.dao.impl.DomainDaoImpl;
import her.dao.impl.HazardDaoImpl;
import her.utils.GlobalVariable;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class HazardServlet extends HttpServlet {

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

		if(action.toLowerCase().equalsIgnoreCase("queryeventbytype"))
		{
			String disastertypeid = request.getParameter("disastertypeid");
//			disastertype = new String(disastertype.getBytes("ISO8859-1"),"utf-8");
			String top = request.getParameter("top");
			HazardDaoImpl hazardDaoImpl = new HazardDaoImpl();
			JSONBean jsonBean = hazardDaoImpl.queryEventByType(disastertypeid, top);

			jsonValueString = jsonBean.toJSONString();
//			System.out.println(jsonValueString);
		}
		else if(action.toLowerCase().equalsIgnoreCase("queryeventbytypepage"))
		{
			String disastertypeid = request.getParameter("disastertypeid");
			String startRow = request.getParameter("startRow");
			String count = request.getParameter("count");
			HazardDaoImpl hazardDaoImpl = new HazardDaoImpl();
			JSONBean jsonBean = hazardDaoImpl.queryEventByTypePage(disastertypeid, startRow, count);
			jsonValueString = jsonBean.toJSONString();
			System.out.println(jsonValueString);
		}
		else if(action.toLowerCase().equalsIgnoreCase("querydisasterhazardmapping"))
		{
			String disastertypeid = request.getParameter("disastertypeid");

			HazardDaoImpl hazardDaoImpl = new HazardDaoImpl();
			JSONBean jsonBean = hazardDaoImpl.queryDisasterHazardMapping(disastertypeid);
			jsonValueString = jsonBean.toJSONString();
			System.out.println(jsonValueString);
		}
		else if(action.toLowerCase().equalsIgnoreCase("queryhazardinfo"))
		{
			String hazardtable = request.getParameter("hazardtable");
			String starttime = request.getParameter("starttime");
			String endtime = request.getParameter("endtime");
//			String hazardfield = request.getParameter("hazardfield");
			String hazardfieldtype = request.getParameter("hazardfieldtype");
			String renderersql = request.getParameter("renderersql");

			System.out.println(hazardfieldtype);
			HazardDaoImpl hazardDaoImpl = new HazardDaoImpl();
			JSONBean jsonBean = hazardDaoImpl.queryhazardinfo(
					hazardtable,
					hazardfieldtype,
					starttime,
					endtime,
					renderersql==null?"":renderersql);
			if(hazardfieldtype.equalsIgnoreCase("raster"))
				this.SetSession(request, "imagefilename", jsonBean.getImageSession());
			
			jsonValueString = jsonBean.toJSONString();
//			System.out.println(jsonValueString);
		}
		//查询灾害类型
		else if(action.toLowerCase().equalsIgnoreCase("queryalldisastertype"))
		{
			HazardDaoImpl hazardDaoImpl = new HazardDaoImpl();
			JSONBean jsonBean = hazardDaoImpl.queryAllDisasterType();
			jsonValueString = jsonBean.toJSONString();
		}
		//查询事件详细信息
		else if(action.toLowerCase().equalsIgnoreCase("querydetailsinfo"))
		{
			HazardDaoImpl hazardDaoImpl = new HazardDaoImpl();
			String eventids = request.getParameter("eventids");
			String tablename = request.getParameter("tablename");
			String fields = request.getParameter("fields");
			JSONBean jsonBean = hazardDaoImpl.getDetailsInfo(eventids, tablename, fields);
			jsonValueString = jsonBean.toJSONString();
		}
		//查询条件
		else if(action.toLowerCase().equalsIgnoreCase("getquerycondition_h"))
		{
			DomainDaoImpl domainDaoImpl = new DomainDaoImpl();
			JSONBean jsonBean = domainDaoImpl.getQueryCondition_h();
			jsonValueString = jsonBean.toJSONString();
		}
		//查询条件
		else if(action.toLowerCase().equalsIgnoreCase("getqueryconditionitem_h"))
		{
			String retrunField = request.getParameter("retrunField");
			String whereField = request.getParameter("whereField");
//			SELECT distinct replace(hdata_type,'*He','hee')
//			  FROM her_query_condition_item_h;
			DomainDaoImpl domainDaoImpl = new DomainDaoImpl();
			JSONBean jsonBean = domainDaoImpl.getQueryConditionItem_h(retrunField, whereField);
			jsonValueString = jsonBean.toJSONString();
		}
		//区域条件
		else if(action.toLowerCase().equalsIgnoreCase("getregion_xzqh"))
		{
			String code = request.getParameter("code");
			DomainDaoImpl domainDaoImpl = new DomainDaoImpl();
			JSONBean jsonBean = domainDaoImpl.getRegion_xzqh(code);
			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("query_h"))
		{
			String fields = request.getParameter("fields");
			String from = request.getParameter("from");
			String where = request.getParameter("where");
			String other = request.getParameter("other");
			HazardDaoImpl hazardDaoImpl = new HazardDaoImpl();
			JSONBean jsonBean = hazardDaoImpl.queryTestBean(fields,from,where,other);
			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("query_h_nocoluminfo"))
		{
			String fields = request.getParameter("fields");
			String from = request.getParameter("from");
			String where = request.getParameter("where");
			String other = request.getParameter("other");
			HazardDaoImpl hazardDaoImpl = new HazardDaoImpl();
			JSONBean jsonBean = hazardDaoImpl.queryHazardData(fields,from,where,other);
			jsonValueString = jsonBean.toJSONString();
		}
		else if(action.toLowerCase().equalsIgnoreCase("translation_disaster"))
		{
			String disasterids = request.getParameter("disasterids");
			HazardDaoImpl hazardDaoImpl = new HazardDaoImpl();
			JSONBean jsonBean = hazardDaoImpl.translation_disaster(disasterids);
			jsonValueString = jsonBean.toJSONString();
		}
				
		
		out.write(jsonValueString);
		out.flush();
		out.close();
	}
	
	public void SetSession(HttpServletRequest request, String key, String value) {
		HttpSession session = request.getSession();
		if (session.isNew()) {
			session.setAttribute(key, value);
		} else {
			session.invalidate();
			session = request.getSession();
			session.setAttribute(key, value);
		}
	}
}
