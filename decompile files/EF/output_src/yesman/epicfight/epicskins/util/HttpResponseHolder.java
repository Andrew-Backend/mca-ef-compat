package yesman.epicfight.epicskins.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record HttpResponseHolder(int statusCode, String body, Throwable exception) {
}
