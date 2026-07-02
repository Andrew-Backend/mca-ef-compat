package yesman.epicfight.api.client.model.transformer;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.HumanoidModel;
import yesman.epicfight.api.client.model.MeshPartDefinition;
import yesman.epicfight.api.client.model.SingleGroupVertexBuilder;
import yesman.epicfight.api.client.model.SkinnedMesh;

public abstract class HumanoidModelTransformer {
   public abstract SkinnedMesh transformArmorModel(HumanoidModel<?> var1);

   public abstract static class PartTransformer<T> {
      public abstract void bakeCube(
         PoseStack var1,
         MeshPartDefinition var2,
         T var3,
         List<SingleGroupVertexBuilder> var4,
         Map<MeshPartDefinition, IntList> var5,
         HumanoidModelTransformer.PartTransformer.IndexCounter var6
      );

      static void triangluatePolygon(
         Map<MeshPartDefinition, IntList> indices, MeshPartDefinition partDefinition, HumanoidModelTransformer.PartTransformer.IndexCounter indexCounter
      ) {
         IntList list = indices.computeIfAbsent(partDefinition, key -> new IntArrayList());

         for (int i = 0; i < 3; i++) {
            list.add(indexCounter.first());
         }

         for (int i = 0; i < 3; i++) {
            list.add(indexCounter.second());
         }

         for (int i = 0; i < 3; i++) {
            list.add(indexCounter.fourth());
         }

         for (int i = 0; i < 3; i++) {
            list.add(indexCounter.fourth());
         }

         for (int i = 0; i < 3; i++) {
            list.add(indexCounter.second());
         }

         for (int i = 0; i < 3; i++) {
            list.add(indexCounter.third());
         }

         indexCounter.count();
      }

      public static class IndexCounter {
         private int indexCounter = 0;

         private int first() {
            return this.indexCounter;
         }

         private int second() {
            return this.indexCounter + 1;
         }

         private int third() {
            return this.indexCounter + 2;
         }

         private int fourth() {
            return this.indexCounter + 3;
         }

         private void count() {
            this.indexCounter += 4;
         }
      }
   }
}
