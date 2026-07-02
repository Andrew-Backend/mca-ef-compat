package yesman.epicfight.client.renderer.patched.entity;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.BeeStingerLayer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.EntityType;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.patched.layer.PatchedArrowLayer;
import yesman.epicfight.client.renderer.patched.layer.PatchedBeeStingerLayer;
import yesman.epicfight.client.renderer.patched.layer.PatchedCapeLayer;
import yesman.epicfight.client.renderer.patched.layer.PatchedItemInHandLayer;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;

public class PPlayerRenderer
   extends PHumanoidRenderer<AbstractClientPlayer, AbstractClientPlayerPatch<AbstractClientPlayer>, PlayerModel<AbstractClientPlayer>, PlayerRenderer, HumanoidMesh> {
   public PPlayerRenderer(Context context, EntityType<?> entityType) {
      super(Meshes.BIPED, context, entityType);
      this.addPatchedLayer(ArrowLayer.class, new PatchedArrowLayer(context));
      this.addPatchedLayer(BeeStingerLayer.class, new PatchedBeeStingerLayer());
      this.addPatchedLayer(CapeLayer.class, new PatchedCapeLayer());
      this.addPatchedLayer(PlayerItemInHandLayer.class, new PatchedItemInHandLayer());
   }

   protected void prepareModel(
      HumanoidMesh mesh, AbstractClientPlayer entity, AbstractClientPlayerPatch<AbstractClientPlayer> entitypatch, PlayerRenderer renderer
   ) {
      super.prepareModel(mesh, entity, entitypatch, renderer);
      renderer.m_117818_(entity);
      PlayerModel<AbstractClientPlayer> model = (PlayerModel<AbstractClientPlayer>)renderer.m_7200_();
      mesh.head.setHidden(!model.f_102808_.f_104207_);
      mesh.hat.setHidden(!model.f_102809_.f_104207_);
      mesh.jacket.setHidden(!model.f_103378_.f_104207_);
      mesh.torso.setHidden(!model.f_102810_.f_104207_);
      mesh.leftArm.setHidden(!model.f_102812_.f_104207_);
      mesh.leftLeg.setHidden(!model.f_102814_.f_104207_);
      mesh.leftPants.setHidden(!model.f_103376_.f_104207_);
      mesh.leftSleeve.setHidden(!model.f_103374_.f_104207_);
      mesh.rightArm.setHidden(!model.f_102811_.f_104207_);
      mesh.rightLeg.setHidden(!model.f_102813_.f_104207_);
      mesh.rightPants.setHidden(!model.f_103377_.f_104207_);
      mesh.rightSleeve.setHidden(!model.f_103375_.f_104207_);
   }

   public AssetAccessor<HumanoidMesh> getMeshProvider(AbstractClientPlayerPatch<AbstractClientPlayer> entitypatch) {
      return entitypatch.getOriginal().m_108564_().equals("slim") ? Meshes.ALEX : Meshes.BIPED;
   }

   @Override
   public AssetAccessor<HumanoidMesh> getDefaultMesh() {
      return Meshes.BIPED;
   }
}
