package com.firstTry.Adventure.exception;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.firstTry.Adventure.utils.Page;
/**
 * throw new 异常捕获返回
 * 所有抛出的异常都可以在这里面进行拦截自定义返回
 * 支持modeanview界面返回和json格式返回
 * @author Roger
 *
 */
@ControllerAdvice
public class GlobalExceptionHandler {
	  //错误
	  public static final String DEFAULT_ERROR_VIEW = "Error";
	  
	  	/**
	  	 * value里面的值为你想捕获的异常,如Exception/也可以写具体的异常实现类
	  	 * @param req
	  	 * @param e
	  	 * @return
	  	 * @throws Exception
	  	 */
	    @ExceptionHandler(value = Exception.class)
	    public ModelAndView defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
	        ModelAndView mav = new ModelAndView();
	        mav.addObject("exception", e);
	        mav.addObject("url", req.getRequestURL());
	        mav.setViewName(DEFAULT_ERROR_VIEW);
	        return mav;
	    }
	    /**
	     * (捕获的自定义异常)返回为json格式的异常
	     * @param req
	     * @param e
	     * @return
	     * @throws Exception
	     */
	    @ExceptionHandler(value = RRException.class)
	    @ResponseBody
	    public Page<SessionTimeOutException> jsonErrorHandler(HttpServletRequest req, MyException e) throws Exception {
	    	Page<SessionTimeOutException> r = new Page<SessionTimeOutException>();
	    	SessionTimeOutException s=new SessionTimeOutException("404", e.getMessage());
	    	r.setQueryObj(s);
	        return r;
	    }
}
