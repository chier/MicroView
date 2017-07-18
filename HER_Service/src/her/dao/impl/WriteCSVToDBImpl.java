package her.dao.impl;

import her.utils.CsvFileParser;
import her.utils.DBUtils;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WriteCSVToDBImpl {
	public boolean csv2DB(InputStream is) {
		boolean result = false;
		if (is != null) {
			int i = 0;

			Statement ps = null;
			Connection conn = null;
			String sql = null;
			try {
				conn = DBUtils.getClientDBConnection(); // 调用链接数据库的文件
				conn.setAutoCommit(false);
				ps = conn.createStatement();
				// 实例解析器CsvFileParser
				CsvFileParser parser = new CsvFileParser(is);
				// 读取数据
				while (parser.hasMore()) {
					i++;

					String fieldString = "";
					String valueString = "";

					String policy_id = parser.getByFieldName("policy_id");
					String addr = parser.getByFieldName("addr");
					String ceding_company = parser
							.getByFieldName("ceding_company");
					String type_of_insurance = parser
							.getByFieldName("type_of_insurance");
					String insured = parser.getByFieldName("insured");
					String reinsure_currency = parser
							.getByFieldName("reinsure_currency");
					String hazard_category = parser
							.getByFieldName("hazard_category");
					String year_built = parser.getByFieldName("year_built");

					String lon = parser.getByFieldName("lon");
					String lat = parser.getByFieldName("lat");
					String story = parser.getByFieldName("story");
					String treaty_deductible = parser
							.getByFieldName("treaty_deductible");
					String ded = parser.getByFieldName("ded");
					if (!policy_id.isEmpty()) {
						fieldString += "policy_id,";
						valueString += "'" + policy_id + "',";
					}
					if (!addr.isEmpty()) {
						fieldString += "addr,";
						valueString += "'" + addr + "',";
					}
					if (!ceding_company.isEmpty()) {
						fieldString += "ceding_company,";
						valueString += "'" + ceding_company + "',";
					}
					if (!type_of_insurance.isEmpty()) {
						fieldString += "type_of_insurance,";
						valueString += "'" + type_of_insurance + "',";
					}
					if (!insured.isEmpty()) {
						fieldString += "insured,";
						valueString += "'" + insured + "',";
					}
					if (!reinsure_currency.isEmpty()) {
						fieldString += "reinsure_currency,";
						valueString += "'" + reinsure_currency + "',";
					}
					if (!hazard_category.isEmpty()) {
						fieldString += "hazard_category,";
						valueString += "'" + hazard_category + "',";
					}
					if (!year_built.isEmpty()) {
						fieldString += "year_built,";
						valueString += "'" + year_built + "',";
					}

					if (!lon.isEmpty() && isNumeric(lon)) {
						fieldString += "lon,";
						valueString += lon + ",";
					}
					if (!lat.isEmpty() && isNumeric(lat)) {
						fieldString += "lat,";
						valueString += lat + ",";
					}
					if (!story.isEmpty() && isNumeric(story)) {
						fieldString += "story,";
						valueString += story + ",";
					}
					if (!treaty_deductible.isEmpty() && isNumeric(treaty_deductible)) {
						fieldString += "treaty_deductible,";
						valueString += treaty_deductible + ",";
					}
					if (!ded.isEmpty() && isNumeric(ded)) {
						fieldString += "ded,";
						valueString += ded + ",";
					}

					if (fieldString.endsWith(",")) {
						fieldString = fieldString.substring(0,
								fieldString.length() - 1);// 删除最后一个逗号
					}
					if (valueString.endsWith(",")) {
						valueString = valueString.substring(0,
								valueString.length() - 1);// 删除最后一个逗号
					}

					sql = String.format(
							"insert into upt_policy(%s) values(%s)",
							fieldString, valueString);
					ps.executeUpdate(sql);
					if (i % 500 == 0) {
						// 500条记录提交一次
						conn.commit();
						System.out.println("已成功提交" + i + "行!");
					}
				}
				parser.close();

				if (i % 500 != 0) {
					// 不够500条的再提交一次（其实不用判断，直接提交就可以，不会重复提交的）
					conn.commit();
					System.out.println("已成功提交" + i + "行!");
				}
				result = true;
			} catch (Exception ex) {
				System.out.println("导出第" + (i + 1) + "条时出错");
				System.out.println("出错的sql语句是：" + sql);
				System.out.println("错误信息：");
				ex.printStackTrace();
				try {
					if (conn != null) {
						conn.rollback();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				result = false;
			} finally {
				try {
					if (ps != null) {
						ps.close();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				try {
					if (conn != null) {
						conn.close();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		return result;
	}
	
	private boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("-?[0-9]+.?[0-9]+");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}
}
