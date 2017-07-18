package her.utils;



public class WebRoot {
	public static String WEBROOT;
	
	public WebRoot(){
		String classloader = WebRoot.class.getClassLoader().getResource("").toString();
		String spath = classloader.substring(6,classloader.length()-16);
		WebRoot.WEBROOT = spath;
	}
	public static String getWebRoot(){
		if (WebRoot.WEBROOT == null) {
			new WebRoot();
			return WebRoot.WEBROOT;
		}
		return WebRoot.WEBROOT;
	}
}
