package io.github.jdollar.Listener;

import io.github.jdollar.FreezeKill;
import io.github.jdollar.Timer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.*;

public final class PlayerListener implements Listener {

    private Map<UUID, Timer> frozenPlayers = new HashMap<UUID, Timer>();

    public PlayerListener(FreezeKill freezeKill) {
        freezeKill.getServer().getPluginManager().registerEvents(this, freezeKill);
    }

    @EventHandler
    public void freeze(PlayerMoveEvent playerMoveEvent) {
        if (frozenPlayers.containsKey(playerMoveEvent.getPlayer().getUniqueId())) {
            playerMoveEvent.setCancelled(true);
        }
    }

    @EventHandler
    public void killMob(EntityDeathEvent entityDeathEvent) {
        if (entityDeathEvent.getEntity() instanceof Monster) {
            UUID monsterSlayer = entityDeathEvent.getEntity().getKiller().getUniqueId();
            if (frozenPlayers.containsKey(monsterSlayer)) {
                //Player lives!
                removeFrozenPlayer(monsterSlayer);

                //TODO: remove player from scoreboard and keep from getting killed
            }
        }
    }

    public void addFrozenPlayer(UUID playerUuid, int timeLimit) {
        if (!frozenPlayers.containsKey(playerUuid)) {
            frozenPlayers.put(playerUuid, new Timer(timeLimit));
        }
    }

    public void removeFrozenPlayer(UUID playerUuid) {
        if (frozenPlayers != null) {
            frozenPlayers.remove(playerUuid);
        }
    }
}
