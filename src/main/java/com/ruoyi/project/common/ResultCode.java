package com.ruoyi.project.common;

public enum ResultCode {

	SUCCESS(200,"成功")
	,ERROR(500,"系统错误")
	,CRON_NOT_CORRECT(10000,"定时规则输入不正确")
	;

    private Integer code;
	private String msg;


	ResultCode(Integer status, String msg){
		this.code = status;
		this.msg = msg;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}


}
