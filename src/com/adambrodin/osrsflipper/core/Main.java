package com.adambrodin.osrsflipper.core;

import com.adambrodin.osrsflipper.logic.FlipFinder;
import com.adambrodin.osrsflipper.misc.BotConfig;
import com.adambrodin.osrsflipper.models.FlipItem;
import org.dreambot.api.randoms.RandomEvent;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

@ScriptManifest(category = Category.MONEYMAKING, name = "OSRS Flipper", author = "Adam Brodin", version = 1.0)
public class Main extends AbstractScript {
    @Override
    public void onStart() {
        // Disable auto-login
        if (BotConfig.DISABLE_AUTOLOGIN) {
            getRandomManager().disableSolver(RandomEvent.LOGIN);
        }

        FlipFinder flipFinder = new FlipFinder();
        FlipItem bestItem = flipFinder.GetBestMarginItem(10000000);
        if (bestItem != null)
            log("The best margin item currently is: " + bestItem.item.itemName + " at " + bestItem.marginPerc + "% margin - " + bestItem.marginGp + "gp - potential profit: " + bestItem.potentialProfitGp + "gp"
                    + " - avgLowPrice: " + bestItem.avgLowPrice + " - averaged volume: " + bestItem.averagedVolume);
    }

    @Override
    public int onLoop() {
        return 0;
    }
}
