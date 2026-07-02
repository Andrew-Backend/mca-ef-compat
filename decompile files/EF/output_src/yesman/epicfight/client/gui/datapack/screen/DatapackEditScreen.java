package yesman.epicfight.client.gui.datapack.screen;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.netty.util.internal.StringUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.Pack.Position;
import net.minecraft.server.packs.repository.Pack.ResourcesSupplier;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.HitAnimation;
import yesman.epicfight.api.animation.types.LongHitAnimation;
import yesman.epicfight.api.animation.types.MainFrameAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.animation.types.datapack.DatapackAnimation;
import yesman.epicfight.api.animation.types.datapack.EditorAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.asset.JsonAssetLoader;
import yesman.epicfight.api.asset.SelfAccessor;
import yesman.epicfight.api.client.animation.AnimationSubFileReader;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.collider.MultiOBBCollider;
import yesman.epicfight.api.collider.OBBCollider;
import yesman.epicfight.api.data.reloader.ItemCapabilityReloadListener;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.InstantiateInvoker;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.gui.datapack.widgets.CheckBox;
import yesman.epicfight.client.gui.datapack.widgets.ColorPreviewWidget;
import yesman.epicfight.client.gui.datapack.widgets.ComboBox;
import yesman.epicfight.client.gui.datapack.widgets.Grid;
import yesman.epicfight.client.gui.datapack.widgets.InputComponentList;
import yesman.epicfight.client.gui.datapack.widgets.ModelPreviewer;
import yesman.epicfight.client.gui.datapack.widgets.PopupBox;
import yesman.epicfight.client.gui.datapack.widgets.ResizableComponent;
import yesman.epicfight.client.gui.datapack.widgets.ResizableEditBox;
import yesman.epicfight.client.gui.datapack.widgets.Static;
import yesman.epicfight.client.gui.datapack.widgets.SubScreenOpenButton;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCapability;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.capabilities.item.WeaponTypeReloadListener;
import yesman.epicfight.world.capabilities.provider.EntityPatchProvider;
import yesman.epicfight.world.capabilities.provider.ExtraEntryProvider;
import yesman.epicfight.world.damagesource.StunType;

public class DatapackEditScreen extends Screen implements ExtraEntryProvider {
   public static final Component GUI_EXPORT = Component.m_237115_("gui.epicfight.export");
   private static DatapackEditScreen workingPackScreen;
   private GridLayout bottomButtons;
   private TabNavigationBar tabNavigationBar;
   private final Screen parentScreen;
   private final DatapackEditScreen.WeaponTypeTab weaponTypeTab;
   private final DatapackEditScreen.ItemCapabilityTab itemCapabilityTab;
   private final DatapackEditScreen.MobCapabilityTab mobCapabilityTab;
   private final Map<ResourceLocation, PackEntry<EditorAnimation, DatapackAnimation<? extends StaticAnimation>>> userAnimations = Maps.newLinkedHashMap();
   private final Map<ResourceLocation, AssetAccessor<? extends SkinnedMesh>> userMeshes = Maps.newLinkedHashMap();
   private final Map<ResourceLocation, AssetAccessor<? extends Armature>> userArmatures = Maps.newLinkedHashMap();
   private final Map<ResourceLocation, Function<Item, CapabilityItem.Builder>> userWeaponTypes = Maps.newLinkedHashMap();
   private final TabManager tabManager = new TabManager(x$0 -> {
      AbstractWidget var10000 = (AbstractWidget)this.m_142416_(x$0);
   }, p_267853_ -> this.m_169411_(p_267853_)) {
      public void m_276088_(Tab tab, boolean playSound) {
         if (this.m_267695_() instanceof DatapackEditScreen.DatapackTab<?> datapackTab) {
            DatapackEditScreen.this.m_169411_(datapackTab.packListGrid);
            DatapackEditScreen.this.m_169411_(datapackTab.inputComponentsList);
         }

         super.m_276088_(tab, playSound);
         if (tab instanceof DatapackEditScreen.DatapackTab<?> datapackTab) {
            DatapackEditScreen.this.m_142416_(datapackTab.packListGrid);
            DatapackEditScreen.this.m_142416_(datapackTab.inputComponentsList);
         }
      }
   };

   public static DatapackEditScreen getCurrentScreen() {
      return workingPackScreen;
   }

   public static boolean hasEditingScreen() {
      return workingPackScreen != null;
   }

   public static AssetAccessor<? extends StaticAnimation> animationByKey(String path) {
      return workingPackScreen != null ? workingPackScreen.getExtraOrBuiltInAnimation(path) : AnimationManager.byKey(ResourceLocation.parse(path));
   }

   public static AssetAccessor<? extends SkinnedMesh> getMesh(String path) {
      return workingPackScreen != null ? workingPackScreen.getExtraOrBuiltInMesh(path) : Meshes.get(ResourceLocation.parse(path));
   }

   public static AssetAccessor<? extends Armature> getArmature(String path) {
      return workingPackScreen != null ? workingPackScreen.getExtraOrBuiltInArmature(path) : Armatures.get(ResourceLocation.parse(path));
   }

   public static Function<Item, CapabilityItem.Builder> getWeaponType(String typeName) {
      return workingPackScreen != null ? workingPackScreen.getExtraOrBuiltInWeaponType(typeName) : WeaponTypeReloadListener.get(typeName);
   }

   @Override
   public <T extends StaticAnimation> AnimationManager.AnimationAccessor<T> getExtraOrBuiltInAnimation(String path) throws ClassCastException {
      ResourceLocation rl = ResourceLocation.parse(path);
      return this.userAnimations.containsKey(rl) ? this.userAnimations.get(rl).getValue() : AnimationManager.byKey(rl);
   }

   @Override
   public AssetAccessor<? extends SkinnedMesh> getExtraOrBuiltInMesh(String path) {
      ResourceLocation rl = ResourceLocation.parse(path);
      return workingPackScreen != null && workingPackScreen.userMeshes.containsKey(rl) ? workingPackScreen.userMeshes.get(rl) : Meshes.get(rl);
   }

   @Override
   public AssetAccessor<? extends Armature> getExtraOrBuiltInArmature(String path) {
      ResourceLocation rl = ResourceLocation.parse(path);
      return workingPackScreen != null && workingPackScreen.userArmatures.containsKey(rl) ? workingPackScreen.userArmatures.get(rl) : Armatures.get(rl);
   }

   @Override
   public Function<Item, CapabilityItem.Builder> getExtraOrBuiltInWeaponType(String typeName) {
      ResourceLocation typeId = ResourceLocation.parse(typeName);
      return this.userWeaponTypes.containsKey(typeId) ? this.userWeaponTypes.get(typeId) : WeaponTypeReloadListener.get(typeName);
   }

   public static Set<Entry<ResourceLocation, Function<Item, CapabilityItem.Builder>>> getSerializableWeaponTypes() {
      return workingPackScreen.weaponTypeTab.packList.stream().reduce(Sets.newHashSet(), (set, entry) -> {
         try {
            WeaponCapability.Builder builder = WeaponTypeReloadListener.deserializeWeaponCapabilityBuilder(entry.getKey(), entry.getValue(), workingPackScreen);
            Function<Item, CapabilityItem.Builder> provider = itemstack -> builder;
            if (!workingPackScreen.userWeaponTypes.containsKey(entry.getKey())) {
               workingPackScreen.userWeaponTypes.put(entry.getKey(), provider);
            }

            set.add(PackEntry.ofValue(entry.getKey(), provider));
            return set;
         } catch (Exception e) {
            EpicFightMod.LOGGER.error("Can not deserialize weapon type " + entry.getKey());
            return set;
         }
      }, (set1, set2) -> {
         set1.addAll(set2);
         return set1;
      });
   }

   public DatapackEditScreen(Screen parentScreen) {
      super(Component.m_237115_(EpicFightMod.format("gui.%s.datapack_edit")));
      this.parentScreen = parentScreen;
      this.f_96541_ = parentScreen.getMinecraft();
      this.f_96547_ = parentScreen.getMinecraft().f_91062_;
      this.weaponTypeTab = new DatapackEditScreen.WeaponTypeTab();
      this.itemCapabilityTab = new DatapackEditScreen.ItemCapabilityTab();
      this.mobCapabilityTab = new DatapackEditScreen.MobCapabilityTab();
      if (workingPackScreen == null) {
         workingPackScreen = this;
      }
   }

   public boolean importDataPack(Path path) {
      ResourcesSupplier pack$resourcessupplier = FolderRepositorySource.m_254985_(path, false);
      if (pack$resourcessupplier != null) {
         String s = path.getFileName().toString();
         Pack pack = Pack.m_245429_("file/" + s, Component.m_237113_(s), false, pack$resourcessupplier, PackType.SERVER_DATA, Position.TOP, PackSource.f_10529_);
         PackResources packResources = pack.m_10445_();
         this.importUserData(packResources);
         this.weaponTypeTab.importEntries(packResources);
         this.itemCapabilityTab.importEntries(packResources);
         this.mobCapabilityTab.importEntries(packResources);
         packResources.close();
         return true;
      } else {
         this.f_96541_.m_91152_(new MessageScreen("Invalid datapack", "", this, button2 -> this.f_96541_.m_91152_(this), 160, 60));
         return false;
      }
   }

   public boolean exportDataPack(String packName) {
      ZipOutputStream out = null;
      File zipFile = null;

      try {
         File resourcePackDirectory = Minecraft.m_91087_().m_245161_().toFile();
         zipFile = new File(resourcePackDirectory, packName + ".zip");

         for (int duplicateCount = 1; zipFile.exists(); duplicateCount++) {
            zipFile = new File(resourcePackDirectory, packName + String.format(" (%d).zip", duplicateCount));
         }

         out = new ZipOutputStream(new FileOutputStream(zipFile));
         this.weaponTypeTab.exportEntries(out);
         this.itemCapabilityTab.exportEntries(out);
         this.mobCapabilityTab.exportEntries(out);
         this.exportUserData(out);
         ZipEntry zipEntry = new ZipEntry("pack.mcmeta");
         Gson gson = new GsonBuilder().setPrettyPrinting().create();
         JsonObject root = new JsonObject();
         JsonObject pack = new JsonObject();
         int datapackVersion = SharedConstants.m_183709_().m_264084_(PackType.SERVER_DATA);
         int resourcepackVersion = SharedConstants.m_183709_().m_264084_(PackType.CLIENT_RESOURCES);
         if (datapackVersion != resourcepackVersion) {
            EpicFightMod.LOGGER.warn("Pack version is not matching in " + SharedConstants.m_183709_().m_132492_());
         }

         pack.addProperty("description", packName);
         pack.addProperty("pack_format", datapackVersion);
         root.add("pack", pack);
         out.putNextEntry(zipEntry);
         out.write(gson.toJson(root).getBytes());
         out.closeEntry();
         out.close();
         Util.m_137581_().m_137644_(resourcePackDirectory);
         return true;
      } catch (Exception e) {
         this.f_96541_
            .m_91152_(
               new MessageScreen("Failed to export datapack", e.getMessage(), this, button2 -> this.f_96541_.m_91152_(this), 400, 110).autoCalculateHeight()
            );
         e.printStackTrace();

         try {
            if (out != null) {
               out.close();
            }
         } catch (IOException e1) {
            e1.printStackTrace();
         }

         zipFile.delete();
         return false;
      }
   }

   public void m_7400_(List<Path> filePath) {
      boolean isJsonFiles = true;

      for (Path path : filePath) {
         isJsonFiles &= path.getFileName().toString().indexOf(".json") > -1;
      }

      if (isJsonFiles) {
         this.f_96541_
            .m_91152_(
               new MessageScreen<>(
                  "",
                  "Enter the mod id",
                  this,
                  modid -> {
                     StringBuilder sb = new StringBuilder();
                     boolean hasException = false;

                     for (Path pathx : filePath) {
                        try {
                           InputStream stream = Files.newInputStream(pathx);
                           ResourceLocation registryName = ResourceLocation.fromNamespaceAndPath(modid, pathx.getFileName().toString().replaceAll(".json", ""));
                           if (this.tabManager.m_267695_() == this.weaponTypeTab) {
                              this.weaponTypeTab.importJson(registryName, stream);
                           } else if (this.tabManager.m_267695_() == this.itemCapabilityTab) {
                              this.itemCapabilityTab.importJson(registryName, null, stream);
                           } else if (this.tabManager.m_267695_() == this.mobCapabilityTab) {
                              this.mobCapabilityTab.importJson(registryName, stream);
                           }
                        } catch (Exception e) {
                           hasException = true;
                           sb.append(String.format("%s : %s\n", pathx.getFileName().toString(), e.getMessage()));
                           e.printStackTrace();
                        }
                     }

                     if (hasException) {
                        this.f_96541_
                           .m_91152_(
                              new MessageScreen("Failed to import these items", sb.toString(), this, button2 -> this.f_96541_.m_91152_(this), 300, 70)
                                 .autoCalculateHeight()
                           );
                     } else {
                        this.f_96541_.m_91152_(this);
                     }
                  },
                  button -> this.f_96541_.m_91152_(this),
                  new ResizableEditBox(this.f_96541_.f_91062_, 0, 0, 0, 16, Component.m_237113_("datapack_edit.input"), null, null),
                  120,
                  80
               )
            );
      } else {
         if (filePath.size() > 1) {
            this.f_96541_.m_91152_(new MessageScreen("", "Please drop only one file", this, button -> this.f_96541_.m_91152_(this), 160, 50));
         }

         Path path = filePath.get(0);
         if (this.weaponTypeTab.packList.size() <= 0 && this.itemCapabilityTab.packList.size() <= 0 && this.mobCapabilityTab.packList.size() <= 0) {
            workingPackScreen = this;
            this.importDataPack(path);
         } else {
            this.f_96541_
               .m_91152_(
                  new MessageScreen("", "The current entries will be removed if you import the new data pack. Do you want to proceed?", this, button -> {
                     DatapackEditScreen newScreen = new DatapackEditScreen(this.parentScreen);
                     workingPackScreen = newScreen;
                     if (newScreen.importDataPack(path)) {
                        this.f_96541_.m_91152_(newScreen);
                     }
                  }, button -> this.f_96541_.m_91152_(this), 200, 90)
               );
         }
      }
   }

   protected void m_7856_() {
      Minecraft.m_91087_().m_91385_().enableStencil();
      this.tabNavigationBar = TabNavigationBar.m_267630_(this.tabManager, this.f_96543_)
         .m_267824_(new Tab[]{this.weaponTypeTab, this.itemCapabilityTab, this.mobCapabilityTab})
         .m_267625_();
      this.tabNavigationBar.m_276089_(0, false);
      this.m_142416_(this.tabNavigationBar);
      this.bottomButtons = new GridLayout().m_267749_(10);
      RowHelper gridlayout$rowhelper = this.bottomButtons.m_264606_(2);
      gridlayout$rowhelper.m_264139_(
         Button.m_253074_(
               GUI_EXPORT,
               button -> {
                  try {
                     this.weaponTypeTab.validateBeforeExport();
                     this.itemCapabilityTab.validateBeforeExport();
                     this.mobCapabilityTab.validateBeforeExport();
                  } catch (Exception e) {
                     e.printStackTrace();
                     this.f_96541_
                        .m_91152_(
                           new MessageScreen(
                                 "",
                                 e.getMessage(),
                                 this,
                                 button2 -> this.f_96541_.m_91152_(this.createExportScreen()),
                                 button3 -> this.f_96541_.m_91152_(this),
                                 400,
                                 110
                              )
                              .autoCalculateHeight()
                              .withOkTitle(Component.m_237115_("datapack_edit.export_with_exceptions"))
                        );
                     return;
                  }

                  this.f_96541_.m_91152_(this.createExportScreen());
               }
            )
            .m_253136_()
      );
      gridlayout$rowhelper.m_264139_(Button.m_253074_(CommonComponents.f_130656_, button -> this.m_7379_()).m_253136_());
      this.bottomButtons.m_264134_(button -> {
         button.m_267708_(1);
         this.m_142416_(button);
      });
      this.m_267719_();
      if (!this.f_267454_ && workingPackScreen != null && workingPackScreen != this) {
         this.f_267454_ = true;
         this.f_96541_
            .m_91152_(new MessageScreen("", "Would you like to load the previous pack?", this, button -> this.f_96541_.m_91152_(workingPackScreen), button -> {
               if (workingPackScreen != null) {
                  workingPackScreen.destroyUserData();
               }

               this.f_96541_.m_91152_(this);
               workingPackScreen = this;
            }, 180, 70));
      }
   }

   public void m_267719_() {
      if (this.tabNavigationBar != null && this.bottomButtons != null) {
         this.tabNavigationBar.m_267604_(this.f_96543_);
         this.tabNavigationBar.m_267766_();
         this.bottomButtons.m_264036_();
         FrameLayout.m_264159_(this.bottomButtons, 0, this.f_96544_ - 36, this.f_96543_, 36);
         int i = this.tabNavigationBar.m_264198_().m_274349_();
         ScreenRectangle screenrectangle = new ScreenRectangle(0, i, this.f_96543_, this.bottomButtons.m_252907_() - i);
         this.tabManager.m_267817_(screenrectangle);
      }
   }

   public void m_86600_() {
      this.tabManager.m_267695_().m_267681_();
   }

   public void m_7379_() {
      if (this.weaponTypeTab.packList.size() <= 0
         && this.itemCapabilityTab.packList.size() <= 0
         && this.mobCapabilityTab.packList.size() <= 0
         && this.userMeshes.isEmpty()
         && this.userArmatures.isEmpty()
         && this.userAnimations.isEmpty()) {
         workingPackScreen = null;
      } else {
         workingPackScreen = this;
      }

      this.weaponTypeTab.modelPreviewer.onDestroy();
      this.itemCapabilityTab.modelPreviewer.onDestroy();
      this.f_96541_.m_91152_(this.parentScreen);
   }

   public boolean m_6375_(double x, double y, int button) {
      return this.m_7222_() != null && this.m_7222_().m_6375_(x, y, button) ? true : super.m_6375_(x, y, button);
   }

   public boolean m_7933_(int keycode, int p_100876_, int p_100877_) {
      if (this.tabNavigationBar.m_269419_(keycode)) {
         return true;
      } else {
         return super.m_7933_(keycode, p_100876_, p_100877_) ? true : keycode == 257 || keycode == 335;
      }
   }

   public boolean m_7979_(double mouseX, double mouseY, int button, double dx, double dy) {
      return this.m_94729_(mouseX, mouseY).filter(listener -> listener.m_7979_(mouseX, mouseY, button, dx, dy)).isPresent();
   }

   public void m_7522_(@Nullable GuiEventListener target) {
      if (this.m_7222_() != target) {
         super.m_7522_(target);
      }
   }

   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.m_280273_(guiGraphics);
      guiGraphics.m_280163_(CreateWorldScreen.f_273875_, 0, Mth.m_144941_(this.f_96544_ - 36 - 2, 2), 0.0F, 0.0F, this.f_96543_, 2, 32, 2);
      super.m_88315_(guiGraphics, mouseX, mouseY, partialTick);
   }

   private <T> MessageScreen<T> createExportScreen() {
      return new MessageScreen<T>("", "Enter the pack title", this, null, 180, 70) {
         @Override
         protected void m_7856_() {
            this.parentScreen.m_6575_(this.f_96541_, this.f_96543_, this.f_96544_);
            int height = this.messageBoxHeight / 2;
            EditBox titleEditBox = new EditBox(
               this.f_96547_, this.f_96543_ / 2 - 72, this.f_96544_ / 2 - 6, 144, 16, Component.m_237113_("pack_title_input_box")
            );
            this.m_142416_(titleEditBox);
            this.m_142416_(Button.m_253074_(CommonComponents.f_286989_, button$2 -> {
               if (DatapackEditScreen.this.exportDataPack(titleEditBox.m_94155_())) {
                  DatapackEditScreen.this.f_96541_.m_91152_(DatapackEditScreen.this);
               }
            }).m_252987_(this.f_96543_ / 2 - 56, this.f_96544_ / 2 + height - 20, 55, 16).m_253136_());
            this.m_142416_(
               Button.m_253074_(CommonComponents.f_130656_, button$2 -> this.f_96541_.m_91152_(DatapackEditScreen.this))
                  .m_252987_(this.f_96543_ / 2 + 1, this.f_96544_ / 2 + height - 20, 55, 16)
                  .m_253136_()
            );
         }
      };
   }

   private void importUserData(PackResources packResources) {
      packResources.m_5698_(PackType.CLIENT_RESOURCES)
         .stream()
         .distinct()
         .forEach(
            namespace -> {
               packResources.m_8031_(
                  PackType.CLIENT_RESOURCES,
                  namespace,
                  "animmodels/entity",
                  (resourceLocation, stream) -> {
                     try {
                        JsonReader jsonReader = new JsonReader(new InputStreamReader((InputStream)stream.m_247737_(), StandardCharsets.UTF_8));
                        jsonReader.setLenient(true);
                        JsonObject jsonObject = Streams.parse(jsonReader).getAsJsonObject();
                        ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(
                           resourceLocation.m_135827_(), resourceLocation.m_135815_().replaceAll("animmodels/", "").replaceAll(".json", "")
                        );
                        JsonAssetLoader modelLoader = new JsonAssetLoader(jsonObject, resourceLocation);
                        SkinnedMesh mesh = null;
                        Armature armature = null;

                        try {
                           mesh = modelLoader.loadSkinnedMesh(SkinnedMesh::new);
                           armature = modelLoader.loadArmature(Armature::new);
                        } catch (Exception e) {
                           e.printStackTrace();
                        } finally {
                           ;
                        }

                        if (mesh != null) {
                           this.userMeshes.put(rl, SelfAccessor.create(rl, mesh));
                        }

                        if (armature != null) {
                           this.userArmatures.put(rl, SelfAccessor.create(rl, armature));
                        }
                     } catch (Exception e) {
                        EpicFightMod.LOGGER.error("Failed to read model " + resourceLocation);
                        e.printStackTrace();
                     }
                  }
               );
               packResources.m_8031_(
                  PackType.CLIENT_RESOURCES,
                  namespace,
                  "animmodels/animations",
                  (resourceLocation, stream) -> {
                     if (!resourceLocation.m_135815_().contains("/data/")) {
                        try {
                           JsonReader jsonReader = new JsonReader(new InputStreamReader((InputStream)stream.m_247737_(), StandardCharsets.UTF_8));
                           jsonReader.setLenient(true);
                           JsonObject jsonObject = Streams.parse(jsonReader).getAsJsonObject();
                           ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(
                              resourceLocation.m_135827_(), resourceLocation.m_135815_().replaceAll("animmodels/animations/", "").replaceAll(".json", "")
                           );
                           ResourceLocation datapath = AnimationManager.getSubAnimationFileLocation(
                              resourceLocation, AnimationSubFileReader.SUBFILE_CLIENT_PROPERTY
                           );
                           IoSupplier<InputStream> streamSupplier = packResources.m_214146_(PackType.CLIENT_RESOURCES, datapath);
                           JsonElement constructorElement = jsonObject.getAsJsonObject().get("constructor");
                           if (constructorElement == null) {
                              throw new IllegalStateException(String.format("No constructor information has provided in User animation %s", rl));
                           }

                           JsonObject constructorObject = constructorElement.getAsJsonObject();
                           String invocationCommand = constructorObject.get("invocation_command").getAsString();
                           if (invocationCommand.lastIndexOf(35) == -1) {
                              throw new IllegalStateException(
                                 String.format("Invocation command exception: Missing separator %s in animation %s", invocationCommand, rl)
                              );
                           }

                           String className = invocationCommand.substring(invocationCommand.lastIndexOf(35) + 1);
                           Class<? extends DatapackAnimation<? extends StaticAnimation>> animationClass = EditorAnimation.switchType(
                              (Class<? extends StaticAnimation>)Class.forName(className)
                           );
                           DatapackAnimation<? extends StaticAnimation> animation = (DatapackAnimation<? extends StaticAnimation>)InstantiateInvoker.invoke(
                                 invocationCommand, animationClass
                              )
                              .getResult();
                           if (!animation.registryName().equals(rl)) {
                              throw new IllegalArgumentException(
                                 "Initializer registry name " + animation.registryName() + " is not matching with file path " + rl
                              );
                           }

                           if (streamSupplier != null) {
                              AnimationSubFileReader.SUBFILE_CLIENT_PROPERTY.apply((InputStream)streamSupplier.m_247737_(), animation.get());
                           }

                           JsonAssetLoader modelLoader = new JsonAssetLoader(jsonObject, resourceLocation);
                           animation.setAnimationClip(modelLoader.loadAnimationClip(animation.get().getArmature().get()));
                           this.userAnimations
                              .put(
                                 animation.registryName(),
                                 PackEntry.ofValue(animation.readAnimationFromJson(modelLoader.getRootJson().get("animation").getAsJsonArray()), animation)
                              );
                        } catch (Exception e) {
                           EpicFightMod.LOGGER.error("Failed to read animation " + resourceLocation);
                           e.printStackTrace();
                        }
                     }
                  }
               );
            }
         );
   }

   private void exportUserData(ZipOutputStream out) throws Exception {
      Map<ResourceLocation, JsonObject> models = Maps.newHashMap();

      for (Entry<ResourceLocation, AssetAccessor<? extends SkinnedMesh>> entry : this.userMeshes.entrySet()) {
         models.put(entry.getKey(), entry.getValue().get().toJsonObject());
      }

      for (Entry<ResourceLocation, AssetAccessor<? extends Armature>> entry : this.userArmatures.entrySet()) {
         String exportPath = String.format("data/%s/animmodels/%s.json", entry.getKey().m_135827_(), entry.getKey().m_135815_());
         ZipEntry zipEntry = new ZipEntry(exportPath);
         Gson gson = new GsonBuilder().setPrettyPrinting().create();
         JsonObject armatureJson = entry.getValue().get().toJsonObject();
         out.putNextEntry(zipEntry);
         out.write(gson.toJson(armatureJson).getBytes());
         out.closeEntry();
         models.computeIfAbsent(entry.getKey(), k -> armatureJson);
         models.computeIfPresent(entry.getKey(), (k, oldVal) -> {
            oldVal.add("armature", armatureJson.get("armature"));
            return (JsonObject)oldVal;
         });
      }

      for (Entry<ResourceLocation, JsonObject> entry : models.entrySet()) {
         String exportPath = String.format("assets/%s/animmodels/%s.json", entry.getKey().m_135827_(), entry.getKey().m_135815_());
         ZipEntry zipEntry = new ZipEntry(exportPath);
         Gson gson = new GsonBuilder().setPrettyPrinting().create();
         out.putNextEntry(zipEntry);
         out.write(gson.toJson((JsonElement)entry.getValue()).getBytes());
         out.closeEntry();
      }

      for (Entry<ResourceLocation, PackEntry<EditorAnimation, DatapackAnimation<? extends StaticAnimation>>> entry : this.userAnimations.entrySet()) {
         String exportPath = String.format("assets/%s/animmodels/animations/%s.json", entry.getKey().m_135827_(), entry.getKey().m_135815_());
         ResourceLocation clientData = AnimationManager.getSubAnimationFileLocation(
            ResourceLocation.fromNamespaceAndPath(entry.getKey().m_135827_(), exportPath), AnimationSubFileReader.SUBFILE_CLIENT_PROPERTY
         );
         ZipEntry asResource = new ZipEntry(exportPath);
         ZipEntry asResourceClientData = new ZipEntry(clientData.m_135815_());
         ZipEntry asData = new ZipEntry(String.format("data/%s/animmodels/animations/%s.json", entry.getKey().m_135827_(), entry.getKey().m_135815_()));
         Gson gson = new GsonBuilder().setPrettyPrinting().create();
         EditorAnimation animation = entry.getValue().getValue().getCreator();
         JsonObject root = new JsonObject();
         JsonObject constructorInfo = new JsonObject();
         constructorInfo.addProperty("invocation_command", animation.getInvocationCommand());
         root.add("constructor", constructorInfo);
         root.add("animation", animation.getRawAnimationJson());
         out.putNextEntry(asResource);
         out.write(gson.toJson(root).getBytes());
         out.closeEntry();
         if (animation.getPropertiesJson().size() > 0) {
            out.putNextEntry(asResourceClientData);
            out.write(gson.toJson(animation.getPropertiesJson()).getBytes());
            out.closeEntry();
         }

         out.putNextEntry(asData);
         out.write(gson.toJson(root).getBytes());
         out.closeEntry();
      }
   }

   public void destroyUserData() {
      this.userMeshes.values().forEach(accessor -> accessor.get().destroy());
   }

   public Map<ResourceLocation, AssetAccessor<? extends SkinnedMesh>> getUserMeshes() {
      return this.userMeshes;
   }

   public Map<ResourceLocation, AssetAccessor<? extends Armature>> getUserArmatures() {
      return this.userArmatures;
   }

   public Map<ResourceLocation, PackEntry<EditorAnimation, DatapackAnimation<? extends StaticAnimation>>> getUserAniamtions() {
      return this.userAnimations;
   }

   abstract class DatapackTab<T> extends GridLayoutTab {
      protected Grid packListGrid;
      protected InputComponentList<CompoundTag> inputComponentsList;
      protected final IForgeRegistry<T> registry;
      protected final Set<ResourceLocation> namesSet = Sets.newHashSet();
      protected final List<PackEntry<ResourceLocation, CompoundTag>> packList = Lists.newLinkedList();
      protected final String directory;

      public DatapackTab(Component title, String directory, @Nullable IForgeRegistry<T> registry) {
         this(title, directory, registry, item -> true);
      }

      public DatapackTab(Component title, String directory, @Nullable IForgeRegistry<T> registry, Predicate<T> filter) {
         super(title);
         this.directory = directory;
         ScreenRectangle screenRect = DatapackEditScreen.this.m_264198_();
         this.packListGrid = Grid.builder(DatapackEditScreen.this)
            .xy1(8, screenRect.m_274449_() + 14)
            .xy2(150, screenRect.f_263800_() - screenRect.m_274449_() - 7)
            .rowHeight(26)
            .rowEditable(Grid.GridBuilder.RowEditButton.ADD_REMOVE)
            .transparentBackground(true)
            .rowpositionChanged(this::packGridRowpositionChanged)
            .addColumn(
               Grid.editbox("pack_item")
                  .editWidgetCreated(editbox -> editbox.m_94153_(str -> ResourceLocation.m_135830_(str)))
                  .valueChanged(event -> this.packList.get(event.rowposition).setPackKey(ResourceLocation.parse(event.postValue)))
                  .defaultVal(EpicFightMod.prefix(""))
                  .editable(registry == null)
                  .width(180)
            )
            .pressAdd((grid, button) -> {
               if (registry != null) {
                  DatapackEditScreen.this.f_96541_.m_91152_(new SelectFromRegistryScreen<>(DatapackEditScreen.this, registry, (registryName, selItem) -> {
                     grid.setValueChangeEnabled(false);
                     int rowpositionx = grid.addRowWithDefaultValues("pack_item", registryName);
                     this.packList.add(rowpositionx, PackEntry.of(ResourceLocation.parse(registryName), CompoundTag::new));
                     grid.setGridFocus(rowpositionx, "pack_item");
                     grid.setValueChangeEnabled(true);
                  }, (registryName, selItem) -> {}, filter));
               } else {
                  grid.setValueChangeEnabled(false);
                  int rowposition = grid.addRowWithDefaultValues("pack_item", EpicFightMod.prefix(""));
                  this.packList.add(rowposition, PackEntry.of(EpicFightMod.identifier(""), CompoundTag::new));
                  grid.setGridFocus(rowposition, "pack_item");
                  grid.setValueChangeEnabled(true);
               }

               DatapackEditScreen.this.m_7522_(grid);
            })
            .pressRemove((grid, button) -> {
               grid.removeRow(removedRow -> this.packList.remove(removedRow));
               if (this.packList.size() == 0) {
                  this.inputComponentsList.setComponentsActive(false);
               }
            })
            .build();
         this.registry = registry;
      }

      public void m_267697_(ScreenRectangle screenRectangle) {
         this.f_267367_.m_264036_();
         this.f_267367_.m_253211_(screenRectangle.m_274449_());
         this.packListGrid.m_93437_(150, screenRectangle.f_263800_(), screenRectangle.m_274449_() + 14, screenRectangle.f_263800_() + 7);
         this.inputComponentsList
            .m_93437_(screenRectangle.f_263770_() - 172, screenRectangle.f_263800_(), screenRectangle.m_274449_() + 14, screenRectangle.f_263800_() + 7);
         this.packListGrid.m_93507_(8);
         this.inputComponentsList.m_93507_(164);
      }

      public void m_267681_() {
         this.packListGrid._tick();
         this.inputComponentsList.tick();
      }

      public void clear() {
         this.packList.clear();
         this.packListGrid.reset();
         this.inputComponentsList.resetComponents();
         this.inputComponentsList.setComponentsActive(false);
      }

      public void packGridRowpositionChanged(int rowposition, Map<String, Object> values) {
         this.inputComponentsList.importTag(this.packList.get(rowposition).getValue());
      }

      public abstract void validateBeforeExport();

      public abstract void importEntries(PackResources var1) throws Exception;

      public abstract void exportEntries(ZipOutputStream var1) throws Exception;
   }

   class ItemCapabilityTab extends DatapackEditScreen.DatapackTab<Item> {
      private ModelPreviewer modelPreviewer;
      private ComboBox<DatapackEditScreen.ItemCapabilityTab.ItemType> itemTypeCombo;
      private Consumer<DatapackEditScreen.ItemCapabilityTab.ItemType> responder;

      public ItemCapabilityTab() {
         super(Component.m_237115_(EpicFightMod.format("gui.%s.tab.datapack.item_capability")), "capabilities", ForgeRegistries.ITEMS);
         Screen parentScreen = DatapackEditScreen.this;
         this.modelPreviewer = new ModelPreviewer(20, 15, 0, 150, ResizableComponent.HorizontalSizing.LEFT_RIGHT, null, Armatures.BIPED, Meshes.BIPED);
         this.modelPreviewer.setColliderJoint(Armatures.BIPED.get().searchJointByName("Tool_R"));
         this.responder = itemType -> {
            CompoundTag tag = this.packList.get(this.packListGrid.getRowposition()).getValue();
            tag.f_128329_.clear();
            tag.m_128359_("item_type", ParseUtil.nullParam(itemType));
            this.rearrangeElements(itemType, tag);
            if (itemType == DatapackEditScreen.ItemCapabilityTab.ItemType.WEAPON) {
               CompoundTag trailTag = ParseUtil.getOrSupply(tag, "trail", CompoundTag::new);
               CompoundTag colliderTag = ParseUtil.getOrSupply(tag, "collider", CompoundTag::new);
               boolean centerInit = colliderTag.m_128441_("center");
               boolean sizeInit = colliderTag.m_128441_("size");
               boolean colorInit = trailTag.m_128441_("color");
               boolean beginInit = trailTag.m_128441_("begin_pos");
               boolean endInit = trailTag.m_128441_("end_pos");
               this.itemTypeCombo._setResponder(null);
               this.inputComponentsList
                  .setDataBindingComponenets(
                     new Object[]{
                        itemType,
                        DatapackEditScreen.getWeaponType(tag.m_128461_("type")),
                        null,
                        ParseUtil.nullParam(colliderTag.m_128423_("number")),
                        centerInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(colliderTag.m_128437_("center", 6).get(0), Tag::m_7916_)) : "",
                        centerInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(colliderTag.m_128437_("center", 6).get(1), Tag::m_7916_)) : "",
                        centerInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(colliderTag.m_128437_("center", 6).get(2), Tag::m_7916_)) : "",
                        sizeInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(colliderTag.m_128437_("size", 6).get(0), Tag::m_7916_)) : "",
                        sizeInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(colliderTag.m_128437_("size", 6).get(1), Tag::m_7916_)) : "",
                        sizeInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(colliderTag.m_128437_("size", 6).get(2), Tag::m_7916_)) : "",
                        colorInit ? ParseUtil.nullParam(ParseUtil.nullOrToString(trailTag.m_128437_("color", 3).get(0), Tag::m_7916_)) : "",
                        colorInit ? ParseUtil.nullParam(ParseUtil.nullOrToString(trailTag.m_128437_("color", 3).get(1), Tag::m_7916_)) : "",
                        colorInit ? ParseUtil.nullParam(ParseUtil.nullOrToString(trailTag.m_128437_("color", 3).get(2), Tag::m_7916_)) : "",
                        beginInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(trailTag.m_128437_("begin_pos", 6).get(0), Tag::m_7916_)) : "",
                        beginInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(trailTag.m_128437_("begin_pos", 6).get(1), Tag::m_7916_)) : "",
                        beginInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(trailTag.m_128437_("begin_pos", 6).get(2), Tag::m_7916_)) : "",
                        endInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(trailTag.m_128437_("end_pos", 6).get(0), Tag::m_7916_)) : "",
                        endInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(trailTag.m_128437_("end_pos", 6).get(1), Tag::m_7916_)) : "",
                        endInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(trailTag.m_128437_("end_pos", 6).get(2), Tag::m_7916_)) : "",
                        ParseUtil.nullParam(trailTag.m_128423_("lifetime")),
                        ParseUtil.nullParam(trailTag.m_128423_("interpolations")),
                        ParseUtil.nullParam(trailTag.m_128461_("texture_path")),
                        ForgeRegistries.PARTICLE_TYPES.getValue(ResourceLocation.parse(trailTag.m_128461_("particle_type")))
                     }
                  );
               this.itemTypeCombo._setResponder(this.responder);
            } else {
               this.itemTypeCombo._setResponder(null);
               this.inputComponentsList.setDataBindingComponenets(new Object[]{itemType});
               this.itemTypeCombo._setResponder(this.responder);
            }
         };
         this.itemTypeCombo = new ComboBox<>(
            parentScreen,
            parentScreen.getMinecraft().f_91062_,
            0,
            124,
            100,
            15,
            ResizableComponent.HorizontalSizing.LEFT_WIDTH,
            null,
            8,
            Component.m_237115_("datapack_edit.item_capability.item_type"),
            List.of(DatapackEditScreen.ItemCapabilityTab.ItemType.values()),
            ParseUtil::snakeToSpacedCamel,
            this.responder
         );
         this.inputComponentsList = new InputComponentList<CompoundTag>(DatapackEditScreen.this, 0, 0, 0, 0, 30) {
            public void importTag(CompoundTag tag) {
               DatapackEditScreen.ItemCapabilityTab.ItemType itemType = null;

               try {
                  itemType = DatapackEditScreen.ItemCapabilityTab.ItemType.valueOf(tag.m_128461_("item_type"));
               } catch (IllegalArgumentException var10) {
               }

               ItemCapabilityTab.this.rearrangeElements(itemType, tag);
               this.setComponentsActive(true);
               if (itemType == DatapackEditScreen.ItemCapabilityTab.ItemType.WEAPON) {
                  CompoundTag trailTag = ParseUtil.getOrSupply(tag, "trail", CompoundTag::new);
                  CompoundTag colliderTag = ParseUtil.getOrSupply(tag, "collider", CompoundTag::new);
                  boolean centerInit = colliderTag.m_128441_("center");
                  boolean sizeInit = colliderTag.m_128441_("size");
                  boolean colorInit = trailTag.m_128441_("color");
                  boolean beginInit = trailTag.m_128441_("begin_pos");
                  boolean endInit = trailTag.m_128441_("end_pos");
                  ItemCapabilityTab.this.itemTypeCombo._setResponder(null);
                  this.setDataBindingComponenets(
                     new Object[]{
                        itemType,
                        DatapackEditScreen.getWeaponType(tag.m_128461_("type")),
                        null,
                        ParseUtil.nullParam(colliderTag.m_128423_("number")),
                        centerInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(colliderTag.m_128437_("center", 6).get(0), Tag::m_7916_)) : "",
                        centerInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(colliderTag.m_128437_("center", 6).get(1), Tag::m_7916_)) : "",
                        centerInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(colliderTag.m_128437_("center", 6).get(2), Tag::m_7916_)) : "",
                        sizeInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(colliderTag.m_128437_("size", 6).get(0), Tag::m_7916_)) : "",
                        sizeInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(colliderTag.m_128437_("size", 6).get(1), Tag::m_7916_)) : "",
                        sizeInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(colliderTag.m_128437_("size", 6).get(2), Tag::m_7916_)) : "",
                        colorInit ? ParseUtil.nullParam(ParseUtil.nullOrToString(trailTag.m_128437_("color", 3).get(0), Tag::m_7916_)) : "",
                        colorInit ? ParseUtil.nullParam(ParseUtil.nullOrToString(trailTag.m_128437_("color", 3).get(1), Tag::m_7916_)) : "",
                        colorInit ? ParseUtil.nullParam(ParseUtil.nullOrToString(trailTag.m_128437_("color", 3).get(2), Tag::m_7916_)) : "",
                        beginInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(trailTag.m_128437_("begin_pos", 6).get(0), Tag::m_7916_)) : "",
                        beginInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(trailTag.m_128437_("begin_pos", 6).get(1), Tag::m_7916_)) : "",
                        beginInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(trailTag.m_128437_("begin_pos", 6).get(2), Tag::m_7916_)) : "",
                        endInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(trailTag.m_128437_("end_pos", 6).get(0), Tag::m_7916_)) : "",
                        endInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(trailTag.m_128437_("end_pos", 6).get(1), Tag::m_7916_)) : "",
                        endInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(trailTag.m_128437_("end_pos", 6).get(2), Tag::m_7916_)) : "",
                        ParseUtil.nullParam(trailTag.m_128423_("lifetime")),
                        ParseUtil.nullParam(trailTag.m_128423_("interpolations")),
                        ParseUtil.nullParam(trailTag.m_128461_("texture_path")),
                        ForgeRegistries.PARTICLE_TYPES.getValue(ResourceLocation.parse(trailTag.m_128461_("particle_type")))
                     }
                  );
                  ItemCapabilityTab.this.itemTypeCombo._setResponder(ItemCapabilityTab.this.responder);
               } else {
                  ItemCapabilityTab.this.itemTypeCombo._setResponder(null);
                  this.setDataBindingComponenets(new Object[]{itemType});
                  ItemCapabilityTab.this.itemTypeCombo._setResponder(ItemCapabilityTab.this.responder);
               }
            }
         };
         this.inputComponentsList.newRow();
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  parentScreen,
                  this.inputComponentsList.nextStart(4),
                  100,
                  60,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  "datapack_edit.item_capability.item_type"
               )
            );
         this.inputComponentsList
            .addComponentCurrentRow(this.itemTypeCombo.relocateX(DatapackEditScreen.this.m_264198_(), this.inputComponentsList.nextStart(5)));
         this.inputComponentsList.setComponentsActive(false);
      }

      private void rearrangeElements(DatapackEditScreen.ItemCapabilityTab.ItemType itemType, CompoundTag tag) {
         this.inputComponentsList.clearComponents();
         Font font = DatapackEditScreen.this.f_96547_;
         ScreenRectangle rect = DatapackEditScreen.this.m_264198_();
         this.inputComponentsList.newRow();
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  DatapackEditScreen.this.parentScreen,
                  this.inputComponentsList.nextStart(4),
                  100,
                  60,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  "datapack_edit.item_capability.item_type"
               )
            );
         this.inputComponentsList.addComponentCurrentRow(this.itemTypeCombo.relocateX(rect, this.inputComponentsList.nextStart(5)));
         if (itemType == DatapackEditScreen.ItemCapabilityTab.ItemType.WEAPON) {
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(4),
                     100,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.item_capability.attributes"
                  )
               );
            this.inputComponentsList
               .addComponentCurrentRow(
                  SubScreenOpenButton.builder()
                     .subScreen(
                        () -> new WeaponAttributeScreen(
                           DatapackEditScreen.this,
                           ParseUtil.getOrDefaultTag(this.packList.get(this.packListGrid.getRowposition()).getValue(), "attributes", new CompoundTag()),
                           itemType
                        )
                     )
                     .bounds(this.inputComponentsList.nextStart(4), 0, 15, 15)
                     .build()
               );
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(4),
                     100,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.item_capability.type"
                  )
               );
            this.inputComponentsList
               .addComponentCurrentRow(
                  new PopupBox.WeaponTypePopupBox(
                     DatapackEditScreen.this,
                     font,
                     this.inputComponentsList.nextStart(5),
                     15,
                     15,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_RIGHT,
                     null,
                     Component.m_237115_("datapack_edit.item_capability.type"),
                     pair -> {
                        CompoundTag currentTag = this.packList.get(this.packListGrid.getRowposition()).getValue();
                        currentTag.m_128359_("type", (String)pair.getFirst());
                        if (pair.getSecond() != null) {
                           CapabilityItem.Builder builder = (CapabilityItem.Builder)((Function)pair.getSecond())
                              .apply((Item)this.registry.getValue(this.packList.get(this.packListGrid.getRowposition()).getKey()));
                           if (builder instanceof WeaponCapability.Builder weaponBuilder) {
                              this.modelPreviewer.clearAnimations();
                              List<AssetAccessor<? extends StaticAnimation>> allAnimations = weaponBuilder.getComboAnimations()
                                 .entrySet()
                                 .stream()
                                 .reduce(Lists.newArrayList(), (list, entry) -> {
                                    list.addAll(entry.getValue());
                                    return list;
                                 }, (list1, list2) -> {
                                    list1.addAll(list2);
                                    return list1;
                                 });
                              allAnimations.stream().forEach(this.modelPreviewer::addAnimationToPlay);
                              boolean hasCustomCollider = false;
                              if (currentTag.m_128441_("collider")) {
                                 try {
                                    ColliderPreset.deserializeSimpleCollider(currentTag.m_128469_("collider"));
                                    hasCustomCollider = true;
                                 } catch (IllegalArgumentException var8x) {
                                 }
                              }

                              if (!hasCustomCollider) {
                                 this.modelPreviewer.setCollider(weaponBuilder.getCollider());
                              }
                           }
                        }
                     }
                  )
               );
            ResizableEditBox colliderCount = new ResizableEditBox(
               font, 0, 40, 0, 15, Component.m_237115_("datapack_edit.collider.count"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
            );
            ResizableEditBox colliderCenterX = new ResizableEditBox(
               font, 0, 35, 0, 15, Component.m_237115_("datapack_edit.collider.center.x"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
            );
            ResizableEditBox colliderCenterY = new ResizableEditBox(
               font, 0, 35, 0, 15, Component.m_237115_("datapack_edit.collider.center.y"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
            );
            ResizableEditBox colliderCenterZ = new ResizableEditBox(
               font, 0, 35, 0, 15, Component.m_237115_("datapack_edit.collider.center.z"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
            );
            ResizableEditBox colliderSizeX = new ResizableEditBox(
               font, 0, 35, 0, 15, Component.m_237115_("datapack_edit.collider.size.x"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
            );
            ResizableEditBox colliderSizeY = new ResizableEditBox(
               font, 0, 35, 0, 15, Component.m_237115_("datapack_edit.collider.size.y"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
            );
            ResizableEditBox colliderSizeZ = new ResizableEditBox(
               font, 0, 35, 0, 15, Component.m_237115_("datapack_edit.collider.size.z"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
            );
            colliderCount.m_94151_(
               input -> {
                  CompoundTag collider = ParseUtil.getOrDefaultTag(
                     this.packList.get(this.packListGrid.getRowposition()).getValue(), "collider", new CompoundTag()
                  );
                  if (StringUtil.isNullOrEmpty(input)) {
                     collider.m_128473_("number");
                  } else {
                     collider.m_128365_("number", IntTag.m_128679_(Integer.valueOf(input)));
                  }

                  try {
                     this.modelPreviewer.setCollider(ColliderPreset.deserializeSimpleCollider(collider));
                  } catch (IllegalArgumentException e) {
                     this.modelPreviewer.setCollider(null);
                  }
               }
            );
            colliderCenterX.m_94151_(
               input -> {
                  CompoundTag collider = ParseUtil.getOrDefaultTag(
                     this.packList.get(this.packListGrid.getRowposition()).getValue(), "collider", new CompoundTag()
                  );
                  ListTag centerVec = ParseUtil.getOrDefaultTag(collider, "center", new ListTag());
                  if (centerVec.isEmpty()) {
                     centerVec.add(DoubleTag.m_128500_(0.0));
                     centerVec.add(DoubleTag.m_128500_(0.0));
                     centerVec.add(DoubleTag.m_128500_(0.0));
                  }

                  double i = StringUtil.isNullOrEmpty(input) ? 0.0 : ParseUtil.parseOrGet(input, Double::valueOf, 0.0);
                  centerVec.set(0, DoubleTag.m_128500_(i));

                  try {
                     this.modelPreviewer.setCollider(ColliderPreset.deserializeSimpleCollider(collider));
                  } catch (IllegalArgumentException e) {
                     this.modelPreviewer.setCollider(null);
                  }
               }
            );
            colliderCenterY.m_94151_(
               input -> {
                  CompoundTag collider = ParseUtil.getOrDefaultTag(
                     this.packList.get(this.packListGrid.getRowposition()).getValue(), "collider", new CompoundTag()
                  );
                  ListTag centerVec = ParseUtil.getOrDefaultTag(collider, "center", new ListTag());
                  if (centerVec.isEmpty()) {
                     centerVec.add(DoubleTag.m_128500_(0.0));
                     centerVec.add(DoubleTag.m_128500_(0.0));
                     centerVec.add(DoubleTag.m_128500_(0.0));
                  }

                  double i = StringUtil.isNullOrEmpty(input) ? 0.0 : ParseUtil.parseOrGet(input, Double::valueOf, 0.0);
                  centerVec.set(1, DoubleTag.m_128500_(i));

                  try {
                     this.modelPreviewer.setCollider(ColliderPreset.deserializeSimpleCollider(collider));
                  } catch (IllegalArgumentException e) {
                     this.modelPreviewer.setCollider(null);
                  }
               }
            );
            colliderCenterZ.m_94151_(
               input -> {
                  CompoundTag collider = ParseUtil.getOrDefaultTag(
                     this.packList.get(this.packListGrid.getRowposition()).getValue(), "collider", new CompoundTag()
                  );
                  ListTag centerVec = ParseUtil.getOrDefaultTag(collider, "center", new ListTag());
                  if (centerVec.isEmpty()) {
                     centerVec.add(DoubleTag.m_128500_(0.0));
                     centerVec.add(DoubleTag.m_128500_(0.0));
                     centerVec.add(DoubleTag.m_128500_(0.0));
                  }

                  double i = StringUtil.isNullOrEmpty(input) ? 0.0 : ParseUtil.parseOrGet(input, Double::valueOf, 0.0);
                  centerVec.set(2, DoubleTag.m_128500_(i));

                  try {
                     this.modelPreviewer.setCollider(ColliderPreset.deserializeSimpleCollider(collider));
                  } catch (IllegalArgumentException e) {
                     this.modelPreviewer.setCollider(null);
                  }
               }
            );
            colliderSizeX.m_94151_(
               input -> {
                  CompoundTag collider = ParseUtil.getOrDefaultTag(
                     this.packList.get(this.packListGrid.getRowposition()).getValue(), "collider", new CompoundTag()
                  );
                  ListTag sizeVec = ParseUtil.getOrDefaultTag(collider, "size", new ListTag());
                  if (sizeVec.isEmpty()) {
                     sizeVec.add(DoubleTag.m_128500_(0.0));
                     sizeVec.add(DoubleTag.m_128500_(0.0));
                     sizeVec.add(DoubleTag.m_128500_(0.0));
                  }

                  double i = StringUtil.isNullOrEmpty(input) ? 0.0 : Double.valueOf(input);
                  sizeVec.set(0, DoubleTag.m_128500_(i));

                  try {
                     this.modelPreviewer.setCollider(ColliderPreset.deserializeSimpleCollider(collider));
                  } catch (IllegalArgumentException e) {
                     this.modelPreviewer.setCollider(null);
                  }
               }
            );
            colliderSizeY.m_94151_(
               input -> {
                  CompoundTag collider = ParseUtil.getOrDefaultTag(
                     this.packList.get(this.packListGrid.getRowposition()).getValue(), "collider", new CompoundTag()
                  );
                  ListTag sizeVec = ParseUtil.getOrDefaultTag(collider, "size", new ListTag());
                  if (sizeVec.isEmpty()) {
                     sizeVec.add(DoubleTag.m_128500_(0.0));
                     sizeVec.add(DoubleTag.m_128500_(0.0));
                     sizeVec.add(DoubleTag.m_128500_(0.0));
                  }

                  double i = StringUtil.isNullOrEmpty(input) ? 0.0 : Double.valueOf(input);
                  sizeVec.set(1, DoubleTag.m_128500_(i));

                  try {
                     this.modelPreviewer.setCollider(ColliderPreset.deserializeSimpleCollider(collider));
                  } catch (IllegalArgumentException e) {
                     this.modelPreviewer.setCollider(null);
                  }
               }
            );
            colliderSizeZ.m_94151_(
               input -> {
                  CompoundTag collider = ParseUtil.getOrDefaultTag(
                     this.packList.get(this.packListGrid.getRowposition()).getValue(), "collider", new CompoundTag()
                  );
                  ListTag sizeVec = ParseUtil.getOrDefaultTag(collider, "size", new ListTag());
                  if (sizeVec.isEmpty()) {
                     sizeVec.add(DoubleTag.m_128500_(0.0));
                     sizeVec.add(DoubleTag.m_128500_(0.0));
                     sizeVec.add(DoubleTag.m_128500_(0.0));
                  }

                  double i = StringUtil.isNullOrEmpty(input) ? 0.0 : Double.valueOf(input);
                  sizeVec.set(2, DoubleTag.m_128500_(i));

                  try {
                     this.modelPreviewer.setCollider(ColliderPreset.deserializeSimpleCollider(collider));
                  } catch (IllegalArgumentException e) {
                     this.modelPreviewer.setCollider(null);
                  }
               }
            );
            colliderCount.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Integer::parseInt));
            colliderCenterX.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsableAllowingMinus(context, Double::parseDouble));
            colliderCenterY.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsableAllowingMinus(context, Double::parseDouble));
            colliderCenterZ.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsableAllowingMinus(context, Double::parseDouble));
            colliderSizeX.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Double::parseDouble));
            colliderSizeY.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Double::parseDouble));
            colliderSizeZ.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Double::parseDouble));
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(4),
                     100,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     Component.m_237115_("datapack_edit.collider"),
                     Component.m_237115_("datapack_edit.item_capability.collider.tooltip")
                  )
               );
            this.inputComponentsList
               .addComponentCurrentRow(
                  new PopupBox.ColliderPopupBox(
                        DatapackEditScreen.this,
                        font,
                        this.inputComponentsList.nextStart(5),
                        15,
                        130,
                        15,
                        ResizableComponent.HorizontalSizing.LEFT_RIGHT,
                        null,
                        Component.m_237115_("datapack_edit.collider"),
                        pair -> {
                           if (pair.getSecond() != null) {
                              CompoundTag colliderTag = ParseUtil.getOrDefaultTag(
                                 this.packList.get(this.packListGrid.getRowposition()).getValue(), "collider", new CompoundTag()
                              );
                              ((Collider)pair.getSecond()).serialize(colliderTag);
                              colliderCount.m_94144_(String.valueOf(colliderTag.m_128451_("number")));
                              ListTag centerVec = colliderTag.m_128437_("center", 6);
                              colliderCenterX.m_94144_(String.valueOf(centerVec.m_128772_(0)));
                              colliderCenterY.m_94144_(String.valueOf(centerVec.m_128772_(1)));
                              colliderCenterZ.m_94144_(String.valueOf(centerVec.m_128772_(2)));
                              ListTag sizeVec = colliderTag.m_128437_("size", 6);
                              colliderSizeX.m_94144_(String.valueOf(sizeVec.m_128772_(0)));
                              colliderSizeY.m_94144_(String.valueOf(sizeVec.m_128772_(1)));
                              colliderSizeZ.m_94144_(String.valueOf(sizeVec.m_128772_(2)));
                           } else if (this.packList.get(this.packListGrid.getRowposition()).getValue().m_128441_("collider")) {
                              colliderCount.m_94144_("");
                              colliderCenterX.m_94144_("");
                              colliderCenterY.m_94144_("");
                              colliderCenterZ.m_94144_("");
                              colliderSizeX.m_94144_("");
                              colliderSizeY.m_94144_("");
                              colliderSizeZ.m_94144_("");
                           }

                           this.modelPreviewer.setCollider((Collider)pair.getSecond());
                        }
                     )
                     .applyFilter(collider -> collider instanceof OBBCollider || collider instanceof MultiOBBCollider)
               );
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(20),
                     40,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.collider.count"
                  )
               );
            this.inputComponentsList.addComponentCurrentRow(colliderCount.relocateX(rect, this.inputComponentsList.nextStart(5)));
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(20),
                     40,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.collider.center"
                  )
               );
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(5),
                     8,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     Component.m_237113_("X: ")
                  )
               );
            this.inputComponentsList.addComponentCurrentRow(colliderCenterX.relocateX(rect, this.inputComponentsList.nextStart(5)));
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(8),
                     8,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     Component.m_237113_("Y: ")
                  )
               );
            this.inputComponentsList.addComponentCurrentRow(colliderCenterY.relocateX(rect, this.inputComponentsList.nextStart(5)));
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(8),
                     8,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     Component.m_237113_("Z: ")
                  )
               );
            this.inputComponentsList.addComponentCurrentRow(colliderCenterZ.relocateX(rect, this.inputComponentsList.nextStart(5)));
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(20),
                     40,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.collider.size"
                  )
               );
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(5),
                     8,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     Component.m_237113_("X: ")
                  )
               );
            this.inputComponentsList.addComponentCurrentRow(colliderSizeX.relocateX(rect, this.inputComponentsList.nextStart(5)));
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(8),
                     8,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     Component.m_237113_("Y: ")
                  )
               );
            this.inputComponentsList.addComponentCurrentRow(colliderSizeY.relocateX(rect, this.inputComponentsList.nextStart(5)));
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(8),
                     8,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     Component.m_237113_("Z: ")
                  )
               );
            this.inputComponentsList.addComponentCurrentRow(colliderSizeZ.relocateX(rect, this.inputComponentsList.nextStart(5)));
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(4),
                     90,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.item_capability.trail"
                  )
               );
            ResizableEditBox colorR = new ResizableEditBox(
               font, 0, 30, 0, 15, Component.m_237115_("datapack_edit.item_capability.trail.color.r"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
            );
            ResizableEditBox colorG = new ResizableEditBox(
               font, 0, 30, 0, 15, Component.m_237115_("datapack_edit.item_capability.trail.color.g"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
            );
            ResizableEditBox colorB = new ResizableEditBox(
               font, 0, 30, 0, 15, Component.m_237115_("datapack_edit.item_capability.trail.color.b"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
            );
            ColorPreviewWidget colorWidget = new ColorPreviewWidget(
               0, 12, 0, 12, ResizableComponent.HorizontalSizing.LEFT_WIDTH, null, Component.m_237115_("datapack_edit.item_capability.color")
            );
            colorR.m_94151_(
               input -> {
                  CompoundTag trailTag = this.packList.get(this.packListGrid.getRowposition()).getValue().m_128469_("trail");
                  ListTag list = ParseUtil.getOrDefaultTag(trailTag, "color", new ListTag());
                  if (list.isEmpty()) {
                     list.add(IntTag.m_128679_(0));
                     list.add(IntTag.m_128679_(0));
                     list.add(IntTag.m_128679_(0));
                  }

                  int i = StringUtil.isNullOrEmpty(input) ? 0 : Integer.valueOf(input);
                  list.set(0, IntTag.m_128679_(i));
                  colorWidget.setColor(
                     ParseUtil.parseOrGet(input, Integer::valueOf, 0),
                     ParseUtil.parseOrGet(colorG.m_94155_(), Integer::valueOf, 0),
                     ParseUtil.parseOrGet(colorB.m_94155_(), Integer::valueOf, 0)
                  );
                  TrailInfo trailInfo = TrailInfo.deserialize(trailTag);
                  this.modelPreviewer.setTrailInfo(trailInfo);
               }
            );
            colorG.m_94151_(
               input -> {
                  CompoundTag trailTag = this.packList.get(this.packListGrid.getRowposition()).getValue().m_128469_("trail");
                  ListTag list = ParseUtil.getOrDefaultTag(trailTag, "color", new ListTag());
                  if (list.isEmpty()) {
                     list.add(IntTag.m_128679_(0));
                     list.add(IntTag.m_128679_(0));
                     list.add(IntTag.m_128679_(0));
                  }

                  int i = StringUtil.isNullOrEmpty(input) ? 0 : Integer.valueOf(input);
                  list.set(1, IntTag.m_128679_(i));
                  colorWidget.setColor(
                     ParseUtil.parseOrGet(colorR.m_94155_(), Integer::valueOf, 0),
                     ParseUtil.parseOrGet(input, Integer::valueOf, 0),
                     ParseUtil.parseOrGet(colorB.m_94155_(), Integer::valueOf, 0)
                  );
                  TrailInfo trailInfo = TrailInfo.deserialize(trailTag);
                  this.modelPreviewer.setTrailInfo(trailInfo);
               }
            );
            colorB.m_94151_(
               input -> {
                  CompoundTag trailTag = this.packList.get(this.packListGrid.getRowposition()).getValue().m_128469_("trail");
                  ListTag list = ParseUtil.getOrDefaultTag(trailTag, "color", new ListTag());
                  if (list.isEmpty()) {
                     list.add(IntTag.m_128679_(0));
                     list.add(IntTag.m_128679_(0));
                     list.add(IntTag.m_128679_(0));
                  }

                  int i = StringUtil.isNullOrEmpty(input) ? 0 : Integer.valueOf(input);
                  list.set(2, IntTag.m_128679_(i));
                  colorWidget.setColor(
                     ParseUtil.parseOrGet(colorR.m_94155_(), Integer::valueOf, 0),
                     ParseUtil.parseOrGet(colorG.m_94155_(), Integer::valueOf, 0),
                     ParseUtil.parseOrGet(input, Integer::valueOf, 0)
                  );
                  TrailInfo trailInfo = TrailInfo.deserialize(trailTag);
                  this.modelPreviewer.setTrailInfo(trailInfo);
               }
            );
            colorR.m_94153_(
               context -> StringUtil.isNullOrEmpty(context)
                  || ParseUtil.isParsable(context, Integer::parseInt) && ParseUtil.parseOrGet(context, Integer::parseInt, 0) < 256
            );
            colorG.m_94153_(
               context -> StringUtil.isNullOrEmpty(context)
                  || ParseUtil.isParsable(context, Integer::parseInt) && ParseUtil.parseOrGet(context, Integer::parseInt, 0) < 256
            );
            colorB.m_94153_(
               context -> StringUtil.isNullOrEmpty(context)
                  || ParseUtil.isParsable(context, Integer::parseInt) && ParseUtil.parseOrGet(context, Integer::parseInt, 0) < 256
            );
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(20),
                     28,
                     0,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.item_capability.color"
                  )
               );
            this.inputComponentsList.addComponentCurrentRow(colorWidget.relocateX(rect, this.inputComponentsList.nextStart(4)));
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(41),
                     8,
                     0,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     Component.m_237113_("R: ")
                  )
               );
            this.inputComponentsList.addComponentCurrentRow(colorR.relocateX(rect, this.inputComponentsList.nextStart(4)));
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(8),
                     8,
                     0,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     Component.m_237113_("G: ")
                  )
               );
            this.inputComponentsList.addComponentCurrentRow(colorG.relocateX(rect, this.inputComponentsList.nextStart(4)));
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(8),
                     8,
                     0,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     Component.m_237113_("B: ")
                  )
               );
            this.inputComponentsList.addComponentCurrentRow(colorB.relocateX(rect, this.inputComponentsList.nextStart(4)));
            ResizableEditBox beginX = new ResizableEditBox(
               font, 0, 35, 0, 15, Component.m_237115_("datapack_edit.item_capability.trail.begin_pos.x"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
            );
            ResizableEditBox beginY = new ResizableEditBox(
               font, 0, 35, 0, 15, Component.m_237115_("datapack_edit.item_capability.trail.begin_pos.y"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
            );
            ResizableEditBox beginZ = new ResizableEditBox(
               font, 0, 35, 0, 15, Component.m_237115_("datapack_edit.item_capability.trail.begin_pos.z"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
            );
            beginX.m_94151_(input -> {
               CompoundTag trailTag = this.packList.get(this.packListGrid.getRowposition()).getValue().m_128469_("trail");
               ListTag list = ParseUtil.getOrDefaultTag(trailTag, "begin_pos", new ListTag());
               if (list.isEmpty()) {
                  list.add(DoubleTag.m_128500_(0.0));
                  list.add(DoubleTag.m_128500_(0.0));
                  list.add(DoubleTag.m_128500_(0.0));
               }

               double d = StringUtil.isNullOrEmpty(input) ? 0.0 : ParseUtil.parseOrGet(input, Double::valueOf, 0.0);
               list.set(0, DoubleTag.m_128500_(d));
               TrailInfo trailInfo = TrailInfo.deserialize(trailTag);
               this.modelPreviewer.setTrailInfo(trailInfo);
            });
            beginY.m_94151_(input -> {
               CompoundTag trailTag = this.packList.get(this.packListGrid.getRowposition()).getValue().m_128469_("trail");
               ListTag list = ParseUtil.getOrDefaultTag(trailTag, "begin_pos", new ListTag());
               if (list.isEmpty()) {
                  list.add(DoubleTag.m_128500_(0.0));
                  list.add(DoubleTag.m_128500_(0.0));
                  list.add(DoubleTag.m_128500_(0.0));
               }

               double d = StringUtil.isNullOrEmpty(input) ? 0.0 : ParseUtil.parseOrGet(input, Double::valueOf, 0.0);
               list.set(1, DoubleTag.m_128500_(d));
               TrailInfo trailInfo = TrailInfo.deserialize(trailTag);
               this.modelPreviewer.setTrailInfo(trailInfo);
            });
            beginZ.m_94151_(input -> {
               CompoundTag trailTag = this.packList.get(this.packListGrid.getRowposition()).getValue().m_128469_("trail");
               ListTag list = ParseUtil.getOrDefaultTag(trailTag, "begin_pos", new ListTag());
               if (list.isEmpty()) {
                  list.add(DoubleTag.m_128500_(0.0));
                  list.add(DoubleTag.m_128500_(0.0));
                  list.add(DoubleTag.m_128500_(0.0));
               }

               double d = StringUtil.isNullOrEmpty(input) ? 0.0 : ParseUtil.parseOrGet(input, Double::valueOf, 0.0);
               list.set(2, DoubleTag.m_128500_(d));
               TrailInfo trailInfo = TrailInfo.deserialize(trailTag);
               this.modelPreviewer.setTrailInfo(trailInfo);
            });
            beginX.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsableAllowingMinus(context, Double::parseDouble));
            beginY.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsableAllowingMinus(context, Double::parseDouble));
            beginZ.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsableAllowingMinus(context, Double::parseDouble));
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(20),
                     80,
                     0,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.item_capability.begin_pos"
                  )
               );
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(5),
                     8,
                     0,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     Component.m_237113_("X: ")
                  )
               );
            this.inputComponentsList.addComponentCurrentRow(beginX.relocateX(rect, this.inputComponentsList.nextStart(4)));
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(8),
                     8,
                     0,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     Component.m_237113_("Y: ")
                  )
               );
            this.inputComponentsList.addComponentCurrentRow(beginY.relocateX(rect, this.inputComponentsList.nextStart(4)));
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(8),
                     8,
                     0,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     Component.m_237113_("Z: ")
                  )
               );
            this.inputComponentsList.addComponentCurrentRow(beginZ.relocateX(rect, this.inputComponentsList.nextStart(4)));
            ResizableEditBox endX = new ResizableEditBox(
               font, 0, 35, 0, 15, Component.m_237115_("datapack_edit.item_capability.trail.end_pos.x"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
            );
            ResizableEditBox endY = new ResizableEditBox(
               font, 0, 35, 0, 15, Component.m_237115_("datapack_edit.item_capability.trail.end_pos.y"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
            );
            ResizableEditBox endZ = new ResizableEditBox(
               font, 0, 35, 0, 15, Component.m_237115_("datapack_edit.item_capability.trail.end_pos.z"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
            );
            endX.m_94151_(input -> {
               CompoundTag trailTag = this.packList.get(this.packListGrid.getRowposition()).getValue().m_128469_("trail");
               ListTag list = ParseUtil.getOrDefaultTag(trailTag, "end_pos", new ListTag());
               if (list.isEmpty()) {
                  list.add(DoubleTag.m_128500_(0.0));
                  list.add(DoubleTag.m_128500_(0.0));
                  list.add(DoubleTag.m_128500_(0.0));
               }

               double d = StringUtil.isNullOrEmpty(input) ? 0.0 : ParseUtil.parseOrGet(input, Double::valueOf, 0.0);
               list.remove(0);
               list.add(0, DoubleTag.m_128500_(d));
               TrailInfo trailInfo = TrailInfo.deserialize(trailTag);
               this.modelPreviewer.setTrailInfo(trailInfo);
            });
            endY.m_94151_(input -> {
               CompoundTag trailTag = this.packList.get(this.packListGrid.getRowposition()).getValue().m_128469_("trail");
               ListTag list = ParseUtil.getOrDefaultTag(trailTag, "end_pos", new ListTag());
               if (list.isEmpty()) {
                  list.add(DoubleTag.m_128500_(0.0));
                  list.add(DoubleTag.m_128500_(0.0));
                  list.add(DoubleTag.m_128500_(0.0));
               }

               double d = StringUtil.isNullOrEmpty(input) ? 0.0 : ParseUtil.parseOrGet(input, Double::valueOf, 0.0);
               list.remove(1);
               list.add(1, DoubleTag.m_128500_(d));
               TrailInfo trailInfo = TrailInfo.deserialize(trailTag);
               this.modelPreviewer.setTrailInfo(trailInfo);
            });
            endZ.m_94151_(input -> {
               CompoundTag trailTag = this.packList.get(this.packListGrid.getRowposition()).getValue().m_128469_("trail");
               ListTag list = ParseUtil.getOrDefaultTag(trailTag, "end_pos", new ListTag());
               if (list.isEmpty()) {
                  list.add(DoubleTag.m_128500_(0.0));
                  list.add(DoubleTag.m_128500_(0.0));
                  list.add(DoubleTag.m_128500_(0.0));
               }

               double d = StringUtil.isNullOrEmpty(input) ? 0.0 : ParseUtil.parseOrGet(input, Double::valueOf, 0.0);
               list.remove(2);
               list.add(2, DoubleTag.m_128500_(d));
               TrailInfo trailInfo = TrailInfo.deserialize(trailTag);
               this.modelPreviewer.setTrailInfo(trailInfo);
            });
            endX.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsableAllowingMinus(context, Double::parseDouble));
            endY.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsableAllowingMinus(context, Double::parseDouble));
            endZ.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsableAllowingMinus(context, Double::parseDouble));
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(20),
                     80,
                     0,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.item_capability.end_pos"
                  )
               );
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(5),
                     8,
                     0,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     Component.m_237113_("X: ")
                  )
               );
            this.inputComponentsList.addComponentCurrentRow(endX.relocateX(rect, this.inputComponentsList.nextStart(4)));
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(8),
                     8,
                     0,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     Component.m_237113_("Y: ")
                  )
               );
            this.inputComponentsList.addComponentCurrentRow(endY.relocateX(rect, this.inputComponentsList.nextStart(4)));
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(8),
                     8,
                     0,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     Component.m_237113_("Z: ")
                  )
               );
            this.inputComponentsList.addComponentCurrentRow(endZ.relocateX(rect, this.inputComponentsList.nextStart(4)));
            ResizableEditBox lifetime = new ResizableEditBox(
               font, 0, 30, 0, 15, Component.m_237115_("datapack_edit.item_capability.trail.lifetime"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
            );
            ResizableEditBox interpolation = new ResizableEditBox(
               font,
               0,
               30,
               0,
               15,
               Component.m_237115_("datapack_edit.item_capability.trail.interpolation"),
               ResizableComponent.HorizontalSizing.LEFT_WIDTH,
               null
            );
            lifetime.m_94151_(input -> {
               int i = StringUtil.isNullOrEmpty(input) ? 0 : Integer.valueOf(input);
               CompoundTag trailTag = this.packList.get(this.packListGrid.getRowposition()).getValue().m_128469_("trail");
               trailTag.m_128365_("lifetime", IntTag.m_128679_(i));
               TrailInfo trailInfo = TrailInfo.deserialize(trailTag);
               this.modelPreviewer.setTrailInfo(trailInfo);
            });
            lifetime.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Integer::parseInt));
            interpolation.m_94151_(input -> {
               int i = StringUtil.isNullOrEmpty(input) ? 0 : Integer.valueOf(input);
               CompoundTag trailTag = this.packList.get(this.packListGrid.getRowposition()).getValue().m_128469_("trail");
               trailTag.m_128365_("interpolations", IntTag.m_128679_(i));
               TrailInfo trailInfo = TrailInfo.deserialize(trailTag);
               this.modelPreviewer.setTrailInfo(trailInfo);
            });
            interpolation.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Integer::parseInt));
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(20),
                     80,
                     0,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.item_capability.lifetime"
                  )
               );
            this.inputComponentsList.addComponentCurrentRow(lifetime.relocateX(rect, this.inputComponentsList.nextStart(8)));
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(20),
                     80,
                     0,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.item_capability.interpolations"
                  )
               );
            this.inputComponentsList.addComponentCurrentRow(interpolation.relocateX(rect, this.inputComponentsList.nextStart(8)));
            ResizableEditBox texturePath = new ResizableEditBox(
               font, 0, 15, 0, 15, Component.m_237115_("datapack_edit.item_capability.trail.end_pos.z"), ResizableComponent.HorizontalSizing.LEFT_RIGHT, null
            );
            texturePath.m_94151_(input -> {
               CompoundTag trailTag = ParseUtil.getOrDefaultTag(this.packList.get(this.packListGrid.getRowposition()).getValue(), "trail", new CompoundTag());
               trailTag.m_128359_("texture_path", ResourceLocation.parse(input).toString());
               TrailInfo trailInfo = TrailInfo.deserialize(trailTag);
               this.modelPreviewer.setTrailInfo(trailInfo);
            });
            texturePath.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ResourceLocation.m_135830_(context));
            texturePath.m_94199_(100);
            texturePath.m_94144_("epicfight:textures/particle/swing_trail.png");
            texturePath.m_94198_();
            PopupBox<ParticleType<?>> particlePopup = new PopupBox.RegistryPopupBox<>(
               DatapackEditScreen.this,
               font,
               0,
               15,
               130,
               15,
               ResizableComponent.HorizontalSizing.LEFT_RIGHT,
               null,
               Component.m_237115_("datapack_edit.weapon_type.hit_particle"),
               ForgeRegistries.PARTICLE_TYPES,
               pair -> {
                  CompoundTag trailTag = ParseUtil.getOrDefaultTag(this.packList.get(this.packListGrid.getRowposition()).getValue(), "trail", new CompoundTag());
                  trailTag.m_128359_("particle_type", ParseUtil.getRegistryName((ParticleType)pair.getSecond(), ForgeRegistries.PARTICLE_TYPES));
                  TrailInfo trailInfo = TrailInfo.deserialize(trailTag);
                  this.modelPreviewer.setTrailInfo(trailInfo);
               }
            );
            particlePopup._setValue((ParticleType<?>)EpicFightParticles.SWING_TRAIL.get());
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(20),
                     80,
                     0,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.item_capability.texture_path"
                  )
               );
            this.inputComponentsList.addComponentCurrentRow(texturePath.relocateX(rect, this.inputComponentsList.nextStart(8)));
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(20),
                     80,
                     0,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.item_capability.particle_type"
                  )
               );
            this.inputComponentsList.addComponentCurrentRow(particlePopup.relocateX(rect, this.inputComponentsList.nextStart(8)));
            this.inputComponentsList.newRow();
            this.inputComponentsList.newRow();
            this.inputComponentsList.newRow();
            this.inputComponentsList.addComponentCurrentRow(this.modelPreviewer.relocateX(rect, this.inputComponentsList.nextStart(8)));
            this.inputComponentsList.newRow();
            this.inputComponentsList.newRow();
         } else if (itemType == DatapackEditScreen.ItemCapabilityTab.ItemType.ARMOR) {
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     DatapackEditScreen.this.parentScreen,
                     this.inputComponentsList.nextStart(4),
                     100,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.item_capability.attributes"
                  )
               );
            this.inputComponentsList
               .addComponentCurrentRow(
                  SubScreenOpenButton.builder()
                     .subScreen(
                        () -> new WeaponAttributeScreen(
                           DatapackEditScreen.this,
                           ParseUtil.getOrDefaultTag(this.packList.get(this.packListGrid.getRowposition()).getValue(), "attributes", new CompoundTag()),
                           itemType
                        )
                     )
                     .bounds(this.inputComponentsList.nextStart(4), 0, 15, 15)
                     .build()
               );
         }

         this.inputComponentsList.m_93507_(164);
      }

      @Override
      public void packGridRowpositionChanged(int rowposition, Map<String, Object> values) {
         CompoundTag tag = this.packList.get(rowposition).getValue();
         this.inputComponentsList.importTag(tag);
         ResourceLocation rl = ResourceLocation.parse(ParseUtil.nullParam(values.get("pack_item")));
         if (this.registry.containsKey(rl)) {
            this.modelPreviewer.setItemToRender((Item)this.registry.getValue(rl));
         }

         Function<Item, CapabilityItem.Builder> builderProvider = DatapackEditScreen.getWeaponType(tag.m_128461_("type"));
         if (builderProvider != null) {
            CapabilityItem.Builder builder = builderProvider.apply((Item)this.registry.getValue(this.packList.get(rowposition).getKey()));
            if (builder instanceof WeaponCapability.Builder weaponBuilder) {
               this.modelPreviewer.clearAnimations();
               List<AssetAccessor<? extends StaticAnimation>> allAnimations = weaponBuilder.getComboAnimations()
                  .entrySet()
                  .stream()
                  .reduce(Lists.newArrayList(), (list, entry) -> {
                     list.addAll(entry.getValue());
                     return list;
                  }, (list1, list2) -> {
                     list1.addAll(list2);
                     return list1;
                  });
               allAnimations.stream().forEach(this.modelPreviewer::addAnimationToPlay);
               boolean hasCustomCollider = false;
               if (tag.m_128441_("collider")) {
                  try {
                     ColliderPreset.deserializeSimpleCollider(tag.m_128469_("collider"));
                     hasCustomCollider = true;
                  } catch (IllegalArgumentException var11) {
                  }
               }

               if (!hasCustomCollider) {
                  this.modelPreviewer.setCollider(weaponBuilder.getCollider());
               }
            }
         }
      }

      @Override
      public void validateBeforeExport() {
         for (PackEntry<ResourceLocation, CompoundTag> packEntry : this.packList) {
            try {
               if (!packEntry.getValue().m_128441_("item_type")) {
                  throw new NoSuchElementException("Item type undefined!");
               }

               Item item = (Item)ForgeRegistries.ITEMS.getValue(packEntry.getKey());
               DatapackEditScreen.ItemCapabilityTab.ItemType itemType = DatapackEditScreen.ItemCapabilityTab.ItemType.valueOf(
                  packEntry.getValue().m_128461_("item_type")
               );
               if (itemType == DatapackEditScreen.ItemCapabilityTab.ItemType.WEAPON && !packEntry.getValue().m_128441_("type")) {
                  throw new IllegalStateException("Weapon type undefined!");
               }

               if (itemType == DatapackEditScreen.ItemCapabilityTab.ItemType.WEAPON) {
                  ItemCapabilityReloadListener.deserializeWeapon(item, packEntry.getValue(), DatapackEditScreen.this);
               } else {
                  ItemCapabilityReloadListener.deserializeArmor(item, packEntry.getValue());
               }
            } catch (Exception e) {
               e.printStackTrace();
               throw new IllegalStateException("Failed to export item capability " + packEntry.getKey() + " :\n" + e.getMessage());
            }
         }
      }

      @Override
      public void importEntries(PackResources packResources) {
         packResources.m_5698_(PackType.SERVER_DATA)
            .stream()
            .distinct()
            .forEach(
               namespace -> packResources.m_8031_(
                  PackType.SERVER_DATA,
                  namespace,
                  this.directory,
                  (resourceLocation, streamSupplier) -> {
                     if (!resourceLocation.toString().contains("/types/")) {
                        try {
                           ResourceLocation registryName = ResourceLocation.fromNamespaceAndPath(
                              resourceLocation.m_135827_(),
                              resourceLocation.m_135815_()
                                 .replaceAll(String.format("%s/%s/", this.directory, DatapackEditScreen.ItemCapabilityTab.ItemType.WEAPON.directoryName), "")
                                 .replaceAll(String.format("%s/%s/", this.directory, DatapackEditScreen.ItemCapabilityTab.ItemType.ARMOR.directoryName), "")
                                 .replaceAll(".json", "")
                           );
                           DatapackEditScreen.ItemCapabilityTab.ItemType itemType = resourceLocation.m_135815_()
                                 .contains(DatapackEditScreen.ItemCapabilityTab.ItemType.WEAPON.directoryName)
                              ? DatapackEditScreen.ItemCapabilityTab.ItemType.WEAPON
                              : DatapackEditScreen.ItemCapabilityTab.ItemType.ARMOR;
                           this.importJson(registryName, itemType, (InputStream)streamSupplier.m_247737_());
                           ResourceLocation itemSkin = ResourceLocation.fromNamespaceAndPath(
                              registryName.m_135827_(), "item_skins/" + registryName.m_135815_() + ".json"
                           );
                           IoSupplier<InputStream> itemSkinStreamSupplier = packResources.m_214146_(PackType.CLIENT_RESOURCES, itemSkin);
                           if (itemSkinStreamSupplier != null) {
                              InputStream itemSkinInputStream = (InputStream)itemSkinStreamSupplier.m_247737_();
                              if (itemSkinInputStream != null) {
                                 JsonReader jsonReader = new JsonReader(new InputStreamReader(itemSkinInputStream, StandardCharsets.UTF_8));
                                 jsonReader.setLenient(true);
                                 CompoundTag comp = null;

                                 try {
                                    comp = TagParser.m_129359_(Streams.parse(jsonReader).getAsJsonObject().toString());
                                 } finally {
                                    jsonReader.close();
                                 }

                                 this.packList.get(this.packList.size() - 1).getValue().m_128365_("trail", comp.m_128423_("trail"));
                              }
                           }
                        } catch (Exception e) {
                           EpicFightMod.LOGGER.info("Failed to import " + resourceLocation + ": " + e.getMessage());
                           e.printStackTrace();
                        }
                     }
                  }
               )
            );
      }

      public void importJson(ResourceLocation registryName, DatapackEditScreen.ItemCapabilityTab.ItemType itemType, InputStream inputStream) throws Exception {
         JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

         try {
            jsonReader.setLenient(true);
            JsonObject jsonObject = Streams.parse(jsonReader).getAsJsonObject();
            CompoundTag compTag = TagParser.m_129359_(jsonObject.toString());
            if (itemType != null) {
               compTag.m_128359_("item_type", itemType.toString());
            }

            if (itemType != DatapackEditScreen.ItemCapabilityTab.ItemType.WEAPON) {
               if (itemType == DatapackEditScreen.ItemCapabilityTab.ItemType.ARMOR && compTag.m_128425_("attributes", 10)) {
                  Map<String, Condition.ParameterEditor> armorAttributeEditors = Maps.newLinkedHashMap();
                  armorAttributeEditors.put(
                     "stun_armor",
                     Condition.ParameterEditor.of(
                        value -> DoubleTag.m_128500_(Double.parseDouble(value.toString())), tag -> ParseUtil.valueOfOmittingType(tag.m_7916_()), null
                     )
                  );
                  armorAttributeEditors.put(
                     "weight",
                     Condition.ParameterEditor.of(
                        value -> DoubleTag.m_128500_(Double.parseDouble(value.toString())), tag -> ParseUtil.valueOfOmittingType(tag.m_7916_()), null
                     )
                  );
                  CompoundTag comp = compTag.m_128469_("attributes");

                  for (Entry<String, Tag> attrEntry : comp.f_128329_.entrySet()) {
                     if (attrEntry.getValue().m_7060_() == 8) {
                        Condition.ParameterEditor paramEditor = armorAttributeEditors.get(attrEntry.getKey());
                        if (ParseUtil.isParsable(attrEntry.getValue().m_7916_(), Double::parseDouble)) {
                           comp.m_128365_(attrEntry.getKey(), paramEditor.toTag.apply(attrEntry.getValue().m_7916_()));
                        } else {
                           comp.m_128473_(attrEntry.getKey());
                        }
                     }
                  }
               }
            } else if (compTag.m_128425_("attributes", 10)) {
               Map<String, Condition.ParameterEditor> weaponAttributeEditors = Maps.newLinkedHashMap();
               weaponAttributeEditors.put(
                  "armor_negation",
                  Condition.ParameterEditor.of(
                     value -> DoubleTag.m_128500_(Double.parseDouble(value.toString())), tag -> ParseUtil.valueOfOmittingType(tag.m_7916_()), null
                  )
               );
               weaponAttributeEditors.put(
                  "impact",
                  Condition.ParameterEditor.of(
                     value -> DoubleTag.m_128500_(Double.parseDouble(value.toString())), tag -> ParseUtil.valueOfOmittingType(tag.m_7916_()), null
                  )
               );
               weaponAttributeEditors.put(
                  "max_strikes",
                  Condition.ParameterEditor.of(
                     value -> IntTag.m_128679_(Integer.parseInt(value.toString())), tag -> ParseUtil.valueOfOmittingType(tag.m_7916_()), null
                  )
               );
               weaponAttributeEditors.put(
                  "damage_bonus",
                  Condition.ParameterEditor.of(
                     value -> DoubleTag.m_128500_(Double.parseDouble(value.toString())), tag -> ParseUtil.valueOfOmittingType(tag.m_7916_()), null
                  )
               );
               weaponAttributeEditors.put(
                  "speed_bonus",
                  Condition.ParameterEditor.of(
                     value -> DoubleTag.m_128500_(Double.parseDouble(value.toString())), tag -> ParseUtil.valueOfOmittingType(tag.m_7916_()), null
                  )
               );
               CompoundTag attrComp = compTag.m_128469_("attributes");

               for (Entry<String, Tag> e : attrComp.f_128329_.entrySet()) {
                  CompoundTag comp = (CompoundTag)e.getValue();

                  for (Entry<String, Tag> attrEntry : comp.f_128329_.entrySet()) {
                     if (attrEntry.getValue().m_7060_() == 8) {
                        Condition.ParameterEditor paramEditor = weaponAttributeEditors.get(attrEntry.getKey());
                        if (ParseUtil.isParsable(attrEntry.getValue().m_7916_(), Double::parseDouble)) {
                           comp.m_128365_(attrEntry.getKey(), paramEditor.toTag.apply(attrEntry.getValue().m_7916_()));
                        } else {
                           comp.m_128473_(attrEntry.getKey());
                        }
                     }
                  }
               }
            }

            this.packList.add(PackEntry.ofValue(registryName, compTag));
            this.packListGrid.addRowWithDefaultValues("pack_item", registryName.toString());
         } catch (Exception e) {
            EpicFightMod.LOGGER.info("Failed to import " + registryName + ": " + e.getMessage());
            throw e;
         } finally {
            try {
               jsonReader.close();
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      }

      @Override
      public void exportEntries(ZipOutputStream out) throws Exception {
         for (PackEntry<ResourceLocation, CompoundTag> packEntry : this.packList) {
            String sItemType = packEntry.getValue().m_128461_("item_type");

            try {
               DatapackEditScreen.ItemCapabilityTab.ItemType itemType = DatapackEditScreen.ItemCapabilityTab.ItemType.valueOf(sItemType);
               packEntry.getValue().m_128473_("item_type");
               ZipEntry zipEntry = new ZipEntry(
                  String.format(
                     "data/%s/" + this.directory + "/%s/%s.json", packEntry.getKey().m_135827_(), itemType.directoryName, packEntry.getKey().m_135815_()
                  )
               );
               Gson gson = new GsonBuilder().setPrettyPrinting().create();
               CompoundTag tag = packEntry.getValue();
               if (tag.m_128441_("trail")) {
                  TrailInfo result = TrailInfo.ANIMATION_DEFAULT_TRAIL.overwrite(TrailInfo.deserialize(tag.m_128469_("trail")));
                  if (result.playable()) {
                     ZipEntry asItemSkin = new ZipEntry(
                        String.format("assets/%s/item_skins/%s.json", packEntry.getKey().m_135827_(), packEntry.getKey().m_135815_())
                     );
                     CompoundTag itemSkinsTag = new CompoundTag();
                     itemSkinsTag.m_128365_("trail", tag.m_128469_("trail"));
                     out.putNextEntry(asItemSkin);
                     out.write(gson.toJson(ParseUtil.convertToJsonObject(itemSkinsTag)).getBytes());
                     out.closeEntry();
                  }

                  tag.m_128473_("trail");
               }

               if (tag.m_128441_("collider")) {
                  try {
                     ColliderPreset.deserializeSimpleCollider(tag.m_128469_("collider"));
                  } catch (Exception e) {
                     tag.m_128473_("collider");
                  }
               }

               out.putNextEntry(zipEntry);
               out.write(gson.toJson(ParseUtil.convertToJsonObject(tag)).getBytes());
               out.closeEntry();
            } catch (Exception e) {
               e.printStackTrace();
               throw new IllegalStateException("Failded to export " + packEntry.getKey() + ". " + e.getMessage());
            } finally {
               packEntry.getValue().m_128359_("item_type", sItemType);
            }
         }
      }

      enum ItemType {
         ARMOR("armors"),
         WEAPON("weapons");

         String directoryName;

         ItemType(String directoryName) {
            this.directoryName = directoryName;
         }
      }
   }

   class MobCapabilityTab extends DatapackEditScreen.DatapackTab<EntityType<?>> {
      private final ModelPreviewer modelPreviewer;
      private final ComboBox<EntityType<?>> presetCombo = new ComboBox<>(
         DatapackEditScreen.this,
         DatapackEditScreen.this.f_96547_,
         0,
         124,
         100,
         15,
         ResizableComponent.HorizontalSizing.LEFT_WIDTH,
         null,
         8,
         Component.m_237115_("datapack_edit.mob_patch.preset"),
         EntityPatchProvider.getPatchedEntities(),
         entityType -> entityType == null ? "none" : EntityType.m_20613_(entityType).toString(),
         null
      );
      private final Consumer<EntityType<?>> presetResponder = entityType -> {
         CompoundTag tag = this.packList.get(this.packListGrid.getRowposition()).getValue();
         if (entityType == null) {
            tag.m_128473_("preset");
         } else {
            tag.m_128359_("preset", EntityType.m_20613_(entityType).toString());
         }

         this.rearrangeComponents(false, entityType != null, tag.m_128471_("isHumanoid"));
         if (entityType == null) {
            this.bindTag(tag);
         }
      };
      private final CheckBox disableCheckBox = new CheckBox(
         DatapackEditScreen.this.f_96547_, 0, 60, 0, 10, ResizableComponent.HorizontalSizing.LEFT_WIDTH, null, false, Component.m_237113_(""), null
      );
      private final Consumer<Boolean> disableResponder = value -> {
         CompoundTag tag = this.packList.get(this.packListGrid.getRowposition()).getValue();
         tag.m_128379_("disabled", value);
         boolean preset = tag.m_128425_("preset", 8) && !StringUtil.isNullOrEmpty(tag.m_128461_("preset"));
         boolean isHumanoid = tag.m_128471_("isHumanoid");
         this.rearrangeComponents(value, preset, isHumanoid);
         if (!value) {
            this.bindTag(tag);
         }
      };
      private final CheckBox isHumanoidCheckbox = new CheckBox(
         DatapackEditScreen.this.f_96547_, 0, 60, 0, 10, ResizableComponent.HorizontalSizing.LEFT_WIDTH, null, false, Component.m_237113_(""), null
      );
      private final Consumer<Boolean> isHumanoidResponder = value -> {
         CompoundTag tag = this.packList.get(this.packListGrid.getRowposition()).getValue();
         tag.m_128379_("isHumanoid", value);
         this.rearrangeComponents(false, false, value);
         this.bindTag(tag);
      };
      private final Map<String, Condition.ParameterEditor> attributeEditors = Maps.newLinkedHashMap();
      private PopupBox.MeshPopupBox meshPopupBox;
      private PopupBox.ArmaturePopupBox armaturePopupBox;

      public MobCapabilityTab() {
         super(
            Component.m_237115_(EpicFightMod.format("gui.%s.tab.datapack.mob_patch")),
            "epicfight_mobpatch",
            ForgeRegistries.ENTITY_TYPES,
            entityType -> entityType.m_20674_() != MobCategory.MISC && entityType != EntityType.f_20565_
         );
         this.inputComponentsList = new InputComponentList<CompoundTag>(DatapackEditScreen.this, 0, 0, 0, 0, 30) {
            public void importTag(CompoundTag tag) {
               boolean disabled = tag.m_128471_("disabled");
               boolean preset = tag.m_128425_("preset", 8) && !StringUtil.isNullOrEmpty(tag.m_128461_("preset"));
               if (!tag.m_128441_("isHumanoid")) {
                  tag.m_128379_("isHumanoid", false);
               }

               boolean isHumanoid = tag.m_128471_("isHumanoid");
               MobCapabilityTab.this.rearrangeComponents(disabled, preset, isHumanoid);
               this.setComponentsActive(true);
               if (preset) {
                  MobCapabilityTab.this.presetCombo._setResponder(null);
                  this.setDataBindingComponenets(new Object[]{EntityType.m_20632_(tag.m_128461_("preset")).orElse(null)});
                  MobCapabilityTab.this.presetCombo._setResponder(MobCapabilityTab.this.presetResponder);
               } else if (!disabled) {
                  MobCapabilityTab.this.bindTag(tag);
               }
            }
         };
         this.modelPreviewer = new ModelPreviewer(9, 15, 0, 140, ResizableComponent.HorizontalSizing.LEFT_RIGHT, null, Armatures.BIPED, Meshes.BIPED);
         this.modelPreviewer.setColliderJoint(Armatures.BIPED.get().toolR);
         this.meshPopupBox = new PopupBox.MeshPopupBox(
            DatapackEditScreen.this,
            DatapackEditScreen.this.f_96547_,
            0,
            15,
            130,
            15,
            ResizableComponent.HorizontalSizing.LEFT_RIGHT,
            null,
            Component.m_237115_("datapack_edit.weapon_type.model"),
            pair -> {
               if (this.armaturePopupBox._getValue() != null
                  && ((AssetAccessor)this.armaturePopupBox._getValue()).isPresent()
                  && pair.getSecond() != null
                  && ((AssetAccessor)pair.getSecond()).isPresent()
                  && ((SkinnedMesh)((AssetAccessor)pair.getSecond()).get()).getMaxJointCount()
                     > ((Armature)((AssetAccessor)this.armaturePopupBox._getValue()).get()).getJointNumber()) {
                  this.meshPopupBox._setValue(null);
                  StringBuilder sb = new StringBuilder();
                  sb.append("[");
                  ((Armature)((AssetAccessor)this.armaturePopupBox._getValue()).get()).rootJoint.iterSubJoints(joint -> sb.append(joint.getName() + ", "));
                  sb.replace(sb.length() - 2, sb.length(), "");
                  sb.append("]");
                  throw new IllegalArgumentException(
                     MessageFormat.format(
                        "The model requires {0} joints, but the armature only has {1}. {2}",
                        ((SkinnedMesh)((AssetAccessor)this.meshPopupBox._getValue()).get()).getMaxJointCount(),
                        ((Armature)((AssetAccessor)this.armaturePopupBox._getValue()).get()).getJointNumber(),
                        sb.toString()
                     )
                  );
               }

               this.packList.get(this.packListGrid.getRowposition()).getValue().m_128359_("model", (String)pair.getFirst());
            }
         );
         this.armaturePopupBox = new PopupBox.ArmaturePopupBox(
            DatapackEditScreen.this,
            DatapackEditScreen.this.f_96547_,
            0,
            15,
            130,
            15,
            ResizableComponent.HorizontalSizing.LEFT_RIGHT,
            null,
            Component.m_237115_("datapack_edit.weapon_type.armature"),
            pair -> {
               if (this.meshPopupBox._getValue() != null
                  && ((AssetAccessor)this.meshPopupBox._getValue()).isPresent()
                  && pair.getSecond() != null
                  && ((AssetAccessor)pair.getSecond()).isPresent()
                  && ((SkinnedMesh)((AssetAccessor)this.meshPopupBox._getValue()).get()).getMaxJointCount()
                     > ((Armature)((AssetAccessor)pair.getSecond()).get()).getJointNumber()) {
                  this.armaturePopupBox._setValue(null);
                  StringBuilder sb = new StringBuilder();
                  sb.append("[");
                  ((Armature)((AssetAccessor)pair.getSecond()).get()).rootJoint.iterSubJoints(joint -> sb.append(joint.getName() + ", "));
                  sb.replace(sb.length() - 2, sb.length(), "");
                  sb.append("]");
                  throw new IllegalArgumentException(
                     MessageFormat.format(
                        "The model requires {0} joints, but the armature only has {1}. {2}",
                        ((SkinnedMesh)((AssetAccessor)this.meshPopupBox._getValue()).get()).getMaxJointCount(),
                        ((Armature)((AssetAccessor)pair.getSecond()).get()).getJointNumber(),
                        sb.toString()
                     )
                  );
               }

               this.packList.get(this.packListGrid.getRowposition()).getValue().m_128359_("armature", (String)pair.getFirst());
            }
         );
         ResizableEditBox impactEditBox = new ResizableEditBox(
            DatapackEditScreen.this.getMinecraft().f_91062_, 0, 0, 0, 0, Component.m_237113_("impact"), null, null
         );
         ResizableEditBox armorNegationEditBox = new ResizableEditBox(
            DatapackEditScreen.this.getMinecraft().f_91062_, 0, 0, 0, 0, Component.m_237113_("armor_negation"), null, null
         );
         ResizableEditBox maxStrikesEditBox = new ResizableEditBox(
            DatapackEditScreen.this.getMinecraft().f_91062_, 0, 0, 0, 0, Component.m_237113_("max_strikes"), null, null
         );
         ResizableEditBox chasingSpeedEditBox = new ResizableEditBox(
            DatapackEditScreen.this.getMinecraft().f_91062_, 0, 0, 0, 0, Component.m_237113_("chasing_speed"), null, null
         );
         ResizableEditBox scaleEditBox = new ResizableEditBox(
            DatapackEditScreen.this.getMinecraft().f_91062_, 0, 0, 0, 0, Component.m_237113_("scale"), null, null
         );
         ResizableEditBox stunArmorBox = new ResizableEditBox(
            DatapackEditScreen.this.getMinecraft().f_91062_, 0, 0, 0, 0, Component.m_237113_("stun_armor"), null, null
         );
         impactEditBox.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsableAllowingMinus(context, Double::parseDouble));
         armorNegationEditBox.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsableAllowingMinus(context, Double::parseDouble));
         maxStrikesEditBox.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsableAllowingMinus(context, Integer::parseInt));
         chasingSpeedEditBox.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsableAllowingMinus(context, Double::parseDouble));
         scaleEditBox.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Double::parseDouble));
         stunArmorBox.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsableAllowingMinus(context, Double::parseDouble));
         this.attributeEditors
            .put(
               "impact",
               Condition.ParameterEditor.of(
                  value -> DoubleTag.m_128500_(Double.parseDouble(value.toString())), tag -> ParseUtil.valueOfOmittingType(tag.m_7916_()), impactEditBox
               )
            );
         this.attributeEditors
            .put(
               "armor_negation",
               Condition.ParameterEditor.of(
                  value -> DoubleTag.m_128500_(Double.parseDouble(value.toString())), tag -> ParseUtil.valueOfOmittingType(tag.m_7916_()), armorNegationEditBox
               )
            );
         this.attributeEditors
            .put(
               "max_strikes",
               Condition.ParameterEditor.of(
                  value -> IntTag.m_128679_(Integer.parseInt(value.toString())), tag -> ParseUtil.valueOfOmittingType(tag.m_7916_()), maxStrikesEditBox
               )
            );
         this.attributeEditors
            .put(
               "chasing_speed",
               Condition.ParameterEditor.of(
                  value -> DoubleTag.m_128500_(Double.parseDouble(value.toString())), tag -> ParseUtil.valueOfOmittingType(tag.m_7916_()), chasingSpeedEditBox
               )
            );
         this.attributeEditors
            .put(
               "scale",
               Condition.ParameterEditor.of(
                  value -> DoubleTag.m_128500_(Double.parseDouble(value.toString())), tag -> ParseUtil.valueOfOmittingType(tag.m_7916_()), scaleEditBox
               )
            );
         this.attributeEditors
            .put(
               "stun_armor",
               Condition.ParameterEditor.of(
                  value -> DoubleTag.m_128500_(Double.parseDouble(value.toString())), tag -> ParseUtil.valueOfOmittingType(tag.m_7916_()), stunArmorBox
               )
            );
      }

      private void rearrangeComponents(boolean disable, boolean usePreset, boolean isHumanoid) {
         Screen parentScreen = DatapackEditScreen.this;
         ScreenRectangle screen = DatapackEditScreen.this.m_264198_();
         this.inputComponentsList.clearComponents();
         if (disable) {
            this.disableCheckBox._setResponder(null);
            this.disableCheckBox._setValue(true);
            this.disableCheckBox._setResponder(this.disableResponder);
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     parentScreen,
                     this.inputComponentsList.nextStart(4),
                     100,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.mob_patch.disabled"
                  )
               );
            this.inputComponentsList.addComponentCurrentRow(this.disableCheckBox.relocateX(screen, this.inputComponentsList.nextStart(5)));
         } else if (usePreset) {
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     parentScreen,
                     this.inputComponentsList.nextStart(4),
                     100,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.mob_patch.preset"
                  )
               );
            this.inputComponentsList.addComponentCurrentRow(this.presetCombo.relocateX(screen, this.inputComponentsList.nextStart(5)));
            this.presetCombo._setResponder(this.presetResponder);
         } else {
            this.disableCheckBox._setResponder(null);
            this.disableCheckBox._setValue(false);
            this.disableCheckBox._setResponder(this.disableResponder);
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     parentScreen,
                     this.inputComponentsList.nextStart(4),
                     100,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.mob_patch.disabled"
                  )
               );
            this.inputComponentsList.addComponentCurrentRow(this.disableCheckBox.relocateX(screen, this.inputComponentsList.nextStart(5)));
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     parentScreen,
                     this.inputComponentsList.nextStart(4),
                     100,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.mob_patch.preset"
                  )
               );
            this.inputComponentsList.addComponentCurrentRow(this.presetCombo.relocateX(screen, this.inputComponentsList.nextStart(5)));
            this.presetCombo._setResponder(this.presetResponder);
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     parentScreen,
                     this.inputComponentsList.nextStart(4),
                     100,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.mob_patch.model"
                  )
               );
            this.inputComponentsList.addComponentCurrentRow(this.meshPopupBox.relocateX(screen, this.inputComponentsList.nextStart(5)));
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     parentScreen,
                     this.inputComponentsList.nextStart(4),
                     100,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.mob_patch.armature"
                  )
               );
            this.inputComponentsList.addComponentCurrentRow(this.armaturePopupBox.relocateX(screen, this.inputComponentsList.nextStart(5)));
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     parentScreen,
                     this.inputComponentsList.nextStart(4),
                     100,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.mob_patch.renderer"
                  )
               );
            this.inputComponentsList
               .addComponentCurrentRow(
                  new PopupBox.RendererPopupBox(
                     parentScreen,
                     DatapackEditScreen.this.f_96547_,
                     this.inputComponentsList.nextStart(5),
                     15,
                     130,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_RIGHT,
                     null,
                     Component.m_237115_("datapack_edit.weapon_type.renderer"),
                     pair -> this.packList.get(this.packListGrid.getRowposition()).getValue().m_128359_("renderer", (String)pair.getFirst())
                  )
               );
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     parentScreen,
                     this.inputComponentsList.nextStart(4),
                     100,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.mob_patch.humanoid"
                  )
               );
            this.inputComponentsList.addComponentCurrentRow(this.isHumanoidCheckbox.relocateX(screen, this.inputComponentsList.nextStart(5)));
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     parentScreen,
                     this.inputComponentsList.nextStart(4),
                     100,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.mob_patch.faction"
                  )
               );
            this.inputComponentsList
               .addComponentCurrentRow(
                  new ComboBox<>(
                     parentScreen,
                     parentScreen.getMinecraft().f_91062_,
                     this.inputComponentsList.nextStart(5),
                     124,
                     100,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     8,
                     Component.m_237115_("datapack_edit.mob_patch.faction"),
                     Faction.ENUM_MANAGER.universalValues(),
                     faction -> ParseUtil.snakeToSpacedCamel(faction),
                     faction -> this.packList
                        .get(this.packListGrid.getRowposition())
                        .getValue()
                        .m_128359_("faction", ParseUtil.nullOrToString(faction, value -> value.toString().toLowerCase(Locale.ROOT)))
                  )
               );
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     parentScreen,
                     this.inputComponentsList.nextStart(4),
                     100,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.mob_patch.swing_sound"
                  )
               );
            this.inputComponentsList
               .addComponentCurrentRow(
                  new PopupBox.SoundPopupBox(
                     parentScreen,
                     parentScreen.getMinecraft().f_91062_,
                     this.inputComponentsList.nextStart(5),
                     15,
                     0,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_RIGHT,
                     null,
                     Component.m_237115_("datapack_edit.mob_patch.swing_sound"),
                     soundevent -> this.packList
                        .get(this.packListGrid.getRowposition())
                        .getValue()
                        .m_128359_("swing_sound", ParseUtil.getRegistryName((SoundEvent)soundevent.getSecond(), ForgeRegistries.SOUND_EVENTS))
                  )
               );
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     parentScreen,
                     this.inputComponentsList.nextStart(4),
                     100,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.mob_patch.hit_sound"
                  )
               );
            this.inputComponentsList
               .addComponentCurrentRow(
                  new PopupBox.SoundPopupBox(
                     parentScreen,
                     parentScreen.getMinecraft().f_91062_,
                     this.inputComponentsList.nextStart(5),
                     15,
                     0,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_RIGHT,
                     null,
                     Component.m_237115_("datapack_edit.mob_patch.hit_sound"),
                     soundevent -> this.packList
                        .get(this.packListGrid.getRowposition())
                        .getValue()
                        .m_128359_("hit_sound", ParseUtil.getRegistryName((SoundEvent)soundevent.getSecond(), ForgeRegistries.SOUND_EVENTS))
                  )
               );
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     parentScreen,
                     this.inputComponentsList.nextStart(4),
                     100,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.mob_patch.hit_particle"
                  )
               );
            this.inputComponentsList
               .addComponentCurrentRow(
                  new PopupBox.RegistryPopupBox(
                     parentScreen,
                     DatapackEditScreen.this.f_96547_,
                     this.inputComponentsList.nextStart(5),
                     15,
                     0,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_RIGHT,
                     null,
                     Component.m_237115_("datapack_edit.weapon_type.hit_particle"),
                     ForgeRegistries.PARTICLE_TYPES,
                     pair -> this.packList
                        .get(this.packListGrid.getRowposition())
                        .getValue()
                        .m_128359_("hit_particle", ParseUtil.getRegistryName((ParticleType)pair.getSecond(), ForgeRegistries.PARTICLE_TYPES))
                  )
               );
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     parentScreen,
                     this.inputComponentsList.nextStart(4),
                     100,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.mob_patch.attributes"
                  )
               );
            this.inputComponentsList.newRow();
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  Grid.builder(parentScreen, parentScreen.getMinecraft())
                     .xy1(this.inputComponentsList.nextStart(5), 0)
                     .xy2(15, 90)
                     .horizontalSizing(ResizableComponent.HorizontalSizing.LEFT_RIGHT)
                     .rowHeight(21)
                     .rowEditable(Grid.GridBuilder.RowEditButton.ADD_REMOVE)
                     .transparentBackground(false)
                     .addColumn(
                        Grid.combo("attribute", List.copyOf(this.attributeEditors.values()))
                           .toDisplayText(
                              editor -> ParseUtil.nullOrToString(editor, editor$1 -> ParseUtil.snakeToSpacedCamel(editor.editWidget.m_6035_().getString()))
                           )
                           .valueChanged(
                              event -> {
                                 CompoundTag attributesTag = ParseUtil.getOrSupply(
                                    this.packList.get(this.packListGrid.getRowposition()).getValue(), "attributes", CompoundTag::new
                                 );
                                 if (event.prevValue != null) {
                                    attributesTag.m_128473_(event.prevValue.editWidget.m_6035_().getString());
                                 } else {
                                    attributesTag.m_128473_("");
                                 }

                                 attributesTag.m_128359_(ParseUtil.nullParam(event.postValue.editWidget.m_6035_().getString()), "");
                              }
                           )
                           .width(100)
                     )
                     .addColumn(
                        Grid.wildcard("amount")
                           .editWidgetProvider(row -> {
                              Condition.ParameterEditor editor = row.getValue("attribute");
                              return editor == null ? null : editor.editWidget;
                           })
                           .valueChanged(
                              event -> {
                                 CompoundTag attributesTag = ParseUtil.getOrSupply(
                                    this.packList.get(this.packListGrid.getRowposition()).getValue(), "attributes", CompoundTag::new
                                 );
                                 Condition.ParameterEditor editor = event.grid.getValue(event.rowposition, "attribute");
                                 if (!StringUtil.isNullOrEmpty(ParseUtil.nullParam(event.postValue))) {
                                    attributesTag.m_128365_(editor.editWidget.m_6035_().getString(), editor.toTag.apply(event.postValue));
                                 } else {
                                    attributesTag.m_128473_(editor.editWidget.m_6035_().getString());
                                 }
                              }
                           )
                           .width(150)
                     )
                     .pressAdd(
                        (grid, button) -> {
                           CompoundTag attributeTag = ParseUtil.getOrDefaultTag(
                              this.packList.get(this.packListGrid.getRowposition()).getValue(), "attributes", new CompoundTag()
                           );
                           attributeTag.m_128359_("", "");
                           int rowposition = grid.addRow();
                           grid.setGridFocus(rowposition, "attribute");
                        }
                     )
                     .pressRemove(
                        (grid, button) -> {
                           int rowposition = grid.getRowposition();
                           if (rowposition > -1) {
                              CompoundTag attributeTag = ParseUtil.getOrDefaultTag(
                                 this.packList.get(this.packListGrid.getRowposition()).getValue(), "attributes", new CompoundTag()
                              );
                              attributeTag.m_128473_(ParseUtil.nullParam(grid.getValue(rowposition, "attribute")));
                              grid.removeRow(rowposition);
                           }
                        }
                     )
                     .build()
               );
            this.inputComponentsList.newRow();
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     parentScreen,
                     this.inputComponentsList.nextStart(4),
                     100,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.mob_patch.default_livingmotions"
                  )
               );
            this.inputComponentsList.newRow();
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  Grid.builder(DatapackEditScreen.this)
                     .xy1(this.inputComponentsList.nextStart(5), 0)
                     .xy2(15, 90)
                     .horizontalSizing(ResizableComponent.HorizontalSizing.LEFT_RIGHT)
                     .rowHeight(21)
                     .rowEditable(Grid.GridBuilder.RowEditButton.ADD_REMOVE)
                     .transparentBackground(false)
                     .addColumn(
                        Grid.combo(
                              "living_motion",
                              List.of(
                                 LivingMotions.IDLE,
                                 LivingMotions.WALK,
                                 LivingMotions.CHASE,
                                 LivingMotions.MOUNT,
                                 LivingMotions.FALL,
                                 LivingMotions.FLOAT,
                                 LivingMotions.DEATH,
                                 LivingMotions.RELOAD,
                                 LivingMotions.AIM
                              )
                           )
                           .valueChanged(
                              event -> {
                                 CompoundTag livingMotionTag = ParseUtil.getOrSupply(
                                    this.packList.get(this.packListGrid.getRowposition()).getValue(), "default_livingmotions", CompoundTag::new
                                 );
                                 livingMotionTag.m_128473_(ParseUtil.nullParam(event.prevValue));
                                 livingMotionTag.m_128359_(
                                    ParseUtil.nullOrToString(event.postValue, livingmotion -> livingmotion.name().toLowerCase(Locale.ROOT)), ""
                                 );
                              }
                           )
                           .editable(true)
                           .width(100)
                     )
                     .addColumn(
                        Grid.popup("animation", PopupBox.AnimationPopupBox::new)
                           .filter(accessor -> !accessor.checkType(MainFrameAnimation.class) || accessor.checkType(LongHitAnimation.class))
                           .editWidgetCreated(
                              popupBox -> popupBox.setModel(
                                 (AssetAccessor<? extends Armature>)this.armaturePopupBox._getValue(),
                                 (AssetAccessor<? extends SkinnedMesh>)this.meshPopupBox._getValue()
                              )
                           )
                           .valueChanged(
                              event -> {
                                 CompoundTag livingMotionTag = ParseUtil.getOrSupply(
                                    this.packList.get(this.packListGrid.getRowposition()).getValue(), "default_livingmotions", CompoundTag::new
                                 );
                                 livingMotionTag.m_128359_(
                                    ParseUtil.nullOrToString(
                                       (LivingMotions)event.grid.getValue(event.rowposition, "living_motion"),
                                       livingmotion -> ((LivingMotions)livingmotion).name().toLowerCase(Locale.ROOT)
                                    ),
                                    ParseUtil.nullOrToString(event.postValue, animation -> animation.registryName().toString())
                                 );
                              }
                           )
                           .toDisplayText(item -> item == null ? "" : item.registryName().toString())
                           .width(150)
                     )
                     .pressAdd(
                        (grid, button) -> {
                           CompoundTag attributeTag = ParseUtil.getOrDefaultTag(
                              this.packList.get(this.packListGrid.getRowposition()).getValue(), "default_livingmotions", new CompoundTag()
                           );
                           attributeTag.m_128359_("", "");
                           int rowposition = grid.addRow();
                           grid.setGridFocus(rowposition, "living_motion");
                        }
                     )
                     .pressRemove(
                        (grid, button) -> {
                           int rowposition = grid.getRowposition();
                           if (rowposition > -1) {
                              CompoundTag livingMotionTag = ParseUtil.getOrDefaultTag(
                                 this.packList.get(this.packListGrid.getRowposition()).getValue(), "default_livingmotions", new CompoundTag()
                              );
                              livingMotionTag.m_128473_(ParseUtil.nullParam(grid.getValue(rowposition, "living_motion")).toLowerCase(Locale.ROOT));
                              grid.removeRow(rowposition);
                           }
                        }
                     )
                     .build()
               );
            this.inputComponentsList.newRow();
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     parentScreen,
                     this.inputComponentsList.nextStart(4),
                     100,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.mob_patch.stun_animations"
                  )
               );
            this.inputComponentsList.newRow();
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  Grid.builder(DatapackEditScreen.this)
                     .xy1(this.inputComponentsList.nextStart(5), 0)
                     .xy2(15, 90)
                     .horizontalSizing(ResizableComponent.HorizontalSizing.LEFT_RIGHT)
                     .rowHeight(21)
                     .rowEditable(Grid.GridBuilder.RowEditButton.ADD_REMOVE)
                     .transparentBackground(false)
                     .addColumn(
                        Grid.combo("stun_type", List.of(StunType.values()))
                           .toDisplayText(stunType -> ParseUtil.nullOrToString(stunType, type -> ParseUtil.snakeToSpacedCamel(type.name())))
                           .valueChanged(
                              event -> {
                                 CompoundTag attributesTag = ParseUtil.getOrSupply(
                                    this.packList.get(this.packListGrid.getRowposition()).getValue(), "stun_animations", CompoundTag::new
                                 );
                                 attributesTag.m_128473_(ParseUtil.nullParam(event.prevValue));
                                 attributesTag.m_128359_(ParseUtil.nullOrToString(event.postValue, stunType -> stunType.name().toLowerCase(Locale.ROOT)), "");
                              }
                           )
                           .editable(true)
                           .width(100)
                     )
                     .addColumn(
                        Grid.popup("animation", PopupBox.AnimationPopupBox::new)
                           .filter(animation -> animation.checkType(HitAnimation.class) || animation.checkType(LongHitAnimation.class))
                           .editWidgetCreated(
                              popupBox -> popupBox.setModel(
                                 (AssetAccessor<? extends Armature>)this.armaturePopupBox._getValue(),
                                 (AssetAccessor<? extends SkinnedMesh>)this.meshPopupBox._getValue()
                              )
                           )
                           .valueChanged(
                              event -> {
                                 CompoundTag stunTypeTag = ParseUtil.getOrSupply(
                                    this.packList.get(this.packListGrid.getRowposition()).getValue(), "stun_animations", CompoundTag::new
                                 );
                                 stunTypeTag.m_128359_(
                                    ParseUtil.nullOrToString(
                                       (StunType)event.grid.getValue(event.rowposition, "stun_type"),
                                       stunType -> ((StunType)stunType).name().toLowerCase(Locale.ROOT)
                                    ),
                                    ParseUtil.nullOrToString(event.postValue, animation -> animation.registryName().toString())
                                 );
                              }
                           )
                           .toDisplayText(item -> item == null ? "" : item.registryName().toString())
                           .width(150)
                     )
                     .pressAdd(
                        (grid, button) -> {
                           CompoundTag attributeTag = ParseUtil.getOrDefaultTag(
                              this.packList.get(this.packListGrid.getRowposition()).getValue(), "stun_animations", new CompoundTag()
                           );
                           attributeTag.m_128359_("", "");
                           int rowposition = grid.addRow();
                           grid.setGridFocus(rowposition, "stun_type");
                        }
                     )
                     .pressRemove(
                        (grid, button) -> {
                           int rowposition = grid.getRowposition();
                           if (rowposition > -1) {
                              CompoundTag stunTypeTag = ParseUtil.getOrDefaultTag(
                                 this.packList.get(this.packListGrid.getRowposition()).getValue(), "stun_animations", new CompoundTag()
                              );
                              stunTypeTag.m_128473_(
                                 ParseUtil.nullOrToString(
                                    (StunType)grid.getValue(rowposition, "stun_type"), stunType -> ((StunType)stunType).name().toLowerCase(Locale.ROOT)
                                 )
                              );
                              grid.removeRow(rowposition);
                           }
                        }
                     )
                     .build()
               );
            this.inputComponentsList.newRow();
            if (isHumanoid) {
               this.inputComponentsList.newRow();
               this.inputComponentsList
                  .addComponentCurrentRow(
                     new Static(
                        parentScreen,
                        this.inputComponentsList.nextStart(4),
                        140,
                        60,
                        15,
                        ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                        null,
                        "datapack_edit.mob_patch.humanoid_weapon_motions"
                     )
                  );
               this.inputComponentsList
                  .addComponentCurrentRow(
                     SubScreenOpenButton.builder()
                        .subScreen(
                           () -> (Screen)(this.armaturePopupBox._getValue() != null && this.meshPopupBox._getValue() != null
                              ? new HumanoidWeaponMotionScreen(
                                 DatapackEditScreen.this,
                                 this.packList.get(this.packListGrid.getRowposition()).getValue(),
                                 (AssetAccessor<? extends Armature>)this.armaturePopupBox._getValue(),
                                 (AssetAccessor<? extends SkinnedMesh>)this.meshPopupBox._getValue()
                              )
                              : new MessageScreen(
                                 "",
                                 "Define model and armature first.",
                                 DatapackEditScreen.this,
                                 button2 -> DatapackEditScreen.this.getMinecraft().m_91152_(DatapackEditScreen.this),
                                 180,
                                 60
                              ))
                        )
                        .bounds(this.inputComponentsList.nextStart(4), 0, 15, 15)
                        .build()
                  );
            }

            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     parentScreen,
                     this.inputComponentsList.nextStart(4),
                     140,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.mob_patch.combat_behavior"
                  )
               );
            this.inputComponentsList
               .addComponentCurrentRow(
                  SubScreenOpenButton.builder()
                     .subScreen(
                        () -> {
                           if (this.armaturePopupBox._getValue() == null || this.meshPopupBox._getValue() == null) {
                              return new MessageScreen(
                                 "",
                                 "Define model and armature first.",
                                 DatapackEditScreen.this,
                                 button2 -> DatapackEditScreen.this.getMinecraft().m_91152_(DatapackEditScreen.this),
                                 180,
                                 60
                              );
                           } else {
                              return (Screen)(isHumanoid
                                 ? new HumanoidCombatBehaviorScreen(
                                    DatapackEditScreen.this,
                                    this.packList.get(this.packListGrid.getRowposition()).getValue(),
                                    (AssetAccessor<? extends Armature>)this.armaturePopupBox._getValue(),
                                    (AssetAccessor<? extends SkinnedMesh>)this.meshPopupBox._getValue()
                                 )
                                 : new CombatBehaviorScreen(
                                    DatapackEditScreen.this,
                                    this.packList.get(this.packListGrid.getRowposition()).getValue(),
                                    (AssetAccessor<? extends Armature>)this.armaturePopupBox._getValue(),
                                    (AssetAccessor<? extends SkinnedMesh>)this.meshPopupBox._getValue(),
                                    false
                                 ));
                           }
                        }
                     )
                     .bounds(this.inputComponentsList.nextStart(4), 0, 15, 15)
                     .build()
               );
         }

         this.inputComponentsList.m_93507_(164);
      }

      private void bindTag(CompoundTag tag) {
         this.presetCombo._setResponder(null);
         this.disableCheckBox._setResponder(null);
         this.isHumanoidCheckbox._setResponder(null);
         Grid.PackImporter attributePackImporter = new Grid.PackImporter();
         Grid.PackImporter livingmotionPackImporter = new Grid.PackImporter();
         Grid.PackImporter stunPackImporter = new Grid.PackImporter();

         for (Entry<String, Tag> attributesTag : tag.m_128469_("attributes").f_128329_.entrySet()) {
            Condition.ParameterEditor editor = this.attributeEditors.get(attributesTag.getKey());
            attributePackImporter.newRow();
            attributePackImporter.newValue("attribute", editor);
            attributePackImporter.newValue("amount", ParseUtil.valueOfOmittingType(attributesTag.getValue().m_7916_()));
         }

         for (Entry<String, Tag> livingmotionTag : tag.m_128469_("default_livingmotions").f_128329_.entrySet()) {
            LivingMotion livingMotion = LivingMotion.ENUM_MANAGER.get(livingmotionTag.getKey().toUpperCase(Locale.ROOT));
            livingmotionPackImporter.newRow();
            livingmotionPackImporter.newValue("living_motion", livingMotion);
            livingmotionPackImporter.newValue("animation", DatapackEditScreen.animationByKey(livingmotionTag.getValue().m_7916_()));
         }

         for (Entry<String, Tag> stunTag : tag.m_128469_("stun_animations").f_128329_.entrySet()) {
            StunType stunType = ParseUtil.enumValueOfOrNull(StunType.class, stunTag.getKey().toUpperCase(Locale.ROOT));
            stunPackImporter.newRow();
            stunPackImporter.newValue("stun_type", stunType);
            stunPackImporter.newValue("animation", DatapackEditScreen.animationByKey(stunTag.getValue().m_7916_()));
         }

         this.inputComponentsList
            .setDataBindingComponenets(
               new Object[]{
                  false,
                  EntityType.m_20632_(tag.m_128461_("preset")).orElse(null),
                  DatapackEditScreen.getMesh(tag.m_128461_("model")),
                  DatapackEditScreen.getArmature(tag.m_128461_("armature")),
                  StringUtil.isNullOrEmpty(tag.m_128461_("renderer")) ? null : ResourceLocation.parse(tag.m_128461_("renderer")),
                  tag.m_128471_("isHumanoid"),
                  ParseUtil.nullOrApply(tag.m_128423_("faction"), jsonElement -> Faction.ENUM_MANAGER.getOrThrow(jsonElement.m_7916_())),
                  ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse(tag.m_128461_("swing_sound"))),
                  ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse(tag.m_128461_("hit_sound"))),
                  ForgeRegistries.PARTICLE_TYPES.getValue(ResourceLocation.parse(tag.m_128461_("hit_particle"))),
                  attributePackImporter,
                  livingmotionPackImporter,
                  stunPackImporter
               }
            );
         this.presetCombo._setResponder(this.presetResponder);
         this.disableCheckBox._setResponder(this.disableResponder);
         this.isHumanoidCheckbox._setResponder(this.isHumanoidResponder);
      }

      @Override
      public void packGridRowpositionChanged(int rowposition, Map<String, Object> values) {
         this.inputComponentsList.importTag(this.packList.get(rowposition).getValue());
      }

      @Override
      public void validateBeforeExport() {
         for (PackEntry<ResourceLocation, CompoundTag> packEntry : this.packList) {
            try {
               Optional<EntityType<?>> type = EntityType.m_20632_(packEntry.getKey().toString());
               if (type.isEmpty()) {
                  throw new IllegalStateException("Invalid entity type");
               }

               MobPatchReloadListener.deserializeMobPatchProvider(
                  type.get(), packEntry.getValue(), true, Minecraft.m_91087_().m_91098_(), DatapackEditScreen.this
               );
            } catch (Exception e) {
               e.printStackTrace();
               throw new IllegalStateException("Failed to export mobpatch " + packEntry.getKey() + " :\n" + e.getMessage());
            }
         }
      }

      @Override
      public void importEntries(PackResources packResources) {
         packResources.m_5698_(PackType.SERVER_DATA)
            .stream()
            .distinct()
            .forEach(
               namespace -> packResources.m_8031_(
                  PackType.SERVER_DATA,
                  namespace,
                  this.directory,
                  (resourceLocation, streamSupplier) -> {
                     ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(
                        resourceLocation.m_135827_(), resourceLocation.m_135815_().replaceAll(String.format("%s/", this.directory), "").replaceAll(".json", "")
                     );

                     try {
                        this.importJson(rl, (InputStream)streamSupplier.m_247737_());
                     } catch (Exception e) {
                        EpicFightMod.LOGGER.info("Failed to import " + resourceLocation + ": " + e.getMessage());
                        e.printStackTrace();
                     }
                  }
               )
            );
      }

      public void importJson(ResourceLocation registryName, InputStream inputStream) throws Exception {
         InputStreamReader reader = new InputStreamReader(inputStream);
         StringBuilder sb = new StringBuilder();
         Exception exception = null;

         try {
            for (int data = reader.read(); data != -1; data = reader.read()) {
               sb.append((char)data);
            }

            CompoundTag compTag = TagParser.m_129359_(sb.toString());
            if (compTag.m_128471_("isHumanoid")) {
               ListTag combatBehaviorList = compTag.m_128437_("combat_behavior", 10);
               if (!combatBehaviorList.isEmpty()) {
                  compTag.m_128473_("combat_behavior");
                  compTag.m_128365_("combat_behavior_humanoid", combatBehaviorList);
               }
            }

            this.packList.add(PackEntry.of(registryName, () -> compTag));
            this.packListGrid.addRowWithDefaultValues("pack_item", registryName.toString());
         } catch (IOException | CommandSyntaxException e) {
            exception = e;
         } finally {
            reader.close();
         }

         if (exception != null) {
            throw exception;
         }
      }

      @Override
      public void exportEntries(ZipOutputStream out) throws Exception {
         for (PackEntry<ResourceLocation, CompoundTag> packEntry : this.packList) {
            try {
               CompoundTag packCompound = packEntry.getValue();
               if (packCompound.m_128471_("disabled")) {
                  packCompound.f_128329_.clear();
                  packCompound.m_128379_("disabled", true);
               } else if (packCompound.m_128441_("preset")) {
                  String preset = packCompound.m_128461_("preset");
                  packCompound.f_128329_.clear();
                  packCompound.m_128359_("preset", preset);
               } else {
                  packCompound.m_128473_("disabled");
               }

               if (packCompound.m_128471_("isHumanoid")) {
                  ListTag humanoidCombatBehavior = packCompound.m_128437_("combat_behavior_humanoid", 10);
                  packCompound.m_128473_("combat_behavior_humanoid");
                  packCompound.m_128365_("combat_behavior", humanoidCombatBehavior);
               }

               ZipEntry zipEntry = new ZipEntry(
                  String.format("data/%s/" + this.directory + "/%s.json", packEntry.getKey().m_135827_(), packEntry.getKey().m_135815_())
               );
               Gson gson = new GsonBuilder().setPrettyPrinting().create();
               out.putNextEntry(zipEntry);
               out.write(gson.toJson(ParseUtil.convertToJsonObject(packCompound)).getBytes());
               out.closeEntry();
            } catch (Exception e) {
               e.printStackTrace();
               throw new IllegalStateException("Failded to export " + packEntry.getKey() + ". " + e.getMessage());
            }
         }
      }
   }

   class WeaponTypeTab extends DatapackEditScreen.DatapackTab<ResourceLocation> {
      private final ModelPreviewer modelPreviewer;

      public WeaponTypeTab() {
         super(Component.m_237115_(EpicFightMod.format("gui.%s.tab.datapack.weapon_type")), "capabilities/weapons/types", null);
         Screen parentScreen = DatapackEditScreen.this;
         Font font = DatapackEditScreen.this.f_96547_;
         ScreenRectangle rect = DatapackEditScreen.this.m_264198_();
         this.modelPreviewer = new ModelPreviewer(9, 15, 0, 140, ResizableComponent.HorizontalSizing.LEFT_RIGHT, null, Armatures.BIPED, Meshes.BIPED);
         this.modelPreviewer.setColliderJoint(Armatures.BIPED.get().searchJointByName("Tool_R"));
         this.inputComponentsList = new InputComponentList<CompoundTag>(DatapackEditScreen.this, 0, 0, 0, 0, 30) {
            public void importTag(CompoundTag tag) {
               CompoundTag colliderTag = ParseUtil.getOrSupply(tag, "collider", CompoundTag::new);
               boolean centerInit = colliderTag.m_128441_("center");
               boolean sizeInit = colliderTag.m_128441_("size");
               if (!centerInit) {
                  ListTag list = new ListTag();
                  list.add(DoubleTag.m_128500_(0.0));
                  list.add(DoubleTag.m_128500_(0.0));
                  list.add(DoubleTag.m_128500_(0.0));
                  colliderTag.m_128365_("center", list);
               }

               if (!sizeInit) {
                  ListTag list = new ListTag();
                  list.add(DoubleTag.m_128500_(0.0));
                  list.add(DoubleTag.m_128500_(0.0));
                  list.add(DoubleTag.m_128500_(0.0));
                  colliderTag.m_128365_("size", list);
               }

               this.setComponentsActive(true);
               Grid.PackImporter packImporter = new Grid.PackImporter();

               for (String key : tag.m_128469_("innate_skills").m_128431_()) {
                  packImporter.newRow();
                  packImporter.newValue("style", Style.ENUM_MANAGER.get(key));
                  packImporter.newValue("skill", SkillManager.getSkill(tag.m_128469_("innate_skills").m_128461_(key)));
               }

               this.setDataBindingComponenets(
                  new Object[]{
                     WeaponCategory.ENUM_MANAGER.get(tag.m_128461_("category")),
                     ForgeRegistries.PARTICLE_TYPES.getValue(ResourceLocation.parse(tag.m_128461_("hit_particle"))),
                     ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse(tag.m_128461_("hit_sound"))),
                     ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse(tag.m_128461_("swing_sound"))),
                     tag.m_128441_("usable_in_offhand") ? tag.m_128471_("usable_in_offhand") : true,
                     null,
                     ParseUtil.nullOrToString(colliderTag.m_128423_("number"), Tag::m_7916_),
                     centerInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(colliderTag.m_128437_("center", 6).get(0), Tag::m_7916_)) : "",
                     centerInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(colliderTag.m_128437_("center", 6).get(1), Tag::m_7916_)) : "",
                     centerInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(colliderTag.m_128437_("center", 6).get(2), Tag::m_7916_)) : "",
                     sizeInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(colliderTag.m_128437_("size", 6).get(0), Tag::m_7916_)) : "",
                     sizeInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(colliderTag.m_128437_("size", 6).get(1), Tag::m_7916_)) : "",
                     sizeInit ? ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(colliderTag.m_128437_("size", 6).get(2), Tag::m_7916_)) : "",
                     packImporter
                  }
               );
            }
         };
         this.inputComponentsList.m_93507_(164);
         this.inputComponentsList.newRow();
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  parentScreen,
                  this.inputComponentsList.nextStart(4),
                  100,
                  60,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  "datapack_edit.weapon_type.category"
               )
            );
         this.inputComponentsList
            .addComponentCurrentRow(
               new ComboBox<>(
                  parentScreen,
                  parentScreen.getMinecraft().f_91062_,
                  this.inputComponentsList.nextStart(5),
                  124,
                  100,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  8,
                  Component.m_237115_("datapack_edit.weapon_type.category"),
                  new ArrayList<>(WeaponCategory.ENUM_MANAGER.universalValues()),
                  ParseUtil::snakeToSpacedCamel,
                  weaponCategory -> this.packList
                     .get(this.packListGrid.getRowposition())
                     .getValue()
                     .m_128359_("category", ParseUtil.nullParam(weaponCategory).toLowerCase(Locale.ROOT))
               )
            );
         this.inputComponentsList.newRow();
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  parentScreen,
                  this.inputComponentsList.nextStart(4),
                  100,
                  60,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  "datapack_edit.weapon_type.hit_particle"
               )
            );
         this.inputComponentsList
            .addComponentCurrentRow(
               new PopupBox.RegistryPopupBox(
                  parentScreen,
                  font,
                  this.inputComponentsList.nextStart(5),
                  15,
                  130,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_RIGHT,
                  null,
                  Component.m_237115_("datapack_edit.weapon_type.hit_particle"),
                  ForgeRegistries.PARTICLE_TYPES,
                  pair -> this.packList
                     .get(this.packListGrid.getRowposition())
                     .getValue()
                     .m_128359_("hit_particle", ParseUtil.getRegistryName((ParticleType)pair.getSecond(), ForgeRegistries.PARTICLE_TYPES))
               )
            );
         this.inputComponentsList.newRow();
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  parentScreen,
                  this.inputComponentsList.nextStart(4),
                  100,
                  60,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  "datapack_edit.weapon_type.hit_sound"
               )
            );
         this.inputComponentsList
            .addComponentCurrentRow(
               new PopupBox.SoundPopupBox(
                  parentScreen,
                  font,
                  this.inputComponentsList.nextStart(5),
                  15,
                  130,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_RIGHT,
                  null,
                  Component.m_237115_("datapack_edit.weapon_type.hit_sound"),
                  pair -> this.packList
                     .get(this.packListGrid.getRowposition())
                     .getValue()
                     .m_128359_("hit_sound", ParseUtil.getRegistryName((SoundEvent)pair.getSecond(), ForgeRegistries.SOUND_EVENTS))
               )
            );
         this.inputComponentsList.newRow();
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  parentScreen,
                  this.inputComponentsList.nextStart(4),
                  100,
                  60,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  "datapack_edit.weapon_type.swing_sound"
               )
            );
         this.inputComponentsList
            .addComponentCurrentRow(
               new PopupBox.SoundPopupBox(
                  parentScreen,
                  font,
                  this.inputComponentsList.nextStart(5),
                  15,
                  130,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_RIGHT,
                  null,
                  Component.m_237115_("datapack_edit.weapon_type.swing_sound"),
                  pair -> this.packList
                     .get(this.packListGrid.getRowposition())
                     .getValue()
                     .m_128359_("swing_sound", ParseUtil.getRegistryName((SoundEvent)pair.getSecond(), ForgeRegistries.SOUND_EVENTS))
               )
            );
         this.inputComponentsList.newRow();
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  parentScreen,
                  this.inputComponentsList.nextStart(4),
                  100,
                  60,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  "datapack_edit.weapon_type.styles"
               )
            );
         this.inputComponentsList
            .addComponentCurrentRow(
               SubScreenOpenButton.builder()
                  .subScreen(() -> new StylesScreen(DatapackEditScreen.this, this.packList.get(this.packListGrid.getRowposition()).getValue()))
                  .bounds(this.inputComponentsList.nextStart(4), 0, 15, 15)
                  .build()
            );
         this.inputComponentsList.newRow();
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  parentScreen,
                  this.inputComponentsList.nextStart(4),
                  100,
                  60,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  "datapack_edit.weapon_type.offhand_usability"
               )
            );
         this.inputComponentsList
            .addComponentCurrentRow(
               new CheckBox(
                  font,
                  this.inputComponentsList.nextStart(4),
                  60,
                  0,
                  10,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  null,
                  Component.m_237113_(""),
                  val -> this.packList.get(this.packListGrid.getRowposition()).getValue().m_128379_("usable_in_offhand", val)
               )
            );
         this.inputComponentsList.newRow();
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  parentScreen,
                  this.inputComponentsList.nextStart(4),
                  100,
                  100,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  "datapack_edit.weapon_type.offhand_visibility"
               )
            );
         this.inputComponentsList
            .addComponentCurrentRow(
               SubScreenOpenButton.builder()
                  .subScreen(() -> new OffhandValidatorScreen(DatapackEditScreen.this, this.packList.get(this.packListGrid.getRowposition()).getValue()))
                  .bounds(this.inputComponentsList.nextStart(4), 0, 15, 15)
                  .build()
            );
         ResizableEditBox colliderCount = new ResizableEditBox(
            font, 0, 40, 0, 15, Component.m_237115_("datapack_edit.collider.count"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
         );
         ResizableEditBox colliderCenterX = new ResizableEditBox(
            font, 0, 35, 0, 15, Component.m_237115_("datapack_edit.collider.center.x"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
         );
         ResizableEditBox colliderCenterY = new ResizableEditBox(
            font, 0, 35, 0, 15, Component.m_237115_("datapack_edit.collider.center.y"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
         );
         ResizableEditBox colliderCenterZ = new ResizableEditBox(
            font, 0, 35, 0, 15, Component.m_237115_("datapack_edit.collider.center.z"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
         );
         ResizableEditBox colliderSizeX = new ResizableEditBox(
            font, 0, 35, 0, 15, Component.m_237115_("datapack_edit.collider.size.x"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
         );
         ResizableEditBox colliderSizeY = new ResizableEditBox(
            font, 0, 35, 0, 15, Component.m_237115_("datapack_edit.collider.size.y"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
         );
         ResizableEditBox colliderSizeZ = new ResizableEditBox(
            font, 0, 35, 0, 15, Component.m_237115_("datapack_edit.collider.size.z"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
         );
         Runnable setColliderToPreviewer = () -> {
            CompoundTag comp = this.packList.get(this.packListGrid.getRowposition()).getValue();
            Collider collider = null;
            if (comp.m_128441_("collider")) {
               try {
                  collider = ColliderPreset.deserializeSimpleCollider(comp.m_128469_("collider"));
               } catch (IllegalArgumentException var4x) {
               }
            }

            this.modelPreviewer.setCollider(collider);
         };
         colliderCount.m_94151_(input -> {
            CompoundTag collider = ParseUtil.getOrDefaultTag(this.packList.get(this.packListGrid.getRowposition()).getValue(), "collider", new CompoundTag());
            if (StringUtil.isNullOrEmpty(input)) {
               collider.m_128473_("number");
            } else {
               collider.m_128365_("number", IntTag.m_128679_(Integer.valueOf(input)));
            }

            setColliderToPreviewer.run();
         });
         colliderCenterX.m_94151_(input -> {
            CompoundTag collider = ParseUtil.getOrDefaultTag(this.packList.get(this.packListGrid.getRowposition()).getValue(), "collider", new CompoundTag());
            ListTag centerVec = collider.m_128437_("center", 6);
            double i = StringUtil.isNullOrEmpty(input) ? 0.0 : ParseUtil.parseOrGet(input, Double::valueOf, 0.0);
            centerVec.remove(0);
            centerVec.add(0, DoubleTag.m_128500_(i));
            setColliderToPreviewer.run();
         });
         colliderCenterY.m_94151_(input -> {
            CompoundTag collider = ParseUtil.getOrDefaultTag(this.packList.get(this.packListGrid.getRowposition()).getValue(), "collider", new CompoundTag());
            ListTag centerVec = collider.m_128437_("center", 6);
            double i = StringUtil.isNullOrEmpty(input) ? 0.0 : ParseUtil.parseOrGet(input, Double::valueOf, 0.0);
            centerVec.remove(1);
            centerVec.add(1, DoubleTag.m_128500_(i));
            setColliderToPreviewer.run();
         });
         colliderCenterZ.m_94151_(input -> {
            CompoundTag collider = ParseUtil.getOrDefaultTag(this.packList.get(this.packListGrid.getRowposition()).getValue(), "collider", new CompoundTag());
            ListTag centerVec = collider.m_128437_("center", 6);
            double i = StringUtil.isNullOrEmpty(input) ? 0.0 : ParseUtil.parseOrGet(input, Double::valueOf, 0.0);
            centerVec.remove(2);
            centerVec.add(2, DoubleTag.m_128500_(i));
            setColliderToPreviewer.run();
         });
         colliderSizeX.m_94151_(input -> {
            CompoundTag collider = ParseUtil.getOrDefaultTag(this.packList.get(this.packListGrid.getRowposition()).getValue(), "collider", new CompoundTag());
            ListTag centerVec = collider.m_128437_("size", 6);
            double i = StringUtil.isNullOrEmpty(input) ? 0.0 : Double.valueOf(input);
            centerVec.remove(0);
            centerVec.add(0, DoubleTag.m_128500_(i));
            setColliderToPreviewer.run();
         });
         colliderSizeY.m_94151_(input -> {
            CompoundTag collider = ParseUtil.getOrDefaultTag(this.packList.get(this.packListGrid.getRowposition()).getValue(), "collider", new CompoundTag());
            ListTag centerVec = collider.m_128437_("size", 6);
            double i = StringUtil.isNullOrEmpty(input) ? 0.0 : Double.valueOf(input);
            centerVec.remove(1);
            centerVec.add(1, DoubleTag.m_128500_(i));
            setColliderToPreviewer.run();
         });
         colliderSizeZ.m_94151_(input -> {
            CompoundTag collider = ParseUtil.getOrDefaultTag(this.packList.get(this.packListGrid.getRowposition()).getValue(), "collider", new CompoundTag());
            ListTag centerVec = collider.m_128437_("size", 6);
            double i = StringUtil.isNullOrEmpty(input) ? 0.0 : Double.valueOf(input);
            centerVec.remove(2);
            centerVec.add(2, DoubleTag.m_128500_(i));
            setColliderToPreviewer.run();
         });
         colliderCount.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Integer::parseInt));
         colliderCenterX.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsableAllowingMinus(context, Double::parseDouble));
         colliderCenterY.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsableAllowingMinus(context, Double::parseDouble));
         colliderCenterZ.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsableAllowingMinus(context, Double::parseDouble));
         colliderSizeX.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Double::parseDouble));
         colliderSizeY.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Double::parseDouble));
         colliderSizeZ.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Double::parseDouble));
         this.inputComponentsList.newRow();
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  parentScreen,
                  this.inputComponentsList.nextStart(4),
                  100,
                  60,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  "datapack_edit.collider"
               )
            );
         this.inputComponentsList
            .addComponentCurrentRow(
               new PopupBox.ColliderPopupBox(
                     parentScreen,
                     font,
                     this.inputComponentsList.nextStart(5),
                     15,
                     130,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_RIGHT,
                     null,
                     Component.m_237115_("datapack_edit.collider"),
                     pair -> {
                        if (pair.getSecond() != null) {
                           CompoundTag colliderTag = ParseUtil.getOrDefaultTag(
                              this.packList.get(this.packListGrid.getRowposition()).getValue(), "collider", new CompoundTag()
                           );
                           ((Collider)pair.getSecond()).serialize(colliderTag);
                           colliderCount.m_94144_(String.valueOf(colliderTag.m_128451_("number")));
                           ListTag centerVec = colliderTag.m_128437_("center", 6);
                           colliderCenterX.m_94144_(String.valueOf(centerVec.m_128772_(0)));
                           colliderCenterY.m_94144_(String.valueOf(centerVec.m_128772_(1)));
                           colliderCenterZ.m_94144_(String.valueOf(centerVec.m_128772_(2)));
                           ListTag sizeVec = colliderTag.m_128437_("size", 6);
                           colliderSizeX.m_94144_(String.valueOf(sizeVec.m_128772_(0)));
                           colliderSizeY.m_94144_(String.valueOf(sizeVec.m_128772_(1)));
                           colliderSizeZ.m_94144_(String.valueOf(sizeVec.m_128772_(2)));
                        } else if (this.packList.get(this.packListGrid.getRowposition()).getValue().m_128441_("collider")) {
                           colliderCount.m_94144_("");
                           colliderCenterX.m_94144_("");
                           colliderCenterY.m_94144_("");
                           colliderCenterZ.m_94144_("");
                           colliderSizeX.m_94144_("");
                           colliderSizeY.m_94144_("");
                           colliderSizeZ.m_94144_("");
                        }

                        this.modelPreviewer.setCollider((Collider)pair.getSecond());
                     }
                  )
                  .applyFilter(collider -> collider instanceof OBBCollider || collider instanceof MultiOBBCollider)
            );
         this.inputComponentsList.newRow();
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  parentScreen,
                  this.inputComponentsList.nextStart(20),
                  40,
                  60,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  "datapack_edit.collider.count"
               )
            );
         this.inputComponentsList.addComponentCurrentRow(colliderCount.relocateX(rect, this.inputComponentsList.nextStart(5)));
         this.inputComponentsList.newRow();
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  parentScreen,
                  this.inputComponentsList.nextStart(20),
                  40,
                  60,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  "datapack_edit.collider.center"
               )
            );
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  parentScreen,
                  this.inputComponentsList.nextStart(5),
                  8,
                  60,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  Component.m_237113_("X: ")
               )
            );
         this.inputComponentsList.addComponentCurrentRow(colliderCenterX.relocateX(rect, this.inputComponentsList.nextStart(5)));
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  parentScreen,
                  this.inputComponentsList.nextStart(8),
                  8,
                  60,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  Component.m_237113_("Y: ")
               )
            );
         this.inputComponentsList.addComponentCurrentRow(colliderCenterY.relocateX(rect, this.inputComponentsList.nextStart(5)));
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  parentScreen,
                  this.inputComponentsList.nextStart(8),
                  8,
                  60,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  Component.m_237113_("Z: ")
               )
            );
         this.inputComponentsList.addComponentCurrentRow(colliderCenterZ.relocateX(rect, this.inputComponentsList.nextStart(5)));
         this.inputComponentsList.newRow();
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  parentScreen,
                  this.inputComponentsList.nextStart(20),
                  40,
                  60,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  "datapack_edit.collider.size"
               )
            );
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  parentScreen,
                  this.inputComponentsList.nextStart(5),
                  8,
                  60,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  Component.m_237113_("X: ")
               )
            );
         this.inputComponentsList.addComponentCurrentRow(colliderSizeX.relocateX(rect, this.inputComponentsList.nextStart(5)));
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  parentScreen,
                  this.inputComponentsList.nextStart(8),
                  8,
                  60,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  Component.m_237113_("Y: ")
               )
            );
         this.inputComponentsList.addComponentCurrentRow(colliderSizeY.relocateX(rect, this.inputComponentsList.nextStart(5)));
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  parentScreen,
                  this.inputComponentsList.nextStart(8),
                  8,
                  60,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  Component.m_237113_("Z: ")
               )
            );
         this.inputComponentsList.addComponentCurrentRow(colliderSizeZ.relocateX(rect, this.inputComponentsList.nextStart(5)));
         this.inputComponentsList.newRow();
         this.inputComponentsList.newRow();
         this.inputComponentsList.newRow();
         this.inputComponentsList.addComponentCurrentRow(this.modelPreviewer);
         this.inputComponentsList.newRow();
         this.inputComponentsList.newRow();
         this.inputComponentsList.newRow();
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  parentScreen,
                  this.inputComponentsList.nextStart(4),
                  100,
                  60,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  "datapack_edit.weapon_type.combos"
               )
            );
         this.inputComponentsList
            .addComponentCurrentRow(
               SubScreenOpenButton.builder()
                  .subScreen(() -> new WeaponComboScreen(DatapackEditScreen.this, this.packList.get(this.packListGrid.getRowposition()).getValue()))
                  .bounds(this.inputComponentsList.nextStart(4), 0, 15, 15)
                  .build()
            );
         this.inputComponentsList.newRow();
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  parentScreen,
                  this.inputComponentsList.nextStart(4),
                  100,
                  60,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  "datapack_edit.weapon_type.innate_skill"
               )
            );
         this.inputComponentsList.newRow();
         this.inputComponentsList.newRow();
         this.inputComponentsList
            .addComponentCurrentRow(
               Grid.builder(DatapackEditScreen.this)
                  .xy1(this.inputComponentsList.nextStart(5), 0)
                  .xy2(15, 90)
                  .horizontalSizing(ResizableComponent.HorizontalSizing.LEFT_RIGHT)
                  .rowHeight(26)
                  .rowEditable(Grid.GridBuilder.RowEditButton.ADD_REMOVE)
                  .transparentBackground(false)
                  .addColumn(
                     Grid.combo("style", Style.ENUM_MANAGER.universalValues())
                        .valueChanged(
                           event -> {
                              CompoundTag innateSkillsTag = ParseUtil.getOrDefaultTag(
                                 this.packList.get(this.packListGrid.getRowposition()).getValue(), "innate_skills", new CompoundTag()
                              );
                              innateSkillsTag.m_128473_(ParseUtil.nullParam(event.prevValue).toLowerCase(Locale.ROOT));
                              innateSkillsTag.m_128359_(
                                 ParseUtil.nullParam(event.postValue).toLowerCase(Locale.ROOT),
                                 ParseUtil.nullParam(event.grid.getValue(event.rowposition, "skill"))
                              );
                           }
                        )
                        .editable(true)
                        .width(100)
                  )
                  .addColumn(
                     Grid.<Skill>registryPopup("skill", SkillManager.getSkillRegistry())
                        .filter(skill -> skill.getCategory() == SkillCategories.WEAPON_INNATE)
                        .valueChanged(
                           event -> {
                              CompoundTag innateSkillsTag = ParseUtil.getOrDefaultTag(
                                 this.packList.get(this.packListGrid.getRowposition()).getValue(), "innate_skills", new CompoundTag()
                              );
                              innateSkillsTag.m_128359_(
                                 ParseUtil.nullParam(event.grid.getValue(event.rowposition, "style")).toLowerCase(Locale.ROOT),
                                 ParseUtil.nullParam(event.postValue)
                              );
                           }
                        )
                        .toDisplayText(item -> item == null ? "" : item.getRegistryName().toString())
                        .width(150)
                  )
                  .pressAdd((grid, button) -> {
                     grid.setValueChangeEnabled(false);
                     int rowposition = grid.addRow();
                     grid.setGridFocus(rowposition, "style");
                     grid.setValueChangeEnabled(true);
                  })
                  .pressRemove(
                     (grid, button) -> {
                        int rowposition = grid.getRowposition();
                        if (rowposition > -1) {
                           CompoundTag innateSkillsTag = ParseUtil.getOrDefaultTag(
                              this.packList.get(this.packListGrid.getRowposition()).getValue(), "innate_skills", new CompoundTag()
                           );
                           innateSkillsTag.m_128473_(ParseUtil.nullParam(grid.getValue(rowposition, "style")).toLowerCase(Locale.ROOT));
                           grid.removeRow(rowposition);
                        }
                     }
                  )
                  .build()
            );
         this.inputComponentsList.newRow();
         this.inputComponentsList.newRow();
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  parentScreen,
                  this.inputComponentsList.nextStart(4),
                  100,
                  80,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  "datapack_edit.weapon_type.living_animations"
               )
            );
         this.inputComponentsList
            .addComponentCurrentRow(
               SubScreenOpenButton.builder()
                  .subScreen(
                     () -> new LivingAnimationsScreen(
                        DatapackEditScreen.this,
                        ParseUtil.getOrDefaultTag(this.packList.get(this.packListGrid.getRowposition()).getValue(), "livingmotion_modifier", new CompoundTag())
                     )
                  )
                  .bounds(this.inputComponentsList.nextStart(4), 0, 15, 15)
                  .build()
            );
         this.inputComponentsList.setComponentsActive(false);
      }

      @Override
      public void packGridRowpositionChanged(int rowposition, Map<String, Object> values) {
         super.packGridRowpositionChanged(rowposition, values);

         try {
            CompoundTag tag = this.packList.get(rowposition).getValue();
            Collider collider = ColliderPreset.deserializeSimpleCollider(tag.m_128469_("collider"));
            this.modelPreviewer.setCollider(collider);
         } catch (IllegalArgumentException e) {
            this.modelPreviewer.setCollider(null);
         }
      }

      @Override
      public void validateBeforeExport() {
         for (PackEntry<ResourceLocation, CompoundTag> packEntry : this.packList) {
            try {
               WeaponTypeReloadListener.deserializeWeaponCapabilityBuilder(packEntry.getKey(), packEntry.getValue(), DatapackEditScreen.this);
            } catch (Exception e) {
               e.printStackTrace();
               throw new IllegalStateException("Failed to export weapon type " + packEntry.getKey() + " :\n" + e.getMessage());
            }
         }
      }

      @Override
      public void importEntries(PackResources packResources) {
         packResources.m_5698_(PackType.SERVER_DATA)
            .stream()
            .distinct()
            .forEach(
               namespace -> packResources.m_8031_(
                  PackType.SERVER_DATA,
                  namespace,
                  this.directory,
                  (resourceLocation, streamSupplier) -> {
                     try {
                        ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(
                           resourceLocation.m_135827_(), resourceLocation.m_135815_().replaceAll(this.directory + "/", "").replaceAll(".json", "")
                        );
                        this.importJson(rl, (InputStream)streamSupplier.m_247737_());
                     } catch (Exception e) {
                        EpicFightMod.LOGGER.info("Failed to import " + resourceLocation + ": " + e.getMessage());
                        e.printStackTrace();
                     }
                  }
               )
            );
      }

      public void importJson(ResourceLocation registryName, InputStream inputStream) throws Exception {
         JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

         try {
            jsonReader.setLenient(true);
            JsonObject jsonObject = Streams.parse(jsonReader).getAsJsonObject();
            CompoundTag compTag = TagParser.m_129359_(jsonObject.toString());

            try {
               WeaponCapability.Builder builder = WeaponTypeReloadListener.deserializeWeaponCapabilityBuilder(registryName, compTag, DatapackEditScreen.this);
               DatapackEditScreen.this.userWeaponTypes.put(registryName, item -> builder);
            } catch (Exception e) {
               EpicFightMod.LOGGER.warn("Failed to deserialize weapon type from datapack." + registryName + ": " + e.getMessage());
               e.printStackTrace();
            }

            this.packList.add(PackEntry.ofValue(registryName, compTag));
            this.packListGrid.addRowWithDefaultValues("pack_item", registryName.toString());
         } catch (Exception e) {
            EpicFightMod.LOGGER.info("Failed to import " + registryName + ": " + e.getMessage());
            throw e;
         } finally {
            try {
               jsonReader.close();
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      }

      @Override
      public void exportEntries(ZipOutputStream out) throws Exception {
         for (PackEntry<ResourceLocation, CompoundTag> packEntry : this.packList) {
            try {
               ZipEntry zipEntry = new ZipEntry(
                  String.format("data/%s/" + this.directory + "/%s.json", packEntry.getKey().m_135827_(), packEntry.getKey().m_135815_())
               );
               Gson gson = new GsonBuilder().setPrettyPrinting().create();
               out.putNextEntry(zipEntry);
               out.write(gson.toJson(ParseUtil.convertToJsonObject(packEntry.getValue())).getBytes());
               out.closeEntry();
            } catch (Exception e) {
               e.printStackTrace();
               throw new IllegalStateException("Failded to export " + packEntry.getKey() + ". " + e.getMessage());
            }
         }
      }
   }
}
