package com.adambrodin.osrsflipper.misc;

import com.adambrodin.osrsflipper.core.Flipper;
import com.adambrodin.osrsflipper.core.GEController;
import com.adambrodin.osrsflipper.gui.IngameGUI;
import com.adambrodin.osrsflipper.io.SaveManager;
import org.dreambot.api.Client;
import org.dreambot.api.data.GameState;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.wrappers.interactive.NPC;

import java.util.ArrayList;
import java.util.List;

import static org.dreambot.api.Client.getLocalPlayer;
import static org.dreambot.api.methods.MethodProvider.*;

public class AccountSetup {
    private static boolean bankIsChecked = false;

    // Does the essential things before trading can start (e.g walk to GE && get cash)
    public static void SetupTrading() {
        if (!BotConfig.GRANDEXCHANGE_AREA.contains(getLocalPlayer())) {
            TravelToGE();
        }

        if (GrandExchange.isOpen() && !bankIsChecked) {
            GrandExchange.close();
            sleepUntil(() -> !GrandExchange.isOpen(), BotConfig.MAX_ACTION_TIMEOUT_MS);
        }

        if (!IsReadyToTrade() && Client.getGameState() == GameState.LOGGED_IN) {
            PrepareForTrading();
        }
    }

    public static boolean IsReadyToTrade() {
        // getFirstOpenSlot is only -1 if there are no slots available
        return bankIsChecked && GrandExchange.isOpen();
    }

    private static void PrepareForTrading() {
        try {
            if (!bankIsChecked) {
                if (NPCs.closest("Banker") != null) {
                    NPCs.closest("Banker").interact("Bank");
                } else if (Bank.getBank() != null) {
                    Bank.openClosest();
                } else {
                    logError("No bank was found!");
                }

                sleepUntil(Bank::isOpen, BotConfig.MAX_ACTION_TIMEOUT_MS);
                sleep(500);

                // Items that shouldn't be deposited (that are actively flipped)
                List<String> flipItems = new ArrayList<>();
                flipItems.add("Coins");
                Flipper.activeFlips.forEach(flip -> flipItems.add(flip.item.item.itemName));

                Bank.depositAllExcept(i -> flipItems.contains(i.getName()));
                sleepUntil(() -> Inventory.onlyContains(i -> flipItems.contains(i.getName())) || Inventory.getEmptySlots() == 28, BotConfig.MAX_ACTION_TIMEOUT_MS);
                if (Bank.contains("Coins")) {
                    Bank.withdrawAll("Coins");
                    sleep(500);
                }
                sleepUntil(() -> Inventory.contains("Coins"), BotConfig.MAX_ACTION_TIMEOUT_MS);
                Bank.close();
                sleepUntil(() -> !Bank.isOpen(), BotConfig.MAX_ACTION_TIMEOUT_MS);
                bankIsChecked = true;
                IngameGUI.startingCash = Inventory.get("Coins").getAmount();
            }

            if (!GrandExchange.isOpen()) {
                NPC clerk = NPCs.closest("Grand Exchange Clerk");
                if (clerk != null) {
                    clerk.interact("Exchange");
                    sleepUntil(GrandExchange::isOpen, BotConfig.MAX_ACTION_TIMEOUT_MS);
                    sleep(500);
                }

                for (int i = 0; i < Flipper.activeFlips.size(); i++) {
                    if (!Inventory.contains(Flipper.activeFlips.get(i).item.item.itemName) && !GEController.ItemInSlot(Flipper.activeFlips.get(i).item)) {
                        log("Flip no longer active, removing! - " + "[" + (Flipper.activeFlips.get(i).buy ? "BUY" : "SELL") + "]: " + Flipper.activeFlips.get(i).amount + "x " + Flipper.activeFlips.get(i).item.item.itemName);
                        Flipper.activeFlips.remove(Flipper.activeFlips.get(i));
                        SaveManager.SaveActiveFlips(Flipper.activeFlips);
                    }

                    log("Loaded saved flip: " + "[" + (Flipper.activeFlips.get(i).buy ? "BUY" : "SELL") + "]: " + Flipper.activeFlips.get(i).amount + "x " + Flipper.activeFlips.get(i).item.item.itemName);
                }
            }
        } catch (Exception ignored) {
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
