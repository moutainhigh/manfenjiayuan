package com.manfenjiayuan.im.param;

import com.alibaba.fastjson.JSON;

public class ParamConverter{
	
	public static <T> T ConvertJsonToClass(String jsonString,Class<T> class1){
		
		return JSON.parseObject(jsonString, class1);
	}
}
