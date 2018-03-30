package com.firstTry.Adventure.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firstTry.Adventure.exception.MyException;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Roger
 * @email luojie2luojuan@qq.com
 * @date 2018-3-19
 *
 */
public class JSONMapperUtil {

    public static <T> List<T> obj2List(Object object, TypeReference<?> valueTypeRef) throws MyException {
        return json2List(JSONUtil.toJsonStr(object), valueTypeRef);
    }

    public static <T> List<T> json2List(String jsonStr, TypeReference<?> valueTypeRef) throws MyException {
        List<T> result = json2Generic(jsonStr, valueTypeRef);
        if (result == null){
            return new ArrayList<>();
        }

        return result;
    }
    
    public static <T>T json2Generic(String jsonStr, TypeReference<?> valueTypeRef) throws MyException{
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            return mapper.readValue(jsonStr, valueTypeRef);
        } catch (JsonMappingException e) {
            LoggerFactory.getLogger(JSONMapperUtil.class).error(e.getMessage());
            throw new MyException("json转换异常");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <T> T obj2Object(Object object, Class<T> clazz) throws MyException {
        return json2Object(JSONUtil.toJsonStr(object), clazz);
    }

    public static <T> T json2Object(String jsonStr, Class<T> clazz) throws MyException {
        T result = null;
        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            result = om.readValue(jsonStr, clazz);
        } catch (JsonMappingException e) {
            LoggerFactory.getLogger(JSONMapperUtil.class).error(e.getMessage());
            throw new MyException("json转换异常");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}
