package yesman.epicfight.client.online;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.Items;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.client.model.SoftBodyTranslatable;
import yesman.epicfight.api.client.physics.cloth.ClothColliderPresets;
import yesman.epicfight.api.client.physics.cloth.ClothSimulator;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.gui.widgets.ColorSlider;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.main.EpicFightSharedConstants;

public record EpicSkins(Supplier<ResourceLocation> cloakTexture, float r, float g, float b) {
   public static void initEpicSkins(AbstractClientPlayerPatch<?> playerpatch) {
      if (EpicFightServerConnectionHelper.supported() && ClientConfig.enableCosmetics) {
         EpicFightServerConnectionHelper.getPlayerSkinInfo(
            EpicFightSharedConstants.webServerDomain(),
            playerpatch.getOriginal().m_20148_().toString().replace("-", ""),
            (response, exception) -> {
               if (exception != null) {
                  EpicFightMod.LOGGER.error("Failed at connecting Epic Fight web server: " + exception.getMessage());
               }

               if (response.statusCode() != 200) {
                  EpicFightMod.LOGGER.error("Error code from Epic Fight web server: " + response.body());
               }

               Map<EpicSkins.Slot, EpicSkins.Cosmetic> cosmetics = Maps.newHashMap();

               try {
                  JsonReader jsonReader = new JsonReader(new InputStreamReader(new ByteArrayInputStream(response.body().getBytes()), StandardCharsets.UTF_8));

                  for (JsonElement cosmeticJson : Streams.parse(jsonReader).getAsJsonArray()) {
                     JsonObject cosmeticObj = cosmeticJson.getAsJsonObject();

                     try {
                        EpicSkins.Cosmetic cosmetic = new EpicSkins.Cosmetic(cosmeticObj);
                        cosmetics.put(cosmetic.slot(), cosmetic);
                     } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                     }
                  }
               } catch (Exception var11) {
               }

               if (cosmetics.containsKey(EpicSkins.Slot.CAPE)) {
                  EpicSkins.Cosmetic cosmetic = cosmetics.get(EpicSkins.Slot.CAPE);
                  Supplier<ResourceLocation> cloakTextureProvider = null;
                  if (cosmetic.useBoolParam1() && cosmetic.boolParam1()) {
                     cloakTextureProvider = () -> playerpatch.getOriginal().m_108561_();
                  } else {
                     cloakTextureProvider = () -> cosmetic.textureLocation();
                  }

                  Supplier<ResourceLocation> fCloakTextureProvider = cloakTextureProvider;
                  RemoteAssets.getInstance()
                     .getRemoteMesh(
                        cosmetic.seq(),
                        cosmetic.fileLocation(),
                        mesh -> {
                           SoftBodyTranslatable.TRACKING_SIMULATION_SUBJECTS.add(playerpatch);
                           playerpatch.getClothSimulator()
                              .runWhen(
                                 ClothSimulator.PLAYER_CLOAK,
                                 (SoftBodyTranslatable)mesh,
                                 ClothSimulator.ClothObjectBuilder.create()
                                    .parentJoint(Armatures.BIPED.get().torso)
                                    .putAll("default".equals(playerpatch.getOriginal().m_108564_()) ? ClothColliderPresets.BIPED : ClothColliderPresets.BIPED),
                                 () -> playerpatch.getOriginal().m_108555_()
                                    && !playerpatch.getOriginal().m_20145_()
                                    && playerpatch.getOriginal().m_36170_(PlayerModelPart.CAPE)
                                    && playerpatch.getOriginal().m_6844_(EquipmentSlot.CHEST).m_41720_() != Items.f_42741_
                              );
                           if (!cosmetic.useIntParam1() || cosmetic.useBoolParam1() && cosmetic.boolParam1()) {
                              playerpatch.setEpicSkinsInformation(new EpicSkins(fCloakTextureProvider, 1.0F, 1.0F, 1.0F));
                           } else {
                              double brightness = (cosmetic.intParam1() & 0xFF) / 255.0F;
                              double saturation = ((cosmetic.intParam1() & 0xFF00) >> 8) / 255.0F;
                              double hue = ((cosmetic.intParam1() & 0xFF0000) >> 16) / 255.0F;
                              int hueColor = ColorSlider.rgbColor(hue);
                              int saturationApplied = ColorSlider.sliderPositionToColor(saturation, new int[]{hueColor, -1});
                              int brightnessApplied = ColorSlider.sliderPositionToColor(brightness, new int[]{saturationApplied, -16777216});
                              float r = ((brightnessApplied & 0xFF0000) >> 16) / 255.0F;
                              float g = ((brightnessApplied & 0xFF00) >> 8) / 255.0F;
                              float b = (brightnessApplied & 0xFF) / 255.0F;
                              playerpatch.setEpicSkinsInformation(new EpicSkins(fCloakTextureProvider, r, g, b));
                           }
                        }
                     );
               } else {
                  initDefaultCape(playerpatch);
               }
            }
         );
      } else {
         initDefaultCape(playerpatch);
      }
   }

   public static void initDefaultCape(AbstractClientPlayerPatch<?> playerpatch) {
      SoftBodyTranslatable.TRACKING_SIMULATION_SUBJECTS.add(playerpatch);
      playerpatch.getClothSimulator()
         .runWhen(
            ClothSimulator.PLAYER_CLOAK,
            Meshes.CAPE_DEFAULT,
            ClothSimulator.ClothObjectBuilder.create()
               .parentJoint(Armatures.BIPED.get().torso)
               .putAll("default".equals(playerpatch.getOriginal().m_108564_()) ? ClothColliderPresets.BIPED : ClothColliderPresets.BIPED_SLIM),
            () -> playerpatch.getOriginal().m_108555_()
               && !playerpatch.getOriginal().m_20145_()
               && playerpatch.getOriginal().m_36170_(PlayerModelPart.CAPE)
               && playerpatch.getOriginal().m_6844_(EquipmentSlot.CHEST).m_41720_() != Items.f_42741_
         );
      playerpatch.setEpicSkinsInformation(new EpicSkins(() -> playerpatch.getOriginal().m_108561_(), 1.0F, 1.0F, 1.0F));
   }

   public record Cosmetic(
      int seq,
      EpicSkins.Slot slot,
      int intParam1,
      boolean boolParam1,
      boolean useIntParam1,
      boolean useBoolParam1,
      String fileLocation,
      ResourceLocation textureLocation
   ) {
      public Cosmetic(JsonObject json) throws JsonSyntaxException {
         this(
            GsonHelper.m_13927_(json, "cosmeticSeq"),
            EpicSkins.Slot.valueOf(ParseUtil.toUpperCase(GsonHelper.m_13906_(json, "slot"))),
            GsonHelper.m_13927_(json, "intParam1"),
            GsonHelper.m_13912_(json, "boolParam1"),
            GsonHelper.m_13912_(json, "useIntParam1"),
            GsonHelper.m_13912_(json, "useBoolParam1"),
            GsonHelper.m_13906_(json, "fileLocation"),
            RemoteAssets.getInstance().getRemoteTexture(GsonHelper.m_13906_(json, "textureLocation"))
         );
      }
   }

   public enum Slot {
      CAPE;
   }
}
