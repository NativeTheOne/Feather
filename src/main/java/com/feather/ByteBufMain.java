package com.feather;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;

import java.nio.charset.Charset;

public class ByteBufMain {
    public static void main(String[] args){
        ByteBuf byteBuf = Unpooled.directBuffer();
        System.out.println(byteBuf.isReadable());
        byteBuf.writeBytes("feather never give up".getBytes());
//        for(int i=0;i<byteBuf.capacity();i++){
//            System.out.print((char)byteBuf.getByte(i));
//        }
        for(int i=0;i<byteBuf.writerIndex()-5;i++){
            System.out.print((char)byteBuf.readByte());
        }
        System.out.println(byteBuf.writerIndex());
        System.out.println(byteBuf.readerIndex());
        byteBuf.discardReadBytes();
        System.out.println(byteBuf.writerIndex());
        System.out.println(byteBuf.readerIndex());
        byteBuf.skipBytes(2);
        System.out.println(byteBuf.isReadable());
        System.out.println(byteBuf.isWritable(1));
        ByteBuf sliceByteBuf = byteBuf.slice(0,12).asReadOnly();
        sliceByteBuf.writeBytes("test".getBytes());
        System.out.println(sliceByteBuf.toString());
        System.out.println(byteBuf.toString());
        ByteBuf buffer = new PooledByteBufAllocator(true).buffer();
    }
}
