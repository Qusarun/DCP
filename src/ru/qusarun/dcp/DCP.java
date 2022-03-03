package ru.qusarun.dcp;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class DCP extends JavaPlugin implements Listener {
    @Getter private static DCP instance;
    private final List<DeathListener> listeners = new ArrayList<>();
    private boolean enableDeathMessage, dropInventory, clearInventory, clearEffects, resetSaturation, resetHunger;
    private String defaultDeathMessage;
    private final Map<EntityDamageEvent.DamageCause, String> deathMessages = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        enableDeathMessage = this.getConfig().getBoolean("enable-death-message");
        defaultDeathMessage = this.getConfig().getString("default");
        dropInventory = this.getConfig().getBoolean("drop-inventory");
        clearInventory = this.getConfig().getBoolean("clear-inventory");
        clearEffects = this.getConfig().getBoolean("clear-effects");
        resetSaturation = this.getConfig().getBoolean("reset-saturation");
        resetHunger = this.getConfig().getBoolean("reset-hunger");
        this.getConfig().getKeys(true).stream().filter(s -> !s.contains("reset") && !s.contains("clear") && !s.contains("drop") && !s.equals("enable-death-message") && !s.equals("default")).forEach(s -> deathMessages.put(EntityDamageEvent.DamageCause.valueOf(s.toUpperCase().replace("-", "_")), this.getConfig().getString(s)));
    }

    @Override
    public void onDisable() {

    }

    @EventHandler
    @SuppressWarnings("all")
    public void onDeath(final EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        final Player player = (Player) e.getEntity();
        if (player.getHealth() - e.getFinalDamage() > 0) return;
        e.setCancelled(true);
        final DeathEvent event = new DeathEvent(player, player.getBedSpawnLocation() == null? Bukkit.getWorlds().get(0).getSpawnLocation() : player.getBedSpawnLocation(), dropInventory, clearInventory, clearEffects, resetSaturation, resetHunger);
        listeners.forEach(listener -> listener.onDeath(event));
        if (event.isCancelled())
            return;
        if (event.isClearEffects())
            player.getActivePotionEffects().forEach(eff -> player.removePotionEffect(eff.getType()));
        if (event.isDropInventory())
            Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).forEach(i -> player.getWorld().dropItemNaturally(player.getLocation(), i));
        if (event.isClearInventory())
            player.getInventory().clear();
        if (event.isResetHunger())
            player.setFoodLevel(20);
        if (event.isResetSaturation())
            player.setSaturation(20);
        player.teleport(event.getRespawnLocation());
        player.setHealth(player.getMaxHealth());
        if (!enableDeathMessage)
            return;
        final Entity attacker = e instanceof EntityDamageByEntityEvent? ((EntityDamageByEntityEvent) e).getDamager() : null;
        broadcast(deathMessages.getOrDefault(e.getCause(), defaultDeathMessage), player, attacker);
    }

    @SuppressWarnings("unused")
    public void addListener(final DeathListener listener) {
        listeners.add(listener);
    }

    @SuppressWarnings("unused")
    public void removeListener(final DeathListener listener) {
        listeners.remove(listener);
    }

    private void broadcast(final String s, final Player player, final Entity attacker) {
        final String message = ChatColor.translateAlternateColorCodes('&', s.replace("%p", player.getName()).replace("%a", attacker == null? "null" : attacker.getName()));
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(message));
        Bukkit.getConsoleSender().sendMessage(message);
    }
}
