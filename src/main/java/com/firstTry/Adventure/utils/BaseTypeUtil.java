package com.firstTry.Adventure.utils;

/**
 * 基本类型判断，外加String类型判断。
 */
public class BaseTypeUtil {
	/**
	 * 判断是否为byte或Byte
	 * 
	 * @param clazz
	 * @return return true when clazz is byte or Byte
	 */
	public static boolean isByte(Class<?> clazz) {
		String clazzName = clazz.getSimpleName();
		if (clazzName.equals("byte") || clazzName.equals("Byte")) {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否为char或Character
	 * 
	 * @param clazz
	 * @return return true when clazz char byte or Character
	 */
	public static boolean isChar(Class<?> clazz) {
		String clazzName = clazz.getSimpleName();
		if (clazzName.equals("char") || clazzName.equals("Character")) {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否为short或Short
	 * 
	 * @param clazz
	 * @return return true when clazz is short or Short
	 */
	public static boolean isShort(Class<?> clazz) {
		String clazzName = clazz.getSimpleName();
		if (clazzName.equals("short") || clazzName.equals("Short")) {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否为int或Integer
	 * 
	 * @param clazz
	 * @return return true when clazz is int or Integer
	 */
	public static boolean isInt(Class<?> clazz) {
		String clazzName = clazz.getSimpleName();
		if (clazzName.equals("int") || clazzName.equals("Integer")) {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否为long或Long
	 * 
	 * @param clazz
	 * @return return true when clazz is long or Long
	 */
	public static boolean isLong(Class<?> clazz) {
		String clazzName = clazz.getSimpleName();
		if (clazzName.equals("long") || clazzName.equals("Long")) {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否为float或Float
	 * 
	 * @param clazz
	 * @return return true when clazz is float or Float
	 */
	public static boolean isFloat(Class<?> clazz) {
		String clazzName = clazz.getSimpleName();
		if (clazzName.equals("float") || clazzName.equals("Float")) {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否为double或Double
	 * 
	 * @param clazz
	 * @return return true when clazz is double or Double
	 */
	public static boolean isDouble(Class<?> clazz) {
		String clazzName = clazz.getSimpleName();
		if (clazzName.equals("double") || clazzName.equals("Double")) {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否为boolean或Boolean
	 * 
	 * @param clazz
	 * @return return true when clazz is boolean or Boolean
	 */
	public static boolean isBoolean(Class<?> clazz) {
		String clazzName = clazz.getSimpleName();
		if (clazzName.equals("boolean") || clazzName.equals("Boolean")) {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否为String
	 * 
	 * @param clazz
	 * @return return true when clazz is String
	 */
	public static boolean isString(Class<?> clazz) {
		String clazzName = clazz.getSimpleName();
		if (clazzName.equals("String")) {
			return true;
		}
		return false;
	}

	/**
	 * 获得基本类型或String类型的长度
	 * 
	 * @param obj
	 * @return length
	 */
	public static int getLength(Object obj) {
		Class<?> clazz = obj.getClass();
		if (isByte(clazz)) {
			return Byte.toString((byte) obj).length();
		}
		if (isChar(clazz)) {
			return Character.toString((char) obj).length();
		}
		if (isShort(clazz)) {
			return Short.toString((short) obj).length();
		}
		if (isInt(clazz)) {
			return Integer.toString((int) obj).length();
		}
		if (isLong(clazz)) {
			return Long.toString((long) obj).length();
		}
		if (isFloat(clazz)) {
			return Float.toString((float) obj).length();
		}
		if (isDouble(clazz)) {
			return Double.toString((double) obj).length();
		}
		if (isString(clazz)) {
			return obj.toString().length();
		}
		return 0;
	}

	public static void main(String[] args) {
		char a = 'a';
		Character b = 'b';
		Character c = new Character('c');
		System.out.println(getLength(a));
		System.out.println(getLength(b));
		System.out.println(getLength(c));
	}
}
