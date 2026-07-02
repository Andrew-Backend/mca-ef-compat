package yesman.epicfight.api.animation.types.datapack;

import com.google.gson.JsonArray;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationClip;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;

@OnlyIn(Dist.CLIENT)
public interface DatapackAnimation<A extends StaticAnimation> extends AnimationManager.AnimationAccessor<A> {
   void setAnimationClip(AnimationClip var1);

   void setRegistryName(ResourceLocation var1);

   void setCreator(EditorAnimation var1);

   EditorAnimation getCreator();

   EditorAnimation readAnimationFromJson(JsonArray var1);

   @Override
   default int id() {
      return -1;
   }

   @Override
   default boolean inRegistry() {
      return false;
   }
}
