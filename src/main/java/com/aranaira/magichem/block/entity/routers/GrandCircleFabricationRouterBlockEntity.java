package com.aranaira.magichem.block.entity.routers;

import com.aranaira.magichem.block.CirclePowerBlock;
import com.aranaira.magichem.block.GrandCircleFabricationBlock;
import com.aranaira.magichem.block.entity.GrandCircleFabricationBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractDirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.*;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.mna.items.base.INoCreativeTab;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.ROUTER_TYPE_GRAND_CIRCLE_FABRICATION;

public class GrandCircleFabricationRouterBlockEntity extends BlockEntity implements MenuProvider, IRouterBlockEntity, INoCreativeTab, ICanTakePlugins, IDestroysMasterOnDestruction, IMateriaProvisionRequester, IMateriaSortingRequester, IShlorpReceiver, IHasDeviceRecipeSlot, ICanHaveUnbottledMateriaInInputTray {
    private BlockPos masterPos;
    private GrandCircleFabricationBlockEntity master;
    private DevicePlugDirection plugDirection;

    public GrandCircleFabricationRouterBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.GRAND_CIRCLE_FABRICATION_ROUTER_BE.get(), pPos, pBlockState);

        final int routerType = getBlockState().getValue(ROUTER_TYPE_GRAND_CIRCLE_FABRICATION);
        for (Triplet<BlockPos, Integer, DevicePlugDirection> data : GrandCircleFabricationBlock.getRouterOffsets(pBlockState.getValue(BlockStateProperties.HORIZONTAL_FACING))) {
            if(data.getSecond() == routerType) {
                this.masterPos = pPos.offset(data.getFirst().multiply(-1));
                this.plugDirection = data.getThird();
            }
        }
    }

    public GrandCircleFabricationBlockEntity getMaster(){
        if(master == null || masterPos == null) {
            final int routerType = getBlockState().getValue(ROUTER_TYPE_GRAND_CIRCLE_FABRICATION);
            for (Triplet<BlockPos, Integer, DevicePlugDirection> query : GrandCircleFabricationBlock.getRouterOffsets(getBlockState().getValue(FACING))) {
                if(routerType == query.getSecond()) {
                    BlockPos offset = query.getFirst().multiply(-1);
                    BlockPos target = getBlockPos().offset(offset);
                    BlockEntity be = level.getBlockEntity(target);
                    if(be instanceof GrandCircleFabricationBlockEntity circle) {
                        masterPos = circle.getBlockPos();
                        master = circle;
                        return master;
                    }
                }
            }
        }

        return master;
    }

    @Override
    public Direction getFacing() {
        return null;
    }

    public BlockPos getMasterPos() {
        return masterPos;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.circle_fabrication");
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
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        masterPos = BlockPos.of(nbt.getLong("masterPos"));
        findPlugDirectionFromBlockState();
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

        return nbt;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return getMaster().createMenu(pContainerId, pPlayerInventory, pPlayer);
    }

    @Override
    public void destroyMaster() {
        getLevel().destroyBlock(getMasterPos(), true);
        CirclePowerBlock.destroyRouters(getLevel(), getMasterPos(), null);
    }

    @Override
    public void linkPluginsDeferred() {
        getMaster().linkPluginsDeferred();
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
    public List<AbstractDirectionalPluginBlockEntity> getPlugins() {
        if(master == null) {
            if (masterPos != null)
                master = (GrandCircleFabricationBlockEntity) getLevel().getBlockEntity(masterPos);
        }
        if(master == null)
            return new ArrayList<AbstractDirectionalPluginBlockEntity>();

        return master.getPlugins();
    }

    public DevicePlugDirection getPlugDirection() {
        return this.plugDirection;
    }

    public BlockEntity getPlugEntity() {
        BlockPos target = getBlockPos();

        if(getPlugDirection() == DevicePlugDirection.NORTH) target = target.north();
        else if(getPlugDirection() == DevicePlugDirection.EAST) target = target.east();
        else if(getPlugDirection() == DevicePlugDirection.SOUTH) target = target.south();
        else if(getPlugDirection() == DevicePlugDirection.WEST) target = target.west();

        return getLevel().getBlockEntity(target);
    }

    public void findPlugDirectionFromBlockState() {
        final Direction facing = getBlockState().getValue(FACING);
        final int type = getBlockState().getValue(ROUTER_TYPE_GRAND_CIRCLE_FABRICATION);

        if(type == 6) {
            if(facing == Direction.NORTH) plugDirection = DevicePlugDirection.WEST;
            else if(facing == Direction.EAST) plugDirection = DevicePlugDirection.NORTH;
            else if(facing == Direction.SOUTH) plugDirection = DevicePlugDirection.EAST;
            else if(facing == Direction.WEST) plugDirection = DevicePlugDirection.SOUTH;
        } else if(type == 2) {
            if(facing == Direction.NORTH) plugDirection = DevicePlugDirection.EAST;
            else if(facing == Direction.EAST) plugDirection = DevicePlugDirection.SOUTH;
            else if(facing == Direction.SOUTH) plugDirection = DevicePlugDirection.WEST;
            else if(facing == Direction.WEST) plugDirection = DevicePlugDirection.NORTH;
        }
    }

    @Override
    public boolean allowIncreasedDeliverySize() {
        return getMaster().allowIncreasedDeliverySize();
    }

    @Override
    public boolean needsProvisioning() {
        return getMaster().needsProvisioning();
    }

    @Override
    public Map<MateriaItem, Integer> getProvisioningNeeds() {
        return getMaster().getProvisioningNeeds();
    }

    @Override
    public void setProvisioningInProgress(MateriaItem pMateriaItem) {
        getMaster().setProvisioningInProgress(pMateriaItem);
    }

    @Override
    public void cancelProvisioningInProgress(MateriaItem pMateriaItem) {
        getMaster().cancelProvisioningInProgress(pMateriaItem);
    }

    @Override
    public void provide(ItemStack pStack) {
        getMaster().provide(pStack);
    }

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        return getMaster().canAcceptStackFromShlorp(pStack);
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        return getMaster().insertStackFromShlorp(pStack);
    }

    @Override
    public byte setRecipe(ItemStack pStack, Player player) {
        if(masterPos == null)
            return ERROR_CODE_NO_BLOCK_ENTITY;

        return getMaster().setRecipe(pStack, player);
    }

    @Override
    public ItemStack getRecipeItem() {
        if(masterPos == null)
            return null;

        return getMaster().getRecipeItem();
    }

    @Override
    public ItemStack getRecipeItem(boolean pMakeCopy) {
        if(masterPos == null)
            return null;

        return getMaster().getRecipeItem(pMakeCopy);
    }

    @Override
    public boolean needsSorting() {
        if(master == null) return false;

        return master.needsSorting();
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
