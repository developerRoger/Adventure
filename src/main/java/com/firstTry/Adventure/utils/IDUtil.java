package com.firstTry.Adventure.utils;

import java.util.UUID;

public class IDUtil {
	/**
	 * 生成UUID，并去除横杆
	 * 
	 * @return UUID String
	 */
	public static String makeUUID() {
		return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
	}

	public static void main(String[] args) {

	}
}
