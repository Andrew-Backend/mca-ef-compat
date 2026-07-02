package quilt.net.mca.mixin.client;

import net.minecraft.class_279;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3545;
import net.minecraft.class_757;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quilt.net.mca.Config;
import quilt.net.mca.MCAClient;
import quilt.net.mca.client.model.CommonVillagerModel;
import quilt.net.mca.entity.VillagerLike;

@Mixin(class_757.class)
public abstract class MixinGameRenderer {
   @Shadow
   @Final
   class_310 field_4015;
   @Shadow
   @Nullable
   class_279 field_4024;
   @Unique
   private class_3545<String, class_2960> currentShader;

   @Shadow
   abstract void method_3168(class_2960 var1);

   @Shadow
   public abstract void method_3207();

   @Inject(method = "method_3182", at = @At("TAIL"))
   public void onCameraSet(CallbackInfo ci) {
      if (MCAClient.areShadersAllowed() && this.field_4015.field_1719 != null) {
         VillagerLike<?> villagerLike = CommonVillagerModel.getVillager(this.field_4015.field_1719);
         if (villagerLike != null) {
            if (this.field_4024 == null) {
               if (this.currentShader != null) {
                  this.method_3168((class_2960)this.currentShader.method_15441());
               } else {
                  Config.getInstance()
                     .shaderLocationsMap
                     .entrySet()
                     .stream()
                     .filter(entry -> villagerLike.getTraits().hasTrait(entry.getKey()))
                     .filter(entry -> MCAClient.areShadersAllowed(entry.getKey() + "_shader"))
                     .findFirst()
                     .ifPresent(entry -> {
                        class_2960 shaderId = new class_2960(entry.getValue());
                        this.currentShader = new class_3545(entry.getKey(), shaderId);
                        this.method_3168(shaderId);
                     });
               }
            } else if (this.currentShader != null && !villagerLike.getTraits().hasTrait((String)this.currentShader.method_15442())) {
               this.method_3207();
               this.currentShader = null;
            }
         }
      }
   }
}
