package yesman.epicfight.epicskins.user;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.online.RemoteAssets;

@OnlyIn(Dist.CLIENT)
public record Cosmetic(
   int seq,
   boolean unlocked,
   String title,
   String description,
   String resourceFile,
   Cosmetic.Slot slot,
   @Nullable ResourceLocation textureLocation,
   boolean useIntParam1,
   boolean useBoolParam1
) {
   public Cosmetic(JsonObject json) {
      this(
         GsonHelper.m_13927_(json, "seq"),
         GsonHelper.m_13912_(json, "unlocked"),
         GsonHelper.m_13906_(json, "title"),
         GsonHelper.m_13906_(json, "explanation"),
         GsonHelper.m_13906_(json, "fileLocation"),
         Cosmetic.Slot.valueOf(ParseUtil.toUpperCase(GsonHelper.m_13906_(json, "slot"))),
         RemoteAssets.getInstance().getRemoteTexture(GsonHelper.m_13906_(json, "textureLocation")),
         GsonHelper.m_13912_(json, "useIntParam1"),
         GsonHelper.m_13912_(json, "useBoolParam1")
      );
   }

   public AssetAccessor<? extends Mesh> getAsMesh(Consumer<Mesh> onDownloaded) {
      if (this.slot == Cosmetic.Slot.CAPE) {
         if (this.seq == -1) {
            return AuthenticationHelperImpl.getInstance().playerInfo().m_105338_() != null ? Meshes.CAPE_DEFAULT : null;
         } else {
            return RemoteAssets.getInstance().getRemoteMesh(this.seq, this.resourceFile, onDownloaded);
         }
      } else {
         return null;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public enum Slot {
      CAPE;
   }
}
