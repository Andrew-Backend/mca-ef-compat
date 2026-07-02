package yesman.epicfight.client.particle;

import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.block.state.BlockState;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.world.level.block.FractureBlockState;

public class GroundSlamParticle extends NoRenderParticle {
   protected GroundSlamParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, BlockPos bp, BlockState bs) {
      super(level, x, y, z, dx, dy, dz);
      if (bs.m_60795_()) {
         bs = level.m_8055_(bp.m_7495_());
      }

      if (bs instanceof FractureBlockState fractureBlockState) {
         bs = fractureBlockState.getOriginalBlockState(bp);
         if (bs == null) {
            bs = level.m_8055_(bp);
         }
      }

      if (bs.m_245147_()) {
         Minecraft mc = Minecraft.m_91087_();

         for (int i = 0; i < (int)dy; i++) {
            OpenMatrix4f mat = OpenMatrix4f.createRotatorDeg((float)Math.random() * 360.0F, Vec3f.Y_AXIS);
            Vec3f positionVec = OpenMatrix4f.transform3v(mat, Vec3f.Z_AXIS, null).scale((float)dx);
            Vec3f moveVec = OpenMatrix4f.transform3v(mat, Vec3f.Z_AXIS, null).scale((float)dz);
            Particle blockParticle = new TerrainParticle(level, x + positionVec.x, y, z + positionVec.z, 0.0, 0.0, 0.0, bs, bp);
            blockParticle.m_172260_((moveVec.x + (Math.random() - 0.5)) * 0.3, Math.random() * 0.5, (moveVec.z + (Math.random() - 0.5)) * 0.3);
            blockParticle.m_107257_(60 + new Random().nextInt(20));
            Particle smokeParticle = mc.f_91061_.m_107370_(ParticleTypes.f_123777_, x + positionVec.x * 0.5, y + 1.5, z + positionVec.z * 0.5, 0.0, 0.0, 0.0);
            smokeParticle.m_172260_(moveVec.x * 0.1, Math.random() * 0.05, moveVec.z * 0.1);
            smokeParticle.m_6569_(3.0F);
            smokeParticle.m_107271_(0.33F);
            mc.f_91061_.m_107344_(blockParticle);
            mc.f_91061_.m_107344_(smokeParticle);
         }
      }
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      public Particle createParticle(SimpleParticleType typeIn, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
         BlockPos blockpos = new MutableBlockPos(x, y, z);
         BlockState blockstate = level.m_8055_(blockpos);
         return blockstate == null ? null : new GroundSlamParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, blockpos, blockstate);
      }
   }
}
