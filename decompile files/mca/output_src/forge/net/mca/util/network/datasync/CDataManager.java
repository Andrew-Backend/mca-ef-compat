package forge.net.mca.util.network.datasync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;

public class CDataManager<E extends Entity> {
   private final List<CDataManager.Entry<E, ?, ?>> params;
   private final Map<CParameter<?, ?>, CDataManager.Entry<E, ?, ?>> forwardLookup = new HashMap<>();
   private final Map<EntityDataAccessor<?>, CDataManager.Entry<E, ?, ?>> backwardLookup = new HashMap<>();

   private CDataManager(List<CDataManager.Entry<E, ?, ?>> params) {
      this.params = params;
      params.forEach(param -> {
         this.forwardLookup.put(param.parameter, (CDataManager.Entry<E, ?, ?>)param);
         this.backwardLookup.put(param.data, (CDataManager.Entry<E, ?, ?>)param);
      });
   }

   public boolean isParam(CParameter<?, ?> parameter, EntityDataAccessor<?> data) {
      CDataManager.Entry<E, ?, ?> entry = this.backwardLookup.get(data);
      return entry != null && entry.parameter == parameter;
   }

   public <T, TrackedType> T get(E entity, CParameter<T, TrackedType> parameter) {
      return parameter.get(this.forwardLookup.get(parameter).data, entity.m_20088_());
   }

   public <T, TrackedType> void set(E entity, CParameter<T, TrackedType> parameter, T value) {
      parameter.set(this.forwardLookup.get(parameter).data, entity.m_20088_(), value);
   }

   public void register(E entity) {
      this.params.forEach(p -> p.register(entity));
   }

   public void load(E entity, CompoundTag nbt) {
      this.params.forEach(p -> p.load(entity, nbt));
   }

   public void save(E entity, CompoundTag nbt) {
      this.params.forEach(p -> p.save(entity, nbt));
   }

   public static class Builder<E extends Entity> {
      private final Class<E> type;
      private final List<CDataManager.Entry<E, ?, ?>> params = new ArrayList<>();

      public Builder(Class<E> type) {
         this.type = type;
      }

      public CDataManager.Builder<E> addAll(CParameter<?, ?>... params) {
         Stream.of(params).map(p -> new CDataManager.Entry<>(this.type, (CParameter<?, ?>)p)).forEach(this.params::add);
         return this;
      }

      public CDataManager.Builder<E> add(Function<CDataManager.Builder<E>, CDataManager.Builder<E>> subType) {
         return subType.apply(this);
      }

      public CDataManager<E> build() {
         return new CDataManager<>(this.params);
      }
   }

   private static class Entry<E extends Entity, T, TrackedType> {
      CParameter<T, TrackedType> parameter;
      EntityDataAccessor<TrackedType> data;

      public Entry(Class<E> type, CParameter<T, TrackedType> parameter) {
         this.parameter = parameter;
         this.data = parameter.createParam(type);
      }

      public void save(E entity, CompoundTag nbt) {
         this.parameter.save(nbt, this.parameter.get(this.data, entity.m_20088_()));
      }

      public void load(E entity, CompoundTag nbt) {
         this.parameter.set(this.data, entity.m_20088_(), this.parameter.load(nbt));
      }

      public void register(E entity) {
         entity.m_20088_().m_135372_(this.data, this.parameter.getDefault());
      }
   }
}
