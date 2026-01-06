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

public class BloodMagicFluidRegistry {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, MagiChemMod.MODID);
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, MagiChemMod.MODID);

    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
        FLUID_TYPES.register(eventBus);
    }

    //DEMONITE/HELLFORGED_INGOT
    public static final RegistryObject<FluidType> LIQUEFACTED_DEMONITE_FLUID_TYPE = FLUID_TYPES.register("liquefacted_demonite_fluid_type", () ->
            new FluidType(FluidType.Properties.create().descriptionId("liquefacted_demonite_fluid_type")
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
                            return 0xff9fe4d6;
                        }
                    });
                }
            });
    public static final RegistryObject<Fluid> LIQUEFACTED_DEMONITE = FLUIDS.register("liquefacted_demonite", () -> new ForgeFlowingFluid.Source(getLiquefactedDemoniteProperties()));
    public static final RegistryObject<FlowingFluid> LIQUEFACTED_DEMONITE_FLOWING = FLUIDS.register("liquefacted_demonite_flowing", () -> new ForgeFlowingFluid.Flowing(getLiquefactedDemoniteProperties()));
    public static final RegistryObject<LiquidBlock> LIQUEFACTED_DEMONITE_BLOCK = BlockRegistry.BLOCKS.register("liquefacted_demonite_block", () -> new LiquidBlock(LIQUEFACTED_DEMONITE_FLOWING, BlockBehaviour.Properties.copy(Blocks.LAVA)));

    public static ForgeFlowingFluid.Properties getLiquefactedDemoniteProperties() {
        return new ForgeFlowingFluid.Properties(LIQUEFACTED_DEMONITE_FLUID_TYPE, LIQUEFACTED_DEMONITE, LIQUEFACTED_DEMONITE_FLOWING).block(LIQUEFACTED_DEMONITE_BLOCK).bucket(BloodMagicItemRegistry.LIQUEFACTED_DEMONITE_BUCKET);
    }
}
