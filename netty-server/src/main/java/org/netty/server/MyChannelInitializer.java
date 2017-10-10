package org.netty.server;

import javax.net.ssl.SSLEngine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

public class MyChannelInitializer extends ChannelInitializer<SocketChannel> {
	Log log = LogFactory.getLog(MyChannelInitializer.class); 
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		if (HttpServerConfig.getConfig().getSsl()) {
			log.info("启动ssl访问通过https方式");
			SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
			engine.setNeedClientAuth(false);
			engine.setUseClientMode(false);
			engine.setWantClientAuth(false);
			engine.setEnabledProtocols(new String[] { "SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2" });
			pipeline.addLast("ssl", new SslHandler(engine));
		}
		// 40秒没有数据读入，发生超时机制
		pipeline.addLast(new ReadTimeoutHandler(40));

		// 40秒没有输入写入，发生超时机制
		pipeline.addLast(new WriteTimeoutHandler(40));

		/**
		 * http-request解码器 http服务器端对request解码
		 */
		pipeline.addLast("decoder", new HttpRequestDecoder());

		/**
		 * http-response解码器 http服务器端对response编码
		 */
		pipeline.addLast("encoder", new HttpResponseEncoder());

		/**
		 * HttpObjectAggregator会把多个消息转换为一个单一的FullHttpRequest或是FullHttpResponse。
		 */
		// 1048576
		// 1024*1024*64
		pipeline.addLast("aggregator", new HttpObjectAggregator(1024 * 1024 * 8));

		/**
		 * 压缩 Compresses an HttpMessage and an HttpContent in gzip or deflate
		 * encoding while respecting the "Accept-Encoding" header. If there is
		 * no matching encoding, no compression is done.
		 */
		pipeline.addLast("deflater", new HttpContentCompressor());

		//回调处理类MyServerHandler
		pipeline.addLast("handler", new MyServerHandler());

	}

}
