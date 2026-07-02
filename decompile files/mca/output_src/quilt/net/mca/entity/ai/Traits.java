package quilt.net.mca.entity.ai;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.class_1297;
import net.minecraft.class_2487;
import net.minecraft.class_2561;
import net.minecraft.class_5819;
import quilt.net.mca.Config;
import quilt.net.mca.entity.VillagerLike;
import quilt.net.mca.util.network.datasync.CDataManager;
import quilt.net.mca.util.network.datasync.CDataParameter;
import quilt.net.mca.util.network.datasync.CParameter;

public class Traits {
   private static final CDataParameter<class_2487> TRAITS = CParameter.create("traits", new class_2487());
   public static final Map<String, Traits.Trait> TRAIT_REGISTRY = new HashMap<>();
   public static Traits.Trait LEFT_HANDED = registerTrait("LEFT_HANDED", 1.0F, 0.5F, false);
   public static Traits.Trait WEAK = registerTrait("WEAK", 1.0F, 1.0F, false);
   public static Traits.Trait TOUGH = registerTrait("TOUGH", 1.0F, 1.0F, false);
   public static Traits.Trait COLOR_BLIND = registerTrait("COLOR_BLIND", 1.0F, 0.5F);
   public static Traits.Trait HETEROCHROMIA = registerTrait("HETEROCHROMIA", 1.0F, 0.5F);
   public static Traits.Trait LACTOSE_INTOLERANCE = registerTrait("LACTOSE_INTOLERANCE", 1.0F, 1.0F);
   public static Traits.Trait COELIAC_DISEASE = registerTrait("COELIAC_DISEASE", 1.0F, 1.0F, false);
   public static Traits.Trait DIABETES = registerTrait("DIABETES", 1.0F, 1.0F, false);
   public static Traits.Trait DWARFISM = registerTrait("DWARFISM", 1.0F, 1.0F);
   public static Traits.Trait ALBINISM = registerTrait("ALBINISM", 1.0F, 1.0F);
   public static Traits.Trait VEGETARIAN = registerTrait("VEGETARIAN", 1.0F, 1.0F, false);
   public static Traits.Trait BISEXUAL = registerTrait("BISEXUAL", 1.0F, 0.0F);
   public static Traits.Trait HOMOSEXUAL = registerTrait("HOMOSEXUAL", 1.0F, 0.0F);
   public static Traits.Trait ASEXUAL = registerTrait("ASEXUAL", 1.0F, 0.0F);
   public static Traits.Trait ELECTRIFIED = registerTrait("ELECTRIFIED", 0.0F, 0.0F, false);
   public static Traits.Trait SIRBEN = registerTrait("SIRBEN", 0.025F, 1.0F);
   public static Traits.Trait RAINBOW = registerTrait("RAINBOW", 0.05F, 0.0F);
   public static Traits.Trait UNKNOWN = registerTrait("UNKNOWN", 0.0F, 0.0F, false);
   private class_5819 random = class_5819.method_43047();
   private final VillagerLike<?> entity;

   public static Traits.Trait registerTrait(String id, float chance, float inherit, boolean usableOnPlayer) {
      Traits.Trait trait = new Traits.Trait(id, chance, inherit, usableOnPlayer);
      TRAIT_REGISTRY.put(id, trait);
      return trait;
   }

   public static Traits.Trait registerTrait(String id, float chance, float inherit) {
      return registerTrait(id, chance, inherit, true);
   }

   public static <E extends class_1297> CDataManager.Builder<E> createTrackedData(CDataManager.Builder<E> builder) {
      return builder.addAll(TRAITS);
   }

   public Traits(VillagerLike<?> entity) {
      this.entity = entity;
   }

   public Set<Traits.Trait> getTraits() {
      return this.entity.getTrackedValue(TRAITS).method_10541().stream().map(Traits.Trait::valueOf).collect(Collectors.toSet());
   }

   public Set<Traits.Trait> getInheritedTraits() {
      return this.getTraits()
         .stream()
         .filter(t -> this.random.method_43057() < t.inherit * Config.getInstance().traitInheritChance)
         .collect(Collectors.toSet());
   }

   public boolean hasTrait(VillagerLike<?> target, Traits.Trait trait) {
      return target.getTrackedValue(TRAITS).method_10545(trait.id());
   }

   public boolean hasTrait(Traits.Trait trait) {
      return this.hasTrait(this.entity, trait);
   }

   public boolean hasTrait(String trait) {
      return Traits.Trait.valueOf(trait) != null ? this.hasTrait(this.entity, Traits.Trait.valueOf(trait)) : false;
   }

   public boolean eitherHaveTrait(Traits.Trait trait, VillagerLike<?> other) {
      return this.hasTrait(this.entity, trait) || this.hasTrait(other, trait);
   }

   public boolean hasSameTrait(Traits.Trait trait, VillagerLike<?> other) {
      return this.hasTrait(this.entity, trait) && this.hasTrait(other, trait);
   }

   public void addTrait(Traits.Trait trait) {
      class_2487 traits = this.entity.getTrackedValue(TRAITS).method_10553();
      traits.method_10556(trait.id(), true);
      this.entity.setTrackedValue(TRAITS, traits);
   }

   public void removeTrait(Traits.Trait trait) {
      class_2487 traits = this.entity.getTrackedValue(TRAITS).method_10553();
      traits.method_10551(trait.id());
      this.entity.setTrackedValue(TRAITS, traits);
   }

   public void randomize() {
      float total = (float)Traits.Trait.values().stream().mapToDouble(tr -> tr.chance).sum();

      for (Traits.Trait t : Traits.Trait.values()) {
         float chance = Config.getInstance().traitChance / total * t.chance;
         if (this.random.method_43057() < chance && t.isEnabled()) {
            this.addTrait(t);
         }
      }
   }

   public void inherit(Traits from) {
      for (Traits.Trait t : from.getInheritedTraits()) {
         this.addTrait(t);
      }
   }

   public void inherit(Traits from, long seed) {
      class_5819 old = this.random;
      this.random = class_5819.method_43049(seed);
      this.inherit(from);
      this.random = old;
   }

   public float getVerticalScaleFactor() {
      return this.hasTrait(DWARFISM) ? 0.65F : 1.0F;
   }

   public float getHorizontalScaleFactor() {
      return (this.hasTrait(DWARFISM) ? 0.85F : 1.0F) * (this.hasTrait(TOUGH) ? 1.2F : 1.0F) * (this.hasTrait(WEAK) ? 0.85F : 1.0F);
   }

   public static class Trait {
      private final String id;
      private final float chance;
      private final float inherit;
      private final boolean usableOnPlayer;

      Trait(String id, float chance, float inherit, boolean usableOnPlayer) {
         this.id = id;
         this.chance = chance;
         this.inherit = inherit;
         this.usableOnPlayer = usableOnPlayer;
      }

      public String id() {
         return this.id;
      }

      public static Collection<Traits.Trait> values() {
         return Traits.TRAIT_REGISTRY.values();
      }

      public static Traits.Trait valueOf(String id) {
         return Traits.TRAIT_REGISTRY.getOrDefault(id.toUpperCase(Locale.ROOT), Traits.UNKNOWN);
      }

      public class_2561 getName() {
         return class_2561.method_43471("trait." + this.id().toLowerCase(Locale.ROOT));
      }

      public class_2561 getDescription() {
         return class_2561.method_43471("traitDescription." + this.id().toLowerCase(Locale.ROOT));
      }

      public boolean isUsableOnPlayer() {
         return this.usableOnPlayer;
      }

      public boolean isEnabled() {
         return Config.getServerConfig().enabledTraits.getOrDefault(this.id(), false);
      }
   }
}
