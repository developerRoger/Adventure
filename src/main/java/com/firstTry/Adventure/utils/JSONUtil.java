package com.firstTry.Adventure.utils;

import java.io.IOException;
import java.util.TimeZone;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class JSONUtil {
	public static String toJsonStr(Object o) {
		String result = null;
		ObjectMapper om = new ObjectMapper();
		// Long 转字符串
		// null替换为""
		om.getSerializerProvider().setNullValueSerializer(new JsonSerializer<Object>() {
			@Override
			public void serialize(Object arg0, JsonGenerator arg1, SerializerProvider arg2) throws IOException, JsonProcessingException {
				arg1.writeString("");
			}
		});
		om.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		SimpleModule simpleModule = new SimpleModule();
		simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
		simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
		
		om.registerModule(simpleModule);
		try {
			result = om.writeValueAsString(o);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static Object toObject(String jsonStr, Class<?> clazz) {
		Object result = null;
		ObjectMapper om = new ObjectMapper();
		try {
			result = om.readValue(jsonStr, clazz);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
