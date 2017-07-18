package her.http;

import her.bean.JSONBean;
import her.bean.JSONBean.Msg;
import her.bean.JSONBean.Status;
import her.dao.impl.RasterRainDaoImpl;
import her.dao.impl.RasterTemperatureDaoImpl;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("unused")
public class GetRasterTemperature extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		this.doPost(request, response);
	}


	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String record_date = request.getParameter("date");
		String value_type = request.getParameter("value_type");
		JSONBean jsonBean = new JSONBean();
		if (record_date == null || value_type == null) {
			jsonBean.setStatus(Status.FAILED);
			jsonBean.setMsg(Msg.PARAMETER_ERROR);
			jsonBean.setResult(null);
		} else {
			RasterTemperatureDaoImpl rasterTemperatureDaoImpl = new RasterTemperatureDaoImpl();
			rasterTemperatureDaoImpl.ExecuteQuery(record_date,value_type);
			jsonBean = rasterTemperatureDaoImpl.getJsonBean();
		}
		// 输出到response
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		out.println(jsonBean.toJSONString());
		out.flush();
		out.close();
	}

}
