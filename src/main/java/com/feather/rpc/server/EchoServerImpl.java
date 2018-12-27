package com.feather.rpc.server;

import java.util.Arrays;
import java.util.List;

public class EchoServerImpl implements EchoServer{
    @Override
    public List<String> echo() {
        return Arrays.asList(new String[]{"Hello World"});
    }
}
