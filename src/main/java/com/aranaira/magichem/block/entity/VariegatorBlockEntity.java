package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.gui.VariegatorMenu;
import com.aranaira.magichem.recipe.ColorationRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.util.render.ColorUtils;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.particles.types.movers.ParticleLerpMover;
import com.mna.tools.math.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Random;

public class VariegatorBlockEntity extends BlockEntity implements MenuProvider {

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    public static final int
        SLOT_COUNT = 8,
        SLOT_DYE_INPUT = 0, SLOT_DYE_BOTTLES = 1, SLOT_INPUT_START = 2, SLOT_INPUT_COUNT = 3, SLOT_OUTPUT_START = 5, SLOT_OUTPUT_COUNT = 3,
        DATA_COUNT = 1,
        DATA_PROGRESS = 0;
    public int
        progress = 0, selectedColor = -1;
    public int
        dyeAdmixture,
        dyeRed, dyeOrange, dyeYellow, dyeLime,
        dyeGreen, dyeCyan, dyeLightBlue, dyeBlue,
        dyePurple, dyeMagenta, dyePink, dyeBrown,
        dyeBlack, dyeGray, dyeLightGray, dyeWhite;
    private ColorationRecipe currentRecipe;
    private static final ItemStack ADMIXTURE_COLOR_STACK = new ItemStack(ItemRegistry.getAdmixturesMap(false, false).get("color"));
    public static final DyeColor[] COLOR_GUI_ORDER = {
            DyeColor.WHITE, DyeColor.LIGHT_GRAY, DyeColor.GRAY, DyeColor.BLACK,
            DyeColor.BROWN, DyeColor.RED, DyeColor.ORANGE, DyeColor.YELLOW,
            DyeColor.LIME, DyeColor.GREEN, DyeColor.CYAN, DyeColor.LIGHT_BLUE,
            DyeColor.BLUE, DyeColor.PURPLE, DyeColor.MAGENTA, DyeColor.PINK
    };
    public static final float
        FILL_RATE = 0.03f;
    public float
            beamFill;
    private static final Random r = new Random();

    private ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if(slot == SLOT_DYE_BOTTLES) {
                return false;
            } else if(slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT) {
                return false;
            }

            return super.isItemValid(slot, stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            syncAndSave();
            super.onContentsChanged(slot);
        }
    };
    private ContainerData data = new ContainerData() {
        @Override
        public int get(int pIndex) {
            if(pIndex == DATA_PROGRESS)
                return VariegatorBlockEntity.this.progress;
            return 0;
        }

        @Override
        public void set(int pIndex, int pValue) {
            if(pIndex == DATA_PROGRESS)
                VariegatorBlockEntity.this.progress = pValue;
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public VariegatorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.VARIEGATOR_BE.get(), pPos, pBlockState);
    }

    //////////
    // BOILERPLATE CODE
    //////////

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        nbt.putInt("selectedColor", this.selectedColor);

        CompoundTag colors = packColorsToCompoundTag();

        nbt.put("colors", colors);

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progress = nbt.getInt("craftingProgress");
        selectedColor = nbt.getInt("selectedColor");

        unpackColorsFromCompoundTag(nbt.getCompound("colors"));
    }

    private CompoundTag packColorsToCompoundTag() {
        CompoundTag colors = new CompoundTag();

        colors.putInt("admixture", dyeAdmixture);
        colors.putInt("red", dyeRed);
        colors.putInt("orange", dyeOrange);
        colors.putInt("yellow", dyeYellow);
        colors.putInt("lime", dyeLime);
        colors.putInt("green", dyeGreen);
        colors.putInt("cyan", dyeCyan);
        colors.putInt("lightBlue", dyeLightBlue);
        colors.putInt("blue", dyeBlue);
        colors.putInt("purple", dyePurple);
        colors.putInt("magenta", dyeMagenta);
        colors.putInt("pink", dyePink);
        colors.putInt("brown", dyeBrown);
        colors.putInt("black", dyeBlack);
        colors.putInt("gray", dyeGray);
        colors.putInt("lightGray", dyeLightGray);
        colors.putInt("white", dyeWhite);

        return colors;
    }

    private void unpackColorsFromCompoundTag(CompoundTag nbt) {
        dyeAdmixture = nbt.getInt("admixture");
        dyeRed = nbt.getInt("red");
        dyeOrange = nbt.getInt("orange");
        dyeYellow = nbt.getInt("yellow");
        dyeLime = nbt.getInt("lime");
        dyeGreen = nbt.getInt("green");
        dyeCyan = nbt.getInt("cyan");
        dyeLightBlue = nbt.getInt("lightBlue");
        dyeBlue = nbt.getInt("blue");
        dyePurple = nbt.getInt("purple");
        dyeMagenta = nbt.getInt("magenta");
        dyePink = nbt.getInt("pink");
        dyeBrown = nbt.getInt("brown");
        dyeBlack = nbt.getInt("black");
        dyeGray = nbt.getInt("gray");
        dyeLightGray = nbt.getInt("lightGray");
        dyeWhite = nbt.getInt("white");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        nbt.putInt("selectedColor", this.selectedColor);

        CompoundTag colors = packColorsToCompoundTag();

        nbt.put("colors", colors);

        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void syncAndSave() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public void packInventoryToBlockItem() {
        ItemStack stack = new ItemStack(BlockRegistry.VARIEGATOR.get());

        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.put("colors", packColorsToCompoundTag());
        stack.setTag(nbt);

        Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
    }

    public void unpackDyeGaugesFromNBT(CompoundTag pColorTag) {
        unpackColorsFromCompoundTag(pColorTag);
    }

    public void unpackInventoryFromNBT(CompoundTag pInventoryTag) {
        itemHandler.deserializeNBT(pInventoryTag);
    }

    ////////////////////
    // CRAFTING HANDLERS
    ////////////////////

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new VariegatorMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    public static <E extends BlockEntity> void tick(Level pLevel, BlockPos pPos, BlockState pState, E e) {
        if(e instanceof VariegatorBlockEntity vbe) {
            if(!pLevel.isClientSide()) {
                //Consume items in the insertion slot
                {
                    boolean changed = false;
                    ItemStack insert = vbe.itemHandler.getStackInSlot(SLOT_DYE_INPUT);
                    ItemStack bottles = vbe.itemHandler.getStackInSlot(SLOT_DYE_BOTTLES);
                    //Check if we're inserting Admixture of Color
                    if (insert.getItem() == ADMIXTURE_COLOR_STACK.getItem()) {
                        //Make sure there's somewhere for the bottles to go
                        if (bottles.getCount() < vbe.itemHandler.getSlotLimit(SLOT_DYE_BOTTLES)) {

                            //Allow overfill by one item for GUI aesthetic reasons
                            if (vbe.dyeAdmixture < Config.variegatorMaxAdmixture) {
                                vbe.dyeAdmixture += Config.variegatorAdmixturePerItem;

                                insert.shrink(1);
                                if (bottles.isEmpty()) {
                                    bottles = new ItemStack(Items.GLASS_BOTTLE, 1);
                                } else {
                                    bottles.grow(1);
                                }
                                vbe.itemHandler.setStackInSlot(SLOT_DYE_BOTTLES, bottles);

                                changed = true;
                            }
                        }
                    }
                    //Otherwise, check if we're inserting dye
                    else if (insert.getItem() instanceof DyeItem) {
                        DyeColor color = DyeColor.getColor(insert);

                        if (color != null) {
                            int fill = vbe.getDyeFillByColor(color);
                            //Allow overfill by one item for GUI aesthetic reasons
                            if (fill < Config.variegatorMaxDye) {
                                vbe.setDyeFillByColor(color, fill + Config.variegatorDyePerItem);

                                insert.shrink(1);

                                changed = true;
                            }
                        }
                    }

                    if (changed)
                        vbe.syncAndSave();
                }
            } else {
                //Animation drivers
                {
                    boolean hasItem = !getCurrentProcessingStack(vbe).isEmpty() ||
                            !vbe.itemHandler.getStackInSlot(SLOT_OUTPUT_START).isEmpty() ||
                            !vbe.itemHandler.getStackInSlot(SLOT_OUTPUT_START + 1).isEmpty() ||
                            !vbe.itemHandler.getStackInSlot(SLOT_OUTPUT_START + 2).isEmpty();

                    if(hasItem) {
                        vbe.beamFill = Math.min(1, vbe.beamFill + FILL_RATE);
                    } else {
                        vbe.beamFill = Math.max(0, vbe.beamFill - FILL_RATE);
                    }

                    //Spawn particles
                    if(vbe.beamFill == 1){
                        float vOffset = pState.getValue(MagiChemBlockStateProperties.GROUNDED) ? 1.5f : -0.5f;
                        Vector3 center = new Vector3(pPos.getX() + 0.5, pPos.getY() + vOffset, pPos.getZ() + 0.5);

                        if(vbe.selectedColor != -1) {
                            if (vbe.getLevel().getGameTime() % 8 == 0) {
                                int[] color = ColorUtils.getRGBIntTint(COLOR_GUI_ORDER[vbe.selectedColor]);

                                for (int i = 0; i < 6; i++) {
                                    Vector3 offset = new Vector3(r.nextFloat() - 0.5, r.nextFloat() - 0.5, r.nextFloat() - 0.5).normalize().scale(0.3f);
                                    vbe.getLevel().addParticle(new MAParticleType(ParticleInit.ARCANE_LERP.get())
                                                    .setColor(color[0], color[1], color[2], 128)
                                                    .setScale(0.09f).setMaxAge(16)
                                                    .setMover(new ParticleLerpMover(center.x + offset.x, center.y + offset.y, center.z + offset.z, center.x, center.y, center.z)),
                                            center.x, center.y, center.z,
                                            0, 0, 0);
                                }
                            }
                        }
                    }
                }
            }

            //Crafting logic
            {
                ColorationRecipe recipe = getActiveRecipe(vbe);
                int operationTicks = getOperationTicks(vbe);

                if(recipe != null) {
                    if(canCraftItem(vbe, recipe)) {
                        if(vbe.progress > operationTicks) {
                            if(!pLevel.isClientSide())
                                craftItem(vbe, recipe);
                        } else {
                            vbe.progress++;
                        }
                    }
                } else {
                    vbe.progress = 0;
                }
            }
        }
    }

    protected static void craftItem(VariegatorBlockEntity pEntity, ColorationRecipe pRecipe) {
        DyeColor color = DyeColor.WHITE;
        if(pEntity.selectedColor != -1) color = COLOR_GUI_ORDER[pEntity.selectedColor];

        ItemStack result;
        if(pEntity.selectedColor == -1)
            result = pRecipe.getColorlessDefault().copy();
        else
            result = pRecipe.getResultsAsMap(false).get(color).copy();

        SimpleContainer output = pEntity.getContentsOfOutputSlots();
        output.addItem(result);
        pEntity.setContentsOfOutputSlots(output);

        getCurrentProcessingStack(pEntity).shrink(1);

        if(pEntity.dyeAdmixture > 0)
            pEntity.dyeAdmixture--;
        else if(pEntity.selectedColor != -1){
            pEntity.setDyeFillByColor(color, Math.max(0, pEntity.getDyeFillByColor(color) - 1));
        }

        pEntity.progress = 0;
        pEntity.syncAndSave();
    }

    public static int getOperationTicks(VariegatorBlockEntity pEntity) {
        if(pEntity.currentRecipe == null)
            return 1;

        int currentAdmixture = pEntity.dyeAdmixture;
        int currentDye;
        if(pEntity.selectedColor == -1)
            currentDye = -1;
        else
            currentDye = pEntity.getDyeFillByColor(COLOR_GUI_ORDER[pEntity.selectedColor]);

        boolean sufficientDye = currentDye >= pEntity.currentRecipe.getChargeUsage();
        boolean sufficientAdmixture = currentAdmixture >= pEntity.currentRecipe.getChargeUsage();

        if(sufficientAdmixture && sufficientDye) {
            float fillPct = (float) currentDye / (float) Config.variegatorMaxDye;
            float discount = 1.0f - (fillPct * ((Config.variegatorMatchedColorTimeDiscount) / 100f));

            return Math.max(1, Math.round(discount * Config.variegatorOperationTimeFast));
        } else if(sufficientAdmixture || sufficientDye) {
            return Math.max(1, Config.variegatorOperationTimeFast);
        } else {
            return Math.max(1, Config.variegatorOperationTimeSlow);
        }
    }

    public SimpleContainer getContentsOfOutputSlots() {
        SimpleContainer output = new SimpleContainer(SLOT_OUTPUT_COUNT);

        for(int i = SLOT_OUTPUT_START; i<SLOT_OUTPUT_START+SLOT_OUTPUT_COUNT; i++) {
            output.setItem(i-SLOT_OUTPUT_START, itemHandler.getStackInSlot(i));
        }

        return output;
    }

    public void setContentsOfOutputSlots(SimpleContainer replacementInventory) {
        for(int i = SLOT_OUTPUT_START; i<SLOT_OUTPUT_START+SLOT_OUTPUT_COUNT; i++) {
            itemHandler.setStackInSlot(i, replacementInventory.getItem(i-SLOT_OUTPUT_START));
        }
    }

    ////////////////////
    // RECIPE HANDLING
    ////////////////////

    public static ItemStack getCurrentProcessingStack(VariegatorBlockEntity pEntity) {
        ItemStack query;

        for(int i=SLOT_INPUT_COUNT-1; i>=0; i--) {
            query = pEntity.itemHandler.getStackInSlot(SLOT_INPUT_START + i);

            if(!query.isEmpty()) {
                return query;
            }
        }

        return ItemStack.EMPTY;
    }

    protected static ColorationRecipe getActiveRecipe(VariegatorBlockEntity pEntity) {
        Level level = pEntity.level;

        ItemStack query = getCurrentProcessingStack(pEntity);
        if(!query.isEmpty()) {
            if(pEntity.currentRecipe != null) {
                HashMap<DyeColor, ItemStack> allResults = pEntity.currentRecipe.getResultsAsMap(false);

                if(pEntity.currentRecipe.getColorlessDefault().getItem() == query.getItem())
                    return pEntity.currentRecipe;
                //Iterate through all the possible results of the current recipe
                //If one of them matches, the current recipe is fine, and we don't have to check the registry
                for(ItemStack result : allResults.values()) {
                    if(result.getItem() == query.getItem()) {
                        return pEntity.currentRecipe;
                    }
                }

                //Otherwise we're checkin' the registry
                ColorationRecipe recipe = ColorationRecipe.getColorationRecipe(level, query);
                pEntity.currentRecipe = recipe;
                return recipe;
            } else {
                ColorationRecipe recipe = ColorationRecipe.getColorationRecipe(level, query);
                pEntity.currentRecipe = recipe;
                return recipe;
            }
        }
        return null;
    }

    public static boolean canCraftItem(VariegatorBlockEntity pEntity, ColorationRecipe pRecipe) {
        DyeColor color = DyeColor.WHITE;
        if(pEntity.selectedColor != -1) color = COLOR_GUI_ORDER[pEntity.selectedColor];

        ItemStack result;
        if(pEntity.selectedColor == -1)
            result = pRecipe.getColorlessDefault();
        else
            result = pRecipe.getResultsAsMap(false).get(color);

        return pEntity.getContentsOfOutputSlots().canAddItem(result);
    }

    public int getDyeFillByColor(DyeColor pColor) {
        return switch(pColor) {
            case RED -> dyeRed;
            case ORANGE -> dyeOrange;
            case YELLOW -> dyeYellow;
            case LIME -> dyeLime;
            case GREEN -> dyeGreen;
            case CYAN -> dyeCyan;
            case LIGHT_BLUE -> dyeLightBlue;
            case BLUE -> dyeBlue;
            case PURPLE -> dyePurple;
            case MAGENTA -> dyeMagenta;
            case PINK -> dyePink;
            case BROWN -> dyeBrown;
            case BLACK -> dyeBlack;
            case GRAY -> dyeGray;
            case LIGHT_GRAY -> dyeLightGray;
            case WHITE -> dyeWhite;
        };
    }

    public void setDyeFillByColor(DyeColor pColor, int pValue) {
        switch(pColor) {
            case RED: {
                dyeRed = pValue;
                break;
            }
            case ORANGE: {
                dyeOrange = pValue;
                break;
            }
            case YELLOW: {
                dyeYellow = pValue;
                break;
            }
            case LIME: {
                dyeLime = pValue;
                break;
            }
            case GREEN: {
                dyeGreen = pValue;
                break;
            }
            case CYAN: {
                dyeCyan = pValue;
                break;
            }
            case LIGHT_BLUE: {
                dyeLightBlue = pValue;
                break;
            }
            case BLUE: {
                dyeBlue = pValue;
                break;
            }
            case PURPLE: {
                dyePurple = pValue;
                break;
            }
            case MAGENTA: {
                dyeMagenta = pValue;
                break;
            }
            case PINK: {
                dyePink = pValue;
                break;
            }
            case BROWN: {
                dyeBrown = pValue;
                break;
            }
            case BLACK: {
                dyeBlack = pValue;
                break;
            }
            case GRAY: {
                dyeGray = pValue;
                break;
            }
            case LIGHT_GRAY: {
                dyeLightGray = pValue;
                break;
            }
            case WHITE: {
                dyeWhite = pValue;
            }
        }
    }

    ////////////////////
    // INTERACTION AND VFX
    ////////////////////

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-1, -2, -1), getBlockPos().offset(1,2,1));
    }
}
