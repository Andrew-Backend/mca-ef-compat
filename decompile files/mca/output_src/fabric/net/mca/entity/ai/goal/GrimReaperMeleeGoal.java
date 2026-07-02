package fabric.net.mca.entity.ai.goal;

import fabric.net.mca.entity.GrimReaperEntity;
import fabric.net.mca.entity.ReaperAttackState;
import net.minecraft.class_1268;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1309;
import net.minecraft.class_1352;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_243;
import net.minecraft.class_5134;

public class GrimReaperMeleeGoal extends class_1352 {
   private static final int COOLDOWN = 150;
   private final GrimReaperEntity reaper;
   private int blockDuration;
   private int attackDuration;
   private int retreatDuration;
   private int lastAttack = 0;

   public GrimReaperMeleeGoal(GrimReaperEntity reaper) {
      this.reaper = reaper;
   }

   public boolean method_6264() {
      class_1309 entityToAttack = this.reaper.method_5968();
      return entityToAttack != null
         && this.reaper.method_5858(entityToAttack) <= 144.0
         && this.reaper.field_6012 > this.lastAttack + 150
         && this.reaper.getAttackState() != ReaperAttackState.REST;
   }

   public boolean method_6266() {
      return this.retreatDuration > 0 && this.reaper.getAttackState() != ReaperAttackState.REST;
   }

   public boolean method_6267() {
      return false;
   }

   public void method_6269() {
      this.blockDuration = 50;
      this.attackDuration = 100;
      this.retreatDuration = 20;
      this.lastAttack = this.reaper.field_6012;
   }

   public void method_6270() {
      super.method_6270();
      this.reaper.setAttackState(ReaperAttackState.IDLE);
   }

   private void curse() {
      class_1309 entityToAttack = this.reaper.method_5968();
      if (entityToAttack instanceof class_1657 player && player.method_6039()) {
         double dX = this.reaper.method_23317() - player.method_23317();
         double dZ = this.reaper.method_23321() - player.method_23321();
         this.reaper.method_5859(player.method_23317() - dX * 2.0, player.method_23318() + 2.0, this.reaper.method_23321() - dZ * 2.0);
         if (!this.reaper.method_37908().field_9236 && this.reaper.method_6051().method_43057() >= 0.2F) {
            int currentItem = player.method_31548().field_7545;
            int randomItem = this.reaper.method_6051().method_43048(9);
            class_1799 currentItemStack = player.method_31548().method_5438(currentItem);
            class_1799 randomItemStack = player.method_31548().method_5438(randomItem);
            player.method_31548().method_5447(currentItem, randomItemStack);
            player.method_31548().method_5447(randomItem, currentItemStack);
            entityToAttack.method_6092(new class_1293(class_1294.field_5919, 200));
         }
      }
   }

   public void method_6268() {
      if (this.reaper.getAttackState() != ReaperAttackState.REST) {
         class_1309 entityToAttack = this.reaper.method_5968();
         if (entityToAttack == null) {
            this.retreatDuration = 0;
         } else {
            if (this.blockDuration > 0) {
               this.blockDuration--;
               this.reaper.setAttackState(ReaperAttackState.BLOCK);
               if (this.blockDuration == 0) {
                  this.curse();
               }

               if (this.reaper.method_5858(entityToAttack) <= 4.0) {
                  int rX = this.reaper.method_6051().method_43048(10);
                  int rY = this.reaper.method_6051().method_43048(6);
                  int rZ = this.reaper.method_6051().method_43048(10);
                  this.reaper.method_5859(this.reaper.method_23317() - 5.0 + rX, this.reaper.method_23318() + rY, this.reaper.method_23321() - 5.0 + rZ);
                  this.reaper.method_5942().method_6340();
               }

               class_243 deltaMovement = this.reaper.method_18798();
               this.reaper.method_18799(new class_243(deltaMovement.field_1352, 0.05, deltaMovement.field_1350));
            } else if (this.attackDuration > 0) {
               this.attackDuration--;
               this.reaper.setAttackState(ReaperAttackState.PRE);
               class_243 dir = entityToAttack.method_19538().method_1020(this.reaper.method_19538()).method_1029().method_1021(0.15);
               this.reaper.method_5762(dir.field_1352, dir.field_1351, dir.field_1350);
               if (this.reaper.method_5858(entityToAttack) <= 1.0) {
                  this.reaper.method_6104(class_1268.field_5808);
                  this.attackDuration = 0;
                  entityToAttack.method_5643(
                     this.reaper.method_37908().method_48963().method_48812(this.reaper), (float)this.reaper.method_26825(class_5134.field_23721)
                  );
                  entityToAttack.method_6092(new class_1293(class_1294.field_5920, 200));
               }
            } else {
               this.retreatDuration--;
               this.reaper.setAttackState(ReaperAttackState.POST);
               class_243 dir = entityToAttack.method_19538().method_1020(this.reaper.method_19538()).method_1029().method_1021(-0.1);
               this.reaper.method_18800(dir.field_1352, dir.field_1351, dir.field_1350);
            }
         }
      }
   }
}
