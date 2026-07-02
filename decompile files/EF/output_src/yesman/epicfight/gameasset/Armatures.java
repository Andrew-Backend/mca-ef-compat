package yesman.epicfight.gameasset;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.asset.JsonAssetLoader;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.model.armature.CreeperArmature;
import yesman.epicfight.model.armature.DragonArmature;
import yesman.epicfight.model.armature.EndermanArmature;
import yesman.epicfight.model.armature.HoglinArmature;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.model.armature.IronGolemArmature;
import yesman.epicfight.model.armature.PiglinArmature;
import yesman.epicfight.model.armature.RavagerArmature;
import yesman.epicfight.model.armature.SpiderArmature;
import yesman.epicfight.model.armature.VexArmature;
import yesman.epicfight.model.armature.WitherArmature;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.entity.EpicFightEntities;

public class Armatures {
   public static final Armatures INSTANCE = new Armatures();
   private static ResourceManager resourceManager = null;
   private static final Map<ResourceLocation, Armatures.ArmatureAccessor<? extends Armature>> ACCESSORS = Maps.newHashMap();
   private static final Map<Armatures.ArmatureAccessor<? extends Armature>, Armature> ARMATURES = Maps.newHashMap();
   private static final Map<EntityType<?>, AssetAccessor<? extends Armature>> ENTITY_TYPE_ARMATURE_MAPPER = Maps.newHashMap();
   public static final Armatures.ArmatureAccessor<HumanoidArmature> BIPED = Armatures.ArmatureAccessor.create(
      "epicfight", "entity/biped", HumanoidArmature::new
   );
   public static final Armatures.ArmatureAccessor<CreeperArmature> CREEPER = Armatures.ArmatureAccessor.create(
      "epicfight", "entity/creeper", CreeperArmature::new
   );
   public static final Armatures.ArmatureAccessor<EndermanArmature> ENDERMAN = Armatures.ArmatureAccessor.create(
      "epicfight", "entity/enderman", EndermanArmature::new
   );
   public static final Armatures.ArmatureAccessor<HumanoidArmature> SKELETON = Armatures.ArmatureAccessor.create(
      "epicfight", "entity/skeleton", HumanoidArmature::new
   );
   public static final Armatures.ArmatureAccessor<SpiderArmature> SPIDER = Armatures.ArmatureAccessor.create("epicfight", "entity/spider", SpiderArmature::new);
   public static final Armatures.ArmatureAccessor<IronGolemArmature> IRON_GOLEM = Armatures.ArmatureAccessor.create(
      "epicfight", "entity/iron_golem", IronGolemArmature::new
   );
   public static final Armatures.ArmatureAccessor<RavagerArmature> RAVAGER = Armatures.ArmatureAccessor.create(
      "epicfight", "entity/ravager", RavagerArmature::new
   );
   public static final Armatures.ArmatureAccessor<VexArmature> VEX = Armatures.ArmatureAccessor.create("epicfight", "entity/vex", VexArmature::new);
   public static final Armatures.ArmatureAccessor<PiglinArmature> PIGLIN = Armatures.ArmatureAccessor.create("epicfight", "entity/piglin", PiglinArmature::new);
   public static final Armatures.ArmatureAccessor<HoglinArmature> HOGLIN = Armatures.ArmatureAccessor.create("epicfight", "entity/hoglin", HoglinArmature::new);
   public static final Armatures.ArmatureAccessor<DragonArmature> DRAGON = Armatures.ArmatureAccessor.create("epicfight", "entity/dragon", DragonArmature::new);
   public static final Armatures.ArmatureAccessor<WitherArmature> WITHER = Armatures.ArmatureAccessor.create("epicfight", "entity/wither", WitherArmature::new);

   public static void registerEntityTypes() {
      registerEntityTypeArmature(EntityType.f_20554_, SPIDER);
      registerEntityTypeArmature(EntityType.f_20558_, CREEPER);
      registerEntityTypeArmature(EntityType.f_20562_, BIPED);
      registerEntityTypeArmature(EntityType.f_20566_, ENDERMAN);
      registerEntityTypeArmature(EntityType.f_20568_, BIPED);
      registerEntityTypeArmature(EntityType.f_20456_, HOGLIN);
      registerEntityTypeArmature(EntityType.f_20458_, BIPED);
      registerEntityTypeArmature(EntityType.f_20460_, IRON_GOLEM);
      registerEntityTypeArmature(EntityType.f_20512_, PIGLIN);
      registerEntityTypeArmature(EntityType.f_20511_, PIGLIN);
      registerEntityTypeArmature(EntityType.f_20513_, BIPED);
      registerEntityTypeArmature(EntityType.f_20518_, RAVAGER);
      registerEntityTypeArmature(EntityType.f_20524_, SKELETON);
      registerEntityTypeArmature(EntityType.f_20479_, SPIDER);
      registerEntityTypeArmature(EntityType.f_20481_, SKELETON);
      registerEntityTypeArmature(EntityType.f_20491_, VEX);
      registerEntityTypeArmature(EntityType.f_20493_, BIPED);
      registerEntityTypeArmature(EntityType.f_20495_, BIPED);
      registerEntityTypeArmature(EntityType.f_20497_, SKELETON);
      registerEntityTypeArmature(EntityType.f_20500_, HOGLIN);
      registerEntityTypeArmature(EntityType.f_20501_, BIPED);
      registerEntityTypeArmature(EntityType.f_20530_, BIPED);
      registerEntityTypeArmature(EntityType.f_20531_, PIGLIN);
      registerEntityTypeArmature(EntityType.f_20532_, BIPED);
      registerEntityTypeArmature(EntityType.f_20565_, DRAGON);
      registerEntityTypeArmature(EntityType.f_20496_, WITHER);
      registerEntityTypeArmature((EntityType<?>)EpicFightEntities.WITHER_SKELETON_MINION.get(), SKELETON);
      registerEntityTypeArmature((EntityType<?>)EpicFightEntities.WITHER_GHOST_CLONE.get(), WITHER);
   }

   public static void reload(ResourceManager resourceManager) {
      Armatures.resourceManager = resourceManager;
      ACCESSORS.entrySet().removeIf(entry -> !entry.getValue().inRegistry);
      ARMATURES.clear();
   }

   public static void registerEntityTypeArmature(EntityType<?> entityType, AssetAccessor<? extends Armature> armatureAccessor) {
      ENTITY_TYPE_ARMATURE_MAPPER.put(entityType, armatureAccessor);
   }

   public static void registerEntityTypeArmatureByPreset(EntityType<?> entityType, String presetName) {
      EntityType<?> presetEntityType = (EntityType<?>)ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(presetName));
      ENTITY_TYPE_ARMATURE_MAPPER.put(entityType, ENTITY_TYPE_ARMATURE_MAPPER.get(presetEntityType));
   }

   public static <A extends Armature> A getArmatureFor(EntityPatch<?> entitypatch) {
      return (A)ENTITY_TYPE_ARMATURE_MAPPER.get(entitypatch.getOriginal().m_6095_()).get().deepCopy();
   }

   @Nullable
   public static <A extends Armature> AssetAccessor<A> get(ResourceLocation id) {
      return (AssetAccessor<A>)ACCESSORS.get(id);
   }

   public static <A extends Armature> AssetAccessor<A> getOrCreate(ResourceLocation id, Armatures.ArmatureContructor<A> armatureConstructor) {
      return (AssetAccessor<A>)(ACCESSORS.containsKey(id) ? ACCESSORS.get(id) : Armatures.ArmatureAccessor.create(id, armatureConstructor, false));
   }

   public static <A extends Armature> Set<Pair<ResourceLocation, AssetAccessor<A>>> entry() {
      Set<Pair<ResourceLocation, AssetAccessor<A>>> newset = Sets.newHashSet();

      for (AssetAccessor<? extends Armature> accessor : ACCESSORS.values()) {
         try {
            AssetAccessor<A> casted = (AssetAccessor<A>)accessor;
            newset.add(Pair.of(casted.registryName(), casted));
         } catch (ClassCastException var4) {
         }
      }

      return newset;
   }

   public static ResourceLocation wrapLocation(ResourceLocation rl) {
      return rl.m_135815_().matches("animmodels/.*\\.json")
         ? rl
         : ResourceLocation.fromNamespaceAndPath(rl.m_135827_(), "animmodels/" + rl.m_135815_() + ".json");
   }

   public record ArmatureAccessor<A extends Armature>(ResourceLocation registryName, Armatures.ArmatureContructor<A> armatureConstructor, boolean inRegistry)
      implements AssetAccessor<A> {
      public static <A extends Armature> Armatures.ArmatureAccessor<A> create(
         String namespaceId, String path, Armatures.ArmatureContructor<A> armatureConstructor
      ) {
         return create(ResourceLocation.fromNamespaceAndPath(namespaceId, path), armatureConstructor, true);
      }

      private static <A extends Armature> Armatures.ArmatureAccessor<A> create(
         ResourceLocation id, Armatures.ArmatureContructor<A> armatureConstructor, boolean inRegistry
      ) {
         Armatures.ArmatureAccessor<A> accessor = new Armatures.ArmatureAccessor<>(id, armatureConstructor, inRegistry);
         Armatures.ACCESSORS.put(id, accessor);
         return accessor;
      }

      public A get() {
         if (Armatures.ARMATURES.get(this) == null) {
            JsonAssetLoader jsonAssetLoader = new JsonAssetLoader(Armatures.resourceManager, Armatures.wrapLocation(this.registryName()));
            Armatures.ARMATURES.put(this, jsonAssetLoader.loadArmature(this.armatureConstructor));
         }

         return (A)Armatures.ARMATURES.get(this);
      }

      @Override
      public String toString() {
         return this.registryName.toString();
      }

      @Override
      public int hashCode() {
         return this.registryName.hashCode();
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (obj instanceof Armatures.ArmatureAccessor armatureAccessor) {
            return this.registryName.equals(armatureAccessor.registryName());
         } else if (obj instanceof ResourceLocation rl) {
            return this.registryName.equals(rl);
         } else {
            return obj instanceof String name ? this.registryName.toString().equals(name) : false;
         }
      }
   }

   @FunctionalInterface
   public interface ArmatureContructor<T extends Armature> {
      T invoke(String var1, int var2, Joint var3, Map<String, Joint> var4);
   }
}
