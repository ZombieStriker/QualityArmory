package me.zombie_striker.qg.miscitems;

import org.bukkit.entity.Player;

import me.zombie_striker.qg.ArmoryBaseObject;

public interface InteractableMisc extends ArmoryBaseObject {
	void onRightClick(Player thrower);

	void onLeftClick(Player thrower);

}
