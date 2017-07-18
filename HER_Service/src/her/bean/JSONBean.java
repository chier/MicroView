package her.bean;

import net.sf.json.JSONObject;

public class JSONBean {
	private Object msg;
	private Status status;//FAILED SUCCESS
	private Object result;// 数据
	private int dataCount;//数据总数
	private String imageSession;//记录Raster类型数据的session信息

	public JSONBean() {
		this.msg = Msg.NONE;
		this.result = null;
	}

	public Object getMsg() {
		return msg;
	}

	public void setMsg(Object msg) {
		this.msg = msg;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object data) {
		this.result = data;
	}
	
	public int getDataCount() {
		return dataCount;
	}

	public void setDataCount(int dataCount) {
		this.dataCount = dataCount;
	}
	
	public String getImageSession() {
		return imageSession;
	}

	public void setImageSession(String imageSession) {
		this.imageSession = imageSession;
	}

	public String toJSONString() {
		JSONObject jo = new JSONObject();
		jo.put("status", this.status);
		jo.put("msg", this.msg);
		jo.put("result", this.result == null ? "NULL" : this.result);
		jo.put("count", this.dataCount);
		return jo.toString();
	}

	public static enum Status {
		SUCCESS, FAILED
	}

	public static enum Msg {
		PARAMETER_ERROR, EXECUTE_SQL_EXECPTION, NONE
	}
}
