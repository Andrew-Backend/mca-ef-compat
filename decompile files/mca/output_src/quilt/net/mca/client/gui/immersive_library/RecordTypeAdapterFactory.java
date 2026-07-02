package quilt.net.mca.client.gui.immersive_library;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.HashMap;
import quilt.net.mca.MCA;

public class RecordTypeAdapterFactory implements TypeAdapterFactory {
   public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      final Class<T> clazz = type.getRawType();
      if (!clazz.isRecord()) {
         return null;
      }

      final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
      return new TypeAdapter<T>() {
         public void write(JsonWriter out, T value) throws IOException {
            delegate.write(out, value);
         }

         public T read(JsonReader reader) throws IOException {
            if (reader.peek() == JsonToken.NULL) {
               reader.nextNull();
               return null;
            }

            RecordComponent[] recordComponents = clazz.getRecordComponents();
            HashMap<String, TypeToken<?>> typeMap = new HashMap<>();

            for (RecordComponent component : recordComponents) {
               typeMap.put(component.getName(), TypeToken.get(component.getGenericType()));
            }

            HashMap<String, Object> argsMap = new HashMap<>();
            reader.beginObject();

            while (reader.hasNext()) {
               String name = reader.nextName();
               if (!typeMap.containsKey(name)) {
                  throw new RuntimeException("Unknown key " + name);
               }

               argsMap.put(name, gson.getAdapter(typeMap.get(name)).read(reader));
            }

            reader.endObject();
            Class<?>[] argTypes = new Class[recordComponents.length];
            Object[] args = new Object[recordComponents.length];

            for (int i = 0; i < recordComponents.length; i++) {
               argTypes[i] = recordComponents[i].getType();
               args[i] = argsMap.get(recordComponents[i].getName());
            }

            try {
               Constructor<T> constructor = clazz.getDeclaredConstructor(argTypes);
               constructor.setAccessible(true);
               return constructor.newInstance(args);
            } catch (NoSuchMethodException | InstantiationException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
               MCA.LOGGER.warn(e);
               return null;
            }
         }
      };
   }
}
