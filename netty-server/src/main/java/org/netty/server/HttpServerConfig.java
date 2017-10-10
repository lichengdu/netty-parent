package org.netty.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author lichengdu
 *
 */
public class HttpServerConfig {
	
	static Log log = LogFactory.getLog(HttpServerConfig.class);    
	static HttpServerConfig config;
	private Properties properties;
	
	private boolean ssl;
	
	public HttpServerConfig() {
		loadPropertiesFromSrc();
	}
	
	
	public boolean getSsl() {
		return ssl;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	private int port;
	
	public static HttpServerConfig getConfig() {
		if (config == null) {
			config = new HttpServerConfig();
		}
		return config;
	}
	
	
	
	private boolean SERVER_NEED_CLINET_AUTH=false;
	public boolean getSERVER_NEED_CLINET_AUTH() {
		return SERVER_NEED_CLINET_AUTH;
	}

	public void setSERVER_NEED_CLINET_AUTH(boolean sERVER_NEED_CLINET_AUTH) {
		SERVER_NEED_CLINET_AUTH = sERVER_NEED_CLINET_AUTH;
	}

	
	private boolean supportkeepalive;
	public boolean getSupportkeepalive() {
		return supportkeepalive;
	}
	public void setSupportkeepalive(boolean supportkeepalive) {
		this.supportkeepalive = supportkeepalive;
	}

	/**
	 * 加载配置文件
	 */
	public void loadPropertiesFromSrc() {
		InputStream in = null;
		try {
			log.info("begin load httpserver.properties");
			in = HttpServerConfig.class.getClassLoader().getResourceAsStream("httpserver.properties");
			if (in != null) {
				this.properties = new Properties();
				try {
					this.properties.load(in);
				} catch (IOException e) {
					throw e;
				}
			}
			loadProperties(this.properties);
			log.info("load httpserver.properties finished");
		} catch (IOException e) {
			log.info("load httpserver.properties exception "+e.getMessage());
			e.printStackTrace();

			if (in != null)
				try {
					in.close();
				} catch (IOException ie) {
					ie.printStackTrace();
				}
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	/**
	 * 匹配属性文件
	 * @param pro
	 */
	public void loadProperties(Properties pro) {
		String value = "";
		
		value = pro.getProperty("httpserver.ssl");
		if (!StringUtils.isEmpty(value)) {
			this.ssl =Boolean.parseBoolean(value.trim());
		}
		
		value = pro.getProperty("httpserver.supportkeepalive");
		if (!StringUtils.isEmpty(value)) {
			this.supportkeepalive =Boolean.parseBoolean(value.trim());
		}

		value = pro.getProperty("httpserver.port");
		if (!StringUtils.isEmpty(value)) {
			this.port =Integer.parseInt(value.trim());
		}
		
		value = pro.getProperty("httpserver.SERVER_NEED_CLINET_AUTH");
		if (!StringUtils.isEmpty(value)) {
			this.SERVER_NEED_CLINET_AUTH =Boolean.parseBoolean(value.trim());
		}
	}
}
