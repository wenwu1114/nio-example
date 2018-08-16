import java.nio.IntBuffer;

/**
 *
 */
public class TestIntBuffer {
    public static void main(String[] args) {
        IntBuffer buffer = IntBuffer.allocate(8);
        for (int i = 0; i < buffer.capacity(); i++) {
            int j = 2*(i+1);
            buffer.put(j);
        }

        // 重设buffer，将limit设置为position，position设置为0
        buffer.flip();

        while (buffer.hasRemaining()){
            int j = buffer.get();
            System.out.println(j+" ");
        }
    }
}
