package fabric.net.mca.client.gui.immersive_library;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import fabric.net.mca.MCA;
import fabric.net.mca.client.gui.immersive_library.responses.ContentResponse;
import fabric.net.mca.client.gui.immersive_library.types.LiteContent;
import fabric.net.mca.client.resources.SkinMeta;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1060;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.apache.commons.io.FileUtils;

public class SkinCache {
   private static final class_2960 DEFAULT_SKIN = MCA.locate("skins/empty.png");
   private static final Gson gson = new Gson();
   static final Map<Integer, Boolean> requested = new ConcurrentHashMap<>();
   static final Map<Integer, Integer> cachedVersions = new ConcurrentHashMap<>();
   static final Map<Integer, class_2960> textureIdentifiers = new HashMap<>();
   static final Map<Integer, class_1011> images = new HashMap<>();
   static final Map<Integer, SkinMeta> metas = new HashMap<>();

   private static File getFile(String key) {
      new File("./immersive_library/").mkdirs();
      return new File("./immersive_library/" + key);
   }

   private static void write(String file, String content) {
      try {
         FileUtils.writeStringToFile(getFile(file), content, Charset.defaultCharset(), false);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private static void write(String file, byte[] content) {
      try (OutputStream out = new BufferedOutputStream(new FileOutputStream(getFile(file)))) {
         out.write(content);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private static String read(String file) throws IOException {
      return FileUtils.readFileToString(getFile(file), Charset.defaultCharset());
   }

   public static void enforceSync(int contentid) {
      try {
         Files.delete(getFile(contentid + ".version").toPath());
         cachedVersions.remove(contentid);
      } catch (IOException e) {
         MCA.LOGGER.warn(e);
      }
   }

   public static void sync(LiteContent content) {
      sync(content.contentid(), content.version());
   }

   public static void sync(int contentid, int currentVersion) {
      int version = cachedVersions.computeIfAbsent(contentid, id -> {
         File file = getFile(contentid + ".version");
         if (file.exists()) {
            try {
               String s = FileUtils.readFileToString(file, Charset.defaultCharset());
               return Integer.parseInt(s);
            } catch (Exception e) {
               MCA.LOGGER.warn(e);
            }
         }

         return -1;
      });
      if (currentVersion == version) {
         if (!textureIdentifiers.containsKey(contentid)) {
            loadResources(contentid);
         }
      } else {
         if (version >= 0 && !textureIdentifiers.containsKey(contentid)) {
            loadResources(contentid);
         }

         if (!requested.containsKey(contentid) && (currentVersion > version || !textureIdentifiers.containsKey(contentid))) {
            requested.put(contentid, true);
            CompletableFuture.runAsync(
               () -> {
                  logger("Requested asset " + contentid + " with version " + version + " and current version " + currentVersion);
                  if (Api.request(Api.HttpMethod.GET, ContentResponse.class, "content/mca/" + contentid, Map.of("version", String.valueOf(version))) instanceof ContentResponse contentResponse
                     )
                   {
                     int newVersion = contentResponse.content().version();
                     write(contentid + ".png", Base64.getDecoder().decode(contentResponse.content().data()));
                     write(contentid + ".json", contentResponse.content().meta());
                     write(contentid + ".version", Integer.toString(newVersion));
                     cachedVersions.put(contentid, newVersion);
                     requested.remove(contentid);
                     textureIdentifiers.remove(contentid);
                     logger("Received " + contentid);
                  }
               }
            );
         }
      }
   }

   private static void loadResources(int contentid) {
      logger("Loaded asset " + contentid);

      try {
         String json = read(contentid + ".json");
         SkinMeta meta = (SkinMeta)gson.fromJson(json, SkinMeta.class);
         metas.put(contentid, meta);
      } catch (JsonSyntaxException | IOException e) {
         e.printStackTrace();
         enforceSync(contentid);
         return;
      }

      try (FileInputStream stream = new FileInputStream(getFile(contentid + ".png").getPath())) {
         class_1011 image = class_1011.method_4309(stream);
         class_2960 identifier = new class_2960("immersive_library", String.valueOf(contentid));
         class_1060 textureManager = class_310.method_1551().method_1531();
         textureManager.method_4616(identifier, new class_1043(image));
         textureIdentifiers.put(contentid, identifier);
         images.put(contentid, image);
      } catch (IOException e) {
         e.printStackTrace();
         enforceSync(contentid);
      }
   }

   private static void logger(String s) {
   }

   public static Optional<SkinMeta> getMeta(LiteContent content) {
      sync(content);
      return Optional.ofNullable(metas.get(content.contentid()));
   }

   public static Optional<class_1011> getImage(LiteContent content) {
      sync(content);
      return Optional.ofNullable(images.get(content.contentid()));
   }

   public static class_2960 getTextureIdentifier(LiteContent content) {
      sync(content);
      return textureIdentifiers.getOrDefault(content.contentid(), DEFAULT_SKIN);
   }

   public static class_2960 getTextureIdentifier(int contentid) {
      sync(contentid, -2);
      return textureIdentifiers.getOrDefault(contentid, DEFAULT_SKIN);
   }
}
