package her.dao.impl;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.codec.binary.Base64;





import her.bean.JSONBean;
import her.bean.JSONBean.Msg;
import her.bean.JSONBean.Status;
import her.bean.RasterRain;
import her.utils.CreateTempFile;
import her.utils.DBUtils;
import her.utils.CreateTempFile.ImageFormat;

public class RasterRainDaoImpl {
	private RasterRain rasterRain;
	private JSONBean jsonBean;
	private String sqlString;
	private String imageSession;
	
	public RasterRainDaoImpl() {
		this.rasterRain = new RasterRain();
		this.jsonBean = new JSONBean();
		this.sqlString = "";
		this.imageSession = "";
	}

	public JSONBean getJsonBean() {
		return jsonBean;
	}

	public String getImageSession(){
		return this.imageSession;
	}
	public void ExecuteQuery(String record_date) {
		if (record_date != null) {
			// this.sqlString =
			// String.format("select record_time,rast from haz_precipitation_raster where record_date = '%s'",
			// record_date);
			this.sqlString = String
					.format("select record_date as record_date,ST_AsPNG(ST_ColorMap(rast,1, 'fire')) As rast from haz_precipitation_raster where record_date = '%s'",
							record_date);
			this.ExecuteSQL();
		} else {
			this.jsonBean.setStatus(Status.FAILED);
			this.jsonBean.setMsg(Msg.PARAMETER_ERROR);
			this.jsonBean.setResult(null);
		}
	}

	private void ExecuteSQL() {
		Connection connection = DBUtils.getConnection();
		if (connection != null) {

			try {
				Statement statement = connection.createStatement();
				ResultSet rs = statement.executeQuery(this.sqlString);
				while (rs.next()) {
					this.rasterRain.setRecord_date(rs.getString("record_date"));
					byte[] bytes = rs.getBytes("rast");
					
					//this.rasterRain.setRast(new String(Base64
					//		.encodeBase64(bytes)));

					this.rasterRain.setRast(CreateTempFile.CreateTempImgFile(bytes, ImageFormat.PNG));
					this.imageSession = this.rasterRain.getRast();
					this.jsonBean.setStatus(Status.SUCCESS);
					this.jsonBean.setResult(this.rasterRain);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				this.jsonBean.setStatus(Status.FAILED);
				this.jsonBean.setMsg(Msg.EXECUTE_SQL_EXECPTION);
				this.jsonBean.setResult(null);
			}

		}
	}

}
