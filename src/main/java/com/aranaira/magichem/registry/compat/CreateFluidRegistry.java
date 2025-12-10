package com.aranaira.magichem.registry.compat;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.registry.BlockRegistry;
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

public class CreateFluidRegistry {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, MagiChemMod.MODID);
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, MagiChemMod.MODID);

    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
        FLUID_TYPES.register(eventBus);
    }

    //ZINC
    public static final RegistryObject<FluidType> LIQUEFACTED_ZINC_FLUID_TYPE = FLUID_TYPES.register("liquefacted_zinc_fluid_type", () ->
            new FluidType(FluidType.Properties.create().descriptionId("liquefacted_zinc_fluid_type")
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
                            return 0xff94b69c;
                        }
                    });
                }
            });
    public static final RegistryObject<Fluid> LIQUEFACTED_ZINC = FLUIDS.register("liquefacted_zinc", () -> new ForgeFlowingFluid.Source(getLiquefactedZincProperties()));
    public static final RegistryObject<FlowingFluid> LIQUEFACTED_ZINC_FLOWING = FLUIDS.register("liquefacted_zinc_flowing", () -> new ForgeFlowingFluid.Flowing(getLiquefactedZincProperties()));
    public static final RegistryObject<LiquidBlock> LIQUEFACTED_ZINC_BLOCK = BlockRegistry.BLOCKS.register("liquefacted_zinc_block", () -> new LiquidBlock(LIQUEFACTED_ZINC_FLOWING, BlockBehaviour.Properties.copy(Blocks.LAVA)));

    public static ForgeFlowingFluid.Properties getLiquefactedZincProperties() {
        return new ForgeFlowingFluid.Properties(LIQUEFACTED_ZINC_FLUID_TYPE, LIQUEFACTED_ZINC, LIQUEFACTED_ZINC_FLOWING).block(LIQUEFACTED_ZINC_BLOCK).bucket(CreateItemRegistry.LIQUEFACTED_ZINC_BUCKET);
    }
}
