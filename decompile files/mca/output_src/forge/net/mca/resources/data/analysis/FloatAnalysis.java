package forge.net.mca.resources.data.analysis;

import forge.net.mca.resources.data.SerializablePair;

public class FloatAnalysis extends Analysis<Float> {
   private static final long serialVersionUID = -3009100555809907786L;

   public boolean isPositive(Float v) {
      return v >= 0.0F;
   }

   public String asString(Float v) {
      return (int)(v * 100.0F) + "%";
   }

   public Float getTotal() {
      return (float)Math.max(0.0, this.getSummands().stream().mapToDouble(SerializablePair::getRight).sum());
   }
}
