package forge.net.mca.entity.ai;

import forge.net.mca.Config;
import forge.net.mca.entity.VillagerLike;
import forge.net.mca.util.network.datasync.CDataManager;
import forge.net.mca.util.network.datasync.CDataParameter;
import forge.net.mca.util.network.datasync.CParameter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

public class Traits {
   private static final CDataParameter<CompoundTag> TRAITS = CParameter.create("traits", new CompoundTag());
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
   private RandomSource random = RandomSource.m_216327_();
   private final VillagerLike<?> entity;

   public static Traits.Trait registerTrait(String id, float chance, float inherit, boolean usableOnPlayer) {
      Traits.Trait trait = new Traits.Trait(id, chance, inherit, usableOnPlayer);
      TRAIT_REGISTRY.put(id, trait);
      return trait;
   }

   public static Traits.Trait registerTrait(String id, float chance, float inherit) {
      return registerTrait(id, chance, inherit, true);
   }

   public static <E extends Entity> CDataManager.Builder<E> createTrackedData(CDataManager.Builder<E> builder) {
      return builder.addAll(TRAITS);
   }

   public Traits(VillagerLike<?> entity) {
      this.entity = entity;
   }

   public Set<Traits.Trait> getTraits() {
      return this.entity.getTrackedValue(TRAITS).m_128431_().stream().map(Traits.Trait::valueOf).collect(Collectors.toSet());
   }

   public Set<Traits.Trait> getInheritedTraits() {
      return this.getTraits().stream().filter(t -> this.random.m_188501_() < t.inherit * Config.getInstance().traitInheritChance).collect(Collectors.toSet());
   }

   public boolean hasTrait(VillagerLike<?> target, Traits.Trait trait) {
      return target.getTrackedValue(TRAITS).m_128441_(trait.id());
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
      CompoundTag traits = this.entity.getTrackedValue(TRAITS).m_6426_();
      traits.m_128379_(trait.id(), true);
      this.entity.setTrackedValue(TRAITS, traits);
   }

   public void removeTrait(Traits.Trait trait) {
      CompoundTag traits = this.entity.getTrackedValue(TRAITS).m_6426_();
      traits.m_128473_(trait.id());
      this.entity.setTrackedValue(TRAITS, traits);
   }

   public void randomize() {
      float total = (float)Traits.Trait.values().stream().mapToDouble(tr -> tr.chance).sum();

      for (Traits.Trait t : Traits.Trait.values()) {
         float chance = Config.getInstance().traitChance / total * t.chance;
         if (this.random.m_188501_() < chance && t.isEnabled()) {
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
      RandomSource old = this.random;
      this.random = RandomSource.m_216335_(seed);
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

      public Component getName() {
         return Component.m_237115_("trait." + this.id().toLowerCase(Locale.ROOT));
      }

      public Component getDescription() {
         return Component.m_237115_("traitDescription." + this.id().toLowerCase(Locale.ROOT));
      }

      public boolean isUsableOnPlayer() {
         return this.usableOnPlayer;
      }

      public boolean isEnabled() {
         return Config.getServerConfig().enabledTraits.getOrDefault(this.id(), false);
      }
   }
}
