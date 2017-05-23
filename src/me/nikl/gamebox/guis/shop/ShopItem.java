package me.nikl.gamebox.guis.shop;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Niklas.
 */
public class ShopItem {
    private List<String> commands = new ArrayList<>();
    private List<String> permissions = new ArrayList<>();
    private List<String> noPermissions = new ArrayList<>();

    private ItemStack itemStack;


    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public List<String> getNoPermissions() {
        return noPermissions;
    }

    public void setNoPermissions(List<String> noPermissions) {
        this.noPermissions = noPermissions;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
}
