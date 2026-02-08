package com.radiushelper.selection;

import com.radiushelper.config.RHConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public final class SelectionState {
    private static final SelectionState INSTANCE = new SelectionState();

    private BlockPos point1;
    private BlockPos point2;
    private boolean dirty = true;
    private long cacheKey = Long.MIN_VALUE;
    private List<BlockPos> cached = Collections.emptyList();

    private SelectionState() {
    }

    public static SelectionState get() {
        return INSTANCE;
    }

    public void setPoint1(BlockPos pos) {
        point1 = pos;
        dirty = true;
    }

    public void setPoint2(BlockPos pos) {
        point2 = pos;
        dirty = true;
    }

    public void clearPoint2() {
        point2 = null;
        dirty = true;
    }

    public void markDirty() {
        dirty = true;
    }

    public boolean hasPoint1() {
        return point1 != null;
    }

    public boolean hasPoint2() {
        return point2 != null;
    }

    public BlockPos getPoint1() {
        return point1;
    }

    public BlockPos getPoint2() {
        return point2;
    }

    public boolean hasSelection() {
        return point1 != null && point2 != null;
    }

    public Box getSelectionBox() {
        if (!hasSelection()) {
            return null;
        }
        BlockPos min = getMin();
        BlockPos max = getMax();
        return new Box(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1);
    }

    public BlockPos getMin() {
        return new BlockPos(
            Math.min(point1.getX(), point2.getX()),
            Math.min(point1.getY(), point2.getY()),
            Math.min(point1.getZ(), point2.getZ())
        );
    }

    public BlockPos getMax() {
        return new BlockPos(
            Math.max(point1.getX(), point2.getX()),
            Math.max(point1.getY(), point2.getY()),
            Math.max(point1.getZ(), point2.getZ())
        );
    }

    public List<BlockPos> getPlacements(RHConfig config) {
        if (!hasSelection()) {
            return Collections.emptyList();
        }
        long key = computeCacheKey(config);
        if (dirty || key != cacheKey) {
            BlockPos min = getMin();
            BlockPos max = getMax();
            int size = config.mode == RHConfig.Mode.PRIVATE ? config.privateSize : (config.tntRadius * 2 + 1);
            cached = RegionMath.computeCenters(min, max, size);
            cacheKey = key;
            dirty = false;
        }
        return cached;
    }

    private long computeCacheKey(RHConfig config) {
        long key = 17;
        key = key * 31 + config.mode.ordinal();
        key = key * 31 + config.privateSize;
        key = key * 31 + config.tntRadius;
        key = key * 31 + point1.asLong();
        key = key * 31 + point2.asLong();
        return key;
    }
}
