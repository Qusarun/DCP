package ru.qusarun.dcp;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class DeathEvent {
    @Getter private final Player player;
    @Getter @Setter private boolean cancelled, dropInventory, clearInventory, clearEffects, resetSaturation, resetHunger;
    @Getter @Setter private Location respawnLocation;

    public DeathEvent(final Player player, final Location respawnLocation, final boolean dropInventory, final boolean clearInventory, final boolean clearEffects, final boolean resetSaturation, final boolean resetHunger) {
        this.player = player;
        this.respawnLocation = respawnLocation;
        this.dropInventory = dropInventory;
        this.clearInventory = clearInventory;
        this.clearEffects = clearEffects;
        this.resetSaturation = resetSaturation;
        this.resetHunger = resetHunger;
    }
}
