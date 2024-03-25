package deercloud.loadanother;

import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class LoadAnother extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        globalScheduler = this.getServer().getGlobalRegionScheduler();
        m_cache = new Cache(this);
        config = new ConfigManager(this);
        m_work = new WorkManager(this);

        // 注册事件
        getServer().getPluginManager().registerEvents(new Events(), this);
        // 注册命令
        Objects.requireNonNull(getCommand("loadanother")).setExecutor(new Commands());

        m_work.reset();

        XLogger.info("LoadAnother 已加载");
        XLogger.info("版本: " + getPluginMeta().getVersion());
        // https://patorjk.com/software/taag/#p=display&f=Big&t=LoadAnother
        XLogger.info(" _                     _                      _   _");
        XLogger.info("| |                   | |   /\\               | | | |");
        XLogger.info("| |     ___   __ _  __| |  /  \\   _ __   ___ | |_| |__   ___ _ __");
        XLogger.info("| |    / _ \\ / _` |/ _` | / /\\ \\ | '_ \\ / _ \\| __| '_ \\ / _ \\ '__|");
        XLogger.info("| |___| (_) | (_| | (_| |/ ____ \\| | | | (_) | |_| | | |  __/ |");
        XLogger.info("|______\\___/ \\__,_|\\__,_/_/    \\_\\_| |_|\\___/ \\__|_| |_|\\___|_|");
        XLogger.info("");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public WorkManager getWorkManager() {
        return m_work;
    }

    public Cache getCache() {
        return m_cache;
    }

    public ConfigManager config;
    private WorkManager m_work = null;
    public static LoadAnother instance = null;

    private Cache m_cache = null;
    public static GlobalRegionScheduler globalScheduler;
}
