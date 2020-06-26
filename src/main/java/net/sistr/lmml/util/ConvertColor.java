package net.sistr.lmml.util;

import net.minecraft.item.Item;
import net.minecraftforge.common.Tags;

public class ConvertColor {

    public static byte convertDyeColor(Item item) {
        if (Tags.Items.DYES_WHITE.func_230235_a_(item)) return (byte) 0;
        if (Tags.Items.DYES_ORANGE.func_230235_a_(item)) return (byte) 1;
        if (Tags.Items.DYES_MAGENTA.func_230235_a_(item)) return (byte) 2;
        if (Tags.Items.DYES_LIGHT_BLUE.func_230235_a_(item)) return (byte) 3;
        if (Tags.Items.DYES_YELLOW.func_230235_a_(item)) return (byte) 4;
        if (Tags.Items.DYES_LIME.func_230235_a_(item)) return (byte) 5;
        if (Tags.Items.DYES_PINK.func_230235_a_(item)) return (byte) 6;
        if (Tags.Items.DYES_GRAY.func_230235_a_(item)) return (byte) 7;
        if (Tags.Items.DYES_LIGHT_GRAY.func_230235_a_(item)) return (byte) 8;
        if (Tags.Items.DYES_CYAN.func_230235_a_(item)) return (byte) 9;
        if (Tags.Items.DYES_PURPLE.func_230235_a_(item)) return (byte) 10;
        if (Tags.Items.DYES_BLUE.func_230235_a_(item)) return (byte) 11;
        if (Tags.Items.DYES_BROWN.func_230235_a_(item)) return (byte) 12;
        if (Tags.Items.DYES_GREEN.func_230235_a_(item)) return (byte) 13;
        if (Tags.Items.DYES_RED.func_230235_a_(item)) return (byte) 14;
        if (Tags.Items.DYES_BLACK.func_230235_a_(item)) return (byte) 15;
        return -1;
    }

}
