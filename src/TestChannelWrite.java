import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * NIO向文件写入数据
 */
public class TestChannelWrite {
    private static byte message[] = {84,56,59,127,5,15,15,15};
    public static void main(String[] args) throws Exception {
        FileOutputStream fileOutputStream = new FileOutputStream("D://1.txt");
        FileChannel fileChannel = fileOutputStream.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        for (int i = 0; i < message.length; i++) {
            buffer.put(message[i]);
        }
        buffer.flip();
        fileChannel.write(buffer);
        fileOutputStream.close();
    }
}
