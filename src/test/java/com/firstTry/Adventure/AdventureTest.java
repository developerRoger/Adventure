package com.firstTry.Adventure;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class AdventureTest {
	
	  public static void main(String[] args) {
		  System.out.println(new BCryptPasswordEncoder().encode("123"));
	}
}
