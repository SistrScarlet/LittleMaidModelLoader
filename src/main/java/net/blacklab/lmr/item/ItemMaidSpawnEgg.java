package net.blacklab.lmr.item;

import net.blacklab.lmr.entity.littlemaid.EntityLittleMaid;
import net.blacklab.lmr.setup.ModSetup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

//不明 setHasSubtypes(true);
public class ItemMaidSpawnEgg extends Item {

    public ItemMaidSpawnEgg() {
        super(new Item.Properties()
                .group(ModSetup.ITEM_GROUP));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        ItemStack stack = context.getItem();
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        if (world.isRemote) {
            return ActionResultType.SUCCESS;
        }

        Entity entity = spawnMaid(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);

        if (entity != null) {
            if (entity instanceof LivingEntity && stack.hasDisplayName()) {
                entity.setCustomName(stack.getDisplayName());
            }

            PlayerEntity player = context.getPlayer();
            if (!player.isCreative()) {
                stack.shrink(1);
            }
        }

        return ActionResultType.SUCCESS;
    }

    public static Entity spawnMaid(World world, double x, double y, double z) {
        EntityLittleMaid spawnEntity = null;
        try {
            spawnEntity = new EntityLittleMaid(world);

            spawnEntity.setLocationAndAngles(x, y, z, (world.rand.nextFloat() * 360.0F) - 180.0F, 0.0F);
//			((LMM_EntityLittleMaid)spawnEntity).setTextureNames();
            spawnEntity.onSpawnWithEgg();
            world.addEntity(spawnEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return spawnEntity;
    }

}
