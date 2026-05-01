package com.nukateam.ntgl.common.jei;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.util.helpers.render.ModelRenderUtil;
import com.nukateam.ntgl.common.foundation.crafting.WorkbenchRecipe;
import com.nukateam.ntgl.modules.gunpack.regestry.ModBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.awt.*;

public class WorkbenchCategory implements IRecipeCategory<WorkbenchRecipe> {
    public static final ResourceLocation ID = ResourceLocation.tryBuild(Ntgl.MOD_ID, "workbench");
    public static final ResourceLocation BACKGROUND = ResourceLocation.tryBuild(Ntgl.MOD_ID, "textures/gui/workbench.png");
    public static final String TITLE_KEY = Ntgl.MOD_ID + ".category.workbench.title";
    public static final String MATERIALS_KEY = Ntgl.MOD_ID + ".category.workbench.materials";

    private final IDrawableStatic background;
    private final IDrawableStatic window;
    private final IDrawableStatic inventory;
    private final IDrawableStatic dyeSlot;
    private final IDrawable icon;
    private final Component title;

    public WorkbenchCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(162, 124);
        this.window = helper.createDrawable(BACKGROUND, 7, 15, 162, 72);
        this.inventory = helper.createDrawable(BACKGROUND, 7, 101, 162, 36);
        this.dyeSlot = helper.createDrawable(BACKGROUND, 7, 101, 18, 18);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.WORKBENCH.get()));
        this.title = Component.translatable(TITLE_KEY);
    }

    @Override
    public RecipeType<WorkbenchRecipe> getRecipeType() {
        return GunModPlugin.WORKBENCH;
    }

    @Override
    public Component getTitle() {
        return this.title;
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WorkbenchRecipe recipe, IFocusGroup focuses) {
        ItemStack output = recipe.getItem();
        for (int i = 0; i < recipe.materials().size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, (i % 8) * 18 + 1, 88 + (i / 8) * 18)
                    .addIngredients(recipe.materials().get(i).ingredient());
        }
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(output);
    }

    @Override
    public void draw(WorkbenchRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        this.window.draw(graphics, 0, 0);
        this.inventory.draw(graphics, 0, this.window.getHeight() + 2 + 11 + 2);
        this.dyeSlot.draw(graphics, 140, 51);

        graphics.drawString(Minecraft.getInstance().font, I18n.get(MATERIALS_KEY), 0, 78, Color.WHITE.getRGB());

        ItemStack output = recipe.getItem();
        MutableComponent displayName = output.getHoverName().copy();
        if (output.getCount() > 1) {
            displayName.append(Component.literal(" x " + output.getCount()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        }
        int titleX = this.window.getWidth() / 2;
        graphics.drawCenteredString(Minecraft.getInstance().font, displayName, titleX, 5, Color.WHITE.getRGB());

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(81, 40, 100);
        pose.scale(40F, 40F, 40F);
        pose.mulPose(Axis.XP.rotationDegrees(-5F));
        float partialTicks = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        pose.mulPose(Axis.YP.rotationDegrees(Minecraft.getInstance().player.tickCount + partialTicks));
        pose.scale(-1, -1, -1);

        BakedModel model = ModelRenderUtil.getModel(output);
        Lighting.setupFor3DItems();

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        Minecraft.getInstance().getItemRenderer().render(output, ItemDisplayContext.FIXED, false, pose, buffer, 15728880, OverlayTexture.NO_OVERLAY, model);
        buffer.endBatch();
        pose.popPose();
    }
}
