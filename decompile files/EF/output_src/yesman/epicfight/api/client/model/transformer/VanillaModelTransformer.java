package yesman.epicfight.api.client.model.transformer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.ModelPart.Cube;
import net.minecraft.client.model.geom.ModelPart.Polygon;
import net.minecraft.client.model.geom.ModelPart.Vertex;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.MeshPartDefinition;
import yesman.epicfight.api.client.model.SingleGroupVertexBuilder;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.QuaternionUtils;
import yesman.epicfight.api.utils.math.Vec2f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.mixin.client.MixinAgeableListModel;

public class VanillaModelTransformer extends HumanoidModelTransformer {
   public static final VanillaModelTransformer.SimpleTransformer HEAD = new VanillaModelTransformer.SimpleTransformer(
      AABB.m_165882_(new Vec3(0.0, -4.0, 0.0), 8.0, 8.0, 8.0), 9
   );
   public static final VanillaModelTransformer.SimpleTransformer LEFT_FEET = new VanillaModelTransformer.SimpleTransformer(
      AABB.m_165882_(new Vec3(0.0, -4.0, 0.0), 8.0, 8.0, 8.0), 5
   );
   public static final VanillaModelTransformer.SimpleTransformer RIGHT_FEET = new VanillaModelTransformer.SimpleTransformer(
      AABB.m_165882_(new Vec3(0.0, -4.0, 0.0), 8.0, 8.0, 8.0), 2
   );
   public static final VanillaModelTransformer.LimbPartTransformer LEFT_ARM = new VanillaModelTransformer.LimbPartTransformer(
      AABB.m_165882_(new Vec3(1.0, 6.0, 0.0), 4.0, 12.0, 4.0), 16, 17, 19, 19.0F, false, AABB.m_165882_(new Vec3(-6.0, 18.0, 0.0), 8.0, 14.0, 8.0)
   );
   public static final VanillaModelTransformer.LimbPartTransformer RIGHT_ARM = new VanillaModelTransformer.LimbPartTransformer(
      AABB.m_165882_(new Vec3(-1.0, 6.0, 0.0), 4.0, 12.0, 4.0), 11, 12, 14, 19.0F, false, AABB.m_165882_(new Vec3(6.0, 18.0, 0.0), 8.0, 14.0, 8.0)
   );
   public static final VanillaModelTransformer.LimbPartTransformer LEFT_LEG = new VanillaModelTransformer.LimbPartTransformer(
      AABB.m_165882_(new Vec3(1.9, 18.0, 0.0), 4.0, 12.0, 4.0), 4, 5, 6, 6.0F, true, AABB.m_165882_(new Vec3(-2.0, 6.0, 0.0), 8.0, 14.0, 8.0)
   );
   public static final VanillaModelTransformer.LimbPartTransformer RIGHT_LEG = new VanillaModelTransformer.LimbPartTransformer(
      AABB.m_165882_(new Vec3(-1.9, 18.0, 0.0), 4.0, 12.0, 4.0), 1, 2, 3, 6.0F, true, AABB.m_165882_(new Vec3(2.0, 6.0, 0.0), 8.0, 14.0, 8.0)
   );
   public static final VanillaModelTransformer.ChestPartTransformer CHEST = new VanillaModelTransformer.ChestPartTransformer(
      AABB.m_165882_(new Vec3(0.0, 6.0, 0.0), 8.0, 12.0, 4.0), 8, 7, 18.0F, AABB.m_165882_(new Vec3(0.0, 18.0, 0.0), 12.0, 14.0, 6.0)
   );

   private static HumanoidModelTransformer.PartTransformer<Cube> getModelPartTransformer(ModelPart modelPart) {
      if (HEAD.coverArea.m_82393_(modelPart.f_104200_, modelPart.f_104201_, modelPart.f_104202_)) {
         return HEAD;
      } else if (LEFT_FEET.coverArea.m_82393_(modelPart.f_104200_, modelPart.f_104201_, modelPart.f_104202_)) {
         return LEFT_FEET;
      } else if (RIGHT_FEET.coverArea.m_82393_(modelPart.f_104200_, modelPart.f_104201_, modelPart.f_104202_)) {
         return RIGHT_FEET;
      } else if (LEFT_ARM.coverArea.m_82393_(modelPart.f_104200_, modelPart.f_104201_, modelPart.f_104202_)) {
         return LEFT_ARM;
      } else if (RIGHT_ARM.coverArea.m_82393_(modelPart.f_104200_, modelPart.f_104201_, modelPart.f_104202_)) {
         return RIGHT_ARM;
      } else if (LEFT_LEG.coverArea.m_82393_(modelPart.f_104200_, modelPart.f_104201_, modelPart.f_104202_)) {
         return LEFT_LEG;
      } else if (RIGHT_LEG.coverArea.m_82393_(modelPart.f_104200_, modelPart.f_104201_, modelPart.f_104202_)) {
         return RIGHT_LEG;
      } else {
         return CHEST.coverArea.m_82393_(modelPart.f_104200_, modelPart.f_104201_, modelPart.f_104202_) ? CHEST : CHEST;
      }
   }

   @Override
   public SkinnedMesh transformArmorModel(HumanoidModel<?> humanoidModel) {
      List<VanillaModelTransformer.VanillaModelPartition> partitions = Lists.newArrayList();
      humanoidModel.f_102808_.m_171322_(humanoidModel.f_102808_.m_233566_());
      humanoidModel.f_102809_.m_171322_(humanoidModel.f_102809_.m_233566_());
      humanoidModel.f_102810_.m_171322_(humanoidModel.f_102810_.m_233566_());
      humanoidModel.f_102812_.m_171322_(humanoidModel.f_102812_.m_233566_());
      humanoidModel.f_102811_.m_171322_(humanoidModel.f_102811_.m_233566_());
      humanoidModel.f_102814_.m_171322_(humanoidModel.f_102814_.m_233566_());
      humanoidModel.f_102813_.m_171322_(humanoidModel.f_102813_.m_233566_());
      List<ModelPart> modelParts = Lists.newArrayList();
      MixinAgeableListModel accessorAgeableListModel = (MixinAgeableListModel)humanoidModel;
      Iterable<ModelPart> headParts = accessorAgeableListModel.invoke_headParts();
      Iterable<ModelPart> bodyParts = accessorAgeableListModel.invoke_bodyParts();
      if (headParts != null) {
         headParts.forEach(modelParts::add);
      }

      if (bodyParts != null) {
         bodyParts.forEach(modelParts::add);
      }

      modelParts.forEach(modelPart -> modelPart.m_171322_(modelPart.m_233566_()));
      if (humanoidModel.f_102808_.f_233556_ || humanoidModel.f_102808_.f_104207_) {
         partitions.add(new VanillaModelTransformer.VanillaModelPartition(HEAD, humanoidModel.f_102808_, "head"));
      }

      if (humanoidModel.f_102809_.f_233556_ || humanoidModel.f_102809_.f_104207_) {
         partitions.add(new VanillaModelTransformer.VanillaModelPartition(HEAD, humanoidModel.f_102809_, "hat"));
      }

      if (humanoidModel.f_102810_.f_233556_ || humanoidModel.f_102810_.f_104207_) {
         partitions.add(new VanillaModelTransformer.VanillaModelPartition(CHEST, humanoidModel.f_102810_, "body"));
      }

      if (humanoidModel.f_102811_.f_233556_ || humanoidModel.f_102811_.f_104207_) {
         partitions.add(new VanillaModelTransformer.VanillaModelPartition(RIGHT_ARM, humanoidModel.f_102811_, "rightArm"));
      }

      if (humanoidModel.f_102812_.f_233556_ || humanoidModel.f_102812_.f_104207_) {
         partitions.add(new VanillaModelTransformer.VanillaModelPartition(LEFT_ARM, humanoidModel.f_102812_, "leftArm"));
      }

      if (humanoidModel.f_102814_.f_233556_ || humanoidModel.f_102814_.f_104207_) {
         partitions.add(new VanillaModelTransformer.VanillaModelPartition(LEFT_LEG, humanoidModel.f_102814_, "leftLeg"));
      }

      if (humanoidModel.f_102813_.f_233556_ || humanoidModel.f_102813_.f_104207_) {
         partitions.add(new VanillaModelTransformer.VanillaModelPartition(RIGHT_LEG, humanoidModel.f_102813_, "rightLeg"));
      }

      modelParts.remove(humanoidModel.f_102808_);
      modelParts.remove(humanoidModel.f_102809_);
      modelParts.remove(humanoidModel.f_102810_);
      modelParts.remove(humanoidModel.f_102811_);
      modelParts.remove(humanoidModel.f_102812_);
      modelParts.remove(humanoidModel.f_102813_);
      modelParts.remove(humanoidModel.f_102814_);
      int i = 0;

      for (ModelPart modelpart : modelParts) {
         if (modelpart.f_233556_ || modelpart.f_104207_) {
            partitions.add(new VanillaModelTransformer.VanillaModelPartition(getModelPartTransformer(modelpart), modelpart, "part" + i++));
         }
      }

      return bakeMeshFromCubes(partitions);
   }

   private static SkinnedMesh bakeMeshFromCubes(List<VanillaModelTransformer.VanillaModelPartition> partitions) {
      List<SingleGroupVertexBuilder> vertices = Lists.newArrayList();
      Map<MeshPartDefinition, IntList> indices = Maps.newHashMap();
      PoseStack poseStack = new PoseStack();
      HumanoidModelTransformer.PartTransformer.IndexCounter indexCounter = new HumanoidModelTransformer.PartTransformer.IndexCounter();
      poseStack.m_252781_(QuaternionUtils.YP.rotationDegrees(180.0F));
      poseStack.m_252781_(QuaternionUtils.XP.rotationDegrees(180.0F));
      poseStack.m_252880_(0.0F, -24.0F, 0.0F);

      for (VanillaModelTransformer.VanillaModelPartition modelpartition : partitions) {
         bake(poseStack, modelpartition.partName, modelpartition, modelpartition.modelPart, vertices, indices, Lists.newArrayList(), indexCounter, false);
      }

      return SingleGroupVertexBuilder.loadVertexInformation(vertices, indices);
   }

   private static void bake(
      PoseStack poseStack,
      String partName,
      VanillaModelTransformer.VanillaModelPartition modelpartition,
      ModelPart part,
      List<SingleGroupVertexBuilder> vertices,
      Map<MeshPartDefinition, IntList> indices,
      List<String> path,
      HumanoidModelTransformer.PartTransformer.IndexCounter indexCounter,
      boolean bindPart
   ) {
      PartPose initialPose = part.m_233566_();
      poseStack.m_85836_();
      poseStack.m_252880_(initialPose.f_171405_, initialPose.f_171406_, initialPose.f_171407_);
      poseStack.m_252781_(new Quaternionf().rotationZYX(initialPose.f_171410_, initialPose.f_171409_, initialPose.f_171408_));
      if (!bindPart) {
         poseStack.m_85841_(part.f_233553_, part.f_233554_, part.f_233555_);
      }

      List<String> newList = new ArrayList<>(path);
      if (bindPart) {
         newList.add(partName);
      }

      if (part.f_104207_ && !part.f_233556_) {
         MeshPartDefinition partDefinition = VanillaModelTransformer.VanillaMeshPartDefinition.of(partName);
         if (bindPart) {
            OpenMatrix4f invertedParentTransform = OpenMatrix4f.importFromMojangMatrix(poseStack.m_85850_().m_252922_());
            invertedParentTransform.m30 *= 0.0625F;
            invertedParentTransform.m31 *= 0.0625F;
            invertedParentTransform.m32 *= 0.0625F;
            invertedParentTransform.invert();
            partDefinition = VanillaModelTransformer.VanillaMeshPartDefinition.of(partName, newList, invertedParentTransform, modelpartition.modelPart);
         }

         for (Cube cube : part.f_104212_) {
            modelpartition.partTransformer.bakeCube(poseStack, partDefinition, cube, vertices, indices, indexCounter);
         }
      }

      for (Entry<String, ModelPart> child : part.f_104213_.entrySet()) {
         bake(poseStack, child.getKey(), modelpartition, child.getValue(), vertices, indices, newList, indexCounter, true);
      }

      poseStack.m_85849_();
   }

   static Direction getDirectionFromVector(Vector3f directionVec) {
      for (Direction direction : Direction.values()) {
         Vector3f direcVec = new Vector3f(Float.compare(directionVec.x(), -0.0F) == 0 ? 0.0F : directionVec.x(), directionVec.y(), directionVec.z());
         if (direcVec.equals(direction.m_253071_())) {
            return direction;
         }
      }

      return null;
   }

   static Vec3 getCenterOfCube(PoseStack poseStack, Cube cube) {
      double minX = Double.MAX_VALUE;
      double minY = Double.MAX_VALUE;
      double minZ = Double.MAX_VALUE;
      double maxX = Double.MIN_VALUE;
      double maxY = Double.MIN_VALUE;
      double maxZ = Double.MIN_VALUE;
      Matrix4f matrix = poseStack.m_85850_().m_252922_();

      for (Polygon quad : cube.f_104341_) {
         for (Vertex v : quad.f_104359_) {
            Vector4f translatedPosition = new Vector4f(v.f_104371_, 1.0F);
            translatedPosition.mul(matrix);
            if (minX > translatedPosition.x()) {
               minX = translatedPosition.x();
            }

            if (minY > translatedPosition.y()) {
               minY = translatedPosition.y();
            }

            if (minZ > translatedPosition.z()) {
               minZ = translatedPosition.z();
            }

            if (maxX < translatedPosition.x()) {
               maxX = translatedPosition.x();
            }

            if (maxY < translatedPosition.y()) {
               maxY = translatedPosition.y();
            }

            if (maxZ < translatedPosition.z()) {
               maxZ = translatedPosition.z();
            }
         }
      }

      return new Vec3(minX + (maxX - minX) * 0.5, minY + (maxY - minY) * 0.5, minZ + (maxZ - minZ) * 0.5);
   }

   static Vector3f getClipPoint(Vector3f pos1, Vector3f pos2, float yClip) {
      Vector3f direct = new Vector3f(pos2);
      direct.sub(pos1);
      direct.mul((yClip - pos1.y()) / (pos2.y() - pos1.y()));
      Vector3f clipPoint = new Vector3f(pos1);
      clipPoint.add(direct);
      return clipPoint;
   }

   static Vertex getTranslatedVertex(Vertex original, Matrix4f matrix) {
      Vector4f translatedPosition = new Vector4f(original.f_104371_, 1.0F);
      translatedPosition.mul(matrix);
      return new Vertex(translatedPosition.x(), translatedPosition.y(), translatedPosition.z(), original.f_104372_, original.f_104373_);
   }

   static class AnimatedPolygon {
      public final VanillaModelTransformer.AnimatedVertex[] animatedVertexPositions;
      public final Vector3f normal;

      public AnimatedPolygon(VanillaModelTransformer.AnimatedVertex[] positionsIn, Direction directionIn) {
         this.animatedVertexPositions = positionsIn;
         this.normal = directionIn.m_253071_();
      }

      public AnimatedPolygon(VanillaModelTransformer.AnimatedVertex[] positionsIn, float cor, Direction directionIn) {
         this.animatedVertexPositions = positionsIn;
         positionsIn[0] = new VanillaModelTransformer.AnimatedVertex(
            positionsIn[0], positionsIn[0].f_104372_, positionsIn[0].f_104373_ + cor, positionsIn[0].jointId, positionsIn[0].weight
         );
         positionsIn[1] = new VanillaModelTransformer.AnimatedVertex(
            positionsIn[1], positionsIn[1].f_104372_, positionsIn[1].f_104373_ + cor, positionsIn[1].jointId, positionsIn[1].weight
         );
         positionsIn[2] = new VanillaModelTransformer.AnimatedVertex(
            positionsIn[2], positionsIn[2].f_104372_, positionsIn[2].f_104373_ - cor, positionsIn[2].jointId, positionsIn[2].weight
         );
         positionsIn[3] = new VanillaModelTransformer.AnimatedVertex(
            positionsIn[3], positionsIn[3].f_104372_, positionsIn[3].f_104373_ - cor, positionsIn[3].jointId, positionsIn[3].weight
         );
         this.normal = directionIn.m_253071_();
      }
   }

   static class AnimatedVertex extends Vertex {
      final Vec3i jointId;
      final Vec3f weight;

      public AnimatedVertex(Vertex posTexVertx, int jointId) {
         this(posTexVertx, jointId, 0, 0, 1.0F, 0.0F, 0.0F);
      }

      public AnimatedVertex(Vertex posTexVertx, int jointId1, int jointId2, int jointId3, float weight1, float weight2, float weight3) {
         this(posTexVertx, new Vec3i(jointId1, jointId2, jointId3), new Vec3f(weight1, weight2, weight3));
      }

      public AnimatedVertex(Vertex posTexVertx, Vec3i ids, Vec3f weights) {
         this(posTexVertx, posTexVertx.f_104372_, posTexVertx.f_104373_, ids, weights);
      }

      public AnimatedVertex(Vertex posTexVertx, float u, float v, Vec3i ids, Vec3f weights) {
         super(posTexVertx.f_104371_.x(), posTexVertx.f_104371_.y(), posTexVertx.f_104371_.z(), u, v);
         this.jointId = ids;
         this.weight = weights;
      }
   }

   static class ChestPartTransformer extends HumanoidModelTransformer.PartTransformer<Cube> {
      static final float X_PLANE = 0.0F;
      static final VanillaModelTransformer.ChestPartTransformer.VertexWeight[] WEIGHT_ALONG_Y = new VanillaModelTransformer.ChestPartTransformer.VertexWeight[]{
         new VanillaModelTransformer.ChestPartTransformer.VertexWeight(13.6666F, 0.23F, 0.77F),
         new VanillaModelTransformer.ChestPartTransformer.VertexWeight(15.8333F, 0.254F, 0.746F),
         new VanillaModelTransformer.ChestPartTransformer.VertexWeight(18.0F, 0.5F, 0.5F),
         new VanillaModelTransformer.ChestPartTransformer.VertexWeight(20.1666F, 0.744F, 0.256F),
         new VanillaModelTransformer.ChestPartTransformer.VertexWeight(22.3333F, 0.77F, 0.23F)
      };
      final VanillaModelTransformer.SimpleTransformer upperAttachmentTransformer;
      final VanillaModelTransformer.SimpleTransformer lowerAttachmentTransformer;
      final AABB noneAttachmentArea;
      final AABB coverArea;
      final float yClipCoord;

      public ChestPartTransformer(AABB coverArea, int upperJoint, int lowerJoint, float yBasis, AABB noneAttachmentArea) {
         this.coverArea = coverArea;
         this.noneAttachmentArea = noneAttachmentArea;
         this.upperAttachmentTransformer = new VanillaModelTransformer.SimpleTransformer(null, upperJoint);
         this.lowerAttachmentTransformer = new VanillaModelTransformer.SimpleTransformer(null, lowerJoint);
         this.yClipCoord = yBasis;
      }

      public void bakeCube(
         PoseStack poseStack,
         MeshPartDefinition partDefinition,
         Cube cube,
         List<SingleGroupVertexBuilder> vertices,
         Map<MeshPartDefinition, IntList> indices,
         HumanoidModelTransformer.PartTransformer.IndexCounter indexCounter
      ) {
         Vec3 centerOfCube = VanillaModelTransformer.getCenterOfCube(poseStack, cube);
         if (!this.noneAttachmentArea.m_82390_(centerOfCube)) {
            if (centerOfCube.f_82480_ < this.yClipCoord) {
               this.lowerAttachmentTransformer.bakeCube(poseStack, partDefinition, cube, vertices, indices, indexCounter);
            } else {
               this.upperAttachmentTransformer.bakeCube(poseStack, partDefinition, cube, vertices, indices, indexCounter);
            }
         } else {
            List<VanillaModelTransformer.AnimatedPolygon> xClipPolygons = Lists.newArrayList();
            List<VanillaModelTransformer.AnimatedPolygon> xyClipPolygons = Lists.newArrayList();

            for (Polygon polygon : cube.f_104341_) {
               Matrix4f matrix = poseStack.m_85850_().m_252922_();
               Vertex pos0 = VanillaModelTransformer.getTranslatedVertex(polygon.f_104359_[0], matrix);
               Vertex pos1 = VanillaModelTransformer.getTranslatedVertex(polygon.f_104359_[1], matrix);
               Vertex pos2 = VanillaModelTransformer.getTranslatedVertex(polygon.f_104359_[2], matrix);
               Vertex pos3 = VanillaModelTransformer.getTranslatedVertex(polygon.f_104359_[3], matrix);
               Direction direction = VanillaModelTransformer.getDirectionFromVector(polygon.f_104360_);
               VanillaModelTransformer.ChestPartTransformer.VertexWeight pos0Weight = getYClipWeight(pos0.f_104371_.y());
               VanillaModelTransformer.ChestPartTransformer.VertexWeight pos1Weight = getYClipWeight(pos1.f_104371_.y());
               VanillaModelTransformer.ChestPartTransformer.VertexWeight pos2Weight = getYClipWeight(pos2.f_104371_.y());
               VanillaModelTransformer.ChestPartTransformer.VertexWeight pos3Weight = getYClipWeight(pos3.f_104371_.y());
               if (pos1.f_104371_.x() > 0.0F != pos2.f_104371_.x() > 0.0F) {
                  float distance = pos2.f_104371_.x() - pos1.f_104371_.x();
                  float textureU = pos1.f_104372_ + (pos2.f_104372_ - pos1.f_104372_) * ((0.0F - pos1.f_104371_.x()) / distance);
                  Vertex pos4 = new Vertex(0.0F, pos0.f_104371_.y(), pos0.f_104371_.z(), textureU, pos0.f_104373_);
                  Vertex pos5 = new Vertex(0.0F, pos1.f_104371_.y(), pos1.f_104371_.z(), textureU, pos1.f_104373_);
                  xClipPolygons.add(
                     new VanillaModelTransformer.AnimatedPolygon(
                        new VanillaModelTransformer.AnimatedVertex[]{
                           new VanillaModelTransformer.AnimatedVertex(pos0, 8, 7, 0, pos0Weight.chestWeight, pos0Weight.torsoWeight, 0.0F),
                           new VanillaModelTransformer.AnimatedVertex(pos4, 8, 7, 0, pos0Weight.chestWeight, pos0Weight.torsoWeight, 0.0F),
                           new VanillaModelTransformer.AnimatedVertex(pos5, 8, 7, 0, pos1Weight.chestWeight, pos1Weight.torsoWeight, 0.0F),
                           new VanillaModelTransformer.AnimatedVertex(pos3, 8, 7, 0, pos3Weight.chestWeight, pos3Weight.torsoWeight, 0.0F)
                        },
                        direction
                     )
                  );
                  xClipPolygons.add(
                     new VanillaModelTransformer.AnimatedPolygon(
                        new VanillaModelTransformer.AnimatedVertex[]{
                           new VanillaModelTransformer.AnimatedVertex(pos4, 8, 7, 0, pos0Weight.chestWeight, pos0Weight.torsoWeight, 0.0F),
                           new VanillaModelTransformer.AnimatedVertex(pos1, 8, 7, 0, pos1Weight.chestWeight, pos1Weight.torsoWeight, 0.0F),
                           new VanillaModelTransformer.AnimatedVertex(pos2, 8, 7, 0, pos2Weight.chestWeight, pos2Weight.torsoWeight, 0.0F),
                           new VanillaModelTransformer.AnimatedVertex(pos5, 8, 7, 0, pos1Weight.chestWeight, pos1Weight.torsoWeight, 0.0F)
                        },
                        direction
                     )
                  );
               } else {
                  xClipPolygons.add(
                     new VanillaModelTransformer.AnimatedPolygon(
                        new VanillaModelTransformer.AnimatedVertex[]{
                           new VanillaModelTransformer.AnimatedVertex(pos0, 8, 7, 0, pos0Weight.chestWeight, pos0Weight.torsoWeight, 0.0F),
                           new VanillaModelTransformer.AnimatedVertex(pos1, 8, 7, 0, pos1Weight.chestWeight, pos1Weight.torsoWeight, 0.0F),
                           new VanillaModelTransformer.AnimatedVertex(pos2, 8, 7, 0, pos2Weight.chestWeight, pos2Weight.torsoWeight, 0.0F),
                           new VanillaModelTransformer.AnimatedVertex(pos3, 8, 7, 0, pos3Weight.chestWeight, pos3Weight.torsoWeight, 0.0F)
                        },
                        direction
                     )
                  );
               }
            }

            for (VanillaModelTransformer.AnimatedPolygon polygon : xClipPolygons) {
               boolean upsideDown = polygon.animatedVertexPositions[1].f_104371_.y() > polygon.animatedVertexPositions[2].f_104371_.y();
               VanillaModelTransformer.AnimatedVertex pos0 = upsideDown ? polygon.animatedVertexPositions[2] : polygon.animatedVertexPositions[0];
               VanillaModelTransformer.AnimatedVertex pos1 = upsideDown ? polygon.animatedVertexPositions[3] : polygon.animatedVertexPositions[1];
               VanillaModelTransformer.AnimatedVertex pos2 = upsideDown ? polygon.animatedVertexPositions[0] : polygon.animatedVertexPositions[2];
               VanillaModelTransformer.AnimatedVertex pos3 = upsideDown ? polygon.animatedVertexPositions[1] : polygon.animatedVertexPositions[3];
               Direction direction = VanillaModelTransformer.getDirectionFromVector(polygon.normal);
               List<VanillaModelTransformer.ChestPartTransformer.VertexWeight> vertexWeights = getMiddleYClipWeights(pos1.f_104371_.y(), pos2.f_104371_.y());
               List<VanillaModelTransformer.AnimatedVertex> animatedVertices = Lists.newArrayList();
               animatedVertices.add(pos0);
               animatedVertices.add(pos1);
               if (vertexWeights.size() > 0) {
                  for (VanillaModelTransformer.ChestPartTransformer.VertexWeight vertexWeight : vertexWeights) {
                     float distance = pos2.f_104371_.y() - pos1.f_104371_.y();
                     float textureV = pos1.f_104373_ + (pos2.f_104373_ - pos1.f_104373_) * ((vertexWeight.yClipCoord - pos1.f_104371_.y()) / distance);
                     Vector3f clipPos1 = VanillaModelTransformer.getClipPoint(pos1.f_104371_, pos2.f_104371_, vertexWeight.yClipCoord);
                     Vector3f clipPos2 = VanillaModelTransformer.getClipPoint(pos0.f_104371_, pos3.f_104371_, vertexWeight.yClipCoord);
                     Vertex pos4 = new Vertex(clipPos2, pos0.f_104372_, textureV);
                     Vertex pos5 = new Vertex(clipPos1, pos1.f_104372_, textureV);
                     animatedVertices.add(new VanillaModelTransformer.AnimatedVertex(pos4, 8, 7, 0, vertexWeight.chestWeight, vertexWeight.torsoWeight, 0.0F));
                     animatedVertices.add(new VanillaModelTransformer.AnimatedVertex(pos5, 8, 7, 0, vertexWeight.chestWeight, vertexWeight.torsoWeight, 0.0F));
                  }
               }

               animatedVertices.add(pos3);
               animatedVertices.add(pos2);

               for (int i = 0; i < (animatedVertices.size() - 2) / 2; i++) {
                  int start = i * 2;
                  VanillaModelTransformer.AnimatedVertex p0 = animatedVertices.get(start);
                  VanillaModelTransformer.AnimatedVertex p1 = animatedVertices.get(start + 1);
                  VanillaModelTransformer.AnimatedVertex p2 = animatedVertices.get(start + 3);
                  VanillaModelTransformer.AnimatedVertex p3 = animatedVertices.get(start + 2);
                  xyClipPolygons.add(
                     new VanillaModelTransformer.AnimatedPolygon(
                        new VanillaModelTransformer.AnimatedVertex[]{
                           new VanillaModelTransformer.AnimatedVertex(p0, 8, 7, 0, p0.weight.x, p0.weight.y, 0.0F),
                           new VanillaModelTransformer.AnimatedVertex(p1, 8, 7, 0, p1.weight.x, p1.weight.y, 0.0F),
                           new VanillaModelTransformer.AnimatedVertex(p2, 8, 7, 0, p2.weight.x, p2.weight.y, 0.0F),
                           new VanillaModelTransformer.AnimatedVertex(p3, 8, 7, 0, p3.weight.x, p3.weight.y, 0.0F)
                        },
                        direction
                     )
                  );
               }
            }

            for (VanillaModelTransformer.AnimatedPolygon polygon : xyClipPolygons) {
               Vector3f norm = new Vector3f(polygon.normal);
               norm.mul(poseStack.m_85850_().m_252943_());

               for (VanillaModelTransformer.AnimatedVertex vertex : polygon.animatedVertexPositions) {
                  Vector4f pos = new Vector4f(vertex.f_104371_, 1.0F);
                  float weight1 = vertex.weight.x;
                  float weight2 = vertex.weight.y;
                  int joint1 = vertex.jointId.m_123341_();
                  int joint2 = vertex.jointId.m_123342_();
                  int count = weight1 > 0.0F && weight2 > 0.0F ? 2 : 1;
                  if (weight1 <= 0.0F) {
                     joint1 = joint2;
                     weight1 = weight2;
                  }

                  vertices.add(
                     new SingleGroupVertexBuilder()
                        .setPosition(new Vec3f(pos.x(), pos.y(), pos.z()).scale(0.0625F))
                        .setNormal(new Vec3f(norm.x(), norm.y(), norm.z()))
                        .setTextureCoordinate(new Vec2f(vertex.f_104372_, vertex.f_104373_))
                        .setEffectiveJointIDs(new Vec3f(joint1, joint2, 0.0F))
                        .setEffectiveJointWeights(new Vec3f(weight1, weight2, 0.0F))
                        .setEffectiveJointNumber(count)
                  );
               }

               triangluatePolygon(indices, partDefinition, indexCounter);
            }
         }
      }

      static VanillaModelTransformer.ChestPartTransformer.VertexWeight getYClipWeight(float y) {
         if (y < WEIGHT_ALONG_Y[0].yClipCoord) {
            return new VanillaModelTransformer.ChestPartTransformer.VertexWeight(y, 0.0F, 1.0F);
         }

         int index = -1;
         int i = 0;

         while (i < WEIGHT_ALONG_Y.length) {
            i++;
         }

         if (index > 0) {
            VanillaModelTransformer.ChestPartTransformer.VertexWeight pair = WEIGHT_ALONG_Y[index];
            return new VanillaModelTransformer.ChestPartTransformer.VertexWeight(y, pair.chestWeight, pair.torsoWeight);
         } else {
            return new VanillaModelTransformer.ChestPartTransformer.VertexWeight(y, 1.0F, 0.0F);
         }
      }

      static List<VanillaModelTransformer.ChestPartTransformer.VertexWeight> getMiddleYClipWeights(float minY, float maxY) {
         List<VanillaModelTransformer.ChestPartTransformer.VertexWeight> cutYs = Lists.newArrayList();

         for (VanillaModelTransformer.ChestPartTransformer.VertexWeight vertexWeight : WEIGHT_ALONG_Y) {
            if (vertexWeight.yClipCoord > minY && maxY >= vertexWeight.yClipCoord) {
               cutYs.add(vertexWeight);
            }
         }

         return cutYs;
      }

      static class VertexWeight {
         final float yClipCoord;
         final float chestWeight;
         final float torsoWeight;

         public VertexWeight(float yClipCoord, float chestWeight, float torsoWeight) {
            this.yClipCoord = yClipCoord;
            this.chestWeight = chestWeight;
            this.torsoWeight = torsoWeight;
         }
      }
   }

   static class LimbPartTransformer extends HumanoidModelTransformer.PartTransformer<Cube> {
      final int upperJoint;
      final int lowerJoint;
      final int middleJoint;
      final boolean bendInFront;
      final VanillaModelTransformer.SimpleTransformer upperAttachmentTransformer;
      final VanillaModelTransformer.SimpleTransformer lowerAttachmentTransformer;
      final AABB noneAttachmentArea;
      final AABB coverArea;
      final float yClipCoord;

      public LimbPartTransformer(
         AABB coverArea, int upperJoint, int lowerJoint, int middleJoint, float yClipCoord, boolean bendInFront, AABB noneAttachmentArea
      ) {
         this.upperJoint = upperJoint;
         this.lowerJoint = lowerJoint;
         this.middleJoint = middleJoint;
         this.bendInFront = bendInFront;
         this.upperAttachmentTransformer = new VanillaModelTransformer.SimpleTransformer(null, upperJoint);
         this.lowerAttachmentTransformer = new VanillaModelTransformer.SimpleTransformer(null, lowerJoint);
         this.noneAttachmentArea = noneAttachmentArea;
         this.coverArea = coverArea;
         this.yClipCoord = yClipCoord;
      }

      public void bakeCube(
         PoseStack poseStack,
         MeshPartDefinition partDefinition,
         Cube cube,
         List<SingleGroupVertexBuilder> vertices,
         Map<MeshPartDefinition, IntList> indices,
         HumanoidModelTransformer.PartTransformer.IndexCounter indexCounter
      ) {
         List<VanillaModelTransformer.AnimatedPolygon> polygons = Lists.newArrayList();

         for (Polygon quad : cube.f_104341_) {
            Matrix4f matrix = poseStack.m_85850_().m_252922_();
            Vertex pos0 = VanillaModelTransformer.getTranslatedVertex(quad.f_104359_[0], matrix);
            Vertex pos1 = VanillaModelTransformer.getTranslatedVertex(quad.f_104359_[1], matrix);
            Vertex pos2 = VanillaModelTransformer.getTranslatedVertex(quad.f_104359_[2], matrix);
            Vertex pos3 = VanillaModelTransformer.getTranslatedVertex(quad.f_104359_[3], matrix);
            Direction direction = VanillaModelTransformer.getDirectionFromVector(quad.f_104360_);
            if (pos1.f_104371_.y() > this.yClipCoord != pos2.f_104371_.y() > this.yClipCoord) {
               float distance = pos2.f_104371_.y() - pos1.f_104371_.y();
               float textureV = pos1.f_104373_ + (pos2.f_104373_ - pos1.f_104373_) * ((this.yClipCoord - pos1.f_104371_.y()) / distance);
               Vector3f clipPos1 = VanillaModelTransformer.getClipPoint(pos1.f_104371_, pos2.f_104371_, this.yClipCoord);
               Vector3f clipPos2 = VanillaModelTransformer.getClipPoint(pos0.f_104371_, pos3.f_104371_, this.yClipCoord);
               Vertex pos4 = new Vertex(clipPos2, pos0.f_104372_, textureV);
               Vertex pos5 = new Vertex(clipPos1, pos1.f_104372_, textureV);
               int upperId;
               int lowerId;
               if (distance > 0.0F) {
                  upperId = this.lowerJoint;
                  lowerId = this.upperJoint;
               } else {
                  upperId = this.upperJoint;
                  lowerId = this.lowerJoint;
               }

               polygons.add(
                  new VanillaModelTransformer.AnimatedPolygon(
                     new VanillaModelTransformer.AnimatedVertex[]{
                        new VanillaModelTransformer.AnimatedVertex(pos0, upperId),
                        new VanillaModelTransformer.AnimatedVertex(pos1, upperId),
                        new VanillaModelTransformer.AnimatedVertex(pos5, upperId),
                        new VanillaModelTransformer.AnimatedVertex(pos4, upperId)
                     },
                     direction
                  )
               );
               polygons.add(
                  new VanillaModelTransformer.AnimatedPolygon(
                     new VanillaModelTransformer.AnimatedVertex[]{
                        new VanillaModelTransformer.AnimatedVertex(pos4, lowerId),
                        new VanillaModelTransformer.AnimatedVertex(pos5, lowerId),
                        new VanillaModelTransformer.AnimatedVertex(pos2, lowerId),
                        new VanillaModelTransformer.AnimatedVertex(pos3, lowerId)
                     },
                     direction
                  )
               );
               boolean hasSameZ = pos4.f_104371_.z() < 0.0F == pos5.f_104371_.z() < 0.0F;
               boolean isFront = hasSameZ && pos4.f_104371_.z() < 0.0F == this.bendInFront;
               if (isFront) {
                  polygons.add(
                     new VanillaModelTransformer.AnimatedPolygon(
                        new VanillaModelTransformer.AnimatedVertex[]{
                           new VanillaModelTransformer.AnimatedVertex(pos4, this.middleJoint),
                           new VanillaModelTransformer.AnimatedVertex(pos5, this.middleJoint),
                           new VanillaModelTransformer.AnimatedVertex(pos5, this.upperJoint),
                           new VanillaModelTransformer.AnimatedVertex(pos4, this.upperJoint)
                        },
                        0.001F,
                        direction
                     )
                  );
                  polygons.add(
                     new VanillaModelTransformer.AnimatedPolygon(
                        new VanillaModelTransformer.AnimatedVertex[]{
                           new VanillaModelTransformer.AnimatedVertex(pos4, this.lowerJoint),
                           new VanillaModelTransformer.AnimatedVertex(pos5, this.lowerJoint),
                           new VanillaModelTransformer.AnimatedVertex(pos5, this.middleJoint),
                           new VanillaModelTransformer.AnimatedVertex(pos4, this.middleJoint)
                        },
                        0.001F,
                        direction
                     )
                  );
               } else if (!hasSameZ) {
                  boolean startFront = pos4.f_104371_.z() > 0.0F;
                  int firstJoint = this.lowerJoint;
                  int secondJoint = this.lowerJoint;
                  int thirdJoint = startFront ? this.upperJoint : this.middleJoint;
                  int fourthJoint = startFront ? this.middleJoint : this.upperJoint;
                  int fifthJoint = this.upperJoint;
                  int sixthJoint = this.upperJoint;
                  polygons.add(
                     new VanillaModelTransformer.AnimatedPolygon(
                        new VanillaModelTransformer.AnimatedVertex[]{
                           new VanillaModelTransformer.AnimatedVertex(pos4, firstJoint),
                           new VanillaModelTransformer.AnimatedVertex(pos5, secondJoint),
                           new VanillaModelTransformer.AnimatedVertex(pos5, thirdJoint),
                           new VanillaModelTransformer.AnimatedVertex(pos4, fourthJoint)
                        },
                        0.001F,
                        direction
                     )
                  );
                  polygons.add(
                     new VanillaModelTransformer.AnimatedPolygon(
                        new VanillaModelTransformer.AnimatedVertex[]{
                           new VanillaModelTransformer.AnimatedVertex(pos4, fourthJoint),
                           new VanillaModelTransformer.AnimatedVertex(pos5, thirdJoint),
                           new VanillaModelTransformer.AnimatedVertex(pos5, fifthJoint),
                           new VanillaModelTransformer.AnimatedVertex(pos4, sixthJoint)
                        },
                        0.001F,
                        direction
                     )
                  );
               }
            } else {
               int jointId = pos0.f_104371_.y() > this.yClipCoord ? this.upperJoint : this.lowerJoint;
               polygons.add(
                  new VanillaModelTransformer.AnimatedPolygon(
                     new VanillaModelTransformer.AnimatedVertex[]{
                        new VanillaModelTransformer.AnimatedVertex(pos0, jointId),
                        new VanillaModelTransformer.AnimatedVertex(pos1, jointId),
                        new VanillaModelTransformer.AnimatedVertex(pos2, jointId),
                        new VanillaModelTransformer.AnimatedVertex(pos3, jointId)
                     },
                     direction
                  )
               );
            }
         }

         for (VanillaModelTransformer.AnimatedPolygon quad : polygons) {
            Vector3f norm = new Vector3f(quad.normal);
            norm.mul(poseStack.m_85850_().m_252943_());

            for (VanillaModelTransformer.AnimatedVertex vertex : quad.animatedVertexPositions) {
               Vector4f pos = new Vector4f(vertex.f_104371_, 1.0F);
               vertices.add(
                  new SingleGroupVertexBuilder()
                     .setPosition(new Vec3f(pos.x(), pos.y(), pos.z()).scale(0.0625F))
                     .setNormal(new Vec3f(norm.x(), norm.y(), norm.z()))
                     .setTextureCoordinate(new Vec2f(vertex.f_104372_, vertex.f_104373_))
                     .setEffectiveJointIDs(new Vec3f(vertex.jointId.m_123341_(), 0.0F, 0.0F))
                     .setEffectiveJointWeights(new Vec3f(1.0F, 0.0F, 0.0F))
                     .setEffectiveJointNumber(1)
               );
            }

            triangluatePolygon(indices, partDefinition, indexCounter);
         }
      }
   }

   static class SimpleTransformer extends HumanoidModelTransformer.PartTransformer<Cube> {
      final int jointId;
      final AABB coverArea;

      public SimpleTransformer(AABB coverArea, int jointId) {
         this.coverArea = coverArea;
         this.jointId = jointId;
      }

      public void bakeCube(
         PoseStack poseStack,
         MeshPartDefinition partDefinition,
         Cube cube,
         List<SingleGroupVertexBuilder> vertices,
         Map<MeshPartDefinition, IntList> indices,
         HumanoidModelTransformer.PartTransformer.IndexCounter indexCounter
      ) {
         for (Polygon quad : cube.f_104341_) {
            Vector3f norm = new Vector3f(quad.f_104360_);
            norm.mul(poseStack.m_85850_().m_252943_());

            for (Vertex vertex : quad.f_104359_) {
               Vector4f pos = new Vector4f(vertex.f_104371_, 1.0F);
               pos.mul(poseStack.m_85850_().m_252922_());
               vertices.add(
                  new SingleGroupVertexBuilder()
                     .setPosition(new Vec3f(pos.x(), pos.y(), pos.z()).scale(0.0625F))
                     .setNormal(new Vec3f(norm.x(), norm.y(), norm.z()))
                     .setTextureCoordinate(new Vec2f(vertex.f_104372_, vertex.f_104373_))
                     .setEffectiveJointIDs(new Vec3f(this.jointId, 0.0F, 0.0F))
                     .setEffectiveJointWeights(new Vec3f(1.0F, 0.0F, 0.0F))
                     .setEffectiveJointNumber(1)
               );
            }

            triangluatePolygon(indices, partDefinition, indexCounter);
         }
      }
   }

   public record VanillaMeshPartDefinition(
      String partName, Mesh.RenderProperties renderProperties, List<String> path, OpenMatrix4f invertedParentTransform, ModelPart root
   ) implements MeshPartDefinition {
      public static MeshPartDefinition of(String partName, Mesh.RenderProperties renderProperties) {
         return new VanillaModelTransformer.VanillaMeshPartDefinition(partName, renderProperties, null, null, null);
      }

      public static MeshPartDefinition of(String partName) {
         return new VanillaModelTransformer.VanillaMeshPartDefinition(partName, null, null, null, null);
      }

      public static MeshPartDefinition of(String partName, List<String> path, OpenMatrix4f invertedParentTransform, ModelPart root) {
         return new VanillaModelTransformer.VanillaMeshPartDefinition(partName, null, path, invertedParentTransform, root);
      }

      @Override
      public Supplier<OpenMatrix4f> getModelPartAnimationProvider() {
         return this.root == null
            ? () -> null
            : () -> {
               PoseStack poseStack = new PoseStack();
               poseStack.m_252781_(QuaternionUtils.YP.rotationDegrees(180.0F));
               poseStack.m_252781_(QuaternionUtils.XP.rotationDegrees(180.0F));
               poseStack.m_252880_(0.0F, -24.0F, 0.0F);
               this.progress(this.root, poseStack, false);
               ModelPart part = this.root;
               int idx = 0;

               for (String childPartName : this.path) {
                  idx++;
                  part = part.m_171324_(childPartName);
                  this.progress(part, poseStack, idx == this.path.size());
               }

               OpenMatrix4f animParentTransform = OpenMatrix4f.importFromMojangMatrix(poseStack.m_85850_().m_252922_());
               animParentTransform.m30 *= 0.0625F;
               animParentTransform.m31 *= 0.0625F;
               animParentTransform.m32 *= 0.0625F;
               ModelPart lastPart = part;
               PartPose partPose = part.m_233566_();
               return OpenMatrix4f.mulMatrices(
                  animParentTransform,
                  new OpenMatrix4f()
                     .mulBack(
                        OpenMatrix4f.fromQuaternion(new Quaternionf().rotationZYX(partPose.f_171410_, partPose.f_171409_, partPose.f_171408_))
                           .transpose()
                           .invert()
                     )
                     .translate(
                        new Vec3f(lastPart.f_104200_ - partPose.f_171405_, lastPart.f_104201_ - partPose.f_171406_, lastPart.f_104202_ - partPose.f_171407_)
                           .scale(0.0625F)
                     )
                     .mulBack(
                        OpenMatrix4f.fromQuaternion(new Quaternionf().rotationZYX(partPose.f_171410_, partPose.f_171409_, partPose.f_171408_)).transpose()
                     )
                     .mulBack(
                        OpenMatrix4f.fromQuaternion(
                              new Quaternionf()
                                 .rotationZYX(
                                    lastPart.f_104205_ - partPose.f_171410_, lastPart.f_104204_ - partPose.f_171409_, lastPart.f_104203_ - partPose.f_171408_
                                 )
                           )
                           .transpose()
                     )
                     .scale(new Vec3f(lastPart.f_233553_, lastPart.f_233554_, lastPart.f_233555_)),
                  this.invertedParentTransform
               );
            };
      }

      private void progress(ModelPart part, PoseStack poseStack, boolean last) {
         PartPose initialPose = part.m_233566_();
         if (last) {
            poseStack.m_252880_(initialPose.f_171405_, initialPose.f_171406_, initialPose.f_171407_);
            poseStack.m_252781_(new Quaternionf().rotationZYX(initialPose.f_171410_, initialPose.f_171409_, initialPose.f_171408_));
         } else {
            poseStack.m_252880_(part.f_104200_, part.f_104201_, part.f_104202_);
            poseStack.m_252781_(new Quaternionf().rotationZYX(part.f_104205_, part.f_104204_, part.f_104203_));
            poseStack.m_85841_(part.f_233553_, part.f_233554_, part.f_233555_);
         }
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else {
            return o instanceof MeshPartDefinition comparision ? this.partName.equals(comparision.partName()) : false;
         }
      }

      @Override
      public int hashCode() {
         return this.partName.hashCode();
      }
   }

   record VanillaModelPartition(HumanoidModelTransformer.PartTransformer<Cube> partTransformer, ModelPart modelPart, String partName) {
   }
}
