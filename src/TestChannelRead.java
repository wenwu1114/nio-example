import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 使用nio读取数据
 */
public class TestChannelRead {

    public static void main(String[] args) throws Exception {
        FileInputStream fileInputStream = new FileInputStream("D:\\1.txt");
        FileChannel fileChannel = fileInputStream.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        fileChannel.read(buffer);
        buffer.flip();
        while (buffer.hasRemaining()){
            byte b = buffer.get();
            System.out.print((char)b);
        }
        fileInputStream.close();
    }
}
