package archive;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * NIO服务端
 */
public class NIOServer {
    private Selector selector;
    private ByteBuffer echoBufer = ByteBuffer.allocate(1024);

    public void initServer (int port) throws IOException{
        selector = Selector.open();
        // 获取一个serverSocket通道，并设置为非阻塞
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);

        // 将通道对应的ServerSocket绑定到port端口
        ServerSocket serverSocket = serverSocketChannel.socket();
        InetSocketAddress address = new InetSocketAddress(port);
        serverSocket.bind(address);

        // 将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_ACCEPT事件
        serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);
    }

    /**
     * 循环监听Selector上是否需要处理的事件，如果有，则进行处理
     */
    public void listen() throws IOException{
        System.out.println("服务端启动成功！");

        // 循环访问selector
        while (true){
            selector.select();

            // 返回发生了事件的SelectionKey对象的一个 集合
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> it = selectedKeys.iterator();
            while (it.hasNext()){
                SelectionKey key = it.next();
                // 删除已选择的key 防止重复注册
                it.remove();

                // 客户端请求连接事件
                if ((key.readyOps()&SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT){
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    // 获取和客户端的连接通道
                    SocketChannel channel = server.accept();

                    channel.configureBlocking(false);
                    // 连接成功后，为了可以接收到客户端的信息，需要给通道注册读事件
                    channel.register(this.selector,SelectionKey.OP_READ);
                }else if ((key.readyOps()&SelectionKey.OP_READ) == SelectionKey.OP_READ){
                    // 获取可读事件发生的socket通道
                    SocketChannel channel = (SocketChannel) key.channel();

                    while (true){
                        echoBufer.clear();
                        int r = channel.read(echoBufer);
                        if (r<=0){
                            break;
                        }
                        echoBufer.flip();
                        channel.write(echoBufer);
                    }
                    byte[] data = echoBufer.array();
                    System.out.println("服务器收到并返回信息：" + new String(data).trim());
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        NIOServer server = new NIOServer();
        server.initServer(8000);
        server.listen();
    }
}
