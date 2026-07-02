package forge.net.mca.client.render;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.vertex.PoseStack;
import forge.net.mca.Config;
import forge.net.mca.client.gui.VillagerEditorScreen;
import forge.net.mca.client.model.VillagerEntityBaseModelMCA;
import forge.net.mca.client.model.VillagerEntityModelMCA;
import forge.net.mca.entity.VillagerLike;
import forge.net.mca.entity.ai.relationship.AgeState;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class VillagerLikeEntityMCARenderer<T extends Mob & VillagerLike<T>> extends HumanoidMobRenderer<T, VillagerEntityModelMCA<T>> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/steve.png");

   public VillagerLikeEntityMCARenderer(Context ctx, VillagerEntityModelMCA<T> model) {
      super(ctx, model, 0.5F);
      this.m_115326_(new HumanoidArmorLayer(this, this.createArmorModel(0.3F), this.createArmorModel(0.55F), ctx.m_266367_()));
   }

   private VillagerEntityBaseModelMCA<T> createArmorModel(float modelSize) {
      return new VillagerEntityBaseModelMCA(
         LayerDefinition.m_171565_(VillagerEntityBaseModelMCA.getModelData(new CubeDeformation(modelSize)), 64, 32).m_171564_()
      );
   }

   protected void scale(T villager, PoseStack matrices, float tickDelta) {
      float height = villager.getRawScaleFactor();
      float width = villager.getHorizontalScaleFactor();
      matrices.m_85841_(width, height, width);
      if (villager.getAgeState() == AgeState.BABY && !villager.m_20159_()) {
         matrices.m_252880_(0.0F, 0.6F, 0.0F);
      }
   }

   @Nullable
   protected RenderType getRenderLayer(T entity, boolean showBody, boolean translucent, boolean showOutlines) {
      if (entity.hasCustomSkin()) {
         Minecraft minecraftClient = Minecraft.m_91087_();
         Map<Type, MinecraftProfileTexture> map = minecraftClient.m_91109_().m_118815_(entity.getGameProfile());
         return map.containsKey(Type.SKIN)
            ? RenderType.m_110473_(minecraftClient.m_91109_().m_118825_(map.get(Type.SKIN), Type.SKIN))
            : RenderType.m_110458_(DefaultPlayerSkin.m_118627_(UUIDUtil.m_235875_(entity.getGameProfile())));
      } else {
         return null;
      }
   }

   protected boolean m_6512_(T villager) {
      Player player = Minecraft.m_91087_().f_91074_;
      return villager.m_7770_() != null
         && !(Minecraft.m_91087_().f_91080_ instanceof VillagerEditorScreen)
         && player != null
         && Config.getInstance().showNameTags
         && player.m_20280_(villager) < Math.pow(Config.getInstance().nameTagDistance, 2.0)
         && !villager.m_20177_(player);
   }

   public ResourceLocation getTexture(T mobEntity) {
      return TEXTURE;
   }

   protected boolean isShaking(T entity) {
      return entity.getInfectionProgress() > 0.2F;
   }
}
