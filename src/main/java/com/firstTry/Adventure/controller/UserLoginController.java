package com.firstTry.Adventure.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserLoginController {

/*	@RequestMapping("/index")
	public String index() {
		System.out.println("---------------");
		return "Hello World";
	}*/

	@RequestMapping("/hi")
	public String hello() throws Exception {
		throw new Exception("发生错误");
	}
}
