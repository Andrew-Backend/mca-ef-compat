package forge.net.mca.resources.data.analysis;

import forge.net.mca.resources.data.SerializablePair;

public class ChanceAnalysis extends Analysis<Integer> {
   private static final long serialVersionUID = -2685774468194171791L;

   public boolean isPositive(Integer v) {
      return v >= 0;
   }

   public String asString(Integer v) {
      return String.valueOf(v);
   }

   @Override
   public String getTotalAsString() {
      float positive = this.getSummands().stream().mapToInt(SerializablePair::getRight).filter(v -> v > 0).sum();
      float negative = -this.getSummands().stream().mapToInt(SerializablePair::getRight).filter(v -> v < 0).sum();
      int chance = (int)(positive / (positive + negative) * 100.0F);
      return chance + "% chance";
   }

   public Integer getTotal() {
      return this.getSummands().stream().mapToInt(SerializablePair::getRight).sum();
   }
}
