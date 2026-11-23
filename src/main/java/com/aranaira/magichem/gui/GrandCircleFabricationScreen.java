package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.GrandCircleFabricationBlockEntity;
import com.aranaira.magichem.foundation.ButtonData;
import com.aranaira.magichem.foundation.options.RecipeDisplayOption;
import com.aranaira.magichem.gui.element.FabricationButtonRecipeSelector;
import com.aranaira.magichem.networking.DeviceRecipeClearC2SPacket;
import com.aranaira.magichem.networking.FabricationBatchSizeC2SPacket;
import com.aranaira.magichem.networking.FabricationSyncDataC2SPacket;
import com.aranaira.magichem.recipe.DistillationFabricationRecipe;
import com.aranaira.magichem.recipe.FluidDistillationFabricationRecipe;
import com.aranaira.magichem.registry.PacketRegistry;
import com.aranaira.magichem.util.AdvancementUtil;
import com.mna.tools.math.MathUtils;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class GrandCircleFabricationScreen extends AbstractContainerScreen<GrandCircleFabricationMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_fabrication.png");
    private static final ResourceLocation TEXTURE_EXT =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_fabrication_ext.png");
    private ImageButton
            b_powerLevelUp, b_powerLevelDown;
    private ButtonData[] recipeSelectButtons = new ButtonData[15];
    private EditBox recipeFilterBox;
    private static final int
            PANEL_MAIN_W = 186, PANEL_MAIN_H = 192,
            PANEL_RECIPE_X = -85, PANEL_RECIPE_Y = 10,
            PANEL_RECIPE_U = 160, PANEL_RECIPE_V = 96, PANEL_RECIPE_W = 81, PANEL_RECIPE_H = 126,
            PANEL_STONE_X = 190, PANEL_STONE_Y = 100,
            PANEL_BATCH_X = -85, PANEL_BATCH_Y = 142, PANEL_BATCH_W = 81, PANEL_BATCH_H = 45,
            PANEL_POWER_X = 186, PANEL_POWER_Y = 19,
            PANEL_POWER_U = 0, PANEL_POWER_V = 102, PANEL_POWER_W = 80, PANEL_POWER_H = 66;
    private RecipeDisplayOption lastClickedRecipe = null;
    private String lastUsedFilter = null;
    private Player player;
    private static List<DistillationFabricationRecipe> allDistillationRecipes = new ArrayList<>();
    private static List<FluidDistillationFabricationRecipe> allFluidDistillationRecipes = new ArrayList<>();

    public GrandCircleFabricationScreen(GrandCircleFabricationMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        player = inventory.player;
        if(allDistillationRecipes.size() == 0)
            allDistillationRecipes = DistillationFabricationRecipe.getAllDistillingRecipes(inventory.player.level());
        if(allFluidDistillationRecipes.size() == 0)
            allFluidDistillationRecipes = FluidDistillationFabricationRecipe.getAllDistillingRecipes(inventory.player.level());
    }

    @Override
    protected void init() {
        super.init();
        initializePowerLevelButtons();
        initializeRecipeSelectorButtons();
        updateDisplayedRecipes("");
        initializeRecipeFilterBox();
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if(!this.recipeFilterBox.isFocused()) {
            boolean isNumber = (pKeyCode >= 48) && (pKeyCode <= 57);
            boolean isNumpadNumber = (pKeyCode >= 97) && (pKeyCode <= 105);

            if (isNumber || isNumpadNumber) return false;
        }

        if (pKeyCode == InputConstants.KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        if (Minecraft.getInstance().options.keyInventory.matches(pKeyCode, pScanCode)) {
            if (recipeFilterBox.canConsumeInput()) return recipeFilterBox.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    private void initializeRecipeFilterBox() {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        this.recipeFilterBox = new EditBox(Minecraft.getInstance().font, x, y, 67, 18, Component.empty());
        this.recipeFilterBox.setMaxLength(60);

        renderFilterBox();
    }

    private void initializePowerLevelButtons(){
        b_powerLevelUp = this.addRenderableWidget(new ImageButton(this.leftPos + 213, this.topPos + 126, 12, 7, 81, 242, TEXTURE, button -> {
            ResourceLocation rl;
            boolean isFluid = false;
            if(menu.blockEntity.getCurrentRecipe() instanceof DistillationFabricationRecipe itemRecipe)
                rl = ForgeRegistries.ITEMS.getKey(itemRecipe.getAlchemyObject().getItem());
            else if(menu.blockEntity.getCurrentRecipe() instanceof FluidDistillationFabricationRecipe fluidRecipe) {
                rl = ForgeRegistries.FLUIDS.getKey(fluidRecipe.getAlchemyFluid().getFluid());
                isFluid = true;
            }
            else
                rl = new ResourceLocation("minecraft","barrier");

            menu.blockEntity.incrementPowerUsageSetting();
            PacketRegistry.sendToServer(new FabricationSyncDataC2SPacket(
                    menu.blockEntity.getBlockPos(),
                    rl,
                    isFluid,
                    menu.blockEntity.getPowerUsageSetting()
            ));
        }));
        b_powerLevelDown = this.addRenderableWidget(new ImageButton(this.leftPos + 190, this.topPos + 126, 12, 7, 93, 242, TEXTURE, button -> {
            ResourceLocation rl;
            boolean isFluid = false;
            if(menu.blockEntity.getCurrentRecipe() instanceof DistillationFabricationRecipe itemRecipe)
                rl = ForgeRegistries.ITEMS.getKey(itemRecipe.getAlchemyObject().getItem());
            else if(menu.blockEntity.getCurrentRecipe() instanceof FluidDistillationFabricationRecipe fluidRecipe) {
                rl = ForgeRegistries.FLUIDS.getKey(fluidRecipe.getAlchemyFluid().getFluid());
                isFluid = true;
            }
            else
                rl = new ResourceLocation("minecraft","barrier");

            menu.blockEntity.decrementPowerUsageSetting();
            PacketRegistry.sendToServer(new FabricationSyncDataC2SPacket(
                    menu.blockEntity.getBlockPos(),
                    rl,
                    isFluid,
                    menu.blockEntity.getPowerUsageSetting()
            ));
        }));
    }

    private void initializeRecipeSelectorButtons(){
        int c = 0;
        for(int y=0; y<5; y++) {
            for(int x=0; x<3; x++) {
                recipeSelectButtons[c] = new ButtonData(this.addRenderableWidget(new FabricationButtonRecipeSelector(
                        this, c, this.leftPos, this.topPos, 18, 18, 54, 220, TEXTURE, button -> {

                            GrandCircleFabricationScreen query = (GrandCircleFabricationScreen) ((FabricationButtonRecipeSelector) button).getScreen();
                            query.setActiveRecipe(((FabricationButtonRecipeSelector) button).getArrayIndex());
                })), x*18 - 78, y*18 + 39);
                c++;
            }
        }

        int x = this.leftPos + 68;
        int y = this.topPos + 66;
        new ButtonData(this.addRenderableWidget(new FabricationButtonRecipeSelector(
                this, c, x, y, 9, 9, 72, 238, TEXTURE, button -> {

            GrandCircleFabricationScreen query = (GrandCircleFabricationScreen) ((FabricationButtonRecipeSelector) button).getScreen();
            query.clearActiveRecipe();
        })), x, y);

        renderButtons();
    }

    public void setActiveRecipe(int index) {
        int trueIndex = recipeFilterRow*3 + index;
        if(trueIndex < filteredRecipes.size()) {
            final RecipeDisplayOption option = filteredRecipes.get(trueIndex);

            if (option.getRecipe() instanceof DistillationFabricationRecipe itemRecipe)
                menu.blockEntity.setCurrentRecipe(itemRecipe.getAlchemyObject().getItem());
            else if (option.getRecipe() instanceof FluidDistillationFabricationRecipe fluidRecipe)
                menu.blockEntity.setCurrentRecipe(fluidRecipe.getAlchemyFluid().getFluid());

            ResourceLocation rl = option.isFluidRecipe() ?
                    ForgeRegistries.FLUIDS.getKey(option.getFluidRecipe().getAlchemyFluid().getFluid()) :
                    ForgeRegistries.ITEMS.getKey(option.getItemRecipe().getAlchemyObject().getItem());
            PacketRegistry.sendToServer(new FabricationSyncDataC2SPacket(
                    menu.blockEntity.getBlockPos(),
                    rl,
                    option.isFluidRecipe(),
                    menu.blockEntity.getPowerUsageSetting()
            ));
            lastClickedRecipe = option;
            menu.blockEntity.setBatchSize(1);
        }
    }

    public void clearActiveRecipe() {
        menu.blockEntity.clearRecipeAfterNextProcess = true;
        PacketRegistry.sendToServer(new DeviceRecipeClearC2SPacket(
                menu.blockEntity.getBlockPos()
        ));
    }

    private List<RecipeDisplayOption> filteredRecipes = new ArrayList<>();
    private int recipeFilterRow, recipeFilterRowTotal;
    private void updateDisplayedRecipes(String filter) {
        if (!menu.blockEntity.forceDisplayedRecipeUpdate && Objects.equals(filter, lastUsedFilter)) return;
        lastUsedFilter = filter;

        filteredRecipes.clear();
        List<RecipeDisplayOption> dump = new ArrayList<>();

        for(DistillationFabricationRecipe acr : allDistillationRecipes) {
            String display = acr.getAlchemyObject().getDisplayName().getString();
            boolean nameMatchesFilter = (Objects.equals(filter, "") || display.toLowerCase().contains(filter.toLowerCase()));
            boolean wisdomValidForCurrentStone = acr.getWisdom() <= menu.blockEntity.getCurrentWisdom(GrandCircleFabricationBlockEntity::getVar);
            boolean requiredAdvancementCompliant = true;
            boolean forbiddenAdvancementCompliant = true;

            if(acr.getRequiredAdvancement() != null && player instanceof LocalPlayer lp) {
                requiredAdvancementCompliant = AdvancementUtil.clientHasAdvancement(lp, acr.getRequiredAdvancement());
            }
            if(acr.getForbiddenAdvancement() != null && player instanceof LocalPlayer lp) {
                forbiddenAdvancementCompliant = !AdvancementUtil.clientHasAdvancement(lp, acr.getForbiddenAdvancement());
            }

            if(nameMatchesFilter && wisdomValidForCurrentStone && requiredAdvancementCompliant && forbiddenAdvancementCompliant) {
                dump.add(new RecipeDisplayOption(acr));
            }
        }

        //sort filtered item recipes
        Object[] sortable = dump.toArray();
        Arrays.sort(sortable, Comparator.comparing(o -> ((RecipeDisplayOption)o).getSortingString()));
        for(Object o : sortable) {
            filteredRecipes.add((RecipeDisplayOption)o);
        }
        dump.clear();

        for(FluidDistillationFabricationRecipe facr : allFluidDistillationRecipes) {
            String display = facr.getAlchemyFluid().getDisplayName().getString();
            boolean nameMatchesFilter = (Objects.equals(filter, "") || display.toLowerCase().contains(filter.toLowerCase()));
            boolean wisdomValidForCurrentStone = facr.getWisdom() <= menu.blockEntity.getCurrentWisdom(GrandCircleFabricationBlockEntity::getVar);
            boolean requiredAdvancementCompliant = true;
            boolean forbiddenAdvancementCompliant = true;

            if(facr.getRequiredAdvancement() != null && player instanceof LocalPlayer lp) {
                requiredAdvancementCompliant = AdvancementUtil.clientHasAdvancement(lp, facr.getRequiredAdvancement());
            }
            if(facr.getForbiddenAdvancement() != null && player instanceof LocalPlayer lp) {
                forbiddenAdvancementCompliant = !AdvancementUtil.clientHasAdvancement(lp, facr.getForbiddenAdvancement());
            }

            if(nameMatchesFilter && wisdomValidForCurrentStone && requiredAdvancementCompliant && forbiddenAdvancementCompliant) {
                dump.add(new RecipeDisplayOption(facr));
            }
        }

        //sort filtered item recipes
        sortable = dump.toArray();
        Arrays.sort(sortable, Comparator.comparing(o -> ((RecipeDisplayOption)o).getSortingString()));
        for(Object o : sortable) {
            filteredRecipes.add((RecipeDisplayOption)o);
        }

        recipeFilterRowTotal = (int)Math.ceil(filteredRecipes.size() / 3d);
        recipeFilterRow = MathUtils.clamp(recipeFilterRow, 0, recipeFilterRowTotal - 5);

        menu.blockEntity.forceDisplayedRecipeUpdate = false;
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);

        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        //Main Panel
        gui.blit(TEXTURE, x, y, 0, 0, PANEL_MAIN_W, PANEL_MAIN_H);

        //Recipe Selector Panel
        gui.blit(TEXTURE_EXT, x + PANEL_RECIPE_X, y + PANEL_RECIPE_Y, PANEL_RECIPE_U, PANEL_RECIPE_V, PANEL_RECIPE_W, PANEL_RECIPE_H);

        //Power Settings Panel
        gui.blit(TEXTURE_EXT, x + PANEL_POWER_X, y + PANEL_POWER_Y, PANEL_POWER_U, PANEL_POWER_V, PANEL_POWER_W, PANEL_POWER_H);

        renderProgressBar(gui, x + 79, y + 39);

        renderSelectedRecipe(gui, x + 84, y + 79);

        renderPowerLevelBar(gui, x + 192, y + 37);

        renderSlotGhosts(gui);

        if(!menu.blockEntity.hasSufficientPower()) {
            RenderSystem.setShaderTexture(0, TEXTURE_EXT);
            renderPowerWarning(gui, x, y);
        }

        //Scroll Nubbin for Recipe Selector
        if(recipeFilterRowTotal > 5) {
            float percent = (float)recipeFilterRow / (float)(recipeFilterRowTotal - 5);
            int nubbinShift = (int)Math.floor(percent * 80);
            gui.blit(TEXTURE, x - 20, y + 40 + nubbinShift, 28, 230, 8, 8);
        }

        //Philosopher's Stone hole
        gui.blit(TEXTURE, x + PANEL_STONE_X, y + PANEL_STONE_Y, 0, 198, 32, 32);
        if(menu.blockEntity.getStoneItem().isEmpty())
            gui.blit(TEXTURE, x + 197, y + 107, 54, 202, 18, 18);

        //Batch Size Selector
        gui.blit(TEXTURE_EXT, x + PANEL_BATCH_X, y + PANEL_BATCH_Y, 0, 168, PANEL_BATCH_W, PANEL_BATCH_H);

        //Scroll Nubbin for Batch Size
        int batchLimit = 1;
        if(menu.blockEntity.getCurrentRecipe() instanceof DistillationFabricationRecipe item) batchLimit = item.getBatchSize();
        else if(menu.blockEntity.getCurrentRecipe() instanceof FluidDistillationFabricationRecipe fluid) batchLimit = fluid.getBatchSize();
        if(batchLimit > 1) {
            float percent = (float)(menu.blockEntity.getBatchSize() - 1) / (float)(batchLimit - 1);
            int nubbinShift = (int)Math.floor(percent * 57);
            gui.blit(TEXTURE, x - 77 + nubbinShift, y + 171, 28, 230, 8, 8);
        }

        if(!menu.blockEntity.getFluidInTank(0).isEmpty()) {
            IClientFluidTypeExtensions extension = IClientFluidTypeExtensions.of(menu.blockEntity.getFluidInTank(0).getFluid());
            int packedTint = extension.getTintColor();
            float a = ((packedTint >> 24) & 0xff) / 255.0f;
            float r = ((packedTint >> 16) & 0xff) / 255.0f;
            float g = ((packedTint >> 8) & 0xff) / 255.0f;
            float b = ((packedTint) & 0xff) / 255.0f;
            gui.setColor(r,g,b,a);

            int height = menu.blockEntity.getFluidInTank(0).getAmount() * 88 / menu.blockEntity.getTankCapacity(0);

            ResourceLocation rl = new ResourceLocation(extension.getStillTexture().getNamespace(), "textures/"+extension.getStillTexture().getPath()+".png");
            gui.blit(rl, x + 161, y + 96 - height, 0, 0, 12, height, 16, 16);

            gui.setColor(1.0f,1.0f,1.0f,1.0f);
        }
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        renderBackground(gui);
        super.render(gui, mouseX, mouseY, delta);
        renderTooltip(gui, mouseX, mouseY);
        updateDisplayedRecipes(recipeFilterBox == null ? "" : recipeFilterBox.getValue());
        renderRecipeOptions(gui);
        updateFilterBoxContents();
    }

    private void renderButtons() {

        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        b_powerLevelUp.setPosition(x+190, y+26);
        b_powerLevelUp.active = true;
        b_powerLevelUp.visible = true;

        b_powerLevelDown.setPosition(x+190, y+71);
        b_powerLevelDown.active = true;
        b_powerLevelDown.visible = true;

        for(ButtonData bd : recipeSelectButtons) {
            bd.getButton().setPosition(x+bd.getXOffset(), y+bd.getYOffset());
            bd.getButton().active = true;
            bd.getButton().visible = true;
        }
    }

    private void renderPowerLevelBar(GuiGraphics gui, int x, int y) {
        int powerLevel = menu.blockEntity.getPowerUsageSetting();

        gui.blit(TEXTURE, x, y + (30 - powerLevel), 46, 256 - powerLevel, 8, powerLevel);
    }

    private void renderSelectedRecipe(GuiGraphics gui, int x, int y) {
        if(menu.blockEntity.getCurrentRecipe() instanceof DistillationFabricationRecipe item) {
            if(item.getAlchemyObject().getItem() instanceof BlockItem) {
                gui.renderItem(item.getAlchemyObject(), x + 1, y + 1);
                if(menu.blockEntity.clearRecipeAfterNextProcess) gui.fill(RenderType.guiGhostRecipeOverlay(), x, y, x + 18, y + 18, 0x40ffffff);
            } else {
                float alpha = menu.blockEntity.clearRecipeAfterNextProcess ? 0.5f : 1.0f;
                gui.setColor(1, 1, 1, alpha);
                gui.renderItem(item.getAlchemyObject(), x + 1, y + 1);
                gui.setColor(1, 1, 1, 1);
            }
        }
        else if(menu.blockEntity.getCurrentRecipe() instanceof FluidDistillationFabricationRecipe fluid) {
            IClientFluidTypeExtensions extension = IClientFluidTypeExtensions.of(fluid.getAlchemyFluid().getFluid());
            int packedTint = extension.getTintColor();
            float a = ((packedTint >> 24) & 0xff) / 255.0f;
            float r = ((packedTint >> 16) & 0xff) / 255.0f;
            float g = ((packedTint >> 8) & 0xff) / 255.0f;
            float b = ((packedTint) & 0xff) / 255.0f;
            gui.setColor(r,g,b,a);
            ResourceLocation rl = new ResourceLocation(extension.getStillTexture().getNamespace(), "textures/"+extension.getStillTexture().getPath()+".png");
            gui.blit(rl, x+1, y+1, 0, 0, 16, 16, 16, 16);
            gui.setColor(1f,1f,1f,1f);
        }
        else {
            gui.blit(TEXTURE, x, y, 28, 238, 18, 18);
        }
    }

    private void renderProgressBar(GuiGraphics gui, int x, int y) {
        int sp = GrandCircleFabricationBlockEntity.getScaledProgress(menu.blockEntity);
        if(sp > 0)
            gui.blit(TEXTURE, x, y , 0, 230, sp, 26);
    }

    private void renderFilterBox() {
        int xOrigin = (width - PANEL_MAIN_W) / 2;
        int yOrigin = (height - PANEL_MAIN_H) / 2;

        recipeFilterBox.setX(xOrigin - 78);
        recipeFilterBox.setY(yOrigin + 17);

        if(recipeFilterBox.getValue().isEmpty())
            recipeFilterBox.setSuggestion(Component.translatable("gui.magichem.typetofilter").getString());
        else
            recipeFilterBox.setSuggestion("");

        addRenderableWidget(recipeFilterBox);
    }

    private void renderSlotGhosts(GuiGraphics gui) {
        int xOrigin = (width - PANEL_MAIN_W) / 2;
        int yOrigin = (height - PANEL_MAIN_H) / 2;

        if(menu.blockEntity.getCurrentRecipe() == null)
            return;

        gui.setColor(1f, 1f, 1f, 0.25f);
        int slotGroup = 0;
        if(menu.blockEntity.getCurrentRecipe() instanceof DistillationFabricationRecipe item) {
            for (ItemStack stack : item.getComponentMateria()) {
                gui.renderItem(stack, xOrigin + 31, yOrigin + 8 + (18 * slotGroup));
                gui.renderItem(stack, xOrigin + 49, yOrigin + 8 + (18 * slotGroup));
                slotGroup++;
            }
        }
        else if(menu.blockEntity.getCurrentRecipe() instanceof FluidDistillationFabricationRecipe fluid) {
            for (ItemStack stack : fluid.getComponentMateria()) {
                gui.renderItem(stack, xOrigin + 31, yOrigin + 8 + (18 * slotGroup));
                gui.renderItem(stack, xOrigin + 49, yOrigin + 8 + (18 * slotGroup));
                slotGroup++;
            }
        }
        gui.setColor(1f, 1f, 1f, 1f);
    }

    private void updateFilterBoxContents() {
        if(recipeFilterBox.getValue().isEmpty())
            recipeFilterBox.setSuggestion(Component.translatable("gui.magichem.typetofilter").getString());
        else
            recipeFilterBox.setSuggestion("");
    }

    private void renderRecipeOptions(GuiGraphics gui) {
        int xOrigin = (width - PANEL_MAIN_W) / 2;
        int yOrigin = (height - PANEL_MAIN_H) / 2;

        List<RecipeDisplayOption> snipped = new ArrayList<>();
        for(int i=recipeFilterRow*3; i<Math.min(filteredRecipes.size(), recipeFilterRow*3 + 15); i++) {
            snipped.add(filteredRecipes.get(i));
        }

        int c = 0;
        int cLimit = Math.min(15, snipped.size());
        while(c < cLimit) {

            for(int y=0; y<5; y++) {
                for (int x = 0; x < 3; x++) {
                    snipped.get(c).draw(gui, xOrigin-77 + x*18, yOrigin+40 + y*18);
                    c++;
                    if(c >= cLimit) break;
                }
                if(c >= cLimit) break;
            }
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {

        Font font = Minecraft.getInstance().font;
        List<Component> tooltipContents = new ArrayList<>();
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;
        boolean doOriginalTooltip = true;

        //Selected Recipe
        if(pX >= x+79 && pX <= x+97 &&
                pY >= y+79 && pY <= y+97) {
            if(menu.blockEntity.getCurrentRecipe() == null) {
                tooltipContents.add(Component.translatable("tooltip.magichem.gui.no_selected_recipe").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            } else {
                if(menu.blockEntity.getCurrentRecipe() instanceof DistillationFabricationRecipe item) {
                    ItemStack recipeItem = item.getAlchemyObject();

                    if (recipeItem.isEmpty()) {
                        tooltipContents.add(Component.translatable("tooltip.magichem.gui.no_selected_recipe").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                    } else {
                        tooltipContents.addAll(recipeItem.getTooltipLines(getMinecraft().player, TooltipFlag.NORMAL));
                    }
                }
                else if(menu.blockEntity.getCurrentRecipe() instanceof FluidDistillationFabricationRecipe fluid) {
                    FluidStack recipeFluid = fluid.getAlchemyFluid();

                    if (recipeFluid.isEmpty()) {
                        tooltipContents.add(Component.translatable("tooltip.magichem.gui.no_selected_recipe").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                    } else {
                        tooltipContents.add(Component.translatable(recipeFluid.getTranslationKey()));
                    }
                }
                else {
                    tooltipContents.add(Component.translatable("tooltip.magichem.gui.no_selected_recipe").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                }
            }
        }

        //Items in recipe picker
        if(pX >= x-78 && pX <= x-25 &&
                pY >= y+42 && pY <= y+132) {
            int mx = pX - (x-78);
            int my = pY - (y+42);
            int id = ((my / 18) * 3) + ((mx / 18) % 3);

            if (id >= 0 && id < 16) {
                if(id + recipeFilterRow * 3 < filteredRecipes.size()) {
                    tooltipContents.addAll(filteredRecipes.get(id + recipeFilterRow * 3).getTooltipLines());
                }
            }
        }

        //Item ghosts
        if(menu.blockEntity.getCurrentRecipe() != null) {
            if (pX >= x + 30 && pX <= x + 66 &&
                    pY >= y + 7 && pY <= y + 96) {
                int recipeIndex = (pY - (y + 7)) / 18;
                int left = pX <= x + 48 ? 0 : 1;
                int slotIndex = recipeIndex * 2 + left;

                ItemStack stackInSlot = menu.inputSlots[slotIndex].getItem();

                if(menu.blockEntity.getCurrentRecipe() instanceof DistillationFabricationRecipe item) {
                    if (stackInSlot.isEmpty() && recipeIndex < item.getComponentMateria().size()) {
                        String name = item.getComponentMateria().get(recipeIndex).getDisplayName().getString();
                        tooltipContents.add(Component.literal(name.substring(1, name.length() - 1)).withStyle(ChatFormatting.DARK_GRAY));
                    }
                }
                else if(menu.blockEntity.getCurrentRecipe() instanceof FluidDistillationFabricationRecipe fluid) {
                    if (stackInSlot.isEmpty() && recipeIndex < fluid.getComponentMateria().size()) {
                        String name = fluid.getComponentMateria().get(recipeIndex).getDisplayName().getString();
                        tooltipContents.add(Component.literal(name.substring(1, name.length() - 1)).withStyle(ChatFormatting.DARK_GRAY));
                    }
                }
            }
        }

        //Fluid Bar
        if(!menu.blockEntity.getFluidInTank(0).isEmpty()) {
            if (pX >= x + 160 && pX <= x + 174 &&
                    pY >= y + 7 && pY <= y + 97) {

                tooltipContents.add(Component.empty()
                        .append(Component.translatable(menu.blockEntity.getFluidInTank(0).getTranslationKey()).withStyle(ChatFormatting.GOLD)));
                tooltipContents.add(Component.empty()
                        .append(Component.literal(menu.blockEntity.getFluidInTank(0).getAmount() + " / " + menu.blockEntity.getTankCapacity(0)).withStyle(ChatFormatting.DARK_AQUA)));
                pGuiGraphics.renderTooltip(font, tooltipContents, Optional.empty(), pX, pY);
            }
        }

        if(doOriginalTooltip)
            super.renderTooltip(pGuiGraphics, pX, pY);

        pGuiGraphics.renderTooltip(font, tooltipContents, Optional.empty(), pX, pY);
    }

    protected void renderPowerWarning(GuiGraphics gui, int x, int y) {
        long cycle = Minecraft.getInstance().level.getGameTime() % 20;

        gui.blit(TEXTURE_EXT, x+10, y-30, 0, 230, 156, 26);
        if(cycle < 10) {
            gui.blit(TEXTURE_EXT, x + 17, y - 23, 156, 244, 12, 12);
            gui.blit(TEXTURE_EXT, x + 147, y - 23, 156, 244, 12, 12);
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        if(pMouseX >= x - 78 && pMouseX <= x - 12 &&
                pMouseY >= y + 42 && pMouseY <= y + 132) {
            if (recipeFilterRowTotal > 5) {
                if (pDelta < 0)
                    recipeFilterRow = Math.min(recipeFilterRowTotal - 5, recipeFilterRow + 1);
                else
                    recipeFilterRow = Math.max(0, recipeFilterRow - 1);
            }
        }

        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if(recipeFilterRowTotal > 5 && pButton == 0) {
            int x = (width - PANEL_MAIN_W) / 2;
            int y = (height - PANEL_MAIN_H) / 2;

            if (pMouseX >= x - 21 && pMouseX <= x - 12 &&
                    pMouseY >= y + 39 && pMouseY <= y + 129) {
                double point = pMouseY - (y + 42);
                double percent = point / 80d;

                recipeFilterRow = Math.max(0, Math.min(recipeFilterRowTotal - 5, (int) Math.round(percent * recipeFilterRowTotal)));
            }
        }

        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        //Recipe Selector Scroll Bar
        if(recipeFilterRowTotal > 5 && pButton == 0) {
            int x = (width - PANEL_MAIN_W) / 2;
            int y = (height - PANEL_MAIN_H) / 2;

            if (pMouseX >= x - 21 && pMouseX <= x - 12 &&
                    pMouseY >= y + 39 && pMouseY <= y + 129) {
                double point = pMouseY - (y + 42);
                double percent = point / 80d;

                recipeFilterRow = Math.max(0, Math.min(recipeFilterRowTotal - 5, (int) Math.round(percent * recipeFilterRowTotal)));
            }
        }

        //Batch Size Scroll Bar
        if(menu.blockEntity.getCurrentRecipe() != null){
            int maxBatch = 1;

            if(menu.blockEntity.getCurrentRecipe() instanceof DistillationFabricationRecipe item)
                maxBatch = item.getBatchSize();
            else if(menu.blockEntity.getCurrentRecipe() instanceof FluidDistillationFabricationRecipe fluid)
                maxBatch = fluid.getBatchSize();

            if (maxBatch > 1) {
                int x = (width - PANEL_MAIN_W) / 2;
                int y = (height - PANEL_MAIN_H) / 2;

                if (pMouseX >= x - 77 && pMouseX <= x - 15 &&
                        pMouseY >= y + 171 && pMouseY <= y + 179) {
                    double point = pMouseX - (x - 81);
                    double percent = point / 65d;

                    int newBatchSize = (int)Math.min(maxBatch,
                            Math.max(1,Math.round(percent * maxBatch)));
                    if(newBatchSize != menu.blockEntity.getBatchSize()) {
                        menu.blockEntity.setBatchSize(newBatchSize);
                        PacketRegistry.sendToServer(new FabricationBatchSizeC2SPacket(
                                menu.blockEntity.getBlockPos(),
                                newBatchSize
                        ));
                    }
                }
            }
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int pMouseX, int pMouseY) {
        int powerDraw = menu.blockEntity.getPowerDraw();
        int secWhole = menu.blockEntity.getOperationTicks() / 20;
        int secPartial = (menu.blockEntity.getOperationTicks() % 20) * 5;

        Font font = Minecraft.getInstance().font;
        gui.drawString(font ,powerDraw+"/t", 213, 26, 0xff000000, false);
        gui.drawString(font ,secWhole+"."+(secPartial < 10 ? "0"+secPartial : secPartial)+" s", 213, 45, 0xff000000, false);

        if (!menu.blockEntity.hasSufficientPower()) {
            MutableComponent warningText = Component.translatable("gui.magichem.insufficientpower");
            int width = Minecraft.getInstance().font.width(warningText.getString());
            gui.drawString(font, warningText, 89 - width / 2, -33, 0xff000000, false);
        }

        if(menu.blockEntity.getCurrentRecipe() instanceof DistillationFabricationRecipe itemRecipe) {
            for (int i = 0; i < itemRecipe.getComponentMateria().size(); i++) {
                Component text = Component.literal((itemRecipe.getComponentMateria().get(i).getCount() * menu.blockEntity.getBatchSize()) + "");
                int rightAlignShift = 15 - font.width(text.getString());

                gui.drawString(font, text, 6 + rightAlignShift, -1 + i * 18, 0xff000000, false);
            }

            if (itemRecipe.getOutputRate() < 1f) {
                int amt = (int) Math.round(1f / itemRecipe.getOutputRate());

                gui.drawString(font, amt < 9 ? "x" + amt : "" + amt, 99, 72, 0xff000000, false);
            }

            if (itemRecipe.getBatchSize() > 1) {
                int currentBatchSize = menu.blockEntity.getBatchSize();
                String str = currentBatchSize + " / " + itemRecipe.getBatchSize();
                int width = font.width(str);

                gui.drawString(font, str, -49 - width / 2, 141, 0xff000000, false);
            }
        }
        else if(menu.blockEntity.getCurrentRecipe() instanceof FluidDistillationFabricationRecipe fluidRecipe) {
            for (int i = 0; i < fluidRecipe.getComponentMateria().size(); i++) {
                Component text = Component.literal((fluidRecipe.getComponentMateria().get(i).getCount() * menu.blockEntity.getBatchSize()) + "");
                int rightAlignShift = 15 - font.width(text.getString());

                gui.drawString(font, text, 6 + rightAlignShift, -1 + i * 18, 0xff000000, false);
            }

            if (fluidRecipe.getOutputRate() < 1f) {
                int amt = (int) Math.round(1f / fluidRecipe.getOutputRate());

                gui.drawString(font, amt < 9 ? "x" + amt : "" + amt, 99, 72, 0xff000000, false);
            }

            if (!menu.blockEntity.hasSufficientPower()) {
                MutableComponent warningText = Component.translatable("gui.magichem.insufficientpower");
                int width = Minecraft.getInstance().font.width(warningText.getString());
                gui.drawString(font, warningText, 89 - width / 2, -33, 0xff000000, false);
            }

            if (fluidRecipe.getBatchSize() > 1) {
                int currentBatchSize = menu.blockEntity.getBatchSize();
                String str = currentBatchSize + " / " + fluidRecipe.getBatchSize();
                int width = font.width(str);

                gui.drawString(font, str, -49 - width / 2, 141, 0xff000000, false);
            }
        }
    }

    public static List<Rect2i> getGuiExtraAreas(GrandCircleFabricationScreen screen) {
        int xOrigin = (screen.width - PANEL_MAIN_W) / 2;
        int yOrigin = (screen.height - PANEL_MAIN_H) / 2;
        return List.of(
                new Rect2i(xOrigin + PANEL_RECIPE_X, yOrigin + PANEL_RECIPE_Y, PANEL_RECIPE_W,
                        PANEL_BATCH_H + PANEL_BATCH_Y - PANEL_RECIPE_Y),
                new Rect2i(xOrigin + PANEL_POWER_X, yOrigin + PANEL_POWER_Y, PANEL_POWER_W, PANEL_POWER_H),
                new Rect2i(xOrigin + PANEL_STONE_X, yOrigin + PANEL_STONE_Y, 32, 32)
        );
    }
}
