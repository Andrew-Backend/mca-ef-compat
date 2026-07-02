package forge.net.mca.mixin.client;

import forge.net.mca.Config;
import forge.net.mca.MCAClient;
import forge.net.mca.client.model.CommonVillagerModel;
import forge.net.mca.entity.VillagerLike;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {
   @Shadow
   @Final
   Minecraft f_109059_;
   @Shadow
   @Nullable
   PostChain f_109050_;
   @Unique
   private Tuple<String, ResourceLocation> currentShader;

   @Shadow
   abstract void m_109128_(ResourceLocation var1);

   @Shadow
   public abstract void m_109086_();

   @Inject(method = "m_109148_", at = @At("TAIL"))
   public void onCameraSet(CallbackInfo ci) {
      if (MCAClient.areShadersAllowed() && this.f_109059_.f_91075_ != null) {
         VillagerLike<?> villagerLike = CommonVillagerModel.getVillager(this.f_109059_.f_91075_);
         if (villagerLike != null) {
            if (this.f_109050_ == null) {
               if (this.currentShader != null) {
                  this.m_109128_((ResourceLocation)this.currentShader.m_14419_());
               } else {
                  Config.getInstance()
                     .shaderLocationsMap
                     .entrySet()
                     .stream()
                     .filter(entry -> villagerLike.getTraits().hasTrait(entry.getKey()))
                     .filter(entry -> MCAClient.areShadersAllowed(entry.getKey() + "_shader"))
                     .findFirst()
                     .ifPresent(entry -> {
                        ResourceLocation shaderId = new ResourceLocation(entry.getValue());
                        this.currentShader = new Tuple(entry.getKey(), shaderId);
                        this.m_109128_(shaderId);
                     });
               }
            } else if (this.currentShader != null && !villagerLike.getTraits().hasTrait((String)this.currentShader.m_14418_())) {
               this.m_109086_();
               this.currentShader = null;
            }
         }
      }
   }
}
