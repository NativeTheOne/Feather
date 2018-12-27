package com.feather.client;

import com.feather.Person;
import com.google.gson.Gson;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Random;

class FeatherClientChannelInboundHandler extends ChannelInboundHandlerAdapter{

    private Gson gson = new Gson();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Person person = new Person("theone",24,"Feather@163.com");
        String msg = gson.toJson(person);
        ByteBuf byteBuf = ctx.alloc().heapBuffer().writeBytes(msg.getBytes());
        ctx.writeAndFlush(byteBuf);
        ReferenceCountUtil.release(byteBuf);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(cause.getCause());
    }

}

public class NettySocketClient{
    private static final int PORT = 7070;

    private static final String HOST_NAME = "localhost";

    public static void main(String[] args){
        Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(1);
        bootstrap.localAddress(new InetSocketAddress("localhost",new Random(53298).nextInt(65535)))
                .group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addFirst("stringDecoder",new StringDecoder()).addLast("feather",new FeatherClientChannelInboundHandler()).addLast("stringEncoder",new StringEncoder());
                    }
                });
        try {
            ChannelFuture future = bootstrap.connect(HOST_NAME,PORT).sync();
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if(channelFuture.isSuccess()){
                        System.out.println("Start connection to server");
                    }
                }
            });
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            eventLoopGroup.shutdownGracefully();
        }
    }
}