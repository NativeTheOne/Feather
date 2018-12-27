package com.feather.zookeeper.client;

import org.apache.curator.RetryPolicy;
import org.apache.curator.RetrySleeper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorEventType;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.TimeUnit;

public class CuratorTest {

    private static CuratorFramework zookeeperClient = CuratorFrameworkFactory.newClient("127.0.0.1:2181", 3000, 4000, new RetryPolicy() {
        @Override
        public boolean allowRetry(int i, long l, RetrySleeper retrySleeper) {
            if(i<4){
                try {
                    retrySleeper.sleepFor(1000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            }
            return false;
        }
    });

    public static void main(String[] args) throws Exception {
        zookeeperClient.start();
        zookeeperClient.usingNamespace("tricker");
        Stat stat = new Stat();
        zookeeperClient.create().creatingParentsIfNeeded().storingStatIn(stat).inBackground(new BackgroundCallback() {
            @Override
            public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
                if(curatorEvent.getType() == CuratorEventType.CREATE){
                    System.out.println("create node for zookeeper");
                }
            }
        }).forPath("/tricker/container/123","TheOne".getBytes());
        TimeUnit.SECONDS.sleep(5);
        System.out.println("master");
    }
}
