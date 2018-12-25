package me.zombie_striker.qg.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import me.zombie_striker.qg.QAMain;
import me.zombie_striker.qg.MaterialStorage;
import me.zombie_striker.qg.ammo.Ammo;
import me.zombie_striker.qg.ammo.AmmoType;
import me.zombie_striker.qg.armor.Helmet;
import me.zombie_striker.qg.attachments.AttachmentBase;
import me.zombie_striker.qg.guns.Gun;
import me.zombie_striker.qg.guns.utils.WeaponSounds;
import me.zombie_striker.qg.guns.utils.WeaponType;
import me.zombie_striker.qg.handlers.chargers.ChargingManager;
import me.zombie_striker.qg.handlers.reloaders.ReloadingManager;
import me.zombie_striker.qg.miscitems.Flashbang;
import me.zombie_striker.qg.miscitems.Grenades;
import me.zombie_striker.qg.miscitems.IncendaryGrenades;
import me.zombie_striker.qg.miscitems.MedKit;
import me.zombie_striker.qg.miscitems.MeleeItems;
import me.zombie_striker.qg.miscitems.SmokeGrenades;

public class GunYMLLoader {

	public static void loadAmmo(QAMain main) {

		if (new File(main.getDataFolder(), "ammo").exists())
			for (File f : new File(main.getDataFolder(), "ammo").listFiles()) {
				try {
					if (f.getName().contains("yml")) {
						FileConfiguration f2 = YamlConfiguration.loadConfiguration(f);
						if ((!f2.contains("invalid")) || !f2.getBoolean("invalid")) {
							Material m = f2.contains("material") ? Material.matchMaterial(f2.getString("material"))
									: Material.DIAMOND_AXE;
							int variant = f2.contains("variant") ? f2.getInt("variant") : 0;
							final String name = f2.getString("name");
							main.getLogger().info("-Loading AmmoType: " + name);

							String extraData = null;
							if (f2.contains("skull_owner")) {
								extraData = f2.getString("skull_owner");
							}
							String ed2 = null;
							if (f2.contains("skull_owner_custom_url")
									&& !f2.getString("skull_owner_custom_url").equals(Ammo.NO_SKIN_STRING)) {
								ed2 = f2.getString("skull_owner_custom_url");
							}

							final MaterialStorage ms = MaterialStorage.getMS(m, f2.getInt("id"), variant, extraData,
									ed2);
							final ItemStack[] materails = main
									.convertIngredients(f2.getStringList("craftingRequirements"));
							final String displayname = f2.contains("displayname")
									? ChatColor.translateAlternateColorCodes('&', f2.getString("displayname"))
									: (ChatColor.WHITE + name);
							final List<String> extraLore2 = f2.contains("lore") ? f2.getStringList("lore") : null;
							final List<String> extraLore = new ArrayList<>();
							try {
								for (String lore : extraLore2) {
									extraLore.add(ChatColor.translateAlternateColorCodes('&', lore));
								}
							} catch (Error | Exception re52) {
							}

							final double price = f2.contains("price") ? f2.getDouble("price") : 100;

							int amountA = f2.getInt("maxAmount");

							double piercing = f2.getDouble("piercingSeverity");

							Ammo da = new Ammo(name, displayname, extraLore, ms, amountA, false, 1, price, materails,
									piercing);

							QAMain.ammoRegister.put(ms, da);

							if (extraData != null) {
								da.setSkullOwner(extraData);
							}
							if (ed2 != null) {
								da.setCustomSkin(ed2);
							}
							if (f2.contains("craftingReturnAmount")) {
								da.setCraftingReturn(f2.getInt("craftingReturnAmount"));
							}

						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
	}

	public static void loadArmor(QAMain main) {
		if (new File(main.getDataFolder(), "armor").exists())
			for (File f : new File(main.getDataFolder(), "armor").listFiles()) {
				try {
					if (f.getName().contains("yml")) {
						FileConfiguration f2 = YamlConfiguration.loadConfiguration(f);
						if ((!f2.contains("invalid")) || !f2.getBoolean("invalid")) {
							final String name = f2.getString("name");
							main.getLogger().info("-Loading Armor: " + name);

							Material m = f2.contains("material") ? Material.matchMaterial(f2.getString("material"))
									: Material.DIAMOND_AXE;
							int variant = f2.contains("variant") ? f2.getInt("variant") : 0;
							final MaterialStorage ms = MaterialStorage.getMS(m, f2.getInt("id"), variant, null);
							final ItemStack[] materails = main
									.convertIngredients(f2.getStringList("craftingRequirements"));
							final String displayname = f2.contains("displayname")
									? ChatColor.translateAlternateColorCodes('&', f2.getString("displayname"))
									: (ChatColor.WHITE + name);
							final List<String> rawLore = f2.contains("lore") ? f2.getStringList("lore") : null;
							final List<String> lore = new ArrayList<>();
							try {
								for (String lore2 : rawLore) {
									lore.add(ChatColor.translateAlternateColorCodes('&', lore2));
								}
							} catch (Error | Exception re52) {
							}

							final int price = f2.contains("price") ? f2.getInt("price") : 100;

							WeaponType wt = WeaponType.getByName(f2.getString("MiscType"));

							if (wt == WeaponType.HELMET) {
								QAMain.armorRegister.put(ms, new Helmet(name, displayname, lore, materails, ms, price));
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
	}

	public static void loadMisc(QAMain main) {
		if (new File(main.getDataFolder(), "misc").exists())
			for (File f : new File(main.getDataFolder(), "misc").listFiles()) {
				try {
					if (f.getName().contains("yml")) {
						FileConfiguration f2 = YamlConfiguration.loadConfiguration(f);
						if ((!f2.contains("invalid")) || !f2.getBoolean("invalid")) {
							final String name = f2.getString("name");
							main.getLogger().info("-Loading Misc: " + name);

							Material m = f2.contains("material") ? Material.matchMaterial(f2.getString("material"))
									: Material.DIAMOND_AXE;
							int variant = f2.contains("variant") ? f2.getInt("variant") : 0;
							final MaterialStorage ms = MaterialStorage.getMS(m, f2.getInt("id"), variant, null);
							final ItemStack[] materails = main
									.convertIngredients(f2.getStringList("craftingRequirements"));
							final String displayname = f2.contains("displayname")
									? ChatColor.translateAlternateColorCodes('&', f2.getString("displayname"))
									: (ChatColor.WHITE + name);
							final List<String> rawLore = f2.contains("lore") ? f2.getStringList("lore") : null;
							final List<String> lore = new ArrayList<>();
							try {
								for (String lore2 : rawLore) {
									lore.add(ChatColor.translateAlternateColorCodes('&', lore2));
								}
							} catch (Error | Exception re52) {
							}

							final int price = f2.contains("price") ? f2.getInt("price") : 100;

							int damage = f2.contains("damage") ? f2.getInt("damage") : 1;
							// int durib = f2.contains("durability") ? f2.getInt("durability") : 1000;

							WeaponType wt = WeaponType.getByName(f2.getString("MiscType"));

							int radius = f2.contains("radius") ? f2.getInt("radius") : 0;

							if (wt == WeaponType.MEDKIT)
								QAMain.miscRegister.put(ms, new MedKit(ms, name, displayname, materails, price));
							if (wt == WeaponType.MEELEE)
								QAMain.miscRegister.put(ms,
										new MeleeItems(ms, name, displayname, materails, price, damage));
							if (wt == WeaponType.GRENADES)
								QAMain.miscRegister.put(ms,
										new Grenades(materails, price, damage, radius, name, displayname, lore, ms));
							if (wt == WeaponType.SMOKE_GRENADES)
								QAMain.miscRegister.put(ms, new SmokeGrenades(materails, price, damage, radius, name,
										displayname, lore, ms));
							if (wt == WeaponType.INCENDARY_GRENADES)
								QAMain.miscRegister.put(ms, new IncendaryGrenades(materails, price, damage, radius,
										name, displayname, lore, ms));
							if (wt == WeaponType.FLASHBANGS)
								QAMain.miscRegister.put(ms,
										new Flashbang(materails, price, damage, radius, name, displayname, lore, ms));
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
	}

	public static void loadGuns(QAMain main, File f) {
		try {
			if (f.getName().contains("yml")) {
				FileConfiguration f2 = YamlConfiguration.loadConfiguration(f);
				if ((!f2.contains("invalid")) || !f2.getBoolean("invalid")) {
					final String name = f2.getString("name");
					main.getLogger().info("-Loading Gun: " + name);

					Material m = f2.contains("material") ? Material.matchMaterial(f2.getString("material"))
							: Material.DIAMOND_AXE;
					int variant = f2.contains("variant") ? f2.getInt("variant") : 0;
					final MaterialStorage ms = MaterialStorage.getMS(m, f2.getInt("id"), variant, null);
					WeaponType weatype = f2.contains("guntype") ? WeaponType.valueOf(f2.getString("guntype"))
							: WeaponType.valueOf(f2.getString("weapontype"));
					final ItemStack[] materails = main.convertIngredients(f2.getStringList("craftingRequirements"));

					final String displayname = f2.contains("displayname")
							? ChatColor.translateAlternateColorCodes('&', f2.getString("displayname"))
							: (ChatColor.GOLD + name);
					final List<String> extraLore2 = f2.contains("lore") ? f2.getStringList("lore") : null;
					final List<String> extraLore = new ArrayList<>();

					try {
						for (String lore : extraLore2) {
							extraLore.add(ChatColor.translateAlternateColorCodes('&', lore));
						}
					} catch (Error | Exception re52) {
					}

					// final double price = f2.contains("price") ? f2.getDouble("price") : 100;

					if (weatype.isGun()) {
						// boolean isAutomatic = f2.contains("isAutomatic") ?
						// f2.getBoolean("isAutomatic") : false;

						Gun g = new Gun(name, ms/*
												 * , weatype, f2.getBoolean("enableIronSights"),
												 * AmmoType.getAmmo(f2.getString("ammotype")), f2.getDouble("sway"),
												 * f2.contains("swayMultiplier") ? f2.getDouble("swayMultiplier") : 2,
												 * f2.getInt("maxbullets"), f2.getInt("damage"), isAutomatic,
												 * f2.getInt("durability"), WeaponSounds.GUN_MEDIUM, extraLore,
												 * displayname, price, materails
												 */);
						g.setDisplayName(displayname);
						g.setIngredients(materails);
						QAMain.gunRegister.put(ms, g);
						loadGunSettings(g, f2);
					}

				}
			}
		} catch (Exception e) {
		}

	}

	private static void loadGunSettings(Gun g, FileConfiguration f2) {

		String sound = WeaponSounds.GUN_MEDIUM.getSoundName();
		if (f2.contains("ammotype"))
			g.setAmmo(AmmoType.getAmmo(f2.getString("ammotype")));

		if (f2.contains("sway"))
			g.setSway(f2.getDouble("sway"));
		if (f2.contains("swayMultiplier"))
			g.setSwayMultiplier(f2.getDouble("swayMultiplier"));
		if (f2.contains("enableIronSights"))
			g.setHasIronsights(f2.getBoolean("enableIronSights"));
		if (f2.contains("maxbullets"))
			g.setMaxBullets(f2.getInt("maxbullets"));
		if (f2.contains("damage"))
			g.setDamage(f2.getInt("damage"));
		if (f2.contains("durability"))
			g.setDuribility(f2.getInt("durability"));
		if (f2.contains("price"))
			g.setPrice(f2.getDouble("price"));
		if (f2.contains("isAutomatic"))
			g.setAutomatic(f2.getBoolean("isAutomatic"));

		if (f2.contains("weaponsounds")) {
			sound = f2.getString("weaponsounds");
		} else {

			if (g.getWeaponType() == WeaponType.PISTOL || g.getWeaponType() == WeaponType.SMG)
				sound = WeaponSounds.GUN_SMALL.getSoundName();
			if (g.getWeaponType() == WeaponType.SHOTGUN || g.getWeaponType() == WeaponType.SNIPER)
				sound = WeaponSounds.GUN_BIG.getSoundName();
			if (g.getWeaponType() == WeaponType.RPG)
				sound = WeaponSounds.WARHEAD_LAUNCH.getSoundName();
			if (g.getWeaponType() == WeaponType.LAZER)
				sound = WeaponSounds.LAZERSHOOT.getSoundName();
		}
		g.setSound(sound);

		double partr = f2.contains("particles.bullet_particleR") ? f2.getDouble("particles.bullet_particleR") : 1.0;
		double partg = f2.contains("particles.bullet_particleG") ? f2.getDouble("particles.bullet_particleG") : 1.0;
		double partb = f2.contains("particles.bullet_particleB") ? f2.getDouble("particles.bullet_particleB") : 1.0;

		if (f2.contains("addMuzzleSmoke")) {
			boolean addMuzzleSmoke = f2.contains("addMuzzleSmoke") ? f2.getBoolean("addMuzzleSmoke") : false;
			g.setUseMuzzleSmoke(addMuzzleSmoke);
		}
		if (f2.contains("delayForReload"))
			g.setReloadingTimeInSeconds(f2.getDouble("delayForReload"));

		if (f2.contains("drop-glow-color") && !f2.getString("drop-glow-color").equals("none")) {
			ChatColor c = ChatColor.WHITE;
			for (ChatColor cc : ChatColor.values())
				if (cc.name().equals(f2.getString("drop-glow-color"))) {
					c = cc;
					break;
				}
			g.setGlow(c);
		}

		if (f2.contains("CustomProjectiles.projectileType")) {
			g.setCustomProjectile(f2.getString("CustomProjectiles.projectileType"));
			if (f2.contains("CustomProjectiles.explosionRadius"))
				g.setExplosionRadius(f2.getDouble("CustomProjectiles.explosionRadius"));
			if (f2.contains("CustomProjectiles.Velocity"))
				g.setRealtimeVelocity(f2.getDouble("CustomProjectiles.Velocity"));
		}

		if (f2.contains("recoil"))
			g.setRecoil(f2.getDouble("recoil"));
		if (f2.contains("headshotMultiplier"))
			g.setHeadshotMultiplier(f2.getDouble("headshotMultiplier"));
		if (f2.contains("unlimitedAmmo"))
			g.setUnlimitedAmmo(f2.getBoolean("unlimitedAmmo"));
		if (f2.contains("LightLeveOnShoot"))
			g.setLightOnShoot(f2.getInt("LightLeveOnShoot"));
		if (f2.contains("firerate"))
			g.setFireRate(f2.getInt("firerate"));
		if (f2.contains("ReloadingHandler"))
			g.setReloadingHandler(ReloadingManager.getHandler(f2.getString("ReloadingHandler")));
		if (f2.contains("ChargingHandler") && !f2.getString("ChargingHandler").equals("none"))
			g.setChargingHandler(ChargingManager.getHandler(f2.getString("ChargingHandler")));
		if (f2.contains("delayForShoot"))
			g.setDelayBetweenShots(f2.getDouble("delayForShoot"));
		if (f2.contains("bullets-per-shot"))
			g.setBulletsPerShot(f2.getInt("bullets-per-shot"));
		if (f2.contains("maxBulletDistance"))
			g.setMaxDistance(f2.getInt("maxBulletDistance"));
		if (f2.contains("Version_18_Support"))
			g.set18Supported(f2.getBoolean("Version_18_Support"));
		if (f2.contains("hasNightVisionOnScope"))
			g.setNightVision(f2.getBoolean("hasNightVisionOnScope"));
		if (f2.contains("isPrimaryWeapon"))
			g.setIsPrimary(f2.getBoolean("isPrimaryWeapon"));
		if (f2.contains("setZoomLevel"))
			g.setZoomLevel(f2.getInt("setZoomLevel"));

		if (f2.contains("particles.bullet_particle") || f2.contains("particles.bullet_particleR")) {
			try {
				Particle particle = (Particle) (f2.contains("particles.bullet_particle")
						? Particle.valueOf(f2.getString("particles.bullet_particle"))
						: QAMain.bulletTrail);
				g.setParticles(particle, partr, partg, partb);
			} catch (Error | Exception er5) {
			}
		}
	}

	public static void loadGuns(QAMain main) {
		if(new File(main.getDataFolder(), "newGuns").exists())
		for (File f : new File(main.getDataFolder(), "newGuns").listFiles()) {
			FileConfiguration f2 = YamlConfiguration.loadConfiguration(f);
			if (CrackshotLoader.isCrackshotGun(f2)) {
				main.getLogger().info("-Converting Crackshot: " + f.getName());
				List<Gun> guns = CrackshotLoader.loadCrackshotGuns(f2);
				CrackshotLoader.createYMLForGuns(guns, main.getDataFolder());
				continue;
			}
			loadGuns(main, f);
		}
	}

	public static void loadAttachments(QAMain main) {
		if (new File(main.getDataFolder(), "attachments").exists())
		for (File f : new File(main.getDataFolder(), "attachments").listFiles()) {
			try {
				if (f.getName().contains("yml")) {
					FileConfiguration f2 = YamlConfiguration.loadConfiguration(f);
					if ((!f2.contains("invalid")) || !f2.getBoolean("invalid")) {
						final String name = f2.getString("name");
						main.getLogger().info("-Loading Attachment: " + name);
						final String displayname = f2.contains("displayname")
								? ChatColor.translateAlternateColorCodes('&', f2.getString("displayname"))
								: (ChatColor.GOLD + name);
						final List<String> extraLore2 = f2.contains("lore") ? f2.getStringList("lore") : null;

						Material m = f2.contains("material") ? Material.matchMaterial(f2.getString("material"))
								: Material.DIAMOND_AXE;
						int variant = f2.contains("variant") ? f2.getInt("variant") : 0;
						final MaterialStorage ms = MaterialStorage.getMS(m, f2.getInt("id"), variant, null);

						// Gun baseGun = null;
						MaterialStorage baseGunM = null;
						for (Entry<MaterialStorage, Gun> g : QAMain.gunRegister.entrySet()) {
							if (g.getValue().getName().equalsIgnoreCase(f2.getString("baseGun"))) {
								// baseGun = g.getValue();
								baseGunM = g.getKey();
							}
						}

						final List<String> extraLore = new ArrayList<>();
						try {
							for (String lore : extraLore2) {
								extraLore.add(ChatColor.translateAlternateColorCodes('&', lore));
							}
						} catch (Error | Exception re52) {
						}

						AttachmentBase attach = new AttachmentBase(baseGunM, ms, name, displayname);
						QAMain.gunRegister.put(ms, attach);

						final ItemStack[] materails = main.convertIngredients(f2.getStringList("craftingRequirements"));
						attach.setIngredients(materails);

						// QAMain.attachmentRegister.put(ms, attach);
						loadGunSettings(attach, f2);
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
