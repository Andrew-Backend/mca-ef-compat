package yesman.epicfight.api.animation.types;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationClip;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationVariables;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Keyframe;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationParameters;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.asset.JsonAssetLoader;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.client.animation.property.JointMaskEntry;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.api.exception.AssetLoadingException;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.physics.ik.InverseKinematicsProvider;
import yesman.epicfight.api.physics.ik.InverseKinematicsSimulatable;
import yesman.epicfight.api.physics.ik.InverseKinematicsSimulator;
import yesman.epicfight.api.utils.datastruct.TypeFlexibleHashMap;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.renderer.RenderingTool;
import yesman.epicfight.client.renderer.patched.item.RenderItemBase;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.entity.eventlistener.AnimationBeginEvent;
import yesman.epicfight.world.entity.eventlistener.AnimationEndEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

public class StaticAnimation extends DynamicAnimation implements InverseKinematicsProvider {
   public static final AnimationVariables.IndependentAnimationVariableKey<Boolean> HAD_NO_PHYSICS = AnimationVariables.independent(animator -> false, true);
   protected final Map<AnimationProperty<?>, Object> properties = Maps.newHashMap();
   protected final StateSpectrum.Blueprint stateSpectrumBlueprint = new StateSpectrum.Blueprint();
   protected final StateSpectrum stateSpectrum = new StateSpectrum();
   protected final AssetAccessor<? extends Armature> armature;
   protected ResourceLocation resourceLocation;
   protected AnimationManager.AnimationAccessor<? extends StaticAnimation> accessor;
   private final String filehash;

   public static String getFileHash(ResourceLocation rl) {
      String fileHash;
      try {
         JsonAssetLoader jsonfile = new JsonAssetLoader(AnimationManager.getAnimationResourceManager(), rl);
         fileHash = jsonfile.getFileHash();
      } catch (AssetLoadingException e) {
         fileHash = "";
      }

      return fileHash;
   }

   public StaticAnimation() {
      super(0.0F, true);
      this.resourceLocation = EpicFightMod.identifier("emtpy");
      this.armature = null;
      this.filehash = "";
   }

   public StaticAnimation(boolean isRepeat, AnimationManager.AnimationAccessor<? extends StaticAnimation> accessor, AssetAccessor<? extends Armature> armature) {
      this(0.15F, isRepeat, accessor, armature);
   }

   public StaticAnimation(
      float transitionTime,
      boolean isRepeat,
      AnimationManager.AnimationAccessor<? extends StaticAnimation> accessor,
      AssetAccessor<? extends Armature> armature
   ) {
      super(transitionTime, isRepeat);
      this.resourceLocation = ResourceLocation.fromNamespaceAndPath(
         accessor.registryName().m_135827_(), "animmodels/animations/" + accessor.registryName().m_135815_() + ".json"
      );
      this.armature = armature;
      this.accessor = accessor;
      this.filehash = getFileHash(this.resourceLocation);
   }

   public StaticAnimation(float transitionTime, boolean isRepeat, String path, AssetAccessor<? extends Armature> armature) {
      super(transitionTime, isRepeat);
      ResourceLocation registryName = ResourceLocation.parse(path);
      this.resourceLocation = ResourceLocation.fromNamespaceAndPath(registryName.m_135827_(), "animmodels/animations/" + registryName.m_135815_() + ".json");
      this.armature = armature;
      this.filehash = "";
   }

   public StaticAnimation(
      ResourceLocation fileLocation, float transitionTime, boolean isRepeat, String registryName, AssetAccessor<? extends Armature> armature
   ) {
      super(transitionTime, isRepeat);
      this.resourceLocation = fileLocation;
      this.armature = armature;
      this.filehash = "";
   }

   public void loadAnimation() {
      if (!this.isMetaAnimation()) {
         if (this.properties.containsKey(AnimationProperty.StaticAnimationProperty.IK_DEFINITION)) {
            this.animationClip = AnimationManager.getInstance().loadAnimationClip(this, JsonAssetLoader::loadAllJointsClipForAnimation);
            this.getProperty(AnimationProperty.StaticAnimationProperty.IK_DEFINITION)
               .ifPresent(
                  ikDefinitions -> {
                     boolean correctY = this.getProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL).orElse(false);
                     boolean correctZ = this.isMainFrameAnimation();
                     List<InverseKinematicsSimulator.BakedInverseKinematicsDefinition> bakedIKDefinitionList = ikDefinitions.stream()
                        .map(ikDefinition -> ikDefinition.bake(this.armature, this.animationClip.getJointTransforms(), correctY, correctZ))
                        .toList();
                     this.addProperty(AnimationProperty.StaticAnimationProperty.BAKED_IK_DEFINITION, bakedIKDefinitionList);
                     this.properties.remove(AnimationProperty.StaticAnimationProperty.IK_DEFINITION);
                  }
               );
         } else {
            this.animationClip = AnimationManager.getInstance().loadAnimationClip(this, JsonAssetLoader::loadClipForAnimation);
         }

         this.animationClip.bakeKeyframes();
      }
   }

   public void postInit() {
      this.stateSpectrum.readFrom(this.stateSpectrumBlueprint);
   }

   @Override
   public AnimationClip getAnimationClip() {
      if (this.animationClip == null) {
         this.loadAnimation();
      }

      return this.animationClip;
   }

   public void setLinkAnimation(
      AssetAccessor<? extends DynamicAnimation> fromAnimation,
      Pose startPose,
      boolean isOnSameLayer,
      float transitionTimeModifier,
      LivingEntityPatch<?> entitypatch,
      LinkAnimation dest
   ) {
      if (!entitypatch.isLogicalClient()) {
         startPose = Animations.EMPTY_ANIMATION.getPoseByTime(entitypatch, 0.0F, 1.0F);
      }

      dest.resetNextStartTime();
      float playTime = this.getPlaySpeed(entitypatch, dest);
      AnimationProperty.PlaybackSpeedModifier playSpeedModifier = this.getRealAnimation()
         .get()
         .getProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER)
         .orElse(null);
      if (playSpeedModifier != null) {
         playTime = playSpeedModifier.modify(dest, entitypatch, playTime, 0.0F, playTime);
      }

      playTime = Math.abs(playTime);
      playTime *= 0.05F;
      float linkTime = transitionTimeModifier > 0.0F ? transitionTimeModifier + this.transitionTime : this.transitionTime;
      float totalTime = playTime * (int)Math.ceil(linkTime / playTime);
      float nextStartTime = Math.max(0.0F, -transitionTimeModifier);
      nextStartTime += totalTime - linkTime;
      dest.setNextStartTime(nextStartTime);
      dest.getAnimationClip().reset();
      dest.setTotalTime(totalTime);
      dest.setConnectedAnimations(fromAnimation, this.getAccessor());
      Map<String, JointTransform> data1 = startPose.getJointTransformData();
      Map<String, JointTransform> data2 = this.getPoseByTime(entitypatch, nextStartTime, 0.0F).getJointTransformData();
      Set<String> joint1 = new HashSet<>(isOnSameLayer ? data1.keySet() : Set.of());
      Set<String> joint2 = new HashSet<>(data2.keySet());
      if (entitypatch.isLogicalClient()) {
         JointMaskEntry entry = fromAnimation.get().getJointMaskEntry(entitypatch, false).orElse(null);
         JointMaskEntry entry2 = this.getJointMaskEntry(entitypatch, true).orElse(null);
         if (entry != null) {
            joint1.removeIf(
               jointNamex -> entry.isMasked(
                  fromAnimation.get().getProperty(ClientAnimationProperties.LAYER_TYPE).orElse(Layer.LayerType.BASE_LAYER) == Layer.LayerType.BASE_LAYER
                     ? entitypatch.getClientAnimator().currentMotion()
                     : entitypatch.getClientAnimator().currentCompositeMotion(),
                  jointNamex
               )
            );
         }

         if (entry2 != null) {
            joint2.removeIf(
               jointNamex -> entry2.isMasked(
                  this.getProperty(ClientAnimationProperties.LAYER_TYPE).orElse(Layer.LayerType.BASE_LAYER) == Layer.LayerType.BASE_LAYER
                     ? entitypatch.getCurrentLivingMotion()
                     : entitypatch.currentCompositeMotion,
                  jointNamex
               )
            );
         }
      }

      joint1.addAll(joint2);
      if (linkTime != totalTime) {
         Map<String, JointTransform> firstPose = this.getPoseByTime(entitypatch, 0.0F, 0.0F).getJointTransformData();

         for (String jointName : joint1) {
            Keyframe[] keyframes = new Keyframe[]{
               new Keyframe(0.0F, data1.get(jointName)), new Keyframe(linkTime, firstPose.get(jointName)), new Keyframe(totalTime, data2.get(jointName))
            };
            TransformSheet sheet = new TransformSheet(keyframes);
            dest.getAnimationClip().addJointTransform(jointName, sheet);
         }
      } else {
         for (String jointName : joint1) {
            Keyframe[] keyframes = new Keyframe[]{new Keyframe(0.0F, data1.get(jointName)), new Keyframe(totalTime, data2.get(jointName))};
            TransformSheet sheet = new TransformSheet(keyframes);
            dest.getAnimationClip().addJointTransform(jointName, sheet);
         }
      }
   }

   @Override
   public void begin(LivingEntityPatch<?> entitypatch) {
      this.getAnimationClip();
      this.getProperty(AnimationProperty.StaticAnimationProperty.NO_PHYSICS).ifPresent(val -> {
         if (val) {
            entitypatch.<Animator>getAnimator().getVariables().put(HAD_NO_PHYSICS, this.getAccessor(), entitypatch.getOriginal().f_19794_);
            entitypatch.getOriginal().f_19794_ = true;
         }
      });
      if (entitypatch.isLogicalClient()) {
         this.getProperty(ClientAnimationProperties.TRAIL_EFFECT)
            .ifPresent(
               trailInfos -> {
                  int idx = 0;

                  for (TrailInfo trailInfo : trailInfos) {
                     double eid = Double.longBitsToDouble(entitypatch.getOriginal().m_19879_());
                     double animid = Double.longBitsToDouble(this.getId());
                     double jointId = Double.longBitsToDouble(this.armature.get().searchJointByName(trailInfo.joint()).getId());
                     double index = Double.longBitsToDouble(idx++);
                     if (trailInfo.hand() != null) {
                        RenderItemBase renderitembase = ClientEngine.getInstance()
                           .renderEngine
                           .getItemRenderer(entitypatch.getAdvancedHoldingItemStack(trailInfo.hand()));
                        if (renderitembase != null && renderitembase.trailInfo() != null) {
                           trailInfo = renderitembase.trailInfo().overwrite(trailInfo);
                        }
                     }

                     if (trailInfo.playable()) {
                        entitypatch.getOriginal().m_9236_().m_7106_(trailInfo.particle(), eid, 0.0, animid, jointId, index, 0.0);
                     }
                  }
               }
            );
      }

      this.getProperty(AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS).ifPresent(events -> {
         for (AnimationEvent.SimpleEvent<?> event : events) {
            event.execute(entitypatch, this.getAccessor(), 0.0F, 0.0F);
         }
      });
      if (entitypatch instanceof PlayerPatch<?> playerpatch) {
         playerpatch.getEventListener().triggerEvents(PlayerEventListener.EventType.ANIMATION_BEGIN_EVENT, new AnimationBeginEvent(playerpatch, this));
      }
   }

   @Override
   public void end(LivingEntityPatch<?> entitypatch, AssetAccessor<? extends DynamicAnimation> nextAnimation, boolean isEnd) {
      if (entitypatch instanceof PlayerPatch<?> playerpatch) {
         playerpatch.getEventListener().triggerEvents(PlayerEventListener.EventType.ANIMATION_END_EVENT, new AnimationEndEvent(playerpatch, this, isEnd));
      }

      this.getProperty(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS)
         .ifPresent(
            events -> {
               for (AnimationEvent.SimpleEvent<?> event : events) {
                  event.executeWithNewParams(
                     entitypatch,
                     this.getAccessor(),
                     this.getTotalTime(),
                     this.getTotalTime(),
                     event.getParameters() == null ? AnimationParameters.of(isEnd) : AnimationParameters.addParameter(event.getParameters(), isEnd)
                  );
               }
            }
         );
      this.getProperty(AnimationProperty.StaticAnimationProperty.NO_PHYSICS).ifPresent(val -> {
         if (val) {
            entitypatch.getOriginal().f_19794_ = entitypatch.<Animator>getAnimator().getVariables().getOrDefault(HAD_NO_PHYSICS, this.getAccessor());
         }
      });
      entitypatch.<Animator>getAnimator().getVariables().removeAll(this.getAccessor());
   }

   @Override
   public void tick(LivingEntityPatch<?> entitypatch) {
      this.getProperty(AnimationProperty.StaticAnimationProperty.NO_PHYSICS).ifPresent(val -> {
         if (val) {
            entitypatch.getOriginal().f_19794_ = true;
         }
      });
      this.getProperty(AnimationProperty.StaticAnimationProperty.TICK_EVENTS)
         .ifPresent(events -> entitypatch.<Animator>getAnimator().getPlayer(this.getAccessor()).ifPresent(player -> {
            for (AnimationEvent<?, ?> event : events) {
               float prevElapsed = player.getPrevElapsedTime();
               float elapsed = player.getElapsedTime();
               event.execute(entitypatch, this.getAccessor(), prevElapsed, elapsed);
            }
         }));
   }

   @Override
   public EntityState getState(LivingEntityPatch<?> entitypatch, float time) {
      return new EntityState(this.getStatesMap(entitypatch, time));
   }

   @Override
   public TypeFlexibleHashMap<EntityState.StateFactor<?>> getStatesMap(LivingEntityPatch<?> entitypatch, float time) {
      return this.stateSpectrum.getStateMap(entitypatch, time);
   }

   @Override
   public <T> T getState(EntityState.StateFactor<T> stateFactor, LivingEntityPatch<?> entitypatch, float time) {
      return this.stateSpectrum.getSingleState(stateFactor, entitypatch, time);
   }

   @Override
   public Optional<JointMaskEntry> getJointMaskEntry(LivingEntityPatch<?> entitypatch, boolean useCurrentMotion) {
      return this.getProperty(ClientAnimationProperties.JOINT_MASK);
   }

   @Override
   public void modifyPose(DynamicAnimation animation, Pose pose, LivingEntityPatch<?> entitypatch, float time, float partialTicks) {
      entitypatch.poseTick(animation, pose, time, partialTicks);
      this.getProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER)
         .ifPresent(poseModifier -> poseModifier.modify(animation, pose, entitypatch, time, partialTicks));
   }

   @Override
   public boolean isStaticAnimation() {
      return true;
   }

   @Override
   public boolean doesHeadRotFollowEntityHead() {
      return !this.getProperty(AnimationProperty.StaticAnimationProperty.FIXED_HEAD_ROTATION).orElse(false);
   }

   @Override
   public int getId() {
      return this.accessor.id();
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof StaticAnimation staticAnimation && this.accessor != null && staticAnimation.accessor != null
         ? this.getId() == staticAnimation.getId()
         : super.equals(obj);
   }

   public boolean idBetween(StaticAnimation a1, StaticAnimation a2) {
      return a1.getId() <= this.getId() && a2.getId() >= this.getId();
   }

   public boolean in(StaticAnimation[] animations) {
      for (StaticAnimation animation : animations) {
         if (this.equals(animation)) {
            return true;
         }
      }

      return false;
   }

   public boolean in(AnimationManager.AnimationAccessor<? extends DynamicAnimation>[] animationProviders) {
      for (AnimationManager.AnimationAccessor<? extends DynamicAnimation> animationProvider : animationProviders) {
         if (this.equals(animationProvider.get())) {
            return true;
         }
      }

      return false;
   }

   public <A extends StaticAnimation> A setResourceLocation(String namespace, String path) {
      this.resourceLocation = ResourceLocation.fromNamespaceAndPath(namespace, "animmodels/animations/" + path + ".json");
      return (A)this;
   }

   public ResourceLocation getLocation() {
      return this.resourceLocation;
   }

   @Override
   public ResourceLocation getRegistryName() {
      return this.accessor.registryName();
   }

   public AssetAccessor<? extends Armature> getArmature() {
      return this.armature;
   }

   public String getFileHash() {
      return this.filehash;
   }

   @Override
   public float getPlaySpeed(LivingEntityPatch<?> entitypatch, DynamicAnimation animation) {
      return 1.0F;
   }

   @Override
   public TransformSheet getCoord() {
      return this.getProperty(AnimationProperty.ActionAnimationProperty.COORD).orElse(super.getCoord());
   }

   @Override
   public String toString() {
      String classPath = this.getClass().toString();
      return classPath.substring(classPath.lastIndexOf(".") + 1) + " " + this.getLocation();
   }

   @Deprecated
   public StaticAnimation addPropertyUnsafe(AnimationProperty<?> propertyType, Object value) {
      this.properties.put(propertyType, value);
      this.getSubAnimations().forEach(subAnimation -> subAnimation.get().addPropertyUnsafe(propertyType, value));
      return this;
   }

   public <A extends StaticAnimation, V> A addProperty(AnimationProperty.StaticAnimationProperty<V> propertyType, V value) {
      this.properties.put(propertyType, value);
      this.getSubAnimations().forEach(subAnimation -> subAnimation.get().addProperty(propertyType, value));
      return (A)this;
   }

   public <A extends StaticAnimation> A removeProperty(AnimationProperty.StaticAnimationProperty<?> propertyType) {
      this.properties.remove(propertyType);
      this.getSubAnimations().forEach(subAnimation -> subAnimation.get().removeProperty(propertyType));
      return (A)this;
   }

   @SafeVarargs
   public final <A extends StaticAnimation> A addEvents(AnimationProperty.StaticAnimationProperty<?> key, AnimationEvent<?, ?>... events) {
      this.properties.computeIfPresent(key, (k, v) -> Stream.concat(((Collection)v).stream(), List.of(events).stream()).toList());
      this.properties.computeIfAbsent(key, k -> List.of(events));
      this.getSubAnimations().forEach(subAnimation -> subAnimation.get().addEvents(key, events));
      return (A)this;
   }

   public <A extends StaticAnimation> A addEvents(AnimationEvent<?, ?>... events) {
      this.properties
         .computeIfPresent(
            AnimationProperty.StaticAnimationProperty.TICK_EVENTS, (k, v) -> Stream.concat(((Collection)v).stream(), List.of(events).stream()).toList()
         );
      this.properties.computeIfAbsent(AnimationProperty.StaticAnimationProperty.TICK_EVENTS, k -> List.of(events));
      this.getSubAnimations().forEach(subAnimation -> subAnimation.get().addEvents(events));
      return (A)this;
   }

   @Override
   public <V> Optional<V> getProperty(AnimationProperty<V> propertyType) {
      return Optional.ofNullable((V)this.properties.get(propertyType));
   }

   @OnlyIn(Dist.CLIENT)
   public Layer.Priority getPriority() {
      return this.getProperty(ClientAnimationProperties.PRIORITY).orElse(Layer.Priority.LOWEST);
   }

   @OnlyIn(Dist.CLIENT)
   public Layer.LayerType getLayerType() {
      return this.getProperty(ClientAnimationProperties.LAYER_TYPE).orElse(Layer.LayerType.BASE_LAYER);
   }

   public <A extends StaticAnimation> A newTimePair(float start, float end) {
      this.stateSpectrumBlueprint.newTimePair(start, end);
      return (A)this;
   }

   public <A extends StaticAnimation> A newConditionalTimePair(Function<LivingEntityPatch<?>, Integer> condition, float start, float end) {
      this.stateSpectrumBlueprint.newConditionalTimePair(condition, start, end);
      return (A)this;
   }

   public <T, A extends StaticAnimation> A addState(EntityState.StateFactor<T> factor, T val) {
      this.stateSpectrumBlueprint.addState(factor, val);
      return (A)this;
   }

   public <T, A extends StaticAnimation> A removeState(EntityState.StateFactor<T> factor) {
      this.stateSpectrumBlueprint.removeState(factor);
      return (A)this;
   }

   public <T, A extends StaticAnimation> A addConditionalState(int metadata, EntityState.StateFactor<T> factor, T val) {
      this.stateSpectrumBlueprint.addConditionalState(metadata, factor, val);
      return (A)this;
   }

   public <T, A extends StaticAnimation> A addStateRemoveOld(EntityState.StateFactor<T> factor, T val) {
      this.stateSpectrumBlueprint.addStateRemoveOld(factor, val);
      return (A)this;
   }

   public <T, A extends StaticAnimation> A addStateIfNotExist(EntityState.StateFactor<T> factor, T val) {
      this.stateSpectrumBlueprint.addStateIfNotExist(factor, val);
      return (A)this;
   }

   public Object getModifiedLinkState(EntityState.StateFactor<?> factor, Object val, LivingEntityPatch<?> entitypatch, float elapsedTime) {
      return val;
   }

   public List<AssetAccessor<? extends StaticAnimation>> getSubAnimations() {
      return List.of();
   }

   public AnimationManager.AnimationAccessor<? extends StaticAnimation> getRealAnimation() {
      return this.getAccessor();
   }

   @Override
   public <A extends DynamicAnimation> AnimationManager.AnimationAccessor<A> getAccessor() {
      return (AnimationManager.AnimationAccessor<A>)this.accessor;
   }

   public void setAccessor(AnimationManager.AnimationAccessor<? extends StaticAnimation> accessor) {
      this.accessor = accessor;
   }

   public void invalidate() {
      this.accessor = null;
   }

   public boolean isInvalid() {
      return this.accessor == null;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderDebugging(PoseStack poseStack, MultiBufferSource buffer, LivingEntityPatch<?> entitypatch, float playTime, float partialTicks) {
      if (entitypatch instanceof InverseKinematicsSimulatable ikSimulatable) {
         this.getProperty(AnimationProperty.StaticAnimationProperty.BAKED_IK_DEFINITION)
            .ifPresent(
               ikDefinitions -> {
                  OpenMatrix4f modelmat = ikSimulatable.getModelMatrix(partialTicks);
                  LivingEntity originalEntity = entitypatch.getOriginal();
                  Vec3 entitypos = originalEntity.m_20182_();
                  float x = (float)entitypos.f_82479_;
                  float y = (float)entitypos.f_82480_;
                  float z = (float)entitypos.f_82481_;
                  float xo = (float)originalEntity.f_19854_;
                  float yo = (float)originalEntity.f_19855_;
                  float zo = (float)originalEntity.f_19856_;
                  OpenMatrix4f toModelPos = OpenMatrix4f.mul(
                        OpenMatrix4f.createTranslation(xo + (x - xo) * partialTicks, yo + (y - yo) * partialTicks, zo + (z - zo) * partialTicks),
                        modelmat,
                        null
                     )
                     .invert();

                  for (InverseKinematicsSimulator.BakedInverseKinematicsDefinition bakedIKInfo : this.getProperty(
                        AnimationProperty.StaticAnimationProperty.BAKED_IK_DEFINITION
                     )
                     .orElse(null)) {
                     ikSimulatable.getIKSimulator().getRunningObject(bakedIKInfo.endJoint()).ifPresent(ikObjet -> {
                        VertexConsumer vertexBuilder = buffer.m_6299_(EpicFightRenderTypes.m_269166_());
                        Vec3f worldtargetpos = ikObjet.getDestination();
                        Vec3f modeltargetpos = OpenMatrix4f.transform3v(toModelPos, worldtargetpos, null).multiply(-1.0F, 1.0F, -1.0F);
                        RenderingTool.drawQuad(poseStack, vertexBuilder, modeltargetpos, 0.5F, 1.0F, 0.0F, 0.0F);
                        Vec3f jointWorldPos = ikObjet.getTipPosition(partialTicks);
                        Vec3f jointModelpos = OpenMatrix4f.transform3v(toModelPos, jointWorldPos, null);
                        RenderingTool.drawQuad(poseStack, vertexBuilder, jointModelpos.multiply(-1.0F, 1.0F, -1.0F), 0.4F, 0.0F, 0.0F, 1.0F);
                        Pose pose = new Pose();

                        for (String jointName : this.getTransfroms().keySet()) {
                           pose.putJointData(jointName, this.getTransfroms().get(jointName).getInterpolatedTransform(playTime));
                        }
                     });
                  }
               }
            );
      }
   }

   public InverseKinematicsSimulator.InverseKinematicsObject createSimulationData(
      InverseKinematicsProvider provider, InverseKinematicsSimulatable simOwner, InverseKinematicsSimulator.InverseKinematicsBuilder simBuilder
   ) {
      return new InverseKinematicsSimulator.InverseKinematicsObject(simBuilder);
   }
}
