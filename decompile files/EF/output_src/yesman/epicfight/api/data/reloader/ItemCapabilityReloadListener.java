package yesman.epicfight.api.data.reloader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.data.conditions.EpicFightConditions;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.server.SPDatapackSync;
import yesman.epicfight.world.capabilities.item.ArmorCapability;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.TagBasedSeparativeCapability;
import yesman.epicfight.world.capabilities.item.WeaponTypeReloadListener;
import yesman.epicfight.world.capabilities.provider.ExtraEntryProvider;
import yesman.epicfight.world.capabilities.provider.ItemCapabilityProvider;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;

public class ItemCapabilityReloadListener extends SimpleJsonResourceReloadListener {
   public static final String DIRECTORY = "capabilities";
   private static final Gson GSON = new GsonBuilder().create();
   private static final Map<Item, CompoundTag> ARMOR_COMPOUNDS = Maps.newHashMap();
   private static final Map<Item, CompoundTag> WEAPON_COMPOUNDS = Maps.newHashMap();
   private static boolean armorReceived = false;
   private static boolean weaponReceived = false;
   private static boolean weaponTypeReceived = false;

   public ItemCapabilityReloadListener() {
      super(GSON, "capabilities");
   }

   protected Map<ResourceLocation, JsonElement> m_5944_(ResourceManager resourceManager, ProfilerFiller profileIn) {
      ARMOR_COMPOUNDS.clear();
      WEAPON_COMPOUNDS.clear();
      return super.m_5944_(resourceManager, profileIn);
   }

   protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
      for (Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
         ResourceLocation rl = entry.getKey();
         String path = rl.m_135815_();
         if (path.contains("/") && !path.contains("types") && !path.contains("item_keyword")) {
            String[] str = path.split("/", 2);
            ResourceLocation registryName = ResourceLocation.fromNamespaceAndPath(rl.m_135827_(), str[1]);
            if (!ForgeRegistries.ITEMS.containsKey(registryName)) {
               EpicFightMod.LOGGER.warn("Item Capability Exception: No item named " + registryName);
            } else {
               Item item = (Item)ForgeRegistries.ITEMS.getValue(registryName);
               CompoundTag tag = null;

               try {
                  tag = TagParser.m_129359_(entry.getValue().toString());
               } catch (CommandSyntaxException e) {
                  EpicFightMod.LOGGER.warn("Error while deserializing datapack for " + registryName + ": " + e.getLocalizedMessage());
                  continue;
               }

               try {
                  if (str[0].equals("armors")) {
                     CapabilityItem capability = deserializeArmor(item, tag);
                     ItemCapabilityProvider.put(item, capability);
                     ARMOR_COMPOUNDS.put(item, tag);
                  } else if (str[0].equals("weapons")) {
                     CapabilityItem capability = deserializeWeapon(item, tag);
                     ItemCapabilityProvider.put(item, capability);
                     WEAPON_COMPOUNDS.put(item, tag);
                  }
               } catch (Exception e) {
                  EpicFightMod.LOGGER.warn("Error while deserializing datapack for " + registryName + ": " + e.getLocalizedMessage());
               }
            }
         }
      }

      ItemCapabilityProvider.addDefaultItems();
   }

   public static CapabilityItem deserializeArmor(Item item, CompoundTag tag) {
      ArmorCapability.Builder builder = ArmorCapability.builder();
      if (tag.m_128441_("attributes")) {
         CompoundTag attributes = tag.m_128469_("attributes");
         builder.weight(attributes.m_128459_("weight")).stunArmor(attributes.m_128459_("stun_armor"));
      }

      builder.item(item);
      return builder.build();
   }

   public static CapabilityItem deserializeWeapon(Item item, CompoundTag tag) {
      return deserializeWeapon(item, tag, null);
   }

   /** @deprecated */
   public static CapabilityItem deserializeWeapon(Item item, CompoundTag tag, @Nullable ExtraEntryProvider extraEntryProvider) {
      CapabilityItem capability;
      if (tag.m_128441_("variations")) {
         ListTag jsonArray = tag.m_128437_("variations", 10);
         List<Pair<Condition<ItemStack>, CapabilityItem>> list = Lists.newArrayList();
         CapabilityItem.Builder innerDefaultCapabilityBuilder = tag.m_128441_("type")
            ? (extraEntryProvider == null
                  ? WeaponTypeReloadListener.getOrThrow(tag.m_128461_("type"))
                  : extraEntryProvider.getExtraOrBuiltInWeaponType(tag.m_128461_("type")))
               .apply(item)
            : CapabilityItem.builder();
         if (tag.m_128441_("attributes")) {
            CompoundTag attributes = tag.m_128469_("attributes");

            for (String key : attributes.m_128431_()) {
               Map<Attribute, AttributeModifier> attributeEntry = deserializeAttributes(attributes.m_128469_(key));

               for (Entry<Attribute, AttributeModifier> attribute : attributeEntry.entrySet()) {
                  innerDefaultCapabilityBuilder.addStyleAttibutes(Style.ENUM_MANAGER.getOrThrow(key), Pair.of(attribute.getKey(), attribute.getValue()));
               }
            }
         }

         for (Tag jsonElement : jsonArray) {
            CompoundTag innerTag = (CompoundTag)jsonElement;
            Supplier<Condition<ItemStack>> conditionProvider = EpicFightConditions.getConditionOrThrow(ResourceLocation.parse(innerTag.m_128461_("condition")));
            Condition<ItemStack> condition = conditionProvider.get().read(innerTag.m_128469_("predicate"));
            list.add(Pair.of(condition, deserializeWeapon(item, innerTag)));
         }

         capability = new TagBasedSeparativeCapability(list, innerDefaultCapabilityBuilder.build());
      } else {
         CapabilityItem.Builder builder = tag.m_128441_("type")
            ? (extraEntryProvider == null
                  ? WeaponTypeReloadListener.getOrThrow(tag.m_128461_("type"))
                  : extraEntryProvider.getExtraOrBuiltInWeaponType(tag.m_128461_("type")))
               .apply(item)
            : CapabilityItem.builder();
         if (tag.m_128441_("attributes")) {
            CompoundTag attributes = tag.m_128469_("attributes");

            for (String key : attributes.m_128431_()) {
               Map<Attribute, AttributeModifier> attributeEntry = deserializeAttributes(attributes.m_128469_(key));
               Style style = Style.ENUM_MANAGER.getOrThrow(key);

               for (Entry<Attribute, AttributeModifier> attribute : attributeEntry.entrySet()) {
                  builder.addStyleAttibutes(style, Pair.of(attribute.getKey(), attribute.getValue()));
               }
            }
         }

         if (tag.m_128441_("collider")) {
            CompoundTag colliderTag = tag.m_128469_("collider");

            try {
               Collider collider = ColliderPreset.deserializeSimpleCollider(colliderTag);
               builder.collider(collider);
            } catch (IllegalArgumentException e) {
               EpicFightMod.LOGGER.warn("Can't deserialize collider of " + item + ": " + e.getMessage());
            }
         }

         capability = builder.build();
      }

      return capability;
   }

   private static Map<Attribute, AttributeModifier> deserializeAttributes(CompoundTag tag) {
      Map<Attribute, AttributeModifier> modifierMap = Maps.newHashMap();
      if (tag.m_128441_("armor_negation")) {
         modifierMap.put((Attribute)EpicFightAttributes.ARMOR_NEGATION.get(), EpicFightAttributes.getArmorNegationModifier(tag.m_128459_("armor_negation")));
      }

      if (tag.m_128441_("impact")) {
         modifierMap.put((Attribute)EpicFightAttributes.IMPACT.get(), EpicFightAttributes.getImpactModifier(tag.m_128459_("impact")));
      }

      if (tag.m_128441_("max_strikes")) {
         modifierMap.put((Attribute)EpicFightAttributes.MAX_STRIKES.get(), EpicFightAttributes.getMaxStrikesModifier(tag.m_128451_("max_strikes")));
      }

      if (tag.m_128441_("damage_bonus")) {
         modifierMap.put(Attributes.f_22281_, EpicFightAttributes.getDamageBonusModifier(tag.m_128459_("damage_bonus")));
      }

      if (tag.m_128441_("speed_bonus")) {
         modifierMap.put(Attributes.f_22283_, EpicFightAttributes.getSpeedBonusModifier(tag.m_128459_("speed_bonus")));
      }

      return modifierMap;
   }

   public static Stream<CompoundTag> getArmorDataStream() {
      return ARMOR_COMPOUNDS.entrySet().stream().map(entry -> {
         entry.getValue().m_128405_("id", Item.m_41393_(entry.getKey()));
         return entry.getValue();
      });
   }

   public static Stream<CompoundTag> getWeaponDataStream() {
      return WEAPON_COMPOUNDS.entrySet().stream().map(entry -> {
         entry.getValue().m_128405_("id", Item.m_41393_(entry.getKey()));
         return entry.getValue();
      });
   }

   public static int armorCount() {
      return ARMOR_COMPOUNDS.size();
   }

   public static int weaponCount() {
      return WEAPON_COMPOUNDS.size();
   }

   public static void weaponTypeProcessedCheck() {
      weaponTypeReceived = true;
   }

   @OnlyIn(Dist.CLIENT)
   public static void reset() {
      armorReceived = false;
      weaponReceived = false;
      weaponTypeReceived = false;
   }

   @OnlyIn(Dist.CLIENT)
   public static void processServerPacket(SPDatapackSync packet) {
      switch (packet.getType()) {
         case ARMOR:
            for (CompoundTag tag : packet.getTags()) {
               Item item = Item.m_41445_(tag.m_128451_("id"));
               ARMOR_COMPOUNDS.put(item, tag);
            }

            armorReceived = true;
            break;
         case WEAPON:
            for (CompoundTag tag : packet.getTags()) {
               Item item = Item.m_41445_(tag.m_128451_("id"));
               WEAPON_COMPOUNDS.put(item, tag);
            }

            weaponReceived = true;
      }

      if (weaponTypeReceived && armorReceived && weaponReceived) {
         ARMOR_COMPOUNDS.forEach((itemx, tagx) -> {
            try {
               CapabilityItem itemCap = deserializeArmor(itemx, tagx);
               ItemCapabilityProvider.put(itemx, itemCap);
            } catch (NoSuchElementException e) {
               EpicFightMod.LOGGER.warn("Error while creating capability " + itemx + ": " + e.getLocalizedMessage());
            } catch (Exception e) {
               EpicFightMod.LOGGER.warn("Can't read item capability for " + itemx + ": " + e.getLocalizedMessage());
            }
         });
         WEAPON_COMPOUNDS.forEach((itemx, tagx) -> {
            try {
               CapabilityItem itemCap = deserializeWeapon(itemx, tagx);
               ItemCapabilityProvider.put(itemx, itemCap);
            } catch (NoSuchElementException var3) {
            } catch (Exception e) {
               EpicFightMod.LOGGER.warn("Can't read item capability for " + itemx + ": " + e.getLocalizedMessage());
            }
         });
         ItemCapabilityProvider.addDefaultItems();
      }
   }
}
