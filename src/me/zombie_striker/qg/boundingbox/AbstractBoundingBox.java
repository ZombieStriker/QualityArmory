package me.zombie_striker.qg.boundingbox;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface AbstractBoundingBox {

	boolean intersects(Location check, Entity base);
	boolean allowsHeadshots();
	boolean isHeadShot(Location check, Entity base);
	double maximumCheckingDistance(Entity base);
}
