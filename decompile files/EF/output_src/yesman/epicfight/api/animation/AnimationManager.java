package yesman.epicfight.api.animation;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import org.apache.logging.log4j.Logger;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.asset.JsonAssetLoader;
import yesman.epicfight.api.client.animation.AnimationSubFileReader;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.api.exception.AssetLoadingException;
import yesman.epicfight.api.utils.InstantiateInvoker;
import yesman.epicfight.api.utils.MutableBoolean;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.main.EpicFightSharedConstants;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPCheckAnimationRegistryMatches;
import yesman.epicfight.network.server.SPDatapackSync;

public class AnimationManager extends SimplePreparableReloadListener<List<ResourceLocation>> {
   private static final AnimationManager INSTANCE = new AnimationManager();
   private static ResourceManager serverResourceManager = null;
   private static final Gson GSON = new GsonBuilder().create();
   private static final String DIRECTORY = "animmodels/animations";
   private final Map<Integer, AnimationManager.AnimationAccessor<? extends StaticAnimation>> animationById = Maps.newHashMap();
   private final Map<ResourceLocation, AnimationManager.AnimationAccessor<? extends StaticAnimation>> animationByName = Maps.newHashMap();
   private final Map<AnimationManager.AnimationAccessor<? extends StaticAnimation>, StaticAnimation> animations = Maps.newHashMap();
   private final Map<AnimationManager.AnimationAccessor<? extends StaticAnimation>, String> resourcepackAnimationCommands = Maps.newHashMap();
   private static final Set<String> NO_WARNING_MODID = Sets.newHashSet();

   public static AnimationManager getInstance() {
      return INSTANCE;
   }

   public static boolean checkNull(AssetAccessor<? extends StaticAnimation> animation) {
      if (animation != null && !animation.isEmpty()) {
         return false;
      }

      if (animation != null) {
         EpicFightMod.stacktraceIfDevSide("Empty animation accessor: " + animation.registryName(), NoSuchElementException::new);
      } else {
         EpicFightMod.stacktraceIfDevSide("Null animation accessor", NoSuchElementException::new);
      }

      return true;
   }

   public static <T extends StaticAnimation> AnimationManager.AnimationAccessor<T> byKey(String registryName) {
      return byKey(ResourceLocation.parse(registryName));
   }

   public static <T extends StaticAnimation> AnimationManager.AnimationAccessor<T> byKey(ResourceLocation registryName) {
      return (AnimationManager.AnimationAccessor<T>)getInstance().animationByName.get(registryName);
   }

   public static <T extends StaticAnimation> AnimationManager.AnimationAccessor<T> byId(int animationId) {
      return (AnimationManager.AnimationAccessor<T>)getInstance().animationById.get(animationId);
   }

   public Map<ResourceLocation, AnimationManager.AnimationAccessor<? extends StaticAnimation>> getAnimations(
      Predicate<AssetAccessor<? extends StaticAnimation>> filter
   ) {
      Map<ResourceLocation, AnimationManager.AnimationAccessor<? extends StaticAnimation>> filteredItems = this.animationByName
         .entrySet()
         .stream()
         .filter(entry -> filter.test(entry.getValue()))
         .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
      return ImmutableMap.copyOf(filteredItems);
   }

   public AnimationClip loadAnimationClip(StaticAnimation animation, BiFunction<JsonAssetLoader, StaticAnimation, AnimationClip> clipLoader) {
      try {
         if (getAnimationResourceManager() == null) {
            return null;
         }

         JsonAssetLoader modelLoader = new JsonAssetLoader(getAnimationResourceManager(), animation.getLocation());
         return clipLoader.apply(modelLoader, animation);
      } catch (AssetLoadingException e) {
         throw new AssetLoadingException("Failed to load animation clip from: " + animation, e);
      }
   }

   public static void readAnimationProperties(StaticAnimation animation) {
      ResourceLocation dataLocation = getSubAnimationFileLocation(animation.getLocation(), AnimationSubFileReader.SUBFILE_CLIENT_PROPERTY);
      ResourceLocation povLocation = getSubAnimationFileLocation(animation.getLocation(), AnimationSubFileReader.SUBFILE_POV_ANIMATION);
      getAnimationResourceManager()
         .m_213713_(dataLocation)
         .ifPresent(rs -> AnimationSubFileReader.readAndApply(animation, rs, AnimationSubFileReader.SUBFILE_CLIENT_PROPERTY));
      getAnimationResourceManager()
         .m_213713_(povLocation)
         .ifPresent(rs -> AnimationSubFileReader.readAndApply(animation, rs, AnimationSubFileReader.SUBFILE_POV_ANIMATION));
   }

   protected List<ResourceLocation> prepare(ResourceManager resourceManager, ProfilerFiller profilerIn) {
      if (!EpicFightSharedConstants.isPhysicalClient() && serverResourceManager == null) {
         serverResourceManager = resourceManager;
      }

      this.animations.clear();
      this.animationById.entrySet().removeIf(entry -> !entry.getValue().inRegistry());
      this.animationByName.entrySet().removeIf(entry -> !entry.getValue().inRegistry());
      this.resourcepackAnimationCommands.clear();
      List<ResourceLocation> directories = new ArrayList<>();
      scanDirectoryNames(resourceManager, directories);
      return directories;
   }

   private static void scanDirectoryNames(ResourceManager resourceManager, List<ResourceLocation> output) {
      FileToIdConverter filetoidconverter = FileToIdConverter.m_246568_("animmodels/animations");
      filetoidconverter.m_247457_(resourceManager).keySet().stream().map(AnimationManager::pathToId).forEach(output::add);
   }

   protected void apply(List<ResourceLocation> objects, ResourceManager resourceManager, ProfilerFiller profilerIn) {
      Armatures.reload(resourceManager);
      Set<ResourceLocation> registeredAnimation = this.animationById.values().stream().reduce(new HashSet<>(), (set, accessor) -> {
         set.add(accessor.registryName());

         for (AssetAccessor<? extends StaticAnimation> subAnimAccessor : accessor.get().getSubAnimations()) {
            set.add(subAnimAccessor.registryName());
         }

         return set;
      }, (set1, set2) -> {
         set1.addAll(set2);
         return set1;
      });
      objects.stream()
         .filter(animId -> !registeredAnimation.contains(animId) && !animId.m_135815_().contains("/data/") && !animId.m_135815_().contains("/pov/"))
         .sorted(Comparator.comparing(ResourceLocation::toString))
         .forEach(animId -> {
            Optional<Resource> resource = resourceManager.m_213713_(idToPath(animId));

            try (Reader reader = resource.orElseThrow().m_215508_()) {
               JsonElement jsonelement = (JsonElement)GsonHelper.m_13776_(GSON, reader, JsonElement.class);
               this.readResourcepackAnimation(animId, jsonelement.getAsJsonObject());
            } catch (IOException | JsonParseException | IllegalArgumentException resourceReadException) {
               EpicFightMod.LOGGER.error("Couldn't parse animation data from {}", animId, resourceReadException);
            } catch (Exception e) {
               EpicFightMod.LOGGER.error("Failed at constructing {}", animId, e);
            }
         });
      SkillManager.reloadAllSkillsAnimations();
      this.animations.entrySet().stream().reduce(new ArrayList<>(), (list, entry) -> {
         MutableBoolean init = new MutableBoolean(true);
         if (entry.getValue() == null || entry.getValue().getAccessor() == null) {
            EpicFightMod.logAndStacktraceIfDevSide(Logger::error, "Invalid animation implementation: " + entry.getKey(), AssetLoadingException::new);
            init.set(false);
         }

         entry.getValue().getSubAnimations().forEach(subAnimation -> {
            if (subAnimation == null || subAnimation.get() == null) {
               EpicFightMod.logAndStacktraceIfDevSide(Logger::error, "Invalid sub animation implementation: " + entry.getKey(), AssetLoadingException::new);
               init.set(false);
            }
         });
         if (init.value()) {
            list.add(entry.getValue().getAccessor());
            list.addAll(entry.getValue().getSubAnimations());
         }

         return list;
      }, (list1, list2) -> {
         list1.addAll(list2);
         return list1;
      }).forEach(accessor -> {
         accessor.doOrThrow(StaticAnimation::postInit);
         if (EpicFightSharedConstants.isPhysicalClient()) {
            readAnimationProperties((StaticAnimation)accessor.get());
         }
      });
   }

   public static ResourceLocation getSubAnimationFileLocation(ResourceLocation location, AnimationSubFileReader.SubFileType<?> subFileType) {
      int splitIdx = location.m_135815_().lastIndexOf(47);
      if (splitIdx < 0) {
         splitIdx = 0;
      }

      return ResourceLocation.fromNamespaceAndPath(
         location.m_135827_(),
         String.format("%s/" + subFileType.getDirectory() + "%s", location.m_135815_().substring(0, splitIdx), location.m_135815_().substring(splitIdx))
      );
   }

   public static ResourceLocation idToPath(ResourceLocation rl) {
      return rl.m_135815_().matches("animmodels/animations/.*\\.json")
         ? rl
         : ResourceLocation.fromNamespaceAndPath(rl.m_135827_(), "animmodels/animations/" + rl.m_135815_() + ".json");
   }

   public static ResourceLocation pathToId(ResourceLocation rl) {
      return ResourceLocation.fromNamespaceAndPath(rl.m_135827_(), rl.m_135815_().replace("animmodels/animations/", "").replace(".json", ""));
   }

   public static void setServerResourceManager(ResourceManager pResourceManager) {
      serverResourceManager = pResourceManager;
   }

   public static ResourceManager getAnimationResourceManager() {
      return EpicFightSharedConstants.isPhysicalClient() ? Minecraft.m_91087_().m_91098_() : serverResourceManager;
   }

   public int getResourcepackAnimationCount() {
      return this.resourcepackAnimationCommands.size();
   }

   public Stream<CompoundTag> getResourcepackAnimationStream() {
      return this.resourcepackAnimationCommands.entrySet().stream().map(entry -> {
         CompoundTag compTag = new CompoundTag();
         compTag.m_128359_("registry_name", entry.getKey().registryName().toString());
         compTag.m_128405_("id", entry.getKey().id());
         compTag.m_128359_("invoke_command", entry.getValue());
         return compTag;
      });
   }

   @OnlyIn(Dist.CLIENT)
   public void processServerPacket(SPDatapackSync packet, boolean mandatoryPack) {
      if (mandatoryPack) {
         for (CompoundTag tag : packet.getTags()) {
            String invocationCommand = tag.m_128461_("invoke_command");
            ResourceLocation registryName = ResourceLocation.parse(tag.m_128461_("registry_name"));
            int id = tag.m_128451_("id");
            if (!this.animationByName.containsKey(registryName)) {
               AnimationManager.AnimationAccessor<? extends StaticAnimation> accessor = AnimationManager.AnimationAccessorImpl.create(
                  registryName, this.getResourcepackAnimationCount(), false, accessor$2 -> {
                     try {
                        return InstantiateInvoker.invoke(invocationCommand, StaticAnimation.class).getResult();
                     } catch (Exception e) {
                        EpicFightMod.LOGGER.warn("Failed at creating animation from server resource pack");
                        e.printStackTrace();
                        return Animations.EMPTY_ANIMATION;
                     }
                  }
               );
               this.animationById.put(id, accessor);
               this.animationByName.put(registryName, accessor);
            }
         }
      }

      int animationCount = this.animations.size();
      String[] registryNames = new String[animationCount];

      for (int i = 0; i < animationCount; i++) {
         String registryName = this.animationById.get(i + 1).registryName().toString();
         registryNames[i] = registryName;
      }

      CPCheckAnimationRegistryMatches registrySyncPacket = new CPCheckAnimationRegistryMatches(animationCount, registryNames);
      EpicFightNetworkManager.sendToServer(registrySyncPacket);
   }

   public void validateClientAnimationRegistry(CPCheckAnimationRegistryMatches msg, ServerGamePacketListenerImpl connection) {
      StringBuilder messageBuilder = new StringBuilder();
      int count = 0;
      Set<String> clientAnimationRegistry = new HashSet<>(Set.of(msg.registryNames));

      for (String registryName : this.animations.keySet().stream().map(rl -> rl.toString()).toList()) {
         if (!clientAnimationRegistry.contains(registryName)) {
            if (count < 10) {
               messageBuilder.append(registryName);
               messageBuilder.append("\n");
            }

            count++;
         } else {
            clientAnimationRegistry.remove(registryName);
         }
      }

      for (String registryName : clientAnimationRegistry) {
         if (!registryName.equals("empty")) {
            if (count < 10) {
               messageBuilder.append(registryName);
               messageBuilder.append("\n");
            }

            count++;
         }
      }

      if (count >= 10) {
         messageBuilder.append(Component.m_237110_("gui.epicfight.warn.animation_unsync.etc", new Object[]{count - 9}).getString());
         messageBuilder.append("\n");
      }

      if (!messageBuilder.isEmpty()) {
         connection.m_9942_(Component.m_237110_("gui.epicfight.warn.animation_unsync", new Object[]{messageBuilder.toString()}));
      }
   }

   public static void addNoWarningModId(String modid) {
      NO_WARNING_MODID.add(modid);
   }

   private void readResourcepackAnimation(ResourceLocation rl, JsonObject json) throws Exception {
      JsonElement constructorElement = json.get("constructor");
      if (constructorElement == null) {
         if (!NO_WARNING_MODID.contains(rl.m_135827_())) {
            EpicFightMod.logAndStacktraceIfDevSide(
               Logger::error,
               "Datapack animation reading failed: No constructor information has provided: " + rl,
               IllegalStateException::new,
               "No constructor information has provided in User animation, "
                  + rl
                  + "\nPlease remove this resource if it's unnecessary to optimize your project."
            );
         }
      } else {
         JsonObject constructorObject = constructorElement.getAsJsonObject();
         String invocationCommand = constructorObject.get("invocation_command").getAsString();
         StaticAnimation animation = InstantiateInvoker.invoke(invocationCommand, StaticAnimation.class).getResult();
         JsonElement propertiesElement = json.getAsJsonObject().get("properties");
         if (propertiesElement != null) {
            JsonObject propertiesObject = propertiesElement.getAsJsonObject();

            for (Entry<String, JsonElement> entry : propertiesObject.entrySet()) {
               AnimationProperty<?> propertyKey = AnimationProperty.getSerializableProperty(entry.getKey());
               Object value = propertyKey.parseFrom(entry.getValue());
               animation.addPropertyUnsafe(propertyKey, value);
            }
         }

         AnimationManager.AnimationAccessor<StaticAnimation> accessor = AnimationManager.AnimationAccessorImpl.create(
            rl, this.animations.size() + 1, false, null
         );
         animation.setAccessor(accessor);
         this.resourcepackAnimationCommands.put(accessor, invocationCommand);
         this.animationById.put(accessor.id(), accessor);
         this.animationByName.put(accessor.registryName(), accessor);
         this.animations.put(accessor, animation);
      }
   }

   public interface AnimationAccessor<A extends DynamicAnimation> extends AssetAccessor<A> {
      int id();

      default boolean idBetween(
         AnimationManager.AnimationAccessor<? extends StaticAnimation> a1, AnimationManager.AnimationAccessor<? extends StaticAnimation> a2
      ) {
         return a1.id() <= this.id() && a2.id() >= this.id();
      }
   }

   public record AnimationAccessorImpl<A extends StaticAnimation>(
      ResourceLocation registryName, int id, boolean inRegistry, Function<AnimationManager.AnimationAccessor<A>, A> onLoad
   ) implements AnimationManager.AnimationAccessor<A> {
      private static <A extends StaticAnimation> AnimationManager.AnimationAccessor<A> create(
         ResourceLocation registryName, int id, boolean inRegistry, Function<AnimationManager.AnimationAccessor<A>, A> onLoad
      ) {
         return new AnimationManager.AnimationAccessorImpl<>(registryName, id, inRegistry, onLoad);
      }

      public A get() {
         if (!AnimationManager.INSTANCE.animations.containsKey(this)) {
            AnimationManager.INSTANCE.animations.put(this, this.onLoad.apply(this));
         }

         return (A)AnimationManager.INSTANCE.animations.get(this);
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
         } else if (obj instanceof AnimationManager.AnimationAccessor armatureAccessor) {
            return this.registryName.equals(armatureAccessor.registryName());
         } else if (obj instanceof ResourceLocation rl) {
            return this.registryName.equals(rl);
         } else {
            return obj instanceof String name ? this.registryName.toString().equals(name) : false;
         }
      }
   }

   public record AnimationBuilder(String namespace, Consumer<AnimationManager.AnimationBuilder> task) {
      public <T extends StaticAnimation> AnimationManager.AnimationAccessor<T> nextAccessor(
         String id, Function<AnimationManager.AnimationAccessor<T>, T> onLoad
      ) {
         AnimationManager.AnimationAccessor<T> accessor = AnimationManager.AnimationAccessorImpl.create(
            ResourceLocation.fromNamespaceAndPath(this.namespace, id), AnimationManager.INSTANCE.animations.size() + 1, true, onLoad
         );
         AnimationManager.INSTANCE.animationById.put(accessor.id(), accessor);
         AnimationManager.INSTANCE.animationByName.put(accessor.registryName(), accessor);
         AnimationManager.INSTANCE.animations.put(accessor, null);
         return accessor;
      }
   }

   public static class AnimationRegistryEvent extends Event implements IModBusEvent {
      private List<AnimationManager.AnimationBuilder> builders = Lists.newArrayList();
      private Set<String> namespaces = Sets.newHashSet();

      public void newBuilder(String namespace, Consumer<AnimationManager.AnimationBuilder> build) {
         if (this.namespaces.contains(namespace)) {
            throw new IllegalArgumentException("Animation builder namespace '" + namespace + "' already exists!");
         }

         this.namespaces.add(namespace);
         this.builders.add(new AnimationManager.AnimationBuilder(namespace, build));
      }

      public List<AnimationManager.AnimationBuilder> getBuilders() {
         return this.builders;
      }
   }
}
