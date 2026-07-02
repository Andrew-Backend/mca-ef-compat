package quilt.net.mca.client.tts;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import quilt.net.mca.MCA;
import quilt.net.mca.client.tts.sound.PCMAudioStream;

public class AudioCache {
   private static final int MIN_SIZE = 128;
   private static final String CACHE_DIR = "tts_cache/";
   public static Map<String, PCMAudioStream> inMemory = new ConcurrentHashMap<>();
   private static final MessageDigest MESSAGEDIGEST;

   private static void setInMemoryAudio(String identifier, ByteBuffer buffer) {
      if (inMemory.containsKey(identifier)) {
         inMemory.get(identifier).setBuffer(buffer);
      } else {
         inMemory.put(identifier, new PCMAudioStream(buffer));
      }
   }

   public static PCMAudioStream getPCMAudioStream(String identifier) {
      return inMemory.containsKey(identifier) ? inMemory.get(identifier) : new PCMAudioStream(readFromDisk(identifier));
   }

   public static boolean get(String identifier, Consumer<OutputStream> retriever, boolean persistent) {
      if (persistent) {
         return cachedRetrieve(identifier, retriever);
      }

      ByteBuffer byteBuffer = retrieve(retriever);
      if (byteBuffer == null) {
         return false;
      }

      setInMemoryAudio(identifier, byteBuffer);
      return true;
   }

   private static ByteBuffer retrieve(Consumer<OutputStream> retriever) {
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
         retriever.accept(baos);
         return ByteBuffer.wrap(baos.toByteArray());
      } catch (IOException e) {
         return null;
      }
   }

   public static ByteBuffer readFromDisk(String identifier) {
      File cacheFile = new File("tts_cache/", identifier);
      if (!isSane(cacheFile)) {
         return null;
      }

      try (FileInputStream fis = new FileInputStream(cacheFile)) {
         return ByteBuffer.wrap(fis.readAllBytes());
      } catch (IOException e) {
         MCA.LOGGER.error("Failed to retrieve cached audio file: {}", identifier, e);
         return null;
      }
   }

   public static boolean cachedRetrieve(String identifier, Consumer<OutputStream> retriever) {
      try {
         File cacheFile = new File("tts_cache/", identifier);
         if (isSane(cacheFile)) {
            return true;
         }

         cacheFile.getParentFile().mkdirs();

         try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
            retriever.accept(fos);
         }

         return isSane(cacheFile);
      } catch (IOException e) {
         MCA.LOGGER.error("Failed to cache audio file: {}", identifier, e);
         return false;
      }
   }

   private static boolean isSane(File cacheFile) {
      return cacheFile.exists() && cacheFile.length() > 128L;
   }

   public static String getHash(String text) {
      MESSAGEDIGEST.update(text.getBytes());
      return toHex(MESSAGEDIGEST.digest()).toLowerCase(Locale.ROOT);
   }

   public static String toHex(byte[] bytes) {
      BigInteger bi = new BigInteger(1, bytes);
      return String.format(Locale.ROOT, "%0" + (bytes.length << 1) + "X", bi);
   }

   static {
      try {
         MESSAGEDIGEST = MessageDigest.getInstance("SHA-256");
      } catch (NoSuchAlgorithmException e) {
         throw new RuntimeException(e);
      }
   }
}
