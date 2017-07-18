package her.dao.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.codec.binary.Base64;

import her.bean.JSONBean;
import her.bean.RasterTemperature;
import her.bean.JSONBean.Msg;
import her.bean.JSONBean.Status;
import her.utils.CreateTempFile;
import her.utils.CreateTempFile.ImageFormat;
import her.utils.DBUtils;

public class RasterTemperatureDaoImpl {
	private String sqlString;
	private RasterTemperature rasterTemperature;
	private JSONBean jsonBean;
	
	public RasterTemperatureDaoImpl() {
		this.rasterTemperature = new RasterTemperature();
		this.jsonBean = new JSONBean();
		this.sqlString = "";
	}

	public JSONBean getJsonBean() {
		return jsonBean;
	}

	public void ExecuteQuery(String record_date,String value_type) {
		if (record_date != null && value_type != null) {

			this.sqlString = String
					.format("select rast,value_type from sss where record_date = '%s' and value_type = '%s' ",
							record_date,value_type);
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
					this.rasterTemperature.setRecord_date(rs.getString("record_date"));
					byte[] bytes = rs.getBytes("rast");
					this.rasterTemperature.setRast(new String(Base64
							.encodeBase64(bytes)));
					//this.rasterTemperature.setRast(CreateTempFile.CreateTempImgFile(bytes, ImageFormat.PNG));
					this.jsonBean.setStatus(Status.SUCCESS);
					this.jsonBean.setResult(this.rasterTemperature);
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
