package com.nukateam.ntgl.common.foundation.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Workbench recipes are not matched via standard crafting; this placeholder satisfies {@link net.minecraft.world.item.crafting.Recipe}.
 */
public enum EmptyWorkbenchRecipeInput implements RecipeInput {
    INSTANCE;

    @Override
    public ItemStack getItem(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 0;
    }
}
