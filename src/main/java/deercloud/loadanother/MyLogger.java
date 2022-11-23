package deercloud.loadanother;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public class MyLogger {

    public MyLogger(LoadAnother plugin) {
        this.m_plugin = plugin;
        m_logger = m_plugin.getLogger();
    }

    public void info(Player player, String msg) {
        m_logger.info(ChatColor.GREEN + msg);
        player.sendMessage(ChatColor.GREEN + msg);
    }
    public void info(String msg) {
        m_logger.info(ChatColor.GREEN + msg);
    }

    public void warn(Player player, String msg) {
        m_logger.warning(ChatColor.YELLOW + msg);
        player.sendMessage(ChatColor.YELLOW + msg);
    }
    public void warn(String msg) {
        m_logger.warning(ChatColor.YELLOW + msg);
    }

    public void error(Player player, String msg) {
        m_logger.severe(ChatColor.RED + msg);
        player.sendMessage(ChatColor.RED + msg);
    }
    public void error(String msg) {
        m_logger.severe(ChatColor.RED + msg);
    }

    private LoadAnother m_plugin = null;
    private Logger m_logger = null;

}
