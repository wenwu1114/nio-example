import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 测试flip()方法
 */
public class TestBufferField {
    public static void main(String[] args) throws Exception {
        FileInputStream fileInputStream = new FileInputStream("D:\\1.txt");
        FileChannel fcin = fileInputStream.getChannel();
        FileOutputStream fileOutputStream = new FileOutputStream("D:\\2.txt");
        FileChannel fcout = fileOutputStream.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(10);
        output("初始化",buffer);

        // 读数据
        fcin.read(buffer);
        output("调用read()",buffer);

        // 重设buffer,limit置为position，然后position置为0
        buffer.flip();
        output("调用flip()",buffer);

        // 将缓冲区的数据写入fcout
        fcout.write(buffer);

        // 重设buffer
        buffer.clear();
        output("调用clear()",buffer);

        fileInputStream.close();
        fileOutputStream.close();
    }


    public static void output(String step,Buffer buffer){
        System.out.println(step+" : ");
        System.out.print("position: "+buffer.position()+", ");
        System.out.print("limit: "+buffer.limit()+", ");
        System.out.println("capacity: "+buffer.capacity());
        System.out.println();
    }
}
