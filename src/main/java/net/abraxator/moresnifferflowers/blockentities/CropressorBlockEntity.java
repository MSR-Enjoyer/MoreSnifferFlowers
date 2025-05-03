package net.abraxator.moresnifferflowers.blockentities;

import net.abraxator.moresnifferflowers.blocks.cropressor.CropressorBlockBase;
import net.abraxator.moresnifferflowers.init.*;
import net.abraxator.moresnifferflowers.recipes.CropressingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public class CropressorBlockEntity extends ModBlockEntity {
    public int[] cropCount = new int[5];
    public ItemStack currentCrop = ItemStack.EMPTY;
    public ItemStack result = ItemStack.EMPTY;
    public int progress = 0;
    public final int MAX_PROGRESS = 100;
    private static final int INV_SIZE = 16;
    private final RecipeManager.CachedCheck<Container, CropressingRecipe> quickCheck = RecipeManager.createCheck(ModRecipeTypes.CROPRESSING.get());
    private boolean sound = true;

    public CropressorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.CROPRESSOR.get(), pPos, pBlockState);
    }

    @Override
    public void tick(Level level) {
        if (cropCount.length < 5) cropCount = new int[5];
        suckInItems(level);

        for (Crop crop : Crop.values()) {
            if (cropCount[crop.ordinal()] >= 16) {
                if (result.isEmpty()) {
                    currentCrop = new ItemStack(crop.item);
                    currentCrop.setCount(16);
                }
                var recipeInput = new SimpleContainer(currentCrop);
                var cropressingRecipeOptional = quickCheck.getRecipeFor(recipeInput, level);

                if (result.isEmpty()) cropressingRecipeOptional.ifPresent(cropressingRecipe -> result = cropressingRecipe.result());
                if (Crop.fromCropressed(result.getItem()) == null) return;
                if (crop.item.equals((Objects.requireNonNull(Crop.fromCropressed(result.getItem()))).item)) progress++;

                if (sound) {
                    level.playSound(null, worldPosition, ModSoundEvents.CROPRESSOR_BELT.get(), SoundSource.BLOCKS, 1.0F, (float) (1.0F + (level.getRandom().nextFloat() * 0.2)));
                    sound = false;
                }

                if (progress % 10 == 0) {
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
                }

                if (progress >= MAX_PROGRESS) {
                    Vec3 blockPos = getBlockPos().relative(getBlockState().getValue(CropressorBlockBase.FACING).getOpposite()).getCenter();
                    ItemEntity entity = new ItemEntity(level, blockPos.x, blockPos.y + 0.5, blockPos.z, result);

                    currentCrop = ItemStack.EMPTY;
                    cropCount[crop.ordinal()] = 0;
                    result = ItemStack.EMPTY;
                    updateFullness(0, crop);

                    level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.4F, 1.0F);
                    level.addFreshEntity(entity);
                    level.gameEvent(GameEvent.BLOCK_CHANGE, getBlockPos(), GameEvent.Context.of(getBlockState()));
                    setChanged();
                    progress = 0;
                }
            }
        }
    }

    public boolean canInteract() {
        return progress <= 0;
    }

    public ItemStack addItem(ItemStack pStack) {
        Crop crop = Crop.fromItem(pStack.getItem());
        if (cropCount.length < 5) cropCount = new int[5];
        if (Crop.canAddCrop(cropCount, crop)) {
            int index = crop.ordinal();
            
            int currentCount = this.cropCount[index];
            int maxAddable = 16 - currentCount;
            int itemsToAdd = Math.min(pStack.getCount(), maxAddable);

            if (itemsToAdd > 0) {
                this.cropCount[index] += itemsToAdd;
                this.currentCrop = new ItemStack(crop.item, this.cropCount[index]);
                
                pStack.shrink(itemsToAdd);
                
                int fullness = Mth.ceil((float) this.cropCount[index] / 2);
                updateFullness(fullness, crop);
            }
        }

        return pStack;
    }

    public void suckInItems(Level level) {
        Direction direction = this.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        BlockPos pos = this.worldPosition.relative(direction).above();
        Container container = HopperBlockEntity.getContainerAt(level, pos);
        if (container != null) {
            Direction direction1 = Direction.DOWN;
            if (!isEmptyContainer(container, direction1)) {
                getSlots(container, direction1).anyMatch((p_59363_) -> tryTakeInItemFromSlot(container, p_59363_, direction1));
            }
        }
    }

    private static boolean isEmptyContainer(Container container, Direction direction) {
        return getSlots(container, direction).allMatch((p_59319_) -> {
            return container.getItem(p_59319_).isEmpty();
        });
    }

    private static IntStream getSlots(Container container, Direction direction) {
        return container instanceof WorldlyContainer ? IntStream.of(((WorldlyContainer)container).getSlotsForFace(direction)) : IntStream.range(0, container.getContainerSize());
    }

    private boolean tryTakeInItemFromSlot(Container container, int slot, Direction direction) {
        ItemStack itemstack = container.getItem(slot);
        if (!itemstack.isEmpty() && canTakeItemFromContainer(container, itemstack, slot, direction)) {
            ItemStack itemstack1 = itemstack.copy();
            ItemStack itemstack2 = addItem(itemstack1);
            if (itemstack2.isEmpty()) {
                container.setChanged();
            }
            container.setItem(slot, itemstack2);
            return true;
        }

        return false;
    }

    private boolean canTakeItemFromContainer(Container destination, ItemStack itemStack, int slot, Direction direction) {
        Crop crop = Crop.fromItem(itemStack.getItem());
        if (cropCount.length < 5) cropCount = new int[5];
        if (!Crop.canAddCrop(cropCount ,crop)) {
            return false;
        } else {
            if (destination instanceof WorldlyContainer) {
                WorldlyContainer worldlycontainer = (WorldlyContainer)destination;
                return worldlycontainer.canTakeItemThroughFace(slot, itemStack, direction);
            }

            return true;
        }
    }

    private void updateFullness(int fullness, Crop crop) {
        var pos = getBlockPos().relative(getBlockState().getValue(HorizontalDirectionalBlock.FACING));
        level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(ModStateProperties.FULLNESS, fullness));
        level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(ModStateProperties.FULLNESS, fullness).setValue(ModStateProperties.CROP, crop));
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putIntArray("crop_count", cropCount);
        pTag.put("content", currentCrop.save(new CompoundTag()));
        pTag.putInt("progress", progress);
        pTag.put("result", result.save(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        cropCount = pTag.getIntArray("crop_count");
        currentCrop = ItemStack.of(pTag.getCompound("content"));
        progress = pTag.getInt("progress");
        result = ItemStack.of(pTag.getCompound("result"));
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.put("result", result.save(new CompoundTag()));
        compoundtag.putInt("progress", progress);
        return compoundtag;
    }
    
    public enum Crop implements StringRepresentable {
        CARROT("carrot", Items.CARROT, 0xffa135),
        POTATO("potato", Items.POTATO, 0xb88c4c),
        WHEAT("wheat", Items.WHEAT, 0xfff35e),
        NETHERWART("netherwart", Items.NETHER_WART, 0x9e392b),
        BEETROOT("beetroot", Items.BEETROOT, 0xc36866);
        
        String name;
        public Item item;
        public int tint;

        Crop(String name, Item item, int tint) {
            this.name = name;
            this.item = item;
            this.tint = tint;
        }

        public static boolean canAddCrop(int[] cropCount, Crop crop) {
            return crop != null && getCount(cropCount, crop) < 16;
        }
        
        public static int getCount(int[] cropCount, Crop crop) {
            return cropCount[crop.ordinal()];
        }

        @Nullable
        public static Crop fromCropressed(Item item) {
            Map<Item, Item> map = new HashMap<>();
            map.put(ModItems.CROPRESSED_CARROT.get().asItem(), Items.CARROT.asItem());
            map.put(ModItems.CROPRESSED_POTATO.get().asItem(), Items.POTATO.asItem());
            map.put(ModItems.CROPRESSED_WHEAT.get().asItem(), Items.WHEAT.asItem());
            map.put(ModItems.CROPRESSED_NETHERWART.get().asItem(), Items.NETHER_WART.asItem());
            map.put(ModItems.CROPRESSED_BEETROOT.get().asItem(), Items.BEETROOT.asItem());
            return fromItem(map.get(item));
        }


        @Nullable
        public static Crop fromItem(Item item) {
            return Arrays.stream(values())
                    .filter(crops -> crops.item == item)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
