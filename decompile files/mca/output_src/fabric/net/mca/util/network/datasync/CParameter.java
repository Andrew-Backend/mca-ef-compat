package fabric.net.mca.util.network.datasync;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_2487;
import net.minecraft.class_2940;
import net.minecraft.class_2943;
import net.minecraft.class_2945;

public interface CParameter<T, TrackedType> {
   static CDataParameter<Integer> create(String id, int def) {
      return new CDataParameter<>(id, class_2943.field_13327, def, (nbt, key) -> NbtCompoundDefaultGetters.getInt(nbt, key, def), class_2487::method_10569);
   }

   static CDataParameter<Float> create(String id, float def) {
      return new CDataParameter<>(id, class_2943.field_13320, def, (nbt, key) -> NbtCompoundDefaultGetters.getFloat(nbt, key, def), class_2487::method_10548);
   }

   static CDataParameter<Boolean> create(String id, boolean def) {
      return new CDataParameter<>(id, class_2943.field_13323, def, class_2487::method_10577, class_2487::method_10556);
   }

   static CDataParameter<String> create(String id, String def) {
      return new CDataParameter<>(id, class_2943.field_13326, def, (nbt, key) -> NbtCompoundDefaultGetters.getString(nbt, key, def), class_2487::method_10582);
   }

   static CDataParameter<class_2487> create(String id, class_2487 def) {
      return new CDataParameter<>(id, class_2943.field_13318, def, (nbt, key) -> NbtCompoundDefaultGetters.getCompound(nbt, key, def), class_2487::method_10566);
   }

   static CDataParameter<class_1799> create(String id, class_1799 def) {
      return new CDataParameter<>(
         "babyItem",
         class_2943.field_13322,
         class_1799.field_8037,
         (nbt, key) -> NbtCompoundDefaultGetters.getItemStack(nbt, key, class_1799.field_8037),
         (nbt, key, stack) -> {
            class_2487 item = new class_2487();
            stack.method_7953(item);
            nbt.method_10566(key, item);
         }
      );
   }

   static CDataParameter<class_2338> create(String id, class_2338 def) {
      return new CDataParameter<>(
         id,
         class_2943.field_13324,
         def,
         (tag, key) -> new class_2338(tag.method_10550(key + "X"), tag.method_10550(key + "Y"), tag.method_10550(key + "Z")),
         (tag, key, pos) -> {
            tag.method_10569(key + "X", pos.method_10263());
            tag.method_10569(key + "Y", pos.method_10264());
            tag.method_10569(key + "Z", pos.method_10260());
         }
      );
   }

   static CDataParameter<Optional<UUID>> create(String id, Optional<UUID> def) {
      return new CDataParameter<>(
         id,
         class_2943.field_13313,
         def,
         (tag, key) -> tag.method_25928(key) ? Optional.of(tag.method_25926(key)) : Optional.empty(),
         (tag, key, v) -> v.ifPresent(uuid -> tag.method_25927(key, uuid))
      );
   }

   static <T extends Enum<T>> CEnumParameter<T> create(String id, T def) {
      return new CEnumParameter<>(id, (Class<T>)def.getClass(), def);
   }

   static <T extends Enum<T>> CEnumParameter<T> create(String id, Class<T> type) {
      return new CEnumParameter<>(id, type, null);
   }

   TrackedType getDefault();

   T get(class_2940<TrackedType> var1, class_2945 var2);

   void set(class_2940<TrackedType> var1, class_2945 var2, T var3);

   T load(class_2487 var1);

   void save(class_2487 var1, T var2);

   class_2940<TrackedType> createParam(Class<? extends class_1297> var1);
}
