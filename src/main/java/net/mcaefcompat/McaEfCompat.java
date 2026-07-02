package net.mcaefcompat;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("mcaefcompat")
public class McaEfCompat {

    public static final String MOD_ID = "mcaefcompat";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public McaEfCompat() {
        LOGGER.info("[MCA-EF Compat] Loaded.");

        // Register directly in constructor — this is the standard Forge pattern.
        // FMLClientSetupEvent runs on a worker thread which can cause issues.
        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
            LOGGER.info("[MCA-EF Compat] ClientEventHandler registered.");
        }
    }
}
