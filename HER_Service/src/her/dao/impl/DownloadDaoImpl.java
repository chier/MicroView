package her.dao.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;

import her.utils.CsvWriter;
import her.utils.DBUtils;

public class DownloadDaoImpl {
	
	public DownloadDaoImpl()
	{

	}

	public void downloadPolicyData(HttpServletResponse response, String sql, String fileName) throws IOException, SQLException
	{
		// 要输出的内容  
		//***** Output strOut to Response ******
		response.reset(); // Reset the response
		response.setContentType("application/csv;charset=UTF-8"); // the encoding of this example is GB2312
		response.setHeader("Content-Disposition","attachment; filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"");
		response.setCharacterEncoding("UTF-8");
		//写入下载数据
		writePolicyData(response.getOutputStream(), sql);
//		PrintWriter out;
//		try
//		{
//			out = response.getWriter();
//			out.write(strOut);
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
		//***************************************
		//加上UTF-8文件的标识字符 
//		out.write(new byte []{( byte ) 0xEF ,( byte ) 0xBB ,( byte ) 0xBF });  
	}
	
	private void writePolicyData(OutputStream outStream, String sql) throws IOException, SQLException
	{
		Writer wr = new OutputStreamWriter(outStream, Charset.forName("GBK"));
		CsvWriter cw = new CsvWriter(wr,',');
		
		Connection connection = DBUtils.getClientDBConnection();
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		System.out.println(sql);
		try {
			preparedStatement = connection.prepareStatement(sql);
			rs = preparedStatement.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			
			int colcount = rsmd.getColumnCount();// 取得全部列数
			String[] header = new String[colcount];
			for (int i = 0; i < colcount; i++) {
				header[i] = rsmd.getColumnName(i+1);
			}
			cw.writeRecord(header);
			
			while (rs.next()) {
				String[] contents = new String[colcount];
				for (int i = 0; i < colcount; i++) {
					contents[i] = rs.getString(i+1);
				}
				cw.writeRecord(contents);
			}
			
			preparedStatement.close();
			connection.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			rs.close();
			preparedStatement.close();
			connection.close();
		}

		cw.close();
	}
}
