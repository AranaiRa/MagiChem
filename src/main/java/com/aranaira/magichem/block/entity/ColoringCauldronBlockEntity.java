package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.recipe.ColorationRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ColoringCauldronBlockEntity extends BlockEntity {

    private static final float
        PROCESSING_TIME_COEFFICIENT = 0.90625f;
    public static final int
        BITPACK_RED = 1, BITPACK_ORANGE = 2, BITPACK_YELLOW = 4, BITPACK_LIME = 8,
        BITPACK_GREEN = 16, BITPACK_CYAN = 32, BITPACK_LIGHT_BLUE = 64, BITPACK_BLUE = 128,
        BITPACK_PURPLE = 256, BITPACK_MAGENTA = 512, BITPACK_PINK = 1024, BITPACK_BROWN = 2048,
        BITPACK_BLACK = 4096, BITPACK_GRAY = 8192, BITPACK_LIGHT_GRAY = 16384, BITPACK_WHITE = 32768;
    private int
            bitpackedColors, operationsRemaining, progress;
    private boolean readyToCollect;
    private ItemStack containedItem = ItemStack.EMPTY;
    private ColorationRecipe recipe = null;
    private static final Random r = new Random();

    public ColoringCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.COLORING_CAULDRON_BE.get(), pos, state);
    }

    public ColoringCauldronBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public boolean insertItemStack(ItemStack pStack) {
        if(pStack.getItem() instanceof DyeItem) {
            int bitpackID = getBitpackIDFromColor(DyeColor.getColor(pStack));
            if((bitpackedColors & bitpackID) == 0 && getTotalColors() < 15) {
                bitpackedColors = bitpackedColors | bitpackID;
                progress = getOperationTicks();
                operationsRemaining++;
                syncAndSave();
                return true;
            }
            return false;
        }

        if(containedItem.isEmpty()) {
            final ColorationRecipe recipeQuery = ColorationRecipe.getColorationRecipe(getLevel(), pStack);
            if (recipeQuery != null) {
                containedItem = pStack.copy();
                containedItem.setCount(1);
                recipe = recipeQuery;
                if(bitpackedColors != 0)
                    progress = getOperationTicks();
                syncAndSave();
                return true;
            }
        }
        return false;
    }

    private int getBitpackIDFromColor(DyeColor pColor) {
        return switch(pColor) {
            case RED -> BITPACK_RED;
            case ORANGE -> BITPACK_ORANGE;
            case YELLOW -> BITPACK_YELLOW;
            case LIME -> BITPACK_LIME;
            case GREEN -> BITPACK_GREEN;
            case CYAN -> BITPACK_CYAN;
            case LIGHT_BLUE -> BITPACK_LIGHT_BLUE;
            case BLUE -> BITPACK_BLUE;
            case PURPLE -> BITPACK_PURPLE;
            case MAGENTA -> BITPACK_MAGENTA;
            case PINK -> BITPACK_PINK;
            case BROWN -> BITPACK_BROWN;
            case BLACK -> BITPACK_BLACK;
            case GRAY -> BITPACK_GRAY;
            case LIGHT_GRAY -> BITPACK_LIGHT_GRAY;
            case WHITE -> BITPACK_WHITE;
        };
    }

    private int getTotalColors() {
        int total = 0;

        if((bitpackedColors & BITPACK_RED) == BITPACK_RED) total++;
        if((bitpackedColors & BITPACK_ORANGE) == BITPACK_ORANGE) total++;
        if((bitpackedColors & BITPACK_YELLOW) == BITPACK_YELLOW) total++;
        if((bitpackedColors & BITPACK_LIME) == BITPACK_LIME) total++;
        if((bitpackedColors & BITPACK_GREEN) == BITPACK_GREEN) total++;
        if((bitpackedColors & BITPACK_CYAN) == BITPACK_CYAN) total++;
        if((bitpackedColors & BITPACK_LIGHT_BLUE) == BITPACK_LIGHT_BLUE) total++;
        if((bitpackedColors & BITPACK_BLUE) == BITPACK_BLUE) total++;
        if((bitpackedColors & BITPACK_PURPLE) == BITPACK_PURPLE) total++;
        if((bitpackedColors & BITPACK_MAGENTA) == BITPACK_MAGENTA) total++;
        if((bitpackedColors & BITPACK_PINK) == BITPACK_PINK) total++;
        if((bitpackedColors & BITPACK_BROWN) == BITPACK_BROWN) total++;
        if((bitpackedColors & BITPACK_BLACK) == BITPACK_BLACK) total++;
        if((bitpackedColors & BITPACK_GRAY) == BITPACK_GRAY) total++;
        if((bitpackedColors & BITPACK_LIGHT_GRAY) == BITPACK_LIGHT_GRAY) total++;
        if((bitpackedColors & BITPACK_WHITE) == BITPACK_WHITE) total++;

        return total;
    }

    public void syncAndSave() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", containedItem.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        nbt.putInt("bitpackedColors", this.bitpackedColors);
        nbt.putInt("operationsRemaining", this.operationsRemaining);
        nbt.putBoolean("readyToCollect", this.readyToCollect);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        Item query = ForgeRegistries.ITEMS.getValue(new ResourceLocation(nbt.getCompound("inventory").getString("id")));
        if(query != null)
            containedItem = new ItemStack(query);
        progress = nbt.getInt("craftingProgress");
        bitpackedColors = nbt.getInt("bitpackedColors");
        operationsRemaining = nbt.getInt("operationsRemaining");
        readyToCollect = nbt.getBoolean("readyToCollect");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", containedItem.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        nbt.putInt("bitpackedColors", this.bitpackedColors);
        nbt.putInt("operationsRemaining", this.operationsRemaining);
        nbt.putBoolean("readyToCollect", this.readyToCollect);
        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public boolean hasItem() {
        return !containedItem.isEmpty();
    }

    public boolean hasColors() {
        return bitpackedColors != 0;
    }

    public boolean isReadyToCollect() {
        return readyToCollect;
    }

    public int getOperationsRemaining() {
        return operationsRemaining;
    }

    public static <E extends BlockEntity> void tick(Level pLevel, BlockPos pPos, BlockState pBlockState, ColoringCauldronBlockEntity pBlockEntity) {
        if(!pLevel.isClientSide() && pBlockEntity.bitpackedColors != 0) {
            if(pBlockEntity.recipe != null && !pBlockEntity.readyToCollect) {
                pBlockEntity.progress--;

                if (pBlockEntity.progress <= 0) {
                    pBlockEntity.operationsRemaining--;
                    pBlockEntity.readyToCollect = true;

                    final HashMap<DyeColor, ItemStack> resultsAsMap = pBlockEntity.recipe.getResultsAsMap(false);
                    DyeColor color = pBlockEntity.pickRandomColorFromInverseBitpack(resultsAsMap.keySet());
                    pBlockEntity.containedItem = resultsAsMap.get(color).copy();

                    pBlockEntity.recipe = null;
                    pBlockEntity.syncAndSave();
                }
            }

            if(pBlockEntity.operationsRemaining <= 0) {
                pBlockEntity.bitpackedColors = 0;
                pBlockEntity.operationsRemaining = 0;
            }
        }
    }

    public void collectItem(Level pLevel, Player pPlayer) {
        ItemEntity ie = new ItemEntity(pLevel, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), containedItem.copy());
        pLevel.addFreshEntity(ie);
        containedItem = ItemStack.EMPTY;
        readyToCollect = false;
        syncAndSave();
    }

    private int getOperationTicks() {
        return Math.round(Config.coloringCauldronBaseOperationTime * (float)Math.pow(PROCESSING_TIME_COEFFICIENT, getTotalColors() - 1));
    }

    private DyeColor pickRandomColorFromInverseBitpack(Set<DyeColor> validColors) {
        ArrayList<DyeColor> colorOptions = getInverseBitpackedColors();

        colorOptions.removeIf(color -> !validColors.contains(color));

        if(colorOptions.size() == 0)
            return null;

        int index = r.nextInt(colorOptions.size());
        return colorOptions.get(index);
    }

    @NotNull
    private ArrayList<DyeColor> getInverseBitpackedColors() {
        ArrayList<DyeColor> colorOptions = new ArrayList<>();

        if((bitpackedColors & BITPACK_RED) == 0)        colorOptions.add(DyeColor.RED);
        if((bitpackedColors & BITPACK_ORANGE) == 0)     colorOptions.add(DyeColor.ORANGE);
        if((bitpackedColors & BITPACK_YELLOW) == 0)     colorOptions.add(DyeColor.YELLOW);
        if((bitpackedColors & BITPACK_LIME) == 0)       colorOptions.add(DyeColor.LIME);
        if((bitpackedColors & BITPACK_GREEN) == 0)      colorOptions.add(DyeColor.GREEN);
        if((bitpackedColors & BITPACK_CYAN) == 0)       colorOptions.add(DyeColor.CYAN);
        if((bitpackedColors & BITPACK_LIGHT_BLUE) == 0) colorOptions.add(DyeColor.LIGHT_BLUE);
        if((bitpackedColors & BITPACK_BLUE) == 0)       colorOptions.add(DyeColor.BLUE);
        if((bitpackedColors & BITPACK_PURPLE) == 0)     colorOptions.add(DyeColor.PURPLE);
        if((bitpackedColors & BITPACK_MAGENTA) == 0)    colorOptions.add(DyeColor.MAGENTA);
        if((bitpackedColors & BITPACK_PINK) == 0)       colorOptions.add(DyeColor.PINK);
        if((bitpackedColors & BITPACK_BROWN) == 0)      colorOptions.add(DyeColor.BROWN);
        if((bitpackedColors & BITPACK_BLACK) == 0)      colorOptions.add(DyeColor.BLACK);
        if((bitpackedColors & BITPACK_GRAY) == 0)       colorOptions.add(DyeColor.GRAY);
        if((bitpackedColors & BITPACK_LIGHT_GRAY) == 0) colorOptions.add(DyeColor.LIGHT_GRAY);
        if((bitpackedColors & BITPACK_WHITE) == 0)      colorOptions.add(DyeColor.WHITE);

        return colorOptions;
    }

    public List<String> getInfoReadout() {
        List<String> output = new ArrayList<>();

        String itemName = containedItem.getDisplayName().getString();

        if(readyToCollect)
            output.add(itemName + Component.translatable("hud.magichem.coloring_cauldron.current_item.waiting").getString());
        else
            output.add(Component.translatable("hud.magichem.coloring_cauldron.current_item.processing").getString() + itemName);

        List<String> lineBreaker = new ArrayList<>();
        for (DyeColor color : getInverseBitpackedColors()) {
            lineBreaker.add(Component.translatable("item.minecraft.firework_star."+color.getName()).getString());

            if(lineBreaker.size() == 4) {
                String line = "";
                line += lineBreaker.get(0) + ",  " + lineBreaker.get(1) + ",  " + lineBreaker.get(2) + ",  " + lineBreaker.get(3) + ",";
                output.add(line);
                lineBreaker.clear();
            }
        }

        if(lineBreaker.size() == 1) {
            output.add(lineBreaker.get(0));
        } else if(lineBreaker.size() > 1) {
            String line = lineBreaker.get(0);
            for(int i=1; i<lineBreaker.size(); i++) {
                line += ",  " + lineBreaker.get(i);
            }
            output.add(line);
        }

        return output;
    }
}
