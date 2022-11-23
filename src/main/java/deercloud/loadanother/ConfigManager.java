package deercloud.loadanother;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    public ConfigManager(LoadAnother plugin) {
        this.m_plugin = plugin;
        m_logger = m_plugin.getMyLogger();
        m_plugin.saveDefaultConfig();
        reload();
    }

    public void reload() {
        m_plugin.reloadConfig();
        m_config_file = m_plugin.getConfig();
        m_enable = m_config_file.getBoolean("enable", true);
        m_load_time = m_config_file.getInt("loadTime", 0);
        m_delay = m_config_file.getInt("delay", 0);
        m_logger.info("加载配置文件完成");
        m_logger.info("   -  是否启用功能        ：" + m_enable);
        m_logger.info("   -  加载持续时间        ：" + m_load_time + "秒（0 代表不限制）");
        m_logger.info("   -  延迟卸载时间        ：" + m_delay + "秒");
    }

    public boolean getEnable() {
        return m_enable;
    }

    public int getLoadTime() {
        return m_load_time;
    }
    public void setLoadTime(int load_time) {
        m_load_time = load_time;
        m_config_file.set("loadTime", load_time);
        m_plugin.saveConfig();
    }
    public int getDelay() {
        return m_delay;
    }
    public void setDelay(int delay) {
        m_delay = delay;
        m_config_file.set("delay", delay);
        m_plugin.saveConfig();
    }

    private LoadAnother m_plugin = null;
    private MyLogger m_logger = null;
    private FileConfiguration m_config_file;

    private boolean m_enable = false;
    private int m_load_time = 0;
    private int m_delay = 0;

}
