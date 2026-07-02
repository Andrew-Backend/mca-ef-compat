package yesman.epicfight.api.animation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import org.checkerframework.checker.nullness.qual.NonNull;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.api.utils.datastruct.TypeFlexibleHashMap;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.network.common.AnimationVariablePacket;

public class AnimationVariables {
   protected final Animator animator;
   protected final TypeFlexibleHashMap<AnimationVariables.AnimationVariableKey<?>> animationVariables = new TypeFlexibleHashMap<>(false);

   public AnimationVariables(Animator animator) {
      this.animator = animator;
   }

   public <T> Optional<T> getSharedVariable(AnimationVariables.SharedAnimationVariableKey<T> key) {
      return Optional.ofNullable(this.animationVariables.get(key));
   }

   public <T> T getOrDefaultSharedVariable(AnimationVariables.SharedAnimationVariableKey<T> key) {
      return ParseUtil.orElse(this.animationVariables.get(key), () -> (T)key.defaultValue(this.animator));
   }

   public <T> Optional<T> get(AnimationVariables.IndependentAnimationVariableKey<T> key, AssetAccessor<? extends StaticAnimation> animation) {
      if (animation == null) {
         return Optional.empty();
      }

      Map<ResourceLocation, Object> subMap = this.animationVariables.get(key);
      return subMap == null ? Optional.empty() : Optional.ofNullable((T)subMap.get(animation.registryName()));
   }

   public <T> T getOrDefault(AnimationVariables.IndependentAnimationVariableKey<T> key, AssetAccessor<? extends StaticAnimation> animation) {
      if (animation == null) {
         return Objects.requireNonNull((T)key.defaultValue(this.animator), "Null value returned by default provider.");
      }

      Map<ResourceLocation, Object> subMap = this.animationVariables.get(key);
      return subMap == null
         ? Objects.requireNonNull((T)key.defaultValue(this.animator), "Null value returned by default provider.")
         : ParseUtil.orElse((T)subMap.get(animation.registryName()), () -> (T)key.defaultValue(this.animator));
   }

   public <T> void putDefaultSharedVariable(AnimationVariables.SharedAnimationVariableKey<T> key) {
      T value = (T)key.defaultValue(this.animator);
      Objects.requireNonNull(value, "Null value returned by default provider.");
      this.putSharedVariable(key, value);
   }

   public <T> void putSharedVariable(AnimationVariables.SharedAnimationVariableKey<T> key, T value) {
      this.putSharedVariable(key, value, true);
   }

   @Deprecated
   public <T> void putSharedVariable(AnimationVariables.SharedAnimationVariableKey<T> key, T value, boolean synchronize) {
      if (this.animationVariables.containsKey(key) && !key.mutable()) {
         throw new UnsupportedOperationException("Can't modify a const variable");
      }

      this.animationVariables.put(key, value);
      if (synchronize && key instanceof SynchedAnimationVariableKey<T> synchedanimationvariablekey) {
         synchedanimationvariablekey.sync(this.animator.entitypatch, (AssetAccessor<? extends StaticAnimation>)null, value, AnimationVariablePacket.Action.PUT);
      }
   }

   public <T> void putDefaultValue(AnimationVariables.IndependentAnimationVariableKey<T> key, AssetAccessor<? extends StaticAnimation> animation) {
      T value = (T)key.defaultValue(this.animator);
      Objects.requireNonNull(value, "Null value returned by default provider.");
      this.put(key, animation, value);
   }

   public <T> void put(AnimationVariables.IndependentAnimationVariableKey<T> key, AssetAccessor<? extends StaticAnimation> animation, T value) {
      this.put(key, animation, value, true);
   }

   @Deprecated
   public <T> void put(
      AnimationVariables.IndependentAnimationVariableKey<T> key, AssetAccessor<? extends StaticAnimation> animation, T value, boolean synchronize
   ) {
      if (animation != Animations.EMPTY_ANIMATION) {
         this.animationVariables.computeIfPresent(key, (k, v) -> {
            Map<ResourceLocation, Object> variablesByAnimations = (Map<ResourceLocation, Object>)v;
            if (!key.mutable() && variablesByAnimations.containsKey(animation.registryName())) {
               throw new UnsupportedOperationException("Can't modify a const variable");
            }

            variablesByAnimations.put(animation.registryName(), value);
            return (Object)v;
         });
         this.animationVariables.computeIfAbsent(key, k -> new HashMap<>(Map.of(animation.registryName(), value)));
         if (synchronize && key instanceof SynchedAnimationVariableKey<T> synchedanimationvariablekey) {
            synchedanimationvariablekey.sync(this.animator.entitypatch, animation, value, AnimationVariablePacket.Action.PUT);
         }
      }
   }

   public <T> T removeSharedVariable(AnimationVariables.SharedAnimationVariableKey<T> key) {
      return this.removeSharedVariable(key, true);
   }

   @Deprecated
   public <T> T removeSharedVariable(AnimationVariables.SharedAnimationVariableKey<T> key, boolean synchronize) {
      if (!key.mutable()) {
         throw new UnsupportedOperationException("Can't remove a const variable");
      }

      if (synchronize && key instanceof SynchedAnimationVariableKey<T> synchedanimationvariablekey) {
         synchedanimationvariablekey.sync(this.animator.entitypatch, null, null, AnimationVariablePacket.Action.REMOVE);
      }

      return (T)this.animationVariables.remove(key);
   }

   public void removeAll(AnimationManager.AnimationAccessor<? extends StaticAnimation> animation) {
      if (animation != Animations.EMPTY_ANIMATION) {
         for (Entry<AnimationVariables.AnimationVariableKey<?>, Object> entry : this.animationVariables.entrySet()) {
            if (!entry.getKey().isSharedKey()) {
               Map<ResourceLocation, Object> map = (Map<ResourceLocation, Object>)entry.getValue();
               if (map != null) {
                  map.remove(animation.registryName());
               }
            }
         }
      }
   }

   public void remove(AnimationVariables.IndependentAnimationVariableKey<?> key, AssetAccessor<? extends StaticAnimation> animation) {
      this.remove(key, animation, true);
   }

   @Deprecated
   public void remove(AnimationVariables.IndependentAnimationVariableKey<?> key, AssetAccessor<? extends StaticAnimation> animation, boolean synchronize) {
      if (animation != Animations.EMPTY_ANIMATION) {
         Map<ResourceLocation, Object> map = this.animationVariables.get(key);
         if (map != null) {
            map.remove(animation.registryName());
         }

         if (synchronize && key instanceof SynchedAnimationVariableKey<?> synchedanimationvariablekey) {
            synchedanimationvariablekey.sync(this.animator.entitypatch, null, null, AnimationVariablePacket.Action.REMOVE);
         }
      }
   }

   public static <T> AnimationVariables.SharedAnimationVariableKey<T> shared(Function<Animator, T> defaultValueSupplier, boolean mutable) {
      return new AnimationVariables.SharedAnimationVariableKey<>(defaultValueSupplier, mutable);
   }

   public static <T> AnimationVariables.IndependentAnimationVariableKey<T> independent(Function<Animator, T> defaultValueSupplier, boolean mutable) {
      return new AnimationVariables.IndependentAnimationVariableKey<>(defaultValueSupplier, mutable);
   }

   protected abstract static class AnimationVariableKey<T> implements TypeFlexibleHashMap.TypeKey<T> {
      protected final Function<Animator, T> defaultValueSupplier;
      protected final boolean mutable;

      protected AnimationVariableKey(Function<Animator, T> defaultValueSupplier, boolean mutable) {
         this.defaultValueSupplier = defaultValueSupplier;
         this.mutable = mutable;
      }

      public @NonNull T defaultValue(Animator animator) {
         return this.defaultValueSupplier.apply(animator);
      }

      public boolean mutable() {
         return this.mutable;
      }

      @Override
      public T defaultValue() {
         throw new UnsupportedOperationException("Use defaultValue(Animator animator) to get default value of animation variable key");
      }

      public abstract boolean isSharedKey();

      public abstract boolean isSynched();
   }

   public static class IndependentAnimationVariableKey<T> extends AnimationVariables.AnimationVariableKey<T> {
      protected IndependentAnimationVariableKey(Function<Animator, T> initValueSupplier, boolean mutable) {
         super(initValueSupplier, mutable);
      }

      @Override
      public boolean isSharedKey() {
         return false;
      }

      @Override
      public boolean isSynched() {
         return false;
      }
   }

   public static class SharedAnimationVariableKey<T> extends AnimationVariables.AnimationVariableKey<T> {
      protected SharedAnimationVariableKey(Function<Animator, T> initValueSupplier, boolean mutable) {
         super(initValueSupplier, mutable);
      }

      @Override
      public boolean isSharedKey() {
         return true;
      }

      @Override
      public boolean isSynched() {
         return false;
      }
   }
}
