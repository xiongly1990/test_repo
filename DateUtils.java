import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 日期转换工具类
 * 提供各种日期格式转换、计算等功能
 */
public class DateUtils {

    // 常用日期时间格式
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATETIME_MS_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String COMPACT_FORMAT = "yyyyMMddHHmmss";
    public static final String CHINESE_DATE_FORMAT = "yyyy年MM月dd日";
    public static final String CHINESE_DATETIME_FORMAT = "yyyy年MM月dd日 HH时mm分ss秒";

    // ==================== 字符串转日期 ====================

    /**
     * 字符串转LocalDate（默认格式：yyyy-MM-dd）
     */
    public static LocalDate stringToLocalDate(String dateStr) {
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    /**
     * 字符串转LocalDate（自定义格式）
     */
    public static LocalDate stringToLocalDate(String dateStr, String pattern) {
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 字符串转LocalDateTime（默认格式：yyyy-MM-dd HH:mm:ss）
     */
    public static LocalDateTime stringToLocalDateTime(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(DATETIME_FORMAT));
    }

    /**
     * 字符串转LocalDateTime（自定义格式）
     */
    public static LocalDateTime stringToLocalDateTime(String dateTimeStr, String pattern) {
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 字符串转LocalTime（默认格式：HH:mm:ss）
     */
    public static LocalTime stringToLocalTime(String timeStr) {
        return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern(TIME_FORMAT));
    }

    /**
     * 字符串转LocalTime（自定义格式）
     */
    public static LocalTime stringToLocalTime(String timeStr, String pattern) {
        return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern(pattern));
    }

    // ==================== 日期转字符串 ====================

    /**
     * LocalDate转字符串（默认格式：yyyy-MM-dd）
     */
    public static String localDateToString(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    /**
     * LocalDate转字符串（自定义格式）
     */
    public static String localDateToString(LocalDate date, String pattern) {
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * LocalDateTime转字符串（默认格式：yyyy-MM-dd HH:mm:ss）
     */
    public static String localDateTimeToString(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
    }

    /**
     * LocalDateTime转字符串（自定义格式）
     */
    public static String localDateTimeToString(LocalDateTime dateTime, String pattern) {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * LocalTime转字符串（默认格式：HH:mm:ss）
     */
    public static String localTimeToString(LocalTime time) {
        return time.format(DateTimeFormatter.ofPattern(TIME_FORMAT));
    }

    /**
     * LocalTime转字符串（自定义格式）
     */
    public static String localTimeToString(LocalTime time, String pattern) {
        return time.format(DateTimeFormatter.ofPattern(pattern));
    }

    // ==================== 时间戳转换 ====================

    /**
     * 时间戳（毫秒）转LocalDateTime
     */
    public static LocalDateTime timestampToLocalDateTime(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    /**
     * 时间戳（秒）转LocalDateTime
     */
    public static LocalDateTime timestampSecondsToLocalDateTime(long timestampSeconds) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestampSeconds), ZoneId.systemDefault());
    }

    /**
     * LocalDateTime转时间戳（毫秒）
     */
    public static long localDateTimeToTimestamp(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 时间戳（毫秒）转LocalDate
     */
    public static LocalDate timestampToLocalDate(long timestamp) {
        return timestampToLocalDateTime(timestamp).toLocalDate();
    }

    /**
     * LocalDate转时间戳（毫秒，当天00:00:00）
     */
    public static long localDateToTimestamp(LocalDate date) {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 获取当前时间戳（毫秒）
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 获取当前时间戳（秒）
     */
    public static long getCurrentTimestampSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    // ==================== Date与Java8时间类型互转 ====================

    /**
     * Date转LocalDateTime
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * Date转LocalDate
     */
    public static LocalDate dateToLocalDate(Date date) {
        return dateToLocalDateTime(date).toLocalDate();
    }

    /**
     * LocalDateTime转Date
     */
    public static Date localDateTimeToDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * LocalDate转Date（当天00:00:00）
     */
    public static Date localDateToDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    // ==================== 获取当前日期时间 ====================

    /**
     * 获取当前日期
     */
    public static LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    /**
     * 获取当前时间
     */
    public static LocalTime getCurrentTime() {
        return LocalTime.now();
    }

    /**
     * 获取当前日期时间
     */
    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    /**
     * 获取当前日期字符串（默认格式）
     */
    public static String getCurrentDateString() {
        return localDateToString(getCurrentDate());
    }

    /**
     * 获取当前日期时间字符串（默认格式）
     */
    public static String getCurrentDateTimeString() {
        return localDateTimeToString(getCurrentDateTime());
    }

    // ==================== 日期计算 ====================

    /**
     * 日期加减天数
     */
    public static LocalDate plusDays(LocalDate date, long days) {
        return date.plusDays(days);
    }

    /**
     * 日期加减月数
     */
    public static LocalDate plusMonths(LocalDate date, long months) {
        return date.plusMonths(months);
    }

    /**
     * 日期加减年数
     */
    public static LocalDate plusYears(LocalDate date, long years) {
        return date.plusYears(years);
    }

    /**
     * 日期加减周数
     */
    public static LocalDate plusWeeks(LocalDate date, long weeks) {
        return date.plusWeeks(weeks);
    }

    /**
     * 日期时间加减小时
     */
    public static LocalDateTime plusHours(LocalDateTime dateTime, long hours) {
        return dateTime.plusHours(hours);
    }

    /**
     * 日期时间加减分钟
     */
    public static LocalDateTime plusMinutes(LocalDateTime dateTime, long minutes) {
        return dateTime.plusMinutes(minutes);
    }

    /**
     * 日期时间加减秒数
     */
    public static LocalDateTime plusSeconds(LocalDateTime dateTime, long seconds) {
        return dateTime.plusSeconds(seconds);
    }

    /**
     * 计算两个日期之间的天数
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * 计算两个日期时间之间的小时数
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * 计算两个日期时间之间的分钟数
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * 计算两个日期时间之间的秒数
     */
    public static long secondsBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.SECONDS.between(start, end);
    }

    // ==================== 获取日期各部分 ====================

    /**
     * 获取年份
     */
    public static int getYear(LocalDate date) {
        return date.getYear();
    }

    /**
     * 获取月份（1-12）
     */
    public static int getMonth(LocalDate date) {
        return date.getMonthValue();
    }

    /**
     * 获取日期（1-31）
     */
    public static int getDayOfMonth(LocalDate date) {
        return date.getDayOfMonth();
    }

    /**
     * 获取星期几（1-7，周一到周日）
     */
    public static int getDayOfWeek(LocalDate date) {
        return date.getDayOfWeek().getValue();
    }

    /**
     * 获取小时（0-23）
     */
    public static int getHour(LocalDateTime dateTime) {
        return dateTime.getHour();
    }

    /**
     * 获取分钟（0-59）
     */
    public static int getMinute(LocalDateTime dateTime) {
        return dateTime.getMinute();
    }

    /**
     * 获取秒（0-59）
     */
    public static int getSecond(LocalDateTime dateTime) {
        return dateTime.getSecond();
    }

    // ==================== 特殊日期获取 ====================

    /**
     * 获取某月的第一天
     */
    public static LocalDate getFirstDayOfMonth(LocalDate date) {
        return date.withDayOfMonth(1);
    }

    /**
     * 获取某月的最后一天
     */
    public static LocalDate getLastDayOfMonth(LocalDate date) {
        return date.withDayOfMonth(date.lengthOfMonth());
    }

    /**
     * 获取某年的第一天
     */
    public static LocalDate getFirstDayOfYear(LocalDate date) {
        return date.withDayOfYear(1);
    }

    /**
     * 获取某年的最后一天
     */
    public static LocalDate getLastDayOfYear(LocalDate date) {
        return date.withDayOfYear(date.lengthOfYear());
    }

    /**
     * 获取本周一
     */
    public static LocalDate getMondayOfWeek(LocalDate date) {
        return date.with(DayOfWeek.MONDAY);
    }

    /**
     * 获取本周日
     */
    public static LocalDate getSundayOfWeek(LocalDate date) {
        return date.with(DayOfWeek.SUNDAY);
    }

    // ==================== 日期比较 ====================

    /**
     * 判断日期是否在指定范围内
     */
    public static boolean isBetween(LocalDate date, LocalDate start, LocalDate end) {
        return !date.isBefore(start) && !date.isAfter(end);
    }

    /**
     * 判断是否为闰年
     */
    public static boolean isLeapYear(LocalDate date) {
        return date.isLeapYear();
    }

    /**
     * 判断是否为今天
     */
    public static boolean isToday(LocalDate date) {
        return date.equals(LocalDate.now());
    }

    // ==================== 格式转换 ====================

    /**
     * 将日期从一种格式转换为另一种格式
     */
    public static String convertDateFormat(String dateStr, String fromPattern, String toPattern) {
        LocalDate date = stringToLocalDate(dateStr, fromPattern);
        return localDateToString(date, toPattern);
    }

    /**
     * 将日期时间从一种格式转换为另一种格式
     */
    public static String convertDateTimeFormat(String dateTimeStr, String fromPattern, String toPattern) {
        LocalDateTime dateTime = stringToLocalDateTime(dateTimeStr, fromPattern);
        return localDateTimeToString(dateTime, toPattern);
    }
}