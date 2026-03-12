package com.mybook.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 日期工具类
 * 提供常用的日期格式化、解析、计算等方法
 */
public class DateUtil {
    
    // 常用日期格式
    public static final String FORMAT_YMD = "yyyyMMdd";
    public static final String FORMAT_Y_M_D = "yyyy-MM-dd";
    public static final String FORMAT_YMD_HMS = "yyyyMMddHHmmss";
    public static final String FORMAT_Y_M_D_HMS = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_YM = "yyyyMM";
    public static final String FORMAT_Y_M = "yyyy-MM";
    public static final String FORMAT_HMS = "HH:mm:ss";
    
    /**
     * 格式化日期为字符串 (默认格式: yyyy-MM-dd HH:mm:ss)
     * @param date 日期
     * @return 格式化后的字符串
     */
    public static String format(Date date) {
        return format(date, FORMAT_Y_M_D_HMS);
    }
    
    /**
     * 格式化日期为指定格式的字符串
     * @param date 日期
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    public static String format(Date date, String pattern) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }
    
    /**
     * 格式化当前日期为指定格式的字符串
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    public static String formatNow(String pattern) {
        return format(new Date(), pattern);
    }
    
    /**
     * 格式化当前日期为年月格式 (yyyyMM) - 您需要的功能
     * @return 格式如 "202403"
     */
    public static String formatYearMonth() {
        return format(new Date(), FORMAT_YM);
    }
    
    /**
     * 解析字符串为日期
     * @param dateStr 日期字符串
     * @param pattern 格式模式
     * @return 解析后的日期
     */
    public static Date parse(String dateStr, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            throw new IllegalArgumentException("日期格式错误: " + dateStr + ", 期望格式: " + pattern, e);
        }
    }
    
    /**
     * 获取指定日期的开始时间 (00:00:00)
     * @param date 日期
     * @return 当天的开始时间
     */
    public static Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
    
    /**
     * 获取指定日期的结束时间 (23:59:59)
     * @param date 日期
     * @return 当天的结束时间
     */
    public static Date getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }
    
    /**
     * 获取当前时间的年月文件夹路径 (如: "202403/")
     * 适用于您提到的图片存储场景
     * @return 年月文件夹路径
     */
    public static String getYearMonthFolder() {
        return formatYearMonth() + "/";
    }
    
    /**
     * 获取年月日的文件夹路径 (如: "2024/03/12/")
     * @return 年/月/日/ 文件夹路径
     */
    public static String getYmdFolder() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return String.format("%04d/%02d/%02d/", year, month, day);
    }
    
    /**
     * 计算两个日期之间的天数差
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 天数差
     */
    public static long daysBetween(Date startDate, Date endDate) {
        long diff = endDate.getTime() - startDate.getTime();
        return diff / (1000 * 60 * 60 * 24);
    }
    
    /**
     * 在指定日期上增加天数
     * @param date 原始日期
     * @param days 增加的天数 (可为负数)
     * @return 增加后的日期
     */
    public static Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }
    
    /**
     * 在指定日期上增加月数
     * @param date 原始日期
     * @param months 增加的月数 (可为负数)
     * @return 增加后的日期
     */
    public static Date addMonths(Date date, int months) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, months);
        return calendar.getTime();
    }
    
    /**
     * 获取当前月份的第一天
     * @return 当月第一天的日期
     */
    public static Date getFirstDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return getStartOfDay(calendar.getTime());
    }
    
    /**
     * 获取指定日期所在月份的第一天
     * @param date 指定日期
     * @return 当月第一天的日期
     */
    public static Date getFirstDayOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return getStartOfDay(calendar.getTime());
    }
    
    /**
     * 获取当前月份的最后一天
     * @return 当月最后一天的日期
     */
    public static Date getLastDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return getEndOfDay(calendar.getTime());
    }
    
    /**
     * 获取指定日期所在月份的最后一天
     * @param date 指定日期
     * @return 当月最后一天的日期
     */
    public static Date getLastDayOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return getEndOfDay(calendar.getTime());
    }
    
    /**
     * 判断日期是否在今天
     * @param date 要判断的日期
     * @return 是否在今天
     */
    public static boolean isToday(Date date) {
        if (date == null) {
            return false;
        }
        return format(date, FORMAT_YMD).equals(format(new Date(), FORMAT_YMD));
    }
    
    /**
     * 判断日期是否在指定日期之前
     * @param date1 第一个日期
     * @param date2 第二个日期
     * @return date1是否在date2之前
     */
    public static boolean isBefore(Date date1, Date date2) {
        return date1.before(date2);
    }
    
    /**
     * 判断日期是否在指定日期之后
     * @param date1 第一个日期
     * @param date2 第二个日期
     * @return date1是否在date2之后
     */
    public static boolean isAfter(Date date1, Date date2) {
        return date1.after(date2);
    }
    
    /**
     * 获取当前时间的时间戳 (毫秒)
     * @return 当前时间戳
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }
    
    /**
     * 获取当前时间的时间戳 (秒)
     * @return 当前时间戳(秒)
     */
    public static long getCurrentTimestampSeconds() {
        return System.currentTimeMillis() / 1000;
    }
    
    /**
     * 将时间戳转换为日期
     * @param timestamp 时间戳 (毫秒)
     * @return 日期对象
     */
    public static Date fromTimestamp(long timestamp) {
        return new Date(timestamp);
    }
    
    /**
     * 将日期转换为时间戳
     * @param date 日期
     * @return 时间戳 (毫秒)
     */
    public static long toTimestamp(Date date) {
        return date.getTime();
    }
    
    /**
     * 获取当前时间的字符串表示 (用于日志等)
     * @return 格式: yyyy-MM-dd HH:mm:ss.SSS
     */
    public static String getCurrentTimeForLog() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return sdf.format(new Date());
    }
    
    /**
     * 获取指定格式的当前时间字符串
     * @param pattern 格式
     * @return 格式化后的时间字符串
     */
    public static String getCurrentTime(String pattern) {
        return format(new Date(), pattern);
    }
    
    /**
     * 获取指定天数后的日期
     * @param days 天数
     * @return 指定天数后的日期
     */
    public static Date getDateAfterDays(int days) {
        return addDays(new Date(), days);
    }
    
    /**
     * 获取指定天数前的日期
     * @param days 天数
     * @return 指定天数前的日期
     */
    public static Date getDateBeforeDays(int days) {
        return addDays(new Date(), -days);
    }
    
    /**
     * 判断是否为闰年
     * @param year 年份
     * @return 是否为闰年
     */
    public static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }
    
    /**
     * 获取指定月份的天数
     * @param year 年份
     * @param month 月份 (1-12)
     * @return 该月的天数
     */
    public static int getDaysInMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
    
    /**
     * 将Date转换为LocalDateTime
     * @param date 日期
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
    
    /**
     * 将LocalDateTime转换为Date
     * @param localDateTime LocalDateTime
     * @return Date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * 示例用法
     */
    public static void main(String[] args) {
        // 您需要的功能：获取年月格式字符串
        String yearMonth = DateUtil.formatYearMonth();
        System.out.println("当前年月: " + yearMonth); // 输出: 202403
        
        // 获取年月文件夹路径
        String folderPath = DateUtil.getYearMonthFolder();
        System.out.println("文件夹路径: " + folderPath); // 输出: 202403/
        
        // 格式化当前时间
        System.out.println("当前时间: " + DateUtil.format(new Date()));
        
        // 获取今天开始和结束时间
        System.out.println("今天开始: " + DateUtil.format(DateUtil.getStartOfDay(new Date())));
        System.out.println("今天结束: " + DateUtil.format(DateUtil.getEndOfDay(new Date())));
        
        // 日期计算
        Date tomorrow = DateUtil.addDays(new Date(), 1);
        System.out.println("明天: " + DateUtil.format(tomorrow));
        
        // 月份第一天和最后一天
        System.out.println("本月第一天: " + DateUtil.format(DateUtil.getFirstDayOfMonth()));
        System.out.println("本月最后一天: " + DateUtil.format(DateUtil.getLastDayOfMonth()));
        
        // 时间戳
        System.out.println("当前时间戳: " + DateUtil.getCurrentTimestamp());
    }
}
