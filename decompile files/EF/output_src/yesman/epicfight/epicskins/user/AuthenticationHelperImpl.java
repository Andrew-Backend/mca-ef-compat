package yesman.epicfight.epicskins.user;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.authlib.GameProfile;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.client.physics.cloth.ClothColliderPresets;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.online.EpicFightServerConnectionHelper;
import yesman.epicfight.client.online.texture.RemoteTexture;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.epicskins.client.screen.AvatarEditScreen;
import yesman.epicfight.epicskins.exception.HttpResponseException;
import yesman.epicfight.epicskins.exception.OfflineUserException;
import yesman.epicfight.epicskins.util.JsonConverter;
import yesman.epicfight.main.AuthenticationHelper;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.main.EpicFightSharedConstants;

@OnlyIn(Dist.CLIENT)
public class AuthenticationHelperImpl implements AuthenticationHelper {
   private final Multimap<Cosmetic.Slot, Cosmetic> cosmeticsBySlot = HashMultimap.create();
   private final Map<Integer, Cosmetic> cosmetics = Maps.newHashMap();
   private PlayerInfo playerInfo;
   private String accessToken;
   private String refreshToken;
   private AuthenticationHelper.Status status = AuthenticationHelper.Status.UNAUTHENTICATED;
   private AuthenticationHelper.AuthenticationProvider authProvider;
   private AuthenticationHelperImpl.CapeProperties capeProperties;

   public static boolean checkOnlineUser(User user) {
      return !"0".equals(user.m_92547_());
   }

   private static String profileIdToString(User user) {
      return user.m_240411_().toString().replace("-", "");
   }

   public static synchronized AuthenticationHelperImpl getInstance() {
      return (AuthenticationHelperImpl)ClientEngine.getInstance().getAuthHelper();
   }

   private AuthenticationHelperImpl() {
      EpicFightMod.LOGGER.info("Epic Fight web server status: Initialize");
      this.capeProperties = new AuthenticationHelperImpl.CapeProperties();
      new RemoteTexture(null, null);
      Meshes.BIPED.get();
      ClothColliderPresets.BIPED.getClass();
   }

   @Override
   public void initialize(ConfigValue<String> accessToken, ConfigValue<String> refreshToken, EnumValue<AuthenticationHelper.AuthenticationProvider> provider) {
      if (this.status == AuthenticationHelper.Status.UNAUTHENTICATED) {
         this.accessToken = (String)accessToken.get();
         this.refreshToken = (String)refreshToken.get();
         this.authProvider = (AuthenticationHelper.AuthenticationProvider)provider.get();
         User user = Minecraft.m_91087_().m_91094_();
         if (checkOnlineUser(user)) {
            EpicFightServerConnectionHelper.autoLogin(
               EpicFightSharedConstants.webServerDomain(),
               profileIdToString(user),
               this.accessToken,
               this.refreshToken,
               this.authProvider.toString(),
               (response, exception) -> {
                  if (exception != null) {
                     EpicFightMod.LOGGER.warn("Auto login failed: " + exception);
                     exception.printStackTrace();
                  } else if (response.statusCode() == 200) {
                     JsonObject responseJson = JsonConverter.parseJson(response.body()).getAsJsonObject();
                     this.onAuthenticationSuccess(responseJson, () -> {}, ex -> {});
                  } else {
                     EpicFightMod.LOGGER.warn("Auto login failed with status code " + response.statusCode() + ": " + response.body());
                  }
               }
            );
         } else {
            EpicFightMod.LOGGER.info("Epic Fight web server status: failed because of offline mode");
            this.status = AuthenticationHelper.Status.OFFLINE_MODE;
         }

         this.playerInfo = new PlayerInfo(new GameProfile(user.m_240411_(), user.m_92546_()), false);
         this.playerInfo.m_105337_();
      }
   }

   @Override
   public Screen getAvatarEditorScreen(Screen parentScreen) {
      return new AvatarEditScreen(parentScreen);
   }

   @Override
   public boolean valid() {
      return true;
   }

   public void openAuthenticateBrowser() {
      User user = Minecraft.m_91087_().m_91094_();
      Util.m_137581_()
         .m_137648_(URI.create(EpicFightSharedConstants.webServerDomain() + "/login?mc_uuid=" + profileIdToString(user) + "&mc_username=" + user.m_92546_()));
   }

   public void loginWithAuthCode(String code, Runnable onSuccess, Consumer<Throwable> onFail) {
      User user = Minecraft.m_91087_().m_91094_();
      EpicFightServerConnectionHelper.signIn(EpicFightSharedConstants.webServerDomain(), profileIdToString(user), code, (response, exception) -> {
         if (exception != null) {
            onFail.accept(exception);
         } else {
            if (response.statusCode() == 200) {
               JsonReader jsonReader = new JsonReader(new InputStreamReader(new ByteArrayInputStream(response.body().getBytes()), StandardCharsets.UTF_8));
               JsonObject jsonObject = Streams.parse(jsonReader).getAsJsonObject();
               this.onAuthenticationSuccess(jsonObject, onSuccess, onFail);
            } else {
               onFail.accept(new HttpResponseException("Invalid code", response.statusCode(), response.body()));
            }
         }
      });
   }

   private void onAuthenticationSuccess(JsonObject authResponse, Runnable onSuccess, Consumer<Throwable> onFailed) {
      this.status = AuthenticationHelper.Status.AUTHENTICATED;
      this.authProvider = AuthenticationHelper.AuthenticationProvider.valueOf(GsonHelper.m_13906_(authResponse, "provider").toUpperCase(Locale.ROOT));
      this.accessToken = GsonHelper.m_13906_(authResponse, "access_token");
      this.refreshToken = GsonHelper.m_13906_(authResponse, "refresh_token");
      ClientConfig.ACCESS_TOKEN.set(this.accessToken);
      ClientConfig.REFRESH_TOKNE.set(this.refreshToken);
      ClientConfig.PROVIDER.set(this.authProvider);

      for (JsonElement cosmeticElement : GsonHelper.m_13933_(authResponse, "cosmetics")) {
         JsonObject cosemticObject = cosmeticElement.getAsJsonObject();
         if ("cape".equals(GsonHelper.m_13906_(cosemticObject, "slot"))) {
            this.capeProperties.setCape(GsonHelper.m_13927_(cosemticObject, "cosmetic_seq"));
            this.capeProperties.unpackColorSliderPositions(GsonHelper.m_13927_(cosemticObject, "int_param1"));
            this.capeProperties.setVanillaTextureUse(GsonHelper.m_13912_(cosemticObject, "bool_param1"));
         }
      }

      this.cosmeticsBySlot.clear();
      this.cosmetics.clear();
      User user = Minecraft.m_91087_().m_91094_();
      EpicFightServerConnectionHelper.getAvailableCosmetics(
         EpicFightSharedConstants.webServerDomain(),
         profileIdToString(user),
         this.accessToken,
         this.refreshToken,
         this.authProvider.toString(),
         (response, exception) -> {
            if (exception != null) {
               onFailed.accept(exception);
            }

            if (response.statusCode() == 200) {
               try {
                  JsonObject responseJson = JsonConverter.parseJson(response.body()).getAsJsonObject();

                  for (JsonElement json : responseJson.getAsJsonArray("object")) {
                     Cosmetic cosmetic = new Cosmetic(json.getAsJsonObject());
                     this.cosmeticsBySlot.put(cosmetic.slot(), cosmetic);
                     this.cosmetics.put(cosmetic.seq(), cosmetic);
                  }

                  onSuccess.run();
               } catch (Exception e) {
                  e.printStackTrace();
               }
            } else {
               onFailed.accept(new HttpResponseException("Failed at getting available cosmetics", response.statusCode(), response.body()));
            }
         }
      );
   }

   public void signOut(Runnable onSuccess, Consumer<Throwable> onFailed) {
      User user = Minecraft.m_91087_().m_91094_();
      EpicFightServerConnectionHelper.signOut(
         EpicFightSharedConstants.webServerDomain(),
         profileIdToString(user),
         this.accessToken,
         this.refreshToken,
         this.authProvider.toString(),
         (response, exception) -> {
            if (exception != null) {
               onFailed.accept(exception);
            } else if (response.statusCode() == 200) {
               getInstance().onSignOut();
               onSuccess.run();
            } else {
               onFailed.accept(new HttpResponseException("Sign out failed", response.statusCode(), response.body()));
            }
         }
      );
   }

   public void onSignOut() {
      try {
         this.status = AuthenticationHelper.Status.UNAUTHENTICATED;
         this.authProvider = AuthenticationHelper.AuthenticationProvider.NULL;
         this.accessToken = "";
         this.refreshToken = "";
         ClientConfig.ACCESS_TOKEN.set(this.accessToken);
         ClientConfig.REFRESH_TOKNE.set(this.refreshToken);
         ClientConfig.PROVIDER.set(this.authProvider);
         this.cosmeticsBySlot.clear();
         this.cosmetics.clear();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void sendSaveRequest(Consumer<Throwable> callback) {
      User user = Minecraft.m_91087_().m_91094_();
      if (!checkOnlineUser(user)) {
         this.status = AuthenticationHelper.Status.OFFLINE_MODE;
         throw new OfflineUserException("Offline mode user");
      }

      JsonObject postBody = new JsonObject();
      postBody.addProperty("provider", this.authProvider.toString());
      postBody.addProperty("access_token", this.accessToken);
      postBody.addProperty("refresh_token", this.refreshToken);
      postBody.addProperty("minecraft_uuid", profileIdToString(user));
      postBody.addProperty("cosmetic_seq", this.capeProperties.capeSeq());
      postBody.addProperty("slot", "cape");
      postBody.addProperty("int_param1", this.capeProperties.packColorSliderPositions());
      postBody.addProperty("bool_param1", this.capeProperties.useVanillaTexture());
      EpicFightServerConnectionHelper.saveConfiguration(EpicFightSharedConstants.webServerDomain(), postBody.toString(), (response, exception) -> {
         if (exception != null) {
            callback.accept(exception);
         }

         if (response.statusCode() == 200) {
            JsonObject responseJson = JsonConverter.parseJson(response.body()).getAsJsonObject();
            this.accessToken = GsonHelper.m_13906_(responseJson, "accessToken");
            this.refreshToken = GsonHelper.m_13906_(responseJson, "refreshToken");
            ClientConfig.ACCESS_TOKEN.set(this.accessToken);
            ClientConfig.REFRESH_TOKNE.set(this.refreshToken);
            callback.accept(null);
         } else {
            callback.accept(new HttpResponseException("Failed at updating cosmetic information", response.statusCode(), response.body()));
         }
      });
   }

   public void setStatus(AuthenticationHelper.Status status) {
      this.status = status;
   }

   public AuthenticationHelper.AuthenticationProvider authProvider() {
      return this.authProvider;
   }

   public String getAccessToken() {
      return this.accessToken;
   }

   public String getRefreshToken() {
      return this.refreshToken;
   }

   @Override
   public AuthenticationHelper.Status status() {
      return this.status;
   }

   public PlayerInfo playerInfo() {
      return this.playerInfo;
   }

   public Collection<Cosmetic> getAllCosmetics() {
      return this.cosmetics.values();
   }

   public Collection<Cosmetic> getCosmeticsBySlot(Cosmetic.Slot cosmeticSlot) {
      return this.cosmeticsBySlot.get(cosmeticSlot);
   }

   public Cosmetic getCosmetic(int seq) {
      return this.cosmetics.get(seq);
   }

   public AuthenticationHelperImpl.CapeProperties capeProperties() {
      return this.capeProperties;
   }

   static {
      ClientEngine.getInstance().initAuthHelper(new AuthenticationHelperImpl());
   }

   @OnlyIn(Dist.CLIENT)
   public static class CapeProperties {
      private int capeSeq;
      private boolean useVanillaTexture;
      private double hue;
      private double saturation;
      private double brightness;

      public void setCape(int capeSeq) {
         this.capeSeq = capeSeq;
      }

      public void setVanillaTextureUse(boolean useVanillaTexture) {
         this.useVanillaTexture = useVanillaTexture;
      }

      public void setHue(double hue) {
         this.hue = hue;
      }

      public void setSaturation(double saturation) {
         this.saturation = saturation;
      }

      public void setBrightness(double brightness) {
         this.brightness = brightness;
      }

      public int capeSeq() {
         return this.capeSeq;
      }

      public boolean useVanillaTexture() {
         return this.useVanillaTexture;
      }

      public double hue() {
         return this.hue;
      }

      public double saturation() {
         return this.saturation;
      }

      public double brightness() {
         return this.brightness;
      }

      public void unpackColorSliderPositions(int packedColorSlider) {
         this.brightness = (packedColorSlider & 0xFF) / 255.0F;
         this.saturation = ((packedColorSlider & 0xFF00) >> 8) / 255.0F;
         this.hue = ((packedColorSlider & 0xFF0000) >> 16) / 255.0F;
      }

      public int packColorSliderPositions() {
         double huePos = Mth.m_14008_(this.hue, 0.0, 1.0);
         double saturationPos = Mth.m_14008_(this.saturation, 0.0, 1.0);
         double brightnessPos = Mth.m_14008_(this.brightness, 0.0, 1.0);
         return (int)(huePos * 255.0) << 16 | (int)(saturationPos * 255.0) << 8 | (int)(brightnessPos * 255.0);
      }
   }
}
