package net.sistr.lmml.client;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

public class CompatEntityRenderMath {

    public static float[] getLimbSwing(LivingEntity entity, float partialTicks) {
        float limbSwingAmount = 0.0F;
        float limbSwing = 0.0F;

        if (!entity.isBeingRidden()) {
            limbSwingAmount = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
            limbSwing = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);

            if (entity.isChild()) {
                limbSwing *= 3.0F;
            }

            if (limbSwingAmount > 1.0F) {
                limbSwingAmount = 1.0F;
            }
        }
        return new float[]{limbSwing, limbSwingAmount};
    }

    public static float getAgeInTicks(LivingEntity entity, float partialTicks) {
        return (float) entity.ticksExisted + partialTicks;
    }

    public static float[] getYawPitch(LivingEntity entity, float partialTicks) {
        float yawOffset = interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
        float yawHead = interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
        float headYaw = yawHead - yawOffset;

        boolean shouldSit = entity.isPassenger() && (entity.getRidingEntity() != null && entity.getRidingEntity().shouldRiderSit());
        if (shouldSit && entity.getRidingEntity() instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity.getRidingEntity();
            yawOffset = interpolateRotation(living.prevRenderYawOffset, living.renderYawOffset, partialTicks);
            headYaw = yawHead - yawOffset;
            float wrapHeadYaw = MathHelper.wrapDegrees(headYaw);

            if (wrapHeadYaw < -85.0F) {
                wrapHeadYaw = -85.0F;
            }

            if (wrapHeadYaw >= 85.0F) {
                wrapHeadYaw = 85.0F;
            }

            yawOffset = yawHead - wrapHeadYaw;

            if (wrapHeadYaw * wrapHeadYaw > 2500.0F) {
                yawOffset += wrapHeadYaw * 0.2F;
            }

            headYaw = yawHead - yawOffset;
        }

        float headPitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;

        if (!entity.isBeingRidden()) {
            headYaw = yawHead - yawOffset;
        }
        return new float[]{headYaw, headPitch};
    }

    public static float interpolateRotation(float prevYawOffset, float yawOffset, float partialTicks) {
        float f = yawOffset - prevYawOffset;

        while (f < -180.0F) {
            f += 360.0F;
        }

        while (f >= 180.0F) {
            f -= 360.0F;
        }

        return prevYawOffset + partialTicks * f;
    }

}
