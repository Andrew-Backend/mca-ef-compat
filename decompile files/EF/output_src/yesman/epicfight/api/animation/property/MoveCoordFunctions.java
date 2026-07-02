package yesman.epicfight.api.animation.property;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.AnimationVariables;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Keyframe;
import yesman.epicfight.api.animation.SynchedAnimationVariableKeys;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.api.utils.math.Vec4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;

public class MoveCoordFunctions {
   public static final MoveCoordFunctions.MoveCoordGetter MODEL_COORD = (animation, entitypatch, coord, prevElapsedTime, elapsedTime) -> {
      LivingEntity livingentity = entitypatch.getOriginal();
      JointTransform oJt = coord.getInterpolatedTransform(prevElapsedTime);
      JointTransform jt = coord.getInterpolatedTransform(elapsedTime);
      Vec4f prevpos = new Vec4f(oJt.translation());
      Vec4f currentpos = new Vec4f(jt.translation());
      OpenMatrix4f rotationTransform = entitypatch.getModelMatrix(1.0F).removeTranslation().removeScale();
      OpenMatrix4f localTransform = entitypatch.getArmature().searchJointByName("Root").getLocalTransform().removeTranslation();
      rotationTransform.mulBack(localTransform);
      currentpos.transform(rotationTransform);
      prevpos.transform(rotationTransform);
      boolean hasNoGravity = entitypatch.getOriginal().m_20068_();
      boolean moveVertical = animation.getProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL).orElse(false)
         || animation.getProperty(AnimationProperty.ActionAnimationProperty.COORD).isPresent();
      float dx = prevpos.x - currentpos.x;
      float dy = !moveVertical && !hasNoGravity ? 0.0F : currentpos.y - prevpos.y;
      float dz = prevpos.z - currentpos.z;
      dx = Math.abs(dx) > 1.0E-4F ? dx : 0.0F;
      dz = Math.abs(dz) > 1.0E-4F ? dz : 0.0F;
      BlockPos blockpos = new MutableBlockPos(livingentity.m_20185_(), livingentity.m_20191_().f_82289_ - 1.0, livingentity.m_20189_());
      BlockState blockState = livingentity.m_9236_().m_8055_(blockpos);
      AttributeInstance movementSpeed = livingentity.m_21051_(Attributes.f_22279_);
      boolean soulboost = blockState.m_204336_(BlockTags.f_13080_) && EnchantmentHelper.m_44836_(Enchantments.f_44976_, livingentity) > 0;
      float speedFactor = (float)(soulboost ? 1.0 : livingentity.m_9236_().m_8055_(blockpos).m_60734_().m_49961_());
      float moveMultiplier = (float)(
         animation.getProperty(AnimationProperty.ActionAnimationProperty.AFFECT_SPEED).orElse(false)
            ? movementSpeed.m_22135_() / movementSpeed.m_22115_()
            : 1.0
      );
      return new Vec3f(dx * moveMultiplier * speedFactor, dy, dz * moveMultiplier * speedFactor);
   };
   public static final MoveCoordFunctions.MoveCoordGetter WORLD_COORD = (animation, entitypatch, coord, prevElapsedTime, elapsedTime) -> {
      JointTransform jt = coord.getInterpolatedTransform(elapsedTime);
      Vec3 entityPos = entitypatch.getOriginal().m_20182_();
      return jt.translation().copy().sub(Vec3f.fromDoubleVector(entityPos));
   };
   public static final MoveCoordFunctions.MoveCoordGetter ATTACHED = (animation, entitypatch, coord, prevElapsedTime, elapsedTime) -> {
      LivingEntity target = entitypatch.getGrapplingTarget();
      if (target == null) {
         return MODEL_COORD.get(animation, entitypatch, coord, prevElapsedTime, elapsedTime);
      }

      TransformSheet rootCoord = animation.getCoord();
      LivingEntity livingentity = entitypatch.getOriginal();
      Vec3f model = rootCoord.getInterpolatedTransform(elapsedTime).translation();
      Vec3f world = OpenMatrix4f.transform3v(OpenMatrix4f.createRotatorDeg(-target.m_146908_(), Vec3f.Y_AXIS), model, null);
      Vec3f dst = Vec3f.fromDoubleVector(target.m_20182_()).add(world);
      entitypatch.setYRot(Mth.m_14177_(target.m_146908_() + 180.0F));
      return dst.sub(Vec3f.fromDoubleVector(livingentity.m_20182_()));
   };
   public static final AnimationProperty.DestLocationProvider NO_DEST = (self, entitypatch) -> null;
   public static final AnimationProperty.DestLocationProvider ATTACK_TARGET_LOCATION = (self, entitypatch) -> entitypatch.getTarget() == null
      ? null
      : entitypatch.getTarget().m_20182_();
   public static final AnimationProperty.DestLocationProvider SYNCHED_DEST_VARIABLE = (self, entitypatch) -> entitypatch.<Animator>getAnimator()
      .getVariables()
      .getOrDefault((AnimationVariables.IndependentAnimationVariableKey<Vec3>)SynchedAnimationVariableKeys.DESTINATION.get(), self.getRealAnimation());
   public static final AnimationProperty.DestLocationProvider SYNCHED_TARGET_ENTITY_LOCATION_VARIABLE = (self, entitypatch) -> {
      Optional<Integer> targetEntityId = entitypatch.<Animator>getAnimator()
         .getVariables()
         .get((AnimationVariables.IndependentAnimationVariableKey<Integer>)SynchedAnimationVariableKeys.TARGET_ENTITY.get(), self.getRealAnimation());
      if (targetEntityId.isPresent()) {
         Entity entity = entitypatch.getOriginal().m_9236_().m_6815_(targetEntityId.get());
         if (entity != null) {
            return entity.m_20182_();
         }
      }

      return entitypatch.getOriginal().m_20182_();
   };
   public static final AnimationProperty.YRotProvider LOOK_DEST = (self, entitypatch) -> {
      Vec3 destLocation = self.getRealAnimation()
         .get()
         .getProperty(AnimationProperty.ActionAnimationProperty.DEST_LOCATION_PROVIDER)
         .orElse(NO_DEST)
         .get(self, entitypatch);
      if (destLocation != null) {
         Vec3 startInWorld = entitypatch.<Animator>getAnimator().getVariables().getOrDefault(ActionAnimation.BEGINNING_LOCATION, self.getRealAnimation());
         if (startInWorld == null) {
            startInWorld = entitypatch.getOriginal().m_20182_();
         }

         Vec3 toDestWorld = destLocation.m_82546_(startInWorld);
         float yRot = (float)Mth.m_14175_(MathUtils.getYRotOfVector(toDestWorld));
         return MathUtils.rotlerp(entitypatch.getYRot(), yRot, entitypatch.getYRotLimit());
      } else {
         return entitypatch.getYRot();
      }
   };
   public static final AnimationProperty.YRotProvider MOB_ATTACK_TARGET_LOOK = (self, entitypatch) -> {
      if (!entitypatch.isLogicalClient() && entitypatch instanceof MobPatch<?> mobpatch) {
         AnimationPlayer player = entitypatch.<Animator>getAnimator().getPlayerFor(self.getAccessor());
         float elapsedTime = player.getElapsedTime();
         EntityState state = self.getState(entitypatch, elapsedTime);
         if (state.getLevel() == 1 && !state.turningLocked()) {
            mobpatch.getOriginal().m_21573_().m_26573_();
            entitypatch.getOriginal().f_20921_ = 2.0F;
            LivingEntity target = entitypatch.getTarget();
            if (target != null) {
               float currentYRot = Mth.m_14177_(entitypatch.getOriginal().m_146908_());
               float clampedYRot = entitypatch.getYRotDeltaTo(target);
               return currentYRot + clampedYRot;
            }
         }
      }

      return entitypatch.getYRot();
   };
   public static final MoveCoordFunctions.MoveCoordSetter RAW_COORD = (self, entitypatch, transformSheet) -> transformSheet.readFrom(self.getCoord().copyAll());
   public static final MoveCoordFunctions.MoveCoordSetter RAW_COORD_WITH_X_ROT = (self, entitypatch, transformSheet) -> {
      TransformSheet sheet = self.getCoord().copyAll();
      float xRot = entitypatch.getOriginal().m_146909_();

      for (Keyframe kf : sheet.getKeyframes()) {
         kf.transform().translation().rotate(-xRot, Vec3f.X_AXIS);
      }

      transformSheet.readFrom(sheet);
   };
   public static final MoveCoordFunctions.MoveCoordSetter TRACE_ORIGIN_AS_DESTINATION = (self, entitypatch, transformSheet) -> {
      if (self.isLinkAnimation()) {
         transformSheet.readFrom(TransformSheet.EMPTY_SHEET_PROVIDER.apply(entitypatch.getOriginal().m_20182_()));
      } else {
         Keyframe[] coordKeyframes = self.getCoord().getKeyframes();
         int startFrame = self.getRealAnimation().get().getProperty(AnimationProperty.ActionAnimationProperty.COORD_START_KEYFRAME_INDEX).orElse(0);
         int destFrame = self.getRealAnimation()
            .get()
            .getProperty(AnimationProperty.ActionAnimationProperty.COORD_DEST_KEYFRAME_INDEX)
            .orElse(coordKeyframes.length - 1);
         Vec3 destInWorld = self.getRealAnimation()
            .get()
            .getProperty(AnimationProperty.ActionAnimationProperty.DEST_LOCATION_PROVIDER)
            .orElse(NO_DEST)
            .get(self, entitypatch);
         if (destInWorld == null) {
            Vec3f beginningPosition = coordKeyframes[0].transform().translation().copy().multiply(1.0F, 1.0F, -1.0F);
            beginningPosition.rotate(-entitypatch.getYRot(), Vec3f.Y_AXIS);
            destInWorld = entitypatch.getOriginal().m_20182_().m_82520_(-beginningPosition.x, -beginningPosition.y, -beginningPosition.z);
         }

         Vec3 startInWorld = entitypatch.<Animator>getAnimator().getVariables().getOrDefault(ActionAnimation.BEGINNING_LOCATION, self.getRealAnimation());
         if (startInWorld == null) {
            startInWorld = entitypatch.getOriginal().m_20182_();
         }

         Vec3 toTargetInWorld = destInWorld.m_82546_(startInWorld);
         float yRot = (float)Mth.m_14175_(MathUtils.getYRotOfVector(toTargetInWorld));
         Optional<AnimationProperty.YRotProvider> destYRotProvider = self.getRealAnimation()
            .get()
            .getProperty(AnimationProperty.ActionAnimationProperty.DEST_COORD_YROT_PROVIDER);
         float destYRot = destYRotProvider.isEmpty() ? yRot : destYRotProvider.get().get(self, entitypatch);
         TransformSheet result = self.getCoord()
            .transformToWorldCoordOriginAsDest(entitypatch, startInWorld, destInWorld, yRot, destYRot, startFrame, destFrame);
         transformSheet.readFrom(result);
      }
   };
   public static final MoveCoordFunctions.MoveCoordSetter TRACE_TARGET_DISTANCE = (self, entitypatch, transformSheet) -> {
      Vec3 destLocation = self.getRealAnimation()
         .get()
         .getProperty(AnimationProperty.ActionAnimationProperty.DEST_LOCATION_PROVIDER)
         .orElse(NO_DEST)
         .get(self, entitypatch);
      if (destLocation != null) {
         TransformSheet transform = self.getCoord().copyAll();
         Keyframe[] coord = transform.getKeyframes();
         Keyframe[] realAnimationCoord = self.getRealAnimation().get().getCoord().getKeyframes();
         Vec3 startInWorld = entitypatch.<Animator>getAnimator().getVariables().getOrDefault(ActionAnimation.BEGINNING_LOCATION, self.getRealAnimation());
         if (startInWorld == null) {
            startInWorld = entitypatch.getOriginal().m_20182_();
         }

         int startFrame = self.getRealAnimation().get().getProperty(AnimationProperty.ActionAnimationProperty.COORD_START_KEYFRAME_INDEX).orElse(0);
         int realAnimationEndFrame = self.getRealAnimation()
            .get()
            .getProperty(AnimationProperty.ActionAnimationProperty.COORD_DEST_KEYFRAME_INDEX)
            .orElse(self.getRealAnimation().get().getCoord().getKeyframes().length - 1);
         Vec3 toDestWorld = destLocation.m_82546_(startInWorld);
         Vec3f toDestAnim = realAnimationCoord[realAnimationEndFrame].transform().translation();
         LivingEntity attackTarget = entitypatch.getTarget();
         float entityRadius = 0.0F;
         if (attackTarget != null) {
            float reach = 0.0F;
            if (self.getRealAnimation().get() instanceof AttackAnimation attackAnimation) {
               Optional<Float> reachOpt = attackAnimation.getProperty(AnimationProperty.AttackAnimationProperty.REACH);
               if (reachOpt.isPresent()) {
                  reach = reachOpt.get();
               } else {
                  AnimationPlayer player = entitypatch.<Animator>getAnimator().getPlayerFor(self.getAccessor());
                  if (player != null) {
                     AttackAnimation.Phase phase = attackAnimation.getPhaseByTime(player.getElapsedTime());
                     reach = entitypatch.getReach(phase.hand);
                  }
               }
            }

            entityRadius = (attackTarget.m_20205_() + entitypatch.getOriginal().m_20205_()) * 0.7F + reach;
         }

         float worldLength = Math.max((float)toDestWorld.m_82553_() - entityRadius, 0.0F);
         float animLength = toDestAnim.length();
         float dot = entitypatch.<Animator>getAnimator().getVariables().getOrDefault(ActionAnimation.INITIAL_LOOK_VEC_DOT, self.getRealAnimation());
         float lookLength = Mth.m_14179_(dot, animLength, worldLength);
         float scale = Math.min(lookLength / animLength, 1.0F);
         if (self.isLinkAnimation()) {
            scale *= coord[coord.length - 1].transform().translation().length() / animLength;
         }

         int endFrame = self.getRealAnimation().get().getProperty(AnimationProperty.ActionAnimationProperty.COORD_DEST_KEYFRAME_INDEX).orElse(coord.length - 1);

         for (int i = startFrame; i <= endFrame; i++) {
            Vec3f translation = coord[i].transform().translation();
            translation.x *= scale;
            if (translation.z < 0.0F) {
               translation.z *= scale;
            }
         }

         transformSheet.readFrom(transform);
      } else {
         transformSheet.readFrom(self.getCoord().copyAll());
      }
   };
   public static final MoveCoordFunctions.MoveCoordSetter TRACE_TARGET_LOCATION_ROTATION = (self, entitypatch, transformSheet) -> {
      Vec3 destLocation = self.getRealAnimation()
         .get()
         .getProperty(AnimationProperty.ActionAnimationProperty.DEST_LOCATION_PROVIDER)
         .orElse(NO_DEST)
         .get(self, entitypatch);
      if (destLocation != null) {
         TransformSheet transform = self.getCoord().copyAll();
         Keyframe[] coord = transform.getKeyframes();
         Keyframe[] realAnimationCoord = self.getRealAnimation().get().getCoord().getKeyframes();
         Vec3 startInWorld = entitypatch.<Animator>getAnimator().getVariables().getOrDefault(ActionAnimation.BEGINNING_LOCATION, self.getRealAnimation());
         if (startInWorld == null) {
            startInWorld = entitypatch.getOriginal().m_20182_();
         }

         int startFrame = self.getRealAnimation().get().getProperty(AnimationProperty.ActionAnimationProperty.COORD_START_KEYFRAME_INDEX).orElse(0);
         int endFrame = self.isLinkAnimation()
            ? coord.length - 1
            : self.getRealAnimation().get().getProperty(AnimationProperty.ActionAnimationProperty.COORD_DEST_KEYFRAME_INDEX).orElse(coord.length - 1);
         Vec3 toDestWorld = destLocation.m_82546_(startInWorld);
         Vec3f toDestAnim = realAnimationCoord[endFrame].transform().translation();
         LivingEntity attackTarget = entitypatch.getTarget();
         float entityRadius = 0.0F;
         if (attackTarget != null) {
            float reach = 0.0F;
            if (self.getRealAnimation().get() instanceof AttackAnimation attackAnimation) {
               Optional<Float> reachOpt = attackAnimation.getProperty(AnimationProperty.AttackAnimationProperty.REACH);
               if (reachOpt.isPresent()) {
                  reach = reachOpt.get();
               } else {
                  AnimationPlayer player = entitypatch.<Animator>getAnimator().getPlayerFor(self.getAccessor());
                  if (player != null) {
                     AttackAnimation.Phase phase = attackAnimation.getPhaseByTime(player.getElapsedTime());
                     reach = entitypatch.getReach(phase.hand);
                  }
               }
            }

            entityRadius = (attackTarget.m_20205_() + entitypatch.getOriginal().m_20205_()) * 0.7F + reach;
         }

         float worldLength = Math.max((float)toDestWorld.m_82553_() - entityRadius, 0.0F);
         float animLength = toDestAnim.length();
         float scale = Math.min(worldLength / animLength, 1.0F);
         if (self.isLinkAnimation()) {
            scale *= coord[endFrame].transform().translation().length() / animLength;
         }

         for (int i = startFrame; i <= endFrame; i++) {
            Vec3f translation = coord[i].transform().translation();
            translation.x *= scale;
            if (translation.z < 0.0F) {
               translation.z *= scale;
            }
         }

         transformSheet.readFrom(transform);
      } else {
         transformSheet.readFrom(self.getCoord().copyAll());
      }
   };
   public static final MoveCoordFunctions.MoveCoordSetter VEX_TRACE = (self, entitypatch, transformSheet) -> {
      if (!self.isLinkAnimation()) {
         TransformSheet transform = self.getCoord().copyAll();
         if (entitypatch.getTarget() != null) {
            Keyframe[] keyframes = transform.getKeyframes();
            Vec3 pos = entitypatch.getOriginal().m_20182_();
            Vec3 targetpos = entitypatch.getTarget().m_146892_();
            double flyDistance = Math.max(5.0, targetpos.m_82546_(pos).m_82553_() * 2.0);
            transform.forEach(
               (index, keyframe) -> keyframe.transform()
                  .translation()
                  .scale((float)(flyDistance / Math.abs(keyframes[keyframes.length - 1].transform().translation().z)))
            );
            Vec3 toTarget = targetpos.m_82546_(pos);
            float xRot = (float)(-MathUtils.getXRotOfVector(toTarget));
            float yRot = (float)MathUtils.getYRotOfVector(toTarget);
            entitypatch.setYRot(yRot);
            transform.forEach((index, keyframe) -> {
               keyframe.transform().translation().rotateDegree(Vec3f.X_AXIS, xRot);
               keyframe.transform().translation().rotateDegree(Vec3f.Y_AXIS, 180.0F - yRot);
               keyframe.transform().translation().add(((LivingEntity)entitypatch.getOriginal()).m_20182_());
            });
            transformSheet.readFrom(transform);
         } else {
            transform.forEach((index, keyframe) -> {
               keyframe.transform().translation().rotateDegree(Vec3f.Y_AXIS, 180.0F - entitypatch.getYRot());
               keyframe.transform().translation().add(((LivingEntity)entitypatch.getOriginal()).m_20182_());
            });
         }
      }
   };

   @FunctionalInterface
   public interface MoveCoordGetter {
      Vec3f get(DynamicAnimation var1, LivingEntityPatch<?> var2, TransformSheet var3, float var4, float var5);
   }

   @FunctionalInterface
   public interface MoveCoordSetter {
      void set(DynamicAnimation var1, LivingEntityPatch<?> var2, TransformSheet var3);
   }
}
