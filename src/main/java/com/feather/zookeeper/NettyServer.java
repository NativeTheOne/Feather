package com.feather.zookeeper;

public interface NettyServer {

    public void ServerOpen() throws Exception;

    public void ZookeeperRegister() throws Exception;
}
