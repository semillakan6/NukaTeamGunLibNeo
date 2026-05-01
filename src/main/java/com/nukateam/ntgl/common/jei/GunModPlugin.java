package com.nukateam.ntgl.common.jei;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.crafting.ModRecipeType;
import com.nukateam.ntgl.common.foundation.crafting.WorkbenchRecipe;
import com.nukateam.ntgl.modules.gunpack.regestry.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;
import java.util.Objects;

@JeiPlugin
public class GunModPlugin implements IModPlugin {
    public static final RecipeType<WorkbenchRecipe> WORKBENCH = RecipeType.create(Ntgl.MOD_ID, "workbench", WorkbenchRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.tryBuild(Ntgl.MOD_ID, "crafting");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper helper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new WorkbenchCategory(helper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ClientLevel world = Objects.requireNonNull(Minecraft.getInstance().level);
        List<RecipeHolder<WorkbenchRecipe>> holders = world.getRecipeManager().getAllRecipesFor(ModRecipeType.WORKBENCH.get());
        List<WorkbenchRecipe> recipes = holders.stream().map(RecipeHolder::value).toList();
        registration.addRecipes(WORKBENCH, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.WORKBENCH.get()), WORKBENCH);
    }
}
