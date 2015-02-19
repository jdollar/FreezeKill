package io.github.jdollar;

import io.github.jdollar.Listener.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public final class FreezeKill extends JavaPlugin {

    private static final String FREEZE_KILL_COMMAND = "freezekill";

    private PlayerListener playerListener;
    private ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
    private Scoreboard scoreBoard;

    @Override
    public void onEnable() {
        playerListener = new PlayerListener(this);
        scoreBoard = scoreboardManager.getNewScoreboard();
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase(FREEZE_KILL_COMMAND) && args.length == 2) {
            if (args[0].contains("[0-9]+")) {
                //parse the user input from the command
                //TODO: Make it safe for getting the player
                Player unluckyVictim = Bukkit.getServer().getPlayer(args[0]);
                int timeLimit = Integer.parseInt(args[1]);

                if (timeLimit == 0) {
                    //TODO: Kill that fool
                } else {
                    //Add player to the listener and setup their scoreboard
                    playerListener.addFrozenPlayer(unluckyVictim, timeLimit);
                    setupPlayerScoreboard(unluckyVictim, timeLimit);
                }
            }
        }
        return false;
    }

    private void setupPlayerScoreboard(Player unluckyVictim, int timeLimit) {
        //Add player to scoreboard with their time
        Objective playerTimeRow = scoreBoard.registerNewObjective("timeLimit", "dummy");
        Score currentPlayerTime = playerTimeRow.getScore(unluckyVictim);
        currentPlayerTime.setScore(timeLimit);
    }
}
