package fabric.net.mca.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import fabric.net.mca.Config;
import fabric.net.mca.entity.EntitiesMCA;
import fabric.net.mca.entity.VillagerEntityMCA;
import fabric.net.mca.entity.ai.relationship.RelationshipState;
import fabric.net.mca.item.BabyItem;
import fabric.net.mca.server.SpawnQueue;
import fabric.net.mca.server.world.data.Building;
import fabric.net.mca.server.world.data.FamilyTree;
import fabric.net.mca.server.world.data.FamilyTreeNode;
import fabric.net.mca.server.world.data.PlayerSaveData;
import fabric.net.mca.server.world.data.Village;
import fabric.net.mca.server.world.data.VillageManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_156;
import net.minecraft.class_1646;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2338;
import net.minecraft.class_2487;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_2568;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_5242;
import net.minecraft.class_5250;
import net.minecraft.class_5575;
import net.minecraft.class_2558.class_2559;
import net.minecraft.class_2568.class_5247;

public class AdminCommand {
   private static final List<class_2487> storedVillagers = new ArrayList<>();

   public static void register(CommandDispatcher<class_2168> dispatcher) {
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)class_2170.method_9247(
                                                                     "mca-admin"
                                                                  )
                                                                  .then(register("help", AdminCommand::displayHelp)))
                                                               .then(register("clearLoadedVillagers", AdminCommand::clearLoadedVillagers)))
                                                            .then(register("restoreClearedVillagers", AdminCommand::restoreClearedVillagers)))
                                                         .then(
                                                            register("forceBuildingType")
                                                               .then(
                                                                  class_2170.method_9244("type", StringArgumentType.string())
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
                                 register("assumeNameDead")
                                    .then(class_2170.method_9244("name", StringArgumentType.string()).executes(AdminCommand::assumeNameDead))
                              ))
                           .then(
                              register("assumeUuidDead").then(class_2170.method_9244("uuid", class_5242.method_27643()).executes(AdminCommand::assumeUuidDead))
                           ))
                        .then(
                           register("removeVillageWithId")
                              .then(class_2170.method_9244("id", IntegerArgumentType.integer()).executes(AdminCommand::removeVillageWithId))
                        ))
                     .then(
                        register("convertVanillaVillagers")
                           .then(class_2170.method_9244("radius", IntegerArgumentType.integer()).executes(AdminCommand::convertVanillaVillagers))
                     ))
                  .then(register("removeVillage").then(class_2170.method_9244("name", StringArgumentType.string()).executes(AdminCommand::removeVillage))))
               .then(
                  register("buildingProcessingRate")
                     .then(class_2170.method_9244("cooldown", IntegerArgumentType.integer()).executes(AdminCommand::buildingProcessingRate))
               ))
            .requires(serverCommandSource -> serverCommandSource.method_9259(2))
      );
   }

   private static int listVillages(CommandContext<class_2168> ctx) {
      for (Village village : VillageManager.get(((class_2168)ctx.getSource()).method_9225())) {
         class_2338 pos = village.getBox().method_22874();
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
            new class_2568(class_5247.field_24342, class_2561.method_43471("chat.coordinates.tooltip")),
            new class_2558(class_2559.field_11745, "/tp @s " + pos.method_10263() + " ~ " + pos.method_10260())
         );
      }

      return 0;
   }

   private static int assumeNameDead(CommandContext<class_2168> ctx) {
      String name = StringArgumentType.getString(ctx, "name");
      FamilyTree tree = FamilyTree.get(((class_2168)ctx.getSource()).method_9225());
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

   private static int assumeUuidDead(CommandContext<class_2168> ctx) {
      UUID uuid = class_5242.method_27645(ctx, "uuid");
      FamilyTree tree = FamilyTree.get(((class_2168)ctx.getSource()).method_9225());
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

   private static void assumeDead(CommandContext<class_2168> ctx, UUID uuid) {
      for (Village village : VillageManager.get(((class_2168)ctx.getSource()).method_9225())) {
         village.removeResident(uuid);
      }

      FamilyTree tree = FamilyTree.get(((class_2168)ctx.getSource()).method_9225());
      Optional<FamilyTreeNode> node = tree.getOrEmpty(uuid);
      node.filter(n -> n.partner() != null).ifPresent(n -> n.updatePartner(null, RelationshipState.WIDOW));
      ((class_2168)ctx.getSource()).method_9225().method_18456().forEach(player -> {
         PlayerSaveData playerData = PlayerSaveData.get(player);
         if (playerData.getPartnerUUID().orElse(class_156.field_25140).equals(uuid)) {
            playerData.endRelationShip(RelationshipState.SINGLE);
         }
      });
   }

   private static int removeVillageWithId(CommandContext<class_2168> ctx) {
      int id = IntegerArgumentType.getInteger(ctx, "id");
      if (VillageManager.get(((class_2168)ctx.getSource()).method_9225()).removeVillage(id)) {
         success("Village deleted.", ctx);
      } else {
         fail("Village with this ID does not exist.", ctx);
      }

      return 0;
   }

   private static int convertVanillaVillagers(CommandContext<class_2168> ctx) {
      int radius = IntegerArgumentType.getInteger(ctx, "radius");
      class_3218 world = ((class_2168)ctx.getSource()).method_9225();
      world.method_18198(class_1299.field_6077, x -> true).stream().map(class_1646.class::cast).forEach(v -> {
         if (v.method_5739(((class_2168)ctx.getSource()).method_9228()) < radius) {
            SpawnQueue.getInstance().convert(v);
         }
      });
      return 0;
   }

   private static int setBuildingType(CommandContext<class_2168> ctx, String type) {
      class_1657 player = ((class_2168)ctx.getSource()).method_44023();
      if (player == null) {
         return 0;
      }

      VillageManager villages = VillageManager.get(((class_2168)ctx.getSource()).method_9225());
      Optional<Village> village = villages.findNearestVillage(player);
      Optional<Building> building = village.flatMap(v -> v.getBuildings().values().stream().filter(b -> b.containsPos(player.method_24515())).findAny());
      if (building.isPresent()) {
         if (building.get().getType().equals(type)) {
            building.get().setTypeForced(false);
            building.get().determineType();
         } else {
            building.get().setTypeForced(true);
            building.get().setType(type);
         }
      } else {
         fail(class_2561.method_43471("blueprint.noBuilding").getString(), ctx);
      }

      return 0;
   }

   private static int forceBuildingType(CommandContext<class_2168> ctx) {
      return setBuildingType(ctx, StringArgumentType.getString(ctx, "type"));
   }

   private static int clearForcedBuildingType(CommandContext<class_2168> ctx) {
      return setBuildingType(ctx, null);
   }

   private static int removeVillage(CommandContext<class_2168> ctx) {
      String name = StringArgumentType.getString(ctx, "name");
      List<Village> collect = VillageManager.get(((class_2168)ctx.getSource()).method_9225()).findVillages(v -> v.getName().equals(name)).toList();
      if (collect.isEmpty()) {
         fail("No village with this name exists.", ctx);
      } else if (collect.size() > 1) {
         success("Village deleted.", ctx);
         fail("No village with this name exists.", ctx);
      } else if (VillageManager.get(((class_2168)ctx.getSource()).method_9225()).removeVillage(collect.get(0).getId())) {
         success("Village deleted.", ctx);
      } else {
         fail("Unknown error.", ctx);
      }

      return 0;
   }

   private static int buildingProcessingRate(CommandContext<class_2168> ctx) {
      int cooldown = IntegerArgumentType.getInteger(ctx, "cooldown");
      VillageManager.get(((class_2168)ctx.getSource()).method_9225()).setBuildingCooldown(cooldown);
      return 0;
   }

   private static int resetPlayerData(CommandContext<class_2168> ctx) {
      class_3222 player = ((class_2168)ctx.getSource()).method_44023();
      if (player == null) {
         return 0;
      }

      PlayerSaveData playerData = PlayerSaveData.get(player);
      playerData.reset();
      success("Player data reset.", ctx);
      return 0;
   }

   private static int resetMarriage(CommandContext<class_2168> ctx) {
      class_3222 player = ((class_2168)ctx.getSource()).method_44023();
      if (player == null) {
         return 0;
      }

      PlayerSaveData playerData = PlayerSaveData.get(player);
      playerData.endRelationShip(RelationshipState.SINGLE);
      success("Marriage reset.", ctx);
      return 0;
   }

   private static int decrementHearts(CommandContext<class_2168> ctx) {
      class_1657 player = ((class_2168)ctx.getSource()).method_44023();
      if (player == null) {
         return 0;
      }

      getLoadedVillagers(ctx).forEach(v -> v.getVillagerBrain().getMemoriesForPlayer(player).modHearts(-10));
      return 0;
   }

   private static int incrementHearts(CommandContext<class_2168> ctx) {
      class_1657 player = ((class_2168)ctx.getSource()).method_44023();
      if (player == null) {
         return 0;
      }

      getLoadedVillagers(ctx).forEach(v -> v.getVillagerBrain().getMemoriesForPlayer(player).modHearts(10));
      return 0;
   }

   private static int forceChildGrowth(CommandContext<class_2168> ctx) {
      getLoadedVillagers(ctx).forEach(v -> v.method_5614(0));
      return 0;
   }

   private static int forceBabyGrowth(CommandContext<class_2168> ctx) {
      class_1657 player = ((class_2168)ctx.getSource()).method_44023();
      if (player != null) {
         class_1799 heldStack = player.method_6047();
         if (heldStack.method_7909() instanceof BabyItem) {
            class_2487 nbt = BabyItem.getBabyNbt(heldStack);
            nbt.method_10569("age", Config.getInstance().babyItemGrowUpTime);
            success("Baby is old enough to place now.", ctx);
         } else {
            fail("Hold a baby first.", ctx);
         }
      }

      return 0;
   }

   private static int forceFullHearts(CommandContext<class_2168> ctx) {
      class_1657 player = ((class_2168)ctx.getSource()).method_44023();
      if (player != null) {
         getLoadedVillagers(ctx).forEach(v -> v.getVillagerBrain().getMemoriesForPlayer(player).setHearts(1000));
      }

      return 0;
   }

   private static int restoreClearedVillagers(CommandContext<class_2168> ctx) {
      storedVillagers.forEach(
         tag -> class_1299.method_5892(tag, ((class_2168)ctx.getSource()).method_9225())
            .ifPresent(v -> ((class_2168)ctx.getSource()).method_9225().method_8649(v))
      );
      storedVillagers.clear();
      success("Restored cleared villagers.", ctx);
      return 0;
   }

   private static ArgumentBuilder<class_2168, ?> register(String name, com.mojang.brigadier.Command<class_2168> cmd) {
      return ((LiteralArgumentBuilder)class_2170.method_9247(name).requires(cs -> cs.method_9259(2))).executes(cmd);
   }

   private static ArgumentBuilder<class_2168, ?> register(String name) {
      return class_2170.method_9247(name).requires(cs -> cs.method_9259(2));
   }

   private static int clearLoadedVillagers(CommandContext<class_2168> ctx) {
      storedVillagers.clear();
      getLoadedVillagers(ctx).forEach(v -> {
         class_2487 tag = new class_2487();
         if (v.method_5786(tag)) {
            storedVillagers.add(tag);
            v.method_31472();
         }
      });
      success("Removed loaded villagers.", ctx);
      return 0;
   }

   private static Stream<VillagerEntityMCA> getLoadedVillagers(CommandContext<class_2168> ctx) {
      class_3218 world = ((class_2168)ctx.getSource()).method_9225();
      return Stream.<Object>concat(
            world.method_18198((class_5575)EntitiesMCA.FEMALE_VILLAGER.get(), x -> true).stream(),
            world.method_18198((class_5575)EntitiesMCA.MALE_VILLAGER.get(), x -> true).stream()
         )
         .map(VillagerEntityMCA.class::cast);
   }

   private static void success(String message, CommandContext<class_2168> ctx, Object... events) {
      ((class_2168)ctx.getSource()).method_9226(() -> message(message, class_124.field_1060, events), true);
   }

   private static void fail(String message, CommandContext<class_2168> ctx, Object... events) {
      ((class_2168)ctx.getSource()).method_9213(message(message, class_124.field_1061, events));
   }

   private static class_2561 message(String message, class_124 red, Object[] events) {
      class_5250 data = class_2561.method_43470(message).method_27692(red);

      for (Object evt : events) {
         if (evt instanceof class_2558 clickEvent) {
            data.method_27694(style -> style.method_10958(clickEvent));
         }

         if (evt instanceof class_2568 hoverEvent) {
            data.method_27694(style -> style.method_10949(hoverEvent));
         }
      }

      return data;
   }

   private static int displayHelp(CommandContext<class_2168> ctx) {
      class_1297 player = ((class_2168)ctx.getSource()).method_9228();
      if (player == null) {
         return 0;
      }

      sendMessage(player, class_124.field_1079 + "--- " + class_124.field_1065 + "OP COMMANDS" + class_124.field_1079 + " ---");
      sendMessage(
         player,
         class_124.field_1068
            + " /mca-admin forceBuildingType id "
            + class_124.field_1065
            + " - Force a building's type. "
            + class_124.field_1061
            + "(Must be a valid building type)"
      );
      sendMessage(player, class_124.field_1068 + " /mca-admin forceFullHearts " + class_124.field_1065 + " - Force all hearts on all villagers.");
      sendMessage(player, class_124.field_1068 + " /mca-admin forceBabyGrowth " + class_124.field_1065 + " - Force your baby to grow up.");
      sendMessage(player, class_124.field_1068 + " /mca-admin forceChildGrowth " + class_124.field_1065 + " - Force nearby children to grow.");
      sendMessage(
         player,
         class_124.field_1068
            + " /mca-admin clearLoadedVillagers "
            + class_124.field_1065
            + " - Clear all loaded villagers. "
            + class_124.field_1061
            + "(IRREVERSIBLE)"
      );
      sendMessage(player, class_124.field_1068 + " /mca-admin restoreClearedVillagers " + class_124.field_1065 + " - Restores cleared villagers. ");
      sendMessage(player, class_124.field_1068 + " /mca-admin listVillages " + class_124.field_1065 + " - Prints a list of all villages.");
      sendMessage(player, class_124.field_1068 + " /mca-admin removeVillage id" + class_124.field_1065 + " - Removed a village with given ID.");
      sendMessage(
         player,
         class_124.field_1068 + " /mca-admin convertVanillaVillagers radius" + class_124.field_1065 + " - Convert vanilla villagers in the given radius"
      );
      sendMessage(player, class_124.field_1068 + " /mca-admin incrementHearts " + class_124.field_1065 + " - Increase hearts by 10.");
      sendMessage(player, class_124.field_1068 + " /mca-admin decrementHearts " + class_124.field_1065 + " - Decrease hearts by 10.");
      sendMessage(player, class_124.field_1068 + " /mca-admin resetPlayerData " + class_124.field_1065 + " - Resets genetics.");
      sendMessage(player, class_124.field_1068 + " /mca-admin resetMarriage " + class_124.field_1065 + " - Resets your marriage.");
      sendMessage(player, class_124.field_1068 + " /mca-admin listVillages " + class_124.field_1065 + " - List all known villages.");
      sendMessage(player, class_124.field_1068 + " /mca-admin removeVillage " + class_124.field_1065 + " - Remove a given village.");
      sendMessage(player, class_124.field_1079 + "--- " + class_124.field_1065 + "GLOBAL COMMANDS" + class_124.field_1079 + " ---");
      sendMessage(player, class_124.field_1068 + " /mca-admin help " + class_124.field_1065 + " - Shows this list of commands.");
      return 0;
   }

   private static void sendMessage(class_1297 commandSender, String message) {
      commandSender.method_43496(class_2561.method_43470(class_124.field_1065 + "[MCA] " + class_124.field_1070 + message));
   }
}
