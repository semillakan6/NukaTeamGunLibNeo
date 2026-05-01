package com.nukateam.ntgl.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.nukateam.chassis_core.common.foundation.item.StackUtils;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.CompositeModel;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.neoforged.neoforge.client.model.geometry.StandaloneGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.UnbakedGeometryHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

public class GunIconModels implements IUnbakedGeometry<GunIconModels> {
    private static final ResourceLocation BAKE_NAME = ResourceLocation.tryBuild(Ntgl.MOD_ID, "gun_icon");

    @Nonnull
    private final ItemStack stack;

    public GunIconModels(ItemStack stack) {
        this.stack = stack;
    }

    public GunIconModels withStack(ItemStack stack) {
        return new GunIconModels(stack);
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter,
                           ModelState modelState, ItemOverrides overrides) {
        Material particleLocation = context.hasMaterial("particle")
                ? context.getMaterial("particle")
                : new Material(InventoryMenu.BLOCK_ATLAS, MissingTextureAtlasSprite.getLocation());
        TextureAtlasSprite particleSprite = spriteGetter.apply(particleLocation);

        var itemContext = StandaloneGeometryBakingContext.builder(context)
                .withGui3d(false)
                .withUseBlockLight(false)
                .build(BAKE_NAME);

        var overrideHandler = new ItemOverrideHandler(overrides, baker, itemContext, this);

        var builder = CompositeModel.Baked.builder(itemContext, particleSprite, overrideHandler, context.getTransforms());

        String skin = StackUtils.getVariant(stack);
        var itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String namespace = itemKey != null ? itemKey.getNamespace() : Ntgl.MOD_ID;
        String nameItem = itemKey != null ? itemKey.getPath() : "missing";
        ResourceLocation texture = getTexture(namespace, nameItem, skin);
        var baseMaterial = new Material(InventoryMenu.BLOCK_ATLAS, texture);
        Material baseLocation = getMaterial(context, "base");
        TextureAtlasSprite sprite;
        if (!stack.isEmpty()) {
            sprite = spriteGetter.apply(baseMaterial);
        } else if (baseLocation != null) {
            sprite = spriteGetter.apply(baseLocation);
        } else {
            sprite = spriteGetter.apply(baseMaterial);
        }

        var unbaked = UnbakedGeometryHelper.createUnbakedItemElements(0, sprite);
        var quads = UnbakedGeometryHelper.bakeElements(unbaked, spriteGetter, modelState);

        builder.addQuads(getLayerRenderTypes(), quads);
        builder.setParticle(particleSprite);
        return builder.build();
    }

    private static String getItemName(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
    }

    private static ResourceLocation getTexture(String namespace, String nameItem, String skin) {
        return ResourceLocation.tryBuild(namespace, "item/dynamic/" + nameItem + "/" + nameItem + "_" + skin);
    }

    public static RenderTypeGroup getLayerRenderTypes() {
        return new RenderTypeGroup(RenderType.translucent(), RenderType.translucent());
    }

    public enum Loader implements IGeometryLoader<GunIconModels> {
        INSTANCE;

        @Override
        public GunIconModels read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) {
            return new GunIconModels(ItemStack.EMPTY);
        }
    }

    @Nullable
    private static Material getMaterial(IGeometryBakingContext context, String base) {
        return context.hasMaterial(base) ? context.getMaterial(base) : null;
    }

    private static final class ItemOverrideHandler extends ItemOverrides {
        private final ItemOverrides nested;
        private final ModelBaker baker;
        private final IGeometryBakingContext owner;
        private final GunIconModels parent;

        private ItemOverrideHandler(ItemOverrides nested, ModelBaker baker, IGeometryBakingContext owner, GunIconModels parent) {
            this.nested = nested;
            this.baker = baker;
            this.owner = owner;
            this.parent = parent;
        }

        @Override
        public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel level,
                                  @Nullable LivingEntity entity, int seed) {
            BakedModel overriden = nested.resolve(originalModel, stack, level, entity, seed);
            if (overriden != originalModel) return overriden;
            if (!StackUtils.getVariant(stack).equals("default")) {
                GunIconModels unbaked = this.parent.withStack(stack);
                return unbaked.bake(owner, baker, Material::sprite, BlockModelRotation.X0_Y0, nested);
            }
            return originalModel;
        }
    }
}
