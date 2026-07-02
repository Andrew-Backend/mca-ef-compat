package fabric.net.mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import fabric.net.mca.entity.EquipmentSet;
import fabric.net.mca.entity.VillagerEntityMCA;
import fabric.net.mca.entity.ai.MemoryModuleTypeMCA;
import fabric.net.mca.util.InventoryUtils;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.class_1304;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1811;
import net.minecraft.class_3218;
import net.minecraft.class_4097;
import net.minecraft.class_4140;
import net.minecraft.class_4141;

public class EquipmentTask extends class_4097<VillagerEntityMCA> {
   private static final int COOLDOWN = 100;
   private int lastEquipTime;
   private final Predicate<VillagerEntityMCA> condition;
   private final Function<VillagerEntityMCA, EquipmentSet> equipmentSet;
   private boolean lastArmorWearState;

   public EquipmentTask(Predicate<VillagerEntityMCA> condition, Function<VillagerEntityMCA, EquipmentSet> set) {
      super(ImmutableMap.of((class_4140)MemoryModuleTypeMCA.WEARS_ARMOR.get(), class_4141.field_18458));
      this.condition = condition;
      this.equipmentSet = set;
   }

   protected boolean shouldRun(class_3218 world, VillagerEntityMCA villager) {
      if (this.lastArmorWearState != villager.getVillagerBrain().getArmorWear()) {
         return true;
      }

      boolean present = villager.method_18868().method_46873((class_4140)MemoryModuleTypeMCA.WEARS_ARMOR.get()).isPresent();
      if (!this.condition.test(villager)) {
         return villager.field_6012 - this.lastEquipTime > 100 ? present : false;
      }

      this.lastEquipTime = villager.field_6012;
      return !present || this.equipmentSet.apply(villager).getMainHand() != null && villager.method_6047().method_7960();
   }

   private void equipBestArmor(VillagerEntityMCA villager, class_1304 slot, class_1792 fallback) {
      class_1799 stack = InventoryUtils.getBestArmor(villager.method_35199(), slot).orElse(fallback == null ? class_1799.field_8037 : new class_1799(fallback));
      villager.method_5673(slot, stack);
   }

   private void equipBestWeapon(VillagerEntityMCA villager, class_1792 fallback) {
      class_1799 stack = InventoryUtils.getBestSword(villager.method_35199()).orElse(fallback == null ? class_1799.field_8037 : new class_1799(fallback));
      villager.method_5673(villager.getDominantSlot(), stack);
   }

   private void equipBestRanged(VillagerEntityMCA villager, class_1792 fallback) {
      class_1799 stack = InventoryUtils.getBestRanged(villager.method_35199()).orElse(fallback == null ? class_1799.field_8037 : new class_1799(fallback));
      villager.method_5673(villager.getDominantSlot(), stack);
   }

   protected void run(class_3218 world, VillagerEntityMCA villager, long time) {
      super.method_18920(world, villager, time);
      this.lastArmorWearState = villager.getVillagerBrain().getArmorWear();
      EquipmentSet set = this.equipmentSet.apply(villager);
      boolean wear = this.condition.test(villager);
      if (wear) {
         villager.method_18868().method_18878((class_4140)MemoryModuleTypeMCA.WEARS_ARMOR.get(), true);
      } else {
         villager.method_18868().method_18875((class_4140)MemoryModuleTypeMCA.WEARS_ARMOR.get());
      }

      if (wear) {
         if (set.getMainHand() instanceof class_1811) {
            this.equipBestRanged(villager, set.getMainHand());
         } else {
            this.equipBestWeapon(villager, set.getMainHand());
         }

         villager.method_5673(villager.getOpposingSlot(), new class_1799(set.getGetOffHand()));
      } else {
         villager.method_6122(villager.getDominantHand(), class_1799.field_8037);
         villager.method_6122(villager.getOpposingHand(), class_1799.field_8037);
      }

      if (!wear && !villager.getVillagerBrain().getArmorWear()) {
         villager.method_5673(class_1304.field_6169, class_1799.field_8037);
         villager.method_5673(class_1304.field_6174, class_1799.field_8037);
         villager.method_5673(class_1304.field_6172, class_1799.field_8037);
         villager.method_5673(class_1304.field_6166, class_1799.field_8037);
      } else {
         this.equipBestArmor(villager, class_1304.field_6169, set.getHead());
         this.equipBestArmor(villager, class_1304.field_6174, set.getChest());
         this.equipBestArmor(villager, class_1304.field_6172, set.getLegs());
         this.equipBestArmor(villager, class_1304.field_6166, set.getFeet());
      }
   }
}
