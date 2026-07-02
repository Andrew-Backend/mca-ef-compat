package yesman.epicfight.client.events;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.Clone;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.minecraftforge.client.event.ScreenEvent.MouseButtonPressed.Pre;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.data.reloader.ItemCapabilityReloadListener;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponTypeReloadListener;
import yesman.epicfight.world.capabilities.provider.EntityPatchProvider;
import yesman.epicfight.world.capabilities.provider.ItemCapabilityProvider;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.RightClickItemEvent;
import yesman.epicfight.world.gamerule.EpicFightGameRules;
import yesman.epicfight.world.level.block.FractureBlockState;

@EventBusSubscriber(modid = "epicfight", value = Dist.CLIENT)
public class ClientEvents {
   private static final Pair<ResourceLocation, ResourceLocation> OFFHAND_TEXTURE = Pair.of(InventoryMenu.f_39692_, InventoryMenu.f_39697_);
   private static final Minecraft MINECRAFT = Minecraft.m_91087_();
   @Deprecated
   public static ClientboundRespawnPacket packet;

   @SubscribeEvent
   public static void mouseClickEvent(Pre event) {
      if (event.getScreen() instanceof AbstractContainerScreen) {
         Slot slot = ((AbstractContainerScreen)event.getScreen()).getSlotUnderMouse();
         if (slot != null) {
            CapabilityItem cap = EpicFightCapabilities.getItemStackCapability(MINECRAFT.f_91074_.f_36096_.m_142621_());
            if (!cap.canBePlacedOffhand() && slot.m_7543_() != null && slot.m_7543_().equals(OFFHAND_TEXTURE)) {
               event.setCanceled(true);
            }
         }
      }
   }

   @SubscribeEvent
   public static void mouseReleaseEvent(net.minecraftforge.client.event.ScreenEvent.MouseButtonReleased.Pre event) {
      if (event.getScreen() instanceof AbstractContainerScreen) {
         Slot slot = ((AbstractContainerScreen)event.getScreen()).getSlotUnderMouse();
         if (slot != null) {
            CapabilityItem cap = EpicFightCapabilities.getItemStackCapability(MINECRAFT.f_91074_.f_36096_.m_142621_());
            if (!cap.canBePlacedOffhand() && slot.m_7543_() != null && slot.m_7543_().equals(OFFHAND_TEXTURE)) {
               event.setCanceled(true);
            }
         }
      }
   }

   @SubscribeEvent
   public static void presssKeyInGui(net.minecraftforge.client.event.ScreenEvent.KeyPressed.Pre event) {
      CapabilityItem itemCapability = CapabilityItem.EMPTY;
      if (event.getKeyCode() == MINECRAFT.f_91066_.f_92093_.getKey().m_84873_()) {
         if (event.getScreen() instanceof AbstractContainerScreen) {
            Slot slot = ((AbstractContainerScreen)event.getScreen()).getSlotUnderMouse();
            if (slot != null && slot.m_6657_()) {
               itemCapability = EpicFightCapabilities.getItemStackCapability(slot.m_7993_());
               if (!itemCapability.canBePlacedOffhand()) {
                  event.setCanceled(true);
               }
            }
         }
      } else if (event.getKeyCode() >= 49 && event.getKeyCode() <= 57 && event.getScreen() instanceof AbstractContainerScreen) {
         Slot slot = ((AbstractContainerScreen)event.getScreen()).getSlotUnderMouse();
         if (slot != null && slot.m_7543_() != null && slot.m_7543_().equals(OFFHAND_TEXTURE)) {
            itemCapability = EpicFightCapabilities.getItemStackCapability(MINECRAFT.f_91074_.m_150109_().m_8020_(event.getKeyCode() - 49));
            if (!itemCapability.canBePlacedOffhand()) {
               event.setCanceled(true);
            }
         }
      }
   }

   @SubscribeEvent
   public static void rightClickItemClient(RightClickItem event) {
      if (event.getSide() != LogicalSide.SERVER) {
         EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), LocalPlayerPatch.class)
            .ifPresent(
               playerpatch -> {
                  if (!playerpatch.getEntityState().canUseItem()) {
                     event.setCanceled(true);
                  } else if (playerpatch.getOriginal().m_21206_().m_41780_() == UseAnim.NONE) {
                     boolean canceled = playerpatch.getEventListener()
                        .triggerEvents(PlayerEventListener.EventType.CLIENT_ITEM_USE_EVENT, new RightClickItemEvent<>(playerpatch));
                     if (playerpatch.getEntityState().movementLocked()) {
                        canceled = true;
                     }

                     event.setCanceled(canceled);
                  }

                  if (!event.isCanceled()) {
                     EpicFightCameraAPI.getInstance().onItemUseEvent(event.getEntity(), playerpatch, event.getItemStack(), event.getHand());
                  }
               }
            );
      }
   }

   @SubscribeEvent
   public static void clientLoggingInEvent(LoggingIn event) {
      EpicFightCapabilities.getUnparameterizedEntityPatch(event.getPlayer(), LocalPlayerPatch.class)
         .ifPresent(ClientEngine.getInstance().controlEngine::setPlayerPatch);
      ClientEngine.getInstance().renderEngine.initHUD();
   }

   @SubscribeEvent
   public static void clientRespawnEvent(Clone event) {
      LocalPlayerPatch oldCap = EpicFightCapabilities.getEntityPatch(event.getOldPlayer(), LocalPlayerPatch.class);
      LocalPlayerPatch newCap = EpicFightCapabilities.getEntityPatch(event.getNewPlayer(), LocalPlayerPatch.class);
      if (oldCap != null && newCap != null) {
         if (packet != null && packet.m_263558_((byte)3)) {
            event.getNewPlayer().f_19797_ = event.getOldPlayer().f_19797_;
            newCap.copySkillsFrom(oldCap, false);
         }

         packet = null;
         newCap.onRespawnLocalPlayer(event);
         newCap.toMode(oldCap.getPlayerMode(), false);
      }

      EpicFightGameRules.GAME_RULES.values().forEach(gamerule -> {
         Object val = gamerule.getRuleValue(event.getOldPlayer().m_9236_());
         ((EpicFightGameRules.ConfigurableGameRule<Object, ?, ?>)gamerule).setRuleValue(event.getNewPlayer().m_9236_(), val);
      });
      ClientEngine.getInstance().controlEngine.setPlayerPatch(newCap);
      ClientEngine.getInstance().renderEngine.initHUD();
   }

   @SubscribeEvent
   public static void clientLogoutEvent(LoggingOut event) {
      if (event.getPlayer() != null) {
         ItemCapabilityReloadListener.reset();
         ItemCapabilityProvider.clear();
         EntityPatchProvider.clear();
         WeaponTypeReloadListener.clear();
         ClientEngine.getInstance().renderEngine.clear();
         FractureBlockState.reset();
      }
   }
}
