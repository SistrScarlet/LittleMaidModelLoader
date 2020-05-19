package net.sistr.lmml.client;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.sistr.lmml.container.ModelSelectGUIContainer;

public class ModelSelectGUIScreen extends ContainerScreen<ModelSelectGUIContainer> {

    public ModelSelectGUIScreen(ModelSelectGUIContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

    }
}
