package yesman.epicfight.client;

import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.main.AuthenticationHelper;
import yesman.epicfight.network.server.SPPlayUISound;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

public class ClientEngine {
   private static ClientEngine instance = new ClientEngine();
   public Minecraft minecraft;
   public RenderEngine renderEngine;
   public ControlEngine controlEngine;
   private boolean vanillaModelDebuggingMode = false;
   private AuthenticationHelper authenticationHelper = new AuthenticationHelper() {
      @Override
      public void initialize(ConfigValue<String> accessToken, ConfigValue<String> refreshToken, EnumValue<AuthenticationHelper.AuthenticationProvider> provider) {
      }

      @Override
      public boolean valid() {
         return false;
      }

      @Override
      public AuthenticationHelper.Status status() {
         return AuthenticationHelper.Status.OFFLINE_MODE;
      }
   };

   public static ClientEngine getInstance() {
      return instance;
   }

   public ClientEngine() {
      instance = this;
      this.minecraft = Minecraft.m_91087_();
      this.renderEngine = new RenderEngine();
      this.controlEngine = new ControlEngine();
   }

   public boolean switchVanillaModelDebuggingMode() {
      this.vanillaModelDebuggingMode = !this.vanillaModelDebuggingMode;
      return this.vanillaModelDebuggingMode;
   }

   public boolean isVanillaModelDebuggingMode() {
      return this.vanillaModelDebuggingMode;
   }

   @Deprecated(forRemoval = true, since = "1.21.1")
   @Nullable
   public LocalPlayerPatch getPlayerPatch() {
      return EpicFightCapabilities.getEntityPatch(this.minecraft.f_91074_, LocalPlayerPatch.class);
   }

   public void initAuthHelper(AuthenticationHelper authHelper) {
      this.authenticationHelper = authHelper;
   }

   public AuthenticationHelper getAuthHelper() {
      return this.authenticationHelper;
   }

   public void playUISound(SPPlayUISound msg) {
      SoundInstance soundinstance = SimpleSoundInstance.m_119755_(msg.sound(), msg.pitch(), msg.volume());
      Minecraft.m_91087_().m_91106_().m_120367_(soundinstance);
      Minecraft.m_91087_().m_91106_().m_120367_(soundinstance);
   }

   @Deprecated(forRemoval = true, since = "1.21.1")
   public boolean isBattleMode() {
      return this.isEpicFightMode();
   }

   public boolean isEpicFightMode() {
      LocalPlayerPatch localPlayerPatch = EpicFightCapabilities.getEntityPatch(this.minecraft.f_91074_, LocalPlayerPatch.class);
      return localPlayerPatch == null ? false : localPlayerPatch.isEpicFightMode();
   }

   public static Comparator<ParticleRenderType> makeCustomLowestParticleRenderTypeComparator(List<ParticleRenderType> renderOrder) {
      Comparator<ParticleRenderType> vanillaComparator = Comparator.comparingInt(renderOrder::indexOf);
      return (typeOne, typeTwo) -> {
         boolean vanillaOne = renderOrder.contains(typeOne);
         boolean vanillaTwo = renderOrder.contains(typeTwo);
         if (vanillaOne && vanillaTwo) {
            return vanillaComparator.compare(typeOne, typeTwo);
         } else if (!vanillaOne && !vanillaTwo) {
            return Integer.compare(System.identityHashCode(typeOne), System.identityHashCode(typeTwo));
         } else if (typeOne == ParticleRenderType.f_107433_) {
            return 1;
         } else if (typeTwo == ParticleRenderType.f_107433_) {
            return -1;
         } else {
            return vanillaOne ? -1 : 1;
         }
      };
   }
}
