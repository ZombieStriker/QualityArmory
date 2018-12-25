package me.zombie_striker.qg;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import me.zombie_striker.pluginconstructor.HotbarMessager;
import me.zombie_striker.qg.ammo.Ammo;
import me.zombie_striker.qg.ammo.AmmoUtil;
import me.zombie_striker.qg.armor.ArmorObject;
import me.zombie_striker.qg.attachments.AttachmentBase;
import me.zombie_striker.qg.config.GunYML;
import me.zombie_striker.qg.config.GunYMLCreator;
import me.zombie_striker.qg.config.GunYMLLoader;
import me.zombie_striker.qg.guns.Gun;
import me.zombie_striker.qg.guns.utils.WeaponSounds;
import me.zombie_striker.qg.guns.utils.WeaponType;
import me.zombie_striker.qg.handlers.MultiVersionLookup;
import me.zombie_striker.qg.handlers.SkullHandler;
import me.zombie_striker.qg.handlers.WorldGuardSupport;
import me.zombie_striker.qg.miscitems.IronSightsToggleItem;

@Deprecated
public class QualityArmory {

	public static GunYML createAndLoadNewGun(String name, String displayname, Material material, int id,
			WeaponType type, WeaponSounds sound, boolean hasIronSights, String ammotype, int damage, int maxBullets,
			int cost) {
		File newGunsDir = new File(QAMain.getInstance().getDataFolder(), "newGuns");
		final File gunFile = new File(newGunsDir, name);
		new BukkitRunnable() {
			public void run() {
				GunYMLLoader.loadGuns(QAMain.getInstance(), gunFile);
			}
		}.runTaskLater(QAMain.getInstance(), 1);
		return GunYMLCreator.createNewCustomGun(QAMain.getInstance().getDataFolder(), name, name, displayname, id, null,
				type, sound, hasIronSights, ammotype, damage, maxBullets, cost).setMaterial(material);
	}

	public static GunYML createNewGunYML(String name, String displayname, Material material, int id, WeaponType type,
			WeaponSounds sound, boolean hasIronSights, String ammotype, int damage, int maxBullets, int cost) {
		return GunYMLCreator.createNewCustomGun(QAMain.getInstance().getDataFolder(), name, name, displayname, id, null,
				type, sound, hasIronSights, ammotype, damage, maxBullets, cost);
	}

	public static void registerNewUsedExpansionItem(Material used, int id) {
		registerNewUsedExpansionItem(used, id, 0);
	}

	public static void registerNewUsedExpansionItem(Material used, int id, int var) {
		QAMain.expansionPacks.add(MaterialStorage.getMS(used, id, var));
	}

	public static void sendResourcepack(final Player player, final boolean warning) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (QAMain.namesToBypass.contains(player.getName())) {
					QAMain.resourcepackReq.add(player.getUniqueId());
					return;
				}
				if (warning)
					try {
						player.sendTitle(ChatColor.RED + QAMain.S_NORES1, QAMain.S_NORES2);
					} catch (Error e2) {
						player.sendMessage(ChatColor.RED + QAMain.S_NORES1);
						player.sendMessage(ChatColor.RED + QAMain.S_NORES2);
					}
				player.sendMessage(QAMain.prefix + QAMain.S_RESOURCEPACK_HELP);
				new BukkitRunnable() {
					@Override
					public void run() {
						try {
							try {
								if (QAMain.AutoDetectResourcepackVersion
										&& us.myles.ViaVersion.bukkit.util.ProtocolSupportUtil
												.getProtocolVersion(player) < QAMain.ID18) {
									player.setResourcePack(QAMain.url18);
									QAMain.DEBUG("Sending 1.8 auto-detect resourcepack: User is using ID="
											+ (us.myles.ViaVersion.bukkit.util.ProtocolSupportUtil
													.getProtocolVersion(player) + ". ID for 1.8= " + QAMain.ID18));
									QAMain.DEBUG("URL:" + QAMain.url18);
								} else {
									player.setResourcePack(QAMain.url);
									QAMain.DEBUG("Sending default resourcepack: User is using ID="
											+ (us.myles.ViaVersion.bukkit.util.ProtocolSupportUtil
													.getProtocolVersion(player) + ". ID for 1.8= " + QAMain.ID18));
									QAMain.DEBUG("URL:" + QAMain.url);
								}
							} catch (Error | Exception e4) {
								if (QAMain.isVersionHigherThan(1, 9)) {
									QAMain.DEBUG("Sending default resourcepack");
									player.setResourcePack(QAMain.url);
									QAMain.DEBUG("URL:" + QAMain.url);
								} else {
									QAMain.DEBUG("Sending 1.8 resourcepack");
									player.setResourcePack(QAMain.url18);
									QAMain.DEBUG("URL:" + QAMain.url18);
								}
							}

							if (!QAMain.isVersionHigherThan(1, 9)) {
								QAMain.DEBUG("Using 1.8 lower resourcepack handling system");
								QAMain.resourcepackReq.add(player.getUniqueId());
								QAMain.sentResourcepack.put(player.getUniqueId(), System.currentTimeMillis());
							}
							// If the player is on 1.8, manually add them to the resource list.

						} catch (Exception e) {

						}
					}
				}.runTaskLater(QAMain.getInstance(), 20 * (warning ? 1 : 5));
			}
		}.runTaskLater(QAMain.getInstance(), (long) (20 * QAMain.secondsTilSend));
	}

	public static boolean allowGunsInRegion(Location loc) {
		if (!QAMain.supportWorldGuard)
			return true;
		try {
			return WorldGuardSupport.a(loc);
		} catch (Error e) {
		}
		return true;
	}

	public static boolean isCustomItem(ItemStack is) {
		return isCustomItem(is, 0);
	}

	public static ArmoryBaseObject getCustomItem(ItemStack is) {
		if (isGun(is))
			return getGun(is);
		if (isAmmo(is))
			return getAmmo(is);
		if (isArmor(is))
			return getArmor(is);
		if (isMisc(is))
			return getMisc(is);
		if (isGunWithAttchments(is))
			return QAMain.gunRegister.get(getGunWithAttchments(is).getBase());
		return null;
	}

	public static boolean isCustomItem(ItemStack is, int dataOffset) {
		if (is == null)
			return false;
		ItemStack itemstack = is.clone();
		itemstack.setDurability((short) (is.getDurability() + dataOffset));
		return isArmor(itemstack) || isGunWithAttchments(itemstack) || isAmmo(itemstack) || isMisc(itemstack)
				|| isGun(itemstack) || isIronSights(itemstack)
				|| QAMain.expansionPacks.contains(MaterialStorage.getMS(is));
	}

	public static boolean isArmor(ItemStack is) {
		if (is == null)
			return false;
		int var = MaterialStorage.getVarient(is);
		return (is != null
				&& (QAMain.armorRegister.containsKey(MaterialStorage.getMS(is.getType(), is.getDurability(), var))
						|| QAMain.armorRegister.containsKey(MaterialStorage.getMS(is.getType(), -1, var))));
	}

	public static ArmorObject getArmor(ItemStack is) {
		int var = MaterialStorage.getVarient(is);
		if (QAMain.armorRegister.containsKey(MaterialStorage.getMS(is.getType(), is.getDurability(), var)))
			return QAMain.armorRegister.get(MaterialStorage.getMS(is.getType(), is.getDurability(), var));
		return QAMain.armorRegister.get(MaterialStorage.getMS(is.getType(), -1, var));
	}

	public static boolean isMisc(ItemStack is) {
		if (is == null)
			return false;
		int var = MaterialStorage.getVarient(is);
		return (is != null
				&& (QAMain.miscRegister.containsKey(MaterialStorage.getMS(is.getType(), is.getDurability(), var))
						|| QAMain.miscRegister.containsKey(MaterialStorage.getMS(is.getType(), -1, var))));
	}

	public static ArmoryBaseObject getMisc(ItemStack is) {
		int var = MaterialStorage.getVarient(is);
		if (QAMain.miscRegister.containsKey(MaterialStorage.getMS(is.getType(), is.getDurability(), var)))
			return QAMain.miscRegister.get(MaterialStorage.getMS(is.getType(), is.getDurability(), var));
		return QAMain.miscRegister.get(MaterialStorage.getMS(is.getType(), -1, var));
	}

	public static Gun getGun(ItemStack is) {
		int var = MaterialStorage.getVarient(is);
		if (QAMain.gunRegister.containsKey(MaterialStorage.getMS(is.getType(), is.getDurability(), var)))
			return QAMain.gunRegister.get(MaterialStorage.getMS(is.getType(), is.getDurability(), var));
		return QAMain.gunRegister.get(MaterialStorage.getMS(is.getType(), -1, var));
	}

	public static boolean isGun(ItemStack is) {
		if (is == null)
			return false;
		int var = MaterialStorage.getVarient(is);
		return (is != null
				&& (QAMain.gunRegister.containsKey(MaterialStorage.getMS(is.getType(), is.getDurability(), var))
						|| QAMain.gunRegister.containsKey(MaterialStorage.getMS(is.getType(), -1, var))));
	}

	@Deprecated
	public static AttachmentBase getGunWithAttchments(ItemStack is) {
		/*
		 * int var = MaterialStorage.getVarient(is); if
		 * (QAMain.attachmentRegister.containsKey(MaterialStorage.getMS(is.getType(),
		 * is.getDurability(), var))) return
		 * QAMain.attachmentRegister.get(MaterialStorage.getMS(is.getType(),
		 * is.getDurability(), var)); return
		 * QAMain.attachmentRegister.get(MaterialStorage.getMS(is.getType(), -1, var));
		 */
		return null;
	}

	@Deprecated
	public static boolean isGunWithAttchments(
			ItemStack is) {/*
							 * if (is == null) return false; int var = MaterialStorage.getVarient(is);
							 * return (is != null &&
							 * (QAMain.attachmentRegister.containsKey(MaterialStorage.getMS(is.getType(),
							 * is.getDurability(), var)) ||
							 * QAMain.attachmentRegister.containsKey(MaterialStorage.getMS(is.getType(), -1,
							 * var))));
							 */
		return false;
	}

	public static Ammo getAmmo(ItemStack is) {
		int var = MaterialStorage.getVarient(is);
		String extraData = is.getType() == MultiVersionLookup.getSkull() ? ((SkullMeta) is.getItemMeta()).getOwner()
				: null;
		String temp = SkullHandler.getURL64(is);
		if (QAMain.ammoRegister
				.containsKey(MaterialStorage.getMS(is.getType(), is.getDurability(), var, extraData, temp)))
			return QAMain.ammoRegister
					.get(MaterialStorage.getMS(is.getType(), is.getDurability(), var, extraData, temp));
		return QAMain.ammoRegister.get(MaterialStorage.getMS(is.getType(), -1, var, extraData, temp));
	}

	public static Ammo getAmmoByName(String name) {
		for (Entry<MaterialStorage, Ammo> e : QAMain.ammoRegister.entrySet())
			if (e.getValue().getName().equalsIgnoreCase(name)) {
				return e.getValue();
			}
		return null;
	}

	@Deprecated
	public static AttachmentBase getAttachmentByName(
			String name) {/*
							 * for (Entry<MaterialStorage, AttachmentBase> e :
							 * QAMain.attachmentRegister.entrySet()) if
							 * (e.getValue().getName().equalsIgnoreCase(name)) { return e.getValue(); }
							 */
		return null;
	}

	public static boolean isAmmo(ItemStack is) {
		if (is == null)
			return false;
		int var = MaterialStorage.getVarient(is);
		String extraData = is.getType() == MultiVersionLookup.getSkull() ? ((SkullMeta) is.getItemMeta()).getOwner()
				: null;
		String temp = null;
		try {
			temp = SkullHandler.getURL64(is);
		} catch (Error | Exception e4) {
		}
		boolean k = (is != null && (QAMain.ammoRegister
				.containsKey(MaterialStorage.getMS(is.getType(), is.getDurability(), var, extraData, temp))
				|| QAMain.ammoRegister.containsKey(MaterialStorage.getMS(is.getType(), -1, var, extraData, temp))));
		return k;
	}

	public static boolean isIronSights(ItemStack is) {
		if (is == null)
			return false;
        return is != null && is.getType() == IronSightsToggleItem.getMat()
                && is.getDurability() == IronSightsToggleItem.getData();
    }

	public static ArmorObject getArmorByName(String name) {
		for (ArmorObject g : QAMain.armorRegister.values()) {
			if (g.getName().equals(name))
				return g;
		}
		return null;
	}

	public static ArmoryBaseObject getMiscByName(String name) {
		for (ArmoryBaseObject g : QAMain.miscRegister.values()) {
			if (g.getName().equals(name))
				return g;
		}
		return null;
	}

	public static Gun getGunByName(String name) {
		for (Gun g : QAMain.gunRegister.values()) {
			if (g.getName().equals(name))
				return g;
		}
		return null;
	}

	public static void sendHotbarGunAmmoCount(Player p, Gun g, ItemStack usedItem, boolean reloading) {
		sendHotbarGunAmmoCount(p, g, null, usedItem, reloading);
	}

	public static void sendHotbarGunAmmoCount(final Player p, final Gun g, AttachmentBase attachmentBase,
			ItemStack usedItem, boolean reloading) {
		int ammoamount = AmmoUtil.getAmmoAmount(p, g.getAmmoType());

		if (QAMain.showOutOfAmmoOnTitle && ammoamount <= 0 && ItemFact.getAmount(usedItem) < 1) {
			p.sendTitle(" ", QAMain.S_OUT_OF_AMMO, 0, 20, 1);
		} else if (QAMain.showReloadOnTitle && reloading) {
			// p.sendTitle(Main.S_RELOADING_MESSAGE,,0,2,1);
			for (int i = 1; i < g.getReloadTime() * 20; i += 2) {
				final int id = i;
				new BukkitRunnable() {
					@Override
					public void run() {
						StringBuilder sb = new StringBuilder();
						sb.append(ChatColor.GRAY);
						sb.append(StringUtils.repeat("#", (int) (20 * (1.0 * id / (20 * g.getReloadTime())))));
						sb.append(ChatColor.DARK_GRAY);
						sb.append(StringUtils.repeat("#", (int) (20 - ((int) (20.0 * id / (20 * g.getReloadTime()))))));
						p.sendTitle(QAMain.S_RELOADING_MESSAGE, sb.toString(), 0, 4, 0);
					}
				}.runTaskLater(QAMain.getInstance(), i);
			}
		} else {

			try {
				String message = QAMain.S_HOTBAR_FORMAT;

				if (QAMain.disableHotBarMessageOnOutOfAmmo && QAMain.disableHotBarMessageOnReload
						&& QAMain.disableHotBarMessageOnShoot)
					return;
				if (reloading && QAMain.disableHotBarMessageOnReload)
					return;
				if (ammoamount <= 0 && QAMain.disableHotBarMessageOnOutOfAmmo)
					return;
				if (!reloading && ammoamount > 0 && QAMain.disableHotBarMessageOnShoot)
					return;

				if (message.contains("%name%"))
					message = message.replace("%name%",
							(attachmentBase != null ? attachmentBase.getDisplayName() : g.getDisplayName()));
				if (message.contains("%amount%"))
					message = message.replace("%amount%", ItemFact.getAmount(usedItem) + "");
				if (message.contains("%max%"))
					message = message.replace("%max%", g.getMaxBullets() + "");

				if (message.contains("%state%"))
					message = message.replace("%state%", reloading ? QAMain.S_RELOADING_MESSAGE
							: ammoamount <= 0 ? QAMain.S_OUT_OF_AMMO : QAMain.S_MAX_FOUND);
				if (message.contains("%total%"))
					message = message.replace("%total%", "" + ammoamount);

				// (attachmentBase != null ? attachmentBase.getDisplayName() :
				// g.getDisplayName()) + " = "
				// + ItemFact.getAmount(usedItem) + "/" + (g.getMaxBullets()) + "";
				if (QAMain.unknownTranslationKeyFixer) {
					message = ChatColor.stripColor(message);
				} else {
					message = ChatColor.translateAlternateColorCodes('&', message);
				}
				HotbarMessager.sendHotBarMessage(p, message);
			} catch (Error | Exception e5) {
			}
		}
	}

	public static int findSafeSpot(ItemStack newItem, boolean findHighest, boolean allowPockets) {
		return findSafeSpot(newItem.getType(), newItem.getDurability(), findHighest, allowPockets);
	}

	public static int findSafeSpot(Material newItemtype, int startingData, boolean findHighest, boolean allowPockets) {
		int safeDurib = startingData;
		if (allowPockets) {
			List<Integer> idsToWorryAbout = new ArrayList<>();
			for (MaterialStorage j : QAMain.ammoRegister.keySet())
				if (j.getMat() == newItemtype && ((j.getData() > safeDurib) == findHighest))
					idsToWorryAbout.add(j.getData());
			for (MaterialStorage j : QAMain.gunRegister.keySet())
				if (j.getMat() == newItemtype && ((j.getData() > safeDurib) == findHighest))
					idsToWorryAbout.add(j.getData());
			for (MaterialStorage j : QAMain.miscRegister.keySet())
				if (j.getMat() == newItemtype && ((j.getData() > safeDurib) == findHighest))
					idsToWorryAbout.add(j.getData());
			for (MaterialStorage j : QAMain.armorRegister.keySet())
				if (j.getMat() == newItemtype && ((j.getData() > safeDurib) == findHighest))
					idsToWorryAbout.add(j.getData());
			if (findHighest) {
				for (int id = safeDurib+1; id < safeDurib + 1000; id++) {
					if (!idsToWorryAbout.contains(id))
						return id;
				}
			} else {
				for (int id = safeDurib-1; id > 0; id--) {
					if (!idsToWorryAbout.contains(id))
						return id;
				}
			}
			return 0;
		}

		for (MaterialStorage j : QAMain.ammoRegister.keySet())
			if (j.getMat() == newItemtype && (j.getData() > safeDurib) == findHighest)
				safeDurib = j.getData();
		for (MaterialStorage j : QAMain.gunRegister.keySet())
			if (j.getMat() == newItemtype && (j.getData() > safeDurib) == findHighest)
				safeDurib = j.getData();
		for (MaterialStorage j : QAMain.miscRegister.keySet())
			if (j.getMat() == newItemtype && (j.getData() > safeDurib) == findHighest)
				safeDurib = j.getData();
		for (MaterialStorage j : QAMain.armorRegister.keySet())
			if (j.getMat() == newItemtype && (j.getData() > safeDurib) == findHighest)
				safeDurib = j.getData();
		/*
		 * for (MaterialStorage j : QAMain.attachmentRegister.keySet()) if (j.getMat()
		 * == newItemtype && (j.getData() > safeDurib) == findHighest) safeDurib =
		 * j.getData();
		 */
		for (MaterialStorage j : QAMain.expansionPacks)
			if (j.getMat() == newItemtype && (j.getData() > safeDurib) == findHighest)
				safeDurib = j.getData();
		return safeDurib;
	}

	public static int findSafeSpotVariant(ItemStack newItem, boolean findHighest) {
		return findSafeSpotVariant(newItem.getType(), newItem.getDurability(), findHighest);
	}

	public static int findSafeSpotVariant(Material newItemtype, int startingData, boolean findHighest) {
		int safeDurib = 0;
		for (MaterialStorage j : QAMain.ammoRegister.keySet())
			if (j.getMat() == newItemtype && (j.getData() == startingData)
					&& ((j.getVarient() > safeDurib) == findHighest))
				safeDurib = j.getVarient();
		for (MaterialStorage j : QAMain.gunRegister.keySet())
			if (j.getMat() == newItemtype && (j.getData() == startingData)
					&& ((j.getVarient() > safeDurib) == findHighest))
				safeDurib = j.getVarient();
		for (MaterialStorage j : QAMain.miscRegister.keySet())
			if (j.getMat() == newItemtype && (j.getData() == startingData)
					&& ((j.getVarient() > safeDurib) == findHighest))
				safeDurib = j.getVarient();
		for (MaterialStorage j : QAMain.armorRegister.keySet())
			if (j.getMat() == newItemtype && (j.getData() == startingData)
					&& ((j.getVarient() > safeDurib) == findHighest))
				safeDurib = j.getVarient();
		/*
		 * for (MaterialStorage j : QAMain.attachmentRegister.keySet()) if (j.getMat()
		 * == newItemtype && (j.getData() == startingData) && ((j.getVarient() >
		 * safeDurib) == findHighest)) safeDurib = j.getVarient();
		 */
		for (MaterialStorage j : QAMain.expansionPacks)
			if (j.getMat() == newItemtype && (j.getData() == startingData)
					&& ((j.getVarient() > safeDurib) == findHighest))
				safeDurib = j.getVarient();
		return safeDurib;
	}

	public static int getMaxPages() {
		return (QAMain.armorRegister.size() + QAMain.ammoRegister.size() + QAMain.miscRegister.size()
				+ QAMain.gunRegister.size()) / (9 * 5);
	}

	public static boolean isOverLimitForPrimaryWeapons(Gun g, Player p) {
		int count = 0;
		for (ItemStack is : p.getInventory().getContents()) {
			if (is != null && isGun(is)) {
				Gun g2 = getGun(is);
				if (g2.isPrimaryWeapon() == g.isPrimaryWeapon())
					count++;
			}
		}
		return count >= (g.isPrimaryWeapon() ? QAMain.primaryWeaponLimit : QAMain.secondaryWeaponLimit);
	}

	public static ItemStack getCustomItem(ArmoryBaseObject customItem) {
		if (customItem instanceof Gun) {
			return getGunItemStack((Gun) customItem);
		} else if (customItem instanceof AttachmentBase) {
			return getGunItemStack((AttachmentBase) customItem);
		} else if (customItem instanceof Ammo) {
			return getAmmoItemStack((Ammo) customItem);
		} else {
			return getMiscObject(customItem);
		}
	}

	public static ItemStack getGunItemStack(String name) {
		AttachmentBase a = getAttachmentByName(name);
		if (name != null)
			return getGunItemStack(a);
		return getGunItemStack(getGunByName(name));
	}

	public static ItemStack getAmmoItemStack(String name) {
		return getAmmoItemStack(getAmmoByName(name));
	}

	public static ItemStack getGunItemStack(Gun g) {
		return ItemFact.getGun(g);
	}

	public static ItemStack getGunItemStack(AttachmentBase g) {
		return ItemFact.getGun(g);
	}

	public static ItemStack getAmmoItemStack(Ammo g) {
		return ItemFact.getAmmo(g, 1);
	}

	public static ItemStack getMiscObject(String name) {
		return ItemFact.getObject(getMiscByName(name));
	}

	public static ItemStack getMiscObject(ArmoryBaseObject obj) {
		return ItemFact.getObject(obj, 1);
	}

	public static ItemStack getIronSightsItemStack() {
		return ItemFact.getIronSights();
	}

}
