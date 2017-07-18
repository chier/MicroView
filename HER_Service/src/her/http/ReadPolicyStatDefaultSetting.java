package her.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ReadPolicyStatDefaultSetting extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String filePath = "";
	/**
	 * Constructor of the object.
	 */
	public ReadPolicyStatDefaultSetting() {
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
		String selectedTable = request.getParameter("selectedTable");

		//上传文件
		String upLoadPath = this.getServletConfig().getServletContext().getRealPath("/") + "/defaultSetting/";
		System.out.println(upLoadPath);
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
		String result = fileRead(filePath);
		PrintWriter pWriter = response.getWriter();
		
		pWriter.write(result);
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
	public  String fileRead (String filepathString) {
		String pathname = filepathString;
		File file = new File(pathname);
		try {
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String string = bufferedReader.readLine();
			StringBuffer strBuffer = new StringBuffer();
			while (string != null) {
				strBuffer.append(string);
				string = bufferedReader.readLine();	
			}
			return strBuffer.toString();
		} catch (FileNotFoundException e) {
			// TODO: handle exception
			System.out.println("File is not fund!");
			return "";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
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
