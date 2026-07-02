package forge.net.mca.util.network.datasync;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public interface CParameter<T, TrackedType> {
   static CDataParameter<Integer> create(String id, int def) {
      return new CDataParameter<>(
         id, EntityDataSerializers.f_135028_, def, (nbt, key) -> NbtCompoundDefaultGetters.getInt(nbt, key, def), CompoundTag::m_128405_
      );
   }

   static CDataParameter<Float> create(String id, float def) {
      return new CDataParameter<>(
         id, EntityDataSerializers.f_135029_, def, (nbt, key) -> NbtCompoundDefaultGetters.getFloat(nbt, key, def), CompoundTag::m_128350_
      );
   }

   static CDataParameter<Boolean> create(String id, boolean def) {
      return new CDataParameter<>(id, EntityDataSerializers.f_135035_, def, CompoundTag::m_128471_, CompoundTag::m_128379_);
   }

   static CDataParameter<String> create(String id, String def) {
      return new CDataParameter<>(
         id, EntityDataSerializers.f_135030_, def, (nbt, key) -> NbtCompoundDefaultGetters.getString(nbt, key, def), CompoundTag::m_128359_
      );
   }

   static CDataParameter<CompoundTag> create(String id, CompoundTag def) {
      return new CDataParameter<>(
         id, EntityDataSerializers.f_135042_, def, (nbt, key) -> NbtCompoundDefaultGetters.getCompound(nbt, key, def), CompoundTag::m_128365_
      );
   }

   static CDataParameter<ItemStack> create(String id, ItemStack def) {
      return new CDataParameter<>(
         "babyItem",
         EntityDataSerializers.f_135033_,
         ItemStack.f_41583_,
         (nbt, key) -> NbtCompoundDefaultGetters.getItemStack(nbt, key, ItemStack.f_41583_),
         (nbt, key, stack) -> {
            CompoundTag item = new CompoundTag();
            stack.m_41739_(item);
            nbt.m_128365_(key, item);
         }
      );
   }

   static CDataParameter<BlockPos> create(String id, BlockPos def) {
      return new CDataParameter<>(
         id,
         EntityDataSerializers.f_135038_,
         def,
         (tag, key) -> new BlockPos(tag.m_128451_(key + "X"), tag.m_128451_(key + "Y"), tag.m_128451_(key + "Z")),
         (tag, key, pos) -> {
            tag.m_128405_(key + "X", pos.m_123341_());
            tag.m_128405_(key + "Y", pos.m_123342_());
            tag.m_128405_(key + "Z", pos.m_123343_());
         }
      );
   }

   static CDataParameter<Optional<UUID>> create(String id, Optional<UUID> def) {
      return new CDataParameter<>(
         id,
         EntityDataSerializers.f_135041_,
         def,
         (tag, key) -> tag.m_128403_(key) ? Optional.of(tag.m_128342_(key)) : Optional.empty(),
         (tag, key, v) -> v.ifPresent(uuid -> tag.m_128362_(key, uuid))
      );
   }

   static <T extends Enum<T>> CEnumParameter<T> create(String id, T def) {
      return new CEnumParameter<>(id, (Class<T>)def.getClass(), def);
   }

   static <T extends Enum<T>> CEnumParameter<T> create(String id, Class<T> type) {
      return new CEnumParameter<>(id, type, null);
   }

   TrackedType getDefault();

   T get(EntityDataAccessor<TrackedType> var1, SynchedEntityData var2);

   void set(EntityDataAccessor<TrackedType> var1, SynchedEntityData var2, T var3);

   T load(CompoundTag var1);

   void save(CompoundTag var1, T var2);

   EntityDataAccessor<TrackedType> createParam(Class<? extends Entity> var1);
}
