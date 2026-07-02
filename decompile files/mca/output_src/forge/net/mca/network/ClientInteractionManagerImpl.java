package forge.net.mca.network;

import forge.net.mca.Config;
import forge.net.mca.MCAClient;
import forge.net.mca.client.book.Book;
import forge.net.mca.client.book.CivilRegistryBook;
import forge.net.mca.client.gui.BlueprintScreen;
import forge.net.mca.client.gui.CombScreen;
import forge.net.mca.client.gui.ExtendedBookScreen;
import forge.net.mca.client.gui.FamilyTreeScreen;
import forge.net.mca.client.gui.FamilyTreeSearchScreen;
import forge.net.mca.client.gui.InteractScreen;
import forge.net.mca.client.gui.LimitedVillagerEditorScreen;
import forge.net.mca.client.gui.NameBabyScreen;
import forge.net.mca.client.gui.NeedleScreen;
import forge.net.mca.client.gui.SkinListUpdateListener;
import forge.net.mca.client.gui.VillagerEditorScreen;
import forge.net.mca.client.gui.VillagerTrackerSearchScreen;
import forge.net.mca.client.gui.WhistleScreen;
import forge.net.mca.client.tts.SpeechManager;
import forge.net.mca.entity.EntitiesMCA;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.VillagerLike;
import forge.net.mca.item.BabyItem;
import forge.net.mca.item.ExtendedWrittenBookItem;
import forge.net.mca.network.s2c.AnalysisResults;
import forge.net.mca.network.s2c.BabyNameResponse;
import forge.net.mca.network.s2c.CivilRegistryResponse;
import forge.net.mca.network.s2c.ConfigResponse;
import forge.net.mca.network.s2c.CustomSkinsChangedMessage;
import forge.net.mca.network.s2c.FamilyTreeUUIDResponse;
import forge.net.mca.network.s2c.GetFamilyResponse;
import forge.net.mca.network.s2c.GetFamilyTreeResponse;
import forge.net.mca.network.s2c.GetInteractDataResponse;
import forge.net.mca.network.s2c.GetVillageFailedResponse;
import forge.net.mca.network.s2c.GetVillageResponse;
import forge.net.mca.network.s2c.GetVillagerResponse;
import forge.net.mca.network.s2c.InteractionDialogueQuestionResponse;
import forge.net.mca.network.s2c.InteractionDialogueResponse;
import forge.net.mca.network.s2c.OpenDestinyGuiRequest;
import forge.net.mca.network.s2c.OpenGuiRequest;
import forge.net.mca.network.s2c.PlayerDataMessage;
import forge.net.mca.network.s2c.ShowToastRequest;
import forge.net.mca.network.s2c.SkinListResponse;
import forge.net.mca.network.s2c.VillagerMessage;
import forge.net.mca.network.s2c.VillagerNameResponse;
import forge.net.mca.resources.BuildingTypes;
import forge.net.mca.server.world.data.Village;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.SystemToast.SystemToastIds;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

public class ClientInteractionManagerImpl implements ClientInteractionManager {
   private final Minecraft client = Minecraft.m_91087_();

   @Override
   public void handleGuiRequest(OpenGuiRequest message) {
      assert this.client.f_91073_ != null;
      assert Minecraft.m_91087_().f_91074_ != null;
      switch (message.getGui()) {
         case WHISTLE:
            this.client.m_91152_(new WhistleScreen());
            break;
         case BOOK:
            if (this.client.f_91074_ != null) {
               ItemStack item = this.client.f_91074_.m_21120_(InteractionHand.MAIN_HAND);
               if (item.m_41720_() instanceof ExtendedWrittenBookItem bookItem) {
                  Book book = bookItem.getBook(item);
                  this.client.m_91152_(new ExtendedBookScreen(book));
               }
            }
            break;
         case BLUEPRINT:
            this.client.m_91152_(new BlueprintScreen());
            break;
         case INTERACT:
            if (this.client.f_91074_ != null) {
               ItemStack item = this.client.f_91074_.m_21120_(InteractionHand.MAIN_HAND);
               boolean isOnBlacklist = Config.getInstance()
                  .villagerInteractionItemBlacklist
                  .contains(BuiltInRegistries.f_257033_.m_7981_(item.m_41720_()).toString());
               if (!isOnBlacklist) {
                  VillagerLike<?> villager = (VillagerLike<?>)this.client.f_91073_.m_6815_(message.villager);
                  this.client.m_91152_(new InteractScreen(villager));
               }
            }
            break;
         case VILLAGER_EDITOR:
            Entity entityxxx = this.client.f_91073_.m_6815_(message.villager);
            assert entityxxx != null;
            this.client.m_91152_(new VillagerEditorScreen(entityxxx.m_20148_(), Minecraft.m_91087_().f_91074_.m_20148_()));
            break;
         case LIMITED_VILLAGER_EDITOR:
            Entity entityxx = this.client.f_91073_.m_6815_(message.villager);
            assert entityxx != null;
            this.client.m_91152_(new LimitedVillagerEditorScreen(entityxx.m_20148_(), Minecraft.m_91087_().f_91074_.m_20148_()));
            break;
         case NEEDLE_AND_THREAD:
            Entity entityx = this.client.f_91073_.m_6815_(message.villager);
            if (entityx == null) {
               this.client.m_91152_(new NeedleScreen(Minecraft.m_91087_().f_91074_.m_20148_()));
            } else {
               this.client.m_91152_(new NeedleScreen(entityx.m_20148_(), Minecraft.m_91087_().f_91074_.m_20148_()));
            }
            break;
         case COMB:
            Entity entity = this.client.f_91073_.m_6815_(message.villager);
            if (entity == null) {
               this.client.m_91152_(new CombScreen(Minecraft.m_91087_().f_91074_.m_20148_()));
            } else {
               this.client.m_91152_(new CombScreen(entity.m_20148_(), Minecraft.m_91087_().f_91074_.m_20148_()));
            }
            break;
         case BABY_NAME:
            if (this.client.f_91074_ != null) {
               ItemStack item = this.client.f_91074_.m_21120_(InteractionHand.MAIN_HAND);
               if (item.m_41720_() instanceof BabyItem) {
                  this.client.m_91152_(new NameBabyScreen(this.client.f_91074_, item));
               }
            }
            break;
         case FAMILY_TREE:
            this.client.m_91152_(new FamilyTreeSearchScreen());
            break;
         case VILLAGER_TRACKER:
            this.client.m_91152_(new VillagerTrackerSearchScreen());
      }
   }

   @Override
   public void handleFamilyTreeResponse(GetFamilyTreeResponse message) {
      if (this.client.f_91080_ instanceof FamilyTreeScreen gui) {
         gui.setFamilyData(message.uuid, message.family);
      }
   }

   @Override
   public void handleInteractDataResponse(GetInteractDataResponse message) {
      if (this.client.f_91080_ instanceof InteractScreen gui) {
         gui.setConstraints(message.constraints);
         gui.setParents(message.father, message.mother);
         gui.setSpouse(message.marriageState, message.spouse);
      }
   }

   @Override
   public void handleVillageDataResponse(GetVillageResponse message) {
      if (this.client.f_91080_ instanceof BlueprintScreen gui) {
         BuildingTypes.getInstance().setBuildingTypes(message.buildingTypes);
         Village village = new Village(message.getData(), null);
         gui.setVillage(village);
         gui.setVillageData(message.rank, message.reputation, message.isVillage, message.ids, message.tasks);
      }
   }

   @Override
   public void handleVillageDataFailedResponse(GetVillageFailedResponse message) {
      if (this.client.f_91080_ instanceof BlueprintScreen gui) {
         gui.setVillage(null);
      }
   }

   @Override
   public void handleFamilyDataResponse(GetFamilyResponse message) {
      if (this.client.f_91080_ instanceof WhistleScreen gui) {
         gui.setVillagerData(message.getData());
      }
   }

   @Override
   public void handleVillagerDataResponse(GetVillagerResponse message) {
      if (this.client.f_91080_ instanceof VillagerEditorScreen gui) {
         gui.setVillagerData(message.getData());
      }
   }

   @Override
   public void handleDialogueResponse(InteractionDialogueResponse message) {
      if (this.client.f_91080_ instanceof InteractScreen gui) {
         gui.setDialogue(message.question, message.answers);
      }
   }

   @Override
   public void handleDialogueQuestionResponse(InteractionDialogueQuestionResponse message) {
      if (this.client.f_91080_ instanceof InteractScreen gui) {
         gui.setLastPhrase(message.getQuestionText(), message.silent);
      }
   }

   @Override
   public void handleSkinListResponse(AnalysisResults message) {
      InteractScreen.setAnalysis(message.analysis);
   }

   @Override
   public void handleBabyNameResponse(BabyNameResponse message) {
      if (this.client.f_91080_ instanceof NameBabyScreen gui) {
         gui.setBabyName(message.getName());
      }
   }

   @Override
   public void handleVillagerNameResponse(VillagerNameResponse message) {
      if (this.client.f_91080_ instanceof VillagerEditorScreen gui) {
         gui.setVillagerName(message.getName());
      }
   }

   @Override
   public void handleToastMessage(ShowToastRequest message) {
      SystemToast.m_94855_(this.client.m_91300_(), SystemToastIds.TUTORIAL_HINT, message.getTitle(), message.getMessage());
   }

   @Override
   public void handleFamilyTreeUUIDResponse(FamilyTreeUUIDResponse response) {
      if (this.client.f_91080_ instanceof FamilyTreeSearchScreen gui) {
         gui.setList(response.getList());
      }
   }

   @Override
   public void handlePlayerDataMessage(PlayerDataMessage response) {
      VillagerEntityMCA villager = (VillagerEntityMCA)((EntityType)EntitiesMCA.MALE_VILLAGER.get()).m_20615_(Minecraft.m_91087_().f_91073_);
      assert villager != null;
      villager.m_7378_(response.getData());
      MCAClient.addPlayerData(response.uuid, villager);
   }

   @Override
   public void handleSkinListResponse(SkinListResponse message) {
      Screen screen = this.client.f_91080_;
      VillagerEditorScreen.setSkinList(message.getClothing(), message.getHair());
      if (screen instanceof SkinListUpdateListener gui) {
         gui.skinListUpdatedCallback();
      }
   }

   @Override
   public void handleDestinyGuiRequest(OpenDestinyGuiRequest message) {
      MCAClient.getDestinyManager().requestOpen(message.allowTeleportation);
   }

   @Override
   public void handleConfigResponse(ConfigResponse message) {
      Config.setServerConfig(message.getConfig());
   }

   @Override
   public void handleVillagerMessage(VillagerMessage message) {
      this.client.m_240442_().m_240494_(message.getMessage(), false);
      SpeechManager.INSTANCE.onChatMessage(message.getContent(), message.getUuid());
   }

   @Override
   public void handleCustomSkinsChangedMessage(CustomSkinsChangedMessage message) {
      VillagerEditorScreen.setSkinListOutdated();
   }

   @Override
   public void handleCivilRegistryResponse(CivilRegistryResponse response) {
      if (this.client.f_91080_ instanceof ExtendedBookScreen extendedBookScreen && extendedBookScreen.getBook() instanceof CivilRegistryBook civilRegistryBook) {
         civilRegistryBook.receive(response.getIndex(), response.getLines());
      }
   }
}
