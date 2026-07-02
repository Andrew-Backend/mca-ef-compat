package forge.net.mca.util.network.datasync;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class CEnumParameter<T extends Enum<T>> implements CParameter<T, Integer> {
   private final String id;
   @Nullable
   private final T defaultValue;
   private final T[] values;

   public CEnumParameter(String id, Class<T> type, @Nullable T dv) {
      this.id = id;
      this.defaultValue = dv;
      this.values = type.getEnumConstants();
   }

   public Integer getDefault() {
      return this.defaultValue == null ? -1 : this.defaultValue.ordinal();
   }

   public T get(EntityDataAccessor<Integer> param, SynchedEntityData tracker) {
      return this.fromIndex((Integer)tracker.m_135370_(param));
   }

   public void set(EntityDataAccessor<Integer> param, SynchedEntityData tracker, @Nullable T v) {
      tracker.m_135381_(param, v == null ? -1 : v.ordinal());
   }

   public T load(CompoundTag nbt) {
      return nbt.m_128425_(this.id, 99) ? this.fromIndex(nbt.m_128451_(this.id)) : this.defaultValue;
   }

   public void save(CompoundTag nbt, T value) {
      if (value != null) {
         nbt.m_128405_(this.id, value.ordinal());
      }
   }

   private T fromIndex(int index) {
      return index >= 0 && index < this.values.length ? this.values[index] : this.defaultValue;
   }

   @Override
   public EntityDataAccessor<Integer> createParam(Class<? extends Entity> type) {
      return SynchedEntityData.m_135353_(type, EntityDataSerializers.f_135028_);
   }
}
