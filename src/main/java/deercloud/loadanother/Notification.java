package deercloud.loadanother;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

public class Notification {
    private static final Style i_style = Style.style(TextColor.color(139, 255, 123));
    private static final Style w_style = Style.style(TextColor.color(255, 185, 69));
    private static final Style e_style = Style.style(TextColor.color(255, 96, 72));

    public static void info(Player player, String msg) {
        player.sendMessage(Component.text("[LA] " + msg, i_style));
    }

    public static void warn(Player player, String msg) {
        player.sendMessage(Component.text("[LA] " + msg, w_style));
    }

    public static void error(Player player, String msg) {
        player.sendMessage(Component.text("[LA] " + msg, e_style));
    }
}
