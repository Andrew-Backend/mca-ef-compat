package forge.net.mca.entity.ai.brain.tasks.chore;

import com.google.common.collect.ImmutableMap;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.Chore;
import forge.net.mca.util.InventoryUtils;
import java.util.Comparator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

public class HuntingTask extends AbstractChoreTask {
   private int ticks = 0;
   private int nextAction = 0;
   private Animal target = null;

   public HuntingTask() {
      super(ImmutableMap.of(MemoryModuleType.f_26371_, MemoryStatus.VALUE_ABSENT, MemoryModuleType.f_26370_, MemoryStatus.VALUE_ABSENT));
   }

   @Override
   protected boolean shouldRun(ServerLevel world, VillagerEntityMCA villager) {
      return villager.getVillagerBrain().getCurrentJob() == Chore.HUNT && super.shouldRun(world, villager);
   }

   protected boolean shouldKeepRunning(ServerLevel world, VillagerEntityMCA villager, long time) {
      return this.shouldRun(world, villager);
   }

   protected void finishRunning(ServerLevel world, VillagerEntityMCA villager, long time) {
      ItemStack stack = villager.m_21120_(villager.getDominantHand());
      if (!stack.m_41619_()) {
         villager.m_21008_(villager.getDominantHand(), ItemStack.f_41583_);
      }
   }

   @Override
   protected void run(ServerLevel world, VillagerEntityMCA villager, long time) {
      super.run(world, villager, time);
      if (!villager.m_21033_(villager.getDominantSlot())) {
         int i = InventoryUtils.getFirstSlotContainingItem(villager.m_35311_(), stackx -> stackx.m_41720_() instanceof SwordItem);
         if (i == -1) {
            this.abandonJobWithMessage("chore.hunting.nosword");
         } else {
            ItemStack stack = villager.m_35311_().m_8020_(i);
            villager.m_21008_(villager.getDominantHand(), stack);
         }
      }
   }

   @Override
   protected void keepRunning(ServerLevel world, VillagerEntityMCA villager, long time) {
      super.keepRunning(world, villager, time);
      if (!InventoryUtils.contains(villager.m_35311_(), SwordItem.class) && !villager.m_21033_(villager.getDominantSlot())) {
         this.abandonJobWithMessage("chore.hunting.nosword");
      } else if (!villager.m_21033_(villager.getDominantSlot())) {
         int i = InventoryUtils.getFirstSlotContainingItem(villager.m_35311_(), stackx -> stackx.m_41720_() instanceof SwordItem);
         ItemStack stack = villager.m_35311_().m_8020_(i);
         villager.m_21008_(villager.getDominantHand(), stack);
      }

      if (this.target == null) {
         this.ticks++;
         if (this.ticks >= this.nextAction) {
            this.ticks = 0;
            if (villager.m_9236_().f_46441_.m_188501_() >= 0.0) {
               villager.m_9236_()
                  .m_45976_(Animal.class, villager.m_20191_().m_82377_(15.0, 3.0, 15.0))
                  .stream()
                  .filter(a -> !(a instanceof TamableAnimal))
                  .filter(a -> !a.m_6162_())
                  .min(Comparator.comparingDouble(villager::m_20280_))
                  .ifPresent(animal -> {
                     this.target = animal;
                     villager.moveTowards(this.target.m_20183_(), 1.0F);
                  });
            }

            this.nextAction = 50;
            if (this.target == null) {
               this.failedTicks = 100;
            }
         }
      } else {
         villager.moveTowards(this.target.m_20183_());
         if (this.target.m_21224_()) {
            villager.m_9236_().m_45976_(ItemEntity.class, villager.m_20191_().m_82377_(15.0, 3.0, 15.0)).forEach(item -> {
               villager.m_35311_().m_19173_(item.m_32055_());
               item.m_146870_();
            });
            this.target = null;
         } else if (villager.m_20280_(this.target) <= 12.25) {
            villager.moveTowards(this.target.m_20183_());
            villager.m_6674_(villager.getDominantHand());
            this.target.m_6469_(world.m_269111_().m_269333_(villager), 6.0F);
            villager.m_21205_().m_41622_(1, villager, e -> e.m_21166_(e.getDominantSlot()));
         }
      }
   }
}
