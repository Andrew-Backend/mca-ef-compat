package yesman.epicfight.api.client.model.transformer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.tr7zw.skinlayers.SkinLayersModBase;
import dev.tr7zw.skinlayers.api.LayerFeatureTransformerAPI;
import dev.tr7zw.skinlayers.api.MeshTransformer;
import dev.tr7zw.skinlayers.api.SkinLayersAPI;
import dev.tr7zw.skinlayers.versionless.render.CustomModelPart;
import dev.tr7zw.skinlayers.versionless.render.CustomizableCube;
import dev.tr7zw.skinlayers.versionless.render.CustomizableCube.Polygon;
import dev.tr7zw.skinlayers.versionless.util.Direction;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelPart.Cube;
import net.minecraft.client.model.geom.ModelPart.Vertex;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import yesman.epicfight.api.client.model.MeshPartDefinition;
import yesman.epicfight.api.client.model.SingleGroupVertexBuilder;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.utils.math.QuaternionUtils;
import yesman.epicfight.api.utils.math.Vec2f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.mixin.skinlayers.MixinCustomModelPart;
import yesman.epicfight.mixin.skinlayers.MixinCustomizableCubeWrapper;

public class SkinLayer3DTransformer extends CustomizableCube {
   static final HumanoidModelTransformer.PartTransformer<CustomizableCube> HEAD = new SkinLayer3DTransformer.SimpleTransformer(9);
   static final HumanoidModelTransformer.PartTransformer<CustomizableCube> LEFT_FEET = new SkinLayer3DTransformer.SimpleTransformer(5);
   static final HumanoidModelTransformer.PartTransformer<CustomizableCube> RIGHT_FEET = new SkinLayer3DTransformer.SimpleTransformer(2);
   static final HumanoidModelTransformer.PartTransformer<CustomizableCube> LEFT_ARM = new SkinLayer3DTransformer.LimbPartTransformer(16, 17, 19, 19.0F, false);
   static final HumanoidModelTransformer.PartTransformer<CustomizableCube> RIGHT_ARM = new SkinLayer3DTransformer.LimbPartTransformer(11, 12, 14, 19.0F, false);
   static final HumanoidModelTransformer.PartTransformer<CustomizableCube> LEFT_LEG = new SkinLayer3DTransformer.LimbPartTransformer(4, 5, 6, 6.0F, true);
   static final HumanoidModelTransformer.PartTransformer<CustomizableCube> RIGHT_LEG = new SkinLayer3DTransformer.LimbPartTransformer(1, 2, 3, 6.0F, true);
   static final HumanoidModelTransformer.PartTransformer<CustomizableCube> CHEST = new SkinLayer3DTransformer.ChestPartTransformer(18.0F);

   private SkinLayer3DTransformer() {
      super(0, 0, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, false, 0.0F, 0.0F, new Direction[0], new Direction[][]{new Direction[0]});
   }

   public static SkinnedMesh transformMesh(
      AbstractClientPlayer abstractClientPlayer,
      CustomModelPart skinlayerModelPart,
      ModelPart vanillaModelPart,
      PlayerModelPart modelPart,
      List<Cube> vanillaCubes,
      List<CustomizableCube> cubes
   ) {
      List<SkinLayer3DTransformer.ModelPartition> partitions = Lists.newArrayList();
      float widthScale = SkinLayersModBase.config.baseVoxelSize;
      float heightScale = 1.035F;
      switch (modelPart) {
         case JACKET:
            partitions.add(
               new SkinLayer3DTransformer.ModelPartition(
                  VanillaModelTransformer.CHEST,
                  CHEST,
                  "jacket",
                  skinlayerModelPart,
                  vanillaModelPart,
                  vanillaCubes,
                  cubes,
                  poseStack -> poseStack.m_85841_(SkinLayersModBase.config.bodyVoxelWidthSize, heightScale, widthScale)
               )
            );
            break;
         case LEFT_SLEEVE:
            partitions.add(
               new SkinLayer3DTransformer.ModelPartition(
                  VanillaModelTransformer.LEFT_ARM,
                  LEFT_ARM,
                  "leftSleeve",
                  skinlayerModelPart,
                  vanillaModelPart,
                  vanillaCubes,
                  cubes,
                  poseStack -> poseStack.m_85841_(widthScale, heightScale, widthScale)
               )
            );
            break;
         case RIGHT_SLEEVE:
            partitions.add(
               new SkinLayer3DTransformer.ModelPartition(
                  VanillaModelTransformer.RIGHT_ARM,
                  RIGHT_ARM,
                  "rightSleeve",
                  skinlayerModelPart,
                  vanillaModelPart,
                  vanillaCubes,
                  cubes,
                  poseStack -> poseStack.m_85841_(widthScale, heightScale, widthScale)
               )
            );
            break;
         case LEFT_PANTS_LEG:
            partitions.add(
               new SkinLayer3DTransformer.ModelPartition(
                  VanillaModelTransformer.LEFT_LEG,
                  LEFT_LEG,
                  "leftPantsLeg",
                  skinlayerModelPart,
                  vanillaModelPart,
                  vanillaCubes,
                  cubes,
                  poseStack -> poseStack.m_85841_(widthScale, heightScale, widthScale)
               )
            );
            break;
         case RIGHT_PANTS_LEG:
            partitions.add(
               new SkinLayer3DTransformer.ModelPartition(
                  VanillaModelTransformer.RIGHT_LEG,
                  RIGHT_LEG,
                  "rightPantsLeg",
                  skinlayerModelPart,
                  vanillaModelPart,
                  vanillaCubes,
                  cubes,
                  poseStack -> poseStack.m_85841_(widthScale, heightScale, widthScale)
               )
            );
            break;
         case HAT:
            partitions.add(
               new SkinLayer3DTransformer.ModelPartition(
                  VanillaModelTransformer.HEAD, HEAD, "hat", skinlayerModelPart, vanillaModelPart, vanillaCubes, cubes, poseStack -> {
                     float headsize = SkinLayersModBase.config.headVoxelSize;
                     poseStack.m_85837_(0.0, -6.0, 0.0);
                     poseStack.m_85841_(headsize, headsize, headsize);
                     poseStack.m_85837_(0.0, 6.0, 0.0);
                     poseStack.m_85837_(0.0, -0.96, 0.0);
                  }
               )
            );
            break;
         default:
            return null;
      }

      return bakeMeshFromCubes(abstractClientPlayer, partitions);
   }

   private static SkinnedMesh bakeMeshFromCubes(AbstractClientPlayer abstractClientPlayer, List<SkinLayer3DTransformer.ModelPartition> partitions) {
      List<SingleGroupVertexBuilder> vertices = Lists.newArrayList();
      Map<MeshPartDefinition, IntList> indices = Maps.newHashMap();
      PoseStack poseStack = new PoseStack();
      HumanoidModelTransformer.PartTransformer.IndexCounter indexCounter = new HumanoidModelTransformer.PartTransformer.IndexCounter();
      poseStack.m_252781_(QuaternionUtils.YP.rotationDegrees(180.0F));
      poseStack.m_252781_(QuaternionUtils.XP.rotationDegrees(180.0F));
      poseStack.m_252880_(0.0F, -24.0F, 0.0F);

      for (SkinLayer3DTransformer.ModelPartition modelpartition : partitions) {
         bake(abstractClientPlayer, poseStack, modelpartition, vertices, indices, indexCounter);
      }

      return SingleGroupVertexBuilder.loadVertexInformation(vertices, indices);
   }

   private static void bake(
      AbstractClientPlayer abstractClientPlayer,
      PoseStack poseStack,
      SkinLayer3DTransformer.ModelPartition modelpartition,
      List<SingleGroupVertexBuilder> vertices,
      Map<MeshPartDefinition, IntList> indices,
      HumanoidModelTransformer.PartTransformer.IndexCounter indexCounter
   ) {
      modelpartition.vanillaModelPart.m_171322_(modelpartition.vanillaModelPart.m_233566_());
      ModelPart part = modelpartition.vanillaModelPart;
      poseStack.m_85836_();
      poseStack.m_252880_(part.f_104200_, part.f_104201_, part.f_104202_);
      if (part.f_104203_ != 0.0F || part.f_104204_ != 0.0F || part.f_104205_ != 0.0F) {
         poseStack.m_252781_(new Quaternionf().rotationZYX(part.f_104205_, part.f_104204_, part.f_104203_));
      }

      if (part.f_233553_ != 1.0F || part.f_233554_ != 1.0F || part.f_233555_ != 1.0F) {
         poseStack.m_85841_(part.f_233553_, part.f_233554_, part.f_233555_);
      }

      MeshTransformer transformer = SkinLayersAPI.getMeshTransformerProvider().prepareTransformer(modelpartition.vanillaModelPart);
      LayerFeatureTransformerAPI.getTransformer().transform(abstractClientPlayer, poseStack, modelpartition.vanillaModelPart);
      modelpartition.transformFunction.accept(poseStack);
      MixinCustomModelPart customModelPart = (MixinCustomModelPart)modelpartition.skinlayerModelPart;
      poseStack.m_252880_(customModelPart.getX(), customModelPart.getY(), customModelPart.getZ());
      if (customModelPart.getXRot() != 0.0F || customModelPart.getYRot() != 0.0F || customModelPart.getZRot() != 0.0F) {
         poseStack.m_252781_(new Quaternionf().rotationZYX(customModelPart.getXRot(), customModelPart.getYRot(), customModelPart.getZRot()));
      }

      for (Cube cube : modelpartition.vanillaCubes) {
         transformer.transform(cube);
         modelpartition.vanillaPartTransformer
            .bakeCube(poseStack, VanillaModelTransformer.VanillaMeshPartDefinition.of(modelpartition.partName), cube, vertices, indices, indexCounter);
      }

      for (CustomizableCube cube : modelpartition.customizableCubes) {
         modelpartition.partTransformer
            .bakeCube(poseStack, VanillaModelTransformer.VanillaMeshPartDefinition.of(modelpartition.partName), cube, vertices, indices, indexCounter);
      }

      poseStack.m_85849_();
   }

   static net.minecraft.core.Direction getDirectionFromVector(float x, float y, float z) {
      for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
         Vector3f direcVec = new Vector3f(Float.compare(x, -0.0F) == 0 ? 0.0F : x, y, z);
         if (direcVec.equals(direction.m_253071_())) {
            return direction;
         }
      }

      return null;
   }

   static Vector3f getClipPoint(Vector3f pos1, Vector3f pos2, float yClip) {
      Vector3f direct = new Vector3f(pos2);
      direct.sub(pos1);
      direct.mul((yClip - pos1.y()) / (pos2.y() - pos1.y()));
      Vector3f clipPoint = new Vector3f(pos1);
      clipPoint.add(direct);
      return clipPoint;
   }

   static Vertex getTranslatedVertex(dev.tr7zw.skinlayers.versionless.render.CustomizableCube.Vertex original, Matrix4f matrix) {
      Vector4f translatedPosition = new Vector4f(original.pos.x, original.pos.y, original.pos.z, 1.0F);
      translatedPosition.mul(matrix);
      return new Vertex(translatedPosition.x(), translatedPosition.y(), translatedPosition.z(), original.u, original.v);
   }

   static class AnimatedPolygon {
      public final SkinLayer3DTransformer.AnimatedVertex[] animatedVertexPositions;
      public final Vector3f normal;

      public AnimatedPolygon(SkinLayer3DTransformer.AnimatedVertex[] positionsIn, net.minecraft.core.Direction directionIn) {
         this.animatedVertexPositions = positionsIn;
         this.normal = directionIn.m_253071_();
      }

      public AnimatedPolygon(SkinLayer3DTransformer.AnimatedVertex[] positionsIn, float cor, net.minecraft.core.Direction directionIn) {
         this.animatedVertexPositions = positionsIn;
         positionsIn[0] = new SkinLayer3DTransformer.AnimatedVertex(
            positionsIn[0], positionsIn[0].f_104372_, positionsIn[0].f_104373_ + cor, positionsIn[0].jointId, positionsIn[0].weight
         );
         positionsIn[1] = new SkinLayer3DTransformer.AnimatedVertex(
            positionsIn[1], positionsIn[1].f_104372_, positionsIn[1].f_104373_ + cor, positionsIn[1].jointId, positionsIn[1].weight
         );
         positionsIn[2] = new SkinLayer3DTransformer.AnimatedVertex(
            positionsIn[2], positionsIn[2].f_104372_, positionsIn[2].f_104373_ - cor, positionsIn[2].jointId, positionsIn[2].weight
         );
         positionsIn[3] = new SkinLayer3DTransformer.AnimatedVertex(
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

   static class ChestPartTransformer extends HumanoidModelTransformer.PartTransformer<CustomizableCube> {
      static final float X_PLANE = 0.0F;
      static final SkinLayer3DTransformer.ChestPartTransformer.VertexWeight[] WEIGHT_ALONG_Y = new SkinLayer3DTransformer.ChestPartTransformer.VertexWeight[]{
         new SkinLayer3DTransformer.ChestPartTransformer.VertexWeight(13.6666F, 0.23F, 0.77F),
         new SkinLayer3DTransformer.ChestPartTransformer.VertexWeight(15.8333F, 0.254F, 0.746F),
         new SkinLayer3DTransformer.ChestPartTransformer.VertexWeight(18.0F, 0.5F, 0.5F),
         new SkinLayer3DTransformer.ChestPartTransformer.VertexWeight(20.1666F, 0.744F, 0.256F),
         new SkinLayer3DTransformer.ChestPartTransformer.VertexWeight(22.3333F, 0.77F, 0.23F)
      };
      final float yClipCoord;

      public ChestPartTransformer(float yBasis) {
         this.yClipCoord = yBasis;
      }

      public void bakeCube(
         PoseStack poseStack,
         MeshPartDefinition partName,
         CustomizableCube cube,
         List<SingleGroupVertexBuilder> vertices,
         Map<MeshPartDefinition, IntList> indices,
         HumanoidModelTransformer.PartTransformer.IndexCounter indexCounter
      ) {
         List<SkinLayer3DTransformer.AnimatedPolygon> xClipPolygons = Lists.newArrayList();
         List<SkinLayer3DTransformer.AnimatedPolygon> xyClipPolygons = Lists.newArrayList();
         Polygon[] polygons = ((MixinCustomizableCubeWrapper.SkinLayer3DMixinCustomModelCube)cube).getPolygons();

         for (Polygon polygon : polygons) {
            if (polygon != null) {
               Matrix4f matrix = poseStack.m_85850_().m_252922_();
               Vertex pos0 = SkinLayer3DTransformer.getTranslatedVertex(polygon.vertices[0], matrix);
               Vertex pos1 = SkinLayer3DTransformer.getTranslatedVertex(polygon.vertices[1], matrix);
               Vertex pos2 = SkinLayer3DTransformer.getTranslatedVertex(polygon.vertices[2], matrix);
               Vertex pos3 = SkinLayer3DTransformer.getTranslatedVertex(polygon.vertices[3], matrix);
               net.minecraft.core.Direction direction = SkinLayer3DTransformer.getDirectionFromVector(polygon.normal.x, polygon.normal.y, polygon.normal.z);
               SkinLayer3DTransformer.ChestPartTransformer.VertexWeight pos0Weight = getYClipWeight(pos0.f_104371_.y());
               SkinLayer3DTransformer.ChestPartTransformer.VertexWeight pos1Weight = getYClipWeight(pos1.f_104371_.y());
               SkinLayer3DTransformer.ChestPartTransformer.VertexWeight pos2Weight = getYClipWeight(pos2.f_104371_.y());
               SkinLayer3DTransformer.ChestPartTransformer.VertexWeight pos3Weight = getYClipWeight(pos3.f_104371_.y());
               if (pos1.f_104371_.x() > 0.0F != pos2.f_104371_.x() > 0.0F) {
                  float distance = pos2.f_104371_.x() - pos1.f_104371_.x();
                  float textureU = pos1.f_104372_ + (pos2.f_104372_ - pos1.f_104372_) * ((0.0F - pos1.f_104371_.x()) / distance);
                  Vertex pos4 = new Vertex(0.0F, pos0.f_104371_.y(), pos0.f_104371_.z(), textureU, pos0.f_104373_);
                  Vertex pos5 = new Vertex(0.0F, pos1.f_104371_.y(), pos1.f_104371_.z(), textureU, pos1.f_104373_);
                  xClipPolygons.add(
                     new SkinLayer3DTransformer.AnimatedPolygon(
                        new SkinLayer3DTransformer.AnimatedVertex[]{
                           new SkinLayer3DTransformer.AnimatedVertex(pos0, 8, 7, 0, pos0Weight.chestWeight, pos0Weight.torsoWeight, 0.0F),
                           new SkinLayer3DTransformer.AnimatedVertex(pos4, 8, 7, 0, pos0Weight.chestWeight, pos0Weight.torsoWeight, 0.0F),
                           new SkinLayer3DTransformer.AnimatedVertex(pos5, 8, 7, 0, pos1Weight.chestWeight, pos1Weight.torsoWeight, 0.0F),
                           new SkinLayer3DTransformer.AnimatedVertex(pos3, 8, 7, 0, pos3Weight.chestWeight, pos3Weight.torsoWeight, 0.0F)
                        },
                        direction
                     )
                  );
                  xClipPolygons.add(
                     new SkinLayer3DTransformer.AnimatedPolygon(
                        new SkinLayer3DTransformer.AnimatedVertex[]{
                           new SkinLayer3DTransformer.AnimatedVertex(pos4, 8, 7, 0, pos0Weight.chestWeight, pos0Weight.torsoWeight, 0.0F),
                           new SkinLayer3DTransformer.AnimatedVertex(pos1, 8, 7, 0, pos1Weight.chestWeight, pos1Weight.torsoWeight, 0.0F),
                           new SkinLayer3DTransformer.AnimatedVertex(pos2, 8, 7, 0, pos2Weight.chestWeight, pos2Weight.torsoWeight, 0.0F),
                           new SkinLayer3DTransformer.AnimatedVertex(pos5, 8, 7, 0, pos1Weight.chestWeight, pos1Weight.torsoWeight, 0.0F)
                        },
                        direction
                     )
                  );
               } else {
                  xClipPolygons.add(
                     new SkinLayer3DTransformer.AnimatedPolygon(
                        new SkinLayer3DTransformer.AnimatedVertex[]{
                           new SkinLayer3DTransformer.AnimatedVertex(pos0, 8, 7, 0, pos0Weight.chestWeight, pos0Weight.torsoWeight, 0.0F),
                           new SkinLayer3DTransformer.AnimatedVertex(pos1, 8, 7, 0, pos1Weight.chestWeight, pos1Weight.torsoWeight, 0.0F),
                           new SkinLayer3DTransformer.AnimatedVertex(pos2, 8, 7, 0, pos2Weight.chestWeight, pos2Weight.torsoWeight, 0.0F),
                           new SkinLayer3DTransformer.AnimatedVertex(pos3, 8, 7, 0, pos3Weight.chestWeight, pos3Weight.torsoWeight, 0.0F)
                        },
                        direction
                     )
                  );
               }
            }
         }

         for (SkinLayer3DTransformer.AnimatedPolygon polygon : xClipPolygons) {
            boolean upsideDown = polygon.animatedVertexPositions[1].f_104371_.y() > polygon.animatedVertexPositions[2].f_104371_.y();
            SkinLayer3DTransformer.AnimatedVertex pos0 = upsideDown ? polygon.animatedVertexPositions[2] : polygon.animatedVertexPositions[0];
            SkinLayer3DTransformer.AnimatedVertex pos1 = upsideDown ? polygon.animatedVertexPositions[3] : polygon.animatedVertexPositions[1];
            SkinLayer3DTransformer.AnimatedVertex pos2 = upsideDown ? polygon.animatedVertexPositions[0] : polygon.animatedVertexPositions[2];
            SkinLayer3DTransformer.AnimatedVertex pos3 = upsideDown ? polygon.animatedVertexPositions[1] : polygon.animatedVertexPositions[3];
            net.minecraft.core.Direction direction = SkinLayer3DTransformer.getDirectionFromVector(polygon.normal.x, polygon.normal.y, polygon.normal.z);
            List<SkinLayer3DTransformer.ChestPartTransformer.VertexWeight> vertexWeights = getMiddleYClipWeights(pos1.f_104371_.y(), pos2.f_104371_.y());
            List<SkinLayer3DTransformer.AnimatedVertex> animatedVertices = Lists.newArrayList();
            animatedVertices.add(pos0);
            animatedVertices.add(pos1);
            if (vertexWeights.size() > 0) {
               for (SkinLayer3DTransformer.ChestPartTransformer.VertexWeight vertexWeight : vertexWeights) {
                  float distance = pos2.f_104371_.y() - pos1.f_104371_.y();
                  float textureV = pos1.f_104373_ + (pos2.f_104373_ - pos1.f_104373_) * ((vertexWeight.yClipCoord - pos1.f_104371_.y()) / distance);
                  Vector3f clipPos1 = SkinLayer3DTransformer.getClipPoint(pos1.f_104371_, pos2.f_104371_, vertexWeight.yClipCoord);
                  Vector3f clipPos2 = SkinLayer3DTransformer.getClipPoint(pos0.f_104371_, pos3.f_104371_, vertexWeight.yClipCoord);
                  Vertex pos4 = new Vertex(clipPos2, pos0.f_104372_, textureV);
                  Vertex pos5 = new Vertex(clipPos1, pos1.f_104372_, textureV);
                  animatedVertices.add(new SkinLayer3DTransformer.AnimatedVertex(pos4, 8, 7, 0, vertexWeight.chestWeight, vertexWeight.torsoWeight, 0.0F));
                  animatedVertices.add(new SkinLayer3DTransformer.AnimatedVertex(pos5, 8, 7, 0, vertexWeight.chestWeight, vertexWeight.torsoWeight, 0.0F));
               }
            }

            animatedVertices.add(pos3);
            animatedVertices.add(pos2);

            for (int i = 0; i < (animatedVertices.size() - 2) / 2; i++) {
               int start = i * 2;
               SkinLayer3DTransformer.AnimatedVertex p0 = animatedVertices.get(start);
               SkinLayer3DTransformer.AnimatedVertex p1 = animatedVertices.get(start + 1);
               SkinLayer3DTransformer.AnimatedVertex p2 = animatedVertices.get(start + 3);
               SkinLayer3DTransformer.AnimatedVertex p3 = animatedVertices.get(start + 2);
               xyClipPolygons.add(
                  new SkinLayer3DTransformer.AnimatedPolygon(
                     new SkinLayer3DTransformer.AnimatedVertex[]{
                        new SkinLayer3DTransformer.AnimatedVertex(p0, 8, 7, 0, p0.weight.x, p0.weight.y, 0.0F),
                        new SkinLayer3DTransformer.AnimatedVertex(p1, 8, 7, 0, p1.weight.x, p1.weight.y, 0.0F),
                        new SkinLayer3DTransformer.AnimatedVertex(p2, 8, 7, 0, p2.weight.x, p2.weight.y, 0.0F),
                        new SkinLayer3DTransformer.AnimatedVertex(p3, 8, 7, 0, p3.weight.x, p3.weight.y, 0.0F)
                     },
                     direction
                  )
               );
            }
         }

         for (SkinLayer3DTransformer.AnimatedPolygon polygon : xyClipPolygons) {
            Vector3f norm = new Vector3f(polygon.normal);
            norm.mul(poseStack.m_85850_().m_252943_());

            for (SkinLayer3DTransformer.AnimatedVertex vertex : polygon.animatedVertexPositions) {
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

            triangluatePolygon(indices, partName, indexCounter);
         }
      }

      static SkinLayer3DTransformer.ChestPartTransformer.VertexWeight getYClipWeight(float y) {
         if (y < WEIGHT_ALONG_Y[0].yClipCoord) {
            return new SkinLayer3DTransformer.ChestPartTransformer.VertexWeight(y, 0.0F, 1.0F);
         }

         int index = -1;
         int i = 0;

         while (i < WEIGHT_ALONG_Y.length) {
            i++;
         }

         if (index > 0) {
            SkinLayer3DTransformer.ChestPartTransformer.VertexWeight pair = WEIGHT_ALONG_Y[index];
            return new SkinLayer3DTransformer.ChestPartTransformer.VertexWeight(y, pair.chestWeight, pair.torsoWeight);
         } else {
            return new SkinLayer3DTransformer.ChestPartTransformer.VertexWeight(y, 1.0F, 0.0F);
         }
      }

      static List<SkinLayer3DTransformer.ChestPartTransformer.VertexWeight> getMiddleYClipWeights(float minY, float maxY) {
         List<SkinLayer3DTransformer.ChestPartTransformer.VertexWeight> cutYs = Lists.newArrayList();

         for (SkinLayer3DTransformer.ChestPartTransformer.VertexWeight vertexWeight : WEIGHT_ALONG_Y) {
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

   static class LimbPartTransformer extends HumanoidModelTransformer.PartTransformer<CustomizableCube> {
      final int upperJoint;
      final int lowerJoint;
      final int middleJoint;
      final boolean bendInFront;
      final float yClipCoord;

      public LimbPartTransformer(int upperJoint, int lowerJoint, int middleJoint, float yClipCoord, boolean bendInFront) {
         this.upperJoint = upperJoint;
         this.lowerJoint = lowerJoint;
         this.middleJoint = middleJoint;
         this.bendInFront = bendInFront;
         this.yClipCoord = yClipCoord;
      }

      public void bakeCube(
         PoseStack poseStack,
         MeshPartDefinition partName,
         CustomizableCube cube,
         List<SingleGroupVertexBuilder> vertices,
         Map<MeshPartDefinition, IntList> indices,
         HumanoidModelTransformer.PartTransformer.IndexCounter indexCounter
      ) {
         List<SkinLayer3DTransformer.AnimatedPolygon> animatedPolygons = Lists.newArrayList();
         Polygon[] polygons = ((MixinCustomizableCubeWrapper.SkinLayer3DMixinCustomModelCube)cube).getPolygons();

         for (Polygon polygon : polygons) {
            if (polygon != null) {
               Matrix4f matrix = poseStack.m_85850_().m_252922_();
               Vertex pos0 = SkinLayer3DTransformer.getTranslatedVertex(polygon.vertices[0], matrix);
               Vertex pos1 = SkinLayer3DTransformer.getTranslatedVertex(polygon.vertices[1], matrix);
               Vertex pos2 = SkinLayer3DTransformer.getTranslatedVertex(polygon.vertices[2], matrix);
               Vertex pos3 = SkinLayer3DTransformer.getTranslatedVertex(polygon.vertices[3], matrix);
               net.minecraft.core.Direction direction = SkinLayer3DTransformer.getDirectionFromVector(polygon.normal.x, polygon.normal.y, polygon.normal.z);
               if (pos1.f_104371_.y() > this.yClipCoord != pos2.f_104371_.y() > this.yClipCoord) {
                  float distance = pos2.f_104371_.y() - pos1.f_104371_.y();
                  float textureV = pos1.f_104373_ + (pos2.f_104373_ - pos1.f_104373_) * ((this.yClipCoord - pos1.f_104371_.y()) / distance);
                  Vector3f clipPos1 = SkinLayer3DTransformer.getClipPoint(pos1.f_104371_, pos2.f_104371_, this.yClipCoord);
                  Vector3f clipPos2 = SkinLayer3DTransformer.getClipPoint(pos0.f_104371_, pos3.f_104371_, this.yClipCoord);
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

                  animatedPolygons.add(
                     new SkinLayer3DTransformer.AnimatedPolygon(
                        new SkinLayer3DTransformer.AnimatedVertex[]{
                           new SkinLayer3DTransformer.AnimatedVertex(pos0, upperId),
                           new SkinLayer3DTransformer.AnimatedVertex(pos1, upperId),
                           new SkinLayer3DTransformer.AnimatedVertex(pos5, upperId),
                           new SkinLayer3DTransformer.AnimatedVertex(pos4, upperId)
                        },
                        direction
                     )
                  );
                  animatedPolygons.add(
                     new SkinLayer3DTransformer.AnimatedPolygon(
                        new SkinLayer3DTransformer.AnimatedVertex[]{
                           new SkinLayer3DTransformer.AnimatedVertex(pos4, lowerId),
                           new SkinLayer3DTransformer.AnimatedVertex(pos5, lowerId),
                           new SkinLayer3DTransformer.AnimatedVertex(pos2, lowerId),
                           new SkinLayer3DTransformer.AnimatedVertex(pos3, lowerId)
                        },
                        direction
                     )
                  );
                  boolean hasSameZ = pos4.f_104371_.z() < 0.0F == pos5.f_104371_.z() < 0.0F;
                  boolean isFront = hasSameZ && pos4.f_104371_.z() < 0.0F == this.bendInFront;
                  if (isFront) {
                     animatedPolygons.add(
                        new SkinLayer3DTransformer.AnimatedPolygon(
                           new SkinLayer3DTransformer.AnimatedVertex[]{
                              new SkinLayer3DTransformer.AnimatedVertex(pos4, this.middleJoint),
                              new SkinLayer3DTransformer.AnimatedVertex(pos5, this.middleJoint),
                              new SkinLayer3DTransformer.AnimatedVertex(pos5, this.upperJoint),
                              new SkinLayer3DTransformer.AnimatedVertex(pos4, this.upperJoint)
                           },
                           0.001F,
                           direction
                        )
                     );
                     animatedPolygons.add(
                        new SkinLayer3DTransformer.AnimatedPolygon(
                           new SkinLayer3DTransformer.AnimatedVertex[]{
                              new SkinLayer3DTransformer.AnimatedVertex(pos4, this.lowerJoint),
                              new SkinLayer3DTransformer.AnimatedVertex(pos5, this.lowerJoint),
                              new SkinLayer3DTransformer.AnimatedVertex(pos5, this.middleJoint),
                              new SkinLayer3DTransformer.AnimatedVertex(pos4, this.middleJoint)
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
                     animatedPolygons.add(
                        new SkinLayer3DTransformer.AnimatedPolygon(
                           new SkinLayer3DTransformer.AnimatedVertex[]{
                              new SkinLayer3DTransformer.AnimatedVertex(pos4, firstJoint),
                              new SkinLayer3DTransformer.AnimatedVertex(pos5, secondJoint),
                              new SkinLayer3DTransformer.AnimatedVertex(pos5, thirdJoint),
                              new SkinLayer3DTransformer.AnimatedVertex(pos4, fourthJoint)
                           },
                           0.001F,
                           direction
                        )
                     );
                     animatedPolygons.add(
                        new SkinLayer3DTransformer.AnimatedPolygon(
                           new SkinLayer3DTransformer.AnimatedVertex[]{
                              new SkinLayer3DTransformer.AnimatedVertex(pos4, fourthJoint),
                              new SkinLayer3DTransformer.AnimatedVertex(pos5, thirdJoint),
                              new SkinLayer3DTransformer.AnimatedVertex(pos5, fifthJoint),
                              new SkinLayer3DTransformer.AnimatedVertex(pos4, sixthJoint)
                           },
                           0.001F,
                           direction
                        )
                     );
                  }
               } else {
                  int jointId = pos0.f_104371_.y() > this.yClipCoord ? this.upperJoint : this.lowerJoint;
                  animatedPolygons.add(
                     new SkinLayer3DTransformer.AnimatedPolygon(
                        new SkinLayer3DTransformer.AnimatedVertex[]{
                           new SkinLayer3DTransformer.AnimatedVertex(pos0, jointId),
                           new SkinLayer3DTransformer.AnimatedVertex(pos1, jointId),
                           new SkinLayer3DTransformer.AnimatedVertex(pos2, jointId),
                           new SkinLayer3DTransformer.AnimatedVertex(pos3, jointId)
                        },
                        direction
                     )
                  );
               }
            }
         }

         for (SkinLayer3DTransformer.AnimatedPolygon quad : animatedPolygons) {
            Vector3f norm = new Vector3f(quad.normal);
            norm.mul(poseStack.m_85850_().m_252943_());

            for (SkinLayer3DTransformer.AnimatedVertex vertex : quad.animatedVertexPositions) {
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

            triangluatePolygon(indices, partName, indexCounter);
         }
      }
   }

   static class ModelPartition {
      final HumanoidModelTransformer.PartTransformer<Cube> vanillaPartTransformer;
      final HumanoidModelTransformer.PartTransformer<CustomizableCube> partTransformer;
      final String partName;
      final CustomModelPart skinlayerModelPart;
      final ModelPart vanillaModelPart;
      final List<Cube> vanillaCubes;
      final List<CustomizableCube> customizableCubes;
      final Consumer<PoseStack> transformFunction;

      private ModelPartition(
         HumanoidModelTransformer.PartTransformer<Cube> vanillaPartTransformer,
         HumanoidModelTransformer.PartTransformer<CustomizableCube> partTransformer,
         String partName,
         CustomModelPart skinlayerModelPart,
         ModelPart vanillaModelPart,
         List<Cube> vanillaCubes,
         List<CustomizableCube> customCubes,
         Consumer<PoseStack> transformFunction
      ) {
         this.vanillaPartTransformer = vanillaPartTransformer;
         this.partTransformer = partTransformer;
         this.partName = partName;
         this.skinlayerModelPart = skinlayerModelPart;
         this.vanillaModelPart = vanillaModelPart;
         this.vanillaCubes = vanillaCubes;
         this.customizableCubes = customCubes;
         this.transformFunction = transformFunction;
      }
   }

   static class SimpleTransformer extends HumanoidModelTransformer.PartTransformer<CustomizableCube> {
      final int jointId;

      public SimpleTransformer(int jointId) {
         this.jointId = jointId;
      }

      public void bakeCube(
         PoseStack poseStack,
         MeshPartDefinition partName,
         CustomizableCube cube,
         List<SingleGroupVertexBuilder> vertices,
         Map<MeshPartDefinition, IntList> indices,
         HumanoidModelTransformer.PartTransformer.IndexCounter indexCounter
      ) {
         Polygon[] polygons = ((MixinCustomizableCubeWrapper.SkinLayer3DMixinCustomModelCube)cube).getPolygons();

         for (Polygon polygon : polygons) {
            if (polygon != null) {
               Vector3f norm = new Vector3f(polygon.normal.x, polygon.normal.y, polygon.normal.z);
               norm.mul(poseStack.m_85850_().m_252943_());

               for (dev.tr7zw.skinlayers.versionless.render.CustomizableCube.Vertex vertex : polygon.vertices) {
                  Vector4f pos = new Vector4f(vertex.pos.x, vertex.pos.y, vertex.pos.z, 1.0F);
                  pos.mul(poseStack.m_85850_().m_252922_());
                  vertices.add(
                     new SingleGroupVertexBuilder()
                        .setPosition(new Vec3f(pos.x(), pos.y(), pos.z()).scale(0.0625F))
                        .setNormal(new Vec3f(norm.x(), norm.y(), norm.z()))
                        .setTextureCoordinate(new Vec2f(vertex.u, vertex.v))
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
