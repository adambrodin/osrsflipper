package com.adambrodin.osrsflipper.misc;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.wrappers.interactive.NPC;

import static org.dreambot.api.Client.getLocalPlayer;
import static org.dreambot.api.methods.MethodProvider.logError;
import static org.dreambot.api.methods.MethodProvider.sleepUntil;

public class AccountSetup {
    private static boolean bankIsChecked;

    // Does the essential things before trading can start (e.g walk to GE && get cash)
    public static void SetupTrading() {
        if (!BotConfig.GRANDEXCHANGE_AREA.contains(getLocalPlayer())) {
            TravelToGE();
        }

        if (GrandExchange.isOpen() && !bankIsChecked) {
            GrandExchange.close();
            sleepUntil(() -> !GrandExchange.isOpen(), BotConfig.MAX_ACTION_TIMEOUT_MS);
        }

        PrepareForTrading();
    }

    private static void PrepareForTrading() {
        if (!bankIsChecked || !Inventory.contains("Coins")) {
            if (NPCs.closest("Banker") != null) {
                NPCs.closest("Banker").interact("Bank");
            } else if (Bank.getBank() != null) {
                Bank.openClosest();
            } else {
                logError("No bank was found!");
            }

            sleepUntil(Bank::isOpen, BotConfig.MAX_ACTION_TIMEOUT_MS);
            Bank.depositAllExcept("Coins");
            sleepUntil(() -> Inventory.onlyContains("Coins") || Inventory.getEmptySlots() == 28, BotConfig.MAX_ACTION_TIMEOUT_MS);
            if (Bank.contains("Coins")) {
                Bank.withdrawAll("Coins");
            }
            sleepUntil(() -> Inventory.contains("Coins"), BotConfig.MAX_ACTION_TIMEOUT_MS);
            Bank.close();
            sleepUntil(() -> !Bank.isOpen(), BotConfig.MAX_ACTION_TIMEOUT_MS);
        }

        if (!GrandExchange.isOpen()) {
            NPC clerk = NPCs.closest("Grand Exchange Clerk");
            if (clerk != null) {
                clerk.interact("Exchange");
                sleepUntil(() -> GrandExchange.isOpen(), BotConfig.MAX_ACTION_TIMEOUT_MS);
            }
        }
    }

    private static void TravelToGE() {
        int randomEnergyActivate = Calculations.random(BotConfig.MIN_RUNENERGY_ACTIVATE, BotConfig.MAX_RUNENERGY_ACTIVATE);
        while (!BotConfig.GRANDEXCHANGE_AREA.contains(getLocalPlayer())) {
            if (!Walking.isRunEnabled() && Walking.getRunEnergy() >= randomEnergyActivate) {
                Walking.toggleRun();
                randomEnergyActivate = Calculations.random(BotConfig.MIN_RUNENERGY_ACTIVATE, BotConfig.MAX_RUNENERGY_ACTIVATE);
            }

            Walking.walk(BotConfig.GRANDEXCHANGE_AREA.getCenter());
            sleepUntil(Walking::shouldWalk, Calculations.random(BotConfig.MIN_WALK_CLICK_DELAY_MS, BotConfig.MAX_WALK_CLICK_DELAY_MS));
        }
    }
}
