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
        if (m_load_time < 0) {
            m_load_time = 0;
            setLoadTime(m_load_time);
            m_logger.warn("loadTime 不能小于0，已经自动调整为0。");
        }
        m_delay = m_config_file.getInt("delay", 0);
        if (m_delay < 0) {
            m_delay = 0;
            setDelay(m_delay);
            m_logger.warn("延迟时间不能小于0，已经自动调整为0。");
        }
        m_radius = m_config_file.getInt("radius", 1);
        if (m_radius < 1) {
            m_radius = 1;
            setRadius(m_radius);
            m_logger.warn("radius 不能小于1，已重置为默认值 1");
        }
        m_logger.info("加载配置文件完成");
        m_logger.info("   -  是否启用功能        ：" + m_enable);
        m_logger.info("   -  加载持续时间        ：" + m_load_time + "秒（0 代表不限制）");
        m_logger.info("   -  延迟卸载时间        ：" + m_delay + "秒");
        m_logger.info("   -  强加载半径为        ：" + m_radius + "区块");
    }

    public boolean getEnable() {
        return m_enable;
    }
    public void setDefault(boolean enable) {
        m_config_file.set("enable", enable);
        m_plugin.saveConfig();
        m_enable = enable;
    }

    public int getLoadTime() {
        return m_load_time;
    }
    public void setLoadTime(int load_time) {
        if (load_time < 0) {
            load_time = 0;
        }
        m_load_time = load_time;
        m_config_file.set("loadTime", load_time);
        m_plugin.saveConfig();
    }
    public int getDelay() {
        return m_delay;
    }
    public void setDelay(int delay) {
        if (delay < 0) {
            delay = 0;
        }
        m_delay = delay;
        m_config_file.set("delay", delay);
        m_plugin.saveConfig();
    }
    public int getRadius() {
        return m_radius;
    }
    public void setRadius(int radius) {
        if (radius < 1) {
            radius = 1;
        }
        m_radius = radius;
        m_config_file.set("radius", radius);
        m_plugin.saveConfig();
    }

    private final LoadAnother m_plugin;
    private final MyLogger m_logger;
    private FileConfiguration m_config_file;

    private boolean m_enable = false;
    private int m_load_time = 0;
    private int m_delay = 0;
    private int m_radius = 1;

}
