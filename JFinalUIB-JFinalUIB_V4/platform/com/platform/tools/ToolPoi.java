package com.platform.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.jfinal.kit.PathKit;
import com.jfinal.log.Log;
import com.platform.plugin.ParamInitPlugin;

/**
 * poi工具类
 * 
 * @author 董华健 dongcb678@163.com 
 * 
 * 描述：建议导出规则是，先定义好excel模板，然后填充数据，这样避免编写很多不必要的样式代码
 * 
 */
public abstract class ToolPoi {

	private static final Log log = Log.getLog(ParamInitPlugin.class);

	/**
	 * excel导出
	 * 
	 * @param templatePath 模板路径
	 */
	@SuppressWarnings("unused")
	public static String export(String templatePath) {
		// 导出文件存放目录
		String filePath = PathKit.getWebRootPath() + File.separator + "exportFile";
		File fileDir = new File(filePath);
		if (!fileDir.exists()) {
			fileDir.mkdir();
		}

		// 导出文件路径
		String path = filePath + File.separator + ToolDateTime.format(new Date(), "yyyyMMddHHmmssSSS") + ".xlsx";

		XSSFWorkbook wb = null;
		SXSSFWorkbook swb = null;
		FileOutputStream os = null;
		try {
			// 1.载入模板
			wb = new XSSFWorkbook(new File(templatePath)); // 初始化HSSFWorkbook对象
			wb.setSheetName(0, "用户信息导出");
			Sheet sheet = wb.getSheetAt(0); // wb.createSheet("监控点资源状态");

			// 2.读取模板处理好样式

			// 3.转换成大数据读取模式
			swb = new SXSSFWorkbook(wb, 1000); // 用于大文件导出
			sheet = swb.getSheetAt(0);

			// 4.大批量写入数据

			// 5.保存到本地文件夹
			os = new FileOutputStream(new File(path));
			swb.write(os);

			return path;
		} catch (IOException e) {
			if(log.isErrorEnabled()) log.error("导出失败：" + e.getMessage());
			e.printStackTrace();
			return null;
		} catch (InvalidFormatException e) {
			if(log.isErrorEnabled()) log.error("导出失败：" + e.getMessage());
			e.printStackTrace();
			return null;
		} finally {
			close(os, swb, wb);
		}
	}

	/**
	 * 资源关闭
	 * 
	 * @param os
	 * @param wb
	 * @param swb
	 */
	public static void close(FileOutputStream os, SXSSFWorkbook swb, XSSFWorkbook wb) {
		if (null != os) {
			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (null != swb) {
			try {
				swb.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (null != wb) {
			try {
				wb.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 将文档写入文件
	 * 
	 * @param wb
	 * @param name
	 */
	public static String writeExcel(SXSSFWorkbook wb, String name) {
		String filePath = PathKit.getWebRootPath() + File.separator + "WEB-INF" + File.separator + "files" + File.separator + "export";
		File f = new File(filePath);
		if (!f.exists()) {
			f.mkdir();
		}
		
		String path = filePath + File.separator + name + ToolDateTime.format(new Date(), "_yyyy_MM_dd_HH_mm_ss_SSS") + ".xlsx";
		
		FileOutputStream os = null;
		try {
			File file = new File(path);
			os = new FileOutputStream(file);
			wb.write(os);
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != os) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return path;
	}

	/**
	 * 导出标题样式
	 */
	public static void setTitleFont(SXSSFWorkbook wb, Iterator<Cell> it) {
		XSSFColor color = new XSSFColor(new java.awt.Color(219, 229, 241));
		Font font = createFont(wb, Font.BOLDWEIGHT_BOLD, Font.COLOR_NORMAL, (short) 11);
		CellStyle style = createCellStyle(wb, color, CellStyle.ALIGN_CENTER, font);
		while (it.hasNext()) {
			it.next().setCellStyle(style);
		}
	}

	/**
	 * 导出表头样式
	 */
	public static void setHeadStyle(SXSSFWorkbook wb, Iterator<Cell> it) {
		XSSFColor color = new XSSFColor(new java.awt.Color(79, 129, 189));
		Font font = createFont(wb, Font.BOLDWEIGHT_NORMAL, HSSFColor.WHITE.index, (short) 11);
		CellStyle style = createCellStyle(wb, color, CellStyle.ALIGN_CENTER, font);
		while (it.hasNext()) {
			it.next().setCellStyle(style);
		}
	}

	/**
	 * 导出表数据样式 默认居中
	 */
	public static void setContentStyle(SXSSFWorkbook wb, Iterator<Cell> it) {
		XSSFColor color = new XSSFColor(new java.awt.Color(255, 255, 255));
		Font font = createFont(wb, Font.BOLDWEIGHT_NORMAL, Font.COLOR_NORMAL, (short) 10);
		CellStyle style = createBorderCellStyle(wb, HSSFColor.WHITE.index, color, CellStyle.ALIGN_CENTER, font);
		while (it.hasNext()) {
			it.next().setCellStyle(style);
		}
	}

	/**
	 * 导出表数据样式 左对齐
	 */
	public static void setContentLeftStyle(SXSSFWorkbook wb, Cell cell) {
		XSSFColor color = new XSSFColor(new java.awt.Color(255, 255, 255));
		Font font = createFont(wb, Font.BOLDWEIGHT_NORMAL, Font.COLOR_NORMAL, (short) 10);
		CellStyle style = createBorderCellStyle(wb, HSSFColor.WHITE.index, color, CellStyle.ALIGN_LEFT, font);
		style.setWrapText(true); // 实现换行
		cell.setCellStyle(style);
	}

	/**
	 * 设置合并单元格边框
	 */
	public static void setBorderStyle(Workbook wb, Sheet sheet, CellRangeAddress cra) {
		int border = HSSFColor.WHITE.index;
		RegionUtil.setBorderBottom(border, cra, sheet, wb);
		RegionUtil.setBorderLeft(border, cra, sheet, wb);
		RegionUtil.setBorderRight(border, cra, sheet, wb);
		RegionUtil.setBorderTop(border, cra, sheet, wb);
	}

	/**
	 * 功能：创建HSSFSheet工作簿
	 *
	 * @param wb  SXSSFWorkbook
	 * @param sheetName String
	 * @return HSSFSheet
	 */
	public static Sheet createSheet(SXSSFWorkbook wb, String sheetName) {
		Sheet sheet = wb.createSheet(sheetName);
		sheet.setDefaultColumnWidth(30);
		sheet.setColumnWidth(0, 7 * 256);
		sheet.setDefaultRowHeight((short) 400);
		sheet.setDisplayGridlines(true);
		return sheet;
	}

	/**
	 * 功能：创建CellStyle样式
	 *
	 * @param wb SXSSFWorkbook
	 * @param color  背景色
	 * @param align 前置色
	 * @param font  字体
	 * @return CellStyle
	 */
	public static CellStyle createCellStyle(SXSSFWorkbook wb, XSSFColor color, short align, Font font) {
		XSSFCellStyle cs = (XSSFCellStyle) wb.createCellStyle();
		cs.setAlignment(align); // 水平居中
		cs.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		cs.setFillForegroundColor(color);
		cs.setFillPattern(CellStyle.SOLID_FOREGROUND);
		cs.setFont(font);
		return cs;
	}

	/**
	 * 功能：创建带边框的CellStyle样式
	 *
	 * @param wb SXSSFWorkbook
	 * @param backgroundColor 背景色
	 * @param foregroundColor 前置色
	 * @param font 字体
	 * @return CellStyle
	 */
	public static CellStyle createBorderCellStyle(SXSSFWorkbook wb, short backgroundColor, XSSFColor foregroundColor,
			short halign, Font font) {
		XSSFCellStyle cs = (XSSFCellStyle) wb.createCellStyle();
		cs.setAlignment(halign);
		cs.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		cs.setFillBackgroundColor(backgroundColor);
		cs.setFillForegroundColor(foregroundColor);
		cs.setFillPattern(CellStyle.SOLID_FOREGROUND);
		cs.setFont(font);
		cs.setBorderLeft(CellStyle.BORDER_DASHED);
		cs.setLeftBorderColor(HSSFColor.GREY_80_PERCENT.index);
		cs.setBorderRight(CellStyle.BORDER_DASHED);
		cs.setRightBorderColor(HSSFColor.GREY_80_PERCENT.index);
		cs.setBorderTop(CellStyle.BORDER_DASHED);
		cs.setTopBorderColor(HSSFColor.GREY_80_PERCENT.index);
		cs.setBorderBottom(CellStyle.BORDER_DASHED);
		cs.setBottomBorderColor(HSSFColor.GREY_80_PERCENT.index);
		return cs;
	}

	/**
	 * 功能：创建字体
	 *
	 * @param wb HSSFWorkbook
	 * @param boldweight short
	 * @param color short
	 * @return Font
	 */
	public static Font createFont(SXSSFWorkbook wb, short boldweight, short color, short size) {
		Font font = wb.createFont();
		font.setBoldweight(boldweight);
		font.setColor(color);
		font.setFontHeightInPoints(size);
		return font;
	}

	/**
	 * 功能：合并单元格
	 *
	 * @param sheet  Sheet
	 * @param firstRow  int
	 * @param lastRow  int
	 * @param firstColumn int
	 * @param lastColumn int
	 * @return int 合并区域号码
	 */
	public static int mergeCell(Sheet sheet, int firstRow, int lastRow, int firstColumn, int lastColumn) {
		return sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstColumn, lastColumn));
	}

	/**
	 * 功能：创建Row
	 *
	 * @param sheet Sheet
	 * @param rowNum int
	 * @param height int
	 * @return HSSFRow
	 */
	public static Row createRow(Sheet sheet, int rowNum, int height) {
		Row row = sheet.createRow(rowNum);
		row.setHeight((short) height);
		return row;
	}

//	public static String nullVal(Object val){
//		if(val == null){
//			return "";
//		}
//		return val;
//	}
	
}
