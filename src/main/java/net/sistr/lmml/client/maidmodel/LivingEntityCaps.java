package net.sistr.lmml.client.maidmodel;

import net.minecraft.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;


public class LivingEntityCaps extends EntityCaps {
    protected LivingEntity owner;
    private static final Map<String, Integer> caps = new HashMap<>();

    public LivingEntityCaps(LivingEntity owner) {
        super(owner);
    }

    static {
        caps.putAll(EntityCaps.getStaticModelCaps());
    }

}
