package yesman.epicfight.client.renderer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard.ColorLogicStateShard;
import net.minecraft.client.renderer.RenderStateShard.CullStateShard;
import net.minecraft.client.renderer.RenderStateShard.DepthTestStateShard;
import net.minecraft.client.renderer.RenderStateShard.EmptyTextureStateShard;
import net.minecraft.client.renderer.RenderStateShard.LayeringStateShard;
import net.minecraft.client.renderer.RenderStateShard.LightmapStateShard;
import net.minecraft.client.renderer.RenderStateShard.LineStateShard;
import net.minecraft.client.renderer.RenderStateShard.OutputStateShard;
import net.minecraft.client.renderer.RenderStateShard.OverlayStateShard;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderStateShard.TexturingStateShard;
import net.minecraft.client.renderer.RenderStateShard.TransparencyStateShard;
import net.minecraft.client.renderer.RenderStateShard.WriteMaskStateShard;
import net.minecraft.client.renderer.RenderType.CompositeRenderType;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.client.renderer.RenderType.OutlineProperty;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Vector4f;
import yesman.epicfight.main.EpicFightMod;

public final class EpicFightRenderTypes extends RenderType {
   private static final BiFunction<ResourceLocation, CullStateShard, RenderType> TRIANGULATED_OUTLINE = Util.m_143821_(
      (texLocation, cullStateShard) -> RenderType.m_173215_(
         EpicFightMod.prefix("outline"),
         DefaultVertexFormat.f_85818_,
         Mode.TRIANGLES,
         256,
         false,
         false,
         CompositeState.m_110628_()
            .m_173292_(f_173077_)
            .m_173290_(new TextureStateShard(texLocation, false, false))
            .m_110661_(cullStateShard)
            .m_110663_(f_110111_)
            .m_110675_(f_110124_)
            .m_110689_(OutlineProperty.IS_OUTLINE)
      )
   );
   private static final Map<String, Map<ResourceLocation, RenderType>> TRIANGLED_RENDERTYPES_BY_NAME_TEXTURE = new HashMap<>();
   private static final Function<RenderType, RenderType> TRIANGULATED_RENDER_TYPES = Util.m_143827_(
      renderType -> {
         if (renderType.m_173186_() == Mode.TRIANGLES) {
            return renderType;
         }

         if (renderType instanceof CompositeRenderType compositeRenderType) {
            Optional<ResourceLocation> cutoutTexture;
            if (compositeRenderType.f_110511_.f_110576_ instanceof TextureStateShard texStateShard) {
               cutoutTexture = texStateShard.f_110328_;
            } else {
               cutoutTexture = Optional.empty();
            }

            if (TRIANGLED_RENDERTYPES_BY_NAME_TEXTURE.containsKey(renderType.f_110133_)) {
               Map<ResourceLocation, RenderType> renderTypesByTexture = TRIANGLED_RENDERTYPES_BY_NAME_TEXTURE.get(renderType.f_110133_);
               if (compositeRenderType.f_110511_.f_110576_ instanceof TextureStateShard) {
                  ResourceLocation texLocation = cutoutTexture.orElse(null);
                  if (renderTypesByTexture.containsKey(texLocation)) {
                     return renderTypesByTexture.get(texLocation);
                  }
               }
            }

            CompositeRenderType triangulatedRenderType = new CompositeRenderType(
               renderType.f_110133_,
               renderType.f_110389_,
               Mode.TRIANGLES,
               renderType.m_110507_(),
               renderType.m_110405_(),
               renderType.f_110393_,
               compositeRenderType.f_110511_
            );
            triangulatedRenderType.f_110513_ = triangulatedRenderType.f_110513_.isEmpty()
               ? triangulatedRenderType.f_110513_
               : cutoutTexture.map(texLocation -> TRIANGULATED_OUTLINE.apply(texLocation, compositeRenderType.f_110511_.f_110582_));
            return triangulatedRenderType;
         } else {
            return renderType;
         }
      }
   );
   protected static final ShaderStateShard PARTICLE_SHADER = new ShaderStateShard(GameRenderer::m_172829_);
   private static final RenderType ENTITY_UI_COLORED = m_173215_(
      EpicFightMod.prefix("ui_color"),
      DefaultVertexFormat.f_85815_,
      Mode.QUADS,
      256,
      true,
      false,
      CompositeState.m_110628_().m_173292_(f_173104_).m_110685_(f_110139_).m_110671_(f_110153_).m_110677_(f_110155_).m_110691_(false)
   );
   private static final Function<ResourceLocation, RenderType> ENTITY_UI_TEXTURE = Util.m_143827_(
      textureLocation -> m_173215_(
         EpicFightMod.prefix("ui_texture"),
         DefaultVertexFormat.f_85817_,
         Mode.QUADS,
         256,
         true,
         false,
         CompositeState.m_110628_()
            .m_173292_(f_173102_)
            .m_173290_(new TextureStateShard(textureLocation, false, false))
            .m_110685_(f_110134_)
            .m_110671_(f_110153_)
            .m_110677_(f_110155_)
            .m_110691_(false)
      )
   );
   private static final RenderType OBB = m_173215_(
      EpicFightMod.prefix("debug_collider"),
      DefaultVertexFormat.f_166851_,
      Mode.LINE_STRIP,
      256,
      false,
      false,
      CompositeState.m_110628_()
         .m_173292_(f_173104_)
         .m_110673_(new LineStateShard(OptionalDouble.empty()))
         .m_110669_(f_110119_)
         .m_110685_(f_110139_)
         .m_110675_(f_110129_)
         .m_110687_(f_110114_)
         .m_110661_(f_110110_)
         .m_110691_(false)
   );
   private static final RenderType DEBUG_QUADS = m_173215_(
      EpicFightMod.prefix("debug_quad"),
      DefaultVertexFormat.f_85815_,
      Mode.QUADS,
      256,
      false,
      false,
      CompositeState.m_110628_().m_173292_(f_173104_).m_110669_(f_110119_).m_110685_(f_110134_).m_110687_(f_110114_).m_110661_(f_110110_).m_110691_(false)
   );
   private static final RenderType GUI_TRIANGLE = m_173215_(
      EpicFightMod.prefix("gui_triangle"),
      DefaultVertexFormat.f_85815_,
      Mode.TRIANGLES,
      256,
      false,
      false,
      CompositeState.m_110628_().m_173292_(f_285573_).m_110685_(f_110139_).m_110663_(f_110113_).m_110691_(false)
   );
   private static final Function<ResourceLocation, RenderType> OVERLAY_MODEL = Util.m_143827_(
      texLocation -> m_173215_(
         EpicFightMod.prefix("overlay_model"),
         DefaultVertexFormat.f_85812_,
         Mode.TRIANGLES,
         256,
         false,
         false,
         CompositeState.m_110628_()
            .m_173292_(f_173066_)
            .m_173290_(new TextureStateShard(texLocation, false, false))
            .m_110687_(f_110115_)
            .m_110661_(f_110110_)
            .m_110663_(f_110112_)
            .m_110685_(f_110139_)
            .m_110671_(f_110152_)
            .m_110691_(false)
      )
   );
   private static final RenderType ENTITY_AFTERIMAGE_WHITE = m_173215_(
      EpicFightMod.prefix("entity_afterimage"),
      DefaultVertexFormat.f_85813_,
      Mode.TRIANGLES,
      256,
      true,
      true,
      CompositeState.m_110628_()
         .m_173292_(PARTICLE_SHADER)
         .m_173290_(new TextureStateShard(EpicFightMod.identifier("textures/common/white.png"), false, false))
         .m_110661_(f_110110_)
         .m_110687_(f_110115_)
         .m_110663_(f_110112_)
         .m_110685_(f_110139_)
         .m_110671_(f_110152_)
         .m_110691_(false)
   );
   private static final RenderType ITEM_AFTERIMAGE_WHITE = m_173215_(
      EpicFightMod.prefix("item_afterimage"),
      DefaultVertexFormat.f_85813_,
      Mode.QUADS,
      256,
      true,
      true,
      CompositeState.m_110628_()
         .m_173292_(PARTICLE_SHADER)
         .m_173290_(new TextureStateShard(EpicFightMod.identifier("textures/common/white.png"), false, false))
         .m_110661_(f_110110_)
         .m_110687_(f_110115_)
         .m_110663_(f_110112_)
         .m_110685_(f_110139_)
         .m_110671_(f_110152_)
         .m_110691_(false)
   );
   private static final Function<ResourceLocation, RenderType> ENTITY_PARTICLE = Util.m_143827_(
      texLocation -> m_173215_(
         EpicFightMod.prefix("entity_particle"),
         DefaultVertexFormat.f_85812_,
         Mode.TRIANGLES,
         256,
         true,
         true,
         CompositeState.m_110628_()
            .m_173292_(f_173066_)
            .m_173290_(new TextureStateShard(texLocation, false, false))
            .m_110687_(f_110115_)
            .m_110663_(f_110112_)
            .m_110685_(f_110139_)
            .m_110661_(f_110110_)
            .m_110671_(f_110152_)
            .m_110691_(false)
      )
   );
   private static final RenderType ITEM_PARTICLE = m_173215_(
      EpicFightMod.prefix("item_particle"),
      DefaultVertexFormat.f_85812_,
      Mode.QUADS,
      256,
      true,
      true,
      CompositeState.m_110628_()
         .m_173292_(f_173066_)
         .m_173290_(new TextureStateShard(InventoryMenu.f_39692_, false, false))
         .m_110687_(f_110115_)
         .m_110663_(f_110112_)
         .m_110685_(f_110139_)
         .m_110661_(f_110110_)
         .m_110671_(f_110152_)
         .m_110691_(false)
   );
   private static final Function<ResourceLocation, RenderType> ENTITY_PARTICLE_STENCIL = Util.m_143827_(
      texLocation -> m_173215_(
         EpicFightMod.prefix("entity_particle_stencil"),
         DefaultVertexFormat.f_85817_,
         Mode.TRIANGLES,
         256,
         false,
         false,
         CompositeState.m_110628_().m_173292_(f_173102_).m_173290_(new TextureStateShard(texLocation, false, false)).m_110687_(f_110116_).m_110691_(false)
      )
   );
   private static final RenderType ITEM_PARTICLE_STENCIL = m_173215_(
      EpicFightMod.prefix("item_particle_stencil"),
      DefaultVertexFormat.f_85817_,
      Mode.QUADS,
      256,
      false,
      false,
      CompositeState.m_110628_()
         .m_173292_(f_173102_)
         .m_173290_(new TextureStateShard(InventoryMenu.f_39692_, false, false))
         .m_110687_(f_110116_)
         .m_110691_(false)
   );
   private static final CompositeRenderType BLOCK_HIGHLIGHT = m_173215_(
      EpicFightMod.prefix("block_highlight"),
      DefaultVertexFormat.f_85811_,
      Mode.QUADS,
      256,
      false,
      true,
      CompositeState.m_110628_()
         .m_173290_(new TextureStateShard(EpicFightMod.identifier("textures/common/white.png"), false, false))
         .m_110671_(f_110152_)
         .m_173292_(f_173108_)
         .m_110685_(f_110139_)
         .m_110663_(f_110112_)
         .m_110691_(false)
   );
   private static final Map<Entity, CompositeRenderType> WORLD_RENDERTYPES_COLORED_GLINT = new HashMap<>();

   public static RenderType makeTriangulated(RenderType renderType) {
      if (renderType.m_173186_() == Mode.TRIANGLES) {
         return renderType;
      } else {
         return (RenderType)(renderType instanceof CompositeRenderType compositeRenderType
            ? new CompositeRenderType(
               renderType.f_110133_,
               renderType.f_110389_,
               Mode.TRIANGLES,
               renderType.m_110507_(),
               renderType.m_110405_(),
               renderType.f_110393_,
               compositeRenderType.f_110511_
            )
            : renderType);
      }
   }

   public static RenderType getTriangulated(RenderType renderType) {
      return TRIANGULATED_RENDER_TYPES.apply(renderType);
   }

   public static void addRenderType(String name, ResourceLocation textureLocation, RenderType renderType) {
      Map<ResourceLocation, RenderType> renderTypesByTexture = TRIANGLED_RENDERTYPES_BY_NAME_TEXTURE.computeIfAbsent(name, k -> Maps.newHashMap());
      renderTypesByTexture.put(textureLocation, renderType);
   }

   private static RenderType replaceTextureShard(ResourceLocation texToReplace, RenderType renderType) {
      if (renderType instanceof CompositeRenderType compositeRenderType && compositeRenderType.f_110511_.f_110576_ instanceof TextureStateShard texStateShard) {
         CompositeState textureReplacedState = new CompositeState(
            new TextureStateShard(texToReplace, texStateShard.f_110329_, texStateShard.f_110330_),
            compositeRenderType.f_110511_.f_173274_,
            compositeRenderType.f_110511_.f_110577_,
            compositeRenderType.f_110511_.f_110581_,
            compositeRenderType.f_110511_.f_110582_,
            compositeRenderType.f_110511_.f_110583_,
            compositeRenderType.f_110511_.f_110584_,
            compositeRenderType.f_110511_.f_110586_,
            compositeRenderType.f_110511_.f_110587_,
            compositeRenderType.f_110511_.f_110588_,
            compositeRenderType.f_110511_.f_110589_,
            compositeRenderType.f_110511_.f_110590_,
            compositeRenderType.f_110511_.f_285566_,
            compositeRenderType.f_110511_.f_110591_
         );
         return new CompositeRenderType(
            renderType.f_110133_,
            renderType.f_110389_,
            compositeRenderType.m_173186_(),
            renderType.m_110507_(),
            renderType.m_110405_(),
            renderType.f_110393_,
            textureReplacedState
         );
      } else {
         return null;
      }
   }

   public static RenderType replaceTexture(ResourceLocation texLocation, RenderType renderType) {
      if (TRIANGLED_RENDERTYPES_BY_NAME_TEXTURE.containsKey(renderType.f_110133_)) {
         Map<ResourceLocation, RenderType> renderTypesByTexture = TRIANGLED_RENDERTYPES_BY_NAME_TEXTURE.get(renderType.f_110133_);
         if (renderTypesByTexture.containsKey(texLocation)) {
            return renderTypesByTexture.get(texLocation);
         }
      }

      RenderType textureReplacedRenderType = replaceTextureShard(texLocation, renderType);
      if (textureReplacedRenderType == null) {
         return renderType;
      }

      Map<ResourceLocation, RenderType> renderTypesByTexture = TRIANGLED_RENDERTYPES_BY_NAME_TEXTURE.computeIfAbsent(
         textureReplacedRenderType.f_110133_, k -> Maps.newHashMap()
      );
      renderTypesByTexture.put(texLocation, textureReplacedRenderType);
      return textureReplacedRenderType;
   }

   public static RenderType entityUIColor() {
      return ENTITY_UI_COLORED;
   }

   public static RenderType entityUITexture(ResourceLocation resourcelocation) {
      return ENTITY_UI_TEXTURE.apply(resourcelocation);
   }

   public static RenderType debugCollider() {
      return OBB;
   }

   public static RenderType m_269166_() {
      return DEBUG_QUADS;
   }

   public static RenderType guiTriangle() {
      return GUI_TRIANGLE;
   }

   public static RenderType overlayModel(ResourceLocation textureLocation) {
      return OVERLAY_MODEL.apply(textureLocation);
   }

   public static RenderType entityAfterimageStencil(ResourceLocation textureLocation) {
      return ENTITY_PARTICLE_STENCIL.apply(textureLocation);
   }

   public static RenderType itemAfterimageStencil() {
      return ITEM_PARTICLE_STENCIL;
   }

   public static RenderType entityAfterimageTranslucent(ResourceLocation textureLocation) {
      return ENTITY_PARTICLE.apply(textureLocation);
   }

   public static RenderType itemAfterimageTranslucent() {
      return ITEM_PARTICLE;
   }

   public static RenderType entityAfterimageWhite() {
      return ENTITY_AFTERIMAGE_WHITE;
   }

   public static RenderType itemAfterimageWhite() {
      return ITEM_AFTERIMAGE_WHITE;
   }

   public static RenderType blockHighlight() {
      return BLOCK_HIGHLIGHT;
   }

   public static void freeUnusedWorldRenderTypes() {
      WORLD_RENDERTYPES_COLORED_GLINT.entrySet().removeIf(entry -> entry.getKey().m_213877_());
   }

   public static void clearWorldRenderTypes() {
      WORLD_RENDERTYPES_COLORED_GLINT.clear();
   }

   public static RenderType coloredGlintWorldRendertype(Entity owner, float r, float g, float b) {
      CompositeRenderType glintRenderType = WORLD_RENDERTYPES_COLORED_GLINT.computeIfAbsent(
         owner,
         k -> m_173215_(
            EpicFightMod.prefix("colored_glint"),
            DefaultVertexFormat.f_85817_,
            Mode.TRIANGLES,
            256,
            false,
            false,
            EpicFightRenderTypes.MutableCompositeState.mutableStateBuilder()
               .setShaderState(f_173079_)
               .setTextureState(new TextureStateShard(EpicFightMod.identifier("textures/entity/overlay/glint_white.png"), true, false))
               .setWriteMaskState(f_110115_)
               .setCullState(f_110110_)
               .setDepthTestState(f_110112_)
               .setTransparencyState(f_110137_)
               .setTexturingState(f_110151_)
               .createCompositeState(false)
         )
      );
      ((EpicFightRenderTypes.MutableCompositeState)glintRenderType.f_110511_).setShaderColor(r, g, b, 1.0F);
      return glintRenderType;
   }

   public static RenderType coloredGlintWorldRendertype(Entity owner, int r, int g, int b) {
      return coloredGlintWorldRendertype(owner, r / 255.0F, g / 255.0F, b / 255.0F);
   }

   private EpicFightRenderTypes() {
      super(null, null, null, -1, false, false, null, null);
   }

   public static class MutableCompositeState extends CompositeState {
      private EpicFightRenderTypes.ShaderColorStateShard shaderColorState = new EpicFightRenderTypes.ShaderColorStateShard(new Vector4f(1.0F));

      public MutableCompositeState(
         EmptyTextureStateShard pTextureState,
         ShaderStateShard pShaderState,
         TransparencyStateShard pTransparencyState,
         DepthTestStateShard pDepthState,
         CullStateShard pCullState,
         LightmapStateShard pLightmapState,
         OverlayStateShard pOverlayState,
         LayeringStateShard pLayeringState,
         OutputStateShard pOutputState,
         TexturingStateShard pTexturingState,
         WriteMaskStateShard pWriteMaskState,
         LineStateShard pLineState,
         ColorLogicStateShard pColorLogicState,
         OutlineProperty pOutlineProperty
      ) {
         super(
            pTextureState,
            pShaderState,
            pTransparencyState,
            pDepthState,
            pCullState,
            pLightmapState,
            pOverlayState,
            pLayeringState,
            pOutputState,
            pTexturingState,
            pWriteMaskState,
            pLineState,
            pColorLogicState,
            pOutlineProperty
         );
         List<RenderStateShard> list = new ArrayList<>(this.f_110592_);
         list.add(this.shaderColorState);
         this.f_110592_ = ImmutableList.copyOf(list);
      }

      public void setShaderColor(int r, int g, int b, int a) {
         this.shaderColorState.setColor(r / 255.0F, g / 255.0F, b / 255.0F, a / 255.0F);
      }

      public void setShaderColor(float r, float g, float b, float a) {
         this.shaderColorState.setColor(r, g, b, a);
      }

      public static EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder mutableStateBuilder() {
         return new EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder();
      }

      public static class MutableCompositeStateBuilder {
         private EmptyTextureStateShard textureState = EpicFightRenderTypes.f_110147_;
         private ShaderStateShard shaderState = EpicFightRenderTypes.f_173096_;
         private TransparencyStateShard transparencyState = EpicFightRenderTypes.f_110134_;
         private DepthTestStateShard depthTestState = EpicFightRenderTypes.f_110113_;
         private CullStateShard cullState = EpicFightRenderTypes.f_110158_;
         private LightmapStateShard lightmapState = EpicFightRenderTypes.f_110153_;
         private OverlayStateShard overlayState = EpicFightRenderTypes.f_110155_;
         private LayeringStateShard layeringState = EpicFightRenderTypes.f_110117_;
         private OutputStateShard outputState = EpicFightRenderTypes.f_110123_;
         private TexturingStateShard texturingState = EpicFightRenderTypes.f_110148_;
         private WriteMaskStateShard writeMaskState = EpicFightRenderTypes.f_110114_;
         private LineStateShard lineState = EpicFightRenderTypes.f_110130_;
         private ColorLogicStateShard colorLogicState = EpicFightRenderTypes.f_285585_;

         public EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder setTextureState(EmptyTextureStateShard pTextureState) {
            this.textureState = pTextureState;
            return this;
         }

         public EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder setShaderState(ShaderStateShard pShaderState) {
            this.shaderState = pShaderState;
            return this;
         }

         public EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder setTransparencyState(TransparencyStateShard pTransparencyState) {
            this.transparencyState = pTransparencyState;
            return this;
         }

         public EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder setDepthTestState(DepthTestStateShard pDepthTestState) {
            this.depthTestState = pDepthTestState;
            return this;
         }

         public EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder setCullState(CullStateShard pCullState) {
            this.cullState = pCullState;
            return this;
         }

         public EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder setLightmapState(LightmapStateShard pLightmapState) {
            this.lightmapState = pLightmapState;
            return this;
         }

         public EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder setOverlayState(OverlayStateShard pOverlayState) {
            this.overlayState = pOverlayState;
            return this;
         }

         public EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder setLayeringState(LayeringStateShard pLayerState) {
            this.layeringState = pLayerState;
            return this;
         }

         public EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder setOutputState(OutputStateShard pOutputState) {
            this.outputState = pOutputState;
            return this;
         }

         public EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder setTexturingState(TexturingStateShard pTexturingState) {
            this.texturingState = pTexturingState;
            return this;
         }

         public EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder setWriteMaskState(WriteMaskStateShard pWriteMaskState) {
            this.writeMaskState = pWriteMaskState;
            return this;
         }

         public EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder setLineState(LineStateShard pLineState) {
            this.lineState = pLineState;
            return this;
         }

         public EpicFightRenderTypes.MutableCompositeState.MutableCompositeStateBuilder setColorLogicState(ColorLogicStateShard pColorLogicState) {
            this.colorLogicState = pColorLogicState;
            return this;
         }

         public EpicFightRenderTypes.MutableCompositeState createCompositeState(boolean pOutline) {
            return this.createCompositeState(pOutline ? OutlineProperty.AFFECTS_OUTLINE : OutlineProperty.NONE);
         }

         public EpicFightRenderTypes.MutableCompositeState createCompositeState(OutlineProperty pOutlineState) {
            return new EpicFightRenderTypes.MutableCompositeState(
               this.textureState,
               this.shaderState,
               this.transparencyState,
               this.depthTestState,
               this.cullState,
               this.lightmapState,
               this.overlayState,
               this.layeringState,
               this.outputState,
               this.texturingState,
               this.writeMaskState,
               this.lineState,
               this.colorLogicState,
               pOutlineState
            );
         }
      }
   }

   public static class ShaderColorStateShard extends RenderStateShard {
      private Vector4f color;

      public ShaderColorStateShard(Vector4f color) {
         super("shader_color", () -> RenderSystem.setShaderColor(color.x, color.y, color.z, color.w), () -> RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F));
         this.color = color;
      }

      public void setColor(float r, float g, float b, float a) {
         this.color.set(r, g, b, a);
      }
   }
}
