package fabric.net.mca.entity;

import fabric.net.mca.Config;
import fabric.net.mca.SoundsMCA;
import fabric.net.mca.entity.ai.goal.GrimReaperIdleGoal;
import fabric.net.mca.entity.ai.goal.GrimReaperMeleeGoal;
import fabric.net.mca.entity.ai.goal.GrimReaperRestGoal;
import fabric.net.mca.entity.ai.goal.GrimReaperTargetGoal;
import fabric.net.mca.item.ItemsMCA;
import fabric.net.mca.util.network.datasync.CDataManager;
import fabric.net.mca.util.network.datasync.CEnumParameter;
import fabric.net.mca.util.network.datasync.CParameter;
import fabric.net.mca.util.network.datasync.CTrackedEntity;
import net.minecraft.class_1267;
import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1310;
import net.minecraft.class_1314;
import net.minecraft.class_1331;
import net.minecraft.class_1335;
import net.minecraft.class_1361;
import net.minecraft.class_1407;
import net.minecraft.class_1408;
import net.minecraft.class_1542;
import net.minecraft.class_1588;
import net.minecraft.class_1657;
import net.minecraft.class_1676;
import net.minecraft.class_1935;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_3213;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_5134;
import net.minecraft.class_8111;
import net.minecraft.class_1259.class_1260;
import net.minecraft.class_1259.class_1261;
import net.minecraft.class_5132.class_5133;

public class GrimReaperEntity extends class_1314 implements CTrackedEntity<GrimReaperEntity> {
   public static final CEnumParameter<ReaperAttackState> ATTACK_STAGE = CParameter.create("attackStage", ReaperAttackState.IDLE);
   public static final CDataManager<GrimReaperEntity> DATA = new CDataManager.Builder(GrimReaperEntity.class).addAll(ATTACK_STAGE).build();
   private final class_3213 bossInfo = (class_3213)new class_3213(this.method_5476(), class_1260.field_5783, class_1261.field_5795).method_5406(true);

   public GrimReaperEntity(class_1299<? extends GrimReaperEntity> type, class_1937 world) {
      super(type, world);
      this.field_6194 = 100;
      this.field_6207 = new class_1331(this, 10, false);
      this.getTypeDataManager().register(this);
   }

   @Override
   public CDataManager<GrimReaperEntity> getTypeDataManager() {
      return DATA;
   }

   public static class_5133 createAttributes() {
      return class_1588.method_26918()
         .method_26868(class_5134.field_23721, 10.0)
         .method_26868(class_5134.field_23716, 300.0)
         .method_26868(class_5134.field_23719, 0.3F)
         .method_26868(class_5134.field_23720, 0.3F)
         .method_26868(class_5134.field_23717, 40.0);
   }

   public boolean method_5740() {
      return true;
   }

   public class_3419 method_5634() {
      return class_3419.field_15251;
   }

   public class_1310 method_6046() {
      return class_1310.field_6289;
   }

   protected void method_5959() {
      this.field_6185.method_6277(1, new GrimReaperTargetGoal(this));
      this.field_6201.method_6277(0, new class_1361(this, class_1657.class, 24.0F, 1.0F));
      this.field_6201.method_6277(1, new GrimReaperRestGoal(this));
      this.field_6201.method_6277(2, new GrimReaperMeleeGoal(this));
      this.field_6201.method_6277(3, new GrimReaperIdleGoal(this, 1.0));
   }

   public class_1335 method_5962() {
      return this.field_6207;
   }

   public void method_5982() {
      if (this.method_37908().method_8407() == class_1267.field_5801 && this.method_23734()) {
         this.method_31472();
      }
   }

   protected boolean method_23734() {
      return true;
   }

   protected class_1408 method_5965(class_1937 world) {
      class_1407 navigator = new class_1407(this, world) {
         public boolean method_6333(class_2338 pos) {
            return true;
         }
      };
      navigator.method_6332(false);
      navigator.method_6354(false);
      navigator.method_6331(true);
      return navigator;
   }

   protected void method_6099(class_1282 source, int lootingLvl, boolean hitByPlayer) {
      super.method_6099(source, lootingLvl, hitByPlayer);
      class_1542 itemEntity = this.method_5706((class_1935)ItemsMCA.SCYTHE.get());
      if (itemEntity != null) {
         itemEntity.method_6976();
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
               this.method_5783((class_3414)SoundsMCA.REAPER_SCYTHE_OUT.get(), 1.0F, 1.0F);
               break;
            case POST:
               this.method_5783((class_3414)SoundsMCA.REAPER_SCYTHE_SWING.get(), 1.0F, 1.0F);
         }
      }
   }

   public boolean method_5643(class_1282 source, float damage) {
      if (!source.method_49708(class_8111.field_42340)
         && !source.method_49708(class_8111.field_42337)
         && !source.method_49708(class_8111.field_42331)
         && !source.method_49708(class_8111.field_42335)) {
         class_1297 entity = source.method_5526();
         class_1297 attacker = source.method_5529();
         if (this.getAttackState() == ReaperAttackState.BLOCK && attacker != null) {
            this.method_5783((class_3414)SoundsMCA.REAPER_BLOCK.get(), 1.0F, 1.0F);
            return false;
         }

         if (entity instanceof class_1676 && this.getAttackState() != ReaperAttackState.REST && attacker != null && this.field_5974.method_43056()) {
            double newX = attacker.method_23317() + (this.field_5974.method_43057() >= 0.5F ? 4 : -4);
            double newZ = attacker.method_23321() + (this.field_5974.method_43057() >= 0.5F ? 4 : -4);
            this.method_5859(newX, attacker.method_23318(), newZ);
            return false;
         }

         if (!this.method_37908().field_9236 && this.field_5974.method_43057() >= 0.3F && attacker != null) {
            double deltaX = this.method_23317() - attacker.method_23317();
            double deltaZ = this.method_23321() - attacker.method_23321();
            double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            double length = Math.max(5.0, distance) / distance * 0.95;
            this.method_5859(attacker.method_23317() - deltaX * length, attacker.method_23318() + 1.5, attacker.method_23321() - deltaZ * length);
         }

         if (this.getAttackState() == ReaperAttackState.REST) {
            damage *= 0.25F;
         }

         return super.method_5643(source, damage);
      } else {
         if (source.method_49708(class_8111.field_42340)) {
            this.method_5859(this.method_23317(), this.method_23318() + 3.0, this.method_23321());
         }

         return false;
      }
   }

   protected class_3414 method_5994() {
      return (class_3414)SoundsMCA.REAPER_IDLE.get();
   }

   protected class_3414 method_6002() {
      return (class_3414)SoundsMCA.REAPER_DEATH.get();
   }

   protected class_3414 method_6011(class_1282 source) {
      return class_3417.field_14688;
   }

   public void method_5773() {
      super.method_5773();
      this.bossInfo.method_5408(this.method_6032() / this.method_6063());
      if (!Config.getInstance().allowGrimReaper) {
         this.method_31472();
      }

      if (this.method_6032() <= 0.0F) {
         this.method_18799(class_243.field_1353);
      } else {
         class_1309 entityToAttack = this.method_5968();
         if (entityToAttack != null && entityToAttack.method_29504()) {
            this.method_5980(null);
            this.setAttackState(ReaperAttackState.IDLE);
         }

         this.field_6017 = 0.0F;
      }
   }

   public void method_5859(double x, double y, double z) {
      if (this.method_37908() instanceof class_3218) {
         this.method_5783(class_3417.field_14879, 1.0F, 1.0F);
         super.method_5859(x, y, z);
         this.method_5783(class_3417.field_14879, 1.0F, 1.0F);
      }
   }

   public void method_5837(class_3222 player) {
      super.method_5837(player);
      this.bossInfo.method_14088(player);
   }

   public void method_5742(class_3222 player) {
      super.method_5742(player);
      this.bossInfo.method_14089(player);
   }
}
