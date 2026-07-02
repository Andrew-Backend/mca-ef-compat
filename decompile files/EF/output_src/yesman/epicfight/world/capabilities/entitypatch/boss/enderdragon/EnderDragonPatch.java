package yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.storage.loot.LootContext.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.physics.PhysicsSimulator;
import yesman.epicfight.api.physics.SimulationTypes;
import yesman.epicfight.api.physics.ik.InverseKinematicsProvider;
import yesman.epicfight.api.physics.ik.InverseKinematicsSimulatable;
import yesman.epicfight.api.physics.ik.InverseKinematicsSimulator;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.data.loot.function.SetSkillFunction;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.capabilities.entitypatch.boss.BossPatch;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.item.EpicFightItems;

public class EnderDragonPatch extends MobPatch<EnderDragon> implements InverseKinematicsSimulatable, BossPatch<EnderDragon> {
   public static final TargetingConditions DRAGON_TARGETING = TargetingConditions.m_148352_().m_148355_();
   private final Map<LivingMotions, AnimationManager.AnimationAccessor<? extends StaticAnimation>> livingMotions = Maps.newHashMap();
   private final Object2IntMap<Player> contributors = new Object2IntOpenHashMap();
   private boolean groundPhase;
   public LivingMotion prevMotion = LivingMotions.FLY;
   private float xRoot;
   private float xRootO;
   private float zRoot;
   private float zRootO;
   private final InverseKinematicsSimulator ikSimulator = new InverseKinematicsSimulator();

   public void onConstructed(EnderDragon enderdragon) {
      this.livingMotions.put(LivingMotions.IDLE, Animations.DRAGON_IDLE);
      this.livingMotions.put(LivingMotions.WALK, Animations.DRAGON_WALK);
      this.livingMotions.put(LivingMotions.FLY, Animations.DRAGON_FLY);
      this.livingMotions.put(LivingMotions.CHASE, Animations.DRAGON_AIRSTRIKE);
      this.livingMotions.put(LivingMotions.DEATH, Animations.DRAGON_DEATH);
      super.onConstructed(enderdragon);
      this.currentLivingMotion = LivingMotions.FLY;
   }

   @Override
   public void onStartTracking(ServerPlayer trackingPlayer) {
      if (this.getBossEvent() != null) {
         this.recordBossEventOwner(trackingPlayer);
      }
   }

   @Override
   public void onStopTracking(ServerPlayer trackingPlayer) {
      if (this.getBossEvent() != null) {
         this.removeBossEventOwner(trackingPlayer);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void entityPairing(SPEntityPairingPacket packet) {
      super.entityPairing(packet);
      if (packet.getPairingPacketType() == EntityPairingPacketTypes.SET_BOSS_EVENT_OWNER) {
         this.processOwnerRecordPacket(packet.getBuffer());
      }
   }

   public void onJoinWorld(EnderDragon enderdragon, EntityJoinLevelEvent event) {
      super.onJoinWorld(enderdragon, event);
      DragonPhaseInstance currentPhase = this.original.f_31074_.m_31415_();
      EnderDragonPhase<?> startPhase = currentPhase != null && currentPhase instanceof PatchedDragonPhase
         ? this.original.f_31074_.m_31415_().m_7309_()
         : PatchedPhases.FLYING;
      this.original.f_31074_ = new PhaseManagerPatch(this.original, this);
      this.original.f_31074_.m_31416_(startPhase);
      enderdragon.m_274367_(1.0F);
   }

   public static void initAttributes(EntityAttributeModificationEvent event) {
      event.add(EntityType.f_20565_, (Attribute)EpicFightAttributes.IMPACT.get(), 8.0);
      event.add(EntityType.f_20565_, (Attribute)EpicFightAttributes.MAX_STRIKES.get(), Double.MAX_VALUE);
      event.add(EntityType.f_20565_, Attributes.f_22281_, 10.0);
   }

   @Override
   public void saveData(CompoundTag compoundTag) {
   }

   @Override
   public void initAnimator(Animator animator) {
      super.initAnimator(animator);

      for (Entry<LivingMotions, AnimationManager.AnimationAccessor<? extends StaticAnimation>> livingmotionEntry : this.livingMotions.entrySet()) {
         animator.addLivingAnimation(livingmotionEntry.getKey(), livingmotionEntry.getValue());
      }
   }

   @Override
   public void updateMotion(boolean considerInaction) {
      if (this.original.m_21223_() <= 0.0F) {
         this.currentLivingMotion = LivingMotions.DEATH;
      } else if (this.state.inaction() && considerInaction) {
         this.currentLivingMotion = LivingMotions.INACTION;
      } else {
         DragonPhaseInstance phase = this.original.m_31157_().m_31415_();
         if (!this.groundPhase) {
            if (phase.m_7309_() == PatchedPhases.AIRSTRIKE && ((DragonAirstrikePhase)phase).isActuallyAttacking()) {
               this.currentLivingMotion = LivingMotions.CHASE;
            } else {
               this.currentLivingMotion = LivingMotions.FLY;
            }
         } else if (phase.m_7309_() == PatchedPhases.GROUND_BATTLE) {
            if (this.original.m_5448_() != null) {
               this.currentLivingMotion = LivingMotions.WALK;
            } else {
               this.currentLivingMotion = LivingMotions.IDLE;
            }
         } else {
            this.currentLivingMotion = LivingMotions.IDLE;
         }
      }
   }

   @Override
   public void tick(LivingTickEvent event) {
      super.tick(event);
      if (this.original.m_31157_().m_31415_().m_7080_()) {
         this.original.f_31086_ = null;
      }
   }

   @Override
   public void poseTick(DynamicAnimation animation, Pose pose, float elapsedTime, float partialTicks) {
      if (animation instanceof InverseKinematicsProvider inverseKinematicsProvider) {
         if (animation.getProperty(AnimationProperty.StaticAnimationProperty.BAKED_IK_DEFINITION).isEmpty()) {
            return;
         }

         float x = (float)this.getOriginal().m_20185_();
         float y = (float)this.getOriginal().m_20186_();
         float z = (float)this.getOriginal().m_20189_();
         float xo = (float)this.getOriginal().f_19854_;
         float yo = (float)this.getOriginal().f_19855_;
         float zo = (float)this.getOriginal().f_19856_;
         OpenMatrix4f toModelPos = OpenMatrix4f.mul(
               OpenMatrix4f.translate(
                  new Vec3f(xo + (x - xo) * partialTicks, yo + (y - yo) * partialTicks, zo + (z - zo) * partialTicks), new OpenMatrix4f(), null
               ),
               this.getModelMatrix(partialTicks),
               null
            )
            .invert();
         if (pose.hasTransform("Root")) {
            inverseKinematicsProvider.correctRootRotation(pose.get("Root"), this, partialTicks);
         }

         animation.getProperty(AnimationProperty.StaticAnimationProperty.BAKED_IK_DEFINITION)
            .ifPresent(
               ikDefinitions -> {
                  for (InverseKinematicsSimulator.BakedInverseKinematicsDefinition bakedIKInfo : ikDefinitions) {
                     if (this.ikSimulator.isRunning(bakedIKInfo.endJoint())) {
                        for (String jointName : bakedIKInfo.pathToEndJoint()) {
                           pose.putJointData(
                              jointName, animation.getTransfroms().get(jointName).getKeyframes()[bakedIKInfo.initialPoseFrame()].transform().copy()
                           );
                        }

                        InverseKinematicsSimulator.InverseKinematicsObject ikObject = this.ikSimulator.getRunningObject(bakedIKInfo.endJoint()).get();
                        JointTransform jt = ikObject.getTipTransform(partialTicks);
                        Vec3f jointModelpos = OpenMatrix4f.transform3v(toModelPos, jt.translation(), null);
                        inverseKinematicsProvider.applyFabrikToJoint(
                           jointModelpos.multiply(-1.0F, 1.0F, -1.0F),
                           pose,
                           this.getArmature(),
                           bakedIKInfo.startJoint(),
                           bakedIKInfo.endJoint(),
                           jt.rotation()
                        );
                     }
                  }
               }
            );
      }
   }

   @Override
   public void serverTick(LivingTickEvent event) {
      super.serverTick(event);
      this.original.f_20916_ = 2;
      this.original.m_21574_().m_26789_();
      this.updateMotion(true);
      if (this.prevMotion != this.currentLivingMotion && !this.animator.getEntityState().inaction()) {
         if (this.livingMotions.containsKey(this.currentLivingMotion)) {
            this.animator.playAnimation(this.livingMotions.get(this.currentLivingMotion), 0.0F);
         }

         this.prevMotion = this.currentLivingMotion;
      }

      this.ikSimulator.tick(null);
      this.setIKHeightAndRootRotation();
      Entity bodyPart = this.original.getParts()[2];
      AABB bodyBoundingBox = bodyPart.m_20191_();
      List<Entity> list = this.original.m_9236_().m_6249_(this.original, bodyBoundingBox, EntitySelector.m_20421_(this.original));
      if (!list.isEmpty()) {
         for (int l = 0; l < list.size(); l++) {
            Entity entity = list.get(l);
            double d0 = entity.m_20185_() - this.original.m_20185_();
            double d1 = entity.m_20189_() - this.original.m_20189_();
            double d2 = Mth.m_14005_(d0, d1);
            if (d2 >= 0.01) {
               d2 = Math.sqrt(d2);
               d0 /= d2;
               d1 /= d2;
               double d3 = 1.0 / d2;
               if (d3 > 1.0) {
                  d3 = 1.0;
               }

               d0 = d0 * d3 * 0.2;
               d1 = d1 * d3 * 0.2;
               if (!entity.m_20160_()) {
                  entity.m_5997_(d0, 0.0, d1);
                  entity.f_19864_ = true;
               }
            }
         }
      }

      this.contributors.object2IntEntrySet().removeIf(entry -> this.original.f_19797_ - entry.getIntValue() > 600 || !((Player)entry.getKey()).m_6084_());
   }

   @Override
   public void clientTick(LivingTickEvent event) {
      this.xRootO = this.xRoot;
      this.zRootO = this.zRoot;
      super.clientTick(event);
      this.ikSimulator.tick(null);
      this.setIKHeightAndRootRotation();
   }

   @Override
   public void damageStunShield(float damage, float impact) {
      super.damageStunShield(damage, impact);
      if (this.getStunShield() <= 0.0F) {
         DragonPhaseInstance currentPhase = this.original.m_31157_().m_31415_();
         if (currentPhase.m_7309_() == PatchedPhases.CRYSTAL_LINK && ((DragonCrystalLinkPhase)currentPhase).getChargingCount() > 0) {
            this.original.m_5496_((SoundEvent)EpicFightSounds.NEUTRALIZE_BOSSES.get(), 5.0F, 1.0F);
            this.original.m_31157_().m_31416_(PatchedPhases.NEUTRALIZED);
         }
      }
   }

   @Override
   public AttackResult tryHurt(DamageSource damageSource, float amount) {
      boolean isConsumingCrystal = this.original.m_31157_().m_31415_().m_7309_() == PatchedPhases.CRYSTAL_LINK;
      if (!isConsumingCrystal && amount > 0.0F && damageSource.m_7639_() instanceof Player player) {
         this.contributors.put(player, this.original.f_19797_);
      }

      return super.tryHurt(damageSource, isConsumingCrystal ? 0.0F : amount);
   }

   @Override
   public void rotateTo(Entity target, float limit, boolean partialSync) {
      double d0 = target.m_20185_() - this.original.m_20185_();
      double d1 = target.m_20189_() - this.original.m_20189_();
      float degree = 180.0F - (float)Math.toDegrees(Mth.m_14136_(d0, d1));
      super.rotateTo(degree, limit, partialSync);
   }

   @Override
   public float getYRotDeltaTo(Entity target) {
      double d0 = target.m_20185_() - this.original.m_20185_();
      double d1 = target.m_20189_() - this.original.m_20189_();
      float degree = 180.0F - (float)Math.toDegrees(Mth.m_14136_(d0, d1));
      return Mth.m_14036_(Mth.m_14177_(degree - Mth.m_14177_(this.getOriginal().m_146908_())), -this.getYRotLimit(), this.getYRotLimit());
   }

   @Override
   public void onDeath(LivingDeathEvent event) {
      super.onDeath(event);
      ObjectIterator var2 = this.contributors.keySet().iterator();

      while (var2.hasNext()) {
         Player player = (Player)var2.next();
         ItemStack skillbook = new ItemStack((ItemLike)EpicFightItems.SKILLBOOK.get());
         ItemStack modified = (ItemStack)SetSkillFunction.builder(EpicFightSkills.DEMOLITION_LEAP.getRegistryName().toString())
            .m_7453_()
            .apply(
               skillbook,
               new Builder(
                     new net.minecraft.world.level.storage.loot.LootParams.Builder(((ServerPlayer)player).m_284548_())
                        .m_287286_(LootContextParams.f_81455_, this.original)
                        .m_287286_(LootContextParams.f_81460_, player.m_20182_())
                        .m_287235_(LootContextParamSets.f_81419_)
                  )
                  .m_287259_(null)
            );
         if (!modified.m_150930_(Items.f_41852_)) {
            player.m_36356_(modified);
         }
      }
   }

   public void setIKHeightAndRootRotation() {
      this.ikSimulator
         .getAllRunningObjects()
         .stream()
         .map(pair -> (InverseKinematicsSimulator.InverseKinematicsObject)pair.getRight())
         .filter(InverseKinematicsSimulator.InverseKinematicsObject::isOnWorking)
         .forEach(InverseKinematicsSimulator.InverseKinematicsObject::tick);
      if (this.ikSimulator.isRunning(Armatures.DRAGON.get().legFrontL3)
         && this.ikSimulator.isRunning(Armatures.DRAGON.get().legFrontR3)
         && this.ikSimulator.isRunning(Armatures.DRAGON.get().legBackL3)
         && this.ikSimulator.isRunning(Armatures.DRAGON.get().legBackR3)) {
         InverseKinematicsSimulator.InverseKinematicsObject frontL = this.ikSimulator.getRunningObject(Armatures.DRAGON.get().legFrontL3).get();
         InverseKinematicsSimulator.InverseKinematicsObject frontR = this.ikSimulator.getRunningObject(Armatures.DRAGON.get().legFrontR3).get();
         InverseKinematicsSimulator.InverseKinematicsObject backL = this.ikSimulator.getRunningObject(Armatures.DRAGON.get().legBackL3).get();
         InverseKinematicsSimulator.InverseKinematicsObject backR = this.ikSimulator.getRunningObject(Armatures.DRAGON.get().legBackR3).get();
         float entityPosY = (float)this.original.m_20182_().f_82480_;
         float yFrontL = frontL != null && frontL.isTouchingGround() ? frontL.getDestination().y : entityPosY;
         float yFrontR = frontR != null && frontR.isTouchingGround() ? frontR.getDestination().y : entityPosY;
         float yBackL = backL != null && backL.isTouchingGround() ? backL.getDestination().y : entityPosY;
         float yBackR = backR != null && backR.isTouchingGround() ? backR.getDestination().y : entityPosY;
         float xdiff = (yFrontL + yBackL) * 0.5F - (yFrontR + yBackR) * 0.5F;
         float zdiff = (yFrontL + yFrontR) * 0.5F - (yBackL + yBackR) * 0.5F;
         float xdistance = 4.0F;
         float zdistance = 5.7F;
         this.xRoot = this.xRoot + Mth.m_14036_((float)Math.toDegrees(Math.atan2(zdiff, zdistance)) - this.xRoot, -1.0F, 1.0F);
         this.zRoot = this.zRoot + Mth.m_14036_((float)Math.toDegrees(Math.atan2(xdiff, xdistance)) - this.zRoot, -1.0F, 1.0F);
         float averageY = (yFrontL + yFrontR + yBackL + yBackR) * 0.25F;
         if (!this.isLogicalClient()) {
            float dy = averageY - entityPosY;
            this.original.m_6478_(MoverType.SELF, new Vec3(0.0, dy, 0.0));
         }
      }
   }

   public int getNearbyCrystals() {
      return this.original.m_31158_() != null ? this.original.m_31158_().m_64098_() : 0;
   }

   public void setFlyingPhase() {
      this.groundPhase = false;
      this.original.f_19862_ = false;
      this.original.f_19863_ = false;
   }

   public void setGroundPhase() {
      this.groundPhase = true;
   }

   public boolean isGroundPhase() {
      return this.groundPhase;
   }

   @Override
   public boolean shouldMoveOnCurrentSide(ActionAnimation actionAnimation) {
      return true;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean isOutlineVisible(LocalPlayer player) {
      return false;
   }

   @Override
   public SoundEvent getSwingSound(InteractionHand hand) {
      return (SoundEvent)EpicFightSounds.WHOOSH_BIG.get();
   }

   public AnimationManager.AnimationAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) {
      return null;
   }

   @Override
   public OpenMatrix4f getModelMatrix(float partialTick) {
      return MathUtils.getModelMatrixIntegral(
         0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, this.original.f_19859_, this.original.m_146908_(), partialTick, -1.0F, 1.0F, -1.0F
      );
   }

   @Override
   public double getAngleTo(Entity entityIn) {
      Vec3 a = this.original.m_20154_().m_82490_(-1.0);
      Vec3 b = new Vec3(
            entityIn.m_20185_() - this.original.m_20185_(), entityIn.m_20186_() - this.original.m_20186_(), entityIn.m_20189_() - this.original.m_20189_()
         )
         .m_82541_();
      double cosTheta = a.f_82479_ * b.f_82479_ + a.f_82480_ * b.f_82480_ + a.f_82481_ * b.f_82481_;
      return Math.toDegrees(Math.acos(cosTheta));
   }

   @Override
   public double getAngleToHorizontal(Entity entityIn) {
      Vec3 a = this.original.m_20154_().m_82490_(-1.0);
      Vec3 b = new Vec3(entityIn.m_20185_() - this.original.m_20185_(), 0.0, entityIn.m_20189_() - this.original.m_20189_()).m_82541_();
      double cos = a.f_82479_ * b.f_82479_ + a.f_82480_ * b.f_82480_ + a.f_82481_ * b.f_82481_;
      return Math.toDegrees(Math.acos(cos));
   }

   @Override
   public <SIM extends PhysicsSimulator<?, ?, ?, ?, ?>> Optional<SIM> getSimulator(SimulationTypes<?, ?, ?, ?, ?, SIM> simulationType) {
      if (simulationType == SimulationTypes.INVERSE_KINEMATICS) {
         Optional.of(this.ikSimulator);
      }

      return Optional.empty();
   }

   @Override
   public InverseKinematicsSimulator getIKSimulator() {
      return this.ikSimulator;
   }

   @Override
   public Entity toEntity() {
      return this.getOriginal();
   }

   @Override
   public float getRootXRot() {
      return this.xRoot;
   }

   @Override
   public float getRootXRotO() {
      return this.xRootO;
   }

   @Override
   public float getRootZRot() {
      return this.zRoot;
   }

   @Override
   public float getRootZRotO() {
      return this.zRootO;
   }

   @Override
   public BossEvent getBossEvent() {
      if (this.original.m_31158_() == null && !this.original.m_9236_().f_46443_) {
         ServerLevel serverlevel = (ServerLevel)this.original.m_9236_();
         EndDragonFight enddragonfight = serverlevel.m_8586_();
         if (enddragonfight != null && this.original.m_20148_().equals(enddragonfight.m_288211_())) {
            this.original.m_287231_(enddragonfight);
         }
      }

      return this.original.m_31158_() == null ? null : this.original.m_31158_().f_64060_;
   }
}
