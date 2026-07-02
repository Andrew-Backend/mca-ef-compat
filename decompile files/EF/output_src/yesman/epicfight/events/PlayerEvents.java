package yesman.epicfight.events;

import java.io.File;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent.Start;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent.Stop;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.entity.player.PlayerEvent.LoadFromFile;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerEvent.StopTracking;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.common.AnimatorControlPacket;
import yesman.epicfight.network.server.SPAbsorption;
import yesman.epicfight.network.server.SPAnimatorControl;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.eventlistener.ItemUseEndEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.RightClickItemEvent;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

@EventBusSubscriber(modid = "epicfight")
public class PlayerEvents {
   @SubscribeEvent
   public static void arrowLoose(ArrowLooseEvent event) {
      EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), PlayerPatch.class)
         .ifPresent(
            playerpatch -> {
               if (playerpatch.isLogicalClient()) {
                  playerpatch.<Animator>getAnimator().playShootingAnimation();
               } else {
                  EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(
                     new SPAnimatorControl(AnimatorControlPacket.Action.SHOT, -1, event.getEntity().m_19879_(), 0.0F, false), event.getEntity()
                  );
               }
            }
         );
   }

   @SubscribeEvent
   public static void startTrackingEvent(StartTracking event) {
      if (event.getTarget() instanceof LivingEntity livingEntity && livingEntity.m_6103_() > 0.0F) {
         EpicFightNetworkManager.sendToPlayer(new SPAbsorption(event.getTarget().m_19879_(), livingEntity.m_6103_()), (ServerPlayer)event.getEntity());
      }

      EpicFightCapabilities.getUnparameterizedEntityPatch(event.getTarget(), EntityPatch.class)
         .ifPresent(entitypatch -> entitypatch.onStartTracking((ServerPlayer)event.getEntity()));
   }

   @SubscribeEvent
   public static void stopTrackingEvent(StopTracking event) {
      EpicFightCapabilities.getUnparameterizedEntityPatch(event.getTarget(), EntityPatch.class)
         .ifPresent(entitypatch -> entitypatch.onStopTracking((ServerPlayer)event.getEntity()));
   }

   @SubscribeEvent
   public static void playerLoadEvent(LoadFromFile event) {
      EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), ServerPlayerPatch.class)
         .ifPresent(
            playerpatch -> {
               File file = new File(event.getPlayerDirectory(), event.getPlayerUUID() + ".dat");
               if (!file.exists()) {
                  int initialMode = Math.min(
                     EpicFightGameRules.INITIAL_PLAYER_MODE.getRuleValue(event.getEntity().m_9236_()), PlayerPatch.PlayerMode.values().length - 1
                  );
                  playerpatch.toMode(PlayerPatch.PlayerMode.values()[initialMode], true);
               }
            }
         );
   }

   @SubscribeEvent
   public static void cloneEvent(Clone event) {
      event.getOriginal().reviveCaps();
      EpicFightCapabilities.getUnparameterizedEntityPatch(event.getOriginal(), ServerPlayerPatch.class)
         .ifPresent(oldCap -> EpicFightCapabilities.getPlayerPatchAsOptional(event.getEntity()).ifPresent(newCap -> {
            if (!event.isWasDeath() || EpicFightGameRules.KEEP_SKILLS.getRuleValue(event.getOriginal().m_9236_())) {
               newCap.copySkillsFrom(oldCap, event.isWasDeath());
            }

            newCap.toMode(oldCap.getPlayerMode(), false);
         }));
      event.getOriginal().invalidateCaps();
   }

   @SubscribeEvent
   public static void changeDimensionEvent(PlayerChangedDimensionEvent event) {
      EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), ServerPlayerPatch.class)
         .ifPresent(
            playerpatch -> {
               playerpatch.<Animator>getAnimator().resetLivingAnimations();
               playerpatch.modifyLivingMotionByCurrentItem(true);
               EpicFightNetworkManager.PayloadBundleBuilder packetBundleBuilder = EpicFightNetworkManager.PayloadBundleBuilder.create();
               playerpatch.getSkillCapability()
                  .listSkillContainers()
                  .filter(SkillContainer::hasSkill)
                  .forEach(skillContainer -> skillContainer.getSkill().onTracked(skillContainer, packetBundleBuilder));
               packetBundleBuilder.send((start, others) -> EpicFightNetworkManager.sendToPlayer(start, playerpatch.getOriginal(), others));
            }
         );
   }

   @SubscribeEvent
   public static void rightClickItemServerEvent(RightClickItem event) {
      if (event.getSide() != LogicalSide.CLIENT) {
         EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), ServerPlayerPatch.class)
            .ifPresent(
               playerpatch -> {
                  ItemStack itemstack = playerpatch.getOriginal().m_21206_();
                  if (!playerpatch.getEntityState().canUseItem()) {
                     event.setCanceled(true);
                  } else if (itemstack.m_41780_() == UseAnim.NONE
                     || !playerpatch.getHoldingItemCapability(InteractionHand.MAIN_HAND).getStyle(playerpatch).canUseOffhand()) {
                     boolean canceled = playerpatch.getEventListener()
                        .triggerEvents(PlayerEventListener.EventType.SERVER_ITEM_USE_EVENT, new RightClickItemEvent<>(playerpatch));
                     if (playerpatch.getEntityState().movementLocked()) {
                        canceled = true;
                     }

                     event.setCanceled(canceled);
                  }
               }
            );
      }
   }

   @SubscribeEvent
   public static void itemUseStartEvent(Start event) {
      EpicFightCapabilities.getPlayerPatchAsOptional(event.getEntity())
         .ifPresent(
            playerpatch -> {
               InteractionHand hand = playerpatch.getOriginal().m_21120_(InteractionHand.MAIN_HAND).equals(event.getItem())
                  ? InteractionHand.MAIN_HAND
                  : InteractionHand.OFF_HAND;
               CapabilityItem itemCap = playerpatch.getHoldingItemCapability(hand);
               if (!playerpatch.getEntityState().canUseItem()) {
                  event.setCanceled(true);
               } else if (event.getItem() == playerpatch.getOriginal().m_21206_()
                  && !playerpatch.getHoldingItemCapability(InteractionHand.MAIN_HAND).getStyle(playerpatch).canUseOffhand()) {
                  event.setCanceled(true);
               }

               if (itemCap.getUseAnimation(playerpatch) == UseAnim.BLOCK) {
                  event.setDuration(event.getItem().m_41779_());
               }
            }
         );
   }

   @SubscribeEvent
   public static void itemUseStopEvent(Stop event) {
      EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), ServerPlayerPatch.class)
         .ifPresent(
            playerpatch -> {
               boolean canceled = playerpatch.getEventListener()
                  .triggerEvents(PlayerEventListener.EventType.SERVER_ITEM_STOP_EVENT, new ItemUseEndEvent(playerpatch, event));
               event.setCanceled(canceled);
            }
         );
   }

   public static boolean fakePlayerCheck(Player source) {
      return source instanceof FakePlayer;
   }
}
