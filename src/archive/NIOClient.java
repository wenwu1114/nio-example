package archive;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * NIO客户端
 */
public class NIOClient {
    private Selector selector;

    public void initClient(String ip,int port)throws IOException{
        selector = Selector.open();
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(ip,port));
        socketChannel.register(selector,SelectionKey.OP_CONNECT);
    }


    /**
     * 循环监听selector上是否有需要处理的事件，如果有，则进行处理
     * @throws IOException
     */
    public void listen() throws IOException{
        while (true){
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()){
                SelectionKey key = iterator.next();
                iterator.remove();
                if ((key.readyOps()& SelectionKey.OP_CONNECT) == SelectionKey.OP_CONNECT){
                    SocketChannel channel = (SocketChannel) key.channel();
                    if (channel.isConnectionPending()){
                        channel.finishConnect();
                    }
                    channel.configureBlocking(false);
                    channel.register(this.selector,SelectionKey.OP_WRITE);
                }else if ((key.readyOps()&SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE){
                    SocketChannel channel = (SocketChannel) key.channel();
                    String msg = new String("Hello,Nice to meet you Server!");
                    channel.write(ByteBuffer.wrap(msg.getBytes()));
                    // 注册读取事件，以便接收服务端发来的消息
                    channel.register(this.selector,SelectionKey.OP_READ);
                    System.out.println("客户端发出消息："+msg);
                }else if ((key.readyOps()&SelectionKey.OP_READ) == SelectionKey.OP_READ){
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer echoBuffer = ByteBuffer.allocate(1024);
                    while (true){
                        echoBuffer.clear();
                        int read = channel.read(echoBuffer);
                        if (read <= 0){
                            break;
                        }
                    }
                    byte[] data = echoBuffer.array();
                    System.out.println("客户端收到的消息："+new String(data).trim());
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        NIOClient client = new NIOClient();
        client.initClient("localhost",8000);
        client.listen();
    }
}
