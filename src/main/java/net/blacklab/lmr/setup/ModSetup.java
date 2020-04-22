package net.blacklab.lmr.setup;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ModSetup {

    public static final ItemGroup ITEM_GROUP = new ItemGroup("lmreengaged") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Items.CAKE);
        }
    };

}
