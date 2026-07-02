package fabric.net.mca.resources;

import java.util.ArrayList;
import java.util.List;

public class WeightedPool<T> {
   private final T defaultValue;
   protected final List<WeightedPool.Entry<T>> entries = new ArrayList<>();

   public WeightedPool(T defaultValue) {
      this.defaultValue = defaultValue;
   }

   public T pickOne() {
      double totalChance = this.entries.stream().mapToDouble(a -> a.weight).sum() * API.getRng().method_43058();

      for (WeightedPool.Entry<T> e : this.entries) {
         totalChance -= e.weight;
         if (totalChance <= 0.0) {
            return e.value;
         }
      }

      return this.defaultValue;
   }

   public T pickNext(T current, int next) {
      for (int i = 0; i < this.entries.size(); i++) {
         if (this.entries.get(i).value.equals(current)) {
            return this.entries.get(Math.floorMod(i + next, this.entries.size())).value;
         }
      }

      return this.pickOne();
   }

   public List<WeightedPool.Entry<T>> getEntries() {
      return this.entries;
   }

   public static class Entry<T> {
      private final T value;
      private final float weight;

      public Entry(T value, float weight) {
         this.value = value;
         this.weight = weight;
      }

      public T getValue() {
         return this.value;
      }

      public float getWeight() {
         return this.weight;
      }
   }

   public static class Mutable<T> extends WeightedPool<T> {
      public Mutable(T defaultValue) {
         super(defaultValue);
      }

      public void add(T value, float weight) {
         this.entries.add(new WeightedPool.Entry<>(value, weight));
      }
   }
}
