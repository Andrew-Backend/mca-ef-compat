package quilt.net.mca.network;

import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_370;
import net.minecraft.class_437;
import net.minecraft.class_7923;
import net.minecraft.class_370.class_371;
import quilt.net.mca.Config;
import quilt.net.mca.MCAClient;
import quilt.net.mca.client.book.Book;
import quilt.net.mca.client.book.CivilRegistryBook;
import quilt.net.mca.client.gui.BlueprintScreen;
import quilt.net.mca.client.gui.CombScreen;
import quilt.net.mca.client.gui.ExtendedBookScreen;
import quilt.net.mca.client.gui.FamilyTreeScreen;
import quilt.net.mca.client.gui.FamilyTreeSearchScreen;
import quilt.net.mca.client.gui.InteractScreen;
import quilt.net.mca.client.gui.LimitedVillagerEditorScreen;
import quilt.net.mca.client.gui.NameBabyScreen;
import quilt.net.mca.client.gui.NeedleScreen;
import quilt.net.mca.client.gui.SkinListUpdateListener;
import quilt.net.mca.client.gui.VillagerEditorScreen;
import quilt.net.mca.client.gui.VillagerTrackerSearchScreen;
import quilt.net.mca.client.gui.WhistleScreen;
import quilt.net.mca.client.tts.SpeechManager;
import quilt.net.mca.entity.EntitiesMCA;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.VillagerLike;
import quilt.net.mca.item.BabyItem;
import quilt.net.mca.item.ExtendedWrittenBookItem;
import quilt.net.mca.network.s2c.AnalysisResults;
import quilt.net.mca.network.s2c.BabyNameResponse;
import quilt.net.mca.network.s2c.CivilRegistryResponse;
import quilt.net.mca.network.s2c.ConfigResponse;
import quilt.net.mca.network.s2c.CustomSkinsChangedMessage;
import quilt.net.mca.network.s2c.FamilyTreeUUIDResponse;
import quilt.net.mca.network.s2c.GetFamilyResponse;
import quilt.net.mca.network.s2c.GetFamilyTreeResponse;
import quilt.net.mca.network.s2c.GetInteractDataResponse;
import quilt.net.mca.network.s2c.GetVillageFailedResponse;
import quilt.net.mca.network.s2c.GetVillageResponse;
import quilt.net.mca.network.s2c.GetVillagerResponse;
import quilt.net.mca.network.s2c.InteractionDialogueQuestionResponse;
import quilt.net.mca.network.s2c.InteractionDialogueResponse;
import quilt.net.mca.network.s2c.OpenDestinyGuiRequest;
import quilt.net.mca.network.s2c.OpenGuiRequest;
import quilt.net.mca.network.s2c.PlayerDataMessage;
import quilt.net.mca.network.s2c.ShowToastRequest;
import quilt.net.mca.network.s2c.SkinListResponse;
import quilt.net.mca.network.s2c.VillagerMessage;
import quilt.net.mca.network.s2c.VillagerNameResponse;
import quilt.net.mca.resources.BuildingTypes;
import quilt.net.mca.server.world.data.Village;

public class ClientInteractionManagerImpl implements ClientInteractionManager {
   private final class_310 client = class_310.method_1551();

   @Override
   public void handleGuiRequest(OpenGuiRequest message) {
      assert this.client.field_1687 != null;
      assert class_310.method_1551().field_1724 != null;
      switch (message.getGui()) {
         case WHISTLE:
            this.client.method_1507(new WhistleScreen());
            break;
         case BOOK:
            if (this.client.field_1724 != null) {
               class_1799 item = this.client.field_1724.method_5998(class_1268.field_5808);
               if (item.method_7909() instanceof ExtendedWrittenBookItem bookItem) {
                  Book book = bookItem.getBook(item);
                  this.client.method_1507(new ExtendedBookScreen(book));
               }
            }
            break;
         case BLUEPRINT:
            this.client.method_1507(new BlueprintScreen());
            break;
         case INTERACT:
            if (this.client.field_1724 != null) {
               class_1799 item = this.client.field_1724.method_5998(class_1268.field_5808);
               boolean isOnBlacklist = Config.getInstance()
                  .villagerInteractionItemBlacklist
                  .contains(class_7923.field_41178.method_10221(item.method_7909()).toString());
               if (!isOnBlacklist) {
                  VillagerLike<?> villager = (VillagerLike<?>)this.client.field_1687.method_8469(message.villager);
                  this.client.method_1507(new InteractScreen(villager));
               }
            }
            break;
         case VILLAGER_EDITOR:
            class_1297 entityxxx = this.client.field_1687.method_8469(message.villager);
            assert entityxxx != null;
            this.client.method_1507(new VillagerEditorScreen(entityxxx.method_5667(), class_310.method_1551().field_1724.method_5667()));
            break;
         case LIMITED_VILLAGER_EDITOR:
            class_1297 entityxx = this.client.field_1687.method_8469(message.villager);
            assert entityxx != null;
            this.client.method_1507(new LimitedVillagerEditorScreen(entityxx.method_5667(), class_310.method_1551().field_1724.method_5667()));
            break;
         case NEEDLE_AND_THREAD:
            class_1297 entityx = this.client.field_1687.method_8469(message.villager);
            if (entityx == null) {
               this.client.method_1507(new NeedleScreen(class_310.method_1551().field_1724.method_5667()));
            } else {
               this.client.method_1507(new NeedleScreen(entityx.method_5667(), class_310.method_1551().field_1724.method_5667()));
            }
            break;
         case COMB:
            class_1297 entity = this.client.field_1687.method_8469(message.villager);
            if (entity == null) {
               this.client.method_1507(new CombScreen(class_310.method_1551().field_1724.method_5667()));
            } else {
               this.client.method_1507(new CombScreen(entity.method_5667(), class_310.method_1551().field_1724.method_5667()));
            }
            break;
         case BABY_NAME:
            if (this.client.field_1724 != null) {
               class_1799 item = this.client.field_1724.method_5998(class_1268.field_5808);
               if (item.method_7909() instanceof BabyItem) {
                  this.client.method_1507(new NameBabyScreen(this.client.field_1724, item));
               }
            }
            break;
         case FAMILY_TREE:
            this.client.method_1507(new FamilyTreeSearchScreen());
            break;
         case VILLAGER_TRACKER:
            this.client.method_1507(new VillagerTrackerSearchScreen());
      }
   }

   @Override
   public void handleFamilyTreeResponse(GetFamilyTreeResponse message) {
      if (this.client.field_1755 instanceof FamilyTreeScreen gui) {
         gui.setFamilyData(message.uuid, message.family);
      }
   }

   @Override
   public void handleInteractDataResponse(GetInteractDataResponse message) {
      if (this.client.field_1755 instanceof InteractScreen gui) {
         gui.setConstraints(message.constraints);
         gui.setParents(message.father, message.mother);
         gui.setSpouse(message.marriageState, message.spouse);
      }
   }

   @Override
   public void handleVillageDataResponse(GetVillageResponse message) {
      if (this.client.field_1755 instanceof BlueprintScreen gui) {
         BuildingTypes.getInstance().setBuildingTypes(message.buildingTypes);
         Village village = new Village(message.getData(), null);
         gui.setVillage(village);
         gui.setVillageData(message.rank, message.reputation, message.isVillage, message.ids, message.tasks);
      }
   }

   @Override
   public void handleVillageDataFailedResponse(GetVillageFailedResponse message) {
      if (this.client.field_1755 instanceof BlueprintScreen gui) {
         gui.setVillage(null);
      }
   }

   @Override
   public void handleFamilyDataResponse(GetFamilyResponse message) {
      if (this.client.field_1755 instanceof WhistleScreen gui) {
         gui.setVillagerData(message.getData());
      }
   }

   @Override
   public void handleVillagerDataResponse(GetVillagerResponse message) {
      if (this.client.field_1755 instanceof VillagerEditorScreen gui) {
         gui.setVillagerData(message.getData());
      }
   }

   @Override
   public void handleDialogueResponse(InteractionDialogueResponse message) {
      if (this.client.field_1755 instanceof InteractScreen gui) {
         gui.setDialogue(message.question, message.answers);
      }
   }

   @Override
   public void handleDialogueQuestionResponse(InteractionDialogueQuestionResponse message) {
      if (this.client.field_1755 instanceof InteractScreen gui) {
         gui.setLastPhrase(message.getQuestionText(), message.silent);
      }
   }

   @Override
   public void handleSkinListResponse(AnalysisResults message) {
      InteractScreen.setAnalysis(message.analysis);
   }

   @Override
   public void handleBabyNameResponse(BabyNameResponse message) {
      if (this.client.field_1755 instanceof NameBabyScreen gui) {
         gui.setBabyName(message.getName());
      }
   }

   @Override
   public void handleVillagerNameResponse(VillagerNameResponse message) {
      if (this.client.field_1755 instanceof VillagerEditorScreen gui) {
         gui.setVillagerName(message.getName());
      }
   }

   @Override
   public void handleToastMessage(ShowToastRequest message) {
      class_370.method_27024(this.client.method_1566(), class_371.field_2218, message.getTitle(), message.getMessage());
   }

   @Override
   public void handleFamilyTreeUUIDResponse(FamilyTreeUUIDResponse response) {
      if (this.client.field_1755 instanceof FamilyTreeSearchScreen gui) {
         gui.setList(response.getList());
      }
   }

   @Override
   public void handlePlayerDataMessage(PlayerDataMessage response) {
      VillagerEntityMCA villager = (VillagerEntityMCA)((class_1299)EntitiesMCA.MALE_VILLAGER.get()).method_5883(class_310.method_1551().field_1687);
      assert villager != null;
      villager.method_5749(response.getData());
      MCAClient.addPlayerData(response.uuid, villager);
   }

   @Override
   public void handleSkinListResponse(SkinListResponse message) {
      class_437 screen = this.client.field_1755;
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
      this.client.method_44714().method_44736(message.getMessage(), false);
      SpeechManager.INSTANCE.onChatMessage(message.getContent(), message.getUuid());
   }

   @Override
   public void handleCustomSkinsChangedMessage(CustomSkinsChangedMessage message) {
      VillagerEditorScreen.setSkinListOutdated();
   }

   @Override
   public void handleCivilRegistryResponse(CivilRegistryResponse response) {
      if (this.client.field_1755 instanceof ExtendedBookScreen extendedBookScreen
         && extendedBookScreen.getBook() instanceof CivilRegistryBook civilRegistryBook) {
         civilRegistryBook.receive(response.getIndex(), response.getLines());
      }
   }
}
