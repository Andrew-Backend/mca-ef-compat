package yesman.epicfight.api.animation.types;

import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class ReboundAnimation extends AimAnimation {
   public ReboundAnimation(
      float transitionTime,
      boolean repeatPlay,
      AnimationManager.AnimationAccessor<? extends ReboundAnimation> accessor,
      String path1,
      String path2,
      String path3,
      String path4,
      AssetAccessor<? extends Armature> armature
   ) {
      super(transitionTime, repeatPlay, accessor, path1, path2, path3, path4, armature);
   }

   public ReboundAnimation(
      boolean repeatPlay,
      AnimationManager.AnimationAccessor<? extends ReboundAnimation> accessor,
      String path1,
      String path2,
      String path3,
      String path4,
      AssetAccessor<? extends Armature> armature
   ) {
      this(0.15F, repeatPlay, accessor, path1, path2, path3, path4, armature);
   }

   @Override
   public void tick(LivingEntityPatch<?> entitypatch) {
   }

   @Override
   public boolean isReboundAnimation() {
      return true;
   }
}
