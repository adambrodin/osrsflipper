package com.adambrodin.osrsflipper.gui;

import com.adambrodin.osrsflipper.core.Flipper;
import com.adambrodin.osrsflipper.core.GEController;
import com.adambrodin.osrsflipper.core.Main;
import com.adambrodin.osrsflipper.misc.BotConfig;
import com.adambrodin.osrsflipper.models.ActiveFlip;
import org.dreambot.api.Client;
import org.dreambot.api.data.GameState;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import java.awt.*;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class IngameGUI {
    private static final List<int[]> slotWidgets = Arrays.asList(
            new int[]{465, 7}, // Slot one
            new int[]{465, 8},
            new int[]{465, 9},
            new int[]{465, 10},
            new int[]{465, 11},
            new int[]{465, 12},
            new int[]{465, 13},
            new int[]{465, 14}); // Slot eight

    public static void Draw(Graphics2D g2d) {
        if (Main.hasLoggedIn) {
            WidgetChild chatboxWidget = Widgets.getWidgetChild(161, 32);
            Dimension overlaySize = new Dimension(chatboxWidget.getWidth(), chatboxWidget.getHeight());
            int x = chatboxWidget.getX(), y = chatboxWidget.getY() - BotConfig.OVERLAY_TEXT_Y_OFFSET;

            g2d.setColor(Color.blue);
            g2d.fillRect(x, y, overlaySize.width, overlaySize.height);
            g2d.setFont(BotConfig.OVERLAY_FONT);
            g2d.setColor(Color.WHITE);

            g2d.drawString(Main.currentAction, x + BotConfig.OVERLAY_TEXT_X_OFFSET, y + BotConfig.OVERLAY_TEXT_Y_OFFSET);
            g2d.drawString("UPTIME: " + GetFormattedTime(GetTimeSeconds(Main.loggedInMillis), false), x + BotConfig.OVERLAY_TEXT_X_OFFSET, y + BotConfig.OVERLAY_TEXT_Y_OFFSET * 2);
            g2d.drawString("SESSION PROFIT: " + GetFormattedNumbers(Main.sessionProfit, true, false), x + BotConfig.OVERLAY_TEXT_X_OFFSET, y + BotConfig.OVERLAY_TEXT_Y_OFFSET * 3);
            g2d.drawString("GP/HOUR: " + (Main.sessionProfit != 0 ? GetFormattedNumbers(((Main.sessionProfit / GetTimeSeconds(Main.loggedInMillis)) * 3600), false, false) : "-"), x + BotConfig.OVERLAY_TEXT_X_OFFSET, y + BotConfig.OVERLAY_TEXT_Y_OFFSET * 4);

            if (Main.startingCash > 0) {
                g2d.drawString("STARTING CASH: " + GetFormattedNumbers(Main.startingCash, false, false), x + BotConfig.OVERLAY_TEXT_X_OFFSET, y + BotConfig.OVERLAY_TEXT_Y_OFFSET * 6);
            }

            if (Client.getGameState() == GameState.LOGIN_SCREEN) {
                g2d.drawString("TIME BEFORE LOGGING BACK IN: " + GetFormattedTime((int) ((Main.loggingBackInMillis - System.currentTimeMillis()) / 1000), false), 3, 15);
            }

            DrawGEOverlay(g2d);
        }
    }

    private static void DrawGEOverlay(Graphics2D g2d) {
        for (ActiveFlip flip : Flipper.activeFlips) {
            if (flip.slot != -1) {
                WidgetChild widget = Widgets.getWidgetChild(slotWidgets.get(flip.slot)[0], slotWidgets.get(flip.slot)[1]);
                if (widget != null && widget.isVisible()) {
                    if (flip.buy) {
                        g2d.setColor(Color.green);
                    } else {
                        g2d.setColor(Color.red);
                    }
                    g2d.fillRect(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight() / 4);
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(BotConfig.SLOT_OVERLAY_FONT);
                    g2d.drawString(GetFormattedTime(GetTimeSeconds(flip.startedTimeEpochsMs), true), widget.getX() + 15, (widget.getY() + ((widget.getHeight() / 4) / 2)) - 2);
                    g2d.drawString(String.format("%.2f", GEController.GetCompletedPercentage(flip.item, flip.buy)) + "% - " + GetFormattedNumbers(flip.item.potentialProfitGp, true, false), widget.getX() + 8, (widget.getY() + ((widget.getHeight() / 4) / 2)) + 11);
                }
            } else {
                flip.slot = GEController.GetSlotFromItem(flip.item, flip.buy);
            }
        }
    }

    public static int GetTimeSeconds(long startMillis) {
        return (int) (System.currentTimeMillis() - startMillis) / 1000;
    }

    public static String GetFormattedTime(int uptimeSeconds, boolean shortened) {
        int seconds = uptimeSeconds % 60;
        int minutes = (uptimeSeconds % 3600) / 60;
        int hours = uptimeSeconds / 3600;

        if (shortened) {
            return hours + "H:" + minutes + "M:" + seconds + "S";
        }

        return hours + " hours, " + minutes + " minutes, " + seconds + " seconds";
    }

    public static String GetFormattedNumbers(int gold, boolean roundGold, boolean isVolume) {
        int startGold = gold;
        String suffix = "K";
        if (roundGold) {
            if (gold >= 1000000) {
                gold /= 1000000;
                suffix = "M";
            } else if (gold >= 1000) {
                gold /= 1000;
            }
        }

        return NumberFormat.getInstance(Locale.US).format(gold) + (roundGold && startGold >= 1000 ? suffix : !isVolume ? " gp" : "");
    }
}
