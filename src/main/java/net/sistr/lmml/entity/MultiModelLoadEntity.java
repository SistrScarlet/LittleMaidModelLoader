package net.sistr.lmml.entity;


import net.blacklab.lmr.entity.maidmodel.TextureBox;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.sistr.lmml.setup.Registration;
import net.sistr.lmml.util.manager.ModelManager;

public class MultiModelLoadEntity extends CreatureEntity {
    public TextureBox textureBox = ModelManager.instance.getTextureBox(ModelManager.instance.getRandomTextureString(this.rand));
    public int color = getRandomColor();

    private int getRandomColor() {
        int wildColor = textureBox.getRandomWildColor(this.rand);
        int contractColor = textureBox.getRandomContractColor(this.rand);
        return wildColor == -1 ? contractColor : wildColor;
    }

    public MultiModelLoadEntity(EntityType<MultiModelLoadEntity> type, World worldIn) {
        super(type, worldIn);
    }

    public MultiModelLoadEntity(World world) {
        super(Registration.MULTI_MODEL_LOAD_ENTITY.get(), world);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.addGoal(8, new LookRandomlyGoal(this));

    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23);
    }
}
