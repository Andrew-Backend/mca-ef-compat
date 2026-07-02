package yesman.epicfight.gameasset;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.RegistryObject;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import yesman.epicfight.api.animation.AnimationClip;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationVariables;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Keyframe;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.SynchedAnimationVariableKeys;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationParameters;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.property.MoveCoordFunctions;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.types.AimAnimation;
import yesman.epicfight.api.animation.types.AirSlashAnimation;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.BasicAttackAnimation;
import yesman.epicfight.api.animation.types.DashAttackAnimation;
import yesman.epicfight.api.animation.types.DirectStaticAnimation;
import yesman.epicfight.api.animation.types.DodgeAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.GuardAnimation;
import yesman.epicfight.api.animation.types.HitAnimation;
import yesman.epicfight.api.animation.types.InvincibleAnimation;
import yesman.epicfight.api.animation.types.KnockdownAnimation;
import yesman.epicfight.api.animation.types.LongHitAnimation;
import yesman.epicfight.api.animation.types.MirrorAnimation;
import yesman.epicfight.api.animation.types.MountAttackAnimation;
import yesman.epicfight.api.animation.types.MovementAnimation;
import yesman.epicfight.api.animation.types.OffAnimation;
import yesman.epicfight.api.animation.types.RangedAttackAnimation;
import yesman.epicfight.api.animation.types.ReboundAnimation;
import yesman.epicfight.api.animation.types.SelectiveAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.animation.types.grappling.GrapplingAttackAnimation;
import yesman.epicfight.api.animation.types.grappling.GrapplingTryAnimation;
import yesman.epicfight.api.animation.types.procedural.EnderDragonActionAnimation;
import yesman.epicfight.api.animation.types.procedural.EnderDragonAttackAnimation;
import yesman.epicfight.api.animation.types.procedural.EnderDragonDeathAnimation;
import yesman.epicfight.api.animation.types.procedural.EnderDragonDynamicActionAnimation;
import yesman.epicfight.api.animation.types.procedural.EnderDragonWalkAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.collider.OBBCollider;
import yesman.epicfight.api.physics.ik.InverseKinematicsSimulator;
import yesman.epicfight.api.utils.HitEntityList;
import yesman.epicfight.api.utils.LevelUtil;
import yesman.epicfight.api.utils.TimePairList;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.QuaternionUtils;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.model.armature.types.ToolHolderArmature;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.identity.MeteorSlamSkill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.boss.WitherPatch;
import yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon.DragonGroundBattlePhase;
import yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon.DragonLandingPhase;
import yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon.EnderDragonPatch;
import yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon.PatchedPhases;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.ExtraDamageInstance;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;

@EventBusSubscriber(modid = "epicfight", bus = Bus.MOD)
public class Animations {
   public static DirectStaticAnimation EMPTY_ANIMATION = new DirectStaticAnimation() {
      public static final ResourceLocation EMPTY_ANIMATION_REGISTRY_NAME = ResourceLocation.fromNamespaceAndPath("epicfight", "empty");

      @Override
      public void loadAnimation() {
      }

      @Override
      public AnimationClip getAnimationClip() {
         return AnimationClip.EMPTY_CLIP;
      }

      @Override
      public ResourceLocation registryName() {
         return EMPTY_ANIMATION_REGISTRY_NAME;
      }
   };
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_IDLE;
   public static AnimationManager.AnimationAccessor<MovementAnimation> BIPED_WALK;
   public static AnimationManager.AnimationAccessor<MovementAnimation> BIPED_RUN;
   public static AnimationManager.AnimationAccessor<MovementAnimation> BIPED_SNEAK;
   public static AnimationManager.AnimationAccessor<MovementAnimation> BIPED_SWIM;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_FLOAT;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_KNEEL;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_FALL;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_FLYING;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_CREATIVE_IDLE;
   public static AnimationManager.AnimationAccessor<SelectiveAnimation> BIPED_CREATIVE_FLYING;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_MOUNT;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_SIT;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_JUMP;
   public static AnimationManager.AnimationAccessor<LongHitAnimation> BIPED_DEATH;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_DIG_MAINHAND;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_DIG_OFFHAND;
   public static AnimationManager.AnimationAccessor<SelectiveAnimation> BIPED_DIG;
   public static AnimationManager.AnimationAccessor<MovementAnimation> BIPED_RUN_SPEAR;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_HOLD_GREATSWORD;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_HOLD_UCHIGATANA_SHEATHING;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_HOLD_UCHIGATANA;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_HOLD_TACHI;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_HOLD_LONGSWORD;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_HOLD_LIECHTENAUER;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_HOLD_SPEAR;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_HOLD_DUAL_WEAPON;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_HOLD_CROSSBOW;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_HOLD_MAP_TWOHAND;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_HOLD_MAP_OFFHAND;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_HOLD_MAP_MAINHAND;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_HOLD_MAP_TWOHAND_MOVE;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_HOLD_MAP_OFFHAND_MOVE;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_HOLD_MAP_MAINHAND_MOVE;
   public static AnimationManager.AnimationAccessor<MovementAnimation> BIPED_WALK_GREATSWORD;
   public static AnimationManager.AnimationAccessor<MovementAnimation> BIPED_WALK_SPEAR;
   public static AnimationManager.AnimationAccessor<MovementAnimation> BIPED_WALK_UCHIGATANA_SHEATHING;
   public static AnimationManager.AnimationAccessor<MovementAnimation> BIPED_WALK_UCHIGATANA;
   public static AnimationManager.AnimationAccessor<MovementAnimation> BIPED_WALK_TWOHAND;
   public static AnimationManager.AnimationAccessor<MovementAnimation> BIPED_WALK_LONGSWORD;
   public static AnimationManager.AnimationAccessor<MovementAnimation> BIPED_WALK_LIECHTENAUER;
   public static AnimationManager.AnimationAccessor<MovementAnimation> BIPED_RUN_GREATSWORD;
   public static AnimationManager.AnimationAccessor<MovementAnimation> BIPED_RUN_UCHIGATANA;
   public static AnimationManager.AnimationAccessor<MovementAnimation> BIPED_RUN_UCHIGATANA_SHEATHING;
   public static AnimationManager.AnimationAccessor<MovementAnimation> BIPED_RUN_DUAL;
   public static AnimationManager.AnimationAccessor<MovementAnimation> BIPED_RUN_LONGSWORD;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_UCHIGATANA_SCRAP;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_LIECHTENAUER_READY;
   public static AnimationManager.AnimationAccessor<MirrorAnimation> BIPED_HIT_SHIELD;
   public static AnimationManager.AnimationAccessor<MovementAnimation> BIPED_CLIMBING;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_SLEEPING;
   public static AnimationManager.AnimationAccessor<AimAnimation> BIPED_BOW_AIM;
   public static AnimationManager.AnimationAccessor<ReboundAnimation> BIPED_BOW_SHOT;
   public static AnimationManager.AnimationAccessor<MirrorAnimation> BIPED_DRINK;
   public static AnimationManager.AnimationAccessor<MirrorAnimation> BIPED_EAT;
   public static AnimationManager.AnimationAccessor<MirrorAnimation> BIPED_SPYGLASS_USE;
   public static AnimationManager.AnimationAccessor<AimAnimation> BIPED_CROSSBOW_AIM;
   public static AnimationManager.AnimationAccessor<ReboundAnimation> BIPED_CROSSBOW_SHOT;
   public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_CROSSBOW_RELOAD;
   public static AnimationManager.AnimationAccessor<AimAnimation> BIPED_JAVELIN_AIM;
   public static AnimationManager.AnimationAccessor<ReboundAnimation> BIPED_JAVELIN_THROW;
   public static AnimationManager.AnimationAccessor<HitAnimation> BIPED_HIT_SHORT;
   public static AnimationManager.AnimationAccessor<LongHitAnimation> BIPED_HIT_LONG;
   public static AnimationManager.AnimationAccessor<LongHitAnimation> BIPED_HIT_ON_MOUNT;
   public static AnimationManager.AnimationAccessor<LongHitAnimation> BIPED_LANDING;
   public static AnimationManager.AnimationAccessor<KnockdownAnimation> BIPED_KNOCKDOWN;
   public static AnimationManager.AnimationAccessor<MirrorAnimation> BIPED_BLOCK;
   public static AnimationManager.AnimationAccessor<DodgeAnimation> BIPED_ROLL_FORWARD;
   public static AnimationManager.AnimationAccessor<DodgeAnimation> BIPED_ROLL_BACKWARD;
   public static AnimationManager.AnimationAccessor<DodgeAnimation> BIPED_STEP_FORWARD;
   public static AnimationManager.AnimationAccessor<DodgeAnimation> BIPED_STEP_BACKWARD;
   public static AnimationManager.AnimationAccessor<DodgeAnimation> BIPED_STEP_LEFT;
   public static AnimationManager.AnimationAccessor<DodgeAnimation> BIPED_STEP_RIGHT;
   public static AnimationManager.AnimationAccessor<DodgeAnimation> BIPED_KNOCKDOWN_WAKEUP_LEFT;
   public static AnimationManager.AnimationAccessor<DodgeAnimation> BIPED_KNOCKDOWN_WAKEUP_RIGHT;
   public static AnimationManager.AnimationAccessor<ActionAnimation> BIPED_DEMOLITION_LEAP_CHARGING;
   public static AnimationManager.AnimationAccessor<ActionAnimation> BIPED_DEMOLITION_LEAP;
   public static AnimationManager.AnimationAccessor<ActionAnimation> BIPED_PHANTOM_ASCENT_FORWARD;
   public static AnimationManager.AnimationAccessor<ActionAnimation> BIPED_PHANTOM_ASCENT_BACKWARD;
   public static AnimationManager.AnimationAccessor<AttackAnimation> BIPED_MOB_ONEHAND1;
   public static AnimationManager.AnimationAccessor<AttackAnimation> BIPED_MOB_ONEHAND2;
   public static AnimationManager.AnimationAccessor<AttackAnimation> BIPED_MOB_GREATSWORD;
   public static AnimationManager.AnimationAccessor<AttackAnimation> BIPED_MOB_TACHI;
   public static AnimationManager.AnimationAccessor<AttackAnimation> BIPED_MOB_SPEAR_ONEHAND;
   public static AnimationManager.AnimationAccessor<AttackAnimation> BIPED_MOB_SPEAR_TWOHAND1;
   public static AnimationManager.AnimationAccessor<AttackAnimation> BIPED_MOB_SPEAR_TWOHAND2;
   public static AnimationManager.AnimationAccessor<AttackAnimation> BIPED_MOB_SPEAR_TWOHAND3;
   public static AnimationManager.AnimationAccessor<AttackAnimation> BIPED_MOB_SWORD_DUAL1;
   public static AnimationManager.AnimationAccessor<AttackAnimation> BIPED_MOB_SWORD_DUAL2;
   public static AnimationManager.AnimationAccessor<AttackAnimation> BIPED_MOB_SWORD_DUAL3;
   public static AnimationManager.AnimationAccessor<AttackAnimation> BIPED_MOB_LONGSWORD1;
   public static AnimationManager.AnimationAccessor<AttackAnimation> BIPED_MOB_LONGSWORD2;
   public static AnimationManager.AnimationAccessor<AttackAnimation> BIPED_MOB_UCHIGATANA1;
   public static AnimationManager.AnimationAccessor<AttackAnimation> BIPED_MOB_UCHIGATANA2;
   public static AnimationManager.AnimationAccessor<AttackAnimation> BIPED_MOB_UCHIGATANA3;
   public static AnimationManager.AnimationAccessor<AttackAnimation> BIPED_MOB_DAGGER_ONEHAND1;
   public static AnimationManager.AnimationAccessor<AttackAnimation> BIPED_MOB_DAGGER_ONEHAND2;
   public static AnimationManager.AnimationAccessor<AttackAnimation> BIPED_MOB_DAGGER_ONEHAND3;
   public static AnimationManager.AnimationAccessor<AttackAnimation> BIPED_MOB_DAGGER_TWOHAND1;
   public static AnimationManager.AnimationAccessor<AttackAnimation> BIPED_MOB_DAGGER_TWOHAND2;
   public static AnimationManager.AnimationAccessor<RangedAttackAnimation> BIPED_MOB_THROW;
   public static AnimationManager.AnimationAccessor<StaticAnimation> CREEPER_IDLE;
   public static AnimationManager.AnimationAccessor<MovementAnimation> CREEPER_WALK;
   public static AnimationManager.AnimationAccessor<LongHitAnimation> CREEPER_HIT_LONG;
   public static AnimationManager.AnimationAccessor<HitAnimation> CREEPER_HIT_SHORT;
   public static AnimationManager.AnimationAccessor<LongHitAnimation> CREEPER_DEATH;
   public static AnimationManager.AnimationAccessor<StaticAnimation> DRAGON_IDLE;
   public static AnimationManager.AnimationAccessor<EnderDragonWalkAnimation> DRAGON_WALK;
   public static AnimationManager.AnimationAccessor<StaticAnimation> DRAGON_FLY;
   public static AnimationManager.AnimationAccessor<EnderDragonDeathAnimation> DRAGON_DEATH;
   public static AnimationManager.AnimationAccessor<EnderDragonActionAnimation> DRAGON_GROUND_TO_FLY;
   public static AnimationManager.AnimationAccessor<EnderDragonDynamicActionAnimation> DRAGON_FLY_TO_GROUND;
   public static AnimationManager.AnimationAccessor<EnderDragonAttackAnimation> DRAGON_ATTACK1;
   public static AnimationManager.AnimationAccessor<EnderDragonAttackAnimation> DRAGON_ATTACK2;
   public static AnimationManager.AnimationAccessor<EnderDragonAttackAnimation> DRAGON_ATTACK3;
   public static AnimationManager.AnimationAccessor<EnderDragonAttackAnimation> DRAGON_ATTACK4;
   public static AnimationManager.AnimationAccessor<EnderDragonActionAnimation> DRAGON_ATTACK4_RECOVERY;
   public static AnimationManager.AnimationAccessor<EnderDragonActionAnimation> DRAGON_FIREBALL;
   public static AnimationManager.AnimationAccessor<StaticAnimation> DRAGON_AIRSTRIKE;
   public static AnimationManager.AnimationAccessor<EnderDragonActionAnimation> DRAGON_BACKJUMP_PREPARE;
   public static AnimationManager.AnimationAccessor<AttackAnimation> DRAGON_BACKJUMP_MOVE;
   public static AnimationManager.AnimationAccessor<EnderDragonActionAnimation> DRAGON_BACKJUMP_RECOVERY;
   public static AnimationManager.AnimationAccessor<EnderDragonActionAnimation> DRAGON_CRYSTAL_LINK;
   public static AnimationManager.AnimationAccessor<EnderDragonActionAnimation> DRAGON_NEUTRALIZED;
   public static AnimationManager.AnimationAccessor<EnderDragonActionAnimation> DRAGON_NEUTRALIZED_RECOVERY;
   public static AnimationManager.AnimationAccessor<StaticAnimation> ENDERMAN_IDLE;
   public static AnimationManager.AnimationAccessor<MovementAnimation> ENDERMAN_WALK;
   public static AnimationManager.AnimationAccessor<LongHitAnimation> ENDERMAN_DEATH;
   public static AnimationManager.AnimationAccessor<HitAnimation> ENDERMAN_HIT_SHORT;
   public static AnimationManager.AnimationAccessor<LongHitAnimation> ENDERMAN_HIT_LONG;
   public static AnimationManager.AnimationAccessor<LongHitAnimation> ENDERMAN_NEUTRALIZED;
   public static AnimationManager.AnimationAccessor<InvincibleAnimation> ENDERMAN_CONVERT_RAGE;
   public static AnimationManager.AnimationAccessor<StaticAnimation> ENDERMAN_RAGE_IDLE;
   public static AnimationManager.AnimationAccessor<MovementAnimation> ENDERMAN_RAGE_WALK;
   public static AnimationManager.AnimationAccessor<AttackAnimation> ENDERMAN_GRASP;
   public static AnimationManager.AnimationAccessor<AttackAnimation> ENDERMAN_TP_KICK1;
   public static AnimationManager.AnimationAccessor<AttackAnimation> ENDERMAN_TP_KICK2;
   public static AnimationManager.AnimationAccessor<AttackAnimation> ENDERMAN_KNEE;
   public static AnimationManager.AnimationAccessor<AttackAnimation> ENDERMAN_KICK1;
   public static AnimationManager.AnimationAccessor<AttackAnimation> ENDERMAN_KICK2;
   public static AnimationManager.AnimationAccessor<AttackAnimation> ENDERMAN_KICK_COMBO;
   public static AnimationManager.AnimationAccessor<ActionAnimation> ENDERMAN_TP_EMERGENCE;
   public static AnimationManager.AnimationAccessor<StaticAnimation> SPIDER_IDLE;
   public static AnimationManager.AnimationAccessor<MovementAnimation> SPIDER_CRAWL;
   public static AnimationManager.AnimationAccessor<LongHitAnimation> SPIDER_DEATH;
   public static AnimationManager.AnimationAccessor<HitAnimation> SPIDER_HIT;
   public static AnimationManager.AnimationAccessor<LongHitAnimation> SPIDER_NEUTRALIZED;
   public static AnimationManager.AnimationAccessor<AttackAnimation> SPIDER_ATTACK;
   public static AnimationManager.AnimationAccessor<AttackAnimation> SPIDER_JUMP_ATTACK;
   public static AnimationManager.AnimationAccessor<StaticAnimation> GOLEM_IDLE;
   public static AnimationManager.AnimationAccessor<MovementAnimation> GOLEM_WALK;
   public static AnimationManager.AnimationAccessor<LongHitAnimation> GOLEM_DEATH;
   public static AnimationManager.AnimationAccessor<AttackAnimation> GOLEM_ATTACK1;
   public static AnimationManager.AnimationAccessor<AttackAnimation> GOLEM_ATTACK2;
   public static AnimationManager.AnimationAccessor<AttackAnimation> GOLEM_ATTACK3;
   public static AnimationManager.AnimationAccessor<AttackAnimation> GOLEM_ATTACK4;
   public static AnimationManager.AnimationAccessor<StaticAnimation> HOGLIN_IDLE;
   public static AnimationManager.AnimationAccessor<MovementAnimation> HOGLIN_WALK;
   public static AnimationManager.AnimationAccessor<LongHitAnimation> HOGLIN_DEATH;
   public static AnimationManager.AnimationAccessor<AttackAnimation> HOGLIN_ATTACK;
   public static AnimationManager.AnimationAccessor<StaticAnimation> ILLAGER_IDLE;
   public static AnimationManager.AnimationAccessor<MovementAnimation> ILLAGER_WALK;
   public static AnimationManager.AnimationAccessor<StaticAnimation> VINDICATOR_IDLE_AGGRESSIVE;
   public static AnimationManager.AnimationAccessor<MovementAnimation> VINDICATOR_CHASE;
   public static AnimationManager.AnimationAccessor<AttackAnimation> VINDICATOR_SWING_AXE1;
   public static AnimationManager.AnimationAccessor<AttackAnimation> VINDICATOR_SWING_AXE2;
   public static AnimationManager.AnimationAccessor<AttackAnimation> VINDICATOR_SWING_AXE3;
   public static AnimationManager.AnimationAccessor<StaticAnimation> EVOKER_CAST_SPELL;
   public static AnimationManager.AnimationAccessor<StaticAnimation> PIGLIN_IDLE;
   public static AnimationManager.AnimationAccessor<MovementAnimation> PIGLIN_WALK;
   public static AnimationManager.AnimationAccessor<StaticAnimation> PIGLIN_ZOMBIFIED_IDLE;
   public static AnimationManager.AnimationAccessor<MovementAnimation> PIGLIN_ZOMBIFIED_WALK;
   public static AnimationManager.AnimationAccessor<MovementAnimation> PIGLIN_ZOMBIFIED_CHASE;
   public static AnimationManager.AnimationAccessor<StaticAnimation> PIGLIN_CELEBRATE1;
   public static AnimationManager.AnimationAccessor<StaticAnimation> PIGLIN_CELEBRATE2;
   public static AnimationManager.AnimationAccessor<StaticAnimation> PIGLIN_CELEBRATE3;
   public static AnimationManager.AnimationAccessor<StaticAnimation> PIGLIN_ADMIRE;
   public static AnimationManager.AnimationAccessor<LongHitAnimation> PIGLIN_DEATH;
   public static AnimationManager.AnimationAccessor<StaticAnimation> RAVAGER_IDLE;
   public static AnimationManager.AnimationAccessor<MovementAnimation> RAVAGER_WALK;
   public static AnimationManager.AnimationAccessor<LongHitAnimation> RAVAGER_DEATH;
   public static AnimationManager.AnimationAccessor<ActionAnimation> RAVAGER_STUN;
   public static AnimationManager.AnimationAccessor<AttackAnimation> RAVAGER_ATTACK1;
   public static AnimationManager.AnimationAccessor<AttackAnimation> RAVAGER_ATTACK2;
   public static AnimationManager.AnimationAccessor<AttackAnimation> RAVAGER_ATTACK3;
   public static AnimationManager.AnimationAccessor<StaticAnimation> VEX_IDLE;
   public static AnimationManager.AnimationAccessor<StaticAnimation> VEX_FLIPPING;
   public static AnimationManager.AnimationAccessor<LongHitAnimation> VEX_DEATH;
   public static AnimationManager.AnimationAccessor<HitAnimation> VEX_HIT;
   public static AnimationManager.AnimationAccessor<AttackAnimation> VEX_CHARGE;
   public static AnimationManager.AnimationAccessor<LongHitAnimation> VEX_NEUTRALIZED;
   public static AnimationManager.AnimationAccessor<StaticAnimation> WITCH_DRINKING;
   public static AnimationManager.AnimationAccessor<StaticAnimation> WITHER_SKELETON_IDLE;
   public static AnimationManager.AnimationAccessor<InvincibleAnimation> WITHER_SKELETON_SPECIAL_SPAWN;
   public static AnimationManager.AnimationAccessor<MovementAnimation> WITHER_SKELETON_WALK;
   public static AnimationManager.AnimationAccessor<MovementAnimation> WITHER_SKELETON_CHASE;
   public static AnimationManager.AnimationAccessor<AttackAnimation> WITHER_SKELETON_ATTACK1;
   public static AnimationManager.AnimationAccessor<AttackAnimation> WITHER_SKELETON_ATTACK2;
   public static AnimationManager.AnimationAccessor<AttackAnimation> WITHER_SKELETON_ATTACK3;
   public static AnimationManager.AnimationAccessor<StaticAnimation> WITHER_IDLE;
   public static AnimationManager.AnimationAccessor<AttackAnimation> WITHER_CHARGE;
   public static AnimationManager.AnimationAccessor<LongHitAnimation> WITHER_DEATH;
   public static AnimationManager.AnimationAccessor<LongHitAnimation> WITHER_NEUTRALIZED;
   public static AnimationManager.AnimationAccessor<InvincibleAnimation> WITHER_SPELL_ARMOR;
   public static AnimationManager.AnimationAccessor<ActionAnimation> WITHER_BLOCKED;
   public static AnimationManager.AnimationAccessor<InvincibleAnimation> WITHER_GHOST_STANDBY;
   public static AnimationManager.AnimationAccessor<AttackAnimation> WITHER_SWIRL;
   public static AnimationManager.AnimationAccessor<ActionAnimation> WITHER_BEAM;
   public static AnimationManager.AnimationAccessor<AttackAnimation> WITHER_BACKFLIP;
   public static AnimationManager.AnimationAccessor<StaticAnimation> ZOMBIE_IDLE;
   public static AnimationManager.AnimationAccessor<MovementAnimation> ZOMBIE_WALK;
   public static AnimationManager.AnimationAccessor<MovementAnimation> ZOMBIE_CHASE;
   public static AnimationManager.AnimationAccessor<AttackAnimation> ZOMBIE_ATTACK1;
   public static AnimationManager.AnimationAccessor<AttackAnimation> ZOMBIE_ATTACK2;
   public static AnimationManager.AnimationAccessor<AttackAnimation> ZOMBIE_ATTACK3;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> AXE_AUTO1;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> AXE_AUTO2;
   public static AnimationManager.AnimationAccessor<DashAttackAnimation> AXE_DASH;
   public static AnimationManager.AnimationAccessor<AirSlashAnimation> AXE_AIRSLASH;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> FIST_AUTO1;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> FIST_AUTO2;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> FIST_AUTO3;
   public static AnimationManager.AnimationAccessor<DashAttackAnimation> FIST_DASH;
   public static AnimationManager.AnimationAccessor<AirSlashAnimation> FIST_AIR_SLASH;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> SPEAR_ONEHAND_AUTO;
   public static AnimationManager.AnimationAccessor<AirSlashAnimation> SPEAR_ONEHAND_AIR_SLASH;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> SPEAR_TWOHAND_AUTO1;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> SPEAR_TWOHAND_AUTO2;
   public static AnimationManager.AnimationAccessor<AirSlashAnimation> SPEAR_TWOHAND_AIR_SLASH;
   public static AnimationManager.AnimationAccessor<DashAttackAnimation> SPEAR_DASH;
   public static AnimationManager.AnimationAccessor<MountAttackAnimation> SPEAR_MOUNT_ATTACK;
   public static AnimationManager.AnimationAccessor<StaticAnimation> SPEAR_GUARD;
   public static AnimationManager.AnimationAccessor<GuardAnimation> SPEAR_GUARD_HIT;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> SWORD_AUTO1;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> SWORD_AUTO2;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> SWORD_AUTO3;
   public static AnimationManager.AnimationAccessor<DashAttackAnimation> SWORD_DASH;
   public static AnimationManager.AnimationAccessor<AirSlashAnimation> SWORD_AIR_SLASH;
   public static AnimationManager.AnimationAccessor<StaticAnimation> SWORD_GUARD;
   public static AnimationManager.AnimationAccessor<GuardAnimation> SWORD_GUARD_HIT;
   public static AnimationManager.AnimationAccessor<GuardAnimation> SWORD_GUARD_ACTIVE_HIT1;
   public static AnimationManager.AnimationAccessor<GuardAnimation> SWORD_GUARD_ACTIVE_HIT2;
   public static AnimationManager.AnimationAccessor<GuardAnimation> SWORD_GUARD_ACTIVE_HIT3;
   public static AnimationManager.AnimationAccessor<GuardAnimation> LONGSWORD_GUARD_ACTIVE_HIT1;
   public static AnimationManager.AnimationAccessor<GuardAnimation> LONGSWORD_GUARD_ACTIVE_HIT2;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> SWORD_DUAL_AUTO1;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> SWORD_DUAL_AUTO2;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> SWORD_DUAL_AUTO3;
   public static AnimationManager.AnimationAccessor<DashAttackAnimation> SWORD_DUAL_DASH;
   public static AnimationManager.AnimationAccessor<AirSlashAnimation> SWORD_DUAL_AIR_SLASH;
   public static AnimationManager.AnimationAccessor<StaticAnimation> SWORD_DUAL_GUARD;
   public static AnimationManager.AnimationAccessor<GuardAnimation> SWORD_DUAL_GUARD_HIT;
   public static AnimationManager.AnimationAccessor<LongHitAnimation> BIPED_COMMON_NEUTRALIZED;
   public static AnimationManager.AnimationAccessor<LongHitAnimation> GREATSWORD_GUARD_BREAK;
   public static AnimationManager.AnimationAccessor<AttackAnimation> METEOR_SLAM;
   public static AnimationManager.AnimationAccessor<AttackAnimation> REVELATION_ONEHAND;
   public static AnimationManager.AnimationAccessor<AttackAnimation> REVELATION_TWOHAND;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> LONGSWORD_AUTO1;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> LONGSWORD_AUTO2;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> LONGSWORD_AUTO3;
   public static AnimationManager.AnimationAccessor<DashAttackAnimation> LONGSWORD_DASH;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> LONGSWORD_LIECHTENAUER_AUTO1;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> LONGSWORD_LIECHTENAUER_AUTO2;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> LONGSWORD_LIECHTENAUER_AUTO3;
   public static AnimationManager.AnimationAccessor<AirSlashAnimation> LONGSWORD_AIR_SLASH;
   public static AnimationManager.AnimationAccessor<StaticAnimation> LONGSWORD_GUARD;
   public static AnimationManager.AnimationAccessor<GuardAnimation> LONGSWORD_GUARD_HIT;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> TACHI_AUTO1;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> TACHI_AUTO2;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> TACHI_AUTO3;
   public static AnimationManager.AnimationAccessor<DashAttackAnimation> TACHI_DASH;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> TOOL_AUTO1;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> TOOL_AUTO2;
   public static AnimationManager.AnimationAccessor<DashAttackAnimation> TOOL_DASH;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> UCHIGATANA_AUTO1;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> UCHIGATANA_AUTO2;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> UCHIGATANA_AUTO3;
   public static AnimationManager.AnimationAccessor<DashAttackAnimation> UCHIGATANA_DASH;
   public static AnimationManager.AnimationAccessor<AirSlashAnimation> UCHIGATANA_AIR_SLASH;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> UCHIGATANA_SHEATHING_AUTO;
   public static AnimationManager.AnimationAccessor<DashAttackAnimation> UCHIGATANA_SHEATHING_DASH;
   public static AnimationManager.AnimationAccessor<AirSlashAnimation> UCHIGATANA_SHEATH_AIR_SLASH;
   public static AnimationManager.AnimationAccessor<StaticAnimation> UCHIGATANA_GUARD;
   public static AnimationManager.AnimationAccessor<GuardAnimation> UCHIGATANA_GUARD_HIT;
   public static AnimationManager.AnimationAccessor<MountAttackAnimation> SWORD_MOUNT_ATTACK;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> GREATSWORD_AUTO1;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> GREATSWORD_AUTO2;
   public static AnimationManager.AnimationAccessor<DashAttackAnimation> GREATSWORD_DASH;
   public static AnimationManager.AnimationAccessor<AirSlashAnimation> GREATSWORD_AIR_SLASH;
   public static AnimationManager.AnimationAccessor<StaticAnimation> GREATSWORD_GUARD;
   public static AnimationManager.AnimationAccessor<GuardAnimation> GREATSWORD_GUARD_HIT;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> DAGGER_AUTO1;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> DAGGER_AUTO2;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> DAGGER_AUTO3;
   public static AnimationManager.AnimationAccessor<DashAttackAnimation> DAGGER_DASH;
   public static AnimationManager.AnimationAccessor<AirSlashAnimation> DAGGER_AIR_SLASH;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> DAGGER_DUAL_AUTO1;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> DAGGER_DUAL_AUTO2;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> DAGGER_DUAL_AUTO3;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> DAGGER_DUAL_AUTO4;
   public static AnimationManager.AnimationAccessor<DashAttackAnimation> DAGGER_DUAL_DASH;
   public static AnimationManager.AnimationAccessor<AirSlashAnimation> DAGGER_DUAL_AIR_SLASH;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> TRIDENT_AUTO1;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> TRIDENT_AUTO2;
   public static AnimationManager.AnimationAccessor<BasicAttackAnimation> TRIDENT_AUTO3;
   public static AnimationManager.AnimationAccessor<AttackAnimation> THE_GUILLOTINE;
   public static AnimationManager.AnimationAccessor<AttackAnimation> SWEEPING_EDGE;
   public static AnimationManager.AnimationAccessor<AttackAnimation> DANCING_EDGE;
   public static AnimationManager.AnimationAccessor<AttackAnimation> HEARTPIERCER;
   public static AnimationManager.AnimationAccessor<AttackAnimation> GRASPING_SPIRAL_FIRST;
   public static AnimationManager.AnimationAccessor<AttackAnimation> GRASPING_SPIRAL_SECOND;
   public static AnimationManager.AnimationAccessor<StaticAnimation> STEEL_WHIRLWIND_CHARGING;
   public static AnimationManager.AnimationAccessor<AttackAnimation> STEEL_WHIRLWIND;
   public static AnimationManager.AnimationAccessor<AttackAnimation> BATTOJUTSU;
   public static AnimationManager.AnimationAccessor<AttackAnimation> BATTOJUTSU_DASH;
   public static AnimationManager.AnimationAccessor<AttackAnimation> RUSHING_TEMPO1;
   public static AnimationManager.AnimationAccessor<AttackAnimation> RUSHING_TEMPO2;
   public static AnimationManager.AnimationAccessor<AttackAnimation> RUSHING_TEMPO3;
   public static AnimationManager.AnimationAccessor<AttackAnimation> RELENTLESS_COMBO;
   public static AnimationManager.AnimationAccessor<AttackAnimation> EVISCERATE_FIRST;
   public static AnimationManager.AnimationAccessor<AttackAnimation> EVISCERATE_SECOND;
   public static AnimationManager.AnimationAccessor<AttackAnimation> BLADE_RUSH_COMBO1;
   public static AnimationManager.AnimationAccessor<AttackAnimation> BLADE_RUSH_COMBO2;
   public static AnimationManager.AnimationAccessor<AttackAnimation> BLADE_RUSH_COMBO3;
   public static AnimationManager.AnimationAccessor<LongHitAnimation> BLADE_RUSH_HIT;
   public static AnimationManager.AnimationAccessor<GrapplingAttackAnimation> BLADE_RUSH_EXECUTE_BIPED;
   public static AnimationManager.AnimationAccessor<GrapplingTryAnimation> BLADE_RUSH_TRY;
   public static AnimationManager.AnimationAccessor<ActionAnimation> BLADE_RUSH_FAILED;
   public static AnimationManager.AnimationAccessor<AttackAnimation> WRATHFUL_LIGHTING;
   public static AnimationManager.AnimationAccessor<AttackAnimation> TSUNAMI;
   public static AnimationManager.AnimationAccessor<AttackAnimation> TSUNAMI_REINFORCED;
   public static AnimationManager.AnimationAccessor<ActionAnimation> EVERLASTING_ALLEGIANCE_CALL;
   public static AnimationManager.AnimationAccessor<ActionAnimation> EVERLASTING_ALLEGIANCE_CATCH;
   public static AnimationManager.AnimationAccessor<AttackAnimation> SHARP_STAB;
   public static AnimationManager.AnimationAccessor<OffAnimation> OFF_ANIMATION_HIGHEST;
   public static AnimationManager.AnimationAccessor<OffAnimation> OFF_ANIMATION_MIDDLE;
   public static AnimationManager.AnimationAccessor<OffAnimation> OFF_ANIMATION_LOWEST;

   @SubscribeEvent
   public static void registerAnimations(AnimationManager.AnimationRegistryEvent event) {
      event.newBuilder("epicfight", Animations::build);
   }

   public static void build(AnimationManager.AnimationBuilder builder) {
      BIPED_IDLE = builder.nextAccessor("biped/living/idle", accessor -> new StaticAnimation(true, accessor, Armatures.BIPED));
      BIPED_WALK = builder.nextAccessor("biped/living/walk", accessor -> new MovementAnimation(true, accessor, Armatures.BIPED));
      BIPED_FLYING = builder.nextAccessor("biped/living/fly", accessor -> new StaticAnimation(true, accessor, Armatures.BIPED));
      BIPED_CREATIVE_IDLE = builder.nextAccessor("biped/living/creative_idle", accessor -> new StaticAnimation(true, accessor, Armatures.BIPED));
      BIPED_CREATIVE_FLYING = builder.nextAccessor(
         "biped/living/creative_fly",
         accessor -> new SelectiveAnimation(
            entitypatch -> {
               Vec3 view = entitypatch.getOriginal().m_20252_(1.0F);
               Vec3 move = entitypatch.getOriginal().m_20184_();
               double dot = view.m_82526_(move);
               return dot < 0.0 ? 1 : 0;
            },
            accessor,
            new DirectStaticAnimation(0.15F, true, EpicFightMod.identifier("biped/living/creative_fly_forward"), Armatures.BIPED)
               .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.FLYING_CORRECTION),
            new DirectStaticAnimation(0.15F, true, EpicFightMod.identifier("biped/living/creative_fly_backward"), Armatures.BIPED)
               .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.FLYING_CORRECTION2)
         )
      );
      BIPED_HOLD_CROSSBOW = builder.nextAccessor("biped/living/hold_crossbow", accessor -> new StaticAnimation(true, accessor, Armatures.BIPED));
      BIPED_HOLD_MAP_TWOHAND = builder.nextAccessor(
         "biped/living/hold_map_twohand",
         accessor -> new StaticAnimation(true, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.MAP_ARMS_CORRECTION)
      );
      BIPED_HOLD_MAP_OFFHAND = builder.nextAccessor(
         "biped/living/hold_map_offhand",
         accessor -> new StaticAnimation(true, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.MAP_ARMS_CORRECTION)
      );
      BIPED_HOLD_MAP_MAINHAND = builder.nextAccessor(
         "biped/living/hold_map_mainhand",
         accessor -> new StaticAnimation(true, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.MAP_ARMS_CORRECTION)
      );
      BIPED_HOLD_MAP_TWOHAND_MOVE = builder.nextAccessor(
         "biped/living/hold_map_twohand_move",
         accessor -> new StaticAnimation(true, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.MAP_ARMS_CORRECTION)
      );
      BIPED_HOLD_MAP_OFFHAND_MOVE = builder.nextAccessor(
         "biped/living/hold_map_offhand_move",
         accessor -> new StaticAnimation(true, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.MAP_ARMS_CORRECTION)
      );
      BIPED_HOLD_MAP_MAINHAND_MOVE = builder.nextAccessor(
         "biped/living/hold_map_mainhand_move",
         accessor -> new StaticAnimation(true, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.MAP_ARMS_CORRECTION)
      );
      BIPED_RUN = builder.nextAccessor("biped/living/run", accessor -> new MovementAnimation(true, accessor, Armatures.BIPED));
      BIPED_SNEAK = builder.nextAccessor("biped/living/sneak", accessor -> new MovementAnimation(true, accessor, Armatures.BIPED));
      BIPED_SWIM = builder.nextAccessor("biped/living/swim", accessor -> new MovementAnimation(true, accessor, Armatures.BIPED));
      BIPED_FLOAT = builder.nextAccessor("biped/living/float", accessor -> new StaticAnimation(true, accessor, Armatures.BIPED));
      BIPED_KNEEL = builder.nextAccessor("biped/living/kneel", accessor -> new StaticAnimation(true, accessor, Armatures.BIPED));
      BIPED_FALL = builder.nextAccessor("biped/living/fall", accessor -> new StaticAnimation(true, accessor, Armatures.BIPED));
      BIPED_MOUNT = builder.nextAccessor(
         "biped/living/mount",
         accessor -> new StaticAnimation(true, accessor, Armatures.BIPED)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.StaticAnimationProperty.FIXED_HEAD_ROTATION, true)
            .<StaticAnimation, AnimationEvent.SimpleEvent<AnimationEvent.E2<CapabilityItem, CapabilityItem>>>addProperty(
               AnimationProperty.StaticAnimationProperty.ON_ITEM_CHANGE_EVENT,
               AnimationEvent.SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_MOUNT_AND_ITEM_CHANGED, AnimationEvent.Side.CLIENT)
            )
            .<StaticAnimation>addEvents(
               AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS,
               AnimationEvent.SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_MOUNT, AnimationEvent.Side.CLIENT)
            )
            .addEvents(
               AnimationProperty.StaticAnimationProperty.ON_END_EVENTS,
               AnimationEvent.SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, AnimationEvent.Side.CLIENT)
            )
      );
      BIPED_SIT = builder.nextAccessor(
         "biped/living/sit",
         accessor -> new StaticAnimation(true, accessor, Armatures.BIPED).addProperty(AnimationProperty.StaticAnimationProperty.FIXED_HEAD_ROTATION, true)
      );
      BIPED_DIG = builder.nextAccessor(
         "biped/living/dig",
         accessor -> new SelectiveAnimation(
            entitypatch -> entitypatch.getOriginal().f_20912_ == InteractionHand.OFF_HAND ? 1 : 0,
            accessor,
            new DirectStaticAnimation(0.1F, true, EpicFightMod.identifier("biped/living/dig_mainhand"), Armatures.BIPED),
            new DirectStaticAnimation(0.1F, true, EpicFightMod.identifier("biped/living/dig_offhand"), Armatures.BIPED)
         )
      );
      BIPED_BOW_AIM = builder.nextAccessor(
         "biped/combat/bow_aim",
         accessor -> new AimAnimation(
            true, accessor, "biped/combat/bow_aim_mid", "biped/combat/bow_aim_up", "biped/combat/bow_aim_down", "biped/combat/bow_aim_lying", Armatures.BIPED
         )
      );
      BIPED_BOW_SHOT = builder.nextAccessor(
         "biped/combat/bow_shot",
         accessor -> new ReboundAnimation(
            0.05F,
            false,
            accessor,
            "biped/combat/bow_shot_mid",
            "biped/combat/bow_shot_up",
            "biped/combat/bow_shot_down",
            "biped/combat/bow_shot_lying",
            Armatures.BIPED
         )
      );
      BIPED_DRINK = builder.nextAccessor(
         "biped/living/drink",
         accessor -> new MirrorAnimation(0.35F, true, accessor, "biped/living/drink_mainhand", "biped/living/drink_offhand", Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.FIXED_HEAD_ROTATION, true)
      );
      BIPED_EAT = builder.nextAccessor(
         "biped/living/eat",
         accessor -> new MirrorAnimation(0.35F, true, accessor, "biped/living/eat_mainhand", "biped/living/eat_offhand", Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.FIXED_HEAD_ROTATION, true)
      );
      BIPED_SPYGLASS_USE = builder.nextAccessor(
         "biped/living/spyglass",
         accessor -> new MirrorAnimation(0.15F, true, accessor, "biped/living/spyglass_mainhand", "biped/living/spyglass_offhand", Armatures.BIPED)
            .<StaticAnimation, AnimationProperty.PlaybackSpeedModifier>addProperty(
               AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER,
               (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> self.isLinkAnimation() ? speed : 0.0F
            )
            .<StaticAnimation, AnimationProperty.PoseModifier>addProperty(
               AnimationProperty.StaticAnimationProperty.POSE_MODIFIER,
               (self, pose, entitypatch, elapsedTime, partialTicks) -> {
                  if (entitypatch.isFirstPerson()) {
                     pose.disableAllJoints();
                  } else if (!self.isLinkAnimation()) {
                     LivingMotion livingMotion = entitypatch.getCurrentLivingMotion();
                     Pose rawPose;
                     if (livingMotion != LivingMotions.SWIM && livingMotion != LivingMotions.FLY && livingMotion != LivingMotions.CREATIVE_FLY) {
                        float xRot = Mth.m_14036_((entitypatch.getOriginal().m_146909_() + 90.0F) * 0.016666668F, 0.0F, 3.0F);
                        rawPose = self.getRawPose(xRot);
                        float f = 90.0F;
                        float ratio = (f - Math.abs(entitypatch.getOriginal().m_146909_())) / f;
                        float yawOffset = entitypatch.getOriginal().m_20202_() != null
                           ? entitypatch.getOriginal().m_6080_()
                           : entitypatch.getOriginal().f_20883_;
                        rawPose.get("Chest")
                           .frontResult(
                              JointTransform.rotation(QuaternionUtils.YP.rotationDegrees(Mth.m_14177_(entitypatch.getOriginal().m_6080_() - yawOffset) * ratio)),
                              OpenMatrix4f::mulAsOriginInverse
                           );
                     } else {
                        rawPose = self.getRawPose(3.3333F);
                     }

                     pose.load(rawPose, Pose.LoadOperation.OVERWRITE);
                  }
               }
            )
            .addProperty(AnimationProperty.StaticAnimationProperty.FIXED_HEAD_ROTATION, true)
      );
      BIPED_CROSSBOW_AIM = builder.nextAccessor(
         "biped/combat/crossbow_aim",
         accessor -> new AimAnimation(
            true,
            accessor,
            "biped/combat/crossbow_aim_mid",
            "biped/combat/crossbow_aim_up",
            "biped/combat/crossbow_aim_down",
            "biped/combat/crossbow_aim_lying",
            Armatures.BIPED
         )
      );
      BIPED_CROSSBOW_SHOT = builder.nextAccessor(
         "biped/combat/crossbow_shot",
         accessor -> new ReboundAnimation(
            false,
            accessor,
            "biped/combat/crossbow_shot_mid",
            "biped/combat/crossbow_shot_up",
            "biped/combat/crossbow_shot_down",
            "biped/combat/crossbow_shot_lying",
            Armatures.BIPED
         )
      );
      BIPED_CROSSBOW_RELOAD = builder.nextAccessor("biped/combat/crossbow_reload", accessor -> new StaticAnimation(false, accessor, Armatures.BIPED));
      BIPED_JUMP = builder.nextAccessor("biped/living/jump", accessor -> new StaticAnimation(0.083F, false, accessor, Armatures.BIPED));
      BIPED_RUN_SPEAR = builder.nextAccessor("biped/living/run_spear", accessor -> new MovementAnimation(true, accessor, Armatures.BIPED));
      BIPED_BLOCK = builder.nextAccessor(
         "biped/living/shield",
         accessor -> new MirrorAnimation(0.25F, true, accessor, "biped/living/shield_mainhand", "biped/living/shield_offhand", Armatures.BIPED)
      );
      BIPED_HOLD_GREATSWORD = builder.nextAccessor("biped/living/hold_greatsword", accessor -> new StaticAnimation(true, accessor, Armatures.BIPED));
      BIPED_HOLD_UCHIGATANA_SHEATHING = builder.nextAccessor(
         "biped/living/hold_uchigatana_sheath", accessor -> new StaticAnimation(true, accessor, Armatures.BIPED)
      );
      BIPED_HOLD_UCHIGATANA = builder.nextAccessor("biped/living/hold_uchigatana", accessor -> new StaticAnimation(true, accessor, Armatures.BIPED));
      BIPED_HOLD_TACHI = builder.nextAccessor("biped/living/hold_tachi", accessor -> new StaticAnimation(true, accessor, Armatures.BIPED));
      BIPED_HOLD_LONGSWORD = builder.nextAccessor("biped/living/hold_longsword", accessor -> new StaticAnimation(true, accessor, Armatures.BIPED));
      BIPED_HOLD_SPEAR = builder.nextAccessor("biped/living/hold_spear", accessor -> new StaticAnimation(true, accessor, Armatures.BIPED));
      BIPED_HOLD_DUAL_WEAPON = builder.nextAccessor("biped/living/hold_dual", accessor -> new StaticAnimation(true, accessor, Armatures.BIPED));
      BIPED_HOLD_LIECHTENAUER = builder.nextAccessor("biped/living/hold_liechtenauer", accessor -> new StaticAnimation(true, accessor, Armatures.BIPED));
      BIPED_WALK_GREATSWORD = builder.nextAccessor("biped/living/walk_greatsword", accessor -> new MovementAnimation(true, accessor, Armatures.BIPED));
      BIPED_WALK_SPEAR = builder.nextAccessor("biped/living/walk_spear", accessor -> new MovementAnimation(true, accessor, Armatures.BIPED));
      BIPED_WALK_UCHIGATANA_SHEATHING = builder.nextAccessor(
         "biped/living/walk_uchigatana_sheath", accessor -> new MovementAnimation(true, accessor, Armatures.BIPED)
      );
      BIPED_WALK_UCHIGATANA = builder.nextAccessor("biped/living/walk_uchigatana", accessor -> new MovementAnimation(true, accessor, Armatures.BIPED));
      BIPED_WALK_TWOHAND = builder.nextAccessor("biped/living/walk_twohand", accessor -> new MovementAnimation(true, accessor, Armatures.BIPED));
      BIPED_WALK_LONGSWORD = builder.nextAccessor("biped/living/walk_longsword", accessor -> new MovementAnimation(true, accessor, Armatures.BIPED));
      BIPED_WALK_LIECHTENAUER = builder.nextAccessor("biped/living/walk_liechtenauer", accessor -> new MovementAnimation(true, accessor, Armatures.BIPED));
      BIPED_RUN_GREATSWORD = builder.nextAccessor("biped/living/run_greatsword", accessor -> new MovementAnimation(true, accessor, Armatures.BIPED));
      BIPED_RUN_UCHIGATANA = builder.nextAccessor("biped/living/run_uchigatana", accessor -> new MovementAnimation(true, accessor, Armatures.BIPED));
      BIPED_RUN_UCHIGATANA_SHEATHING = builder.nextAccessor(
         "biped/living/run_uchigatana_sheath", accessor -> new MovementAnimation(true, accessor, Armatures.BIPED)
      );
      BIPED_RUN_DUAL = builder.nextAccessor("biped/living/run_dual", accessor -> new MovementAnimation(true, accessor, Armatures.BIPED));
      BIPED_RUN_LONGSWORD = builder.nextAccessor("biped/living/run_longsword", accessor -> new MovementAnimation(true, accessor, Armatures.BIPED));
      BIPED_UCHIGATANA_SCRAP = builder.nextAccessor(
         "biped/living/uchigatana_scrap",
         accessor -> new StaticAnimation(0.05F, false, accessor, Armatures.BIPED)
            .addEvents(
               AnimationEvent.InTimeEvent.create(0.15F, Animations.ReusableSources.PLAY_SOUND, AnimationEvent.Side.CLIENT)
                  .params((SoundEvent)EpicFightSounds.SWORD_IN.get())
            )
      );
      BIPED_LIECHTENAUER_READY = builder.nextAccessor(
         "biped/living/liechtenauer_ready", accessor -> new StaticAnimation(0.1F, false, accessor, Armatures.BIPED)
      );
      BIPED_HIT_SHIELD = builder.nextAccessor(
         "biped/combat/hit_shield",
         accessor -> new MirrorAnimation(0.05F, false, accessor, "biped/combat/hit_shield_mainhand", "biped/combat/hit_shield_offhand", Armatures.BIPED)
      );
      BIPED_CLIMBING = builder.nextAccessor(
         "biped/living/climb",
         accessor -> new MovementAnimation(0.16F, true, accessor, Armatures.BIPED)
            .<StaticAnimation, AnimationProperty.PlaybackSpeedModifier>addProperty(
               AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> {
                  if (self.isLinkAnimation()) {
                     return 1.0F;
                  } else {
                     double y = entitypatch.getOriginal().m_20186_() - entitypatch.getYOld();
                     if (Math.abs(y) < 0.04) {
                        return 0.0F;
                     } else {
                        return y < 0.0 ? -1.0F : 1.0F;
                     }
                  }
               }
            )
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.StaticAnimationProperty.FIXED_HEAD_ROTATION, true)
            .<StaticAnimation, AnimationEvent.SimpleEvent<AnimationEvent.E2<CapabilityItem, CapabilityItem>>>addProperty(
               AnimationProperty.StaticAnimationProperty.ON_ITEM_CHANGE_EVENT,
               AnimationEvent.SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_ITEM_CHANGED, AnimationEvent.Side.CLIENT)
            )
            .<StaticAnimation>addEvents(
               AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS,
               AnimationEvent.SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, AnimationEvent.Side.CLIENT),
               AnimationEvent.SimpleEvent.create(Animations.ReusableSources.UPDATE_Y_TO_NEARBY_LADDER, AnimationEvent.Side.CLIENT)
            )
            .<StaticAnimation>addEvents(
               AnimationProperty.StaticAnimationProperty.TICK_EVENTS,
               AnimationEvent.SimpleEvent.create(Animations.ReusableSources.UPDATE_Y_TO_NEARBY_LADDER, AnimationEvent.Side.CLIENT)
            )
            .<StaticAnimation>addEvents(
               AnimationProperty.StaticAnimationProperty.ON_END_EVENTS,
               AnimationEvent.SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, AnimationEvent.Side.CLIENT)
            )
            .<StaticAnimation>newTimePair(0.0F, 10000.0F)
            .<Boolean, StaticAnimation>addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, false)
            .<Boolean, StaticAnimation>addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, false)
            .<Boolean, StaticAnimation>addStateRemoveOld(EntityState.TURNING_LOCKED, true)
            .addStateRemoveOld(EntityState.INACTION, true)
      );
      BIPED_SLEEPING = builder.nextAccessor("biped/living/sleep", accessor -> new StaticAnimation(0.16F, true, accessor, Armatures.BIPED));
      BIPED_JAVELIN_AIM = builder.nextAccessor(
         "biped/combat/javelin_aim",
         accessor -> new AimAnimation(
               false,
               accessor,
               "biped/combat/javelin_aim_mid",
               "biped/combat/javelin_aim_up",
               "biped/combat/javelin_aim_down",
               "biped/combat/javelin_aim_lying",
               Armatures.BIPED
            )
            .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, (animation, entitypatch, speed, prevElapsedTime, elapsedTime) -> {
               if (animation.isLinkAnimation()) {
                  return 1.0F;
               } else {
                  return entitypatch.getOriginal().m_6117_() && elapsedTime + 0.05F * speed > animation.getTotalTime() ? 0.0F : 1.0F;
               }
            })
      );
      BIPED_JAVELIN_THROW = builder.nextAccessor(
         "biped/combat/javelin_throw",
         accessor -> new ReboundAnimation(
            0.08F,
            false,
            accessor,
            "biped/combat/javelin_throw_mid",
            "biped/combat/javelin_throw_up",
            "biped/combat/javelin_throw_down",
            "biped/combat/javelin_throw_lying",
            Armatures.BIPED
         )
      );
      OFF_ANIMATION_HIGHEST = builder.nextAccessor("common/off_highest", accessor -> new OffAnimation(accessor));
      OFF_ANIMATION_MIDDLE = builder.nextAccessor("common/off_middle", accessor -> new OffAnimation(accessor));
      OFF_ANIMATION_LOWEST = builder.nextAccessor("common/off_lowest", accessor -> new OffAnimation(accessor));
      ZOMBIE_IDLE = builder.nextAccessor("zombie/idle", accessor -> new StaticAnimation(true, accessor, Armatures.BIPED));
      ZOMBIE_WALK = builder.nextAccessor("zombie/walk", accessor -> new MovementAnimation(true, accessor, Armatures.BIPED));
      ZOMBIE_CHASE = builder.nextAccessor("zombie/chase", accessor -> new MovementAnimation(true, accessor, Armatures.BIPED));
      CREEPER_IDLE = builder.nextAccessor("creeper/idle", accessor -> new StaticAnimation(true, accessor, Armatures.CREEPER));
      CREEPER_WALK = builder.nextAccessor("creeper/walk", accessor -> new MovementAnimation(true, accessor, Armatures.CREEPER));
      ENDERMAN_IDLE = builder.nextAccessor("enderman/idle", accessor -> new StaticAnimation(true, accessor, Armatures.ENDERMAN));
      ENDERMAN_WALK = builder.nextAccessor("enderman/walk", accessor -> new MovementAnimation(true, accessor, Armatures.ENDERMAN));
      ENDERMAN_RAGE_IDLE = builder.nextAccessor("enderman/rage_idle", accessor -> new StaticAnimation(true, accessor, Armatures.ENDERMAN));
      ENDERMAN_RAGE_WALK = builder.nextAccessor("enderman/rage_walk", accessor -> new MovementAnimation(true, accessor, Armatures.ENDERMAN));
      WITHER_SKELETON_WALK = builder.nextAccessor("wither_skeleton/walk", accessor -> new MovementAnimation(true, accessor, Armatures.BIPED));
      WITHER_SKELETON_CHASE = builder.nextAccessor("wither_skeleton/chase", accessor -> new MovementAnimation(0.36F, true, accessor, Armatures.BIPED));
      WITHER_SKELETON_IDLE = builder.nextAccessor("wither_skeleton/idle", accessor -> new StaticAnimation(true, accessor, Armatures.BIPED));
      WITHER_SKELETON_SPECIAL_SPAWN = builder.nextAccessor(
         "wither_skeleton/special_spawn", accessor -> new InvincibleAnimation(0.0F, accessor, Armatures.BIPED)
      );
      SPIDER_IDLE = builder.nextAccessor("spider/idle", accessor -> new StaticAnimation(true, accessor, Armatures.SPIDER));
      SPIDER_CRAWL = builder.nextAccessor("spider/crawl", accessor -> new MovementAnimation(true, accessor, Armatures.SPIDER));
      GOLEM_IDLE = builder.nextAccessor("iron_golem/idle", accessor -> new StaticAnimation(true, accessor, Armatures.IRON_GOLEM));
      GOLEM_WALK = builder.nextAccessor("iron_golem/walk", accessor -> new MovementAnimation(true, accessor, Armatures.IRON_GOLEM));
      HOGLIN_IDLE = builder.nextAccessor("hoglin/idle", accessor -> new StaticAnimation(true, accessor, Armatures.HOGLIN));
      HOGLIN_WALK = builder.nextAccessor("hoglin/walk", accessor -> new MovementAnimation(true, accessor, Armatures.HOGLIN));
      ILLAGER_IDLE = builder.nextAccessor("illager/idle", accessor -> new StaticAnimation(true, accessor, Armatures.BIPED));
      ILLAGER_WALK = builder.nextAccessor("illager/walk", accessor -> new MovementAnimation(true, accessor, Armatures.BIPED));
      VINDICATOR_IDLE_AGGRESSIVE = builder.nextAccessor("illager/idle_aggressive", accessor -> new StaticAnimation(true, accessor, Armatures.BIPED));
      VINDICATOR_CHASE = builder.nextAccessor("illager/chase", accessor -> new MovementAnimation(true, accessor, Armatures.BIPED));
      EVOKER_CAST_SPELL = builder.nextAccessor("illager/spellcast", accessor -> new StaticAnimation(true, accessor, Armatures.BIPED));
      RAVAGER_IDLE = builder.nextAccessor("ravager/idle", accessor -> new StaticAnimation(true, accessor, Armatures.RAVAGER));
      RAVAGER_WALK = builder.nextAccessor("ravager/walk", accessor -> new MovementAnimation(true, accessor, Armatures.RAVAGER));
      VEX_IDLE = builder.nextAccessor("vex/idle", accessor -> new StaticAnimation(true, accessor, Armatures.VEX));
      VEX_FLIPPING = builder.nextAccessor("vex/flip", accessor -> new StaticAnimation(0.05F, true, accessor, Armatures.VEX));
      PIGLIN_IDLE = builder.nextAccessor("piglin/idle", accessor -> new StaticAnimation(true, accessor, Armatures.PIGLIN));
      PIGLIN_WALK = builder.nextAccessor("piglin/walk", accessor -> new MovementAnimation(true, accessor, Armatures.PIGLIN));
      PIGLIN_ZOMBIFIED_IDLE = builder.nextAccessor("piglin/zombified_idle", accessor -> new StaticAnimation(true, accessor, Armatures.PIGLIN));
      PIGLIN_ZOMBIFIED_WALK = builder.nextAccessor("piglin/zombified_walk", accessor -> new MovementAnimation(true, accessor, Armatures.PIGLIN));
      PIGLIN_ZOMBIFIED_CHASE = builder.nextAccessor("piglin/zombified_chase", accessor -> new MovementAnimation(true, accessor, Armatures.PIGLIN));
      PIGLIN_CELEBRATE1 = builder.nextAccessor("piglin/celebrate1", accessor -> new StaticAnimation(true, accessor, Armatures.PIGLIN));
      PIGLIN_CELEBRATE2 = builder.nextAccessor("piglin/celebrate2", accessor -> new StaticAnimation(true, accessor, Armatures.PIGLIN));
      PIGLIN_CELEBRATE3 = builder.nextAccessor("piglin/celebrate3", accessor -> new StaticAnimation(true, accessor, Armatures.PIGLIN));
      PIGLIN_ADMIRE = builder.nextAccessor("piglin/admire", accessor -> new StaticAnimation(true, accessor, Armatures.PIGLIN));
      WITHER_IDLE = builder.nextAccessor("wither/idle", accessor -> new StaticAnimation(true, accessor, Armatures.WITHER));
      SPEAR_GUARD = builder.nextAccessor("biped/skill/guard_spear", accessor -> new StaticAnimation(true, accessor, Armatures.BIPED));
      SWORD_GUARD = builder.nextAccessor("biped/skill/guard_sword", accessor -> new StaticAnimation(true, accessor, Armatures.BIPED));
      SWORD_DUAL_GUARD = builder.nextAccessor("biped/skill/guard_dualsword", accessor -> new StaticAnimation(true, accessor, Armatures.BIPED));
      GREATSWORD_GUARD = builder.nextAccessor("biped/skill/guard_greatsword", accessor -> new StaticAnimation(0.25F, true, accessor, Armatures.BIPED));
      UCHIGATANA_GUARD = builder.nextAccessor("biped/skill/guard_uchigatana", accessor -> new StaticAnimation(0.25F, true, accessor, Armatures.BIPED));
      LONGSWORD_GUARD = builder.nextAccessor("biped/skill/guard_longsword", accessor -> new StaticAnimation(0.25F, true, accessor, Armatures.BIPED));
      STEEL_WHIRLWIND_CHARGING = builder.nextAccessor(
         "biped/skill/steel_whirlwind_charging",
         accessor -> new StaticAnimation(0.15F, false, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CHARGING)
      );
      BIPED_ROLL_FORWARD = builder.nextAccessor(
         "biped/skill/roll_forward",
         accessor -> new DodgeAnimation(0.1F, accessor, 0.6F, 0.8F, Armatures.BIPED)
            .addEvents(
               AnimationEvent.InTimeEvent.create(0.0F, Animations.ReusableSources.PLAY_SOUND, AnimationEvent.Side.CLIENT)
                  .params((SoundEvent)EpicFightSounds.ROLL.get())
            )
      );
      BIPED_ROLL_BACKWARD = builder.nextAccessor(
         "biped/skill/roll_backward",
         accessor -> new DodgeAnimation(0.1F, accessor, 0.6F, 0.8F, Armatures.BIPED)
            .addEvents(
               AnimationEvent.InTimeEvent.create(0.0F, Animations.ReusableSources.PLAY_SOUND, AnimationEvent.Side.CLIENT)
                  .params((SoundEvent)EpicFightSounds.ROLL.get())
            )
      );
      BIPED_STEP_FORWARD = builder.nextAccessor(
         "biped/skill/step_forward",
         accessor -> new DodgeAnimation(0.1F, 0.35F, accessor, 0.6F, 1.65F, Armatures.BIPED)
            .<Boolean, StaticAnimation>addState(EntityState.LOCKON_ROTATE, true)
            .<StaticAnimation>newTimePair(0.0F, 0.2F)
            .<Boolean, StaticAnimation>addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, false)
            .<Boolean, StaticAnimation>addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, false)
            .addEvents(AnimationEvent.InTimeEvent.create(0.0F, Animations.ReusableSources.PLAY_STEPPING_SOUND, AnimationEvent.Side.CLIENT))
      );
      BIPED_STEP_BACKWARD = builder.nextAccessor(
         "biped/skill/step_backward",
         accessor -> new DodgeAnimation(0.1F, 0.35F, accessor, 0.6F, 1.65F, Armatures.BIPED)
            .<Boolean, StaticAnimation>addState(EntityState.LOCKON_ROTATE, true)
            .<StaticAnimation>newTimePair(0.0F, 0.2F)
            .<Boolean, StaticAnimation>addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, false)
            .<Boolean, StaticAnimation>addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, false)
            .addEvents(AnimationEvent.InTimeEvent.create(0.0F, Animations.ReusableSources.PLAY_STEPPING_SOUND, AnimationEvent.Side.CLIENT))
      );
      BIPED_STEP_LEFT = builder.nextAccessor(
         "biped/skill/step_left",
         accessor -> new DodgeAnimation(0.1F, 0.35F, accessor, 0.6F, 1.65F, Armatures.BIPED)
            .<Boolean, StaticAnimation>addState(EntityState.LOCKON_ROTATE, true)
            .<StaticAnimation>newTimePair(0.0F, 0.2F)
            .<Boolean, StaticAnimation>addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, false)
            .<Boolean, StaticAnimation>addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, false)
            .addEvents(AnimationEvent.InTimeEvent.create(0.0F, Animations.ReusableSources.PLAY_STEPPING_SOUND, AnimationEvent.Side.CLIENT))
      );
      BIPED_STEP_RIGHT = builder.nextAccessor(
         "biped/skill/step_right",
         accessor -> new DodgeAnimation(0.1F, 0.35F, accessor, 0.6F, 1.65F, Armatures.BIPED)
            .<Boolean, StaticAnimation>addState(EntityState.LOCKON_ROTATE, true)
            .<StaticAnimation>newTimePair(0.0F, 0.2F)
            .<Boolean, StaticAnimation>addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, false)
            .<Boolean, StaticAnimation>addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, false)
            .addEvents(AnimationEvent.InTimeEvent.create(0.0F, Animations.ReusableSources.PLAY_STEPPING_SOUND, AnimationEvent.Side.CLIENT))
      );
      BIPED_KNOCKDOWN_WAKEUP_LEFT = builder.nextAccessor(
         "biped/skill/knockdown_wakeup_left", accessor -> new DodgeAnimation(0.1F, accessor, 0.8F, 0.6F, Armatures.BIPED)
      );
      BIPED_KNOCKDOWN_WAKEUP_RIGHT = builder.nextAccessor(
         "biped/skill/knockdown_wakeup_right", accessor -> new DodgeAnimation(0.1F, accessor, 0.8F, 0.6F, Armatures.BIPED)
      );
      BIPED_DEMOLITION_LEAP_CHARGING = builder.nextAccessor(
         "biped/skill/demolition_leap_charge",
         accessor -> new ActionAnimation(0.15F, accessor, Armatures.BIPED)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.STOP_MOVEMENT, true)
            .<StaticAnimation, AnimationProperty.PlaybackSpeedModifier>addProperty(
               AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CHARGING
            )
            .<StaticAnimation, AnimationProperty.PoseModifier>addProperty(
               AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, (self, pose, entitypatch, time, partialTicks) -> {
                  if (self.isStaticAnimation()) {
                     float xRot = Mth.m_14036_(entitypatch.getOriginal().m_146909_(), -60.0F, 50.0F);
                     JointTransform head = pose.orElseEmpty("Head");
                     MathUtils.mulQuaternion(QuaternionUtils.XP.rotationDegrees(xRot), head.rotation(), head.rotation());
                  }
               }
            )
            .<StaticAnimation, LivingMotion>addProperty(AnimationProperty.StaticAnimationProperty.RESET_LIVING_MOTION, LivingMotions.IDLE)
            .<StaticAnimation>newTimePair(0.0F, Float.MAX_VALUE)
            .<Boolean, StaticAnimation>addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, true)
            .addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, true)
      );
      BIPED_DEMOLITION_LEAP = builder.nextAccessor(
         "biped/skill/demolition_leap",
         accessor -> new ActionAnimation(0.05F, 0.4F, accessor, Armatures.BIPED)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.SYNC_CAMERA, true)
            .<StaticAnimation>newTimePair(0.0F, 1.0F)
            .addStateRemoveOld(EntityState.UPDATE_LIVING_MOTION, false)
      );
      BIPED_PHANTOM_ASCENT_FORWARD = builder.nextAccessor(
         "biped/skill/phantom_ascent_forward",
         accessor -> new ActionAnimation(0.05F, 0.7F, accessor, Armatures.BIPED)
            .<Boolean, StaticAnimation>addStateRemoveOld(EntityState.MOVEMENT_LOCKED, false)
            .<StaticAnimation>newTimePair(0.0F, 0.5F)
            .<Boolean, StaticAnimation>addStateRemoveOld(EntityState.INACTION, true)
            .addEvents(
               AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS,
               AnimationEvent.SimpleEvent.create(
                  (entitypatch, animation, params) -> {
                     Vec3 pos = ((LivingEntity)entitypatch.getOriginal()).m_20182_();
                     entitypatch.playSound((SoundEvent)EpicFightSounds.TUMBLE.get(), 0.0F, 0.0F);
                     ((LivingEntity)entitypatch.getOriginal())
                        .m_9236_()
                        .m_7107_(
                           (ParticleOptions)EpicFightParticles.AIR_BURST.get(),
                           pos.f_82479_,
                           pos.f_82480_ + ((LivingEntity)entitypatch.getOriginal()).m_20206_() * 0.5,
                           pos.f_82481_,
                           0.0,
                           -1.0,
                           2.0
                        );
                  },
                  AnimationEvent.Side.CLIENT
               )
            )
      );
      BIPED_PHANTOM_ASCENT_BACKWARD = builder.nextAccessor(
         "biped/skill/phantom_ascent_backward",
         accessor -> new ActionAnimation(0.05F, 0.7F, accessor, Armatures.BIPED)
            .<Boolean, StaticAnimation>addStateRemoveOld(EntityState.MOVEMENT_LOCKED, false)
            .<StaticAnimation>newTimePair(0.0F, 0.5F)
            .<Boolean, StaticAnimation>addStateRemoveOld(EntityState.INACTION, true)
            .addEvents(
               AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS,
               AnimationEvent.SimpleEvent.create(
                  (entitypatch, animation, params) -> {
                     Vec3 pos = ((LivingEntity)entitypatch.getOriginal()).m_20182_();
                     entitypatch.playSound((SoundEvent)EpicFightSounds.TUMBLE.get(), 0.0F, 0.0F);
                     ((LivingEntity)entitypatch.getOriginal())
                        .m_9236_()
                        .m_7107_(
                           (ParticleOptions)EpicFightParticles.AIR_BURST.get(),
                           pos.f_82479_,
                           pos.f_82480_ + ((LivingEntity)entitypatch.getOriginal()).m_20206_() * 0.5,
                           pos.f_82481_,
                           0.0,
                           -1.0,
                           2.0
                        );
                  },
                  AnimationEvent.Side.CLIENT
               )
            )
      );
      FIST_AUTO1 = builder.nextAccessor(
         "biped/combat/fist_auto1",
         accessor -> new BasicAttackAnimation(
               0.08F, 0.05F, 0.15F, 0.15F, InteractionHand.OFF_HAND, null, Armatures.BIPED.get().toolL, accessor, Armatures.BIPED
            )
            .<RegistryObject<HitParticleType>, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLUNT)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 3.2F)
      );
      FIST_AUTO2 = builder.nextAccessor(
         "biped/combat/fist_auto2",
         accessor -> new BasicAttackAnimation(0.08F, 0.05F, 0.15F, 0.15F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .<RegistryObject<HitParticleType>, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLUNT)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 3.2F)
      );
      FIST_AUTO3 = builder.nextAccessor(
         "biped/combat/fist_auto3",
         accessor -> new BasicAttackAnimation(0.08F, 0.05F, 0.15F, 0.5F, InteractionHand.OFF_HAND, null, Armatures.BIPED.get().toolL, accessor, Armatures.BIPED)
            .<RegistryObject<HitParticleType>, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLUNT)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 3.2F)
      );
      FIST_DASH = builder.nextAccessor(
         "biped/combat/fist_dash",
         accessor -> new DashAttackAnimation(0.06F, 0.05F, 0.15F, 0.3F, 0.7F, null, Armatures.BIPED.get().shoulderR, accessor, Armatures.BIPED)
            .<RegistryObject<HitParticleType>, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLUNT)
            .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)
      );
      SWORD_AUTO1 = builder.nextAccessor(
         "biped/combat/sword_auto1",
         accessor -> new BasicAttackAnimation(0.1F, 0.0F, 0.1F, 0.4F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F)
      );
      SWORD_AUTO2 = builder.nextAccessor(
         "biped/combat/sword_auto2",
         accessor -> new BasicAttackAnimation(0.1F, 0.05F, 0.15F, 0.4F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F)
      );
      SWORD_AUTO3 = builder.nextAccessor(
         "biped/combat/sword_auto3",
         accessor -> new BasicAttackAnimation(0.1F, 0.05F, 0.15F, 0.6F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F)
      );
      SWORD_DASH = builder.nextAccessor(
         "biped/combat/sword_dash",
         accessor -> new DashAttackAnimation(0.1F, 0.1F, 0.15F, 0.25F, 0.65F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED, true)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F)
      );
      GREATSWORD_AUTO1 = builder.nextAccessor(
         "biped/combat/greatsword_auto1",
         accessor -> new BasicAttackAnimation(0.25F, 0.15F, 0.25F, 0.65F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .<StaticAnimation, Float>addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F)
            .<StaticAnimation>newTimePair(0.0F, 0.5F)
            .addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, false)
      );
      GREATSWORD_AUTO2 = builder.nextAccessor(
         "biped/combat/greatsword_auto2",
         accessor -> new BasicAttackAnimation(0.1F, 0.5F, 0.65F, 1.5F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .<StaticAnimation, Float>addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F)
            .<StaticAnimation>newTimePair(0.0F, 0.5F)
            .addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, false)
      );
      GREATSWORD_DASH = builder.nextAccessor(
         "biped/combat/greatsword_dash",
         accessor -> new DashAttackAnimation(0.2F, 0.2F, 0.35F, 0.6F, 1.2F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED, false)
            .<Set<TagKey<DamageType>>, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.FINISHER))
            .<StaticAnimation, Float>addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, false)
            .addEvents(
               AnimationEvent.InTimeEvent.create(0.4F, Animations.ReusableSources.FRACTURE_GROUND_SIMPLE, AnimationEvent.Side.CLIENT)
                  .params(new Vec3f(0.0F, -0.24F, -2.0F), Armatures.BIPED.get().toolR, 1.1, 0.55F)
            )
      );
      SPEAR_ONEHAND_AUTO = builder.nextAccessor(
         "biped/combat/spear_onehand_auto",
         accessor -> new BasicAttackAnimation(0.1F, 0.35F, 0.45F, 0.75F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F)
      );
      SPEAR_TWOHAND_AUTO1 = builder.nextAccessor(
         "biped/combat/spear_twohand_auto1",
         accessor -> new BasicAttackAnimation(0.1F, 0.2F, 0.3F, 0.45F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .<StaticAnimation, Float>addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F)
            .<StaticAnimation>newTimePair(0.0F, 0.55F)
            .addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, false)
      );
      SPEAR_TWOHAND_AUTO2 = builder.nextAccessor(
         "biped/combat/spear_twohand_auto2",
         accessor -> new BasicAttackAnimation(0.1F, 0.2F, 0.3F, 0.7F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F)
      );
      SPEAR_DASH = builder.nextAccessor(
         "biped/combat/spear_dash",
         accessor -> new DashAttackAnimation(0.1F, 0.25F, 0.3F, 0.4F, 0.8F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED, true)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F)
      );
      TOOL_AUTO1 = builder.nextAccessor(
         "biped/combat/tool_auto1",
         accessor -> new BasicAttackAnimation(0.13F, 0.05F, 0.15F, 0.3F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .setResourceLocation("epicfight", "biped/combat/sword_auto1")
      );
      TOOL_AUTO2 = builder.nextAccessor(
         "biped/combat/sword_auto4",
         accessor -> new BasicAttackAnimation(0.13F, 0.05F, 0.15F, 0.4F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
      );
      TOOL_DASH = builder.nextAccessor(
         "biped/combat/tool_dash",
         accessor -> new DashAttackAnimation(0.16F, 0.08F, 0.15F, 0.25F, 0.58F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED, true)
            .addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(1.0F))
      );
      AXE_DASH = builder.nextAccessor(
         "biped/combat/axe_dash",
         accessor -> new DashAttackAnimation(0.25F, 0.08F, 0.4F, 0.46F, 0.9F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED, true)
      );
      SWORD_DUAL_AUTO1 = builder.nextAccessor(
         "biped/combat/sword_dual_auto1",
         accessor -> new BasicAttackAnimation(0.08F, 0.1F, 0.2F, 0.3F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .<StaticAnimation, Float>addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F)
            .<StaticAnimation>newTimePair(0.0F, 0.2F)
            .addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, false)
      );
      SWORD_DUAL_AUTO2 = builder.nextAccessor(
         "biped/combat/sword_dual_auto2",
         accessor -> new BasicAttackAnimation(0.1F, 0.1F, 0.2F, 0.3F, InteractionHand.OFF_HAND, null, Armatures.BIPED.get().toolL, accessor, Armatures.BIPED)
            .<StaticAnimation, Float>addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F)
            .<StaticAnimation>newTimePair(0.0F, 0.2F)
            .addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, false)
      );
      SWORD_DUAL_AUTO3 = builder.nextAccessor(
         "biped/combat/sword_dual_auto3",
         accessor -> new BasicAttackAnimation(
               0.1F,
               accessor,
               Armatures.BIPED,
               new AttackAnimation.Phase(
                  0.0F,
                  0.25F,
                  0.25F,
                  0.35F,
                  0.6F,
                  Float.MAX_VALUE,
                  InteractionHand.MAIN_HAND,
                  AttackAnimation.JointColliderPair.of(Armatures.BIPED.get().toolR, null),
                  AttackAnimation.JointColliderPair.of(Armatures.BIPED.get().toolL, null)
               )
            )
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F)
      );
      SWORD_DUAL_DASH = builder.nextAccessor(
         "biped/combat/sword_dual_dash",
         accessor -> new DashAttackAnimation(
               0.16F,
               accessor,
               Armatures.BIPED,
               new AttackAnimation.Phase(
                  0.0F,
                  0.05F,
                  0.05F,
                  0.3F,
                  0.75F,
                  Float.MAX_VALUE,
                  InteractionHand.MAIN_HAND,
                  AttackAnimation.JointColliderPair.of(Armatures.BIPED.get().toolR, null),
                  AttackAnimation.JointColliderPair.of(Armatures.BIPED.get().toolL, null)
               )
            )
            .<StaticAnimation, Float>addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F)
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(
               AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.RAW_COORD
            )
            .addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, null)
      );
      UCHIGATANA_AUTO1 = builder.nextAccessor(
         "biped/combat/uchigatana_auto1",
         accessor -> new BasicAttackAnimation(0.05F, 0.15F, 0.25F, 0.3F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.0F)
      );
      UCHIGATANA_AUTO2 = builder.nextAccessor(
         "biped/combat/uchigatana_auto2",
         accessor -> new BasicAttackAnimation(0.05F, 0.2F, 0.3F, 0.3F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.0F)
      );
      UCHIGATANA_AUTO3 = builder.nextAccessor(
         "biped/combat/uchigatana_auto3",
         accessor -> new BasicAttackAnimation(0.1F, 0.15F, 0.25F, 0.5F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.0F)
      );
      UCHIGATANA_DASH = builder.nextAccessor(
         "biped/combat/uchigatana_dash",
         accessor -> new DashAttackAnimation(0.1F, 0.05F, 0.05F, 0.15F, 0.6F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED, true)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.0F)
      );
      UCHIGATANA_SHEATHING_AUTO = builder.nextAccessor(
         "biped/combat/uchigatana_sheath_auto",
         accessor -> new BasicAttackAnimation(0.05F, 0.0F, 0.1F, 0.65F, ColliderPreset.BATTOJUTSU, Armatures.BIPED.get().rootJoint, accessor, Armatures.BIPED)
            .<ValueModifier, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(30.0F))
            .<ValueModifier, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(2.0F))
            .<ValueModifier, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(3.0F))
            .addProperty(AnimationProperty.AttackPhaseProperty.SWING_SOUND, (SoundEvent)EpicFightSounds.WHOOSH_SHARP.get())
      );
      UCHIGATANA_SHEATHING_DASH = builder.nextAccessor(
         "biped/combat/uchigatana_sheath_dash",
         accessor -> new DashAttackAnimation(
               0.05F, 0.05F, 0.2F, 0.35F, 0.65F, ColliderPreset.BATTOJUTSU_DASH, Armatures.BIPED.get().rootJoint, accessor, Armatures.BIPED
            )
            .<ValueModifier, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(30.0F))
            .<ValueModifier, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(2.0F))
            .<ValueModifier, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(3.0F))
            .addProperty(AnimationProperty.AttackPhaseProperty.SWING_SOUND, (SoundEvent)EpicFightSounds.WHOOSH_SHARP.get())
      );
      AXE_AUTO1 = builder.nextAccessor(
         "biped/combat/axe_auto1",
         accessor -> new BasicAttackAnimation(0.15F, 0.05F, 0.15F, 0.7F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
      );
      AXE_AUTO2 = builder.nextAccessor(
         "biped/combat/axe_auto2",
         accessor -> new BasicAttackAnimation(0.15F, 0.05F, 0.15F, 0.85F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
      );
      LONGSWORD_AUTO1 = builder.nextAccessor(
         "biped/combat/longsword_auto1",
         accessor -> new BasicAttackAnimation(0.1F, 0.25F, 0.35F, 0.5F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F)
      );
      LONGSWORD_AUTO2 = builder.nextAccessor(
         "biped/combat/longsword_auto2",
         accessor -> new BasicAttackAnimation(0.15F, 0.2F, 0.3F, 0.45F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F)
      );
      LONGSWORD_AUTO3 = builder.nextAccessor(
         "biped/combat/longsword_auto3",
         accessor -> new BasicAttackAnimation(0.05F, 0.2F, 0.3F, 1.0F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F)
      );
      LONGSWORD_DASH = builder.nextAccessor(
         "biped/combat/longsword_dash",
         accessor -> new DashAttackAnimation(0.1F, 0.1F, 0.25F, 0.4F, 0.75F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED, true)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F)
      );
      LONGSWORD_LIECHTENAUER_AUTO1 = builder.nextAccessor(
         "biped/combat/longsword_liechtenauer_auto1",
         accessor -> new BasicAttackAnimation(0.1F, 0.15F, 0.25F, 0.5F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F)
      );
      LONGSWORD_LIECHTENAUER_AUTO2 = builder.nextAccessor(
         "biped/combat/longsword_liechtenauer_auto2",
         accessor -> new BasicAttackAnimation(0.1F, 0.2F, 0.3F, 0.5F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F)
      );
      LONGSWORD_LIECHTENAUER_AUTO3 = builder.nextAccessor(
         "biped/combat/longsword_liechtenauer_auto3",
         accessor -> new BasicAttackAnimation(0.25F, 0.1F, 0.2F, 0.7F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F)
      );
      TACHI_AUTO1 = builder.nextAccessor(
         "biped/combat/tachi_auto1",
         accessor -> new BasicAttackAnimation(0.1F, 0.35F, 0.4F, 0.55F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .<StaticAnimation, Float>addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F)
            .addProperty(AnimationProperty.AttackAnimationProperty.EXTRA_COLLIDERS, 3)
      );
      TACHI_AUTO2 = builder.nextAccessor(
         "biped/combat/tachi_auto2",
         accessor -> new BasicAttackAnimation(0.15F, 0.2F, 0.3F, 0.5F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F)
      );
      TACHI_AUTO3 = builder.nextAccessor(
         "biped/combat/tachi_auto3",
         accessor -> new BasicAttackAnimation(0.15F, 0.2F, 0.3F, 0.85F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F)
      );
      TACHI_DASH = builder.nextAccessor(
         "biped/combat/tachi_dash",
         accessor -> new DashAttackAnimation(0.1F, 0.3F, 0.3F, 0.4F, 1.0F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED, true)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F)
      );
      DAGGER_AUTO1 = builder.nextAccessor(
         "biped/combat/dagger_auto1",
         accessor -> new BasicAttackAnimation(0.05F, 0.05F, 0.15F, 0.25F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F)
      );
      DAGGER_AUTO2 = builder.nextAccessor(
         "biped/combat/dagger_auto2",
         accessor -> new BasicAttackAnimation(0.05F, 0.0F, 0.1F, 0.25F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F)
      );
      DAGGER_AUTO3 = builder.nextAccessor(
         "biped/combat/dagger_auto3",
         accessor -> new BasicAttackAnimation(0.05F, 0.2F, 0.25F, 0.4F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F)
      );
      DAGGER_DASH = builder.nextAccessor(
         "biped/combat/dagger_dash",
         accessor -> new DashAttackAnimation(0.05F, 0.1F, 0.2F, 0.25F, 0.6F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED, true)
            .<StaticAnimation, Float>addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F)
            .<StaticAnimation>newTimePair(0.0F, 0.4F)
            .<Boolean, StaticAnimation>addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, false)
            .<StaticAnimation>newConditionalTimePair(entitypatch -> entitypatch.isLastAttackSuccess() ? 1 : 0, 0.4F, 0.6F)
            .<Boolean, StaticAnimation>addConditionalState(0, EntityState.CAN_BASIC_ATTACK, false)
            .addConditionalState(1, EntityState.CAN_BASIC_ATTACK, true)
      );
      DAGGER_DUAL_AUTO1 = builder.nextAccessor(
         "biped/combat/dagger_dual_auto1",
         accessor -> new BasicAttackAnimation(0.05F, 0.1F, 0.2F, 0.25F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F)
      );
      DAGGER_DUAL_AUTO2 = builder.nextAccessor(
         "biped/combat/dagger_dual_auto2",
         accessor -> new BasicAttackAnimation(0.05F, 0.0F, 0.1F, 0.16F, InteractionHand.OFF_HAND, null, Armatures.BIPED.get().toolL, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F)
      );
      DAGGER_DUAL_AUTO3 = builder.nextAccessor(
         "biped/combat/dagger_dual_auto3",
         accessor -> new BasicAttackAnimation(0.05F, 0.0F, 0.1F, 0.2F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F)
      );
      DAGGER_DUAL_AUTO4 = builder.nextAccessor(
         "biped/combat/dagger_dual_auto4",
         accessor -> new BasicAttackAnimation(
               0.15F,
               accessor,
               Armatures.BIPED,
               new AttackAnimation.Phase(0.0F, 0.1F, 0.1F, 0.2F, 0.2F, 0.2F, InteractionHand.OFF_HAND, Armatures.BIPED.get().toolL, null),
               new AttackAnimation.Phase(0.2F, 0.2F, 0.3F, 0.6F, 0.6F, Armatures.BIPED.get().toolR, null)
            )
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F)
      );
      DAGGER_DUAL_DASH = builder.nextAccessor(
         "biped/combat/dagger_dual_dash",
         accessor -> new DashAttackAnimation(
               0.1F,
               accessor,
               Armatures.BIPED,
               new AttackAnimation.Phase(
                  0.0F,
                  0.1F,
                  0.2F,
                  0.3F,
                  0.65F,
                  Float.MAX_VALUE,
                  InteractionHand.MAIN_HAND,
                  AttackAnimation.JointColliderPair.of(Armatures.BIPED.get().toolR, null),
                  AttackAnimation.JointColliderPair.of(Armatures.BIPED.get().toolL, null)
               )
            )
            .<StaticAnimation, Float>addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F)
            .addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
      );
      TRIDENT_AUTO1 = builder.nextAccessor(
         "biped/combat/trident_auto1",
         accessor -> new BasicAttackAnimation(0.3F, 0.05F, 0.16F, 0.45F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
      );
      TRIDENT_AUTO2 = builder.nextAccessor(
         "biped/combat/trident_auto2",
         accessor -> new BasicAttackAnimation(0.05F, 0.25F, 0.36F, 0.55F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
      );
      TRIDENT_AUTO3 = builder.nextAccessor(
         "biped/combat/trident_auto3",
         accessor -> new BasicAttackAnimation(0.2F, 0.3F, 0.46F, 0.9F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
      );
      SWORD_AIR_SLASH = builder.nextAccessor(
         "biped/combat/sword_airslash",
         accessor -> new AirSlashAnimation(0.1F, 0.15F, 0.26F, 0.5F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
      );
      SWORD_DUAL_AIR_SLASH = builder.nextAccessor(
         "biped/combat/sword_dual_airslash",
         accessor -> new AirSlashAnimation(
            0.1F, 0.15F, 0.26F, 0.5F, ColliderPreset.DUAL_SWORD_AIR_SLASH, Armatures.BIPED.get().torso, accessor, Armatures.BIPED
         )
      );
      UCHIGATANA_AIR_SLASH = builder.nextAccessor(
         "biped/combat/uchigatana_airslash",
         accessor -> new AirSlashAnimation(0.1F, 0.05F, 0.16F, 0.3F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
      );
      UCHIGATANA_SHEATH_AIR_SLASH = builder.nextAccessor(
         "biped/combat/uchigatana_sheath_airslash",
         accessor -> new AirSlashAnimation(0.1F, 0.1F, 0.16F, 0.3F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .<ValueModifier, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(30.0F))
            .<ValueModifier, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(2.0F))
            .<SoundEvent, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.SWING_SOUND, (SoundEvent)EpicFightSounds.WHOOSH_SHARP.get())
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.0F)
      );
      SPEAR_ONEHAND_AIR_SLASH = builder.nextAccessor(
         "biped/combat/spear_onehand_airslash",
         accessor -> new AirSlashAnimation(0.1F, 0.15F, 0.26F, 0.4F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
      );
      SPEAR_TWOHAND_AIR_SLASH = builder.nextAccessor(
         "biped/combat/spear_twohand_airslash",
         accessor -> new AirSlashAnimation(0.1F, 0.25F, 0.36F, 0.6F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.FINISHER))
      );
      LONGSWORD_AIR_SLASH = builder.nextAccessor(
         "biped/combat/longsword_airslash",
         accessor -> new AirSlashAnimation(0.1F, 0.3F, 0.41F, 0.5F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
      );
      GREATSWORD_AIR_SLASH = builder.nextAccessor(
         "biped/combat/greatsword_airslash",
         accessor -> new AirSlashAnimation(0.1F, 0.5F, 0.55F, 0.71F, 0.75F, false, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.FINISHER))
      );
      FIST_AIR_SLASH = builder.nextAccessor(
         "biped/combat/fist_airslash",
         accessor -> new AirSlashAnimation(0.1F, 0.15F, 0.26F, 0.4F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 4.0F)
      );
      DAGGER_AIR_SLASH = builder.nextAccessor(
         "biped/combat/dagger_airslash",
         accessor -> new AirSlashAnimation(0.1F, 0.15F, 0.26F, 0.45F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F)
      );
      DAGGER_DUAL_AIR_SLASH = builder.nextAccessor(
         "biped/combat/dagger_dual_airslash",
         accessor -> new AirSlashAnimation(
               0.1F, 0.15F, 0.26F, 0.4F, ColliderPreset.DUAL_DAGGER_AIR_SLASH, Armatures.BIPED.get().torso, accessor, Armatures.BIPED
            )
            .<StaticAnimation, Float>addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.0F)
            .setResourceLocation("epicfight", "biped/combat/sword_dual_airslash")
      );
      AXE_AIRSLASH = builder.nextAccessor(
         "biped/combat/axe_airslash", accessor -> new AirSlashAnimation(0.1F, 0.3F, 0.4F, 0.65F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
      );
      SWORD_MOUNT_ATTACK = builder.nextAccessor(
         "biped/combat/sword_mount_attack",
         accessor -> new MountAttackAnimation(0.16F, 0.1F, 0.2F, 0.25F, 0.7F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
      );
      SPEAR_MOUNT_ATTACK = builder.nextAccessor(
         "biped/combat/spear_mount_attack",
         accessor -> new MountAttackAnimation(0.16F, 0.38F, 0.38F, 0.45F, 0.8F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      BIPED_MOB_ONEHAND1 = builder.nextAccessor(
         "biped/combat/mob_onehand1",
         accessor -> new AttackAnimation(0.08F, 0.45F, 0.55F, 0.66F, 0.95F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      BIPED_MOB_ONEHAND2 = builder.nextAccessor(
         "biped/combat/mob_onehand2",
         accessor -> new AttackAnimation(0.08F, 0.45F, 0.5F, 0.61F, 0.95F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      BIPED_MOB_GREATSWORD = builder.nextAccessor(
         "biped/combat/mob_greatsword1",
         accessor -> new AttackAnimation(0.15F, 0.45F, 0.85F, 0.95F, 2.2F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .<StunType, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      BIPED_MOB_TACHI = builder.nextAccessor(
         "biped/combat/mob_tachi_special",
         accessor -> new AttackAnimation(0.15F, 0.15F, 0.25F, 0.35F, 1.0F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      BIPED_MOB_SPEAR_ONEHAND = builder.nextAccessor(
         "biped/combat/mob_spear_onehand",
         accessor -> new AttackAnimation(0.15F, 0.15F, 0.4F, 0.5F, 1.1F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      BIPED_MOB_SPEAR_TWOHAND1 = builder.nextAccessor(
         "biped/combat/mob_spear_twohand1",
         accessor -> new AttackAnimation(0.15F, 0.15F, 0.4F, 0.5F, 1.0F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      BIPED_MOB_SPEAR_TWOHAND2 = builder.nextAccessor(
         "biped/combat/mob_spear_twohand2",
         accessor -> new AttackAnimation(0.15F, 0.15F, 0.4F, 0.5F, 1.0F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      BIPED_MOB_SPEAR_TWOHAND3 = builder.nextAccessor(
         "biped/combat/mob_spear_twohand3",
         accessor -> new AttackAnimation(0.15F, 0.15F, 0.4F, 0.5F, 1.0F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      BIPED_MOB_SWORD_DUAL1 = builder.nextAccessor(
         "biped/combat/mob_sword_dual1",
         accessor -> new AttackAnimation(
               0.1F,
               accessor,
               Armatures.BIPED,
               new AttackAnimation.Phase(0.0F, 0.35F, 0.4F, 0.5F, 0.55F, 0.55F, InteractionHand.OFF_HAND, Armatures.BIPED.get().toolL, null),
               new AttackAnimation.Phase(0.55F, 0.55F, 0.65F, 0.75F, 1.15F, Float.MAX_VALUE, Armatures.BIPED.get().toolR, null)
            )
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      BIPED_MOB_SWORD_DUAL2 = builder.nextAccessor(
         "biped/combat/mob_sword_dual2",
         accessor -> new AttackAnimation(
               0.1F,
               accessor,
               Armatures.BIPED,
               new AttackAnimation.Phase(0.0F, 0.3F, 0.3F, 0.45F, 0.55F, 0.55F, InteractionHand.OFF_HAND, Armatures.BIPED.get().toolL, null),
               new AttackAnimation.Phase(0.55F, 0.55F, 0.65F, 0.75F, 1.15F, Float.MAX_VALUE, Armatures.BIPED.get().toolR, null)
            )
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      BIPED_MOB_SWORD_DUAL3 = builder.nextAccessor(
         "biped/combat/mob_sword_dual3",
         accessor -> new AttackAnimation(0.1F, 0.25F, 0.85F, 0.95F, 1.4F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
      );
      BIPED_MOB_LONGSWORD1 = builder.nextAccessor(
         "biped/combat/mob_longsword1",
         accessor -> new AttackAnimation(
               0.15F,
               accessor,
               Armatures.BIPED,
               new AttackAnimation.Phase(0.0F, 0.15F, 0.25F, 0.35F, 0.45F, 0.65F, Armatures.BIPED.get().toolR, null),
               new AttackAnimation.Phase(0.65F, 0.85F, 1.0F, 1.1F, 1.55F, Float.MAX_VALUE, Armatures.BIPED.get().toolR, null)
            )
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      BIPED_MOB_LONGSWORD2 = builder.nextAccessor(
         "biped/combat/mob_longsword2",
         accessor -> new AttackAnimation(0.25F, 0.3F, 0.45F, 0.55F, 1.0F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      BIPED_MOB_UCHIGATANA1 = builder.nextAccessor(
         "biped/combat/mob_uchigatana1",
         accessor -> new AttackAnimation(0.05F, 0.3F, 0.2F, 0.3F, 0.7F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      BIPED_MOB_UCHIGATANA2 = builder.nextAccessor(
         "biped/combat/mob_uchigatana2",
         accessor -> new AttackAnimation(0.15F, 0.01F, 0.01F, 0.1F, 0.55F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      BIPED_MOB_UCHIGATANA3 = builder.nextAccessor(
         "biped/combat/mob_uchigatana3",
         accessor -> new AttackAnimation(0.15F, 0.01F, 0.1F, 0.2F, 0.7F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      BIPED_MOB_DAGGER_ONEHAND1 = builder.nextAccessor(
         "biped/combat/mob_dagger_onehand1",
         accessor -> new AttackAnimation(0.1F, 0.05F, 0.15F, 0.25F, 0.4F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      BIPED_MOB_DAGGER_ONEHAND2 = builder.nextAccessor(
         "biped/combat/mob_dagger_onehand2",
         accessor -> new AttackAnimation(0.1F, 0.05F, 0.01F, 0.1F, 0.45F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      BIPED_MOB_DAGGER_ONEHAND3 = builder.nextAccessor(
         "biped/combat/mob_dagger_onehand3",
         accessor -> new AttackAnimation(0.1F, 0.3F, 0.5F, 0.6F, 0.9F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      BIPED_MOB_DAGGER_TWOHAND1 = builder.nextAccessor(
         "biped/combat/mob_dagger_twohand1",
         accessor -> new AttackAnimation(
               0.15F,
               accessor,
               Armatures.BIPED,
               new AttackAnimation.Phase(0.0F, 0.0F, 0.05F, 0.15F, 0.3F, 0.3F, Armatures.BIPED.get().toolR, null),
               new AttackAnimation.Phase(0.3F, 0.3F, 0.3F, 0.4F, 0.5F, 0.5F, InteractionHand.OFF_HAND, Armatures.BIPED.get().toolL, null),
               new AttackAnimation.Phase(0.5F, 0.5F, 0.55F, 0.65F, 1.0F, Float.MAX_VALUE, Armatures.BIPED.get().toolR, null)
            )
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      BIPED_MOB_DAGGER_TWOHAND2 = builder.nextAccessor(
         "biped/combat/mob_dagger_twohand2",
         accessor -> new AttackAnimation(0.1F, 0.25F, 0.75F, 0.85F, 1.0F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      BIPED_MOB_THROW = builder.nextAccessor(
         "biped/combat/mob_throw",
         accessor -> new RangedAttackAnimation(0.11F, 0.1F, 0.45F, 0.49F, 0.95F, null, Armatures.BIPED.get().rootJoint, accessor, Armatures.BIPED)
      );
      SWORD_GUARD_HIT = builder.nextAccessor("biped/skill/guard_sword_hit", accessor -> new GuardAnimation(0.05F, accessor, Armatures.BIPED));
      SWORD_GUARD_ACTIVE_HIT1 = builder.nextAccessor(
         "biped/skill/guard_sword_hit_active1", accessor -> new GuardAnimation(0.05F, 0.2F, accessor, Armatures.BIPED)
      );
      SWORD_GUARD_ACTIVE_HIT2 = builder.nextAccessor(
         "biped/skill/guard_sword_hit_active2", accessor -> new GuardAnimation(0.05F, 0.2F, accessor, Armatures.BIPED)
      );
      SWORD_GUARD_ACTIVE_HIT3 = builder.nextAccessor(
         "biped/skill/guard_sword_hit_active3", accessor -> new GuardAnimation(0.05F, 0.2F, accessor, Armatures.BIPED)
      );
      LONGSWORD_GUARD_ACTIVE_HIT1 = builder.nextAccessor(
         "biped/skill/guard_longsword_hit_active1", accessor -> new GuardAnimation(0.05F, 0.2F, accessor, Armatures.BIPED)
      );
      LONGSWORD_GUARD_ACTIVE_HIT2 = builder.nextAccessor(
         "biped/skill/guard_longsword_hit_active2", accessor -> new GuardAnimation(0.05F, 0.2F, accessor, Armatures.BIPED)
      );
      SWORD_DUAL_GUARD_HIT = builder.nextAccessor("biped/skill/guard_dualsword_hit", accessor -> new GuardAnimation(0.05F, accessor, Armatures.BIPED));
      BIPED_COMMON_NEUTRALIZED = builder.nextAccessor("biped/skill/guard_break1", accessor -> new LongHitAnimation(0.05F, accessor, Armatures.BIPED));
      GREATSWORD_GUARD_BREAK = builder.nextAccessor("biped/skill/guard_break2", accessor -> new LongHitAnimation(0.05F, accessor, Armatures.BIPED));
      LONGSWORD_GUARD_HIT = builder.nextAccessor("biped/skill/guard_longsword_hit", accessor -> new GuardAnimation(0.05F, accessor, Armatures.BIPED));
      SPEAR_GUARD_HIT = builder.nextAccessor("biped/skill/guard_spear_hit", accessor -> new GuardAnimation(0.05F, accessor, Armatures.BIPED));
      GREATSWORD_GUARD_HIT = builder.nextAccessor("biped/skill/guard_greatsword_hit", accessor -> new GuardAnimation(0.05F, accessor, Armatures.BIPED));
      UCHIGATANA_GUARD_HIT = builder.nextAccessor("biped/skill/guard_uchigatana_hit", accessor -> new GuardAnimation(0.05F, accessor, Armatures.BIPED));
      METEOR_SLAM = builder.nextAccessor(
         "biped/skill/greatsword_slam",
         accessor -> new AttackAnimation(0.05F, 0.0F, 0.2F, 0.3F, 1.0F, ColliderPreset.GREATSWORD, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .<AttackAnimation>removeProperty(AnimationProperty.AttackPhaseProperty.SWING_SOUND)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.MOVE_ON_LINK, false)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.STOP_MOVEMENT, true)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(
               AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, (self, entitypatch, transformSheet) -> {
                  if (!self.isLinkAnimation()) {
                     HitResult hitResult = entitypatch.getOriginal().m_19907_(50.0, 1.0F, false);
                     Vec3 to = hitResult.m_82450_();
                     Vec3 from = entitypatch.getOriginal().m_20182_();
                     Vec3 correction = to.m_82546_(from).m_82541_().m_82490_(5.0);
                     TransformSheet correctedCoord = self.getCoord().getCorrectedModelCoord(entitypatch, from, to.m_82549_(correction), 0, 2);
                     transformSheet.readFrom(correctedCoord);
                  }
               }
            )
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, null)
            .<StaticAnimation, AnimationProperty.PlaybackSpeedModifier>addProperty(
               AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> {
                  if (0.2F > elapsedTime && entitypatch instanceof PlayerPatch<?> playerpatch) {
                     Optional<SkillContainer> skill = playerpatch.getSkillContainerFor(EpicFightSkills.METEOR_STRIKE);
                     if (skill.isPresent()) {
                        return (float)Math.sqrt(7.0F / MeteorSlamSkill.getFallDistance(skill.get()));
                     }
                  }

                  return 1.0F;
               }
            )
            .<StaticAnimation>addEvents(
               AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS,
               AnimationEvent.SimpleEvent.create(
                  (entitypatch, animation, params) -> entitypatch.playSound((SoundEvent)EpicFightSounds.ENTITY_MOVE.get(), 1.0F, 0.0F, 0.0F),
                  AnimationEvent.Side.CLIENT
               )
            )
            .addEvents(
               AnimationEvent.InTimeEvent.create(0.25F, Animations.ReusableSources.FRACTURE_METEOR_STRIKE, AnimationEvent.Side.SERVER)
                  .params(new Vec3f(0.0F, -0.2F, -1.8F), Armatures.BIPED.get().toolR, 0.3F)
            )
      );
      REVELATION_ONEHAND = builder.nextAccessor(
         "biped/skill/revelation_normal",
         accessor -> new AttackAnimation(0.05F, 0.0F, 0.05F, 0.1F, 0.35F, ColliderPreset.FIST, Armatures.BIPED.get().legR, accessor, Armatures.BIPED)
            .<SoundEvent, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.SWING_SOUND, (SoundEvent)EpicFightSounds.WHOOSH.get())
            .<RegistryObject<HitParticleType>, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLUNT)
            .<SoundEvent, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.HIT_SOUND, (SoundEvent)EpicFightSounds.BLUNT_HIT.get())
            .<Set<TagKey<DamageType>>, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.COUNTER))
            .<StunType, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.NEUTRALIZE)
            .<ValueModifier, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1.0F))
            .<ValueModifier, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.setter(0.5F))
            .<ValueModifier, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.setter(0.0F))
            .<ValueModifier, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(2.0F))
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, null)
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(
               AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_TARGET_LOCATION_ROTATION
            )
            .<StaticAnimation, AnimationProperty.YRotProvider>addProperty(
               AnimationProperty.ActionAnimationProperty.ENTITY_YROT_PROVIDER, MoveCoordFunctions.LOOK_DEST
            )
            .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)
      );
      REVELATION_TWOHAND = builder.nextAccessor(
         "biped/skill/revelation_twohand",
         accessor -> new AttackAnimation(0.1F, 0.0F, 0.05F, 0.1F, 0.35F, ColliderPreset.FIST_FIXED, Armatures.BIPED.get().rootJoint, accessor, Armatures.BIPED)
            .<SoundEvent, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.SWING_SOUND, (SoundEvent)EpicFightSounds.WHOOSH.get())
            .<RegistryObject<HitParticleType>, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLUNT)
            .<SoundEvent, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.HIT_SOUND, (SoundEvent)EpicFightSounds.BLUNT_HIT.get())
            .<Set<TagKey<DamageType>>, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.COUNTER))
            .<StunType, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.NEUTRALIZE)
            .<ValueModifier, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1.0F))
            .<ValueModifier, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.setter(0.5F))
            .<ValueModifier, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.setter(0.0F))
            .<ValueModifier, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(2.0F))
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, null)
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(
               AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_TARGET_LOCATION_ROTATION
            )
            .<StaticAnimation, AnimationProperty.YRotProvider>addProperty(
               AnimationProperty.ActionAnimationProperty.ENTITY_YROT_PROVIDER, MoveCoordFunctions.LOOK_DEST
            )
            .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)
      );
      BIPED_HIT_SHORT = builder.nextAccessor("biped/combat/hit_short", accessor -> new HitAnimation(0.05F, accessor, Armatures.BIPED));
      BIPED_HIT_LONG = builder.nextAccessor("biped/combat/hit_long", accessor -> new LongHitAnimation(0.08F, accessor, Armatures.BIPED));
      BIPED_HIT_ON_MOUNT = builder.nextAccessor(
         "biped/combat/hit_on_mount",
         accessor -> new LongHitAnimation(0.08F, accessor, Armatures.BIPED)
            .<StaticAnimation, AnimationEvent.SimpleEvent<AnimationEvent.E2<CapabilityItem, CapabilityItem>>>addProperty(
               AnimationProperty.StaticAnimationProperty.ON_ITEM_CHANGE_EVENT,
               AnimationEvent.SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_MOUNT_AND_ITEM_CHANGED, AnimationEvent.Side.CLIENT)
            )
            .<StaticAnimation>addEvents(
               AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS,
               AnimationEvent.SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_MOUNT, AnimationEvent.Side.CLIENT)
            )
            .addEvents(
               AnimationProperty.StaticAnimationProperty.ON_END_EVENTS,
               AnimationEvent.SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, AnimationEvent.Side.CLIENT)
            )
      );
      BIPED_LANDING = builder.nextAccessor("biped/living/landing", accessor -> new LongHitAnimation(0.03F, accessor, Armatures.BIPED));
      BIPED_KNOCKDOWN = builder.nextAccessor("biped/combat/knockdown", accessor -> new KnockdownAnimation(0.08F, accessor, Armatures.BIPED));
      BIPED_DEATH = builder.nextAccessor("biped/living/death", accessor -> new LongHitAnimation(0.16F, accessor, Armatures.BIPED));
      CREEPER_HIT_SHORT = builder.nextAccessor("creeper/hit_short", accessor -> new HitAnimation(0.05F, accessor, Armatures.CREEPER));
      CREEPER_HIT_LONG = builder.nextAccessor("creeper/hit_long", accessor -> new LongHitAnimation(0.08F, accessor, Armatures.CREEPER));
      CREEPER_DEATH = builder.nextAccessor("creeper/death", accessor -> new LongHitAnimation(0.16F, accessor, Armatures.CREEPER));
      ENDERMAN_HIT_SHORT = builder.nextAccessor("enderman/hit_short", accessor -> new HitAnimation(0.05F, accessor, Armatures.ENDERMAN));
      ENDERMAN_HIT_LONG = builder.nextAccessor("enderman/hit_long", accessor -> new LongHitAnimation(0.08F, accessor, Armatures.ENDERMAN));
      ENDERMAN_NEUTRALIZED = builder.nextAccessor("enderman/neutralized", accessor -> new LongHitAnimation(0.18F, accessor, Armatures.ENDERMAN));
      ENDERMAN_CONVERT_RAGE = builder.nextAccessor("enderman/convert_rage", accessor -> new InvincibleAnimation(0.16F, accessor, Armatures.ENDERMAN));
      ENDERMAN_TP_KICK1 = builder.nextAccessor(
         "enderman/tp_kick1",
         accessor -> new AttackAnimation(
            0.06F, 0.15F, 0.3F, 0.4F, 1.0F, ColliderPreset.ENDERMAN_LIMB, Armatures.ENDERMAN.get().legR, accessor, Armatures.ENDERMAN
         )
      );
      ENDERMAN_TP_KICK2 = builder.nextAccessor(
         "enderman/tp_kick2",
         accessor -> new AttackAnimation(
            0.16F, 0.15F, 0.25F, 0.45F, 1.0F, ColliderPreset.ENDERMAN_LIMB, Armatures.ENDERMAN.get().legR, accessor, Armatures.ENDERMAN
         )
      );
      ENDERMAN_KICK1 = builder.nextAccessor(
         "enderman/rush_kick",
         accessor -> new AttackAnimation(
               0.16F, 0.66F, 0.7F, 0.81F, 1.6F, ColliderPreset.ENDERMAN_LIMB, Armatures.ENDERMAN.get().legL, accessor, Armatures.ENDERMAN
            )
            .addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.setter(4.0F))
      );
      ENDERMAN_KICK2 = builder.nextAccessor(
         "enderman/jump_kick",
         accessor -> new AttackAnimation(
            0.16F, 0.8F, 0.8F, 0.9F, 1.3F, ColliderPreset.ENDERMAN_LIMB, Armatures.ENDERMAN.get().legR, accessor, Armatures.ENDERMAN
         )
      );
      ENDERMAN_KNEE = builder.nextAccessor(
         "enderman/knee",
         accessor -> new AttackAnimation(0.16F, 0.25F, 0.25F, 0.31F, 1.0F, ColliderPreset.FIST, Armatures.ENDERMAN.get().legR, accessor, Armatures.ENDERMAN)
            .addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.LONG)
      );
      ENDERMAN_KICK_COMBO = builder.nextAccessor(
         "enderman/kick_twice",
         accessor -> new AttackAnimation(
               0.1F,
               accessor,
               Armatures.ENDERMAN,
               new AttackAnimation.Phase(0.0F, 0.15F, 0.15F, 0.21F, 0.46F, 0.6F, Armatures.ENDERMAN.get().legR, ColliderPreset.ENDERMAN_LIMB),
               new AttackAnimation.Phase(0.6F, 0.75F, 0.75F, 0.81F, 1.6F, Float.MAX_VALUE, Armatures.ENDERMAN.get().legL, ColliderPreset.ENDERMAN_LIMB)
            )
            .addProperty(AnimationProperty.ActionAnimationProperty.STOP_MOVEMENT, true)
      );
      ENDERMAN_GRASP = builder.nextAccessor(
         "enderman/grasp",
         accessor -> new AttackAnimation(
               0.06F, 0.5F, 0.45F, 1.0F, 1.0F, ColliderPreset.ENDERMAN_LIMB, Armatures.BIPED.get().toolR, accessor, Armatures.ENDERMAN
            )
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      ENDERMAN_DEATH = builder.nextAccessor("enderman/death", accessor -> new LongHitAnimation(0.16F, accessor, Armatures.ENDERMAN));
      ENDERMAN_TP_EMERGENCE = builder.nextAccessor(
         "enderman/teleport",
         accessor -> new ActionAnimation(0.05F, accessor, Armatures.ENDERMAN).addProperty(AnimationProperty.ActionAnimationProperty.STOP_MOVEMENT, true)
      );
      DRAGON_IDLE = builder.nextAccessor("dragon/idle", accessor -> new StaticAnimation(0.6F, true, accessor, Armatures.DRAGON));
      DRAGON_WALK = builder.nextAccessor(
         "dragon/walk",
         accessor -> new EnderDragonWalkAnimation(0.35F, accessor, Armatures.DRAGON)
            .addProperty(
               AnimationProperty.StaticAnimationProperty.IK_DEFINITION,
               List.of(
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legFrontL1,
                     Armatures.DRAGON.get().legFrontL3,
                     Armatures.DRAGON.get().legFrontR3,
                     IntIntPair.of(0, 3),
                     0.12F,
                     0,
                     new boolean[]{true, true, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legFrontR1,
                     Armatures.DRAGON.get().legFrontR3,
                     Armatures.DRAGON.get().legFrontL3,
                     IntIntPair.of(2, 5),
                     0.12F,
                     2,
                     new boolean[]{true, true, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legBackL1,
                     Armatures.DRAGON.get().legBackL3,
                     Armatures.DRAGON.get().legBackR3,
                     IntIntPair.of(2, 5),
                     0.1344F,
                     4,
                     new boolean[]{true, true, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legBackR1,
                     Armatures.DRAGON.get().legBackR3,
                     Armatures.DRAGON.get().legBackL3,
                     IntIntPair.of(0, 3),
                     0.1344F,
                     2,
                     new boolean[]{true, true, true}
                  )
               )
            )
      );
      DRAGON_FLY = builder.nextAccessor(
         "dragon/fly",
         accessor -> new StaticAnimation(0.35F, true, accessor, Armatures.DRAGON)
            .addEvents(AnimationEvent.InTimeEvent.create(0.4F, Animations.ReusableSources.WING_FLAP, AnimationEvent.Side.CLIENT))
      );
      DRAGON_DEATH = builder.nextAccessor("dragon/death", accessor -> new EnderDragonDeathAnimation(1.0F, accessor, Armatures.DRAGON));
      DRAGON_GROUND_TO_FLY = builder.nextAccessor(
         "dragon/ground_to_fly",
         accessor -> new EnderDragonActionAnimation(0.25F, accessor, Armatures.DRAGON)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.STOP_MOVEMENT, true)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
            .<StaticAnimation, List<InverseKinematicsSimulator.InverseKinematicsDefinition>>addProperty(
               AnimationProperty.StaticAnimationProperty.IK_DEFINITION,
               List.of(
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legFrontL1,
                     Armatures.DRAGON.get().legFrontL3,
                     null,
                     IntIntPair.of(3, 7),
                     0.12F,
                     0,
                     new boolean[]{true, false, false, false}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legFrontR1,
                     Armatures.DRAGON.get().legFrontR3,
                     null,
                     IntIntPair.of(3, 7),
                     0.12F,
                     0,
                     new boolean[]{true, false, false, false}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legBackL1,
                     Armatures.DRAGON.get().legBackL3,
                     null,
                     IntIntPair.of(4, 7),
                     0.1344F,
                     0,
                     new boolean[]{true, false, false, false}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legBackR1,
                     Armatures.DRAGON.get().legBackR3,
                     null,
                     IntIntPair.of(4, 7),
                     0.1344F,
                     0,
                     new boolean[]{true, false, false, false}
                  )
               )
            )
            .addEvents(
               AnimationEvent.InTimeEvent.create(0.25F, Animations.ReusableSources.WING_FLAP, AnimationEvent.Side.CLIENT),
               AnimationEvent.InTimeEvent.create(1.05F, Animations.ReusableSources.WING_FLAP, AnimationEvent.Side.CLIENT),
               AnimationEvent.InTimeEvent.create(1.45F, (entitypatch, animation, params) -> {
                  if (entitypatch instanceof EnderDragonPatch enderDragonPatch) {
                     enderDragonPatch.setFlyingPhase();
                  }
               }, AnimationEvent.Side.BOTH)
            )
      );
      DRAGON_FLY_TO_GROUND = builder.nextAccessor(
         "dragon/fly_to_ground",
         accessor -> new EnderDragonDynamicActionAnimation(0.35F, accessor, Armatures.DRAGON)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.STOP_MOVEMENT, true)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.MOVE_ON_LINK, false)
            .<StaticAnimation, TimePairList>addProperty(AnimationProperty.ActionAnimationProperty.MOVE_TIME, TimePairList.create(0.0F, 1.35F))
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(
               AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN,
               (self, entitypatch, transformSheet) -> {
                  if (!self.isLinkAnimation() && entitypatch instanceof EnderDragonPatch dragonpatch) {
                     TransformSheet transform = self.getCoord().copyAll();
                     Vec3 dragonpos = dragonpatch.getOriginal().m_20182_();
                     Vec3 targetpos = ((DragonLandingPhase)dragonpatch.getOriginal().m_31157_().m_31418_(PatchedPhases.LANDING)).getLandingPosition();
                     float horizontalDistance = (float)dragonpos.m_82492_(0.0, dragonpos.f_82480_, 0.0)
                        .m_82554_(targetpos.m_82492_(0.0, targetpos.f_82480_, 0.0));
                     float verticalDistance = (float)Math.abs(dragonpos.f_82480_ - targetpos.f_82480_);
                     JointTransform jt0 = transform.getKeyframes()[0].transform();
                     JointTransform jt1 = transform.getKeyframes()[1].transform();
                     JointTransform jt2 = transform.getKeyframes()[2].transform();
                     OpenMatrix4f coordReverse = OpenMatrix4f.createRotatorDeg(90.0F, Vec3f.X_AXIS);
                     Vec3f jointCoord = OpenMatrix4f.transform3v(coordReverse, new Vec3f(jt0.translation().x, verticalDistance, horizontalDistance), null);
                     jt0.translation().set(jointCoord);
                     jt1.translation()
                        .set(
                           MathUtils.lerpVector(jt0.translation(), jt2.translation(), transform.getKeyframes()[1].time() / transform.getKeyframes()[2].time())
                        );
                     transformSheet.readFrom(transform);
                  } else {
                     transformSheet.readFrom(TransformSheet.EMPTY_SHEET);
                  }
               }
            )
            .<StaticAnimation, List<InverseKinematicsSimulator.InverseKinematicsDefinition>>addProperty(
               AnimationProperty.StaticAnimationProperty.IK_DEFINITION,
               List.of(
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legFrontL1,
                     Armatures.DRAGON.get().legFrontL3,
                     null,
                     IntIntPair.of(0, 4),
                     0.12F,
                     9,
                     new boolean[]{false, false, false, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legFrontR1,
                     Armatures.DRAGON.get().legFrontR3,
                     null,
                     IntIntPair.of(0, 4),
                     0.12F,
                     9,
                     new boolean[]{false, false, false, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legBackL1,
                     Armatures.DRAGON.get().legBackL3,
                     null,
                     IntIntPair.of(0, 4),
                     0.1344F,
                     7,
                     new boolean[]{false, false, false, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legBackR1,
                     Armatures.DRAGON.get().legBackR3,
                     null,
                     IntIntPair.of(0, 4),
                     0.1344F,
                     7,
                     new boolean[]{false, false, false, true}
                  )
               )
            )
            .addEvents(
               AnimationEvent.InTimeEvent.create(0.3F, Animations.ReusableSources.WING_FLAP, AnimationEvent.Side.CLIENT),
               AnimationEvent.InTimeEvent.create(
                  1.1F,
                  (entitypatch, animation, params) -> {
                     entitypatch.playSound((SoundEvent)EpicFightSounds.SLAM_HEAVY.get(), 0.0F, 0.0F);
                     LivingEntity original = (LivingEntity)entitypatch.getOriginal();
                     BlockPos blockpos = original.m_9236_().m_5452_(Types.WORLD_SURFACE, original.m_20183_());
                     original.m_9236_()
                        .m_7106_(
                           (ParticleOptions)EpicFightParticles.GROUND_SLAM.get(),
                           blockpos.m_123341_(),
                           blockpos.m_123342_(),
                           blockpos.m_123343_(),
                           3.0,
                           100.0,
                           1.0
                        );
                  },
                  AnimationEvent.Side.CLIENT
               ),
               AnimationEvent.InTimeEvent.create(1.1F, (entitypatch, animation, params) -> {
                  LivingEntity original = (LivingEntity)entitypatch.getOriginal();
                  DamageSource extDamageSource = EpicFightDamageSources.mobAttack(original).setAnimation(DRAGON_FLY_TO_GROUND).setStunType(StunType.KNOCKDOWN);

                  for (Entity entity : original.m_9236_().m_45933_(original, original.m_20191_().m_165897_(3.0, 0.0, 3.0))) {
                     entity.m_6469_(extDamageSource, 6.0F);
                  }
               }, AnimationEvent.Side.SERVER)
            )
      );
      DRAGON_ATTACK1 = builder.nextAccessor(
         "dragon/attack1",
         accessor -> new EnderDragonAttackAnimation(
               0.35F, 0.4F, 0.65F, 0.76F, 1.9F, ColliderPreset.DRAGON_LEG, Armatures.DRAGON.get().legFrontR3, accessor, Armatures.DRAGON
            )
            .<StunType, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN)
            .<StaticAnimation, List<InverseKinematicsSimulator.InverseKinematicsDefinition>>addProperty(
               AnimationProperty.StaticAnimationProperty.IK_DEFINITION,
               List.of(
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legFrontL1, Armatures.DRAGON.get().legFrontL3, null, IntIntPair.of(2, 4), 0.12F, 0, new boolean[]{true, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legFrontR1,
                     Armatures.DRAGON.get().legFrontR3,
                     null,
                     IntIntPair.of(0, 5),
                     0.12F,
                     0,
                     new boolean[]{false, false, false, false, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legBackL1, Armatures.DRAGON.get().legBackL3, null, null, 0.1344F, 0, new boolean[0]
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legBackR1,
                     Armatures.DRAGON.get().legBackR3,
                     null,
                     IntIntPair.of(1, 4),
                     0.1344F,
                     0,
                     new boolean[]{true, false, true}
                  )
               )
            )
            .addEvents(
               AnimationEvent.InTimeEvent.create(
                  0.65F,
                  (entitypatch, animation, params) -> {
                     entitypatch.playSound((SoundEvent)EpicFightSounds.SLAM_HEAVY.get(), 0.0F, 0.0F);
                     if (entitypatch instanceof EnderDragonPatch dragonpatch) {
                        dragonpatch.getIKSimulator()
                           .getRunningObject(Armatures.DRAGON.get().legFrontR3)
                           .ifPresent(
                              ikObject -> {
                                 Vec3f tipPosition = ikObject.getDestination();
                                 ((LivingEntity)entitypatch.getOriginal())
                                    .m_9236_()
                                    .m_7106_(
                                       (ParticleOptions)EpicFightParticles.GROUND_SLAM.get(), tipPosition.x, tipPosition.y, tipPosition.z, 0.5, 100.0, 0.5
                                    );
                              }
                           );
                     }
                  },
                  AnimationEvent.Side.CLIENT
               )
            )
      );
      DRAGON_ATTACK2 = builder.nextAccessor(
         "dragon/attack2",
         accessor -> new EnderDragonAttackAnimation(
               0.35F, 0.25F, 0.45F, 0.66F, 0.75F, ColliderPreset.DRAGON_LEG, Armatures.DRAGON.get().legFrontR3, accessor, Armatures.DRAGON
            )
            .addProperty(
               AnimationProperty.StaticAnimationProperty.IK_DEFINITION,
               List.of(
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legFrontL1, Armatures.DRAGON.get().legFrontL3, null, IntIntPair.of(1, 4), 0.12F, 0, new boolean[]{true, true, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legBackL1, Armatures.DRAGON.get().legBackL3, null, null, 0.1344F, 0, new boolean[0]
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legBackR1, Armatures.DRAGON.get().legBackR3, null, null, 0.1344F, 0, new boolean[0]
                  )
               )
            )
      );
      DRAGON_ATTACK3 = builder.nextAccessor(
         "dragon/attack3",
         accessor -> new EnderDragonAttackAnimation(
               0.35F, 0.25F, 0.45F, 0.66F, 0.75F, ColliderPreset.DRAGON_LEG, Armatures.DRAGON.get().legFrontL3, accessor, Armatures.DRAGON
            )
            .addProperty(
               AnimationProperty.StaticAnimationProperty.IK_DEFINITION,
               List.of(
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legFrontR1, Armatures.DRAGON.get().legFrontR3, null, IntIntPair.of(1, 4), 0.12F, 0, new boolean[]{true, true, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legBackL1, Armatures.DRAGON.get().legBackL3, null, null, 0.1344F, 0, new boolean[0]
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legBackR1, Armatures.DRAGON.get().legBackR3, null, null, 0.1344F, 0, new boolean[0]
                  )
               )
            )
      );
      DRAGON_ATTACK4 = builder.nextAccessor(
         "dragon/attack4",
         accessor -> new EnderDragonAttackAnimation(
               0.35F, 0.5F, 1.15F, 1.26F, 1.9F, ColliderPreset.DRAGON_BODY, Armatures.DRAGON.get().rootJoint, accessor, Armatures.DRAGON
            )
            .<StunType, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
            .<StaticAnimation, List<InverseKinematicsSimulator.InverseKinematicsDefinition>>addProperty(
               AnimationProperty.StaticAnimationProperty.IK_DEFINITION,
               List.of(
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legFrontL1,
                     Armatures.DRAGON.get().legFrontL3,
                     null,
                     IntIntPair.of(0, 7),
                     0.12F,
                     0,
                     new boolean[]{false, false, false, false, true, true, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legFrontR1,
                     Armatures.DRAGON.get().legFrontR3,
                     null,
                     IntIntPair.of(0, 7),
                     0.12F,
                     0,
                     new boolean[]{false, false, false, false, true, true, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legBackL1,
                     Armatures.DRAGON.get().legBackL3,
                     null,
                     IntIntPair.of(3, 8),
                     0.1344F,
                     0,
                     new boolean[]{false, false, false, false, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legBackR1,
                     Armatures.DRAGON.get().legBackR3,
                     null,
                     IntIntPair.of(3, 8),
                     0.1344F,
                     0,
                     new boolean[]{false, false, false, false, true}
                  )
               )
            )
            .addEvents(
               AnimationEvent.InTimeEvent.create(
                  1.2F,
                  (entitypatch, animation, params) -> {
                     entitypatch.playSound((SoundEvent)EpicFightSounds.SLAM_HEAVY.get(), 0.0F, 0.0F);
                     if (entitypatch instanceof EnderDragonPatch dragonpatch) {
                        dragonpatch.getIKSimulator()
                           .getRunningObject(Armatures.DRAGON.get().legFrontR3)
                           .ifPresent(
                              ikObject -> {
                                 Vec3f tipPosition = ikObject.getDestination();
                                 ((LivingEntity)entitypatch.getOriginal())
                                    .m_9236_()
                                    .m_7106_(
                                       (ParticleOptions)EpicFightParticles.GROUND_SLAM.get(), tipPosition.x, tipPosition.y, tipPosition.z, 3.0, 100.0, 1.0
                                    );
                              }
                           );
                     }
                  },
                  AnimationEvent.Side.CLIENT
               ),
               AnimationEvent.InTimeEvent.create(
                  1.85F,
                  (entitypatch, animation, params) -> entitypatch.<Animator>getAnimator().reserveAnimation(DRAGON_ATTACK4_RECOVERY),
                  AnimationEvent.Side.BOTH
               )
            )
      );
      DRAGON_ATTACK4_RECOVERY = builder.nextAccessor(
         "dragon/attack4_recovery",
         accessor -> new EnderDragonActionAnimation(0.35F, accessor, Armatures.DRAGON)
            .addProperty(
               AnimationProperty.StaticAnimationProperty.IK_DEFINITION,
               List.of(
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legFrontL1,
                     Armatures.DRAGON.get().legFrontL3,
                     null,
                     IntIntPair.of(0, 4),
                     0.12F,
                     0,
                     new boolean[]{true, false, true, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legFrontR1, Armatures.DRAGON.get().legFrontR3, null, IntIntPair.of(0, 3), 0.12F, 0, new boolean[]{true, true, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legBackL1,
                     Armatures.DRAGON.get().legBackL3,
                     null,
                     IntIntPair.of(0, 5),
                     0.1344F,
                     0,
                     new boolean[]{true, true, false, false, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legBackR1,
                     Armatures.DRAGON.get().legBackR3,
                     null,
                     IntIntPair.of(0, 4),
                     0.1344F,
                     0,
                     new boolean[]{true, true, false, false}
                  )
               )
            )
      );
      DRAGON_FIREBALL = builder.nextAccessor(
         "dragon/fireball",
         accessor -> new EnderDragonActionAnimation(0.16F, accessor, Armatures.DRAGON)
            .<StaticAnimation, List<InverseKinematicsSimulator.InverseKinematicsDefinition>>addProperty(
               AnimationProperty.StaticAnimationProperty.IK_DEFINITION,
               List.of(
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legFrontL1,
                     Armatures.DRAGON.get().legFrontL3,
                     null,
                     IntIntPair.of(0, 5),
                     0.12F,
                     0,
                     new boolean[]{true, true, true, true, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legFrontR1,
                     Armatures.DRAGON.get().legFrontR3,
                     null,
                     IntIntPair.of(0, 5),
                     0.12F,
                     0,
                     new boolean[]{true, true, true, true, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legBackL1,
                     Armatures.DRAGON.get().legBackL3,
                     null,
                     IntIntPair.of(0, 5),
                     0.1344F,
                     0,
                     new boolean[]{true, true, true, true, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legBackR1,
                     Armatures.DRAGON.get().legBackR3,
                     null,
                     IntIntPair.of(0, 5),
                     0.1344F,
                     0,
                     new boolean[]{true, true, true, true, true}
                  )
               )
            )
            .addEvents(AnimationEvent.InTimeEvent.create(0.65F, (entitypatch, animation, params) -> {
               LivingEntity original = (LivingEntity)entitypatch.getOriginal();
               Entity target = entitypatch.getTarget();
               Vec3 pos = original.m_20182_();
               Vec3 toTarget = target.m_20182_().m_82546_(original.m_20182_()).m_82541_().m_82490_(original.m_20205_() * 0.5);
               double d6 = (float)(pos.f_82479_ + toTarget.f_82479_);
               double d7 = (float)(pos.f_82480_ + 2.0);
               double d8 = (float)(pos.f_82481_ + toTarget.f_82481_);
               double d9 = target.m_20185_() - d6;
               double d10 = target.m_20227_(0.5) - d7;
               double d11 = target.m_20189_() - d8;
               if (!original.m_20067_()) {
                  original.m_9236_().m_5898_(null, 1017, original.m_20183_(), 0);
               }

               DragonFireball dragonfireball = new DragonFireball(original.m_9236_(), original, d9, d10, d11);
               dragonfireball.m_7678_(d6, d7, d8, 0.0F, 0.0F);
               original.m_9236_().m_7967_(dragonfireball);
            }, AnimationEvent.Side.SERVER))
      );
      DRAGON_AIRSTRIKE = builder.nextAccessor(
         "dragon/airstrike",
         accessor -> new StaticAnimation(0.35F, true, accessor, Armatures.DRAGON)
            .addEvents(AnimationEvent.InTimeEvent.create(0.3F, Animations.ReusableSources.WING_FLAP, AnimationEvent.Side.CLIENT))
      );
      DRAGON_BACKJUMP_PREPARE = builder.nextAccessor(
         "dragon/backjump_prepare",
         accessor -> new EnderDragonActionAnimation(0.35F, accessor, Armatures.DRAGON)
            .<StaticAnimation, List<InverseKinematicsSimulator.InverseKinematicsDefinition>>addProperty(
               AnimationProperty.StaticAnimationProperty.IK_DEFINITION,
               List.of(
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legFrontL1,
                     Armatures.DRAGON.get().legFrontL3,
                     null,
                     IntIntPair.of(0, 4),
                     0.12F,
                     0,
                     new boolean[]{true, true, true, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legFrontR1,
                     Armatures.DRAGON.get().legFrontR3,
                     null,
                     IntIntPair.of(0, 4),
                     0.12F,
                     0,
                     new boolean[]{true, true, true, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legBackL1,
                     Armatures.DRAGON.get().legBackL3,
                     null,
                     IntIntPair.of(0, 4),
                     0.1344F,
                     0,
                     new boolean[]{true, true, true, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legBackR1,
                     Armatures.DRAGON.get().legBackR3,
                     null,
                     IntIntPair.of(0, 4),
                     0.1344F,
                     0,
                     new boolean[]{true, true, true, true}
                  )
               )
            )
            .addEvents(
               AnimationEvent.InTimeEvent.create(
                  0.3F,
                  (entitypatch, animation, params) -> entitypatch.<Animator>getAnimator().reserveAnimation(DRAGON_BACKJUMP_MOVE),
                  AnimationEvent.Side.BOTH
               )
            )
      );
      DRAGON_BACKJUMP_MOVE = builder.nextAccessor(
         "dragon/backjump_move",
         accessor -> new AttackAnimation(0.0F, 10.0F, 10.0F, 10.0F, 10.0F, ColliderPreset.FIST, Armatures.DRAGON.get().rootJoint, accessor, Armatures.DRAGON)
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(
               AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.RAW_COORD
            )
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, null)
            .addEvents(
               AnimationEvent.InTimeEvent.create(
                  1.0F,
                  (entitypatch, animation, params) -> entitypatch.<Animator>getAnimator().reserveAnimation(DRAGON_BACKJUMP_RECOVERY),
                  AnimationEvent.Side.BOTH
               )
            )
      );
      DRAGON_BACKJUMP_RECOVERY = builder.nextAccessor(
         "dragon/backjump_recovery",
         accessor -> new EnderDragonActionAnimation(0.0F, accessor, Armatures.DRAGON)
            .<StaticAnimation, List<InverseKinematicsSimulator.InverseKinematicsDefinition>>addProperty(
               AnimationProperty.StaticAnimationProperty.IK_DEFINITION,
               List.of(
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legFrontL1,
                     Armatures.DRAGON.get().legFrontL3,
                     null,
                     IntIntPair.of(0, 4),
                     0.12F,
                     0,
                     new boolean[]{false, true, true, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legFrontR1,
                     Armatures.DRAGON.get().legFrontR3,
                     null,
                     IntIntPair.of(0, 4),
                     0.12F,
                     0,
                     new boolean[]{false, true, true, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legBackL1,
                     Armatures.DRAGON.get().legBackL3,
                     null,
                     IntIntPair.of(0, 4),
                     0.1344F,
                     0,
                     new boolean[]{true, true, true, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legBackR1,
                     Armatures.DRAGON.get().legBackR3,
                     null,
                     IntIntPair.of(0, 4),
                     0.1344F,
                     0,
                     new boolean[]{true, true, true, true}
                  )
               )
            )
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
            .addEvents(
               AnimationEvent.InTimeEvent.create(
                  0.15F,
                  (entitypatch, animation, params) -> {
                     entitypatch.playSound((SoundEvent)EpicFightSounds.SLAM_HEAVY.get(), 0.0F, 0.0F);
                     if (entitypatch instanceof EnderDragonPatch dragonpatch) {
                        dragonpatch.getIKSimulator()
                           .getRunningObject(Armatures.DRAGON.get().legFrontR3)
                           .ifPresent(
                              ikObject -> {
                                 Vec3f tipPosition = ikObject.getDestination();
                                 ((LivingEntity)entitypatch.getOriginal())
                                    .m_9236_()
                                    .m_7106_(
                                       (ParticleOptions)EpicFightParticles.GROUND_SLAM.get(), tipPosition.x, tipPosition.y, tipPosition.z, 3.0, 100.0, 1.0
                                    );
                              }
                           );
                     }
                  },
                  AnimationEvent.Side.CLIENT
               )
            )
      );
      DRAGON_CRYSTAL_LINK = builder.nextAccessor(
         "dragon/crystal_link",
         accessor -> new EnderDragonActionAnimation(0.5F, accessor, Armatures.DRAGON)
            .<StaticAnimation, List<InverseKinematicsSimulator.InverseKinematicsDefinition>>addProperty(
               AnimationProperty.StaticAnimationProperty.IK_DEFINITION,
               List.of(
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legFrontL1, Armatures.DRAGON.get().legFrontL3, null, IntIntPair.of(0, 2), 0.12F, 0, new boolean[]{true, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legFrontR1, Armatures.DRAGON.get().legFrontR3, null, IntIntPair.of(0, 2), 0.12F, 0, new boolean[]{true, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legBackL1, Armatures.DRAGON.get().legBackL3, null, IntIntPair.of(0, 2), 0.1344F, 0, new boolean[]{true, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legBackR1, Armatures.DRAGON.get().legBackR3, null, IntIntPair.of(0, 2), 0.1344F, 0, new boolean[]{true, true}
                  )
               )
            )
            .addEvents(
               AnimationEvent.InTimeEvent.create(
                  7.0F,
                  (entitypatch, animation, params) -> {
                     ((LivingEntity)entitypatch.getOriginal())
                        .m_5496_(SoundEvents.f_11894_, 7.0F, 0.8F + ((LivingEntity)entitypatch.getOriginal()).m_217043_().m_188501_() * 0.3F);
                     ((LivingEntity)entitypatch.getOriginal()).m_21153_(((LivingEntity)entitypatch.getOriginal()).m_21233_());
                     if (entitypatch instanceof EnderDragonPatch dragonpatch) {
                        dragonpatch.getOriginal().m_31157_().m_31416_(PatchedPhases.GROUND_BATTLE);
                        dragonpatch.setStunShield(0.0F);
                     }
                  },
                  AnimationEvent.Side.SERVER
               ),
               AnimationEvent.InTimeEvent.create(
                  7.0F,
                  (entitypatch, animation, params) -> {
                     Entity original = entitypatch.getOriginal();
                     original.m_9236_()
                        .m_7106_(
                           (ParticleOptions)EpicFightParticles.FORCE_FIELD_END.get(),
                           original.m_20185_(),
                           original.m_20186_() + 2.0,
                           original.m_20189_(),
                           0.0,
                           0.0,
                           0.0
                        );
                  },
                  AnimationEvent.Side.CLIENT
               )
            )
      );
      DRAGON_NEUTRALIZED = builder.nextAccessor(
         "dragon/neutralized",
         accessor -> new EnderDragonActionAnimation(0.1F, accessor, Armatures.DRAGON)
            .<StaticAnimation, List<InverseKinematicsSimulator.InverseKinematicsDefinition>>addProperty(
               AnimationProperty.StaticAnimationProperty.IK_DEFINITION,
               List.of(
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legFrontL1,
                     Armatures.DRAGON.get().legFrontL3,
                     null,
                     IntIntPair.of(0, 4),
                     0.12F,
                     0,
                     new boolean[]{true, true, true, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legFrontR1,
                     Armatures.DRAGON.get().legFrontR3,
                     null,
                     IntIntPair.of(0, 4),
                     0.12F,
                     0,
                     new boolean[]{true, true, true, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legBackL1,
                     Armatures.DRAGON.get().legBackL3,
                     null,
                     IntIntPair.of(0, 4),
                     0.1344F,
                     0,
                     new boolean[]{true, true, true, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legBackR1,
                     Armatures.DRAGON.get().legBackR3,
                     null,
                     IntIntPair.of(0, 4),
                     0.1344F,
                     0,
                     new boolean[]{true, true, true, true}
                  )
               )
            )
            .addEvents(
               AnimationEvent.InTimeEvent.create(
                  3.95F,
                  (entitypatch, animation, params) -> entitypatch.<Animator>getAnimator().playAnimation(DRAGON_NEUTRALIZED_RECOVERY, 0.0F),
                  AnimationEvent.Side.BOTH
               )
            )
      );
      DRAGON_NEUTRALIZED_RECOVERY = builder.nextAccessor(
         "dragon/neutralized_recovery",
         accessor -> new EnderDragonActionAnimation(0.05F, accessor, Armatures.DRAGON)
            .<StaticAnimation, List<InverseKinematicsSimulator.InverseKinematicsDefinition>>addProperty(
               AnimationProperty.StaticAnimationProperty.IK_DEFINITION,
               List.of(
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legFrontL1,
                     Armatures.DRAGON.get().legFrontL3,
                     null,
                     IntIntPair.of(0, 5),
                     0.12F,
                     0,
                     new boolean[]{true, true, true, false, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legFrontR1,
                     Armatures.DRAGON.get().legFrontR3,
                     null,
                     IntIntPair.of(0, 5),
                     0.12F,
                     0,
                     new boolean[]{true, false, true, true, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legBackL1,
                     Armatures.DRAGON.get().legBackL3,
                     null,
                     IntIntPair.of(0, 5),
                     0.1344F,
                     0,
                     new boolean[]{true, true, true, true, true}
                  ),
                  InverseKinematicsSimulator.InverseKinematicsDefinition.create(
                     Armatures.DRAGON.get().legBackR1,
                     Armatures.DRAGON.get().legBackR3,
                     null,
                     IntIntPair.of(0, 4),
                     0.1344F,
                     0,
                     new boolean[]{true, true, true, true}
                  )
               )
            )
            .addEvents(AnimationEvent.InTimeEvent.create(1.6F, (entitypatch, animation, params) -> {
               if (entitypatch instanceof EnderDragonPatch enderdragonpatch) {
                  ((DragonGroundBattlePhase)enderdragonpatch.getOriginal().m_31157_().m_31418_(PatchedPhases.GROUND_BATTLE)).fly();
               }
            }, AnimationEvent.Side.SERVER))
      );
      SPIDER_ATTACK = builder.nextAccessor(
         "spider/attack",
         accessor -> new AttackAnimation(0.15F, 0.31F, 0.31F, 0.36F, 0.44F, ColliderPreset.SPIDER, Armatures.SPIDER.get().head, accessor, Armatures.SPIDER)
      );
      SPIDER_JUMP_ATTACK = builder.nextAccessor(
         "spider/jump_attack",
         accessor -> new AttackAnimation(0.15F, 0.25F, 0.5F, 0.6F, 1.0F, ColliderPreset.SPIDER, Armatures.SPIDER.get().head, accessor, Armatures.SPIDER)
            .addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
      );
      SPIDER_HIT = builder.nextAccessor("spider/hit", accessor -> new HitAnimation(0.08F, accessor, Armatures.SPIDER));
      SPIDER_NEUTRALIZED = builder.nextAccessor("spider/neutralized", accessor -> new LongHitAnimation(0.08F, accessor, Armatures.SPIDER));
      SPIDER_DEATH = builder.nextAccessor("spider/death", accessor -> new LongHitAnimation(0.16F, accessor, Armatures.SPIDER));
      GOLEM_ATTACK1 = builder.nextAccessor(
         "iron_golem/attack1",
         accessor -> new AttackAnimation(0.2F, 0.1F, 0.2F, 0.35F, 0.9F, ColliderPreset.HEAD, Armatures.IRON_GOLEM.get().head, accessor, Armatures.IRON_GOLEM)
            .addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN)
      );
      GOLEM_ATTACK2 = builder.nextAccessor(
         "iron_golem/attack2",
         accessor -> new AttackAnimation(
               0.34F, 0.1F, 0.4F, 0.6F, 1.3F, ColliderPreset.GOLEM_SMASHDOWN, Armatures.IRON_GOLEM.get().LA4, accessor, Armatures.IRON_GOLEM
            )
            .addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.FINISHER))
      );
      GOLEM_ATTACK3 = builder.nextAccessor(
         "iron_golem/attack3",
         accessor -> new AttackAnimation(
               0.16F, 0.4F, 0.4F, 0.5F, 0.9F, ColliderPreset.GOLEM_SWING_ARM, Armatures.IRON_GOLEM.get().RA4, accessor, Armatures.IRON_GOLEM
            )
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      GOLEM_ATTACK4 = builder.nextAccessor(
         "iron_golem/attack4",
         accessor -> new AttackAnimation(
               0.16F, 0.4F, 0.4F, 0.5F, 0.9F, ColliderPreset.GOLEM_SWING_ARM, Armatures.IRON_GOLEM.get().LA4, accessor, Armatures.IRON_GOLEM
            )
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      GOLEM_DEATH = builder.nextAccessor("iron_golem/death", accessor -> new LongHitAnimation(0.11F, accessor, Armatures.IRON_GOLEM));
      VINDICATOR_SWING_AXE1 = builder.nextAccessor(
         "illager/swing_axe1",
         accessor -> new AttackAnimation(0.2F, 0.2F, 0.3F, 0.4F, 0.9F, ColliderPreset.TOOLS, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
      );
      VINDICATOR_SWING_AXE2 = builder.nextAccessor(
         "illager/swing_axe2",
         accessor -> new AttackAnimation(0.1F, 0.2F, 0.3F, 0.4F, 0.9F, ColliderPreset.TOOLS, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
      );
      VINDICATOR_SWING_AXE3 = builder.nextAccessor(
         "illager/swing_axe3",
         accessor -> new AttackAnimation(0.1F, 0.15F, 0.45F, 0.55F, 1.05F, ColliderPreset.TOOLS, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
      );
      PIGLIN_DEATH = builder.nextAccessor("piglin/death", accessor -> new LongHitAnimation(0.16F, accessor, Armatures.PIGLIN));
      HOGLIN_DEATH = builder.nextAccessor("hoglin/death", accessor -> new LongHitAnimation(0.16F, accessor, Armatures.HOGLIN));
      HOGLIN_ATTACK = builder.nextAccessor(
         "hoglin/attack",
         accessor -> new AttackAnimation(
            0.16F, 0.25F, 0.25F, 0.45F, 1.0F, ColliderPreset.GOLEM_SWING_ARM, Armatures.HOGLIN.get().head, accessor, Armatures.HOGLIN
         )
      );
      RAVAGER_DEATH = builder.nextAccessor("ravager/death", accessor -> new LongHitAnimation(0.11F, accessor, Armatures.RAVAGER));
      RAVAGER_STUN = builder.nextAccessor(
         "ravager/groggy",
         accessor -> new ActionAnimation(0.16F, accessor, Armatures.RAVAGER).addProperty(AnimationProperty.ActionAnimationProperty.STOP_MOVEMENT, true)
      );
      RAVAGER_ATTACK1 = builder.nextAccessor(
         "ravager/attack1",
         accessor -> new AttackAnimation(
            0.16F, 0.2F, 0.4F, 0.5F, 0.55F, ColliderPreset.HEADBUTT_RAVAGER, Armatures.RAVAGER.get().head, accessor, Armatures.RAVAGER
         )
      );
      RAVAGER_ATTACK2 = builder.nextAccessor(
         "ravager/attack2",
         accessor -> new AttackAnimation(
            0.16F, 0.2F, 0.4F, 0.5F, 1.3F, ColliderPreset.HEADBUTT_RAVAGER, Armatures.RAVAGER.get().head, accessor, Armatures.RAVAGER
         )
      );
      RAVAGER_ATTACK3 = builder.nextAccessor(
         "ravager/attack3",
         accessor -> new AttackAnimation(
            0.16F, 0.0F, 1.1F, 1.16F, 1.6F, ColliderPreset.HEADBUTT_RAVAGER, Armatures.RAVAGER.get().head, accessor, Armatures.RAVAGER
         )
      );
      VEX_HIT = builder.nextAccessor("vex/hit", accessor -> new HitAnimation(0.048F, accessor, Armatures.VEX));
      VEX_DEATH = builder.nextAccessor("vex/death", accessor -> new LongHitAnimation(0.16F, accessor, Armatures.VEX));
      VEX_CHARGE = builder.nextAccessor(
         "vex/charge",
         accessor -> new AttackAnimation(0.11F, 0.3F, 0.3F, 0.5F, 1.5F, ColliderPreset.VEX_CHARGE, Armatures.VEX.get().rootJoint, accessor, Armatures.VEX)
            .<Function<LivingEntityPatch<?>, Vec3>, AttackAnimation>addProperty(
               AnimationProperty.AttackPhaseProperty.SOURCE_LOCATION_PROVIDER, LivingEntityPatch::getLastAttackPosition
            )
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(
               AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.VEX_TRACE
            )
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, null)
            .<StaticAnimation, MoveCoordFunctions.MoveCoordGetter>addProperty(
               AnimationProperty.ActionAnimationProperty.COORD_GET, MoveCoordFunctions.WORLD_COORD
            )
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.MOVE_ON_LINK, false)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.REMOVE_DELTA_MOVEMENT, true)
            .<StaticAnimation>addEvents(
               AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS,
               AnimationEvent.SimpleEvent.create((entitypatch, animation, params) -> entitypatch.setLastAttackPosition(), AnimationEvent.Side.SERVER)
            )
            .<StaticAnimation>newTimePair(0.0F, 1.5F)
            .<Boolean, StaticAnimation>addStateRemoveOld(EntityState.MOVEMENT_LOCKED, true)
            .addStateRemoveOld(EntityState.TURNING_LOCKED, true)
      );
      VEX_NEUTRALIZED = builder.nextAccessor("vex/neutralized", accessor -> new LongHitAnimation(0.1F, accessor, Armatures.VEX));
      WITCH_DRINKING = builder.nextAccessor(
         "witch/drink",
         accessor -> new StaticAnimation(0.16F, false, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.FIXED_HEAD_ROTATION, true)
      );
      WITHER_SKELETON_ATTACK1 = builder.nextAccessor(
         "wither_skeleton/sword_attack1",
         accessor -> new AttackAnimation(0.16F, 0.2F, 0.3F, 0.41F, 0.7F, ColliderPreset.SWORD, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      WITHER_SKELETON_ATTACK2 = builder.nextAccessor(
         "wither_skeleton/sword_attack2",
         accessor -> new AttackAnimation(0.16F, 0.25F, 0.25F, 0.36F, 0.7F, ColliderPreset.SWORD, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      WITHER_SKELETON_ATTACK3 = builder.nextAccessor(
         "wither_skeleton/sword_attack3",
         accessor -> new AttackAnimation(0.16F, 0.25F, 0.25F, 0.36F, 0.7F, ColliderPreset.SWORD, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      WITHER_CHARGE = builder.nextAccessor(
         "wither/rush",
         accessor -> new AttackAnimation(
               0.35F, 0.35F, 0.35F, 0.66F, 2.05F, ColliderPreset.WITHER_CHARGE, Armatures.WITHER.get().rootJoint, accessor, Armatures.WITHER
            )
            .<SoundEvent, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.SWING_SOUND, (SoundEvent)EpicFightSounds.BIG_ENTITY_MOVE.get())
            .<SoundEvent, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.HIT_SOUND, (SoundEvent)EpicFightSounds.BLUNT_HIT_HARD.get())
            .<Function<LivingEntityPatch<?>, Vec3>, AttackAnimation>addProperty(
               AnimationProperty.AttackPhaseProperty.SOURCE_LOCATION_PROVIDER, LivingEntityPatch::getLastAttackPosition
            )
            .<StunType, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN)
            .<ValueModifier, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(100.0F))
            .<ValueModifier, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(15.0F))
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(
               AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN,
               (self, entitypatch, transformSheet) -> {
                  Entity target = null;
                  if (!self.isLinkAnimation()
                     && entitypatch instanceof WitherPatch witherpatch
                     && (target = entitypatch.getOriginal().m_9236_().m_6815_(witherpatch.getOriginal().m_31512_(0))) != null) {
                     TransformSheet transform = self.getTransfroms().get("Root").copyAll();
                     Keyframe[] keyframes = transform.getKeyframes();
                     int startFrame = 1;
                     int endFrame = 5;
                     Vec3f keyOrigin = keyframes[startFrame].transform().translation().multiply(1.0F, 1.0F, 0.0F);
                     Vec3f keyLast = keyframes[3].transform().translation();
                     Vec3 pos = entitypatch.getOriginal().m_146892_();
                     Vec3 targetpos = target.m_20182_();
                     float horizontalDistance = (float)targetpos.m_82546_(pos).m_82553_();
                     float verticalDistance = (float)(targetpos.f_82480_ - pos.f_82480_);
                     Vec3f prevPosition = Vec3f.sub(keyLast, keyOrigin, null);
                     Vec3f newPosition = new Vec3f(keyLast.x, verticalDistance, -horizontalDistance);
                     float scale = Math.min(newPosition.length() / prevPosition.length(), 5.0F);
                     Quaternionf rotator = Vec3f.getRotatorBetween(newPosition, keyLast, null);

                     for (int i = startFrame; i <= endFrame; i++) {
                        Vec3f translation = keyframes[i].transform().translation();
                        translation.z *= scale;
                        OpenMatrix4f.transform3v(OpenMatrix4f.fromQuaternion(rotator), translation, translation);
                     }

                     transformSheet.readFrom(transform);
                  } else {
                     transformSheet.readFrom(self.getTransfroms().get("Root").copyAll());
                  }
               }
            )
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, null)
            .<StaticAnimation>addEvents(
               AnimationEvent.InTimeEvent.create(0.4F, (entitypatch, animation, params) -> {
                  if (entitypatch instanceof WitherPatch witherpatch) {
                     witherpatch.startCharging();
                  } else {
                     entitypatch.setLastAttackPosition();
                  }
               }, AnimationEvent.Side.SERVER),
               AnimationEvent.InTimeEvent.create(
                  0.4F,
                  (entitypatch, animation, params) -> {
                     Entity entity = entitypatch.getOriginal();
                     ((LivingEntity)entitypatch.getOriginal())
                        .m_9236_()
                        .m_7106_(
                           (ParticleOptions)EpicFightParticles.WHITE_AFTERIMAGE.get(),
                           entity.m_20185_(),
                           entity.m_20186_(),
                           entity.m_20189_(),
                           Double.longBitsToDouble(entity.m_19879_()),
                           0.0,
                           0.0
                        );
                  },
                  AnimationEvent.Side.CLIENT
               )
            )
            .<StaticAnimation>addEvents(
               AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create((entitypatch, animation, params) -> {
                  if (entitypatch instanceof WitherPatch witherpatch && !witherpatch.getOriginal().m_7090_()) {
                     ((WitherPatch)entitypatch).setArmorActivated(true);
                  }
               }, AnimationEvent.Side.CLIENT)
            )
            .addEvents(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create((entitypatch, animation, params) -> {
               if (entitypatch instanceof WitherPatch witherpatch && !witherpatch.getOriginal().m_7090_()) {
                  ((WitherPatch)entitypatch).setArmorActivated(false);
               }
            }, AnimationEvent.Side.CLIENT))
      );
      WITHER_DEATH = builder.nextAccessor("wither/death", accessor -> new LongHitAnimation(0.16F, accessor, Armatures.WITHER));
      WITHER_NEUTRALIZED = builder.nextAccessor(
         "wither/neutralized",
         accessor -> new LongHitAnimation(0.05F, accessor, Armatures.WITHER)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
            .addEvents(
               AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS,
               AnimationEvent.SimpleEvent.create(
                  (entitypatch, animation, params) -> {
                     Entity entity = entitypatch.getOriginal();
                     entity.m_9236_()
                        .m_7106_(
                           (ParticleOptions)EpicFightParticles.NEUTRALIZE.get(),
                           entity.m_20185_(),
                           entity.m_20188_(),
                           entity.m_20189_(),
                           3.0,
                           Double.longBitsToDouble(15L),
                           Double.NaN
                        );
                  },
                  AnimationEvent.Side.CLIENT
               )
            )
      );
      WITHER_SPELL_ARMOR = builder.nextAccessor(
         "wither/spell_wither_armor",
         accessor -> new InvincibleAnimation(0.35F, accessor, Armatures.WITHER)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, false)
            .<StaticAnimation>addEvents(
               AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS,
               AnimationEvent.SimpleEvent.create(
                  (entitypatch, animation, params) -> {
                     entitypatch.playSound((SoundEvent)EpicFightSounds.WITHER_SPELL_ARMOR.get(), 5.0F, 0.0F, 0.0F);
                     Entity entity = entitypatch.getOriginal();
                     entity.m_9236_()
                        .m_7106_(
                           (ParticleOptions)EpicFightParticles.BOSS_CASTING.get(),
                           entity.m_20185_(),
                           entity.m_20188_(),
                           entity.m_20189_(),
                           5.0,
                           Double.longBitsToDouble(20L),
                           Double.longBitsToDouble(4L)
                        );
                  },
                  AnimationEvent.Side.CLIENT
               )
            )
            .addEvents(
               AnimationEvent.InTimeEvent.create(
                  0.5F, (entitypatch, animation, params) -> ((WitherPatch)entitypatch).setArmorActivated(true), AnimationEvent.Side.SERVER
               )
            )
      );
      WITHER_BLOCKED = builder.nextAccessor(
         "wither/charging_blocked",
         accessor -> new ActionAnimation(0.05F, accessor, Armatures.WITHER)
            .<StaticAnimation>addEvents(
               AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create((entitypatch, animation, params) -> {
                  if (entitypatch instanceof WitherPatch witherpatch && !witherpatch.getOriginal().m_7090_()) {
                     ((WitherPatch)entitypatch).setArmorActivated(true);
                  }
               }, AnimationEvent.Side.SERVER)
            )
            .addEvents(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create((entitypatch, animation, params) -> {
               if (entitypatch instanceof WitherPatch witherpatch && !witherpatch.getOriginal().m_7090_()) {
                  ((WitherPatch)entitypatch).setArmorActivated(false);
               }
            }, AnimationEvent.Side.SERVER))
      );
      WITHER_GHOST_STANDBY = builder.nextAccessor("wither/ghost_stand", accessor -> new InvincibleAnimation(0.16F, accessor, Armatures.WITHER));
      WITHER_SWIRL = builder.nextAccessor(
         "wither/swirl",
         accessor -> new AttackAnimation(0.2F, 0.05F, 0.4F, 0.51F, 1.6F, ColliderPreset.WITHER_CHARGE, Armatures.WITHER.get().torso, accessor, Armatures.WITHER)
            .<SoundEvent, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.SWING_SOUND, (SoundEvent)EpicFightSounds.WHOOSH_BIG.get())
            .<ValueModifier, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(3.0F))
            .addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(6.0F))
      );
      WITHER_BEAM = builder.nextAccessor(
         "wither/laser",
         accessor -> new ActionAnimation(0.05F, accessor, Armatures.WITHER)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, false)
            .addEvents(
               AnimationEvent.InTimeEvent.create(0.0F, (entitypatch, animation, params) -> {
                  entitypatch.playSound((SoundEvent)EpicFightSounds.BUZZ.get(), 0.0F, 0.0F);
                  if (entitypatch instanceof WitherPatch witherpatch) {
                     for (int i = 0; i < 3; i++) {
                        Entity headTarget = witherpatch.getAlternativeTargetEntity(i);
                        if (headTarget == null) {
                           headTarget = witherpatch.getAlternativeTargetEntity(0);
                        }

                        if (headTarget != null) {
                           witherpatch.setLaserTarget(i, headTarget);
                        }
                     }
                  }
               }, AnimationEvent.Side.SERVER),
               AnimationEvent.InTimeEvent.create(0.7F, (entitypatch, animation, params) -> {
                  if (entitypatch instanceof WitherPatch witherpatch) {
                     for (int i = 0; i < 3; i++) {
                        Entity headTarget = witherpatch.getLaserTargetEntity(i);
                        if (headTarget != null) {
                           Vec3 pos = headTarget.m_20182_().m_82520_(0.0, headTarget.m_20206_() * 0.5, 0.0);
                           witherpatch.setLaserTargetPosition(i, pos);
                           witherpatch.setLaserTarget(i, null);
                        }
                     }
                  }
               }, AnimationEvent.Side.SERVER),
               AnimationEvent.InTimeEvent.create(
                  0.9F,
                  (entitypatch, animation, params) -> {
                     if (entitypatch instanceof WitherPatch witherpatch) {
                        WitherBoss witherboss = witherpatch.getOriginal();
                        witherboss.m_9236_()
                           .m_7785_(
                              witherboss.m_20185_(),
                              witherboss.m_20186_(),
                              witherboss.m_20189_(),
                              (SoundEvent)EpicFightSounds.LASER_BLAST.get(),
                              SoundSource.HOSTILE,
                              1.0F,
                              1.0F,
                              false
                           );

                        for (int i = 0; i < 3; i++) {
                           Vec3 laserDestination = witherpatch.getLaserTargetPosition(i);
                           Entity headTarget = witherpatch.getAlternativeTargetEntity(i);
                           if (headTarget != null) {
                              witherpatch.getOriginal()
                                 .m_9236_()
                                 .m_7107_(
                                    (ParticleOptions)EpicFightParticles.LASER.get(),
                                    witherboss.m_31514_(i),
                                    witherboss.m_31516_(i),
                                    witherboss.m_31518_(i),
                                    laserDestination.f_82479_,
                                    laserDestination.f_82480_,
                                    laserDestination.f_82481_
                                 );
                           }
                        }
                     }
                  },
                  AnimationEvent.Side.CLIENT
               ),
               AnimationEvent.InTimeEvent.create(
                  0.9F,
                  (entitypatch, animation, params) -> {
                     if (entitypatch instanceof WitherPatch witherpatch) {
                        WitherBoss witherboss = witherpatch.getOriginal();
                        List<Entity> hurted = Lists.newArrayList();

                        for (int i = 0; i < 3; i++) {
                           Vec3 laserDestination = witherpatch.getLaserTargetPosition(i);
                           Entity headTarget = witherpatch.getAlternativeTargetEntity(i);
                           if (headTarget != null) {
                              double x = witherboss.m_31514_(i);
                              double y = witherboss.m_31516_(i);
                              double z = witherboss.m_31518_(i);
                              Vec3 direction = laserDestination.m_82492_(x, y, z);
                              Vec3 start = new Vec3(x, y, z);
                              Vec3 destination = start.m_82549_(direction.m_82541_().m_82490_(200.0));
                              BlockHitResult hitResult = witherboss.m_9236_().m_45547_(new ClipContext(start, destination, Block.COLLIDER, Fluid.NONE, null));
                              Vec3 hitLocation = hitResult.m_82450_();
                              double xLength = hitLocation.f_82479_ - x;
                              double yLength = hitLocation.f_82480_ - y;
                              double zLength = hitLocation.f_82481_ - z;
                              double horizontalDistance = Math.sqrt(xLength * xLength + zLength * zLength);
                              double length = Math.sqrt(xLength * xLength + yLength * yLength + zLength * zLength);
                              float yRot = (float)(-Math.atan2(zLength, xLength) * (180.0 / Math.PI)) - 90.0F;
                              float xRot = (float)(Math.atan2(yLength, horizontalDistance) * (180.0 / Math.PI));
                              OBBCollider collider = new OBBCollider(0.25, 0.25, length * 0.5, 0.0, 0.0, length * 0.5);
                              collider.transform(
                                 OpenMatrix4f.createTranslation((float)(-x), (float)y, (float)(-z))
                                    .rotateDeg(yRot, Vec3f.Y_AXIS)
                                    .rotateDeg(-xRot, Vec3f.X_AXIS)
                              );
                              List<Entity> hitEntities = collider.getCollideEntities(witherboss);
                              EpicFightDamageSource damagesource = EpicFightDamageSources.witherBeam(witherboss).setAnimation(WITHER_BEAM);
                              hitEntities.forEach(entity -> {
                                 if (!hurted.contains(entity)) {
                                    hurted.add(entity);
                                    entity.m_6469_(damagesource, 12.0F);
                                 }
                              });
                              ExplosionInteraction explosion$blockinteraction = ForgeEventFactory.getMobGriefingEvent(witherboss.m_9236_(), witherboss)
                                 ? ExplosionInteraction.BLOCK
                                 : ExplosionInteraction.NONE;
                              witherboss.m_9236_()
                                 .m_255391_(
                                    witherboss, hitLocation.f_82479_, hitLocation.f_82480_, hitLocation.f_82481_, 0.0F, false, explosion$blockinteraction
                                 );
                           }
                        }
                     }
                  },
                  AnimationEvent.Side.SERVER
               ),
               AnimationEvent.InTimeEvent.create(2.3F, (entitypatch, animation, params) -> {
                  if (entitypatch instanceof WitherPatch witherpatch) {
                     for (int i = 0; i < 3; i++) {
                        witherpatch.setLaserTargetPosition(i, new Vec3(Double.NaN, Double.NaN, Double.NaN));
                     }
                  }
               }, AnimationEvent.Side.SERVER)
            )
      );
      WITHER_BACKFLIP = builder.nextAccessor(
         "wither/backflip",
         accessor -> new AttackAnimation(0.2F, 0.3F, 0.5F, 0.66F, 2.1F, ColliderPreset.WITHER_CHARGE, Armatures.WITHER.get().torso, accessor, Armatures.WITHER)
            .<SoundEvent, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.SWING_SOUND, (SoundEvent)EpicFightSounds.BIG_ENTITY_MOVE.get())
            .<SoundEvent, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.HIT_SOUND, (SoundEvent)EpicFightSounds.BLUNT_HIT_HARD.get())
            .<ValueModifier, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(100.0F))
            .<ValueModifier, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(10.0F))
            .<StunType, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN)
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(
               AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.RAW_COORD
            )
            .addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, null)
      );
      ZOMBIE_ATTACK1 = builder.nextAccessor(
         "zombie/attack1",
         accessor -> new AttackAnimation(0.1F, 0.3F, 0.4F, 0.6F, 0.85F, ColliderPreset.FIST, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      ZOMBIE_ATTACK2 = builder.nextAccessor(
         "zombie/attack2",
         accessor -> new AttackAnimation(0.1F, 0.3F, 0.4F, 0.6F, 0.85F, ColliderPreset.FIST, Armatures.BIPED.get().toolL, accessor, Armatures.BIPED)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      ZOMBIE_ATTACK3 = builder.nextAccessor(
         "zombie/attack3",
         accessor -> new AttackAnimation(0.1F, 0.5F, 0.5F, 0.6F, 1.15F, ColliderPreset.HEAD, Armatures.BIPED.get().head, accessor, Armatures.BIPED)
      );
      SWEEPING_EDGE = builder.nextAccessor(
         "biped/skill/sweeping_edge",
         accessor -> new AttackAnimation(0.1F, 0.0F, 0.15F, 0.3F, 0.8F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .<StaticAnimation, Float>addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F)
            .<StaticAnimation, Integer>addProperty(AnimationProperty.AttackAnimationProperty.EXTRA_COLLIDERS, 1)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      DANCING_EDGE = builder.nextAccessor(
         "biped/skill/dancing_edge",
         accessor -> new AttackAnimation(
               0.1F,
               accessor,
               Armatures.BIPED,
               new AttackAnimation.Phase(0.0F, 0.25F, 0.4F, 0.4F, 0.4F, Armatures.BIPED.get().toolR, null),
               new AttackAnimation.Phase(0.4F, 0.4F, 0.5F, 0.55F, 0.6F, InteractionHand.OFF_HAND, Armatures.BIPED.get().toolL, null),
               new AttackAnimation.Phase(0.6F, 0.6F, 0.7F, 1.15F, Float.MAX_VALUE, Armatures.BIPED.get().toolR, null)
            )
            .<StaticAnimation, Float>addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F)
            .addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
      );
      THE_GUILLOTINE = builder.nextAccessor(
         "biped/skill/the_guillotine",
         accessor -> new AttackAnimation(0.15F, 0.2F, 0.7F, 0.75F, 1.1F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
            .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)
      );
      HEARTPIERCER = builder.nextAccessor(
         "biped/skill/heartpiercer",
         accessor -> new AttackAnimation(
               0.11F,
               accessor,
               Armatures.BIPED,
               new AttackAnimation.Phase(0.0F, 0.3F, 0.36F, 0.5F, 0.5F, Armatures.BIPED.get().toolR, null),
               new AttackAnimation.Phase(0.5F, 0.5F, 0.56F, 0.75F, 0.75F, Armatures.BIPED.get().toolR, null),
               new AttackAnimation.Phase(0.75F, 0.75F, 0.81F, 1.05F, Float.MAX_VALUE, Armatures.BIPED.get().toolR, null)
            )
            .<StaticAnimation, Float>addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      GRASPING_SPIRAL_FIRST = builder.nextAccessor(
         "biped/skill/grasping_spire_first",
         accessor -> new AttackAnimation(0.1F, 0.25F, 0.3F, 0.4F, 0.8F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .<StaticAnimation, Float>addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F)
            .<StaticAnimation, AnimationProperty.PoseModifier>addProperty(
               AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER
            )
            .<StaticAnimation>setResourceLocation("epicfight", "biped/combat/spear_dash")
            .<StaticAnimation>addEvents(
               AnimationProperty.StaticAnimationProperty.ON_END_EVENTS,
               AnimationEvent.SimpleEvent.create(
                  (entitypatch, animation, params) -> {
                     List<LivingEntity> hitEnemies = entitypatch.getCurrentlyActuallyHitEntities();
                     Vec3 vec = ((LivingEntity)entitypatch.getOriginal())
                        .m_20182_()
                        .m_82549_(Vec3.m_82503_(new Vec2(0.0F, ((LivingEntity)entitypatch.getOriginal()).m_146908_())));
                     if (animation.get() instanceof AttackAnimation attackAnimation) {
                        for (LivingEntity e : hitEnemies) {
                           if (e.m_6084_()) {
                              LivingEntityPatch<?> targetpatch = EpicFightCapabilities.getEntityPatch(e, LivingEntityPatch.class);
                              if (targetpatch != null) {
                                 DamageSource dmgSource = attackAnimation.getEpicFightDamageSource(entitypatch, e, attackAnimation.phases[0]);
                                 if (!targetpatch.tryHurt(dmgSource, 0.0F).resultType.dealtDamage()) {
                                    continue;
                                 }
                              }

                              Vec3 toAttacker = e.m_20182_().m_82546_(vec).m_82542_(0.3F, 0.3F, 0.3F);
                              e.m_146884_(vec.m_82549_(toAttacker));
                           }
                        }
                     }
                  },
                  AnimationEvent.Side.SERVER
               )
            )
            .addEvents(AnimationEvent.InTimeEvent.create(0.75F, (entitypatch, animation, params) -> {
               if (entitypatch.isLastAttackSuccess()) {
                  entitypatch.playAnimationSynchronized(GRASPING_SPIRAL_SECOND, 0.0F);
               }
            }, AnimationEvent.Side.SERVER))
      );
      GRASPING_SPIRAL_SECOND = builder.nextAccessor(
         "biped/skill/grasping_spire_second",
         accessor -> new AttackAnimation(0.1F, 0.0F, 0.5F, 0.6F, 0.95F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .<StaticAnimation, Float>addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.2F)
            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER)
      );
      STEEL_WHIRLWIND = builder.nextAccessor(
         "biped/skill/steel_whirlwind",
         accessor -> new AttackAnimation(
               0.15F,
               accessor,
               Armatures.BIPED,
               new AttackAnimation.Phase(0.0F, 0.0F, 0.0F, 0.2F, 0.45F, 0.45F, Armatures.BIPED.get().rootJoint, ColliderPreset.STEEL_WHIRLWIND),
               new AttackAnimation.Phase(0.45F, 0.45F, 0.45F, 0.65F, 1.0F, 1.0F, Armatures.BIPED.get().rootJoint, ColliderPreset.STEEL_WHIRLWIND),
               new AttackAnimation.Phase(1.0F, 1.0F, 1.0F, 1.2F, 2.55F, Float.MAX_VALUE, Armatures.BIPED.get().rootJoint, ColliderPreset.STEEL_WHIRLWIND)
            )
            .<StaticAnimation, Float>addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.0F)
            .<StaticAnimation, Integer>addProperty(AnimationProperty.AttackAnimationProperty.EXTRA_COLLIDERS, 4)
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(
               AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN,
               (animation, entitypatch, transformSheet) -> {
                  if (!animation.isLinkAnimation()) {
                     int chargingPower = entitypatch.<Animator>getAnimator()
                        .getVariables()
                        .<Integer>get(
                           (AnimationVariables.IndependentAnimationVariableKey<Integer>)SynchedAnimationVariableKeys.CHARGING_TICKS.get(),
                           animation.getRealAnimation()
                        )
                        .orElse(0);
                     transformSheet.readFrom(animation.getCoord().copyAll().extendsZCoord(0.6666F + chargingPower / 5.0F, 0, 2));
                  } else {
                     MoveCoordFunctions.RAW_COORD.set(animation, entitypatch, transformSheet);
                  }
               }
            )
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, null)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, false)
            .<StaticAnimation, AnimationProperty.PoseModifier>addProperty(
               AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER
            )
            .<StaticAnimation, AnimationProperty.PlaybackSpeedModifier>addProperty(
               AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER,
               (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> {
                  if (elapsedTime < 1.05F) {
                     int chargingPower = entitypatch.<Animator>getAnimator()
                        .getVariables()
                        .<Integer>get(
                           (AnimationVariables.IndependentAnimationVariableKey<Integer>)SynchedAnimationVariableKeys.CHARGING_TICKS.get(),
                           self.getRealAnimation()
                        )
                        .orElse(0);
                     return 0.6666F + chargingPower / 20.0F;
                  } else {
                     return 1.0F;
                  }
               }
            )
            .<StaticAnimation>newTimePair(0.0F, 2.55F)
            .addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, false)
      );
      BATTOJUTSU = builder.nextAccessor(
         "biped/skill/battojutsu",
         accessor -> new AttackAnimation(0.15F, 0.0F, 0.75F, 0.8F, 1.2F, ColliderPreset.BATTOJUTSU, Armatures.BIPED.get().rootJoint, accessor, Armatures.BIPED)
            .<SoundEvent, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.SWING_SOUND, (SoundEvent)EpicFightSounds.WHOOSH_SHARP.get())
            .<StaticAnimation, AnimationProperty.PlaybackSpeedModifier>addProperty(
               AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE
            )
            .addEvents(
               AnimationEvent.InTimeEvent.create(0.05F, Animations.ReusableSources.PLAY_SOUND, AnimationEvent.Side.SERVER)
                  .params((SoundEvent)EpicFightSounds.SWORD_IN.get())
            )
      );
      BATTOJUTSU_DASH = builder.nextAccessor(
         "biped/skill/battojutsu_dash",
         accessor -> new AttackAnimation(
               0.15F, 0.43F, 0.7F, 0.8F, 1.4F, ColliderPreset.BATTOJUTSU_DASH, Armatures.BIPED.get().rootJoint, accessor, Armatures.BIPED
            )
            .<SoundEvent, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.SWING_SOUND, (SoundEvent)EpicFightSounds.WHOOSH_SHARP.get())
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(
               AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.RAW_COORD
            )
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, null)
            .<StaticAnimation, AnimationProperty.PlaybackSpeedModifier>addProperty(
               AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE
            )
            .addEvents(
               AnimationEvent.InTimeEvent.create(0.05F, Animations.ReusableSources.PLAY_SOUND, AnimationEvent.Side.SERVER)
                  .params((SoundEvent)EpicFightSounds.SWORD_IN.get()),
               AnimationEvent.InTimeEvent.create(
                  0.65F,
                  (entitypatch, animation, params) -> {
                     LivingEntity entity = (LivingEntity)entitypatch.getOriginal();
                     entity.m_9236_()
                        .m_7106_(
                           (ParticleOptions)EpicFightParticles.WHITE_AFTERIMAGE.get(),
                           entity.m_20185_(),
                           entity.m_20186_(),
                           entity.m_20189_(),
                           Double.longBitsToDouble(entity.m_19879_()),
                           0.0,
                           0.0
                        );
                     RandomSource random = entity.m_217043_();
                     double x = entity.m_20185_() + (random.m_188500_() - random.m_188500_()) * 2.0;
                     double y = entity.m_20186_();
                     double z = entity.m_20189_() + (random.m_188500_() - random.m_188500_()) * 2.0;
                     entity.m_9236_().m_7106_(ParticleTypes.f_123813_, x, y, z, random.m_188500_() * 0.005, 0.0, 0.0);
                  },
                  AnimationEvent.Side.CLIENT
               )
            )
      );
      RUSHING_TEMPO1 = builder.nextAccessor(
         "biped/skill/rushing_tempo1",
         accessor -> new AttackAnimation(0.05F, 0.0F, 0.15F, 0.25F, 0.6F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .<StaticAnimation, Float>addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F)
            .<StaticAnimation, Integer>addProperty(AnimationProperty.AttackAnimationProperty.EXTRA_COLLIDERS, 2)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.RESET_PLAYER_COMBO_COUNTER, false)
            .<StaticAnimation>newTimePair(0.0F, 0.25F)
            .addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, false)
      );
      RUSHING_TEMPO2 = builder.nextAccessor(
         "biped/skill/rushing_tempo2",
         accessor -> new AttackAnimation(0.05F, 0.0F, 0.15F, 0.25F, 0.6F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .<StaticAnimation, Float>addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F)
            .<StaticAnimation, Integer>addProperty(AnimationProperty.AttackAnimationProperty.EXTRA_COLLIDERS, 2)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.RESET_PLAYER_COMBO_COUNTER, false)
            .<StaticAnimation>newTimePair(0.0F, 0.25F)
            .addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, false)
      );
      RUSHING_TEMPO3 = builder.nextAccessor(
         "biped/skill/rushing_tempo3",
         accessor -> new AttackAnimation(0.05F, 0.0F, 0.2F, 0.25F, 0.6F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .<StaticAnimation, Float>addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F)
            .<StaticAnimation, Integer>addProperty(AnimationProperty.AttackAnimationProperty.EXTRA_COLLIDERS, 2)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.RESET_PLAYER_COMBO_COUNTER, false)
            .<StaticAnimation>newTimePair(0.0F, 0.25F)
            .addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, false)
      );
      RELENTLESS_COMBO = builder.nextAccessor(
         "biped/skill/relentless_combo",
         accessor -> new AttackAnimation(
               0.05F,
               accessor,
               Armatures.BIPED,
               new AttackAnimation.Phase(
                  0.0F, 0.016F, 0.066F, 0.133F, 0.133F, InteractionHand.OFF_HAND, Armatures.BIPED.get().rootJoint, ColliderPreset.FIST_FIXED
               ),
               new AttackAnimation.Phase(0.133F, 0.133F, 0.183F, 0.25F, 0.25F, Armatures.BIPED.get().rootJoint, ColliderPreset.FIST_FIXED),
               new AttackAnimation.Phase(
                  0.25F, 0.25F, 0.3F, 0.366F, 0.366F, InteractionHand.OFF_HAND, Armatures.BIPED.get().rootJoint, ColliderPreset.FIST_FIXED
               ),
               new AttackAnimation.Phase(0.366F, 0.366F, 0.416F, 0.483F, 0.483F, Armatures.BIPED.get().rootJoint, ColliderPreset.FIST_FIXED),
               new AttackAnimation.Phase(
                  0.483F, 0.483F, 0.533F, 0.6F, 0.6F, InteractionHand.OFF_HAND, Armatures.BIPED.get().rootJoint, ColliderPreset.FIST_FIXED
               ),
               new AttackAnimation.Phase(0.6F, 0.6F, 0.65F, 0.716F, 0.716F, Armatures.BIPED.get().rootJoint, ColliderPreset.FIST_FIXED),
               new AttackAnimation.Phase(
                  0.716F, 0.716F, 0.766F, 0.833F, 0.833F, InteractionHand.OFF_HAND, Armatures.BIPED.get().rootJoint, ColliderPreset.FIST_FIXED
               ),
               new AttackAnimation.Phase(0.833F, 0.833F, 0.883F, 1.1F, 1.1F, Armatures.BIPED.get().rootJoint, ColliderPreset.FIST_FIXED)
            )
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 4.0F)
      );
      EVISCERATE_FIRST = builder.nextAccessor(
         "biped/skill/eviscerate_first",
         accessor -> new AttackAnimation(0.08F, 0.0F, 0.05F, 0.15F, 0.45F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .<StaticAnimation, Float>addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F)
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, null)
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(
               AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_TARGET_LOCATION_ROTATION
            )
            .addProperty(AnimationProperty.ActionAnimationProperty.ENTITY_YROT_PROVIDER, MoveCoordFunctions.LOOK_DEST)
      );
      EVISCERATE_SECOND = builder.nextAccessor(
         "biped/skill/eviscerate_second",
         accessor -> new AttackAnimation(0.15F, 0.0F, 0.04F, 0.05F, 0.4F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
            .<SoundEvent, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.HIT_SOUND, (SoundEvent)EpicFightSounds.EVISCERATE.get())
            .<RegistryObject<HitParticleType>, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.PARTICLE, EpicFightParticles.EVISCERATE)
            .addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 2.4F)
      );
      BLADE_RUSH_COMBO1 = builder.nextAccessor(
         "biped/skill/blade_rush_combo1",
         accessor -> new AttackAnimation(
               0.1F, 0.0F, 0.15F, 0.35F, 0.85F, ColliderPreset.BIPED_BODY_COLLIDER, Armatures.BIPED.get().rootJoint, accessor, Armatures.BIPED
            )
            .<HitEntityList.Priority, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.HIT_PRIORITY, HitEntityList.Priority.TARGET)
            .<StaticAnimation, Float>addProperty(AnimationProperty.AttackAnimationProperty.ATTACK_SPEED_FACTOR, 0.0F)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.MOVE_ON_LINK, false)
            .<StaticAnimation, TimePairList>addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.0F, 0.35F))
            .<StaticAnimation, AnimationProperty.DestLocationProvider>addProperty(
               AnimationProperty.ActionAnimationProperty.DEST_LOCATION_PROVIDER, MoveCoordFunctions.SYNCHED_TARGET_ENTITY_LOCATION_VARIABLE
            )
            .<StaticAnimation, TimePairList>addProperty(AnimationProperty.ActionAnimationProperty.COORD_UPDATE_TIME, TimePairList.create(0.0F, 0.25F))
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, null)
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(
               AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_ORIGIN_AS_DESTINATION
            )
            .<StaticAnimation, MoveCoordFunctions.MoveCoordGetter>addProperty(
               AnimationProperty.ActionAnimationProperty.COORD_GET, MoveCoordFunctions.WORLD_COORD
            )
            .<StaticAnimation, Integer>addProperty(AnimationProperty.ActionAnimationProperty.COORD_START_KEYFRAME_INDEX, 1)
            .<StaticAnimation, Integer>addProperty(AnimationProperty.ActionAnimationProperty.COORD_DEST_KEYFRAME_INDEX, 4)
            .<StaticAnimation, AnimationProperty.YRotProvider>addProperty(
               AnimationProperty.ActionAnimationProperty.ENTITY_YROT_PROVIDER, MoveCoordFunctions.LOOK_DEST
            )
            .<StaticAnimation, AnimationProperty.PlaybackSpeedModifier>addProperty(
               AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE
            )
            .<StaticAnimation>newTimePair(0.0F, 0.65F)
            .addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, false)
      );
      BLADE_RUSH_COMBO2 = builder.nextAccessor(
         "biped/skill/blade_rush_combo2",
         accessor -> new AttackAnimation(
               0.1F, 0.0F, 0.15F, 0.35F, 0.85F, ColliderPreset.BIPED_BODY_COLLIDER, Armatures.BIPED.get().rootJoint, accessor, Armatures.BIPED
            )
            .<HitEntityList.Priority, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.HIT_PRIORITY, HitEntityList.Priority.TARGET)
            .<StaticAnimation, Float>addProperty(AnimationProperty.AttackAnimationProperty.ATTACK_SPEED_FACTOR, 0.0F)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.MOVE_ON_LINK, false)
            .<StaticAnimation, TimePairList>addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.0F, 0.35F))
            .<StaticAnimation, AnimationProperty.DestLocationProvider>addProperty(
               AnimationProperty.ActionAnimationProperty.DEST_LOCATION_PROVIDER, MoveCoordFunctions.SYNCHED_TARGET_ENTITY_LOCATION_VARIABLE
            )
            .<StaticAnimation, TimePairList>addProperty(AnimationProperty.ActionAnimationProperty.COORD_UPDATE_TIME, TimePairList.create(0.0F, 0.3F))
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, null)
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(
               AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_ORIGIN_AS_DESTINATION
            )
            .<StaticAnimation, MoveCoordFunctions.MoveCoordGetter>addProperty(
               AnimationProperty.ActionAnimationProperty.COORD_GET, MoveCoordFunctions.WORLD_COORD
            )
            .<StaticAnimation, Integer>addProperty(AnimationProperty.ActionAnimationProperty.COORD_START_KEYFRAME_INDEX, 1)
            .<StaticAnimation, Integer>addProperty(AnimationProperty.ActionAnimationProperty.COORD_DEST_KEYFRAME_INDEX, 2)
            .<StaticAnimation, AnimationProperty.YRotProvider>addProperty(
               AnimationProperty.ActionAnimationProperty.ENTITY_YROT_PROVIDER, MoveCoordFunctions.LOOK_DEST
            )
            .<StaticAnimation, AnimationProperty.PlaybackSpeedModifier>addProperty(
               AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE
            )
            .<StaticAnimation>newTimePair(0.0F, 0.65F)
            .addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, false)
      );
      BLADE_RUSH_COMBO3 = builder.nextAccessor(
         "biped/skill/blade_rush_combo3",
         accessor -> new AttackAnimation(
               0.1F, 0.0F, 0.2F, 0.35F, 0.85F, ColliderPreset.BIPED_BODY_COLLIDER, Armatures.BIPED.get().rootJoint, accessor, Armatures.BIPED
            )
            .<HitEntityList.Priority, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.HIT_PRIORITY, HitEntityList.Priority.TARGET)
            .<StaticAnimation, Float>addProperty(AnimationProperty.AttackAnimationProperty.ATTACK_SPEED_FACTOR, 0.0F)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.MOVE_ON_LINK, false)
            .<StaticAnimation, TimePairList>addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.0F, 0.35F))
            .<StaticAnimation, AnimationProperty.DestLocationProvider>addProperty(
               AnimationProperty.ActionAnimationProperty.DEST_LOCATION_PROVIDER, MoveCoordFunctions.SYNCHED_TARGET_ENTITY_LOCATION_VARIABLE
            )
            .<StaticAnimation, TimePairList>addProperty(AnimationProperty.ActionAnimationProperty.COORD_UPDATE_TIME, TimePairList.create(0.0F, 0.25F))
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, null)
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(
               AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, MoveCoordFunctions.TRACE_ORIGIN_AS_DESTINATION
            )
            .<StaticAnimation, MoveCoordFunctions.MoveCoordGetter>addProperty(
               AnimationProperty.ActionAnimationProperty.COORD_GET, MoveCoordFunctions.WORLD_COORD
            )
            .<StaticAnimation, Integer>addProperty(AnimationProperty.ActionAnimationProperty.COORD_START_KEYFRAME_INDEX, 1)
            .<StaticAnimation, Integer>addProperty(AnimationProperty.ActionAnimationProperty.COORD_DEST_KEYFRAME_INDEX, 4)
            .<StaticAnimation, AnimationProperty.YRotProvider>addProperty(
               AnimationProperty.ActionAnimationProperty.ENTITY_YROT_PROVIDER, MoveCoordFunctions.LOOK_DEST
            )
            .<StaticAnimation, AnimationProperty.PlaybackSpeedModifier>addProperty(
               AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE
            )
            .<StaticAnimation>newTimePair(0.0F, 0.6F)
            .addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, false)
      );
      BLADE_RUSH_HIT = builder.nextAccessor(
         "biped/interact/blade_rush_hit",
         accessor -> new LongHitAnimation(0.1F, accessor, Armatures.BIPED).addProperty(AnimationProperty.ActionAnimationProperty.IS_DEATH_ANIMATION, true)
      );
      BLADE_RUSH_EXECUTE_BIPED = builder.nextAccessor(
         "biped/skill/blade_rush_execute",
         accessor -> new GrapplingAttackAnimation(0.5F, 1.5F, accessor, Armatures.BIPED)
            .<Set<TagKey<DamageType>>, AttackAnimation>addProperty(
               AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.EXECUTION, DamageTypeTags.f_268490_)
            )
            .<StaticAnimation, TimePairList>addProperty(AnimationProperty.ActionAnimationProperty.COORD_UPDATE_TIME, TimePairList.create(0.0F, 0.5F))
            .<StaticAnimation, TimePairList>addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.0F, 0.95F))
            .addEvents(AnimationEvent.InTimeEvent.create(0.1F, (entitypatch, animation, params) -> {
               LivingEntity grapplingTarget = entitypatch.getGrapplingTarget();
               if (grapplingTarget != null) {
                  entitypatch.playSound((SoundEvent)EpicFightSounds.BLADE_HIT.get(), 0.0F, 0.0F);
               }
            }, AnimationEvent.Side.CLIENT), AnimationEvent.InTimeEvent.create(0.3F, (entitypatch, animation, params) -> {
               LivingEntity grapplingTarget = entitypatch.getGrapplingTarget();
               if (grapplingTarget != null) {
                  entitypatch.playSound((SoundEvent)EpicFightSounds.BLADE_HIT.get(), 0.0F, 0.0F);
               }
            }, AnimationEvent.Side.CLIENT))
      );
      BLADE_RUSH_FAILED = builder.nextAccessor(
         "biped/skill/blade_rush_failed",
         accessor -> new ActionAnimation(0.0F, 0.85F, accessor, Armatures.BIPED)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
            .addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.0F, 0.0F))
      );
      BLADE_RUSH_TRY = builder.nextAccessor(
         "biped/skill/blade_rush_try",
         accessor -> new GrapplingTryAnimation(
               0.1F,
               0.0F,
               0.4F,
               0.4F,
               0.45F,
               ColliderPreset.BIPED_BODY_COLLIDER,
               Armatures.BIPED.get().rootJoint,
               accessor,
               BLADE_RUSH_HIT,
               BLADE_RUSH_EXECUTE_BIPED,
               BLADE_RUSH_FAILED,
               Armatures.BIPED
            )
            .<StaticAnimation, Integer>addProperty(AnimationProperty.ActionAnimationProperty.COORD_START_KEYFRAME_INDEX, 1)
            .<StaticAnimation, TimePairList>addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.15F, 0.35F))
            .addProperty(AnimationProperty.ActionAnimationProperty.DEST_LOCATION_PROVIDER, MoveCoordFunctions.SYNCHED_TARGET_ENTITY_LOCATION_VARIABLE)
      );
      WRATHFUL_LIGHTING = builder.nextAccessor(
         "biped/skill/wrathful_lighting",
         accessor -> new AttackAnimation(
               0.15F,
               accessor,
               Armatures.BIPED,
               new AttackAnimation.Phase(0.0F, 0.0F, 0.3F, 0.36F, 1.0F, Float.MAX_VALUE, Armatures.BIPED.get().toolR, null),
               new AttackAnimation.Phase(InteractionHand.MAIN_HAND, Armatures.BIPED.get().rootJoint, null)
            )
            .<StaticAnimation, AnimationProperty.PlaybackSpeedModifier>addProperty(
               AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE
            )
            .addEvents(AnimationEvent.InTimeEvent.create(0.35F, Animations.ReusableSources.SUMMON_THUNDER, AnimationEvent.Side.SERVER))
      );
      TSUNAMI = builder.nextAccessor(
         "biped/skill/tsunami",
         accessor -> new AttackAnimation(
               0.2F, 0.2F, 0.35F, 1.0F, 1.8F, ColliderPreset.BIPED_BODY_COLLIDER, Armatures.BIPED.get().rootJoint, accessor, Armatures.BIPED
            )
            .<ValueModifier, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(10.0F))
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(
               AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.RAW_COORD
            )
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, null)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
            .<StaticAnimation, TimePairList>addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.2F, 1.1F))
            .<StaticAnimation, AnimationProperty.PlaybackSpeedModifier>addProperty(
               AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE
            )
            .<StaticAnimation>addEvents(
               AnimationProperty.StaticAnimationProperty.ON_END_EVENTS,
               AnimationEvent.SimpleEvent.create(Animations.ReusableSources.RESTORE_BOUNDING_BOX, AnimationEvent.Side.BOTH)
            )
            .<StaticAnimation>addEvents(
               AnimationProperty.StaticAnimationProperty.TICK_EVENTS,
               AnimationEvent.SimpleEvent.create(Animations.ReusableSources.RESIZE_BOUNDING_BOX, AnimationEvent.Side.BOTH)
                  .params(EntityDimensions.m_20395_(0.6F, 1.0F))
            )
            .<StaticAnimation>addEvents(
               AnimationEvent.InPeriodEvent.create(
                  0.35F,
                  1.0F,
                  (entitypatch, animation, params) -> {
                     Vec3 pos = ((LivingEntity)entitypatch.getOriginal()).m_20182_();

                     for (int x = -1; x <= 1; x += 2) {
                        for (int z = -1; z <= 1; z += 2) {
                           Vec3 rand = new Vec3(Math.random() * x, Math.random(), Math.random() * z).m_82541_().m_82490_(2.0);
                           ((LivingEntity)entitypatch.getOriginal())
                              .m_9236_()
                              .m_7106_(
                                 (ParticleOptions)EpicFightParticles.TSUNAMI_SPLASH.get(),
                                 pos.f_82479_ + rand.f_82479_,
                                 pos.f_82480_ + rand.f_82480_ - 1.0,
                                 pos.f_82481_ + rand.f_82481_,
                                 rand.f_82479_ * 0.1,
                                 rand.f_82480_ * 0.1,
                                 rand.f_82481_ * 0.1
                              );
                        }
                     }
                  },
                  AnimationEvent.Side.CLIENT
               )
            )
            .addEvents(
               AnimationEvent.InTimeEvent.create(
                  0.35F, (entitypatch, animation, params) -> entitypatch.playSound(SoundEvents.f_12519_, 0.0F, 0.0F), AnimationEvent.Side.CLIENT
               ),
               AnimationEvent.InTimeEvent.create(0.35F, (entitypatch, animation, params) -> entitypatch.setAirborneState(true), AnimationEvent.Side.SERVER)
            )
      );
      TSUNAMI_REINFORCED = builder.nextAccessor(
         "biped/skill/tsunami_reinforced",
         accessor -> new AttackAnimation(
               0.2F, 0.2F, 0.35F, 0.65F, 1.3F, ColliderPreset.BIPED_BODY_COLLIDER, Armatures.BIPED.get().rootJoint, accessor, Armatures.BIPED
            )
            .<ValueModifier, AttackAnimation>addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(10.0F))
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(
               AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.RAW_COORD_WITH_X_ROT
            )
            .<StaticAnimation, MoveCoordFunctions.MoveCoordSetter>addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, null)
            .<StaticAnimation, Boolean>addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
            .<StaticAnimation, TimePairList>addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.15F, 0.85F))
            .<StaticAnimation, AnimationProperty.PlaybackSpeedModifier>addProperty(
               AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE
            )
            .<StaticAnimation, AnimationProperty.PoseModifier>addProperty(
               AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.ROOT_X_MODIFIER
            )
            .<StaticAnimation>addEvents(
               AnimationProperty.StaticAnimationProperty.ON_END_EVENTS,
               AnimationEvent.SimpleEvent.create(Animations.ReusableSources.RESTORE_BOUNDING_BOX, AnimationEvent.Side.BOTH)
            )
            .<StaticAnimation>addEvents(
               AnimationProperty.StaticAnimationProperty.TICK_EVENTS,
               AnimationEvent.SimpleEvent.create(Animations.ReusableSources.RESIZE_BOUNDING_BOX, AnimationEvent.Side.BOTH)
                  .params(EntityDimensions.m_20395_(0.6F, 1.0F))
            )
            .<StaticAnimation>addEvents(
               AnimationEvent.InPeriodEvent.create(
                  0.35F,
                  1.0F,
                  (entitypatch, animation, params) -> {
                     Vec3 pos = ((LivingEntity)entitypatch.getOriginal()).m_20182_();

                     for (int x = -1; x <= 1; x += 2) {
                        for (int z = -1; z <= 1; z += 2) {
                           Vec3 rand = new Vec3(Math.random() * x, Math.random(), Math.random() * z).m_82541_().m_82490_(2.0);
                           ((LivingEntity)entitypatch.getOriginal())
                              .m_9236_()
                              .m_7106_(
                                 (ParticleOptions)EpicFightParticles.TSUNAMI_SPLASH.get(),
                                 pos.f_82479_ + rand.f_82479_,
                                 pos.f_82480_ + rand.f_82480_ - 1.0,
                                 pos.f_82481_ + rand.f_82481_,
                                 rand.f_82479_ * 0.1,
                                 rand.f_82480_ * 0.1,
                                 rand.f_82481_ * 0.1
                              );
                        }
                     }
                  },
                  AnimationEvent.Side.CLIENT
               )
            )
            .addEvents(
               AnimationEvent.InTimeEvent.create(
                  0.35F, (entitypatch, animation, params) -> entitypatch.playSound(SoundEvents.f_12519_, 0.0F, 0.0F), AnimationEvent.Side.CLIENT
               ),
               AnimationEvent.InTimeEvent.create(0.35F, (entitypatch, animation, params) -> entitypatch.setAirborneState(true), AnimationEvent.Side.SERVER)
            )
      );
      EVERLASTING_ALLEGIANCE_CALL = builder.nextAccessor(
         "biped/skill/everlasting_allegiance_call",
         accessor -> new ActionAnimation(0.1F, 0.55F, accessor, Armatures.BIPED).addProperty(AnimationProperty.ActionAnimationProperty.STOP_MOVEMENT, true)
      );
      EVERLASTING_ALLEGIANCE_CATCH = builder.nextAccessor(
         "biped/skill/everlasting_allegiance_catch",
         accessor -> new ActionAnimation(0.05F, 0.8F, accessor, Armatures.BIPED).addProperty(AnimationProperty.ActionAnimationProperty.STOP_MOVEMENT, true)
      );
      SHARP_STAB = builder.nextAccessor(
         "biped/skill/sharp_stab",
         accessor -> new AttackAnimation(0.15F, 0.05F, 0.1F, 0.15F, 0.7F, ColliderPreset.LONGSWORD, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
      );
   }

   public abstract static class ReusableSources {
      public static final AnimationEvent.E1<EntityDimensions> RESIZE_BOUNDING_BOX = (entitypatch, animation, params) -> {
         if (params != null) {
            entitypatch.resetSize(params.first());
         }
      };
      public static final AnimationEvent.E1<Boolean> RESTORE_BOUNDING_BOX = (entitypatch, animation, params) -> entitypatch.getOriginal().m_6210_();
      public static final AnimationEvent.E0 WING_FLAP = (entitypatch, animation, params) -> {
         if (entitypatch instanceof EnderDragonPatch enderDragonPatch) {
            enderDragonPatch.getOriginal().m_142043_();
         }
      };
      public static final AnimationEvent.E4<Vec3f, Joint, Double, Float> FRACTURE_GROUND_SIMPLE = (entitypatch, animation, params) -> {
         Vec3 position = entitypatch.getOriginal().m_20182_();
         OpenMatrix4f modelTransform = entitypatch.getArmature()
            .getBoundTransformFor(animation.get().getPoseByTime(entitypatch, params.fourth(), 1.0F), params.second())
            .mulFront(
               OpenMatrix4f.createTranslation((float)position.f_82479_, (float)position.f_82480_, (float)position.f_82481_)
                  .mulBack(OpenMatrix4f.createRotatorDeg(180.0F, Vec3f.Y_AXIS).mulBack(entitypatch.getModelMatrix(1.0F)))
            );
         Level level = entitypatch.getOriginal().m_9236_();
         Vec3 weaponEdge = OpenMatrix4f.transform(modelTransform, params.first().toDoubleVector());
         BlockHitResult hitResult = level.m_45547_(
            new ClipContext(position.m_82520_(0.0, 0.1, 0.0), weaponEdge, Block.COLLIDER, Fluid.NONE, entitypatch.getOriginal())
         );
         Vec3 slamStartPos;
         if (hitResult.m_6662_() == Type.BLOCK) {
            Direction direction = hitResult.m_82434_();
            BlockPos collidePos = hitResult.m_82425_().m_7918_(direction.m_122429_(), direction.m_122430_(), direction.m_122431_());
            if (!LevelUtil.canTransferShockWave(level, collidePos, level.m_8055_(collidePos))) {
               collidePos = collidePos.m_7495_();
            }

            slamStartPos = new Vec3(collidePos.m_123341_(), collidePos.m_123342_(), collidePos.m_123343_());
         } else {
            slamStartPos = weaponEdge.m_82492_(0.0, 1.0, 0.0);
         }

         LevelUtil.circleSlamFracture(entitypatch.getOriginal(), level, slamStartPos, params.third(), false, false);
      };
      public static final AnimationEvent.E3<Vec3f, Joint, Float> FRACTURE_METEOR_STRIKE = (entitypatch, animation, params) -> {
         if (entitypatch instanceof PlayerPatch<?> playerpatch) {
            Optional<SkillContainer> skill = playerpatch.getSkillContainerFor(EpicFightSkills.METEOR_STRIKE);
            if (skill.isPresent()) {
               double slamRadius = Math.log(
                  MeteorSlamSkill.getFallDistance(skill.get()) * entitypatch.getOriginal().m_21133_((Attribute)EpicFightAttributes.IMPACT.get())
               );
               FRACTURE_GROUND_SIMPLE.fire(entitypatch, animation, AnimationParameters.of(params.first(), params.second(), slamRadius, params.third()));
            }
         }
      };
      public static final AnimationEvent.E0 SUMMON_THUNDER = (entitypatch, animation, params) -> {
         if (!entitypatch.isLogicalClient()) {
            if (animation.get() instanceof AttackAnimation attackAnimation) {
               AttackAnimation.Phase phase = attackAnimation.phases[1];
               int i = (int)ValueModifier.calculator()
                  .attach(phase.getProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER).orElse(ValueModifier.setter(3.0F)))
                  .getResult(0.0F);
               float damage = ValueModifier.calculator()
                  .attach(phase.getProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER).orElse(ValueModifier.setter(8.0F)))
                  .getResult(0.0F);
               LivingEntity original = (LivingEntity)entitypatch.getOriginal();
               ServerLevel level = (ServerLevel)original.m_9236_();
               float total = damage
                  + ExtraDamageInstance.SWEEPING_EDGE_ENCHANTMENT.create().get(original, original.m_21120_(InteractionHand.MAIN_HAND), null, damage);
               List<Entity> list = level.m_6249_(
                  original,
                  original.m_20191_().m_82377_(10.0, 4.0, 10.0),
                  ex -> !(ex.m_20280_(original) > 100.0) && !ex.m_7307_(original) && ((LivingEntity)entitypatch.getOriginal()).m_142582_(ex)
               );
               list = HitEntityList.Priority.HOSTILITY.sort(entitypatch, list);
               int count = 0;

               while (count < i && count < list.size()) {
                  Entity e = list.get(count++);
                  BlockPos blockpos = e.m_20183_();
                  LightningBolt lightningbolt = (LightningBolt)EntityType.f_20465_.m_20615_(level);
                  lightningbolt.m_20874_(true);
                  lightningbolt.m_20219_(Vec3.m_82539_(blockpos));
                  lightningbolt.setDamage(0.0F);
                  lightningbolt.m_20879_(entitypatch instanceof ServerPlayerPatch serverPlayerPatch ? serverPlayerPatch.getOriginal() : null);
                  DamageSource dmgSource = new DamageSource(
                     e.m_9236_().m_9598_().m_175515_(Registries.f_268580_).m_246971_(DamageTypes.f_268450_), entitypatch.getOriginal()
                  );
                  EpicFightDamageSource damageSource = attackAnimation.getEpicFightDamageSource(dmgSource, entitypatch, e, phase)
                     .setUsedItem(((LivingEntity)entitypatch.getOriginal()).m_21120_(InteractionHand.MAIN_HAND));
                  e.m_6469_(damageSource, total);
                  e.m_8038_(level, lightningbolt);
                  level.m_7967_(lightningbolt);
               }

               if (count > 0) {
                  if (level.m_46469_().m_46207_(GameRules.f_46150_) && level.f_46441_.m_188501_() < 0.08F && level.m_46661_(1.0F) < 1.0F) {
                     level.m_8606_(0, Mth.m_216287_(level.f_46441_, 12000, 180000), true, true);
                  }

                  original.m_5496_(SoundEvents.f_12521_, 5.0F, 1.0F);
               }
            }
         }
      };
      public static final AnimationEvent.E1<SoundEvent> PLAY_SOUND = (entitypatch, animation, params) -> entitypatch.playSound(params.first(), 0.0F, 0.0F);
      public static final AnimationVariables.IndependentAnimationVariableKey<Boolean> TOOLS_IN_BACK = AnimationVariables.independent(animator -> false, true);
      public static final AnimationEvent.E0 SET_TOOLS_BACK = (entitypatch, animation, params) -> {
         if (entitypatch.getArmature() instanceof ToolHolderArmature toolArmature) {
            moveToolBonesToBack(entitypatch, animation, toolArmature);
         }
      };
      public static final AnimationEvent.E0 SET_TOOLS_BACK_WHEN_MOUNT = (entitypatch, animation, params) -> {
         if (!entitypatch.getHoldingItemCapability(InteractionHand.MAIN_HAND).availableOnHorse()
            && entitypatch.getArmature() instanceof ToolHolderArmature toolArmature) {
            moveToolBonesToBack(entitypatch, animation, toolArmature);
         }
      };
      public static final AnimationEvent.E0 UPDATE_Y_TO_NEARBY_LADDER = (entitypatch, animation, params) -> {
         LivingEntity original = (LivingEntity)entitypatch.getOriginal();
         BlockState bs = original.m_146900_();
         Level level = original.m_9236_();
         BlockPos bp = original.m_20183_();
         boolean isSpectator = entitypatch.getOriginal() instanceof Player && ((LivingEntity)entitypatch.getOriginal()).m_5833_();
         Direction direction = null;
         if (isSpectator || original.m_20096_() || !original.m_6084_()) {
            direction = Direction.UP;
         }

         if ((Boolean)ForgeConfig.SERVER.fullBoundingBoxLadders.get()) {
            if (bs.isLadder(level, bp, original)) {
               if (bs.m_61138_(BlockStateProperties.f_61374_)) {
                  direction = (Direction)bs.m_61143_(BlockStateProperties.f_61374_);
               } else if (bs.m_61138_(BlockStateProperties.f_61366_) && (Boolean)bs.m_61143_(BlockStateProperties.f_61366_)) {
                  direction = Direction.UP;
               } else if (bs.m_61138_(BlockStateProperties.f_61368_) && (Boolean)bs.m_61143_(BlockStateProperties.f_61368_)) {
                  direction = Direction.SOUTH;
               } else if (bs.m_61138_(BlockStateProperties.f_61371_) && (Boolean)bs.m_61143_(BlockStateProperties.f_61371_)) {
                  direction = Direction.EAST;
               } else if (bs.m_61138_(BlockStateProperties.f_61370_) && (Boolean)bs.m_61143_(BlockStateProperties.f_61370_)) {
                  direction = Direction.NORTH;
               } else if (bs.m_61138_(BlockStateProperties.f_61369_) && (Boolean)bs.m_61143_(BlockStateProperties.f_61369_)) {
                  direction = Direction.WEST;
               }
            }
         } else {
            AABB bb = original.m_20191_();
            int mX = Mth.m_14107_(bb.f_82288_);
            int mY = Mth.m_14107_(bb.f_82289_);
            int mZ = Mth.m_14107_(bb.f_82290_);

            for (int y2 = mY; y2 < bb.f_82292_; y2++) {
               for (int x2 = mX; x2 < bb.f_82291_; x2++) {
                  for (int z2 = mZ; z2 < bb.f_82293_; z2++) {
                     BlockPos tmp = new BlockPos(x2, y2, z2);
                     bs = level.m_8055_(tmp);
                     if (bs.isLadder(level, tmp, original)) {
                        if (bs.m_61138_(BlockStateProperties.f_61374_)) {
                           direction = (Direction)bs.m_61143_(BlockStateProperties.f_61374_);
                        } else if (bs.m_61138_(BlockStateProperties.f_61366_) && (Boolean)bs.m_61143_(BlockStateProperties.f_61366_)) {
                           direction = Direction.UP;
                        } else if (bs.m_61138_(BlockStateProperties.f_61368_) && (Boolean)bs.m_61143_(BlockStateProperties.f_61368_)) {
                           direction = Direction.SOUTH;
                        } else if (bs.m_61138_(BlockStateProperties.f_61371_) && (Boolean)bs.m_61143_(BlockStateProperties.f_61371_)) {
                           direction = Direction.EAST;
                        } else if (bs.m_61138_(BlockStateProperties.f_61370_) && (Boolean)bs.m_61143_(BlockStateProperties.f_61370_)) {
                           direction = Direction.NORTH;
                        } else if (bs.m_61138_(BlockStateProperties.f_61369_) && (Boolean)bs.m_61143_(BlockStateProperties.f_61369_)) {
                           direction = Direction.WEST;
                        }
                     }
                  }
               }
            }
         }

         if (direction != null) {
            switch (direction) {
               case NORTH:
                  entitypatch.setYRot(0.0F);
                  entitypatch.setYRotO(0.0F);
                  break;
               case EAST:
                  entitypatch.setYRot(90.0F);
                  entitypatch.setYRotO(90.0F);
                  break;
               case WEST:
                  entitypatch.setYRot(-90.0F);
                  entitypatch.setYRotO(-90.0F);
                  break;
               case SOUTH:
                  entitypatch.setYRot(180.0F);
                  entitypatch.setYRotO(180.0F);
            }
         }
      };
      public static final AnimationEvent.E2<CapabilityItem, CapabilityItem> SET_TOOLS_BACK_WHEN_ITEM_CHANGED = (entitypatch, animation, params) -> {
         if (entitypatch.getArmature() instanceof ToolHolderArmature humanoidArmature) {
            if (!params.first().isEmpty()) {
               moveToolBonesToBack(entitypatch, animation, humanoidArmature);
            } else {
               moveToolBonesToHands(entitypatch, animation, humanoidArmature);
            }
         }
      };
      public static final AnimationEvent.E2<CapabilityItem, CapabilityItem> SET_TOOLS_BACK_WHEN_MOUNT_AND_ITEM_CHANGED = (entitypatch, animation, params) -> {
         if (entitypatch.getArmature() instanceof ToolHolderArmature humanoidArmature) {
            if (!params.first().availableOnHorse()) {
               moveToolBonesToBack(entitypatch, animation, humanoidArmature);
            } else {
               moveToolBonesToHands(entitypatch, animation, humanoidArmature);
            }
         }
      };
      public static final AnimationEvent.E0 REVERT_TO_HANDS = (entitypatch, animation, params) -> {
         if (entitypatch.<Animator>getAnimator().getVariables().getOrDefault(TOOLS_IN_BACK, animation)
            && entitypatch.getArmature() instanceof ToolHolderArmature toolArmature) {
            moveToolBonesToHands(entitypatch, animation, toolArmature);
         }
      };
      public static final AnimationEvent.E0 SYNC_COORD_ROTATION = (entitypatch, animation, params) -> ((StaticAnimation)animation.get())
         .getProperty(AnimationProperty.ActionAnimationProperty.COORD)
         .ifPresent(coordTransform -> {
            Quaternionf rotation = coordTransform.getInterpolatedRotation(((StaticAnimation)animation.get()).getTotalTime());
            Vector3f eulerAngles = rotation.getEulerAnglesYXZ(new Vector3f());
            entitypatch.setYRotO(Mth.m_14177_(entitypatch.getYRot() + (float)Math.toDegrees(eulerAngles.y)));
            entitypatch.setYRot(Mth.m_14177_(entitypatch.getYRot() + (float)Math.toDegrees(eulerAngles.y)));
         });
      public static final AnimationEvent.E0 PLAY_STEPPING_SOUND = (entitypatch, animation, params) -> {
         BlockState state = ((LivingEntity)entitypatch.getOriginal()).m_9236_().m_8055_(((LivingEntity)entitypatch.getOriginal()).m_20097_());
         entitypatch.playSound(state.m_60827_().m_56778_(), 0.0F, 0.0F);
      };
      public static final AnimationProperty.PoseModifier COMBO_ATTACK_DIRECTION_MODIFIER = (self, pose, entitypatch, time, partialTicks) -> {
         if (self.isStaticAnimation() && !(entitypatch instanceof PlayerPatch<?> playerpatch && playerpatch.isFirstPerson())) {
            float pitch = entitypatch.getAttackDirectionPitch();
            JointTransform chest = pose.orElseEmpty("Chest");
            chest.frontResult(JointTransform.rotation(QuaternionUtils.XP.rotationDegrees(-pitch)), OpenMatrix4f::mulAsOriginInverse);
            if (entitypatch instanceof PlayerPatch) {
               float xRot = MathUtils.lerpBetween(entitypatch.getOriginal().f_19860_, entitypatch.getOriginal().m_146909_(), partialTicks);
               OpenMatrix4f toOriginalRotation = entitypatch.getArmature()
                  .getBoundTransformFor(pose, entitypatch.getArmature().searchJointByName("Head"))
                  .removeScale()
                  .removeTranslation()
                  .invert();
               Vec3f xAxis = OpenMatrix4f.transform3v(toOriginalRotation, Vec3f.X_AXIS, null);
               OpenMatrix4f headRotation = OpenMatrix4f.createRotatorDeg(-(pitch + xRot), xAxis);
               pose.orElseEmpty("Head").frontResult(JointTransform.fromMatrix(headRotation), OpenMatrix4f::mul);
            }
         }
      };
      public static final AnimationProperty.PoseModifier ROOT_X_MODIFIER = (self, pose, entitypatch, time, partialTicks) -> {
         float pitch = -entitypatch.getOriginal().m_146909_();
         JointTransform chest = pose.orElseEmpty("Root");
         chest.frontResult(JointTransform.rotation(QuaternionUtils.XP.rotationDegrees(-pitch)), OpenMatrix4f::mulAsOriginInverse);
      };
      public static final AnimationProperty.PoseModifier FLYING_CORRECTION = (self, pose, entitypatch, elapsedTime, partialTicks) -> {
         Vec3 vec3d = entitypatch.getOriginal().m_20252_(partialTicks);
         Vec3 vec3d1 = entitypatch.getOriginal().m_20184_();
         double d0 = vec3d1.m_165925_();
         double d1 = vec3d.m_165925_();
         if (d0 > 0.0 && d1 > 0.0) {
            JointTransform root = pose.orElseEmpty("Root");
            JointTransform head = pose.orElseEmpty("Head");
            double d2 = (vec3d1.f_82479_ * vec3d.f_82479_ + vec3d1.f_82481_ * vec3d.f_82481_) / (Math.sqrt(d0) * Math.sqrt(d1));
            double d3 = vec3d1.f_82479_ * vec3d.f_82481_ - vec3d1.f_82481_ * vec3d.f_82479_;
            float zRot = Mth.m_14036_((float)(Math.signum(d3) * Math.acos(d2)), -1.0F, 1.0F);
            root.frontResult(JointTransform.rotation(QuaternionUtils.ZP.rotation(zRot)), OpenMatrix4f::mulAsOriginInverse);
            float xRot = (float)MathUtils.getXRotOfVector(vec3d1) * 2.0F;
            MathUtils.mulQuaternion(QuaternionUtils.XP.rotationDegrees(xRot), root.rotation(), root.rotation());
            MathUtils.mulQuaternion(QuaternionUtils.XP.rotationDegrees(-xRot), head.rotation(), head.rotation());
         }
      };
      public static final AnimationProperty.PoseModifier FLYING_CORRECTION2 = (self, pose, entitypatch, elapsedTime, partialTicks) -> {
         Vec3 vec3d = entitypatch.getOriginal().m_20252_(partialTicks);
         Vec3 vec3d1 = entitypatch.getOriginal().m_20184_();
         double d0 = vec3d1.m_165925_();
         double d1 = vec3d.m_165925_();
         if (d0 > 0.0 && d1 > 0.0) {
            JointTransform root = pose.orElseEmpty("Root");
            JointTransform head = pose.orElseEmpty("Head");
            float xRot = (float)MathUtils.getXRotOfVector(vec3d1) * 2.0F;
            MathUtils.mulQuaternion(QuaternionUtils.XP.rotationDegrees(-xRot), root.rotation(), root.rotation());
            MathUtils.mulQuaternion(QuaternionUtils.XP.rotationDegrees(xRot), head.rotation(), head.rotation());
         }
      };
      public static final AnimationProperty.PoseModifier MAP_ARMS_CORRECTION = (self, pose, entitypatch, elapsedTime, partialTicks) -> {
         float xRot = 50.0F
            - (entitypatch.getOriginal().f_19860_ + (entitypatch.getOriginal().m_146909_() - entitypatch.getOriginal().f_19860_) * partialTicks);
         xRot = Mth.m_14036_(xRot, 0.0F, 50.0F);
         JointTransform shoulderL = pose.orElseEmpty("Shoulder_L");
         JointTransform shoulderR = pose.orElseEmpty("Shoulder_R");
         float trans = xRot / 500.0F;
         shoulderL.jointLocal(JointTransform.translation(new Vec3f(0.0F, trans, -trans)), OpenMatrix4f::mul);
         shoulderR.jointLocal(JointTransform.translation(new Vec3f(0.0F, trans, -trans)), OpenMatrix4f::mul);
         shoulderL.frontResult(JointTransform.rotation(QuaternionUtils.XP.rotationDegrees(xRot)), OpenMatrix4f::mulAsOriginInverse);
         shoulderR.frontResult(JointTransform.rotation(QuaternionUtils.XP.rotationDegrees(xRot)), OpenMatrix4f::mulAsOriginInverse);
      };
      public static final AnimationProperty.PoseModifier APPLY_COORD_ROTATION = (self, pose, entitypatch, elapsedTime, partialTicks) -> {
         if (!entitypatch.<Animator>getAnimator().getPlayerFor(self.getAccessor()).isEnd()) {
            self.getProperty(AnimationProperty.ActionAnimationProperty.COORD).ifPresent(coordTransform -> {
               Quaternionf rotation = coordTransform.getInterpolatedRotation(elapsedTime);
               pose.get("Root").parent(JointTransform.rotation(rotation), OpenMatrix4f::mul);
            });
         }
      };
      public static final AnimationProperty.PlaybackSpeedModifier CONSTANT_ONE = (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> 1.0F;
      public static final AnimationProperty.PlaybackSpeedModifier CHARGING = (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> self.isLinkAnimation()
         ? 1.0F
         : (float)(-Math.pow((self.getTotalTime() - elapsedTime) / self.getTotalTime() - 1.0F, 2.0)) + 1.0F;

      private static void moveToolBonesToBack(
         LivingEntityPatch<?> entitypatch, AssetAccessor<? extends StaticAnimation> animation, ToolHolderArmature toolArmature
      ) {
         entitypatch.setParentJointOfHand(InteractionHand.MAIN_HAND, toolArmature.backToolJoint());
         entitypatch.setParentJointOfHand(InteractionHand.OFF_HAND, toolArmature.backToolJoint());
         entitypatch.<Animator>getAnimator().getVariables().put(TOOLS_IN_BACK, animation, true);
      }

      private static void moveToolBonesToHands(
         LivingEntityPatch<?> entitypatch, AssetAccessor<? extends StaticAnimation> animation, ToolHolderArmature toolArmature
      ) {
         entitypatch.setParentJointOfHand(InteractionHand.MAIN_HAND, toolArmature.rightToolJoint());
         entitypatch.setParentJointOfHand(InteractionHand.OFF_HAND, toolArmature.leftToolJoint());
         entitypatch.<Animator>getAnimator().getVariables().remove(TOOLS_IN_BACK, animation);
      }
   }
}
