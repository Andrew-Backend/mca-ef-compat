package fabric.net.mca.item;

import fabric.net.mca.ClientProxy;
import fabric.net.mca.Config;
import fabric.net.mca.advancement.criterion.CriterionMCA;
import fabric.net.mca.cobalt.network.NetworkHandler;
import fabric.net.mca.entity.VillagerEntityMCA;
import fabric.net.mca.entity.VillagerFactory;
import fabric.net.mca.entity.VillagerLike;
import fabric.net.mca.entity.ai.Memories;
import fabric.net.mca.entity.ai.relationship.AgeState;
import fabric.net.mca.entity.ai.relationship.Gender;
import fabric.net.mca.network.s2c.OpenGuiRequest;
import fabric.net.mca.server.world.data.FamilyTree;
import fabric.net.mca.util.WorldUtils;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1271;
import net.minecraft.class_1297;
import net.minecraft.class_156;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1836;
import net.minecraft.class_1838;
import net.minecraft.class_1937;
import net.minecraft.class_2487;
import net.minecraft.class_2561;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3544;
import net.minecraft.class_3730;
import net.minecraft.class_5250;
import net.minecraft.class_1792.class_1793;
import org.jetbrains.annotations.Nullable;

public class BabyItem extends class_1792 {
   private final Gender gender;

   public BabyItem(Gender gender, class_1793 properties) {
      super(properties);
      this.gender = gender;
   }

   public static class_1799 createItem(class_1297 mother, class_1297 father, long seed) {
      Gender gender = Gender.getRandom();
      class_1799 stack = ((class_1792)(gender.binary() == Gender.MALE ? ItemsMCA.BABY_BOY : ItemsMCA.BABY_GIRL).get()).method_7854();
      class_2487 nbt = getBabyNbt(stack);
      nbt.method_25927("mother", mother.method_5667());
      nbt.method_25927("father", father.method_5667());
      nbt.method_10582("motherName", mother.method_5477().getString());
      nbt.method_10582("fatherName", father.method_5477().getString());
      VillagerLike<?> motherVillager = VillagerLike.toVillager(mother);
      VillagerLike<?> fatherVillager = VillagerLike.toVillager(father);
      VillagerEntityMCA child = VillagerFactory.newVillager(mother.method_37908())
         .withPosition(mother.method_19538())
         .withGender(gender)
         .withAge(-AgeState.getMaxAge())
         .build();
      child.getGenetics().combine(motherVillager.getGenetics(), fatherVillager.getGenetics(), seed);
      child.getTraits().inherit(motherVillager.getTraits(), seed);
      child.getTraits().inherit(fatherVillager.getTraits(), seed);
      class_2487 compound = new class_2487();
      child.method_5652(compound);
      nbt.method_10566("child", compound);
      FamilyTree tree = FamilyTree.get((class_3218)mother.method_37908());
      tree.getOrCreate(mother);
      tree.getOrCreate(father);
      return stack;
   }

   public static class_2487 getBabyNbt(class_1799 stack) {
      class_2487 nbt = stack.method_7948();
      if (!nbt.method_10545("baby")) {
         class_2487 baby = stack.method_7911("baby");
         baby.method_25927("mother", class_156.field_25140);
         baby.method_25927("father", class_156.field_25140);
         baby.method_10582("motherName", "Unknown");
         baby.method_10582("fatherName", "Unknown");
         baby.method_10569("age", 0);
      }

      return stack.method_7941("baby");
   }

   public Gender getGender() {
      return this.gender;
   }

   public class_1269 method_7884(class_1838 context) {
      return class_1269.field_5811;
   }

   public boolean onDropped(class_1799 stack, class_1657 player) {
      if (!hasBeenInvalidated(stack)) {
         if (!player.method_37908().field_9236) {
            int count = 0;
            if (stack.method_7948().method_10573("dropAttempts", 3)) {
               count = stack.method_7948().method_10550("dropAttempts") + 1;
            }

            stack.method_7948().method_10569("dropAttempts", count);
            CriterionMCA.BABY_DROPPED_CRITERION.trigger((class_3222)player, count);
            player.method_7353(class_2561.method_43471("item.mca.baby.no_drop"), true);
         }

         return false;
      } else {
         return true;
      }
   }

   public static boolean shouldCancelDrop(class_1799 stack, class_1657 player) {
      return stack.method_7909() instanceof BabyItem baby && !baby.onDropped(stack, player);
   }

   public void method_7888(class_1799 stack, class_1937 world, class_1297 entity, int slot, boolean selected) {
      if (!world.field_9236) {
         if (stack.method_7938()) {
            getBabyNbt(stack).method_10582("babyName", stack.method_7964().getString());
            stack.method_7925();
            if (entity instanceof class_3222 player) {
               CriterionMCA.GENERIC_EVENT_CRITERION.trigger(player, "rename_baby");
            }
         }

         if (world.method_8510() % 1200L == 0L) {
            getBabyNbt(stack).method_10569("age", getBabyNbt(stack).method_10550("age") + 1200);
         }
      }
   }

   public class_2561 method_7864(class_1799 stack) {
      return (class_2561)(getBabyNbt(stack).method_10545("babyName")
         ? class_2561.method_43469(this.method_7866(stack) + ".named", new Object[]{getBabyNbt(stack).method_10558("babyName")})
         : super.method_7864(stack));
   }

   public String method_7866(class_1799 stack) {
      return hasBeenInvalidated(stack) ? super.method_7866(stack) + ".blanket" : super.method_7866(stack);
   }

   public final class_1271<class_1799> method_7836(class_1937 world, class_1657 player, class_1268 hand) {
      class_1799 stack = player.method_5998(hand);
      if (world.field_9236) {
         return class_1271.method_22430(stack);
      }

      if (!getBabyNbt(stack).method_10545("babyName")) {
         if (player instanceof class_3222 serverPlayer) {
            NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.BABY_NAME), serverPlayer);
         }

         return class_1271.method_22430(stack);
      } else {
         if (!isReadyToGrowUp(stack)) {
            return class_1271.method_22430(stack);
         }

         if (player instanceof class_3222 serverPlayer) {
            this.birthChild(stack, (class_3218)world, serverPlayer);
         }

         stack.method_7934(1);
         return class_1271.method_22427(stack);
      }
   }

   protected VillagerEntityMCA birthChild(class_1799 stack, class_3218 world, class_3222 player) {
      VillagerEntityMCA child = VillagerFactory.newVillager(world)
         .withPosition(player.method_19538())
         .withGender(this.gender)
         .withAge(-AgeState.getMaxAge())
         .build();
      if (getBabyNbt(stack).method_10545("child")) {
         child.method_5749(getBabyNbt(stack).method_10562("child"));
      }

      child.setName(getBabyNbt(stack).method_10558("babyName"));
      WorldUtils.spawnEntity(world, child, class_3730.field_16466);
      FamilyTree tree = FamilyTree.get(world);
      child.getRelationships()
         .getFamilyEntry()
         .replaceParents(
            Stream.of("mother", "father")
               .map(key -> getBabyNbt(stack).method_25926(key))
               .map(uuid -> Optional.ofNullable(world.method_14190(uuid)).map(tree::getOrCreate).or(() -> tree.getOrEmpty(uuid)))
               .flatMap(Optional::stream)
         );
      Stream.of("mother", "father")
         .map(key -> world.method_14190(getBabyNbt(stack).method_25926(key)))
         .filter(Objects::nonNull)
         .filter(e -> e instanceof class_3222)
         .map(class_3222.class::cast)
         .distinct()
         .forEach(ply -> {
            CriterionMCA.FAMILY.trigger(ply);
            Memories memories = child.getVillagerBrain().getMemoriesForPlayer(ply);
            memories.setHearts(Config.getInstance().childInitialHearts);
         });
      return child;
   }

   public void method_7851(class_1799 stack, @Nullable class_1937 world, List<class_2561> tooltip, class_1836 flag) {
      class_1657 player = ClientProxy.getClientPlayer();
      int age = getBabyNbt(stack).method_10550("age") + (int)(world == null ? 0L : world.method_8510() % 1200L);
      if (getBabyNbt(stack).method_10545("babyName")) {
         class_5250 text = class_2561.method_43470(getBabyNbt(stack).method_10558("babyName"));
         tooltip.add(
            class_2561.method_43469("item.mca.baby.name", new Object[]{text.method_10862(text.method_10866().method_36139(this.gender.getColor()))})
               .method_27692(class_124.field_1080)
         );
         if (age > 0) {
            tooltip.add(class_2561.method_43469("item.mca.baby.age", new Object[]{class_3544.method_15439(age)}).method_27692(class_124.field_1080));
         }
      } else {
         tooltip.add(class_2561.method_43471("item.mca.baby.give_name").method_27692(class_124.field_1054));
      }

      tooltip.add(class_2561.method_43470(""));
      Stream.of("mother", "father")
         .forEach(
            p -> tooltip.add(
               class_2561.method_43469(
                     "item.mca.baby." + p,
                     new Object[]{
                        player != null && getBabyNbt(stack).method_25926(p).equals(player.method_5667())
                           ? class_2561.method_43471("item.mca.baby.owner.you")
                           : getBabyNbt(stack).method_10558(p + "Name")
                     }
                  )
                  .method_27692(class_124.field_1080)
            )
         );
      if (getBabyNbt(stack).method_10545("babyName") && canGrow(age)) {
         tooltip.add(class_2561.method_43471("item.mca.baby.state.ready").method_27692(class_124.field_1077));
      }

      if (getBabyNbt(stack).method_10583("infectionProgress") > 0.0F) {
         tooltip.add(class_2561.method_43471("item.mca.baby.state.infected").method_27692(class_124.field_1077));
      }
   }

   public static boolean hasBeenInvalidated(class_1799 stack) {
      return stack.method_7948().method_10545("invalidated");
   }

   private static boolean canGrow(int age) {
      return age >= Config.getServerConfig().babyItemGrowUpTime;
   }

   private static boolean isReadyToGrowUp(class_1799 stack) {
      return stack.method_7985() && canGrow(getBabyNbt(stack).method_10550("age"));
   }
}
