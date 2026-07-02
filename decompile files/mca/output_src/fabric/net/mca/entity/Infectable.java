package fabric.net.mca.entity;

public interface Infectable {
   float INITIAL_INFECTION_AMOUNT = 0.001F;
   float FEVER_THRESHOLD = 0.2F;
   float BABBLING_THRESHOLD = 0.6F;

   default boolean isInfected() {
      return this.getInfectionProgress() > 0.0F;
   }

   default void setInfected(boolean infected) {
      this.setInfectionProgress(infected ? Math.max(this.getInfectionProgress(), 0.001F) : 0.0F);
   }

   float getInfectionProgress();

   void setInfectionProgress(float var1);
}
