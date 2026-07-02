package forge.net.mca.network;

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

public interface ClientInteractionManager {
   void handleGuiRequest(OpenGuiRequest var1);

   void handleFamilyTreeResponse(GetFamilyTreeResponse var1);

   void handleInteractDataResponse(GetInteractDataResponse var1);

   void handleVillageDataResponse(GetVillageResponse var1);

   void handleVillageDataFailedResponse(GetVillageFailedResponse var1);

   void handleFamilyDataResponse(GetFamilyResponse var1);

   void handleVillagerDataResponse(GetVillagerResponse var1);

   void handleDialogueResponse(InteractionDialogueResponse var1);

   void handleSkinListResponse(AnalysisResults var1);

   void handleBabyNameResponse(BabyNameResponse var1);

   void handleVillagerNameResponse(VillagerNameResponse var1);

   void handleToastMessage(ShowToastRequest var1);

   void handleFamilyTreeUUIDResponse(FamilyTreeUUIDResponse var1);

   void handlePlayerDataMessage(PlayerDataMessage var1);

   void handleSkinListResponse(SkinListResponse var1);

   void handleDestinyGuiRequest(OpenDestinyGuiRequest var1);

   void handleDialogueQuestionResponse(InteractionDialogueQuestionResponse var1);

   void handleConfigResponse(ConfigResponse var1);

   void handleVillagerMessage(VillagerMessage var1);

   void handleCustomSkinsChangedMessage(CustomSkinsChangedMessage var1);

   void handleCivilRegistryResponse(CivilRegistryResponse var1);
}
