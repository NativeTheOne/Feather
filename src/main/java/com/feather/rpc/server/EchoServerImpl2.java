package com.feather.rpc.server;

import java.util.Arrays;
import java.util.List;

public class EchoServerImpl2 implements EchoServer{

    @Override
    public List<String> echo() {
        return Arrays.asList(new String[]{"The One Never Give Up"});
    }
}
