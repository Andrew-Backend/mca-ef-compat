package quilt.net.mca.network;

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
