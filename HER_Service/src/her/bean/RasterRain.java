package her.bean;

public class RasterRain {
	private String record_date;
	private String rast;
	public RasterRain(){
		this.record_date = "";
		this.rast = "";
	}
	public String getRecord_date() {
		return record_date;
	}
	public void setRecord_date(String record_time) {
		this.record_date = record_time;
	}
	public String getRast() {
		return rast;
	}
	public void setRast(String rast) {
		this.rast = rast;
	}
	
}
