package mod.patrigan.slimierslimes.blocks.slimeblock;

import mod.patrigan.slimierslimes.SlimierSlimes;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlimeBlock;
import mod.patrigan.slimierslimes.util.ModBlockColor;
import mod.patrigan.slimierslimes.util.ModItemColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockDisplayReader;

import javax.annotation.Nullable;

import net.minecraft.block.AbstractBlock.Properties;

public class MagentaSlimeBlock extends SlimeBlock implements ModBlockColor, ModItemColor {

    public MagentaSlimeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void bounceUp(Entity entity) {
        if(SlimierSlimes.MAIN_CONFIG.allowSlimeBlockEffects.get()) {
            Vector3d vector3d = entity.getDeltaMovement();
            entity.setDeltaMovement(vector3d.x, 0, vector3d.z);
            effectEntity(entity);
        }else{
            super.bounceUp(entity);
        }
    }

    private void effectEntity(Entity entityIn) {
        if(entityIn instanceof LivingEntity){
            ((LivingEntity) entityIn).addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 20, 2));
        }
    }

    @Override
    public int getColor(BlockState blockState, @Nullable IBlockDisplayReader iBlockDisplayReader, @Nullable BlockPos blockPos, int tintIndex) {
        return getColor(tintIndex);
    }

    @Override
    public int getColor(ItemStack itemStack, int tintIndex) {
        return getColor(tintIndex);
    }

    public int getColor(int tintIndex) {
        return tintIndex == 0 ? DyeColor.MAGENTA.getColorValue() : -1;
    }
}
