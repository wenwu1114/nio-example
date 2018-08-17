package example1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class EchoClient implements Runnable {

    // 空闲计数器，如果空闲超过10次，将检测server是否终端连接
    private static int idleCounter = 0;
    private Selector selector;
    private SocketChannel socketChannel;
    private ByteBuffer temp = ByteBuffer.allocate(1024);

    public static void main(String[] args) throws IOException {
        EchoClient client = new EchoClient();
        new Thread(client).start();
    }

    public EchoClient() throws IOException {
        this.selector = Selector.open();
         socketChannel = SocketChannel.open();
        // 如果快速的建立了连接，返回true，如果没有建立连接，返回false,并在连接后发出connect事件
        boolean isConnected = socketChannel.connect(new InetSocketAddress("localhost", 7878));
        socketChannel.configureBlocking(false);
        SelectionKey key = socketChannel.register(this.selector, SelectionKey.OP_CONNECT);
        if (isConnected){
            this.sendFirstMsg();
        }else {
            // 如果连接还在尝试中，则注册connect事件的监听
            key.interestOps(SelectionKey.OP_CONNECT);
        }
    }

    public void sendFirstMsg() throws IOException {
        String msg = "Hello NIO";
        socketChannel.write(ByteBuffer.wrap(msg.getBytes(Charset.forName("UTF-8"))));
    }

    @Override
    public void run() {
        while (true){
            try {
                // 阻塞，等待事件发生，或者1ms超时
                final int num = this.selector.select();
                if (num == 0){
                    idleCounter++;
                    if (idleCounter > 10){
                        // 超时的发送消息失败，会抛异常
                        try{
                            sendFirstMsg();
                        }catch (IOException e){
                            e.printStackTrace();
                            socketChannel.close();
                            return;
                        }
                    }
                    continue;
                }else {
                    idleCounter = 0;
                }
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()){
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()){
                        SocketChannel channel = (SocketChannel) key.channel();
                        if (channel.isConnectionPending()){
                            channel.finishConnect();
                        }
                        sendFirstMsg();
                    }
                    if (key.isReadable()){
                        SocketChannel channel = (SocketChannel) key.channel();
                         temp = ByteBuffer.allocate(1024);
                        int count = channel.read(temp);
                        if (count < 0){
                            channel.close();
                            continue;
                        }
                        // 切换buffer到read状态，内部指针归位
                        temp.flip();
                        String msg = Charset.forName("UTF-8").decode(temp).toString();
                        System.out.println("Client received ["+msg+"] from server address:" + channel.getRemoteAddress());
                            Thread.sleep(1000);
                            socketChannel.write(ByteBuffer.wrap(msg.getBytes(Charset.forName("UTF-8"))));
                            temp.clear();
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }
}
