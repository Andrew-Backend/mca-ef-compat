package yesman.epicfight.api.client.physics.cloth;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.function.Function;
import yesman.epicfight.api.utils.math.OpenMatrix4f;

public class ClothColliderPresets {
   public static final List<Pair<Function<ClothSimulatable, OpenMatrix4f>, ClothSimulator.ClothOBBCollider>> BIPED_SLIM = ImmutableList.builder()
      .add(
         Pair.of(
            (Function<ClothSimulatable, OpenMatrix4f>)simObject -> simObject.getArmature().getPoseMatrices()[1],
            new ClothSimulator.ClothOBBCollider(0.125, 0.24, 0.125, 0.0, 0.22, 0.0)
         )
      )
      .add(
         Pair.of(
            (Function<ClothSimulatable, OpenMatrix4f>)simObject -> simObject.getArmature().getPoseMatrices()[2],
            new ClothSimulator.ClothOBBCollider(0.125, 0.1875, 0.125, 0.0, 0.1875, 0.0)
         )
      )
      .add(
         Pair.of(
            (Function<ClothSimulatable, OpenMatrix4f>)simObject -> simObject.getArmature().getPoseMatrices()[4],
            new ClothSimulator.ClothOBBCollider(0.125, 0.24, 0.125, 0.0, 0.22, 0.0)
         )
      )
      .add(
         Pair.of(
            (Function<ClothSimulatable, OpenMatrix4f>)simObject -> simObject.getArmature().getPoseMatrices()[5],
            new ClothSimulator.ClothOBBCollider(0.125, 0.1875, 0.125, 0.0, 0.1875, 0.0)
         )
      )
      .add(
         Pair.of(
            (Function<ClothSimulatable, OpenMatrix4f>)simObject -> simObject.getArmature().getPoseMatrices()[7],
            new ClothSimulator.ClothOBBCollider(0.25, 0.25, 0.13, 0.0, 0.125, 0.0)
         )
      )
      .add(
         Pair.of(
            (Function<ClothSimulatable, OpenMatrix4f>)simObject -> simObject.getArmature().getPoseMatrices()[8],
            new ClothSimulator.ClothOBBCollider(0.25, 0.25, 0.13, 0.0, 0.3, 0.0)
         )
      )
      .add(
         Pair.of(
            (Function<ClothSimulatable, OpenMatrix4f>)simObject -> simObject.getArmature().getPoseMatrices()[9],
            new ClothSimulator.ClothOBBCollider(0.25, 0.25, 0.25, 0.0, 0.2, 0.0)
         )
      )
      .add(
         Pair.of(
            (Function<ClothSimulatable, OpenMatrix4f>)simObject -> simObject.getArmature().getPoseMatrices()[11],
            new ClothSimulator.ClothOBBCollider(0.12, 0.24, 0.125, -0.05, 0.14, 0.0)
         )
      )
      .add(
         Pair.of(
            (Function<ClothSimulatable, OpenMatrix4f>)simObject -> simObject.getArmature().getPoseMatrices()[12],
            new ClothSimulator.ClothOBBCollider(0.12, 0.1875, 0.125, -0.05, 0.14, 0.0)
         )
      )
      .add(
         Pair.of(
            (Function<ClothSimulatable, OpenMatrix4f>)simObject -> simObject.getArmature().getPoseMatrices()[16],
            new ClothSimulator.ClothOBBCollider(0.12, 0.24, 0.125, 0.05, 0.14, 0.0)
         )
      )
      .add(
         Pair.of(
            (Function<ClothSimulatable, OpenMatrix4f>)simObject -> simObject.getArmature().getPoseMatrices()[17],
            new ClothSimulator.ClothOBBCollider(0.12, 0.1875, 0.125, 0.05, 0.14, 0.0)
         )
      )
      .build();
   public static final List<Pair<Function<ClothSimulatable, OpenMatrix4f>, ClothSimulator.ClothOBBCollider>> BIPED = ImmutableList.builder()
      .add(
         Pair.of(
            (Function<ClothSimulatable, OpenMatrix4f>)simObject -> simObject.getArmature().getPoseMatrices()[1],
            new ClothSimulator.ClothOBBCollider(0.125, 0.24, 0.125, 0.0, 0.22, 0.0)
         )
      )
      .add(
         Pair.of(
            (Function<ClothSimulatable, OpenMatrix4f>)simObject -> simObject.getArmature().getPoseMatrices()[2],
            new ClothSimulator.ClothOBBCollider(0.125, 0.1875, 0.125, 0.0, 0.1875, 0.0)
         )
      )
      .add(
         Pair.of(
            (Function<ClothSimulatable, OpenMatrix4f>)simObject -> simObject.getArmature().getPoseMatrices()[4],
            new ClothSimulator.ClothOBBCollider(0.125, 0.24, 0.125, 0.0, 0.22, 0.0)
         )
      )
      .add(
         Pair.of(
            (Function<ClothSimulatable, OpenMatrix4f>)simObject -> simObject.getArmature().getPoseMatrices()[5],
            new ClothSimulator.ClothOBBCollider(0.125, 0.1875, 0.125, 0.0, 0.1875, 0.0)
         )
      )
      .add(
         Pair.of(
            (Function<ClothSimulatable, OpenMatrix4f>)simObject -> simObject.getArmature().getPoseMatrices()[7],
            new ClothSimulator.ClothOBBCollider(0.25, 0.25, 0.13, 0.0, 0.125, 0.0)
         )
      )
      .add(
         Pair.of(
            (Function<ClothSimulatable, OpenMatrix4f>)simObject -> simObject.getArmature().getPoseMatrices()[8],
            new ClothSimulator.ClothOBBCollider(0.25, 0.25, 0.13, 0.0, 0.3, 0.0)
         )
      )
      .add(
         Pair.of(
            (Function<ClothSimulatable, OpenMatrix4f>)simObject -> simObject.getArmature().getPoseMatrices()[9],
            new ClothSimulator.ClothOBBCollider(0.25, 0.25, 0.25, 0.0, 0.2, 0.0)
         )
      )
      .add(
         Pair.of(
            (Function<ClothSimulatable, OpenMatrix4f>)simObject -> simObject.getArmature().getPoseMatrices()[11],
            new ClothSimulator.ClothOBBCollider(0.13, 0.24, 0.13, -0.0, 0.14, 0.0)
         )
      )
      .add(
         Pair.of(
            (Function<ClothSimulatable, OpenMatrix4f>)simObject -> simObject.getArmature().getPoseMatrices()[12],
            new ClothSimulator.ClothOBBCollider(0.13, 0.1875, 0.13, -0.0, 0.14, 0.0)
         )
      )
      .add(
         Pair.of(
            (Function<ClothSimulatable, OpenMatrix4f>)simObject -> simObject.getArmature().getPoseMatrices()[16],
            new ClothSimulator.ClothOBBCollider(0.13, 0.24, 0.13, 0.0, 0.14, 0.0)
         )
      )
      .add(
         Pair.of(
            (Function<ClothSimulatable, OpenMatrix4f>)simObject -> simObject.getArmature().getPoseMatrices()[17],
            new ClothSimulator.ClothOBBCollider(0.13, 0.1875, 0.13, 0.0, 0.14, 0.0)
         )
      )
      .build();
}
