package fabric.net.mca.resources.data.analysis;

import fabric.net.mca.resources.data.SerializablePair;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public abstract class Analysis<T extends Serializable> implements Serializable, Iterable<Analysis.AnalysisElement> {
   private static final long serialVersionUID = 2255112660663961645L;
   private final List<SerializablePair<String, T>> summands = new LinkedList<>();

   public void add(String key, T value) {
      this.summands.add(new SerializablePair<>(key, value));
   }

   public List<SerializablePair<String, T>> getSummands() {
      return this.summands;
   }

   public String getTotalAsString() {
      return this.asString(this.getTotal());
   }

   @NotNull
   @Override
   public Iterator<Analysis.AnalysisElement> iterator() {
      return new Iterator<Analysis.AnalysisElement>() {
         private int i = 0;

         @Override
         public boolean hasNext() {
            return this.i < Analysis.this.summands.size();
         }

         public Analysis.AnalysisElement next() {
            SerializablePair<String, T> pair = Analysis.this.summands.get(this.i++);
            return new Analysis.AnalysisElement(Analysis.this.isPositive(pair.getRight()), Analysis.this.asString(pair.getRight()), pair.getLeft());
         }

         @Override
         public void remove() {
            throw new UnsupportedOperationException();
         }
      };
   }

   public abstract boolean isPositive(T var1);

   public abstract String asString(T var1);

   public abstract T getTotal();

   public static class AnalysisElement {
      private final boolean positive;
      private final String value;
      private final String key;

      public AnalysisElement(boolean positive, String value, String key) {
         this.positive = positive;
         this.value = value;
         this.key = key;
      }

      public boolean isPositive() {
         return this.positive;
      }

      public String getValue() {
         return this.value;
      }

      public String getKey() {
         return this.key;
      }
   }
}
