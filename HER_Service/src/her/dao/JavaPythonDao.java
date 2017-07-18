package her.dao;

import her.bean.JSONBean;
import her.bean.JSONBean.Status;
import her.utils.WebConfigUtils;
import her.utils.WebRoot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.JSONObject;
import org.python.core.Py;
import org.python.core.PyArray;
import org.python.core.PyFunction;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

public class JavaPythonDao {

	/**
	 * @param argJson
	 * @param exp
	 * @return
	 */
	public String mathExp(String argJson, String expJson) {
		String resultNum = "";
		PythonInterpreter interpreter = new PythonInterpreter();
		PySystemState sys = Py.getSystemState();
		sys.path.add(WebConfigUtils.getValue("pythonLibs"));
		// sys.path.add("E:\\test\\my_utils.py");
		// interpreter.exec("import sys");
		// interpreter.exec("sys.path.append('E:\\test\\my_utils.py')");
		interpreter.execfile("E:\\test\\func_utils.py");
		PyFunction func = (PyFunction) interpreter.get("func_segmented",
				PyFunction.class);

		// Map<String, Double> map = new HashMap<String, Double>();
		// map.put("he1", 0.2);
		// map.put("he2", 0.3);
		// JSONObject jsonObject = new JSONObject(map);
		// String arg = jsonObject.toString();
		// System.out.println(arg);
		// System.out.println(exp);

		PyObject pyobj = func
				.__call__(new PyString(argJson), new PyString(expJson));
		resultNum = pyobj.toString();
		// System.out.println("anwser = " + pyobj.toString());

		return resultNum;

		// System.out.println("start");
		// Process pr = null;
		// try {
		// pr = Runtime.getRuntime().exec("python E:\\test\\my_utils.py");
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// BufferedReader in = new BufferedReader(new
		// InputStreamReader(pr.getInputStream()));
		// String line;
		// try {
		// while ((line = in.readLine()) != null) {
		// System.out.println(line);
		// }
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// try {
		// in.close();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// try {
		// pr.waitFor();
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}
	
	public String mathExpCMD(String argJson, String expJson) {
		String resultNum = "";
		try{
			argJson = argJson.replace("\"", "\"\"\"").replaceAll(" ", "");
			expJson = expJson.replace("\"", "\"\"\"").replaceAll(" ", "");
			ProcessBuilder pb = new ProcessBuilder("python",WebRoot.getWebRoot().concat("python-src/").concat("func_utils_cmd.py"),argJson,expJson);
			
			Process p = pb.start();
			//sys.argv[1]

			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			resultNum = in.readLine();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		};
		return resultNum;
	}

	public JSONBean buildChart(String sql, String xField, String yField, String chartType) {
		String chartpath = "";
		JSONBean jsonBean = new JSONBean();
		try{
//			String prg = "import sys\nprint int(sys.argv[1])+int(sys.argv[2])\n";
//			BufferedWriter out = new BufferedWriter(new FileWriter("E:\\test\\MatPlot.py"));
//			out.write(prg);
//			out.close();
//			Random random = new Random();
//			List<Map<String, Double>> data = new ArrayList<Map<String, Double>>();// 数据
//			for (int i = 1; i <= 1000; i++) {
//				Map<String, Double> map = new HashMap<String, Double>();
//				map.put("x", random.nextDouble()*10000);
//				data.add(map);
//			}
//			JSONObject jo = new JSONObject();
//			jo.put("data", data);

			System.out.println("chartSQL : "+sql);
			ProcessBuilder pb = new ProcessBuilder("python",WebRoot.getWebRoot().concat("python-src/").concat("MatPlot.py"),sql,xField,yField,chartType);
			Process p = pb.start();
			//sys.argv[1]
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String chartname = in.readLine();
			chartpath = "/tempimg/".concat(chartname).concat(".png");
			
			jsonBean.setResult(chartpath);
			jsonBean.setImageSession(chartpath);
			jsonBean.setStatus(Status.SUCCESS);
			System.out.println("create chart image : "+chartpath);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			jsonBean.setStatus(Status.FAILED);
			jsonBean.setMsg(e.getMessage());
		};

		return jsonBean;
	}
}

