package io.github.xdotdash.trackerjacker.listener;

import io.github.xdotdash.trackerjacker.TrackerJacker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class TrackerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (event.getPlayer().hasPermission("tracker.track")) {
            TrackerJacker.ONLINE_UUIDS.add(event.getPlayer().getUniqueId().toString());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (event.getPlayer().hasPermission("tracker.track")) {
            TrackerJacker.ONLINE_UUIDS.remove(event.getPlayer().getUniqueId().toString());
        }
    }
}