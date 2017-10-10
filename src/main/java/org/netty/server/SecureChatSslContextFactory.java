package org.netty.server;

import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

public final class SecureChatSslContextFactory {

	private static final String PROTOCOL = "SSL";
	private static final SSLContext SERVER_CONTEXT;
	/**
	 * 证书文件生成方式
	 * 
	 * keytool -genkey -keysize 2048 -validity 365 -keyalg RSA -keypass 123456 -storepass 123456 -keystore F:lichengdu.jks
	 * keytool为JDK提供的生成证书工具 
	 * -keysize 2048 密钥长度2048位（这个长度的密钥目前可认为无法被暴力破解）
	 * -validity 365 证书有效期365天
	 * -keyalg RSA 使用RSA非对称加密算法 
	 * -keypass 123456 密钥的访问密码为123456 
	 * -storepass 123456 密钥库的访问密码为123456（通常都设置一样，方便记） 
	 * -keystore F:lichengdu.jks 指定生成的密钥库文件为F:lichengdu.jks
	 * 完了之后就拿到了F:lichengdu.jks
	 */
	private static String SERVER_KEY_STORE = "lichengdu.jks";
	//证书密码storepass
	private static String SERVER_KEY_STORE_PASSWORD = "123456";

	static {

		SSLContext serverContext;
		try {

			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			// 获得生成的jks签名证书
			InputStream ksInputStream = SecureChatSslContextFactory.class.getClassLoader()
					.getResourceAsStream(SERVER_KEY_STORE);
			ks.load(ksInputStream, SERVER_KEY_STORE_PASSWORD.toCharArray());

			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, SERVER_KEY_STORE_PASSWORD.toCharArray());

			serverContext = SSLContext.getInstance(PROTOCOL);
			serverContext.init(kmf.getKeyManagers(), null, null);

		} catch (Exception e) {
			throw new Error("Failed to initialize the server-side SSLContext", e);
		}

		SERVER_CONTEXT = serverContext;
	}

	public static SSLContext getServerContext() {
		return SERVER_CONTEXT;
	}

	private SecureChatSslContextFactory() {
		// Unused
	}
}
