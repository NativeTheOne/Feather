package com.feather.zookeeper.client;

import com.google.gson.Gson;
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

    private static class TomcatConfiguration{

        private int port;

        private String tempDir;

        private String appBase;

        private String webAppContextPath;

        private String webAppDocBase;

        public TomcatConfiguration(int port, String tempDir, String appBase, String webAppContextPath, String webAppDocBase) {
            this.port = port;
            this.tempDir = tempDir;
            this.appBase = appBase;
            this.webAppContextPath = webAppContextPath;
            this.webAppDocBase = webAppDocBase;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getTempDir() {
            return tempDir;
        }

        public void setTempDir(String tempDir) {
            this.tempDir = tempDir;
        }

        public String getAppBase() {
            return appBase;
        }

        public void setAppBase(String appBase) {
            this.appBase = appBase;
        }

        public String getWebAppContextPath() {
            return webAppContextPath;
        }

        public void setWebAppContextPath(String webAppContextPath) {
            this.webAppContextPath = webAppContextPath;
        }

        public String getWebAppDocBase() {
            return webAppDocBase;
        }

        public void setWebAppDocBase(String webAppDocBase) {
            this.webAppDocBase = webAppDocBase;
        }
    }

    private static CuratorFramework zookeeperClient;

    private static String listenPath = "/tomcat/configuration";

    private static int port = 8080;

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
        TomcatConfiguration configuration = new TomcatConfiguration(port, File.createTempFile("tomcat.","."+port).getAbsolutePath(),".","",".");
        String configurationJson = gson.toJson(configuration);
        zookeeperClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(listenPath,configurationJson.getBytes());
        NodeCache nodeCache = new NodeCache(zookeeperClient,listenPath);
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                byte[] bs = nodeCache.getCurrentData().getData();
                String newConfiguration = new String(bs);
                TomcatConfiguration newTomcatConfig = gson.fromJson(newConfiguration,TomcatConfiguration.class);
            }
        });
    }

}
