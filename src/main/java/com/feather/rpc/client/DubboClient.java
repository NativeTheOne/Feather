package com.feather.rpc.client;

import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.service.EchoService;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.feather.rpc.server.EchoServer;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DubboClient {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("consumer.xml");
        context.start();
        EchoServer echoServer = (EchoServer) context.getBean("echoServer");
        echoServer.echo();
        Future<List<String>> future = RpcContext.getContext().getFuture();
        System.out.println(future.get());
    }
}
