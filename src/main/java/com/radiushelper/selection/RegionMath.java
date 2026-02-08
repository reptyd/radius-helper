package com.radiushelper.selection;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.BlockPos;

public final class RegionMath {
    private RegionMath() {
    }

    public static List<BlockPos> computeCenters(BlockPos min, BlockPos max, int size) {
        if (size < 1) {
            return List.of();
        }
        List<Integer> xs = computeAxisCenters(min.getX(), max.getX(), size);
        List<Integer> ys = computeAxisCenters(min.getY(), max.getY(), size);
        List<Integer> zs = computeAxisCenters(min.getZ(), max.getZ(), size);
        List<BlockPos> result = new ArrayList<>(xs.size() * ys.size() * zs.size());
        for (int x : xs) {
            for (int y : ys) {
                for (int z : zs) {
                    result.add(new BlockPos(x, y, z));
                }
            }
        }
        return result;
    }

    private static List<Integer> computeAxisCenters(int min, int max, int size) {
        int halfLow = size / 2;
        int start = min + halfLow;
        if (start > max) {
            start = max;
        }
        List<Integer> centers = new ArrayList<>();
        for (int c = start; c <= max; c += size) {
            centers.add(c);
        }
        return centers;
    }
}
