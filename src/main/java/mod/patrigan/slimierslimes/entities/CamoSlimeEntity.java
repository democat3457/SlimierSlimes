package mod.patrigan.slimierslimes.entities;

import mod.patrigan.slimierslimes.entities.ai.goal.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

public class CamoSlimeEntity extends AbstractSlimeEntity {
    private static final int FLEE_TIME = 120;

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new FleeGoal(this, FLEE_TIME, () -> !this.isHidden(), this::hide));
        this.goalSelector.addGoal(3, new AttackGoal(this));
        this.goalSelector.addGoal(4, new FaceRandomGoal(this));
        this.goalSelector.addGoal(5, new HopGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, 10, true, false,
                livingEntity -> Math.abs(livingEntity.getPosY() - this.getPosY()) <= 4.0D));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolemEntity.class, true));
    }

    public CamoSlimeEntity(EntityType<? extends AbstractSlimeEntity> type, World worldIn) {
        super(type, worldIn);
    }


    @Override
    protected void registerData() {
        super.registerData();
        this.setInvisible(true);
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putBoolean("hidden", this.isHidden());
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        if (compound.getBoolean("ignited")) {
            this.hide();
        }
    }

    public boolean isHidden() {
        return isInvisible();
    }

    public void hide() {
        this.setInvisible(true);
    }

    @Override
    protected void dealDamage(LivingEntity entityIn) {
        super.dealDamage(entityIn);
        this.setInvisible(false);
    }
}
