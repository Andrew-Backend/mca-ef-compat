package yesman.epicfight.api.animation;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraftforge.common.MinecraftForge;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.forgeevent.InitAnimatorEvent;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public abstract class Animator {
   protected final Map<LivingMotion, AssetAccessor<? extends StaticAnimation>> livingAnimations = Maps.newHashMap();
   protected final AnimationVariables animationVariables = new AnimationVariables(this);
   protected final LivingEntityPatch<?> entitypatch;

   public Animator(LivingEntityPatch<?> entitypatch) {
      this.entitypatch = entitypatch;
   }

   public abstract void playAnimation(AssetAccessor<? extends StaticAnimation> var1, float var2);

   public final void playAnimation(int id, float transitionTimeModifier) {
      this.playAnimation(AnimationManager.byId(id), transitionTimeModifier);
   }

   public abstract void playAnimationInstantly(AssetAccessor<? extends StaticAnimation> var1);

   public final void playAnimationInstantly(int id) {
      this.playAnimationInstantly(AnimationManager.byId(id));
   }

   public abstract void reserveAnimation(AssetAccessor<? extends StaticAnimation> var1);

   public final void reserveAnimation(int id) {
      this.reserveAnimation(AnimationManager.byId(id));
   }

   public abstract boolean stopPlaying(AssetAccessor<? extends StaticAnimation> var1);

   public abstract void playShootingAnimation();

   public final boolean stopPlaying(int id) {
      return this.stopPlaying(AnimationManager.byId(id));
   }

   public abstract void setSoftPause(boolean var1);

   public abstract void setHardPause(boolean var1);

   public abstract void tick();

   public abstract EntityState getEntityState();

   @Nullable
   public abstract AnimationPlayer getPlayerFor(@Nullable AssetAccessor<? extends DynamicAnimation> var1);

   @Nullable
   public abstract Optional<AnimationPlayer> getPlayer(AssetAccessor<? extends DynamicAnimation> var1);

   public abstract <T> Pair<AnimationPlayer, T> findFor(Class<T> var1);

   public abstract Pose getPose(float var1);

   public void postInit() {
      InitAnimatorEvent initAnimatorEvent = new InitAnimatorEvent(this.entitypatch, this);
      MinecraftForge.EVENT_BUS.post(initAnimatorEvent);
   }

   public void playDeathAnimation() {
      this.playAnimation(this.livingAnimations.getOrDefault(LivingMotions.DEATH, Animations.BIPED_DEATH), 0.0F);
   }

   public void addLivingAnimation(LivingMotion livingMotion, AssetAccessor<? extends StaticAnimation> animation) {
      if (AnimationManager.checkNull(animation)) {
         EpicFightMod.LOGGER.warn("Unable to put an empty animation for " + livingMotion);
      } else {
         this.livingAnimations.put(livingMotion, animation);
      }
   }

   public AssetAccessor<? extends StaticAnimation> getLivingAnimation(LivingMotion livingMotion, AssetAccessor<? extends StaticAnimation> defaultGetter) {
      return this.livingAnimations.getOrDefault(livingMotion, defaultGetter);
   }

   public Map<LivingMotion, AssetAccessor<? extends StaticAnimation>> getLivingAnimations() {
      return ImmutableMap.copyOf(this.livingAnimations);
   }

   public AnimationVariables getVariables() {
      return this.animationVariables;
   }

   public LivingEntityPatch<?> getEntityPatch() {
      return this.entitypatch;
   }

   public void resetLivingAnimations() {
      this.livingAnimations.clear();
   }
}
