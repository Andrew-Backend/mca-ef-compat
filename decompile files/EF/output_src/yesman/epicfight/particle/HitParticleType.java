package yesman.epicfight.particle;

import java.util.Random;
import java.util.function.BiFunction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class HitParticleType extends SimpleParticleType {
   public static final BiFunction<Entity, Entity, Vector3d> CENTER_OF_TARGET = (target, attacker) -> {
      EntityDimensions size = target.m_6972_(target.m_20089_());
      double x = target.m_20185_();
      double y = target.m_20186_() + size.f_20377_ * 0.5;
      double z = target.m_20189_();
      return new Vector3d(x, y, z);
   };
   public static final BiFunction<Entity, Entity, Vector3d> RANDOM_WITHIN_BOUNDING_BOX = (target, attacker) -> {
      EntityDimensions size = target.m_6972_(target.m_20089_());
      Random random = new Random();
      double x = target.m_20185_() + (random.nextDouble() - 0.5) * size.f_20377_;
      double y = target.m_20186_() + (random.nextDouble() + size.f_20378_) * 0.5;
      double z = target.m_20189_() + (random.nextDouble() - 0.5) * size.f_20377_;
      return new Vector3d(x, y, z);
   };
   public static final BiFunction<Entity, Entity, Vector3d> FRONT_OF_EYES = (target, attacker) -> {
      Vec3 eyePosition = target.m_146892_();
      Vec3 viewVec = target.m_20154_().m_82490_(2.0);
      return new Vector3d(eyePosition.f_82479_ + viewVec.f_82479_, eyePosition.f_82480_ + viewVec.f_82480_, eyePosition.f_82481_ + viewVec.f_82481_);
   };
   public static final BiFunction<Entity, Entity, Vector3d> MIDDLE_OF_ENTITIES = (target, attacker) -> {
      Vec3 targetPos = target.m_20182_().m_82520_(0.0, target.m_20206_() * 0.5F, 0.0);
      Vec3 attackerPos = attacker.m_20182_().m_82520_(0.0, target.m_20206_() * 0.5F, 0.0);
      Vec3 to = attackerPos.m_82546_(targetPos).m_82490_(0.5);
      return new Vector3d(targetPos.f_82479_ + to.f_82479_, targetPos.f_82480_ + to.f_82480_, targetPos.f_82481_ + to.f_82481_);
   };
   public static final BiFunction<Entity, Entity, Vector3d> ZERO = (target, attacker) -> new Vector3d(0.0, 0.0, 0.0);
   public static final BiFunction<Entity, Entity, Vector3d> ATTACKER_XY_ROTATION = (target, attacker) -> new Vector3d(
      attacker.m_146909_(), 180.0F - attacker.m_146908_(), -1.0
   );
   public static final BiFunction<Entity, Entity, Vector3d> ATTACKER_Y_ROTATION = (target, attacker) -> new Vector3d(90.0, attacker.m_146908_(), -1.0);
   public BiFunction<Entity, Entity, Vector3d> positionProvider;
   public BiFunction<Entity, Entity, Vector3d> argumentProvider;

   public HitParticleType(boolean pOverrideLimiter) {
      this(pOverrideLimiter, CENTER_OF_TARGET, ZERO);
   }

   public HitParticleType(
      boolean pOverrideLimiter, BiFunction<Entity, Entity, Vector3d> positionProvider, BiFunction<Entity, Entity, Vector3d> argumentProvider
   ) {
      super(pOverrideLimiter);
      this.positionProvider = positionProvider;
      this.argumentProvider = argumentProvider;
   }

   public void spawnParticleWithArgument(ServerLevel world, Entity e1, Entity e2) {
      this.spawnParticleWithArgument(world, null, null, e1, e2);
   }

   public void spawnParticleWithArgument(
      ServerLevel world, BiFunction<Entity, Entity, Vector3d> positionProvider, BiFunction<Entity, Entity, Vector3d> argumentProvider, Entity e1, Entity e2
   ) {
      Vector3d position = positionProvider == null ? this.positionProvider.apply(e1, e2) : positionProvider.apply(e1, e2);
      Vector3d arguments = argumentProvider == null ? this.argumentProvider.apply(e1, e2) : argumentProvider.apply(e1, e2);
      world.m_8767_(this, position.x, position.y, position.z, 0, arguments.x, arguments.y, arguments.z, 1.0);
   }

   public void spawnParticleWithArgument(ServerLevel world, double posX, double posY, double posZ, double argX, double argY, double argZ) {
      world.m_8767_(this, posX, posY, posZ, 0, argX, argY, argZ, 1.0);
   }
}
