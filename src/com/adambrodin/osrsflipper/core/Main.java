package com.adambrodin.osrsflipper.core;

import com.adambrodin.osrsflipper.gui.IngameGUI;
import com.adambrodin.osrsflipper.misc.AccountSetup;
import com.adambrodin.osrsflipper.misc.BotConfig;
import org.dreambot.api.Client;
import org.dreambot.api.data.GameState;
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
    }

    @Override
    public int onLoop() {
        if (Client.getGameState() == GameState.LOGGED_IN) {
            if (!hasLoggedIn) {
                hasLoggedIn = true;
                IngameGUI.hasLoggedIn = true;
                IngameGUI.loggedInMillis = System.currentTimeMillis();
            }

            AccountSetup.SetupTrading();

            // Checks if flips are finished and creates new ones if needed
            Flipper.ExecuteFlips();
        } else if (hasLoggedIn) {
            getRandomManager().disableSolver(RandomEvent.LOGIN);
            log("Logged out! Waiting " + BotConfig.LOGOUT_SLEEP_DURATION_MINUTES + " minutes before logging back in.");
            sleepUntil(() -> Client.getGameState() == GameState.LOGGED_IN, (BotConfig.LOGOUT_SLEEP_DURATION_MINUTES * 60) * 1000);
            log("Logging back in!");
            getRandomManager().enableSolver(RandomEvent.LOGIN);
            sleepUntil(() -> Client.getGameState() == GameState.LOGGED_IN, 60000);

            if (BotConfig.DISABLE_AUTOLOGIN) {
                getRandomManager().disableSolver(RandomEvent.LOGIN);
            }
        }
        return 0;
    }

    @Override
    public void onPaint(Graphics2D graphics) {
        super.onPaint(graphics);
        IngameGUI.Draw(graphics);
    }
}
