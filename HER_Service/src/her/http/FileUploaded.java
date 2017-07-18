package her.http;

import her.bean.JSONBean;
import her.bean.JSONBean.Status;
import her.dao.impl.ScalingImpl;
import her.dao.impl.WriteCSVToDBImpl;
import her.dao.impl.WriteExcelToDBImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jxl.read.biff.BiffException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONException;
import org.json.JSONObject;
import org.python.antlr.PythonParser.else_clause_return;

/**
 * Servlet implementation class FileUploaded
 */
@WebServlet("/FileUploaded")
public class FileUploaded extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    // 定义文件的上传路径

    private String uploadPath = "d://a//";

    // 限制文件的上传大小
    private int maxPostSize = 100 * 1024 * 1024;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FileUploaded() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public void destroy() {
        super.destroy();
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		processRequest(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		long start=System.currentTimeMillis(); //获取最初时间
		
		processRequest(request, response);
		
		long end=System.currentTimeMillis(); //获取运行结束时间
		System.out.println("数据导入结束，共用时间： "+(end-start)+"ms");
	}

	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
		String jsonString = "";
		String actionType = request.getParameter("action");//上传数据类型
        System.out.println("执行上传!");
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        JSONObject outjson = new JSONObject();
        
        JSONBean jsonBean = new JSONBean();
        
        //保存文件到服务器中
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(4096);
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setSizeMax(maxPostSize);
        InputStream is = null;
        try {
        	List<FileItem> fileItems = upload.parseRequest(request);
            Iterator<FileItem> iter = fileItems.iterator();
            while (iter.hasNext()) {
                FileItem item = (FileItem) iter.next();
                if (!item.isFormField()) {
                    String name = item.getName();
                    System.out.println(name);//记录文件名
                    //获取二进制数据流
                    is = item.getInputStream();
//                    try {
//                        item.write(new File(uploadPath + name));//写入本地文件
//                       // SaveFile s = new SaveFile();
//                       // s.saveFile(name);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                }
            }
            
            WriteExcelToDBImpl excel2DB= new WriteExcelToDBImpl();
//            WriteCSVToDBImpl csv2DB = new WriteCSVToDBImpl();
            
            
            if(actionType.equalsIgnoreCase("per_render"))
            {
            	
            }
            else if(actionType.equalsIgnoreCase("aggr_render"))
            {
            	 try {
     				JSONBean res = excel2DB.excel2DB(is,"aggr","","","");
//     				boolean res = csv2DB.csv2DB(is);
     				if(res != null)
     				{
     					ScalingImpl scale = new ScalingImpl();
     					jsonBean = scale.upScaling("select p.id,p.code,p.zone,p.premium as value,c.geom from policy_aggr_test p,county_400w c where p.code=c.code", "code");
     					jsonString = jsonBean.toJSONString();
//     					outjson.put("status", Status.SUCCESS);
//     					outjson.put("msg", "");
     				}
     				else {
     					outjson.put("status", Status.FAILED);
     					outjson.put("msg", "数据导入错误");
     					jsonString = outjson.toString();
     				}
     			} catch (Exception e) {
     				// TODO Auto-generated catch block
     				e.printStackTrace();
     				try {
     					outjson.put("status", Status.FAILED);
     					outjson.put("msg", e.getMessage());
     					jsonString = outjson.toString();
     				} catch (JSONException e1) {
     					// TODO Auto-generated catch block
     					e1.printStackTrace();
     				}
     			}
            }
            else if(actionType.equalsIgnoreCase("aggr_load"))
            {
            	try {
            		JSONBean res = excel2DB.excel2DB(is,"aggr","","","");
     				if(res != null)
     				{
     					outjson.put("status", Status.SUCCESS);
     					outjson.put("msg", "");
     				}
     				else {
     					outjson.put("status", Status.FAILED);
     					outjson.put("msg", "数据导入错误");
     				}
     				jsonString = outjson.toString();
     				
     			} catch (Exception e) {
     				// TODO Auto-generated catch block
     				e.printStackTrace();
     				try {
     					outjson.put("status", Status.FAILED);
     					outjson.put("msg", e.getMessage());
     					jsonString = outjson.toString();
     				} catch (JSONException e1) {
     					// TODO Auto-generated catch block
     					e1.printStackTrace();
     				}
     			}
            }
           
        } catch (FileUploadException e) {
            e.printStackTrace();
            try {
				outjson.put("status", Status.FAILED);
				outjson.put("msg", e.getMessage());
				jsonString = outjson.toString();
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            System.out.println(e.getMessage() + "结束");
        }
        
        out.write(jsonString); 
        out.flush();
        out.close();
    }
	
    // </editor-fold>
}
