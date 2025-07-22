package net.abraxator.moresnifferflowers.events;

import net.abraxator.moresnifferflowers.MoreSnifferFlowers;
import net.abraxator.moresnifferflowers.blockentities.BondripiaBlockEntity;
import net.abraxator.moresnifferflowers.blockentities.GiantCropBlockEntity;
import net.abraxator.moresnifferflowers.init.ModBlocks;
import net.abraxator.moresnifferflowers.init.ModItems;
import net.abraxator.moresnifferflowers.init.ModStateProperties;
import net.abraxator.moresnifferflowers.init.ModTags;
import net.abraxator.moresnifferflowers.items.JarOfBonmeelItem;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MoreSnifferFlowers.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity livingEntity = event.getEntity();
        Level level = livingEntity.level();
        Vec3 loc = livingEntity.position();
        BlockPos blockPos = BlockPos.containing(loc);
        
        if(level.getBlockState(blockPos).is(ModBlocks.CORRUPTED_SLIME_LAYER.get()) || level.getBlockState(blockPos.below()).is(ModBlocks.CORRUPTED_SLIME_LAYER.get())) {
            livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().multiply(1, 0.3, 1));
        }
    }

    @SubscribeEvent
    public static void onPlayerInteractRightClickItem(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack itemStack = event.getItemStack();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        var state = level.getBlockState(pos);
        var item = player.getItemInHand(hand).getItem().getDefaultInstance();


        if (event.isCanceled()) return;
        if(item.getItem() instanceof JarOfBonmeelItem item2 && state.is(ModTags.ModBlockTags.BONMEELABLE)) {
            event.setCanceled(true);
            event.setCancellationResult(item2.useOn(new UseOnContext(player, hand, event.getHitVec())));

        } else
            if((item.is(ModItems.REBREWED_POTION.get()) || item.is(ModItems.EXTRACTED_BOTTLE.get())) && state.is(Blocks.DIRT)) {
            event.setCanceled(true);
        } else {
                if(item.is(ItemTags.AXES) && (state.is(ModBlocks.VIVICUS_LOG.get()) || state.is(ModBlocks.VIVICUS_WOOD.get()))) {
                var strippedBlock = AxeItem.STRIPPABLES.get(state.getBlock());
                var state1 = strippedBlock.defaultBlockState()
                        .setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS))
                        .setValue(ModStateProperties.COLOR, state.getValue(ModStateProperties.COLOR));

                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, item);
                }

                level.playSound(player, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);

                level.setBlock(pos, state1, 3);
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, state1));
                itemStack.hurtAndBreak(1, player, player1 -> {
                    player1.broadcastBreakEvent(player1.getUsedItemHand());
                });

                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);

                } else
                if (((item.is(ModItems.JAR_OF_BONMEEL.get()) || item.is(ModItems.JAR_OF_ACID.get())) && state.is(Blocks.CAULDRON))){
                    var cauldronType = item.is(ModItems.JAR_OF_BONMEEL.get()) ? ModBlocks.BONMEEL_FILLED_CAULDRON.get() :  ModBlocks.ACID_FILLED_CAULDRON.get();
                    var state1 = cauldronType.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3);
                    level.setBlock(pos, state1, 3);
                    level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
                    if (!player.isCreative()) player.setItemInHand(hand, ItemUtils.createFilledResult(itemStack, player, new ItemStack(Items.GLASS_BOTTLE)));
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
            }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        BlockPos pos = event.getPos();
        LevelAccessor level = event.getLevel();

        if(level.getBlockEntity(pos) instanceof GiantCropBlockEntity entity) {
            BlockPos.withinManhattanStream(entity.center, 1, 1, 1).forEach(blockPos -> {
               if (level.getBlockState(blockPos).is(ModTags.ModBlockTags.GIANT_CROPS)) level.destroyBlock(blockPos, true);
            });
        }
        
        if (level.getBlockEntity(pos) instanceof BondripiaBlockEntity entity) {
            Direction.Plane.HORIZONTAL.forEach(direction -> {
                BlockPos blockPos = entity.center.relative(direction);

                level.destroyBlock(blockPos, true);
            });
            level.destroyBlock(entity.center, true);
        }
    }
}
