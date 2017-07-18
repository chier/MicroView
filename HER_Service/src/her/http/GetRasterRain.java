package her.http;

import her.bean.JSONBean;
import her.bean.JSONBean.Msg;
import her.bean.JSONBean.Status;
import her.dao.impl.RasterRainDaoImpl;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;




public class GetRasterRain extends RasterSessionBaseServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		this.doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String record_date = request.getParameter("date");
		JSONBean jsonBean = new JSONBean();
		if (record_date == null) {
			jsonBean.setStatus(Status.FAILED);
			jsonBean.setMsg(Msg.PARAMETER_ERROR);
			jsonBean.setResult(null);
		} else {
			RasterRainDaoImpl rasterRainDaoImpl = new RasterRainDaoImpl();
			rasterRainDaoImpl.ExecuteQuery(record_date);
			jsonBean = rasterRainDaoImpl.getJsonBean();
			this.SetSession(request, "imagefilename",rasterRainDaoImpl.getImageSession());
		}
		// 输出到response
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		out.println(jsonBean.toJSONString());
		out.flush();
		out.close();
	}
	
	
}
