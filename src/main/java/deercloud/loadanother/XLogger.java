package deercloud.loadanother;

public class XLogger {
    static void info(String msg) {
        msg = "§aINFO : " + msg;
        LoadAnother.instance.getLogger().info(msg);
    }

    public static void warn(String msg) {
        msg = "§eWARN : " + msg;
        LoadAnother.instance.getLogger().warning(msg);
    }

    public static void error(String msg) {
        msg = "§cERROR: " + msg;
        LoadAnother.instance.getLogger().severe(msg);
    }

    public static void debug(String msg) {
        if (!LoadAnother.instance.config.getDebug()) {
            return;
        }
        msg = "§bDEBUG: " + msg;
        LoadAnother.instance.getLogger().info(msg);
    }


}
