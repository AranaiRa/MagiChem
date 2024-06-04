package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.gui.AlchemicalNexusMenu;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AlchemicalNexusBlockEntity extends BlockEntity implements MenuProvider {
    public static final int
        DATA_COUNT = 0;
    protected ContainerData data;

    public AlchemicalNexusBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.ALCHEMICAL_NEXUS_BE.get(), pPos, pBlockState);

        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return 0;
            }

            @Override
            public void set(int pIndex, int pValue) {

            }

            @Override
            public int getCount() {
                return 0;
            }
        };
    }

    public static <E extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, E e) {

    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.alchemical_nexus");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new AlchemicalNexusMenu(pContainerId, pPlayerInventory, this, this.data);
    }
}
