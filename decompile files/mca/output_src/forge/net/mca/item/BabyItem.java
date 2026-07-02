package forge.net.mca.item;

import forge.net.mca.ClientProxy;
import forge.net.mca.Config;
import forge.net.mca.advancement.criterion.CriterionMCA;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.VillagerFactory;
import forge.net.mca.entity.VillagerLike;
import forge.net.mca.entity.ai.Memories;
import forge.net.mca.entity.ai.relationship.AgeState;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.network.s2c.OpenGuiRequest;
import forge.net.mca.server.world.data.FamilyTree;
import forge.net.mca.util.WorldUtils;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class BabyItem extends Item {
   private final Gender gender;

   public BabyItem(Gender gender, Properties properties) {
      super(properties);
      this.gender = gender;
   }

   public static ItemStack createItem(Entity mother, Entity father, long seed) {
      Gender gender = Gender.getRandom();
      ItemStack stack = ((Item)(gender.binary() == Gender.MALE ? ItemsMCA.BABY_BOY : ItemsMCA.BABY_GIRL).get()).m_7968_();
      CompoundTag nbt = getBabyNbt(stack);
      nbt.m_128362_("mother", mother.m_20148_());
      nbt.m_128362_("father", father.m_20148_());
      nbt.m_128359_("motherName", mother.m_7755_().getString());
      nbt.m_128359_("fatherName", father.m_7755_().getString());
      VillagerLike<?> motherVillager = VillagerLike.toVillager(mother);
      VillagerLike<?> fatherVillager = VillagerLike.toVillager(father);
      VillagerEntityMCA child = VillagerFactory.newVillager(mother.m_9236_())
         .withPosition(mother.m_20182_())
         .withGender(gender)
         .withAge(-AgeState.getMaxAge())
         .build();
      child.getGenetics().combine(motherVillager.getGenetics(), fatherVillager.getGenetics(), seed);
      child.getTraits().inherit(motherVillager.getTraits(), seed);
      child.getTraits().inherit(fatherVillager.getTraits(), seed);
      CompoundTag compound = new CompoundTag();
      child.m_7380_(compound);
      nbt.m_128365_("child", compound);
      FamilyTree tree = FamilyTree.get((ServerLevel)mother.m_9236_());
      tree.getOrCreate(mother);
      tree.getOrCreate(father);
      return stack;
   }

   public static CompoundTag getBabyNbt(ItemStack stack) {
      CompoundTag nbt = stack.m_41784_();
      if (!nbt.m_128441_("baby")) {
         CompoundTag baby = stack.m_41698_("baby");
         baby.m_128362_("mother", Util.f_137441_);
         baby.m_128362_("father", Util.f_137441_);
         baby.m_128359_("motherName", "Unknown");
         baby.m_128359_("fatherName", "Unknown");
         baby.m_128405_("age", 0);
      }

      return stack.m_41737_("baby");
   }

   public Gender getGender() {
      return this.gender;
   }

   public InteractionResult m_6225_(UseOnContext context) {
      return InteractionResult.PASS;
   }

   public boolean onDropped(ItemStack stack, Player player) {
      if (!hasBeenInvalidated(stack)) {
         if (!player.m_9236_().f_46443_) {
            int count = 0;
            if (stack.m_41784_().m_128425_("dropAttempts", 3)) {
               count = stack.m_41784_().m_128451_("dropAttempts") + 1;
            }

            stack.m_41784_().m_128405_("dropAttempts", count);
            CriterionMCA.BABY_DROPPED_CRITERION.trigger((ServerPlayer)player, count);
            player.m_5661_(Component.m_237115_("item.mca.baby.no_drop"), true);
         }

         return false;
      } else {
         return true;
      }
   }

   public static boolean shouldCancelDrop(ItemStack stack, Player player) {
      return stack.m_41720_() instanceof BabyItem baby && !baby.onDropped(stack, player);
   }

   public void m_6883_(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
      if (!world.f_46443_) {
         if (stack.m_41788_()) {
            getBabyNbt(stack).m_128359_("babyName", stack.m_41786_().getString());
            stack.m_41787_();
            if (entity instanceof ServerPlayer player) {
               CriterionMCA.GENERIC_EVENT_CRITERION.trigger(player, "rename_baby");
            }
         }

         if (world.m_46467_() % 1200L == 0L) {
            getBabyNbt(stack).m_128405_("age", getBabyNbt(stack).m_128451_("age") + 1200);
         }
      }
   }

   public Component m_7626_(ItemStack stack) {
      return (Component)(getBabyNbt(stack).m_128441_("babyName")
         ? Component.m_237110_(this.m_5671_(stack) + ".named", new Object[]{getBabyNbt(stack).m_128461_("babyName")})
         : super.m_7626_(stack));
   }

   public String m_5671_(ItemStack stack) {
      return hasBeenInvalidated(stack) ? super.m_5671_(stack) + ".blanket" : super.m_5671_(stack);
   }

   public final InteractionResultHolder<ItemStack> m_7203_(Level world, Player player, InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      if (world.f_46443_) {
         return InteractionResultHolder.m_19098_(stack);
      }

      if (!getBabyNbt(stack).m_128441_("babyName")) {
         if (player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.BABY_NAME), serverPlayer);
         }

         return InteractionResultHolder.m_19098_(stack);
      } else {
         if (!isReadyToGrowUp(stack)) {
            return InteractionResultHolder.m_19098_(stack);
         }

         if (player instanceof ServerPlayer serverPlayer) {
            this.birthChild(stack, (ServerLevel)world, serverPlayer);
         }

         stack.m_41774_(1);
         return InteractionResultHolder.m_19090_(stack);
      }
   }

   protected VillagerEntityMCA birthChild(ItemStack stack, ServerLevel world, ServerPlayer player) {
      VillagerEntityMCA child = VillagerFactory.newVillager(world)
         .withPosition(player.m_20182_())
         .withGender(this.gender)
         .withAge(-AgeState.getMaxAge())
         .build();
      if (getBabyNbt(stack).m_128441_("child")) {
         child.m_7378_(getBabyNbt(stack).m_128469_("child"));
      }

      child.setName(getBabyNbt(stack).m_128461_("babyName"));
      WorldUtils.spawnEntity(world, child, MobSpawnType.BREEDING);
      FamilyTree tree = FamilyTree.get(world);
      child.getRelationships()
         .getFamilyEntry()
         .replaceParents(
            Stream.of("mother", "father")
               .map(key -> getBabyNbt(stack).m_128342_(key))
               .map(uuid -> Optional.ofNullable(world.m_8791_(uuid)).map(tree::getOrCreate).or(() -> tree.getOrEmpty(uuid)))
               .flatMap(Optional::stream)
         );
      Stream.of("mother", "father")
         .map(key -> world.m_8791_(getBabyNbt(stack).m_128342_(key)))
         .filter(Objects::nonNull)
         .filter(e -> e instanceof ServerPlayer)
         .map(ServerPlayer.class::cast)
         .distinct()
         .forEach(ply -> {
            CriterionMCA.FAMILY.trigger(ply);
            Memories memories = child.getVillagerBrain().getMemoriesForPlayer(ply);
            memories.setHearts(Config.getInstance().childInitialHearts);
         });
      return child;
   }

   public void m_7373_(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
      Player player = ClientProxy.getClientPlayer();
      int age = getBabyNbt(stack).m_128451_("age") + (int)(world == null ? 0L : world.m_46467_() % 1200L);
      if (getBabyNbt(stack).m_128441_("babyName")) {
         MutableComponent text = Component.m_237113_(getBabyNbt(stack).m_128461_("babyName"));
         tooltip.add(
            Component.m_237110_("item.mca.baby.name", new Object[]{text.m_6270_(text.m_7383_().m_178520_(this.gender.getColor()))})
               .m_130940_(ChatFormatting.GRAY)
         );
         if (age > 0) {
            tooltip.add(Component.m_237110_("item.mca.baby.age", new Object[]{StringUtil.m_14404_(age)}).m_130940_(ChatFormatting.GRAY));
         }
      } else {
         tooltip.add(Component.m_237115_("item.mca.baby.give_name").m_130940_(ChatFormatting.YELLOW));
      }

      tooltip.add(Component.m_237113_(""));
      Stream.of("mother", "father")
         .forEach(
            p -> tooltip.add(
               Component.m_237110_(
                     "item.mca.baby." + p,
                     new Object[]{
                        player != null && getBabyNbt(stack).m_128342_(p).equals(player.m_20148_())
                           ? Component.m_237115_("item.mca.baby.owner.you")
                           : getBabyNbt(stack).m_128461_(p + "Name")
                     }
                  )
                  .m_130940_(ChatFormatting.GRAY)
            )
         );
      if (getBabyNbt(stack).m_128441_("babyName") && canGrow(age)) {
         tooltip.add(Component.m_237115_("item.mca.baby.state.ready").m_130940_(ChatFormatting.DARK_GREEN));
      }

      if (getBabyNbt(stack).m_128457_("infectionProgress") > 0.0F) {
         tooltip.add(Component.m_237115_("item.mca.baby.state.infected").m_130940_(ChatFormatting.DARK_GREEN));
      }
   }

   public static boolean hasBeenInvalidated(ItemStack stack) {
      return stack.m_41784_().m_128441_("invalidated");
   }

   private static boolean canGrow(int age) {
      return age >= Config.getServerConfig().babyItemGrowUpTime;
   }

   private static boolean isReadyToGrowUp(ItemStack stack) {
      return stack.m_41782_() && canGrow(getBabyNbt(stack).m_128451_("age"));
   }
}
