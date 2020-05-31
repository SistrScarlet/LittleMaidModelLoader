package net.sistr.lmml.util;

import net.minecraft.item.Item;
import net.minecraftforge.common.Tags;

public class ConvertColor {

    public static byte convertDyeColor(Item item) {
        if (Tags.Items.DYES_WHITE.contains(item)) return (byte) 0;
        if (Tags.Items.DYES_ORANGE.contains(item)) return (byte) 1;
        if (Tags.Items.DYES_MAGENTA.contains(item)) return (byte) 2;
        if (Tags.Items.DYES_LIGHT_BLUE.contains(item)) return (byte) 3;
        if (Tags.Items.DYES_YELLOW.contains(item)) return (byte) 4;
        if (Tags.Items.DYES_LIME.contains(item)) return (byte) 5;
        if (Tags.Items.DYES_PINK.contains(item)) return (byte) 6;
        if (Tags.Items.DYES_GRAY.contains(item)) return (byte) 7;
        if (Tags.Items.DYES_LIGHT_GRAY.contains(item)) return (byte) 8;
        if (Tags.Items.DYES_CYAN.contains(item)) return (byte) 9;
        if (Tags.Items.DYES_PURPLE.contains(item)) return (byte) 10;
        if (Tags.Items.DYES_BLUE.contains(item)) return (byte) 11;
        if (Tags.Items.DYES_BROWN.contains(item)) return (byte) 12;
        if (Tags.Items.DYES_GREEN.contains(item)) return (byte) 13;
        if (Tags.Items.DYES_RED.contains(item)) return (byte) 14;
        if (Tags.Items.DYES_BLACK.contains(item)) return (byte) 15;
        return -1;
    }

}
