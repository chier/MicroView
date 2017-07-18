package her.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.UUID;

public class CreateTempFile {
	public static String CreateTempImgFile(byte[] bytes, ImageFormat format){
//		String spath = WebRoot.getWebRoot().concat("tempfile/");
		String spath = WebConfigUtils.getValue("tempfilepath").trim().isEmpty()?GlobalVariable.filePath:WebConfigUtils.getValue("tempfilepath").trim();
//		ServletContext sc = this.getServletContext();
//		String spath = sc.getRealPath("tempfile/");
		File path = new File(spath);
		try {
			File file = File.createTempFile(UUID.randomUUID().toString(),".".concat(format.toString().toLowerCase()),path);
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(bytes);
			fos.close();
			return "/tempfile/".concat(file.getName());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
		
	}
	
	public static String CreateTempHtmlFile(String htmlString){
		String spath = WebConfigUtils.getValue("tempfilepath").trim().isEmpty()?GlobalVariable.filePath:WebConfigUtils.getValue("tempfilepath").trim();
		File path = new File(spath);
		try {
//			File file = File.createTempFile(UUID.randomUUID().toString(),".".concat("html"),path);
//			BufferedWriter fos = new BufferedWriter(new FileWriter(file));
//			fos.write(htmlString);
//			fos.close();
			
			File file = File.createTempFile(UUID.randomUUID().toString(),".".concat("html"),path);
			Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
			writer.write(htmlString);
			writer.close();
			return "/tempfile/".concat(file.getName());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}

	public static enum ImageFormat{PNG,TIFF,JPG,JPEG}
}

