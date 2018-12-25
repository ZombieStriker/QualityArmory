package me.zombie_striker.qg.armor;


import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.zombie_striker.qg.api.QualityArmory;

public class BulletProtectionUtil {

	public static boolean stoppedBullet(Player p, Location bullet, Vector velocity) {
		if (p.getInventory().getHelmet() != null) {
			if (me.zombie_striker.qg.api.QualityArmory.isArmor(p.getInventory().getHelmet())) {
				ArmorObject armor = me.zombie_striker.qg.api.QualityArmory.getArmor(p.getInventory().getHelmet());
				double offset = (p.isSneaking() ? armor.getShifitngHeightOffset() : 0);
				if (bullet.getY() - p.getLocation().getY() > armor.getMinH() + offset) {
					if (bullet.getY() - p.getLocation().getY() < armor.getMaxH() + offset) {
						// Within kevlar range
						return true;
					} else {
						if (velocity.getY() < 0) {
							Vector fourth = velocity.normalize().multiply(0.25);
                            return bullet.clone().add(fourth).getY() < armor.getMaxH() + offset;
						} else {
							return false;
						}
					}
				} else {
					if (velocity.getY() > 0) {

						Vector fourth = velocity.normalize().multiply(0.25);
                        return bullet.clone().add(fourth).getY() > armor.getMinH() + offset;
					} else {
						return false;
					}
				}
			}
		}
		return false;
	}
	
	public static boolean  negatesHeadshot(Player p) {
		if (p.getInventory().getHelmet() != null) {
			if (QualityArmory.isArmor(p.getInventory().getHelmet())) {
				ArmorObject armor = QualityArmory.getArmor(p.getInventory().getHelmet());
				return armor.getNegateHeadshots();
			}
		}
		return false;
	}
}
