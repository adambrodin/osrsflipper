package com.adambrodin.osrsflipper.core;

import com.adambrodin.osrsflipper.gui.IngameGUI;
import com.adambrodin.osrsflipper.io.SaveManager;
import com.adambrodin.osrsflipper.misc.AccountSetup;
import com.adambrodin.osrsflipper.misc.BotConfig;
import org.dreambot.api.Client;
import org.dreambot.api.data.GameState;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.randoms.RandomEvent;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

import java.awt.*;

@ScriptManifest(category = Category.MONEYMAKING, name = "OSRS Flipper", author = "Adam Brodin", version = 1.0)
public class Main extends AbstractScript {
    public static boolean hasLoggedIn = false;
    public static long loggingBackInMillis;
    public static long loggedInMillis;
    public static String currentAction = "Idling...";
    public static int sessionProfit = 0;
    public static int startingCash = 0;

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
        logInfo("Loaded " + Flipper.activeFlips.size() + "x saved flips!");
    }

    @Override
    public int onLoop() {
        if (Client.getGameState() == GameState.LOGGED_IN) {
            if (!hasLoggedIn) {
                getRandomManager().disableSolver(RandomEvent.LOGIN);
                hasLoggedIn = true;
                loggedInMillis = System.currentTimeMillis();
            }

            AccountSetup.SetupTrading();

            // Checks if flips are finished and creates new ones if needed
            Flipper.ExecuteFlips();
        } else if (hasLoggedIn) {
            getRandomManager().disableSolver(RandomEvent.LOGIN);
            int logoutTimeMinutes = BotConfig.LOGOUT_SLEEP_DURATION_MINUTES + Calculations.random(-3, 3);
            loggingBackInMillis = System.currentTimeMillis() + ((logoutTimeMinutes * 60) * 1000);
            log("Logged out! Waiting " + logoutTimeMinutes + " minutes before logging back in.");
            sleepUntil(() -> Client.getGameState() == GameState.LOGGED_IN, (logoutTimeMinutes * 60) * 1000);
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
            SaveManager.tradingInfo.totalUptimeSeconds += IngameGUI.GetTimeSeconds(loggedInMillis);
            SaveManager.tradingInfo.totalProfitGp += sessionProfit;
            SaveManager.SaveActiveFlips(Flipper.activeFlips);
            log("Session ended! (" + IngameGUI.GetFormattedTime(IngameGUI.GetTimeSeconds(loggedInMillis), false) + ") - PROFIT: " + IngameGUI.GetFormattedNumbers(sessionProfit, true, false)
                    + " - " + (SaveManager.tradingInfo.totalFlipsInitiated - SaveManager.sessionStartFlipsInitiated) + "x flips initiated. - STARTING BALANCE: " + IngameGUI.GetFormattedNumbers(startingCash, false, false))
            ;
        }
        super.onExit();
    }
}
