package her.http;

import her.bean.JSONBean;
import her.bean.JSONBean.Msg;
import her.bean.JSONBean.Status;
import her.dao.impl.TyphoonListDaoImpl;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



public class GetTyphoonList extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		this.doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		int method = Integer
				.parseInt(request.getParameter("method") == null ? "0"
						: request.getParameter("method"));
		String param = request.getParameter("param");
		JSONBean jsonBean = new JSONBean();

		if (param != null && method != 0) {
			TyphoonListDaoImpl tl = new TyphoonListDaoImpl();
			tl.ExecuteQuery(method, param);
			jsonBean = tl.getJsonBean();
		} else {
			jsonBean.setStatus(Status.FAILED);
			jsonBean.setMsg(Msg.PARAMETER_ERROR);

		}
		// 把jsonbean对象转换成json对象
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		out.print(jsonBean.toJSONString());
		out.flush();
		out.close();
	}

}
