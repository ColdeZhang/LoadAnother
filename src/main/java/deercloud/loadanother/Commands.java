package deercloud.loadanother;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Commands implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.isOp()) {
                    printHelpInfoOP(sender);
                } else {
                    printHelpInfoPlayer(player);
                }
            } else {
                printHelpInfoOP(sender);
            }
            return true;
        }
        switch (args[0]) {
            case "help":
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (player.isOp()) {
                        printHelpInfoOP(sender);
                    } else {
                        printHelpInfoPlayer(player);
                    }
                } else {
                    printHelpInfoOP(sender);
                }
                break;
            case "enable":
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    m_work_manager.playerEnable(player);
                    Notification.info(player, "已启用 LoadAnother 区块强加载");
                } else {
                    XLogger.error("此命令只能由玩家执行");
                }
                break;
            case "disable":
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    m_work_manager.playerDisable(player);
                    Notification.warn(player, "已禁用 LoadAnother 区块强加载");
                } else {
                    XLogger.error("此命令只能由玩家执行");
                }
                break;
            case "setDefault":
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (player.isOp()) {
                        if (args.length == 2) {
                            try {
                                m_config_manager.setDefault(Boolean.parseBoolean(args[1]));
                                Notification.info(player, "已设置 LoadAnother 默认启用状态为 " + args[1]);
                            } catch (NumberFormatException e) {
                                Notification.error(player, "参数错误");
                            }
                        } else {
                            Notification.error(player, "参数错误");
                        }
                    } else {
                        Notification.error(player, "你没有权限执行此命令");
                    }
                } else {
                    try {
                        m_config_manager.setDefault(Boolean.parseBoolean(args[1]));
                        XLogger.info("已设置 LoadAnother 默认启用状态为 " + args[1]);
                    } catch (NumberFormatException e) {
                        XLogger.error("参数错误");
                    }
                }
                break;
            case "setLoadTime":
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (player.isOp()) {
                        if (args.length == 2) {
                            try {
                                int time = Integer.parseInt(args[1]);
                                m_config_manager.setLoadTime(time);
                                Notification.info(player, "已设置 LoadAnother 区块强加载时间为 " + time + " 秒");
                            } catch (NumberFormatException e) {
                                Notification.error(player, "参数错误");
                            }
                        } else {
                            Notification.error(player, "参数错误");
                        }
                    } else {
                        Notification.error(player, "你没有权限执行此命令");
                    }
                } else {
                    try {
                        int time = Integer.parseInt(args[1]);
                        m_config_manager.setLoadTime(time);
                        XLogger.info("已设置 LoadAnother 区块强加载时间为 " + time + " 秒");
                    } catch (NumberFormatException e) {
                        XLogger.error("参数错误");
                    }
                }
                break;
            case "setDelay":
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (player.isOp()) {
                        setDelay(args);
                    } else {
                        Notification.error(player, "你没有权限执行此命令");
                    }
                } else {
                    setDelay(args);
                }
                break;
            case "reload":
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (player.isOp()) {
                        Notification.info(player, "正在重载 LoadAnother 配置文件");
                        m_config_manager.reload();
                        m_work_manager.reset();
                        Notification.info(player, "已重新加载 LoadAnother 配置文件");
                    } else {
                        Notification.error(player, "你没有权限执行此命令");
                    }
                } else {
                    XLogger.info("正在重载 LoadAnother 配置文件");
                    m_config_manager.reload();
                    m_work_manager.reset();
                    XLogger.info("已重新加载 LoadAnother 配置文件");
                }
                break;
            case "status":
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (player.isOp()) {
                        m_work_manager.printStatusOP(sender);
                    } else {
                        m_work_manager.printStatusPlayer(player);
                    }
                } else {
                    m_work_manager.printStatusOP(sender);
                }
        }
        return true;
    }

    private void setDelay(String[] args) {
        if (args.length == 2) {
            try {
                int time = Integer.parseInt(args[1]);
                m_config_manager.setDelay(time);
                XLogger.info("已设置 LoadAnother 区块强加载延迟为 " + time + " 秒");
            } catch (NumberFormatException e) {
                XLogger.error("参数错误");
            }
        } else {
            XLogger.error("参数错误");
        }
    }

    private void printHelpInfoOP(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Notification.info(player, "| ===== LoadAnother 命令帮助 =====");
            Notification.info(player, "| /loadanother help - 显示帮助");
            Notification.info(player, "| /loadanother reload - 重载配置文件");
            Notification.info(player, "| /loadanother setDefault - 玩家默认是否启用插件");
            Notification.info(player, "| /loadanother enable - 启用插件");
            Notification.info(player, "| /loadanother disable - 禁用插件");
            Notification.info(player, "| /loadanother status - 查看状态");
            Notification.info(player, "| /loadanother setLoadTime <整数> - 设置强加载持续时间（0 代表不限制）");
            Notification.info(player, "| /loadanother setDelay <整数>- 设置玩家离开后卸载延迟时间");
            Notification.info(player, "| /loadanother setRadius <整数> - 设置加载区块半径");
            Notification.info(player, "| ===============================");
        } else {
            XLogger.info("| ===== LoadAnother 命令帮助 =====");
            XLogger.info("| /loadanother help - 显示帮助");
            XLogger.info("| /loadanother reload - 重载配置文件");
            XLogger.info("| /loadanother setDefault - 玩家默认是否启用插件");
            XLogger.info("| /loadanother enable - 启用插件");
            XLogger.info("| /loadanother disable - 禁用插件");
            XLogger.info("| /loadanother status - 查看状态");
            XLogger.info("| /loadanother setLoadTime <整数> - 设置强加载持续时间（0 代表不限制）");
            XLogger.info("| /loadanother setDelay <整数>- 设置玩家离开后卸载延迟时间");
            XLogger.info("| /loadanother setRadius <整数> - 设置加载区块半径");
            XLogger.info("| ===============================");
        }

    }

    private void printHelpInfoPlayer(Player player) {
        Notification.info(player, "| ===== LoadAnother 命令帮助 =====");
        Notification.info(player, "| /loadanother help - 显示帮助");
        Notification.info(player, "| /loadanother enable - 启用强加载");
        Notification.info(player, "| /loadanother disable - 禁用强加载");
        Notification.info(player, "| /loadanother status - 查看状态");
        Notification.info(player, "| ===============================");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("help", "reload", "enable", "disable", "setLoadTime", "setDelay", "setRadius", "status");
        } else if (args.length == 2) {
            if (args[0].equals("setLoadTime")) {
                return Collections.singletonList("请输入整数 （单位：秒） 设置强加载持续时间（0为不限制）");
            }
            if (args[0].equals("setDelay")) {
                return Collections.singletonList("请输入整数 （单位：秒） 设置玩家离开后延迟卸载时间（0为立即卸载）");
            }
            if (args[0].equals("setRadius")) {
                return Collections.singletonList("请输入整数 （单位：区块） 设置加载区块半径");
            }
        }
        return null;
    }

    private final WorkManager m_work_manager = LoadAnother.instance.getWorkManager();
    private final ConfigManager m_config_manager = LoadAnother.instance.config;
}
