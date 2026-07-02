package forge.net.mca.util.network.datasync;

import net.minecraft.world.entity.Entity;

public interface CTrackedEntity<T extends Entity> {
   CDataManager<T> getTypeDataManager();

   default <P, TrackedP> void setTrackedValue(CParameter<P, TrackedP> key, P value) {
      this.getTypeDataManager().set((T)this, key, value);
   }

   default <P, TrackedP> P getTrackedValue(CParameter<P, TrackedP> key) {
      return this.getTypeDataManager().get((T)this, key);
   }
}
