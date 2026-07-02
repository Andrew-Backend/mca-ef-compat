package yesman.epicfight.api.utils.datastruct;

import java.util.HashMap;

public class TypeFlexibleHashMap<A extends TypeFlexibleHashMap.TypeKey<?>> extends HashMap<A, Object> {
   final boolean immutable;

   public TypeFlexibleHashMap(boolean immutable) {
      this.immutable = immutable;
   }

   public <T> T put(TypeFlexibleHashMap.TypeKey<T> typeKey, T val) {
      if (this.immutable) {
         throw new UnsupportedOperationException();
      } else {
         return super.put(typeKey, val);
      }
   }

   public <T> T get(A typeKey) {
      return (T)super.get(typeKey);
   }

   public <T> T getOrDefault(A typeKey) {
      return (T)super.getOrDefault(typeKey, typeKey.defaultValue());
   }

   public interface TypeKey<T> {
      T defaultValue();
   }
}
