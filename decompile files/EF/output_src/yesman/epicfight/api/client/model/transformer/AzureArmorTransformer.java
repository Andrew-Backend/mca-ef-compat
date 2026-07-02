package yesman.epicfight.api.client.model.transformer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import mod.azure.azurelibarmor.animatable.client.RenderProvider;
import mod.azure.azurelibarmor.cache.object.GeoBone;
import mod.azure.azurelibarmor.cache.object.GeoCube;
import mod.azure.azurelibarmor.cache.object.GeoQuad;
import mod.azure.azurelibarmor.cache.object.GeoVertex;
import mod.azure.azurelibarmor.core.animatable.GeoAnimatable;
import mod.azure.azurelibarmor.core.state.BoneSnapshot;
import mod.azure.azurelibarmor.renderer.GeoArmorRenderer;
import mod.azure.azurelibarmor.util.RenderUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart.Vertex;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import yesman.epicfight.api.client.forgeevent.AnimatedArmorTextureEvent;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.MeshPartDefinition;
import yesman.epicfight.api.client.model.SingleGroupVertexBuilder;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec2f;
import yesman.epicfight.api.utils.math.Vec3f;

public class AzureArmorTransformer extends HumanoidModelTransformer {
   static final HumanoidModelTransformer.PartTransformer<GeoCube> HEAD = new AzureArmorTransformer.SimpleTransformer(9);
   static final HumanoidModelTransformer.PartTransformer<GeoCube> LEFT_FEET = new AzureArmorTransformer.SimpleTransformer(5);
   static final HumanoidModelTransformer.PartTransformer<GeoCube> RIGHT_FEET = new AzureArmorTransformer.SimpleTransformer(2);
   static final HumanoidModelTransformer.PartTransformer<GeoCube> LEFT_ARM = new AzureArmorTransformer.LimbPartTransformer(
      16, 17, 19, 1.125F, false, AABB.m_165882_(new Vec3(-0.375, 1.125, 0.0), 0.5, 0.85, 0.5)
   );
   static final HumanoidModelTransformer.PartTransformer<GeoCube> RIGHT_ARM = new AzureArmorTransformer.LimbPartTransformer(
      11, 12, 14, 1.125F, false, AABB.m_165882_(new Vec3(0.375, 1.125, 0.0), 0.5, 0.85, 0.5)
   );
   static final HumanoidModelTransformer.PartTransformer<GeoCube> LEFT_LEG = new AzureArmorTransformer.LimbPartTransformer(
      4, 5, 6, 0.375F, true, AABB.m_165882_(new Vec3(-0.15, 0.375, 0.0), 0.5, 0.85, 0.5)
   );
   static final HumanoidModelTransformer.PartTransformer<GeoCube> RIGHT_LEG = new AzureArmorTransformer.LimbPartTransformer(
      1, 2, 3, 0.375F, true, AABB.m_165882_(new Vec3(0.15, 0.375, 0.0), 0.5, 0.85, 0.5)
   );
   static final HumanoidModelTransformer.PartTransformer<GeoCube> CHEST = new AzureArmorTransformer.ChestPartTransformer(
      8, 7, 1.125F, AABB.m_165882_(new Vec3(0.0, 1.125, 0.0), 0.9, 0.85, 0.45)
   );

   public static void getGeoArmorTexturePath(AnimatedArmorTextureEvent event) {
      RenderProvider customRenderProperties = RenderProvider.of(event.getItemstack());
      if (customRenderProperties != null
         && customRenderProperties.getHumanoidArmorModel(event.getLivingEntity(), event.getItemstack(), event.getEquipmentSlot(), event.getOriginalModel()) instanceof GeoArmorRenderer geoArmorRenderer
         && event.getItemstack().m_41720_() instanceof GeoAnimatable geoAnimatable) {
         event.setResultLocation(geoArmorRenderer.getTextureLocation(geoAnimatable));
      }
   }

   @Override
   public SkinnedMesh transformArmorModel(HumanoidModel<?> humanoidModel) {
      if (humanoidModel instanceof GeoArmorRenderer<?> geoModel) {
         ArrayList boxes = Lists.newArrayList();
         GeoBone headBone = geoModel.getHeadBone();
         GeoBone bodyBone = geoModel.getBodyBone();
         GeoBone rightArmBone = geoModel.getRightArmBone();
         GeoBone leftArmBone = geoModel.getLeftArmBone();
         GeoBone rightLegBone = geoModel.getRightLegBone();
         GeoBone leftLegBone = geoModel.getLeftLegBone();
         GeoBone rightBootBone = geoModel.getRightBootBone();
         GeoBone leftBootBone = geoModel.getLeftBootBone();
         if (headBone != null) {
            headBone.setRotX(0.0F);
            headBone.setRotY(0.0F);
            headBone.setRotZ(0.0F);
         }

         if (bodyBone != null) {
            bodyBone.setRotX(0.0F);
            bodyBone.setRotY(0.0F);
            bodyBone.setRotZ(0.0F);
         }

         if (rightArmBone != null) {
            rightArmBone.setRotX(0.0F);
            rightArmBone.setRotY(0.0F);
            rightArmBone.setRotZ(0.0F);
         }

         if (leftArmBone != null) {
            leftArmBone.setRotX(0.0F);
            leftArmBone.setRotY(0.0F);
            leftArmBone.setRotZ(0.0F);
         }

         if (rightLegBone != null) {
            rightLegBone.setRotX(0.0F);
            rightLegBone.setRotY(0.0F);
            rightLegBone.setRotZ(0.0F);
         }

         if (leftLegBone != null) {
            leftLegBone.setRotX(0.0F);
            leftLegBone.setRotY(0.0F);
            leftLegBone.setRotZ(0.0F);
         }

         if (rightBootBone != null) {
            rightBootBone.setRotX(0.0F);
            rightBootBone.setRotY(0.0F);
            rightBootBone.setRotZ(0.0F);
         }

         if (leftBootBone != null) {
            leftBootBone.setRotX(0.0F);
            leftBootBone.setRotY(0.0F);
            leftBootBone.setRotZ(0.0F);
         }

         boxes.add(new AzureArmorTransformer.GeoModelPartition(HEAD, headBone));
         boxes.add(new AzureArmorTransformer.GeoModelPartition(CHEST, bodyBone));
         boxes.add(new AzureArmorTransformer.GeoModelPartition(RIGHT_ARM, rightArmBone));
         boxes.add(new AzureArmorTransformer.GeoModelPartition(LEFT_ARM, leftArmBone));
         boxes.add(new AzureArmorTransformer.GeoModelPartition(LEFT_LEG, leftLegBone));
         boxes.add(new AzureArmorTransformer.GeoModelPartition(RIGHT_LEG, rightLegBone));
         boxes.add(new AzureArmorTransformer.GeoModelPartition(LEFT_FEET, leftBootBone));
         boxes.add(new AzureArmorTransformer.GeoModelPartition(RIGHT_FEET, rightBootBone));
         return bakeMeshFromCubes(boxes);
      } else {
         return null;
      }
   }

   private static SkinnedMesh bakeMeshFromCubes(List<AzureArmorTransformer.GeoModelPartition> partitions) {
      List<SingleGroupVertexBuilder> vertices = Lists.newArrayList();
      Map<MeshPartDefinition, IntList> indices = Maps.newHashMap();
      PoseStack poseStack = new PoseStack();
      HumanoidModelTransformer.PartTransformer.IndexCounter indexCounter = new HumanoidModelTransformer.PartTransformer.IndexCounter();

      for (AzureArmorTransformer.GeoModelPartition modelpartition : partitions) {
         bake(poseStack, modelpartition, modelpartition.geoBone.getName(), modelpartition.geoBone, vertices, indices, Lists.newArrayList(), indexCounter, false);
      }

      return SingleGroupVertexBuilder.loadVertexInformation(vertices, indices);
   }

   private static void bake(
      PoseStack poseStack,
      AzureArmorTransformer.GeoModelPartition modelpartition,
      String partName,
      GeoBone geoBone,
      List<SingleGroupVertexBuilder> vertices,
      Map<MeshPartDefinition, IntList> indices,
      List<String> path,
      HumanoidModelTransformer.PartTransformer.IndexCounter indexCounter,
      boolean bindPartAnimation
   ) {
      if (geoBone != null) {
         poseStack.m_85836_();
         RenderUtils.prepMatrixForBone(poseStack, geoBone);
         List<String> newList = new ArrayList<>(path);
         if (bindPartAnimation) {
            newList.add(partName);
         }

         if (!geoBone.isHidden()) {
            for (GeoCube cube : geoBone.getCubes()) {
               poseStack.m_85836_();
               RenderUtils.translateToPivotPoint(poseStack, cube);
               RenderUtils.rotateMatrixAroundCube(poseStack, cube);
               RenderUtils.translateAwayFromPivotPoint(poseStack, cube);
               MeshPartDefinition partDefinition = GeoModelTransformer.GeoMeshPartDefinition.of(partName);
               if (bindPartAnimation) {
                  OpenMatrix4f invertedParentTransform = OpenMatrix4f.importFromMojangMatrix(poseStack.m_85850_().m_252922_());
                  invertedParentTransform.m30 *= 0.0625F;
                  invertedParentTransform.m31 *= 0.0625F;
                  invertedParentTransform.m32 *= 0.0625F;
                  invertedParentTransform.invert();
                  partDefinition = AzureArmorTransformer.AzureArmorMeshPartDefinition.of(partName, newList, invertedParentTransform, modelpartition.geoBone);
               }

               modelpartition.partTransformer.bakeCube(poseStack, partDefinition, cube, vertices, indices, indexCounter);
               poseStack.m_85849_();
            }
         }

         if (!geoBone.isHidingChildren()) {
            for (GeoBone childBone : geoBone.getChildBones()) {
               bake(poseStack, modelpartition, partName, childBone, vertices, indices, newList, indexCounter, true);
            }
         }

         poseStack.m_85849_();
      }
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

   static Vec3 getCenterOfCube(PoseStack poseStack, GeoCube cube) {
      double minX = Double.MAX_VALUE;
      double minY = Double.MAX_VALUE;
      double minZ = Double.MAX_VALUE;
      double maxX = Double.MIN_VALUE;
      double maxY = Double.MIN_VALUE;
      double maxZ = Double.MIN_VALUE;
      Matrix4f matrix = poseStack.m_85850_().m_252922_();

      for (GeoQuad quad : cube.quads()) {
         for (GeoVertex v : quad.vertices()) {
            Vector4f translatedPosition = new Vector4f(v.position(), 1.0F);
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

   static Vertex getTranslatedVertex(GeoVertex original, Matrix4f matrix) {
      Vector4f translatedPosition = new Vector4f(original.position(), 1.0F);
      translatedPosition.mul(matrix);
      return new Vertex(translatedPosition.x(), translatedPosition.y(), translatedPosition.z(), original.texU(), original.texV());
   }

   public static OpenMatrix4f of(PoseStack poseStack, GeoBone bone) {
      BoneSnapshot boneSnapshot = bone.getInitialSnapshot();
      poseStack.m_85836_();
      poseStack.m_252880_(boneSnapshot.getOffsetX(), boneSnapshot.getOffsetY(), boneSnapshot.getOffsetZ());
      if (boneSnapshot.getRotX() != 0.0F || boneSnapshot.getRotY() != 0.0F || boneSnapshot.getRotZ() != 0.0F) {
         poseStack.m_252781_(new Quaternionf().rotationZYX(boneSnapshot.getRotZ(), boneSnapshot.getRotY(), boneSnapshot.getRotX()));
      }

      Matrix4f lastPose = new Matrix4f(poseStack.m_85850_().m_252922_());
      poseStack.m_85849_();
      OpenMatrix4f matrix = OpenMatrix4f.importFromMojangMatrix(lastPose);
      matrix.m30 *= 0.0625F;
      matrix.m31 *= 0.0625F;
      matrix.m32 *= 0.0625F;
      return OpenMatrix4f.mulMatrices(
         matrix,
         new OpenMatrix4f()
            .translate(
               new Vec3f(bone.getPosX() - boneSnapshot.getOffsetX(), bone.getPosY() - boneSnapshot.getOffsetY(), bone.getPosZ() - boneSnapshot.getOffsetZ())
                  .scale(0.0625F)
            )
            .mulBack(
               OpenMatrix4f.fromQuaternion(
                  new Quaternionf()
                     .rotationZYX(boneSnapshot.getRotZ() - bone.getRotZ(), boneSnapshot.getRotY() - bone.getRotY(), boneSnapshot.getRotX() - bone.getRotX())
               )
            )
            .scale(new Vec3f(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ())),
         OpenMatrix4f.invert(matrix, null)
      );
   }

   static class AnimatedPolygon {
      public final AzureArmorTransformer.AnimatedVertex[] animatedVertexPositions;
      public final Vector3f normal;

      public AnimatedPolygon(AzureArmorTransformer.AnimatedVertex[] positionsIn, Direction directionIn) {
         this.animatedVertexPositions = positionsIn;
         this.normal = directionIn.m_253071_();
      }

      public AnimatedPolygon(AzureArmorTransformer.AnimatedVertex[] positionsIn, float cor, Direction directionIn) {
         this.animatedVertexPositions = positionsIn;
         positionsIn[0] = new AzureArmorTransformer.AnimatedVertex(
            positionsIn[0], positionsIn[0].f_104372_, positionsIn[0].f_104373_ + cor, positionsIn[0].jointId, positionsIn[0].weight
         );
         positionsIn[1] = new AzureArmorTransformer.AnimatedVertex(
            positionsIn[1], positionsIn[1].f_104372_, positionsIn[1].f_104373_ + cor, positionsIn[1].jointId, positionsIn[1].weight
         );
         positionsIn[2] = new AzureArmorTransformer.AnimatedVertex(
            positionsIn[2], positionsIn[2].f_104372_, positionsIn[2].f_104373_ - cor, positionsIn[2].jointId, positionsIn[2].weight
         );
         positionsIn[3] = new AzureArmorTransformer.AnimatedVertex(
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

   public record AzureArmorMeshPartDefinition(String partName, List<String> path, OpenMatrix4f invertedParentTransform, GeoBone root)
      implements MeshPartDefinition {
      public static MeshPartDefinition of(String partName) {
         return new AzureArmorTransformer.AzureArmorMeshPartDefinition(partName, null, null, null);
      }

      public static MeshPartDefinition of(String partName, List<String> path, OpenMatrix4f invertedParentTransform, GeoBone root) {
         return new AzureArmorTransformer.AzureArmorMeshPartDefinition(partName, path, invertedParentTransform, root);
      }

      public static GeoBone getChildBone(GeoBone bone, String boneName) {
         for (GeoBone childBone : bone.getChildBones()) {
            if (childBone.getName().equals(boneName)) {
               return childBone;
            }
         }

         return null;
      }

      @Override
      public Supplier<OpenMatrix4f> getModelPartAnimationProvider() {
         return this.root == null
            ? () -> null
            : () -> {
               PoseStack poseStack = new PoseStack();
               this.progress(this.root, poseStack, false);
               GeoBone bone = this.root;
               int idx = 0;

               for (String childPartName : this.path) {
                  bone = getChildBone(bone, childPartName);
                  if (bone == null) {
                     return null;
                  }

                  idx++;
                  this.progress(bone, poseStack, idx == this.path.size());
               }

               OpenMatrix4f parentTransform = OpenMatrix4f.importFromMojangMatrix(poseStack.m_85850_().m_252922_());
               GeoBone lastBone = bone;
               BoneSnapshot boneSnapshot = bone.getInitialSnapshot();
               return OpenMatrix4f.mulMatrices(
                  parentTransform,
                  new OpenMatrix4f()
                     .mulBack(
                        OpenMatrix4f.fromQuaternion(new Quaternionf().rotationZYX(boneSnapshot.getRotZ(), boneSnapshot.getRotY(), boneSnapshot.getRotX()))
                           .transpose()
                           .invert()
                     )
                     .translate(
                        new Vec3f(
                              lastBone.getPosX() - boneSnapshot.getOffsetX(),
                              lastBone.getPosY() - boneSnapshot.getOffsetY(),
                              lastBone.getPosZ() - boneSnapshot.getOffsetZ()
                           )
                           .scale(0.0625F)
                     )
                     .mulBack(
                        OpenMatrix4f.fromQuaternion(new Quaternionf().rotationZYX(boneSnapshot.getRotZ(), boneSnapshot.getRotY(), boneSnapshot.getRotX()))
                           .transpose()
                     )
                     .mulBack(
                        OpenMatrix4f.fromQuaternion(
                           new Quaternionf()
                              .rotationZYX(
                                 boneSnapshot.getRotZ() - lastBone.getRotZ(),
                                 boneSnapshot.getRotY() - lastBone.getRotY(),
                                 boneSnapshot.getRotX() - lastBone.getRotX()
                              )
                        )
                     )
                     .scale(new Vec3f(lastBone.getScaleX(), lastBone.getScaleY(), lastBone.getScaleZ())),
                  this.invertedParentTransform
               );
            };
      }

      @Override
      public Mesh.RenderProperties renderProperties() {
         return null;
      }

      private void progress(GeoBone bone, PoseStack poseStack, boolean last) {
         BoneSnapshot boneSnapshot = bone.getInitialSnapshot();
         if (last) {
            poseStack.m_252880_(boneSnapshot.getOffsetX(), boneSnapshot.getOffsetY(), boneSnapshot.getOffsetZ());
            poseStack.m_252781_(new Quaternionf().rotationZYX(boneSnapshot.getRotZ(), boneSnapshot.getRotY(), boneSnapshot.getRotX()));
         } else {
            poseStack.m_252880_(bone.getPosX(), bone.getPosY(), bone.getPosZ());
            poseStack.m_252781_(new Quaternionf().rotationZYX(bone.getRotZ(), bone.getRotY(), bone.getRotX()));
            poseStack.m_85841_(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
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

   static class ChestPartTransformer extends HumanoidModelTransformer.PartTransformer<GeoCube> {
      static final float X_PLANE = 0.0F;
      static final AzureArmorTransformer.ChestPartTransformer.VertexWeight[] WEIGHT_ALONG_Y = new AzureArmorTransformer.ChestPartTransformer.VertexWeight[]{
         new AzureArmorTransformer.ChestPartTransformer.VertexWeight(13.6666F, 0.23F, 0.77F),
         new AzureArmorTransformer.ChestPartTransformer.VertexWeight(15.8333F, 0.254F, 0.746F),
         new AzureArmorTransformer.ChestPartTransformer.VertexWeight(18.0F, 0.5F, 0.5F),
         new AzureArmorTransformer.ChestPartTransformer.VertexWeight(20.1666F, 0.744F, 0.256F),
         new AzureArmorTransformer.ChestPartTransformer.VertexWeight(22.3333F, 0.77F, 0.23F)
      };
      final AzureArmorTransformer.SimpleTransformer upperAttachmentTransformer;
      final AzureArmorTransformer.SimpleTransformer lowerAttachmentTransformer;
      final AABB noneAttachmentArea;
      final float yClipCoord;

      public ChestPartTransformer(int upperJoint, int lowerJoint, float yBasis, AABB noneAttachmentArea) {
         this.noneAttachmentArea = noneAttachmentArea;
         this.upperAttachmentTransformer = new AzureArmorTransformer.SimpleTransformer(upperJoint);
         this.lowerAttachmentTransformer = new AzureArmorTransformer.SimpleTransformer(lowerJoint);
         this.yClipCoord = yBasis;
      }

      public void bakeCube(
         PoseStack poseStack,
         MeshPartDefinition partName,
         GeoCube cube,
         List<SingleGroupVertexBuilder> vertices,
         Map<MeshPartDefinition, IntList> indices,
         HumanoidModelTransformer.PartTransformer.IndexCounter indexCounter
      ) {
         Vec3 centerOfCube = AzureArmorTransformer.getCenterOfCube(poseStack, cube);
         if (!this.noneAttachmentArea.m_82390_(centerOfCube)) {
            if (centerOfCube.f_82480_ < this.yClipCoord) {
               this.lowerAttachmentTransformer.bakeCube(poseStack, partName, cube, vertices, indices, indexCounter);
            } else {
               this.upperAttachmentTransformer.bakeCube(poseStack, partName, cube, vertices, indices, indexCounter);
            }
         } else {
            List<AzureArmorTransformer.AnimatedPolygon> xClipPolygons = Lists.newArrayList();
            List<AzureArmorTransformer.AnimatedPolygon> xyClipPolygons = Lists.newArrayList();

            for (GeoQuad polygon : cube.quads()) {
               Matrix4f matrix = poseStack.m_85850_().m_252922_();
               Vertex pos0 = AzureArmorTransformer.getTranslatedVertex(polygon.vertices()[0], matrix);
               Vertex pos1 = AzureArmorTransformer.getTranslatedVertex(polygon.vertices()[1], matrix);
               Vertex pos2 = AzureArmorTransformer.getTranslatedVertex(polygon.vertices()[2], matrix);
               Vertex pos3 = AzureArmorTransformer.getTranslatedVertex(polygon.vertices()[3], matrix);
               Direction direction = AzureArmorTransformer.getDirectionFromVector(polygon.normal());
               AzureArmorTransformer.ChestPartTransformer.VertexWeight pos0Weight = getYClipWeight(pos0.f_104371_.y());
               AzureArmorTransformer.ChestPartTransformer.VertexWeight pos1Weight = getYClipWeight(pos1.f_104371_.y());
               AzureArmorTransformer.ChestPartTransformer.VertexWeight pos2Weight = getYClipWeight(pos2.f_104371_.y());
               AzureArmorTransformer.ChestPartTransformer.VertexWeight pos3Weight = getYClipWeight(pos3.f_104371_.y());
               if (pos1.f_104371_.x() > 0.0F != pos2.f_104371_.x() > 0.0F) {
                  float distance = pos2.f_104371_.x() - pos1.f_104371_.x();
                  float textureU = pos1.f_104372_ + (pos2.f_104372_ - pos1.f_104372_) * ((0.0F - pos1.f_104371_.x()) / distance);
                  Vertex pos4 = new Vertex(0.0F, pos0.f_104371_.y(), pos0.f_104371_.z(), textureU, pos0.f_104373_);
                  Vertex pos5 = new Vertex(0.0F, pos1.f_104371_.y(), pos1.f_104371_.z(), textureU, pos1.f_104373_);
                  xClipPolygons.add(
                     new AzureArmorTransformer.AnimatedPolygon(
                        new AzureArmorTransformer.AnimatedVertex[]{
                           new AzureArmorTransformer.AnimatedVertex(pos0, 8, 7, 0, pos0Weight.chestWeight, pos0Weight.torsoWeight, 0.0F),
                           new AzureArmorTransformer.AnimatedVertex(pos4, 8, 7, 0, pos0Weight.chestWeight, pos0Weight.torsoWeight, 0.0F),
                           new AzureArmorTransformer.AnimatedVertex(pos5, 8, 7, 0, pos1Weight.chestWeight, pos1Weight.torsoWeight, 0.0F),
                           new AzureArmorTransformer.AnimatedVertex(pos3, 8, 7, 0, pos3Weight.chestWeight, pos3Weight.torsoWeight, 0.0F)
                        },
                        direction
                     )
                  );
                  xClipPolygons.add(
                     new AzureArmorTransformer.AnimatedPolygon(
                        new AzureArmorTransformer.AnimatedVertex[]{
                           new AzureArmorTransformer.AnimatedVertex(pos4, 8, 7, 0, pos0Weight.chestWeight, pos0Weight.torsoWeight, 0.0F),
                           new AzureArmorTransformer.AnimatedVertex(pos1, 8, 7, 0, pos1Weight.chestWeight, pos1Weight.torsoWeight, 0.0F),
                           new AzureArmorTransformer.AnimatedVertex(pos2, 8, 7, 0, pos2Weight.chestWeight, pos2Weight.torsoWeight, 0.0F),
                           new AzureArmorTransformer.AnimatedVertex(pos5, 8, 7, 0, pos1Weight.chestWeight, pos1Weight.torsoWeight, 0.0F)
                        },
                        direction
                     )
                  );
               } else {
                  xClipPolygons.add(
                     new AzureArmorTransformer.AnimatedPolygon(
                        new AzureArmorTransformer.AnimatedVertex[]{
                           new AzureArmorTransformer.AnimatedVertex(pos0, 8, 7, 0, pos0Weight.chestWeight, pos0Weight.torsoWeight, 0.0F),
                           new AzureArmorTransformer.AnimatedVertex(pos1, 8, 7, 0, pos1Weight.chestWeight, pos1Weight.torsoWeight, 0.0F),
                           new AzureArmorTransformer.AnimatedVertex(pos2, 8, 7, 0, pos2Weight.chestWeight, pos2Weight.torsoWeight, 0.0F),
                           new AzureArmorTransformer.AnimatedVertex(pos3, 8, 7, 0, pos3Weight.chestWeight, pos3Weight.torsoWeight, 0.0F)
                        },
                        direction
                     )
                  );
               }
            }

            for (AzureArmorTransformer.AnimatedPolygon polygon : xClipPolygons) {
               boolean upsideDown = polygon.animatedVertexPositions[1].f_104371_.y() > polygon.animatedVertexPositions[2].f_104371_.y();
               AzureArmorTransformer.AnimatedVertex pos0 = upsideDown ? polygon.animatedVertexPositions[2] : polygon.animatedVertexPositions[0];
               AzureArmorTransformer.AnimatedVertex pos1 = upsideDown ? polygon.animatedVertexPositions[3] : polygon.animatedVertexPositions[1];
               AzureArmorTransformer.AnimatedVertex pos2 = upsideDown ? polygon.animatedVertexPositions[0] : polygon.animatedVertexPositions[2];
               AzureArmorTransformer.AnimatedVertex pos3 = upsideDown ? polygon.animatedVertexPositions[1] : polygon.animatedVertexPositions[3];
               Direction direction = AzureArmorTransformer.getDirectionFromVector(polygon.normal);
               List<AzureArmorTransformer.ChestPartTransformer.VertexWeight> vertexWeights = getMiddleYClipWeights(pos1.f_104371_.y(), pos2.f_104371_.y());
               List<AzureArmorTransformer.AnimatedVertex> animatedVertices = Lists.newArrayList();
               animatedVertices.add(pos0);
               animatedVertices.add(pos1);
               if (!vertexWeights.isEmpty()) {
                  for (AzureArmorTransformer.ChestPartTransformer.VertexWeight vertexWeight : vertexWeights) {
                     float distance = pos2.f_104371_.y() - pos1.f_104371_.y();
                     float textureV = pos1.f_104373_ + (pos2.f_104373_ - pos1.f_104373_) * ((vertexWeight.yClipCoord - pos1.f_104371_.y()) / distance);
                     Vector3f clipPos1 = AzureArmorTransformer.getClipPoint(pos1.f_104371_, pos2.f_104371_, vertexWeight.yClipCoord);
                     Vector3f clipPos2 = AzureArmorTransformer.getClipPoint(pos0.f_104371_, pos3.f_104371_, vertexWeight.yClipCoord);
                     Vertex pos4 = new Vertex(clipPos2, pos0.f_104372_, textureV);
                     Vertex pos5 = new Vertex(clipPos1, pos1.f_104372_, textureV);
                     animatedVertices.add(new AzureArmorTransformer.AnimatedVertex(pos4, 8, 7, 0, vertexWeight.chestWeight, vertexWeight.torsoWeight, 0.0F));
                     animatedVertices.add(new AzureArmorTransformer.AnimatedVertex(pos5, 8, 7, 0, vertexWeight.chestWeight, vertexWeight.torsoWeight, 0.0F));
                  }
               }

               animatedVertices.add(pos3);
               animatedVertices.add(pos2);

               for (int i = 0; i < (animatedVertices.size() - 2) / 2; i++) {
                  int start = i * 2;
                  AzureArmorTransformer.AnimatedVertex p0 = animatedVertices.get(start);
                  AzureArmorTransformer.AnimatedVertex p1 = animatedVertices.get(start + 1);
                  AzureArmorTransformer.AnimatedVertex p2 = animatedVertices.get(start + 3);
                  AzureArmorTransformer.AnimatedVertex p3 = animatedVertices.get(start + 2);
                  xyClipPolygons.add(
                     new AzureArmorTransformer.AnimatedPolygon(
                        new AzureArmorTransformer.AnimatedVertex[]{
                           new AzureArmorTransformer.AnimatedVertex(p0, 8, 7, 0, p0.weight.x, p0.weight.y, 0.0F),
                           new AzureArmorTransformer.AnimatedVertex(p1, 8, 7, 0, p1.weight.x, p1.weight.y, 0.0F),
                           new AzureArmorTransformer.AnimatedVertex(p2, 8, 7, 0, p2.weight.x, p2.weight.y, 0.0F),
                           new AzureArmorTransformer.AnimatedVertex(p3, 8, 7, 0, p3.weight.x, p3.weight.y, 0.0F)
                        },
                        direction
                     )
                  );
               }
            }

            for (AzureArmorTransformer.AnimatedPolygon polygon : xyClipPolygons) {
               Vector3f norm = new Vector3f(polygon.normal);
               norm.mul(poseStack.m_85850_().m_252943_());

               for (AzureArmorTransformer.AnimatedVertex vertex : polygon.animatedVertexPositions) {
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
                        .setPosition(new Vec3f(pos.x(), pos.y(), pos.z()))
                        .setNormal(new Vec3f(norm.x(), norm.y(), norm.z()))
                        .setTextureCoordinate(new Vec2f(vertex.f_104372_, vertex.f_104373_))
                        .setEffectiveJointIDs(new Vec3f(joint1, joint2, 0.0F))
                        .setEffectiveJointWeights(new Vec3f(weight1, weight2, 0.0F))
                        .setEffectiveJointNumber(count)
                  );
               }

               triangluatePolygon(indices, partName, indexCounter);
            }
         }
      }

      static AzureArmorTransformer.ChestPartTransformer.VertexWeight getYClipWeight(float y) {
         if (y < WEIGHT_ALONG_Y[0].yClipCoord) {
            return new AzureArmorTransformer.ChestPartTransformer.VertexWeight(y, 0.0F, 1.0F);
         }

         int index = -1;
         int i = 0;

         while (i < WEIGHT_ALONG_Y.length) {
            i++;
         }

         if (index > 0) {
            AzureArmorTransformer.ChestPartTransformer.VertexWeight pair = WEIGHT_ALONG_Y[index];
            return new AzureArmorTransformer.ChestPartTransformer.VertexWeight(y, pair.chestWeight, pair.torsoWeight);
         } else {
            return new AzureArmorTransformer.ChestPartTransformer.VertexWeight(y, 1.0F, 0.0F);
         }
      }

      static List<AzureArmorTransformer.ChestPartTransformer.VertexWeight> getMiddleYClipWeights(float minY, float maxY) {
         List<AzureArmorTransformer.ChestPartTransformer.VertexWeight> cutYs = Lists.newArrayList();

         for (AzureArmorTransformer.ChestPartTransformer.VertexWeight vertexWeight : WEIGHT_ALONG_Y) {
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

   static class GeoModelPartition {
      final HumanoidModelTransformer.PartTransformer<GeoCube> partTransformer;
      final GeoBone geoBone;

      private GeoModelPartition(HumanoidModelTransformer.PartTransformer<GeoCube> partTransformer, GeoBone geoBone) {
         this.partTransformer = partTransformer;
         this.geoBone = geoBone;
      }
   }

   static class LimbPartTransformer extends HumanoidModelTransformer.PartTransformer<GeoCube> {
      final int upperJoint;
      final int lowerJoint;
      final int middleJoint;
      final boolean bendInFront;
      final AzureArmorTransformer.SimpleTransformer upperAttachmentTransformer;
      final AzureArmorTransformer.SimpleTransformer lowerAttachmentTransformer;
      final AABB noneAttachmentArea;
      final float yClipCoord;

      public LimbPartTransformer(int upperJoint, int lowerJoint, int middleJoint, float yClipCoord, boolean bendInFront, AABB noneAttachmentArea) {
         this.upperJoint = upperJoint;
         this.lowerJoint = lowerJoint;
         this.middleJoint = middleJoint;
         this.bendInFront = bendInFront;
         this.upperAttachmentTransformer = new AzureArmorTransformer.SimpleTransformer(upperJoint);
         this.lowerAttachmentTransformer = new AzureArmorTransformer.SimpleTransformer(lowerJoint);
         this.noneAttachmentArea = noneAttachmentArea;
         this.yClipCoord = yClipCoord;
      }

      public void bakeCube(
         PoseStack poseStack,
         MeshPartDefinition partName,
         GeoCube cube,
         List<SingleGroupVertexBuilder> vertices,
         Map<MeshPartDefinition, IntList> indices,
         HumanoidModelTransformer.PartTransformer.IndexCounter indexCounter
      ) {
         Vec3 centerOfCube = AzureArmorTransformer.getCenterOfCube(poseStack, cube);
         if (!this.noneAttachmentArea.m_82390_(centerOfCube)) {
            if (centerOfCube.f_82480_ < this.yClipCoord) {
               this.lowerAttachmentTransformer.bakeCube(poseStack, partName, cube, vertices, indices, indexCounter);
            } else {
               this.upperAttachmentTransformer.bakeCube(poseStack, partName, cube, vertices, indices, indexCounter);
            }
         } else {
            List<AzureArmorTransformer.AnimatedPolygon> polygons = Lists.newArrayList();

            for (GeoQuad quad : cube.quads()) {
               Matrix4f matrix = poseStack.m_85850_().m_252922_();
               Vertex pos0 = AzureArmorTransformer.getTranslatedVertex(quad.vertices()[0], matrix);
               Vertex pos1 = AzureArmorTransformer.getTranslatedVertex(quad.vertices()[1], matrix);
               Vertex pos2 = AzureArmorTransformer.getTranslatedVertex(quad.vertices()[2], matrix);
               Vertex pos3 = AzureArmorTransformer.getTranslatedVertex(quad.vertices()[3], matrix);
               Direction direction = AzureArmorTransformer.getDirectionFromVector(quad.normal());
               if (pos1.f_104371_.y() > this.yClipCoord != pos2.f_104371_.y() > this.yClipCoord) {
                  float distance = pos2.f_104371_.y() - pos1.f_104371_.y();
                  float textureV = pos1.f_104373_ + (pos2.f_104373_ - pos1.f_104373_) * ((this.yClipCoord - pos1.f_104371_.y()) / distance);
                  Vector3f clipPos1 = AzureArmorTransformer.getClipPoint(pos1.f_104371_, pos2.f_104371_, this.yClipCoord);
                  Vector3f clipPos2 = AzureArmorTransformer.getClipPoint(pos0.f_104371_, pos3.f_104371_, this.yClipCoord);
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
                     new AzureArmorTransformer.AnimatedPolygon(
                        new AzureArmorTransformer.AnimatedVertex[]{
                           new AzureArmorTransformer.AnimatedVertex(pos0, upperId),
                           new AzureArmorTransformer.AnimatedVertex(pos1, upperId),
                           new AzureArmorTransformer.AnimatedVertex(pos5, upperId),
                           new AzureArmorTransformer.AnimatedVertex(pos4, upperId)
                        },
                        direction
                     )
                  );
                  polygons.add(
                     new AzureArmorTransformer.AnimatedPolygon(
                        new AzureArmorTransformer.AnimatedVertex[]{
                           new AzureArmorTransformer.AnimatedVertex(pos4, lowerId),
                           new AzureArmorTransformer.AnimatedVertex(pos5, lowerId),
                           new AzureArmorTransformer.AnimatedVertex(pos2, lowerId),
                           new AzureArmorTransformer.AnimatedVertex(pos3, lowerId)
                        },
                        direction
                     )
                  );
                  boolean hasSameZ = pos4.f_104371_.z() < 0.0F == pos5.f_104371_.z() < 0.0F;
                  boolean isFront = hasSameZ && pos4.f_104371_.z() < 0.0F == this.bendInFront;
                  if (isFront) {
                     polygons.add(
                        new AzureArmorTransformer.AnimatedPolygon(
                           new AzureArmorTransformer.AnimatedVertex[]{
                              new AzureArmorTransformer.AnimatedVertex(pos4, this.middleJoint),
                              new AzureArmorTransformer.AnimatedVertex(pos5, this.middleJoint),
                              new AzureArmorTransformer.AnimatedVertex(pos5, this.upperJoint),
                              new AzureArmorTransformer.AnimatedVertex(pos4, this.upperJoint)
                           },
                           0.001F,
                           direction
                        )
                     );
                     polygons.add(
                        new AzureArmorTransformer.AnimatedPolygon(
                           new AzureArmorTransformer.AnimatedVertex[]{
                              new AzureArmorTransformer.AnimatedVertex(pos4, this.lowerJoint),
                              new AzureArmorTransformer.AnimatedVertex(pos5, this.lowerJoint),
                              new AzureArmorTransformer.AnimatedVertex(pos5, this.middleJoint),
                              new AzureArmorTransformer.AnimatedVertex(pos4, this.middleJoint)
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
                        new AzureArmorTransformer.AnimatedPolygon(
                           new AzureArmorTransformer.AnimatedVertex[]{
                              new AzureArmorTransformer.AnimatedVertex(pos4, firstJoint),
                              new AzureArmorTransformer.AnimatedVertex(pos5, secondJoint),
                              new AzureArmorTransformer.AnimatedVertex(pos5, thirdJoint),
                              new AzureArmorTransformer.AnimatedVertex(pos4, fourthJoint)
                           },
                           0.001F,
                           direction
                        )
                     );
                     polygons.add(
                        new AzureArmorTransformer.AnimatedPolygon(
                           new AzureArmorTransformer.AnimatedVertex[]{
                              new AzureArmorTransformer.AnimatedVertex(pos4, fourthJoint),
                              new AzureArmorTransformer.AnimatedVertex(pos5, thirdJoint),
                              new AzureArmorTransformer.AnimatedVertex(pos5, fifthJoint),
                              new AzureArmorTransformer.AnimatedVertex(pos4, sixthJoint)
                           },
                           0.001F,
                           direction
                        )
                     );
                  }
               } else {
                  int jointId = pos0.f_104371_.y() > this.yClipCoord ? this.upperJoint : this.lowerJoint;
                  polygons.add(
                     new AzureArmorTransformer.AnimatedPolygon(
                        new AzureArmorTransformer.AnimatedVertex[]{
                           new AzureArmorTransformer.AnimatedVertex(pos0, jointId),
                           new AzureArmorTransformer.AnimatedVertex(pos1, jointId),
                           new AzureArmorTransformer.AnimatedVertex(pos2, jointId),
                           new AzureArmorTransformer.AnimatedVertex(pos3, jointId)
                        },
                        direction
                     )
                  );
               }
            }

            for (AzureArmorTransformer.AnimatedPolygon quad : polygons) {
               Vector3f norm = new Vector3f(quad.normal);
               norm.mul(poseStack.m_85850_().m_252943_());

               for (AzureArmorTransformer.AnimatedVertex vertex : quad.animatedVertexPositions) {
                  Vector4f pos = new Vector4f(vertex.f_104371_, 1.0F);
                  vertices.add(
                     new SingleGroupVertexBuilder()
                        .setPosition(new Vec3f(pos.x(), pos.y(), pos.z()))
                        .setNormal(new Vec3f(norm.x(), norm.y(), norm.z()))
                        .setTextureCoordinate(new Vec2f(vertex.f_104372_, vertex.f_104373_))
                        .setEffectiveJointIDs(new Vec3f(vertex.jointId.m_123341_(), 0.0F, 0.0F))
                        .setEffectiveJointWeights(new Vec3f(1.0F, 0.0F, 0.0F))
                        .setEffectiveJointNumber(1)
                  );
               }

               triangluatePolygon(indices, partName, indexCounter);
            }
         }
      }
   }

   static class SimpleTransformer extends HumanoidModelTransformer.PartTransformer<GeoCube> {
      final int jointId;

      public SimpleTransformer(int jointId) {
         this.jointId = jointId;
      }

      public void bakeCube(
         PoseStack poseStack,
         MeshPartDefinition partName,
         GeoCube cube,
         List<SingleGroupVertexBuilder> vertices,
         Map<MeshPartDefinition, IntList> indices,
         HumanoidModelTransformer.PartTransformer.IndexCounter indexCounter
      ) {
         for (GeoQuad quad : cube.quads()) {
            if (quad != null) {
               Vector3f norm = new Vector3f(quad.normal());
               norm.mul(poseStack.m_85850_().m_252943_());

               for (GeoVertex vertex : quad.vertices()) {
                  Vector4f pos = new Vector4f(vertex.position(), 1.0F);
                  pos.mul(poseStack.m_85850_().m_252922_());
                  vertices.add(
                     new SingleGroupVertexBuilder()
                        .setPosition(new Vec3f(pos.x(), pos.y(), pos.z()))
                        .setNormal(new Vec3f(norm.x(), norm.y(), norm.z()))
                        .setTextureCoordinate(new Vec2f(vertex.texU(), vertex.texV()))
                        .setEffectiveJointIDs(new Vec3f(this.jointId, 0.0F, 0.0F))
                        .setEffectiveJointWeights(new Vec3f(1.0F, 0.0F, 0.0F))
                        .setEffectiveJointNumber(1)
                  );
               }

               triangluatePolygon(indices, partName, indexCounter);
            }
         }
      }
   }
}
