package com.adambrodin.osrsflipper.gui;

import com.adambrodin.osrsflipper.misc.BotConfig;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

public class IngameGUI {
    public static boolean hasLoggedIn = false;
    public static long loggedInMillis;
    public static String currentAction = "Idling...";
    public static int sessionProfit = 0;

    public static void Draw(Graphics2D g2d) {
        if (hasLoggedIn) {
            WidgetChild chatboxWidget = Widgets.getWidgetChild(161, 32);
            Dimension overlaySize = new Dimension(chatboxWidget.getWidth(), chatboxWidget.getHeight());
            int x = chatboxWidget.getX(), y = chatboxWidget.getY() - BotConfig.OVERLAY_TEXT_Y_OFFSET;

            g2d.setColor(Color.magenta);
            g2d.fillRect(x, y, overlaySize.width, overlaySize.height);
            g2d.setFont(BotConfig.OVERLAY_FONT);
            g2d.setColor(Color.WHITE);

            g2d.drawString("CURRENT ACTION: " + currentAction, x + BotConfig.OVERLAY_TEXT_X_OFFSET, y + BotConfig.OVERLAY_TEXT_Y_OFFSET);
            g2d.drawString("UPTIME: " + GetFormattedUptime(), x + BotConfig.OVERLAY_TEXT_X_OFFSET, y + BotConfig.OVERLAY_TEXT_Y_OFFSET * 2);
            g2d.drawString("SESSION PROFIT: " + GetFormattedSessionProfit(), x + BotConfig.OVERLAY_TEXT_X_OFFSET, y + BotConfig.OVERLAY_TEXT_Y_OFFSET * 3);
        }
    }

    private static String GetFormattedUptime() {
        int uptimeSeconds = (int) (System.currentTimeMillis() - loggedInMillis) / 1000;
        int seconds = uptimeSeconds % 60;
        int minutes = (uptimeSeconds % 3600) / 60;
        int hours = uptimeSeconds / 3600;

        return hours + " hours, " + minutes + " minutes, " + seconds + " seconds";
    }

    private static String GetFormattedSessionProfit() {
        return NumberFormat.getInstance(Locale.US).format(sessionProfit) + " gp";
    }
}
