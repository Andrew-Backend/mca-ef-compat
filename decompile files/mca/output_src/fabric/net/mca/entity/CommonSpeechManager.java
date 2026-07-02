package fabric.net.mca.entity;

import fabric.net.mca.util.LimitedLinkedHashMap;
import net.minecraft.class_7417;

public class CommonSpeechManager {
   public static final CommonSpeechManager INSTANCE = new CommonSpeechManager();
   public String lastResolvedKey;
   public final LimitedLinkedHashMap<class_7417, String> translations = new LimitedLinkedHashMap<>(100);
}
