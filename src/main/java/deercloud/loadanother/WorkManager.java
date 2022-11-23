package deercloud.loadanother;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class WorkManager {

    public WorkManager(LoadAnother plugin) {
        this.m_plugin = plugin;
        m_config = m_plugin.getConfigManager();
        m_logger = m_plugin.getMyLogger();
    }

    public void chunkUnloadController(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        BindChunk bind_chunk = null;
        for (Map.Entry<Integer, BindChunk> entry : m_bind_chunks.entrySet()) {
            BindChunk bind_chunk_t = entry.getValue();
            if (bind_chunk_t.isContain(chunk)) {
                bind_chunk = bind_chunk_t;
                break;
            }
        }
        // 如果被卸载的区块不在强加载列表里则忽略
        if (bind_chunk == null) {
            return;
        }
        switch (bind_chunk.getState()) {
            case FORCE:
                event.setCancelled(true);
                if (m_config.getDelay() > 0) {
                    m_logger.warn(bind_chunk.getLastPlayer(), "您已离开强加载区块，如果不在" + m_config.getDelay() + "秒内返回则区块" + bind_chunk.getHashCode() + "将被卸载");
                }
                bind_chunk.setState(BindChunk.State.DELAY);
                delayTask task = new delayTask(bind_chunk);
                task.runTaskLater(m_plugin, m_config.getDelay() * 20L);
                break;
            case DELAY:
                event.setCancelled(true);
                break;
            case UNLOAD:
                m_bind_chunks.remove(bind_chunk.getHashCode());
                break;
        }
    }

    public void playerPortalController(PlayerPortalEvent event) {
        Player player =  event.getPlayer();
        if (!player_setting.get(player)){
            return; // 如果玩家设置了不强加载则忽略
        }
        Chunk from = event.getFrom().getChunk();
        Chunk to = event.getTo().getChunk();
        BindChunk bindChunk = new BindChunk(player, from, to);
        m_bind_chunks.put(bindChunk.getHashCode(), bindChunk);
        if (m_config.getLoadTime() > 0) {
            loadTimeTask task = new loadTimeTask(bindChunk);
            task.runTaskLater(m_plugin, m_config.getLoadTime() * 20L);
        }
        m_logger.info(player, ChatColor.GREEN + "| 强加载开启 - " + bindChunk.getHashCode());
        m_logger.info(player, ChatColor.GREEN + "| 当前所在世界区块 " + to.getX() + ", " + to.getZ());
        m_logger.info(player, ChatColor.GREEN + "| 对应另一维度区块 " + from.getX() + ", " + from.getZ());
    }

    public void playerJoinController(Player player) {
        if (m_config.getEnable()) {
            playerEnable(player);
            m_logger.info(player, ChatColor.GREEN + "| 强加载已开启 使用 /loadanother help 查看帮助");
        } else {
            playerDisable(player);
            m_logger.info(player, ChatColor.RED + "| 强加载已关闭 使用 /loadanother help 查看帮助");
        }
    }

    public void playerQuitController(Player player) {
        BindChunk bind_chunk = null;
        for (Map.Entry<Integer, BindChunk> entry : m_bind_chunks.entrySet()) {
            BindChunk bind_chunk_t = entry.getValue();
            if (bind_chunk_t.getLastPlayer() == player) {
                bind_chunk = bind_chunk_t;
                break;
            }
        }
        // 如果被卸载的区块不在强加载列表里则忽略
        if (bind_chunk== null) {
            return;
        }
        m_logger.info(ChatColor.YELLOW + "| 由于玩家退出，区块 " + bind_chunk.getHashCode() + " 强加载结束");
        m_bind_chunks.remove(bind_chunk.getHashCode());
        player_setting.remove(player);
    }

    public void playerEnable(Player player) {
        player_setting.put(player, true);
    }

    public void playerDisable(Player player) {
        player_setting.put(player, false);
    }

    // 延迟卸载处理器
    private static class delayTask extends BukkitRunnable {
        public delayTask(BindChunk chunk) { this.m_bind_chunk = chunk; }
        @Override
        public void run() {
            BindChunk.State state = m_bind_chunk.getState();
            // 如果不是DELAY状态则忽略
            if (state != BindChunk.State.DELAY) {
                this.cancel();
                return;
            }
            // 如果区块内存在玩家则把状态改回FORCE 否则改为UNLOAD
            Player player = m_bind_chunk.getContainingPlayer();
            if (player != null) {
                m_bind_chunk.updateLastPlayer(player);
                m_bind_chunk.setState(BindChunk.State.FORCE);
            } else {
                m_bind_chunk.setState(BindChunk.State.UNLOAD);
            }
            this.cancel();
        }
        BindChunk m_bind_chunk;
    }

    // 强加载持续时间处理器
    private static class loadTimeTask extends BukkitRunnable {
        public loadTimeTask(BindChunk chunk) { this.m_bind_chunk = chunk; }
        @Override
        public void run() {
            BindChunk.State state = m_bind_chunk.getState();
            // 如果不是FORCE状态则忽略
            if (state == BindChunk.State.UNLOAD) {
                this.cancel();
                return;
            }
            m_bind_chunk.setState(BindChunk.State.UNLOAD);
            this.cancel();
        }
        BindChunk m_bind_chunk;
    }

    public void printStatusPlayer(Player player) {
        m_logger.info(player, "| =====插件配置=====");
        m_logger.info(player, "| 强加载状态: " + (player_setting.get(player) ? "开启" : "关闭"));
        m_logger.info(player, "| 强加载持续时间: " + m_config.getLoadTime() + "秒（0 代表不限制）");
        m_logger.info(player, "| 强加载延迟卸载时间: " + m_config.getDelay() + "秒");
        m_logger.info(player, "| =====强加载内容=====");
        for (Map.Entry<Integer, BindChunk> entry : m_bind_chunks.entrySet()) {
            BindChunk bind_chunk = entry.getValue();
            if (bind_chunk.getLastPlayer() == player) {
                m_logger.info(player, "| 强加载区块Hash " + bind_chunk.getHashCode());
                m_logger.info(player, "| 当前所在世界区块 " + bind_chunk.getWorldChunk().getX() + ", " + bind_chunk.getWorldChunk().getZ());
                m_logger.info(player, "| 对应另一维度区块 " + bind_chunk.getNetherChunk().getX() + ", " + bind_chunk.getNetherChunk().getZ());
                return;
            }
        }
        m_logger.info(player, "| 无，使用地狱门跳跃后可查看");
    }

    public void printStatusOP(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            m_logger.info(player, "| =====插件配置=====");
            m_logger.info(player, "| 默认强加载状态: " + (m_config.getEnable() ? "开启" : "关闭"));
            m_logger.info(player, "| 强加载持续时间: " + m_config.getLoadTime() + "秒（0 代表不限制）");
            m_logger.info(player, "| 强加载延迟卸载时间: " + m_config.getDelay() + "秒");
            m_logger.info(player, "| =====强加载区块列表=====");
            for (Map.Entry<Integer, BindChunk> entry : m_bind_chunks.entrySet()) {
                BindChunk bind_chunk = entry.getValue();
                m_logger.info(player, "| 强加载区块Hash: " + bind_chunk.getHashCode());
                m_logger.info(player, "|  | World : " + bind_chunk.getWorldChunk().getX() + ", " + bind_chunk.getWorldChunk().getZ());
                m_logger.info(player, "|  | Nether: " + bind_chunk.getNetherChunk().getX() + ", " + bind_chunk.getNetherChunk().getZ());
            }
        } else {
            m_logger.info("| =====插件配置=====");
            m_logger.info("| 默认强加载状态: " + (m_config.getEnable() ? "开启" : "关闭"));
            m_logger.info("| 强加载持续时间: " + m_config.getLoadTime() + "秒（0 代表不限制）");
            m_logger.info("| 强加载延迟卸载时间: " + m_config.getDelay() + "秒");
            m_logger.info("| =====强加载区块列表=====");
            for (Map.Entry<Integer, BindChunk> entry : m_bind_chunks.entrySet()) {
                BindChunk bind_chunk = entry.getValue();
                m_logger.info("| 强加载区块Hash: " + bind_chunk.getHashCode());
                m_logger.info("|  | World : " + bind_chunk.getWorldChunk().getX() + ", " + bind_chunk.getWorldChunk().getZ());
                m_logger.info("|  | Nether: " + bind_chunk.getNetherChunk().getX() + ", " + bind_chunk.getNetherChunk().getZ());
            }
        }
    }

    LoadAnother m_plugin = null;
    ConfigManager m_config = null;
    MyLogger m_logger = null;

    // 正在强加载的区块
    Map<Integer ,BindChunk> m_bind_chunks = new HashMap<>();

    // 保存玩家设置
    Map<Player, Boolean> player_setting = new HashMap<>();

}
