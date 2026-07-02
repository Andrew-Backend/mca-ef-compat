package forge.net.mca.entity.ai;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

public class StatusEffectDangerSet {
   public static final Set<MobEffect> isDanger = new HashSet<>();

   static {
      isDanger.add(MobEffects.f_19597_);
      isDanger.add(MobEffects.f_19599_);
      isDanger.add(MobEffects.f_19602_);
      isDanger.add(MobEffects.f_19604_);
      isDanger.add(MobEffects.f_19610_);
      isDanger.add(MobEffects.f_19612_);
      isDanger.add(MobEffects.f_19613_);
      isDanger.add(MobEffects.f_19614_);
      isDanger.add(MobEffects.f_19615_);
      isDanger.add(MobEffects.f_19620_);
      isDanger.add(MobEffects.f_19590_);
      isDanger.add(MobEffects.f_19596_);
   }
}
