package yesman.epicfight.world.capabilities.item;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import io.netty.util.internal.StringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.data.reloader.ItemCapabilityReloadListener;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.api.forgeevent.WeaponCapabilityPresetRegistryEvent;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.data.conditions.EpicFightConditions;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.server.SPDatapackSync;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.provider.ExtraEntryProvider;

public class WeaponTypeReloadListener extends SimpleJsonResourceReloadListener {
   public static final String DIRECTORY = "capabilities/weapons/types";
   private static final Gson GSON = new GsonBuilder().create();
   private static final Map<ResourceLocation, Function<Item, CapabilityItem.Builder>> PRESETS = Maps.newHashMap();
   private static final Map<ResourceLocation, CompoundTag> CAPABILITY_COMPOUNDS = Maps.newHashMap();

   public static void registerDefaultWeaponTypes() {
      Map<ResourceLocation, Function<Item, CapabilityItem.Builder>> typeEntry = Maps.newHashMap();
      typeEntry.put(EpicFightMod.identifier("axe"), WeaponCapabilityPresets.AXE);
      typeEntry.put(EpicFightMod.identifier("fist"), WeaponCapabilityPresets.FIST);
      typeEntry.put(EpicFightMod.identifier("hoe"), WeaponCapabilityPresets.HOE);
      typeEntry.put(EpicFightMod.identifier("pickaxe"), WeaponCapabilityPresets.PICKAXE);
      typeEntry.put(EpicFightMod.identifier("shovel"), WeaponCapabilityPresets.SHOVEL);
      typeEntry.put(EpicFightMod.identifier("sword"), WeaponCapabilityPresets.SWORD);
      typeEntry.put(EpicFightMod.identifier("spear"), WeaponCapabilityPresets.SPEAR);
      typeEntry.put(EpicFightMod.identifier("greatsword"), WeaponCapabilityPresets.GREATSWORD);
      typeEntry.put(EpicFightMod.identifier("uchigatana"), WeaponCapabilityPresets.UCHIGATANA);
      typeEntry.put(EpicFightMod.identifier("tachi"), WeaponCapabilityPresets.TACHI);
      typeEntry.put(EpicFightMod.identifier("longsword"), WeaponCapabilityPresets.LONGSWORD);
      typeEntry.put(EpicFightMod.identifier("dagger"), WeaponCapabilityPresets.DAGGER);
      typeEntry.put(EpicFightMod.identifier("bow"), WeaponCapabilityPresets.BOW);
      typeEntry.put(EpicFightMod.identifier("crossbow"), WeaponCapabilityPresets.CROSSBOW);
      typeEntry.put(EpicFightMod.identifier("trident"), WeaponCapabilityPresets.TRIDENT);
      typeEntry.put(EpicFightMod.identifier("shield"), WeaponCapabilityPresets.SHIELD);
      WeaponCapabilityPresetRegistryEvent weaponCapabilityPresetRegistryEvent = new WeaponCapabilityPresetRegistryEvent(typeEntry);
      ModLoader.get().postEvent(weaponCapabilityPresetRegistryEvent);
      PRESETS.putAll(weaponCapabilityPresetRegistryEvent.getTypeEntry());
   }

   public WeaponTypeReloadListener() {
      super(GSON, "capabilities/weapons/types");
   }

   protected void apply(Map<ResourceLocation, JsonElement> packEntry, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
      clear();

      for (Entry<ResourceLocation, JsonElement> entry : packEntry.entrySet()) {
         CompoundTag compTag = null;

         try {
            compTag = TagParser.m_129359_(entry.getValue().toString());
         } catch (CommandSyntaxException e) {
            e.printStackTrace();
         }

         try {
            CompoundTag comptagFinal = compTag;
            PRESETS.put(entry.getKey(), itemstack -> deserializeWeaponCapabilityBuilder(entry.getKey(), comptagFinal));
            CAPABILITY_COMPOUNDS.put(entry.getKey(), compTag);
         } catch (Exception e) {
            EpicFightMod.LOGGER.warn("Error while deserializing weapon type datapack: " + entry.getKey());
            e.printStackTrace();
         }
      }
   }

   public static Function<Item, CapabilityItem.Builder> getOrThrow(String typeName) {
      ResourceLocation rl = ResourceLocation.parse(typeName);
      if (!PRESETS.containsKey(rl)) {
         throw new IllegalArgumentException("Can't find weapon type: " + rl);
      } else {
         return PRESETS.get(rl);
      }
   }

   public static Function<Item, CapabilityItem.Builder> get(String typeName) {
      return get(ResourceLocation.parse(typeName));
   }

   public static Function<Item, CapabilityItem.Builder> get(ResourceLocation typeName) {
      return PRESETS.get(typeName);
   }

   public static void register(ResourceLocation rl, CapabilityItem.Builder builder) {
      PRESETS.put(rl, item -> builder);
   }

   public static WeaponCapability.Builder deserializeWeaponCapabilityBuilder(ResourceLocation rl, CompoundTag tag) {
      return deserializeWeaponCapabilityBuilder(rl, tag, null);
   }

   @Deprecated
   public static WeaponCapability.Builder deserializeWeaponCapabilityBuilder(
      ResourceLocation rl, CompoundTag tag, @Nullable ExtraEntryProvider extraEntryProvider
   ) {
      WeaponCapability.Builder builder = WeaponCapability.builder();
      if (tag.m_128441_("category") && !StringUtil.isNullOrEmpty(tag.m_128461_("category"))) {
         builder.category(WeaponCategory.ENUM_MANAGER.getOrThrow(tag.m_128461_("category")));
         builder.collider(ColliderPreset.deserializeSimpleCollider(tag.m_128469_("collider")));
         builder.canBePlacedOffhand(tag.m_128441_("usable_in_offhand") ? tag.m_128471_("usable_in_offhand") : true);
         if (tag.m_128441_("hit_particle")) {
            ParticleType<?> particleType = (ParticleType<?>)ForgeRegistries.PARTICLE_TYPES.getValue(ResourceLocation.parse(tag.m_128461_("hit_particle")));
            if (particleType == null) {
               EpicFightMod.LOGGER.warn("Can't find a particle type " + tag.m_128461_("hit_particle") + " in " + rl);
            } else if (!(particleType instanceof HitParticleType)) {
               EpicFightMod.LOGGER.warn(tag.m_128461_("hit_particle") + " is not a hit particle type in " + rl);
            } else {
               builder.hitParticle((HitParticleType)particleType);
            }
         }

         if (tag.m_128441_("swing_sound")) {
            SoundEvent sound = (SoundEvent)ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse(tag.m_128461_("swing_sound")));
            if (sound == null) {
               EpicFightMod.LOGGER.warn("Can't find a swing sound " + tag.m_128461_("swing_sound") + " in " + rl);
            } else {
               builder.swingSound(sound);
            }
         }

         if (tag.m_128441_("hit_sound")) {
            SoundEvent sound = (SoundEvent)ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse(tag.m_128461_("hit_sound")));
            if (sound == null) {
               EpicFightMod.LOGGER.warn("Can't find a hit sound " + tag.m_128461_("hit_sound") + " in " + rl);
            } else {
               builder.hitSound(sound);
            }
         }

         CompoundTag combosTag = tag.m_128469_("combos");

         for (String key : combosTag.m_128431_()) {
            Style style = Style.ENUM_MANAGER.getOrThrow(key);
            ListTag comboAnimations = combosTag.m_128437_(key, 8);
            List<AnimationManager.AnimationAccessor<? extends AttackAnimation>> anims = new ArrayList<>();

            for (int i = 0; i < comboAnimations.size(); i++) {
               String animId = comboAnimations.m_128778_(i);
               AnimationManager.AnimationAccessor<? extends AttackAnimation> animation = extraEntryProvider == null
                  ? AnimationManager.byKey(animId)
                  : extraEntryProvider.getExtraOrBuiltInAnimation(animId);
               if (animation == null) {
                  EpicFightMod.LOGGER.warn("Can't find an animation named " + comboAnimations.m_128778_(i) + " in " + rl);
               } else {
                  anims.add(animation);
               }
            }

            builder.newStyleCombo(style, anims.toArray(new AnimationManager.AnimationAccessor[0]));
         }

         CompoundTag innateSkillsTag = tag.m_128469_("innate_skills");

         for (String key : innateSkillsTag.m_128431_()) {
            Style style = Style.ENUM_MANAGER.getOrThrow(key);
            builder.innateSkill(style, itemstack -> SkillManager.getSkill(innateSkillsTag.m_128461_(key)));
         }

         CompoundTag livingmotionModifierTag = tag.m_128469_("livingmotion_modifier");

         for (String sStyle : livingmotionModifierTag.m_128431_()) {
            Style style = Style.ENUM_MANAGER.getOrThrow(sStyle);
            CompoundTag styleAnimationTag = livingmotionModifierTag.m_128469_(sStyle);

            for (String sLivingmotion : styleAnimationTag.m_128431_()) {
               LivingMotion livingmotion = LivingMotion.ENUM_MANAGER.getOrThrow(sLivingmotion);
               String animId = styleAnimationTag.m_128461_(sLivingmotion);
               AnimationManager.AnimationAccessor<? extends AttackAnimation> animation = extraEntryProvider == null
                  ? AnimationManager.byKey(animId)
                  : extraEntryProvider.getExtraOrBuiltInAnimation(animId);
               if (animation == null) {
                  EpicFightMod.LOGGER.warn("No animation named {}", styleAnimationTag.m_128461_(sLivingmotion));
               } else {
                  builder.livingMotionModifier(style, livingmotion, animation);
               }
            }
         }

         CompoundTag stylesTag = tag.m_128469_("styles");
         List<Pair<Predicate<LivingEntityPatch<?>>, Style>> conditions = Lists.newArrayList();
         Style defaultStyle = Style.ENUM_MANAGER.getOrThrow(stylesTag.m_128461_("default"));

         for (Tag caseTag : stylesTag.m_128437_("cases", 10)) {
            CompoundTag caseCompTag = (CompoundTag)caseTag;
            List<Condition.EntityPatchCondition> conditionList = Lists.newArrayList();

            for (Tag offhandTag : caseCompTag.m_128437_("conditions", 10)) {
               CompoundTag offhandCompound = (CompoundTag)offhandTag;
               Supplier<Condition.EntityPatchCondition> conditionProvider = EpicFightConditions.getConditionOrThrow(
                  ResourceLocation.parse(offhandCompound.m_128461_("predicate"))
               );
               Condition.EntityPatchCondition condition = conditionProvider.get();
               condition.read(offhandCompound);
               conditionList.add(condition);
            }

            conditions.add(Pair.of((Predicate<LivingEntityPatch>)entitypatch -> {
               for (Condition.EntityPatchCondition conditionx : conditionList) {
                  if (!conditionx.predicate(entitypatch)) {
                     return false;
                  }
               }

               return true;
            }, Style.ENUM_MANAGER.getOrThrow(caseCompTag.m_128461_("style"))));
         }

         builder.styleProvider(entitypatch -> {
            for (Pair<Predicate<LivingEntityPatch<?>>, Style> entry : conditions) {
               if (((Predicate)entry.getFirst()).test(entitypatch)) {
                  return (Style)entry.getSecond();
               }
            }

            return defaultStyle;
         });
         if (tag.m_128441_("offhand_item_compatible_predicate")) {
            ListTag offhandValidatorList = tag.m_128437_("offhand_item_compatible_predicate", 10);
            List<Condition.EntityPatchCondition> conditionList = Lists.newArrayList();

            for (Tag offhandTag : offhandValidatorList) {
               CompoundTag offhandCompound = (CompoundTag)offhandTag;
               Supplier<Condition.EntityPatchCondition> conditionProvider = EpicFightConditions.getConditionOrThrow(
                  ResourceLocation.parse(offhandCompound.m_128461_("predicate"))
               );
               Condition.EntityPatchCondition condition = conditionProvider.get();
               condition.read(offhandCompound);
               conditionList.add(condition);
            }

            builder.weaponCombinationPredicator(entitypatch -> {
               for (Condition.EntityPatchCondition conditionx : conditionList) {
                  if (!conditionx.predicate(entitypatch)) {
                     return false;
                  }
               }

               return true;
            });
         }

         return builder;
      } else {
         throw new IllegalArgumentException("Define weapon category.");
      }
   }

   public static int getTagCount() {
      return CAPABILITY_COMPOUNDS.size();
   }

   public static Stream<CompoundTag> getWeaponTypeDataStream() {
      return CAPABILITY_COMPOUNDS.entrySet().stream().map(entry -> {
         entry.getValue().m_128359_("registry_name", entry.getKey().toString());
         return entry.getValue();
      });
   }

   public static Set<Entry<ResourceLocation, Function<Item, CapabilityItem.Builder>>> entries() {
      return PRESETS.entrySet();
   }

   public static void clear() {
      PRESETS.clear();
      registerDefaultWeaponTypes();
   }

   @OnlyIn(Dist.CLIENT)
   public static void processServerPacket(SPDatapackSync packet) {
      if (packet.getType() == SPDatapackSync.Type.WEAPON_TYPE) {
         PRESETS.clear();
         registerDefaultWeaponTypes();

         for (CompoundTag tag : packet.getTags()) {
            ResourceLocation rl = ResourceLocation.parse(tag.m_128461_("registry_name"));

            try {
               PRESETS.put(rl, itemstack -> deserializeWeaponCapabilityBuilder(rl, tag));
            } catch (Exception e) {
               throw new RuntimeException("Weapon type " + rl + " encountered an error", e);
            }
         }

         ItemCapabilityReloadListener.weaponTypeProcessedCheck();
      }
   }
}
