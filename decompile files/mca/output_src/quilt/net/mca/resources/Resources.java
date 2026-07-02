package quilt.net.mca.resources;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import org.apache.commons.io.IOUtils;
import quilt.net.mca.MCA;
import quilt.net.mca.entity.interaction.InteractionPredicate;

public interface Resources {
   String RESOURCE_PREFIX = "assets/mca/";
   Gson GSON = new GsonBuilder().registerTypeAdapter(InteractionPredicate.class, InteractionPredicateTypeAdapter.INSTANCE).create();

   static String read(String path) throws IOException {
      return IOUtils.toString(new InputStreamReader(MCA.class.getClassLoader().getResourceAsStream("assets/mca/" + path)));
   }

   static <T> T read(String path, Type type) throws Resources.BrokenResourceException {
      try {
         return (T)GSON.fromJson(read(path), type);
      } catch (IOException | JsonParseException e) {
         throw new Resources.BrokenResourceException(path, e);
      }
   }

   static <T> T read(String path, Class<T> type) throws Resources.BrokenResourceException {
      return read(path, (Type)type);
   }

   class BrokenResourceException extends Exception {
      private static final long serialVersionUID = -7371322414731622879L;

      BrokenResourceException(String path, Throwable cause) {
         super("Unable to load resource from path " + path, cause);
      }
   }
}
