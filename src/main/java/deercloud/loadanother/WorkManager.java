package deercloud.loadanother;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class WorkManager {

    public WorkManager(LoadAnother plugin) {
        this.m_plugin = plugin;
        m_config = m_plugin.getConfigManager();
        m_logger = m_plugin.getMyLogger();
    }

    // 当玩家完成了世界跳转后检查跳转结果 再决定是否创建强加载
    public void playerChangeWorldController(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (!player_setting.get(player)) {
            return;
        }
        World.Environment from_world =  event.getFrom().getEnvironment();
        // 如果玩家跳转到了末地或者是从末地回来 则忽略
        if (player.getWorld().getEnvironment() == World.Environment.THE_END || from_world == World.Environment.THE_END) {
            teleport_cache.remove(player);
            return;
        }
        BindChunk bind_chunk = null;
        // 检查能否创建成功强加载区块
        for (Map.Entry<Player, BindChunk> entry : teleport_cache.entrySet()) {
            if (entry.getValue().setDestination(player, player.getLocation().getChunk())) {
                bind_chunk = entry.getValue();
                teleport_cache.remove(entry.getKey());
                break;
            }
        }
        // 如果不能成功创建 或者 玩家仍然在缓存中 则清空这条
        if (bind_chunk == null || teleport_cache.containsKey(player)) {
            teleport_cache.remove(player);
            return;
        }
        // 如果成功创建了强加载区块 则启动生命周期
        BindChunkLifeCycle life_cycle = new BindChunkLifeCycle(this, bind_chunk);
        life_cycle.runTaskTimer(m_plugin, 0L, 200L);
        if (m_config.getLoadTime() > 0) {
            loadTimeTask task = new loadTimeTask(bind_chunk);
            task.runTaskLater(m_plugin, m_config.getLoadTime() * 20L);
        }
        m_bind_chunks.put(bind_chunk.getHash(), bind_chunk);
        m_logger.info(player, "| =======强加载启动=======");
        m_logger.info(player, "| 编号 - " + bind_chunk.getHash());
        m_logger.info(player, "| | 主世界区块 " + bind_chunk.getWorldChunk().getX() + ", " + bind_chunk.getWorldChunk().getZ());
        m_logger.info(player, "| | 下届区块为 " + bind_chunk.getNetherChunk().getX() + ", " + bind_chunk.getNetherChunk().getZ());
    }

    public void playerPortalController(PlayerPortalEvent event) {
        Player player =  event.getPlayer();
        if (!player_setting.get(player)){
            return; // 如果玩家设置了不强加载则忽略
        }
        if (findBindChunkWithChunk(player.getLocation().getChunk()) != null) {
            return; // 如果玩家已经在强加载区块中则忽略
        }
        // 玩家如果进入了下届传送门则创建一个强加载区块 但是放在缓存中
        BindChunk bindChunk = new BindChunk(player, player.getLocation().getChunk());
        teleport_cache.put(player, bindChunk);
    }

    // 卸载强加载区块并且从列表中移除
    public void unloadBindChunk(BindChunk bindChunk) {
        bindChunk.unload();
        m_bind_chunks.remove(bindChunk.getHash());
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


    // 怪物进入强加载区块之前进行重命名
    public void monsterPortalController(EntityPortalEnterEvent event) {
        if (!(event.getEntity() instanceof Monster)) {
            return;
        }
        Monster monster = (Monster) event.getEntity();
        Chunk monster_chunk = monster.getLocation().getChunk();
        BindChunk bind_chunk = findBindChunkWithChunk(monster_chunk);
        if (bind_chunk == null) {
            return;
        }
        monster.setCustomName(bind_chunk.getHash());
        monster.setRemoveWhenFarAway(false);
    }

    // 检查区块是否在强加载列表中
    public BindChunk findBindChunkWithChunk(Chunk chunk) {
        for (Map.Entry<String , BindChunk> entry : m_bind_chunks.entrySet()) {
            if (entry.getValue().isContain(chunk)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public BindChunk findBindChunkWithPlayer(Player player) {
        for (Map.Entry<String , BindChunk> entry : m_bind_chunks.entrySet()) {
            if (entry.getValue().getCreator().equals(player)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void playerEnable(Player player) {
        player_setting.put(player, true);
    }

    public void playerDisable(Player player) {
        player_setting.put(player, false);
        if (findBindChunkWithPlayer(player) != null) {
            findBindChunkWithPlayer(player).setState(BindChunk.State.UNLOAD);
        }
    }

    // 延迟卸载处理器
    public static class delayTask extends BukkitRunnable {
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
            if (m_bind_chunk.isCreatorExist()) {
                m_bind_chunk.setState(BindChunk.State.FORCE);
                m_bind_chunk.getCreator().sendMessage(ChatColor.GREEN + "| 强加载区块已恢复");
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
            m_bind_chunk.setState(BindChunk.State.UNLOAD);
            this.cancel();
        }
        BindChunk m_bind_chunk;
    }

    // 强加载区块生命周期管理器
    private static class BindChunkLifeCycle extends BukkitRunnable {
        public BindChunkLifeCycle(WorkManager m_work_manger,  BindChunk chunk) {
            this.m_work_manger = m_work_manger;
            this.m_bind_chunk = chunk;
            m_config = m_work_manger.m_config;
        }
        @Override
        public void run() {
            if (!m_bind_chunk.getCreator().isOnline()) {
                m_work_manger.m_logger.info(ChatColor.YELLOW + "| 由于玩家退出，区块 " + m_bind_chunk.getHash() + " 强加载结束");
                m_work_manger.unloadBindChunk(m_bind_chunk);
                this.cancel();
                return;
            }
            if (m_bind_chunk.isCreatorExist()) {
                return;
            }
            if (m_bind_chunk.getState() == BindChunk.State.FORCE){
                if (m_work_manger.m_config.getDelay() > 0) {
                    m_work_manger.m_logger.warn(m_bind_chunk.getCreator(), "| 您已离开强加载区块，如果不在" + m_config.getDelay() + "秒内返回否则" + m_bind_chunk.getHash() + "将被卸载");
                }
                m_bind_chunk.setState(BindChunk.State.DELAY);
                WorkManager.delayTask task = new WorkManager.delayTask(m_bind_chunk);
                task.runTaskLater(m_work_manger.m_plugin, m_work_manger.m_config.getDelay() * 20L);
            } else if (m_bind_chunk.getState() == BindChunk.State.UNLOAD) {
                m_work_manger.m_logger.warn(m_bind_chunk.getCreator(), "| 强加载区块，" + m_bind_chunk.getHash() + "已经被卸载");
                m_work_manger.unloadBindChunk(m_bind_chunk);
                this.cancel();
            }
        }

        BindChunk m_bind_chunk;
        WorkManager m_work_manger;
        ConfigManager m_config;
    }

    public void printStatusPlayer(Player player) {
        m_logger.info(player, "| =====插件配置=====");
        m_logger.info(player, "| 强加载状态: " + (player_setting.get(player) ? "开启" : "关闭"));
        m_logger.info(player, "| 强加载持续时间: " + m_config.getLoadTime() + "秒（0 代表不限制）");
        m_logger.info(player, "| 强加载延迟卸载时间: " + m_config.getDelay() + "秒");
        m_logger.info(player, "| =====强加载内容=====");
        for (Map.Entry<String , BindChunk> entry : m_bind_chunks.entrySet()) {
            BindChunk bind_chunk = entry.getValue();
            if (bind_chunk.getCreator() == player) {
                m_logger.info(player, "| 强加载区块Hash " + bind_chunk.getHash());
                m_logger.info(player, "| 主世界区块坐标 " + bind_chunk.getWorldChunk().getX() + ", " + bind_chunk.getWorldChunk().getZ());
                m_logger.info(player, "| 地狱侧区块坐标 " + bind_chunk.getNetherChunk().getX() + ", " + bind_chunk.getNetherChunk().getZ());
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
            for (Map.Entry<String, BindChunk> entry : m_bind_chunks.entrySet()) {
                BindChunk bind_chunk = entry.getValue();
                m_logger.info(player, "| 强加载区块Hash: " + bind_chunk.getHash());
                m_logger.info(player, "|  | World : " + bind_chunk.getWorldChunk().getX() + ", " + bind_chunk.getWorldChunk().getZ());
                m_logger.info(player, "|  | Nether: " + bind_chunk.getNetherChunk().getX() + ", " + bind_chunk.getNetherChunk().getZ());
            }
        } else {
            m_logger.info("| =====插件配置=====");
            m_logger.info("| 默认强加载状态: " + (m_config.getEnable() ? "开启" : "关闭"));
            m_logger.info("| 强加载持续时间: " + m_config.getLoadTime() + "秒（0 代表不限制）");
            m_logger.info("| 强加载延迟卸载时间: " + m_config.getDelay() + "秒");
            m_logger.info("| =====强加载区块列表=====");
            for (Map.Entry<String, BindChunk> entry : m_bind_chunks.entrySet()) {
                BindChunk bind_chunk = entry.getValue();
                m_logger.info("| 强加载区块Hash: " + bind_chunk.getHash());
                m_logger.info("|  | World : " + bind_chunk.getWorldChunk().getX() + ", " + bind_chunk.getWorldChunk().getZ());
                m_logger.info("|  | Nether: " + bind_chunk.getNetherChunk().getX() + ", " + bind_chunk.getNetherChunk().getZ());
            }
        }
    }

    LoadAnother m_plugin;
    ConfigManager m_config;
    MyLogger m_logger;

    // 正在强加载的区块
    Map<String ,BindChunk> m_bind_chunks = new HashMap<>();

    Map<Player, BindChunk> teleport_cache = new HashMap<>();

    // 保存玩家设置
    Map<Player, Boolean> player_setting = new HashMap<>();

}
