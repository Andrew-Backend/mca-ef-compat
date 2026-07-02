package quilt.net.mca.client.model;

import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1937;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_630;
import quilt.net.mca.MCAClient;
import quilt.net.mca.entity.EntitiesMCA;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.VillagerLike;
import quilt.net.mca.entity.ai.relationship.Gender;
import quilt.net.mca.entity.ai.relationship.VillagerDimensions;

public interface CommonVillagerModel<T extends class_1309> {
   class_630 getBreastPart();

   class_630 getBodyPart();

   Iterable<class_630> getCommonHeadParts();

   Iterable<class_630> getCommonBodyParts();

   Iterable<class_630> getBreastParts();

   VillagerDimensions.Mutable getDimensions();

   float getBreastSize();

   void setBreastSize(float var1);

   default void renderCommon(class_4587 matrices, class_4588 vertices, int light, int overlay, float red, float green, float blue, float alpha) {
      float headSize = this.getDimensions().getHead();
      matrices.method_22903();
      matrices.method_22905(headSize, headSize, headSize);
      this.getCommonHeadParts().forEach(a -> a.method_22699(matrices, vertices, light, overlay, red, green, blue, alpha));
      matrices.method_22909();
      this.getCommonBodyParts().forEach(a -> a.method_22699(matrices, vertices, light, overlay, red, green, blue, alpha));
      if (this.getBreastPart().field_3665 && this.getBodyPart().field_3665) {
         float breastSize = this.getBreastSize() * this.getDimensions().getBreasts();
         if (breastSize > 0.0F) {
            matrices.method_22903();
            matrices.method_22905(breastSize * 0.2F + 1.05F, breastSize * 0.75F + 0.75F, breastSize * 0.75F + 0.75F);

            for (class_630 part : this.getBreastParts()) {
               part.method_22699(matrices, vertices, light, overlay, red, green, blue, alpha);
            }

            matrices.method_22909();
         }
      }
   }

   default void applyVillagerDimensions(VillagerLike<?> villager, boolean isSneaking) {
      this.getDimensions().set(villager.getVillagerDimensions());
      this.setBreastSize(villager.getGenetics().getBreastSize());
      this.getBreastPart().field_3665 = villager.getGenetics().getGender() == Gender.FEMALE;

      for (class_630 part : this.getBreastParts()) {
         part.field_3654 = 0.9424779F + this.getBodyPart().field_3654;
         float cy = 0.0F;
         float cz = 0.0F;
         if (isSneaking) {
            cy = 3.0F;
            cz = 1.5F;
         }

         part.method_2851(0.25F, (float)(5.0 - Math.pow(this.getBreastSize(), 0.5) * 2.5 + cy), -1.5F + this.getBreastSize() * 0.25F + cz);
      }
   }

   default void copyCommonAttributes(CommonVillagerModel<T> target) {
      target.getDimensions().set(this.getDimensions());
      target.setBreastSize(this.getBreastSize());
   }

   static VillagerLike<?> getVillager(class_1937 world, UUID uuid) {
      if (MCAClient.fallbackVillager == null) {
         MCAClient.fallbackVillager = (VillagerEntityMCA)((class_1299)EntitiesMCA.MALE_VILLAGER.get()).method_5883(world);
      }

      return MCAClient.playerData.getOrDefault(uuid, MCAClient.fallbackVillager);
   }

   static VillagerLike<?> getVillager(class_1297 villager) {
      return villager instanceof VillagerLike<?> v ? v : getVillager(villager.method_37908(), villager.method_5667());
   }
}
