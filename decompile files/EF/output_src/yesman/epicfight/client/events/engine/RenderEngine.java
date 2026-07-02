package yesman.epicfight.client.events.engine;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.RavagerModel;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.VexModel;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.model.ZombieVillagerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.DrownedRenderer;
import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RavagerRenderer;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.VexRenderer;
import net.minecraft.client.renderer.entity.WitchRenderer;
import net.minecraft.client.renderer.entity.WitherBossRenderer;
import net.minecraft.client.renderer.entity.ZombieVillagerRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent.BossEventProgress;
import net.minecraftforge.client.event.RenderHighlightEvent.Block;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.client.event.RenderLivingEvent.Pre;
import net.minecraftforge.client.event.ViewportEvent.ComputeCameraAngles;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.client.animation.AnimationSubFileReader;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.client.forgeevent.PatchedRenderersEvent;
import yesman.epicfight.api.client.forgeevent.RenderEnderDragonEvent;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.gui.EntityUI;
import yesman.epicfight.client.gui.VersionNotifier;
import yesman.epicfight.client.gui.screen.config.UISetupScreen;
import yesman.epicfight.client.gui.screen.overlay.OverlayManager;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.client.mesh.CreeperMesh;
import yesman.epicfight.client.mesh.EndermanMesh;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.mesh.IronGolemMesh;
import yesman.epicfight.client.mesh.RavagerMesh;
import yesman.epicfight.client.mesh.SpiderMesh;
import yesman.epicfight.client.mesh.VexMesh;
import yesman.epicfight.client.mesh.VillagerMesh;
import yesman.epicfight.client.mesh.WitherMesh;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.renderer.FakeBlockRenderer;
import yesman.epicfight.client.renderer.FirstPersonRenderer;
import yesman.epicfight.client.renderer.VanillaFakeBlockRenderer;
import yesman.epicfight.client.renderer.patched.entity.PCreeperRenderer;
import yesman.epicfight.client.renderer.patched.entity.PCustomEntityRenderer;
import yesman.epicfight.client.renderer.patched.entity.PCustomHumanoidEntityRenderer;
import yesman.epicfight.client.renderer.patched.entity.PDrownedRenderer;
import yesman.epicfight.client.renderer.patched.entity.PEnderDragonRenderer;
import yesman.epicfight.client.renderer.patched.entity.PEndermanRenderer;
import yesman.epicfight.client.renderer.patched.entity.PHoglinRenderer;
import yesman.epicfight.client.renderer.patched.entity.PHumanoidRenderer;
import yesman.epicfight.client.renderer.patched.entity.PIllagerRenderer;
import yesman.epicfight.client.renderer.patched.entity.PIronGolemRenderer;
import yesman.epicfight.client.renderer.patched.entity.PPlayerRenderer;
import yesman.epicfight.client.renderer.patched.entity.PRavagerRenderer;
import yesman.epicfight.client.renderer.patched.entity.PSpiderRenderer;
import yesman.epicfight.client.renderer.patched.entity.PStrayRenderer;
import yesman.epicfight.client.renderer.patched.entity.PVexRenderer;
import yesman.epicfight.client.renderer.patched.entity.PVindicatorRenderer;
import yesman.epicfight.client.renderer.patched.entity.PWitchRenderer;
import yesman.epicfight.client.renderer.patched.entity.PWitherRenderer;
import yesman.epicfight.client.renderer.patched.entity.PWitherSkeletonMinionRenderer;
import yesman.epicfight.client.renderer.patched.entity.PZombieVillagerRenderer;
import yesman.epicfight.client.renderer.patched.entity.PatchedEntityRenderer;
import yesman.epicfight.client.renderer.patched.entity.PatchedLivingEntityRenderer;
import yesman.epicfight.client.renderer.patched.entity.PresetRenderer;
import yesman.epicfight.client.renderer.patched.entity.WitherGhostCloneRenderer;
import yesman.epicfight.client.renderer.patched.item.RenderFilledMap;
import yesman.epicfight.client.renderer.patched.item.RenderItemBase;
import yesman.epicfight.client.renderer.patched.item.RenderKatana;
import yesman.epicfight.client.renderer.patched.item.RenderShield;
import yesman.epicfight.client.renderer.patched.item.RenderTrident;
import yesman.epicfight.client.renderer.patched.item.RenderTwoHandedRangedWeapon;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.capabilities.entitypatch.boss.BossPatch;
import yesman.epicfight.world.capabilities.entitypatch.boss.WitherPatch;
import yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon.EnderDragonPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.CreeperPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.DrownedPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.EndermanPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.IronGolemPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.RavagerPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.SkeletonPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.SpiderPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.VexPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.WitchPatch;
import yesman.epicfight.world.capabilities.item.BowCapability;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CrossbowCapability;
import yesman.epicfight.world.capabilities.item.MapCapability;
import yesman.epicfight.world.capabilities.item.ShieldCapability;
import yesman.epicfight.world.capabilities.item.TridentCapability;
import yesman.epicfight.world.entity.EpicFightEntities;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

public class RenderEngine {
   public final BattleModeGui battleModeUI;
   public final VersionNotifier versionNotifier;
   public final Minecraft minecraft;
   private final BiMap<EntityType<?>, Function<EntityType<?>, PatchedEntityRenderer>> entityRendererProvider;
   private final Map<EntityType<?>, PatchedEntityRenderer> entityRendererCache;
   private final Map<Item, RenderItemBase> itemRendererMapByInstance;
   private final Map<Class<?>, RenderItemBase> itemRendererMapByClass;
   private final Map<UUID, BossPatch> bossEventOwners = Maps.newConcurrentMap();
   private final OverlayManager overlayManager;
   private FakeBlockRenderer fakeBlockRenderer;
   private FirstPersonRenderer firstPersonRenderer;
   private PHumanoidRenderer<?, ?, ?, ?, ?> basicHumanoidRenderer;
   private int modelInitTimer;

   public RenderEngine() {
      RenderEngine.Events.renderEngine = this;
      this.minecraft = Minecraft.m_91087_();
      this.battleModeUI = new BattleModeGui(this.minecraft);
      this.versionNotifier = new VersionNotifier(this.minecraft);
      this.entityRendererProvider = HashBiMap.create();
      this.entityRendererCache = Maps.newHashMap();
      this.itemRendererMapByInstance = Maps.newHashMap();
      this.itemRendererMapByClass = Maps.newHashMap();
      this.overlayManager = new OverlayManager();
      this.fakeBlockRenderer = new VanillaFakeBlockRenderer();
   }

   @Deprecated(forRemoval = true)
   public void initialize() {
   }

   public void reloadFakeBlockRenderer(FakeBlockRenderer fakeBlockRenderer) {
      this.fakeBlockRenderer = fakeBlockRenderer;
   }

   public void reloadEntityRenderers(Context context) {
      this.entityRendererProvider.clear();
      this.entityRendererProvider
         .put(
            EntityType.f_20558_,
            (Function<EntityType, PatchedLivingEntityRenderer<Creeper, CreeperPatch, CreeperModel<Creeper>, CreeperRenderer, CreeperMesh>>)entityType -> new PCreeperRenderer(
                  context, entityType
               )
               .initLayerLast(context, entityType)
         );
      this.entityRendererProvider
         .put(
            EntityType.f_20566_,
            (Function<EntityType, PatchedLivingEntityRenderer<EnderMan, EndermanPatch, EndermanModel<EnderMan>, EndermanRenderer, EndermanMesh>>)entityType -> new PEndermanRenderer(
                  context, entityType
               )
               .initLayerLast(context, entityType)
         );
      this.entityRendererProvider
         .put(
            EntityType.f_20501_,
            (Function<EntityType, PatchedEntityRenderer>)entityType -> new PHumanoidRenderer(Meshes.BIPED_OLD_TEX, context, entityType)
               .initLayerLast(context, entityType)
         );
      this.entityRendererProvider
         .put(
            EntityType.f_20530_,
            (Function<EntityType, PatchedLivingEntityRenderer<ZombieVillager, MobPatch<ZombieVillager>, ZombieVillagerModel<ZombieVillager>, ZombieVillagerRenderer, VillagerMesh>>)entityType -> new PZombieVillagerRenderer(
                  context, entityType
               )
               .initLayerLast(context, entityType)
         );
      this.entityRendererProvider
         .put(
            EntityType.f_20531_,
            (Function<EntityType, PatchedEntityRenderer>)entityType -> new PHumanoidRenderer(Meshes.PIGLIN, context, entityType)
               .initLayerLast(context, entityType)
         );
      this.entityRendererProvider
         .put(
            EntityType.f_20458_,
            (Function<EntityType, PatchedEntityRenderer>)entityType -> new PHumanoidRenderer(Meshes.BIPED_OLD_TEX, context, entityType)
               .initLayerLast(context, entityType)
         );
      this.entityRendererProvider
         .put(
            EntityType.f_20524_,
            (Function<EntityType, PatchedEntityRenderer>)entityType -> new PHumanoidRenderer(Meshes.SKELETON, context, entityType)
               .initLayerLast(context, entityType)
         );
      this.entityRendererProvider
         .put(
            EntityType.f_20497_,
            (Function<EntityType, PatchedEntityRenderer>)entityType -> new PHumanoidRenderer(Meshes.SKELETON, context, entityType)
               .initLayerLast(context, entityType)
         );
      this.entityRendererProvider
         .put(
            EntityType.f_20481_,
            (Function<EntityType, PatchedLivingEntityRenderer<PathfinderMob, SkeletonPatch<PathfinderMob>, HumanoidModel<PathfinderMob>, HumanoidMobRenderer<PathfinderMob, HumanoidModel<PathfinderMob>>, HumanoidMesh>>)entityType -> new PStrayRenderer(
                  context, entityType
               )
               .initLayerLast(context, entityType)
         );
      this.entityRendererProvider
         .put(
            EntityType.f_20532_,
            (Function<EntityType, PatchedLivingEntityRenderer<AbstractClientPlayer, AbstractClientPlayerPatch<AbstractClientPlayer>, PlayerModel<AbstractClientPlayer>, PlayerRenderer, HumanoidMesh>>)entityType -> new PPlayerRenderer(
                  context, entityType
               )
               .initLayerLast(context, entityType)
         );
      this.entityRendererProvider
         .put(
            EntityType.f_20479_,
            (Function<EntityType, PatchedLivingEntityRenderer<Spider, SpiderPatch<Spider>, SpiderModel<Spider>, SpiderRenderer<Spider>, SpiderMesh>>)entityType -> new PSpiderRenderer(
                  context, entityType
               )
               .initLayerLast(context, entityType)
         );
      this.entityRendererProvider
         .put(
            EntityType.f_20554_,
            (Function<EntityType, PatchedLivingEntityRenderer<Spider, SpiderPatch<Spider>, SpiderModel<Spider>, SpiderRenderer<Spider>, SpiderMesh>>)entityType -> new PSpiderRenderer(
                  context, entityType
               )
               .initLayerLast(context, entityType)
         );
      this.entityRendererProvider
         .put(
            EntityType.f_20460_,
            (Function<EntityType, PatchedLivingEntityRenderer<IronGolem, IronGolemPatch, IronGolemModel<IronGolem>, IronGolemRenderer, IronGolemMesh>>)entityType -> new PIronGolemRenderer(
                  context, entityType
               )
               .initLayerLast(context, entityType)
         );
      this.entityRendererProvider
         .put(
            EntityType.f_20493_,
            (Function<EntityType, PatchedLivingEntityRenderer<AbstractIllager, MobPatch<AbstractIllager>, IllagerModel<AbstractIllager>, IllagerRenderer<AbstractIllager>, HumanoidMesh>>)entityType -> new PVindicatorRenderer(
                  context, entityType
               )
               .initLayerLast(context, entityType)
         );
      this.entityRendererProvider
         .put(
            EntityType.f_20568_,
            (Function<EntityType, PatchedEntityRenderer>)entityType -> new PIllagerRenderer(context, entityType).initLayerLast(context, entityType)
         );
      this.entityRendererProvider
         .put(
            EntityType.f_20495_,
            (Function<EntityType, PatchedLivingEntityRenderer<Witch, WitchPatch, WitchModel<Witch>, WitchRenderer, VillagerMesh>>)entityType -> new PWitchRenderer(
                  context, entityType
               )
               .initLayerLast(context, entityType)
         );
      this.entityRendererProvider
         .put(
            EntityType.f_20562_,
            (Function<EntityType, PatchedLivingEntityRenderer<Drowned, DrownedPatch, DrownedModel<Drowned>, DrownedRenderer, HumanoidMesh>>)entityType -> new PDrownedRenderer(
                  context, entityType
               )
               .initLayerLast(context, entityType)
         );
      this.entityRendererProvider
         .put(
            EntityType.f_20513_,
            (Function<EntityType, PatchedEntityRenderer>)entityType -> new PIllagerRenderer(context, entityType).initLayerLast(context, entityType)
         );
      this.entityRendererProvider
         .put(
            EntityType.f_20518_,
            (Function<EntityType, PatchedLivingEntityRenderer<Ravager, RavagerPatch, RavagerModel, RavagerRenderer, RavagerMesh>>)entityType -> new PRavagerRenderer(
                  context, entityType
               )
               .initLayerLast(context, entityType)
         );
      this.entityRendererProvider
         .put(
            EntityType.f_20491_,
            (Function<EntityType, PatchedLivingEntityRenderer<Vex, VexPatch, VexModel, VexRenderer, VexMesh>>)entityType -> new PVexRenderer(
                  context, entityType
               )
               .initLayerLast(context, entityType)
         );
      this.entityRendererProvider
         .put(
            EntityType.f_20511_,
            (Function<EntityType, PatchedEntityRenderer>)entityType -> new PHumanoidRenderer(Meshes.PIGLIN, context, entityType)
               .initLayerLast(context, entityType)
         );
      this.entityRendererProvider
         .put(
            EntityType.f_20512_,
            (Function<EntityType, PatchedEntityRenderer>)entityType -> new PHumanoidRenderer(Meshes.PIGLIN, context, entityType)
               .initLayerLast(context, entityType)
         );
      this.entityRendererProvider
         .put(
            EntityType.f_20456_,
            (Function<EntityType, PatchedEntityRenderer>)entityType -> new PHoglinRenderer(context, entityType).initLayerLast(context, entityType)
         );
      this.entityRendererProvider
         .put(
            EntityType.f_20500_,
            (Function<EntityType, PatchedEntityRenderer>)entityType -> new PHoglinRenderer(context, entityType).initLayerLast(context, entityType)
         );
      this.entityRendererProvider.put(EntityType.f_20565_, (Function<EntityType, PatchedEntityRenderer>)entityType -> new PEnderDragonRenderer());
      this.entityRendererProvider
         .put(
            EntityType.f_20496_,
            (Function<EntityType, PatchedLivingEntityRenderer<WitherBoss, WitherPatch, WitherBossModel<WitherBoss>, WitherBossRenderer, WitherMesh>>)entityType -> new PWitherRenderer(
                  context, entityType
               )
               .initLayerLast(context, entityType)
         );
      this.entityRendererProvider
         .put(
            (EntityType)EpicFightEntities.WITHER_SKELETON_MINION.get(),
            (Function<EntityType, PatchedLivingEntityRenderer<PathfinderMob, HumanoidMobPatch<PathfinderMob>, HumanoidModel<PathfinderMob>, HumanoidMobRenderer<PathfinderMob, HumanoidModel<PathfinderMob>>, HumanoidMesh>>)entityType -> new PWitherSkeletonMinionRenderer(
                  context, entityType
               )
               .initLayerLast(context, entityType)
         );
      this.entityRendererProvider
         .put((EntityType)EpicFightEntities.WITHER_GHOST_CLONE.get(), (Function<EntityType, PatchedEntityRenderer>)entityType -> new WitherGhostCloneRenderer());
      this.firstPersonRenderer = new FirstPersonRenderer(context, EntityType.f_20532_);
      this.basicHumanoidRenderer = new PHumanoidRenderer<>(Meshes.BIPED, context, EntityType.f_20532_);
      ModLoader.get().postEvent(new PatchedRenderersEvent.Add(this.entityRendererProvider, context));
      this.resetRenderers();
   }

   public void reloadItemRenderers(Map<ResourceLocation, JsonElement> objects) {
      this.itemRendererMapByInstance.clear();
      this.itemRendererMapByClass.clear();
      Map<ResourceLocation, Function<JsonElement, RenderItemBase>> itemRenderers = Maps.newHashMap();
      itemRenderers.put(ResourceLocation.withDefaultNamespace("base"), RenderItemBase::new);
      itemRenderers.put(ResourceLocation.withDefaultNamespace("ranged"), RenderTwoHandedRangedWeapon::new);
      itemRenderers.put(ResourceLocation.withDefaultNamespace("map"), RenderFilledMap::new);
      itemRenderers.put(ResourceLocation.withDefaultNamespace("shield"), RenderShield::new);
      itemRenderers.put(ResourceLocation.withDefaultNamespace("trident"), RenderTrident::new);
      itemRenderers.put(EpicFightMod.identifier("uchigatana"), RenderKatana::new);
      ModLoader.get().postEvent(new PatchedRenderersEvent.RegisterItemRenderer(itemRenderers));

      for (Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
         ResourceLocation rl = entry.getKey();
         String pathString = rl.m_135815_();
         ResourceLocation registryName = ResourceLocation.fromNamespaceAndPath(rl.m_135827_(), pathString);
         if (!ForgeRegistries.ITEMS.containsKey(registryName)) {
            EpicFightMod.LOGGER.warn("Failed to load item skin: no item named " + registryName);
         } else {
            Item item = (Item)ForgeRegistries.ITEMS.getValue(registryName);
            Function<JsonElement, RenderItemBase> rendererProvider;
            if (entry.getValue().getAsJsonObject().has("renderer")) {
               ResourceLocation rendererName = ResourceLocation.parse(entry.getValue().getAsJsonObject().get("renderer").getAsString());
               if (itemRenderers.containsKey(rendererName)) {
                  rendererProvider = itemRenderers.get(rendererName);
               } else {
                  EpicFightMod.LOGGER.warn("No renderer named " + rendererName);
                  rendererProvider = RenderItemBase::new;
               }
            } else {
               rendererProvider = RenderItemBase::new;
            }

            RenderItemBase itemRenderer = rendererProvider.apply(entry.getValue());
            this.itemRendererMapByInstance.put(item, itemRenderer);
         }
      }

      RenderItemBase baseRenderer = new RenderItemBase(new JsonObject());
      RenderTwoHandedRangedWeapon bowRenderer = new RenderTwoHandedRangedWeapon(objects.get(ForgeRegistries.ITEMS.getKey(Items.f_42411_)).getAsJsonObject());
      RenderTwoHandedRangedWeapon crossbowRenderer = new RenderTwoHandedRangedWeapon(
         objects.get(ForgeRegistries.ITEMS.getKey(Items.f_42717_)).getAsJsonObject()
      );
      RenderTrident tridentRenderer = new RenderTrident(objects.get(ForgeRegistries.ITEMS.getKey(Items.f_42713_)).getAsJsonObject());
      RenderFilledMap mapRenderer = new RenderFilledMap(objects.get(ForgeRegistries.ITEMS.getKey(Items.f_42573_)).getAsJsonObject());
      RenderShield shieldRenderer = new RenderShield(objects.get(ForgeRegistries.ITEMS.getKey(Items.f_42740_)).getAsJsonObject());
      this.itemRendererMapByClass.put(BowItem.class, bowRenderer);
      this.itemRendererMapByClass.put(CrossbowItem.class, crossbowRenderer);
      this.itemRendererMapByClass.put(ShieldItem.class, baseRenderer);
      this.itemRendererMapByClass.put(TridentItem.class, tridentRenderer);
      this.itemRendererMapByClass.put(ShieldItem.class, shieldRenderer);
      this.itemRendererMapByClass.put(BowCapability.class, bowRenderer);
      this.itemRendererMapByClass.put(CrossbowCapability.class, crossbowRenderer);
      this.itemRendererMapByClass.put(TridentCapability.class, tridentRenderer);
      this.itemRendererMapByClass.put(MapCapability.class, mapRenderer);
      this.itemRendererMapByClass.put(ShieldCapability.class, shieldRenderer);
   }

   public void resetRenderers() {
      this.entityRendererCache.clear();

      for (Entry<EntityType<?>, Function<EntityType<?>, PatchedEntityRenderer>> entry : this.entityRendererProvider.entrySet()) {
         this.entityRendererCache.put(entry.getKey(), entry.getValue().apply(entry.getKey()));
      }

      ModLoader.get().postEvent(new PatchedRenderersEvent.Modify(this.entityRendererCache));
   }

   public void registerCustomEntityRenderer(EntityType<?> entityType, String rendererName, CompoundTag compound) {
      if (!StringUtil.m_14408_(rendererName)) {
         EntityRenderDispatcher erd = this.minecraft.m_91290_();
         Context context = new Context(
            erd,
            this.minecraft.m_91291_(),
            this.minecraft.m_91289_(),
            erd.m_234586_(),
            this.minecraft.m_91098_(),
            this.minecraft.m_167973_(),
            this.minecraft.f_91062_
         );
         if ("player".equals(rendererName)) {
            this.entityRendererCache.put(entityType, this.basicHumanoidRenderer);
         } else if ("epicfight:custom".equals(rendererName)) {
            if (compound.m_128471_("humanoid")) {
               this.entityRendererCache
                  .put(
                     entityType,
                     new PCustomHumanoidEntityRenderer<>(
                        Meshes.getOrCreate(
                           ResourceLocation.parse(compound.m_128461_("model")), jsonAssetLoader -> jsonAssetLoader.loadSkinnedMesh(HumanoidMesh::new)
                        ),
                        context,
                        entityType
                     )
                  );
            } else {
               this.entityRendererCache
                  .put(
                     entityType,
                     new PCustomEntityRenderer(
                        Meshes.getOrCreate(
                           ResourceLocation.parse(compound.m_128461_("model")), jsonAssetLoader -> jsonAssetLoader.loadSkinnedMesh(HumanoidMesh::new)
                        ),
                        context
                     )
                  );
            }
         } else {
            EntityType<?> presetEntityType = (EntityType<?>)ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(rendererName));
            if (!this.entityRendererProvider.containsKey(presetEntityType)) {
               throw new IllegalArgumentException("Datapack Mob Patch Crash: Invalid Renderer type " + rendererName);
            }

            PatchedEntityRenderer renderer = (PatchedEntityRenderer)((Function)this.entityRendererProvider.get(presetEntityType)).apply(entityType);
            if (!(this.minecraft.m_91290_().f_114362_.get(entityType) instanceof LivingEntityRenderer)
               && renderer instanceof PatchedLivingEntityRenderer patchedLivingEntityRenderer) {
               this.entityRendererCache
                  .put(
                     entityType,
                     new PresetRenderer(
                        context,
                        entityType,
                        (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>)context.m_174022_().f_114362_.get(presetEntityType),
                        patchedLivingEntityRenderer.getDefaultMesh()
                     )
                  );
            } else {
               this.entityRendererCache.put(entityType, (PatchedEntityRenderer)((Function)this.entityRendererProvider.get(presetEntityType)).apply(entityType));
            }
         }
      }
   }

   public RenderItemBase getItemRenderer(ItemStack itemstack) {
      RenderItemBase renderItem = this.itemRendererMapByInstance.get(itemstack.m_41720_());
      if (renderItem == null) {
         renderItem = this.findMatchingRendererByClass(itemstack.m_41720_().getClass());
         if (renderItem == null) {
            CapabilityItem itemCap = EpicFightCapabilities.getItemStackCapability(itemstack);
            renderItem = this.findMatchingRendererByClass(itemCap.getClass());
         }

         if (renderItem == null) {
            renderItem = this.itemRendererMapByInstance.get(Items.f_41852_);
         }

         this.itemRendererMapByInstance.put(itemstack.m_41720_(), renderItem);
      }

      return renderItem;
   }

   private RenderItemBase findMatchingRendererByClass(Class<?> clazz) {
      RenderItemBase renderer = null;

      while (clazz != null && renderer == null) {
         renderer = this.itemRendererMapByClass.get(clazz);
         clazz = clazz.getSuperclass();
      }

      return renderer;
   }

   public void renderEntityArmatureModel(
      LivingEntity livingEntity,
      LivingEntityPatch<?> entitypatch,
      EntityRenderer<? extends Entity> renderer,
      MultiBufferSource buffer,
      PoseStack matStack,
      int packedLight,
      float partialTicks
   ) {
      this.getEntityRenderer(livingEntity).render(livingEntity, entitypatch, renderer, buffer, matStack, packedLight, partialTicks);
   }

   public PatchedEntityRenderer getEntityRenderer(Entity entity) {
      return this.getEntityRenderer(entity.m_6095_());
   }

   public PatchedEntityRenderer getEntityRenderer(EntityType entityType) {
      return this.entityRendererCache.get(entityType);
   }

   public boolean hasRendererFor(Entity entity) {
      return this.entityRendererCache
            .computeIfAbsent(
               entity.m_6095_(),
               key -> this.entityRendererProvider.containsKey(key)
                  ? (PatchedEntityRenderer)((Function)this.entityRendererProvider.get(entity.m_6095_())).apply(entity.m_6095_())
                  : null
            )
         != null;
   }

   public Set<ResourceLocation> getRendererEntries() {
      Set<ResourceLocation> availableRendererEntities = this.entityRendererProvider
         .keySet()
         .stream()
         .map(entityType -> EntityType.m_20613_(entityType))
         .collect(Collectors.toSet());
      availableRendererEntities.add(EpicFightMod.identifier("custom"));
      return availableRendererEntities;
   }

   public void setModelInitializerTimer(int tick) {
      this.modelInitTimer = tick;
   }

   public OverlayManager getOverlayManager() {
      return this.overlayManager;
   }

   public FirstPersonRenderer getFirstPersonRenderer() {
      return this.firstPersonRenderer;
   }

   public void upSlideSkillUI() {
      this.battleModeUI.slideUp();
   }

   public void downSlideSkillUI() {
      this.battleModeUI.slideDown();
   }

   public boolean shouldRenderVanillaModel() {
      return ClientEngine.getInstance().isVanillaModelDebuggingMode() || this.modelInitTimer > 0;
   }

   public void addBossEventOwner(UUID uuid, BossPatch bosspatch) {
      this.bossEventOwners.put(uuid, bosspatch);
   }

   public void removeBossEventOwner(UUID uuid, BossPatch bosspatch) {
      this.bossEventOwners.remove(uuid);
   }

   public void initHUD() {
      this.battleModeUI.init();
      this.versionNotifier.init();
   }

   public void freeUnusedSources() {
      this.bossEventOwners.entrySet().removeIf(entry -> {
         Entity entity = entry.getValue().cast().getOriginal();
         return !entity.m_6084_() || entity.m_213877_();
      });
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> EpicFightRenderTypes.freeUnusedWorldRenderTypes());
      } else {
         EpicFightRenderTypes.freeUnusedWorldRenderTypes();
      }
   }

   public void clear() {
      EpicFightCameraAPI.getInstance().zoomOut(0);
      this.bossEventOwners.clear();
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> {
            this.resetRenderers();
            EpicFightRenderTypes.clearWorldRenderTypes();
         });
      } else {
         this.resetRenderers();
         EpicFightRenderTypes.clearWorldRenderTypes();
      }
   }

   public static boolean hitResultEquals(@Nullable HitResult hitResult, Type hitType) {
      return hitResult == null ? false : hitType.equals(hitResult.m_6662_());
   }

   public static boolean hitResultNotEquals(@Nullable HitResult hitResult, Type hitType) {
      return hitResult == null ? true : !hitType.equals(hitResult.m_6662_());
   }

   public static BlockHitResult asBlockHitResult(@Nullable HitResult hitResult) {
      if (hitResult == null) {
         return null;
      } else {
         return hitResult.m_6662_() == Type.BLOCK && hitResult instanceof BlockHitResult blockHitResult ? blockHitResult : null;
      }
   }

   public static EntityHitResult asEntityHitResult(@Nullable HitResult hitResult) {
      if (hitResult == null) {
         return null;
      } else {
         return hitResult.m_6662_() == Type.ENTITY && hitResult instanceof EntityHitResult entityHitResult ? entityHitResult : null;
      }
   }

   @Deprecated
   public void correctCamera(ComputeCameraAngles event, float partialTicks) {
      event.getCamera().m_90572_(1.0F, 1.0F);
   }

   @Deprecated
   public void setRangedWeaponThirdPerson(ComputeCameraAngles event, CameraType pov, double partialTicks) {
   }

   @Deprecated(forRemoval = true)
   public void zoomIn() {
      EpicFightCameraAPI.getInstance().zoomIn();
   }

   @Deprecated(forRemoval = true)
   public void zoomOut(int zoomOutTicks) {
      EpicFightCameraAPI.getInstance().zoomOut(zoomOutTicks);
   }

   @EventBusSubscriber(modid = "epicfight", value = Dist.CLIENT)
   public static class Events {
      static RenderEngine renderEngine;
      private static final Vector3f CAMERA_ROTATION_EULER = new Vector3f();
      private static final OpenMatrix4f PLAYER_ROTATION = new OpenMatrix4f();

      @SubscribeEvent
      public static void renderLivingEvent(Pre<? extends LivingEntity, ? extends EntityModel<? extends LivingEntity>> event) {
         LivingEntity livingentity = event.getEntity();
         if (livingentity.m_9236_() != null) {
            if (renderEngine.hasRendererFor(livingentity)) {
               LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(livingentity, LivingEntityPatch.class);
               float originalYRot = 0.0F;
               if ((event.getPartialTick() == 0.0F || event.getPartialTick() == 1.0F) && entitypatch instanceof LocalPlayerPatch localplayerpatch) {
                  if (entitypatch.overrideRender()) {
                     originalYRot = localplayerpatch.getModelYRot();
                     localplayerpatch.setModelYRotInGui(livingentity.m_146908_());
                     event.getPoseStack().m_85837_(0.0, 0.1, 0.0);
                     boolean compusteShaderSetting = ClientConfig.activateComputeShader;
                     ClientConfig.activateComputeShader = false;
                     renderEngine.renderEntityArmatureModel(
                        livingentity,
                        entitypatch,
                        event.getRenderer(),
                        event.getMultiBufferSource(),
                        event.getPoseStack(),
                        event.getPackedLight(),
                        event.getPartialTick()
                     );
                     ClientConfig.activateComputeShader = compusteShaderSetting;
                     event.setCanceled(true);
                     localplayerpatch.disableModelYRotInGui(originalYRot);
                  }

                  return;
               }

               if (entitypatch != null && entitypatch.overrideRender()) {
                  renderEngine.renderEntityArmatureModel(
                     livingentity,
                     entitypatch,
                     event.getRenderer(),
                     event.getMultiBufferSource(),
                     event.getPoseStack(),
                     event.getPackedLight(),
                     event.getPartialTick()
                  );
                  if (renderEngine.shouldRenderVanillaModel()) {
                     event.getPoseStack().m_252880_(renderEngine.modelInitTimer > 0 ? 10000.0F : 1.5F, 0.0F, 0.0F);
                     renderEngine.modelInitTimer--;
                  } else {
                     event.setCanceled(true);
                  }
               }
            }

            if (!renderEngine.minecraft.f_91066_.f_92062_ && !EpicFightGameRules.DISABLE_ENTITY_UI.getRuleValue(livingentity.m_9236_())) {
               EpicFightCapabilities.getUnparameterizedEntityPatch(renderEngine.minecraft.f_91074_, LocalPlayerPatch.class)
                  .ifPresent(
                     playerpatch -> {
                        LivingEntityPatch<?> entityPatch = EpicFightCapabilities.getEntityPatch(livingentity, LivingEntityPatch.class);

                        for (EntityUI entityIndicator : EntityUI.ENTITY_UI_LIST) {
                           if (entityIndicator.shouldDraw(livingentity, entityPatch, playerpatch, event.getPartialTick())) {
                              entityIndicator.draw(
                                 livingentity, entityPatch, playerpatch, event.getPoseStack(), event.getMultiBufferSource(), event.getPartialTick()
                              );
                           }
                        }
                     }
                  );
            }
         }
      }

      @SubscribeEvent
      public static void itemTooltip(ItemTooltipEvent event) {
         if (ClientConfig.showEpicFightAttributesInTooltip && event.getEntity() != null && event.getEntity().m_9236_().f_46443_) {
            EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), LocalPlayerPatch.class)
               .ifPresent(
                  playerpatch -> {
                     CapabilityItem cap = EpicFightCapabilities.getItemStackCapabilityOr(event.getItemStack(), null);
                     if (cap != null) {
                        if (InputManager.isActionPhysicallyActive(EpicFightInputAction.WEAPON_INNATE_SKILL_TOOLTIP)) {
                           Skill weaponInnateSkill = cap.getInnateSkill(playerpatch, event.getItemStack());
                           if (weaponInnateSkill != null) {
                              event.getToolTip().clear();

                              for (Component s : weaponInnateSkill.getTooltipOnItem(event.getItemStack(), cap, playerpatch)) {
                                 event.getToolTip().add(s);
                              }
                           }
                        } else {
                           List<Component> tooltip = event.getToolTip();
                           cap.modifyItemTooltip(event.getItemStack(), event.getToolTip(), playerpatch);

                           for (int i = 0; i < tooltip.size(); i++) {
                              Component textComp = tooltip.get(i);
                              if (!textComp.m_7360_().isEmpty()) {
                                 Component sibling = (Component)textComp.m_7360_().get(0);
                                 if (sibling instanceof MutableComponent mutableComponent
                                    && mutableComponent.m_214077_() instanceof TranslatableContents translatableContent
                                    && translatableContent.m_237523_().length > 1
                                    && translatableContent.m_237523_()[1] instanceof MutableComponent mutableComponent$2
                                    && mutableComponent$2.m_214077_() instanceof TranslatableContents translatableContent$2) {
                                    if (translatableContent$2.m_237508_().equals(Attributes.f_22283_.m_22087_())) {
                                       float weaponSpeed = (float)playerpatch.getWeaponAttribute(Attributes.f_22283_, event.getItemStack());
                                       tooltip.remove(i);
                                       tooltip.add(
                                          i,
                                          Component.m_237113_(String.format(" %.2f ", playerpatch.getModifiedAttackSpeed(cap, weaponSpeed)))
                                             .m_7220_(Component.m_237115_(Attributes.f_22283_.m_22087_()))
                                       );
                                    } else if (translatableContent$2.m_237508_().equals(Attributes.f_22281_.m_22087_())) {
                                       float weaponDamage = (float)playerpatch.getWeaponAttribute(Attributes.f_22281_, event.getItemStack());
                                       float damageBonus = EnchantmentHelper.m_44833_(event.getItemStack(), MobType.f_21640_);
                                       String damageFormat = ItemStack.f_41584_.format(playerpatch.getModifiedBaseDamage(weaponDamage) + damageBonus);
                                       tooltip.remove(i);
                                       tooltip.add(
                                          i,
                                          Component.m_237113_(String.format(" %s ", damageFormat))
                                             .m_7220_(Component.m_237115_(Attributes.f_22281_.m_22087_()))
                                             .m_130940_(ChatFormatting.DARK_GREEN)
                                       );
                                    }
                                 }
                              }
                           }

                           Skill weaponInnateSkill = cap.getInnateSkill(playerpatch, event.getItemStack());
                           if (weaponInnateSkill != null) {
                              event.getToolTip()
                                 .add(
                                    Component.m_237110_(
                                          "inventory.epicfight.guide_innate_tooltip",
                                          new Object[]{EpicFightKeyMappings.WEAPON_INNATE_SKILL_TOOLTIP.getKey().m_84875_()}
                                       )
                                       .m_130940_(ChatFormatting.DARK_GRAY)
                                 );
                           }
                        }
                     }
                  }
               );
         }
      }

      @SubscribeEvent
      public static void cameraSetupEvent(ComputeCameraAngles event) {
         EpicFightCapabilities.getUnparameterizedEntityPatch(renderEngine.minecraft.f_91074_, LocalPlayerPatch.class)
            .ifPresent(
               playerpatch -> {
                  if (ClientConfig.enablePovAction
                     && renderEngine.minecraft.f_91066_.m_92176_().m_90612_()
                     && playerpatch.isEpicFightMode()
                     && !playerpatch.getFirstPersonLayer().isOff()) {
                     float partialTick = (float)event.getPartialTick();
                     EpicFightCameraAPI cameraApi = EpicFightCameraAPI.getInstance();
                     if (cameraApi.isLerpingFpv()) {
                        float xRot = cameraApi.getLerpedFpvXRot(partialTick);
                        float yRot = cameraApi.getLerpedFpvYRot(partialTick);
                        renderEngine.minecraft.f_91075_.m_146926_(xRot);
                        renderEngine.minecraft.f_91075_.m_146922_(yRot);
                     } else {
                        AnimationSubFileReader.PovSettings.ViewLimit viewLimit = playerpatch.getPovSettings().viewLimit();
                        if (viewLimit != null) {
                           float clampedXRot = Mth.m_14036_(event.getPitch(), viewLimit.xRotMin(), viewLimit.xRotMax());
                           float bodyY = MathUtils.findNearestRotation(event.getYaw(), playerpatch.getYRot());
                           float clampedYRot = Mth.m_14036_(event.getYaw(), bodyY + viewLimit.yRotMin(), bodyY + viewLimit.yRotMax());
                           if (Float.compare(clampedXRot, event.getPitch()) != 0 || Float.compare(clampedYRot, event.getYaw()) != 0) {
                              cameraApi.fixFpvRotation(clampedXRot, playerpatch.getYRot(), 5);
                           }
                        }
                     }

                     if (playerpatch.hasCameraAnimation()) {
                        float time = Mth.m_14179_(
                           partialTick,
                           playerpatch.getFirstPersonLayer().animationPlayer.getPrevElapsedTime(),
                           playerpatch.getFirstPersonLayer().animationPlayer.getElapsedTime()
                        );
                        JointTransform cameraTransform;
                        if (!playerpatch.getFirstPersonLayer().animationPlayer.getAnimation().get().isLinkAnimation() && playerpatch.getPovSettings() != null) {
                           cameraTransform = playerpatch.getPovSettings().cameraTransform().getInterpolatedTransform(time);
                        } else {
                           cameraTransform = playerpatch.getFirstPersonLayer().getLinkCameraTransform().getInterpolatedTransform(time);
                        }

                        float xRot = playerpatch.getOriginal().m_146909_();
                        float yRot = playerpatch.getOriginal().m_146908_();
                        Vec3f translation = OpenMatrix4f.transform3v(
                           OpenMatrix4f.ofRotationDegree(yRot, Vec3f.Y_AXIS, PLAYER_ROTATION).rotate(xRot, Vec3f.X_AXIS), cameraTransform.translation(), null
                        );
                        Quaternionf rot = cameraTransform.rotation();
                        rot.getEulerAnglesXYZ(CAMERA_ROTATION_EULER);
                        CAMERA_ROTATION_EULER.x = (float)Math.toDegrees(CAMERA_ROTATION_EULER.x);
                        CAMERA_ROTATION_EULER.y = (float)Math.toDegrees(CAMERA_ROTATION_EULER.y);
                        CAMERA_ROTATION_EULER.z = (float)Math.toDegrees(CAMERA_ROTATION_EULER.z);
                        event.getCamera().m_90568_(translation.x, translation.y, translation.z);
                        event.setPitch(event.getPitch() + CAMERA_ROTATION_EULER.x);
                        event.setYaw(event.getYaw() + CAMERA_ROTATION_EULER.y);
                        event.setRoll(event.getRoll() + CAMERA_ROTATION_EULER.z);
                     }
                  }
               }
            );
      }

      @SubscribeEvent
      public static void renderGui(net.minecraftforge.client.event.RenderGuiEvent.Pre event) {
         Window window = Minecraft.m_91087_().m_91268_();
         LocalPlayerPatch playerpatch = ClientEngine.getInstance().getPlayerPatch();
         if (playerpatch != null) {
            playerpatch.getSkillCapability().listSkillContainers().forEach(skillContainer -> {
               if (skillContainer.getSkill() != null) {
                  skillContainer.getSkill().onScreen(playerpatch, window.m_85445_(), window.m_85446_());
               }
            });
            renderEngine.overlayManager.renderTick(window.m_85445_(), window.m_85446_());
            if (Minecraft.m_91404_() && !(Minecraft.m_91087_().f_91080_ instanceof UISetupScreen)) {
               renderEngine.battleModeUI.renderTick();
            }

            renderEngine.versionNotifier.render(event.getGuiGraphics(), true);
         }
      }

      @SubscribeEvent
      public static void renderGameOverlayPost(BossEventProgress event) {
         if (event.getBossEvent().m_18861_().getString().equals("Ender Dragon") && renderEngine.bossEventOwners.containsKey(event.getBossEvent().m_18860_())) {
            LivingEntityPatch<?> entitypatch = renderEngine.bossEventOwners.get(event.getBossEvent().m_18860_()).cast();
            float stunShield = entitypatch.getStunShield();
            if (stunShield > 0.0F) {
               float progression = stunShield / entitypatch.getMaxStunShield();
               int x = event.getX();
               int y = event.getY();
               RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
               event.getGuiGraphics().m_280411_(BossHealthOverlay.f_93697_, x, y + 6, 183, 2, 0.0F, 45.0F, 182, 6, 255, 255);
               event.getGuiGraphics()
                  .m_280411_(
                     BossHealthOverlay.f_93697_, x + (int)(183.0F * progression), y + 6, (int)(183.0F * (1.0F - progression)), 2, 0.0F, 39.0F, 182, 6, 255, 255
                  );
            }
         }
      }

      @SubscribeEvent(priority = EventPriority.HIGHEST)
      public static void renderHand(RenderHandEvent event) {
         LocalPlayerPatch playerpatch = ClientEngine.getInstance().getPlayerPatch();
         if (playerpatch != null) {
            boolean isBattleMode = playerpatch.isEpicFightMode();
            if (isBattleMode && ClientConfig.enableAnimatedFirstPersonModel) {
               RenderItemBase mainhandItemSkin = renderEngine.getItemRenderer(playerpatch.getOriginal().m_21205_());
               RenderItemBase offhandItemSkin = renderEngine.getItemRenderer(playerpatch.getOriginal().m_21206_());
               boolean useEpicFightModel = (mainhandItemSkin == null || !mainhandItemSkin.forceVanillaFirstPerson())
                  && (offhandItemSkin == null || !offhandItemSkin.forceVanillaFirstPerson());
               if (useEpicFightModel) {
                  if (event.getHand() == InteractionHand.MAIN_HAND) {
                     renderEngine.firstPersonRenderer
                        .render(
                           playerpatch.getOriginal(),
                           playerpatch,
                           (LivingEntityRenderer<LocalPlayer, PlayerModel<LocalPlayer>>)renderEngine.minecraft.m_91290_().m_114382_(playerpatch.getOriginal()),
                           event.getMultiBufferSource(),
                           event.getPoseStack(),
                           event.getPackedLight(),
                           event.getPartialTick()
                        );
                  }

                  event.setCanceled(true);
               }
            }
         }
      }

      @SubscribeEvent
      public static void renderWorldLast(RenderLevelStageEvent event) {
         if (event.getStage() == Stage.AFTER_TRIPWIRE_BLOCKS) {
            BlockHitResult blockHitResult = RenderEngine.asBlockHitResult(renderEngine.minecraft.f_91077_);
            if (ClientConfig.mineBlockGuideOption.showBlockHighlight() && blockHitResult != null) {
               EpicFightCapabilities.getUnparameterizedEntityPatch(renderEngine.minecraft.f_91074_, LocalPlayerPatch.class)
                  .ifPresent(
                     playerpatch -> {
                        if (!playerpatch.canPlayAttackAnimation() && playerpatch.isEpicFightMode()) {
                           renderEngine.fakeBlockRenderer
                              .render(
                                 event.getCamera(),
                                 event.getPoseStack(),
                                 renderEngine.minecraft.m_91269_().m_110104_(),
                                 renderEngine.minecraft.f_91073_,
                                 blockHitResult.m_82425_(),
                                 1.0F,
                                 1.0F,
                                 1.0F,
                                 0.4F
                              );
                        }
                     }
                  );
            }
         }
      }

      @SubscribeEvent
      public static void renderEnderDragonEvent(RenderEnderDragonEvent event) {
         EnderDragon livingentity = event.getEntity();
         if (renderEngine.hasRendererFor(livingentity)) {
            EpicFightCapabilities.getUnparameterizedEntityPatch(livingentity, EnderDragonPatch.class)
               .ifPresent(
                  enderdragonpatch -> {
                     event.setCanceled(true);
                     renderEngine.getEntityRenderer(livingentity)
                        .render(
                           livingentity,
                           enderdragonpatch,
                           event.getRenderer(),
                           event.getBuffers(),
                           event.getPoseStack(),
                           event.getLight(),
                           event.getPartialRenderTick()
                        );
                  }
               );
         }
      }

      @SubscribeEvent
      public static void renderBlockHighlight(Block event) {
         EpicFightCapabilities.getUnparameterizedEntityPatch(renderEngine.minecraft.f_91074_, LocalPlayerPatch.class).ifPresent(playerpatch -> {
            if (playerpatch.canPlayAttackAnimation()) {
               event.setCanceled(true);
            }
         });
      }

      @SubscribeEvent
      public static void renderTickEvent(RenderTickEvent event) {
         if (event.phase == Phase.START) {
            EntityUI.HEALTH_BAR.reset();
         } else {
            EntityUI.HEALTH_BAR.remove();
         }
      }

      @SubscribeEvent
      public static void clientTickEvent(ClientTickEvent event) {
         if (event.phase == Phase.START) {
            renderEngine.freeUnusedSources();
            EpicFightCameraAPI.getInstance().preClientTick();
         } else {
            EpicFightCameraAPI.getInstance().postClientTick();
         }
      }

      @SubscribeEvent
      public static void rightClickBlockEvent(RightClickBlock event) {
         if (event.getSide() == LogicalSide.CLIENT) {
            EpicFightCameraAPI cameraApi = EpicFightCameraAPI.getInstance();
            if (cameraApi.isTPSMode()) {
               EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), LocalPlayerPatch.class).ifPresent(playerpatch -> {
                  Vec3 toHit = event.getHitVec().m_82450_().m_82546_(playerpatch.getOriginal().m_146892_());
                  float xRot = (float)MathUtils.getXRotOfVector(toHit);
                  float yRot = (float)MathUtils.getYRotOfVector(toHit);
                  playerpatch.getOriginal().m_146926_(xRot);
                  playerpatch.getOriginal().m_146922_(yRot);
                  playerpatch.getOriginal().m_5616_(yRot);
               });
            }
         }
      }

      @SubscribeEvent
      public static void levelTickEvent(LevelTickEvent event) {
         if (event.level.m_5776_() && event.phase == Phase.END) {
            EntityUI.HEALTH_BAR.tick();
         }
      }
   }
}
