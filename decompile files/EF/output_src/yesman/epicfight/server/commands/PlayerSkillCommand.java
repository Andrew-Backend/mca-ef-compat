package yesman.epicfight.server.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPClearSkills;
import yesman.epicfight.network.server.SPRemoveSkillAndLearn;
import yesman.epicfight.server.commands.arguments.SkillArgument;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class PlayerSkillCommand {
   private static final SimpleCommandExceptionType ERROR_ADD_FAILED = new SimpleCommandExceptionType(Component.m_237115_("commands.epicfight.skill.add.failed"));
   private static final SimpleCommandExceptionType ERROR_REMOVE_FAILED = new SimpleCommandExceptionType(
      Component.m_237115_("commands.epicfight.skill.remove.failed")
   );
   private static final SimpleCommandExceptionType ERROR_CLEAR_FAILED = new SimpleCommandExceptionType(
      Component.m_237115_("commands.epicfight.skill.clear.failed")
   );

   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      RequiredArgumentBuilder<CommandSourceStack, EntitySelector> addCommandBuilder = Commands.m_82129_("targets", EntityArgument.m_91470_());
      RequiredArgumentBuilder<CommandSourceStack, EntitySelector> removeCommandBuilder = Commands.m_82129_("targets", EntityArgument.m_91470_());

      for (SkillSlot skillSlot : SkillSlot.ENUM_MANAGER.universalValues()) {
         if (skillSlot.category().learnable()) {
            addCommandBuilder.then(
               Commands.m_82127_(skillSlot.toString().toLowerCase(Locale.ROOT))
                  .then(
                     Commands.m_82129_("skill", SkillArgument.skill())
                        .executes(
                           commandContext -> addSkill(
                              (CommandSourceStack)commandContext.getSource(),
                              EntityArgument.m_91477_(commandContext, "targets"),
                              skillSlot,
                              SkillArgument.getSkill(commandContext, "skill")
                           )
                        )
                  )
            );
            removeCommandBuilder.then(
               ((LiteralArgumentBuilder)Commands.m_82127_(skillSlot.toString().toLowerCase(Locale.ROOT))
                     .executes(
                        commandContext -> removeSkill(
                           (CommandSourceStack)commandContext.getSource(), EntityArgument.m_91477_(commandContext, "targets"), skillSlot, null
                        )
                     ))
                  .then(
                     Commands.m_82129_("skill", SkillArgument.skill())
                        .executes(
                           commandContext -> removeSkill(
                              (CommandSourceStack)commandContext.getSource(),
                              EntityArgument.m_91477_(commandContext, "targets"),
                              skillSlot,
                              SkillArgument.getSkill(commandContext, "skill")
                           )
                        )
                  )
            );
         }
      }

      LiteralArgumentBuilder<CommandSourceStack> builder = (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_(
                     "skill"
                  )
                  .requires(commandSourceStack -> commandSourceStack.m_6761_(2)))
               .then(
                  ((LiteralArgumentBuilder)Commands.m_82127_("clear")
                        .executes(
                           commandContext -> clearSkill(
                              (CommandSourceStack)commandContext.getSource(), ImmutableList.of(((CommandSourceStack)commandContext.getSource()).m_81375_())
                           )
                        ))
                     .then(
                        Commands.m_82129_("targets", EntityArgument.m_91470_())
                           .executes(
                              commandContext -> clearSkill((CommandSourceStack)commandContext.getSource(), EntityArgument.m_91477_(commandContext, "targets"))
                           )
                     )
               ))
            .then(Commands.m_82127_("add").then(addCommandBuilder)))
         .then(Commands.m_82127_("remove").then(removeCommandBuilder));
      dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_("epicfight").then(builder));
   }

   public static int clearSkill(CommandSourceStack commandSourceStack, Collection<? extends ServerPlayer> targets) throws CommandSyntaxException {
      int i = 0;

      for (ServerPlayer player : targets) {
         EpicFightCapabilities.getUnparameterizedEntityPatch(player, ServerPlayerPatch.class).ifPresent(playerpatch -> {
            playerpatch.getSkillCapability().clearContainersAndLearnedSkills(true);
            SPClearSkills clearpacket = new SPClearSkills(player.m_19879_());
            EpicFightNetworkManager.sendToPlayer(clearpacket, player);
            EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(clearpacket, player);
         });
         i++;
      }

      if (i > 0) {
         if (i == 1) {
            commandSourceStack.m_288197_(
               wrap(Component.m_237110_("commands.epicfight.skill.clear.success.single", new Object[]{targets.iterator().next().m_5446_()})), true
            );
         } else {
            commandSourceStack.m_288197_(wrap(Component.m_237110_("commands.epicfight.skill.clear.success.multiple", new Object[]{i})), true);
         }

         return i;
      } else {
         throw ERROR_CLEAR_FAILED.create();
      }
   }

   public static int addSkill(CommandSourceStack commandSourceStack, Collection<? extends ServerPlayer> targets, SkillSlot slot, Skill skill) throws CommandSyntaxException {
      int i = 0;

      for (ServerPlayer player : targets) {
         ServerPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
         SkillContainer skillContainer = playerpatch.getSkillCapability().getSkillContainerFor(slot);
         if (skillContainer.setSkill(skill)) {
            if (skill.getCategory().learnable()) {
               playerpatch.getSkillCapability().addLearnedSkill(skill);
            }

            EpicFightNetworkManager.sendToPlayer(skillContainer.createSyncPacketToLocalPlayer(), player);
            EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(skillContainer.createSyncPacketToRemotePlayer(), player);
            i++;
         }
      }

      if (i > 0) {
         if (i == 1) {
            commandSourceStack.m_288197_(
               wrap(
                  Component.m_237110_("commands.epicfight.skill.add.success.single", new Object[]{skill.getDisplayName(), targets.iterator().next().m_5446_()})
               ),
               true
            );
         } else {
            commandSourceStack.m_288197_(
               wrap(Component.m_237110_("commands.epicfight.skill.add.success.multiple", new Object[]{skill.getDisplayName(), i})), true
            );
         }

         return i;
      } else {
         throw ERROR_ADD_FAILED.create();
      }
   }

   public static int removeSkill(CommandSourceStack commandSourceStack, Collection<? extends ServerPlayer> targets, SkillSlot slot, Skill skill) throws CommandSyntaxException {
      int i = 0;

      for (ServerPlayer player : targets) {
         ServerPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
         if (playerpatch != null) {
            if (skill == null) {
               SkillContainer skillContainer = playerpatch.getSkill(slot);
               skill = skillContainer.getSkill();
               if (skill != null) {
                  skillContainer.setSkill(null);
                  EpicFightNetworkManager.sendToPlayer(new SPRemoveSkillAndLearn(slot, skill), player);
                  EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(skillContainer.createSyncPacketToRemotePlayer(), player);
                  i++;
               }
            } else if (playerpatch.getSkillCapability().removeLearnedSkill(skill)) {
               SkillContainer skillContainer = playerpatch.getSkill(slot);
               if (skillContainer.getSkill() == skill) {
                  skillContainer.setSkill(null);
                  EpicFightNetworkManager.sendToPlayer(new SPRemoveSkillAndLearn(slot, skill), player);
                  EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(skillContainer.createSyncPacketToRemotePlayer(), player);
                  i++;
               }
            }
         }
      }

      if (i > 0) {
         if (i == 1) {
            commandSourceStack.m_288197_(
               wrap(
                  Component.m_237110_(
                     "commands.epicfight.skill.remove.success.single", new Object[]{skill.getDisplayName(), targets.iterator().next().m_5446_()}
                  )
               ),
               true
            );
         } else {
            commandSourceStack.m_288197_(
               wrap(Component.m_237110_("commands.epicfight.skill.remove.success.multiple", new Object[]{skill.getDisplayName(), i})), true
            );
         }

         return i;
      } else {
         throw ERROR_REMOVE_FAILED.create();
      }
   }

   private static <T> Supplier<T> wrap(T value) {
      return () -> value;
   }
}
