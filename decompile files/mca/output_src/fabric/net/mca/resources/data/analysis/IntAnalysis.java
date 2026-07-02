package fabric.net.mca.resources.data.analysis;

import fabric.net.mca.resources.data.SerializablePair;

public class IntAnalysis extends Analysis<Integer> {
   private static final long serialVersionUID = -2685774468194171791L;

   public boolean isPositive(Integer v) {
      return v >= 0;
   }

   public String asString(Integer v) {
      return String.valueOf(v);
   }

   public Integer getTotal() {
      return this.getSummands().stream().mapToInt(SerializablePair::getRight).sum();
   }
}
