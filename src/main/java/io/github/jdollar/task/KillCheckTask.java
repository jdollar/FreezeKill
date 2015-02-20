package io.github.jdollar.task;

import io.github.jdollar.FreezeKill;
import io.github.jdollar.Timer;
import io.github.jdollar.listener.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.*;

public class KillCheckTask extends BukkitRunnable {

    private final JavaPlugin freezeKill;
    private final PlayerListener playerListener;
    private Scoreboard scoreBoard;
    private Map<UUID, Timer> frozenPlayers = new HashMap<UUID, Timer>();

    public KillCheckTask(FreezeKill freezeKill, PlayerListener playerListener, Scoreboard scoreBoard) {
        this.freezeKill = freezeKill;
        this.playerListener = playerListener;
        this.scoreBoard = scoreBoard;
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
            removeScoreboards();
            this.cancel();
        } else {
            updateScoreboard();
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.setScoreboard(scoreBoard);
            }
        }
    }

    public void addFrozenPlayer(UUID playerUuid, int timeLimit) {
        if (!frozenPlayers.containsKey(playerUuid)) {
            frozenPlayers.put(playerUuid, new Timer(timeLimit));
        }

        playerListener.setFrozenPlayers(frozenPlayers);

        for (Player onlinePlayers : Bukkit.getServer().getOnlinePlayers()) {
            Bukkit.getServer().broadcastMessage(onlinePlayers.getName());
            onlinePlayers.setScoreboard(scoreBoard);
        }
    }

    public void removeFrozenPlayer(UUID playerUuid) {
        if (frozenPlayers != null) {
            frozenPlayers.remove(playerUuid);
        }

        playerListener.setFrozenPlayers(frozenPlayers);
    }

    private void updateScoreboard() {
        Score score;
        Objective objective = scoreBoard.getObjective("timeLimit");
        for (Map.Entry<UUID, Timer> frozenPlayer : frozenPlayers.entrySet()) {
            score = objective.getScore(Bukkit.getPlayer(frozenPlayer.getKey()));
            score.setScore(frozenPlayer.getValue().getSeconds());
        }
    }

    private void removeScoreboards() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }

    public void setScoreBoard(Scoreboard scoreBoard) {
        this.scoreBoard = scoreBoard;
    }
}
