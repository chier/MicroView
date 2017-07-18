package her.utils;

import java.io.File;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Application Lifecycle Listener implementation class ImageListener
 *
 */
@WebListener
public class ImageListener implements HttpSessionListener {

    /**
     * Default constructor. 
     */
    public ImageListener() {
        // TODO Auto-generated constructor stub
    }

	/**
     * @see HttpSessionListener#sessionCreated(HttpSessionEvent)
     */
    public void sessionCreated(HttpSessionEvent se)  { 
         // TODO Auto-generated method stub
    	//System.out.println("sessin created");
    }

	/**
     * @see HttpSessionListener#sessionDestroyed(HttpSessionEvent)
     */
    public void sessionDestroyed(HttpSessionEvent se)  { 
         // TODO Auto-generated method stub
    	//System.out.println("session destroy");
    	HttpSession session = se.getSession();
    	String imagefilename =(String) session.getAttribute("imagefilename");
    	if(imagefilename == null){
    		return;
    	}
    	File file = new File(WebRoot.getWebRoot().concat(imagefilename));
    	if(file.exists()){
    	file.delete();
    	}
    }
	
}
