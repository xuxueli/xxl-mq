package com.xxl.mq.client.rpc.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期格式化，日期解析
 * @author xuxueli 2015-5-5 17:52:43
 */
public class DateFormatUtil {
	
	/**
	 * 日期格式化 (yyyy-MM-dd)
	 * 
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	public static String formatDate(Date date) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");	// 非线程安全
		return dateFormat.format(date);
	}

	/**
	 * 日期格式化 (yyyy-MM-dd HH:mm:ss)
	 * 
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	public static String formatDateTime(Date date) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(date);
	}

	/**
	 * 日期格式化
	 * 
	 * @param date
	 * @param patten
	 * @return
	 * @throws ParseException
	 */
	public static String format(Date date, String patten) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(patten);
		return dateFormat.format(date);
	}

	/**
	 * 日期解析 (yyyy-MM-dd)
	 * 
	 * @param dateString
	 * @return
	 * @throws ParseException
	 */
	public static Date parseDate(String dateString) throws ParseException {
		SimpleDateFormat bartDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = bartDateFormat.parse(dateString);
		return date;
	}

	/**
	 * 日期解析 (yyyy-MM-dd HH:mm:ss)
	 * 
	 * @param dateString
	 * @return
	 * @throws ParseException
	 */
	public static Date parseDateTime(String dateString) throws ParseException {
		SimpleDateFormat bartDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		Date date = bartDateFormat.parse(dateString);
		return date;
	}

	/**
	 * 日期解析
	 * 
	 * @param date
	 * @param pattern
	 * @return
	 * @throws ParseException
	 */
	public static Date parse(String dateString, String pattern)
			throws ParseException {
		SimpleDateFormat bartDateFormat = new SimpleDateFormat(pattern);
		Date date = bartDateFormat.parse(dateString);
		return date;
	}
	
	public static void main(String[] args) throws ParseException {
		String temp = DateFormatUtil.formatDateTime(new Date());
		System.out.println(temp);
		Date temp2 = DateFormatUtil.parseDateTime(temp);
		System.out.println(temp2);
	}

}
