package net.sistr.lmml.mixin;


import net.sistr.lmml.LittleMaidModelLoader;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

public class Connector implements IMixinConnector {

    @Override
    public void connect() {
        LittleMaidModelLoader.LOGGER.info("Invoking Mixin Connector");
        Mixins.addConfiguration("assets/littlemaidmodelloader/littlemaidmodelloader.mixins.json");
    }

}
