package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
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
                    .supportsBoating(true).canHydrate(false).viscosity(12)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        public static final ResourceLocation FLUID_STILL = new ResourceLocation(MagiChemMod.MODID, "block/fluid/steam");
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
                    .supportsBoating(true).canHydrate(false).viscosity(12)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        public static final ResourceLocation FLUID_STILL = new ResourceLocation(MagiChemMod.MODID, "block/fluid/smoke");
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
    //-----BOILERPLATE
    //////////////////////
    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
        FLUID_TYPES.register(eventBus);
    }
}
