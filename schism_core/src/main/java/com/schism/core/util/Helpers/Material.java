package com.schism.core.util.Helpers;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class Material {
    public static final BlockBehaviour.Properties AIR;
    public static final BlockBehaviour.Properties STRUCTURAL_AIR;
    public static final BlockBehaviour.Properties PORTAL;
    public static final BlockBehaviour.Properties CLOTH_DECORATION;
    public static final BlockBehaviour.Properties PLANT;
    public static final BlockBehaviour.Properties WATER_PLANT;
    public static final BlockBehaviour.Properties REPLACEABLE_PLANT;
    public static final BlockBehaviour.Properties REPLACEABLE_FIREPROOF_PLANT;
    public static final BlockBehaviour.Properties REPLACEABLE_WATER_PLANT;
    public static final BlockBehaviour.Properties WATER;
    public static final BlockBehaviour.Properties BUBBLE_COLUMN;
    public static final BlockBehaviour.Properties LAVA;
    public static final BlockBehaviour.Properties TOP_SNOW;
    public static final BlockBehaviour.Properties FIRE;
    public static final BlockBehaviour.Properties DECORATION;
    public static final BlockBehaviour.Properties WEB;
    public static final BlockBehaviour.Properties SCULK;
    public static final BlockBehaviour.Properties BUILDABLE_GLASS;
    public static final BlockBehaviour.Properties CLAY;
    public static final BlockBehaviour.Properties DIRT;
    public static final BlockBehaviour.Properties GRASS;
    public static final BlockBehaviour.Properties ICE_SOLID;
    public static final BlockBehaviour.Properties SAND;
    public static final BlockBehaviour.Properties SPONGE;
    public static final BlockBehaviour.Properties SHULKER_SHELL;
    public static final BlockBehaviour.Properties WOOD;
    public static final BlockBehaviour.Properties NETHER_WOOD;
    public static final BlockBehaviour.Properties BAMBOO_SAPLING;
    public static final BlockBehaviour.Properties BAMBOO;
    public static final BlockBehaviour.Properties WOOL;
    public static final BlockBehaviour.Properties EXPLOSIVE;
    public static final BlockBehaviour.Properties LEAVES;
    public static final BlockBehaviour.Properties GLASS;
    public static final BlockBehaviour.Properties ICE;
    public static final BlockBehaviour.Properties CACTUS;
    public static final BlockBehaviour.Properties STONE;
    public static final BlockBehaviour.Properties METAL;
    public static final BlockBehaviour.Properties SNOW;
    public static final BlockBehaviour.Properties HEAVY_METAL;
    public static final BlockBehaviour.Properties BARRIER;
    public static final BlockBehaviour.Properties PISTON;
    public static final BlockBehaviour.Properties MOSS;
    public static final BlockBehaviour.Properties VEGETABLE;
    public static final BlockBehaviour.Properties EGG;
    public static final BlockBehaviour.Properties CAKE;
    public static final BlockBehaviour.Properties AMETHYST;
    public static final BlockBehaviour.Properties POWDER_SNOW;
    private final MapColor color;
    private final PushReaction pushReaction;
    private final boolean blocksMotion;
    private final boolean flammable;
    private final boolean liquid;
    private final boolean solidBlocking;
    private final boolean replaceable;
    private final boolean solid;

    public Material(MapColor p_76324_, boolean p_76325_, boolean p_76326_, boolean p_76327_, boolean p_76328_, boolean p_76329_, boolean p_76330_, PushReaction p_76331_) {
        this.color = p_76324_;
        this.liquid = p_76325_;
        this.solid = p_76326_;
        this.blocksMotion = p_76327_;
        this.solidBlocking = p_76328_;
        this.flammable = p_76329_;
        this.replaceable = p_76330_;
        this.pushReaction = p_76331_;
    }

    public boolean isLiquid() {
        return this.liquid;
    }

    public boolean isSolid() {
        return this.solid;
    }

    public boolean blocksMotion() {
        return this.blocksMotion;
    }

    public boolean isFlammable() {
        return this.flammable;
    }

    public boolean isReplaceable() {
        return this.replaceable;
    }

    public boolean isSolidBlocking() {
        return this.solidBlocking;
    }

    public PushReaction getPushReaction() {
        return this.pushReaction;
    }

    public MapColor getColor() {
        return this.color;
    }

    static {
        AIR = (new Builder(MapColor.NONE)).noCollider().nonSolid().replaceable().build();
        STRUCTURAL_AIR = (new Builder(MapColor.NONE)).noCollider().nonSolid().replaceable().build();
        PORTAL = (new Builder(MapColor.NONE)).noCollider().nonSolid().notPushable().build();
        CLOTH_DECORATION = (new Builder(MapColor.WOOL)).noCollider().nonSolid().flammable().build();
        PLANT = (new Builder(MapColor.PLANT)).noCollider().nonSolid().destroyOnPush().build();
        WATER_PLANT = (new Builder(MapColor.WATER)).noCollider().nonSolid().destroyOnPush().build();
        REPLACEABLE_PLANT = (new Builder(MapColor.PLANT)).noCollider().nonSolid().destroyOnPush().replaceable().flammable().build();
        REPLACEABLE_FIREPROOF_PLANT = (new Builder(MapColor.PLANT)).noCollider().nonSolid().destroyOnPush().replaceable().build();
        REPLACEABLE_WATER_PLANT = (new Builder(MapColor.WATER)).noCollider().nonSolid().destroyOnPush().replaceable().build();
        WATER = (new Builder(MapColor.WATER)).noCollider().nonSolid().destroyOnPush().replaceable().liquid().build();
        BUBBLE_COLUMN = (new Builder(MapColor.WATER)).noCollider().nonSolid().destroyOnPush().replaceable().liquid().build();
        LAVA = (new Builder(MapColor.FIRE)).noCollider().nonSolid().destroyOnPush().replaceable().liquid().build();
        TOP_SNOW = (new Builder(MapColor.SNOW)).noCollider().nonSolid().destroyOnPush().replaceable().build();
        FIRE = (new Builder(MapColor.NONE)).noCollider().nonSolid().destroyOnPush().replaceable().build();
        DECORATION = (new Builder(MapColor.NONE)).noCollider().nonSolid().destroyOnPush().build();
        WEB = (new Builder(MapColor.WOOL)).noCollider().destroyOnPush().build();
        SCULK = (new Builder(MapColor.COLOR_BLACK)).build();
        BUILDABLE_GLASS = (new Builder(MapColor.NONE)).build();
        CLAY = (new Builder(MapColor.CLAY)).build();
        DIRT = (new Builder(MapColor.DIRT)).build();
        GRASS = (new Builder(MapColor.GRASS)).build();
        ICE_SOLID = (new Builder(MapColor.ICE)).build();
        SAND = (new Builder(MapColor.SAND)).build();
        SPONGE = (new Builder(MapColor.COLOR_YELLOW)).build();
        SHULKER_SHELL = (new Builder(MapColor.COLOR_PURPLE)).build();
        WOOD = (new Builder(MapColor.WOOD)).flammable().build();
        NETHER_WOOD = (new Builder(MapColor.WOOD)).build();
        BAMBOO_SAPLING = (new Builder(MapColor.WOOD)).flammable().destroyOnPush().noCollider().build();
        BAMBOO = (new Builder(MapColor.WOOD)).flammable().destroyOnPush().build();
        WOOL = (new Builder(MapColor.WOOL)).flammable().build();
        EXPLOSIVE = (new Builder(MapColor.FIRE)).flammable().build();
        LEAVES = (new Builder(MapColor.PLANT)).flammable().destroyOnPush().build();
        GLASS = (new Builder(MapColor.NONE)).build();
        ICE = (new Builder(MapColor.ICE)).build();
        CACTUS = (new Builder(MapColor.PLANT)).destroyOnPush().build();
        STONE = (new Builder(MapColor.STONE)).build();
        METAL = (new Builder(MapColor.METAL)).build();
        SNOW = (new Builder(MapColor.SNOW)).build();
        HEAVY_METAL = (new Builder(MapColor.METAL)).notPushable().build();
        BARRIER = (new Builder(MapColor.NONE)).notPushable().build();
        PISTON = (new Builder(MapColor.STONE)).notPushable().build();
        MOSS = (new Builder(MapColor.PLANT)).destroyOnPush().build();
        VEGETABLE = (new Builder(MapColor.PLANT)).destroyOnPush().build();
        EGG = (new Builder(MapColor.PLANT)).destroyOnPush().build();
        CAKE = (new Builder(MapColor.NONE)).destroyOnPush().build();
        AMETHYST = (new Builder(MapColor.COLOR_PURPLE)).build();
        POWDER_SNOW = (new Builder(MapColor.SNOW)).nonSolid().noCollider().build();
    }

    public static class Builder {
        private PushReaction pushReaction;
        private boolean blocksMotion;
        private boolean flammable;
        private boolean liquid;
        private boolean replaceable;
        private boolean solid;
        private final MapColor color;

        public Builder(MapColor p_76349_) {
            this.pushReaction = PushReaction.NORMAL;
            this.blocksMotion = true;
            this.solid = true;
            this.color = p_76349_;
        }

        public Builder liquid() {
            this.liquid = true;
            return this;
        }

        public Builder nonSolid() {
            this.solid = false;
            return this;
        }

        public Builder noCollider() {
            this.blocksMotion = false;
            return this;
        }



        protected Builder flammable() {
            this.flammable = true;
            return this;
        }

        public Builder replaceable() {
            this.replaceable = true;
            return this;
        }

        protected Builder destroyOnPush() {
            this.pushReaction = PushReaction.DESTROY;
            return this;
        }

        protected Builder notPushable() {
            this.pushReaction = PushReaction.BLOCK;
            return this;
        }

        public BlockBehaviour.Properties build() {
            var props = BlockBehaviour.Properties.of();
            if (this.liquid) {
                props.liquid();
            }
            if (!this.blocksMotion) {
                props.noCollission();
            }
            if (!this.solid) {
                props.forceSolidOn();
            }
            if (this.flammable) {
                props.ignitedByLava();
            }
            if (this.replaceable) {
                props.replaceable();
            }
            props.pushReaction(this.pushReaction);
            props.mapColor(this.color);
            return props;
        }
    }
}
