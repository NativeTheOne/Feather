package com.feather.zookeeper.client;

import com.feather.Person;
import com.google.gson.Gson;
import org.apache.catalina.startup.Tomcat;
import org.apache.curator.RetryPolicy;
import org.apache.curator.RetrySleeper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.zookeeper.CreateMode;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ZookeeperEmbedTomcatConfiugration {

    private static Gson gson = new Gson();

    private static CuratorFramework zookeeperClient;

    private static String listenPath = "/configuration";

    private static NodeCache nodeCache;

    static{
        zookeeperClient = CuratorFrameworkFactory.newClient("127.0.0.1:2181", 3000, 2000, new RetryPolicy() {
            @Override
            public boolean allowRetry(int i, long l, RetrySleeper retrySleeper) {
                if(i < 4){
                    try {
                        retrySleeper.sleepFor(1000, TimeUnit.MILLISECONDS);
                        return true;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
    }

    /*
    判断节点是新增、修改还是删除应该通过PathChildrenCache来处理，PathChildrenCacheListener有event参数，
    NodeCache和NodeCacheListener是监听节点内容变更的，直接通过cn.getCurrentData().data()可以获得节点最新内容
     */
    public static void main(String[] args) throws Exception {
        zookeeperClient.start();
        nodeCache = new NodeCache(zookeeperClient,listenPath);
        Person person = new Person("theone",23,"theone");
        String personJson = gson.toJson(person);
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                Person person = gson.fromJson(new String(nodeCache.getCurrentData().getData()),Person.class);
                System.out.println("New Data:"+person);
            }
        });
        nodeCache.start();
        zookeeperClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(listenPath,personJson.getBytes());
        TimeUnit.SECONDS.sleep(Integer.MAX_VALUE);
    }

}
