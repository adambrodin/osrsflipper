package com.adambrodin.osrsflipper.core;

import com.adambrodin.osrsflipper.misc.AccountSetup;
import com.adambrodin.osrsflipper.misc.BotConfig;
import org.dreambot.api.Client;
import org.dreambot.api.data.GameState;
import org.dreambot.api.randoms.RandomEvent;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

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
            }

            AccountSetup.SetupTrading();

            // Checks if flips are finished and creates new ones if needed
            Flipper.ExecuteFlips();
        } else if (hasLoggedIn) {
            getRandomManager().disableSolver(RandomEvent.LOGIN);
            log("Logged out! Waiting " + BotConfig.LOGOUT_SLEEP_DURATION_MINUTES + " minutes before logging back in.");
            sleep((BotConfig.LOGOUT_SLEEP_DURATION_MINUTES * 60) * 1000);
            log("Logging back in!");
            getRandomManager().enableSolver(RandomEvent.LOGIN);
        }
        return 0;
    }
}
