package fabric.net.mca.mixin;

import fabric.net.mca.MCA;
import fabric.net.mca.advancement.criterion.CriterionMCA;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.class_2609;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_2609.class)
public class MixinAbstractFurnaceBlockEntity {
   @Final
   @Shadow
   private Object2IntOpenHashMap<class_2960> field_11986;

   @Inject(method = "method_17763", at = @At("HEAD"))
   public void onDropExperience(class_3222 player, CallbackInfo ci) {
      this.field_11986.forEach((identifier, count) -> {
         if (identifier.method_12836().equals("mca")) {
            boolean isBaby = identifier.equals(MCA.locate("baby_boy_from_smelting"));
            boolean isSirbenBaby = identifier.equals(MCA.locate("baby_sirben_boy_from_smelting"));
            if (isBaby || isSirbenBaby) {
               CriterionMCA.BABY_SMELTED_CRITERION.trigger(player, count);
               if (isSirbenBaby) {
                  CriterionMCA.BABY_SIRBEN_SMELTED_CRITERION.trigger(player, count);
               }
            }
         }
      });
   }
}
