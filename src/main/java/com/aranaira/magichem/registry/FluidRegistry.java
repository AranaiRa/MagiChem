package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.fluid.AcidFluidBlock;
import com.aranaira.magichem.block.fluid.LiquidLightFluidBlock;
import com.aranaira.magichem.fluid.AcidFluidType;
import com.aranaira.magichem.registry.compat.OccultismFluidRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

public class FluidRegistry {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, MagiChemMod.MODID);
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, MagiChemMod.MODID);

    //////////////////////
    //-----ACADEMIC SLURRY
    //////////////////////
    public static final RegistryObject<FluidType> ACADEMIC_SLURRY_FLUID_TYPE = FLUID_TYPES.register("academic_slurry_fluid_type", () ->
            new FluidType(FluidType.Properties.create().descriptionId("academic_slurry_fluid_type")
                    .canExtinguish(true).canConvertToSource(false)
                    .supportsBoating(true).canHydrate(false).viscosity(12)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        public static final ResourceLocation FLUID_STILL = new ResourceLocation(MagiChemMod.MODID, "block/fluid/experience_still");
                        public static final ResourceLocation FLUID_FLOWING = new ResourceLocation(MagiChemMod.MODID, "block/fluid/experience_flow");

                        @Override
                        public ResourceLocation getStillTexture() {
                            return FLUID_STILL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return FLUID_FLOWING;
                        }
                    });
                }
            });
    public static final RegistryObject<FlowingFluid> ACADEMIC_SLURRY = FLUIDS.register("academic_slurry", () -> new ForgeFlowingFluid.Source(getAcademicSlurryProperties()));
    public static final RegistryObject<Fluid> ACADEMIC_SLURRY_FLOWING = FLUIDS.register("academic_slurry_flowing", () -> new ForgeFlowingFluid.Flowing(getAcademicSlurryProperties()));
    public static final RegistryObject<LiquidBlock> ACADEMIC_SLURRY_BLOCK = BlockRegistry.BLOCKS.register("academic_slurry_block", () -> new LiquidBlock(ACADEMIC_SLURRY, BlockBehaviour.Properties.copy(Blocks.WATER)));

    public static ForgeFlowingFluid.Properties getAcademicSlurryProperties() {
        return new ForgeFlowingFluid.Properties(ACADEMIC_SLURRY_FLUID_TYPE, ACADEMIC_SLURRY, ACADEMIC_SLURRY_FLOWING).block(ACADEMIC_SLURRY_BLOCK).bucket(ItemRegistry.ACADEMIC_SLURRY_BUCKET);
    }


    //////////////////////
    //-----STEAM
    //////////////////////

    public static final RegistryObject<FluidType> STEAM_FLUID_TYPE = FLUID_TYPES.register("steam_fluid_type", () ->
            new FluidType(FluidType.Properties.create().descriptionId("steam_fluid_type")
                    .canExtinguish(true).canConvertToSource(false)
                    .supportsBoating(true).canHydrate(false).viscosity(0).canPushEntity(false)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        public static final ResourceLocation FLUID_STILL = new ResourceLocation(MagiChemMod.MODID, "block/fluid/steam_still");
                        public static final ResourceLocation FLUID_FLOWING = new ResourceLocation(MagiChemMod.MODID, "block/fluid/steam_flow");

                        @Override
                        public ResourceLocation getStillTexture() {
                            return FLUID_STILL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return FLUID_FLOWING;
                        }
                    });
                }
            });
    public static final RegistryObject<FlowingFluid> STEAM = FLUIDS.register("steam", () -> new ForgeFlowingFluid.Source(getSteamProperties()));
    public static final RegistryObject<Fluid> STEAM_FLOWING = FLUIDS.register("steam_flowing", () -> new ForgeFlowingFluid.Flowing(getSteamProperties()));
    public static final RegistryObject<LiquidBlock> STEAM_BLOCK = BlockRegistry.BLOCKS.register("steam_block", () -> new LiquidBlock(STEAM, BlockBehaviour.Properties.copy(Blocks.WATER)));

    public static ForgeFlowingFluid.Properties getSteamProperties() {
        return new ForgeFlowingFluid.Properties(STEAM_FLUID_TYPE, STEAM, STEAM_FLOWING).block(STEAM_BLOCK).bucket(ItemRegistry.STEAM_BUCKET);
    }

    //////////////////////
    //-----SMOKE
    //////////////////////

    public static final RegistryObject<FluidType> SMOKE_FLUID_TYPE = FLUID_TYPES.register("smoke_fluid_type", () ->
            new FluidType(FluidType.Properties.create().descriptionId("smoke_fluid_type")
                    .canExtinguish(true).canConvertToSource(false)
                    .supportsBoating(true).canHydrate(false).viscosity(1).canPushEntity(false)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        public static final ResourceLocation FLUID_STILL = new ResourceLocation(MagiChemMod.MODID, "block/fluid/smoke_still");
                        public static final ResourceLocation FLUID_FLOWING = new ResourceLocation(MagiChemMod.MODID, "block/fluid/smoke_flow");

                        @Override
                        public ResourceLocation getStillTexture() {
                            return FLUID_STILL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return FLUID_FLOWING;
                        }
                    });
                }
            });
    public static final RegistryObject<FlowingFluid> SMOKE = FLUIDS.register("smoke", () -> new ForgeFlowingFluid.Source(getSmokeProperties()));
    public static final RegistryObject<Fluid> SMOKE_FLOWING = FLUIDS.register("smoke_flowing", () -> new ForgeFlowingFluid.Flowing(getSmokeProperties()));
    public static final RegistryObject<LiquidBlock> SMOKE_BLOCK = BlockRegistry.BLOCKS.register("smoke_block", () -> new LiquidBlock(SMOKE, BlockBehaviour.Properties.copy(Blocks.WATER)));

    public static ForgeFlowingFluid.Properties getSmokeProperties() {
        return new ForgeFlowingFluid.Properties(SMOKE_FLUID_TYPE, SMOKE, SMOKE_FLOWING).block(SMOKE_BLOCK).bucket(ItemRegistry.SMOKE_BUCKET);
    }

    //////////////////////
    //-----LIQUID LIGHT
    //////////////////////

    public static final RegistryObject<FluidType> LIQUID_LIGHT_FLUID_TYPE = FLUID_TYPES.register("liquid_light_fluid_type", () ->
            new FluidType(FluidType.Properties.create().descriptionId("liquid_light_fluid_type")
                    .canExtinguish(true).canConvertToSource(false)
                    .supportsBoating(true).canHydrate(false).viscosity(0).canPushEntity(false)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        public static final ResourceLocation FLUID_STILL = new ResourceLocation(MagiChemMod.MODID, "block/fluid/liquid_light_still");
                        public static final ResourceLocation FLUID_FLOWING = new ResourceLocation(MagiChemMod.MODID, "block/fluid/liquid_light_flow");

                        @Override
                        public ResourceLocation getStillTexture() {
                            return FLUID_STILL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return FLUID_FLOWING;
                        }
                    });
                }
            });
    public static final RegistryObject<FlowingFluid> LIQUID_LIGHT = FLUIDS.register("liquid_light", () -> new ForgeFlowingFluid.Source(getLiquidLightProperties()));
    public static final RegistryObject<Fluid> LIQUID_LIGHT_FLOWING = FLUIDS.register("liquid_light_flowing", () -> new ForgeFlowingFluid.Flowing(getLiquidLightProperties()));
    public static final RegistryObject<LiquidBlock> LIQUID_LIGHT_BLOCK = BlockRegistry.BLOCKS.register("liquid_light_block", () -> new LiquidLightFluidBlock(LIQUID_LIGHT.get(), BlockBehaviour.Properties.of()));

    public static ForgeFlowingFluid.Properties getLiquidLightProperties() {
        return new ForgeFlowingFluid.Properties(LIQUID_LIGHT_FLUID_TYPE, LIQUID_LIGHT, LIQUID_LIGHT_FLOWING).block(LIQUID_LIGHT_BLOCK).bucket(ItemRegistry.LIQUID_LIGHT_BUCKET);
    }

    //////////////////////
    //-----AQUA VITAE
    //////////////////////

    public static final RegistryObject<FluidType> AQUA_VITAE_FLUID_TYPE = FLUID_TYPES.register("aqua_vitae_fluid_type", () ->
            new FluidType(FluidType.Properties.create().descriptionId("aqua_vitae_fluid_type")
                    .canExtinguish(true).canConvertToSource(false)
                    .supportsBoating(true).canHydrate(false).viscosity(1).canPushEntity(false)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        public static final ResourceLocation FLUID_STILL = new ResourceLocation(MagiChemMod.MODID, "block/fluid/aqua_vitae_still");
                        public static final ResourceLocation FLUID_FLOWING = new ResourceLocation(MagiChemMod.MODID, "block/fluid/aqua_vitae_flow");

                        @Override
                        public ResourceLocation getStillTexture() {
                            return FLUID_STILL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return FLUID_FLOWING;
                        }
                    });
                }
            });
    public static final RegistryObject<FlowingFluid> AQUA_VITAE = FLUIDS.register("aqua_vitae", () -> new ForgeFlowingFluid.Source(getAquaVitaeProperties()));
    public static final RegistryObject<Fluid> AQUA_VITAE_FLOWING = FLUIDS.register("aqua_vitae_flowing", () -> new ForgeFlowingFluid.Flowing(getAquaVitaeProperties()));
    public static final RegistryObject<LiquidBlock> AQUA_VITAE_BLOCK = BlockRegistry.BLOCKS.register("aqua_vitae_block", () -> new LiquidBlock(AQUA_VITAE, BlockBehaviour.Properties.copy(Blocks.WATER)));

    public static ForgeFlowingFluid.Properties getAquaVitaeProperties() {
        return new ForgeFlowingFluid.Properties(AQUA_VITAE_FLUID_TYPE, AQUA_VITAE, AQUA_VITAE_FLOWING).block(AQUA_VITAE_BLOCK).bucket(ItemRegistry.AQUA_VITAE_BUCKET);
    }

    //////////////////////
    //-----WINE COMPONENTS
    //////////////////////

    public static final RegistryObject<FluidType> SWEETBERRY_WINE_FLUID_TYPE = FLUID_TYPES.register("sweetberry_wine_fluid_type", () ->
            new FluidType(FluidType.Properties.create().descriptionId("sweetberry_wine_fluid_type")
                    .canExtinguish(true).canConvertToSource(false)
                    .supportsBoating(true).canHydrate(false).viscosity(1).canPushEntity(false)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        public static final ResourceLocation FLUID_STILL = new ResourceLocation(MagiChemMod.MODID, "block/fluid/sweetberry_wine_still");
                        public static final ResourceLocation FLUID_FLOWING = new ResourceLocation(MagiChemMod.MODID, "block/fluid/sweetberry_wine_flow");

                        @Override
                        public ResourceLocation getStillTexture() {
                            return FLUID_STILL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return FLUID_FLOWING;
                        }
                    });
                }
            });
    public static final RegistryObject<FlowingFluid> SWEETBERRY_WINE = FLUIDS.register("sweetberry_wine", () -> new ForgeFlowingFluid.Source(getSweetberryWineProperties()));
    public static final RegistryObject<Fluid> SWEETBERRY_WINE_FLOWING = FLUIDS.register("sweetberry_wine_flowing", () -> new ForgeFlowingFluid.Flowing(getSweetberryWineProperties()));
    public static final RegistryObject<LiquidBlock> SWEETBERRY_WINE_BLOCK = BlockRegistry.BLOCKS.register("sweetberry_wine_block", () -> new LiquidBlock(SWEETBERRY_WINE, BlockBehaviour.Properties.copy(Blocks.WATER)));

    public static ForgeFlowingFluid.Properties getSweetberryWineProperties() {
        return new ForgeFlowingFluid.Properties(SWEETBERRY_WINE_FLUID_TYPE, SWEETBERRY_WINE, SWEETBERRY_WINE_FLOWING).block(SWEETBERRY_WINE_BLOCK).bucket(ItemRegistry.SWEETBERRY_WINE_BUCKET);
    }

    public static final RegistryObject<FluidType> SHIMMERING_WINE_FLUID_TYPE = FLUID_TYPES.register("shimmering_wine_fluid_type", () ->
            new FluidType(FluidType.Properties.create().descriptionId("shimmering_wine_fluid_type")
                    .canExtinguish(true).canConvertToSource(false)
                    .supportsBoating(true).canHydrate(false).viscosity(1).canPushEntity(false)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        public static final ResourceLocation FLUID_STILL = new ResourceLocation(MagiChemMod.MODID, "block/fluid/shimmering_wine_still");
                        public static final ResourceLocation FLUID_FLOWING = new ResourceLocation(MagiChemMod.MODID, "block/fluid/shimmering_wine_flow");

                        @Override
                        public ResourceLocation getStillTexture() {
                            return FLUID_STILL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return FLUID_FLOWING;
                        }
                    });
                }
            });
    public static final RegistryObject<FlowingFluid> SHIMMERING_WINE = FLUIDS.register("shimmering_wine", () -> new ForgeFlowingFluid.Source(getShimmeringWineProperties()));
    public static final RegistryObject<Fluid> SHIMMERING_WINE_FLOWING = FLUIDS.register("shimmering_wine_flowing", () -> new ForgeFlowingFluid.Flowing(getShimmeringWineProperties()));
    public static final RegistryObject<LiquidBlock> SHIMMERING_WINE_BLOCK = BlockRegistry.BLOCKS.register("shimmering_wine_block", () -> new LiquidBlock(SHIMMERING_WINE, BlockBehaviour.Properties.copy(Blocks.WATER)));

    public static ForgeFlowingFluid.Properties getShimmeringWineProperties() {
        return new ForgeFlowingFluid.Properties(SHIMMERING_WINE_FLUID_TYPE, SHIMMERING_WINE, SHIMMERING_WINE_FLOWING).block(SHIMMERING_WINE_BLOCK).bucket(ItemRegistry.SHIMMERING_WINE_BUCKET);
    }

    //////////////////////
    //-----LIQUEFACTED METALS
    //////////////////////

    //Copper
    public static final RegistryObject<FluidType> LIQUEFACTED_COPPER_FLUID_TYPE = FLUID_TYPES.register("liquefacted_copper_fluid_type", () ->
            new FluidType(FluidType.Properties.create().descriptionId("liquefacted_copper_fluid_type")
                    .canExtinguish(true).canConvertToSource(false)
                    .supportsBoating(true).canHydrate(false).viscosity(0).canPushEntity(false)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        public static final ResourceLocation FLUID_STILL = new ResourceLocation(MagiChemMod.MODID, "block/fluid/liquefacted_metal_still");
                        public static final ResourceLocation FLUID_FLOWING = new ResourceLocation(MagiChemMod.MODID, "block/fluid/liquefacted_metal_still");

                        @Override
                        public ResourceLocation getStillTexture() {
                            return FLUID_STILL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return FLUID_FLOWING;
                        }

                        @Override
                        public int getTintColor() {
                            return 0xffc15a36;
                        }
                    });
                }
            });
    public static final RegistryObject<Fluid> LIQUEFACTED_COPPER = FLUIDS.register("liquefacted_copper", () -> new ForgeFlowingFluid.Source(getLiquefactedCopperProperties()));
    public static final RegistryObject<FlowingFluid> LIQUEFACTED_COPPER_FLOWING = FLUIDS.register("liquefacted_copper_flowing", () -> new ForgeFlowingFluid.Flowing(getLiquefactedCopperProperties()));
    public static final RegistryObject<LiquidBlock> LIQUEFACTED_COPPER_BLOCK = BlockRegistry.BLOCKS.register("liquefacted_copper_block", () -> new AcidFluidBlock(LIQUEFACTED_COPPER_FLOWING.get(), BlockBehaviour.Properties.copy(Blocks.LAVA)));

    public static ForgeFlowingFluid.Properties getLiquefactedCopperProperties() {
        return new ForgeFlowingFluid.Properties(LIQUEFACTED_COPPER_FLUID_TYPE, LIQUEFACTED_COPPER, LIQUEFACTED_COPPER_FLOWING).block(LIQUEFACTED_COPPER_BLOCK).bucket(ItemRegistry.LIQUEFACTED_COPPER_BUCKET);
    }

    //Iron
    public static final RegistryObject<FluidType> LIQUEFACTED_IRON_FLUID_TYPE = FLUID_TYPES.register("liquefacted_iron_fluid_type", () ->
            new FluidType(FluidType.Properties.create().descriptionId("liquefacted_iron_fluid_type")
                    .canExtinguish(true).canConvertToSource(false)
                    .supportsBoating(true).canHydrate(false).viscosity(0).canPushEntity(false)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        public static final ResourceLocation FLUID_STILL = new ResourceLocation(MagiChemMod.MODID, "block/fluid/liquefacted_metal_still");
                        public static final ResourceLocation FLUID_FLOWING = new ResourceLocation(MagiChemMod.MODID, "block/fluid/liquefacted_metal_still");

                        @Override
                        public ResourceLocation getStillTexture() {
                            return FLUID_STILL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return FLUID_FLOWING;
                        }

                        @Override
                        public int getTintColor() {
                            return 0xffab8b8b;
                        }
                    });
                }
            });
    public static final RegistryObject<Fluid> LIQUEFACTED_IRON = FLUIDS.register("liquefacted_iron", () -> new ForgeFlowingFluid.Source(getLiquefactedIronProperties()));
    public static final RegistryObject<FlowingFluid> LIQUEFACTED_IRON_FLOWING = FLUIDS.register("liquefacted_iron_flowing", () -> new ForgeFlowingFluid.Flowing(getLiquefactedIronProperties()));
    public static final RegistryObject<LiquidBlock> LIQUEFACTED_IRON_BLOCK = BlockRegistry.BLOCKS.register("liquefacted_iron_block", () -> new AcidFluidBlock(LIQUEFACTED_IRON_FLOWING.get(), BlockBehaviour.Properties.copy(Blocks.LAVA)));

    public static ForgeFlowingFluid.Properties getLiquefactedIronProperties() {
        return new ForgeFlowingFluid.Properties(LIQUEFACTED_IRON_FLUID_TYPE, LIQUEFACTED_IRON, LIQUEFACTED_IRON_FLOWING).block(LIQUEFACTED_IRON_BLOCK).bucket(ItemRegistry.LIQUEFACTED_IRON_BUCKET);
    }

    //Gold
    public static final RegistryObject<FluidType> LIQUEFACTED_GOLD_FLUID_TYPE = FLUID_TYPES.register("liquefacted_gold_fluid_type", () ->
            new FluidType(FluidType.Properties.create().descriptionId("liquefacted_gold_fluid_type")
                    .canExtinguish(true).canConvertToSource(false)
                    .supportsBoating(true).canHydrate(false).viscosity(0).canPushEntity(false)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        public static final ResourceLocation FLUID_STILL = new ResourceLocation(MagiChemMod.MODID, "block/fluid/liquefacted_metal_still");
                        public static final ResourceLocation FLUID_FLOWING = new ResourceLocation(MagiChemMod.MODID, "block/fluid/liquefacted_metal_still");

                        @Override
                        public ResourceLocation getStillTexture() {
                            return FLUID_STILL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return FLUID_FLOWING;
                        }

                        @Override
                        public int getTintColor() {
                            return 0xffefcd56;
                        }
                    });
                }
            });
    public static final RegistryObject<Fluid> LIQUEFACTED_GOLD = FLUIDS.register("liquefacted_gold", () -> new ForgeFlowingFluid.Source(getLiquefactedGoldProperties()));
    public static final RegistryObject<FlowingFluid> LIQUEFACTED_GOLD_FLOWING = FLUIDS.register("liquefacted_gold_flowing", () -> new ForgeFlowingFluid.Flowing(getLiquefactedGoldProperties()));
    public static final RegistryObject<LiquidBlock> LIQUEFACTED_GOLD_BLOCK = BlockRegistry.BLOCKS.register("liquefacted_gold_block", () -> new AcidFluidBlock(LIQUEFACTED_GOLD_FLOWING.get(), BlockBehaviour.Properties.copy(Blocks.LAVA)));

    public static ForgeFlowingFluid.Properties getLiquefactedGoldProperties() {
        return new ForgeFlowingFluid.Properties(LIQUEFACTED_GOLD_FLUID_TYPE, LIQUEFACTED_GOLD, LIQUEFACTED_GOLD_FLOWING).block(LIQUEFACTED_GOLD_BLOCK).bucket(ItemRegistry.LIQUEFACTED_GOLD_BUCKET);
    }

    //Debris
    public static final RegistryObject<FluidType> LIQUEFACTED_DEBRIS_FLUID_TYPE = FLUID_TYPES.register("liquefacted_debris_fluid_type", () ->
            new FluidType(FluidType.Properties.create().descriptionId("liquefacted_debris_fluid_type")
                    .canExtinguish(true).canConvertToSource(false)
                    .supportsBoating(true).canHydrate(false).viscosity(0).canPushEntity(false)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        public static final ResourceLocation FLUID_STILL = new ResourceLocation(MagiChemMod.MODID, "block/fluid/liquefacted_metal_still");
                        public static final ResourceLocation FLUID_FLOWING = new ResourceLocation(MagiChemMod.MODID, "block/fluid/liquefacted_metal_still");

                        @Override
                        public ResourceLocation getStillTexture() {
                            return FLUID_STILL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return FLUID_FLOWING;
                        }

                        @Override
                        public int getTintColor() {
                            return 0xff4f2920;
                        }
                    });
                }
            });
    public static final RegistryObject<Fluid> LIQUEFACTED_DEBRIS = FLUIDS.register("liquefacted_debris", () -> new ForgeFlowingFluid.Source(getLiquefactedDebrisProperties()));
    public static final RegistryObject<FlowingFluid> LIQUEFACTED_DEBRIS_FLOWING = FLUIDS.register("liquefacted_debris_flowing", () -> new ForgeFlowingFluid.Flowing(getLiquefactedDebrisProperties()));
    public static final RegistryObject<LiquidBlock> LIQUEFACTED_DEBRIS_BLOCK = BlockRegistry.BLOCKS.register("liquefacted_debris_block", () -> new AcidFluidBlock(LIQUEFACTED_DEBRIS_FLOWING.get(), BlockBehaviour.Properties.copy(Blocks.LAVA)));

    public static ForgeFlowingFluid.Properties getLiquefactedDebrisProperties() {
        return new ForgeFlowingFluid.Properties(LIQUEFACTED_DEBRIS_FLUID_TYPE, LIQUEFACTED_DEBRIS, LIQUEFACTED_DEBRIS_FLOWING).block(LIQUEFACTED_DEBRIS_BLOCK).bucket(ItemRegistry.LIQUEFACTED_DEBRIS_BUCKET);
    }

    //////////////////////
    //-----ACIDS
    //////////////////////

    //Simple Acid
    public static final RegistryObject<AcidFluidType> SIMPLE_ACID_FLUID_TYPE = FLUID_TYPES.register("simple_acid_fluid_type", () ->
            new AcidFluidType(FluidType.Properties.create().descriptionId("simple_acid_fluid_type")
                    .canExtinguish(true).canConvertToSource(false)
                    .supportsBoating(true).canHydrate(false).viscosity(0).canPushEntity(false)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH), 1) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        public static final ResourceLocation FLUID_STILL = new ResourceLocation(MagiChemMod.MODID, "block/fluid/acid_simple");
                        public static final ResourceLocation FLUID_FLOWING = new ResourceLocation(MagiChemMod.MODID, "block/fluid/acid_simple");

                        @Override
                        public ResourceLocation getStillTexture() {
                            return FLUID_STILL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return FLUID_FLOWING;
                        }
                    });
                }
            });
    public static final RegistryObject<Fluid> SIMPLE_ACID = FLUIDS.register("simple_acid", () -> new ForgeFlowingFluid.Source(getSimpleAcidProperties()));
    public static final RegistryObject<FlowingFluid> SIMPLE_ACID_FLOWING = FLUIDS.register("simple_acid_flowing", () -> new ForgeFlowingFluid.Flowing(getSimpleAcidProperties()));
    public static final RegistryObject<LiquidBlock> SIMPLE_ACID_BLOCK = BlockRegistry.BLOCKS.register("simple_acid_block", () -> new AcidFluidBlock(SIMPLE_ACID_FLOWING.get(), BlockBehaviour.Properties.copy(Blocks.WATER)));

    public static ForgeFlowingFluid.Properties getSimpleAcidProperties() {
        return new ForgeFlowingFluid.Properties(SIMPLE_ACID_FLUID_TYPE, SIMPLE_ACID, SIMPLE_ACID_FLOWING).block(SIMPLE_ACID_BLOCK).bucket(ItemRegistry.SIMPLE_ACID_BUCKET);
    }

    //Aqua Fortis
    public static final RegistryObject<AcidFluidType> AQUA_FORTIS_FLUID_TYPE = FLUID_TYPES.register("aqua_fortis_fluid_type", () ->
            new AcidFluidType(FluidType.Properties.create().descriptionId("aqua_fortis_fluid_type")
                    .canExtinguish(true).canConvertToSource(false)
                    .supportsBoating(true).canHydrate(false).viscosity(0).canPushEntity(false)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH), 2) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        public static final ResourceLocation FLUID_STILL = new ResourceLocation(MagiChemMod.MODID, "block/fluid/acid_aqua_fortis");
                        public static final ResourceLocation FLUID_FLOWING = new ResourceLocation(MagiChemMod.MODID, "block/fluid/acid_aqua_fortis");

                        @Override
                        public ResourceLocation getStillTexture() {
                            return FLUID_STILL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return FLUID_FLOWING;
                        }
                    });
                }
            });
    public static final RegistryObject<Fluid> AQUA_FORTIS = FLUIDS.register("aqua_fortis", () -> new ForgeFlowingFluid.Source(getAquaFortisProperties()));
    public static final RegistryObject<FlowingFluid> AQUA_FORTIS_FLOWING = FLUIDS.register("aqua_fortis_flowing", () -> new ForgeFlowingFluid.Flowing(getAquaFortisProperties()));
    public static final RegistryObject<LiquidBlock> AQUA_FORTIS_BLOCK = BlockRegistry.BLOCKS.register("aqua_fortis_block", () -> new AcidFluidBlock(AQUA_FORTIS_FLOWING.get(), BlockBehaviour.Properties.copy(Blocks.WATER)));

    public static ForgeFlowingFluid.Properties getAquaFortisProperties() {
        return new ForgeFlowingFluid.Properties(AQUA_FORTIS_FLUID_TYPE, AQUA_FORTIS, AQUA_FORTIS_FLOWING).block(AQUA_FORTIS_BLOCK).bucket(ItemRegistry.AQUA_FORTIS_BUCKET);
    }

    //Aqua Regia
    public static final RegistryObject<AcidFluidType> AQUA_REGIA_FLUID_TYPE = FLUID_TYPES.register("aqua_regia_fluid_type", () ->
            new AcidFluidType(FluidType.Properties.create().descriptionId("aqua_regia_fluid_type")
                    .canExtinguish(true).canConvertToSource(false)
                    .supportsBoating(true).canHydrate(false).viscosity(0).canPushEntity(false)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH), 3) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        public static final ResourceLocation FLUID_STILL = new ResourceLocation(MagiChemMod.MODID, "block/fluid/acid_aqua_regia");
                        public static final ResourceLocation FLUID_FLOWING = new ResourceLocation(MagiChemMod.MODID, "block/fluid/acid_aqua_regia");

                        @Override
                        public ResourceLocation getStillTexture() {
                            return FLUID_STILL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return FLUID_FLOWING;
                        }
                    });
                }
            });
    public static final RegistryObject<Fluid> AQUA_REGIA = FLUIDS.register("aqua_regia", () -> new ForgeFlowingFluid.Source(getAquaRegiaProperties()));
    public static final RegistryObject<FlowingFluid> AQUA_REGIA_FLOWING = FLUIDS.register("aqua_regia_flowing", () -> new ForgeFlowingFluid.Flowing(getAquaRegiaProperties()));
    public static final RegistryObject<LiquidBlock> AQUA_REGIA_BLOCK = BlockRegistry.BLOCKS.register("aqua_regia_block", () -> new AcidFluidBlock(AQUA_REGIA_FLOWING.get(), BlockBehaviour.Properties.copy(Blocks.WATER)));

    public static ForgeFlowingFluid.Properties getAquaRegiaProperties() {
        return new ForgeFlowingFluid.Properties(AQUA_REGIA_FLUID_TYPE, AQUA_REGIA, AQUA_REGIA_FLOWING).block(AQUA_REGIA_BLOCK).bucket(ItemRegistry.AQUA_REGIA_BUCKET);
    }

    //Oil of Vitriol
    public static final RegistryObject<AcidFluidType> OIL_OF_VITRIOL_FLUID_TYPE = FLUID_TYPES.register("oil_of_vitriol_fluid_type", () ->
            new AcidFluidType(FluidType.Properties.create().descriptionId("oil_of_vitriol_fluid_type")
                    .canExtinguish(true).canConvertToSource(false)
                    .supportsBoating(true).canHydrate(false).viscosity(0).canPushEntity(false)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH), 4) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        public static final ResourceLocation FLUID_STILL = new ResourceLocation(MagiChemMod.MODID, "block/fluid/acid_oil_of_vitriol");
                        public static final ResourceLocation FLUID_FLOWING = new ResourceLocation(MagiChemMod.MODID, "block/fluid/acid_oil_of_vitriol");

                        @Override
                        public ResourceLocation getStillTexture() {
                            return FLUID_STILL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return FLUID_FLOWING;
                        }
                    });
                }
            });
    public static final RegistryObject<Fluid> OIL_OF_VITRIOL = FLUIDS.register("oil_of_vitriol", () -> new ForgeFlowingFluid.Source(getOilOfVitriolProperties()));
    public static final RegistryObject<FlowingFluid> OIL_OF_VITRIOL_FLOWING = FLUIDS.register("oil_of_vitriol_flowing", () -> new ForgeFlowingFluid.Flowing(getOilOfVitriolProperties()));
    public static final RegistryObject<LiquidBlock> OIL_OF_VITRIOL_BLOCK = BlockRegistry.BLOCKS.register("oil_of_vitriol_block", () -> new AcidFluidBlock(OIL_OF_VITRIOL_FLOWING.get(), BlockBehaviour.Properties.copy(Blocks.WATER)));

    public static ForgeFlowingFluid.Properties getOilOfVitriolProperties() {
        return new ForgeFlowingFluid.Properties(OIL_OF_VITRIOL_FLUID_TYPE, OIL_OF_VITRIOL, OIL_OF_VITRIOL_FLOWING).block(OIL_OF_VITRIOL_BLOCK).bucket(ItemRegistry.OIL_OF_VITRIOL_BUCKET);
    }

    //Azoth
    public static final RegistryObject<AcidFluidType> AZOTH_FLUID_TYPE = FLUID_TYPES.register("azoth_fluid_type", () ->
            new AcidFluidType(FluidType.Properties.create().descriptionId("azoth_fluid_type")
                    .canExtinguish(true).canConvertToSource(false)
                    .supportsBoating(true).canHydrate(false).viscosity(0).canPushEntity(false)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH), 5) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        public static final ResourceLocation FLUID_STILL = new ResourceLocation(MagiChemMod.MODID, "block/fluid/acid_azoth");
                        public static final ResourceLocation FLUID_FLOWING = new ResourceLocation(MagiChemMod.MODID, "block/fluid/acid_azoth");

                        @Override
                        public ResourceLocation getStillTexture() {
                            return FLUID_STILL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return FLUID_FLOWING;
                        }
                    });
                }
            });
    public static final RegistryObject<Fluid> AZOTH = FLUIDS.register("azoth", () -> new ForgeFlowingFluid.Source(getAzothProperties()));
    public static final RegistryObject<FlowingFluid> AZOTH_FLOWING = FLUIDS.register("azoth_flowing", () -> new ForgeFlowingFluid.Flowing(getAzothProperties()));
    public static final RegistryObject<LiquidBlock> AZOTH_BLOCK = BlockRegistry.BLOCKS.register("azoth_block", () -> new AcidFluidBlock(AZOTH_FLOWING.get(), BlockBehaviour.Properties.copy(Blocks.WATER)));

    public static ForgeFlowingFluid.Properties getAzothProperties() {
        return new ForgeFlowingFluid.Properties(AZOTH_FLUID_TYPE, AZOTH, AZOTH_FLOWING).block(AZOTH_BLOCK).bucket(ItemRegistry.AZOTH_BUCKET);
    }

    //////////////////////
    //-----BOILERPLATE
    //////////////////////
    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
        FLUID_TYPES.register(eventBus);

        ModList modList = ModList.get();

        if(modList.isLoaded("occultism")) {
            OccultismFluidRegistry.register(eventBus);
        }
    }
}
