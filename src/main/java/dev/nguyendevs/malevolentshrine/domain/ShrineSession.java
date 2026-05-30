package dev.nguyendevs.malevolentshrine.domain;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
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
    private final Map<Long, BlockData> originalSurfaceBlocks;
    private final Map<Long, BlockData> originalDismantleBlocks;
    private final Set<Entity> affectedEntities;
    private int cleaveTickCounter;
    private int cleaveNextInterval;
    private final List<Integer> dismantleTaskIds;

    public ShrineSession(UUID playerId, Location center, double radius, int durationTicks) {
        this.playerId = playerId;
        this.center = center.clone();
        this.radius = radius;
        this.durationTicks = durationTicks;
        this.elapsedTicks = 0;
        this.active = true;
        this.taskId = -1;
        this.originalSurfaceBlocks = new HashMap<>();
        this.originalDismantleBlocks = new HashMap<>();
        this.affectedEntities = new HashSet<>();
        this.dismantleTaskIds = new ArrayList<>();
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
    public Map<Long, BlockData> getOriginalSurfaceBlocks() { return originalSurfaceBlocks; }
    public Map<Long, BlockData> getOriginalDismantleBlocks() { return originalDismantleBlocks; }
    public Set<Entity> getAffectedEntities() { return affectedEntities; }
    public int getCleaveTickCounter() { return cleaveTickCounter; }
    public void setCleaveTickCounter(int cleaveTickCounter) { this.cleaveTickCounter = cleaveTickCounter; }
    public int getCleaveNextInterval() { return cleaveNextInterval; }
    public void setCleaveNextInterval(int cleaveNextInterval) { this.cleaveNextInterval = cleaveNextInterval; }

    public List<Integer> getDismantleTaskIds() { return dismantleTaskIds; }
    public void addDismantleTaskId(int taskId) { dismantleTaskIds.add(taskId); }
}
