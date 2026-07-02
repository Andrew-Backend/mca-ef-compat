package fabric.net.mca.entity.ai.relationship;

import fabric.net.mca.entity.EntityWrapper;

public interface CompassionateEntity<T extends EntityRelationship> extends EntityWrapper {
   T getRelationships();
}
