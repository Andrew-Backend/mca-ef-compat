package forge.net.mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import forge.net.mca.entity.EquipmentSet;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.MemoryModuleTypeMCA;
import forge.net.mca.util.InventoryUtils;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;

public class EquipmentTask extends Behavior<VillagerEntityMCA> {
   private static final int COOLDOWN = 100;
   private int lastEquipTime;
   private final Predicate<VillagerEntityMCA> condition;
   private final Function<VillagerEntityMCA, EquipmentSet> equipmentSet;
   private boolean lastArmorWearState;

   public EquipmentTask(Predicate<VillagerEntityMCA> condition, Function<VillagerEntityMCA, EquipmentSet> set) {
      super(ImmutableMap.of((MemoryModuleType)MemoryModuleTypeMCA.WEARS_ARMOR.get(), MemoryStatus.REGISTERED));
      this.condition = condition;
      this.equipmentSet = set;
   }

   protected boolean shouldRun(ServerLevel world, VillagerEntityMCA villager) {
      if (this.lastArmorWearState != villager.getVillagerBrain().getArmorWear()) {
         return true;
      }

      boolean present = villager.m_6274_().m_257414_((MemoryModuleType)MemoryModuleTypeMCA.WEARS_ARMOR.get()).isPresent();
      if (!this.condition.test(villager)) {
         return villager.f_19797_ - this.lastEquipTime > 100 ? present : false;
      }

      this.lastEquipTime = villager.f_19797_;
      return !present || this.equipmentSet.apply(villager).getMainHand() != null && villager.m_21205_().m_41619_();
   }

   private void equipBestArmor(VillagerEntityMCA villager, EquipmentSlot slot, Item fallback) {
      ItemStack stack = InventoryUtils.getBestArmor(villager.m_35311_(), slot).orElse(fallback == null ? ItemStack.f_41583_ : new ItemStack(fallback));
      villager.m_8061_(slot, stack);
   }

   private void equipBestWeapon(VillagerEntityMCA villager, Item fallback) {
      ItemStack stack = InventoryUtils.getBestSword(villager.m_35311_()).orElse(fallback == null ? ItemStack.f_41583_ : new ItemStack(fallback));
      villager.m_8061_(villager.getDominantSlot(), stack);
   }

   private void equipBestRanged(VillagerEntityMCA villager, Item fallback) {
      ItemStack stack = InventoryUtils.getBestRanged(villager.m_35311_()).orElse(fallback == null ? ItemStack.f_41583_ : new ItemStack(fallback));
      villager.m_8061_(villager.getDominantSlot(), stack);
   }

   protected void run(ServerLevel world, VillagerEntityMCA villager, long time) {
      super.m_6735_(world, villager, time);
      this.lastArmorWearState = villager.getVillagerBrain().getArmorWear();
      EquipmentSet set = this.equipmentSet.apply(villager);
      boolean wear = this.condition.test(villager);
      if (wear) {
         villager.m_6274_().m_21879_((MemoryModuleType)MemoryModuleTypeMCA.WEARS_ARMOR.get(), true);
      } else {
         villager.m_6274_().m_21936_((MemoryModuleType)MemoryModuleTypeMCA.WEARS_ARMOR.get());
      }

      if (wear) {
         if (set.getMainHand() instanceof ProjectileWeaponItem) {
            this.equipBestRanged(villager, set.getMainHand());
         } else {
            this.equipBestWeapon(villager, set.getMainHand());
         }

         villager.m_8061_(villager.getOpposingSlot(), new ItemStack(set.getGetOffHand()));
      } else {
         villager.m_21008_(villager.getDominantHand(), ItemStack.f_41583_);
         villager.m_21008_(villager.getOpposingHand(), ItemStack.f_41583_);
      }

      if (!wear && !villager.getVillagerBrain().getArmorWear()) {
         villager.m_8061_(EquipmentSlot.HEAD, ItemStack.f_41583_);
         villager.m_8061_(EquipmentSlot.CHEST, ItemStack.f_41583_);
         villager.m_8061_(EquipmentSlot.LEGS, ItemStack.f_41583_);
         villager.m_8061_(EquipmentSlot.FEET, ItemStack.f_41583_);
      } else {
         this.equipBestArmor(villager, EquipmentSlot.HEAD, set.getHead());
         this.equipBestArmor(villager, EquipmentSlot.CHEST, set.getChest());
         this.equipBestArmor(villager, EquipmentSlot.LEGS, set.getLegs());
         this.equipBestArmor(villager, EquipmentSlot.FEET, set.getFeet());
      }
   }
}
