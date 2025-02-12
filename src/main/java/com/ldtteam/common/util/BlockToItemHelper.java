package com.ldtteam.common.util;

import com.ldtteam.blockui.mod.item.BlockStateRenderingData;
import com.ldtteam.common.fakelevel.SingleBlockFakeLevel.SidedSingleBlockFakeLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.Nullable;

/**
 * Methods for getting itemStack from blockState.
 */
public class BlockToItemHelper
{
    public static final HitResult ZERO_POS_HIT_RESULT = new BlockHitResult(Vec3.atCenterOf(BlockPos.ZERO), Direction.NORTH, BlockPos.ZERO, true);
    private static final SidedSingleBlockFakeLevel fakeLevel = new SidedSingleBlockFakeLevel();

    /**
     * Mostly for use in UI where you dont have level instance (eg. player selects block, from xml, but not when displaying real world
     * info - see {@link BlockStateRenderingData#of(Level, BlockPos, Player)}).
     * 
     * @return result of player middle-mouse-button click with more sensible defaults (liquids -> buckets, fire -> flint+steel), might
     *         be {@link ItemStack#isEmpty()} in case of error
     */
    public static ItemStack getItemStack(final BlockState blockState, final BlockEntity blockEntity, final Player player)
    {
        // quick path air blocks
        if (blockState.getBlock() instanceof AirBlock)
        {
            return ItemStack.EMPTY;
        }

        // client vs server concurrency - we dont care if create two instances, the other should just disappear

        return fakeLevel.get(player.level()).useFakeLevelContext(blockState,
            blockEntity,
            player.level(),
            level -> getItemStackUsingPlayerPick(level, BlockPos.ZERO, player, ZERO_POS_HIT_RESULT));
    }

    /**
     * Mostly for use by machines/entities when you dont have player instance - uses fake player.
     * 
     * @return result of player middle-mouse-button click with more sensible defaults (liquids -> buckets, fire -> flint&steel), might
     *         be {@link ItemStack#isEmpty()} in case of error
     */
    public static ItemStack getItemStack(final ServerLevel serverLevel, final BlockPos pos)
    {
        return getItemStack(serverLevel, pos, FakePlayerFactory.getMinecraft(serverLevel));
    }

    /**
     * General method when you have everything block->item mapping needs, but you don't have hit result (ray trace from camera).
     * 
     * @return result of player middle-mouse-button click with more sensible defaults (liquids -> buckets, fire -> flint&steel), might
     *         be {@link ItemStack#isEmpty()} in case of error
     */
    public static ItemStack getItemStack(final Level level, final BlockPos pos, final Player player)
    {
        return getItemStackUsingPlayerPick(level, pos, player, null);
    }

    /**
     * @return result of player middle-mouse-button click with more sensible defaults (liquids -> buckets, fire -> flint&steel), might
     *         be {@link ItemStack#isEmpty()} in case of error
     */
    public static ItemStack getItemStackUsingPlayerPick(final Level level, final BlockPos pos, final Player player, @Nullable HitResult hitResult)
    {
        if (hitResult == null)
        {
            hitResult = new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, true);
        }

        final BlockState blockState = level.getBlockState(pos);
        ItemStack result = blockState.getCloneItemStack(hitResult, level, pos, player);

        if (result.isEmpty())
        {
            result = getItem(blockState).getDefaultInstance();
        }

        return result;
    }

    /**
     * @param blockState source for item
     * @return vanilla result with few fixes
     */
    public static Item getItem(final BlockState blockState)
    {
        final Block block = blockState.getBlock();
        if (block instanceof final LiquidBlock liquid)
        {
            return liquid.fluid.getBucket();
        }
        else if (block instanceof BubbleColumnBlock)
        {
            return Fluids.WATER.getBucket();
        }
        else if (block instanceof BaseFireBlock)
        {
            return Items.FLINT_AND_STEEL;
        }

        return block.asItem();
    }
}
