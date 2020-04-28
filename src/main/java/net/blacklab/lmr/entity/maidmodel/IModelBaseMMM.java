package net.blacklab.lmr.entity.maidmodel;


import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.LivingEntity;

public interface IModelBaseMMM extends IModelCaps {

	public void renderItems(LivingEntity pEntity, EntityRenderer<?> pRender);
	public void showArmorParts(int pParts);
	public void setEntityCaps(IModelCaps pModelCaps);
	public void setRender(EntityRenderer<?> pRender);
	public void setArmorRendering(boolean pFlag);

}
