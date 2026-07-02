package yesman.epicfight.world.level.block.entity;

import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import yesman.epicfight.world.level.block.FractureBlockState;

public class FractureBlockEntity extends BlockEntity {
   private Vector3f translate;
   private Quaternionf rotation;
   private BlockState originalBlockState;
   private double bouncing;
   private int maxLifeTime;
   private int lifeTime = 0;

   public FractureBlockEntity(BlockPos blockPos, BlockState originalBlockState) {
      super((BlockEntityType)EpicFightBlockEntities.FRACTURE.get(), blockPos, originalBlockState);
   }

   public FractureBlockEntity(BlockPos blockPos, BlockState blockState, FractureBlockState fractureBlockState) {
      super((BlockEntityType)EpicFightBlockEntities.FRACTURE.get(), blockPos, blockState);
      this.originalBlockState = fractureBlockState.getOriginalBlockState(blockPos);
      this.bouncing = fractureBlockState.getBouncing();
      this.translate = fractureBlockState.getTranslate();
      this.rotation = fractureBlockState.getRotation();
      this.maxLifeTime = fractureBlockState.getLifeTime();
   }

   public BlockState getOriginalBlockState() {
      return this.originalBlockState;
   }

   public Vector3f getTranslate() {
      return this.translate;
   }

   public Quaternionf getRotation() {
      return this.rotation;
   }

   public double getBouncing() {
      return this.bouncing;
   }

   public int getMaxLifeTime() {
      return this.maxLifeTime;
   }

   public int getLifeTime() {
      return this.lifeTime;
   }

   @OnlyIn(Dist.CLIENT)
   public static void lifeTimeTick(Level level, BlockPos blockPos, BlockState blockState, FractureBlockEntity fractureBlockEntity) {
      if (fractureBlockEntity.originalBlockState.m_245147_() && fractureBlockEntity.maxLifeTime - fractureBlockEntity.lifeTime < 10) {
         Particle blockParticle = new TerrainParticle(
            (ClientLevel)level,
            blockPos.m_123341_(),
            blockPos.m_123342_(),
            blockPos.m_123343_(),
            0.0,
            0.0,
            0.0,
            fractureBlockEntity.originalBlockState,
            blockPos
         );
         blockParticle.m_172260_((Math.random() - 0.5) * 0.3, Math.random() * 0.5, (Math.random() - 0.5) * 0.3);
         blockParticle.m_107257_(10 + new Random().nextInt(60));
         Minecraft mc = Minecraft.m_91087_();
         mc.f_91061_.m_107344_(blockParticle);
      }

      if (fractureBlockEntity.lifeTime++ > fractureBlockEntity.maxLifeTime) {
         level.m_46747_(blockPos);
         FractureBlockState.remove(blockPos);
         level.m_7731_(blockPos, fractureBlockEntity.getOriginalBlockState(), 0);
      }
   }
}
