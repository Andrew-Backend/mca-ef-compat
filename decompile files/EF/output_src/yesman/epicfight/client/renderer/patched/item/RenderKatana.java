package yesman.epicfight.client.renderer.patched.item;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Objects;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.item.EpicFightItems;

public class RenderKatana extends RenderItemBase {
   private final ItemStack sheathStack;

   public RenderKatana(JsonElement jsonElement) {
      super(jsonElement);
      if (jsonElement.getAsJsonObject().has("sheath")) {
         this.sheathStack = new ItemStack(
            Objects.requireNonNull((Item)ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(jsonElement.getAsJsonObject().get("sheath").getAsString())))
         );
      } else {
         this.sheathStack = new ItemStack((ItemLike)EpicFightItems.UCHIGATANA_SHEATH.get());
      }
   }

   @Override
   public void renderItemInHand(
      ItemStack stack,
      LivingEntityPatch<?> entitypatch,
      InteractionHand hand,
      OpenMatrix4f[] poses,
      MultiBufferSource buffer,
      PoseStack poseStack,
      int packedLight,
      float partialTicks
   ) {
      OpenMatrix4f modelMatrix = this.getCorrectionMatrix(entitypatch, InteractionHand.MAIN_HAND, poses);
      poseStack.m_85836_();
      MathUtils.mulStack(poseStack, modelMatrix);
      itemRenderer.m_269128_(stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, packedLight, OverlayTexture.f_118083_, poseStack, buffer, null, 0);
      poseStack.m_85849_();
      modelMatrix = this.getCorrectionMatrix(entitypatch, InteractionHand.OFF_HAND, poses);
      poseStack.m_85836_();
      MathUtils.mulStack(poseStack, modelMatrix);
      itemRenderer.m_269128_(this.sheathStack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, packedLight, OverlayTexture.f_118083_, poseStack, buffer, null, 0);
      poseStack.m_85849_();
   }
}
