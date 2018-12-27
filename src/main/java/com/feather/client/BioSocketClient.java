package com.feather.client;

import java.io.*;
import java.net.Socket;

public class BioSocketClient {
    private final static int PORT = 9600;

    private final static String message = "Feather Never Give Up";

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("127.0.0.1",PORT);
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.write(message.getBytes());
        out.flush();
        out.close();
        DataInputStream in = new DataInputStream(socket.getInputStream());
        byte[] b = new byte[1024];
        in.read(b);
        System.out.println(new String(b));
    }
}
