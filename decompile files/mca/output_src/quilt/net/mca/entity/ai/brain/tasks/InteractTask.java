package quilt.net.mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import net.minecraft.class_3218;
import net.minecraft.class_4095;
import net.minecraft.class_4097;
import net.minecraft.class_4102;
import net.minecraft.class_4140;
import net.minecraft.class_4141;
import net.minecraft.class_4142;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.ai.Chore;

public class InteractTask extends class_4097<VillagerEntityMCA> {
   private final float speedModifier;

   public InteractTask(float speedModifier) {
      super(ImmutableMap.of(class_4140.field_18445, class_4141.field_18458, class_4140.field_18446, class_4141.field_18458), Integer.MAX_VALUE);
      this.speedModifier = speedModifier;
   }

   protected boolean shouldRun(class_3218 world, VillagerEntityMCA villager) {
      return shouldRun(villager);
   }

   public static boolean shouldRun(VillagerEntityMCA villager) {
      return villager.method_5805()
         && villager.method_18868().method_46873(class_4140.field_22355).isEmpty()
         && villager.getInteractions().getInteractingPlayer().filter(player -> villager.method_5858(player) <= 25.0).isPresent()
         && !villager.method_5799()
         && !villager.field_6037
         && villager.getVillagerBrain().getCurrentJob() == Chore.NONE;
   }

   protected boolean shouldKeepRunning(class_3218 world, VillagerEntityMCA villager, long time) {
      return this.shouldRun(world, villager);
   }

   protected void run(class_3218 world, VillagerEntityMCA villager, long time) {
      this.followPlayer(villager);
   }

   protected void finishRunning(class_3218 world, VillagerEntityMCA villager, long time) {
      class_4095<?> brain = villager.method_18868();
      brain.method_18875(class_4140.field_18445);
      brain.method_18875(class_4140.field_18446);
   }

   protected void keepRunning(class_3218 world, VillagerEntityMCA villager, long time) {
      this.followPlayer(villager);
   }

   protected boolean method_18915(long time) {
      return false;
   }

   private void followPlayer(VillagerEntityMCA villager) {
      class_4095<?> brain = villager.method_18868();
      villager.getInteractions().getInteractingPlayer().ifPresentOrElse(player -> {
         brain.method_18878(class_4140.field_18445, new class_4142(player, this.speedModifier, 2));
         brain.method_18878(class_4140.field_18446, new class_4102(player, true));
      }, () -> {
         brain.method_18875(class_4140.field_18445);
         brain.method_18875(class_4140.field_18446);
      });
   }
}
