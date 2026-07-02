package yesman.epicfight.api.animation.property;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.RegistryObject;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.physics.ik.InverseKinematicsSimulator;
import yesman.epicfight.api.utils.HitEntityList;
import yesman.epicfight.api.utils.TimePairList;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.ExtraDamageInstance;
import yesman.epicfight.world.damagesource.StunType;

public abstract class AnimationProperty<T> {
   private static final Map<String, AnimationProperty<?>> SERIALIZABLE_ANIMATION_PROPERTY_KEYS = Maps.newHashMap();
   private final Codec<T> codecs;
   private final String name;

   public static <T> AnimationProperty<T> getSerializableProperty(String name) {
      if (!SERIALIZABLE_ANIMATION_PROPERTY_KEYS.containsKey(name)) {
         throw new IllegalStateException("No property key named " + name);
      } else {
         return (AnimationProperty<T>)SERIALIZABLE_ANIMATION_PROPERTY_KEYS.get(name);
      }
   }

   public AnimationProperty(String name, @Nullable Codec<T> codecs) {
      this.codecs = codecs;
      this.name = name;
      if (name != null) {
         if (SERIALIZABLE_ANIMATION_PROPERTY_KEYS.containsKey(name)) {
            throw new IllegalStateException("Animation property key " + name + " is already registered.");
         }

         SERIALIZABLE_ANIMATION_PROPERTY_KEYS.put(name, this);
      }
   }

   public AnimationProperty(String name) {
      this(name, null);
   }

   public T parseFrom(JsonElement e) {
      return (T)this.codecs
         .parse(JsonOps.INSTANCE, e)
         .resultOrPartial(errm -> EpicFightMod.LOGGER.warn("Failed to parse property " + this.name + " because of " + errm))
         .orElseThrow();
   }

   public Codec<T> getCodecs() {
      return this.codecs;
   }

   public static class ActionAnimationProperty<T> extends AnimationProperty.StaticAnimationProperty<T> {
      public static final AnimationProperty.ActionAnimationProperty<Boolean> STOP_MOVEMENT = new AnimationProperty.ActionAnimationProperty<>(
         "stop_movements", Codec.BOOL
      );
      public static final AnimationProperty.ActionAnimationProperty<Boolean> REMOVE_DELTA_MOVEMENT = new AnimationProperty.ActionAnimationProperty<>(
         "revmoe_delta_move", Codec.BOOL
      );
      public static final AnimationProperty.ActionAnimationProperty<Boolean> MOVE_VERTICAL = new AnimationProperty.ActionAnimationProperty<>(
         "move_vertically", Codec.BOOL
      );
      public static final AnimationProperty.ActionAnimationProperty<TimePairList> NO_GRAVITY_TIME = new AnimationProperty.ActionAnimationProperty<>();
      public static final AnimationProperty.ActionAnimationProperty<TransformSheet> COORD = new AnimationProperty.ActionAnimationProperty<>();
      public static final AnimationProperty.ActionAnimationProperty<Boolean> MOVE_ON_LINK = new AnimationProperty.ActionAnimationProperty<>(
         "move_during_link", Codec.BOOL
      );
      public static final AnimationProperty.ActionAnimationProperty<TimePairList> MOVE_TIME = new AnimationProperty.ActionAnimationProperty<>();
      public static final AnimationProperty.ActionAnimationProperty<MoveCoordFunctions.MoveCoordSetter> COORD_SET_BEGIN = new AnimationProperty.ActionAnimationProperty<>();
      public static final AnimationProperty.ActionAnimationProperty<MoveCoordFunctions.MoveCoordSetter> COORD_SET_TICK = new AnimationProperty.ActionAnimationProperty<>();
      public static final AnimationProperty.ActionAnimationProperty<MoveCoordFunctions.MoveCoordGetter> COORD_GET = new AnimationProperty.ActionAnimationProperty<>();
      public static final AnimationProperty.ActionAnimationProperty<Boolean> AFFECT_SPEED = new AnimationProperty.ActionAnimationProperty<>(
         "move_speed_based_distance", Codec.BOOL
      );
      public static final AnimationProperty.ActionAnimationProperty<Boolean> CANCELABLE_MOVE = new AnimationProperty.ActionAnimationProperty<>(
         "cancellable_movement", Codec.BOOL
      );
      public static final AnimationProperty.ActionAnimationProperty<Boolean> IS_DEATH_ANIMATION = new AnimationProperty.ActionAnimationProperty<>(
         "is_death", Codec.BOOL
      );
      public static final AnimationProperty.ActionAnimationProperty<TimePairList> COORD_UPDATE_TIME = new AnimationProperty.ActionAnimationProperty<>();
      public static final AnimationProperty.ActionAnimationProperty<Boolean> RESET_PLAYER_COMBO_COUNTER = new AnimationProperty.ActionAnimationProperty<>(
         "reset_combo_attack_counter", Codec.BOOL
      );
      public static final AnimationProperty.ActionAnimationProperty<AnimationProperty.DestLocationProvider> DEST_LOCATION_PROVIDER = new AnimationProperty.ActionAnimationProperty<>();
      public static final AnimationProperty.ActionAnimationProperty<AnimationProperty.YRotProvider> ENTITY_YROT_PROVIDER = new AnimationProperty.ActionAnimationProperty<>();
      public static final AnimationProperty.ActionAnimationProperty<AnimationProperty.YRotProvider> DEST_COORD_YROT_PROVIDER = new AnimationProperty.ActionAnimationProperty<>();
      public static final AnimationProperty.ActionAnimationProperty<Integer> COORD_START_KEYFRAME_INDEX = new AnimationProperty.ActionAnimationProperty<>();
      public static final AnimationProperty.ActionAnimationProperty<Integer> COORD_DEST_KEYFRAME_INDEX = new AnimationProperty.ActionAnimationProperty<>();
      public static final AnimationProperty.ActionAnimationProperty<Boolean> SYNC_CAMERA = new AnimationProperty.ActionAnimationProperty<>(
         "sync_camera", Codec.BOOL
      );

      public ActionAnimationProperty(String rl, @Nullable Codec<T> codecs) {
         super(rl, codecs);
      }

      public ActionAnimationProperty() {
         this(null, null);
      }
   }

   public static class AttackAnimationProperty<T> extends AnimationProperty.ActionAnimationProperty<T> {
      public static final AnimationProperty.AttackAnimationProperty<Boolean> FIXED_MOVE_DISTANCE = new AnimationProperty.AttackAnimationProperty<>(
         "fixed_movement_distance", Codec.BOOL
      );
      public static final AnimationProperty.AttackAnimationProperty<Float> ATTACK_SPEED_FACTOR = new AnimationProperty.AttackAnimationProperty<>(
         "attack_speed_factor", Codec.FLOAT
      );
      public static final AnimationProperty.AttackAnimationProperty<Float> BASIS_ATTACK_SPEED = new AnimationProperty.AttackAnimationProperty<>(
         "basis_attack_speed", Codec.FLOAT
      );
      public static final AnimationProperty.AttackAnimationProperty<Integer> EXTRA_COLLIDERS = new AnimationProperty.AttackAnimationProperty<>(
         "extra_colliders", Codec.INT
      );
      public static final AnimationProperty.AttackAnimationProperty<Float> REACH = new AnimationProperty.AttackAnimationProperty<>("reach", Codec.FLOAT);

      public AttackAnimationProperty(String rl, @Nullable Codec<T> codecs) {
         super(rl, codecs);
      }

      public AttackAnimationProperty() {
         this(null, null);
      }
   }

   public static class AttackPhaseProperty<T> {
      public static final AnimationProperty.AttackPhaseProperty<ValueModifier> MAX_STRIKES_MODIFIER = new AnimationProperty.AttackPhaseProperty<>(
         "max_strikes", ValueModifier.CODEC
      );
      public static final AnimationProperty.AttackPhaseProperty<ValueModifier> DAMAGE_MODIFIER = new AnimationProperty.AttackPhaseProperty<>(
         "damage", ValueModifier.CODEC
      );
      public static final AnimationProperty.AttackPhaseProperty<ValueModifier> ARMOR_NEGATION_MODIFIER = new AnimationProperty.AttackPhaseProperty<>(
         "armor_negation", ValueModifier.CODEC
      );
      public static final AnimationProperty.AttackPhaseProperty<ValueModifier> IMPACT_MODIFIER = new AnimationProperty.AttackPhaseProperty<>(
         "impact", ValueModifier.CODEC
      );
      public static final AnimationProperty.AttackPhaseProperty<Set<ExtraDamageInstance>> EXTRA_DAMAGE = new AnimationProperty.AttackPhaseProperty<>();
      public static final AnimationProperty.AttackPhaseProperty<StunType> STUN_TYPE = new AnimationProperty.AttackPhaseProperty<>();
      public static final AnimationProperty.AttackPhaseProperty<SoundEvent> SWING_SOUND = new AnimationProperty.AttackPhaseProperty<>();
      public static final AnimationProperty.AttackPhaseProperty<SoundEvent> HIT_SOUND = new AnimationProperty.AttackPhaseProperty<>();
      public static final AnimationProperty.AttackPhaseProperty<RegistryObject<HitParticleType>> PARTICLE = new AnimationProperty.AttackPhaseProperty<>();
      public static final AnimationProperty.AttackPhaseProperty<HitEntityList.Priority> HIT_PRIORITY = new AnimationProperty.AttackPhaseProperty<>();
      public static final AnimationProperty.AttackPhaseProperty<Set<TagKey<DamageType>>> SOURCE_TAG = new AnimationProperty.AttackPhaseProperty<>();
      public static final AnimationProperty.AttackPhaseProperty<Function<LivingEntityPatch<?>, Vec3>> SOURCE_LOCATION_PROVIDER = new AnimationProperty.AttackPhaseProperty<>();

      public AttackPhaseProperty(String rl, @Nullable Codec<? extends T> codecs) {
      }

      public AttackPhaseProperty() {
      }
   }

   @FunctionalInterface
   public interface DestLocationProvider {
      Vec3 get(DynamicAnimation var1, LivingEntityPatch<?> var2);
   }

   @FunctionalInterface
   public interface PlaybackSpeedModifier {
      float modify(DynamicAnimation var1, LivingEntityPatch<?> var2, float var3, float var4, float var5);
   }

   @FunctionalInterface
   public interface PlaybackTimeModifier {
      Pair<Float, Float> modify(DynamicAnimation var1, LivingEntityPatch<?> var2, float var3, float var4, float var5);
   }

   @FunctionalInterface
   public interface PoseModifier {
      void modify(DynamicAnimation var1, Pose var2, LivingEntityPatch<?> var3, float var4, float var5);
   }

   @FunctionalInterface
   public interface Registerer<T> {
      void register(Map<AnimationProperty<T>, Object> var1, AnimationProperty<T> var2, T var3);
   }

   public static class StaticAnimationProperty<T> extends AnimationProperty<T> {
      public static final AnimationProperty.StaticAnimationProperty<List<AnimationEvent<?, ?>>> TICK_EVENTS = new AnimationProperty.StaticAnimationProperty<>();
      public static final AnimationProperty.StaticAnimationProperty<List<AnimationEvent.SimpleEvent<?>>> ON_BEGIN_EVENTS = new AnimationProperty.StaticAnimationProperty<>();
      public static final AnimationProperty.StaticAnimationProperty<List<AnimationEvent.SimpleEvent<?>>> ON_END_EVENTS = new AnimationProperty.StaticAnimationProperty<>();
      public static final AnimationProperty.StaticAnimationProperty<AnimationEvent.SimpleEvent<AnimationEvent.E2<CapabilityItem, CapabilityItem>>> ON_ITEM_CHANGE_EVENT = new AnimationProperty.StaticAnimationProperty<>();
      public static final AnimationProperty.StaticAnimationProperty<AnimationProperty.PlaybackSpeedModifier> PLAY_SPEED_MODIFIER = new AnimationProperty.StaticAnimationProperty<>();
      public static final AnimationProperty.StaticAnimationProperty<AnimationProperty.PlaybackTimeModifier> ELAPSED_TIME_MODIFIER = new AnimationProperty.StaticAnimationProperty<>();
      public static final AnimationProperty.StaticAnimationProperty<AnimationProperty.PoseModifier> POSE_MODIFIER = new AnimationProperty.StaticAnimationProperty<>();
      public static final AnimationProperty.StaticAnimationProperty<Boolean> FIXED_HEAD_ROTATION = new AnimationProperty.StaticAnimationProperty<>();
      public static final AnimationProperty.StaticAnimationProperty<Map<ResourceLocation, AnimationManager.AnimationAccessor<? extends StaticAnimation>>> TRANSITION_ANIMATIONS_FROM = new AnimationProperty.StaticAnimationProperty<>();
      public static final AnimationProperty.StaticAnimationProperty<Map<ResourceLocation, AnimationManager.AnimationAccessor<? extends StaticAnimation>>> TRANSITION_ANIMATIONS_TO = new AnimationProperty.StaticAnimationProperty<>();
      public static final AnimationProperty.StaticAnimationProperty<Boolean> NO_PHYSICS = new AnimationProperty.StaticAnimationProperty<>(
         "no_physics", Codec.BOOL
      );
      public static final AnimationProperty.StaticAnimationProperty<List<InverseKinematicsSimulator.InverseKinematicsDefinition>> IK_DEFINITION = new AnimationProperty.StaticAnimationProperty<>();
      public static final AnimationProperty.StaticAnimationProperty<List<InverseKinematicsSimulator.BakedInverseKinematicsDefinition>> BAKED_IK_DEFINITION = new AnimationProperty.StaticAnimationProperty<>();
      public static final AnimationProperty.StaticAnimationProperty<LivingMotion> RESET_LIVING_MOTION = new AnimationProperty.StaticAnimationProperty<>();

      public StaticAnimationProperty(String rl, @Nullable Codec<T> codecs) {
         super(rl, codecs);
      }

      public StaticAnimationProperty() {
         this(null, null);
      }
   }

   @FunctionalInterface
   public interface YRotProvider {
      float get(DynamicAnimation var1, LivingEntityPatch<?> var2);
   }
}
