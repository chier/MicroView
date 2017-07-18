package her.http;

import her.bean.JSONBean;
import her.bean.JSONBean.Msg;
import her.bean.JSONBean.Status;
import her.dao.impl.RasterRainDaoImpl;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public abstract class RasterSessionBaseServlet extends HttpServlet {

	public RasterSessionBaseServlet() {
		super();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

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