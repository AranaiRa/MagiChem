package com.aranaira.magichem.block.entity.routers;

import com.aranaira.magichem.block.PrimeAggregatorBlock;
import com.aranaira.magichem.block.PrimeAggregatorRouterBlock;
import com.aranaira.magichem.block.entity.PrimeAggregatorBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractBlockEntityWithEfficiency;
import com.aranaira.magichem.block.entity.ext.AbstractDirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.*;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.foundation.enums.PrimeAggregatorRouterType;
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

import java.util.HashMap;
import java.util.Map;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.*;

public class PrimeAggregatorRouterBlockEntity extends BlockEntity implements MenuProvider, INoCreativeTab, ICanTakePlugins, IRouterBlockEntity, IPoweredAlchemyDevice, IMateriaProvisionRequester, IShlorpReceiver, IHasDeviceRecipeSlot, IDestroysMasterOnDestruction {
    private BlockPos masterPos;
    private PrimeAggregatorBlockEntity master;
    private DevicePlugDirection plugDirection = DevicePlugDirection.NONE;
    private int packedData;

    public PrimeAggregatorRouterBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.PRIME_AGGREGATOR_ROUTER_BE.get(), pPos, pBlockState);

        final PrimeAggregatorRouterType routerType = PrimeAggregatorRouterBlock.unmapRouterTypeFromInt(getBlockState().getValue(ROUTER_TYPE_PRIME_AGGREGATOR));
        for (Triplet<BlockPos, PrimeAggregatorRouterType, DevicePlugDirection> data : PrimeAggregatorBlock.getRouterOffsets(pBlockState.getValue(BlockStateProperties.HORIZONTAL_FACING))) {
            if(data.getSecond() == routerType) {
                this.masterPos = pPos.offset(data.getFirst().multiply(-1));
                this.plugDirection = data.getThird();
            }
        }
    }

    public Direction getFacing() {
        return getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public PrimeAggregatorRouterType getRouterType() {
        return PrimeAggregatorRouterBlock.unmapRouterTypeFromInt(getBlockState().getValue(ROUTER_TYPE_GRAND_CENTRIFUGE));
    }

    public DevicePlugDirection getPlugDirection() {
        PrimeAggregatorRouterType type = PrimeAggregatorRouterBlock.unmapRouterTypeFromInt(getBlockState().getValue(ROUTER_TYPE_GRAND_CENTRIFUGE));

        if(type == PrimeAggregatorRouterType.PLUG_LEFT || type == PrimeAggregatorRouterType.PLUG_RIGHT){
            return this.plugDirection;
        } else {
            return DevicePlugDirection.NONE;
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

    public PrimeAggregatorBlockEntity getMaster(){
        if(master == null || masterPos == null) {
            final PrimeAggregatorRouterType routerType = PrimeAggregatorRouterBlock.unmapRouterTypeFromInt(getBlockState().getValue(ROUTER_TYPE_PRIME_AGGREGATOR));
            for (Triplet<BlockPos, PrimeAggregatorRouterType, DevicePlugDirection> query : PrimeAggregatorBlock.getRouterOffsets(getBlockState().getValue(FACING))) {
                if(routerType == query.getSecond()) {
                    BlockPos offset = query.getFirst().multiply(-1);
                    BlockPos target = getBlockPos().offset(offset);
                    BlockEntity be = level.getBlockEntity(target);
                    if(be instanceof PrimeAggregatorBlockEntity aggregator) {
                        masterPos = aggregator.getBlockPos();
                        master = aggregator;
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
        return Component.translatable("block.magichem.prime_aggregator");
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
    public void destroyMaster() {
        getLevel().destroyBlock(getMasterPos(), true);
        PrimeAggregatorBlock.destroyRouters(getLevel(), getMasterPos(), getFacing());
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
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        if(getMaster() == null) return 0;

        return master.canAcceptStackFromShlorp(pStack);
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        if(getMaster() == null) return 0;

        return master.insertStackFromShlorp(pStack);
    }

    @Override
    public byte setRecipe(ItemStack pStack, Player player) {
        return getMaster().setRecipe(pStack, player);
    }

    @Override
    public ItemStack getRecipeItem() {
        return getMaster().getRecipeItem();
    }

    @Override
    public ItemStack getRecipeItem(boolean pMakeCopy) {
        return getMaster().getRecipeItem(pMakeCopy);
    }
}
