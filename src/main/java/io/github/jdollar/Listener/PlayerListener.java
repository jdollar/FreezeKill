package io.github.jdollar.Listener;

import io.github.jdollar.FreezeKill;
import io.github.jdollar.Timer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;

public final class PlayerListener implements Listener {

    private Map<Player, Timer> frozenPlayers = new HashMap<Player, Timer>();

    public PlayerListener(FreezeKill freezeKill) {
        freezeKill.getServer().getPluginManager().registerEvents(this, freezeKill);
    }

    @EventHandler
    public void freeze(PlayerMoveEvent playerMoveEvent) {
        if (frozenPlayers.containsKey(playerMoveEvent.getPlayer())) {
            playerMoveEvent.setCancelled(true);
        }
    }

    public void addFrozenPlayer(Player player, int timeLimit) {
        if (!frozenPlayers.containsKey(player)) {
            frozenPlayers.put(player, new Timer(timeLimit));
        }
    }

    public void removeFrozenPlayer(Player player) {
        if (frozenPlayers != null) {
            frozenPlayers.remove(player);
        }
    }
}
