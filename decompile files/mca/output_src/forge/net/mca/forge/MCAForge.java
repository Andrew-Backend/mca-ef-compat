package forge.net.mca.forge;

import dev.architectury.platform.forge.EventBuses;
import forge.net.mca.ParticleTypesMCA;
import forge.net.mca.SoundsMCA;
import forge.net.mca.TradeOffersMCA;
import forge.net.mca.advancement.criterion.CriterionMCA;
import forge.net.mca.block.BlocksMCA;
import forge.net.mca.entity.EntitiesMCA;
import forge.net.mca.entity.interaction.gifts.GiftLoader;
import forge.net.mca.forge.cobalt.network.NetworkHandlerImpl;
import forge.net.mca.item.ItemsMCA;
import forge.net.mca.network.MessagesMCA;
import forge.net.mca.resources.ApiReloadListener;
import forge.net.mca.resources.BuildingTypes;
import forge.net.mca.resources.ClothingList;
import forge.net.mca.resources.Dialogues;
import forge.net.mca.resources.HairList;
import forge.net.mca.resources.Names;
import forge.net.mca.resources.Tasks;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Arrays;
import java.util.List;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("mca")
@EventBusSubscriber(modid = "mca", bus = Bus.MOD)
public final class MCAForge {
   public MCAForge() {
      EventBuses.registerModEventBus("mca", FMLJavaModLoadingContext.get().getModEventBus());
      new NetworkHandlerImpl();
      MinecraftForge.EVENT_BUS.addListener(this::onAddReloadListener);
      MinecraftForge.EVENT_BUS.addListener(this::onVillagerTrades);
      BlocksMCA.bootstrap();
      ItemsMCA.bootstrap();
      SoundsMCA.bootstrap();
      ParticleTypesMCA.bootstrap();
      EntitiesMCA.bootstrap();
      MessagesMCA.bootstrap();
      CriterionMCA.bootstrap();
   }

   private void onAddReloadListener(AddReloadListenerEvent event) {
      event.addListener(new ApiReloadListener());
      event.addListener(new ClothingList());
      event.addListener(new HairList());
      event.addListener(new GiftLoader());
      event.addListener(new Dialogues());
      event.addListener(new Tasks());
      event.addListener(new Names());
      event.addListener(new BuildingTypes());
   }

   private void onVillagerTrades(VillagerTradesEvent event) {
      Int2ObjectMap<ItemListing[]> trades = TradeOffersMCA.createTradeMap().get(event.getType());
      if (trades != null) {
         trades.int2ObjectEntrySet().forEach(entry -> ((List)event.getTrades().get(entry.getIntKey())).addAll(Arrays.asList((ItemListing[])entry.getValue())));
      }
   }
}
