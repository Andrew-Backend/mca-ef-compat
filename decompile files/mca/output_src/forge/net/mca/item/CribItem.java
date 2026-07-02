package forge.net.mca.item;

import forge.net.mca.entity.CribEntity;
import forge.net.mca.entity.CribWoodType;
import forge.net.mca.entity.EntitiesMCA;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class CribItem extends Item {
   private final CribWoodType wood;
   private final DyeColor color;

   public CribItem(Properties settings, CribWoodType wood, DyeColor color) {
      super(settings);
      this.wood = wood;
      this.color = color;
   }

   public CribWoodType getWood() {
      return this.wood;
   }

   public DyeColor getColor() {
      return this.color;
   }

   public InteractionResult m_6225_(UseOnContext context) {
      Direction direction = context.m_43719_();
      if (direction == Direction.DOWN) {
         return InteractionResult.FAIL;
      }

      Level world = context.m_43725_();
      BlockPlaceContext itemPlacementContext = new BlockPlaceContext(context);
      BlockPos blockPos = itemPlacementContext.m_8083_();
      ItemStack itemStack = context.m_43722_();
      Vec3 vec3d = Vec3.m_82539_(blockPos);
      AABB box = ((EntityType)EntitiesMCA.CRIB.get()).m_20680_().m_20384_(vec3d.m_7096_(), vec3d.m_7098_(), vec3d.m_7094_());
      if (world.m_45756_(null, box) && world.m_45933_(null, box).isEmpty()) {
         if (world instanceof ServerLevel serverWorld) {
            CribEntity crib = (CribEntity)((EntityType)EntitiesMCA.CRIB.get()).m_262451_(serverWorld, null, null, blockPos, MobSpawnType.SPAWN_EGG, true, true);
            crib.setWoodType(this.wood);
            crib.setColor(this.color);
            float f = Mth.m_14143_((Mth.m_14177_(context.m_7074_() - 180.0F) + 22.5F) / 45.0F) * 45.0F;
            crib.m_7678_(crib.m_20185_(), crib.m_20186_(), crib.m_20189_(), f, 0.0F);
            serverWorld.m_47205_(crib);
            world.m_6263_(null, crib.m_20185_(), crib.m_20186_(), crib.m_20189_(), SoundEvents.f_11684_, SoundSource.BLOCKS, 0.75F, 0.8F);
            crib.m_146852_(GameEvent.f_157810_, context.m_43723_());
         }

         itemStack.m_41774_(1);
         return InteractionResult.m_19078_(world.f_46443_);
      } else {
         return InteractionResult.FAIL;
      }
   }
}
