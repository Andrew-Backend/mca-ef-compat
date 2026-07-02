package yesman.epicfight.api.animation.types;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationVariables;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class SelectiveAnimation extends StaticAnimation {
   public static final AnimationVariables.IndependentAnimationVariableKey<Integer> PREVIOUS_STATE = AnimationVariables.independent(animator -> 0, true);
   private final Function<LivingEntityPatch<?>, Integer> selector;
   private final List<AssetAccessor<? extends StaticAnimation>> animationsInEachState;

   @SafeVarargs
   public SelectiveAnimation(
      Function<LivingEntityPatch<?>, Integer> selector,
      AnimationManager.AnimationAccessor<? extends SelectiveAnimation> accessor,
      AssetAccessor<? extends StaticAnimation>... selectOptions
   ) {
      super(0.15F, false, accessor, null);
      this.selector = selector;
      this.animationsInEachState = List.of(selectOptions);

      for (AssetAccessor<? extends StaticAnimation> subAnimations : this.animationsInEachState) {
         subAnimations.get().addEvents(AnimationEvent.SimpleEvent.create((entitypatch, animation, params) -> {
            int currentStateId = this.selector.apply(entitypatch);
            Optional<Integer> prevState = entitypatch.<Animator>getAnimator().getVariables().get(PREVIOUS_STATE, this.getAccessor());
            prevState.ifPresentOrElse(prevStateId -> {
               if (prevStateId != currentStateId) {
                  entitypatch.<Animator>getAnimator().playAnimation(this.animationsInEachState.get(currentStateId), 0.0F);
                  entitypatch.<Animator>getAnimator().getVariables().put(PREVIOUS_STATE, this.getAccessor(), currentStateId);
               }
            }, () -> {
               entitypatch.<Animator>getAnimator().playAnimation(this.animationsInEachState.get(0), 0.0F);
               entitypatch.<Animator>getAnimator().getVariables().put(PREVIOUS_STATE, this.getAccessor(), 0);
            });
         }, AnimationEvent.Side.BOTH));
      }
   }

   @Override
   public void begin(LivingEntityPatch<?> entitypatch) {
      super.begin(entitypatch);
      int result = this.selector.apply(entitypatch);
      entitypatch.<Animator>getAnimator().getVariables().put(PREVIOUS_STATE, this.getAccessor(), result);
      entitypatch.<Animator>getAnimator().playAnimation(this.animationsInEachState.get(result), 0.0F);
   }

   @Override
   public void tick(LivingEntityPatch<?> entitypatch) {
      super.tick(entitypatch);
   }

   @Override
   public boolean isMetaAnimation() {
      return true;
   }

   @Override
   public List<AssetAccessor<? extends StaticAnimation>> getSubAnimations() {
      return this.animationsInEachState;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public Layer.Priority getPriority() {
      return this.animationsInEachState.get(0).get().getPriority();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public Layer.LayerType getLayerType() {
      return this.animationsInEachState.get(0).get().getLayerType();
   }
}
