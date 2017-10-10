package org.netty.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.netty.util.GlobalContainer;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {
	Log log = LogFactory.getLog(NettyServer.class); 
	private static int port;
	
	public static void main(String[] args) throws Exception {
		port = HttpServerConfig.getConfig().getPort();
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
		applicationContext.start();
		GlobalContainer.setApplicationContext(applicationContext);
		new NettyServer().run();
	}

	public void run() throws Exception {
		log.info("netty Server 开始启动服务");
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new MyChannelInitializer());
			Channel ch = b.bind(port).sync().channel();
			log.info("netty Server 服务启动成功 端口:"+port);
			ch.closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

}