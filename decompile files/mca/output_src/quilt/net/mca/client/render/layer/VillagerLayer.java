package quilt.net.mca.client.render.layer;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.class_1309;
import net.minecraft.class_151;
import net.minecraft.class_1657;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_572;
import net.minecraft.class_922;
import org.jetbrains.annotations.Nullable;
import quilt.net.mca.MCA;
import quilt.net.mca.MCAClient;
import quilt.net.mca.client.model.CommonVillagerModel;
import quilt.net.mca.client.model.PlayerEntityExtendedModel;
import quilt.net.mca.client.model.VillagerEntityModelMCA;

public abstract class VillagerLayer<T extends class_1309, M extends class_572<T>> extends class_3887<T, M> {
   private static final float[] DEFAULT_COLOR = new float[]{1.0F, 1.0F, 1.0F};
   private static final Map<String, class_2960> TEXTURE_CACHE = Maps.newHashMap();
   private static final Map<class_2960, Boolean> TEXTURE_EXIST_CACHE = Maps.newHashMap();
   public final M model;

   public VillagerLayer(class_3883<T, M> renderer, M model) {
      super(renderer);
      this.model = model;
   }

   @Nullable
   public class_2960 getSkin(T villager) {
      return null;
   }

   @Nullable
   protected class_2960 getOverlay(T villager) {
      return null;
   }

   public float[] getColor(T villager, float tickDelta) {
      return DEFAULT_COLOR;
   }

   protected boolean isTranslucent() {
      return false;
   }

   public void render(
      class_4587 transform,
      class_4597 provider,
      int light,
      T villager,
      float limbAngle,
      float limbDistance,
      float tickDelta,
      float animationProgress,
      float headYaw,
      float headPitch
   ) {
      class_310 client = class_310.method_1551();
      boolean visible = !villager.method_5767();
      boolean glowing = client.method_27022(villager);
      if (!CommonVillagerModel.getVillager(villager).hasCustomSkin()) {
         if (!(villager instanceof class_1657) || MCAClient.useVillagerRenderer(villager.method_5667())) {
            if (this.model instanceof VillagerEntityModelMCA layer) {
               layer.copyVisibility((M)this.method_17165());
            }

            if (this.model instanceof PlayerEntityExtendedModel layer) {
               layer.copyVisibility((M)this.method_17165());
            }

            ((class_572)this.method_17165()).method_2818(this.model);
            this.renderFinal(transform, provider, light, villager, tickDelta, visible, glowing);
         }
      }
   }

   public void renderFinal(class_4587 transform, class_4597 provider, int light, T villager, float tickDelta, boolean visible, boolean glowing) {
      int tint = class_922.method_23622(villager, 0.0F);
      class_2960 skin = this.getSkin(villager);
      if (this.canUse(skin)) {
         float[] color = this.getColor(villager, tickDelta);
         this.renderModel(transform, provider, light, this.model, color[0], color[1], color[2], skin, tint, visible, glowing);
      }

      class_2960 overlay = this.getOverlay(villager);
      if (!Objects.equals(skin, overlay) && this.canUse(overlay)) {
         this.renderModel(transform, provider, light, this.model, 1.0F, 1.0F, 1.0F, overlay, tint, visible, glowing);
      }
   }

   @Nullable
   protected class_1921 getRenderLayer(class_2960 texture, boolean showBody, boolean translucent, boolean showOutline) {
      if (translucent) {
         return class_1921.method_29379(texture);
      } else if (showBody) {
         return this.model.method_23500(texture);
      } else {
         return showOutline ? class_1921.method_23287(texture) : null;
      }
   }

   private void renderModel(
      class_4587 transform,
      class_4597 provider,
      int light,
      M model,
      float r,
      float g,
      float b,
      class_2960 texture,
      int overlay,
      boolean visible,
      boolean glowing
   ) {
      class_1921 layer = this.getRenderLayer(texture, visible, this.isTranslucent(), glowing);
      if (layer != null) {
         class_4588 buffer = provider.getBuffer(layer);
         model.method_2828(transform, buffer, light, overlay, r, g, b, 1.0F);
      }
   }

   public final boolean canUse(class_2960 texture) {
      return TEXTURE_EXIST_CACHE.computeIfAbsent(
         texture,
         s -> texture != null && texture.method_12836().equals("immersive_library")
            ? true
            : texture != null && class_310.method_1551().method_1478().method_14486(texture).isPresent()
      );
   }

   @Nullable
   protected final class_2960 cached(String name, Function<String, class_2960> supplier) {
      return TEXTURE_CACHE.computeIfAbsent(name, s -> {
         try {
            return supplier.apply(s);
         } catch (class_151 ignored) {
            return null;
         }
      });
   }

   static {
      TEXTURE_EXIST_CACHE.put(MCA.locate("temp"), true);
   }
}
