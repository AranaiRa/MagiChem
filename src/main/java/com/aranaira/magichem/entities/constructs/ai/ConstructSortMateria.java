package com.aranaira.magichem.entities.constructs.ai;

import com.aranaira.magichem.block.MateriaVesselBlock;
import com.aranaira.magichem.block.entity.AlembicBlockEntity;
import com.aranaira.magichem.block.entity.MateriaVesselBlockEntity;
import com.aranaira.magichem.block.entity.interfaces.IMateriaProcessingDevice;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ConstructTasksRegistry;
import com.mna.Registries;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.ConstructBlockAreaTask;
import com.mna.api.entities.construct.ai.parameter.ConstructAITaskParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskAreaParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskBooleanParameter;
import com.mna.api.entities.construct.ai.parameter.ConstructTaskPointParameter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.List;

public class ConstructSortMateria extends ConstructAITask<ConstructSortMateria> {
    private static final ConstructCapability[] requiredCaps;
    private BlockPos takeFromTarget;
    private AABB area;
    private boolean voidExcess;

    public ConstructSortMateria(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
    }

    @Override
    public void start() {
        super.start();
        if(isFullyConfigured())
            doMateriaTransfer();
    }

    @Override
    public void tick() {
        super.tick();
    }

    private void doMateriaTransfer() {
        BlockEntity be = construct.asEntity().level().getBlockEntity(takeFromTarget);
        if(be instanceof IMateriaProcessingDevice mpd) {
            SimpleContainer contents = mpd.getContentsOfOutputSlots();
            if(!contents.isEmpty()) {
                int slot = 0;
                ItemStack stack = null;
                MateriaItem type = null;
                while(type == null) {
                    stack = contents.getItem(slot);
                    if(stack != ItemStack.EMPTY && stack.getItem() instanceof MateriaItem mi) {
                        type = mi;
                    }
                    slot++;
                }
                MateriaVesselBlockEntity mvbe = getVesselOfType(type);
                //magic number for now
                int transferLimit = 1;
                stack.shrink(transferLimit);
                mvbe.fill(transferLimit, this.voidExcess);
            }
        }
    }

    private List<MateriaVesselBlockEntity> getMateriaVesselsInRegion() {
        List<MateriaVesselBlockEntity> output = new ArrayList<>();

        Level level = construct.asEntity().level();

        for(int x=(int)area.minX; x<=(int)area.maxX; x++){
            for(int y=(int)area.minY; y<=(int)area.maxY; y++) {
                for (int z=(int)area.minZ; z<=(int)area.maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if(level.getBlockState(pos).getBlock() instanceof MateriaVesselBlock mvb) {
                        output.add((MateriaVesselBlockEntity) level.getBlockEntity(pos));
                    }
                }
            }
        }

        return output;
    }

    /**
     * Returns the first materia vessel that contains the specified type of materia, or the first empty vessel if one isn't found.
     * @param filter The type of materia to look for
     * @return The first materia vessel that contains the specified materia type; else the first empty vessel; else null
     */
    private MateriaVesselBlockEntity getVesselOfType(MateriaItem filter) {
        MateriaVesselBlockEntity firstEmpty = null;
        for(MateriaVesselBlockEntity mvbe : getMateriaVesselsInRegion()) {
            if(mvbe.getMateriaType() == null) {
                if(firstEmpty == null) firstEmpty = mvbe;
            } else if (mvbe.getMateriaType() == filter) {
                return mvbe;
            }
        }
        return firstEmpty;
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.get().getKey(ConstructTasksRegistry.SORT_MATERIA);
    }

    @Override
    public ConstructSortMateria duplicate() {
        return new ConstructSortMateria(this.construct, this.guiIcon).copyFrom(this);
    }

    @Override
    public ConstructSortMateria copyFrom(ConstructAITask<?> other) {
        if(other instanceof ConstructSortMateria task) {
            this.area = task.area;
            this.voidExcess = task.voidExcess;
        }

        return this;
    }

    @Override
    public void readNBT(CompoundTag compoundTag) {

    }

    @Override
    public CompoundTag writeInternal(CompoundTag compoundTag) {
        return compoundTag;
    }

    @Override
    protected List<ConstructAITaskParameter> instantiateParameters() {
        List<ConstructAITaskParameter> parameters = super.instantiateParameters();
        parameters.add(new ConstructTaskPointParameter("sort_materia.point"));
        parameters.add(new ConstructTaskAreaParameter("sort_materia.area"));
        parameters.add(new ConstructTaskBooleanParameter("sort_materia.boolean"));
        return parameters;
    }

    @Override
    public void inflateParameters() {
        this.getParameter("sort_materia.point").ifPresent((param) -> {
            if (param instanceof ConstructTaskPointParameter pointParam) {
                this.takeFromTarget = pointParam.getPosition();
            }
        });

        this.getParameter("sort_materia.area").ifPresent((param) -> {
            if (param instanceof ConstructTaskAreaParameter areaParam) {
                this.area = null;

                if(areaParam.getPoints() != null) {
                    if(areaParam.getArea() != null)
                        this.area = areaParam.getArea();
                }
            }
        });

        this.getParameter("sort_materia.boolean").ifPresent((param) -> {
            if(param instanceof ConstructTaskBooleanParameter boolParam) {
                this.voidExcess = boolParam.getValue();
            }
        });
    }

    @Override
    public boolean isFullyConfigured() {
        return this.area != null && this.takeFromTarget != null;
    }

    static {
        requiredCaps = new ConstructCapability[]{ConstructCapability.FLUID_DISPENSE};
    }
}
