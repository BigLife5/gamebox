package me.nikl.gamebox.games.cookieclicker.upgrades.prism;

import me.nikl.gamebox.games.cookieclicker.CookieClicker;
import me.nikl.gamebox.games.cookieclicker.buildings.Buildings;
import me.nikl.gamebox.games.cookieclicker.upgrades.Upgrade;
import me.nikl.gamebox.games.cookieclicker.upgrades.UpgradeType;

/**
 * Created by Niklas on 09.07.2017.
 *
 */
public class LuxSanctorum extends Upgrade{

    public LuxSanctorum(CookieClicker game) {
        super(game, 306);
        this.cost = 1050000000000000000000000000.;
        productionsRequirements.put(Buildings.PRISM, 200);

        // for the standard upgrade type the building icon is used
        loadLanguage(UpgradeType.CLASSIC, Buildings.PRISM);
    }

    @Override
    public void onActivation() {
        game.getBuilding(Buildings.PRISM).multiply(2);
        game.getBuilding(Buildings.PRISM).visualize(game.getInventory());
        active = true;
    }
}