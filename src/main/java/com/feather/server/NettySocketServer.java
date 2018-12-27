package com.feather.server;

import com.feather.Person;
import com.google.gson.Gson;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@ChannelHandler.Sharable
class FeatherChannelInboundHandler extends ChannelInboundHandlerAdapter{

    private static Gson gson = new Gson();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Person person = gson.fromJson((String) msg,Person.class);
        System.out.println(person);
    }
}

class NameThreadFactory implements ThreadFactory{

    private String mPrefix;

    private boolean daemon;

    private final ThreadGroup threadGroup;

    private static final AtomicInteger POOL_SEQ = new AtomicInteger(0);

    public NameThreadFactory(String prefix,boolean daemon){
        this.mPrefix = prefix + "-thread-";
        this.daemon = daemon;
        SecurityManager securityManager = System.getSecurityManager();
        this.threadGroup = (securityManager == null)?Thread.currentThread().getThreadGroup():securityManager.getThreadGroup();
    }

    @Override
    public Thread newThread(Runnable r) {
        String name = mPrefix + POOL_SEQ.getAndIncrement();
        Thread thread = new Thread(this.threadGroup,r,name,0);
        return thread;
    }
}

public class NettySocketServer{

    private static final int PORT = 8080;

    public static void main(String[] args){
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        ExecutorService bossService = Executors.newCachedThreadPool(new NameThreadFactory("boss",true));
        ExecutorService workerService = Executors.newCachedThreadPool(new NameThreadFactory("worker",true));
        EventLoopGroup bossLoopGroup = new NioEventLoopGroup(1,bossService);
        EventLoopGroup workerLoogGroup = new NioEventLoopGroup(Math.min(Runtime.getRuntime().availableProcessors()+1,32),workerService);
        serverBootstrap.group(bossLoopGroup,workerLoogGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE,true)
                .localAddress(new InetSocketAddress("localhost",PORT))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline channelPipeline = socketChannel.pipeline();
                        channelPipeline.addLast("stringDecode",new StringDecoder());
                        channelPipeline.addLast("stringEncode",new StringEncoder());
                        channelPipeline.addLast("feather",new FeatherChannelInboundHandler());
                    }
                });
        try{
            ChannelFuture channelFuture = serverBootstrap.bind().sync().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if(channelFuture.isSuccess()){
                        System.out.println("Listening "+PORT+" Success");
                    }
                }
            });
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
            bossLoopGroup.shutdownGracefully();
            workerLoogGroup.shutdownGracefully();
        }
    }
}