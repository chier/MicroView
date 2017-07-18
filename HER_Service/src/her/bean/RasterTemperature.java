package her.bean;



public class RasterTemperature {
	private String record_date;
	private String rast;
	private String value_type;
	public RasterTemperature(){
		this.record_date = "";
		this.rast = "";
	}
	public String getRecord_date() {
		return record_date;
	}
	public void setRecord_date(String record_date) {
		this.record_date = record_date;
	}
	public String getRast() {
		return rast;
	}
	public void setRast(String rast) {
		this.rast = rast;
	}
	public String getValue_type() {
		return value_type;
	}
	public void setValue_type(String value_type) {
		this.value_type = value_type;
	}
	
	
}
