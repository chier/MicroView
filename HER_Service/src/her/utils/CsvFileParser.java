package her.utils;

import java.io.IOException;
import java.io.InputStream;

import com.Ostermiller.util.ExcelCSVParser;
import com.Ostermiller.util.LabeledCSVParser;

public class CsvFileParser {
	private LabeledCSVParser csvParser;// csv解析器，对于第一行的表头信息，自动加载为索引关键字
	private int currLineNum = -1;// 文件所读到行数
	private String[] currLine = null;// 用来存放当前行的数据

	/*
	 * 构造函数， Param: in InputStream 要解析的信息流 throws IOException
	 */

	public CsvFileParser(InputStream in) throws IOException {
		csvParser = new LabeledCSVParser(new ExcelCSVParser(in));
		currLineNum = csvParser.getLastLineNumber();
	}

	/*
	 * 检查是否还有数据
	 * 
	 * return ture 还有一行数据，false 没有数据
	 */
	public boolean hasMore() throws IOException {
		currLine = csvParser.getLine();
		currLineNum = csvParser.getLastLineNumber();
		if (null == currLine)
			return false;
		return true;
	}

	/*
	 * 返回当前行数据，关键字所指向的数据 param:String filedName 该行的表头 return:String
	 * 返回当前行数据，关键字所指向的数据
	 */
	public String getByFieldName(String fieldName) {
		return csvParser.getValueByLabel(fieldName);
	}

	/*
	 * 关闭解析器
	 */
	public void close() throws IOException {
		csvParser.close();
	}

	/*
	 * 读取当前行数据
	 * 
	 * return String[] 读取当前行数据
	 */
	public String[] readLine() throws IOException {
		currLine = csvParser.getLine();
		currLineNum = csvParser.getLastLineNumber();
		return currLine;
	}

	public int getCurrLineNum() {
		return currLineNum;
	}
}
