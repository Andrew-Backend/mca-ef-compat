package forge.net.mca.util.network.datasync;

import java.util.function.BiFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;

public class CDataParameter<T> implements CParameter<T, T> {
   private final String id;
   private final T defaultValue;
   private final EntityDataSerializer<T> valueType;
   private final BiFunction<CompoundTag, String, T> load;
   private final CDataParameter.TriConsumer<CompoundTag, String, ? super T> save;

   protected CDataParameter(
      String id,
      EntityDataSerializer<T> valueType,
      T defaultValue,
      BiFunction<CompoundTag, String, T> load,
      CDataParameter.TriConsumer<CompoundTag, String, ? super T> save
   ) {
      this.id = id;
      this.defaultValue = defaultValue;
      this.valueType = valueType;
      this.load = load;
      this.save = save;
   }

   @Override
   public T getDefault() {
      return this.defaultValue;
   }

   @Override
   public T get(EntityDataAccessor<T> param, SynchedEntityData tracker) {
      return (T)tracker.m_135370_(param);
   }

   @Override
   public void set(EntityDataAccessor<T> param, SynchedEntityData tracker, T v) {
      tracker.m_135381_(param, v);
   }

   @Override
   public T load(CompoundTag nbt) {
      return this.load.apply(nbt, this.id);
   }

   @Override
   public void save(CompoundTag nbt, T value) {
      this.save.accept(nbt, this.id, value);
   }

   @Override
   public EntityDataAccessor<T> createParam(Class<? extends Entity> type) {
      return SynchedEntityData.m_135353_(type, this.valueType);
   }

   public interface TriConsumer<A, B, C> {
      void accept(A var1, B var2, C var3);
   }
}
