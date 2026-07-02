package yesman.epicfight.events;

import com.google.common.collect.Multimap;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.EntityEvent.Size;
import net.minecraftforge.event.entity.EntityTeleportEvent.EnderEntity;
import net.minecraftforge.event.entity.ProjectileImpactEvent.ImpactResult;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent.Added;
import net.minecraftforge.event.entity.living.MobEffectEvent.Expired;
import net.minecraftforge.event.entity.living.MobEffectEvent.Remove;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.forgeevent.EntityStunEvent;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.mixin.common.MixinProjectile;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPPotion;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.entitypatch.HurtableEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.EndermanPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.ArmorCapability;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.projectile.ProjectilePatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.effect.EpicFightMobEffects;
import yesman.epicfight.world.entity.EpicFightEntities;
import yesman.epicfight.world.entity.eventlistener.DealDamageEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.PlayerKilledEvent;
import yesman.epicfight.world.entity.eventlistener.ProjectileHitEvent;
import yesman.epicfight.world.entity.eventlistener.TakeDamageEvent;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

@EventBusSubscriber(modid = "epicfight")
public class EntityEvents {
   @SubscribeEvent
   public static void spawnEvent(EntityJoinLevelEvent event) {
      EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), EntityPatch.class).ifPresent(entitypatch -> {
         if (!entitypatch.isInitialized()) {
            entitypatch.onJoinWorld(event.getEntity(), event);
         }
      });
   }

   @SubscribeEvent
   public static void updateEvent(LivingTickEvent event) {
      EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), HurtableEntityPatch.class).ifPresent(entitypatch -> {
         if (entitypatch.getOriginal() != null) {
            entitypatch.tick(event);
         }
      });
   }

   @SubscribeEvent
   public static void deathEvent(LivingDeathEvent event) {
      EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), LivingEntityPatch.class).ifPresent(entitypatch -> entitypatch.onDeath(event));
      EpicFightCapabilities.getUnparameterizedEntityPatch(event.getSource().m_7639_(), ServerPlayerPatch.class)
         .ifPresent(
            playerpatch -> playerpatch.getEventListener()
               .triggerEvents(PlayerEventListener.EventType.PLAYER_KILLED_EVENT, new PlayerKilledEvent(playerpatch, event.getEntity(), event.getSource()))
         );
   }

   @SubscribeEvent
   public static void knockBackEvent(LivingKnockBackEvent event) {
      EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), HurtableEntityPatch.class).ifPresent(entitypatch -> {
         if (entitypatch.shouldCancelKnockback()) {
            event.setCanceled(true);
         }
      });
   }

   @SubscribeEvent
   public static void hurtEvent(LivingHurtEvent event) {
      EpicFightDamageSource epicfightDamageSource = event.getSource() instanceof EpicFightDamageSource ? (EpicFightDamageSource)event.getSource() : null;
      ValueModifier.ResultCalculator damageCalculator = ValueModifier.calculator();
      Entity causingEntity = event.getSource().m_7639_();
      LivingEntity hitEntity = event.getEntity();
      EpicFightCapabilities.getUnparameterizedEntityPatch(hitEntity, ServerPlayerPatch.class).ifPresent(serverplayerpatch -> {
         TakeDamageEvent.Hurt hurtEvent = new TakeDamageEvent.Hurt(serverplayerpatch, event.getSource(), damageCalculator, event.getAmount());
         serverplayerpatch.getEventListener().triggerEvents(PlayerEventListener.EventType.TAKE_DAMAGE_EVENT_HURT, hurtEvent);
      });
      if (causingEntity != null) {
         LivingEntityPatch<?> attackerentitypatch = EpicFightCapabilities.getEntityPatch(causingEntity, LivingEntityPatch.class);
         if (attackerentitypatch != null) {
            event.setAmount(attackerentitypatch.getModifiedBaseDamage(event.getAmount()));
         }

         if (epicfightDamageSource != null) {
            if (attackerentitypatch instanceof ServerPlayerPatch playerpatch) {
               DealDamageEvent.Hurt dealDamageHurt = new DealDamageEvent.Hurt(playerpatch, hitEntity, epicfightDamageSource, event);
               playerpatch.getEventListener().triggerEvents(PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_HURT, dealDamageHurt);
            }

            if (epicfightDamageSource.m_269533_(EpicFightDamageTypeTags.EXECUTION)) {
               EpicFightCapabilities.getUnparameterizedEntityPatch(hitEntity, LivingEntityPatch.class).ifPresentOrElse(entitypatchx -> {
                  int executionResistance = entitypatchx.getExecutionResistance();
                  if (executionResistance > 0) {
                     entitypatchx.setExecutionResistance(executionResistance - 1);
                  } else {
                     event.setAmount(2.1474836E9F);
                  }
               }, () -> event.setAmount(2.1474836E9F));
            }
         }
      }

      if (Float.compare(2.1474836E9F, event.getAmount()) != 0) {
         if (epicfightDamageSource != null) {
            epicfightDamageSource.attachDamageModifier(damageCalculator);
            float result = epicfightDamageSource.calculateDamageAgainst(causingEntity, hitEntity, event.getAmount());
            event.setAmount(result);
         } else {
            float result = damageCalculator.getResult(event.getAmount());
            event.setAmount(result);
         }
      }

      if (Float.compare(event.getAmount(), 0.0F) == 1 && epicfightDamageSource != null && !epicfightDamageSource.m_269533_(EpicFightDamageTypeTags.NO_STUN)) {
         EpicFightCapabilities.getUnparameterizedEntityPatch(hitEntity, HurtableEntityPatch.class)
            .ifPresent(
               hitentitypatch -> {
                  StunType stunType = epicfightDamageSource.getStunType();
                  float stunTime = 0.0F;
                  float knockBackAmount = 0.0F;
                  float stunShield = hitentitypatch.getStunShield();
                  float impact = epicfightDamageSource.calculateImpact();
                  if (stunShield > impact && (stunType == StunType.SHORT || stunType == StunType.LONG)) {
                     stunType = StunType.NONE;
                  }

                  EntityStunEvent entityStunEvent = new EntityStunEvent(epicfightDamageSource, (HurtableEntityPatch<?>)hitentitypatch, stunType);
                  if (!MinecraftForge.EVENT_BUS.post(entityStunEvent)) {
                     hitentitypatch.damageStunShield(event.getAmount(), impact);
                     switch (stunType) {
                        case SHORT:
                           stunType = StunType.NONE;
                           if (!hitEntity.m_21023_((MobEffect)EpicFightMobEffects.STUN_IMMUNITY.get()) && hitentitypatch.getStunShield() == 0.0F) {
                              float totalStunTime = (0.25F + impact * 0.1F) * (1.0F - hitentitypatch.getStunReduction());
                              if (totalStunTime >= 0.075F) {
                                 stunTime = totalStunTime - 0.1F;
                                 boolean isLongStun = totalStunTime >= 0.83F;
                                 stunTime = isLongStun ? 0.83F : stunTime;
                                 stunType = isLongStun ? StunType.LONG : StunType.SHORT;
                                 knockBackAmount = Math.min(isLongStun ? impact * 0.05F : totalStunTime, 2.0F);
                              }

                              stunTime = (float)(stunTime * (1.0 - hitEntity.m_21133_(Attributes.f_22278_)));
                           }
                           break;
                        case LONG:
                           stunType = hitEntity.m_21023_((MobEffect)EpicFightMobEffects.STUN_IMMUNITY.get()) ? StunType.NONE : StunType.LONG;
                           knockBackAmount = Math.min(impact * 0.05F, 5.0F);
                           stunTime = 0.83F;
                           break;
                        case HOLD:
                           stunType = StunType.SHORT;
                           stunTime = impact * 0.25F;
                           break;
                        case KNOCKDOWN:
                           stunType = hitEntity.m_21023_((MobEffect)EpicFightMobEffects.STUN_IMMUNITY.get()) ? StunType.NONE : StunType.KNOCKDOWN;
                           knockBackAmount = Math.min(impact * 0.05F, 5.0F);
                           stunTime = 2.0F;
                           break;
                        case NEUTRALIZE:
                           stunType = StunType.NEUTRALIZE;
                           hitentitypatch.playSound((SoundEvent)EpicFightSounds.NEUTRALIZE_MOBS.get(), 3.0F, 0.0F, 0.1F);
                           ((HitParticleType)EpicFightParticles.AIR_BURST.get())
                              .spawnParticleWithArgument((ServerLevel)hitEntity.m_9236_(), hitEntity, event.getSource().m_7640_());
                           knockBackAmount = 0.0F;
                           stunTime = 2.0F;
                     }

                     Vec3 sourcePosition = epicfightDamageSource.getInitialPosition();
                     hitentitypatch.setStunReductionOnHit(stunType);
                     boolean stunApplied = hitentitypatch.applyStun(stunType, stunTime);
                     if (sourcePosition != null) {
                        if (!(hitEntity instanceof Player) && stunApplied) {
                           hitEntity.m_7618_(Anchor.FEET, sourcePosition);
                        }

                        if (knockBackAmount > 0.0F) {
                           knockBackAmount *= 40.0F / hitentitypatch.getWeight();
                           hitentitypatch.knockBackEntity(sourcePosition, knockBackAmount);
                        }
                     }
                  }
               }
            );
      }

      if (event.getSource().m_276093_(DamageTypes.f_268671_)
         && event.getAmount() > 1.0F
         && EpicFightGameRules.HAS_FALL_ANIMATION.getRuleValue(event.getEntity().m_9236_())) {
         LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), LivingEntityPatch.class);
         if (entitypatch != null && !entitypatch.getEntityState().inaction()) {
            AssetAccessor<? extends StaticAnimation> fallAnimation = entitypatch.<Animator>getAnimator()
               .getLivingAnimation(LivingMotions.LANDING_RECOVERY, entitypatch.getHitAnimation(StunType.FALL));
            if (fallAnimation != null) {
               entitypatch.playAnimationSynchronized(fallAnimation, 0.0F);
            }
         }
      }
   }

   @SubscribeEvent
   public static void damageEvent(LivingDamageEvent event) {
      EpicFightCapabilities.getUnparameterizedEntityPatch(event.getSource().m_7639_(), ServerPlayerPatch.class)
         .ifPresent(
            playerpatch -> {
               if (event.getSource() instanceof EpicFightDamageSource epicFightDamageSource) {
                  playerpatch.getEventListener()
                     .triggerEvents(
                        PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_DAMAGE,
                        new DealDamageEvent.Damage(playerpatch, event.getEntity(), epicFightDamageSource, event)
                     );
               }
            }
         );
      EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), ServerPlayerPatch.class)
         .ifPresent(
            playerpatch -> playerpatch.getEventListener()
               .triggerEvents(
                  PlayerEventListener.EventType.TAKE_DAMAGE_EVENT_DAMAGE, new TakeDamageEvent.Damage(playerpatch, event.getSource(), event.getAmount())
               )
         );
   }

   @SubscribeEvent
   public static void attackEvent(LivingAttackEvent event) {
      if (!event.getEntity().m_9236_().m_5776_()) {
         if (!event.getEntity().m_6673_(event.getSource())) {
            if (event.getEntity().f_19802_ <= 10 || !(event.getAmount() <= event.getEntity().f_20898_)) {
               if (!(event.getEntity().m_21223_() <= 0.0F)) {
                  if (event.getSource() instanceof EpicFightDamageSource epicfightDamagesource
                     && event.getSource().m_7639_() instanceof ServerPlayer serverplayer) {
                     ServerPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(serverplayer, ServerPlayerPatch.class);
                     DealDamageEvent.Attack dealDamageAttack = new DealDamageEvent.Attack(playerpatch, event.getEntity(), epicfightDamagesource, event);
                     playerpatch.getEventListener().triggerEvents(PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_ATTACK, dealDamageAttack);
                     if (dealDamageAttack.isCanceled()) {
                        event.setCanceled(true);
                        return;
                     }
                  }

                  LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), LivingEntityPatch.class);
                  AttackResult result = entitypatch != null
                     ? entitypatch.tryHurt(event.getSource(), event.getAmount())
                     : AttackResult.success(event.getAmount());
                  EpicFightCapabilities.getUnparameterizedEntityPatch(event.getSource().m_7639_(), LivingEntityPatch.class)
                     .ifPresent(attackerentitypatch -> attackerentitypatch.setLastAttackResult(result));
                  if (!result.resultType.dealtDamage()) {
                     event.setCanceled(true);
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void shieldEvent(ShieldBlockEvent event) {
      EpicFightCapabilities.getParameterizedEntityPatch(event.getEntity(), LivingEntity.class, LivingEntityPatch.class)
         .ifPresent(entitypatch -> entitypatch.playAnimationSynchronized(Animations.BIPED_HIT_SHIELD, 0.0F));
   }

   @SubscribeEvent
   public static void dropEvent(LivingDropsEvent event) {
      EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), LivingEntityPatch.class).ifPresent(entitypatch -> {
         if (entitypatch.onDrop(event)) {
            event.setCanceled(true);
         }
      });
   }

   @SubscribeEvent
   public static void projectileImpactEvent(ProjectileImpactEvent event) {
      ProjectilePatch<?> projectilepatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), ProjectilePatch.class);
      if (projectilepatch != null && projectilepatch.onProjectileImpact(event)) {
         event.setImpactResult(ImpactResult.SKIP_ENTITY);
      }

      if (event.getImpactResult() != ImpactResult.SKIP_ENTITY
         && event.getRayTraceResult() instanceof EntityHitResult entityHitResult
         && entityHitResult.m_82443_() != null) {
         EpicFightCapabilities.getUnparameterizedEntityPatch(entityHitResult.m_82443_(), PlayerPatch.class)
            .ifPresent(
               playerpatch -> {
                  playerpatch.getEntityState().setProjectileImpactResult(event);
                  if (event.getImpactResult() == ImpactResult.DEFAULT && playerpatch instanceof ServerPlayerPatch serverplayerpatch) {
                     boolean canceled = playerpatch.getEventListener()
                        .triggerEvents(PlayerEventListener.EventType.PROJECTILE_HIT_EVENT, new ProjectileHitEvent(serverplayerpatch, event));
                     if (canceled) {
                        event.setImpactResult(ImpactResult.SKIP_ENTITY);
                     }
                  }
               }
            );
         if (event.getProjectile().m_19749_() != null) {
            if (entityHitResult.m_82443_().equals(event.getProjectile().m_19749_().m_20202_())) {
               event.setImpactResult(ImpactResult.SKIP_ENTITY);
            }

            if (entityHitResult.m_82443_() instanceof PartEntity<?> partEntity) {
               Entity parent = partEntity.getParent();
               if (event.getProjectile().m_19749_().m_7306_(parent)) {
                  event.setImpactResult(ImpactResult.SKIP_ENTITY);
               }
            }
         }

         if (((EntityType)EpicFightEntities.DODGE_LOCATION_INDICATOR.get()).equals(entityHitResult.m_82443_().m_6095_())) {
            if (event.getEntity() instanceof Projectile projectile) {
               ((MixinProjectile)projectile).invoke_onHitEntity(entityHitResult);
            }

            event.setImpactResult(ImpactResult.SKIP_ENTITY);
         }
      }

      if (projectilepatch != null && event.getImpactResult() == ImpactResult.DEFAULT) {
         projectilepatch.setHit(true);
      }
   }

   @SubscribeEvent
   public static void itemAttributeModifierEvent(ItemAttributeModifierEvent event) {
      CapabilityItem itemCap = EpicFightCapabilities.getItemStackCapability(event.getItemStack());
      if (!itemCap.isEmpty()) {
         Multimap<Attribute, AttributeModifier> multimap = itemCap.getAttributeModifiers(event.getSlotType(), null);

         for (Attribute key : multimap.keys()) {
            for (AttributeModifier modifier : multimap.get(key)) {
               event.addModifier(key, modifier);
            }
         }
      }
   }

   @SubscribeEvent
   public static void equipChangeEvent(LivingEquipmentChangeEvent event) {
      EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), HurtableEntityPatch.class)
         .ifPresent(hurtableEntitypatch -> hurtableEntitypatch.setDefaultStunReduction(event.getSlot(), event.getFrom(), event.getTo()));
      LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), LivingEntityPatch.class);
      CapabilityItem fromCap = EpicFightCapabilities.getItemStackCapability(event.getFrom());
      CapabilityItem toCap = EpicFightCapabilities.getItemStackCapability(event.getTo());
      if (event.getSlot() != EquipmentSlot.OFFHAND) {
         if (fromCap != null) {
            event.getEntity().m_21204_().m_22161_(fromCap.getAttributeModifiers(event.getSlot(), entitypatch));
         }

         if (toCap != null) {
            event.getEntity().m_21204_().m_22178_(toCap.getAttributeModifiers(event.getSlot(), entitypatch));
         }
      }

      if (entitypatch != null && entitypatch.getOriginal() != null) {
         if (event.getSlot().m_20743_() == Type.HAND) {
            InteractionHand hand = event.getSlot() == EquipmentSlot.MAINHAND ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            entitypatch.updateHeldItem(fromCap, toCap, event.getFrom(), event.getTo(), hand);
         } else if (event.getSlot().m_20743_() == Type.ARMOR) {
            boolean isFromItemArmor = fromCap instanceof ArmorCapability;
            boolean isToItemArmor = toCap instanceof ArmorCapability;
            if (isFromItemArmor || isToItemArmor) {
               entitypatch.updateArmor(isFromItemArmor ? (ArmorCapability)fromCap : null, isToItemArmor ? (ArmorCapability)toCap : null, event.getSlot());
            }
         }
      }
   }

   @SubscribeEvent
   public static void sizingEvent(Size event) {
      if (event.getEntity() instanceof EnderDragon) {
         event.setNewSize(EntityDimensions.m_20395_(5.0F, 3.0F));
      }
   }

   @SubscribeEvent
   public static void effectAddEvent(Added event) {
      if (!event.getEntity().m_9236_().m_5776_()) {
         EpicFightNetworkManager.sendToAll(new SPPotion(event.getEffectInstance(), SPPotion.Action.ACTIVATE, event.getEntity().m_19879_()));
      }
   }

   @SubscribeEvent
   public static void effectRemoveEvent(Remove event) {
      if (!event.getEntity().m_9236_().m_5776_() && event.getEffectInstance() != null) {
         EpicFightNetworkManager.sendToAll(new SPPotion(event.getEffectInstance(), SPPotion.Action.REMOVE, event.getEntity().m_19879_()));
      }
   }

   @SubscribeEvent
   public static void effectExpiryEvent(Expired event) {
      if (!event.getEntity().m_9236_().m_5776_()) {
         EpicFightNetworkManager.sendToAll(new SPPotion(event.getEffectInstance(), SPPotion.Action.REMOVE, event.getEntity().m_19879_()));
      }
   }

   @SubscribeEvent
   public static void mountEvent(EntityMountEvent event) {
      EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntityMounting(), HumanoidMobPatch.class).ifPresent(humanoidMobPatch -> {
         if (!event.getLevel().m_5776_() && humanoidMobPatch.getOriginal() != null && event.getEntityBeingMounted() instanceof Mob) {
            humanoidMobPatch.onMount(event.isMounting(), event.getEntityBeingMounted());
         }
      });
   }

   @SubscribeEvent
   public static void tpEvent(EnderEntity event) {
      EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), EndermanPatch.class)
         .ifPresent(
            enderManPatch -> {
               if (enderManPatch.getEntityState().inaction()) {
                  for (Entity collideEntity : enderManPatch.getOriginal()
                     .m_9236_()
                     .m_45976_(Entity.class, enderManPatch.getOriginal().m_20191_().m_82377_(0.2, 0.2, 0.2))) {
                     if (collideEntity instanceof Projectile) {
                        return;
                     }
                  }

                  event.setCanceled(true);
               } else if (enderManPatch.isRaging()) {
                  event.setCanceled(true);
               }
            }
         );
   }

   @SubscribeEvent
   public static void jumpEvent(LivingJumpEvent event) {
      EpicFightCapabilities.getParameterizedEntityPatch(event.getEntity(), LivingEntity.class, LivingEntityPatch.class).ifPresent(entitypatch -> {
         if (entitypatch.isLogicalClient() && !entitypatch.getEntityState().inaction() && !event.getEntity().m_20069_()) {
            AssetAccessor<? extends StaticAnimation> jumpAnimation = entitypatch.getClientAnimator().getJumpAnimation();
            entitypatch.playAnimationInClientSide(jumpAnimation, 0.0F);
         }
      });
   }
}
