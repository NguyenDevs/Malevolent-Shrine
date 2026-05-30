package dev.nguyendevs.malevolentshrine.schematic;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import dev.nguyendevs.malevolentshrine.domain.BlockPos;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.Inflater;

public class ShrineSchematic {
    private static final int W = 43, H = 54, L = 45;

    private static final BlockData[] PALETTE = new BlockData[460];
    private static final byte[] DATA;

    static {
        String[] paletteStrs = {
            "minecraft:air",
            "minecraft:netherrack",
            "minecraft:magma_block",
            "minecraft:blackstone_wall[east=none,north=none,south=none,up=true,waterlogged=false,west=none]",
            "minecraft:blackstone_wall[east=low,north=none,south=low,up=true,waterlogged=false,west=none]",
            "minecraft:black_stained_glass_pane[east=false,north=false,south=false,waterlogged=false,west=true]",
            "minecraft:black_stained_glass_pane[east=false,north=true,south=false,waterlogged=false,west=false]",
            "minecraft:pale_oak_trapdoor[facing=north,half=top,open=false,powered=false,waterlogged=false]",
            "minecraft:black_stained_glass_pane[east=false,north=false,south=true,waterlogged=false,west=false]",
            "minecraft:blackstone_wall[east=low,north=low,south=none,up=true,waterlogged=false,west=none]",
            "minecraft:pale_oak_trapdoor[facing=east,half=top,open=false,powered=false,waterlogged=false]",
            "minecraft:black_stained_glass_pane[east=true,north=false,south=false,waterlogged=false,west=false]",
            "minecraft:blackstone_wall[east=none,north=none,south=low,up=true,waterlogged=false,west=low]",
            "minecraft:pale_oak_trapdoor[facing=south,half=top,open=false,powered=false,waterlogged=false]",
            "minecraft:blackstone_wall[east=none,north=low,south=none,up=true,waterlogged=false,west=low]",
            "minecraft:pale_oak_trapdoor[facing=east,half=top,open=false,powered=true,waterlogged=false]",
            "minecraft:blackstone_wall[east=tall,north=none,south=tall,up=true,waterlogged=false,west=none]",
            "minecraft:black_stained_glass_pane[east=false,north=false,south=true,waterlogged=false,west=true]",
            "minecraft:nether_brick_slab[type=top,waterlogged=false]",
            "minecraft:black_stained_glass_pane[east=true,north=true,south=false,waterlogged=false,west=false]",
            "minecraft:blackstone_wall[east=none,north=low,south=low,up=true,waterlogged=false,west=low]",
            "minecraft:pale_oak_stairs[facing=south,half=top,shape=outer_right,waterlogged=false]",
            "minecraft:pale_oak_trapdoor[facing=west,half=top,open=true,powered=false,waterlogged=false]",
            "minecraft:pale_oak_planks",
            "minecraft:pale_oak_stairs[facing=west,half=top,shape=straight,waterlogged=false]",
            "minecraft:black_stained_glass_pane[east=true,north=false,south=true,waterlogged=false,west=false]",
            "minecraft:pale_oak_stairs[facing=north,half=top,shape=outer_left,waterlogged=false]",
            "minecraft:blackstone_wall[east=tall,north=tall,south=none,up=true,waterlogged=false,west=none]",
            "minecraft:black_stained_glass_pane[east=false,north=true,south=false,waterlogged=false,west=true]",
            "minecraft:pale_oak_fence_gate[facing=east,in_wall=false,open=true,powered=false]",
            "minecraft:wither_skeleton_wall_skull[facing=west,powered=false]",
            "minecraft:cherry_stairs[facing=west,half=top,shape=straight,waterlogged=false]",
            "minecraft:blackstone",
            "minecraft:blackstone_wall[east=none,north=none,south=tall,up=true,waterlogged=false,west=tall]",
            "minecraft:pale_oak_stairs[facing=south,half=top,shape=outer_left,waterlogged=false]",
            "minecraft:blackstone_wall[east=low,north=low,south=low,up=true,waterlogged=false,west=none]",
            "minecraft:pale_oak_stairs[facing=east,half=top,shape=straight,waterlogged=false]",
            "minecraft:pale_oak_trapdoor[facing=east,half=top,open=true,powered=false,waterlogged=false]",
            "minecraft:cherry_trapdoor[facing=north,half=top,open=false,powered=false,waterlogged=false]",
            "minecraft:pale_oak_stairs[facing=north,half=top,shape=outer_right,waterlogged=false]",
            "minecraft:blackstone_wall[east=none,north=tall,south=none,up=true,waterlogged=false,west=tall]",
            "minecraft:pale_oak_wall_sign[facing=north,waterlogged=false]",
            "minecraft:pale_oak_fence_gate[facing=south,in_wall=false,open=false,powered=false]",
            "minecraft:pale_oak_stairs[facing=south,half=top,shape=straight,waterlogged=false]",
            "minecraft:pale_oak_stairs[facing=west,half=top,shape=outer_left,waterlogged=false]",
            "minecraft:pale_oak_trapdoor[facing=west,half=bottom,open=false,powered=false,waterlogged=false]",
            "minecraft:hopper[enabled=true,facing=east]",
            "minecraft:hopper[enabled=true,facing=west]",
            "minecraft:pale_oak_wall_sign[facing=south,waterlogged=false]",
            "minecraft:pale_oak_fence_gate[facing=north,in_wall=false,open=false,powered=false]",
            "minecraft:pale_oak_stairs[facing=north,half=top,shape=straight,waterlogged=false]",
            "minecraft:pale_oak_stairs[facing=west,half=top,shape=outer_right,waterlogged=false]",
            "minecraft:cherry_trapdoor[facing=west,half=top,open=false,powered=false,waterlogged=false]",
            "minecraft:pale_oak_trapdoor[facing=west,half=top,open=false,powered=false,waterlogged=false]",
            "minecraft:pale_oak_fence_gate[facing=south,in_wall=false,open=true,powered=false]",
            "minecraft:cherry_fence_gate[facing=east,in_wall=false,open=true,powered=false]",
            "minecraft:cherry_stairs[facing=north,half=top,shape=straight,waterlogged=false]",
            "minecraft:cherry_planks",
            "minecraft:pale_oak_fence[east=false,north=false,south=false,waterlogged=false,west=true]",
            "minecraft:nether_brick_slab[type=double,waterlogged=true]",
            "minecraft:cherry_fence[east=false,north=true,south=false,waterlogged=false,west=false]",
            "minecraft:pale_oak_slab[type=top,waterlogged=false]",
            "minecraft:pale_oak_wall_sign[facing=east,waterlogged=false]",
            "minecraft:nether_brick_slab[type=double,waterlogged=false]",
            "minecraft:cherry_trapdoor[facing=east,half=top,open=false,powered=false,waterlogged=false]",
            "minecraft:hopper[enabled=true,facing=south]",
            "minecraft:pale_oak_fence_gate[facing=west,in_wall=false,open=true,powered=false]",
            "minecraft:cherry_wall_sign[facing=south,waterlogged=false]",
            "minecraft:blackstone_slab[type=double,waterlogged=false]",
            "minecraft:nether_bricks",
            "minecraft:hopper[enabled=true,facing=north]",
            "minecraft:pale_oak_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]",
            "minecraft:nether_brick_slab[type=bottom,waterlogged=true]",
            "minecraft:pale_oak_fence_gate[facing=north,in_wall=false,open=true,powered=false]",
            "minecraft:pale_oak_stairs[facing=east,half=top,shape=outer_right,waterlogged=false]",
            "minecraft:pale_oak_fence[east=true,north=false,south=false,waterlogged=false,west=false]",
            "minecraft:bubble_column[drag=true]",
            "minecraft:pale_oak_wall_sign[facing=west,waterlogged=false]",
            "minecraft:pale_oak_fence[east=false,north=true,south=false,waterlogged=false,west=false]",
            "minecraft:cherry_stairs[facing=east,half=top,shape=outer_left,waterlogged=false]",
            "minecraft:cherry_fence_gate[facing=north,in_wall=false,open=false,powered=false]",
            "minecraft:stone",
            "minecraft:cherry_wall_sign[facing=north,waterlogged=false]",
            "minecraft:cherry_wall_sign[facing=west,waterlogged=false]",
            "minecraft:cherry_slab[type=top,waterlogged=false]",
            "minecraft:pale_oak_fence[east=false,north=false,south=true,waterlogged=false,west=false]",
            "minecraft:cherry_fence[east=false,north=false,south=true,waterlogged=false,west=false]",
            "minecraft:cherry_stairs[facing=south,half=top,shape=straight,waterlogged=false]",
            "minecraft:cherry_stairs[facing=east,half=top,shape=straight,waterlogged=false]",
            "minecraft:blackstone_wall[east=tall,north=tall,south=tall,up=false,waterlogged=false,west=tall]",
            "minecraft:pale_oak_slab[type=bottom,waterlogged=false]",
            "minecraft:cherry_stairs[facing=east,half=top,shape=straight,waterlogged=true]",
            "minecraft:pale_oak_fence_gate[facing=east,in_wall=false,open=false,powered=false]",
            "minecraft:pale_oak_fence_gate[facing=west,in_wall=false,open=false,powered=false]",
            "minecraft:pale_oak_trapdoor[facing=south,half=bottom,open=false,powered=false,waterlogged=false]",
            "minecraft:wither_skeleton_wall_skull[facing=north,powered=false]",
            "minecraft:pale_oak_trapdoor[facing=north,half=bottom,open=false,powered=false,waterlogged=false]",
            "minecraft:cherry_fence_gate[facing=east,in_wall=false,open=false,powered=false]",
            "minecraft:blackstone_wall[east=low,north=none,south=tall,up=true,waterlogged=false,west=tall]",
            "minecraft:nether_brick_slab[type=bottom,waterlogged=false]",
            "minecraft:daylight_detector[inverted=false,power=5]",
            "minecraft:blackstone_wall[east=low,north=tall,south=none,up=true,waterlogged=false,west=tall]",
            "minecraft:cherry_slab[type=bottom,waterlogged=false]",
            "minecraft:cherry_fence[east=true,north=false,south=false,waterlogged=false,west=false]",
            "minecraft:cherry_fence_gate[facing=west,in_wall=false,open=true,powered=false]",
            "minecraft:diorite_stairs[facing=east,half=top,shape=straight,waterlogged=false]",
            "minecraft:diorite_wall[east=none,north=none,south=low,up=true,waterlogged=false,west=low]",
            "minecraft:diorite_slab[type=top,waterlogged=false]",
            "minecraft:pale_oak_trapdoor[facing=east,half=bottom,open=false,powered=false,waterlogged=false]",
            "minecraft:diorite_wall[east=tall,north=none,south=none,up=false,waterlogged=false,west=tall]",
            "minecraft:diorite_wall[east=none,north=low,south=none,up=true,waterlogged=false,west=low]",
            "minecraft:diorite_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]",
            "minecraft:cherry_wall_sign[facing=east,waterlogged=false]",
            "minecraft:pale_oak_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]",
            "minecraft:cherry_fence[east=false,north=false,south=false,waterlogged=false,west=true]",
            "minecraft:pale_oak_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]",
            "minecraft:cherry_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]",
            "minecraft:cherry_trapdoor[facing=east,half=bottom,open=true,powered=false,waterlogged=false]",
            "minecraft:mangrove_fence[east=true,north=false,south=true,waterlogged=false,west=false]",
            "minecraft:red_stained_glass_pane[east=false,north=false,south=true,waterlogged=false,west=true]",
            "minecraft:red_stained_glass_pane[east=true,north=false,south=true,waterlogged=false,west=false]",
            "minecraft:mangrove_fence[east=false,north=false,south=true,waterlogged=false,west=true]",
            "minecraft:wither_skeleton_wall_skull[facing=east,powered=false]",
            "minecraft:blackstone_wall[east=tall,north=tall,south=low,up=true,waterlogged=false,west=none]",
            "minecraft:blackstone_wall[east=none,north=tall,south=low,up=true,waterlogged=false,west=tall]",
            "minecraft:red_terracotta",
            "minecraft:blackstone_wall[east=tall,north=none,south=tall,up=true,waterlogged=false,west=low]",
            "minecraft:red_stained_glass_pane[east=true,north=true,south=false,waterlogged=false,west=false]",
            "minecraft:red_stained_glass_pane[east=false,north=true,south=false,waterlogged=false,west=true]",
            "minecraft:wither_skeleton_skull[powered=false,rotation=3]",
            "minecraft:blackstone_wall[east=tall,north=tall,south=none,up=true,waterlogged=false,west=low]",
            "minecraft:wither_skeleton_skull[powered=false,rotation=5]",
            "minecraft:black_stained_glass_pane[east=true,north=true,south=false,waterlogged=false,west=true]",
            "minecraft:lava[level=0]",
            "minecraft:diorite_wall[east=none,north=tall,south=tall,up=false,waterlogged=false,west=none]",
            "minecraft:diorite_stairs[facing=north,half=top,shape=straight,waterlogged=false]",
            "minecraft:diorite_stairs[facing=west,half=bottom,shape=straight,waterlogged=false]",
            "minecraft:diorite_stairs[facing=south,half=top,shape=straight,waterlogged=false]",
            "minecraft:diorite_wall[east=low,north=none,south=low,up=true,waterlogged=false,west=none]",
            "minecraft:diorite_wall[east=low,north=low,south=none,up=true,waterlogged=false,west=none]",
            "minecraft:diorite_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]",
            "minecraft:pale_oak_stairs[facing=west,half=bottom,shape=straight,waterlogged=false]",
            "minecraft:mangrove_fence[east=true,north=true,south=false,waterlogged=false,west=false]",
            "minecraft:mangrove_fence[east=false,north=true,south=false,waterlogged=false,west=true]",
            "minecraft:blackstone_wall[east=tall,north=low,south=none,up=true,waterlogged=false,west=none]",
            "minecraft:blackstone_wall[east=none,north=low,south=tall,up=true,waterlogged=false,west=tall]",
            "minecraft:blackstone_wall[east=tall,north=low,south=tall,up=true,waterlogged=false,west=none]",
            "minecraft:cherry_fence_gate[facing=west,in_wall=false,open=false,powered=false]",
            "minecraft:cherry_stairs[facing=east,half=top,shape=outer_right,waterlogged=false]",
            "minecraft:cherry_fence_gate[facing=south,in_wall=false,open=false,powered=false]",
            "minecraft:diorite_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]",
            "minecraft:diorite_stairs[facing=west,half=top,shape=straight,waterlogged=false]",
            "minecraft:pale_oak_stairs[facing=east,half=top,shape=outer_left,waterlogged=false]",
            "minecraft:blackstone_wall[east=none,north=low,south=low,up=true,waterlogged=false,west=tall]",
            "minecraft:cherry_trapdoor[facing=south,half=bottom,open=false,powered=false,waterlogged=false]",
            "minecraft:wither_skeleton_skull[powered=false,rotation=9]",
            "minecraft:wither_skeleton_skull[powered=false,rotation=7]",
            "minecraft:wither_skeleton_wall_skull[facing=south,powered=false]",
            "minecraft:wither_skeleton_skull[powered=false,rotation=15]",
            "minecraft:wither_skeleton_skull[powered=false,rotation=1]",
            "minecraft:cherry_trapdoor[facing=west,half=bottom,open=false,powered=false,waterlogged=false]",
            "minecraft:diorite_wall[east=none,north=none,south=none,up=true,waterlogged=false,west=low]",
            "minecraft:diorite_wall[east=none,north=low,south=none,up=true,waterlogged=false,west=none]",
            "minecraft:pale_oak_fence_gate[facing=south,in_wall=true,open=true,powered=false]",
            "minecraft:blackstone_wall[east=low,north=tall,south=none,up=true,waterlogged=false,west=low]",
            "minecraft:diorite",
            "minecraft:diorite_wall[east=none,north=low,south=none,up=true,waterlogged=false,west=tall]",
            "minecraft:wither_skeleton_skull[powered=false,rotation=13]",
            "minecraft:diorite_wall[east=tall,north=none,south=low,up=true,waterlogged=false,west=none]",
            "minecraft:nether_brick_wall[east=none,north=none,south=low,up=true,waterlogged=false,west=low]",
            "minecraft:nether_brick_wall[east=none,north=tall,south=tall,up=true,waterlogged=false,west=tall]",
            "minecraft:wither_skeleton_skull[powered=false,rotation=11]",
            "minecraft:pale_oak_stairs[facing=south,half=bottom,shape=outer_left,waterlogged=false]",
            "minecraft:nether_brick_wall[east=tall,north=none,south=tall,up=true,waterlogged=false,west=none]",
            "minecraft:pale_oak_trapdoor[facing=east,half=bottom,open=true,powered=false,waterlogged=false]",
            "minecraft:red_stained_glass_pane[east=true,north=false,south=true,waterlogged=false,west=true]",
            "minecraft:nether_brick_wall[east=tall,north=tall,south=none,up=true,waterlogged=false,west=none]",
            "minecraft:cherry_stairs[facing=north,half=bottom,shape=outer_right,waterlogged=false]",
            "minecraft:stripped_mangrove_log[axis=x]",
            "minecraft:black_stained_glass_pane[east=true,north=true,south=true,waterlogged=false,west=false]",
            "minecraft:red_stained_glass_pane[east=false,north=true,south=true,waterlogged=false,west=true]",
            "minecraft:nether_brick_wall[east=low,north=none,south=low,up=true,waterlogged=false,west=none]",
            "minecraft:nether_brick_wall[east=tall,north=none,south=tall,up=true,waterlogged=false,west=tall]",
            "minecraft:nether_brick_wall[east=none,north=none,south=tall,up=true,waterlogged=false,west=tall]",
            "minecraft:obsidian",
            "minecraft:black_stained_glass_pane[east=false,north=false,south=false,waterlogged=false,west=false]",
            "minecraft:cherry_trapdoor[facing=east,half=bottom,open=false,powered=false,waterlogged=false]",
            "minecraft:diorite_wall[east=tall,north=tall,south=none,up=true,waterlogged=false,west=none]",
            "minecraft:diorite_wall[east=none,north=none,south=low,up=true,waterlogged=false,west=none]",
            "minecraft:pale_oak_fence_gate[facing=east,in_wall=true,open=true,powered=false]",
            "minecraft:diorite_wall[east=low,north=tall,south=none,up=true,waterlogged=false,west=none]",
            "minecraft:diorite_wall[east=low,north=none,south=none,up=true,waterlogged=false,west=none]",
            "minecraft:diorite_wall[east=none,north=none,south=tall,up=true,waterlogged=false,west=low]",
            "minecraft:pale_oak_fence_gate[facing=west,in_wall=true,open=true,powered=false]",
            "minecraft:diorite_wall[east=none,north=none,south=tall,up=true,waterlogged=false,west=tall]",
            "minecraft:nether_brick_wall[east=none,north=tall,south=none,up=true,waterlogged=false,west=tall]",
            "minecraft:nether_brick_wall[east=tall,north=tall,south=none,up=true,waterlogged=false,west=tall]",
            "minecraft:nether_brick_wall[east=none,north=low,south=none,up=true,waterlogged=false,west=low]",
            "minecraft:cherry_trapdoor[facing=north,half=bottom,open=false,powered=false,waterlogged=false]",
            "minecraft:stripped_mangrove_log[axis=z]",
            "minecraft:stripped_mangrove_wood[axis=z]",
            "minecraft:nether_brick_wall[east=tall,north=tall,south=tall,up=true,waterlogged=false,west=none]",
            "minecraft:red_nether_brick_wall[east=none,north=none,south=none,up=true,waterlogged=false,west=none]",
            "minecraft:nether_brick_wall[east=low,north=low,south=none,up=true,waterlogged=false,west=none]",
            "minecraft:diorite_wall[east=none,north=tall,south=none,up=true,waterlogged=false,west=tall]",
            "minecraft:pale_oak_trapdoor[facing=south,half=top,open=true,powered=false,waterlogged=false]",
            "minecraft:pale_oak_fence_gate[facing=north,in_wall=true,open=true,powered=false]",
            "minecraft:blackstone_wall[east=tall,north=tall,south=none,up=true,waterlogged=false,west=tall]",
            "minecraft:cherry_fence_gate[facing=south,in_wall=false,open=true,powered=false]",
            "minecraft:skeleton_wall_skull[facing=west,powered=false]",
            "minecraft:skeleton_wall_skull[facing=east,powered=false]",
            "minecraft:pale_oak_trapdoor[facing=south,half=bottom,open=true,powered=false,waterlogged=false]",
            "minecraft:blackstone_wall[east=none,north=none,south=none,up=true,waterlogged=false,west=tall]",
            "minecraft:white_stained_glass_pane[east=true,north=false,south=true,waterlogged=false,west=false]",
            "minecraft:white_stained_glass_pane[east=false,north=false,south=true,waterlogged=false,west=true]",
            "minecraft:diorite_stairs[facing=north,half=bottom,shape=outer_right,waterlogged=false]",
            "minecraft:white_stained_glass_pane[east=true,north=true,south=false,waterlogged=false,west=false]",
            "minecraft:diorite_wall[east=low,north=tall,south=low,up=true,waterlogged=false,west=none]",
            "minecraft:nether_brick_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]",
            "minecraft:pale_oak_trapdoor[facing=west,half=bottom,open=true,powered=false,waterlogged=false]",
            "minecraft:blackstone_wall[east=low,north=low,south=none,up=true,waterlogged=false,west=low]",
            "minecraft:red_stained_glass_pane[east=true,north=true,south=true,waterlogged=false,west=true]",
            "minecraft:stripped_mangrove_wood[axis=x]",
            "minecraft:nether_brick_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]",
            "minecraft:nether_brick_stairs[facing=west,half=bottom,shape=straight,waterlogged=false]",
            "minecraft:nether_portal[axis=x]",
            "minecraft:diorite_wall[east=low,north=none,south=low,up=true,waterlogged=false,west=low]",
            "minecraft:white_stained_glass_pane[east=false,north=true,south=false,waterlogged=false,west=true]",
            "minecraft:nether_portal[axis=z]",
            "minecraft:diorite_stairs[facing=east,half=bottom,shape=outer_right,waterlogged=false]",
            "minecraft:skeleton_wall_skull[facing=north,powered=false]",
            "minecraft:skeleton_wall_skull[facing=south,powered=false]",
            "minecraft:diorite_stairs[facing=west,half=bottom,shape=outer_right,waterlogged=false]",
            "minecraft:diorite_wall[east=low,north=low,south=none,up=true,waterlogged=false,west=low]",
            "minecraft:red_stained_glass_pane[east=true,north=true,south=true,waterlogged=false,west=false]",
            "minecraft:nether_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]",
            "minecraft:mangrove_fence[east=false,north=true,south=true,waterlogged=false,west=true]",
            "minecraft:red_stained_glass_pane[east=true,north=true,south=false,waterlogged=false,west=true]",
            "minecraft:mangrove_planks",
            "minecraft:diorite_wall[east=none,north=low,south=low,up=true,waterlogged=false,west=low]",
            "minecraft:diorite_stairs[facing=south,half=bottom,shape=outer_right,waterlogged=false]",
            "minecraft:pale_oak_trapdoor[facing=north,half=bottom,open=true,powered=false,waterlogged=false]",
            "minecraft:red_stained_glass_pane[east=false,north=false,south=false,waterlogged=false,west=false]",
            "minecraft:cherry_fence_gate[facing=north,in_wall=false,open=true,powered=false]",
            "minecraft:pale_oak_stairs[facing=north,half=bottom,shape=outer_right,waterlogged=false]",
            "minecraft:cherry_stairs[facing=east,half=bottom,shape=outer_right,waterlogged=false]",
            "minecraft:pale_oak_stairs[facing=west,half=bottom,shape=outer_left,waterlogged=false]",
            "minecraft:pale_oak_stairs[facing=east,half=bottom,shape=outer_right,waterlogged=false]",
            "minecraft:blackstone_wall[east=low,north=none,south=low,up=true,waterlogged=false,west=low]",
            "minecraft:diorite_wall[east=tall,north=none,south=tall,up=true,waterlogged=false,west=none]",
            "minecraft:diorite_wall[east=none,north=tall,south=none,up=true,waterlogged=false,west=none]",
            "minecraft:diorite_stairs[facing=north,half=bottom,shape=outer_left,waterlogged=false]",
            "minecraft:white_stained_glass_pane[east=true,north=false,south=false,waterlogged=false,west=false]",
            "minecraft:pale_oak_stairs[facing=south,half=bottom,shape=outer_right,waterlogged=false]",
            "minecraft:cherry_stairs[facing=west,half=bottom,shape=straight,waterlogged=false]",
            "minecraft:black_stained_glass_pane[east=false,north=true,south=true,waterlogged=false,west=true]",
            "minecraft:cherry_stairs[facing=north,half=bottom,shape=outer_left,waterlogged=false]",
            "minecraft:blackstone_wall[east=tall,north=tall,south=low,up=true,waterlogged=false,west=tall]",
            "minecraft:diorite_wall[east=tall,north=none,south=none,up=true,waterlogged=false,west=none]",
            "minecraft:white_stained_glass_pane[east=false,north=true,south=false,waterlogged=false,west=false]",
            "minecraft:diorite_stairs[facing=east,half=bottom,shape=outer_left,waterlogged=false]",
            "minecraft:diorite_stairs[facing=west,half=bottom,shape=outer_left,waterlogged=false]",
            "minecraft:white_stained_glass_pane[east=false,north=false,south=true,waterlogged=false,west=false]",
            "minecraft:diorite_wall[east=none,north=none,south=none,up=true,waterlogged=false,west=tall]",
            "minecraft:white_stained_glass_pane[east=false,north=false,south=false,waterlogged=false,west=true]",
            "minecraft:diorite_stairs[facing=south,half=bottom,shape=outer_left,waterlogged=false]",
            "minecraft:diorite_wall[east=none,north=none,south=tall,up=true,waterlogged=false,west=none]",
            "minecraft:cherry_stairs[facing=east,half=bottom,shape=outer_left,waterlogged=false]",
            "minecraft:pale_oak_stairs[facing=west,half=bottom,shape=outer_right,waterlogged=false]",
            "minecraft:blackstone_wall[east=low,north=low,south=none,up=true,waterlogged=false,west=tall]",
            "minecraft:blackstone_wall[east=tall,north=low,south=tall,up=true,waterlogged=false,west=tall]",
            "minecraft:blackstone_wall[east=low,north=tall,south=tall,up=true,waterlogged=false,west=tall]",
            "minecraft:pale_oak_stairs[facing=north,half=bottom,shape=outer_left,waterlogged=false]",
            "minecraft:pale_oak_stairs[facing=east,half=bottom,shape=outer_left,waterlogged=false]",
            "minecraft:cherry_stairs[facing=west,half=bottom,shape=outer_right,waterlogged=false]",
            "minecraft:blackstone_wall[east=none,north=none,south=none,up=true,waterlogged=false,west=low]",
            "minecraft:nether_brick_wall[east=none,north=none,south=none,up=true,waterlogged=false,west=tall]",
            "minecraft:nether_brick_wall[east=tall,north=none,south=none,up=true,waterlogged=false,west=none]",
            "minecraft:nether_brick_wall[east=none,north=tall,south=none,up=true,waterlogged=false,west=none]",
            "minecraft:nether_brick_wall[east=none,north=none,south=tall,up=true,waterlogged=false,west=none]",
            "minecraft:blackstone_wall[east=low,north=none,south=low,up=true,waterlogged=false,west=tall]",
            "minecraft:blackstone_wall[east=none,north=tall,south=low,up=true,waterlogged=false,west=low]",
            "minecraft:blackstone_wall[east=none,north=low,south=none,up=true,waterlogged=false,west=none]",
            "minecraft:nether_brick_stairs[facing=west,half=top,shape=straight,waterlogged=false]",
            "minecraft:nether_brick_stairs[facing=east,half=top,shape=straight,waterlogged=false]",
            "minecraft:nether_brick_stairs[facing=north,half=top,shape=straight,waterlogged=false]",
            "minecraft:nether_brick_stairs[facing=south,half=top,shape=straight,waterlogged=false]",
            "minecraft:red_stained_glass_pane[east=false,north=true,south=false,waterlogged=false,west=false]",
            "minecraft:diorite_stairs[facing=north,half=top,shape=outer_left,waterlogged=false]",
            "minecraft:diorite_stairs[facing=east,half=top,shape=outer_left,waterlogged=false]",
            "minecraft:diorite_stairs[facing=west,half=top,shape=outer_left,waterlogged=false]",
            "minecraft:red_nether_brick_wall[east=none,north=tall,south=none,up=true,waterlogged=false,west=tall]",
            "minecraft:diorite_stairs[facing=south,half=top,shape=outer_left,waterlogged=false]",
            "minecraft:diorite_stairs[facing=north,half=top,shape=outer_right,waterlogged=false]",
            "minecraft:nether_brick_wall[east=low,north=low,south=low,up=true,waterlogged=false,west=none]",
            "minecraft:mangrove_fence[east=true,north=false,south=true,waterlogged=false,west=true]",
            "minecraft:black_stained_glass_pane[east=true,north=false,south=true,waterlogged=false,west=true]",
            "minecraft:nether_brick_wall[east=low,north=none,south=low,up=true,waterlogged=false,west=low]",
            "minecraft:diorite_stairs[facing=east,half=top,shape=outer_right,waterlogged=false]",
            "minecraft:diorite_stairs[facing=west,half=top,shape=outer_right,waterlogged=false]",
            "minecraft:nether_brick_wall[east=low,north=low,south=none,up=true,waterlogged=false,west=low]",
            "minecraft:red_stained_glass",
            "minecraft:nether_brick_wall[east=none,north=low,south=low,up=true,waterlogged=false,west=low]",
            "minecraft:diorite_stairs[facing=south,half=top,shape=outer_right,waterlogged=false]",
            "minecraft:diorite_slab[type=bottom,waterlogged=false]",
            "minecraft:diorite_wall[east=none,north=tall,south=tall,up=true,waterlogged=false,west=tall]",
            "minecraft:mangrove_fence_gate[facing=south,in_wall=false,open=true,powered=false]",
            "minecraft:mangrove_fence_gate[facing=east,in_wall=false,open=true,powered=false]",
            "minecraft:mangrove_fence_gate[facing=west,in_wall=false,open=true,powered=false]",
            "minecraft:mangrove_stairs[facing=west,half=top,shape=straight,waterlogged=false]",
            "minecraft:mangrove_stairs[facing=east,half=top,shape=straight,waterlogged=false]",
            "minecraft:mangrove_stairs[facing=north,half=top,shape=straight,waterlogged=false]",
            "minecraft:diorite_wall[east=tall,north=none,south=tall,up=true,waterlogged=false,west=tall]",
            "minecraft:diorite_wall[east=tall,north=tall,south=none,up=true,waterlogged=false,west=tall]",
            "minecraft:mangrove_stairs[facing=south,half=top,shape=straight,waterlogged=false]",
            "minecraft:mangrove_fence_gate[facing=north,in_wall=false,open=true,powered=false]",
            "minecraft:cherry_trapdoor[facing=south,half=top,open=false,powered=false,waterlogged=false]",
            "minecraft:diorite_wall[east=tall,north=tall,south=tall,up=true,waterlogged=false,west=none]",
            "minecraft:blackstone_slab[type=bottom,waterlogged=false]",
            "minecraft:mangrove_fence[east=true,north=true,south=false,waterlogged=false,west=true]",
            "minecraft:black_wool",
            "minecraft:mangrove_trapdoor[facing=north,half=top,open=false,powered=false,waterlogged=false]",
            "minecraft:mangrove_slab[type=top,waterlogged=false]",
            "minecraft:mangrove_trapdoor[facing=west,half=top,open=false,powered=false,waterlogged=false]",
            "minecraft:mangrove_trapdoor[facing=east,half=top,open=false,powered=false,waterlogged=false]",
            "minecraft:mangrove_trapdoor[facing=south,half=top,open=false,powered=false,waterlogged=false]",
            "minecraft:mangrove_slab[type=double,waterlogged=false]",
            "minecraft:mangrove_wall_sign[facing=north,waterlogged=false]",
            "minecraft:mangrove_wall_sign[facing=west,waterlogged=false]",
            "minecraft:mangrove_wall_sign[facing=east,waterlogged=false]",
            "minecraft:mangrove_wall_sign[facing=south,waterlogged=false]",
            "minecraft:mangrove_trapdoor[facing=north,half=bottom,open=false,powered=false,waterlogged=false]",
            "minecraft:skeleton_skull[powered=false,rotation=0]",
            "minecraft:mangrove_trapdoor[facing=east,half=bottom,open=false,powered=false,waterlogged=false]",
            "minecraft:mangrove_slab[type=bottom,waterlogged=false]",
            "minecraft:skeleton_skull[powered=false,rotation=13]",
            "minecraft:dried_ghast[facing=north,hydration=0,waterlogged=false]",
            "minecraft:skeleton_skull[powered=false,rotation=2]",
            "minecraft:skeleton_skull[powered=false,rotation=14]",
            "minecraft:skeleton_skull[powered=false,rotation=3]",
            "minecraft:stripped_mangrove_wood[axis=y]",
            "minecraft:mangrove_stairs[facing=west,half=bottom,shape=straight,waterlogged=false]",
            "minecraft:mangrove_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]",
            "minecraft:skeleton_skull[powered=false,rotation=15]",
            "minecraft:mangrove_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]",
            "minecraft:red_nether_brick_wall[east=none,north=tall,south=tall,up=true,waterlogged=false,west=tall]",
            "minecraft:mangrove_fence[east=false,north=false,south=false,waterlogged=false,west=false]",
            "minecraft:red_nether_brick_wall[east=none,north=none,south=low,up=true,waterlogged=false,west=none]",
            "minecraft:red_nether_brick_wall[east=tall,north=tall,south=tall,up=true,waterlogged=false,west=none]",
            "minecraft:skeleton_skull[powered=false,rotation=1]",
            "minecraft:skeleton_skull[powered=false,rotation=12]",
            "minecraft:dried_ghast[facing=west,hydration=0,waterlogged=false]",
            "minecraft:red_nether_brick_wall[east=tall,north=none,south=none,up=false,waterlogged=false,west=tall]",
            "minecraft:dried_ghast[facing=east,hydration=0,waterlogged=false]",
            "minecraft:skeleton_skull[powered=false,rotation=4]",
            "minecraft:skeleton_skull[powered=false,rotation=10]",
            "minecraft:mangrove_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]",
            "minecraft:skeleton_skull[powered=false,rotation=6]",
            "minecraft:red_nether_brick_wall[east=tall,north=tall,south=none,up=true,waterlogged=false,west=tall]",
            "minecraft:red_nether_brick_wall[east=none,north=tall,south=tall,up=false,waterlogged=false,west=none]",
            "minecraft:red_nether_brick_wall[east=low,north=none,south=none,up=true,waterlogged=false,west=none]",
            "minecraft:red_nether_brick_wall[east=none,north=none,south=none,up=true,waterlogged=false,west=low]",
            "minecraft:red_nether_brick_wall[east=tall,north=none,south=tall,up=true,waterlogged=false,west=tall]",
            "minecraft:skeleton_skull[powered=false,rotation=9]",
            "minecraft:red_nether_brick_wall[east=none,north=low,south=none,up=true,waterlogged=false,west=none]",
            "minecraft:skeleton_skull[powered=false,rotation=7]",
            "minecraft:skeleton_skull[powered=false,rotation=11]",
            "minecraft:dried_ghast[facing=south,hydration=0,waterlogged=false]",
            "minecraft:skeleton_skull[powered=false,rotation=5]",
            "minecraft:mangrove_trapdoor[facing=west,half=bottom,open=false,powered=false,waterlogged=false]",
            "minecraft:skeleton_skull[powered=false,rotation=8]",
            "minecraft:mangrove_trapdoor[facing=south,half=bottom,open=false,powered=false,waterlogged=false]",
            "minecraft:mangrove_stairs[facing=north,half=top,shape=inner_left,waterlogged=false]",
            "minecraft:mangrove_stairs[facing=east,half=top,shape=inner_left,waterlogged=false]",
            "minecraft:mangrove_stairs[facing=west,half=top,shape=inner_left,waterlogged=false]",
            "minecraft:mangrove_stairs[facing=south,half=top,shape=inner_left,waterlogged=false]",
            "minecraft:deepslate_tile_stairs[facing=south,half=top,shape=straight,waterlogged=false]",
            "minecraft:blackstone_slab[type=top,waterlogged=false]",
            "minecraft:crimson_hyphae[axis=z]",
            "minecraft:crimson_hyphae[axis=x]",
            "minecraft:deepslate_tile_stairs[facing=east,half=top,shape=straight,waterlogged=false]",
            "minecraft:deepslate_tile_stairs[facing=west,half=top,shape=straight,waterlogged=false]",
            "minecraft:deepslate_tile_stairs[facing=north,half=top,shape=straight,waterlogged=false]",
            "minecraft:deepslate_tile_slab[type=top,waterlogged=false]",
            "minecraft:deepslate_tile_slab[type=bottom,waterlogged=false]",
            "minecraft:deepslate_tiles",
            "minecraft:deepslate_tile_slab[type=double,waterlogged=false]",
            "minecraft:red_nether_bricks",
            "minecraft:crimson_hyphae[axis=y]",
            "minecraft:deepslate_tile_stairs[facing=west,half=bottom,shape=straight,waterlogged=false]",
            "minecraft:deepslate_tile_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]",
            "minecraft:deepslate_tile_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]",
            "minecraft:deepslate_tile_stairs[facing=west,half=bottom,shape=outer_right,waterlogged=false]",
            "minecraft:deepslate_tile_stairs[facing=east,half=bottom,shape=outer_left,waterlogged=false]",
            "minecraft:red_nether_brick_slab[type=bottom,waterlogged=false]",
            "minecraft:deepslate_tile_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]",
            "minecraft:deepslate_tile_stairs[facing=west,half=bottom,shape=outer_left,waterlogged=false]",
            "minecraft:deepslate_tile_stairs[facing=east,half=bottom,shape=outer_right,waterlogged=false]",
            "minecraft:red_nether_brick_stairs[facing=south,half=top,shape=outer_left,waterlogged=false]",
            "minecraft:red_nether_brick_stairs[facing=south,half=top,shape=outer_right,waterlogged=false]",
            "minecraft:red_nether_brick_stairs[facing=east,half=top,shape=straight,waterlogged=false]",
            "minecraft:red_nether_brick_stairs[facing=west,half=top,shape=straight,waterlogged=false]",
            "minecraft:chain[axis=y,waterlogged=false]",
            "minecraft:polished_diorite_stairs[facing=south,half=top,shape=outer_left,waterlogged=false]",
            "minecraft:polished_diorite_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]",
            "minecraft:polished_diorite_stairs[facing=south,half=top,shape=outer_right,waterlogged=false]",
            "minecraft:blackstone_stairs[facing=west,half=top,shape=straight,waterlogged=false]",
            "minecraft:polished_diorite_stairs[facing=east,half=top,shape=straight,waterlogged=false]",
            "minecraft:decorated_pot[cracked=false,facing=south,waterlogged=false]",
            "minecraft:polished_diorite_stairs[facing=west,half=top,shape=straight,waterlogged=false]",
            "minecraft:blackstone_stairs[facing=east,half=top,shape=straight,waterlogged=false]",
            "minecraft:red_nether_brick_wall[east=low,north=low,south=none,up=true,waterlogged=false,west=none]",
            "minecraft:red_nether_brick_wall[east=none,north=low,south=none,up=true,waterlogged=false,west=low]",
            "minecraft:red_nether_brick_wall[east=low,north=none,south=low,up=true,waterlogged=false,west=none]",
            "minecraft:red_nether_brick_wall[east=none,north=none,south=low,up=true,waterlogged=false,west=low]",
            "minecraft:polished_diorite_stairs[facing=north,half=top,shape=outer_right,waterlogged=false]",
            "minecraft:polished_diorite_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]",
            "minecraft:polished_diorite_stairs[facing=north,half=top,shape=outer_left,waterlogged=false]",
            "minecraft:red_nether_brick_stairs[facing=north,half=top,shape=outer_right,waterlogged=false]",
            "minecraft:red_nether_brick_stairs[facing=north,half=top,shape=outer_left,waterlogged=false]",
            "minecraft:deepslate_tile_wall[east=none,north=none,south=none,up=true,waterlogged=false,west=none]",
            "minecraft:polished_diorite_slab[type=top,waterlogged=false]",
            "minecraft:mangrove_fence[east=false,north=true,south=true,waterlogged=false,west=false]",
            "minecraft:flower_pot",
            "minecraft:red_nether_brick_wall[east=tall,north=none,south=tall,up=true,waterlogged=false,west=none]",
            "minecraft:red_nether_brick_wall[east=none,north=none,south=tall,up=true,waterlogged=false,west=tall]",
            "minecraft:black_candle[candles=1,lit=false,waterlogged=false]",
            "minecraft:polished_diorite_stairs[facing=east,half=bottom,shape=outer_right,waterlogged=false]",
            "minecraft:polished_diorite_stairs[facing=west,half=bottom,shape=outer_left,waterlogged=false]",
            "minecraft:red_nether_brick_wall[east=tall,north=tall,south=none,up=true,waterlogged=false,west=none]",
            "minecraft:polished_diorite_stairs[facing=east,half=bottom,shape=outer_left,waterlogged=false]",
            "minecraft:polished_diorite_stairs[facing=west,half=bottom,shape=outer_right,waterlogged=false]",
            "minecraft:red_nether_brick_wall[east=none,north=none,south=low,up=true,waterlogged=false,west=tall]",
            "minecraft:red_nether_brick_wall[east=tall,north=none,south=low,up=true,waterlogged=false,west=none]",
            "minecraft:red_nether_brick_wall[east=low,north=tall,south=none,up=true,waterlogged=false,west=none]",
            "minecraft:red_nether_brick_wall[east=none,north=tall,south=none,up=true,waterlogged=false,west=low]",
            "minecraft:red_nether_brick_wall[east=low,north=none,south=tall,up=true,waterlogged=false,west=none]",
            "minecraft:red_nether_brick_wall[east=none,north=none,south=tall,up=true,waterlogged=false,west=low]",
            "minecraft:red_nether_brick_wall[east=none,north=low,south=none,up=true,waterlogged=false,west=tall]",
            "minecraft:red_nether_brick_wall[east=tall,north=low,south=none,up=true,waterlogged=false,west=none]",
            "minecraft:red_stained_glass_pane[east=false,north=false,south=false,waterlogged=false,west=true]",
            "minecraft:red_stained_glass_pane[east=true,north=false,south=false,waterlogged=false,west=false]",
            "minecraft:mangrove_fence_gate[facing=south,in_wall=false,open=false,powered=false]",
            "minecraft:mangrove_fence_gate[facing=north,in_wall=false,open=false,powered=false]",
            "minecraft:red_stained_glass_pane[east=false,north=false,south=true,waterlogged=false,west=false]",
            "minecraft:pale_oak_slab[type=double,waterlogged=false]",
            "minecraft:red_nether_brick_wall[east=tall,north=none,south=none,up=true,waterlogged=false,west=none]",
            "minecraft:mangrove_trapdoor[facing=west,half=bottom,open=true,powered=false,waterlogged=false]",
            "minecraft:chiseled_polished_blackstone",
            "minecraft:mangrove_trapdoor[facing=east,half=bottom,open=true,powered=false,waterlogged=false]",
            "minecraft:mangrove_trapdoor[facing=south,half=bottom,open=true,powered=false,waterlogged=false]",
            "minecraft:blackstone_wall[east=low,north=low,south=low,up=false,waterlogged=false,west=low]",
            "minecraft:blackstone_wall[east=low,north=tall,south=low,up=true,waterlogged=false,west=tall]",
            "minecraft:blackstone_wall[east=low,north=low,south=tall,up=true,waterlogged=false,west=none]",
            "minecraft:blackstone_wall[east=none,north=low,south=tall,up=true,waterlogged=false,west=low]",
            "minecraft:blackstone_wall[east=low,north=tall,south=low,up=true,waterlogged=false,west=none]",
            "minecraft:blackstone_wall[east=none,north=none,south=low,up=true,waterlogged=false,west=none]",
            "minecraft:nether_brick_fence[east=false,north=false,south=false,waterlogged=false,west=false]",
            "minecraft:red_nether_brick_stairs[facing=west,half=bottom,shape=straight,waterlogged=false]",
            "minecraft:red_nether_brick_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]"
        };

        String ver = Bukkit.getBukkitVersion();
        boolean paleOakFallback = false;
        boolean driedGhastFallback = false;
        try {
            String[] parts = ver.split("\\.|-");
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
            boolean ge1214 = major > 1 || (major == 1 && (minor > 21 || (minor == 21 && patch >= 4)));
            boolean ge1216 = major > 1 || (major == 1 && (minor > 21 || (minor == 21 && patch >= 6)));
            paleOakFallback = !ge1214;
            driedGhastFallback = !ge1216;
        } catch (Exception e) {
            paleOakFallback = false;
            driedGhastFallback = false;
        }

        for (int i = 0; i < paletteStrs.length; i++) {
            String s = paletteStrs[i];
            if (paleOakFallback && s.startsWith("minecraft:pale_oak_")) {
                paletteStrs[i] = s.replace("pale_oak_", "crimson_");
            } else if (driedGhastFallback && s.startsWith("minecraft:dried_ghast")) {
                paletteStrs[i] = "minecraft:netherrack";
            }
        }

        for (int i = 0; i < paletteStrs.length; i++) {
            PALETTE[i] = Bukkit.createBlockData(paletteStrs[i]);
        }

        byte[] compressed = Base64.getDecoder().decode("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");
        try {
            Inflater inf = new Inflater();
            inf.setInput(compressed);
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream(109589);
            byte[] buf = new byte[4096];
            while (!inf.finished()) {
                int count = inf.inflate(buf);
                bos.write(buf, 0, count);
            }
            inf.end();
            DATA = bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to decompress schematic data", e);
        }
    }

    private static int decodeVarInt(byte[] data, int[] idxRef) {
        int value = 0;
        int shift = 0;
        int i = idxRef[0];
        while (true) {
            byte b = data[i++];
            value |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0) break;
            shift += 7;
        }
        idxRef[0] = i;
        return value;
    }

    public static Set<BlockPos> capture(World world, int ox, int oy, int oz) {
        Set<BlockPos> result = new HashSet<>();
        for (int y = 0; y < H; y++) {
            for (int z = 0; z < L; z++) {
                for (int x = 0; x < W; x++) {
                    int wx = ox + x;
                    int wy = oy + y;
                    int wz = oz + z;
                    result.add(new BlockPos(wx, wy, wz,
                            world.getBlockAt(wx, wy, wz).getBlockData().clone()));
                }
            }
        }
        return result;
    }

    public static void paste(World world, int ox, int oy, int oz) {
        int[] idxRef = new int[1];
        for (int y = 0; y < H; y++) {
            for (int z = 0; z < L; z++) {
                for (int x = 0; x < W; x++) {
                    int wx = ox + x;
                    int wy = oy + y;
                    int wz = oz + z;
                    int pal = decodeVarInt(DATA, idxRef);
                    if (pal != 0) {
                        world.getBlockAt(wx, wy, wz).setBlockData(PALETTE[pal], false);
                    }
                }
            }
        }
    }
}