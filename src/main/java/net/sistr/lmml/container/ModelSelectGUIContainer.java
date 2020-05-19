package net.sistr.lmml.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.sistr.lmml.setup.Registration;

public class ModelSelectGUIContainer extends Container {

    public ModelSelectGUIContainer(int id) {
        super(Registration.MODEL_SELECT_GUI_CONTAINER.get(), id);
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return false;
    }
}
