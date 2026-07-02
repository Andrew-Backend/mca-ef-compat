package forge.net.mca.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import forge.net.mca.Config;
import forge.net.mca.entity.EntitiesMCA;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.relationship.RelationshipState;
import forge.net.mca.item.BabyItem;
import forge.net.mca.server.SpawnQueue;
import forge.net.mca.server.world.data.Building;
import forge.net.mca.server.world.data.FamilyTree;
import forge.net.mca.server.world.data.FamilyTreeNode;
import forge.net.mca.server.world.data.PlayerSaveData;
import forge.net.mca.server.world.data.Village;
import forge.net.mca.server.world.data.VillageManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.EntityTypeTest;

public class AdminCommand {
   private static final List<CompoundTag> storedVillagers = new ArrayList<>();

   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_(
                                                                     "mca-admin"
                                                                  )
                                                                  .then(register("help", AdminCommand::displayHelp)))
                                                               .then(register("clearLoadedVillagers", AdminCommand::clearLoadedVillagers)))
                                                            .then(register("restoreClearedVillagers", AdminCommand::restoreClearedVillagers)))
                                                         .then(
                                                            register("forceBuildingType")
                                                               .then(
                                                                  Commands.m_82129_("type", StringArgumentType.string())
                                                                     .executes(AdminCommand::forceBuildingType)
                                                               )
                                                               .executes(AdminCommand::clearForcedBuildingType)
                                                         ))
                                                      .then(register("forceFullHearts", AdminCommand::forceFullHearts)))
                                                   .then(register("forceBabyGrowth", AdminCommand::forceBabyGrowth)))
                                                .then(register("forceChildGrowth", AdminCommand::forceChildGrowth)))
                                             .then(register("incrementHearts", AdminCommand::incrementHearts)))
                                          .then(register("decrementHearts", AdminCommand::decrementHearts)))
                                       .then(register("resetPlayerData", AdminCommand::resetPlayerData)))
                                    .then(register("resetMarriage", AdminCommand::resetMarriage)))
                                 .then(register("listVillages", AdminCommand::listVillages)))
                              .then(
                                 register("assumeNameDead").then(Commands.m_82129_("name", StringArgumentType.string()).executes(AdminCommand::assumeNameDead))
                              ))
                           .then(register("assumeUuidDead").then(Commands.m_82129_("uuid", UuidArgument.m_113850_()).executes(AdminCommand::assumeUuidDead))))
                        .then(
                           register("removeVillageWithId")
                              .then(Commands.m_82129_("id", IntegerArgumentType.integer()).executes(AdminCommand::removeVillageWithId))
                        ))
                     .then(
                        register("convertVanillaVillagers")
                           .then(Commands.m_82129_("radius", IntegerArgumentType.integer()).executes(AdminCommand::convertVanillaVillagers))
                     ))
                  .then(register("removeVillage").then(Commands.m_82129_("name", StringArgumentType.string()).executes(AdminCommand::removeVillage))))
               .then(
                  register("buildingProcessingRate")
                     .then(Commands.m_82129_("cooldown", IntegerArgumentType.integer()).executes(AdminCommand::buildingProcessingRate))
               ))
            .requires(serverCommandSource -> serverCommandSource.m_6761_(2))
      );
   }

   private static int listVillages(CommandContext<CommandSourceStack> ctx) {
      for (Village village : VillageManager.get(((CommandSourceStack)ctx.getSource()).m_81372_())) {
         BlockPos pos = village.getBox().m_162394_();
         success(
            String.format(
               Locale.ROOT,
               "%d: %s with %d buildings and %d/%d villager(s)",
               village.getId(),
               village.getName(),
               village.getBuildings().size(),
               village.getPopulation(),
               village.getMaxPopulation()
            ),
            ctx,
            new HoverEvent(Action.f_130831_, Component.m_237115_("chat.coordinates.tooltip")),
            new ClickEvent(net.minecraft.network.chat.ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + pos.m_123341_() + " ~ " + pos.m_123343_())
         );
      }

      return 0;
   }

   private static int assumeNameDead(CommandContext<CommandSourceStack> ctx) {
      String name = StringArgumentType.getString(ctx, "name");
      FamilyTree tree = FamilyTree.get(((CommandSourceStack)ctx.getSource()).m_81372_());
      List<FamilyTreeNode> collect = tree.getAllWithName(name).filter(n -> !n.isDeceased()).toList();
      if (collect.isEmpty()) {
         fail("Villager does not exist.", ctx);
      } else if (collect.size() == 1) {
         collect.get(0).setDeceased(true);
         assumeDead(ctx, collect.get(0).id());
         success("Villager has been marked as deceased", ctx);
      } else {
         fail("Villager not unique, use uuid!", ctx);
      }

      return 0;
   }

   private static int assumeUuidDead(CommandContext<CommandSourceStack> ctx) {
      UUID uuid = UuidArgument.m_113853_(ctx, "uuid");
      FamilyTree tree = FamilyTree.get(((CommandSourceStack)ctx.getSource()).m_81372_());
      Optional<FamilyTreeNode> node = tree.getOrEmpty(uuid);
      if (node.isPresent()) {
         node.get().setDeceased(true);
         assumeDead(ctx, uuid);
         success("Villager has been marked as deceased", ctx);
      } else {
         fail("Villager does not exist.", ctx);
      }

      return 0;
   }

   private static void assumeDead(CommandContext<CommandSourceStack> ctx, UUID uuid) {
      for (Village village : VillageManager.get(((CommandSourceStack)ctx.getSource()).m_81372_())) {
         village.removeResident(uuid);
      }

      FamilyTree tree = FamilyTree.get(((CommandSourceStack)ctx.getSource()).m_81372_());
      Optional<FamilyTreeNode> node = tree.getOrEmpty(uuid);
      node.filter(n -> n.partner() != null).ifPresent(n -> n.updatePartner(null, RelationshipState.WIDOW));
      ((CommandSourceStack)ctx.getSource()).m_81372_().m_6907_().forEach(player -> {
         PlayerSaveData playerData = PlayerSaveData.get(player);
         if (playerData.getPartnerUUID().orElse(Util.f_137441_).equals(uuid)) {
            playerData.endRelationShip(RelationshipState.SINGLE);
         }
      });
   }

   private static int removeVillageWithId(CommandContext<CommandSourceStack> ctx) {
      int id = IntegerArgumentType.getInteger(ctx, "id");
      if (VillageManager.get(((CommandSourceStack)ctx.getSource()).m_81372_()).removeVillage(id)) {
         success("Village deleted.", ctx);
      } else {
         fail("Village with this ID does not exist.", ctx);
      }

      return 0;
   }

   private static int convertVanillaVillagers(CommandContext<CommandSourceStack> ctx) {
      int radius = IntegerArgumentType.getInteger(ctx, "radius");
      ServerLevel world = ((CommandSourceStack)ctx.getSource()).m_81372_();
      world.m_143280_(EntityType.f_20492_, x -> true).stream().map(Villager.class::cast).forEach(v -> {
         if (v.m_20270_(((CommandSourceStack)ctx.getSource()).m_81373_()) < radius) {
            SpawnQueue.getInstance().convert(v);
         }
      });
      return 0;
   }

   private static int setBuildingType(CommandContext<CommandSourceStack> ctx, String type) {
      Player player = ((CommandSourceStack)ctx.getSource()).m_230896_();
      if (player == null) {
         return 0;
      }

      VillageManager villages = VillageManager.get(((CommandSourceStack)ctx.getSource()).m_81372_());
      Optional<Village> village = villages.findNearestVillage(player);
      Optional<Building> building = village.flatMap(v -> v.getBuildings().values().stream().filter(b -> b.containsPos(player.m_20183_())).findAny());
      if (building.isPresent()) {
         if (building.get().getType().equals(type)) {
            building.get().setTypeForced(false);
            building.get().determineType();
         } else {
            building.get().setTypeForced(true);
            building.get().setType(type);
         }
      } else {
         fail(Component.m_237115_("blueprint.noBuilding").getString(), ctx);
      }

      return 0;
   }

   private static int forceBuildingType(CommandContext<CommandSourceStack> ctx) {
      return setBuildingType(ctx, StringArgumentType.getString(ctx, "type"));
   }

   private static int clearForcedBuildingType(CommandContext<CommandSourceStack> ctx) {
      return setBuildingType(ctx, null);
   }

   private static int removeVillage(CommandContext<CommandSourceStack> ctx) {
      String name = StringArgumentType.getString(ctx, "name");
      List<Village> collect = VillageManager.get(((CommandSourceStack)ctx.getSource()).m_81372_()).findVillages(v -> v.getName().equals(name)).toList();
      if (collect.isEmpty()) {
         fail("No village with this name exists.", ctx);
      } else if (collect.size() > 1) {
         success("Village deleted.", ctx);
         fail("No village with this name exists.", ctx);
      } else if (VillageManager.get(((CommandSourceStack)ctx.getSource()).m_81372_()).removeVillage(collect.get(0).getId())) {
         success("Village deleted.", ctx);
      } else {
         fail("Unknown error.", ctx);
      }

      return 0;
   }

   private static int buildingProcessingRate(CommandContext<CommandSourceStack> ctx) {
      int cooldown = IntegerArgumentType.getInteger(ctx, "cooldown");
      VillageManager.get(((CommandSourceStack)ctx.getSource()).m_81372_()).setBuildingCooldown(cooldown);
      return 0;
   }

   private static int resetPlayerData(CommandContext<CommandSourceStack> ctx) {
      ServerPlayer player = ((CommandSourceStack)ctx.getSource()).m_230896_();
      if (player == null) {
         return 0;
      }

      PlayerSaveData playerData = PlayerSaveData.get(player);
      playerData.reset();
      success("Player data reset.", ctx);
      return 0;
   }

   private static int resetMarriage(CommandContext<CommandSourceStack> ctx) {
      ServerPlayer player = ((CommandSourceStack)ctx.getSource()).m_230896_();
      if (player == null) {
         return 0;
      }

      PlayerSaveData playerData = PlayerSaveData.get(player);
      playerData.endRelationShip(RelationshipState.SINGLE);
      success("Marriage reset.", ctx);
      return 0;
   }

   private static int decrementHearts(CommandContext<CommandSourceStack> ctx) {
      Player player = ((CommandSourceStack)ctx.getSource()).m_230896_();
      if (player == null) {
         return 0;
      }

      getLoadedVillagers(ctx).forEach(v -> v.getVillagerBrain().getMemoriesForPlayer(player).modHearts(-10));
      return 0;
   }

   private static int incrementHearts(CommandContext<CommandSourceStack> ctx) {
      Player player = ((CommandSourceStack)ctx.getSource()).m_230896_();
      if (player == null) {
         return 0;
      }

      getLoadedVillagers(ctx).forEach(v -> v.getVillagerBrain().getMemoriesForPlayer(player).modHearts(10));
      return 0;
   }

   private static int forceChildGrowth(CommandContext<CommandSourceStack> ctx) {
      getLoadedVillagers(ctx).forEach(v -> v.m_146762_(0));
      return 0;
   }

   private static int forceBabyGrowth(CommandContext<CommandSourceStack> ctx) {
      Player player = ((CommandSourceStack)ctx.getSource()).m_230896_();
      if (player != null) {
         ItemStack heldStack = player.m_21205_();
         if (heldStack.m_41720_() instanceof BabyItem) {
            CompoundTag nbt = BabyItem.getBabyNbt(heldStack);
            nbt.m_128405_("age", Config.getInstance().babyItemGrowUpTime);
            success("Baby is old enough to place now.", ctx);
         } else {
            fail("Hold a baby first.", ctx);
         }
      }

      return 0;
   }

   private static int forceFullHearts(CommandContext<CommandSourceStack> ctx) {
      Player player = ((CommandSourceStack)ctx.getSource()).m_230896_();
      if (player != null) {
         getLoadedVillagers(ctx).forEach(v -> v.getVillagerBrain().getMemoriesForPlayer(player).setHearts(1000));
      }

      return 0;
   }

   private static int restoreClearedVillagers(CommandContext<CommandSourceStack> ctx) {
      storedVillagers.forEach(
         tag -> EntityType.m_20642_(tag, ((CommandSourceStack)ctx.getSource()).m_81372_())
            .ifPresent(v -> ((CommandSourceStack)ctx.getSource()).m_81372_().m_7967_(v))
      );
      storedVillagers.clear();
      success("Restored cleared villagers.", ctx);
      return 0;
   }

   private static ArgumentBuilder<CommandSourceStack, ?> register(String name, com.mojang.brigadier.Command<CommandSourceStack> cmd) {
      return ((LiteralArgumentBuilder)Commands.m_82127_(name).requires(cs -> cs.m_6761_(2))).executes(cmd);
   }

   private static ArgumentBuilder<CommandSourceStack, ?> register(String name) {
      return Commands.m_82127_(name).requires(cs -> cs.m_6761_(2));
   }

   private static int clearLoadedVillagers(CommandContext<CommandSourceStack> ctx) {
      storedVillagers.clear();
      getLoadedVillagers(ctx).forEach(v -> {
         CompoundTag tag = new CompoundTag();
         if (v.m_20086_(tag)) {
            storedVillagers.add(tag);
            v.m_146870_();
         }
      });
      success("Removed loaded villagers.", ctx);
      return 0;
   }

   private static Stream<VillagerEntityMCA> getLoadedVillagers(CommandContext<CommandSourceStack> ctx) {
      ServerLevel world = ((CommandSourceStack)ctx.getSource()).m_81372_();
      return Stream.<Object>concat(
            world.m_143280_((EntityTypeTest)EntitiesMCA.FEMALE_VILLAGER.get(), x -> true).stream(),
            world.m_143280_((EntityTypeTest)EntitiesMCA.MALE_VILLAGER.get(), x -> true).stream()
         )
         .map(VillagerEntityMCA.class::cast);
   }

   private static void success(String message, CommandContext<CommandSourceStack> ctx, Object... events) {
      ((CommandSourceStack)ctx.getSource()).m_288197_(() -> message(message, ChatFormatting.GREEN, events), true);
   }

   private static void fail(String message, CommandContext<CommandSourceStack> ctx, Object... events) {
      ((CommandSourceStack)ctx.getSource()).m_81352_(message(message, ChatFormatting.RED, events));
   }

   private static Component message(String message, ChatFormatting red, Object[] events) {
      MutableComponent data = Component.m_237113_(message).m_130940_(red);

      for (Object evt : events) {
         if (evt instanceof ClickEvent clickEvent) {
            data.m_130938_(style -> style.m_131142_(clickEvent));
         }

         if (evt instanceof HoverEvent hoverEvent) {
            data.m_130938_(style -> style.m_131144_(hoverEvent));
         }
      }

      return data;
   }

   private static int displayHelp(CommandContext<CommandSourceStack> ctx) {
      Entity player = ((CommandSourceStack)ctx.getSource()).m_81373_();
      if (player == null) {
         return 0;
      }

      sendMessage(player, ChatFormatting.DARK_RED + "--- " + ChatFormatting.GOLD + "OP COMMANDS" + ChatFormatting.DARK_RED + " ---");
      sendMessage(
         player,
         ChatFormatting.WHITE
            + " /mca-admin forceBuildingType id "
            + ChatFormatting.GOLD
            + " - Force a building's type. "
            + ChatFormatting.RED
            + "(Must be a valid building type)"
      );
      sendMessage(player, ChatFormatting.WHITE + " /mca-admin forceFullHearts " + ChatFormatting.GOLD + " - Force all hearts on all villagers.");
      sendMessage(player, ChatFormatting.WHITE + " /mca-admin forceBabyGrowth " + ChatFormatting.GOLD + " - Force your baby to grow up.");
      sendMessage(player, ChatFormatting.WHITE + " /mca-admin forceChildGrowth " + ChatFormatting.GOLD + " - Force nearby children to grow.");
      sendMessage(
         player,
         ChatFormatting.WHITE
            + " /mca-admin clearLoadedVillagers "
            + ChatFormatting.GOLD
            + " - Clear all loaded villagers. "
            + ChatFormatting.RED
            + "(IRREVERSIBLE)"
      );
      sendMessage(player, ChatFormatting.WHITE + " /mca-admin restoreClearedVillagers " + ChatFormatting.GOLD + " - Restores cleared villagers. ");
      sendMessage(player, ChatFormatting.WHITE + " /mca-admin listVillages " + ChatFormatting.GOLD + " - Prints a list of all villages.");
      sendMessage(player, ChatFormatting.WHITE + " /mca-admin removeVillage id" + ChatFormatting.GOLD + " - Removed a village with given ID.");
      sendMessage(
         player, ChatFormatting.WHITE + " /mca-admin convertVanillaVillagers radius" + ChatFormatting.GOLD + " - Convert vanilla villagers in the given radius"
      );
      sendMessage(player, ChatFormatting.WHITE + " /mca-admin incrementHearts " + ChatFormatting.GOLD + " - Increase hearts by 10.");
      sendMessage(player, ChatFormatting.WHITE + " /mca-admin decrementHearts " + ChatFormatting.GOLD + " - Decrease hearts by 10.");
      sendMessage(player, ChatFormatting.WHITE + " /mca-admin resetPlayerData " + ChatFormatting.GOLD + " - Resets genetics.");
      sendMessage(player, ChatFormatting.WHITE + " /mca-admin resetMarriage " + ChatFormatting.GOLD + " - Resets your marriage.");
      sendMessage(player, ChatFormatting.WHITE + " /mca-admin listVillages " + ChatFormatting.GOLD + " - List all known villages.");
      sendMessage(player, ChatFormatting.WHITE + " /mca-admin removeVillage " + ChatFormatting.GOLD + " - Remove a given village.");
      sendMessage(player, ChatFormatting.DARK_RED + "--- " + ChatFormatting.GOLD + "GLOBAL COMMANDS" + ChatFormatting.DARK_RED + " ---");
      sendMessage(player, ChatFormatting.WHITE + " /mca-admin help " + ChatFormatting.GOLD + " - Shows this list of commands.");
      return 0;
   }

   private static void sendMessage(Entity commandSender, String message) {
      commandSender.m_213846_(Component.m_237113_(ChatFormatting.GOLD + "[MCA] " + ChatFormatting.RESET + message));
   }
}
