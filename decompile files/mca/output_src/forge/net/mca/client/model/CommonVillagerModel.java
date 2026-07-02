package forge.net.mca.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import forge.net.mca.MCAClient;
import forge.net.mca.entity.EntitiesMCA;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.VillagerLike;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.entity.ai.relationship.VillagerDimensions;
import java.util.UUID;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public interface CommonVillagerModel<T extends LivingEntity> {
   ModelPart getBreastPart();

   ModelPart getBodyPart();

   Iterable<ModelPart> getCommonHeadParts();

   Iterable<ModelPart> getCommonBodyParts();

   Iterable<ModelPart> getBreastParts();

   VillagerDimensions.Mutable getDimensions();

   float getBreastSize();

   void setBreastSize(float var1);

   default void renderCommon(PoseStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
      float headSize = this.getDimensions().getHead();
      matrices.m_85836_();
      matrices.m_85841_(headSize, headSize, headSize);
      this.getCommonHeadParts().forEach(a -> a.m_104306_(matrices, vertices, light, overlay, red, green, blue, alpha));
      matrices.m_85849_();
      this.getCommonBodyParts().forEach(a -> a.m_104306_(matrices, vertices, light, overlay, red, green, blue, alpha));
      if (this.getBreastPart().f_104207_ && this.getBodyPart().f_104207_) {
         float breastSize = this.getBreastSize() * this.getDimensions().getBreasts();
         if (breastSize > 0.0F) {
            matrices.m_85836_();
            matrices.m_85841_(breastSize * 0.2F + 1.05F, breastSize * 0.75F + 0.75F, breastSize * 0.75F + 0.75F);

            for (ModelPart part : this.getBreastParts()) {
               part.m_104306_(matrices, vertices, light, overlay, red, green, blue, alpha);
            }

            matrices.m_85849_();
         }
      }
   }

   default void applyVillagerDimensions(VillagerLike<?> villager, boolean isSneaking) {
      this.getDimensions().set(villager.getVillagerDimensions());
      this.setBreastSize(villager.getGenetics().getBreastSize());
      this.getBreastPart().f_104207_ = villager.getGenetics().getGender() == Gender.FEMALE;

      for (ModelPart part : this.getBreastParts()) {
         part.f_104203_ = 0.9424779F + this.getBodyPart().f_104203_;
         float cy = 0.0F;
         float cz = 0.0F;
         if (isSneaking) {
            cy = 3.0F;
            cz = 1.5F;
         }

         part.m_104227_(0.25F, (float)(5.0 - Math.pow(this.getBreastSize(), 0.5) * 2.5 + cy), -1.5F + this.getBreastSize() * 0.25F + cz);
      }
   }

   default void copyCommonAttributes(CommonVillagerModel<T> target) {
      target.getDimensions().set(this.getDimensions());
      target.setBreastSize(this.getBreastSize());
   }

   static VillagerLike<?> getVillager(Level world, UUID uuid) {
      if (MCAClient.fallbackVillager == null) {
         MCAClient.fallbackVillager = (VillagerEntityMCA)((EntityType)EntitiesMCA.MALE_VILLAGER.get()).m_20615_(world);
      }

      return MCAClient.playerData.getOrDefault(uuid, MCAClient.fallbackVillager);
   }

   static VillagerLike<?> getVillager(Entity villager) {
      return villager instanceof VillagerLike<?> v ? v : getVillager(villager.m_9236_(), villager.m_20148_());
   }
}
