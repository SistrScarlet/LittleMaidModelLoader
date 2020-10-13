package net.sistr.lmml.util;

import net.minecraft.item.Item;
import net.minecraftforge.common.Tags;

import java.util.Optional;

public class ConvertColor {

    public static Optional<TextureColor> convertDyeColor(Item item) {
        if (Tags.Items.DYES_WHITE.contains(item)) return Optional.of(TextureColor.WHITE);
        if (Tags.Items.DYES_ORANGE.contains(item)) return Optional.of(TextureColor.ORANGE);
        if (Tags.Items.DYES_MAGENTA.contains(item)) return Optional.of(TextureColor.MAGENTA);
        if (Tags.Items.DYES_LIGHT_BLUE.contains(item)) return Optional.of(TextureColor.LIGHT_BLUE);
        if (Tags.Items.DYES_YELLOW.contains(item)) return Optional.of(TextureColor.YELLOW);
        if (Tags.Items.DYES_LIME.contains(item)) return Optional.of(TextureColor.LIME);
        if (Tags.Items.DYES_PINK.contains(item)) return Optional.of(TextureColor.PINK);
        if (Tags.Items.DYES_GRAY.contains(item)) return Optional.of(TextureColor.GRAY);
        if (Tags.Items.DYES_LIGHT_GRAY.contains(item)) return Optional.of(TextureColor.LIGHT_GRAY);
        if (Tags.Items.DYES_CYAN.contains(item)) return Optional.of(TextureColor.CYAN);
        if (Tags.Items.DYES_PURPLE.contains(item)) return Optional.of(TextureColor.PURPLE);
        if (Tags.Items.DYES_BLUE.contains(item)) return Optional.of(TextureColor.BLUE);
        if (Tags.Items.DYES_BROWN.contains(item)) return Optional.of(TextureColor.BROWN);
        if (Tags.Items.DYES_GREEN.contains(item)) return Optional.of(TextureColor.GREEN);
        if (Tags.Items.DYES_RED.contains(item)) return Optional.of(TextureColor.RED);
        if (Tags.Items.DYES_BLACK.contains(item)) return Optional.of(TextureColor.BLACK);
        return Optional.empty();
    }

}
