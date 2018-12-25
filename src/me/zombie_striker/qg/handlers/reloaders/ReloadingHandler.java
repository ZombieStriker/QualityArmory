package me.zombie_striker.qg.handlers.reloaders;

import org.bukkit.entity.Player;

import me.zombie_striker.qg.guns.Gun;

public interface ReloadingHandler {

	boolean isReloading(Player player);
	double reload(Player player, Gun g, int amountReloading);
	
	String getName();
}
