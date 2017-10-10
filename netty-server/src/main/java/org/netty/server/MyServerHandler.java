package org.netty.server;

import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.netty.entry.ResultStruct;
import org.netty.util.GlobalContainer;
import org.netty.util.MethodReflect;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.CharsetUtil;

public class MyServerHandler extends ChannelHandlerAdapter {
	Log log = LogFactory.getLog(MyServerHandler.class);
	private String buf = "";
	private StringBuilder requestbuffer = new StringBuilder();
	private Map<String, String> origin_params;
	Gson gson = new Gson();
	private HttpPostRequestDecoder decoder;
	private String interfaceName;
	private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); // Disk

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		if (decoder != null) {
			decoder.cleanFiles();
		}
		ctx.flush();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		log.info("请求处理开始");
		buf = "";
		HttpRequest request = (HttpRequest) msg;
		boolean isSSl = HttpServerConfig.getConfig().getSsl();
		@SuppressWarnings("unused")
		String type = "http";
		if (isSSl) {
			type = "https";
		}
		try {
			URI uri = new URI(request.uri());
			log.info("请求uri:" + uri.getPath());
			// 每次浏览器请求都会多一次这个请求的，直接return就可以了
			if (uri.getPath().equals("/favicon.ico")) {
				ctx.close();
				return;
			}
			System.out.println(uri.getPath());
		} catch (Exception e) {
			requestbuffer.append("URI解释出错." + e.getMessage());
			buf = "URI解释出错." + e.getMessage();
			writeResponse(request, ctx);
			log.error(requestbuffer.toString());
			return;
		}
		
		if (request.method().equals(HttpMethod.GET)) {
			try {
				log.info("get请求！");
				origin_params = getHttpGetParams(request);
				if (origin_params != null) {
					log.info("请求参数:" + origin_params);
					// buf="你好:"+origin_params.get("name")+",你发起了"+type+"的get请求";
					buf = doPost(origin_params);
				}
				writeResponse(request, ctx);
			} catch (Exception e) {
				buf = "HTTP/HTTPS POST处理出错." + e.getMessage();
				requestbuffer.append("请求完返回结果信息:");
				requestbuffer.append(buf + "\r\n");
				writeResponse(request, ctx);
				log.error(requestbuffer.toString());
				return;
			}
		} else if (request.method().equals(HttpMethod.POST)) {
			log.info("Post请求！");
			try {
				// 获取http post请求参数
				Map<String, String> paramsMap = getHttpPostParams(request);
				//redirectflag = ObjectParser.toInteger(paramsMap.get("redirectflag"));
				requestbuffer.append("请求参数数据:" + JSONObject.toJSONString(paramsMap) + "\r\n");
				buf = doPost(paramsMap);
				// 将http响应的结果返回给用户
				writeResponse(request, ctx);
				log.info(requestbuffer.toString());
				return;
			} catch (Exception e) {
				buf = "HTTP/HTTPS POST处理出错." + e.getMessage();
				requestbuffer.append("请求完返回结果信息:");
				requestbuffer.append(buf + "\r\n");
				writeResponse(request, ctx);
				log.error(requestbuffer.toString());
				return;
			}
		}

	}

	private Map<String, String> getHttpGetParams(HttpRequest request) {
		return getQueryParams(request.uri());
	}

	private Map<String, String> getQueryParams(String params) {
		QueryStringDecoder queryStringDecoder = new QueryStringDecoder(params);
		Map<String, String> paramsMap = new HashMap<String, String>();
		for (Entry<String, List<String>> p : queryStringDecoder.parameters().entrySet()) {
			String key = p.getKey().trim();
			List<String> vals = p.getValue();
			if (vals.size() > 0) {
				String value = vals.get(0);
				requestbuffer.append(key + ":" + value + "\n");
				paramsMap.put(key, value);
			}
		}
		return paramsMap;
	}


	@SuppressWarnings("unchecked")
	private Map<String, String> getHttpPostParams(HttpRequest request) {
		Map<String, String> paramsMap = new HashMap<String, String>();
		// 直接json方式传入参数{id:12,name:maple}
		Map<String, String> objMap = null;
		try {
			HttpContent httpContent = (HttpContent) request;
			String data = URLDecoder.decode(httpContent.content().toString(CharsetUtil.UTF_8), "UTF-8");
			objMap = JSONObject.parseObject(data, Map.class);
		} catch (Exception e) {
			objMap = null;
		}
		if (objMap != null) {
			paramsMap.putAll(objMap);
			return paramsMap;
		}
		try {
			// 正常post传入参数id=12&name=maple
			HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(factory, request);
			while (decoder.hasNext()) {
				InterfaceHttpData ifhData = decoder.next();
				if (ifhData != null) {
					try {
						/**
						 * HttpDataType有三种类型 Attribute, FileUpload,
						 * InternalAttribute
						 */
						if (ifhData.getHttpDataType() == HttpDataType.Attribute) {
							Attribute attribute = (Attribute) ifhData;
							String value = attribute.getValue();
							String key = attribute.getName();
							paramsMap.put(key, value);
						}
					} finally {
						ifhData.release();
					}
				}
			}
			// 直接json方式传入参数{id:12,name:maple}
			if (paramsMap.isEmpty()) {
				HttpContent httpContent = (HttpContent) request;
				String data = URLDecoder.decode(httpContent.content().toString(CharsetUtil.UTF_8), "UTF-8");
				Map<String, String> obj = JSONObject.parseObject(data, Map.class);
				paramsMap.putAll(obj);
			}
		} catch (EndOfDataDecoderException ex) {
			// 最后参数异常不用处理
			log.info(ex.getMessage());
		} catch (Exception e) {
			throw new RuntimeException("解释HTTP/HTTPS POST协议出错." + e.getMessage(), e);
		}
		return paramsMap;
	}

	/**
	 * 请求dubbo
	 * 
	 * @param paramsMap
	 * @return
	 */
	private String doPost(Map<String, String> paramsMap) {
		// 获得几口名
		interfaceName = paramsMap.get("method");
		requestbuffer.append("调用接口:" + interfaceName + "\r\n");
		if (!paramsMap.containsKey("method"))
			return new ResultStruct(-1, "参数并不包含method,请检查提交的参数.").toString();
		// 获取bubbo服务名称
		String serviceName = getServiceName(interfaceName.toLowerCase());
		if (serviceName == null)
			return new ResultStruct(-1, "参数method出错,请检查提交的参数.").toString();
		// 获取dubbo服务对象
		Object client = GlobalContainer.getApplicationContext().getBean(serviceName);
		if (client == null)
			return new ResultStruct(-1, "服务名称为" + serviceName + "为空,请联系后台管理员.").toString();
		// 用反射调用对应系统的方法
		String data = (String) MethodReflect.invokeMethod(client, "doPost", new Object[] { paramsMap });
		if (data == null)
			return new ResultStruct(-1, "调用接口方法为[" + interfaceName + "]失败,返回null.").toString();
		try {
			JSONObject resultObj = JSONObject.parseObject(data);
			if (resultObj.get("mYlogMessage") != null) {
				requestbuffer.append("调用接口返回错误日志信息:" + resultObj.get("mYlogMessage") + "\r\n");
				resultObj.remove("mYlogMessage");
				data = resultObj.toString();
			}
		} catch (Exception e) {
			return data;
		}
		return data;
	}

	/**
	 * 将结果返回给用户
	 * 
	 * @param currentObj
	 * @param ctx
	 * @return
	 */
	private void writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
				currentObj.decoderResult().isSuccess() ? HttpResponseStatus.OK : HttpResponseStatus.BAD_REQUEST,
				Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));
		log.info(buf);
		ChannelFuture future = ctx.writeAndFlush(response);

		log.info("请求响应处理成功");
		future.addListener(ChannelFutureListener.CLOSE);
		// requestbuffer.append("\n---------------服务器主动关闭远程链接.---------------------");

	}

	/**
	 * 通过过接口名称找到对应的服务名称
	 * 
	 * @author 李成都
	 * @createtime 2017年8月23日
	 * @param interfaceName
	 *            接口名称
	 * @return 返回dubbo服务名称
	 */
	public static String getServiceName(String interfaceName) {
		String result = null;
		String[] strs = interfaceName.split("\\.");
		if (strs.length > 2) {
			result = strs[1];
			result += "Service";
		}
		return result;
	}
}