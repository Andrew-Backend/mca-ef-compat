package forge.net.mca.entity.ai.goal;

import forge.net.mca.entity.GrimReaperEntity;
import forge.net.mca.entity.ReaperAttackState;
import forge.net.mca.entity.ai.TaskUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class GrimReaperRestGoal extends Goal {
   private static final int COOLDOWN = 1000;
   private final GrimReaperEntity reaper;
   private int lastHeal = -1000;
   private int healingCount = 0;
   private static final int MAX_HEALING_COUNT = 5;
   private static final int MAX_HEALING_TIME = 400;
   private int healingTime;

   public GrimReaperRestGoal(GrimReaperEntity reaper) {
      this.reaper = reaper;
   }

   public boolean m_8036_() {
      return this.reaper.f_19797_ > this.lastHeal + 1000 && this.reaper.m_21223_() <= this.reaper.m_21233_() * (1.0F - (this.healingCount + 1.0F) / 5.0F);
   }

   public boolean m_8045_() {
      return this.healingTime > 0;
   }

   public boolean m_6767_() {
      return false;
   }

   public void m_8056_() {
      this.reaper.m_6021_(this.reaper.m_20185_(), this.reaper.m_20186_() + 8.0, this.reaper.m_20189_());
      this.healingTime = 400;
      this.lastHeal = this.reaper.f_19797_;
      this.healingCount++;
   }

   public void m_8041_() {
      this.reaper.setAttackState(ReaperAttackState.IDLE);
   }

   public void m_8037_() {
      this.healingTime--;
      this.reaper.setAttackState(ReaperAttackState.REST);
      this.reaper.m_20256_(Vec3.f_82478_);
      if (!this.reaper.m_9236_().f_46443_ && this.healingTime % (10 + this.healingCount * 5) == 0) {
         this.reaper.m_21153_(this.reaper.m_21223_() + 1.0F);
      }

      if (!this.reaper.m_9236_().f_46443_ && this.healingTime % 50 == 0) {
         int dX = this.reaper.m_217043_().m_188503_(16) - 8;
         int dZ = this.reaper.m_217043_().m_188503_(16) - 8;
         int y = TaskUtils.getSpawnSafeTopLevel(this.reaper.m_9236_(), (int)this.reaper.m_20185_() + dX, 256, (int)this.reaper.m_20189_() + dZ);
         EntityType.f_20465_
            .m_262496_(
               (ServerLevel)this.reaper.m_9236_(), BlockPos.m_274561_(this.reaper.m_20185_() + dX, y, this.reaper.m_20189_() + dZ), MobSpawnType.TRIGGERED
            );
         if (!this.reaper.m_9236_().f_46443_ && this.healingTime % 100 == 0) {
            EntityType<?> m = this.reaper.m_217043_().m_188501_() < 0.5F ? EntityType.f_20501_ : EntityType.f_20524_;
            Entity e = m.m_262496_(
               (ServerLevel)this.reaper.m_9236_(), BlockPos.m_274561_(this.reaper.m_20185_() + dX, y, this.reaper.m_20189_() + dZ), MobSpawnType.TRIGGERED
            );
            if (e != null) {
               if (m == EntityType.f_20524_) {
                  e.m_8061_(EquipmentSlot.MAINHAND, new ItemStack(Items.f_42411_));
               } else {
                  e.m_8061_(EquipmentSlot.MAINHAND, new ItemStack(Items.f_42383_));
               }

               e.m_8061_(EquipmentSlot.HEAD, new ItemStack(Items.f_42468_));
               e.m_8061_(EquipmentSlot.CHEST, new ItemStack(Items.f_42469_));
               e.m_8061_(EquipmentSlot.LEGS, new ItemStack(Items.f_42470_));
               e.m_8061_(EquipmentSlot.FEET, new ItemStack(Items.f_42471_));
            }
         }
      }
   }
}
