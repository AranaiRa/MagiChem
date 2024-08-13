package com.aranaira.magichem.foundation;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class MagiChemBlockStateProperties {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final DirectionProperty FACING_OMNI = BlockStateProperties.FACING;

    public static final BooleanProperty GROUNDED = BooleanProperty.create("grounded");
    public static final BooleanProperty HAS_LABORATORY_UPGRADE = BooleanProperty.create("has_laboratory_upgrade");
    public static final BooleanProperty HAS_PASSIVE_HEAT = BooleanProperty.create("has_passive_heat");
    public static final BooleanProperty IS_EMITTING_LIGHT = BooleanProperty.create("is_emitting_light");
    public static final BooleanProperty STACKED = BooleanProperty.create("stacked");

    public static final IntegerProperty ACTUATOR_ELEMENT = IntegerProperty.create("element", 0, 6);
    public static final IntegerProperty ROUTER_TYPE_ALCHEMICAL_NEXUS = IntegerProperty.create("alchemical_nexus_router_type", 0, 9);
    public static final IntegerProperty ROUTER_TYPE_CENTRIFUGE = IntegerProperty.create("centrifuge_router_type", 0, 3);
    public static final IntegerProperty ROUTER_TYPE_DISTILLERY = IntegerProperty.create("distillery_router_type", 0, 3);
    public static final IntegerProperty ROUTER_TYPE_FUSERY = IntegerProperty.create("fusery_router_type", 0, 5);
    public static final IntegerProperty ROUTER_TYPE_GRAND_DISTILLERY = IntegerProperty.create("grand_distillery_router_type", 0, 18);
    public static final IntegerProperty USER_TIER_TYPE = IntegerProperty.create("user_tier_type", 0, 5);
}
