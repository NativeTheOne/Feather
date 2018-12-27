package com.feather.zookeeper.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.ReferenceCountUtil;
import org.apache.curator.RetryPolicy;
import org.apache.curator.RetrySleeper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@ChannelHandler.Sharable
class FeatherChannelHandler extends ChannelInboundHandlerAdapter{

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf byteBuf = ctx.alloc().heapBuffer().writeBytes("TheOne Never Give Up".getBytes());
        ctx.writeAndFlush(byteBuf);
        ReferenceCountUtil.release(byteBuf);
    }

}

@ChannelHandler.Sharable
class FeatherChannelOutboundHandler extends ChannelOutboundHandlerAdapter{

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        System.out.println("Connection is the server ip:"+remoteAddress.toString());
        ctx.connect(remoteAddress, localAddress, promise);
    }
}

public class CuratorClient {

    private static List<String> portList = new ArrayList<>(2);

    private static int port;

    private static String epoch = UUID.randomUUID().toString();

    public static void main(String[] args) throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181")
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(5000)
                .retryPolicy(new RetryPolicy() {
                    @Override
                    public boolean allowRetry(int i, long l, RetrySleeper retrySleeper) {
                        if(i<4){
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
                }).build();
        client.start();
        List<String> childPaths = client.getChildren().forPath("/master-server");
        for(String childPath : childPaths){
            byte[] bs = client.getData().forPath("/master-server/"+childPath);
            portList.add(new String(bs));
        }
        PathChildrenCache cache = new PathChildrenCache(client,"/master-server",true);
        cache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent){
                System.out.println("----------------------"+pathChildrenCacheEvent.getType()+"--------------------------");
                if(pathChildrenCacheEvent.getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED){
                    try{
                        port = Integer.valueOf(portList.get(0));
                        epoch = UUID.randomUUID().toString();
                        portList.remove(0);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else if(pathChildrenCacheEvent.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED){
                    byte[] bs = pathChildrenCacheEvent.getData().getData();
                    portList.add(new String(bs));
                }
            }
        });
        cache.start();
        Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        port = Integer.valueOf(portList.get(0));
        portList.remove(0);
        System.out.println(portList);
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline cp = socketChannel.pipeline();
                        cp.addLast(new StringDecoder());
                        cp.addLast(new FeatherChannelHandler());
                        cp.addLast(new StringEncoder());
                    }
                });
        while(true){
            String localEpoch = epoch;
            try{
                ChannelFuture cf  = bootstrap.connect("localhost",port).sync().addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if(channelFuture.isSuccess()){
                            System.out.println("client open is success");
                        }
                    }
                });
                cf.channel().closeFuture().sync();
            }catch (Exception e){
                TimeUnit.SECONDS.sleep(5);
                while(true){
                    if(localEpoch != epoch){
                        break;
                    }
                }
                localEpoch = epoch;
                System.out.println("reconnection to the new server :"+port);
            }
        }
    }
}
