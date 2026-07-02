package fabric.net.mca;

import com.google.common.collect.ImmutableSet;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import fabric.net.mca.entity.ai.PointOfInterestTypeMCA;
import fabric.net.mca.mixin.MixinVillagerProfession;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.class_1792;
import net.minecraft.class_2248;
import net.minecraft.class_2960;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_3852;
import net.minecraft.class_4158;
import net.minecraft.class_5321;
import net.minecraft.class_6880;
import net.minecraft.class_7924;
import org.jetbrains.annotations.Nullable;

public interface ProfessionsMCA {
   DeferredRegister<class_3852> PROFESSIONS = DeferredRegister.create("mca", class_7924.field_41234);
   RegistrySupplier<class_3852> OUTLAW = register("outlaw", false, true, true, class_4158.field_39277, class_3852.field_39308, class_3417.field_20673);
   RegistrySupplier<class_3852> GUARD = register("guard", false, true, false, class_4158.field_39277, class_3852.field_39308, class_3417.field_20669);
   RegistrySupplier<class_3852> ARCHER = register("archer", false, true, false, class_4158.field_39277, class_3852.field_39308, class_3417.field_20675);
   RegistrySupplier<class_3852> ADVENTURER = register("adventurer", true, true, true, class_4158.field_39277, class_3852.field_39308, class_3417.field_20675);
   RegistrySupplier<class_3852> MERCENARY = register("mercenary", false, true, true, class_4158.field_39277, class_3852.field_39308, class_3417.field_20675);
   RegistrySupplier<class_3852> CULTIST = register("cultist", true, true, true, class_4158.field_39277, class_3852.field_39308, class_3417.field_20675);
   Set<class_3852> canNotTrade = new HashSet<>();
   Set<class_3852> isImportant = new HashSet<>();
   Set<class_3852> needsNoHome = new HashSet<>();

   static void bootstrap() {
      PROFESSIONS.register();
      PointOfInterestTypeMCA.bootstrap();
      canNotTrade.add(class_3852.field_17051);
      canNotTrade.add(class_3852.field_17062);
   }

   private static RegistrySupplier<class_3852> register(
      String name, boolean canTradeWith, boolean important, boolean needsNoHome, class_5321<class_4158> heldWorkstation, @Nullable class_3414 workSound
   ) {
      return register(
         name, canTradeWith, important, needsNoHome, entry -> entry.method_40225(heldWorkstation), entry -> entry.method_40225(heldWorkstation), workSound
      );
   }

   static RegistrySupplier<class_3852> register(
      String name,
      boolean canTradeWith,
      boolean important,
      boolean needsNoHome,
      Predicate<class_6880<class_4158>> heldWorkstation,
      Predicate<class_6880<class_4158>> acquirableWorkstation,
      @Nullable class_3414 workSound
   ) {
      return register(name, canTradeWith, important, needsNoHome, heldWorkstation, acquirableWorkstation, ImmutableSet.of(), ImmutableSet.of(), workSound);
   }

   static RegistrySupplier<class_3852> register(
      String name,
      boolean canTradeWith,
      boolean important,
      boolean needsNoHome,
      class_5321<class_4158> heldWorkstation,
      ImmutableSet<class_1792> gatherableItems,
      ImmutableSet<class_2248> secondaryJobSites,
      @Nullable class_3414 workSound
   ) {
      return register(
         name,
         canTradeWith,
         important,
         needsNoHome,
         entry -> entry.method_40225(heldWorkstation),
         entry -> entry.method_40225(heldWorkstation),
         gatherableItems,
         secondaryJobSites,
         workSound
      );
   }

   static RegistrySupplier<class_3852> register(
      String name,
      boolean canTradeWith,
      boolean important,
      boolean needsNoHome,
      Predicate<class_6880<class_4158>> heldWorkstation,
      Predicate<class_6880<class_4158>> acquirableWorkstation,
      ImmutableSet<class_1792> gatherableItems,
      ImmutableSet<class_2248> secondaryJobSites,
      @Nullable class_3414 workSound
   ) {
      class_2960 id = new class_2960("mca", name);
      return PROFESSIONS.register(
         id,
         () -> {
            class_3852 result = MixinVillagerProfession.init(
               id.toString().replace(':', '.'), heldWorkstation, acquirableWorkstation, gatherableItems, secondaryJobSites, workSound
            );
            if (!canTradeWith) {
               canNotTrade.add(result);
            }

            if (important) {
               isImportant.add(result);
            }

            if (needsNoHome) {
               ProfessionsMCA.needsNoHome.add(result);
            }

            return result;
         }
      );
   }

   static String getFavoredBuilding(class_3852 profession) {
      if (class_3852.field_17054 != profession && class_3852.field_17060 != profession && class_3852.field_17055 != profession) {
         return GUARD.get() != profession && ARCHER.get() != profession ? null : "inn";
      } else {
         return "library";
      }
   }
}
