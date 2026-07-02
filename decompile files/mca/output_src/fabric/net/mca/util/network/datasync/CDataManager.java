package fabric.net.mca.util.network.datasync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.class_1297;
import net.minecraft.class_2487;
import net.minecraft.class_2940;

public class CDataManager<E extends class_1297> {
   private final List<CDataManager.Entry<E, ?, ?>> params;
   private final Map<CParameter<?, ?>, CDataManager.Entry<E, ?, ?>> forwardLookup = new HashMap<>();
   private final Map<class_2940<?>, CDataManager.Entry<E, ?, ?>> backwardLookup = new HashMap<>();

   private CDataManager(List<CDataManager.Entry<E, ?, ?>> params) {
      this.params = params;
      params.forEach(param -> {
         this.forwardLookup.put(param.parameter, (CDataManager.Entry<E, ?, ?>)param);
         this.backwardLookup.put(param.data, (CDataManager.Entry<E, ?, ?>)param);
      });
   }

   public boolean isParam(CParameter<?, ?> parameter, class_2940<?> data) {
      CDataManager.Entry<E, ?, ?> entry = this.backwardLookup.get(data);
      return entry != null && entry.parameter == parameter;
   }

   public <T, TrackedType> T get(E entity, CParameter<T, TrackedType> parameter) {
      return parameter.get(this.forwardLookup.get(parameter).data, entity.method_5841());
   }

   public <T, TrackedType> void set(E entity, CParameter<T, TrackedType> parameter, T value) {
      parameter.set(this.forwardLookup.get(parameter).data, entity.method_5841(), value);
   }

   public void register(E entity) {
      this.params.forEach(p -> p.register(entity));
   }

   public void load(E entity, class_2487 nbt) {
      this.params.forEach(p -> p.load(entity, nbt));
   }

   public void save(E entity, class_2487 nbt) {
      this.params.forEach(p -> p.save(entity, nbt));
   }

   public static class Builder<E extends class_1297> {
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

   private static class Entry<E extends class_1297, T, TrackedType> {
      CParameter<T, TrackedType> parameter;
      class_2940<TrackedType> data;

      public Entry(Class<E> type, CParameter<T, TrackedType> parameter) {
         this.parameter = parameter;
         this.data = parameter.createParam(type);
      }

      public void save(E entity, class_2487 nbt) {
         this.parameter.save(nbt, this.parameter.get(this.data, entity.method_5841()));
      }

      public void load(E entity, class_2487 nbt) {
         this.parameter.set(this.data, entity.method_5841(), this.parameter.load(nbt));
      }

      public void register(E entity) {
         entity.method_5841().method_12784(this.data, this.parameter.getDefault());
      }
   }
}
