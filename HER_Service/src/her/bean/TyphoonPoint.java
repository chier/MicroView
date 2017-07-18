package her.bean;


public class TyphoonPoint {
	private String typ_id = "";// 台风id
	private String record_time = "";// 记录时间
	private String category = "";// 台风强度
	private String wkt = "";//wkt字段
	private String move_speed = "";// 移动速度
	private String max_windspeed = "";// 最大风速
	private String move_dir = "";// 移动方向
	private String mws = "";// 2分钟内最大冈速
	private String aws = "";// 2分钱内平均风速
	private String radius7 = "";// 7级风圈半径
	private String radius10 = "";// 10级风圈半径
	
	public String getRecord_time() {
		return record_time;
	}

	public void setRecord_time(String record_time) {
		this.record_time = record_time;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	
	public String getWkt() {
		return wkt;
	}

	public void setWkt(String wkt) {
		this.wkt = wkt;
	}

	public String getMove_speed() {
		return move_speed;
	}

	public void setMove_speed(String move_speed) {
		this.move_speed = move_speed;
	}

	public String getMax_windspeed() {
		return max_windspeed;
	}

	public void setMax_windspeed(String max_windspeed) {
		this.max_windspeed = max_windspeed;
	}

	public String getMove_dir() {
		return move_dir;
	}

	public void setMove_dir(String move_dir) {
		this.move_dir = move_dir;
	}

	public String getMws() {
		return mws;
	}

	public void setMws(String mws) {
		this.mws = mws;
	}

	public String getAws() {
		return aws;
	}

	public void setAws(String aws) {
		this.aws = aws;
	}

	public String getRadius7() {
		return radius7;
	}

	public void setRadius7(String radius7) {
		this.radius7 = radius7;
	}

	public String getRadius10() {
		return radius10;
	}

	public void setRadius10(String radius10) {
		this.radius10 = radius10;
	}

	

	public TyphoonPoint() {
	}

	public static String GetFieldsString() {
		return "typ_id,record_time,category,St_AsEWKT(geom) as wkt,move_speed,max_windspeed,move_dir,mws,aws,radius7,radius10";
	}


	public TyphoonPoint(String typ_id) {
		this.typ_id = typ_id;
	}

	public String getTyp_id() {
		return typ_id;
	}

	public void setTyp_id(String typ_id) {
		this.typ_id = typ_id;
	}
	
}
