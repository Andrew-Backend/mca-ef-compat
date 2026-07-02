package quilt.net.mca.entity;

import net.minecraft.class_7417;
import quilt.net.mca.util.LimitedLinkedHashMap;

public class CommonSpeechManager {
   public static final CommonSpeechManager INSTANCE = new CommonSpeechManager();
   public String lastResolvedKey;
   public final LimitedLinkedHashMap<class_7417, String> translations = new LimitedLinkedHashMap<>(100);
}
