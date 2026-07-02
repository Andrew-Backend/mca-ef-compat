package quilt.net.mca.entity.ai.relationship;

import net.minecraft.class_3532;

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
         this.width = class_3532.method_16439(f, a.getWidth(), b.getWidth());
         this.height = class_3532.method_16439(f, a.getHeight(), b.getHeight());
         this.breasts = class_3532.method_16439(f, a.getBreasts(), b.getBreasts());
         this.head = class_3532.method_16439(f, a.getHead(), b.getHead());
      }

      public void set(VillagerDimensions a) {
         this.width = a.getWidth();
         this.height = a.getHeight();
         this.breasts = a.getBreasts();
         this.head = a.getHead();
      }
   }
}
