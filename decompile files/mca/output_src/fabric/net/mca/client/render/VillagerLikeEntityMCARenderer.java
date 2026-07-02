package fabric.net.mca.client.render;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import fabric.net.mca.Config;
import fabric.net.mca.client.gui.VillagerEditorScreen;
import fabric.net.mca.client.model.VillagerEntityBaseModelMCA;
import fabric.net.mca.client.model.VillagerEntityModelMCA;
import fabric.net.mca.entity.VillagerLike;
import fabric.net.mca.entity.ai.relationship.AgeState;
import java.util.Map;
import net.minecraft.class_1068;
import net.minecraft.class_1308;
import net.minecraft.class_1657;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4844;
import net.minecraft.class_5605;
import net.minecraft.class_5607;
import net.minecraft.class_909;
import net.minecraft.class_970;
import net.minecraft.class_5617.class_5618;
import org.jetbrains.annotations.Nullable;

public class VillagerLikeEntityMCARenderer<T extends class_1308 & VillagerLike<T>> extends class_909<T, VillagerEntityModelMCA<T>> {
   private static final class_2960 TEXTURE = new class_2960("textures/entity/steve.png");

   public VillagerLikeEntityMCARenderer(class_5618 ctx, VillagerEntityModelMCA<T> model) {
      super(ctx, model, 0.5F);
      this.method_4046(new class_970(this, this.createArmorModel(0.3F), this.createArmorModel(0.55F), ctx.method_48481()));
   }

   private VillagerEntityBaseModelMCA<T> createArmorModel(float modelSize) {
      return new VillagerEntityBaseModelMCA(class_5607.method_32110(VillagerEntityBaseModelMCA.getModelData(new class_5605(modelSize)), 64, 32).method_32109());
   }

   protected void scale(T villager, class_4587 matrices, float tickDelta) {
      float height = villager.getRawScaleFactor();
      float width = villager.getHorizontalScaleFactor();
      matrices.method_22905(width, height, width);
      if (villager.getAgeState() == AgeState.BABY && !villager.method_5765()) {
         matrices.method_46416(0.0F, 0.6F, 0.0F);
      }
   }

   @Nullable
   protected class_1921 getRenderLayer(T entity, boolean showBody, boolean translucent, boolean showOutlines) {
      if (entity.hasCustomSkin()) {
         class_310 minecraftClient = class_310.method_1551();
         Map<Type, MinecraftProfileTexture> map = minecraftClient.method_1582().method_4654(entity.getGameProfile());
         return map.containsKey(Type.SKIN)
            ? class_1921.method_23580(minecraftClient.method_1582().method_4656(map.get(Type.SKIN), Type.SKIN))
            : class_1921.method_23578(class_1068.method_4648(class_4844.method_43343(entity.getGameProfile())));
      } else {
         return null;
      }
   }

   protected boolean method_4071(T villager) {
      class_1657 player = class_310.method_1551().field_1724;
      return villager.method_5797() != null
         && !(class_310.method_1551().field_1755 instanceof VillagerEditorScreen)
         && player != null
         && Config.getInstance().showNameTags
         && player.method_5858(villager) < Math.pow(Config.getInstance().nameTagDistance, 2.0)
         && !villager.method_5756(player);
   }

   public class_2960 getTexture(T mobEntity) {
      return TEXTURE;
   }

   protected boolean isShaking(T entity) {
      return entity.getInfectionProgress() > 0.2F;
   }
}
