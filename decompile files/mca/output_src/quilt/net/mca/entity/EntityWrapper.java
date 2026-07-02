package quilt.net.mca.entity;

import net.minecraft.class_1308;

public interface EntityWrapper {
   default class_1308 asEntity() {
      return (class_1308)this;
   }
}
