package quilt.net.mca.entity.ai.relationship;

import quilt.net.mca.entity.EntityWrapper;

public interface CompassionateEntity<T extends EntityRelationship> extends EntityWrapper {
   T getRelationships();
}
