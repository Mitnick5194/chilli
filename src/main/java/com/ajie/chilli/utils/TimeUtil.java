package com.ajie.chilli.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日期处理工具
 *
 * @author niezhenjie
 *
 */
public final class TimeUtil {
	public static Logger logger = LoggerFactory.getLogger(TimeUtil.class);
	/** yyyy-MM-dd HH:mm:ss */
	public static final SimpleDateFormat _DATE = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	/** yyyy-MM-dd HH:mm */
	public static final SimpleDateFormat _DATE_HM = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm");
	/** yyyy-MM-dd */
	public static final SimpleDateFormat _YMD = new SimpleDateFormat(
			"yyyy-MM-dd");
	/** HH:mm:ss */
	public static final SimpleDateFormat _HMS = new SimpleDateFormat("HH:mm:ss");
	/** HH:mm */
	public static final SimpleDateFormat _HM = new SimpleDateFormat("HH:mm");
	/** yyyyMMddHHmmss */
	public static final SimpleDateFormat _COMPACT_DATE = new SimpleDateFormat(
			"yyyyMMddHHmmss");
	/** yyyyMMddHHmm */
	public static final SimpleDateFormat _COMPACT_DATE_HM = new SimpleDateFormat(
			"yyyyMMddHHmm");
	/** yyyyMMdd */
	public static final SimpleDateFormat _COMPACT_YMD = new SimpleDateFormat(
			"yyyyMMdd");
	/** HHmmss */
	public static final SimpleDateFormat _COMPACT_HMS = new SimpleDateFormat(
			"HHmmss");
	/** HHmm */
	public static final SimpleDateFormat _COMPACT_HM = new SimpleDateFormat(
			"HHmm");

	/** 日期处理类 */
	public static final Calendar calendar = Calendar.getInstance();

	/** 一小时的毫秒数 */
	public static final long MILLIOFHOUR = 60 * 60 * 1000;
	/** 一天的毫秒数 */
	public static final long MILLIOFDAY = 24 * MILLIOFHOUR;

	private TimeUtil() {
	}

	/**
	 * 格式化日期 yyyy-MM-dd HH:mm:ss
	 * 
	 * @param date
	 * @return
	 */
	public static String formatDate(Date date) {
		if (null == date)
			return null;
		synchronized (_DATE) {
			return _DATE.format(date);
		}
	}

	/**
	 * 格式化日期yyyy-MM-dd HH:mm
	 * 
	 * @param date
	 * @return
	 */
	public static String formatDateHM(Date date) {
		if (null == date)
			return null;
		synchronized (_DATE_HM) {
			return _DATE_HM.format(date);
		}
	}

	/**
	 * 格式化日期 yyyy-MM-dd
	 * 
	 * @param date
	 * @return
	 */
	public static String formatYMD(Date date) {
		if (null == date)
			return null;
		synchronized (_YMD) {
			return _YMD.format(date);
		}
	}

	/**
	 * 格式化日期 HH:mm:ss
	 * 
	 * @param date
	 * @return
	 */
	public static String formatHMS(Date date) {
		if (null == date)
			return null;
		synchronized (_HMS) {
			return _HMS.format(date);
		}
	}

	/**
	 * 格式化日期 HH:mm
	 * 
	 * @param date
	 * @return
	 */
	public static String formatHM(Date date) {
		if (null == date)
			return null;
		synchronized (_HM) {
			return _HM.format(date);
		}
	}

	/**
	 * 格式化日期 yyyyMMddHHmmss
	 * 
	 * @param date
	 * @return
	 */
	public static String compactDate(Date date) {
		if (null == date)
			return null;
		synchronized (_COMPACT_DATE) {
			return _COMPACT_DATE.format(date);
		}
	}

	/**
	 * 格式化日期 yyyyMMddHHmm
	 * 
	 * @param date
	 * @return
	 */
	public static String compactDateHM(Date date) {
		if (null == date)
			return null;
		synchronized (_COMPACT_DATE_HM) {
			return _COMPACT_DATE_HM.format(date);
		}
	}

	/**
	 * 格式化日期 yyyyMMdd
	 * 
	 * @param date
	 * @return
	 */
	public static String compactYMD(Date date) {
		if (null == date)
			return null;
		synchronized (_COMPACT_YMD) {
			return _COMPACT_YMD.format(date);
		}
	}

	/**
	 * 格式化日期 HHmmss
	 * 
	 * @param date
	 * @return
	 */
	public static String compactHMS(Date date) {
		if (null == date)
			return null;
		synchronized (_COMPACT_HMS) {
			return _COMPACT_HMS.format(date);
		}
	}

	/**
	 * 格式化日期 HHmm
	 * 
	 * @param date
	 * @return
	 */
	public static String compactHM(Date date) {
		if (null == date)
			return null;
		synchronized (_COMPACT_HM) {
			return _COMPACT_HM.format(date);
		}
	}

	/**
	 * 对比date2比date1时分秒置零后大多少个天数（如果date1较date2大，则返回负数）
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static int inteval(Date date1, Date date2) {
		if (null == date1 || null == date2) {
			return 0;
		}
		synchronized (calendar) {
			calendar.setTime(date1);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			date1 = calendar.getTime();
			calendar.setTime(date2);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			date2 = calendar.getTime();
		}
		return (int) ((date2.getTime() - date1.getTime()) / MILLIOFDAY);
	}

	/**
	 * 对比两个时间是否为同一天
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static boolean isSameDay(Date date1, Date date2) {
		return 0 == inteval(date1, date2);
	}

	/**
	 * 字符串转换成日期格式，支持格式: yyyy-MM-dd HH:mm:ss、yyyy-MM-dd HH:mm、yyyy-MM-dd、yyyyMMdd
	 * yyyyMMddHHmm、yyyyMMddHHmmss
	 * 
	 * @param str
	 * @return
	 */
	public static Date parse(String str) {
		if (null == str || str.length() < 8)
			return null;
		int len = str.length();
		Date date = null;
		try {
			if (len == 19) { // yyyy-MM-dd HH:mm:ss
				synchronized (_DATE) {
					date = _DATE.parse(str);
				}
			} else if (len == 16) { // yyyy-MM-dd HH:ss
				synchronized (_DATE_HM) {
					date = _DATE_HM.parse(str);
				}
			} else if (len == 14) { // yyyyMMddHHmmss
				synchronized (_COMPACT_DATE) {
					date = _COMPACT_DATE.parse(str);
				}
			} else if (len == 12) { // yyyyMMddHHmm
				synchronized (_COMPACT_DATE_HM) {
					date = _COMPACT_DATE_HM.parse(str);
				}
			} else if (len == 10) { // yyyy-MM-dd
				synchronized (date) {
					date = _YMD.parse(str);
				}
			} else if (len == 8) { // yyyyMMdd
				synchronized (_COMPACT_YMD) {
					date = _COMPACT_YMD.parse(str);
				}
			} else {
				logger.warn("解析失败，格式错误：" + str);
			}
		} catch (ParseException e) {
			logger.warn("解析失败：" + str, e);
		}
		return date;
	}

	/**
	 * 秒转换成毫秒
	 * 
	 * @return
	 */
	public static long secondToMills(int seconds) {
		return seconds * 1000;
	}

	public static long minuteToMills(int minute) {
		return secondToMills(minute * 60);
	}

	public static long hourToMills(int hour) {
		return minuteToMills(hour * 60);
	}
}
