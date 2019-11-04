package com.openxsl.config.exception;

/**
 * ServiceException的错误码
 * @author xiongsl
 */
public enum ErrorCodes {
	//服务器错误类型：
	RUNTIME(100), DB(101), JMS(102), CACHE(103), DUBBO(104), MAIL(105), HTTP(106), FTP(107), REMOTING(108),
	//签名验签、加解密、ACL、重复操作（幂等性）、查询不到
	SIGNATURE(110), ENCRYPT(111), ACCESS_DENIED(112), 
	//服务配置错误
	ILL_SERVICE(113), ILL_PROTOCOL(114), ILL_TEMPLATE(115), MIS_CONF_ITEM(116), ILL_CONF_DOMAIN(117),
	//数据方面：服务幂等性、不存在、格式、过期或无效
	BIZ_DONE(120), DATA_EXISTS(121), DATA_NOTFOUND(123), DATA_FORMAT(124), DATA_EXPIRES(125),
	//服务提供者(dubbo)、消费者(jms)、序列化(ws)
	NO_PROVIDER(130), NO_CONSUMER(131), UN_MARSHAL(132),
	//客户端：参数校验、请求超时、服务找不到、未取到结果
	VALIDATION(400), TIMEOUT(408), REQ_FORBIT(403), NOT_FOUND(404), ERR_HEADER(406),
	;
	
	//银行（对方系统）的错误码都是4位，需要翻译

	private int code;
	private ErrorCodes(int code){
		this.code = code;
	}
	
	public int code(){
		return code;
	}
	
}
