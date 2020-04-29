package net.blacklab.lmr.entity.littlemaid;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.world.World;

//互換性のために用意
//ローダー部分が完成したあと、メイドさんを作るにしても、ここを直接は使わないかも
public class EntityLittleMaid extends Entity {
    public String maidMode = "";

    public EntityLittleMaid(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Override
    protected void registerData() {

    }

    @Override
    protected void readAdditional(CompoundNBT compound) {

    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {

    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return null;
    }
}
