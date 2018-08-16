import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * MIO整个过程的说明
 *
 * 1.向Selector对象注册感兴趣的事件
 * 2.从Selector中获取感兴趣的事件
 * 3.根据不同的事件惊醒相应的处理
 */
public class NIOExample {
    private int port;

    /**
     * 注册事件
     */
    protected Selector getSelector() throws IOException{
        // 创建Selector对象
        Selector selector = Selector.open();

        // 创建可选择通道，并配置为非阻塞模式
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        // 绑定到指定的端口
        ServerSocket socket = serverChannel.socket();
        InetSocketAddress address = new InetSocketAddress(port);
        socket.bind(address);

        // 向Selector中注册感兴趣事件
        serverChannel.register(selector,SelectionKey.OP_ACCEPT);
        return selector;
    }

    /**
     * 开始监听
     * @param selector
     */
    public void listen(Selector selector){
        System.out.println("listen on "+port);
        try {
            while (true){
                // 该调用会一直阻塞，直到至少有一个事件发生
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()){
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    process(selector,key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据不同的事件做处理
     * @param selector
     * @param key
     */
    protected void process(Selector selector,SelectionKey key) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        // 接收请求
        if (key.isAcceptable()){
            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            SocketChannel channel = server.accept();
            channel.configureBlocking(false);
            channel.register(selector,SelectionKey.OP_READ);
        }

        // 读信息
        else if (key.isReadable()){
            SocketChannel channel = (SocketChannel) key.channel();

            int count = channel.read(buffer);
            if (count>0){
                buffer.flip();
                CharBuffer charBuffer = buffer.asCharBuffer();
                String name = charBuffer.toString();
                SelectionKey sKey = channel.register(selector,SelectionKey.OP_READ);
                sKey.attach(name);
            }else {
                channel.close();
            }
            buffer.clear();
        }

        // 写事件
        else if (key.isWritable()){
            SocketChannel channel = (SocketChannel) key.channel();
            String name = (String) key.attachment();
            String s = "Hello "+name;
            buffer = ByteBuffer.wrap(s.getBytes("UTF-8"));
            if (buffer != null){
                channel.write(buffer);
            }else {
                channel.close();
            }
        }
    }
}
