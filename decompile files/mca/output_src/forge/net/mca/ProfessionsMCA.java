package forge.net.mca;

import com.google.common.collect.ImmutableSet;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import forge.net.mca.entity.ai.PointOfInterestTypeMCA;
import forge.net.mca.mixin.MixinVillagerProfession;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public interface ProfessionsMCA {
   DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create("mca", Registries.f_256749_);
   RegistrySupplier<VillagerProfession> OUTLAW = register("outlaw", false, true, true, PoiType.f_218034_, VillagerProfession.f_219627_, SoundEvents.f_12567_);
   RegistrySupplier<VillagerProfession> GUARD = register("guard", false, true, false, PoiType.f_218034_, VillagerProfession.f_219627_, SoundEvents.f_12510_);
   RegistrySupplier<VillagerProfession> ARCHER = register("archer", false, true, false, PoiType.f_218034_, VillagerProfession.f_219627_, SoundEvents.f_12569_);
   RegistrySupplier<VillagerProfession> ADVENTURER = register(
      "adventurer", true, true, true, PoiType.f_218034_, VillagerProfession.f_219627_, SoundEvents.f_12569_
   );
   RegistrySupplier<VillagerProfession> MERCENARY = register(
      "mercenary", false, true, true, PoiType.f_218034_, VillagerProfession.f_219627_, SoundEvents.f_12569_
   );
   RegistrySupplier<VillagerProfession> CULTIST = register("cultist", true, true, true, PoiType.f_218034_, VillagerProfession.f_219627_, SoundEvents.f_12569_);
   Set<VillagerProfession> canNotTrade = new HashSet<>();
   Set<VillagerProfession> isImportant = new HashSet<>();
   Set<VillagerProfession> needsNoHome = new HashSet<>();

   static void bootstrap() {
      PROFESSIONS.register();
      PointOfInterestTypeMCA.bootstrap();
      canNotTrade.add(VillagerProfession.f_35585_);
      canNotTrade.add(VillagerProfession.f_35596_);
   }

   private static RegistrySupplier<VillagerProfession> register(
      String name, boolean canTradeWith, boolean important, boolean needsNoHome, ResourceKey<PoiType> heldWorkstation, @Nullable SoundEvent workSound
   ) {
      return register(
         name, canTradeWith, important, needsNoHome, entry -> entry.m_203565_(heldWorkstation), entry -> entry.m_203565_(heldWorkstation), workSound
      );
   }

   static RegistrySupplier<VillagerProfession> register(
      String name,
      boolean canTradeWith,
      boolean important,
      boolean needsNoHome,
      Predicate<Holder<PoiType>> heldWorkstation,
      Predicate<Holder<PoiType>> acquirableWorkstation,
      @Nullable SoundEvent workSound
   ) {
      return register(name, canTradeWith, important, needsNoHome, heldWorkstation, acquirableWorkstation, ImmutableSet.of(), ImmutableSet.of(), workSound);
   }

   static RegistrySupplier<VillagerProfession> register(
      String name,
      boolean canTradeWith,
      boolean important,
      boolean needsNoHome,
      ResourceKey<PoiType> heldWorkstation,
      ImmutableSet<Item> gatherableItems,
      ImmutableSet<Block> secondaryJobSites,
      @Nullable SoundEvent workSound
   ) {
      return register(
         name,
         canTradeWith,
         important,
         needsNoHome,
         entry -> entry.m_203565_(heldWorkstation),
         entry -> entry.m_203565_(heldWorkstation),
         gatherableItems,
         secondaryJobSites,
         workSound
      );
   }

   static RegistrySupplier<VillagerProfession> register(
      String name,
      boolean canTradeWith,
      boolean important,
      boolean needsNoHome,
      Predicate<Holder<PoiType>> heldWorkstation,
      Predicate<Holder<PoiType>> acquirableWorkstation,
      ImmutableSet<Item> gatherableItems,
      ImmutableSet<Block> secondaryJobSites,
      @Nullable SoundEvent workSound
   ) {
      ResourceLocation id = new ResourceLocation("mca", name);
      return PROFESSIONS.register(
         id,
         () -> {
            VillagerProfession result = MixinVillagerProfession.init(
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

   static String getFavoredBuilding(VillagerProfession profession) {
      if (VillagerProfession.f_35588_ != profession && VillagerProfession.f_35594_ != profession && VillagerProfession.f_35589_ != profession) {
         return GUARD.get() != profession && ARCHER.get() != profession ? null : "inn";
      } else {
         return "library";
      }
   }
}
