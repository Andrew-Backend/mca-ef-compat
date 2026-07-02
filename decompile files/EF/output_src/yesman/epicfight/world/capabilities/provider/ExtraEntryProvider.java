package yesman.epicfight.world.capabilities.provider;

import java.util.function.Function;
import net.minecraft.world.item.Item;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

@Deprecated(forRemoval = true, since = "26.1")
public interface ExtraEntryProvider {
   <T extends StaticAnimation> AnimationManager.AnimationAccessor<T> getExtraOrBuiltInAnimation(String var1);

   AssetAccessor<? extends SkinnedMesh> getExtraOrBuiltInMesh(String var1);

   AssetAccessor<? extends Armature> getExtraOrBuiltInArmature(String var1);

   Function<Item, CapabilityItem.Builder> getExtraOrBuiltInWeaponType(String var1);
}
