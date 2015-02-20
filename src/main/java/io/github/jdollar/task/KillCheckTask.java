package io.github.jdollar.task;

import io.github.jdollar.FreezeKill;
import io.github.jdollar.Timer;
import io.github.jdollar.listener.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class KillCheckTask extends BukkitRunnable {

    private final JavaPlugin freezeKill;
    private final PlayerListener playerListener;
    private final Scoreboard scoreBoard;
    private Map<UUID, Timer> frozenPlayers = new HashMap<UUID, Timer>();

    public KillCheckTask(FreezeKill freezeKill, PlayerListener playerListener, UUID playerUuid, int timeLimit, Scoreboard scoreboard) {
        this.freezeKill = freezeKill;
        this.playerListener = playerListener;
        this.scoreBoard = scoreboard;

        for (Player onlinePlayers : Bukkit.getServer().getOnlinePlayers()) {
            onlinePlayers.setScoreboard(scoreboard);
        }

        this.frozenPlayers.put(playerUuid, new Timer(timeLimit));
        this.playerListener.setFrozenPlayers(this.frozenPlayers);
    }

    @Override
    public void run() {
        List<UUID> unluckyPlayers = new ArrayList<UUID>();
        for(Map.Entry<UUID, Timer> frozenPlayer : frozenPlayers.entrySet()) {
            if (frozenPlayer.getValue().getSeconds() == 0) {
                unluckyPlayers.add(frozenPlayer.getKey());
            } else {
                frozenPlayer.getValue().setSeconds(frozenPlayer.getValue().getSeconds() - 1);
            }
        }

        if (!unluckyPlayers.isEmpty()) {
            Player killVictim;
            for (UUID playerUuid : unluckyPlayers) {
                killVictim = Bukkit.getPlayer(playerUuid);
                if (Bukkit.getOnlinePlayers().contains(killVictim)) {
                    killVictim.setHealth(0);
                }
                frozenPlayers.remove(playerUuid);
            }
        }

        if (frozenPlayers.isEmpty()) {
            this.cancel();
        }
    }

    public void addFrozenPlayer(UUID playerUuid, int timeLimit) {
        if (!frozenPlayers.containsKey(playerUuid)) {
            frozenPlayers.put(playerUuid, new Timer(timeLimit));
        }

        playerListener.setFrozenPlayers(frozenPlayers);
    }

    public void removeFrozenPlayer(UUID playerUuid) {
        if (frozenPlayers != null) {
            frozenPlayers.remove(playerUuid);
        }

        playerListener.setFrozenPlayers(frozenPlayers);
    }
}
