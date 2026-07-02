package quilt.net.mca.item;

import dev.architectury.core.item.ArchitecturySpawnEggItem;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.class_124;
import net.minecraft.class_1747;
import net.minecraft.class_1761;
import net.minecraft.class_1767;
import net.minecraft.class_1792;
import net.minecraft.class_2248;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2960;
import net.minecraft.class_7924;
import net.minecraft.class_1792.class_1793;
import quilt.net.mca.MCA;
import quilt.net.mca.TagsMCA;
import quilt.net.mca.block.BlocksMCA;
import quilt.net.mca.client.book.Book;
import quilt.net.mca.client.book.CivilRegistryBook;
import quilt.net.mca.client.book.pages.CenteredTextPage;
import quilt.net.mca.client.book.pages.DynamicListPage;
import quilt.net.mca.client.book.pages.ScribbleTextPage;
import quilt.net.mca.client.book.pages.TitlePage;
import quilt.net.mca.entity.CribWoodType;
import quilt.net.mca.entity.EntitiesMCA;
import quilt.net.mca.entity.ai.relationship.Gender;
import quilt.net.mca.resources.Supporters;

public interface ItemsMCA {
   DeferredRegister<class_1792> ITEMS = DeferredRegister.create("mca", class_7924.field_41197);
   DeferredRegister<class_1761> ITEM_GROUPS = DeferredRegister.create("mca", class_7924.field_44688);
   RegistrySupplier<class_1761> MCA_GROUP = registerTab(
      "mca_tab",
      () -> CreativeTabRegistry.create(class_2561.method_43471("itemGroup.mca.mca_tab"), () -> ((class_1792)ItemsMCA.ENGAGEMENT_RING.get()).method_7854())
   );
   RegistrySupplier<class_1792> MALE_VILLAGER_SPAWN_EGG = register(
      "male_villager_spawn_egg", () -> new ArchitecturySpawnEggItem(EntitiesMCA.MALE_VILLAGER, 6200063, 3368636, baseProps())
   );
   RegistrySupplier<class_1792> FEMALE_VILLAGER_SPAWN_EGG = register(
      "female_villager_spawn_egg", () -> new ArchitecturySpawnEggItem(EntitiesMCA.FEMALE_VILLAGER, 15228065, 14890636, baseProps())
   );
   RegistrySupplier<class_1792> MALE_ZOMBIE_VILLAGER_SPAWN_EGG = register(
      "male_zombie_villager_spawn_egg", () -> new ArchitecturySpawnEggItem(EntitiesMCA.MALE_ZOMBIE_VILLAGER, 6208255, 3385020, baseProps())
   );
   RegistrySupplier<class_1792> FEMALE_ZOMBIE_VILLAGER_SPAWN_EGG = register(
      "female_zombie_villager_spawn_egg", () -> new ArchitecturySpawnEggItem(EntitiesMCA.FEMALE_ZOMBIE_VILLAGER, 15248545, 14919308, baseProps())
   );
   RegistrySupplier<class_1792> GRIM_REAPER_SPAWN_EGG = register(
      "grim_reaper_spawn_egg", () -> new ArchitecturySpawnEggItem(EntitiesMCA.GRIM_REAPER, 3151125, 2759732, baseProps())
   );
   RegistrySupplier<class_1792> BABY_BOY = register("baby_boy", () -> new BabyItem(Gender.MALE, baseProps().method_7889(1)));
   RegistrySupplier<class_1792> BABY_GIRL = register("baby_girl", () -> new BabyItem(Gender.FEMALE, baseProps().method_7889(1)));
   RegistrySupplier<class_1792> SIRBEN_BABY_BOY = register(
      "sirben_baby_boy", () -> new SirbenBabyItem(Gender.MALE, baseProps().arch$tab((class_1761)null).method_7889(1))
   );
   RegistrySupplier<class_1792> SIRBEN_BABY_GIRL = register(
      "sirben_baby_girl", () -> new SirbenBabyItem(Gender.FEMALE, baseProps().arch$tab((class_1761)null).method_7889(1))
   );
   RegistrySupplier<class_1792> WEDDING_RING = register("wedding_ring", () -> new WeddingRingItem(unstackableProps()));
   RegistrySupplier<class_1792> WEDDING_RING_RG = register("wedding_ring_rg", () -> new WeddingRingItem(unstackableProps()));
   RegistrySupplier<class_1792> ENGAGEMENT_RING = register("engagement_ring", () -> new EngagementRingItem(unstackableProps()));
   RegistrySupplier<class_1792> ENGAGEMENT_RING_RG = register("engagement_ring_rg", () -> new EngagementRingItem(unstackableProps()));
   RegistrySupplier<class_1792> MATCHMAKERS_RING = register("matchmakers_ring", () -> new MatchmakersRingItem(baseProps().method_7889(2)));
   RegistrySupplier<class_1792> VILLAGER_EDITOR = register("villager_editor", () -> new VillagerEditorItem(baseProps()));
   RegistrySupplier<class_1792> STAFF_OF_LIFE = register("staff_of_life", () -> new StaffOfLifeItem(baseProps().method_7895(10)));
   RegistrySupplier<class_1792> WHISTLE = register("whistle", () -> new WhistleItem(baseProps()));
   RegistrySupplier<class_1792> BLUEPRINT = register("blueprint", () -> new BlueprintItem(baseProps()));
   RegistrySupplier<class_1792> FAMILY_TREE = register("family_tree", () -> new FamilyTreeItem(baseProps()));
   RegistrySupplier<class_1792> VILLAGER_TRACKER = register("villager_tracker", () -> new VillagerTrackerItem(baseProps().method_7889(1)));
   RegistrySupplier<class_1792> SCYTHE = register("scythe", () -> new ScytheItem(baseProps()));
   RegistrySupplier<class_1792> BOUQUET = register("bouquet", () -> new BouquetItem(baseProps()));
   RegistrySupplier<class_1792> POTION_OF_FEMINITY = register(
      "potion_of_feminity", () -> new PotionOfMetamorphosisItem(baseProps().method_7889(1), Gender.FEMALE)
   );
   RegistrySupplier<class_1792> POTION_OF_MASCULINITY = register(
      "potion_of_masculinity", () -> new PotionOfMetamorphosisItem(baseProps().method_7889(1), Gender.MALE)
   );
   RegistrySupplier<class_1792> NEEDLE_AND_THREAD = register("needle_and_thread", () -> new NeedleAndThreadItem(baseProps().method_7895(8)));
   RegistrySupplier<class_1792> COMB = register("comb", () -> new CombItem(baseProps().method_7895(8)));
   RegistrySupplier<class_1792> BOOK_DEATH = register(
      "book_death",
      () -> new ExtendedWrittenBookItem(
         baseProps(),
         new Book("death")
            .setBackground(MCA.locate("textures/gui/books/death.png"))
            .setTextFormatting(class_124.field_1068)
            .setTextShadow(true)
            .addPage(new TitlePage("death", class_124.field_1080))
            .addSimplePages(3, 0)
            .addPage(new ScribbleTextPage(MCA.locate("textures/gui/scribbles/test.png"), "death", 3))
            .addSimplePages(9, 4)
      )
   );
   RegistrySupplier<class_1792> BOOK_ROMANCE = register(
      "book_romance",
      () -> new ExtendedWrittenBookItem(
         baseProps(), new Book("romance").setBackground(MCA.locate("textures/gui/books/romance.png")).addPage(new TitlePage("romance")).addSimplePages(10)
      )
   );
   RegistrySupplier<class_1792> BOOK_FAMILY = register(
      "book_family",
      () -> new ExtendedWrittenBookItem(
         baseProps(), new Book("family").setBackground(MCA.locate("textures/gui/books/family.png")).addPage(new TitlePage("family")).addSimplePages(6)
      )
   );
   RegistrySupplier<class_1792> BOOK_ROSE_GOLD = register(
      "book_rose_gold",
      () -> new ExtendedWrittenBookItem(
         baseProps(), new Book("rose_gold").setBackground(MCA.locate("textures/gui/books/rose_gold.png")).addPage(new TitlePage("rose_gold")).addSimplePages(4)
      )
   );
   RegistrySupplier<class_1792> BOOK_INFECTION = register(
      "book_infection",
      () -> new ExtendedWrittenBookItem(
         baseProps(), new Book("infection").setBackground(MCA.locate("textures/gui/books/infection.png")).addPage(new TitlePage("infection")).addSimplePages(6)
      )
   );
   RegistrySupplier<class_1792> BOOK_BLUEPRINT = register(
      "book_blueprint",
      () -> new ExtendedWrittenBookItem(
         baseProps(),
         new Book("blueprint")
            .setBackground(MCA.locate("textures/gui/books/blueprint.png"))
            .setTextFormatting(class_124.field_1068)
            .setTextShadow(true)
            .addPage(new TitlePage("blueprint", class_124.field_1068))
            .addSimplePages(6)
      )
   );
   RegistrySupplier<class_1792> BOOK_SUPPORTERS = register(
      "book_supporters",
      () -> new ExtendedWrittenBookItem(
         baseProps(),
         new Book("supporters")
            .setBackground(MCA.locate("textures/gui/books/supporters.png"))
            .addPage(new TitlePage("supporters"))
            .addPage(
               new DynamicListPage(
                  "mca.books.supporters.contributors",
                  page -> Supporters.getSupporterGroup("mca:contributors")
                     .stream()
                     .map(s -> class_2561.method_43470(s).method_27692(class_124.field_1077))
                     .collect(Collectors.toList())
               )
            )
            .addPage(
               new DynamicListPage(
                  "mca.books.supporters.wiki",
                  page -> Supporters.getSupporterGroup("mca:wiki")
                     .stream()
                     .map(s -> class_2561.method_43470(s).method_27692(class_124.field_1065))
                     .collect(Collectors.toList())
               )
            )
            .addPage(
               new DynamicListPage(
                  "mca.books.supporters.patrons",
                  page -> Supporters.getSupporterGroup("mca:patrons")
                     .stream()
                     .map(s -> class_2561.method_43470(s).method_27692(class_124.field_1061))
                     .collect(Collectors.toList())
               )
            )
            .addPage(
               new DynamicListPage(
                  "mca.books.supporters.translators",
                  page -> Supporters.getSupporterGroup("mca:translators")
                     .stream()
                     .map(s -> class_2561.method_43470(s).method_27692(class_124.field_1058))
                     .collect(Collectors.toList())
               )
            )
            .addPage(
               new DynamicListPage(
                  "mca.books.supporters.old",
                  page -> Supporters.getSupporterGroup("mca:old")
                     .stream()
                     .map(s -> class_2561.method_43470(s).method_27692(class_124.field_1074))
                     .collect(Collectors.toList())
               )
            )
            .addPage(new TitlePage("mca.books.supporters.thanks", ""))
      )
   );
   RegistrySupplier<class_1792> BOOK_CULT_0 = register(
      "book_cult_0",
      () -> new ExtendedWrittenBookItem(
         baseProps(),
         new Book("cult_0")
            .setBackground(MCA.locate("textures/gui/books/cult.png"))
            .setTextFormatting(class_124.field_1079)
            .addPage(new TitlePage("cult_0", class_124.field_1079))
            .addPage(new CenteredTextPage("cult_0", 0))
            .addPage(new CenteredTextPage("cult_0", 1))
            .addPage(new CenteredTextPage("cult_0", 2))
            .addPage(new CenteredTextPage("cult_0", 3))
            .addPage(new ScribbleTextPage(MCA.locate("textures/gui/scribbles/goat.png"), ""))
      )
   );
   RegistrySupplier<class_1792> BOOK_CULT_1 = register(
      "book_cult_1",
      () -> new ExtendedWrittenBookItem(
         baseProps(),
         new Book("cult_1")
            .setBackground(MCA.locate("textures/gui/books/cult.png"))
            .setTextFormatting(class_124.field_1079)
            .addPage(new TitlePage("cult_1", class_124.field_1079))
            .addPage(new CenteredTextPage("cult_1", 0))
            .addPage(new CenteredTextPage("cult_1", 1))
            .addPage(new CenteredTextPage("cult_1", 2))
            .addPage(new CenteredTextPage("cult_1", 3))
            .addPage(new ScribbleTextPage(MCA.locate("textures/gui/scribbles/goat.png"), ""))
      )
   );
   RegistrySupplier<class_1792> BOOK_CULT_2 = register(
      "book_cult_2",
      () -> new ExtendedWrittenBookItem(
         baseProps(),
         new Book("cult_2")
            .setBackground(MCA.locate("textures/gui/books/cult.png"))
            .setTextFormatting(class_124.field_1079)
            .addPage(new TitlePage("cult_2", class_124.field_1079))
            .addPage(new CenteredTextPage("cult_2", 0))
            .addPage(new CenteredTextPage("cult_2", 1))
            .addPage(new CenteredTextPage("cult_2", 2))
            .addPage(new CenteredTextPage("cult_2", 3))
            .addPage(new ScribbleTextPage(MCA.locate("textures/gui/scribbles/goat.png"), ""))
      )
   );
   RegistrySupplier<class_1792> BOOK_CULT_ANCIENT = register(
      "book_cult_ancient",
      () -> new ExtendedWrittenBookItem(
         baseProps(),
         new Book("cult_ancient")
            .setBackground(MCA.locate("textures/gui/books/cult.png"))
            .setTextFormatting(class_124.field_1079)
            .addPage(new TitlePage("cult_ancient", class_124.field_1079))
            .addPage(
               new CenteredTextPage(
                     "We are the universe. We are everything you think isn't you. You are looking at us now, through your skin and your eyes. And why does the universe touch your skin, and throw light on you? To see you, player. To know you. And to be known. I shall tell you a story."
                  )
                  .setStyle(class_2583.field_24360.method_27704(new class_2960("minecraft", "alt")))
            )
      )
   );
   RegistrySupplier<class_1792> CIVIL_REGISTRY = register(
      "civil_registry",
      () -> new CivilRegistry(
         baseProps().method_7889(1), new CivilRegistryBook("civil_registry", null).setBackground(MCA.locate("textures/gui/books/supporters.png"))
      )
   );
   RegistrySupplier<class_1792> LETTER = register(
      "letter",
      () -> new ExtendedWrittenBookItem(baseProps().method_7889(1), new Book("letter", null).setBackground(MCA.locate("textures/gui/books/paper.png")))
   );
   RegistrySupplier<class_1792> DIVORCE_PAPERS = register("divorce_papers", () -> new TooltippedItem(baseProps()));
   RegistrySupplier<class_1792> ROSE_GOLD_DUST = register("rose_gold_dust", () -> new class_1792(baseProps()));
   RegistrySupplier<class_1792> ROSE_GOLD_INGOT = register("rose_gold_ingot", () -> new class_1792(baseProps()));
   RegistrySupplier<class_1792> ROSE_GOLD_BLOCK = register("rose_gold_block", () -> new class_1747((class_2248)BlocksMCA.ROSE_GOLD_BLOCK.get(), baseProps()));
   RegistrySupplier<class_1792> JEWELER_WORKBENCH = register(
      "jeweler_workbench", () -> new class_1747((class_2248)BlocksMCA.JEWELER_WORKBENCH.get(), baseProps())
   );
   RegistrySupplier<class_1792> GRAVELLING_HEADSTONE = register(
      "gravelling_headstone", () -> new class_1747((class_2248)BlocksMCA.GRAVELLING_HEADSTONE.get(), baseProps())
   );
   RegistrySupplier<class_1792> UPRIGHT_HEADSTONE = register(
      "upright_headstone", () -> new class_1747((class_2248)BlocksMCA.UPRIGHT_HEADSTONE.get(), baseProps())
   );
   RegistrySupplier<class_1792> SLANTED_HEADSTONE = register(
      "slanted_headstone", () -> new class_1747((class_2248)BlocksMCA.SLANTED_HEADSTONE.get(), baseProps())
   );
   RegistrySupplier<class_1792> CROSS_HEADSTONE = register("cross_headstone", () -> new class_1747((class_2248)BlocksMCA.CROSS_HEADSTONE.get(), baseProps()));
   RegistrySupplier<class_1792> WALL_HEADSTONE = register("wall_headstone", () -> new class_1747((class_2248)BlocksMCA.WALL_HEADSTONE.get(), baseProps()));
   RegistrySupplier<class_1792> COBBLESTONE_UPRIGHT_HEADSTONE = register(
      "cobblestone_upright_headstone", () -> new class_1747((class_2248)BlocksMCA.COBBLESTONE_UPRIGHT_HEADSTONE.get(), baseProps())
   );
   RegistrySupplier<class_1792> COBBLESTONE_SLANTED_HEADSTONE = register(
      "cobblestone_slanted_headstone", () -> new class_1747((class_2248)BlocksMCA.COBBLESTONE_SLANTED_HEADSTONE.get(), baseProps())
   );
   RegistrySupplier<class_1792> WOODEN_UPRIGHT_HEADSTONE = register(
      "wooden_upright_headstone", () -> new class_1747((class_2248)BlocksMCA.WOODEN_UPRIGHT_HEADSTONE.get(), baseProps())
   );
   RegistrySupplier<class_1792> WOODEN_SLANTED_HEADSTONE = register(
      "wooden_slanted_headstone", () -> new class_1747((class_2248)BlocksMCA.WOODEN_SLANTED_HEADSTONE.get(), baseProps())
   );
   RegistrySupplier<class_1792> GOLDEN_UPRIGHT_HEADSTONE = register(
      "golden_upright_headstone", () -> new class_1747((class_2248)BlocksMCA.GOLDEN_UPRIGHT_HEADSTONE.get(), baseProps())
   );
   RegistrySupplier<class_1792> GOLDEN_SLANTED_HEADSTONE = register(
      "golden_slanted_headstone", () -> new class_1747((class_2248)BlocksMCA.GOLDEN_SLANTED_HEADSTONE.get(), baseProps())
   );
   RegistrySupplier<class_1792> DEEPSLATE_UPRIGHT_HEADSTONE = register(
      "deepslate_upright_headstone", () -> new class_1747((class_2248)BlocksMCA.DEEPSLATE_UPRIGHT_HEADSTONE.get(), baseProps())
   );
   RegistrySupplier<class_1792> DEEPSLATE_SLANTED_HEADSTONE = register(
      "deepslate_slanted_headstone", () -> new class_1747((class_2248)BlocksMCA.DEEPSLATE_SLANTED_HEADSTONE.get(), baseProps())
   );
   List<RegistrySupplier<class_1792>> CRIBS = registerAllCribTypes();

   static void bootstrap() {
      ITEM_GROUPS.register();
      ITEMS.register();
      TagsMCA.Blocks.bootstrap();
   }

   static List<RegistrySupplier<class_1792>> registerAllCribTypes() {
      List<RegistrySupplier<class_1792>> cribs = new ArrayList<>();

      for (CribWoodType wood : CribWoodType.values()) {
         for (class_1767 color : class_1767.values()) {
            cribs.add(
               register(color.method_7792() + "_" + wood.toString().toLowerCase(Locale.ROOT) + "_crib", () -> new CribItem(unstackableProps(), wood, color))
            );
         }
      }

      return cribs;
   }

   static RegistrySupplier<class_1792> register(String name, Supplier<class_1792> item) {
      return ITEMS.register(new class_2960("mca", name), item);
   }

   static RegistrySupplier<class_1761> registerTab(String name, Supplier<class_1761> group) {
      return ITEM_GROUPS.register(new class_2960("mca", name), group);
   }

   static class_1793 baseProps() {
      return new class_1793().arch$tab(MCA_GROUP);
   }

   static class_1793 unstackableProps() {
      return baseProps().method_7889(1);
   }
}
