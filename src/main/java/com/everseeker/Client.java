package com.everseeker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by everseeker on 2016/11/14.
 */
public class Client {
    private String server_ip;
    private int port;

    public Client(String server_ip, int port) {
        this.server_ip = server_ip;
        this.port = port;
    }

    public void start() throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(server_ip, port));

        //写入数据到缓存
        String data = "New String to write to channel, 通道, ..." + Thread.currentThread().getName();
        ByteBuffer buf = ByteBuffer.allocate(16);
        buf.clear();
        byte[] dataByte = data.getBytes();
        int toPut = 0, len = dataByte.length;
        while (toPut < len) {
            int step = (len - toPut) < 16 ? (len - toPut) : 16;
            buf.put(dataByte, toPut, step);
            toPut += step;
            buf.flip();
            //发送缓存到channel
            while (buf.hasRemaining()) {
                //write方法无法保证能写入多少字节
                socketChannel.write(buf);
            }
            buf.clear();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        socketChannel.close();
    }

    public static void main(String[] args) throws InterruptedException {
        new MyThread().start();
        new MyThread().start();
        Thread.sleep(2000);
        new MyThread().start();
        new MyThread().start();
    }
}

class MyThread extends Thread {
    @Override
    public void run() {
        try {
            new Client("127.0.0.1", 9999).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}