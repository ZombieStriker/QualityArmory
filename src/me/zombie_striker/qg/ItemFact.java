package me.zombie_striker.qg;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import me.zombie_striker.qg.ammo.Ammo;
import me.zombie_striker.qg.armor.ArmorObject;
import me.zombie_striker.qg.guns.Gun;
import me.zombie_striker.qg.handlers.SkullHandler;
import me.zombie_striker.qg.miscitems.IronSightsToggleItem;
import me.zombie_striker.qg.miscitems.MedKit;

public class ItemFact {

	public static List<String> getArmorLore(ArmorObject a, ItemStack current) {
		List<String> lore = new ArrayList<>();
		if (a.getCustomLore() != null)
			lore.addAll(a.getCustomLore());

		if (current != null && current.hasItemMeta() && current.getItemMeta().hasLore())
			for (String s : current.getItemMeta().getLore()) {
				if (ChatColor.stripColor(s).contains("UUID")) {
					lore.add(s);
					break;
				}
			}
		return lore;
	}

	private static final String CALCTEXT = ChatColor.BLACK + "useddata:";

	public static int getCalculatedExtraDurib(ItemStack is) {
		if (!is.hasItemMeta() || !is.getItemMeta().hasLore() || is.getItemMeta().getLore().isEmpty())
			return -1;
		List<String> lore = is.getItemMeta().getLore();
        for(String s : lore) {
            if(s.startsWith(CALCTEXT))
                return Integer.parseInt(s.split(CALCTEXT)[1]);
        }
		return -1;
	}

	public static ItemStack addCalulatedExtraDurib(ItemStack is, int number) {
		ItemMeta im = is.getItemMeta();
		List<String> lore = im.getLore();
		if (lore == null) {
			lore = new ArrayList<>();
		} else {
			if (getCalculatedExtraDurib(is) != -1)
				is = removeCalculatedExtra(is);
		}
		lore.add(CALCTEXT + number);
		im.setLore(lore);
		is.setItemMeta(im);
		return is;
	}

	public static ItemStack decrementCalculatedExtra(ItemStack is) {
		ItemMeta im = is.getItemMeta();
		List<String> lore = is.getItemMeta().getLore();
		for (int i = 0; i < lore.size(); i++) {
			if (lore.get(i).startsWith(CALCTEXT)) {
				lore.set(i, CALCTEXT + "" + (Integer.parseInt(lore.get(i).split(CALCTEXT)[1]) - 1));
			}
		}
		im.setLore(lore);
		is.setItemMeta(im);
		return is;
	}

	public static void addOutOfAmmoToDisplayname(final Gun g, final Player player, ItemStack is, final int slot) {
		final ItemStack k = is;
		ItemMeta im = k.getItemMeta();
		im.setDisplayName(g.getDisplayName() + QAMain.S_OUT_OF_AMMO);
		k.setItemMeta(im);
		player.getInventory().setItem(slot, k);
		new BukkitRunnable() {
			public void run() {
				removeOutOfAmmoToDisplayname(g, player, k, slot);
			}
		}.runTaskLater(QAMain.getInstance(), 20 * 3);
	}

	public static void removeOutOfAmmoToDisplayname(final Gun g, final Player player, ItemStack is, final int slot) {
		ItemStack temp = player.getInventory().getItem(slot);
		if (temp != null && temp.isSimilar(is)) {
			ItemMeta im = is.getItemMeta();
			im.setDisplayName(g.getDisplayName());
			is.setItemMeta(im);
			player.getInventory().setItem(slot, is);
		}
	}

	public static ItemStack removeCalculatedExtra(ItemStack is) {
		if (is.hasItemMeta() && is.getItemMeta().hasLore()) {
			ItemMeta im = is.getItemMeta();
			List<String> lore = is.getItemMeta().getLore();
			for (int i = 0; i < lore.size(); i++) {
				if (lore.get(i).startsWith(CALCTEXT)) {
					lore.remove(i);
				}
			}
			im.setLore(lore);
			is.setItemMeta(im);
		}
		return is;
	}

	public static List<String> getGunLore(Gun g, ItemStack current, int amount) {
		List<String> lore = new ArrayList<>();
		if (g.getCustomLore() != null)
			lore.addAll(g.getCustomLore());
		addVarientData(lore, g);
		lore.add(QAMain.S_ITEM_BULLETS + ": " + (amount) + "/" + (g.getMaxBullets()));
		if (QAMain.ENABLE_LORE_INFO) {
			lore.add(QAMain.S_ITEM_DAMAGE + ": " + g.getDamage());
			lore.add(QAMain.S_ITEM_DPS + ": "
					+ (g.isAutomatic()
							? (2 * g.getFireRate() * g.getDamage()) + ""
									+ (g.getBulletsPerShot() > 1 ? "x" + g.getBulletsPerShot() : "")
							: "" + ((int) (1.0 / g.getDelayBetweenShotsInSeconds()) * g.getDamage())
									+ (g.getBulletsPerShot() > 1 ? "x" + g.getBulletsPerShot() : "")));
			if (g.getAmmoType() != null)
				lore.add(QAMain.S_ITEM_AMMO + ": " + g.getAmmoType().getDisplayName());
		}
		if (QAMain.AutoDetectResourcepackVersion && Bukkit.getPluginManager().isPluginEnabled("ViaRewind")) {
			if (g.is18Support()) {
				lore.add(ChatColor.GRAY + "1.8 Weapon");
			}
		}

		if (QAMain.enableDurability)
			if (current == null) {
				lore.add(QAMain.S_ITEM_DURIB + ":" + g.getDurability() + "/" + g.getDurability());
			} else {
				lore = setDamage(g, lore, getDamage(current));
			}
		if (QAMain.ENABLE_LORE_HELP) {
			if (g.isAutomatic()) {
				lore.add(QAMain.S_LMB_SINGLE);
				lore.add(QAMain.S_LMB_FULLAUTO);
				lore.add(QAMain.S_RMB_RELOAD);
			} else {
				lore.add(QAMain.S_LMB_SINGLE);
				lore.add(QAMain.enableIronSightsON_RIGHT_CLICK ? QAMain.S_RMB_R1 : QAMain.S_RMB_R2);
				if (g.hasIronSights())
					lore.add(QAMain.enableIronSightsON_RIGHT_CLICK ? QAMain.S_RMB_A1 : QAMain.S_RMB_A2);
			}
		}

		if (current != null && current.hasItemMeta() && current.getItemMeta().hasLore())
			for (String s : current.getItemMeta().getLore()) {
				if (ChatColor.stripColor(s).contains("UUID")) {
					lore.add(s);
					break;
				}
			}
		return lore;
	}

	public static ItemStack addShopLore(ArmoryBaseObject obj, ItemStack current) {
		ItemMeta meta = current.getItemMeta();
		List<String> lore = meta.getLore();
		for (String loreS : new ArrayList<>(lore)) {
			if (loreS.startsWith(ChatColor.DARK_RED + "Crafts") || loreS.startsWith(ChatColor.RED + QAMain.S_ITEM_ING)
					|| loreS.startsWith(ChatColor.RED + "-"))
				lore.remove(loreS);

		}
		if (obj.getCraftingReturn() > 1)
			lore.add(ChatColor.DARK_RED + "Returns: " + obj.getCraftingReturn());
		lore.add(QAMain.S_ITEM_COST + (obj.cost()));
		meta.setLore(lore);
		ItemStack is = current;
		is.setItemMeta(meta);
		return is;
	}

	public static List<String> getCraftingGunLore(Gun g) {
		List<String> lore = getGunLore(g, null, 1);
		lore.addAll(getCraftingLore(g));
		return lore;
	}

	@SuppressWarnings("deprecation")
	public static List<String> getCraftingLore(ArmoryBaseObject a) {
		List<String> lore = new ArrayList<>();
		lore.add(ChatColor.RED + QAMain.S_ITEM_ING + ": ");
		for (ItemStack is : a.getIngredients()) {
			StringBuilder sb = new StringBuilder();
			sb.append(ChatColor.RED + "-" + is.getAmount() + " " + is.getType().name());
			if (is.getDurability() != 0)
				sb.append(":" + is.getDurability());
			lore.add(sb.toString());
		}
		if (a.getCraftingReturn() > 1) {
			lore.add(ChatColor.DARK_RED + "Crafts " + a.getCraftingReturn());
		}
		return lore;
	}

	public static ItemStack getGun(int durib) {
		return getGun(durib, 0);
	}

	public static ItemStack getGun(int durib, int varient) {
		return getGun(Material.DIAMOND_AXE, durib, varient);
	}

	public static ItemStack getGun(Material type, int durib, int varient) {
		return getGun(MaterialStorage.getMS(type, durib, varient, null));
	}

	public static ItemStack getGun(MaterialStorage durib) {
		Gun g = QAMain.gunRegister.get(durib);
		return getGun(g);
	}

	@SuppressWarnings("deprecation")
	public static ItemStack getIronSights() {

		ItemStack ironsights = new ItemStack(IronSightsToggleItem.getMat(), 1, (short) IronSightsToggleItem.getData());
		ItemMeta im = ironsights.getItemMeta();
		im.setDisplayName(IronSightsToggleItem.getItemName());
		if (QAMain.ITEM_enableUnbreakable) {
			try {
				im.setUnbreakable(true);
			} catch (Error | Exception e3423) {
				try {
					im.spigot().setUnbreakable(true);
				} catch (Error | Exception e342) {

				}
			}
		}
		try {
			if (QAMain.ITEM_enableUnbreakable) {
				im.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
			}
			im.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
			im.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_DESTROYS);
		} catch (Error | Exception e34) {
		}
		ironsights.setItemMeta(im);
		return ironsights;
	}

	@SuppressWarnings("deprecation")
	public static ItemStack getGun(Gun g) {

		MaterialStorage ms = g.getItemData();
		String displayname = g.getDisplayName();
		if (ms == null || ms.getMat() == null)
			return new ItemStack(Material.AIR);

		ItemStack is = new ItemStack(ms.getMat(), 0, (short) ms.getData());
		if (ms.getData() < 0)
			is.setDurability((short) 0);
		ItemMeta im = is.getItemMeta();
		if (im == null)
			im = Bukkit.getServer().getItemFactory().getItemMeta(ms.getMat());
		if (im != null) {
			im.setDisplayName(displayname);
			List<String> lore = getGunLore(g, null, g.getMaxBullets());
			im.setLore(lore);
			if (QAMain.ITEM_enableUnbreakable) {
				try {
					im.setUnbreakable(true);
				} catch (Error | Exception e34) {
					try {
						im.spigot().setUnbreakable(true);
					} catch (Error | Exception e344) {
					}
				}
			}
			try {
				if (QAMain.ITEM_enableUnbreakable) {
					im.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
				}
				im.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
				im.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_DESTROYS);
			} catch (Error e) {

			}

			is.setItemMeta(im);
		} else {
			// Item meta is still null. Catch and report.
			QAMain.getInstance().getLogger()
					.warning(QAMain.prefix + " ItemMeta is null for " + g.getName() + ". I have");
		}
		if (QAMain.enableVisibleAmounts)
			is.setAmount(g.getMaxBullets() > 64 ? 64 : g.getMaxBullets());
		else
			is.setAmount(1);

		is = addGunRegister(is);
		return is;
	}

	@SuppressWarnings("deprecation")
	public static ItemStack getArmor(ArmorObject a) {
		ItemStack is = new ItemStack(a.getItemData().getMat(), 0, (short) a.getItemData().getData());
		if (a.getItemData().getData() < 0)
			is.setDurability((short) 0);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(a.getDisplayName());
		List<String> lore = getArmorLore(a, is);
		addVarientData(lore, a);
		im.setLore(lore);
		if (QAMain.ITEM_enableUnbreakable) {
			try {
				im.setUnbreakable(true);
			} catch (Error | Exception e34) {
				try {
					im.spigot().setUnbreakable(true);
				} catch (Error | Exception e343) {
				}
			}
		}
		try {
			if (QAMain.ITEM_enableUnbreakable) {
				im.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
			}
			im.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
			im.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_DESTROYS);
		} catch (Error e) {

		}

		is.setItemMeta(im);
		return is;
	}

	public static ItemStack getAmmo(Material m, int data, String extraValues) {
		return getAmmo(m, data, 0, extraValues);
	}

	public static ItemStack getAmmo(Material m, int data, int varient, String extraValues) {
		if (QAMain.ammoRegister.containsKey(MaterialStorage.getMS(m, data, varient, extraValues)))
			return getAmmo(QAMain.ammoRegister.get(MaterialStorage.getMS(m, data, varient, extraValues)));
		return null;
	}

	public static ItemStack getAmmo(Ammo a) {
		return getAmmo(a, a.getMaxAmount() > 64 ? 64 : a.getMaxAmount());
	}

	@SuppressWarnings("deprecation")
	public static ItemStack getAmmo(Ammo a, int amount) {
		ItemStack is = new ItemStack(a.getItemData().getMat(), 0, (short) a.getItemData().getData());
		boolean setSkull = false;
		if (a.isSkull() && a.hasCustomSkin()) {
			setSkull = true;
			is = SkullHandler.getCustomSkull64(a.getCustomSkin());
		}
		if (a.getItemData().getData() < 0)
			is.setDurability((short) 0);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(a.getDisplayName());
		if (QAMain.ITEM_enableUnbreakable) {
			try {
				im.setUnbreakable(true);
			} catch (Error | Exception e34) {
				try {
					im.spigot().setUnbreakable(true);
				} catch (Error | Exception e343) {

				}
			}
		}
		try {
			if (QAMain.ITEM_enableUnbreakable) {
				im.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
			}
			im.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
			im.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_DESTROYS);
		} catch (Error e) {
		}
		List<String> lore = new ArrayList<>();
		lore.addAll(a.getCustomLore());
		addVarientData(lore, a);
		im.setLore(lore);
		if (a.isSkull() && !setSkull) {
			((SkullMeta) im).setOwner(a.getSkullOwner());
		}

		is.setItemMeta(im);
		is.setAmount(amount);

		return is;
	}

	public static ItemStack getObject(ArmoryBaseObject a) {
		return getObject(a, a.getCraftingReturn());
	}

	@SuppressWarnings("deprecation")
	public static ItemStack getObject(ArmoryBaseObject a, int amount) {
		ItemStack is = new ItemStack(a.getItemData().getMat(), 0, (short) a.getItemData().getData());
		if (a.getItemData().getData() < 0)
			is.setDurability((short) 0);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(a.getDisplayName());

		if (QAMain.ITEM_enableUnbreakable) {
			try {
				im.setUnbreakable(true);
			} catch (Error | Exception e34) {
				try {
					im.spigot().setUnbreakable(true);
				} catch (Error | Exception e343) {

				}
			}
		}
		try {
			if (QAMain.ITEM_enableUnbreakable) {
				im.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
			}
			im.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
			im.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_DESTROYS);
		} catch (Error e) {
		}
		List<String> lore = new ArrayList<>();
		if (a instanceof MedKit) {
			if (QAMain.ENABLE_LORE_HELP) {
				lore.add(QAMain.S_MEDKIT_LORE_INFO);
			}
		}
		if (a.getCustomLore() != null)
			lore.addAll(a.getCustomLore());
		addVarientData(lore, a);
		im.setLore(lore);

		is.setItemMeta(im);
		is.setAmount(amount);

		return is;
	}

	public static int getDamage(ItemStack is) {
		for (String lore : is.getItemMeta().getLore()) {
			if (ChatColor.stripColor(lore).startsWith(QAMain.S_ITEM_DURIB)) {
				return Integer.parseInt(lore.split(":")[1].split("/")[0].trim());
			}
		}
		return -1;
	}

	public static ItemStack damage(Gun g, ItemStack is) {
		return setDamage(g, is, getDamage(is) - 1);
	}

	public static ItemStack setDamage(Gun g, ItemStack is, int damage) {
		ItemMeta im = is.getItemMeta();
		im.setLore(setDamage(g, im.getLore(), damage));
		is.setItemMeta(im);
		return is;
	}

	public static List<String> setDamage(Gun g, List<String> lore, int damage) {
		boolean foundLine = false;
		double k = ((double) damage) / g.getDurability();
		ChatColor c = k > 0.5 ? ChatColor.DARK_GREEN : k > 0.25 ? ChatColor.GOLD : ChatColor.DARK_RED;
		for (int j = 0; j < lore.size(); j++) {
			if (ChatColor.stripColor(lore.get(j)).contains(QAMain.S_ITEM_DURIB)) {
				lore.set(j, c + QAMain.S_ITEM_DURIB + ":" + damage + "/" + g.getDurability());
				foundLine = true;
				break;
			}
		}
		if (!foundLine) {
			lore.add(c + QAMain.S_ITEM_DURIB + ":" + damage + "/" + g.getDurability());
		}
		return lore;
	}

	public static ItemStack addGunRegister(ItemStack base) {
		ItemMeta im = base.getItemMeta();
		try {
			if (QAMain.enableVisibleAmounts)
				im.setLocalizedName("" + UUID.randomUUID());
		} catch (Exception | Error e) {
			List<String> lore = im.getLore();
			lore.add(ChatColor.DARK_GRAY + "UUID" + UUID.randomUUID().toString());
			im.setLore(lore);
		}
		base.setItemMeta(im);
		return base;
	}

	public static boolean sameGun(ItemStack is1, ItemStack is2) {
		if (!QAMain.enableVisibleAmounts)
			return false;
		try {
			if (is1.hasItemMeta() && is1.getItemMeta().hasLocalizedName())
				if (is2.hasItemMeta() && is2.getItemMeta().hasLocalizedName())
					return is1.getItemMeta().getLocalizedName().equals(is2.getItemMeta().getLocalizedName());
		} catch (Exception | Error e1) {
			if (is1.hasItemMeta() && is1.getItemMeta().hasLore())
				if (is2.hasItemMeta() && is2.getItemMeta().hasLore())
					for (String s : is1.getItemMeta().getLore())
						if (s.contains("UUID"))
							return is2.getItemMeta().getLore().contains(s);
		}
		return false;
	}

	public static void addVarientData(List<String> lore, ArmoryBaseObject object) {
		if (object.getItemData().isVarient())
			lore.add(QAMain.S_ITEM_VARIENTS_NEW + " " + object.getItemData().getVarient());
	}

	public static int getAmount(ItemStack is) {
		if (is != null) {
			if (is.hasItemMeta() && is.getItemMeta().hasLore()) {
				for (String lore : is.getItemMeta().getLore()) {
					if (lore.contains(QAMain.S_ITEM_BULLETS)) {
						return Integer.parseInt(lore.split(":")[1].split("/")[0].trim());
					}
				}
				return 0;
			} else {
				if (QAMain.enableVisibleAmounts)
					return is.getAmount();
			}
		}
		return 0;
	}

}
