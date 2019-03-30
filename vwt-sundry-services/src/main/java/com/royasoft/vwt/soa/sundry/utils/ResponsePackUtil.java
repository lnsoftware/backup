package com.royasoft.vwt.soa.sundry.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponsePackUtil {
	public static final Logger logger = LoggerFactory.getLogger(ResponsePackUtil.class);

	/**
	 * 打包应答实体
	 * 
	 * @param code
	 * @param body
	 * @return
	 */
	public static Response buildResponse(String code, Object body) {
		Response response = new Response();
		response.setResponse_code(code);
		response.setResponse_body(body);
		response.setResponse_desc(ResponseInfoConstant.responseMap.get(code));
		return response;
	}
}
