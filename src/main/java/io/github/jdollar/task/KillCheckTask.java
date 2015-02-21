package io.github.jdollar.task;

import io.github.jdollar.FreezeKill;
import io.github.jdollar.Timer;
import io.github.jdollar.listener.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.*;

public class KillCheckTask extends BukkitRunnable {

    private final JavaPlugin freezeKill;
    private final PlayerListener playerListener;
    private final ScoreboardManager scoreboardManager;

    public KillCheckTask(FreezeKill freezeKill, PlayerListener playerListener, ScoreboardManager scoreboardManager) {
        this.freezeKill = freezeKill;
        this.playerListener = playerListener;
        this.scoreboardManager = scoreboardManager;
    }

    @Override
    public void run() {
        if (playerListener != null) {
            List<UUID> unluckyPlayers = new ArrayList<>();
            Map<UUID, Timer> currentFrozenPlayers = playerListener.getFrozenPlayers();

            for (Map.Entry<UUID, Timer> frozenPlayer : currentFrozenPlayers.entrySet()) {
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
                    if (freezeKill.getServer().getOnlinePlayers().contains(killVictim)) {
                        killVictim.setHealth(0);
                    }
                    playerListener.removeFrozenPlayer(playerUuid);
                }
            }

            if (playerListener.getFrozenPlayers().isEmpty()) {
                removeScoreboards();
                this.cancel();
            } else {
                updateScoreboards();
            }
        } else {
            //if the player listener isn't set then just cancel the task
            this.cancel();
        }
    }

    public void addFrozenPlayer(UUID playerUuid, int timeLimit) {
        playerListener.addFrozenPlayer(playerUuid, timeLimit);

        Player newFrozenPlayer = Bukkit.getPlayer(playerUuid);
        if (newFrozenPlayer != null && freezeKill.getServer().getOnlinePlayers().contains(newFrozenPlayer)) {
            Scoreboard newScoreBoard = scoreboardManager.getNewScoreboard();

            if (newFrozenPlayer.getScoreboard() != null && !newFrozenPlayer.getScoreboard().equals(newScoreBoard)) {
                newScoreBoard = newFrozenPlayer.getScoreboard();
            }

            Objective timeLimitBoard = newScoreBoard.registerNewObjective("timeLimit", "dummy");
            timeLimitBoard.setDisplaySlot(DisplaySlot.SIDEBAR);
            timeLimitBoard.setDisplayName("Time Limit");
            newFrozenPlayer.setScoreboard(newScoreBoard);
        }
    }

    public void removeFrozenPlayer(UUID playerUuid) {
        playerListener.removeFrozenPlayer(playerUuid);
    }

    private void updateScoreboards() {
        Score score;
        Scoreboard playerScoreboard;
        Player currentFrozenPlayer;
        Objective objective;
        for (Map.Entry<UUID, Timer> frozenPlayer : playerListener.getFrozenPlayers().entrySet()) {
            currentFrozenPlayer = Bukkit.getPlayer(frozenPlayer.getKey());
            playerScoreboard = currentFrozenPlayer.getScoreboard();
            objective = playerScoreboard.getObjective("timeLimit");
            score = objective.getScore(currentFrozenPlayer.getName());
            score.setScore(frozenPlayer.getValue().getSeconds());
        }
    }

    private void removeScoreboards() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }
}
