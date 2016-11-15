package com.everseeker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by everseeker on 2016/11/14.
 */
public class Server {
    private static final int port = 9999;
    private static ServerSocketChannel serverSocketChannel;
    private static Selector selector;

    /**
     * 单例模式
     */
    private static class ServerHolder {
        private static Server INSTANCE = new Server();
    }
    private static Server getInstance() {
        return ServerHolder.INSTANCE;
    }

    public Server() {
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();   //建立ServerSocketChannel
            serverSocketChannel.socket().bind(new InetSocketAddress(port));     //创建Selector
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 单独一个线程专门负责监听，之后注册到selector
     */
    public void listen() {
        //监听新的channel连接
        System.out.println("Ready for listening on port " + port);
        while (true) {
            try {
                SocketChannel inChannel = serverSocketChannel.accept();
                if (inChannel != null) {
                    //向selector注册通道
                    inChannel.configureBlocking(false);
                    inChannel.register(selector, SelectionKey.OP_READ);     //返回SelectionKey类型
                }
            } catch (ClosedChannelException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 另外一个线程负责处理接收到的数据
     */
    public void start() {
        while (true) {
            try {
                int readyChannels = selector.select(100);
                if (readyChannels == 0)
                    continue;
                System.out.println("\ncurrent all channels num: " + readyChannels);
                Set seletedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIter = seletedKeys.iterator();
                while (keyIter.hasNext()) {
                    SelectionKey key = keyIter.next();
                    if (key.isReadable()) {
                        SocketChannel inChannel = (SocketChannel)key.channel();
                        //分配buffer
                        ByteBuffer buf = ByteBuffer.allocate(16);
                        int bytesRead = inChannel.read(buf);
                        while (bytesRead != -1) {
                            if (bytesRead > 0)
                                System.out.print("\nRead " + bytesRead + ":  \t\t");
                            buf.flip();     //切换buffer为读模式
                            while (buf.hasRemaining()) {
                                System.out.print((char)buf.get());
                            }
                            buf.clear();    //清空buf，以用于接收新的数据; 注：所谓的清空只是重置了position, limit的值，并非删除buf中的数据
                            bytesRead = inChannel.read(buf);
                        }
                    }
                    keyIter.remove();
                    key.cancel();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        final Server server = Server.getInstance();
        new Thread(new Runnable() {
            public void run() {
                server.listen();
            }
        }).start();
        new Thread(new Runnable() {
            public void run() {
                server.start();
            }
        }).start();
    }
}
