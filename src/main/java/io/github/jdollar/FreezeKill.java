package io.github.jdollar;

import io.github.jdollar.Listener.PlayerListener;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class FreezeKill extends JavaPlugin {

    PlayerListener playerListener;

    @Override
    public void onEnable() {
        playerListener = new PlayerListener(this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }
}
