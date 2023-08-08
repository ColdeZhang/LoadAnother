package deercloud.loadanother;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class LoadAnother extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        m_cache = new Cache(this);
        config = new ConfigManager(this);
        m_work = new WorkManager(this);

        // 注册事件
        getServer().getPluginManager().registerEvents(new Events(), this);
        // 注册命令
        Objects.requireNonNull(getCommand("loadanother")).setExecutor(new Commands());

        m_work.reset();

        XLogger.info("LoadAnother 启动完成");

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
}
