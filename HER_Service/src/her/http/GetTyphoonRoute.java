package her.http;

import java.io.IOException;
import java.io.PrintWriter;

import her.bean.JSONBean;
import her.bean.JSONBean.Msg;
import her.bean.JSONBean.Status;
import her.dao.impl.TyphoonRouteDaoImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



public class GetTyphoonRoute extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		this.doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String typ_id = request.getParameter("typ_id");
		JSONBean jsonBean = new JSONBean();
		if (typ_id != null) {
			TyphoonRouteDaoImpl tr = new TyphoonRouteDaoImpl(typ_id);
			tr.ExecuteQuery();
			jsonBean = tr.getJsonBean();

		} else {// 如果没有typ_id，则不进行查询，返回一个空的json字符串

			jsonBean.setMsg(Msg.PARAMETER_ERROR);
			jsonBean.setStatus(Status.FAILED);

		}
		// 输出到response
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		out.println(jsonBean.toJSONString());
		out.flush();
		out.close();

	}

}
