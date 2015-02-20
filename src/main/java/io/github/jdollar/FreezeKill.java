package io.github.jdollar;

import io.github.jdollar.listener.PlayerListener;
import io.github.jdollar.task.KillCheckTask;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.UUID;

public final class FreezeKill extends JavaPlugin {

    private static final String FREEZE_KILL_COMMAND = "freezekill";
    private static final String OBJECTIVE_TIME_LIMIT = "timeLimit";

    private PlayerListener playerListener;
    private ScoreboardManager scoreboardManager;
    private Scoreboard scoreBoard;

    private BukkitTask runningKillCheck;
    private KillCheckTask killCheckTask;

    @Override
    public void onEnable() {
        playerListener = new PlayerListener(this);
        scoreboardManager = Bukkit.getScoreboardManager();
        scoreBoard = scoreboardManager.getNewScoreboard();
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase(FREEZE_KILL_COMMAND) && args.length == 2) {
            if (args[1].matches("[0-9]+")) {
                //parse the user input from the command
                //TODO: Make it safe for getting the player
                UUID playerUuid = Bukkit.getServer().getPlayer(args[0]).getUniqueId();
                int timeLimit = Integer.parseInt(args[1]);

                if (timeLimit == 0) {
                    //They don't have any time to kill anything so just kill the player
                    Bukkit.getPlayer(playerUuid).setHealth(0);
                } else {
                    //Add player to the scoreboard
                    setupPlayerScoreboard(playerUuid, timeLimit);

                    //startup a task for every second or add player to currently running task
                    if (runningKillCheck == null || !Bukkit.getScheduler().isCurrentlyRunning(runningKillCheck.getTaskId())) {
                        killCheckTask = new KillCheckTask(this, new PlayerListener(this), scoreBoard);
                        killCheckTask.addFrozenPlayer(playerUuid, timeLimit);
                        killCheckTask.setScoreBoard(scoreBoard);
                        runningKillCheck= killCheckTask.runTaskTimer(this, 0, 20);
                    } else {
                        killCheckTask.addFrozenPlayer(playerUuid, timeLimit);
                        killCheckTask.setScoreBoard(scoreBoard);
                    }

                    LivingEntity playerEntity = (LivingEntity) Bukkit.getPlayer(playerUuid);
                    for (Entity potentialMobs : playerEntity.getNearbyEntities(50, 50, 50)) {
                        if (potentialMobs instanceof Monster) {
                            ((Monster) potentialMobs).setTarget(playerEntity);
                        }
                    }
                }

                return true;
            }
        }
        return false;
    }

    private void setupPlayerScoreboard(UUID unluckyVictimUuid, int timeLimit) {
        Objective playerTimeBoard = scoreBoard.getObjective(OBJECTIVE_TIME_LIMIT);

        //Add player to scoreboard with their time
        if (playerTimeBoard == null) {
            playerTimeBoard = scoreBoard.registerNewObjective("timeLimit", "dummy");
            playerTimeBoard.setDisplaySlot(DisplaySlot.SIDEBAR);
            playerTimeBoard.setDisplayName("Time Limit");
        } else {
            Score currentPlayerTime = playerTimeBoard.getScore(Bukkit.getPlayer(unluckyVictimUuid));
            currentPlayerTime.setScore(timeLimit);
        }
    }
}
