package me.zombie_striker.qg;

import java.util.List;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public interface ArmoryBaseObject {

	String getName();
	ItemStack[] getIngredients();
	int getCraftingReturn();
	MaterialStorage getItemData();
	
	List<String> getCustomLore();
	String getDisplayName();
	
	double cost();
	
	ItemStack getItemStack();
	
	boolean is18Support();

	void set18Supported(boolean b);
	
	void onRMB(PlayerInteractEvent e, ItemStack usedItem);
	
	void onLMB(PlayerInteractEvent e, ItemStack usedItem);
}
