package deercloud.loadanother;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class LoadAnother extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        m_logger = new MyLogger(this);
        m_config = new ConfigManager(this);
        m_work = new WorkManager(this);

        // 注册事件
        getServer().getPluginManager().registerEvents(new Events(), this);
        // 注册命令
        Objects.requireNonNull(getCommand("loadanother")).setExecutor(new Commands());

        m_logger.info("LoadAnother 启动完成");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public ConfigManager getConfigManager() {
        return m_config;
    }

    public MyLogger getMyLogger() {
        return m_logger;
    }

    public WorkManager getWorkManager() {
        return m_work;
    }

    public static LoadAnother getInstance() {
        return instance;
    }

    private ConfigManager m_config = null;
    private MyLogger m_logger = null;
    private WorkManager m_work = null;
    private static LoadAnother instance = null;
}
