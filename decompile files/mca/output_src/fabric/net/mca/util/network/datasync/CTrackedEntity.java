package fabric.net.mca.util.network.datasync;

import net.minecraft.class_1297;

public interface CTrackedEntity<T extends class_1297> {
   CDataManager<T> getTypeDataManager();

   default <P, TrackedP> void setTrackedValue(CParameter<P, TrackedP> key, P value) {
      this.getTypeDataManager().set((T)this, key, value);
   }

   default <P, TrackedP> P getTrackedValue(CParameter<P, TrackedP> key) {
      return this.getTypeDataManager().get((T)this, key);
   }
}
