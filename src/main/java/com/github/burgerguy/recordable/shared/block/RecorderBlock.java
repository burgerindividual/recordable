package com.github.burgerguy.recordable.shared.block;

import com.github.burgerguy.recordable.server.score.record.BlockEntityScoreRecorder;
import com.github.burgerguy.recordable.shared.Recordable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

public class RecorderBlock extends BaseEntityBlock {
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(Recordable.MOD_ID, "recorder");
    public static final Block INSTANCE = new RecorderBlock(QuiltBlockSettings.of(Material.METAL).strength(4.0f));
    public static final Item ITEM_INSTANCE = new BlockItem(INSTANCE, new QuiltItemSettings().group(CreativeModeTab.TAB_MISC));

    private static final VoxelShape AABB_SOUTH = Shapes.or(
            Shapes.box(0, 0, 0, 1, 0.375, 0.9375),
            Shapes.box(0, 0.375, 0.0625, 1, 1, 0.5),
            Shapes.box(0.125, 0.375, 0.5, 0.75, 0.75, 0.9375)
    );
    private static final VoxelShape AABB_WEST = Shapes.or(
            Shapes.box(0.0625, 0, 0, 1, 0.375, 1),
            Shapes.box(0.5, 0.375, 0, 0.9375, 1, 1),
            Shapes.box(0.0625, 0.375, 0.125, 0.5, 0.75, 0.75)
    );
    private static final VoxelShape AABB_NORTH = Shapes.or(
            Shapes.box(0, 0, 0.0625, 1, 0.375, 1),
            Shapes.box(0, 0.375, 0.5, 1, 1, 0.9375),
            Shapes.box(0.25, 0.375, 0.0625, 0.875, 0.75, 0.5)
    );
    private static final VoxelShape AABB_EAST = Shapes.or(
            Shapes.box(0, 0, 0, 0.9375, 0.375, 1),
            Shapes.box(0.0625, 0.375, 0, 0.5, 1, 1),
            Shapes.box(0.25, 0.375, 0.0625, 0.875, 0.75, 0.5)
    );

    public RecorderBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RecorderBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof RecorderBlockEntity recorderBlockEntity) {
            if (!level.isClientSide) {
                BlockEntityScoreRecorder scoreRecorder = recorderBlockEntity.getScoreRecorder();
                if (scoreRecorder == null) return InteractionResult.FAIL;
                if (scoreRecorder.isRecording()) {
                    scoreRecorder.stop();
                    recorderBlockEntity.setRecordItem(null);
                    return InteractionResult.SUCCESS;
                }
            }

            boolean hadRecord = recorderBlockEntity.hasRecord();
            if (hadRecord && !level.isClientSide) {
                recorderBlockEntity.dropRecord();
            }

            recorderBlockEntity.setRecordItem(null);
            return hadRecord ? InteractionResult.CONSUME : InteractionResult.PASS;
        } else {
            return super.use(state, level, pos, player, hand, hit);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (level.getBlockEntity(pos) instanceof RecorderBlockEntity recorderBlockEntity) {
            if (!level.isClientSide && recorderBlockEntity.hasRecord()) {
                recorderBlockEntity.dropRecord();
            }
            recorderBlockEntity.setRecordItem(null);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        return switch (direction) {
            case NORTH -> AABB_NORTH;
            case SOUTH -> AABB_SOUTH;
            case WEST -> AABB_WEST;
            case EAST -> AABB_EAST;
            default -> throw new IllegalStateException("Unexpected direction: " + direction);
        };
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(BlockStateProperties.HORIZONTAL_FACING, rotation.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }
}
