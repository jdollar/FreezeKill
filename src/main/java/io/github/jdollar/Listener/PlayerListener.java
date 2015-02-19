package io.github.jdollar.Listener;

import io.github.jdollar.FreezeKill;
import org.bukkit.event.Listener;

public final class PlayerListener implements Listener {
    public PlayerListener(FreezeKill freezeKill) {
        freezeKill.getServer().getPluginManager().registerEvents(this, freezeKill);
    }
}
