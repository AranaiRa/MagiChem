package com.aranaira.magichem.block.entity.routers;

import com.aranaira.magichem.block.CentrifugeBlock;
import com.aranaira.magichem.block.entity.CentrifugeBlockEntity;
import com.aranaira.magichem.foundation.enums.CentrifugeRouterType;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CentrifugeRouterBlockEntity extends BlockEntity implements MenuProvider {
    private BlockPos masterPos;
    private CentrifugeBlockEntity master;
    private CentrifugeRouterType routerType = CentrifugeRouterType.NONE;
    private Direction facing;
    private int packedData;

    public ContainerData data;

    public CentrifugeRouterBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.CENTRIFUGE_ROUTER_BE.get(), pPos, pBlockState);

        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                if(pIndex == 0) {
                    return CentrifugeRouterBlockEntity.this.packedData;
                    //return (byte)(mapFacingToByte(facing) << 4 | mapTypeToByte(routerType));
                }
                return -1;
            }

            @Override
            public void set(int pIndex, int pValue) {
                if(pIndex == 0) {
                    CentrifugeRouterBlockEntity.this.packedData = pValue;
                    /*
                    routerType = unmapTypeFromByte((byte)(pValue << 4));
                    facing = unmapFacingFromByte((byte) (pValue & 0x1111));*/
                }
            }

            @Override
            public int getCount() {
                return 1;
            }
        };
    }

    public Direction getFacing() {
        //return unmapFacingFromByte((byte)(packedData & 0x1111));
        return this.facing;
    }

    public CentrifugeRouterType getRouterType() {
        //return unmapTypeFromByte((byte)(packedData >> 4));
        return this.routerType;
    }

    public void configure(BlockPos pMasterPos, CentrifugeRouterType pRouterType, Direction pFacing) {
        this.masterPos = pMasterPos;
        this.routerType = pRouterType;
        this.facing = pFacing;
        getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
//        byte packedData = (byte)(mapFacingToByte(pFacing) << 4 | mapTypeToByte(pType));
//        data.set(0, packedData);
//        setChanged();
    }

    public CentrifugeBlockEntity getMaster(){
        if(master == null) {
            master = (CentrifugeBlockEntity) getLevel().getBlockEntity(masterPos);
        }
        return master;
    }

    @Override
    public void setRemoved() {
        for(BlockPos offset : CentrifugeBlock.getRouterOffsets(facing)) {
            getLevel().destroyBlock(masterPos.offset(offset), true);
        }
        getLevel().destroyBlock(masterPos, true);
        super.setRemoved();
    }

    public BlockPos getMasterPos() {
        return masterPos;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.centrifuge");
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return getMaster().getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.putLong("masterPos", masterPos.asLong());
        nbt.putByte("typeAndFacing", (byte)(mapFacingToByte(facing) << 4 | mapTypeToByte(routerType)));
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        masterPos = BlockPos.of(nbt.getLong("masterPos"));
        byte typeAndFacing = nbt.getByte("typeAndFacing");
        routerType = unmapTypeFromByte((byte) (typeAndFacing & 15));
        facing = unmapFacingFromByte((byte) (typeAndFacing >> 4));
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();

        nbt.putLong("masterPos", masterPos.asLong());
        nbt.putByte("typeAndFacing", (byte)(mapFacingToByte(facing) << 4 | mapTypeToByte(routerType)));

        return nbt;
    }

    private byte mapTypeToByte(CentrifugeRouterType pRouterType) {
        if(pRouterType == null)
            return 0;

        return (byte)switch(pRouterType) {
            case PLUG_LEFT -> 1;
            case PLUG_RIGHT -> 2;
            case COG -> 3;
            default -> 0;
        };
    }

    private CentrifugeRouterType unmapTypeFromByte(byte input) {
        return switch(input) {
            case 1 -> CentrifugeRouterType.PLUG_LEFT;
            case 2 -> CentrifugeRouterType.PLUG_RIGHT;
            case 3 -> CentrifugeRouterType.COG;
            default -> CentrifugeRouterType.NONE;
        };
    }

    private byte mapFacingToByte(Direction pFacing) {
        if(pFacing == null)
            return 0;

        return (byte)switch(pFacing) {
            case NORTH -> 1;
            case SOUTH -> 2;
            case EAST -> 3;
            case WEST -> 4;
            default -> 0;
        };
    }

    private Direction unmapFacingFromByte(byte input) {
        return switch(input) {
            case 1 -> Direction.NORTH;
            case 2 -> Direction.SOUTH;
            case 3 -> Direction.EAST;
            case 4 -> Direction.WEST;
            default -> null;
        };
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return getMaster().createMenu(pContainerId, pPlayerInventory, pPlayer);
    }
}
