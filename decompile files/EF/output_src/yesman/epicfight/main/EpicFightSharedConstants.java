package yesman.epicfight.main;

import java.util.function.Function;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.ServerAnimator;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class EpicFightSharedConstants {
   public static final int MAX_WEIGHTS = 3;
   public static final int MAX_JOINTS = 1000;
   public static final float A_TICK = 0.05F;
   public static final float GENERAL_ANIMATION_TRANSITION_TIME = 0.15F;
   public static final float EXECUTION_DAMAGE = 2.1474836E9F;
   public static final boolean IS_DEV_ENV = !FMLEnvironment.production;
   public static final String PUBLIC_SERVER_DOMAIN = "https://epic-fight.com";
   public static final String LOCAL_WEB_SERVER_DOMAIN = "http://127.0.0.1:8080";
   private static final Function<LivingEntityPatch<?>, Animator> ANIMATOR_PROVIDER = isPhysicalClient()
      ? ClientAnimator::getAnimator
      : ServerAnimator::getAnimator;

   public static Animator getAnimator(LivingEntityPatch<?> entitypatch) {
      return ANIMATOR_PROVIDER.apply(entitypatch);
   }

   public static boolean isPhysicalClient() {
      return FMLEnvironment.dist == Dist.CLIENT;
   }

   public static String webServerDomain() {
      return "https://epic-fight.com";
   }
}
