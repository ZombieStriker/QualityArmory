package me.zombie_striker.qg.guns.projectiles;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.zombie_striker.qg.guns.Gun;

public interface RealtimeCalculationProjectile {

	String getName();
	
	void spawn(final Gun g, final Location starting, final Player player, final Vector dir);
}
