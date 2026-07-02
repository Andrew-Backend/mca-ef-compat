package fabric.net.mca.client.particle;

import net.minecraft.class_2400;
import net.minecraft.class_3999;
import net.minecraft.class_4002;
import net.minecraft.class_4003;
import net.minecraft.class_638;
import net.minecraft.class_703;
import net.minecraft.class_707;

public class InteractionParticle extends class_4003 {
   protected InteractionParticle(class_638 world, double x, double y, double z) {
      super(world, x, y, z);
      this.field_3852 *= 0.01F;
      this.field_3869 *= 0.01F;
      this.field_3850 *= 0.01F;
      this.field_3869 += 0.1;
      this.field_17867 *= 1.5F;
      this.field_3847 = 20;
      this.field_3862 = false;
   }

   public class_3999 method_18122() {
      return class_3999.field_17828;
   }

   public float method_18132(float tickDelta) {
      return 0.3F;
   }

   public void method_3070() {
      this.field_3858 = this.field_3874;
      this.field_3838 = this.field_3854;
      this.field_3856 = this.field_3871;
      if (this.field_3866++ >= this.field_3847) {
         this.method_3085();
      } else {
         if (this.field_3854 == this.field_3838) {
            this.field_3852 *= 1.1;
            this.field_3850 *= 1.1;
         }

         this.field_3852 *= 0.86F;
         this.field_3869 *= 0.86F;
         this.field_3850 *= 0.86F;
         if (this.field_3845) {
            this.field_3852 *= 0.7F;
            this.field_3850 *= 0.7F;
         }
      }
   }

   public static class Factory implements class_707<class_2400> {
      private final class_4002 sprite;

      public Factory(class_4002 sprite) {
         this.sprite = sprite;
      }

      public class_703 createParticle(
         class_2400 particleType, class_638 world, double x, double y, double z, double velocityX, double velocityY, double velocityZ
      ) {
         InteractionParticle heartparticle = new InteractionParticle(world, x, y + 0.5, z);
         heartparticle.method_18140(this.sprite);
         heartparticle.method_3084(1.0F, 1.0F, 1.0F);
         return heartparticle;
      }
   }
}
