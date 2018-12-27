package com.feather.server;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class TestServerSocketChannel {

    public static void main(String[] args) {

        try {

            //新建一个selector对象
            Selector selector = Selector.open();

            //新建一个ServerSocketChannel对象，并且绑定地址
            ServerSocketChannel ssc = ServerSocketChannel.open();

            ssc.configureBlocking(false);

            ServerSocket ss = ssc.socket();

            InetSocketAddress inet = new InetSocketAddress("127.0.0.1", 8888);

            ss.bind(inet);

            //向selector对象中注册这个serverSocketChannel
            SelectionKey key = ssc.register(selector, SelectionKey.OP_ACCEPT);

            //处理连接事件
            while (true) {
                int num = selector.select();

                Set keys = selector.selectedKeys();

                Iterator it = keys.iterator();

                while (it.hasNext()) {

                    SelectionKey sk = (SelectionKey) it.next();

                    if ((sk.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {

                        ServerSocketChannel ssc1 = (ServerSocketChannel) sk
                                .channel();
                        // 有访问进来
                        SocketChannel sc = ssc1.accept();

                        sc.configureBlocking(false);

                        sc.register(selector, SelectionKey.OP_READ);

                        it.remove();

                    }

                    else if ((sk.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {

                        SocketChannel sc1 = (SocketChannel) sk.channel();

                        ByteBuffer buffer = ByteBuffer.allocate(10);

                        StringBuffer sb = new StringBuffer();

                        while (true) {

                            buffer.clear();
                            int n = sc1.read(buffer);
                            if (n == -1) {
                                break;
                            }
                            buffer.flip();

                            int limit = buffer.limit();

                            char[] dst=new char[limit];

                            for(int i=0;i<limit;i++)
                            {
                                dst[i]=(char)buffer.get(i);
                            }
                            sb.append(dst);

                        }
                        //一定要添加该代码，不关闭的情况下是不会移除这个Selectorkey对象的
                        sc1.close();

                        System.out.println(sb.toString());

                        it.remove();
                    }
                }

            }

        } catch (Exception e) {

        }

    }

}
