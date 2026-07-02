package forge.net.mca.forge;

import forge.net.mca.MCA;
import forge.net.mca.MCAClient;
import forge.net.mca.server.ServerInteractionManager;
import forge.net.mca.server.command.AdminCommand;
import forge.net.mca.server.command.Command;
import forge.net.mca.server.world.data.VillageManager;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = "mca")
public class ForgeBusEvents {
   @SubscribeEvent
   public static void onCommandRegister(RegisterCommandsEvent event) {
      AdminCommand.register(event.getDispatcher());
      Command.register(event.getDispatcher());
   }

   @SubscribeEvent
   public static void onWorldTick(LevelTickEvent event) {
      if (!event.level.f_46443_ && event.side == LogicalSide.SERVER && event.phase == Phase.END) {
         VillageManager.get((ServerLevel)event.level).tick();
      }
   }

   @SubscribeEvent
   public static void onServerTick(ServerTickEvent event) {
      if (event.side == LogicalSide.SERVER && event.phase == Phase.END) {
         ServerInteractionManager.getInstance().tick();
      }

      MCA.setServer(event.getServer());
   }

   @SubscribeEvent
   public static void OnEntityJoinWorldEvent(EntityJoinLevelEvent event) {
      if (event.getEntity().m_9236_().f_46443_
         && (Minecraft.m_91087_().f_91074_ == null || event.getEntity().m_20148_().equals(Minecraft.m_91087_().f_91074_.m_20148_()))) {
         MCAClient.onLogin();
      }
   }

   @SubscribeEvent
   public static void onPlayerLoggedInEvent(PlayerLoggedInEvent event) {
      ServerInteractionManager.getInstance().onPlayerJoin((ServerPlayer)event.getEntity());
   }

   @SubscribeEvent
   public static void onParticleFactoryRegistration(ClientTickEvent event) {
      MCAClient.tickClient(Minecraft.m_91087_());
   }
}
