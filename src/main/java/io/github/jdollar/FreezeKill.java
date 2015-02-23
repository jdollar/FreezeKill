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

import java.util.*;

public final class FreezeKill extends JavaPlugin {

    private static final String FREEZE_KILL_COMMAND = "freezekill";
    private static final String UNFREEZE_COMMAND = "unfreeze";

    private static final String FREEZE_KILL_PERMISSION = "FreezeKill.freezekill";
    private static final String UNFREEZE_PERMISSION = "FreezeKill.unfreeze";

    private int runningKillCheck = -1;
    private KillCheckTask killCheckTask;
    private PlayerListener playerListener;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        removeOldScoreboardArtifacts();
        playerListener = new PlayerListener(this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        removeOldScoreboardArtifacts();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase(FREEZE_KILL_COMMAND) && args.length == 2) {
            if (isNumberValue(args[1])) {
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
                        if (killCheckTask == null || killCheckTask.isCancelled()) {
                            killCheckTask = new KillCheckTask(this, playerListener, Bukkit.getScoreboardManager());
                            killCheckTask.addFrozenPlayer(playerUuid, timeLimit);
                            runningKillCheck = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, killCheckTask, 0, 20);
                        } else if (killCheckTask.getFrozenPlayers() != null &&
                                        !killCheckTask.getFrozenPlayers().containsKey(playerUuid)){
                            killCheckTask.addFrozenPlayer(playerUuid, timeLimit);
                        } else {
                            sender.sendMessage("This player is already frozen.");
                        }

                        if ("true".equalsIgnoreCase(FreezeKill.this.getConfig().getString("hostileMobSettings.doMobAttackWhenFrozen"))) {
                            String xPos = FreezeKill.this.getConfig().getString("hostileMobSettings.areaAroundPlayer.x");
                            String yPos = FreezeKill.this.getConfig().getString("hostileMobSettings.areaAroundPlayer.y");
                            String zPos = FreezeKill.this.getConfig().getString("hostileMobSettings.areaAroundPlayer.z");
                            if (isNumberValue(xPos) && isNumberValue(yPos) && isNumberValue(zPos)) {
                                LivingEntity playerEntity = Bukkit.getPlayer(playerUuid);
                                for (Entity potentialMobs : playerEntity.getNearbyEntities(50, 50, 50)) {
                                    if (potentialMobs instanceof Monster) {
                                        ((Monster) potentialMobs).setTarget(playerEntity);
                                    }
                                }
                            } else {
                                sender.sendMessage("The area around the player in the config.yml for FreezeKill has invalid values to turn mobs hostile. Ensure the x, y and z values are whole numbers");
                            }
                        }
                    }
                } else {
                    sender.sendMessage("The player name is spelled incorrectly or the player is not online.");
                }
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase(UNFREEZE_COMMAND) && args.length == 1) {
            Player playerToSave = Bukkit.getPlayer(args[0]);
            if (playerToSave != null && Bukkit.getOnlinePlayers().contains(playerToSave)) {
                if (killCheckTask != null && !killCheckTask.isCancelled() && killCheckTask.getFrozenPlayers().containsKey(playerToSave.getUniqueId())) {
                    killCheckTask.removeFrozenPlayer(playerToSave.getUniqueId());
                } else {
                    sender.sendMessage("That player is not frozen.");
                }
            } else {
                sender.sendMessage("The player is not online or the player name was entered incorrectly. Please try again.");
            }
            return true;
        }

        return false;
    }

    private boolean isNumberValue(String numToCheck) {
        return numToCheck != null && numToCheck.matches("[0-9]+");
    }

    private boolean isPlayerWithPermissionOrConsole(CommandSender sender, String permission) {
        return !(sender instanceof Player) || sender.isOp() || sender.hasPermission(permission);
    }

    private void removeOldScoreboardArtifacts() {
        for (Player onlinePlayer : this.getServer().getOnlinePlayers()) {
            onlinePlayer.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }
}
