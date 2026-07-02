package yesman.epicfight.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.network.common.AnimatorControlPacket;
import yesman.epicfight.server.commands.arguments.AnimationArgument;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class AnimatorCommand {
   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(
         (LiteralArgumentBuilder)Commands.m_82127_("epicfight")
            .then(
               ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("animator")
                           .requires(commandSourceStack -> commandSourceStack.m_6761_(2)))
                        .then(
                           Commands.m_82127_("play")
                              .then(
                                 Commands.m_82129_("targets", EntityArgument.m_91460_())
                                    .then(
                                       ((RequiredArgumentBuilder)Commands.m_82129_("animation", AnimationArgument.animation())
                                             .executes(
                                                commandContext -> playAnimation(
                                                   EntityArgument.m_91461_(commandContext, "targets"),
                                                   AnimationArgument.getAnimation(commandContext, "animation"),
                                                   0.0F
                                                )
                                             ))
                                          .then(
                                             Commands.m_82129_("transitionTimeModifier", FloatArgumentType.floatArg())
                                                .executes(
                                                   commandContext -> playAnimation(
                                                      EntityArgument.m_91461_(commandContext, "targets"),
                                                      AnimationArgument.getAnimation(commandContext, "animation"),
                                                      FloatArgumentType.getFloat(commandContext, "transitionTimeModifier")
                                                   )
                                                )
                                          )
                                    )
                              )
                        ))
                     .then(
                        Commands.m_82127_("soft_pause")
                           .then(
                              Commands.m_82129_("targets", EntityArgument.m_91460_())
                                 .then(
                                    Commands.m_82129_("paused", BoolArgumentType.bool())
                                       .executes(
                                          commandContext -> softPause(
                                             EntityArgument.m_91461_(commandContext, "targets"), BoolArgumentType.getBool(commandContext, "paused")
                                          )
                                       )
                                 )
                           )
                     ))
                  .then(
                     Commands.m_82127_("hard_pause")
                        .then(
                           Commands.m_82129_("targets", EntityArgument.m_91460_())
                              .then(
                                 Commands.m_82129_("paused", BoolArgumentType.bool())
                                    .executes(
                                       commandContext -> hardPause(
                                          EntityArgument.m_91461_(commandContext, "targets"), BoolArgumentType.getBool(commandContext, "paused")
                                       )
                                    )
                              )
                        )
                  )
            )
      );
   }

   public static int playAnimation(
      Collection<? extends Entity> targetEntities, AnimationManager.AnimationAccessor<? extends StaticAnimation> animation, float transitionTimeModifier
   ) {
      int successEntityNum = 0;

      for (Entity entity : targetEntities) {
         LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
         if (entitypatch != null) {
            successEntityNum++;
            entitypatch.playAnimationSynchronized(animation, transitionTimeModifier);
         }
      }

      return successEntityNum;
   }

   public static int softPause(Collection<? extends Entity> targetEntities, boolean paused) {
      int successEntityNum = 0;

      for (Entity entity : targetEntities) {
         LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
         if (entitypatch != null) {
            successEntityNum++;
            entitypatch.pauseAnimator(AnimatorControlPacket.Action.SOFT_PAUSE, paused);
         }
      }

      return successEntityNum;
   }

   public static int hardPause(Collection<? extends Entity> targetEntities, boolean paused) {
      int successEntityNum = 0;

      for (Entity entity : targetEntities) {
         LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
         if (entitypatch != null) {
            successEntityNum++;
            entitypatch.pauseAnimator(AnimatorControlPacket.Action.HARD_PAUSE, paused);
         }
      }

      return successEntityNum;
   }
}
