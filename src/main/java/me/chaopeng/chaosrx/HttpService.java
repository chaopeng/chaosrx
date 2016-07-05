package me.chaopeng.chaosrx;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * HttpService
 *
 * @author chao
 * @version 1.0 - 11/24/14
 */
public class HttpService {

    private static final Logger logger = LoggerFactory.getLogger(HttpService.class);

    private int port;
    private HttpServiceHandler httpServiceHandler;
    private int numberOfBossThread;
    private int numberOfWorkerThread;

    public HttpService(int port, HttpServiceHandler httpServiceHandler) throws InterruptedException {
        this(port, httpServiceHandler, 2, Runtime.getRuntime().availableProcessors());
    }

    public HttpService(int port, HttpServiceHandler httpServiceHandler, int numberOfBossThread, int numberOfWorkerThread) throws InterruptedException {
        this.port = port;
        this.httpServiceHandler = new HttpServiceHandler();
        this.httpServiceHandler = httpServiceHandler;
        this.numberOfBossThread = numberOfBossThread;
        this.numberOfWorkerThread = numberOfWorkerThread;
    }

    public void start() throws InterruptedException {
        EventLoopGroup boss = new NioEventLoopGroup(this.numberOfBossThread);
        EventLoopGroup workers = new NioEventLoopGroup(this.numberOfWorkerThread);

        ServerBootstrap b = new ServerBootstrap();
        b.option(ChannelOption.SO_BACKLOG, 1024);
        b.group(boss, workers)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(1024 * 1024 * 64))
                                .addLast(httpServiceHandler)
                        ;
                    }
                });

        b.bind(port).sync().channel();

        logger.info("http service start at " + port);
    }

}
