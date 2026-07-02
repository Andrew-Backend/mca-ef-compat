package yesman.epicfight.world.effect;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;

public class VisibleMobEffect extends MobEffect {
   protected final Int2ObjectMap<ResourceLocation> icons = new Int2ObjectOpenHashMap();
   protected final Function<MobEffectInstance, Integer> metadataGetter;

   public VisibleMobEffect(MobEffectCategory category, int color, ResourceLocation textureLocation) {
      this(category, color, effectInstance -> 0, textureLocation);
   }

   public VisibleMobEffect(MobEffectCategory category, int color, Function<MobEffectInstance, Integer> metadataGetter, ResourceLocation... textureLocations) {
      super(MobEffectCategory.BENEFICIAL, color);
      this.metadataGetter = metadataGetter;

      for (int i = 0; i < textureLocations.length; i++) {
         this.icons.put(i, textureLocations[i]);
      }
   }

   public ResourceLocation getIcon(MobEffectInstance effectInstance) {
      return (ResourceLocation)this.icons.get(this.metadataGetter.apply(effectInstance));
   }
}
