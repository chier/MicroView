/**
 * 
 */
package her.bean;

/**
 * @author DQ
 *台风记录信息
 */
public class TyphoonRecord {
	public String getDisaster_id() {
		return disaster_id;
	}
	public void setDisaster_id(String disaster_id) {
		this.disaster_id = disaster_id;
	}
	public String getTyp_id() {
		return typ_id;
	}
	public void setTyp_id(String typ_id) {
		this.typ_id = typ_id;
	}
	public String getInt_num() {
		return int_num;
	}
	public void setInt_num(String int_num) {
		this.int_num = int_num;
	}
	public String getChina_num() {
		return china_num;
	}
	public void setChina_num(String china_num) {
		this.china_num = china_num;
	}
	public String getTcn() {
		return tcn;
	}
	public void setTcn(String tcn) {
		this.tcn = tcn;
	}
	public String getName_en() {
		return name_en;
	}
	public void setName_en(String name_en) {
		this.name_en = name_en;
	}
	public String getName_cn() {
		return name_cn;
	}
	public void setName_cn(String name_cn) {
		this.name_cn = name_cn;
	}
	public String getStart_time() {
		return start_time;
	}
	public void setStart_time(String start_time) {
		this.start_time = start_time;
	}
	public String getEnd_time() {
		return end_time;
	}
	public void setEnd_time(String end_time) {
		this.end_time = end_time;
	}
	public String getLandfall_category() {
		return landfall_category;
	}
	public void setLandfall_category(String landfall_category) {
		this.landfall_category = landfall_category;
	}
	public String getLandfall_time() {
		return landfall_time;
	}
	public void setLandfall_time(String landfall_time) {
		this.landfall_time = landfall_time;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getLandfall_cp() {
		return landfall_cp;
	}
	public void setLandfall_cp(String landfall_cp) {
		this.landfall_cp = landfall_cp;
	}
	private String disaster_id;//对应的灾害Id
	private String typ_id;//台风id
	private String int_num;//国际编号
	private String china_num;//中国编号
	private String tcn;//包括热带低压在内的所旋编号
	private String name_en;//英文名称
	private String name_cn;//中文名称
	private String start_time;//开始时间
	private String end_time;//结束时间
	private String landfall_category;//登录强度
	private String landfall_cp;//登录时中心气压
	private String landfall_time;//登录时间
	private String description;//其他描述信息
}
