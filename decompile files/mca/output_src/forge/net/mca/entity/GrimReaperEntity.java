package forge.net.mca.entity;

import forge.net.mca.Config;
import forge.net.mca.SoundsMCA;
import forge.net.mca.entity.ai.goal.GrimReaperIdleGoal;
import forge.net.mca.entity.ai.goal.GrimReaperMeleeGoal;
import forge.net.mca.entity.ai.goal.GrimReaperRestGoal;
import forge.net.mca.entity.ai.goal.GrimReaperTargetGoal;
import forge.net.mca.item.ItemsMCA;
import forge.net.mca.util.network.datasync.CDataManager;
import forge.net.mca.util.network.datasync.CEnumParameter;
import forge.net.mca.util.network.datasync.CParameter;
import forge.net.mca.util.network.datasync.CTrackedEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class GrimReaperEntity extends PathfinderMob implements CTrackedEntity<GrimReaperEntity> {
   public static final CEnumParameter<ReaperAttackState> ATTACK_STAGE = CParameter.create("attackStage", ReaperAttackState.IDLE);
   public static final CDataManager<GrimReaperEntity> DATA = new CDataManager.Builder(GrimReaperEntity.class).addAll(ATTACK_STAGE).build();
   private final ServerBossEvent bossInfo = (ServerBossEvent)new ServerBossEvent(this.m_5446_(), BossBarColor.PURPLE, BossBarOverlay.PROGRESS).m_7003_(true);

   public GrimReaperEntity(EntityType<? extends GrimReaperEntity> type, Level world) {
      super(type, world);
      this.f_21364_ = 100;
      this.f_21342_ = new FlyingMoveControl(this, 10, false);
      this.getTypeDataManager().register(this);
   }

   @Override
   public CDataManager<GrimReaperEntity> getTypeDataManager() {
      return DATA;
   }

   public static Builder createAttributes() {
      return Monster.m_33035_()
         .m_22268_(Attributes.f_22281_, 10.0)
         .m_22268_(Attributes.f_22276_, 300.0)
         .m_22268_(Attributes.f_22279_, 0.3F)
         .m_22268_(Attributes.f_22280_, 0.3F)
         .m_22268_(Attributes.f_22277_, 40.0);
   }

   public boolean m_20068_() {
      return true;
   }

   public SoundSource m_5720_() {
      return SoundSource.HOSTILE;
   }

   public MobType m_6336_() {
      return MobType.f_21641_;
   }

   protected void m_8099_() {
      this.f_21346_.m_25352_(1, new GrimReaperTargetGoal(this));
      this.f_21345_.m_25352_(0, new LookAtPlayerGoal(this, Player.class, 24.0F, 1.0F));
      this.f_21345_.m_25352_(1, new GrimReaperRestGoal(this));
      this.f_21345_.m_25352_(2, new GrimReaperMeleeGoal(this));
      this.f_21345_.m_25352_(3, new GrimReaperIdleGoal(this, 1.0));
   }

   public MoveControl m_21566_() {
      return this.f_21342_;
   }

   public void m_6043_() {
      if (this.m_9236_().m_46791_() == Difficulty.PEACEFUL && this.m_8028_()) {
         this.m_146870_();
      }
   }

   protected boolean m_8028_() {
      return true;
   }

   protected PathNavigation m_6037_(Level world) {
      FlyingPathNavigation navigator = new FlyingPathNavigation(this, world) {
         public boolean m_6342_(BlockPos pos) {
            return true;
         }
      };
      navigator.m_26440_(false);
      navigator.m_7008_(false);
      navigator.m_26443_(true);
      return navigator;
   }

   protected void m_7472_(DamageSource source, int lootingLvl, boolean hitByPlayer) {
      super.m_7472_(source, lootingLvl, hitByPlayer);
      ItemEntity itemEntity = this.m_19998_((ItemLike)ItemsMCA.SCYTHE.get());
      if (itemEntity != null) {
         itemEntity.m_32064_();
      }
   }

   public ReaperAttackState getAttackState() {
      return this.getTrackedValue(ATTACK_STAGE);
   }

   public void setAttackState(ReaperAttackState state) {
      if (this.getAttackState() != state) {
         this.setTrackedValue(ATTACK_STAGE, state);
         switch (state) {
            case PRE:
               this.m_5496_((SoundEvent)SoundsMCA.REAPER_SCYTHE_OUT.get(), 1.0F, 1.0F);
               break;
            case POST:
               this.m_5496_((SoundEvent)SoundsMCA.REAPER_SCYTHE_SWING.get(), 1.0F, 1.0F);
         }
      }
   }

   public boolean m_6469_(DamageSource source, float damage) {
      if (!source.m_276093_(DamageTypes.f_268612_)
         && !source.m_276093_(DamageTypes.f_268468_)
         && !source.m_276093_(DamageTypes.f_268565_)
         && !source.m_276093_(DamageTypes.f_268631_)) {
         Entity entity = source.m_7640_();
         Entity attacker = source.m_7639_();
         if (this.getAttackState() == ReaperAttackState.BLOCK && attacker != null) {
            this.m_5496_((SoundEvent)SoundsMCA.REAPER_BLOCK.get(), 1.0F, 1.0F);
            return false;
         }

         if (entity instanceof Projectile && this.getAttackState() != ReaperAttackState.REST && attacker != null && this.f_19796_.m_188499_()) {
            double newX = attacker.m_20185_() + (this.f_19796_.m_188501_() >= 0.5F ? 4 : -4);
            double newZ = attacker.m_20189_() + (this.f_19796_.m_188501_() >= 0.5F ? 4 : -4);
            this.m_6021_(newX, attacker.m_20186_(), newZ);
            return false;
         }

         if (!this.m_9236_().f_46443_ && this.f_19796_.m_188501_() >= 0.3F && attacker != null) {
            double deltaX = this.m_20185_() - attacker.m_20185_();
            double deltaZ = this.m_20189_() - attacker.m_20189_();
            double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            double length = Math.max(5.0, distance) / distance * 0.95;
            this.m_6021_(attacker.m_20185_() - deltaX * length, attacker.m_20186_() + 1.5, attacker.m_20189_() - deltaZ * length);
         }

         if (this.getAttackState() == ReaperAttackState.REST) {
            damage *= 0.25F;
         }

         return super.m_6469_(source, damage);
      } else {
         if (source.m_276093_(DamageTypes.f_268612_)) {
            this.m_6021_(this.m_20185_(), this.m_20186_() + 3.0, this.m_20189_());
         }

         return false;
      }
   }

   protected SoundEvent m_7515_() {
      return (SoundEvent)SoundsMCA.REAPER_IDLE.get();
   }

   protected SoundEvent m_5592_() {
      return (SoundEvent)SoundsMCA.REAPER_DEATH.get();
   }

   protected SoundEvent m_7975_(DamageSource source) {
      return SoundEvents.f_12557_;
   }

   public void m_8119_() {
      super.m_8119_();
      this.bossInfo.m_142711_(this.m_21223_() / this.m_21233_());
      if (!Config.getInstance().allowGrimReaper) {
         this.m_146870_();
      }

      if (this.m_21223_() <= 0.0F) {
         this.m_20256_(Vec3.f_82478_);
      } else {
         LivingEntity entityToAttack = this.m_5448_();
         if (entityToAttack != null && entityToAttack.m_21224_()) {
            this.m_6710_(null);
            this.setAttackState(ReaperAttackState.IDLE);
         }

         this.f_19789_ = 0.0F;
      }
   }

   public void m_6021_(double x, double y, double z) {
      if (this.m_9236_() instanceof ServerLevel) {
         this.m_5496_(SoundEvents.f_11852_, 1.0F, 1.0F);
         super.m_6021_(x, y, z);
         this.m_5496_(SoundEvents.f_11852_, 1.0F, 1.0F);
      }
   }

   public void m_6457_(ServerPlayer player) {
      super.m_6457_(player);
      this.bossInfo.m_6543_(player);
   }

   public void m_6452_(ServerPlayer player) {
      super.m_6452_(player);
      this.bossInfo.m_6539_(player);
   }
}
