package com.firstTry.Adventure.exception;


/**
 * session time out。<br>
 * 运行时异常，由Handler统一处理。<br>
 * 
 * @author billy.lin
 */
public class SessionTimeOutException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	protected String code;
	protected String message;

	public SessionTimeOutException(String code, String message) {
		super(message);
		this.code = code;
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
