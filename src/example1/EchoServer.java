package example1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class EchoServer {
    public static SelectorLoop connectionBell;
    public static SelectorLoop readBell;
    public boolean isReadBellRunning = false;

    public static void main(String[] args) throws IOException {
        new EchoServer().startServer();
    }

    // 启动Server
    public void startServer() throws  IOException{
        // 准备好一个Bell，当有连接进来时进行通知
        connectionBell = new SelectorLoop();
        // 当有read事件时通知
        readBell = new SelectorLoop();
        ServerSocketChannel socketChannel = ServerSocketChannel.open();
        socketChannel.configureBlocking(false);
        ServerSocket serverSocket = socketChannel.socket();
        serverSocket.bind(new InetSocketAddress("localhost",7878));
        // 这个bell只监听连接事件
        socketChannel.register(connectionBell.getSelector(),SelectionKey.OP_ACCEPT);
        new Thread(connectionBell).start();
    }
    // Selector轮询线程类
    public class SelectorLoop implements Runnable{

        private Selector selector;
        private ByteBuffer temp = ByteBuffer.allocate(1024);

        public SelectorLoop() throws IOException {
            this.selector = Selector.open();
        }

        public Selector getSelector() {
            return selector;
        }

        @Override
        public void run() {
            try {
                while (true){
                    this.selector.select();
                    Set<SelectionKey> selectionKeys = this.selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()){
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        // 处理事件可以使用多线程来处理
                        try {
                            this.dispatch(key);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void dispatch(SelectionKey key) throws IOException, InterruptedException {
            if (key.isAcceptable()){
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                SocketChannel channel = ssc.accept();
                channel.configureBlocking(false);
                // 对新的连接channel祖册read事件，使用readBell闹钟
                channel.register(readBell.getSelector(),SelectionKey.OP_READ);
                // 如果读取线程还没有启动，那就启动一个读取线程。
                synchronized (EchoServer.this){
                    if (!EchoServer.this.isReadBellRunning){
                        new Thread(readBell).start();
                        EchoServer.this.isReadBellRunning = true;
                    }
                }
            }else if (key.isReadable()){
                SocketChannel channel = (SocketChannel) key.channel();
                // 写数据到buffer
                int count = channel.read(temp);
                if (count < 0){
                    // 客户端已经断开连接
                    key.cancel();
                    channel.close();
                    return;
                }
                // 切换buffer到读的状态，内部指针归位
                temp.flip();
                String msg = Charset.forName("UTF-8").decode(temp).toString();
                System.out.println("Server received [ "+msg+" ] from client address:"+channel.getRemoteAddress());
                Thread.sleep(1000);
                channel.write(ByteBuffer.wrap(msg.getBytes(Charset.forName("UTF-8"))));
                // 清空buffer
                temp.clear();
            }
        }
    }
}
