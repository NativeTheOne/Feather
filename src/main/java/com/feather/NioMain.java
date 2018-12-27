package com.feather;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;

public class NioMain {
    public static  void main(String[] args) throws IOException, IllegalAccessException, InterruptedException {
        byte[] b = new byte[1024];
        FileInputStream fileInputStream = new FileInputStream("E:\\vim.txt");
        fileInputStream.read(b);
        System.out.println(new String(b));
        System.out.println("--------------------------------------------------------------------");
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream("E:\\vim.txt"),1024);
        Field[] fields = bufferedInputStream.getClass().getDeclaredFields();
        for(int i=0;i<fields.length;i++){
            fields[i].setAccessible(true);
            if(fields[i].get(bufferedInputStream) instanceof byte[] == true){
                int size = 0;
                for(int j=0;j<((byte[])fields[i].get(bufferedInputStream)).length;j++){
                    if(((byte[])fields[i].get(bufferedInputStream))[j] != 0){
                        size++;
                    }
                }
                System.out.println(size);
                if(size != 0){
                    break;
                }
            }

        }
        System.out.println("--------------------------------------------------------------------");
        b = new byte[100];
        bufferedInputStream.read(b);
        System.out.println(new String(b));
        System.out.println("--------------------------------------------------------------------");
        for(int i=0;i<fields.length;i++){
            fields[i].setAccessible(true);
            if(fields[i].get(bufferedInputStream) instanceof byte[] == true){
                int size = 0;
                for(int j=0;j<((byte[])fields[i].get(bufferedInputStream)).length;j++){
                    if(((byte[])fields[i].get(bufferedInputStream))[j] != 0){
                        size++;
                    }
                }
                System.out.println(size);
                if(size != 0){
                    break;
                }
            }
        }
        System.out.println("--------------------------------------------------------------------");
        fileInputStream = new FileInputStream("E:\\vim.txt");
        FileChannel fileChannel = fileInputStream.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        int flow = 0;
        int time = 0;
        while(true){
            time = fileChannel.read(byteBuffer);
            byte[] bytes = byteBuffer.array();
            byteBuffer.flip();
            if(time != -1){
                System.out.println(new String(bytes,0,bytes.length));
            }
            byteBuffer.clear();
            System.out.println(flow++);
            TimeUnit.SECONDS.sleep(1);
        }
    }
}
