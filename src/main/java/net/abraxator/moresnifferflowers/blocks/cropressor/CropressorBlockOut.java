package net.abraxator.moresnifferflowers.blocks.cropressor;

import net.abraxator.moresnifferflowers.blockentities.CropressorBlockEntity;
import net.abraxator.moresnifferflowers.blocks.ModEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CropressorBlockOut extends CropressorBlockBase implements ModEntityBlock {
    public CropressorBlockOut(Properties pProperties, Part part) {
        super(pProperties, part);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return tickerHelper(pLevel);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if(level.getBlockEntity(pos) instanceof CropressorBlockEntity entity && !newState.is(this)) {
            Containers.dropContents(level, pos, entity.container);
        }
        
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        ENTITY_POS = pPos;
        return new CropressorBlockEntity(pPos, pState);
    }
}
