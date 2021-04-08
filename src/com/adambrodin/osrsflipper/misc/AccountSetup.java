package com.adambrodin.osrsflipper.misc;

import org.dreambot.api.methods.grandexchange.GrandExchange;

import static org.dreambot.api.Client.getLocalPlayer;
import static org.dreambot.api.methods.MethodProvider.sleepUntil;

public class AccountSetup {
    private static boolean bankIsChecked;

    // Does the essential things before trading can start (e.g walk to GE && get cash)
    public static void SetupTrading()
    {
        if(!BotConfig.GRANDEXCHANGE_AREA.contains(getLocalPlayer()))
        {
            TravelToGE();
        }

        if(GrandExchange.isOpen())
        {
            GrandExchange.close();
            sleepUntil(() -> !GrandExchange.isOpen(), BotConfig.MAX_ACTION_TIMEOUT_MS);
        }

        if(!IsTradingPossible())
        {

        }
    }

    private static boolean IsTradingPossible()
    {
        if(!bankIsChecked)
        {

        }
        return false;
    }

    private static void TravelToGE()
    {

    }
}
