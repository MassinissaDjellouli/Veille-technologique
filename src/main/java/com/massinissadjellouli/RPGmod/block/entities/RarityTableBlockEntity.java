package com.massinissadjellouli.RPGmod.block.entities;

import com.massinissadjellouli.RPGmod.recipe.RarityTableRecipe;
import com.massinissadjellouli.RPGmod.screen.RarityTableMenu;
import com.massinissadjellouli.RPGmod.tags.RarityTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static com.massinissadjellouli.RPGmod.recipe.RarityTableRecipe.Type.INSTANCE;


public class RarityTableBlockEntity extends BlockEntity implements MenuProvider {

    public static final int AMOUNT_OF_SLOTS_TO_INSERT_ROWS = 3;
    public static final int AMOUNT_OF_SLOTS_TO_INSERT_COLS = 3;
    public static final int AMOUNT_OF_SLOTS_TO_INSERT = AMOUNT_OF_SLOTS_TO_INSERT_ROWS * AMOUNT_OF_SLOTS_TO_INSERT_COLS;
    public static final int POSITION_OF_ITEM_SLOT = 9;
    public static final int POSITION_OF_RESULT_SLOT = 10;
    public static final int AMOUNT_OF_SLOTS = 11;
    private boolean recentlyCrafted = false;


    public RarityTableBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.RARITY_TABLE.get(), pPos, pBlockState);
    }

    private final ItemStackHandler itemStackHandler = new ItemStackHandler(AMOUNT_OF_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    @Override
    public Component getDisplayName() {
        return Component.literal("Table de rareté");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new RarityTableMenu(pContainerId, pPlayerInventory, this);

    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @NotNull Direction side) {

        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemStackHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemStackHandler.serializeNBT());
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemStackHandler.deserializeNBT(nbt.getCompound("inventory"));
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemStackHandler.getSlots());
        for (int i = 0; i < itemStackHandler.getSlots(); i++) {
            inventory.setItem(i, itemStackHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public static void tick(Level level, BlockPos blockPos,
                            BlockState state, RarityTableBlockEntity rarityTable) {

        if (level.isClientSide()) return;
        SimpleContainer inventory = new SimpleContainer(rarityTable.itemStackHandler.getSlots());
        for (int i = 0; i < rarityTable.itemStackHandler.getSlots(); i++) {
            inventory.setItem(i, rarityTable.itemStackHandler.getStackInSlot(i));
        }

        if (!rarityTable.recentlyCrafted) {
            craftItem(rarityTable);
            setChanged(level, blockPos, state);
            return;
        }
        if (!hasRecipe(rarityTable) && !inventory.getItem(POSITION_OF_RESULT_SLOT).isEmpty()) {
            rarityTable.itemStackHandler.extractItem(POSITION_OF_RESULT_SLOT, 1, false);
            rarityTable.recentlyCrafted = false;
            return;
        }
        if (inventory.getItem(POSITION_OF_RESULT_SLOT).isEmpty()) {
            for (int i = 0; i < AMOUNT_OF_SLOTS_TO_INSERT; i++) {
                rarityTable.itemStackHandler.extractItem(i, 1, false);
            }
            rarityTable.itemStackHandler.extractItem(POSITION_OF_ITEM_SLOT, 1, false);
            rarityTable.recentlyCrafted = false;
        }
        setChanged(level, blockPos, state);
    }

    private static Optional<RarityTableRecipe> getRecipe(RarityTableBlockEntity rarityTable) {
        Level level = rarityTable.level;
        SimpleContainer inventory = new SimpleContainer(rarityTable.itemStackHandler.getSlots());
        for (int i = 0; i < rarityTable.itemStackHandler.getSlots(); i++) {
            inventory.setItem(i, rarityTable.itemStackHandler.getStackInSlot(i));
        }
        return level.getRecipeManager().getRecipeFor(INSTANCE, inventory, level);
    }

    private static boolean hasRecipe(RarityTableBlockEntity rarityTable) {
        SimpleContainer inventory = new SimpleContainer(rarityTable.itemStackHandler.getSlots());
        for (int i = 0; i < rarityTable.itemStackHandler.getSlots(); i++) {
            inventory.setItem(i, rarityTable.itemStackHandler.getStackInSlot(i));
        }

        Level level = rarityTable.level;

        Optional<RarityTableRecipe> recipe = getRecipe(rarityTable);

        return recipe.isPresent() && recipe.get().matches(inventory, level);
    }

    private static boolean canCraft(RarityTableBlockEntity rarityTable) {
        SimpleContainer inventory = new SimpleContainer(rarityTable.itemStackHandler.getSlots());
        for (int i = 0; i < rarityTable.itemStackHandler.getSlots(); i++) {
            inventory.setItem(i, rarityTable.itemStackHandler.getStackInSlot(i));
        }
        return hasRecipe(rarityTable) && canInsertAmountIntoResultSlot(inventory);
    }

    private static boolean canInsertAmountIntoResultSlot(SimpleContainer inventory) {
        return inventory.getItem(POSITION_OF_RESULT_SLOT).isEmpty() ||
                inventory.getItem(POSITION_OF_RESULT_SLOT).getMaxStackSize() > inventory.getItem(POSITION_OF_RESULT_SLOT).getCount();
    }


    private static void craftItem(RarityTableBlockEntity rarityTable) {
        ItemStack item = rarityTable.itemStackHandler.getStackInSlot(POSITION_OF_ITEM_SLOT);
        if (canCraft(rarityTable) && !rarityTable.recentlyCrafted) {
            ItemStack itemStack = new ItemStack(item.getItem(), 1);
            String itemRarity = item.getTag().getString("item_rarity");
            int level = RarityTags.getTag(itemRarity).level;
            itemStack.addTagElement("item_rarity", StringTag.valueOf(RarityTags.getTagByLevel(level + 1).name));
            rarityTable.itemStackHandler.insertItem(POSITION_OF_RESULT_SLOT, itemStack, false);
            rarityTable.recentlyCrafted = true;
        }

    }
}
