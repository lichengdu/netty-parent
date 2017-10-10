package org.netty.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * httpserver服务的一些配置参数
 * @author 黄永丰
 * @createtime 2016年2月3日
 * @version 1.0
 */
public class HttpServerUtil
{
	/**开启的服务端口*/
	public static final int HTTPS_PORT;
	public static final int HTTP_PORT;
	/**是否开启http协议的Keep-Alive*/
	public static final boolean KEEP_ALIVE;
	/**是否支持ssl连接*/
	public static final boolean HTTPS_SSL;
	public static final boolean HTTP_SSL;
	/**是否双向加密*/
	public static final boolean WAY_ENCRYPTION;
	/**数据读入超时时间*/
	public static final int READTIME;
	/**输入写入超时时间*/
	public static final int WRITETIME;
	/**aggregator返回数据大小*/
	public static final int AGGREGATOR;
	/**用于分配处理业务线程的线程组个数*/
	public static final int BIZ_GROUP_SIZE;
	/**业务出现线程大小  */
	public static final int BIZ_THREAD_SIZE;
	
	static
	{
		try
		{
			InputStream is = HttpServerUtil.class.getResourceAsStream("/httpserver.properties");
			Properties pro = new Properties();
			pro.load(is);
			HTTPS_PORT = Integer.parseInt(pro.getProperty("httpserver.https_port"));
			HTTP_PORT = Integer.parseInt(pro.getProperty("httpserver.http_port"));
			KEEP_ALIVE = Boolean.parseBoolean(pro.getProperty("httpserver.keep_alive"));
			HTTPS_SSL = Boolean.parseBoolean(pro.getProperty("httpserver.https_ssl"));
			HTTP_SSL = Boolean.parseBoolean(pro.getProperty("httpserver.http_ssl"));
			WAY_ENCRYPTION = Boolean.parseBoolean(pro.getProperty("httpserver.way_encryption"));
			READTIME = Integer.parseInt(pro.getProperty("httpserver.readtime"));
			WRITETIME = Integer.parseInt(pro.getProperty("httpserver.writetime"));
			AGGREGATOR = Integer.parseInt(pro.getProperty("httpserver.aggregator"));
			BIZ_GROUP_SIZE = Runtime.getRuntime().availableProcessors()*2; //默认
			BIZ_THREAD_SIZE = Integer.parseInt(pro.getProperty("httpserver.biz_thread_size"));
		}
		catch (IOException e)
		{
			throw new RuntimeException("加载httpserver配置文件失败."+e.getMessage(),e);
		}
	}
	
}

