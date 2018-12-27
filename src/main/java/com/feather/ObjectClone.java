package com.feather;

import io.netty.buffer.ByteBufUtil;

public class ObjectClone {

    public static void main(String[] args) throws CloneNotSupportedException {
        String code = "546865204f6e652057696c6c20446573746f72792045766572797468696e67";
        byte[] bs = ByteBufUtil.decodeHexDump(code);
        System.out.println(new String(bs));

    }

}
