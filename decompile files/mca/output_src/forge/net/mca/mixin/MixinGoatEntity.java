package forge.net.mca.mixin;

import forge.net.mca.advancement.criterion.CriterionMCA;
import forge.net.mca.item.ItemsMCA;
import forge.net.mca.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements.Type;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Goat.class)
public abstract class MixinGoatEntity extends Animal {
   protected MixinGoatEntity(EntityType<? extends Animal> entityType, Level world) {
      super(entityType, world);
   }

   @Inject(method = "m_149403_()Lnet/minecraft/sounds/SoundEvent;", at = @At("HEAD"))
   protected void getMilkingSound(CallbackInfoReturnable<SoundEvent> cir) {
      if (!this.m_9236_().f_46443_ && this.m_9236_().m_46471_()) {
         long time = this.m_9236_().m_46468_() % 24000L;
         BlockPos pos = this.m_20183_();
         if (time > 16000L
            && time < 20000L
            && ((Biome)this.m_9236_().m_204166_(pos).m_203334_()).m_198904_(pos)
            && NaturalSpawner.m_47051_(Type.ON_GROUND, this.m_9236_(), pos, EntityType.f_20497_)) {
            WitherSkeleton ancientCultist = (WitherSkeleton)EntityType.f_20497_.m_20615_(this.m_9236_());
            if (ancientCultist != null) {
               ancientCultist.m_6034_(pos.m_123341_(), pos.m_123342_(), pos.m_123343_());
               WorldUtils.spawnEntity(this.m_9236_(), ancientCultist, MobSpawnType.EVENT);
               ancientCultist.m_8061_(EquipmentSlot.HEAD, new ItemStack(Items.f_42476_));
               ancientCultist.m_8061_(EquipmentSlot.CHEST, new ItemStack(Items.f_42477_));
               ancientCultist.m_8061_(EquipmentSlot.LEGS, new ItemStack(Items.f_42478_));
               ancientCultist.m_8061_(EquipmentSlot.FEET, new ItemStack(Items.f_42479_));
               ancientCultist.m_8061_(EquipmentSlot.MAINHAND, new ItemStack(Items.f_42430_));
               ancientCultist.m_8061_(EquipmentSlot.OFFHAND, new ItemStack((ItemLike)ItemsMCA.BOOK_CULT_ANCIENT.get()));
               ancientCultist.m_21409_(EquipmentSlot.OFFHAND, 1.0F);
               ancientCultist.m_6593_(Component.m_237115_("entity.mca.ancient_cultist"));
               ((ServerLevel)this.m_9236_())
                  .m_6907_()
                  .stream()
                  .filter(p -> p.m_20270_(this) < 30.0F)
                  .forEach(p -> CriterionMCA.GENERIC_EVENT_CRITERION.trigger(p, "ancient_cultists"));
               this.m_6074_();
               this.m_9236_().m_6580_(10);
               LightningBolt bolt = (LightningBolt)EntityType.f_20465_.m_20615_(this.m_9236_());
               if (bolt != null) {
                  bolt.m_20874_(true);
                  bolt.m_20248_(pos.m_123341_() + 0.5F, pos.m_123342_(), pos.m_123343_() + 0.5F);
                  this.m_9236_().m_7967_(bolt);
               }
            }
         }
      }
   }
}
