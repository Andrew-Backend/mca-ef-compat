package forge.net.mca.entity.ai.relationship;

import net.minecraft.util.Mth;

public interface VillagerDimensions {
   float getWidth();

   float getHeight();

   float getBreasts();

   float getHead();

   final class Mutable implements VillagerDimensions {
      private float width;
      private float height;
      private float breasts;
      private float head;

      public Mutable(VillagerDimensions dimensions) {
         this.set(dimensions);
      }

      @Override
      public float getWidth() {
         return this.width;
      }

      @Override
      public float getHeight() {
         return this.height;
      }

      @Override
      public float getBreasts() {
         return this.breasts;
      }

      @Override
      public float getHead() {
         return this.head;
      }

      public void interpolate(VillagerDimensions a, VillagerDimensions b, float f) {
         this.width = Mth.m_14179_(f, a.getWidth(), b.getWidth());
         this.height = Mth.m_14179_(f, a.getHeight(), b.getHeight());
         this.breasts = Mth.m_14179_(f, a.getBreasts(), b.getBreasts());
         this.head = Mth.m_14179_(f, a.getHead(), b.getHead());
      }

      public void set(VillagerDimensions a) {
         this.width = a.getWidth();
         this.height = a.getHeight();
         this.breasts = a.getBreasts();
         this.head = a.getHead();
      }
   }
}
