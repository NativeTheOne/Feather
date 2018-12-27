package com.feather.zookeeper.impl;

import com.feather.zookeeper.NettyServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import org.apache.curator.RetryPolicy;
import org.apache.curator.RetrySleeper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.zookeeper.CreateMode;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@ChannelHandler.Sharable
class FeatherChannelInBoundHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(msg);
    }
}

public class SimpleNettyServerImpl implements NettyServer {

    private static final int PORT = 7070;

    private static class NamedThreadFactory implements ThreadFactory{

        private String prefix;

        private boolean daemon;

        private final ThreadGroup threadGroup;

        private static final AtomicInteger counter = new AtomicInteger(0);

        public NamedThreadFactory(String prefix,boolean daemon){
            this.prefix = prefix;
            this.daemon = daemon;
            SecurityManager securityManager = System.getSecurityManager();
            this.threadGroup = (securityManager == null )?Thread.currentThread().getThreadGroup():securityManager.getThreadGroup();
        }

        @Override
        public Thread newThread(Runnable r) {
            String threadName = this.prefix+"-thread-"+counter.getAndIncrement();
            Thread thread = new Thread(this.threadGroup,r,threadName);
            return thread;
        }
    }

    @Override
    public void ServerOpen() throws Exception {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        ExecutorService bossExecutorService = Executors.newCachedThreadPool(new NamedThreadFactory("boss",false));
        ExecutorService workerExecutorService = Executors.newCachedThreadPool(new NamedThreadFactory("worker",false));
        EventLoopGroup bossEventLoopGroup = new NioEventLoopGroup(1,bossExecutorService);
        EventLoopGroup workerEventLoopGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors()*2,workerExecutorService);
        serverBootstrap.group(bossEventLoopGroup,workerEventLoopGroup)
                .localAddress("localhost",PORT)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline cp = socketChannel.pipeline();
                        cp.addLast("stringDecoder",new StringDecoder());
                        cp.addLast("feather",new FeatherChannelInBoundHandler());
                    }
                });
        ChannelFuture channelFuture = serverBootstrap.bind().sync().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if(channelFuture.isSuccess()){
                    ZookeeperRegister(); //监听成功，注册zk
                    System.out.println("listen the port:"+PORT);
                }
            }
        });
        channelFuture.channel().closeFuture().sync();
    }

    @Override
    public void ZookeeperRegister() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("localhost:2181")
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(5000)
                .retryPolicy(new RetryPolicy() {
                    @Override
                    public boolean allowRetry(int i, long l, RetrySleeper retrySleeper) {
                        if(i<3){
                            try {
                                retrySleeper.sleepFor(2, TimeUnit.SECONDS);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            return true;
                        }else{
                            return false;
                        }
                    }
                })
                .build();
        client.start();
        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).inBackground(new BackgroundCallback() {
            @Override
            public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
                System.out.println(curatorEvent.getResultCode());
            }
        }).forPath("/master-server/"+PORT,String.valueOf(PORT).getBytes());
    }

    public static void main(String[] args) throws Exception {
        SimpleNettyServerImpl simpleNettyServer = new SimpleNettyServerImpl();
        simpleNettyServer.ServerOpen(); //打开监听端口
    }
}
