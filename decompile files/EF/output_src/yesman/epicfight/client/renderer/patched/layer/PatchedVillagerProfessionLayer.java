package yesman.epicfight.client.renderer.patched.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ZombieVillagerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.client.resources.metadata.animation.VillagerMetaDataSection.Hat;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.mesh.VillagerMesh;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;

public class PatchedVillagerProfessionLayer
   extends ModelRenderLayer<ZombieVillager, MobPatch<ZombieVillager>, ZombieVillagerModel<ZombieVillager>, VillagerProfessionLayer<ZombieVillager, ZombieVillagerModel<ZombieVillager>>, VillagerMesh> {
   public PatchedVillagerProfessionLayer() {
      super(Meshes.VILLAGER_ZOMBIE);
   }

   protected void renderLayer(
      MobPatch<ZombieVillager> entitypatch,
      ZombieVillager entityliving,
      VillagerProfessionLayer<ZombieVillager, ZombieVillagerModel<ZombieVillager>> vanillaLayer,
      PoseStack postStack,
      MultiBufferSource buffer,
      int packedLight,
      OpenMatrix4f[] poses,
      float bob,
      float yRot,
      float xRot,
      float partialTicks
   ) {
      if (!entityliving.m_20145_()) {
         VillagerData villagerdata = ((VillagerDataHolder)entitypatch.getOriginal()).m_7141_();
         Hat typeHat = vanillaLayer.m_117658_(vanillaLayer.f_117623_, "type", BuiltInRegistries.f_256934_, villagerdata.m_35560_());
         Hat professionHat = vanillaLayer.m_117658_(vanillaLayer.f_117624_, "profession", BuiltInRegistries.f_256735_, villagerdata.m_35571_());
         if (typeHat != Hat.NONE && (typeHat != Hat.PARTIAL || professionHat == Hat.FULL) || !entityliving.m_6844_(EquipmentSlot.HEAD).m_41619_()) {
            this.mesh.get().head.setHidden(true);
            this.mesh.get().hat.setHidden(true);
         }

         if (!((ZombieVillager)entitypatch.getOriginal()).m_6844_(EquipmentSlot.LEGS).m_41619_()) {
            this.mesh.get().jacket.setHidden(true);
         }

         this.mesh
            .get()
            .draw(
               postStack,
               buffer,
               RenderType.m_110458_(vanillaLayer.m_117668_("type", BuiltInRegistries.f_256934_.m_7981_(villagerdata.m_35560_()))),
               packedLight,
               1.0F,
               1.0F,
               1.0F,
               1.0F,
               LivingEntityRenderer.m_115338_(entityliving, 0.0F),
               entitypatch.getArmature(),
               poses
            );
         if (villagerdata.m_35571_() != VillagerProfession.f_35585_) {
            this.mesh
               .get()
               .draw(
                  postStack,
                  buffer,
                  RenderType.m_110458_(vanillaLayer.m_117668_("profession", ForgeRegistries.VILLAGER_PROFESSIONS.getKey(villagerdata.m_35571_()))),
                  packedLight,
                  1.0F,
                  1.0F,
                  1.0F,
                  1.0F,
                  LivingEntityRenderer.m_115338_(entityliving, 0.0F),
                  entitypatch.getArmature(),
                  poses
               );
         }
      }
   }
}
