package net.blacklab.lmr.entity.maidmodel;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entityのデータ読み取り用のクラス
 * 別にEntityにインターフェース付けてもOK
 */
public class EntityCaps implements IModelCaps {
    protected Entity owner;
    private static final Map<String, Integer> caps = new HashMap<>();

    static {
        caps.put("Entity", caps_Entity);
        caps.put("health", caps_health);
        caps.put("healthFloat", caps_healthFloat);
        caps.put("ticksExisted", caps_ticksExisted);
        caps.put("heldItems", caps_heldItems);
        caps.put("currentEquippedItem", caps_currentEquippedItem);
        caps.put("currentArmor", caps_currentArmor);
        caps.put("onGround", caps_onGround);
        caps.put("isRiding", caps_isRiding);
        caps.put("isChild", caps_isChild);
        caps.put("isWet", caps_isWet);
        caps.put("isDead", caps_isDead);
        caps.put("isJumping", caps_isJumping);
        caps.put("isInWeb", caps_isInWeb);
        caps.put("isSwingInProgress", caps_isSwingInProgress);
        caps.put("isSneak", caps_isSneak);
        caps.put("isBlocking", caps_isBlocking);
        caps.put("isBurning", caps_isBurning);
        caps.put("isInWater", caps_isInWater);
        caps.put("isInvisible", caps_isInvisible);
        caps.put("isSprinting", caps_isSprinting);
        caps.put("PosBlockID", caps_PosBlockID);
        caps.put("PosBlockState", caps_PosBlockState);
        caps.put("PosBlockAir", caps_PosBlockAir);
        caps.put("PosBlockLight", caps_PosBlockLight);
        caps.put("PosBlockPower", caps_PosBlockPower);
        caps.put("isRidingPlayer", caps_isRidingPlayer);
        caps.put("posX", caps_posX);
        caps.put("posY", caps_posY);
        caps.put("posZ", caps_posZ);
        caps.put("pos", caps_pos);
        caps.put("motionX", caps_motionX);
        caps.put("motionY", caps_motionY);
        caps.put("motionZ", caps_motionZ);
        caps.put("motion", caps_pos);
        caps.put("WorldTotalTime", caps_WorldTotalTime);
        caps.put("WorldTime", caps_WorldTime);
        caps.put("MoonPhase", caps_MoonPhase);
        caps.put("rotationYaw", caps_rotationYaw);
        caps.put("rotationPitch", caps_rotationPitch);
        caps.put("prevRotationYaw", caps_prevRotationYaw);
        caps.put("prevRotationPitch", caps_prevRotationPitch);
        caps.put("renderYawOffset", caps_renderYawOffset);
        caps.put("TextureEntity", caps_TextureEntity);

    }

    public EntityCaps(Entity pOwner) {
        owner = pOwner;
    }

    public static Map<String, Integer> getStaticModelCaps() {
        return caps;
    }

    @Override
    public Map<String, Integer> getModelCaps() {
        return caps;
    }

    @Override
    public Object getCapsValue(int pIndex, Object... pArg) {
        switch (pIndex) {
            case caps_Entity:
                return owner;
            case caps_ticksExisted:
                return owner.ticksExisted;
            case caps_currentArmor:
                ItemStack aromor = ((List<ItemStack>) owner.getArmorInventoryList()).get((Integer) pArg[0]);
                if (aromor.isEmpty()) aromor = null;
                return aromor;
            case caps_posX:
                return owner.getPosX();
            case caps_posY:
                return owner.getPosY();
            case caps_posZ:
                return owner.getPosZ();
            case caps_pos:
                if (pArg == null) {
                    return new Double[]{owner.getPosX(), owner.getPosY(), owner.getPosZ()};
                }
                return (Integer) pArg[0] == 0 ? owner.getPosX() : (Integer) pArg[0] == 1 ? owner.getPosY() : owner.getPosZ();
            case caps_motionX:
                return owner.getMotion().getX();
            case caps_motionY:
                return owner.getMotion().getY();
            case caps_motionZ:
                return owner.getMotion().getZ();
            case caps_motion:
                Vec3d vec = owner.getMotion();
                if (pArg == null) {
                    return new Double[]{vec.getX(), vec.getY(), vec.getZ()};
                }
                return (Integer) pArg[0] == 0 ? vec.getX() : (Integer) pArg[0] == 1 ? vec.getY() : vec.getZ();

            case caps_rotationYaw:
                return owner.rotationYaw;
            case caps_rotationPitch:
                return owner.rotationPitch;
            case caps_prevRotationYaw:
                return owner.prevRotationYaw;
            case caps_prevRotationPitch:
                return owner.prevRotationPitch;
            case caps_renderYawOffset:
                return owner.getYOffset();
            case caps_onGround:
                return owner.onGround;
            case caps_isRiding:
                return owner.isPassenger();
            case caps_isRidingPlayer:
                return owner.getRidingEntity() instanceof PlayerEntity;
            case caps_isWet:
                return owner.isWet();
            case caps_isDead:
                return !owner.isAlive();
            case caps_isSneak:
                return owner.isSneaking();
            case caps_isBurning:
                return owner.isBurning();
            case caps_isInWater:
                return owner.isInWater();
            case caps_isInvisible:
                return owner.isInvisible();
            case caps_isSprinting:
                return owner.isSprinting();
            case caps_PosBlockID:
                return owner.getEntityWorld().getBlockState(new BlockPos(
                        MathHelper.floor(owner.getPosX() + (Double) pArg[0]),
                        MathHelper.floor(owner.getPosY() + (Double) pArg[1]),
                        MathHelper.floor(owner.getPosZ() + (Double) pArg[2]))).getBlock();
            case caps_PosBlockState:
                return owner.getEntityWorld().getBlockState(new BlockPos(
                        MathHelper.floor(owner.getPosX() + (Double) pArg[0]),
                        MathHelper.floor(owner.getPosY() + (Double) pArg[1]),
                        MathHelper.floor(owner.getPosZ() + (Double) pArg[2])));
            case caps_PosBlockAir:
                BlockPos pos = new BlockPos(
                        MathHelper.floor(owner.getPosX() + (Double) pArg[0]),
                        MathHelper.floor(owner.getPosY() + (Double) pArg[1]),
                        MathHelper.floor(owner.getPosZ() + (Double) pArg[2]));
                BlockState state = owner.getEntityWorld().getBlockState(pos);
                //移動可能ブロックかつ通常ブロックではない
                //Block.causesSuffocationから変更
                return !(state.getMaterial().blocksMovement() && !state.isNormalCube(owner.world, pos));

            case caps_PosBlockLight:
                return owner.getEntityWorld().getLight(new BlockPos(
                        MathHelper.floor(owner.getPosX() + (Double) pArg[0]),
                        MathHelper.floor(owner.getPosY() + (Double) pArg[1]),
                        MathHelper.floor(owner.getPosZ() + (Double) pArg[2])));
            case caps_PosBlockPower:
                return owner.getEntityWorld().getStrongPower(new BlockPos(
                        MathHelper.floor(owner.getPosX() + (Double) pArg[0]),
                        MathHelper.floor(owner.getPosY() + (Double) pArg[1]),
                        MathHelper.floor(owner.getPosZ() + (Double) pArg[2])));
            case caps_boundingBox:
                if (pArg == null) {
                    return owner.getBoundingBox();
                }
                switch ((Integer) pArg[0]) {
                    case 0:
                        return owner.getBoundingBox().maxX;
                    case 1:
                        return owner.getBoundingBox().maxY;
                    case 2:
                        return owner.getBoundingBox().maxZ;
                    case 3:
                        return owner.getBoundingBox().minX;
                    case 4:
                        return owner.getBoundingBox().minY;
                    case 5:
                        return owner.getBoundingBox().minZ;
                }
            case caps_getRidingName:
                return owner.getRidingEntity() == null ? "" : EntityType.getKey(owner.getRidingEntity().getType()).toString();

            // World
            case caps_WorldTotalTime:
                return owner.getEntityWorld().getWorldInfo().getGameTime();
            case caps_WorldTime:
                return owner.getEntityWorld().getWorldInfo().getDayTime();
            case caps_MoonPhase:
                return owner.getEntityWorld().getMoonPhase();
        }

        return null;
    }

    @Override
    public boolean setCapsValue(int pIndex, Object... pArg) {
        switch (pIndex) {
            case caps_ticksExisted:
                owner.ticksExisted = (Integer) pArg[0];
                return true;
            case caps_posX:
                owner.setRawPosition((Double) pArg[0], owner.getPosY(), owner.getPosZ());
                return true;
            case caps_posY:
                owner.setRawPosition(owner.getPosX(), (Double) pArg[0], owner.getPosZ());
                return true;
            case caps_posZ:
                owner.setRawPosition(owner.getPosX(), owner.getPosY(), (Double) pArg[0]);
                return true;
            case caps_pos:
                owner.setPosition((Double) pArg[0], (Double) pArg[1], (Double) pArg[2]);
                return true;
            case caps_motionX:
                owner.setMotion(owner.getMotion().mul(0, 1, 1).add((Double) pArg[0], 0, 0));
                return true;
            case caps_motionY:
                owner.setMotion(owner.getMotion().mul(1, 0, 1).add(0, (Double) pArg[0], 0));
                return true;
            case caps_motionZ:
                owner.setMotion(owner.getMotion().mul(1, 1, 0).add(0, 0, (Double) pArg[0]));
                return true;
            case caps_motion:
                owner.setVelocity((Double) pArg[0], (Double) pArg[1], (Double) pArg[2]);
                return true;
            case caps_onGround:
                owner.onGround = (Boolean) pArg[0];
                return true;
            case caps_isSneak:
                owner.setSneaking((Boolean) pArg[0]);
        }

        return false;
    }

}
