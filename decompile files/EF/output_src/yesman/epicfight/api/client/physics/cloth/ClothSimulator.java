package yesman.epicfight.api.client.physics.cloth;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.client.model.CompositeMesh;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.MeshPart;
import yesman.epicfight.api.client.model.SoftBodyTranslatable;
import yesman.epicfight.api.client.model.VertexBuilder;
import yesman.epicfight.api.client.physics.AbstractSimulator;
import yesman.epicfight.api.collider.OBBCollider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.physics.SimulationObject;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.main.EpicFightSharedConstants;

public class ClothSimulator
   extends AbstractSimulator<ResourceLocation, ClothSimulator.ClothObjectBuilder, SoftBodyTranslatable, ClothSimulatable, ClothSimulator.ClothObject> {
   public static final ResourceLocation PLAYER_CLOAK = EpicFightMod.identifier("ingame_cloak");
   public static final ResourceLocation MODELPREVIEWER_CLOAK = EpicFightMod.identifier("previewer_cloak");
   private static final float SPATIAL_HASH_SPACING = 0.05F;
   private static boolean DRAW_MESH_COLLIDERS = false;
   private static boolean DRAW_NORMAL_OFFSET = true;
   private static boolean DRAW_OUTLINES = false;

   public static void drawMeshColliders(boolean flag) {
      if (!EpicFightSharedConstants.IS_DEV_ENV) {
         throw new IllegalStateException("Can't switch developer configuration in product environment.");
      }

      DRAW_MESH_COLLIDERS = flag;
   }

   public static void drawNormalOffset(boolean flag) {
      if (!EpicFightSharedConstants.IS_DEV_ENV) {
         throw new IllegalStateException("Can't switch developer configuration in product environment.");
      }

      DRAW_NORMAL_OFFSET = flag;
   }

   public static void drawOutlines(boolean flag) {
      if (!EpicFightSharedConstants.IS_DEV_ENV) {
         throw new IllegalStateException("Can't switch developer configuration in product environment.");
      }

      DRAW_OUTLINES = flag;
   }

   public static class ClothOBBCollider extends OBBCollider {
      private static final Vec3f WORLD_CENTER = new Vec3f();
      private static final Vec3f TO_OPPONENT = new Vec3f();
      private static final Vec3f SEP_AXIS = new Vec3f();
      private static final Vec3f TO_VERTEX = new Vec3f();
      private static final Vec3f MAX_PROJ = new Vec3f();
      private static final Vec3f TO_OPPONENT_PROJECTION = new Vec3f();
      private static final Vec3f VERTEX_PROJECTION = new Vec3f();
      private static final Vec3f PROJECTION1 = new Vec3f();
      private static final Vec3f PROJECTION2 = new Vec3f();
      private static final Vec3f PROJECTION3 = new Vec3f();
      private static final Vec3f TO_PLANE1 = new Vec3f();
      private static final Vec3f TO_PLANE2 = new Vec3f();
      private static final Vec3f TO_PLANE3 = new Vec3f();
      private final Vec3f[] destinations = new Vec3f[]{new Vec3f(), new Vec3f(), new Vec3f(), new Vec3f(), new Vec3f(), new Vec3f()};

      public ClothOBBCollider(double vertexX, double vertexY, double vertexZ, double centerX, double centerY, double centerZ) {
         super(vertexX, vertexY, vertexZ, centerX, centerY, centerZ);
      }

      public AABB getOuterAABB(float particleRadius) {
         double maxX = -1000000.0;
         double maxY = -1000000.0;
         double maxZ = -1000000.0;

         for (Vec3 rotated : this.rotatedVertices) {
            double xdistance = Math.abs(rotated.f_82479_);
            if (xdistance > maxX) {
               maxX = xdistance;
            }

            double ydistance = Math.abs(rotated.f_82480_);
            if (ydistance > maxY) {
               maxY = ydistance;
            }

            double zdistance = Math.abs(rotated.f_82481_);
            if (zdistance > maxZ) {
               maxZ = zdistance;
            }
         }

         maxX += particleRadius;
         maxY += particleRadius;
         maxZ += particleRadius;
         return new AABB(-maxX, -maxY, -maxZ, maxX, maxY, maxZ).m_82383_(this.worldCenter);
      }

      private boolean doesPointCollide(Vec3 point, float radius) {
         Vec3 toOpponent = point.m_82546_(this.worldCenter);

         for (Vec3 seperateAxis : this.rotatedNormals) {
            Vec3 maxProj = null;
            double maxDot = -1000000.0;
            if (seperateAxis.m_82526_(toOpponent) < 0.0) {
               seperateAxis = seperateAxis.m_82490_(-1.0);
            }

            for (Vec3 vertexVector : this.rotatedVertices) {
               Vec3 toVertex = seperateAxis.m_82526_(vertexVector) > 0.0 ? vertexVector : vertexVector.m_82490_(-1.0);
               double dot = seperateAxis.m_82526_(toVertex);
               if (dot > maxDot || maxProj == null) {
                  maxDot = dot;
                  maxProj = toVertex;
               }
            }

            Vec3 opponentProjection = MathUtils.projectVector(toOpponent, seperateAxis);
            Vec3 vertexProjection = MathUtils.projectVector(maxProj, seperateAxis);
            if (opponentProjection.m_82553_() > vertexProjection.m_82553_() + radius) {
               return false;
            }
         }

         return true;
      }

      public void pushIfPointInside(Vec3f point, Vec3f root, float selfCollision, List<Vec3f> destnations, List<ClothSimulator.ClothOBBCollider> others) {
         WORLD_CENTER.set(this.worldCenter);
         Vec3f.sub(point, WORLD_CENTER, TO_OPPONENT);
         int order = 0;

         for (Vec3 seperateAxis : this.rotatedNormals) {
            SEP_AXIS.set(seperateAxis);
            float maxDot = -10000.0F;
            if (Vec3f.dot(SEP_AXIS, TO_OPPONENT) < 0.0) {
               SEP_AXIS.scale(-1.0F);
            }

            for (Vec3 vertexVector : this.rotatedVertices) {
               TO_VERTEX.set(vertexVector);
               if (Vec3f.dot(SEP_AXIS, TO_VERTEX) < 0.0) {
                  TO_VERTEX.scale(-1.0F);
               }

               float dot = Vec3f.dot(SEP_AXIS, TO_VERTEX);
               if (dot > maxDot) {
                  maxDot = dot;
                  MAX_PROJ.set(TO_VERTEX);
               }
            }

            MathUtils.projectVector(TO_OPPONENT, SEP_AXIS, TO_OPPONENT_PROJECTION);
            MathUtils.projectVector(MAX_PROJ, SEP_AXIS, VERTEX_PROJECTION);
            if (TO_OPPONENT_PROJECTION.length() > VERTEX_PROJECTION.length() + selfCollision) {
               return;
            }

            switch (order) {
               case 0:
                  PROJECTION1.set(TO_OPPONENT_PROJECTION);
                  Vec3f.scale(VERTEX_PROJECTION, TO_PLANE1, (VERTEX_PROJECTION.length() + selfCollision) / VERTEX_PROJECTION.length());
                  break;
               case 1:
                  PROJECTION2.set(TO_OPPONENT_PROJECTION);
                  Vec3f.scale(VERTEX_PROJECTION, TO_PLANE2, (VERTEX_PROJECTION.length() + selfCollision) / VERTEX_PROJECTION.length());
                  break;
               case 2:
                  PROJECTION3.set(TO_OPPONENT_PROJECTION);
                  Vec3f.scale(VERTEX_PROJECTION, TO_PLANE3, (VERTEX_PROJECTION.length() + selfCollision) / VERTEX_PROJECTION.length());
            }

            order++;
         }

         this.destinations[0].set(0.0F, 0.0F, 0.0F).add(PROJECTION1).add(PROJECTION2).add(TO_PLANE3).add(this.worldCenter);
         this.destinations[1].set(0.0F, 0.0F, 0.0F).add(PROJECTION2).add(PROJECTION3).add(TO_PLANE1).add(this.worldCenter);
         this.destinations[2].set(0.0F, 0.0F, 0.0F).add(PROJECTION3).add(PROJECTION1).add(TO_PLANE2).add(this.worldCenter);
         this.destinations[3].set(0.0F, 0.0F, 0.0F).add(PROJECTION1).add(PROJECTION2).sub(TO_PLANE3).add(this.worldCenter);
         this.destinations[4].set(0.0F, 0.0F, 0.0F).add(PROJECTION2).add(PROJECTION3).sub(TO_PLANE1).add(this.worldCenter);
         this.destinations[5].set(0.0F, 0.0F, 0.0F).add(PROJECTION3).add(PROJECTION1).sub(TO_PLANE2).add(this.worldCenter);

         for (Vec3f dest : this.destinations) {
            for (ClothSimulator.ClothOBBCollider other : others) {
               if (other != this && other.doesPointCollide(dest.toDoubleVector(), selfCollision * 0.5F)) {
                  dest.invalidate();
                  break;
               }
            }
         }

         for (Vec3f dest : this.destinations) {
            if (dest.validateValues()) {
               destnations.add(dest);
            }
         }
      }
   }

   public static class ClothObject implements SimulationObject<ClothSimulator.ClothObjectBuilder, SoftBodyTranslatable, ClothSimulatable>, Mesh {
      private final SoftBodyTranslatable provider;
      private final Map<String, ClothSimulator.ClothObject.ClothPart> parts;
      private final Map<Integer, ClothSimulator.ClothObject.Particle> particles;
      private final Map<Integer, ClothSimulator.ClothObject.ClothPart.OffsetParticle> normalOffsetParticles;
      private final List<Map<Integer, Vec3f>> particleNormals;
      private final Quaternionf rotationO = new Quaternionf();
      private final Vec3f centrifugalO = new Vec3f();
      @Nullable
      protected List<Pair<Function<ClothSimulatable, OpenMatrix4f>, ClothSimulator.ClothOBBCollider>> clothColliders;
      protected final Joint parentJoint;
      private static final Vec3f TRASNFORMED = new Vec3f();
      private static final Vector4f POSITION = new Vector4f();
      private static final Vector3f NORMAL = new Vector3f();
      private static final int SUB_STEPS = 6;
      private static final Vec3f EXTERNAL_FORCE = new Vec3f();
      private static final Vec3f OFFSET = new Vec3f();
      private static final Vec3f CENTRIFUGAL = new Vec3f();
      private static final Vec3f CIRCULAR = new Vec3f();
      private static final OpenMatrix4f[] BOUND_ANIMATION_TRANSFORM = OpenMatrix4f.allocateMatrixArray(1000);
      private static final OpenMatrix4f COLLIDER_TRANSFORM = new OpenMatrix4f();
      private static final OpenMatrix4f TO_CENTRIFUGAL = new OpenMatrix4f();
      private static final OpenMatrix4f OBJECT_TRANSFORM = new OpenMatrix4f();
      private static final OpenMatrix4f INVERTED = new OpenMatrix4f();
      private static final Quaternionf ROTATOR = new Quaternionf();
      private static final Vec3f TO_P2 = new Vec3f();
      private static final Vec3f TO_P3 = new Vec3f();
      private static final Vec3f CROSS = new Vec3f();
      private static final Vec3f SCALE = new Vec3f();
      private static final Vector3f SCALER = new Vector3f();

      public ClothObject(ClothSimulator.ClothObjectBuilder builder, SoftBodyTranslatable provider, Map<String, MeshPart> parts, float[] positions) {
         this.clothColliders = builder.clothColliders;
         this.parentJoint = builder.joint;
         this.provider = provider;
         this.particles = Maps.newHashMap();
         this.normalOffsetParticles = Maps.newHashMap();
         this.particleNormals = Lists.newArrayList();

         for (int i = 0; i < positions.length / 3; i++) {
            this.particleNormals.add(Maps.newHashMap());
         }

         for (Entry<String, MeshPart> meshPart : parts.entrySet()) {
            for (VertexBuilder vb : meshPart.getValue().getVertices()) {
               Map<Integer, Vec3f> posNormals = this.particleNormals.get(vb.position);
               if (!posNormals.containsKey(vb.normal)) {
                  provider.getOriginalMesh().getVertexNormal(vb.normal, NORMAL);
                  posNormals.put(vb.normal, new Vec3f(NORMAL.x, NORMAL.y, NORMAL.z));
               }
            }
         }

         Builder<String, ClothSimulator.ClothObject.ClothPart> partBuilder = ImmutableMap.builder();

         for (Entry<String, SoftBodyTranslatable.ClothSimulationInfo> entry : provider.getSoftBodySimulationInfo().entrySet()) {
            partBuilder.put(entry.getKey(), new ClothSimulator.ClothObject.ClothPart(entry.getValue(), positions));
         }

         this.parts = partBuilder.build();
      }

      private ClothObject(ClothSimulator.ClothObject copyTarget) {
         this.provider = copyTarget.provider;
         this.parts = copyTarget.parts;
         this.particles = new HashMap<>();
         this.normalOffsetParticles = new HashMap<>();

         for (Entry<Integer, ClothSimulator.ClothObject.Particle> entry : copyTarget.particles.entrySet()) {
            this.particles.put(entry.getKey(), entry.getValue().copy());
         }

         for (Entry<Integer, ClothSimulator.ClothObject.ClothPart.OffsetParticle> entry : copyTarget.normalOffsetParticles.entrySet()) {
            this.normalOffsetParticles.put(entry.getKey(), entry.getValue().copy());
         }

         for (Entry<Integer, ClothSimulator.ClothObject.ClothPart.OffsetParticle> entry : copyTarget.normalOffsetParticles.entrySet()) {
            this.normalOffsetParticles.put(entry.getKey(), entry.getValue().copy());
         }

         this.particleNormals = ImmutableList.copyOf(copyTarget.particleNormals);
         this.parentJoint = copyTarget.parentJoint;
      }

      public ClothSimulator.ClothObject captureMyself() {
         return new ClothSimulator.ClothObject(this);
      }

      public void tick(
         ClothSimulatable simulatableObj,
         Function<Float, OpenMatrix4f> colliderTransformGetter,
         float partialTick,
         @Nullable Armature armature,
         @Nullable OpenMatrix4f[] poses
      ) {
         if (!Minecraft.m_91087_().m_91104_()) {
            boolean skinned = poses != null && armature != null;

            for (int j = 0; j < armature.getJointNumber(); j++) {
               if (skinned) {
                  BOUND_ANIMATION_TRANSFORM[j].load(poses[j]);
                  BOUND_ANIMATION_TRANSFORM[j].mulBack(armature.searchJointById(j).getToOrigin());
                  BOUND_ANIMATION_TRANSFORM[j].removeScale();
               }
            }

            float deltaFrameTime = Minecraft.m_91087_().m_91297_();
            float subStebInvert = 0.16666667F;
            float subSteppingDeltaTime = deltaFrameTime * subStebInvert;
            float gravity = simulatableObj.getGravity() * subSteppingDeltaTime * 0.05F;
            float yRot = Mth.m_14177_(Mth.m_14189_(partialTick, Mth.m_14177_(simulatableObj.getYRotO()), Mth.m_14177_(simulatableObj.getYRot())));
            TO_CENTRIFUGAL.load(BOUND_ANIMATION_TRANSFORM[this.parentJoint.getId()]);
            TO_CENTRIFUGAL.mulFront(OpenMatrix4f.createRotatorDeg(-yRot + 180.0F, Vec3f.Y_AXIS));
            TO_CENTRIFUGAL.toQuaternion(ROTATOR);
            Vec3 velocity = simulatableObj.getObjectVelocity();
            float delta = MathUtils.wrapRadian(MathUtils.getAngleBetween(this.rotationO, ROTATOR));
            float speed = Math.min((float)velocity.m_82553_() * deltaFrameTime, 0.2F);
            float rotationForce = Math.abs(delta);
            this.rotationO.set(ROTATOR);
            OpenMatrix4f.transform3v(TO_CENTRIFUGAL, Vec3f.Z_AXIS, CENTRIFUGAL);
            int deltaSign = Math.abs(delta) < 0.02 ? 0 : MathUtils.getSign(delta);
            if (deltaSign == 0) {
               CIRCULAR.set(Vec3f.ZERO);
            } else {
               Vec3f.sub(CENTRIFUGAL, this.centrifugalO, CIRCULAR);
               CIRCULAR.normalize();
            }

            this.centrifugalO.set(CENTRIFUGAL);
            CENTRIFUGAL.scale(rotationForce * (1.0F + speed * 50.0F));
            CIRCULAR.scale(rotationForce * (1.0F + speed * 50.0F));
            velocity = velocity.m_82490_(rotationForce);
            Vec3f.add(CIRCULAR, CENTRIFUGAL, EXTERNAL_FORCE);
            EXTERNAL_FORCE.add(velocity);
            this.particleNormals.forEach(poseNormals -> poseNormals.values().forEach(vec3f -> vec3f.set(0.0F, 0.0F, 0.0F)));
            Vec3 pos = simulatableObj.getAccuratePartialLocation(partialTick);
            float yRotLerp = simulatableObj.getAccurateYRot(partialTick);
            OpenMatrix4f objectTransform = OpenMatrix4f.ofTranslation((float)pos.f_82479_, (float)pos.f_82480_, (float)pos.f_82481_, OBJECT_TRANSFORM)
               .rotateDeg(180.0F - yRotLerp, Vec3f.Y_AXIS);
            OpenMatrix4f.invert(objectTransform, INVERTED);

            for (ClothSimulator.ClothObject.ClothPart part : this.parts.values()) {
               part.tick(objectTransform, EXTERNAL_FORCE, skinned ? BOUND_ANIMATION_TRANSFORM : null);
            }

            for (int i = 0; i < 6; i++) {
               float substepPartialTick = partialTick - deltaFrameTime + subSteppingDeltaTime * (i + 1);
               if (this.clothColliders != null) {
                  simulatableObj.getArmature().setPose(simulatableObj.getSimulatableAnimator().getPose(Mth.m_14036_(substepPartialTick, 0.0F, 1.0F)));
                  OpenMatrix4f colliderTransform = colliderTransformGetter.apply(substepPartialTick);

                  for (Pair<Function<ClothSimulatable, OpenMatrix4f>, ClothSimulator.ClothOBBCollider> entry : this.clothColliders) {
                     ((ClothSimulator.ClothOBBCollider)entry.getSecond())
                        .transform(OpenMatrix4f.mul(colliderTransform, (OpenMatrix4f)((Function)entry.getFirst()).apply(simulatableObj), COLLIDER_TRANSFORM));
                  }
               }

               for (ClothSimulator.ClothObject.ClothPart part : this.parts.values()) {
                  part.substepTick(gravity, subSteppingDeltaTime, i + 1, this.clothColliders);
               }
            }
         }

         this.updateNormal(false);
         if (!this.normalOffsetParticles.isEmpty()) {
            for (ClothSimulator.ClothObject.ClothPart.OffsetParticle offsetParticle : this.normalOffsetParticles.values()) {
               ClothSimulator.ClothObject.Particle rootParticle = offsetParticle.rootParticle();
               Map<Integer, Vec3f> rootNormalMap = this.particleNormals.get(rootParticle.meshVertexId);
               OFFSET.set(0.0F, 0.0F, 0.0F);

               for (Integer normIdx : offsetParticle.positionNormalMembers()) {
                  OFFSET.add(rootNormalMap.get(normIdx).normalize());
               }

               OFFSET.scale(offsetParticle.length / OFFSET.length());
               offsetParticle.position.set(rootParticle.position.x - OFFSET.x, rootParticle.position.y - OFFSET.y, rootParticle.position.z - OFFSET.z);
            }
         }

         this.updateNormal(true);
         this.captureModelPosition(INVERTED);
      }

      private void updateNormal(boolean updateOffsetParticles) {
         SoftBodyTranslatable softBodyMesh = this.provider;

         for (MeshPart modelPart : softBodyMesh.getOriginalMesh().getAllParts()) {
            for (int i = 0; i < modelPart.getVertices().size() / 3; i++) {
               VertexBuilder triP1 = modelPart.getVertices().get(i * 3);
               VertexBuilder triP2 = modelPart.getVertices().get(i * 3 + 1);
               VertexBuilder triP3 = modelPart.getVertices().get(i * 3 + 2);
               if (this.particles.containsKey(triP1.position) && this.particles.containsKey(triP2.position) && this.particles.containsKey(triP3.position)
                  ? !updateOffsetParticles
                  : updateOffsetParticles) {
                  Vec3f p1Pos = this.getParticlePosition(triP1.position);
                  Vec3f p2Pos = this.getParticlePosition(triP2.position);
                  Vec3f p3Pos = this.getParticlePosition(triP3.position);
                  Vec3f.cross(Vec3f.sub(p2Pos, p1Pos, TO_P2), Vec3f.sub(p3Pos, p1Pos, TO_P3), CROSS);
                  CROSS.normalize();
                  Map<Integer, Vec3f> triP1Normals = this.particleNormals.get(triP1.position);
                  Map<Integer, Vec3f> triP2Normals = this.particleNormals.get(triP2.position);
                  Map<Integer, Vec3f> triP3Normals = this.particleNormals.get(triP3.position);
                  triP1Normals.get(triP1.normal).add(CROSS);
                  triP2Normals.get(triP2.normal).add(CROSS);
                  triP3Normals.get(triP3.normal).add(CROSS);
               }
            }
         }
      }

      public void scaleFromPose(PoseStack poseStack, OpenMatrix4f[] poses) {
         OpenMatrix4f poseMat = poses[this.parentJoint.getId()];
         poseMat.toScaleVector(SCALE);
         poseStack.m_252880_(poseMat.m30, poseMat.m31, poseMat.m32);
         poseStack.m_85841_(SCALE.x, SCALE.y, SCALE.z);
         poseStack.m_252880_(-poseMat.m30, -poseMat.m31, -poseMat.m32);
      }

      @Override
      public void draw(
         PoseStack poseStack,
         VertexConsumer bufferBuilder,
         Mesh.DrawingFunction drawingFunction,
         int packedLight,
         float r,
         float g,
         float b,
         float a,
         int overlay
      ) {
         if (ClothSimulator.DRAW_OUTLINES) {
            this.drawOutline(
               poseStack, Minecraft.m_91087_().m_91269_().m_110104_().m_6299_(RenderType.m_110504_()), Mesh.DrawingFunction.POSITION_COLOR_NORMAL, r, g, b, a
            );
         } else {
            this.drawParts(poseStack, bufferBuilder, drawingFunction, packedLight, r, g, b, a, overlay);
         }

         if (this.provider instanceof CompositeMesh compositeMesh) {
            poseStack.m_85849_();
            compositeMesh.getStaticMesh().draw(poseStack, bufferBuilder, drawingFunction, packedLight, 1.0F, 1.0F, 1.0F, 1.0F, overlay);
            poseStack.m_85836_();
         }
      }

      @Override
      public void drawPosed(
         PoseStack poseStack,
         VertexConsumer bufferBuilder,
         Mesh.DrawingFunction drawingFunction,
         int packedLight,
         float r,
         float g,
         float b,
         float a,
         int overlay,
         Armature armature,
         OpenMatrix4f[] poses
      ) {
         if (ClothSimulator.DRAW_OUTLINES) {
            this.drawOutline(
               poseStack, Minecraft.m_91087_().m_91269_().m_110104_().m_6299_(RenderType.m_110504_()), Mesh.DrawingFunction.POSITION_COLOR_NORMAL, r, g, b, a
            );
         } else {
            this.drawParts(poseStack, bufferBuilder, drawingFunction, packedLight, r, g, b, a, overlay);
         }

         if (ClothSimulator.DRAW_MESH_COLLIDERS && this.clothColliders != null) {
            for (Pair<Function<ClothSimulatable, OpenMatrix4f>, ClothSimulator.ClothOBBCollider> entry : this.clothColliders) {
               ((ClothSimulator.ClothOBBCollider)entry.getSecond()).draw(poseStack, Minecraft.m_91087_().m_91269_().m_110104_(), -1);
            }
         }

         poseStack.m_85850_().m_252922_().getScale(SCALER);
         float scaleX = SCALER.x;
         float scaleY = SCALER.y;
         float scaleZ = SCALER.z;
         poseStack.m_85849_();
         poseStack.m_85850_().m_252922_().getScale(SCALER);
         float xDiv = scaleX / SCALER.x;
         float yDiv = scaleY / SCALER.y;
         float zDiv = scaleZ / SCALER.z;
         poseStack.m_85841_(xDiv, yDiv, zDiv);
         if (this.provider instanceof CompositeMesh compositeMesh) {
            compositeMesh.getStaticMesh().drawPosed(poseStack, bufferBuilder, drawingFunction, packedLight, 1.0F, 1.0F, 1.0F, 1.0F, overlay, armature, poses);
         }

         poseStack.m_85836_();
      }

      public Vec3f getParticlePosition(int idx) {
         return this.particles.containsKey(idx) ? this.particles.get(idx).position : this.normalOffsetParticles.get(idx).position;
      }

      private void captureModelPosition(OpenMatrix4f objectTranslformInv) {
         for (ClothSimulator.ClothObject.Particle p : this.particles.values()) {
            OpenMatrix4f.transform3v(objectTranslformInv, p.position, p.modelPosition);
         }
      }

      public void drawParts(
         PoseStack poseStack,
         VertexConsumer bufferBuilder,
         Mesh.DrawingFunction drawingFunction,
         int packedLight,
         float r,
         float g,
         float b,
         float a,
         int overlay
      ) {
         SoftBodyTranslatable softBodyMesh = this.provider;
         float[] uvs = softBodyMesh.getOriginalMesh().uvs();

         for (MeshPart meshPart : softBodyMesh.getOriginalMesh().getAllParts()) {
            if (!meshPart.isHidden()) {
               Vector4f color = meshPart.getColor(r, g, b, a);
               Matrix4f matrix4f = poseStack.m_85850_().m_252922_();
               Matrix3f matrix3f = poseStack.m_85850_().m_252943_();

               for (int i = 0; i < meshPart.getVertices().size(); i++) {
                  if (!ClothSimulator.DRAW_NORMAL_OFFSET && i % 3 == 0 && i + 1 != meshPart.getVertices().size() && i + 2 != meshPart.getVertices().size()) {
                     VertexBuilder v1 = meshPart.getVertices().get(i);
                     VertexBuilder v2 = meshPart.getVertices().get(i + 1);
                     VertexBuilder v3 = meshPart.getVertices().get(i + 2);
                     if (!this.particles.containsKey(v1.position) || !this.particles.containsKey(v2.position) || !this.particles.containsKey(v3.position)) {
                        i += 2;
                        continue;
                     }
                  }

                  VertexBuilder vb = meshPart.getVertices().get(i);
                  Vec3f particlePosition = this.getParticlePosition(vb.position);
                  Vec3f poseNormal = this.particleNormals.get(vb.position).get(vb.normal);
                  poseNormal.normalize();
                  POSITION.set(particlePosition.x, particlePosition.y, particlePosition.z);
                  NORMAL.set(poseNormal.x, poseNormal.y, poseNormal.z);
                  POSITION.mul(matrix4f);
                  NORMAL.mul(matrix3f);
                  drawingFunction.draw(
                     bufferBuilder,
                     POSITION.x,
                     POSITION.y,
                     POSITION.z,
                     NORMAL.x(),
                     NORMAL.y(),
                     NORMAL.z(),
                     packedLight,
                     color.x,
                     color.y,
                     color.z,
                     color.w,
                     uvs[vb.uv * 2],
                     uvs[vb.uv * 2 + 1],
                     overlay
                  );
               }
            }
         }
      }

      public void drawOutline(PoseStack poseStack, VertexConsumer builder, Mesh.DrawingFunction drawingFunction, float r, float g, float b, float a) {
         SoftBodyTranslatable softBodyMesh = this.provider;

         for (MeshPart meshPart : softBodyMesh.getOriginalMesh().getAllParts()) {
            if (!meshPart.isHidden()) {
               Matrix4f matrix4f = poseStack.m_85850_().m_252922_();
               Matrix3f matrix3f = poseStack.m_85850_().m_252943_();

               for (int i = 0; i < meshPart.getVertices().size() / 3; i++) {
                  VertexBuilder v1 = meshPart.getVertices().get(i * 3);
                  VertexBuilder v2 = meshPart.getVertices().get(i * 3 + 1);
                  VertexBuilder v3 = meshPart.getVertices().get(i * 3 + 2);
                  if (ClothSimulator.DRAW_NORMAL_OFFSET
                     || this.particles.containsKey(v1.position) && this.particles.containsKey(v2.position) && this.particles.containsKey(v3.position)) {
                     Vec3f pos1 = this.getParticlePosition(v1.position);
                     Vec3f pos2 = this.getParticlePosition(v2.position);
                     Vec3f pos3 = this.getParticlePosition(v3.position);
                     POSITION.set(pos1.x, pos1.y, pos1.z);
                     NORMAL.set(pos2.x - pos1.x, pos2.x - pos1.x, pos2.x - pos1.x);
                     POSITION.mul(matrix4f);
                     NORMAL.mul(matrix3f);
                     drawingFunction.draw(builder, POSITION.x, POSITION.y, POSITION.z, NORMAL.x(), NORMAL.y(), NORMAL.z(), -1, r, g, b, a, 0.0F, 0.0F, 0);
                     POSITION.set(pos2.x, pos2.y, pos2.z);
                     POSITION.mul(matrix4f);
                     drawingFunction.draw(builder, POSITION.x, POSITION.y, POSITION.z, NORMAL.x(), NORMAL.y(), NORMAL.z(), -1, r, g, b, a, 0.0F, 0.0F, 0);
                     POSITION.set(pos2.x, pos2.y, pos2.z);
                     NORMAL.set(pos3.x - pos2.x, pos3.x - pos2.x, pos3.x - pos2.x);
                     POSITION.mul(matrix4f);
                     NORMAL.mul(matrix3f);
                     drawingFunction.draw(builder, POSITION.x, POSITION.y, POSITION.z, NORMAL.x(), NORMAL.y(), NORMAL.z(), -1, r, g, b, a, 0.0F, 0.0F, 0);
                     POSITION.set(pos3.x, pos3.y, pos3.z);
                     POSITION.mul(matrix4f);
                     drawingFunction.draw(builder, POSITION.x, POSITION.y, POSITION.z, NORMAL.x(), NORMAL.y(), NORMAL.z(), -1, r, g, b, a, 0.0F, 0.0F, 0);
                     POSITION.set(pos3.x, pos3.y, pos3.z);
                     NORMAL.set(pos1.x - pos3.x, pos1.x - pos3.x, pos1.x - pos3.x);
                     POSITION.mul(matrix4f);
                     NORMAL.mul(matrix3f);
                     drawingFunction.draw(builder, POSITION.x, POSITION.y, POSITION.z, NORMAL.x(), NORMAL.y(), NORMAL.z(), -1, r, g, b, a, 0.0F, 0.0F, 0);
                     POSITION.set(pos1.x, pos1.y, pos1.z);
                     POSITION.mul(matrix4f);
                     drawingFunction.draw(builder, POSITION.x, POSITION.y, POSITION.z, NORMAL.x(), NORMAL.y(), NORMAL.z(), -1, r, g, b, a, 0.0F, 0.0F, 0);
                  }
               }
            }
         }
      }

      public void drawNormals(PoseStack poseStack, VertexConsumer builder, Mesh.DrawingFunction drawingFunction, float r, float g, float b, float a) {
         if (!this.normalOffsetParticles.isEmpty()) {
            Matrix4f matrix4f = poseStack.m_85850_().m_252922_();
            Matrix3f matrix3f = poseStack.m_85850_().m_252943_();

            for (ClothSimulator.ClothObject.ClothPart.OffsetParticle offsetParticle : this.normalOffsetParticles.values()) {
               ClothSimulator.ClothObject.Particle rootParticle = offsetParticle.rootParticle();
               Map<Integer, Vec3f> rootNormalMap = this.particleNormals.get(rootParticle.meshVertexId);
               if (rootNormalMap.size() >= 2) {
                  OFFSET.set(0.0F, 0.0F, 0.0F);

                  for (Integer normIdx : offsetParticle.positionNormalMembers()) {
                     OFFSET.add(rootNormalMap.get(normIdx).normalize());
                  }

                  OFFSET.scale(offsetParticle.length / OFFSET.length());
                  Vec3f rootpos = this.getParticlePosition(rootParticle.meshVertexId);
                  POSITION.set(rootpos.x, rootpos.y, rootpos.z);
                  NORMAL.set(-OFFSET.x, -OFFSET.x, -OFFSET.x);
                  POSITION.mul(matrix4f);
                  NORMAL.mul(matrix3f);
                  drawingFunction.draw(builder, POSITION.x, POSITION.y, POSITION.z, NORMAL.x(), NORMAL.y(), NORMAL.z(), -1, r, g, b, a, 0.0F, 0.0F, 0);
                  POSITION.set(rootpos.x - OFFSET.x, rootpos.y - OFFSET.y, rootpos.z - OFFSET.z);
                  POSITION.mul(matrix4f);
                  drawingFunction.draw(builder, POSITION.x, POSITION.y, POSITION.z, NORMAL.x(), NORMAL.y(), NORMAL.z(), -1, r, g, b, a, 0.0F, 0.0F, 0);
               }
            }
         }
      }

      @Override
      public void initialize() {
      }

      public class ClothPart {
         final List<ClothSimulator.ClothObject.Particle> particleList = Lists.newArrayList();
         final List<ClothSimulator.ClothObject.ClothPart.ConstraintList> constraints;
         final Multimap<Integer, ClothSimulator.ClothObject.Particle> spatialHash;
         final float selfCollision;
         final float particleMass;
         final int hashTableSize;
         private static final Vec3f AVERAGE = new Vec3f();
         private static final Vec3f VEC3F = new Vec3f();
         private static final Vector4f POSITION = new Vector4f(0.0F, 0.0F, 0.0F, 1.0F);
         private static final Vec3f DIFF = new Vec3f();
         private static final Vec3f PARTIAL_VELOCITY = new Vec3f();

         ClothPart(SoftBodyTranslatable.ClothSimulationInfo clothInfo, float[] positions) {
            com.google.common.collect.ImmutableList.Builder<ClothSimulator.ClothObject.ClothPart.ConstraintList> constraintsBuilder = ImmutableList.builder();
            this.selfCollision = clothInfo.selfCollision();
            this.particleMass = clothInfo.particleMass();

            for (int i = 0; i < clothInfo.particles().length / 2; i++) {
               int positionIndex = clothInfo.particles()[i * 2];
               int weightIndex = clothInfo.particles()[i * 2 + 1];
               float influence = clothInfo.weights()[weightIndex];
               float rootDistance = clothInfo.rootDistance()[i];
               float x = positions[positionIndex * 3];
               float y = positions[positionIndex * 3 + 1];
               float z = positions[positionIndex * 3 + 2];
               ClothSimulator.ClothObject.Particle particle = ClothObject.this.new Particle(new Vec3f(x, y, z), influence, rootDistance, positionIndex);
               ClothObject.this.particles.put(positionIndex, particle);
               this.particleList.add(particle);
            }

            this.hashTableSize = this.particleList.size() * 2;
            this.spatialHash = HashMultimap.create(this.hashTableSize, 2);
            int idx = 0;

            for (int[] constraints : clothInfo.constraints()) {
               float compliance = clothInfo.compliances()[idx];
               ClothSimulator.ClothObject.ClothPart.ConstraintType constraintType = clothInfo.constraintTypes()[idx];
               idx++;
               switch (constraintType) {
                  case STRETCHING:
                     List<ClothSimulator.ClothObject.ClothPart.Constraint> constraintList = new ArrayList<>(constraints.length / 2);

                     for (int i = 0; i < constraints.length / 2; i++) {
                        int idx1 = constraints[i * 2];
                        int idx2 = constraints[i * 2 + 1];
                        constraintList.add(
                           new ClothSimulator.ClothObject.ClothPart.StretchingConstraint(
                              ClothObject.this.particles.get(idx1), ClothObject.this.particles.get(idx2)
                           )
                        );
                     }

                     constraintsBuilder.add(new ClothSimulator.ClothObject.ClothPart.ConstraintList(compliance, constraintType, constraintList));
                     break;
                  case SHAPING:
                     List<ClothSimulator.ClothObject.ClothPart.Constraint> constraintList = new ArrayList<>(constraints.length / 2);

                     for (int i = 0; i < constraints.length / 2; i++) {
                        int idx1 = constraints[i * 2];
                        int idx2 = constraints[i * 2 + 1];
                        constraintList.add(
                           new ClothSimulator.ClothObject.ClothPart.ShapingConstraint(
                              ClothObject.this.particles.get(idx1), ClothObject.this.particles.get(idx2)
                           )
                        );
                     }

                     constraintsBuilder.add(new ClothSimulator.ClothObject.ClothPart.ConstraintList(compliance, constraintType, constraintList));
                     break;
                  case BENDING:
                     List<ClothSimulator.ClothObject.ClothPart.Constraint> constraintList = new ArrayList<>(constraints.length / 4);

                     for (int i = 0; i < constraints.length / 4; i++) {
                        int idx1 = constraints[i * 4];
                        int idx2 = constraints[i * 4 + 1];
                        int idx3 = constraints[i * 4 + 2];
                        int idx4 = constraints[i * 4 + 3];
                        constraintList.add(
                           new ClothSimulator.ClothObject.ClothPart.BendingConstraint(
                              ClothObject.this.particles.get(idx1),
                              ClothObject.this.particles.get(idx2),
                              ClothObject.this.particles.get(idx3),
                              ClothObject.this.particles.get(idx4)
                           )
                        );
                     }

                     constraintsBuilder.add(new ClothSimulator.ClothObject.ClothPart.ConstraintList(compliance, constraintType, constraintList));
                     break;
                  case VOLUME:
                     List<ClothSimulator.ClothObject.ClothPart.Constraint> constraintList = new ArrayList<>(constraints.length / 4);

                     for (int i = 0; i < constraints.length / 4; i++) {
                        int idx1 = constraints[i * 4];
                        int idx2 = constraints[i * 4 + 1];
                        int idx3 = constraints[i * 4 + 2];
                        int idx4 = constraints[i * 4 + 3];
                        constraintList.add(
                           new ClothSimulator.ClothObject.ClothPart.VolumeConstraint(
                              ClothObject.this.particles.get(idx1),
                              ClothObject.this.particles.get(idx2),
                              ClothObject.this.particles.get(idx3),
                              ClothObject.this.particles.get(idx4)
                           )
                        );
                     }

                     constraintsBuilder.add(new ClothSimulator.ClothObject.ClothPart.ConstraintList(compliance, constraintType, constraintList));
               }
            }

            this.constraints = constraintsBuilder.build();
            if (clothInfo.normalOffsetMapping() != null) {
               for (int i = 0; i < clothInfo.normalOffsetMapping().length / 2; i++) {
                  int rootParticle = clothInfo.normalOffsetMapping()[i * 2];
                  int offsetParticleIdx = clothInfo.normalOffsetMapping()[i * 2 + 1];
                  Vec3f offsetDirection = new Vec3f(
                     positions[offsetParticleIdx * 3] - positions[rootParticle * 3],
                     positions[offsetParticleIdx * 3 + 1] - positions[rootParticle * 3 + 1],
                     positions[offsetParticleIdx * 3 + 2] - positions[rootParticle * 3 + 2]
                  );
                  List<Integer> positionNormalMembers = Lists.newArrayList();
                  List<Integer> inverseNormals = Lists.newArrayList();
                  ClothSimulator.ClothObject.ClothPart.OffsetParticle offsetParticle = new ClothSimulator.ClothObject.ClothPart.OffsetParticle(
                     offsetParticleIdx,
                     offsetDirection.length(),
                     ClothObject.this.particles.get(rootParticle),
                     new Vec3f(),
                     positionNormalMembers,
                     inverseNormals
                  );
                  offsetDirection.normalize();
                  Map<Integer, Vec3f> rootNormalMap = ClothObject.this.particleNormals.get(rootParticle);
                  List<Vec3f> rootNormals = new ArrayList<>(rootNormalMap.values());
                  List<Set<Integer>> normalSubsets = new ArrayList<>(MathUtils.getSubset(IntStream.rangeClosed(0, rootNormals.size() - 1).boxed().toList()));
                  int candidate = -1;
                  int loopIdx = 0;
                  float maxDot = -10000.0F;

                  for (Set<Integer> subset : normalSubsets) {
                     Set<Vec3f> rootNormal = subset.stream().map(normIdx -> rootNormals.get(normIdx)).collect(Collectors.toSet());
                     Vec3f.average(rootNormal, AVERAGE);
                     AVERAGE.scale(-1.0F);
                     AVERAGE.normalize();
                     float dot = Vec3f.dot(offsetDirection, AVERAGE);
                     if (maxDot < dot) {
                        maxDot = dot;
                        candidate = loopIdx;
                     }

                     loopIdx++;
                  }

                  normalSubsets.get(candidate).forEach(orderIdx -> {
                     int iterCountx = 0;

                     for (Entry<Integer, Vec3f> entryx : rootNormalMap.entrySet()) {
                        if (orderIdx == iterCountx) {
                           positionNormalMembers.add(entryx.getKey());
                           break;
                        }

                        iterCountx++;
                     }
                  });
                  ClothObject.this.normalOffsetParticles.put(offsetParticleIdx, offsetParticle);

                  for (Vec3f normal : ClothObject.this.particleNormals.get(offsetParticleIdx).values()) {
                     int leastDotIdx = MathUtils.getLeastAngleVectorIdx(normal, rootNormals.toArray(new Vec3f[0]));
                     int iterCount = 0;

                     for (Entry<Integer, Vec3f> entry : rootNormalMap.entrySet()) {
                        if (leastDotIdx == iterCount) {
                           inverseNormals.add(entry.getKey());
                           break;
                        }

                        iterCount++;
                     }
                  }
               }
            }
         }

         public void buildSpatialHash() {
            this.spatialHash.clear();

            for (ClothSimulator.ClothObject.Particle p : this.particleList) {
               int hash = this.getHash(p.position.x, p.position.y, p.position.z);
               this.spatialHash.put(hash, p);
            }
         }

         public void tick(OpenMatrix4f objectTransform, Vec3f externalForce, OpenMatrix4f[] poses) {
            for (ClothSimulator.ClothObject.Particle p : this.particleList) {
               p.velocity.scale(0.92F);
               p.velocity
                  .add(
                     externalForce.x * p.rootDistance * p.influence * this.particleMass,
                     externalForce.y * p.rootDistance * p.influence * this.particleMass,
                     externalForce.z * p.rootDistance * p.influence * this.particleMass
                  );
               if (p.collided) {
                  VEC3F.set(p.modelPosition);
                  OpenMatrix4f.transform3v(objectTransform, VEC3F, ClothSimulator.ClothObject.TRASNFORMED);
                  p.position.set(ClothSimulator.ClothObject.TRASNFORMED);
               } else {
                  float influenceInv = 1.0F - p.influence;
                  if (influenceInv > 0.0F) {
                     ClothObject.this.provider.getOriginalMesh().getVertexPosition(p.meshVertexId, POSITION, poses);
                     VEC3F.set(POSITION.x, POSITION.y, POSITION.z);
                     OpenMatrix4f.transform3v(objectTransform, VEC3F, ClothSimulator.ClothObject.TRASNFORMED);
                     Vec3f.interpolate(p.position, ClothSimulator.ClothObject.TRASNFORMED, influenceInv, ClothSimulator.ClothObject.TRASNFORMED);
                     p.position.set(ClothSimulator.ClothObject.TRASNFORMED);
                  }
               }
            }
         }

         public void substepTick(
            float substepGravity,
            float substepDeltaTime,
            int stepCount,
            List<Pair<Function<ClothSimulatable, OpenMatrix4f>, ClothSimulator.ClothOBBCollider>> clothColliders
         ) {
            for (ClothSimulator.ClothObject.Particle p : this.particleList) {
               p.position.y = p.position.y - substepGravity * this.particleMass * p.influence;
               p.position.add(Vec3f.scale(p.velocity, PARTIAL_VELOCITY, 0.16666667F));
            }

            for (ClothSimulator.ClothObject.ClothPart.ConstraintList constraintsBundle : this.constraints) {
               float alpha = constraintsBundle.compliance() / (substepDeltaTime * substepDeltaTime);

               for (ClothSimulator.ClothObject.ClothPart.Constraint c : constraintsBundle.constraints()) {
                  c.solve(alpha, stepCount);
               }
            }

            if (stepCount == 1) {
               this.buildSpatialHash();
            }

            for (ClothSimulator.ClothObject.Particle p1 : this.particleList) {
               int hash = this.getHash(p1.position.x, p1.position.y, p1.position.z);

               for (ClothSimulator.ClothObject.Particle p2 : this.spatialHash.get(hash)) {
                  if (p1 != p2) {
                     float influenceSum = p1.influence + p2.influence;
                     if (influenceSum != 0.0F) {
                        Vec3f.sub(p1.position, p2.position, VEC3F);
                        float length = VEC3F.length();
                        if (length < this.selfCollision) {
                           float scale = (this.selfCollision - length) / this.selfCollision;
                           float p1Move = p1.influence / influenceSum;
                           float p2Move = p2.influence / influenceSum;
                           VEC3F.scale(scale);
                           p1.position.add(VEC3F.x * p1Move, VEC3F.y * p1Move, VEC3F.z * p1Move);
                           p2.position.sub(VEC3F.x * p2Move, VEC3F.y * p2Move, VEC3F.z * p2Move);
                        }
                     }
                  }
               }
            }

            if (clothColliders != null) {
               for (ClothSimulator.ClothObject.ClothPart.ConstraintList constraintList : this.constraints) {
                  if (constraintList.constraintType() == ClothSimulator.ClothObject.ClothPart.ConstraintType.SHAPING) {
                     List<ClothSimulator.ClothObject.ClothPart.ShapingConstraint> constraints = constraintList.constraints();
                     List<ClothSimulator.ClothOBBCollider> colliders = Lists.newArrayList();
                     List<Vec3f> destinations = Lists.newArrayList();

                     for (ClothSimulator.ClothObject.ClothPart.ShapingConstraint constraint : constraints) {
                        if (constraint.p1.influence != 0.0F || constraint.p2.influence != 0.0F) {
                           for (Pair<Function<ClothSimulatable, OpenMatrix4f>, ClothSimulator.ClothOBBCollider> entry : clothColliders) {
                              ClothSimulator.ClothOBBCollider clothCollider = (ClothSimulator.ClothOBBCollider)entry.getSecond();
                              if (clothCollider.getOuterAABB(this.selfCollision * 0.5F)
                                    .m_82393_(constraint.p2.position.x, constraint.p2.position.y, constraint.p2.position.z)
                                 && !clothCollider.doesPointCollide(constraint.p1.position.toDoubleVector(), this.selfCollision * 0.5F)) {
                                 colliders.add((ClothSimulator.ClothOBBCollider)entry.getSecond());
                              }
                           }

                           for (ClothSimulator.ClothOBBCollider collider : colliders) {
                              collider.pushIfPointInside(constraint.p2.position, constraint.p1.position, this.selfCollision * 0.5F, destinations, colliders);
                           }

                           int i = Vec3f.getNearest(constraint.p2.position, destinations);
                           constraint.p2.collided = i != -1;
                           if (i != -1) {
                              Vec3f nearest = destinations.get(i);
                              Vec3f.sub(nearest, constraint.p2.position, DIFF);
                              constraint.p2.position.set(nearest);
                           }

                           colliders.clear();
                           destinations.clear();
                        }
                     }
                  }
               }
            }
         }

         private int getHash(double x, double y, double z) {
            int xi = (int)Math.floor(x / 0.05F);
            int yi = (int)Math.floor(y / 0.05F);
            int zi = (int)Math.floor(z / 0.05F);
            int hash = xi * 92837111 ^ yi * 689287499 ^ zi * 283923481;
            return Math.abs(hash) % this.hashTableSize;
         }

         class BendingConstraint extends ClothSimulator.ClothObject.ClothPart.Constraint {
            final ClothSimulator.ClothObject.Particle p1;
            final ClothSimulator.ClothObject.Particle p2;
            final ClothSimulator.ClothObject.Particle p3;
            final ClothSimulator.ClothObject.Particle p4;
            final float restAngle;
            final float oppositeDistance;
            static final Vec3f[] GRADIENTS = new Vec3f[]{new Vec3f(), new Vec3f(), new Vec3f(), new Vec3f()};
            static final Vec3f NORMAL_SUM = new Vec3f();
            static float STIFFNESS = 1.0F;
            static final Vec3f P2P1 = new Vec3f();
            static final Vec3f P3P1 = new Vec3f();
            static final Vec3f P4P2 = new Vec3f();
            static final Vec3f P4P3 = new Vec3f();
            static final Vec3f EDGE = new Vec3f();
            static final Vec3f EDGE_NORM = new Vec3f();
            static final Vec3f CROSS1 = new Vec3f();
            static final Vec3f CROSS2 = new Vec3f();
            static final Vec3f CROSS3 = new Vec3f();

            BendingConstraint(
               ClothSimulator.ClothObject.Particle p1,
               ClothSimulator.ClothObject.Particle p2,
               ClothSimulator.ClothObject.Particle p3,
               ClothSimulator.ClothObject.Particle p4
            ) {
               this.p1 = p1;
               this.p2 = p2;
               this.p3 = p3;
               this.p4 = p4;
               this.restAngle = this.getDihedralAngle();
               this.oppositeDistance = Vec3f.sub(this.p1.position, this.p4.position, null).lengthSqr();
            }

            @Override
            void solve(float alpha, int stepcount) {
               float influenceSum = this.p1.influence + this.p2.influence + this.p3.influence + this.p4.influence;
               if (!(influenceSum < 1.0E-8)) {
                  float currentAngle = this.getDihedralAngle();
                  float constraint = this.restAngle - currentAngle;

                  while (constraint > Math.PI) {
                     constraint = (float)(constraint - (Math.PI * 2));
                  }

                  while (constraint < -Math.PI) {
                     constraint = (float)(constraint + (Math.PI * 2));
                  }

                  constraint = this.oppositeDistance * constraint;
                  float edgeLength = EDGE.length();
                  CROSS1.scale(edgeLength);
                  CROSS2.scale(edgeLength);
                  GRADIENTS[0].set(CROSS1);
                  GRADIENTS[3].set(CROSS2);
                  Vec3f.add(CROSS1, CROSS2, NORMAL_SUM);
                  NORMAL_SUM.scale(-0.5F);
                  GRADIENTS[1].set(NORMAL_SUM);
                  GRADIENTS[2].set(NORMAL_SUM);
                  float weight = this.p1.influence * GRADIENTS[0].lengthSqr()
                     + this.p2.influence * GRADIENTS[1].lengthSqr()
                     + this.p3.influence * GRADIENTS[2].lengthSqr()
                     + this.p4.influence * GRADIENTS[3].lengthSqr();
                  if (!(weight < 1.0E-8)) {
                     float force = -constraint * STIFFNESS / (influenceSum + alpha);
                     GRADIENTS[0].scale(force * this.p1.influence);
                     GRADIENTS[1].scale(force * this.p2.influence);
                     GRADIENTS[2].scale(force * this.p3.influence);
                     GRADIENTS[3].scale(force * this.p4.influence);
                     Vec3f.add(this.p1.position, GRADIENTS[0], this.p1.position);
                     Vec3f.add(this.p2.position, GRADIENTS[1], this.p2.position);
                     Vec3f.add(this.p3.position, GRADIENTS[2], this.p3.position);
                     Vec3f.add(this.p4.position, GRADIENTS[3], this.p4.position);
                  }
               }
            }

            public float getDihedralAngle() {
               Vec3f.sub(this.p1.position, this.p2.position, P2P1);
               Vec3f.sub(this.p1.position, this.p3.position, P3P1);
               Vec3f.sub(this.p4.position, this.p2.position, P4P2);
               Vec3f.sub(this.p4.position, this.p3.position, P4P3);
               Vec3f.sub(this.p3.position, this.p2.position, EDGE);
               Vec3f.cross(P2P1, P3P1, CROSS1);
               Vec3f.cross(P4P3, P4P2, CROSS2);
               CROSS1.normalize();
               CROSS2.normalize();
               Vec3f.normalize(EDGE, EDGE_NORM);
               float cos = Vec3f.dot(CROSS1, CROSS2);
               float sin = Vec3f.dot(Vec3f.cross(CROSS1, CROSS2, CROSS3), EDGE_NORM);
               return (float)Math.atan2(sin, cos);
            }
         }

         abstract class Constraint {
            abstract void solve(float var1, int var2);
         }

         public record ConstraintList(
            float compliance,
            ClothSimulator.ClothObject.ClothPart.ConstraintType constraintType,
            List<? extends ClothSimulator.ClothObject.ClothPart.Constraint> constraints
         ) {
         }

         public enum ConstraintType {
            STRETCHING,
            SHAPING,
            BENDING,
            VOLUME;
         }

         public record OffsetParticle(
            int offsetVertexId,
            float length,
            ClothSimulator.ClothObject.Particle rootParticle,
            Vec3f position,
            List<Integer> positionNormalMembers,
            List<Integer> inverseNormal
         ) {
            public ClothSimulator.ClothObject.ClothPart.OffsetParticle copy() {
               return new ClothSimulator.ClothObject.ClothPart.OffsetParticle(
                  this.offsetVertexId, this.length, this.rootParticle, this.position.copy(), this.positionNormalMembers, this.inverseNormal
               );
            }
         }

         class ShapingConstraint extends ClothSimulator.ClothObject.ClothPart.Constraint {
            final ClothSimulator.ClothObject.Particle p1;
            final ClothSimulator.ClothObject.Particle p2;
            final float restLength;
            static final Vec3f TOWARD = new Vec3f();

            ShapingConstraint(ClothSimulator.ClothObject.Particle p1, ClothSimulator.ClothObject.Particle p2) {
               this.p1 = p1;
               this.p2 = p2;
               this.restLength = p1.position.distance(p2.position);
            }

            @Override
            void solve(float alpha, int stepcount) {
               float p1Influence = stepcount == 6 && !this.p1.collided ? 0.0F : this.p1.influence;
               float p2Influence = this.p2.influence;
               float influenceSum = p1Influence + p2Influence;
               if (!(influenceSum < 1.0E-5)) {
                  Vec3f.sub(this.p2.position, this.p1.position, TOWARD);
                  float distanceLength = TOWARD.length();
                  if (distanceLength != 0.0F) {
                     TOWARD.scale(1.0F / distanceLength);
                     float distanceGap = distanceLength - this.restLength;
                     float force = distanceGap / (influenceSum + alpha);
                     float p1Move = force * p1Influence;
                     float p2Move = -force * p2Influence;
                     this.p1.position.add(TOWARD.x * p1Move, TOWARD.y * p1Move, TOWARD.z * p1Move);
                     this.p2.position.add(TOWARD.x * p2Move, TOWARD.y * p2Move, TOWARD.z * p2Move);
                  }
               }
            }
         }

         class StretchingConstraint extends ClothSimulator.ClothObject.ClothPart.Constraint {
            final ClothSimulator.ClothObject.Particle p1;
            final ClothSimulator.ClothObject.Particle p2;
            final float restLength;
            static final Vec3f GRADIENT = new Vec3f();

            StretchingConstraint(ClothSimulator.ClothObject.Particle p1, ClothSimulator.ClothObject.Particle p2) {
               this.p1 = p1;
               this.p2 = p2;
               this.restLength = p1.position.distance(p2.position);
            }

            @Override
            void solve(float alpha, int stepcount) {
               float p1Influence = this.p1.influence;
               float p2Influence = this.p2.influence;
               float influenceSum = p1Influence + p2Influence;
               if (!(influenceSum < 1.0E-8)) {
                  Vec3f.sub(this.p2.position, this.p1.position, GRADIENT);
                  float currentLength = GRADIENT.length();
                  if (!(currentLength < 1.0E-8)) {
                     GRADIENT.scale(1.0F / currentLength);
                     float constraint = currentLength - this.restLength;
                     float force = constraint / (influenceSum + alpha);
                     float p1Move = force * p1Influence;
                     float p2Move = -force * p2Influence;
                     this.p1.position.add(GRADIENT.x * p1Move, GRADIENT.y * p1Move, GRADIENT.z * p1Move);
                     this.p2.position.add(GRADIENT.x * p2Move, GRADIENT.y * p2Move, GRADIENT.z * p2Move);
                  }
               }
            }
         }

         class VolumeConstraint extends ClothSimulator.ClothObject.ClothPart.Constraint {
            final ClothSimulator.ClothObject.Particle[] particles = new ClothSimulator.ClothObject.Particle[4];
            final float restVolume;
            static final float SUBDIVISION = 0.16666667F;
            static final int[][] VOLUME_ORDER = new int[][]{{1, 3, 2}, {0, 2, 3}, {0, 3, 1}, {0, 1, 2}};
            static final Vec3f[] SHRINK_DIRECTIONS = new Vec3f[]{new Vec3f(), new Vec3f(), new Vec3f(), new Vec3f()};
            static final Vec3f P1_TO_P2 = new Vec3f();
            static final Vec3f P1_TO_P3 = new Vec3f();
            static final Vec3f P1_TO_P4 = new Vec3f();
            static final Vec3f TET_CROSS = new Vec3f();

            VolumeConstraint(
               ClothSimulator.ClothObject.Particle p1,
               ClothSimulator.ClothObject.Particle p2,
               ClothSimulator.ClothObject.Particle p3,
               ClothSimulator.ClothObject.Particle p4
            ) {
               this.particles[0] = p1;
               this.particles[1] = p2;
               this.particles[2] = p3;
               this.particles[3] = p4;
               this.restVolume = this.getTetrahedralVolume();
            }

            @Override
            void solve(float alpha, int stepcount) {
               float weight = 0.0F;

               for (int i = 0; i < 4; i++) {
                  ClothSimulator.ClothObject.Particle p1 = this.particles[VOLUME_ORDER[i][0]];
                  ClothSimulator.ClothObject.Particle p2 = this.particles[VOLUME_ORDER[i][1]];
                  ClothSimulator.ClothObject.Particle p3 = this.particles[VOLUME_ORDER[i][2]];
                  Vec3f.sub(p2.position, p1.position, P1_TO_P2);
                  Vec3f.sub(p3.position, p1.position, P1_TO_P3);
                  Vec3f.cross(P1_TO_P2, P1_TO_P3, SHRINK_DIRECTIONS[i]);
                  SHRINK_DIRECTIONS[i].scale(0.16666667F);
                  weight += this.particles[i].influence * SHRINK_DIRECTIONS[i].lengthSqr();
               }

               if (!(weight < 1.0E-8)) {
                  float constraint = this.restVolume - this.getTetrahedralVolume();
                  float force = constraint / (weight + alpha);

                  for (int i = 0; i < 4; i++) {
                     SHRINK_DIRECTIONS[i].scale(force * this.particles[i].influence);
                     Vec3f.add(this.particles[i].position, SHRINK_DIRECTIONS[i], this.particles[i].position);
                  }
               }
            }

            float getTetrahedralVolume() {
               Vec3f.sub(this.particles[1].position, this.particles[0].position, P1_TO_P2);
               Vec3f.sub(this.particles[2].position, this.particles[0].position, P1_TO_P3);
               Vec3f.sub(this.particles[3].position, this.particles[0].position, P1_TO_P4);
               Vec3f.cross(P1_TO_P2, P1_TO_P3, TET_CROSS);
               return Vec3f.dot(TET_CROSS, P1_TO_P4) / 6.0F;
            }
         }
      }

      class Particle {
         final Vec3f position;
         final Vec3f modelPosition;
         final Vec3f velocity = new Vec3f();
         final float influence;
         final float rootDistance;
         final int meshVertexId;
         boolean collided;

         Particle(Vec3f position, float influence, float rootDistance, int meshVertexId) {
            this.position = position;
            this.modelPosition = position.copy();
            this.influence = influence;
            this.rootDistance = rootDistance;
            this.meshVertexId = meshVertexId;
            this.collided = false;
         }

         ClothSimulator.ClothObject.Particle copy() {
            return ClothObject.this.new Particle(this.position.copy(), this.influence, this.rootDistance, this.meshVertexId);
         }
      }
   }

   public static class ClothObjectBuilder extends SimulationObject.SimulationObjectBuilder {
      List<Pair<Function<ClothSimulatable, OpenMatrix4f>, ClothSimulator.ClothOBBCollider>> clothColliders = Lists.newArrayList();
      Joint joint;

      public ClothSimulator.ClothObjectBuilder addEntry(
         Function<ClothSimulatable, OpenMatrix4f> obbTransformer, ClothSimulator.ClothOBBCollider clothOBBCollider
      ) {
         this.clothColliders.add(Pair.of(obbTransformer, clothOBBCollider));
         return this;
      }

      public ClothSimulator.ClothObjectBuilder putAll(List<Pair<Function<ClothSimulatable, OpenMatrix4f>, ClothSimulator.ClothOBBCollider>> clothOBBColliders) {
         this.clothColliders.addAll(clothOBBColliders);
         return this;
      }

      public ClothSimulator.ClothObjectBuilder parentJoint(Joint joint) {
         this.joint = joint;
         return this;
      }

      public static ClothSimulator.ClothObjectBuilder create() {
         return new ClothSimulator.ClothObjectBuilder();
      }
   }
}
