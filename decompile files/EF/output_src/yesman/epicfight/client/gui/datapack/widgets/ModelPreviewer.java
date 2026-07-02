package yesman.epicfight.client.gui.datapack.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Keyframe;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.LayerOffAnimation;
import yesman.epicfight.api.animation.types.LinkAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.SoftBodyTranslatable;
import yesman.epicfight.api.client.physics.cloth.ClothSimulatable;
import yesman.epicfight.api.client.physics.cloth.ClothSimulator;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.physics.PhysicsSimulator;
import yesman.epicfight.api.physics.SimulatableObject;
import yesman.epicfight.api.physics.SimulationTypes;
import yesman.epicfight.api.physics.bezier.CubicBezierCurve;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.QuaternionUtils;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.api.utils.math.Vec4f;
import yesman.epicfight.client.particle.AbstractTrailParticle;
import yesman.epicfight.client.particle.AnimationTrailParticle;
import yesman.epicfight.client.renderer.EpicFightShaders;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.StunType;

public class ModelPreviewer extends AbstractWidget implements ResizableComponent {
   private final ModelPreviewer.ModelRenderTarget modelRenderTarget;
   private final List<AssetAccessor<? extends StaticAnimation>> animationsToPlay = Lists.newArrayList();
   private final List<ModelPreviewer.CustomTrailParticle> trailParticles = Lists.newArrayList();
   private final CheckBox showColliderCheckbox = new CheckBox(
      Minecraft.m_91087_().f_91062_, 0, 60, 0, 10, null, null, true, Component.m_237115_("datapack_edit.model_player.collider"), null
   );
   private final CheckBox showItemCheckbox = new CheckBox(
      Minecraft.m_91087_().f_91062_, 0, 40, 0, 10, null, null, true, Component.m_237115_("datapack_edit.model_player.item"), null
   );
   private final CheckBox showTrailCheckbox = new CheckBox(
      Minecraft.m_91087_().f_91062_, 0, 40, 0, 10, null, null, true, Component.m_237115_("datapack_edit.model_player.trail"), null
   );
   private double zoom = -3.0;
   private float xRot = 0.0F;
   private float yRotO = 180.0F;
   private float yRot = 180.0F;
   private float xMove = 0.0F;
   private float yMove = 0.0F;
   private int index;
   private float attackTimeBegin;
   private float attackTimeEnd;
   private ModelPreviewer.FakeEntityPatch entitypatch;
   private ModelPreviewer.NoEntityAnimator animator;
   private AssetAccessor<? extends SkinnedMesh> mesh;
   private AssetAccessor<? extends Armature> armature;
   private ResourceLocation figureTexture;
   private Joint colliderJoint;
   private Collider collider;
   private List<TrailInfo> trailInfoList = Lists.newArrayList();
   private Item item;
   private Vec4f backgroundClearColor;
   private boolean cameraMoveEnabled = true;
   private boolean cameraRotationEnabled = true;
   private boolean zoomingCameraEnabled = true;
   private ClothSimulator clothSimulator;
   private SoftBodyTranslatable cloakMesh;
   private ResourceLocation cloakTexture;
   private Vec3f cloakColor = new Vec3f(1.0F, 1.0F, 1.0F);
   private int x1;
   private int x2;
   private int y1;
   private int y2;
   private final ResizableComponent.HorizontalSizing horizontalSizingOption;
   private final ResizableComponent.VerticalSizing verticalSizingOption;

   public ModelPreviewer(
      int x1,
      int x2,
      int y1,
      int y2,
      ResizableComponent.HorizontalSizing horizontal,
      ResizableComponent.VerticalSizing vertical,
      AssetAccessor<? extends Armature> armature,
      AssetAccessor<? extends SkinnedMesh> mesh
   ) {
      super(x1, y1, x2, y2, Component.m_237113_(""));
      if (armature != null) {
         this.entitypatch = new ModelPreviewer.FakeEntityPatch(armature.get());
         this.animator = new ModelPreviewer.NoEntityAnimator(this.entitypatch);
         this.entitypatch.initAnimator(this.animator);
      }

      this.x1 = x1;
      this.x2 = x2;
      this.y1 = y1;
      this.y2 = y2;
      this.horizontalSizingOption = horizontal;
      this.verticalSizingOption = vertical;
      this.mesh = mesh;
      this.armature = armature;
      this.modelRenderTarget = new ModelPreviewer.ModelRenderTarget();
      this.resize(Minecraft.m_91087_().f_91080_.m_264198_());
      this.modelRenderTarget.m_83931_(0.1552F, 0.1552F, 0.1552F, 1.0F);
      this.modelRenderTarget.m_83954_(Minecraft.f_91002_);
   }

   public void setMesh(AssetAccessor<? extends SkinnedMesh> mesh) {
      this.mesh = mesh;
   }

   public void setFigureTexture(ResourceLocation texture) {
      this.figureTexture = texture;
   }

   public void initCloakInfo(SoftBodyTranslatable cloakMesh, ResourceLocation cloakTexture, ClothSimulator.ClothObjectBuilder builder) {
      this.cloakMesh = cloakMesh;
      this.cloakTexture = cloakTexture;
      if (builder != null) {
         this.clothSimulator = new ClothSimulator();
         this.clothSimulator.runWhen(ClothSimulator.MODELPREVIEWER_CLOAK, cloakMesh, builder, () -> true);
      }
   }

   public void removeCloak() {
      if (this.clothSimulator != null) {
         this.clothSimulator.stop(ClothSimulator.MODELPREVIEWER_CLOAK);
         this.clothSimulator = null;
      }

      this.cloakMesh = null;
      this.cloakTexture = null;
   }

   public void setCloakColor(int colorCode) {
      float b = (colorCode & 0xFF) / 255.0F;
      float g = ((colorCode & 0xFF00) >> 8) / 255.0F;
      float r = ((colorCode & 0xFF0000) >> 16) / 255.0F;
      this.cloakColor = new Vec3f(r, g, b);
   }

   public void setCloakColor(float r, float g, float b) {
      this.cloakColor = new Vec3f(r, g, b);
   }

   public Vec3f getCloakColor() {
      return this.cloakColor;
   }

   public SoftBodyTranslatable getCloakMesh() {
      return this.cloakMesh;
   }

   public void setCloakTexture(ResourceLocation cloakTexture) {
      this.cloakTexture = cloakTexture;
   }

   public ResourceLocation getCloakTexture() {
      return this.cloakTexture;
   }

   public AssetAccessor<? extends Armature> getArmature() {
      return this.armature;
   }

   public AssetAccessor<? extends SkinnedMesh> getMesh() {
      return this.mesh;
   }

   public Animator getAnimator() {
      return this.animator;
   }

   public void setCameraTransform(double zoom, float xRot, float yRot, float xMove, float yMove) {
      this.zoom = zoom;
      this.xRot = xRot;
      this.yRotO = yRot;
      this.yRot = yRot;
      this.xMove = xMove;
      this.yMove = yMove;
   }

   public void setCollider(Collider collider) {
      this.collider = collider;
   }

   public void setCollider(Collider collider, Joint joint) {
      this.collider = collider;
      this.colliderJoint = joint;
   }

   public void setColliderJoint(Joint joint) {
      this.colliderJoint = joint;
   }

   public void setTrailInfo(TrailInfo... trailInfos) {
      this.trailInfoList.clear();

      for (TrailInfo trailInfo : trailInfos) {
         this.trailInfoList.add(trailInfo);
      }
   }

   public void setItemToRender(Item item) {
      this.item = item;
   }

   public void setAttackTimeBegin(float attackTimeBegin) {
      this.attackTimeBegin = attackTimeBegin;
   }

   public void setAttackTimeEnd(float attackTimeEnd) {
      this.attackTimeEnd = attackTimeEnd;
   }

   public void setBackgroundClearColor(Vec4f clearColor) {
      this.backgroundClearColor = clearColor;
   }

   public void enableCameraControll(boolean enable) {
      this.cameraMoveEnabled = enable;
      this.cameraRotationEnabled = enable;
      this.zoomingCameraEnabled = enable;
   }

   public void enableCameraMove(boolean enable) {
      this.cameraMoveEnabled = enable;
   }

   public void enableCameraRotation(boolean enable) {
      this.cameraRotationEnabled = enable;
   }

   public void enableZoomingCamera(boolean enable) {
      this.zoomingCameraEnabled = enable;
   }

   public void addAnimationToPlay(AssetAccessor<? extends StaticAnimation> animation) {
      if (animation != null) {
         if (this.index == -1) {
            this.index = 0;
         }

         this.animationsToPlay.add(animation);
         this.animator.playAnimation(animation, 0.0F);
      }
   }

   public void removeAnimationPlayingAnimation(AssetAccessor<? extends StaticAnimation> animation) {
      this.animationsToPlay.remove(animation);
   }

   public void clearAnimations() {
      this.index = -1;
      this.animationsToPlay.clear();
      this.animator.playAnimation(Animations.EMPTY_ANIMATION, 0.0F);
      this.animator.iterAllLayers(layer -> layer.off(this.animator.getEntityPatch()));
      this.animator.playAnimation(Animations.OFF_ANIMATION_HIGHEST, 0.0F);
      this.animator.playAnimation(Animations.OFF_ANIMATION_MIDDLE, 0.0F);
      this.animator.playAnimation(Animations.OFF_ANIMATION_LOWEST, 0.0F);
   }

   public void restartAnimations() {
      this.index = 0;
      if (this.animationsToPlay.size() == 0) {
         this.index = -1;
      } else {
         this.animator.playAnimation(this.animationsToPlay.get(0), 0.0F);
      }
   }

   @Override
   public void _tick() {
      if (this.animator != null) {
         this.animator.tick();
      }

      this.yRotO = this.yRot;
      this.trailParticles.forEach(trail -> trail.m_5989_());
      this.trailParticles.removeIf(trail -> !trail.m_107276_());
      if (this.entitypatch != null && this.clothSimulator != null) {
         this.clothSimulator.tick(this.entitypatch);
      }
   }

   public boolean m_6375_(double x, double y, int button) {
      if (this.f_93623_ && this.f_93624_ && this.m_7972_(button)) {
         boolean flag = this.m_93680_(x, y);
         if (flag) {
            this.m_7435_(Minecraft.m_91087_().m_91106_());
            if (this.item != null && this.showItemCheckbox.m_6375_(x, y, button)) {
               return true;
            }

            if (!this.trailInfoList.isEmpty() && this.showTrailCheckbox.m_6375_(x, y, button)) {
               return true;
            }

            if (this.collider != null && this.showColliderCheckbox.m_6375_(x, y, button)) {
               return true;
            }

            return true;
         }
      }

      return false;
   }

   public boolean m_7979_(double mouseX, double mouseY, int button, double dx, double dy) {
      if (this.m_5953_(mouseX, mouseY)) {
         if (button == 0) {
            if (this.cameraRotationEnabled) {
               this.xRot = (float)Mth.m_14008_(this.xRot + dy * 2.5, -30.0, 45.0);
               this.yRot = (float)(this.yRot + dx * 2.5);
            }
         } else if (button == 2 && this.cameraMoveEnabled) {
            this.xMove = (float)(this.xMove + (float)dx * 0.015F * -this.zoom);
            this.yMove = (float)(this.yMove + -((float)dy) * 0.015F * -this.zoom);
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean m_6050_(double x, double y, double amount) {
      if (this.zoomingCameraEnabled) {
         this.zoom = Mth.m_14008_(this.zoom + amount * 0.5, -20.0, -0.5);
         return true;
      } else {
         return false;
      }
   }

   protected void renderFigure(GuiGraphics guiGraphics, Tesselator tesselator, BufferBuilder bufferbuilder, float partialTicks) {
      if (this.mesh != null && this.mesh.get() != null) {
         if (this.animator != null) {
            Pose pose = this.animator.getPose(partialTicks);
            this.mesh.get().initialize();
            OpenMatrix4f[] poseMatrices = this.entitypatch.getArmature().getPoseAsTransformMatrix(pose, false);
            if (this.figureTexture != null) {
               RenderSystem.setShader(GameRenderer::m_172838_);
               RenderSystem.setShaderTexture(0, this.figureTexture);
               bufferbuilder.m_166779_(Mode.TRIANGLES, DefaultVertexFormat.f_85822_);
               this.mesh
                  .get()
                  .drawPosed(
                     guiGraphics.m_280168_(),
                     bufferbuilder,
                     Mesh.DrawingFunction.POSITION_TEX_COLOR_NORMAL,
                     -1,
                     0.9411F,
                     0.9411F,
                     0.9411F,
                     1.0F,
                     -1,
                     this.entitypatch.getArmature(),
                     poseMatrices
                  );
            } else {
               RenderSystem.setShader(EpicFightShaders::getPositionColorNormalShader);
               bufferbuilder.m_166779_(Mode.TRIANGLES, DefaultVertexFormat.f_166851_);
               this.mesh
                  .get()
                  .drawPosed(
                     guiGraphics.m_280168_(),
                     bufferbuilder,
                     Mesh.DrawingFunction.POSITION_COLOR_NORMAL,
                     -1,
                     0.9411F,
                     0.9411F,
                     0.9411F,
                     1.0F,
                     -1,
                     this.entitypatch.getArmature(),
                     poseMatrices
                  );
            }

            BufferUploader.m_231202_(bufferbuilder.m_231175_());
            if (this.item != null && this.showItemCheckbox._getValue()) {
               BufferSource bufferSource = Minecraft.m_91087_().m_91269_().m_110104_();
               ItemRenderer itemRenderer = Minecraft.m_91087_().m_91291_();
               ItemStack itemstack = new ItemStack(this.item);
               OpenMatrix4f correction = new OpenMatrix4f().translate(0.0F, 0.0F, -0.13F).rotateDeg(-90.0F, Vec3f.X_AXIS);
               OpenMatrix4f handTransform = correction.mulFront(
                  this.entitypatch.getArmature().getBoundTransformFor(pose, this.getArmature().get().searchJointByName("Tool_R"))
               );
               guiGraphics.m_280168_().m_85836_();
               MathUtils.mulStack(guiGraphics.m_280168_(), handTransform);
               BakedModel model = itemRenderer.m_115103_().m_109394_(this.item);
               BakedModel overridedModel = model.m_7343_().m_173464_(model, itemstack, null, null, 0);
               DynamicTexture light = Minecraft.m_91087_().f_91063_.m_109154_().f_109870_;
               light.m_117991_().m_84988_(0, 0, -1);
               light.m_117985_();
               itemRenderer.m_115143_(
                  itemstack,
                  ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                  false,
                  guiGraphics.m_280168_(),
                  bufferSource,
                  0,
                  OverlayTexture.f_118083_,
                  overridedModel
               );
               bufferSource.m_109911_();
               guiGraphics.m_280168_().m_85849_();
            }

            if (!this.trailParticles.isEmpty() && this.showTrailCheckbox._getValue()) {
               RenderSystem.setShader(GameRenderer::m_172829_);
               DynamicTexture light = Minecraft.m_91087_().f_91063_.m_109154_().f_109870_;
               light.m_117991_().m_84988_(0, 0, -1);
               light.m_117985_();

               for (ModelPreviewer.CustomTrailParticle trail : this.trailParticles) {
                  ParticleRenderType particleRendertype = trail.m_7556_();
                  particleRendertype.m_6505_(bufferbuilder, Minecraft.m_91087_().f_90987_);
                  trail.m_5744_(bufferbuilder, null, partialTicks);
                  particleRendertype.m_6294_(tesselator);
               }
            }

            if (this.collider != null && this.showColliderCheckbox._getValue()) {
               RenderType renderType = this.collider.getRenderType();
               bufferbuilder.m_166779_(renderType.m_173186_(), renderType.f_110389_);
               RenderSystem.setShader(GameRenderer::m_172757_);
               AnimationPlayer player = this.animator.getPlayerFor(null);
               float elapsedTime = player.getPrevElapsedTime() + (player.getElapsedTime() - player.getPrevElapsedTime()) * partialTicks;
               boolean red = elapsedTime >= this.attackTimeBegin && elapsedTime <= this.attackTimeEnd;
               if (this.colliderJoint != null) {
                  Pose prevPose = this.animator.getPose(0.0F);
                  Pose currentPose = this.animator.getPose(1.0F);
                  this.collider
                     .drawInternal(
                        guiGraphics.m_280168_(),
                        bufferbuilder,
                        this.entitypatch.getArmature(),
                        this.colliderJoint,
                        prevPose,
                        currentPose,
                        partialTicks,
                        red ? -65536 : -1
                     );
               } else {
                  DynamicAnimation animation = player.getAnimation().get();
                  if (animation instanceof AttackAnimation attackanimation) {
                     AttackAnimation.Phase phase = attackanimation.getPhaseByTime(elapsedTime);

                     for (AttackAnimation.JointColliderPair pair : phase.getColliders()) {
                        Pose prevPose = animation.getRawPose(player.getPrevElapsedTime());
                        Pose currentPose = animation.getRawPose(player.getElapsedTime());
                        this.collider
                           .drawInternal(
                              guiGraphics.m_280168_(),
                              bufferbuilder,
                              this.entitypatch.getArmature(),
                              (Joint)pair.getFirst(),
                              prevPose,
                              currentPose,
                              partialTicks,
                              -1
                           );
                     }
                  }
               }

               RenderSystem.lineWidth(3.0F);
               RenderSystem.disableCull();
               BufferUploader.m_231202_(bufferbuilder.m_231175_());
               RenderSystem.lineWidth(1.0F);
               RenderSystem.enableCull();
            }
         } else {
            RenderSystem.setShader(EpicFightShaders::getPositionColorNormalShader);
            this.mesh.get().initialize();
            if (this.figureTexture != null) {
               RenderSystem.setShader(GameRenderer::m_172838_);
               RenderSystem.setShaderTexture(0, this.figureTexture);
               bufferbuilder.m_166779_(Mode.TRIANGLES, DefaultVertexFormat.f_85822_);
               this.mesh
                  .get()
                  .draw(
                     guiGraphics.m_280168_(),
                     bufferbuilder,
                     Mesh.DrawingFunction.POSITION_TEX_COLOR_NORMAL,
                     -1,
                     0.9411F,
                     0.9411F,
                     0.9411F,
                     1.0F,
                     OverlayTexture.f_118083_
                  );
            } else {
               RenderSystem.setShader(EpicFightShaders::getPositionColorNormalShader);
               bufferbuilder.m_166779_(Mode.TRIANGLES, DefaultVertexFormat.f_166851_);
               this.mesh
                  .get()
                  .draw(
                     guiGraphics.m_280168_(),
                     bufferbuilder,
                     Mesh.DrawingFunction.POSITION_COLOR_NORMAL,
                     -1,
                     0.9411F,
                     0.9411F,
                     0.9411F,
                     1.0F,
                     OverlayTexture.f_118083_
                  );
            }

            BufferUploader.m_231202_(bufferbuilder.m_231175_());
         }
      }

      if (this.cloakMesh != null && this.cloakTexture != null) {
         TextureManager textureManager = Minecraft.m_91087_().f_90987_;
         AbstractTexture texture = textureManager.m_118506_(this.cloakTexture);
         if (texture != MissingTextureAtlasSprite.m_118080_()) {
            this.cloakMesh.getOriginalMesh().initialize();
            if (this.animator != null && this.entitypatch != null && this.clothSimulator != null) {
               this.clothSimulator
                  .getRunningObject(ClothSimulator.MODELPREVIEWER_CLOAK)
                  .ifPresent(
                     clothObj -> {
                        Function<Float, OpenMatrix4f> partialColliderTransformProvider = partialFrame -> {
                           Vec3 pos = this.entitypatch.getAccuratePartialLocation(partialFrame);
                           float yRotLerp = this.entitypatch.getAccurateYRot(partialFrame);
                           return OpenMatrix4f.createTranslation((float)pos.f_82479_, (float)pos.f_82480_, (float)pos.f_82481_)
                              .rotateDeg(180.0F - yRotLerp, Vec3f.Y_AXIS);
                        };
                        Pose pose = this.animator.getPose(partialTicks);
                        this.entitypatch.getArmature().setPose(pose);
                        clothObj.tick(
                           this.entitypatch,
                           partialColliderTransformProvider,
                           partialTicks,
                           this.entitypatch.getArmature(),
                           this.entitypatch.getArmature().getPoseMatrices()
                        );
                        RenderSystem.setShader(GameRenderer::m_172838_);
                        RenderSystem.setShaderTexture(0, this.cloakTexture);
                        bufferbuilder.m_166779_(Mode.TRIANGLES, DefaultVertexFormat.f_85822_);
                        guiGraphics.m_280168_().m_85836_();
                        guiGraphics.m_280168_().m_252781_(Axis.f_252436_.m_252977_(this.entitypatch.getYRot() - 180.0F));
                        clothObj.drawPosed(
                           guiGraphics.m_280168_(),
                           bufferbuilder,
                           Mesh.DrawingFunction.POSITION_TEX_COLOR_NORMAL,
                           -1,
                           this.cloakColor.x,
                           this.cloakColor.y,
                           this.cloakColor.z,
                           1.0F,
                           OverlayTexture.f_118083_,
                           this.entitypatch.getArmature(),
                           this.entitypatch.getArmature().getPoseMatrices()
                        );
                        guiGraphics.m_280168_().m_85849_();
                        BufferUploader.m_231202_(bufferbuilder.m_231175_());
                     }
                  );
            } else {
               RenderSystem.setShader(GameRenderer::m_172838_);
               bufferbuilder.m_166779_(Mode.TRIANGLES, DefaultVertexFormat.f_85822_);
               RenderSystem.setShaderTexture(0, this.cloakTexture);
               ((Mesh)this.cloakMesh)
                  .draw(
                     guiGraphics.m_280168_(),
                     bufferbuilder,
                     Mesh.DrawingFunction.POSITION_TEX_COLOR_NORMAL,
                     -1,
                     this.cloakColor.x,
                     this.cloakColor.y,
                     this.cloakColor.z,
                     1.0F,
                     OverlayTexture.f_118083_
                  );
               BufferUploader.m_231202_(bufferbuilder.m_231175_());
            }
         }
      }
   }

   public void m_87963_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      Minecraft minecraft = Minecraft.m_91087_();
      minecraft.m_91385_().m_83970_();
      RenderSystem.enableDepthTest();
      ScreenRectangle screenrectangle = null;
      boolean scissorApplied = guiGraphics.f_279587_.f_279656_.size() > 0;
      if (scissorApplied) {
         screenrectangle = (ScreenRectangle)guiGraphics.f_279587_.f_279656_.peekLast();
         guiGraphics.m_280618_();
      }

      this.modelRenderTarget.m_83954_(true);
      this.modelRenderTarget.m_83947_(true);
      if (this.backgroundClearColor != null) {
         this.modelRenderTarget.m_83931_(this.backgroundClearColor.x, this.backgroundClearColor.y, this.backgroundClearColor.z, this.backgroundClearColor.w);
      }

      Tesselator tesselator = RenderSystem.renderThreadTesselator();
      BufferBuilder bufferbuilder = tesselator.m_85915_();
      Matrix4f oldProjection = RenderSystem.getProjectionMatrix();
      Matrix4f perspective = new Matrix4f().setPerspective((float)Math.toRadians(50.0), (float)this.f_93618_ / this.f_93619_, 0.05F, 100.0F);
      ShaderInstance prevShader = RenderSystem.getShader();
      RenderSystem.setProjectionMatrix(perspective, VertexSorting.f_276450_);
      RenderSystem.getModelViewStack().m_85836_();
      RenderSystem.getModelViewStack().m_166856_();
      RenderSystem.applyModelViewMatrix();
      guiGraphics.m_280168_().m_85836_();
      guiGraphics.m_280168_().m_85837_(this.xMove, this.yMove - 1.0, this.zoom);
      guiGraphics.m_280168_().m_252781_(Axis.f_252529_.m_252977_(this.xRot));
      guiGraphics.m_280168_().m_252781_(Axis.f_252436_.m_252977_(this.yRot));
      this.renderFigure(guiGraphics, tesselator, bufferbuilder, minecraft.m_91296_());
      guiGraphics.m_280168_().m_85849_();
      RenderSystem.setProjectionMatrix(oldProjection, VertexSorting.f_276633_);
      RenderSystem.getModelViewStack().m_85849_();
      RenderSystem.applyModelViewMatrix();
      RenderSystem.setShader(() -> prevShader);
      this.modelRenderTarget.m_83970_();
      minecraft.m_91385_().m_83947_(true);
      if (scissorApplied) {
         guiGraphics.m_280588_(screenrectangle.m_274563_(), screenrectangle.m_274449_(), screenrectangle.m_274445_(), screenrectangle.m_274349_());
      }

      this.modelRenderTarget.blitToScreen(guiGraphics);
      if (this.animator != null) {
         int top = this._getY() + 6;
         int right = this._getX() + this._getWidth() - 2;
         if (!this.trailInfoList.isEmpty()) {
            right -= this.showTrailCheckbox._getWidth();
            this.showTrailCheckbox._setX(right);
            this.showTrailCheckbox._setY(top);
            this.showTrailCheckbox._renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
         }

         if (this.item != null) {
            right -= this.showItemCheckbox._getWidth();
            this.showItemCheckbox._setX(right);
            this.showItemCheckbox._setY(top);
            this.showItemCheckbox._renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
         }

         if (this.collider != null) {
            right -= this.showColliderCheckbox._getWidth();
            this.showColliderCheckbox._setX(right);
            this.showColliderCheckbox._setY(top);
            this.showColliderCheckbox._renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
         }
      }

      RenderSystem.disableDepthTest();
   }

   protected void m_168797_(NarrationElementOutput narrationElementInput) {
      narrationElementInput.m_169146_(NarratedElementType.TITLE, this.m_5646_());
   }

   @Override
   public void resize(ScreenRectangle screenRectangle) {
      if (this.getHorizontalSizingOption() != null) {
         this.getHorizontalSizingOption().resizeFunction.resize(this, screenRectangle, this.getX1(), this.getX2());
      }

      if (this.getVerticalSizingOption() != null) {
         this.getVerticalSizingOption().resizeFunction.resize(this, screenRectangle, this.getY1(), this.getY2());
      }

      double guiScale = Minecraft.m_91087_().m_91268_().m_85449_();
      this.modelRenderTarget.m_83941_(this._getWidth() * (int)guiScale, this._getHeight() * (int)guiScale, true);
   }

   public void onDestroy() {
      this.modelRenderTarget.m_83930_();
   }

   @Override
   public void setX1(int x1) {
      this.x1 = x1;
   }

   @Override
   public void setX2(int x2) {
      this.x2 = x2;
   }

   @Override
   public void setY1(int y1) {
      this.y1 = y1;
   }

   @Override
   public void setY2(int y2) {
      this.y2 = y2;
   }

   @Override
   public int getX1() {
      return this.x1;
   }

   @Override
   public int getX2() {
      return this.x2;
   }

   @Override
   public int getY1() {
      return this.y1;
   }

   @Override
   public int getY2() {
      return this.y2;
   }

   @Override
   public ResizableComponent.HorizontalSizing getHorizontalSizingOption() {
      return this.horizontalSizingOption;
   }

   @Override
   public ResizableComponent.VerticalSizing getVerticalSizingOption() {
      return this.verticalSizingOption;
   }

   @Override
   public void _setActive(boolean active) {
   }

   @Override
   public int _getX() {
      return this.m_252754_();
   }

   @Override
   public int _getY() {
      return this.m_252907_();
   }

   @Override
   public int _getWidth() {
      return this.m_5711_();
   }

   @Override
   public int _getHeight() {
      return this.m_93694_();
   }

   @Override
   public void _setX(int x) {
      this.m_252865_(x);
   }

   @Override
   public void _setY(int y) {
      this.m_253211_(y);
   }

   @Override
   public void _setWidth(int width) {
      this.m_93674_(width);
   }

   @Override
   public void _setHeight(int height) {
      this.setHeight(height);
   }

   @Override
   public Component _getMessage() {
      return this.m_6035_();
   }

   @Override
   public void _renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      this.m_87963_(guiGraphics, mouseX, mouseY, partialTicks);
   }

   public void sysoutTranslationInfo() {
      System.out.println("x move: " + this.xMove + " y move: " + this.yMove + " x rot: " + this.xRot + " y rot: " + this.yRot + " zoom: " + this.zoom);
   }

   class CustomTrailParticle extends AnimationTrailParticle {
      protected CustomTrailParticle(Joint joint, AssetAccessor<? extends StaticAnimation> animation, TrailInfo trailInfo) {
         super(ModelPreviewer.this.entitypatch.getArmature(), ModelPreviewer.this.entitypatch, joint, animation, trailInfo);
      }

      @Override
      public void m_5989_() {
         AnimationPlayer animPlayer = ModelPreviewer.this.animator.getPlayerFor(null);
         this.trailEdges.removeIf(v -> !v.isAlive());
         if (this.shouldRemove) {
            if (this.f_107225_-- == 0) {
               this.m_107274_();
            }
         } else if (this.animation != animPlayer.getRealAnimation() || animPlayer.getElapsedTime() > this.trailInfo.endTime()) {
            this.shouldRemove = true;
            this.f_107225_ = this.trailInfo.trailLifetime();
         }

         if (!(this.trailInfo.fadeTime() > 0.0F) || !(this.trailInfo.endTime() < animPlayer.getElapsedTime())) {
            boolean isTrailInvisible = animPlayer.getAnimation().get().isLinkAnimation() || animPlayer.getElapsedTime() <= this.trailInfo.startTime();
            boolean isFirstTrail = this.trailEdges.isEmpty();
            boolean needCorrection = !isTrailInvisible && isFirstTrail;
            if (needCorrection) {
               float startCorrection = Math.max(
                  (this.trailInfo.startTime() - animPlayer.getPrevElapsedTime()) / (animPlayer.getElapsedTime() - animPlayer.getPrevElapsedTime()), 0.0F
               );
               this.startEdgeCorrection = this.trailInfo.interpolateCount() * 2 * startCorrection;
            }

            TrailInfo trailInfo = this.trailInfo;
            Pose prevPose = this.owner.<Animator>getAnimator().getPose(0.0F);
            Pose middlePose = this.owner.<Animator>getAnimator().getPose(0.5F);
            Pose currentPose = this.owner.<Animator>getAnimator().getPose(1.0F);
            OpenMatrix4f prevJointTf = ModelPreviewer.this.entitypatch.getArmature().getBoundTransformFor(prevPose, this.joint);
            OpenMatrix4f middleJointTf = ModelPreviewer.this.entitypatch.getArmature().getBoundTransformFor(middlePose, this.joint);
            OpenMatrix4f currentJointTf = ModelPreviewer.this.entitypatch.getArmature().getBoundTransformFor(currentPose, this.joint);
            Vec3 prevStartPos = OpenMatrix4f.transform(prevJointTf, trailInfo.start());
            Vec3 prevEndPos = OpenMatrix4f.transform(prevJointTf, trailInfo.end());
            Vec3 middleStartPos = OpenMatrix4f.transform(middleJointTf, trailInfo.start());
            Vec3 middleEndPos = OpenMatrix4f.transform(middleJointTf, trailInfo.end());
            Vec3 currentStartPos = OpenMatrix4f.transform(currentJointTf, trailInfo.start());
            Vec3 currentEndPos = OpenMatrix4f.transform(currentJointTf, trailInfo.end());
            List<Vec3> finalStartPositions;
            List<Vec3> finalEndPositions;
            boolean visibleTrail;
            if (isTrailInvisible) {
               finalStartPositions = Lists.newArrayList();
               finalEndPositions = Lists.newArrayList();
               finalStartPositions.add(prevStartPos);
               finalStartPositions.add(middleStartPos);
               finalEndPositions.add(prevEndPos);
               finalEndPositions.add(middleEndPos);
               this.invisibleTrailEdges.clear();
               visibleTrail = false;
            } else {
               List<Vec3> startPosList = Lists.newArrayList();
               List<Vec3> endPosList = Lists.newArrayList();
               AbstractTrailParticle.TrailEdge edge1;
               AbstractTrailParticle.TrailEdge edge2;
               if (isFirstTrail) {
                  int lastIdx = this.invisibleTrailEdges.size() - 1;
                  edge1 = this.invisibleTrailEdges.get(lastIdx);
                  edge2 = new AbstractTrailParticle.TrailEdge(prevStartPos, prevEndPos, -1);
               } else {
                  edge1 = this.trailEdges.get(this.trailEdges.size() - (this.trailInfo.interpolateCount() / 2 + 1));
                  edge2 = this.trailEdges.get(this.trailEdges.size() - 1);
                  edge2.lifetime++;
               }

               startPosList.add(edge1.start);
               endPosList.add(edge1.end);
               startPosList.add(edge2.start);
               endPosList.add(edge2.end);
               startPosList.add(middleStartPos);
               endPosList.add(middleEndPos);
               startPosList.add(currentStartPos);
               endPosList.add(currentEndPos);
               finalStartPositions = CubicBezierCurve.getBezierInterpolatedPoints(startPosList, 1, 3, this.trailInfo.interpolateCount());
               finalEndPositions = CubicBezierCurve.getBezierInterpolatedPoints(endPosList, 1, 3, this.trailInfo.interpolateCount());
               if (!isFirstTrail) {
                  finalStartPositions.remove(0);
                  finalEndPositions.remove(0);
               }

               visibleTrail = true;
            }

            this.makeTrailEdges(finalStartPositions, finalEndPositions, visibleTrail ? this.trailEdges : this.invisibleTrailEdges);
         }
      }

      @Override
      public void m_5744_(VertexConsumer vertexConsumer, Camera camera, float partialTick) {
         if (!this.trailEdges.isEmpty()) {
            TextureManager texturemanager = Minecraft.m_91087_().m_91097_();
            AbstractTexture abstracttexture = texturemanager.m_118506_(this.trailInfo.texturePath());
            RenderSystem.bindTexture(abstracttexture.m_117963_());
            RenderSystem.texParameter(3553, 10242, 33071);
            RenderSystem.texParameter(3553, 10243, 33071);
            RenderSystem.setShaderTexture(0, abstracttexture.m_117963_());
            PoseStack poseStack = new PoseStack();
            this.setupPoseStack(poseStack, camera, partialTick);
            Matrix4f matrix4f = poseStack.m_85850_().m_252922_();
            int edges = this.trailEdges.size() - 1;
            boolean startFade = this.trailEdges.get(0).lifetime == 1;
            boolean endFade = this.trailEdges.get(edges).lifetime == this.trailInfo.trailLifetime();
            float startEdge = (startFade ? this.trailInfo.interpolateCount() * 2 * partialTick : 0.0F) + this.startEdgeCorrection;
            float endEdge = endFade ? Math.min(edges - this.trailInfo.interpolateCount() * 2 * (1.0F - partialTick), edges - 1) : edges - 1;
            float interval = 1.0F / (endEdge - startEdge);
            float fading = 1.0F;
            if (this.shouldRemove) {
               if (TrailInfo.isValidTime(this.trailInfo.fadeTime())) {
                  fading = (float)this.f_107225_ / this.trailInfo.trailLifetime();
               } else {
                  fading = Mth.m_14036_((this.f_107225_ + (1.0F - partialTick)) / this.trailInfo.trailLifetime(), 0.0F, 1.0F);
               }
            }

            float partialStartEdge = interval * (startEdge % 1.0F);
            float from = -partialStartEdge;
            float to = -partialStartEdge + interval;

            for (int i = (int)startEdge; i < (int)endEdge + 1; i++) {
               AbstractTrailParticle.TrailEdge e1 = this.trailEdges.get(i);
               AbstractTrailParticle.TrailEdge e2 = this.trailEdges.get(i + 1);
               Vector4f pos1 = new Vector4f((float)e1.start.f_82479_, (float)e1.start.f_82480_, (float)e1.start.f_82481_, 1.0F);
               Vector4f pos2 = new Vector4f((float)e1.end.f_82479_, (float)e1.end.f_82480_, (float)e1.end.f_82481_, 1.0F);
               Vector4f pos3 = new Vector4f((float)e2.end.f_82479_, (float)e2.end.f_82480_, (float)e2.end.f_82481_, 1.0F);
               Vector4f pos4 = new Vector4f((float)e2.start.f_82479_, (float)e2.start.f_82480_, (float)e2.start.f_82481_, 1.0F);
               pos1.mul(matrix4f);
               pos2.mul(matrix4f);
               pos3.mul(matrix4f);
               pos4.mul(matrix4f);
               float alphaFrom = Mth.m_14036_(from, 0.0F, 1.0F);
               float alphaTo = Mth.m_14036_(to, 0.0F, 1.0F);
               vertexConsumer.m_5483_(pos1.x(), pos1.y(), pos1.z())
                  .m_7421_(from, 1.0F)
                  .m_85950_(this.f_107227_, this.f_107228_, this.f_107229_, this.f_107230_ * alphaFrom * fading)
                  .m_85969_(0)
                  .m_5752_();
               vertexConsumer.m_5483_(pos2.x(), pos2.y(), pos2.z())
                  .m_7421_(from, 0.0F)
                  .m_85950_(this.f_107227_, this.f_107228_, this.f_107229_, this.f_107230_ * alphaFrom * fading)
                  .m_85969_(0)
                  .m_5752_();
               vertexConsumer.m_5483_(pos3.x(), pos3.y(), pos3.z())
                  .m_7421_(to, 0.0F)
                  .m_85950_(this.f_107227_, this.f_107228_, this.f_107229_, this.f_107230_ * alphaTo * fading)
                  .m_85969_(0)
                  .m_5752_();
               vertexConsumer.m_5483_(pos4.x(), pos4.y(), pos4.z())
                  .m_7421_(to, 1.0F)
                  .m_85950_(this.f_107227_, this.f_107228_, this.f_107229_, this.f_107230_ * alphaTo * fading)
                  .m_85969_(0)
                  .m_5752_();
               from += interval;
               to += interval;
            }
         }
      }

      @Override
      protected void setupPoseStack(PoseStack poseStack, Camera camera, float partialTicks) {
         float x = ModelPreviewer.this.xMove;
         float y = ModelPreviewer.this.yMove;
         float z = (float)ModelPreviewer.this.zoom;
         float xRot = ModelPreviewer.this.xRot;
         float yRot = ModelPreviewer.this.yRot;
         poseStack.m_85837_(x, y - 1.0, z);
         poseStack.m_252781_(QuaternionUtils.XP.rotationDegrees(xRot));
         poseStack.m_252781_(QuaternionUtils.YP.rotationDegrees(yRot));
      }
   }

   public class FakeEntityPatch extends LivingEntityPatch<LivingEntity> implements SimulatableObject, ClothSimulatable {
      public FakeEntityPatch(Armature armature) {
         this.armature = armature.deepCopy();
      }

      public void setAnimator() {
         this.animator = ModelPreviewer.this.animator;
      }

      @Override
      public void initAnimator(Animator animator) {
         this.animator = animator;
      }

      @Override
      public void updateMotion(boolean considerInaction) {
      }

      @Override
      public AssetAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) {
         return null;
      }

      @Override
      public boolean isLogicalClient() {
         return true;
      }

      @Override
      public void cancelItemUse() {
      }

      @Override
      public float getAttackDirectionPitch() {
         return 0.0F;
      }

      @Override
      public OpenMatrix4f getModelMatrix(float partialTicks) {
         return MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, partialTicks, 1.0F, 1.0F, 1.0F);
      }

      @Override
      public void poseTick(DynamicAnimation animation, Pose pose, float time, float partialTicks) {
      }

      @Override
      public void updateEntityState() {
      }

      @Override
      public boolean invalid() {
         return false;
      }

      @Override
      public Vec3 getObjectVelocity() {
         return Vec3.f_82478_;
      }

      @Override
      public Vec3 getAccurateCloakLocation(float partialFrame) {
         return Vec3.f_82478_;
      }

      @Override
      public Vec3 getAccuratePartialLocation(float partialFrame) {
         return Vec3.f_82478_;
      }

      @Override
      public float getAccurateYRot(float partialFrame) {
         return ModelPreviewer.this.yRot;
      }

      @Override
      public float getYRotDelta(float partialFrame) {
         return ModelPreviewer.this.yRot - ModelPreviewer.this.yRotO;
      }

      @Override
      public float getYRot() {
         return ModelPreviewer.this.yRot;
      }

      @Override
      public float getYRotO() {
         return ModelPreviewer.this.yRotO;
      }

      @Override
      public <SIM extends PhysicsSimulator<?, ?, ?, ?, ?>> Optional<SIM> getSimulator(SimulationTypes<?, ?, ?, ?, ?, SIM> simulationType) {
         return Optional.empty();
      }

      @Override
      public float getScale() {
         return 1.0F;
      }

      @Override
      public Animator getSimulatableAnimator() {
         return this.animator;
      }

      @Override
      public float getGravity() {
         return 9.8F;
      }

      @Override
      public ClothSimulator getClothSimulator() {
         return null;
      }

      @Override
      public Faction getFaction() {
         return null;
      }
   }

   class ModelRenderTarget extends RenderTarget {
      public ModelRenderTarget() {
         super(true);
         RenderSystem.assertOnRenderThreadOrInit();
         Window window = Minecraft.m_91087_().m_91268_();
         this.m_83941_(window.m_85441_(), window.m_85442_(), false);
      }

      private void blitToScreen(GuiGraphics guiGraphics) {
         RenderSystem.setShader(GameRenderer::m_172820_);
         RenderSystem.setShaderTexture(0, this.f_83923_);
         float left = ModelPreviewer.this._getX();
         float top = ModelPreviewer.this._getY();
         float right = left + ModelPreviewer.this._getWidth();
         float bottom = top + ModelPreviewer.this._getHeight();
         float u = (float)this.f_83917_ / this.f_83915_;
         float v = (float)this.f_83918_ / this.f_83916_;
         guiGraphics.m_280168_().m_85836_();
         Matrix4f matrix4f = guiGraphics.m_280168_().m_85850_().m_252922_();
         Tesselator tesselator = Tesselator.m_85913_();
         BufferBuilder bufferbuilder = tesselator.m_85915_();
         bufferbuilder.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85819_);
         bufferbuilder.m_252986_(matrix4f, left, bottom, 0.0F).m_7421_(0.0F, 0.0F).m_6122_(255, 255, 255, 255).m_5752_();
         bufferbuilder.m_252986_(matrix4f, right, bottom, 0.0F).m_7421_(u, 0.0F).m_6122_(255, 255, 255, 255).m_5752_();
         bufferbuilder.m_252986_(matrix4f, right, top, 0.0F).m_7421_(u, v).m_6122_(255, 255, 255, 255).m_5752_();
         bufferbuilder.m_252986_(matrix4f, left, top, 0.0F).m_7421_(0.0F, v).m_6122_(255, 255, 255, 255).m_5752_();
         BufferUploader.m_231202_(bufferbuilder.m_231175_());
         guiGraphics.m_280168_().m_85849_();
      }
   }

   public class NoEntityAnimator extends ClientAnimator {
      public NoEntityAnimator(LivingEntityPatch<?> entitypatch) {
         super(entitypatch, ModelPreviewer.NoEntityAnimator.NoEntityBaseLayer::new);
      }

      @Override
      public void tick() {
         this.baseLayer.update(this.entitypatch);
         if (this.baseLayer.animationPlayer.isEnd() && this.baseLayer.getNextAnimation() == null) {
            AssetAccessor<? extends StaticAnimation> toPlay = ModelPreviewer.this.index > -1
                  && ModelPreviewer.this.index < ModelPreviewer.this.animationsToPlay.size()
               ? ModelPreviewer.this.animationsToPlay.get(ModelPreviewer.this.index)
               : Animations.EMPTY_ANIMATION;
            this.baseLayer.playAnimation(toPlay, this.entitypatch, 0.0F);
            if (!ModelPreviewer.this.trailInfoList.isEmpty()) {
               for (TrailInfo trailInfo : ModelPreviewer.this.trailInfoList) {
                  if (trailInfo.playable()) {
                     ModelPreviewer.CustomTrailParticle trail = ModelPreviewer.this.new CustomTrailParticle(
                        ModelPreviewer.this.entitypatch.getArmature().searchJointByName(trailInfo.joint()), toPlay, trailInfo
                     );
                     ModelPreviewer.this.trailParticles.add(trail);
                  } else {
                     toPlay.get()
                        .getProperty(ClientAnimationProperties.TRAIL_EFFECT)
                        .ifPresent(
                           trailInfos -> {
                              for (TrailInfo info : trailInfos) {
                                 if (info.hand() == InteractionHand.MAIN_HAND) {
                                    TrailInfo combinedTrailInfo = trailInfo.overwrite(info);
                                    if (combinedTrailInfo.playable()) {
                                       ModelPreviewer.CustomTrailParticle trailx = ModelPreviewer.this.new CustomTrailParticle(
                                          ModelPreviewer.this.entitypatch.getArmature().searchJointByName(combinedTrailInfo.joint()), toPlay, combinedTrailInfo
                                       );
                                       ModelPreviewer.this.trailParticles.add(trailx);
                                    }
                                 }
                              }
                           }
                        );
                  }
               }
            }

            ModelPreviewer.this.index = (ModelPreviewer.this.index + 1) % ModelPreviewer.this.animationsToPlay.size();
         }
      }

      @Override
      public LivingEntityPatch<?> getEntityPatch() {
         return this.entitypatch;
      }

      static class NoEntityAnimationPlayer extends AnimationPlayer {
         @Override
         public void tick(LivingEntityPatch<?> entitypatch) {
            DynamicAnimation nowPlay = this.getAnimation().get();
            this.prevElapsedTime = this.elapsedTime;
            this.elapsedTime = this.elapsedTime + 0.05F * (this.isReversed() && nowPlay.canBePlayedReverse() ? -1.0F : 1.0F);
            if (this.elapsedTime >= nowPlay.getTotalTime()) {
               if (nowPlay.isRepeat()) {
                  this.prevElapsedTime = 0.0F;
                  this.elapsedTime = this.elapsedTime % nowPlay.getTotalTime();
               } else {
                  this.elapsedTime = nowPlay.getTotalTime();
                  this.isEnd = true;
               }
            } else if (this.elapsedTime < 0.0F) {
               if (nowPlay.isRepeat()) {
                  this.prevElapsedTime = nowPlay.getTotalTime();
                  this.elapsedTime = nowPlay.getTotalTime() + this.elapsedTime;
               } else {
                  this.elapsedTime = 0.0F;
                  this.isEnd = true;
               }
            }
         }

         @Override
         public void begin(AssetAccessor<? extends DynamicAnimation> animation, LivingEntityPatch<?> entitypatch) {
         }

         @Override
         public Pose getCurrentPose(LivingEntityPatch<?> entitypatch, float partialTicks) {
            return this.play.get().getRawPose(this.prevElapsedTime + (this.elapsedTime - this.prevElapsedTime) * partialTicks);
         }
      }

      static class NoEntityBaseLayer extends Layer.BaseLayer {
         public NoEntityBaseLayer() {
            super(ModelPreviewer.NoEntityAnimator.NoEntityAnimationPlayer::new);
            this.compositeLayers.clear();

            for (Layer.Priority priority : Layer.Priority.values()) {
               this.compositeLayers.computeIfAbsent(priority, ModelPreviewer.NoEntityAnimator.NoEntityLayer::new);
            }

            this.baseLayerPriority = Layer.Priority.LOWEST;
         }

         @Override
         public void playAnimation(AssetAccessor<? extends StaticAnimation> nextAnimation, LivingEntityPatch<?> entitypatch, float convertTimeModifier) {
            Layer.Priority priority = nextAnimation.get().getPriority();
            this.baseLayerPriority = priority;
            this.offCompositeLayersLowerThan(entitypatch, nextAnimation);
            Pose lastPose = entitypatch.<Animator>getAnimator().getPose(1.0F);
            this.resume();
            if (!nextAnimation.get().isMetaAnimation()) {
               this.setLinkAnimation(nextAnimation, entitypatch, lastPose, convertTimeModifier);
               this.linkAnimation.putOnPlayer(this.animationPlayer, entitypatch);
               entitypatch.updateEntityState();
               this.nextAnimation = nextAnimation;
            }
         }

         @Override
         public void playAnimationInstantly(AssetAccessor<? extends DynamicAnimation> nextAnimation, LivingEntityPatch<?> entitypatch) {
            this.resume();
            nextAnimation.get().putOnPlayer(this.animationPlayer, entitypatch);
            this.nextAnimation = null;
         }

         @Override
         protected void playLivingAnimation(AssetAccessor<? extends StaticAnimation> nextAnimation, LivingEntityPatch<?> entitypatch) {
            this.resume();
            if (!nextAnimation.get().isMetaAnimation()) {
               this.concurrentLinkAnimation
                  .acceptFrom(this.animationPlayer.getAnimation().get().getRealAnimation(), nextAnimation, this.animationPlayer.getElapsedTime());
               this.concurrentLinkAnimation.putOnPlayer(this.animationPlayer, entitypatch);
               this.nextAnimation = nextAnimation;
            }
         }

         @Override
         public void update(LivingEntityPatch<?> entitypatch) {
            if (this.paused) {
               this.animationPlayer.setElapsedTime(this.animationPlayer.getElapsedTime());
            } else {
               this.animationPlayer.tick(entitypatch);
            }

            if (!this.paused && this.animationPlayer.isEnd()) {
               if (this.nextAnimation != null) {
                  this.nextAnimation.get().putOnPlayer(this.animationPlayer, entitypatch);
                  this.nextAnimation = null;
               } else if (this.animationPlayer.getAnimation() instanceof LayerOffAnimation) {
                  this.animationPlayer.getAnimation().get().end(entitypatch, Animations.EMPTY_ANIMATION, true);
               } else {
                  this.off(entitypatch);
               }
            }

            for (Layer layer : this.compositeLayers.values()) {
               layer.update(entitypatch);
            }
         }

         @Override
         protected void setLinkAnimation(
            AssetAccessor<? extends StaticAnimation> nextAnimation, LivingEntityPatch<?> entitypatch, Pose lastPose, float convertTimeModifier
         ) {
            Pose currentPose = this.animationPlayer.getAnimation().get().getRawPose(this.animationPlayer.getElapsedTime());
            Pose nextAnimationPose = nextAnimation.get().getRawPose(0.0F);
            float totalTime = nextAnimation.get().getTransitionTime();
            AssetAccessor<? extends DynamicAnimation> fromAnimation = this.animationPlayer.isEmpty()
               ? entitypatch.getClientAnimator().baseLayer.animationPlayer.getAnimation()
               : this.animationPlayer.getAnimation();
            if (fromAnimation instanceof LinkAnimation linkAnimation) {
               fromAnimation = linkAnimation.getFromAnimation();
            }

            this.linkAnimation.getAnimationClip().reset();
            this.linkAnimation.setTotalTime(totalTime);
            this.linkAnimation.setConnectedAnimations(fromAnimation, nextAnimation);
            Map<String, JointTransform> data1 = currentPose.getJointTransformData();
            Map<String, JointTransform> data2 = nextAnimationPose.getJointTransformData();

            for (String jointName : data1.keySet()) {
               if (data1.containsKey(jointName) && data2.containsKey(jointName)) {
                  Keyframe[] keyframes = new Keyframe[]{new Keyframe(0.0F, data1.get(jointName)), new Keyframe(totalTime, data2.get(jointName))};
                  TransformSheet sheet = new TransformSheet(keyframes);
                  this.linkAnimation.getAnimationClip().addJointTransform(jointName, sheet);
               }
            }

            this.animationPlayer.setPlayAnimation(this.linkAnimation);
         }

         public void offCompositeLayerLowerThan(LivingEntityPatch<?> entitypatch, StaticAnimation nextAnimation) {
            for (Layer.Priority p : nextAnimation.getPriority().lowersAndEqual()) {
               if (p != Layer.Priority.LOWEST || nextAnimation.isMainFrameAnimation()) {
                  this.compositeLayers.get(p).off(entitypatch);
               }
            }
         }

         @Override
         public Layer getLayer(Layer.Priority priority) {
            return this.compositeLayers.get(priority);
         }

         @Override
         public void off(LivingEntityPatch<?> entitypatch) {
         }

         @Override
         protected boolean isDisabled() {
            return false;
         }

         @Override
         protected boolean isBaseLayer() {
            return true;
         }
      }

      static class NoEntityLayer extends Layer {
         public NoEntityLayer(Layer.Priority priority) {
            super(priority, ModelPreviewer.NoEntityAnimator.NoEntityAnimationPlayer::new);
         }

         @Override
         public void playAnimation(AssetAccessor<? extends StaticAnimation> nextAnimation, LivingEntityPatch<?> entitypatch, float convertTimeModifier) {
            Pose lastPose = entitypatch.<Animator>getAnimator().getPose(1.0F);
            this.resume();
            if (!nextAnimation.get().isMetaAnimation()) {
               this.setLinkAnimation(nextAnimation, entitypatch, lastPose, convertTimeModifier);
               this.linkAnimation.putOnPlayer(this.animationPlayer, entitypatch);
               this.nextAnimation = nextAnimation;
            }
         }

         @Override
         public void playAnimationInstantly(AssetAccessor<? extends DynamicAnimation> nextAnimation, LivingEntityPatch<?> entitypatch) {
            this.resume();
            nextAnimation.get().putOnPlayer(this.animationPlayer, entitypatch);
            this.nextAnimation = null;
         }

         @Override
         protected void setLinkAnimation(
            AssetAccessor<? extends StaticAnimation> nextAnimation, LivingEntityPatch<?> entitypatch, Pose lastPose, float convertTimeModifier
         ) {
            Pose currentPose = this.animationPlayer.getAnimation().get().getRawPose(this.animationPlayer.getElapsedTime());
            Pose nextAnimationPose = nextAnimation.get().getRawPose(0.0F);
            float totalTime = nextAnimation.get().getTransitionTime();
            AssetAccessor<? extends DynamicAnimation> fromAnimation = this.animationPlayer.isEmpty()
               ? entitypatch.getClientAnimator().baseLayer.animationPlayer.getAnimation()
               : this.animationPlayer.getAnimation();
            if (fromAnimation instanceof LinkAnimation linkAnimation) {
               fromAnimation = linkAnimation.getFromAnimation();
            }

            this.linkAnimation.getAnimationClip().reset();
            this.linkAnimation.setTotalTime(totalTime);
            this.linkAnimation.setConnectedAnimations(fromAnimation, nextAnimation);
            Map<String, JointTransform> data1 = currentPose.getJointTransformData();
            Map<String, JointTransform> data2 = nextAnimationPose.getJointTransformData();

            for (String jointName : data1.keySet()) {
               if (data1.containsKey(jointName) && data2.containsKey(jointName)) {
                  Keyframe[] keyframes = new Keyframe[]{new Keyframe(0.0F, data1.get(jointName)), new Keyframe(totalTime, data2.get(jointName))};
                  TransformSheet sheet = new TransformSheet(keyframes);
                  this.linkAnimation.getAnimationClip().addJointTransform(jointName, sheet);
               }
            }

            this.animationPlayer.setPlayAnimation(this.linkAnimation);
         }

         @Override
         public void update(LivingEntityPatch<?> entitypatch) {
            if (this.paused) {
               this.animationPlayer.setElapsedTime(this.animationPlayer.getElapsedTime());
            } else {
               this.animationPlayer.tick(entitypatch);
            }

            if (!this.paused && this.animationPlayer.isEnd()) {
               if (this.nextAnimation != null) {
                  this.nextAnimation.get().putOnPlayer(this.animationPlayer, entitypatch);
                  this.nextAnimation = null;
               } else if (this.animationPlayer.getAnimation() instanceof LayerOffAnimation) {
                  this.animationPlayer.getAnimation().get().end(entitypatch, Animations.EMPTY_ANIMATION, true);
               } else {
                  this.off(entitypatch);
               }
            }
         }

         public Pose getEnabledPose(LivingEntityPatch<?> entitypatch, float partialTick) {
            DynamicAnimation animation = this.animationPlayer.getAnimation().get();
            Pose pose = animation.getRawPose(
               this.animationPlayer.getPrevElapsedTime() + (this.animationPlayer.getElapsedTime() - this.animationPlayer.getPrevElapsedTime()) * partialTick
            );
            pose.disableJoint(entry -> !animation.hasTransformFor((String)entry.getKey()));
            return pose;
         }

         @Override
         public void off(LivingEntityPatch<?> entitypatch) {
            if (!this.isDisabled() && !(this.animationPlayer.getAnimation() instanceof LayerOffAnimation)) {
               float convertTime = entitypatch.getClientAnimator().baseLayer.animationPlayer.getAnimation().get().getTransitionTime();
               setLayerOffAnimation(this.animationPlayer.getAnimation(), this.getEnabledPose(entitypatch, 1.0F), this.layerOffAnimation, convertTime);
               this.playAnimationInstantly(this.layerOffAnimation, entitypatch);
            }
         }
      }
   }
}
