package yesman.epicfight.client.renderer.patched.entity;

import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.utils.math.Vec2i;
import yesman.epicfight.client.mesh.CreeperMesh;
import yesman.epicfight.world.capabilities.entitypatch.mob.CreeperPatch;

public class PCreeperRenderer extends PatchedLivingEntityRenderer<Creeper, CreeperPatch, CreeperModel<Creeper>, CreeperRenderer, CreeperMesh> {
   public PCreeperRenderer(Context context, EntityType<?> entityType) {
      super(context, entityType);
   }

   protected int getOverlayCoord(Creeper entity, CreeperPatch entitypatch, float partialTick) {
      float swelling = entity.m_32320_(partialTick);
      float u = (int)(swelling * 10.0F) % 2 == 0 ? 0.0F : Mth.m_14036_(swelling, 0.5F, 1.0F);
      int initU = OverlayTexture.m_118088_(u);
      int initV = OverlayTexture.m_118096_(entity.f_20916_ > 0 || entity.f_20919_ > 0);
      Vec2i coord = new Vec2i(initU, initV);
      entitypatch.getEntityDecorations().modifyOverlay(coord, partialTick);
      return OverlayTexture.m_118093_(coord.x, coord.y);
   }

   @Override
   public AssetAccessor<CreeperMesh> getDefaultMesh() {
      return Meshes.CREEPER;
   }
}
