package me.zombie_striker.qg.handlers.chargers;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.zombie_striker.qg.guns.Gun;

public interface ChargingHandler {

	boolean isCharging(Player player);
	boolean shoot(Gun g, Player player, ItemStack stack);
	
	String getName();
}
