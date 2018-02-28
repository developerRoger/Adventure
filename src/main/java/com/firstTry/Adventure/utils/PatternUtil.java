package com.firstTry.Adventure.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 常用正则表达式验证，提供验证正则表达式的方法
 * 
 * @author Roger
 * @version 1.0.0
 */
public class PatternUtil {
	/**
	 * 字符创正则匹配
	 * 
	 * @param value
	 * @param regex
	 * @return return true if matches
	 */
	public static boolean match(String toCheck, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(toCheck);
		return matcher.matches();
	}

	/**
	 * 正整数
	 * 
	 * @param toCheck
	 * @return return true if [toCheck] is positive numeric
	 */
	public static boolean isPositiveNum(String toCheck) {
		String regex = "^([1-9][0-9]*)$";
		return match(toCheck, regex);
	}

	/**
	 * 负整数，0，正整数
	 * 
	 * @param toCheck
	 * @return return true if [toCheck] is numeric
	 */
	public static boolean isNumeric(String toCheck) {
		String regex = "^([0])|(\\-?[1-9][0-9]*)$";
		return match(toCheck, regex);
	}

	/**
	 * 小数(最多4位)，包含负数
	 * 
	 * @param toCheck
	 * @return return true if [toCheck] is decimal
	 */
	public static boolean isDecimal(String toCheck) {
		String regex = "^(\\-?[0-9]*\\.\\d{1,4})$";
		return match(toCheck, regex);
	}

	/**
	 * 由26个引文字母组成的字符串
	 * 
	 * @param toCheck
	 * @return return true if [toCheck] is character
	 */
	public static boolean isCharacter(String toCheck) {
		String regex = "^[A-Za-z]+$";
		return match(toCheck, regex);
	}

	/**
	 * 密码验证：长度在6-18之间，以数字、字母和下划线组成
	 * 
	 * @param toCheck
	 * @return return true if [toCheck] is password pattern
	 */
	public static boolean isPassword(String toCheck) {
		String regex = "^[\\w\\_]{6,18}$";
		return match(toCheck, regex);
	}

	/**
	 * 电话号码验证：<br>
	 * 010-12345678<br>
	 * 0912-1234567<br>
	 * (010)-12345678<br>
	 * (0912)1234567<br>
	 * (010)12345678<br>
	 * (0912)-1234567<br>
	 * 01012345678<br>
	 * 09121234567<br>
	 * 
	 * @param toCheck
	 * @return
	 */
	public static boolean isPhone(String toCheck) {
		String regex = "^(^0\\d{2}-?\\d{8}$)|(^0\\d{3}-?\\d{7}$)|(^\\(0\\d{2}\\)-?\\d{8}$)|(^\\(0\\d{3}\\)-?\\d{7}$)$";
		return match(toCheck, regex);
	}

	/**
	 * 手机验证：1开头的11位数字
	 * 
	 * @param toCheck
	 * @return return true if [toCheck] is mobile
	 */
	public static boolean isMobile(String toCheck) {
		String regex = "^([+]\\d{1,4})?1[3,5,7,8]\\d{9}$";
		return match(toCheck, regex);
	}

	/**
	 * 电子邮箱验证
	 * 
	 * @param toCheck
	 * @return return true if [toCheck] is email
	 */
	public static boolean isEmail(String toCheck) {
		String regex = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
		return match(toCheck, regex);
	}

	/**
	 * 身份证验证
	 * 
	 * @param toCheck
	 * @return return true if [toCheck] is id card
	 */
	public static boolean isIdCard(String toCheck) {
		String regex = "^(^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$)|(^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])((\\d{4})|\\d{3}[Xx])$)$";
		return match(toCheck, regex);
	}

	/**
	 * 邮政编码
	 * 
	 * @param toCheck
	 * @return return true if [toCheck] is post code
	 */
	public static boolean isPostCode(String toCheck) {
		String regex = "^\\d{6}$";
		return match(toCheck, regex);
	}

	/**
	 * IP地址验证
	 * 
	 * @param toCheck
	 * @return return true if [toCheck] is ip address
	 */
	public static boolean isIP(String toCheck) {
		String regex = "^((([1-9]\\d?)|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))\\.){3}(([1-9]\\d?)|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))$";
		return match(toCheck, regex);
	}

	/**
	 * 中文验证
	 * 
	 * @param toCheck
	 * @return return true if [toCheck] is Chinese
	 */
	public static boolean isChinese(String toCheck) {
		String regex = "^[\u4e00-\u9fa5]+$";
		return match(toCheck, regex);
	}

	/**
	 * URL：http https和ftp
	 * 
	 * @param toCheck
	 * @return return true if [toCheck] is URL
	 */
	public static boolean isURL(String toCheck) {
		String regex = "^(([hH][tT]{2}[pP][sS]?)|([fF][tT][pP]))\\:\\/\\/[wW]{3}\\.[\\w-]+\\.\\w{2,4}(\\/.*)?$";
		return match(toCheck, regex);
	}

	/**
	 * 日期格式：2012-05-14 2012/05/6 2012.5.14 20120528
	 * 
	 * @param toCheck
	 * @return return true if [toCheck] is date
	 */
	public static boolean isDate(String toCheck) {
		String regex = "^[1-9]\\d{3}([-|\\/|\\.])?((0\\d)|([1-9])|(1[0-2]))\1(([0|1|2]\\d)|([1-9])|3[0-1])$";
		return match(toCheck, regex);
	}

	/**
	 * 证件号码
	 * 
	 * @param toCheck
	 * @return return true if [toCheck] is certification
	 */
	public static boolean isCertify(String toCheck) {
		String regex = "^\\w*[-_.(]*\\w*[-_.)]*$";
		return match(toCheck, regex);
	}

	/**
	 * 车牌号
	 * 
	 * @param toCheck
	 * @return return true if [toCheck] is car no.
	 */
	public static boolean isCarNo(String toCheck) {
		String regex = "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-Z0-9]{4}[A-Z0-9挂学警港澳]{1}$";
		return match(toCheck, regex);
	}

	public static void main(String[] args) {
		System.out.println(PatternUtil.isNumeric("123123213"));
		System.out.println(PatternUtil.isCertify("123123213.1223"));
		System.out.println(PatternUtil.isPassword("_dac"));
	}
}
