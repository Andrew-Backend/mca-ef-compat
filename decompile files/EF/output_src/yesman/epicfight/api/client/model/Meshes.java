package yesman.epicfight.api.client.model;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier;
import net.minecraft.util.profiling.ProfilerFiller;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.asset.JsonAssetLoader;
import yesman.epicfight.api.client.physics.cloth.ClothSimulatable;
import yesman.epicfight.api.client.physics.cloth.ClothSimulator;
import yesman.epicfight.client.mesh.CreeperMesh;
import yesman.epicfight.client.mesh.DragonMesh;
import yesman.epicfight.client.mesh.EndermanMesh;
import yesman.epicfight.client.mesh.HoglinMesh;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.mesh.IronGolemMesh;
import yesman.epicfight.client.mesh.PiglinMesh;
import yesman.epicfight.client.mesh.RavagerMesh;
import yesman.epicfight.client.mesh.SpiderMesh;
import yesman.epicfight.client.mesh.VexMesh;
import yesman.epicfight.client.mesh.VillagerMesh;
import yesman.epicfight.client.mesh.WitherMesh;

public class Meshes implements PreparableReloadListener {
   private static final Map<ResourceLocation, Meshes.MeshAccessor<? extends Mesh>> ACCESSORS = Maps.newHashMap();
   private static final Map<Meshes.MeshAccessor<? extends Mesh>, Mesh> MESHES = Maps.newHashMap();
   private static ResourceManager resourceManager = null;
   public static final Meshes INSTANCE = new Meshes();
   public static final Meshes.MeshAccessor<HumanoidMesh> ALEX = Meshes.MeshAccessor.create(
      "epicfight", "entity/biped_slim_arm", jsonModelLoader -> jsonModelLoader.loadSkinnedMesh(HumanoidMesh::new)
   );
   public static final Meshes.MeshAccessor<HumanoidMesh> BIPED = Meshes.MeshAccessor.create(
      "epicfight", "entity/biped", jsonModelLoader -> jsonModelLoader.loadSkinnedMesh(HumanoidMesh::new)
   );
   public static final Meshes.MeshAccessor<HumanoidMesh> BIPED_OLD_TEX = Meshes.MeshAccessor.create(
      "epicfight", "entity/biped_old_texture", jsonModelLoader -> jsonModelLoader.loadSkinnedMesh(HumanoidMesh::new)
   );
   public static final Meshes.MeshAccessor<HumanoidMesh> BIPED_OUTLAYER = Meshes.MeshAccessor.create(
      "epicfight", "entity/biped_outlayer", jsonModelLoader -> jsonModelLoader.loadSkinnedMesh(HumanoidMesh::new)
   );
   public static final Meshes.MeshAccessor<VillagerMesh> VILLAGER_ZOMBIE = Meshes.MeshAccessor.create(
      "epicfight", "entity/zombie_villager", jsonModelLoader -> jsonModelLoader.loadSkinnedMesh(VillagerMesh::new)
   );
   public static final Meshes.MeshAccessor<CreeperMesh> CREEPER = Meshes.MeshAccessor.create(
      "epicfight", "entity/creeper", jsonModelLoader -> jsonModelLoader.loadSkinnedMesh(CreeperMesh::new)
   );
   public static final Meshes.MeshAccessor<EndermanMesh> ENDERMAN = Meshes.MeshAccessor.create(
      "epicfight", "entity/enderman", jsonModelLoader -> jsonModelLoader.loadSkinnedMesh(EndermanMesh::new)
   );
   public static final Meshes.MeshAccessor<HumanoidMesh> SKELETON = Meshes.MeshAccessor.create(
      "epicfight", "entity/skeleton", jsonModelLoader -> jsonModelLoader.loadSkinnedMesh(HumanoidMesh::new)
   );
   public static final Meshes.MeshAccessor<SpiderMesh> SPIDER = Meshes.MeshAccessor.create(
      "epicfight", "entity/spider", jsonModelLoader -> jsonModelLoader.loadSkinnedMesh(SpiderMesh::new)
   );
   public static final Meshes.MeshAccessor<IronGolemMesh> IRON_GOLEM = Meshes.MeshAccessor.create(
      "epicfight", "entity/iron_golem", jsonModelLoader -> jsonModelLoader.loadSkinnedMesh(IronGolemMesh::new)
   );
   public static final Meshes.MeshAccessor<HumanoidMesh> ILLAGER = Meshes.MeshAccessor.create(
      "epicfight", "entity/illager", jsonModelLoader -> jsonModelLoader.loadSkinnedMesh(VillagerMesh::new)
   );
   public static final Meshes.MeshAccessor<VillagerMesh> WITCH = Meshes.MeshAccessor.create(
      "epicfight", "entity/witch", jsonModelLoader -> jsonModelLoader.loadSkinnedMesh(VillagerMesh::new)
   );
   public static final Meshes.MeshAccessor<RavagerMesh> RAVAGER = Meshes.MeshAccessor.create(
      "epicfight", "entity/ravager", jsonModelLoader -> jsonModelLoader.loadSkinnedMesh(RavagerMesh::new)
   );
   public static final Meshes.MeshAccessor<VexMesh> VEX = Meshes.MeshAccessor.create(
      "epicfight", "entity/vex", jsonModelLoader -> jsonModelLoader.loadSkinnedMesh(VexMesh::new)
   );
   public static final Meshes.MeshAccessor<PiglinMesh> PIGLIN = Meshes.MeshAccessor.create(
      "epicfight", "entity/piglin", jsonModelLoader -> jsonModelLoader.loadSkinnedMesh(PiglinMesh::new)
   );
   public static final Meshes.MeshAccessor<HoglinMesh> HOGLIN = Meshes.MeshAccessor.create(
      "epicfight", "entity/hoglin", jsonModelLoader -> jsonModelLoader.loadSkinnedMesh(HoglinMesh::new)
   );
   public static final Meshes.MeshAccessor<DragonMesh> DRAGON = Meshes.MeshAccessor.create(
      "epicfight", "entity/dragon", jsonModelLoader -> jsonModelLoader.loadSkinnedMesh(DragonMesh::new)
   );
   public static final Meshes.MeshAccessor<WitherMesh> WITHER = Meshes.MeshAccessor.create(
      "epicfight", "entity/wither", jsonModelLoader -> jsonModelLoader.loadSkinnedMesh(WitherMesh::new)
   );
   public static final Meshes.MeshAccessor<SkinnedMesh> HELMET = Meshes.MeshAccessor.create(
      "epicfight", "armor/helmet", jsonModelLoader -> jsonModelLoader.loadSkinnedMesh(SkinnedMesh::new)
   );
   public static final Meshes.MeshAccessor<SkinnedMesh> HELMET_PIGLIN = Meshes.MeshAccessor.create(
      "epicfight", "armor/piglin_helmet", jsonModelLoader -> jsonModelLoader.loadSkinnedMesh(SkinnedMesh::new)
   );
   public static final Meshes.MeshAccessor<SkinnedMesh> HELMET_VILLAGER = Meshes.MeshAccessor.create(
      "epicfight", "armor/villager_helmet", jsonModelLoader -> jsonModelLoader.loadSkinnedMesh(SkinnedMesh::new)
   );
   public static final Meshes.MeshAccessor<SkinnedMesh> CHESTPLATE = Meshes.MeshAccessor.create(
      "epicfight", "armor/chestplate", jsonModelLoader -> jsonModelLoader.loadSkinnedMesh(SkinnedMesh::new)
   );
   public static final Meshes.MeshAccessor<SkinnedMesh> LEGGINS = Meshes.MeshAccessor.create(
      "epicfight", "armor/leggins", jsonModelLoader -> jsonModelLoader.loadSkinnedMesh(SkinnedMesh::new)
   );
   public static final Meshes.MeshAccessor<SkinnedMesh> BOOTS = Meshes.MeshAccessor.create(
      "epicfight", "armor/boots", jsonModelLoader -> jsonModelLoader.loadSkinnedMesh(SkinnedMesh::new)
   );
   public static final Meshes.MeshAccessor<ClassicMesh> AIR_BURST = Meshes.MeshAccessor.create(
      "epicfight", "particle/air_burst", jsonModelLoader -> jsonModelLoader.loadClassicMesh(ClassicMesh::new)
   );
   public static final Meshes.MeshAccessor<ClassicMesh> FORCE_FIELD = Meshes.MeshAccessor.create(
      "epicfight", "particle/force_field", jsonModelLoader -> jsonModelLoader.loadClassicMesh(ClassicMesh::new)
   );
   public static final Meshes.MeshAccessor<ClassicMesh> LASER = Meshes.MeshAccessor.create(
      "epicfight", "particle/laser", jsonModelLoader -> jsonModelLoader.loadClassicMesh(ClassicMesh::new)
   );
   public static final Meshes.MeshAccessor<SkinnedMesh> CAPE_DEFAULT = Meshes.MeshAccessor.create(
      "epicfight", "layer/default_cape", jsonModelLoader -> jsonModelLoader.loadSkinnedMesh(SkinnedMesh::new)
   );

   public static void reload(ResourceManager resourceManager) {
      Meshes.resourceManager = resourceManager;
      ACCESSORS.entrySet().removeIf(entry -> !entry.getValue().inRegistry);
      MESHES.values().forEach(mesh -> {
         if (mesh instanceof SkinnedMesh skinnedMesh) {
            skinnedMesh.destroy();
         }
      });
      MESHES.clear();
   }

   @Nullable
   public static <M extends Mesh> AssetAccessor<M> get(ResourceLocation id) {
      return (AssetAccessor<M>)ACCESSORS.get(id);
   }

   public static <M extends Mesh> AssetAccessor<M> getOrCreate(ResourceLocation id, Function<JsonAssetLoader, M> jsonLoader) {
      return (AssetAccessor<M>)(ACCESSORS.containsKey(id) ? ACCESSORS.get(id) : Meshes.MeshAccessor.create(id, jsonLoader, false));
   }

   public static <M extends Mesh> Set<AssetAccessor<M>> entry(Class<? extends Mesh> filter) {
      return ACCESSORS.values()
         .stream()
         .filter(accessor -> filter.isAssignableFrom(accessor.get().getClass()))
         .map(accessor -> accessor)
         .collect(Collectors.toSet());
   }

   public static ResourceLocation wrapLocation(ResourceLocation rl) {
      return rl.m_135815_().matches("animmodels/.*\\.json")
         ? rl
         : ResourceLocation.fromNamespaceAndPath(rl.m_135827_(), "animmodels/" + rl.m_135815_() + ".json");
   }

   public CompletableFuture<Void> m_5540_(
      PreparationBarrier stage,
      ResourceManager resourceManager,
      ProfilerFiller preparationsProfiler,
      ProfilerFiller reloadProfiler,
      Executor backgroundExecutor,
      Executor gameExecutor
   ) {
      return CompletableFuture.runAsync(() -> reload(resourceManager), gameExecutor).thenCompose(stage::m_6769_);
   }

   public record MeshAccessor<M extends Mesh>(ResourceLocation registryName, Function<JsonAssetLoader, M> jsonLoader, boolean inRegistry)
      implements AssetAccessor<M>,
      SoftBodyTranslatable {
      public static <M extends Mesh> Meshes.MeshAccessor<M> create(String namespaceId, String path, Function<JsonAssetLoader, M> jsonLoader) {
         return create(ResourceLocation.fromNamespaceAndPath(namespaceId, path), jsonLoader, true);
      }

      private static <M extends Mesh> Meshes.MeshAccessor<M> create(ResourceLocation id, Function<JsonAssetLoader, M> jsonLoader, boolean inRegistry) {
         Meshes.MeshAccessor<M> accessor = new Meshes.MeshAccessor<>(id, jsonLoader, inRegistry);
         Meshes.ACCESSORS.put(id, accessor);
         return accessor;
      }

      public M get() {
         if (!Meshes.MESHES.containsKey(this)) {
            JsonAssetLoader jsonModelLoader = new JsonAssetLoader(Meshes.resourceManager, Meshes.wrapLocation(this.registryName));
            Meshes.MESHES.put(this, this.jsonLoader.apply(jsonModelLoader));
         }

         return (M)Meshes.MESHES.get(this);
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
         } else if (obj instanceof Meshes.MeshAccessor armatureAccessor) {
            return this.registryName.equals(armatureAccessor.registryName());
         } else if (obj instanceof ResourceLocation rl) {
            return this.registryName.equals(rl);
         } else {
            return obj instanceof String name ? this.registryName.toString().equals(name) : false;
         }
      }

      @Override
      public boolean canStartSoftBodySimulation() {
         Mesh mesh = this.get();
         if (mesh instanceof StaticMesh<?> staticMesh) {
            return staticMesh.canStartSoftBodySimulation();
         } else {
            return mesh instanceof CompositeMesh compositeMesh ? compositeMesh.canStartSoftBodySimulation() : false;
         }
      }

      public ClothSimulator.ClothObject createSimulationData(
         SoftBodyTranslatable provider, ClothSimulatable simOwner, ClothSimulator.ClothObjectBuilder simBuilder
      ) {
         Mesh mesh = this.get();
         if (mesh instanceof StaticMesh<?> staticMesh) {
            return staticMesh.createSimulationData(provider, simOwner, simBuilder);
         } else {
            return mesh instanceof CompositeMesh compositeMesh ? compositeMesh.createSimulationData(provider, simOwner, simBuilder) : null;
         }
      }

      @Override
      public void putSoftBodySimulationInfo(Map<String, SoftBodyTranslatable.ClothSimulationInfo> sofyBodySimulationInfo) {
         if (this.get() instanceof SoftBodyTranslatable softBodyTranslatable) {
            softBodyTranslatable.putSoftBodySimulationInfo(sofyBodySimulationInfo);
         }
      }

      @Override
      public Map<String, SoftBodyTranslatable.ClothSimulationInfo> getSoftBodySimulationInfo() {
         return this.get() instanceof SoftBodyTranslatable softBodyTranslatable ? softBodyTranslatable.getSoftBodySimulationInfo() : null;
      }
   }

   @FunctionalInterface
   public interface MeshContructor<P extends MeshPart, V extends VertexBuilder, M extends StaticMesh<P>> {
      M invoke(Map<String, Number[]> var1, Map<MeshPartDefinition, List<V>> var2, M var3, Mesh.RenderProperties var4);
   }
}
