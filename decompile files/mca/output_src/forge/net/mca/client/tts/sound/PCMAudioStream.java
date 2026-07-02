package forge.net.mca.client.tts.sound;

import java.io.IOException;
import java.nio.ByteBuffer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import net.minecraft.client.sounds.AudioStream;
import org.lwjgl.BufferUtils;

public class PCMAudioStream implements AudioStream {
   private static final AudioFormat FORMAT = new AudioFormat(Encoding.PCM_SIGNED, 22050.0F, 16, 1, 2, 22050.0F, false);
   private ByteBuffer buffer;

   public PCMAudioStream(ByteBuffer buffer) {
      this.buffer = buffer;
   }

   public AudioFormat m_6206_() {
      return FORMAT;
   }

   public void setBuffer(ByteBuffer buffer) {
      this.buffer = buffer;
   }

   public ByteBuffer m_7118_(int size) {
      if (this.buffer == null) {
         return null;
      }

      int remaining = this.buffer.remaining();
      if (remaining <= 0) {
         return null;
      }

      int bytesToRead = Math.min(size, remaining);
      ByteBuffer result = this.buffer.slice();
      result.limit(bytesToRead);
      this.buffer.position(this.buffer.position() + bytesToRead);
      ByteBuffer byteBuffer = BufferUtils.createByteBuffer(size);
      byteBuffer.put(result);
      byteBuffer.flip();
      return byteBuffer;
   }

   public void close() throws IOException {
   }
}
