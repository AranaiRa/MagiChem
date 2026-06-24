package com.aranaira.magichem.block.entity.routers;

import com.aranaira.magichem.block.GrandFuseryBlock;
import com.aranaira.magichem.block.GrandFuseryRouterBlock;
import com.aranaira.magichem.block.entity.GrandFuseryBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractBlockEntityWithEfficiency;
import com.aranaira.magichem.block.entity.ext.AbstractDirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.*;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.foundation.enums.GrandFuseryRouterType;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.items.base.INoCreativeTab;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.*;

public class GrandFuseryRouterBlockEntity extends AbstractBlockEntityWithEfficiency implements MenuProvider, INoCreativeTab, ICanTakePlugins, IRouterBlockEntity, IPoweredAlchemyDevice, IDestroysMasterOnDestruction, IMateriaProvisionRequester, IMateriaSortingRequester, IShlorpReceiver, ICanHaveUnbottledMateriaInInputTray {
    private BlockPos masterPos;
    private GrandFuseryBlockEntity master;
    private DevicePlugDirection plugDirection = DevicePlugDirection.NONE;
    private int packedData;

    public GrandFuseryRouterBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.GRAND_FUSERY_ROUTER_BE.get(), pPos, pBlockState);

        final GrandFuseryRouterType routerType = GrandFuseryRouterBlock.unmapRouterTypeFromInt(getBlockState().getValue(ROUTER_TYPE_GRAND_FUSERY));
        for (Triplet<BlockPos, GrandFuseryRouterType, DevicePlugDirection> data : GrandFuseryBlock.getRouterOffsets(pBlockState.getValue(BlockStateProperties.HORIZONTAL_FACING))) {
            if(data.getSecond() == routerType) {
                this.masterPos = pPos.offset(data.getFirst().multiply(-1));
                this.plugDirection = data.getThird();
            }
        }
    }

    public Direction getFacing() {
        return getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public GrandFuseryRouterType getRouterType() {
        return GrandFuseryRouterBlock.unmapRouterTypeFromInt(getBlockState().getValue(ROUTER_TYPE_GRAND_FUSERY));
    }

    public DevicePlugDirection getPlugDirection() {
        GrandFuseryRouterType type = GrandFuseryRouterBlock.unmapRouterTypeFromInt(getBlockState().getValue(ROUTER_TYPE_GRAND_FUSERY));

        if(type == GrandFuseryRouterType.PLUG_MID_LEFT || type == GrandFuseryRouterType.PLUG_MID_RIGHT){
            if(getBlockState().getValue(HAS_LABORATORY_UPGRADE)) {
                return this.plugDirection;
            } else {
                return DevicePlugDirection.NONE;
            }
        } else {
            return this.plugDirection;
        }
    }

    public BlockEntity getPlugEntity() {
        BlockPos target = getBlockPos();

        if(getPlugDirection() == DevicePlugDirection.NORTH) target = target.north();
        else if(getPlugDirection() == DevicePlugDirection.EAST) target = target.east();
        else if(getPlugDirection() == DevicePlugDirection.SOUTH) target = target.south();
        else if(getPlugDirection() == DevicePlugDirection.WEST) target = target.west();

        return getLevel().getBlockEntity(target);
    }

    @Override
    public void linkPlugins() {
        getMaster().linkPlugins();
    }

    @Override
    public void removePlugin(AbstractDirectionalPluginBlockEntity pPlugin) {
        getMaster().removePlugin(pPlugin);
    }

    @Override
    public void linkPluginsDeferred() {
        getMaster().linkPluginsDeferred();
    }

    @Override
    public List<AbstractDirectionalPluginBlockEntity> getPlugins() {
        if(master == null) {
            if (masterPos != null)
                master = (GrandFuseryBlockEntity) getLevel().getBlockEntity(masterPos);
        }
        if(master == null)
            return new ArrayList<AbstractDirectionalPluginBlockEntity>();

        return master.getPlugins();
    }

    public GrandFuseryBlockEntity getMaster(){
        if(master == null || masterPos == null) {
            final GrandFuseryRouterType routerType = GrandFuseryRouterBlock.unmapRouterTypeFromInt(getBlockState().getValue(ROUTER_TYPE_GRAND_FUSERY));
            for (Triplet<BlockPos, GrandFuseryRouterType, DevicePlugDirection> query : GrandFuseryBlock.getRouterOffsets(getBlockState().getValue(FACING))) {
                if(routerType == query.getSecond()) {
                    BlockPos offset = query.getFirst().multiply(-1);
                    BlockPos target = getBlockPos().offset(offset);
                    BlockEntity be = level.getBlockEntity(target);
                    if(be instanceof GrandFuseryBlockEntity Fusery) {
                        masterPos = Fusery.getBlockPos();
                        master = Fusery;
                        return master;
                    }
                }
            }
        }

        return master;
    }

    public BlockPos getMasterPos() {
        return masterPos;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.grand_fusery");
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return (masterPos == null || getMaster() == null) ? LazyOptional.empty() : getMaster().getCapability(cap, side);
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
        nbt.putInt("plugDirection", mapPlugDirToInt(plugDirection));
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        masterPos = BlockPos.of(nbt.getLong("masterPos"));
        plugDirection = unmapPlugDirFromInt(nbt.getInt("plugDirection"));
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
        nbt.putInt("plugDirection", mapPlugDirToInt(plugDirection));

        return nbt;
    }

    private int mapPlugDirToInt(DevicePlugDirection pPlugDirection) {
        if(pPlugDirection == null)
            return 0;

        return switch(pPlugDirection) {
            case NORTH -> 1;
            case SOUTH -> 2;
            case EAST -> 3;
            case WEST -> 4;
            default -> 0;
        };
    }

    private DevicePlugDirection unmapPlugDirFromInt(int pBitpack) {
        return switch(pBitpack) {
            case 1 -> DevicePlugDirection.NORTH;
            case 2 -> DevicePlugDirection.SOUTH;
            case 3 -> DevicePlugDirection.EAST;
            case 4 -> DevicePlugDirection.WEST;
            default -> DevicePlugDirection.NONE;
        };
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return getMaster().createMenu(pContainerId, pPlayerInventory, pPlayer);
    }

    @Override
    public int getMaximumGrime() {
        return getMaster().getMaximumGrime();
    }

    @Override
    public int clean() {
        return getMaster().clean();
    }

    @Override
    public void destroyMaster() {
        if(getBlockState().getValue(HAS_LABORATORY_UPGRADE)) {
            ItemStack charmStack = new ItemStack(ItemRegistry.LABORATORY_CHARM.get());
            ItemEntity ie = new ItemEntity(getLevel(), getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), charmStack);
            getLevel().addFreshEntity(ie);
        }

        getLevel().destroyBlock(getMasterPos(), true);
        GrandFuseryBlock.destroyRouters(getLevel(), getMasterPos(), getFacing());
    }

    @Override
    public boolean allowIncreasedDeliverySize() {
        if(getMaster() == null)
            return false;
        return getMaster().allowIncreasedDeliverySize();
    }

    @Override
    public boolean needsProvisioning() {
        if(getMaster() == null)
            return false;
        return getMaster().needsProvisioning();
    }

    @Override
    public Map<MateriaItem, Integer> getProvisioningNeeds() {
        if(getMaster() == null)
            return new HashMap<>();
        return getMaster().getProvisioningNeeds();
    }

    @Override
    public void setProvisioningInProgress(MateriaItem pMateriaItem) {
        if(getMaster() != null)
            getMaster().setProvisioningInProgress(pMateriaItem);
    }

    @Override
    public void cancelProvisioningInProgress(MateriaItem pMateriaItem) {
        if(getMaster() != null)
            getMaster().cancelProvisioningInProgress(pMateriaItem);
    }

    @Override
    public void provide(ItemStack pStack) {
        if(getMaster() != null)
            getMaster().provide(pStack);
    }

    @Override
    public boolean needsSorting() {
        if(master == null) return false;

        return master.needsSorting();
    }

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        if(master != null || masterPos != null) {
            if(getMaster() == null) return 0;
            return master.canAcceptStackFromShlorp(pStack);
        }

        return 0;
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        if(master != null || masterPos != null) {
            if(getMaster() == null) return 0;
            return master.insertStackFromShlorp(pStack);
        }

        return 0;
    }

    @Override
    public ItemStack tryExtractUnbottled(ItemStack pBottlesInHand) {
        if(masterPos == null)
            return ItemStack.EMPTY;

        return getMaster().tryExtractUnbottled(pBottlesInHand);
    }

    @Override
    public boolean isClogged() {
        if(masterPos == null)
            return false;

        return getMaster().isClogged();
    }
}

