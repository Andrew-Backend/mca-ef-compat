package yesman.epicfight.world.entity.eventlistener;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.MainFrameAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public class ComboCounterHandleEvent extends AbstractPlayerEvent<ServerPlayerPatch> {
   private final ComboCounterHandleEvent.Causal causal;
   private final AnimationManager.AnimationAccessor<? extends StaticAnimation> animation;
   private final int prevValue;
   private int nextValue;

   public ComboCounterHandleEvent(
      ComboCounterHandleEvent.Causal causal,
      ServerPlayerPatch playerpatch,
      AnimationManager.AnimationAccessor<? extends StaticAnimation> animation,
      int prevValue,
      int nextValue
   ) {
      super(playerpatch, true);
      this.causal = causal;
      this.animation = animation;
      this.prevValue = prevValue;
      this.nextValue = nextValue;
   }

   public ComboCounterHandleEvent.Causal getCausal() {
      return this.causal;
   }

   public AnimationManager.AnimationAccessor<? extends StaticAnimation> getAnimation() {
      return this.animation;
   }

   public int getPrevValue() {
      return this.prevValue;
   }

   public int getNextValue() {
      return this.nextValue;
   }

   public void setNextValue(int nextValue) {
      this.nextValue = nextValue;
   }

   public enum Causal {
      ANOTHER_ACTION_ANIMATION,
      TIME_EXPIRED;
   }

   @FunctionalInterface
   public interface ComboCounterHandler {
      ComboCounterHandleEvent.ComboCounterHandler DEFAULT_COMBO_HANDLER = (itemCapability, causal, entitypatch, nextAnimation, comboCounter) -> {
         if (causal == ComboCounterHandleEvent.Causal.TIME_EXPIRED) {
            return 0;
         }

         List<AnimationManager.AnimationAccessor<? extends AttackAnimation>> comboAnimations = itemCapability.getAutoAttackMotion(entitypatch);
         if (comboAnimations == null) {
            return 0;
         }

         Set<AnimationManager.AnimationAccessor<? extends AttackAnimation>> attackMotionSet = Sets.newHashSet(comboAnimations);
         if (!attackMotionSet.contains(nextAnimation) && causal != ComboCounterHandleEvent.Causal.TIME_EXPIRED && itemCapability.shouldCancelCombo(entitypatch)
            )
          {
            return 0;
         }

         int comboSize = comboAnimations.size();
         return !nextAnimation.equals(comboAnimations.get(comboSize - 1)) && !nextAnimation.equals(comboAnimations.get(comboSize - 2)) ? comboCounter : 0;
      };
      Function<Class<? extends StaticAnimation>, ComboCounterHandleEvent.ComboCounterHandler> NO_RESET_WITH_ANIM_TYPE = Util.m_143827_(
         animType -> (itemCapability, causal, entitypatch, nextAnimation, comboCounter) -> {
            if (causal == ComboCounterHandleEvent.Causal.TIME_EXPIRED) {
               return 0;
            }

            List<AnimationManager.AnimationAccessor<? extends AttackAnimation>> comboAnimations = itemCapability.getAutoAttackMotion(entitypatch);
            if (comboAnimations == null) {
               return 0;
            }

            Set<AnimationManager.AnimationAccessor<? extends AttackAnimation>> attackMotionSet = Sets.newHashSet(comboAnimations);
            if (!nextAnimation.checkType(animType) && !attackMotionSet.contains(nextAnimation)) {
               return 0;
            }

            int comboSize = comboAnimations.size();
            return !nextAnimation.equals(comboAnimations.get(comboSize - 1)) && !nextAnimation.equals(comboAnimations.get(comboSize - 2)) ? comboCounter : 0;
         }
      );

      int handleComboCounter(
         CapabilityItem var1,
         ComboCounterHandleEvent.Causal var2,
         PlayerPatch<?> var3,
         @Nullable AnimationManager.AnimationAccessor<? extends MainFrameAnimation> var4,
         int var5
      );
   }
}
