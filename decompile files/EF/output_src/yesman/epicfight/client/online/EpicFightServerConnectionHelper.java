package yesman.epicfight.client.online;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.function.BiConsumer;
import javax.net.ssl.SSLContext;
import net.minecraft.Util;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.main.EpicFightMod;

public class EpicFightServerConnectionHelper {
   public static HttpClient HTTP_CLIENT;
   private static final String LIB_FILE = "ServerCommunicationHelper";
   private static boolean SUPPORTED;

   public static boolean supported() {
      return SUPPORTED;
   }

   public static boolean init(String configPath) {
      EpicFightMod.LOGGER.info("Epic Fight web server connection helper: Initialize");
      EpicFightServerConnectionHelper.SupportedOS os = EpicFightServerConnectionHelper.SupportedOS.getOS();
      if (os == null) {
         EpicFightMod.LOGGER.error("Unsupported OS type {} for dynamic library", Util.m_137581_());
         return false;
      }

      boolean supported = false;

      try {
         SSLContext ssl = SSLContext.getInstance("TLSv1.3");
         ssl.init(null, null, null);
         HTTP_CLIENT = HttpClient.newBuilder().sslContext(ssl).connectTimeout(Duration.ofMillis(60000L)).build();
      } catch (NoSuchAlgorithmException e) {
         EpicFightMod.LOGGER.warn("TLS 1.3 not found, we do not support TLS communication lower than 1.3");
         HTTP_CLIENT = null;
         SUPPORTED = false;
         return false;
      } catch (KeyManagementException e) {
         EpicFightMod.LOGGER.warn("Failed at initializing SSL context");
         HTTP_CLIENT = null;
         SUPPORTED = false;
         return false;
      } catch (Exception e) {
         EpicFightMod.LOGGER.warn("Failed at initializing " + e);
         HTTP_CLIENT = null;
         SUPPORTED = false;
         return false;
      }

      String libpath = MessageFormat.format("/assets/epicfight/nativelib/{0}/{1}{2}", os.telemetryName(), "ServerCommunicationHelper", os.libExtension());
      InputStream inputstream = EpicFightMod.class.getResourceAsStream(libpath);
      if (inputstream != null) {
         File configNativeFile = new File(configPath + "/epicfight/native/ServerCommunicationHelper" + os.libExtension());
         byte[] resourceBytes = null;
         boolean shouldCreate;
         if (configNativeFile.exists()) {
            try {
               String configFileSHA256 = ParseUtil.getBytesSHA256Hash(new FileInputStream(configNativeFile).readAllBytes());
               resourceBytes = inputstream.readAllBytes();
               String resourceFileSHA256 = ParseUtil.getBytesSHA256Hash(resourceBytes);
               shouldCreate = !configFileSHA256.equals(resourceFileSHA256);
            } catch (IOException e) {
               e.printStackTrace();
               shouldCreate = true;
            }
         } else {
            shouldCreate = true;
         }

         if (shouldCreate) {
            try {
               EpicFightMod.LOGGER.info("Created temporary lib file at: " + configNativeFile.getPath());
               configNativeFile.delete();
               if (!configNativeFile.getParentFile().isDirectory()) {
                  configNativeFile.getParentFile().mkdirs();
               }

               configNativeFile.createNewFile();
               FileOutputStream fos = new FileOutputStream(configNativeFile);
               if (resourceBytes == null) {
                  resourceBytes = inputstream.readAllBytes();
               }

               fos.write(resourceBytes, 0, resourceBytes.length);
               fos.flush();
               fos.close();
            } catch (IOException e) {
               e.printStackTrace();
               EpicFightMod.LOGGER.info("Can't read library file: " + e);
            }
         }

         boolean exceptionOccurred = false;

         try {
            System.load(configNativeFile.toString());
         } catch (UnsatisfiedLinkError e) {
            exceptionOccurred = true;
            EpicFightMod.LOGGER.warn("Failed at loading library file");
         }

         supported = !exceptionOccurred;
      } else {
         supported = false;
         EpicFightMod.LOGGER.info("Can't read library file: " + libpath);
      }

      SUPPORTED = supported;
      return supported;
   }

   public static native void autoLogin(String var0, String var1, String var2, String var3, String var4, BiConsumer<HttpResponse<String>, Exception> var5);

   public static native void signIn(String var0, String var1, String var2, BiConsumer<HttpResponse<String>, Exception> var3);

   public static native void signOut(String var0, String var1, String var2, String var3, String var4, BiConsumer<HttpResponse<String>, Exception> var5);

   public static native void getAvailableCosmetics(
      String var0, String var1, String var2, String var3, String var4, BiConsumer<HttpResponse<String>, Exception> var5
   );

   public static native void saveConfiguration(String var0, String var1, BiConsumer<HttpResponse<String>, Exception> var2);

   public static native void getPlayerSkinInfo(String var0, String var1, BiConsumer<HttpResponse<String>, Exception> var2);

   public static native void loadRemoteMesh(String var0, String var1, BiConsumer<Mesh, Exception> var2);

   private enum SupportedOS {
      LINUX("linux", ".so"),
      WINDOWS("windows", ".dll"),
      OSX("osx", ".dylib"),
      MAC("mac", ".dylib");

      private final String telemetryName;
      private final String libExtension;

      public static EpicFightServerConnectionHelper.SupportedOS getOS() {
         try {
            return valueOf(Util.m_137581_().name());
         } catch (IllegalArgumentException ex) {
            return null;
         }
      }

      SupportedOS(String telemetryName, String libExtension) {
         this.telemetryName = telemetryName;
         this.libExtension = libExtension;
      }

      String telemetryName() {
         return this.telemetryName;
      }

      String libExtension() {
         return this.libExtension;
      }
   }
}
