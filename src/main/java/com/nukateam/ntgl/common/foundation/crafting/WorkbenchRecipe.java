package com.nukateam.ntgl.common.foundation.crafting;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import com.nukateam.ntgl.common.foundation.init.ModRecipeSerializers;

public record WorkbenchRecipe(ItemStack result, ImmutableList<WorkbenchMaterial> materials) implements Recipe<EmptyWorkbenchRecipeInput> {
    public WorkbenchRecipe {
        result = result.copy();
        materials = ImmutableList.copyOf(materials);
    }

    public ItemStack getItem() {
        return this.result.copy();
    }

    @Override
    public boolean matches(EmptyWorkbenchRecipeInput input, Level worldIn) {
        return false;
    }

    @Override
    public ItemStack assemble(EmptyWorkbenchRecipeInput entity, HolderLookup.Provider access) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider access) {
        return this.result.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.WORKBENCH.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeType.WORKBENCH.get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        for (WorkbenchMaterial m : materials) {
            list.add(m.ingredient());
        }
        return list;
    }

    public boolean hasMaterials(Player player) {
        for (WorkbenchMaterial material : this.materials) {
            if (!material.has(player)) {
                return false;
            }
        }
        return true;
    }

    public void consumeMaterials(Player player) {
        for (WorkbenchMaterial material : this.materials) {
            material.consume(player);
        }
    }
}
