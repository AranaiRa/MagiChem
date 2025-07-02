package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.capabilities.grime.IGrimeCapability;
import com.aranaira.magichem.gui.EldrinOrreryMenu;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.api.affinity.Affinity;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import com.mna.items.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class EldrinOrreryBlockEntity extends BlockEntity implements MenuProvider {
    public static final int
        SLOT_COUNT = 10, SLOT_INPUT_START = 0, SLOT_INPUT_COUNT = 5, SLOT_OUTPUT_START = 5, SLOT_OUTPUT_COUNT = 5,
        SLOT_SOLAR_INPUT = 0, SLOT_LUNAR_INPUT = 1, SLOT_SIDEREAL_INPUT = 2, SLOT_FIRMAMENT_INPUT = 3, SLOT_REALM_INPUT = 4,
        SLOT_SOLAR_OUTPUT = 5, SLOT_LUNAR_OUTPUT = 6, SLOT_SIDEREAL_OUTPUT = 7, SLOT_FIRMAMENT_OUTPUT = 8, SLOT_REALM_OUTPUT = 9,
        DATA_COUNT = 5,
        DATA_SOLAR = 0, DATA_LUNAR = 1, DATA_SIDEREAL = 2, DATA_FIRMAMENT = 3, DATA_REALM = 4;

    private UUID placedBy;
    private int solar, lunar, sidereal, firmament, realm;

    private ContainerData data = new ContainerData() {
        @Override
        public int get(int pIndex) {
            switch(pIndex) {
                case DATA_SOLAR: {
                    return solar;
                }
                case DATA_LUNAR: {
                    return lunar;
                }
                case DATA_SIDEREAL: {
                    return sidereal;
                }
                case DATA_FIRMAMENT: {
                    return firmament;
                }
                case DATA_REALM: {
                    return realm;
                }
                default: return -1;
            }
        }

        @Override
        public void set(int pIndex, int pValue) {
            switch(pIndex) {
                case DATA_SOLAR: {
                    solar = pValue;
                }
                case DATA_LUNAR: {
                    lunar = pValue;
                }
                case DATA_SIDEREAL: {
                    sidereal = pValue;
                }
                case DATA_FIRMAMENT: {
                    firmament = pValue;
                }
                case DATA_REALM: {
                    realm = pValue;
                }
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };
    private ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == SLOT_SOLAR_INPUT)
                return stack.getItem() == ItemRegistry.SOLAR_ORB.get() || stack.getItem() == ItemRegistry.DEBUG_ORB.get();
            else if (slot == SLOT_LUNAR_INPUT)
                return stack.getItem() == ItemRegistry.LUNAR_ORB.get() || stack.getItem() == ItemRegistry.DEBUG_ORB.get();
            else if (slot == SLOT_SIDEREAL_INPUT)
                return stack.getItem() == ItemRegistry.SIDEREAL_ORB.get() || stack.getItem() == ItemRegistry.DEBUG_ORB.get();
            else if (slot == SLOT_FIRMAMENT_INPUT)
                return (stack.getItem() instanceof MateriaItem mi && mi.getMateriaName().equals("firmament"));
            else if (slot == SLOT_REALM_INPUT)
                return (stack.getItem() instanceof MateriaItem mi && mi.getMateriaName().equals("realm"));

            return false;
        }
    };
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public EldrinOrreryBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.ELDRIN_ORRERY_BE.get(), pPos, pBlockState);
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new EldrinOrreryMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    private static final Affinity[] AFFINITIES = {Affinity.ENDER, Affinity.EARTH, Affinity.WATER, Affinity.WIND, Affinity.FIRE, Affinity.ARCANE};
    private void injectPower() {
        if (!this.getLevel().isClientSide()) {
            this.getLevel().getCapability(WorldMagicProvider.MAGIC).ifPresent((m) -> {
                for(Affinity aff : AFFINITIES) {
                    for (int i = 0; i < AFFINITIES.length; i++) {
                        float amount = this.getPowerPerTick(aff);
                        m.getWellspringRegistry().insertPower(this.placedBy, this.level, aff, amount);
                    }
                }
            });
        }
    }

    public float getPowerPerTick(Affinity aff) {
        MutableFloat powerPerTick = new MutableFloat(0.0F);
        this.getLevel().getCapability(WorldMagicProvider.MAGIC).ifPresent((m) -> {
            float amount = 0.02F;
            float multiplier = m.getWellspringRegistry().getEldrinGenerationMultiplierFor(this.placedBy, this.level, aff);
            powerPerTick.setValue(amount * multiplier);
        });
        return powerPerTick.floatValue();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) return lazyItemHandler.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }
}
