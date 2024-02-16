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

    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
        FLUID_TYPES.register(eventBus);
    }
}
