package dev.nguyendevs.malevolentshrine.domain;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;

import java.util.*;

public class ShrineSession {
    private final UUID playerId;
    private final Location center;
    private final double radius;
    private final int durationTicks;
    private int elapsedTicks;
    private boolean active;
    private int taskId;
    private final Set<BlockPos> originalSurfaceBlocks;
    private final Set<BlockPos> originalDismantleBlocks;
    private final Set<BlockPos> schematicOriginalBlocks;
    private final Set<Entity> affectedEntities;
    private int cleaveTickCounter;
    private int cleaveNextInterval;
    private final List<Integer> dismantleTaskIds;
    private int schematicPasteTaskId;
    private int schemMinX, schemMinY, schemMinZ;
    private int schemMaxX, schemMaxY, schemMaxZ;
    private boolean hasSchematicBounds;
    private final BossBar bossBar;
    private final Set<UUID> bossBarViewers;

    public ShrineSession(UUID playerId, Location center, double radius, int durationTicks) {
        this.playerId = playerId;
        this.center = center.clone();
        this.radius = radius;
        this.durationTicks = durationTicks;
        this.elapsedTicks = 0;
        this.active = true;
        this.taskId = -1;
        this.originalSurfaceBlocks = new HashSet<>();
        this.originalDismantleBlocks = new HashSet<>();
        this.schematicOriginalBlocks = new HashSet<>();
        this.affectedEntities = new HashSet<>();
        this.dismantleTaskIds = new ArrayList<>();
        this.schematicPasteTaskId = -1;
        this.hasSchematicBounds = false;
        this.bossBar = Bukkit.createBossBar(
                ChatColor.DARK_RED + "Malevolent Shrine",
                BarColor.RED,
                BarStyle.SEGMENTED_20
        );
        this.bossBarViewers = new HashSet<>();
    }

    public UUID getPlayerId() { return playerId; }
    public Location getCenter() { return center.clone(); }
    public double getRadius() { return radius; }
    public int getDurationTicks() { return durationTicks; }
    public int getElapsedTicks() { return elapsedTicks; }
    public void setElapsedTicks(int elapsedTicks) { this.elapsedTicks = elapsedTicks; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }
    public Set<BlockPos> getOriginalSurfaceBlocks() { return originalSurfaceBlocks; }
    public Set<BlockPos> getOriginalDismantleBlocks() { return originalDismantleBlocks; }
    public Set<BlockPos> getSchematicOriginalBlocks() { return schematicOriginalBlocks; }
    public Set<Entity> getAffectedEntities() { return affectedEntities; }
    public int getCleaveTickCounter() { return cleaveTickCounter; }
    public void setCleaveTickCounter(int cleaveTickCounter) { this.cleaveTickCounter = cleaveTickCounter; }
    public int getCleaveNextInterval() { return cleaveNextInterval; }
    public void setCleaveNextInterval(int cleaveNextInterval) { this.cleaveNextInterval = cleaveNextInterval; }

    public List<Integer> getDismantleTaskIds() { return dismantleTaskIds; }
    public void addDismantleTaskId(int taskId) { dismantleTaskIds.add(taskId); }

    public int getSchematicPasteTaskId() { return schematicPasteTaskId; }
    public void setSchematicPasteTaskId(int schematicPasteTaskId) { this.schematicPasteTaskId = schematicPasteTaskId; }

    public BossBar getBossBar() { return bossBar; }
    public Set<UUID> getBossBarViewers() { return bossBarViewers; }

    public void setSchematicBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.schemMinX = minX; this.schemMinY = minY; this.schemMinZ = minZ;
        this.schemMaxX = maxX; this.schemMaxY = maxY; this.schemMaxZ = maxZ;
        this.hasSchematicBounds = true;
    }

    public boolean isInsideSchematic(int x, int y, int z) {
        return hasSchematicBounds
                && x >= schemMinX && x <= schemMaxX
                && y >= schemMinY && y <= schemMaxY
                && z >= schemMinZ && z <= schemMaxZ;
    }
}
