package yesman.epicfight.api.data.reloader;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import io.netty.util.internal.StringUtil;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.data.conditions.EpicFightConditions;
import yesman.epicfight.data.conditions.entity.HasCustomTag;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.main.EpicFightSharedConstants;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.network.server.SPDatapackSync;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.entitypatch.CustomHumanoidMobPatch;
import yesman.epicfight.world.capabilities.entitypatch.CustomMobPatch;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.capabilities.provider.EntityPatchProvider;
import yesman.epicfight.world.capabilities.provider.ExtraEntryProvider;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

public class MobPatchReloadListener extends SimpleJsonResourceReloadListener {
   public static final String DIRECTORY = "epicfight_mobpatch";
   private static final Gson GSON = new GsonBuilder().create();
   private static final Map<EntityType<?>, CompoundTag> TAGMAP = Maps.newHashMap();
   private static final Map<EntityType<?>, MobPatchReloadListener.AbstractMobPatchProvider> MOB_PATCH_PROVIDERS = Maps.newHashMap();

   public MobPatchReloadListener() {
      super(GSON, "epicfight_mobpatch");
   }

   protected Map<ResourceLocation, JsonElement> m_5944_(ResourceManager resourceManager, ProfilerFiller profileIn) {
      MOB_PATCH_PROVIDERS.clear();
      TAGMAP.clear();
      return super.m_5944_(resourceManager, profileIn);
   }

   protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManager, ProfilerFiller profilerIn) {
      for (Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
         ResourceLocation rl = entry.getKey();
         String pathString = rl.m_135815_();
         ResourceLocation registryName = ResourceLocation.fromNamespaceAndPath(rl.m_135827_(), pathString);
         if (!ForgeRegistries.ENTITY_TYPES.containsKey(registryName)) {
            EpicFightMod.LOGGER.warn("Mob Patch Exception: No Entity named " + registryName);
         } else {
            EntityType<?> entityType = (EntityType<?>)ForgeRegistries.ENTITY_TYPES.getValue(registryName);
            CompoundTag tag = null;

            try {
               tag = TagParser.m_129359_(entry.getValue().toString());
            } catch (CommandSyntaxException e) {
               EpicFightMod.LOGGER.warn("Error while deserializing datapack for " + registryName + ": " + e.getLocalizedMessage());
               continue;
            }

            MobPatchReloadListener.AbstractMobPatchProvider abstractMobpatchProvider = null;

            try {
               abstractMobpatchProvider = deserialize(entityType, tag, false, resourceManager);
            } catch (Exception e) {
               EpicFightMod.LOGGER.warn("Can't deserialize mob capability: " + registryName + ": " + e.getLocalizedMessage());
               continue;
            }

            MOB_PATCH_PROVIDERS.put(entityType, abstractMobpatchProvider);
            EntityPatchProvider.putCustomEntityPatch(entityType, entity -> () -> MOB_PATCH_PROVIDERS.get(entity.m_6095_()).get(entity));
            TAGMAP.put(entityType, filterClientData(tag));
            if (EpicFightSharedConstants.isPhysicalClient()) {
               ClientEngine.getInstance()
                  .renderEngine
                  .registerCustomEntityRenderer(entityType, tag.m_128441_("preset") ? tag.m_128461_("preset") : tag.m_128461_("renderer"), tag);
            }
         }
      }
   }

   public static MobPatchReloadListener.AbstractMobPatchProvider deserialize(
      EntityType<?> entityType, CompoundTag tag, boolean clientSide, ResourceManager resourceManager
   ) {
      MobPatchReloadListener.AbstractMobPatchProvider provider = null;
      int i = 0;
      boolean hasBranch = tag.m_128441_(String.format("branch_%d", i));
      if (hasBranch) {
         provider = new MobPatchReloadListener.BranchProvider();
         ((MobPatchReloadListener.BranchProvider)provider).defaultProvider = deserializeMobPatchProvider(entityType, tag, clientSide, resourceManager);
      } else {
         provider = deserializeMobPatchProvider(entityType, tag, clientSide, resourceManager);
      }

      while (hasBranch) {
         CompoundTag branchTag = tag.m_128469_(String.format("branch_%d", i));
         ((MobPatchReloadListener.BranchProvider)provider)
            .providers
            .add(Pair.of(deserializeBranchPredicate(branchTag.m_128469_("condition")), deserialize(entityType, branchTag, clientSide, resourceManager)));
         hasBranch = tag.m_128441_(String.format("branch_%d", ++i));
      }

      return provider;
   }

   public static HasCustomTag deserializeBranchPredicate(CompoundTag tag) {
      String predicateType = tag.m_128461_("predicate");
      HasCustomTag predicate = null;
      if ("has_tags".equals(predicateType)) {
         if (!tag.m_128425_("tags", 9)) {
            EpicFightMod.LOGGER
               .info(
                  "Mob capability deserializing exception: Can't find a proper argument for %s. [name: %s, type: %s]"
                     .formatted("has_tags", "tags", "string list")
               );
         }

         predicate = new HasCustomTag(tag.m_128437_("tags", 8));
      }

      if (predicate == null) {
         throw new IllegalArgumentException("Mob capability deserializing exception: No predicate type: " + predicateType);
      } else {
         return predicate;
      }
   }

   public static MobPatchReloadListener.AbstractMobPatchProvider deserializeMobPatchProvider(
      EntityType<?> entityType, CompoundTag tag, boolean clientSide, ResourceManager resourceManager
   ) {
      return deserializeMobPatchProvider(entityType, tag, clientSide, resourceManager, null);
   }

   /** @deprecated */
   public static MobPatchReloadListener.AbstractMobPatchProvider deserializeMobPatchProvider(
      EntityType<?> entityType, CompoundTag tag, boolean clientSide, ResourceManager resourceManager, @Nullable ExtraEntryProvider extraEntryProvider
   ) {
      boolean disabled = tag.m_128441_("disabled") && tag.m_128471_("disabled");
      if (disabled) {
         return new MobPatchReloadListener.NullPatchProvider();
      }

      if (tag.m_128441_("preset")) {
         String presetName = tag.m_128461_("preset");
         Function<Entity, Supplier<EntityPatch<?>>> preset = EntityPatchProvider.get(presetName);
         if (extraEntryProvider == null) {
            Armatures.registerEntityTypeArmatureByPreset(entityType, presetName);
         }

         return new MobPatchReloadListener.MobPatchPresetProvider(preset);
      } else {
         boolean humanoid = tag.m_128471_("isHumanoid");
         MobPatchReloadListener.CustomMobPatchProvider provider = humanoid
            ? new MobPatchReloadListener.CustomHumanoidMobPatchProvider()
            : new MobPatchReloadListener.CustomMobPatchProvider();
         provider.attributeValues = deserializeAttributes(tag.m_128469_("attributes"));
         ResourceLocation modelLocation = ResourceLocation.parse(tag.m_128461_("model"));
         ResourceLocation armatureId = ResourceLocation.parse(tag.m_128461_("armature"));
         if (EpicFightSharedConstants.isPhysicalClient() && extraEntryProvider == null) {
            Meshes.getOrCreate(modelLocation, jsonAssetLoader -> jsonAssetLoader.loadSkinnedMesh(humanoid ? SkinnedMesh::new : HumanoidMesh::new));
         }

         if (extraEntryProvider == null) {
            Armatures.registerEntityTypeArmature(entityType, Armatures.getOrCreate(armatureId, Armature::new));
         }

         provider.defaultAnimations = deserializeDefaultAnimations(tag.m_128469_("default_livingmotions"));
         provider.faction = Faction.ENUM_MANAGER.getOrThrow(tag.m_128461_("faction"));
         provider.scale = tag.m_128469_("attributes").m_128441_("scale") ? (float)tag.m_128469_("attributes").m_128459_("scale") : 1.0F;
         if (tag.m_128441_("swing_sound")) {
            SoundEvent soundEvent = (SoundEvent)ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse(tag.m_128461_("swing_sound")));
            if (soundEvent == null) {
               EpicFightMod.LOGGER.warn("Can't find a swing sound " + tag.m_128461_("swing_sound") + " for the next mot patch: " + entityType.toString());
            } else {
               provider.swingSound = soundEvent;
            }
         }

         if (tag.m_128441_("hit_sound")) {
            SoundEvent soundEvent = (SoundEvent)ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse(tag.m_128461_("hit_sound")));
            if (soundEvent == null) {
               EpicFightMod.LOGGER.warn("Can't find a hit sound " + tag.m_128461_("hit_sound") + " for the next mot patch: " + entityType.toString());
            } else {
               provider.hitSound = soundEvent;
            }
         }

         if (tag.m_128441_("hit_particle")) {
            HitParticleType hitParticle = (HitParticleType)ForgeRegistries.PARTICLE_TYPES.getValue(ResourceLocation.parse(tag.m_128461_("hit_particle")));
            if (hitParticle == null) {
               EpicFightMod.LOGGER.warn("Can't find a hit particle type" + tag.m_128461_("hit_particle") + " for the next mot patch: " + entityType.toString());
            } else {
               provider.hitParticle = hitParticle;
            }
         }

         if (!clientSide) {
            provider.stunAnimations = deserializeStunAnimations(tag.m_128469_("stun_animations"));
            if (tag.m_128469_("attributes").m_128441_("chasing_speed")) {
               provider.chasingSpeed = tag.m_128469_("attributes").m_128459_("chasing_speed");
            }

            if (humanoid) {
               MobPatchReloadListener.CustomHumanoidMobPatchProvider humanoidProvider = (MobPatchReloadListener.CustomHumanoidMobPatchProvider)provider;
               humanoidProvider.humanoidCombatBehaviors = deserializeHumanoidCombatBehaviors(tag.m_128437_("combat_behavior", 10));
               humanoidProvider.humanoidWeaponMotions = deserializeHumanoidWeaponMotions(tag.m_128437_("humanoid_weapon_motions", 10));
            } else {
               provider.combatBehaviorsBuilder = deserializeCombatBehaviorsBuilder(tag.m_128437_("combat_behavior", 10));
            }
         }

         return provider;
      }
   }

   public static Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> deserializeHumanoidCombatBehaviors(ListTag tag) {
      Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> combatBehaviorsMapBuilder = Maps.newHashMap();

      for (int i = 0; i < tag.size(); i++) {
         CompoundTag combatBehavior = tag.m_128728_(i);
         ListTag categories = combatBehavior.m_128437_("weapon_categories", 8);
         Style style = Style.ENUM_MANAGER.getOrThrow(combatBehavior.m_128461_("style"));
         CombatBehaviors.Builder<HumanoidMobPatch<?>> builder = deserializeCombatBehaviorsBuilder(combatBehavior.m_128437_("behavior_series", 10));

         for (int j = 0; j < categories.size(); j++) {
            WeaponCategory category = WeaponCategory.ENUM_MANAGER.getOrThrow(categories.m_128778_(j));
            combatBehaviorsMapBuilder.computeIfAbsent(category, key -> Maps.newHashMap());
            combatBehaviorsMapBuilder.get(category).put(style, builder);
         }
      }

      return combatBehaviorsMapBuilder;
   }

   public static List<Pair<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>>> deserializeDefaultAnimations(
      CompoundTag defaultLivingmotions
   ) {
      List<Pair<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>>> defaultAnimations = Lists.newArrayList();

      for (String key : defaultLivingmotions.m_128431_()) {
         String animation = defaultLivingmotions.m_128461_(key);
         defaultAnimations.add(Pair.of(LivingMotion.ENUM_MANAGER.getOrThrow(key), AnimationManager.byKey(animation)));
      }

      return defaultAnimations;
   }

   public static Map<StunType, AnimationManager.AnimationAccessor<? extends StaticAnimation>> deserializeStunAnimations(CompoundTag tag) {
      Map<StunType, AnimationManager.AnimationAccessor<? extends StaticAnimation>> stunAnimations = Maps.newHashMap();

      for (StunType stunType : StunType.values()) {
         String lowerCaseName = tag.m_128461_(stunType.name().toLowerCase(Locale.ROOT));
         if (!StringUtil.isNullOrEmpty(lowerCaseName)) {
            stunAnimations.put(stunType, AnimationManager.byKey(lowerCaseName));
         }
      }

      return stunAnimations;
   }

   public static Object2DoubleMap<Attribute> deserializeAttributes(CompoundTag tag) {
      Object2DoubleMap<Attribute> attributes = new Object2DoubleOpenHashMap();
      attributes.put((Attribute)EpicFightAttributes.IMPACT.get(), tag.m_128425_("impact", 6) ? tag.m_128459_("impact") : 0.5);
      attributes.put((Attribute)EpicFightAttributes.ARMOR_NEGATION.get(), tag.m_128425_("armor_negation", 6) ? tag.m_128459_("armor_negation") : 0.0);
      attributes.put((Attribute)EpicFightAttributes.MAX_STRIKES.get(), tag.m_128425_("max_strikes", 3) ? tag.m_128451_("max_strikes") : 1);
      attributes.put((Attribute)EpicFightAttributes.STUN_ARMOR.get(), tag.m_128425_("stun_armor", 6) ? tag.m_128459_("stun_armor") : 0.0);
      if (tag.m_128425_("attack_damage", 6)) {
         attributes.put(Attributes.f_22281_, tag.m_128459_("attack_damage"));
      }

      return attributes;
   }

   public static Map<WeaponCategory, Map<Style, Set<Pair<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>>>>> deserializeHumanoidWeaponMotions(
      ListTag tag
   ) {
      Map<WeaponCategory, Map<Style, Set<Pair<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>>>>> map = Maps.newHashMap();

      for (int i = 0; i < tag.size(); i++) {
         Builder<Pair<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>>> motions = ImmutableSet.builder();
         CompoundTag weaponMotionTag = tag.m_128728_(i);
         Style style = Style.ENUM_MANAGER.getOrThrow(weaponMotionTag.m_128461_("style"));
         CompoundTag motionsTag = weaponMotionTag.m_128469_("livingmotions");

         for (String key : motionsTag.m_128431_()) {
            motions.add(Pair.of(LivingMotion.ENUM_MANAGER.getOrThrow(key), AnimationManager.byKey(motionsTag.m_128461_(key))));
         }

         Tag weponTypeTag = weaponMotionTag.m_128423_("weapon_categories");
         if (weponTypeTag instanceof StringTag) {
            WeaponCategory weaponCategory = WeaponCategory.ENUM_MANAGER.getOrThrow(weponTypeTag.m_7916_());
            if (!map.containsKey(weaponCategory)) {
               map.put(weaponCategory, Maps.newHashMap());
            }

            map.get(weaponCategory).put(style, motions.build());
         } else if (weponTypeTag instanceof ListTag weponTypesTag) {
            for (int j = 0; j < weponTypesTag.size(); j++) {
               WeaponCategory weaponCategory = WeaponCategory.ENUM_MANAGER.getOrThrow(weponTypesTag.m_128778_(j));
               if (!map.containsKey(weaponCategory)) {
                  map.put(weaponCategory, Maps.newHashMap());
               }

               map.get(weaponCategory).put(style, motions.build());
            }
         }
      }

      return map;
   }

   public static <T extends MobPatch<?>> CombatBehaviors.Builder<T> deserializeCombatBehaviorsBuilder(ListTag tag) {
      CombatBehaviors.Builder<T> builder = CombatBehaviors.builder();

      for (int i = 0; i < tag.size(); i++) {
         CompoundTag behaviorSeries = tag.m_128728_(i);
         float weight = (float)behaviorSeries.m_128459_("weight");
         int cooldown = behaviorSeries.m_128441_("cooldown") ? behaviorSeries.m_128451_("cooldown") : 0;
         boolean canBeInterrupted = behaviorSeries.m_128441_("canBeInterrupted") && behaviorSeries.m_128471_("canBeInterrupted");
         boolean looping = behaviorSeries.m_128441_("looping") && behaviorSeries.m_128471_("looping");
         ListTag behaviorList = behaviorSeries.m_128437_("behaviors", 10);
         CombatBehaviors.BehaviorSeries.Builder<T> behaviorSeriesBuilder = CombatBehaviors.BehaviorSeries.builder();
         behaviorSeriesBuilder.weight(weight).cooldown(cooldown).canBeInterrupted(canBeInterrupted).looping(looping);

         for (int j = 0; j < behaviorList.size(); j++) {
            CombatBehaviors.Behavior.Builder<T> behaviorBuilder = CombatBehaviors.Behavior.builder();
            CompoundTag behavior = behaviorList.m_128728_(j);
            String animationName = behavior.m_128461_("animation");
            AnimationManager.AnimationAccessor<? extends StaticAnimation> animation = AnimationManager.byKey(animationName);
            if (animation == null) {
               throw new NoSuchElementException("No animation named " + animationName);
            }

            ListTag conditionList = behavior.m_128437_("conditions", 10);
            behaviorBuilder.animationBehavior(animation);

            for (int k = 0; k < conditionList.size(); k++) {
               CompoundTag condition = conditionList.m_128728_(k);
               Condition<T> predicate = deserializeBehaviorPredicate(condition.m_128461_("predicate"), condition);
               behaviorBuilder.predicate(predicate);
            }

            behaviorSeriesBuilder.nextBehavior(behaviorBuilder);
         }

         builder.newBehaviorSeries(behaviorSeriesBuilder);
      }

      return builder;
   }

   public static <T extends MobPatch<?>> Condition<T> deserializeBehaviorPredicate(String type, CompoundTag args) {
      ResourceLocation rl;
      if (type.contains(":")) {
         rl = ResourceLocation.parse(type);
      } else {
         rl = EpicFightMod.identifier(type);
      }

      Supplier<Condition<T>> predicateProvider = EpicFightConditions.getConditionOrNull(rl);
      Condition<T> condition = predicateProvider.get();
      condition.read(args);
      return condition;
   }

   public static CompoundTag filterClientData(CompoundTag tag) {
      CompoundTag clientTag = new CompoundTag();
      int i = 0;

      for (boolean hasBranch = tag.m_128441_(String.format("branch_%d", i)); hasBranch; hasBranch = tag.m_128441_(String.format("branch_%d", ++i))) {
         CompoundTag branchTag = tag.m_128469_(String.format("branch_%d", i));
         CompoundTag copiedTag = new CompoundTag();
         extractBranch(copiedTag, branchTag);
         clientTag.m_128365_(String.format("branch_%d", i), copiedTag);
      }

      extractBranch(clientTag, tag);
      return clientTag;
   }

   public static CompoundTag extractBranch(CompoundTag extract, CompoundTag original) {
      if (original.m_128441_("disabled") && original.m_128471_("disabled")) {
         extract.m_128365_("disabled", original.m_128423_("disabled"));
      } else if (original.m_128441_("preset")) {
         extract.m_128365_("preset", original.m_128423_("preset"));
      } else {
         extract.m_128365_("model", original.m_128423_("model"));
         extract.m_128365_("armature", original.m_128423_("armature"));
         extract.m_128379_("isHumanoid", original.m_128441_("isHumanoid") ? original.m_128471_("isHumanoid") : false);
         extract.m_128365_("renderer", original.m_128423_("renderer"));
         extract.m_128365_("faction", original.m_128423_("faction"));
         extract.m_128365_("default_livingmotions", original.m_128423_("default_livingmotions"));
         if (original.m_128425_("attributes", 10)) {
            extract.m_128365_("attributes", original.m_128423_("attributes"));
         }
      }

      return extract;
   }

   public static Stream<CompoundTag> getDataStream() {
      return TAGMAP.entrySet().stream().map(entry -> {
         entry.getValue().m_128359_("id", ForgeRegistries.ENTITY_TYPES.getKey(entry.getKey()).toString());
         return entry.getValue();
      });
   }

   public static int getTagCount() {
      return TAGMAP.size();
   }

   @OnlyIn(Dist.CLIENT)
   public static void processServerPacket(SPDatapackSync packet) {
      for (CompoundTag tag : packet.getTags()) {
         boolean disabled = false;
         if (tag.m_128441_("disabled")) {
            disabled = tag.m_128471_("disabled");
         }

         EntityType<?> entityType = (EntityType<?>)ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(tag.m_128461_("id")));
         MOB_PATCH_PROVIDERS.put(entityType, deserialize(entityType, tag, true, Minecraft.m_91087_().m_91098_()));
         EntityPatchProvider.putCustomEntityPatch(entityType, entity -> () -> MOB_PATCH_PROVIDERS.get(entity.m_6095_()).get(entity));
         if (!disabled) {
            if (tag.m_128441_("preset")) {
               Armatures.registerEntityTypeArmatureByPreset(entityType, tag.m_128461_("preset"));
            } else {
               ResourceLocation armatureLocation = ResourceLocation.parse(tag.m_128461_("armature"));
               boolean humanoid = tag.m_128471_("isHumanoid");
               AssetAccessor<? extends Armature> armature = Armatures.getOrCreate(armatureLocation, humanoid ? Armature::new : HumanoidArmature::new);
               Armatures.registerEntityTypeArmature(entityType, armature);
            }

            ClientEngine.getInstance()
               .renderEngine
               .registerCustomEntityRenderer(entityType, tag.m_128441_("preset") ? tag.m_128461_("preset") : tag.m_128461_("renderer"), tag);
         }
      }
   }

   public abstract static class AbstractMobPatchProvider {
      public abstract EntityPatch<?> get(Entity var1);
   }

   public static class BranchProvider extends MobPatchReloadListener.AbstractMobPatchProvider {
      protected List<Pair<HasCustomTag, MobPatchReloadListener.AbstractMobPatchProvider>> providers = Lists.newArrayList();
      protected MobPatchReloadListener.AbstractMobPatchProvider defaultProvider;

      @Override
      public EntityPatch<?> get(Entity entity) {
         for (Pair<HasCustomTag, MobPatchReloadListener.AbstractMobPatchProvider> provider : this.providers) {
            if (((HasCustomTag)provider.getFirst()).predicate(entity)) {
               return ((MobPatchReloadListener.AbstractMobPatchProvider)provider.getSecond()).get(entity);
            }
         }

         return this.defaultProvider.get(entity);
      }
   }

   public static class CustomHumanoidMobPatchProvider extends MobPatchReloadListener.CustomMobPatchProvider {
      protected Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> humanoidCombatBehaviors;
      protected Map<WeaponCategory, Map<Style, Set<Pair<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>>>>> humanoidWeaponMotions;

      @Override
      public EntityPatch<?> get(Entity entity) {
         if (this.humanoidCombatBehaviors == null && !entity.m_9236_().m_5776_()) {
            EpicFightMod.LOGGER.warn("Custom humanoid mob capability undefined combat behaviors");
            return null;
         } else if (this.humanoidWeaponMotions == null && !entity.m_9236_().m_5776_()) {
            EpicFightMod.LOGGER.warn("Custom humanoid mob capability undefined weapon motions");
            return null;
         } else {
            return new CustomHumanoidMobPatch(this.faction, this);
         }
      }

      public Map<WeaponCategory, Map<Style, Set<Pair<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>>>>> getHumanoidWeaponMotions() {
         return this.humanoidWeaponMotions;
      }

      public Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> getHumanoidCombatBehaviors() {
         return this.humanoidCombatBehaviors;
      }
   }

   public static class CustomMobPatchProvider extends MobPatchReloadListener.AbstractMobPatchProvider {
      protected CombatBehaviors.Builder<?> combatBehaviorsBuilder;
      protected List<Pair<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>>> defaultAnimations;
      protected Map<StunType, AnimationManager.AnimationAccessor<? extends StaticAnimation>> stunAnimations;
      protected Object2DoubleMap<Attribute> attributeValues;
      protected Faction faction;
      protected double chasingSpeed = 1.0;
      protected float scale;
      protected SoundEvent swingSound = (SoundEvent)EpicFightSounds.WHOOSH.get();
      protected SoundEvent hitSound = (SoundEvent)EpicFightSounds.BLUNT_HIT.get();
      protected HitParticleType hitParticle = (HitParticleType)EpicFightParticles.HIT_BLUNT.get();

      @Override
      public EntityPatch<?> get(Entity entity) {
         if (this.combatBehaviorsBuilder == null && !entity.m_9236_().m_5776_()) {
            EpicFightMod.LOGGER.warn("Combat behavior undefined for mob capability of " + entity.getClass());
            return null;
         } else {
            return new CustomMobPatch(this.faction, this);
         }
      }

      public CombatBehaviors.Builder<?> getCombatBehaviorsBuilder() {
         return this.combatBehaviorsBuilder;
      }

      public List<Pair<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>>> getDefaultAnimations() {
         return this.defaultAnimations;
      }

      public Map<StunType, AnimationManager.AnimationAccessor<? extends StaticAnimation>> getStunAnimations() {
         return this.stunAnimations;
      }

      public Object2DoubleMap<Attribute> getAttributeValues() {
         return this.attributeValues;
      }

      public double getChasingSpeed() {
         return this.chasingSpeed;
      }

      public float getScale() {
         return this.scale;
      }

      public SoundEvent getSwingSound() {
         return this.swingSound;
      }

      public SoundEvent getHitSound() {
         return this.hitSound;
      }

      public HitParticleType getHitParticle() {
         return this.hitParticle;
      }
   }

   public static class MobPatchPresetProvider extends MobPatchReloadListener.AbstractMobPatchProvider {
      protected final Function<Entity, Supplier<EntityPatch<?>>> presetProvider;

      public MobPatchPresetProvider(Function<Entity, Supplier<EntityPatch<?>>> presetProvider) {
         this.presetProvider = presetProvider;
      }

      @Override
      public EntityPatch<?> get(Entity entity) {
         return this.presetProvider.apply(entity).get();
      }
   }

   public static class NullPatchProvider extends MobPatchReloadListener.AbstractMobPatchProvider {
      @Override
      public EntityPatch<?> get(Entity entity) {
         return null;
      }
   }
}
