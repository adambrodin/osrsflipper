package com.adambrodin.osrsflipper.core;

import com.adambrodin.osrsflipper.gui.IngameGUI;
import com.adambrodin.osrsflipper.io.SaveManager;
import com.adambrodin.osrsflipper.misc.AccountSetup;
import com.adambrodin.osrsflipper.misc.BotConfig;
import org.dreambot.api.Client;
import org.dreambot.api.data.GameState;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.randoms.RandomEvent;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

import java.awt.*;

@ScriptManifest(category = Category.MONEYMAKING, name = "OSRS Flipper", author = "Adam Brodin", version = 1.0)
public class Main extends AbstractScript {
    private boolean hasLoggedIn = false;

    @Override
    public void onStart() {
        // Disable auto-login
        if (BotConfig.DISABLE_AUTOLOGIN) {
            getRandomManager().disableSolver(RandomEvent.LOGIN);
        }

        // Load all the saved data/flips/settings etc
        SaveManager.Load();
        SaveManager.sessionStartFlipsInitiated = SaveManager.tradingInfo.totalFlipsInitiated;
        Flipper.activeFlips = SaveManager.GetSavedFlips();
        for (int i = 0; i < Flipper.activeFlips.size(); i++) {
            if (!Inventory.contains(Flipper.activeFlips.get(i).item.item.itemName) && !GEController.ItemInSlot(Flipper.activeFlips.get(i).item)) {
                log("Flip no longer active, removing! - " + Flipper.activeFlips.get(i).toString());
                Flipper.activeFlips.remove(Flipper.activeFlips.get(i));
                SaveManager.SaveActiveFlips(Flipper.activeFlips);
                continue;
            }

            log("Loaded saved flip: " + Flipper.activeFlips.get(i).toString());
        }
    }

    @Override
    public int onLoop() {
        if (Client.getGameState() == GameState.LOGGED_IN) {
            if (!hasLoggedIn) {
                getRandomManager().disableSolver(RandomEvent.LOGIN);
                hasLoggedIn = true;
                IngameGUI.hasLoggedIn = true;
                IngameGUI.loggedInMillis = System.currentTimeMillis();
            }

            AccountSetup.SetupTrading();

            // Checks if flips are finished and creates new ones if needed
            Flipper.ExecuteFlips();
        } else if (hasLoggedIn) {
            getRandomManager().disableSolver(RandomEvent.LOGIN);
            IngameGUI.loggingBackInMillis = System.currentTimeMillis() + ((BotConfig.LOGOUT_SLEEP_DURATION_MINUTES * 60) * 1000);
            log("Logged out! Waiting " + BotConfig.LOGOUT_SLEEP_DURATION_MINUTES + " minutes before logging back in.");
            sleepUntil(() -> Client.getGameState() == GameState.LOGGED_IN, (BotConfig.LOGOUT_SLEEP_DURATION_MINUTES * 60) * 1000);
            log("Logging back in!");
            getRandomManager().enableSolver(RandomEvent.LOGIN);
            sleepUntil(() -> Client.getGameState() == GameState.LOGGED_IN, 60000);
            getRandomManager().disableSolver(RandomEvent.LOGIN);
        }
        return 0;
    }

    @Override
    public void onPaint(Graphics2D graphics) {
        super.onPaint(graphics);
        IngameGUI.Draw(graphics);
    }

    @Override
    public void onExit() {
        if (hasLoggedIn) {
            SaveManager.tradingInfo.totalUptimeSeconds += IngameGUI.GetTimeSeconds(IngameGUI.loggedInMillis);
            SaveManager.tradingInfo.totalProfitGp += IngameGUI.sessionProfit;
            SaveManager.SaveActiveFlips(Flipper.activeFlips);
            log("Session ended! (" + IngameGUI.GetFormattedTime(IngameGUI.GetTimeSeconds(IngameGUI.loggedInMillis), false) + ") - PROFIT: " + IngameGUI.GetFormattedGold(IngameGUI.sessionProfit, true)
                    + " - " + (SaveManager.tradingInfo.totalFlipsInitiated - SaveManager.sessionStartFlipsInitiated) + "x flips initiated.")
            ;
        }
        super.onExit();
    }
}
