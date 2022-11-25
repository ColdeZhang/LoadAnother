package deercloud.loadanother;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityPortalExitEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;

public class Events implements Listener {

    @EventHandler
    public void onPlayerPortalEvent(PlayerPortalEvent event) {
        m_work.playerPortalController(event);
    }

    @EventHandler
    public void onMonsterPortalEvent(EntityPortalEnterEvent event) {
        m_work.monsterPortalController(event);
    }

    @EventHandler
    public void onPlayerChangeWorldEvent(PlayerChangedWorldEvent event) {
        m_work.playerChangeWorldController(event);
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        m_work.playerJoinController(event.getPlayer());
    }

    @EventHandler
    public void onWorldLoaded(WorldLoadEvent event) {
        m_work.resetPending();
    }

    WorkManager m_work = LoadAnother.getInstance().getWorkManager();

}
