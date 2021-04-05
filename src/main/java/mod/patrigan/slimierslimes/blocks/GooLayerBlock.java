package mod.patrigan.slimierslimes.blocks;

import mod.patrigan.slimierslimes.entities.AbstractSlimeEntity;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DyeColor;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class GooLayerBlock extends FallingBlock {
    public static final Material GOO = (new Material.Builder(MaterialColor.NONE)).noCollider().build();
    public static final IntegerProperty LAYERS = BlockStateProperties.LAYERS;
    protected static final VoxelShape[] SHAPE_BY_LAYER = new VoxelShape[]{VoxelShapes.empty(), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D)};

    public GooLayerBlock(DyeColor color) {
        super(AbstractBlock.Properties.of(GOO, color).friction(0.8F).sound(SoundType.SLIME_BLOCK).noOcclusion().randomTicks().noCollission().dynamicShape());
        this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, 1));
    }

    @Override
    public boolean isPathfindable(BlockState p_196266_1_, IBlockReader p_196266_2_, BlockPos p_196266_3_, PathType p_196266_4_) {
        switch(p_196266_4_) {
            case LAND:
                return p_196266_1_.getValue(LAYERS) < 5;
            case WATER:
                return false;
            case AIR:
                return false;
            default:
                return false;
        }
    }

    @Override
    public VoxelShape getShape(BlockState blockState, IBlockReader blockReader, BlockPos blockPos, ISelectionContext selectionContext) {
        return SHAPE_BY_LAYER[blockState.getValue(LAYERS)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, IBlockReader blockReader, BlockPos blockPos, ISelectionContext selectionContext) {
        if(blockState.getValue(LAYERS) == 8 && selectionContext.getEntity() instanceof FallingBlockEntity){
            return VoxelShapes.block();
        }
        return VoxelShapes.empty();
        //return SHAPE_BY_LAYER[blockState.getValue(LAYERS) - 1];
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState blockState, IBlockReader blockReader, BlockPos blockPos) {
        return SHAPE_BY_LAYER[blockState.getValue(LAYERS)];
    }

    @Override
    public VoxelShape getVisualShape(BlockState blockState, IBlockReader blockReader, BlockPos blockPos, ISelectionContext selectionContext) {
        return SHAPE_BY_LAYER[blockState.getValue(LAYERS)];
    }

    @Override
    public void entityInside(BlockState blockState, World world, BlockPos blockPos, Entity entity) {
        if(entity instanceof FallingBlockEntity && entity.isAlive()){
            FallingBlockEntity fallingBlockEntity = (FallingBlockEntity) entity;
            if(fallingBlockEntity.getBlockState().is(blockState.getBlock()) && blockState.getValue(LAYERS) < 8){
                int totalLayers = blockState.getValue(LAYERS) + fallingBlockEntity.getBlockState().getValue(LAYERS);
                if(totalLayers > 8) {
                    world.setBlockAndUpdate(blockPos, blockState.setValue(LAYERS, 8));
                    world.setBlockAndUpdate(blockPos.above(), fallingBlockEntity.getBlockState().setValue(LAYERS, totalLayers - 8));
                }else{
                    world.setBlockAndUpdate(blockPos, blockState.setValue(LAYERS, totalLayers));
                }
                fallingBlockEntity.remove();
            }
        }
        if (entity instanceof LivingEntity && !(entity instanceof AbstractSlimeEntity)) {
            double modifier = 1.0-(0.1D*blockState.getValue(LAYERS));
            Vector3d stuckSpeedMultiplier = new Vector3d(modifier, 0.75D, modifier);
            if(entity.stuckSpeedMultiplier.equals(Vector3d.ZERO) || entity.stuckSpeedMultiplier.lengthSqr() > stuckSpeedMultiplier.lengthSqr()) {
                entity.makeStuckInBlock(blockState, stuckSpeedMultiplier);
            }

        }
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState blockState) {
        return true;
    }

    @Override
    public boolean canSurvive(BlockState blockState, IWorldReader world, BlockPos blockPos) {
        BlockState blockstateBelow = world.getBlockState(blockPos.below());
        if(blockstateBelow.getBlock() == this){
            return true;
        }
        if (!blockstateBelow.is(Blocks.HONEY_BLOCK) && !blockstateBelow.is(Blocks.SOUL_SAND) && !blockstateBelow.isAir(world, blockPos)) {
            return Block.isFaceFull(blockstateBelow.getCollisionShape(world, blockPos.below()), Direction.UP);
        } else {
            return true;
        }
    }

    public void onLand(World world, BlockPos blockPos, BlockState blockState, BlockState belowBlockState, FallingBlockEntity fallingBlockEntity) {
        if(belowBlockState.is(blockState.getBlock()) &&  belowBlockState.getValue(LAYERS) < 1){
            world.removeBlock(blockPos, true);
            world.setBlockAndUpdate(blockPos.below(), belowBlockState.setValue(LAYERS, belowBlockState.getValue(LAYERS) + 1));
        }
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState neighbourState, IWorld world, BlockPos blockPos, BlockPos p_196271_6_) {
        return !blockState.canSurvive(world, blockPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(blockState, direction, neighbourState, world, blockPos, p_196271_6_);
    }

    public void tick(BlockState blockState, ServerWorld world, BlockPos blockPos, Random random) {
        BlockState blockStateBelow = world.getBlockState(blockPos.below());
        if ((blockStateBelow.is(this) && blockStateBelow.getValue(LAYERS) < 8) || world.isEmptyBlock(blockPos.below()) || isFree(blockStateBelow) && blockPos.getY() >= 0) {
            FallingBlockEntity fallingblockentity = new FallingBlockEntity(world, (double)blockPos.getX() + 0.5D, (double)blockPos.getY(), (double)blockPos.getZ() + 0.5D, world.getBlockState(blockPos));
            this.falling(fallingblockentity);
            world.addFreshEntity(fallingblockentity);
        }
    }

    @Override
    public void randomTick(BlockState blockState, ServerWorld world, BlockPos blockPos, Random random) {
        if(blockState.getValue(LAYERS) > 1) {
            BlockPos.Mutable mutable = new BlockPos.Mutable();
            HashMap<BlockPos, BlockState> flowTargets = new HashMap<>();
            for(Direction direction : Direction.Plane.HORIZONTAL){
                mutable.setWithOffset(blockPos, direction);
                BlockState blockState1 = world.getBlockState(mutable);
                if(canFlowTo(blockState, blockState1, world, blockPos)){
                    flowTargets.put(mutable.immutable(), blockState1);
                }
            }
            if(flowTargets.size() <= 0){
                return;
            }
            BlockPos flowTargetPos = new ArrayList<>(flowTargets.keySet()).get(random.nextInt(flowTargets.size()));
            BlockState flowTarget = flowTargets.get(flowTargetPos);
            if(flowTarget.is(blockState.getBlock())){
                world.setBlockAndUpdate(blockPos, blockState.setValue(LAYERS, blockState.getValue(LAYERS) - 1));
                world.setBlockAndUpdate(flowTargetPos, flowTarget.setValue(LAYERS, flowTarget.getValue(LAYERS) + 1));
            }else{
                world.setBlockAndUpdate(blockPos, blockState.setValue(LAYERS, blockState.getValue(LAYERS) - 1));
                world.setBlockAndUpdate(flowTargetPos, blockState.getBlock().defaultBlockState());
            }
        }
    }

    private boolean canFlowTo(BlockState blockState, BlockState blockState1, ServerWorld world, BlockPos blockPos) {
        return (blockState1.is(blockState.getBlock()) && blockState.getValue(LAYERS) - blockState1.getValue(LAYERS) > 1)
                || blockState1.isAir(world, blockPos);
    }

    @Override
    public boolean canBeReplaced(BlockState blockState, BlockItemUseContext blockItemUseContext) {
        int i = blockState.getValue(LAYERS);
        if (blockItemUseContext.getItemInHand().getItem() == this.asItem() && i < 8) {
            if (blockItemUseContext.replacingClickedOnBlock()) {
                return blockItemUseContext.getClickedFace() == Direction.UP;
            } else {
                return true;
            }
        } else {
            return i == 1;
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext blockItemUseContext) {
        BlockState blockstate = blockItemUseContext.getLevel().getBlockState(blockItemUseContext.getClickedPos());
        if (blockstate.is(this)) {
            int i = blockstate.getValue(LAYERS);
            return blockstate.setValue(LAYERS, Integer.valueOf(Math.min(8, i + 1)));
        } else {
            return super.getStateForPlacement(blockItemUseContext);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> blockStateBuilder) {
        blockStateBuilder.add(LAYERS);
    }
}