package quilt.net.mca.item;

import java.util.List;
import net.minecraft.class_124;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1271;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1829;
import net.minecraft.class_1834;
import net.minecraft.class_1836;
import net.minecraft.class_1838;
import net.minecraft.class_1839;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_2680;
import net.minecraft.class_3222;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_5819;
import net.minecraft.class_1792.class_1793;
import org.jetbrains.annotations.Nullable;
import quilt.net.mca.SoundsMCA;
import quilt.net.mca.TagsMCA;
import quilt.net.mca.advancement.criterion.CriterionMCA;
import quilt.net.mca.block.TombstoneBlock;
import quilt.net.mca.entity.EntitiesMCA;
import quilt.net.mca.util.localization.FlowingText;

public class ScytheItem extends class_1829 {
   public ScytheItem(class_1793 settings) {
      super(class_1834.field_8929, 10, -2.4F, settings);
   }

   public void method_7851(class_1799 stack, @Nullable class_1937 world, List<class_2561> tooltip, class_1836 context) {
      tooltip.addAll(FlowingText.wrap(class_2561.method_43471(this.method_7866(stack) + ".tooltip").method_27692(class_124.field_1080), 160));
   }

   public class_1839 method_7853(class_1799 stack) {
      return class_1839.field_8949;
   }

   public int method_7881(class_1799 stack) {
      return 72000;
   }

   public void method_7888(class_1799 stack, class_1937 world, class_1297 entity, int slot, boolean selected) {
      if (entity instanceof class_1309 living) {
         boolean active = stack.method_7948().method_10577("active");
         class_5819 r = entity.method_37908().field_9229;
         if (active != selected) {
            stack.method_7948().method_10556("active", selected);
            float baseVolume = selected ? 0.75F : 0.25F;
            entity.method_37908()
               .method_8396(
                  null,
                  entity.method_24515(),
                  (class_3414)SoundsMCA.REAPER_SCYTHE_OUT.get(),
                  entity.method_5634(),
                  baseVolume + r.method_43057() / 2.0F,
                  0.65F + r.method_43057() / 10.0F
               );
         }

         if (selected && living.field_6279 == -1) {
            entity.method_37908().method_8396(null, entity.method_24515(), (class_3414)SoundsMCA.REAPER_SCYTHE_SWING.get(), entity.method_5634(), 0.25F, 1.0F);
         }
      }
   }

   public class_1271<class_1799> method_7836(class_1937 world, class_1657 user, class_1268 hand) {
      user.method_6019(hand);
      return super.method_7836(world, user, hand);
   }

   public class_1269 method_7884(class_1838 context) {
      if (hasSoul(context.method_8041())) {
         class_1269 result = use(context, false);
         if (result == class_1269.field_5812) {
            setSoul(context.method_8041(), false);
         }

         if (result != class_1269.field_5811) {
            return result;
         }
      }

      return super.method_7884(context);
   }

   public boolean method_7886(class_1799 stack) {
      return super.method_7886(stack) || hasSoul(stack);
   }

   public boolean method_7873(class_1799 stack, class_1309 target, class_1309 attacker) {
      if (target.method_37908().field_9229.method_43048(50) > 40) {
         target.method_6092(new class_1293(class_1294.field_5920, 1000, 1));
      }

      class_3414 sound = (class_3414)SoundsMCA.REAPER_SCYTHE_OUT.get();
      if (!hasSoul(stack)
         && target.method_29504()
         && (target.method_5864() == EntitiesMCA.MALE_VILLAGER.get() || target.method_5864() == EntitiesMCA.FEMALE_VILLAGER.get())) {
         setSoul(stack, true);
         sound = class_3417.field_19167;
         if (attacker instanceof class_3222) {
            CriterionMCA.GENERIC_EVENT_CRITERION.trigger((class_3222)attacker, "scytheKill");
         }
      }

      class_5819 r = attacker.method_37908().field_9229;
      attacker.method_37908()
         .method_8396(null, attacker.method_24515(), sound, attacker.method_5634(), 0.75F + r.method_43057() / 2.0F, 0.75F + r.method_43057() / 2.0F);
      return super.method_7873(stack, target, attacker);
   }

   public boolean method_7878(class_1799 stack, class_1799 ingredient) {
      return stack.method_7909() == ingredient.method_7909();
   }

   public static void setSoul(class_1799 stack, boolean soul) {
      stack.method_7948().method_10556("hasSoul", soul);
   }

   public static boolean hasSoul(class_1799 stack) {
      return stack.method_7985() && stack.method_7969().method_10577("hasSoul");
   }

   public static class_1269 use(class_1838 context, boolean cure) {
      class_1937 world = context.method_8045();
      class_2338 pos = context.method_8037();
      class_2680 state = world.method_8320(pos);
      return state.method_26164(TagsMCA.Blocks.TOMBSTONES)
         ? TombstoneBlock.Data.of(world.method_8321(pos)).filter(TombstoneBlock.Data::hasEntity).map(data -> {
            if (!context.method_8045().field_9236) {
               CriterionMCA.GENERIC_EVENT_CRITERION.trigger((class_3222)context.method_8036(), cure ? "staffOfLife" : "scytheRevive");
            }

            if (!world.field_9236 && !data.isResurrecting()) {
               data.startResurrecting(cure);
               return class_1269.field_5812;
            } else {
               return class_1269.field_5811;
            }
         }).orElse(class_1269.field_5814)
         : class_1269.field_5811;
   }
}
