package com.ruoyi.project.common;

public enum ResultCode {

	SUCCESS(200,"成功")
	,ERROR(500,"系统错误")
	,FORBIDDEN(403,"token失效")
	,BIND_ERROR(400,"参数不合法")
	,NO_TOKEN(401,"无token，请重新登录")
	,ERROR_TOKEN(402,"token异常，请重新登陆")
	,CRON_NOT_CORRECT(10000,"定时规则输入不正确")
	;

    private Integer code;
	private String msg;

	/**
	 * 拿到Result对象
	 * @return
	 */
	public Result get(){
		return new Result().setCode(this.code).setMsg(this.msg);
	}

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

	public static ResultCode getByCode(Integer code){
		ResultCode tab = null;
		ResultCode[] values = ResultCode.values();
		for (ResultCode value : values) {
			if (code.equals(value.code)) {
				tab = value;
			}
		}
		return tab;
	}


}
