package quilt.net.mca.mixin;

import net.minecraft.class_1299;
import net.minecraft.class_1304;
import net.minecraft.class_1429;
import net.minecraft.class_1538;
import net.minecraft.class_1639;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;
import net.minecraft.class_1937;
import net.minecraft.class_1948;
import net.minecraft.class_1959;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_3218;
import net.minecraft.class_3414;
import net.minecraft.class_3730;
import net.minecraft.class_6053;
import net.minecraft.class_1317.class_1319;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quilt.net.mca.advancement.criterion.CriterionMCA;
import quilt.net.mca.item.ItemsMCA;
import quilt.net.mca.util.WorldUtils;

@Mixin(class_6053.class)
public abstract class MixinGoatEntity extends class_1429 {
   protected MixinGoatEntity(class_1299<? extends class_1429> entityType, class_1937 world) {
      super(entityType, world);
   }

   @Inject(method = "method_35180()Lnet/minecraft/class_3414;", at = @At("HEAD"))
   protected void getMilkingSound(CallbackInfoReturnable<class_3414> cir) {
      if (!this.method_37908().field_9236 && this.method_37908().method_8419()) {
         long time = this.method_37908().method_8532() % 24000L;
         class_2338 pos = this.method_24515();
         if (time > 16000L
            && time < 20000L
            && ((class_1959)this.method_37908().method_23753(pos).comp_349()).method_33599(pos)
            && class_1948.method_8660(class_1319.field_6317, this.method_37908(), pos, class_1299.field_6076)) {
            class_1639 ancientCultist = (class_1639)class_1299.field_6076.method_5883(this.method_37908());
            if (ancientCultist != null) {
               ancientCultist.method_5814(pos.method_10263(), pos.method_10264(), pos.method_10260());
               WorldUtils.spawnEntity(this.method_37908(), ancientCultist, class_3730.field_16467);
               ancientCultist.method_5673(class_1304.field_6169, new class_1799(class_1802.field_8862));
               ancientCultist.method_5673(class_1304.field_6174, new class_1799(class_1802.field_8678));
               ancientCultist.method_5673(class_1304.field_6172, new class_1799(class_1802.field_8416));
               ancientCultist.method_5673(class_1304.field_6166, new class_1799(class_1802.field_8753));
               ancientCultist.method_5673(class_1304.field_6173, new class_1799(class_1802.field_8845));
               ancientCultist.method_5673(class_1304.field_6171, new class_1799((class_1935)ItemsMCA.BOOK_CULT_ANCIENT.get()));
               ancientCultist.method_5946(class_1304.field_6171, 1.0F);
               ancientCultist.method_5665(class_2561.method_43471("entity.mca.ancient_cultist"));
               ((class_3218)this.method_37908())
                  .method_18456()
                  .stream()
                  .filter(p -> p.method_5739(this) < 30.0F)
                  .forEach(p -> CriterionMCA.GENERIC_EVENT_CRITERION.trigger(p, "ancient_cultists"));
               this.method_5768();
               this.method_37908().method_8509(10);
               class_1538 bolt = (class_1538)class_1299.field_6112.method_5883(this.method_37908());
               if (bolt != null) {
                  bolt.method_29498(true);
                  bolt.method_30634(pos.method_10263() + 0.5F, pos.method_10264(), pos.method_10260() + 0.5F);
                  this.method_37908().method_8649(bolt);
               }
            }
         }
      }
   }
}
