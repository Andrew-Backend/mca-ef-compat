package forge.net.mca.entity.ai.relationship;

import forge.net.mca.entity.EntityWrapper;

public interface CompassionateEntity<T extends EntityRelationship> extends EntityWrapper {
   T getRelationships();
}
