package io.github.jdollar;

import io.github.jdollar.listener.PlayerListener;
import io.github.jdollar.task.KillCheckTask;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.*;

public final class FreezeKill extends JavaPlugin {

    private static final String FREEZE_KILL_COMMAND = "freezekill";
    private static final String UNFREEZE_COMMAND = "unfreeze";

    private ScoreboardManager scoreboardManager;

    private BukkitTask runningKillCheck;
    private KillCheckTask killCheckTask;

    @Override
    public void onEnable() {
        scoreboardManager = Bukkit.getScoreboardManager();
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
                Player argumentPlayer = Bukkit.getPlayer(args[0]);
                int timeLimit = Integer.parseInt(args[1]);
                if (this.getServer().getOnlinePlayers().contains(argumentPlayer)) {
                    UUID playerUuid = argumentPlayer.getUniqueId();

                    if (timeLimit == 0) {
                        //They don't have any time to kill anything so just kill the player
                        this.getServer().getPlayer(playerUuid).setHealth(0);
                    } else {
                        //startup a task for every second or add player to currently running task
                        if (runningKillCheck == null || !Bukkit.getScheduler().isCurrentlyRunning(runningKillCheck.getTaskId())) {
                            killCheckTask = new KillCheckTask(this, new PlayerListener(this), scoreboardManager);
                            killCheckTask.addFrozenPlayer(playerUuid, timeLimit);
                            runningKillCheck= killCheckTask.runTaskTimer(this, 0, 20);
                        } else {
                            killCheckTask.addFrozenPlayer(playerUuid, timeLimit);
                        }

                        LivingEntity playerEntity = Bukkit.getPlayer(playerUuid);
                        for (Entity potentialMobs : playerEntity.getNearbyEntities(50, 50, 50)) {
                            if (potentialMobs instanceof Monster) {
                                ((Monster) potentialMobs).setTarget(playerEntity);
                            }
                        }
                    }
                } else {
                    sender.sendMessage("The player name is spelled incorrectly or the player is not online.");
                }

                return true;
            }
        }

        if (cmd.getName().equalsIgnoreCase(UNFREEZE_COMMAND) && args.length == 1) {
            Player playerToSave = Bukkit.getPlayer(args[0]);
            if (Bukkit.getOnlinePlayers().contains(playerToSave)
                    && runningKillCheck != null
                    && Bukkit.getScheduler().isCurrentlyRunning(runningKillCheck.getTaskId())) {

                killCheckTask.removeFrozenPlayer(playerToSave.getUniqueId());

            } else {
                sender.sendMessage("The player is not online or the player name was entered incorrectly. Please try again.");
            }

            return true;
        }
        return false;
    }
}
