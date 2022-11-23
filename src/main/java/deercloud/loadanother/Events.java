package deercloud.loadanother;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class Events implements Listener {

    @EventHandler
    public void onPlayerPortalEvent(PlayerPortalEvent event) {
        m_work.playerPortalController(event);
    }

    @EventHandler
    public void onChunkUnloadEvent(ChunkUnloadEvent event) {
        m_work.chunkUnloadController(event);
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        m_work.playerJoinController(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        m_work.playerQuitController(event.getPlayer());
    }
    WorkManager m_work = LoadAnother.getInstance().getWorkManager();

}
