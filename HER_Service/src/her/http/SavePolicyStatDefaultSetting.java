package her.http;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.corba.se.spi.orbutil.fsm.Guard.Result;

public class SavePolicyStatDefaultSetting extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String filePath = "";

	/**
	 * Constructor of the object.
	 */
	public SavePolicyStatDefaultSetting() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String defaultSet = request.getParameter("defaultSet");
		String selectedTable = request.getParameter("selectedTable");

		//上传文件
		String upLoadPath = this.getServletConfig().getServletContext().getRealPath("/") + "/defaultSetting/";
		upLoadPath = upLoadPath.replaceAll("%20", " ");// 空格
		File saveDir = new File(upLoadPath);
		// 如果目录不存在，就创建目录,如果存在就删除然后重建
		if (!saveDir.exists()) {
			saveDir.mkdir();
		}
//		else {
//			saveDir.delete();
//			saveDir.mkdir();
//		}
		filePath = upLoadPath+selectedTable+".xml";
		boolean result = fileWrite(filePath,defaultSet);
		PrintWriter pWriter = response.getWriter();
		
		pWriter.write(String.valueOf(result));
		pWriter.close();
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);


	}
	private boolean  fileWrite (String filepathString,String str) {

		
		File file = new File(filepathString);
		try {
			FileWriter fWriter =new FileWriter(file);
			PrintWriter pWriter = new PrintWriter(fWriter);
			pWriter.print(str);
			fWriter.close();
			pWriter.close();
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		
		
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
	}

}
