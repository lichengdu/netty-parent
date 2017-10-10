package org.netty.entry;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;


public class ResultStruct
{
	
	private Map<String, Object> retMap;

	public ResultStruct()
	{
		retMap = new HashMap<String, Object>();
		retMap.put("ret", 0);
		retMap.put("message", "success");
	}

	public ResultStruct(String message)
	{
		retMap = new HashMap<String, Object>();
		retMap.put("ret", 0);
		retMap.put("message", message);
	}

	public ResultStruct(String message, String key, Object value)
	{
		retMap.put("message", message);
		retMap.put(key, value);
	}

	public ResultStruct(String message, Map<String, Object> map)
	{
		retMap = map;
		retMap.put("ret", 0);
		retMap.put("message", message);
	}

	public ResultStruct(int ret, String message)
	{
		retMap = new HashMap<String, Object>();
		retMap.put("ret", ret);
		retMap.put("message", message);
	}
	
	@Override
	public String toString()
	{
		String result = JSONObject.toJSONString(retMap);
		return result;
	}
	
}
