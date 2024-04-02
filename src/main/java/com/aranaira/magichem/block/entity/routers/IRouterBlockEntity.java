package com.aranaira.magichem.block.entity.routers;

import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IRouterBlockEntity {

    BlockEntity getMaster();

    Direction getFacing();

    DevicePlugDirection getPlugDirection();

}
