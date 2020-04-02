package me.zombie_striker.qg.guns;

import me.zombie_striker.customitemmanager.*;
import me.zombie_striker.qg.QAMain;
import me.zombie_striker.qg.ammo.*;
import me.zombie_striker.qg.api.QAWeaponPrepareShootEvent;
import me.zombie_striker.qg.api.QualityArmory;
import me.zombie_striker.qg.guns.projectiles.ProjectileManager;
import me.zombie_striker.qg.guns.projectiles.RealtimeCalculationProjectile;
import me.zombie_striker.qg.guns.utils.GunRefillerRunnable;
import me.zombie_striker.qg.guns.utils.GunUtil;
import me.zombie_striker.qg.guns.utils.WeaponSounds;
import me.zombie_striker.qg.guns.utils.WeaponType;
import me.zombie_striker.qg.handlers.Update19OffhandChecker;
import me.zombie_striker.qg.handlers.chargers.ChargingHandler;
import me.zombie_striker.qg.handlers.reloaders.ReloadingHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class Gun extends CustomBaseObject implements ArmoryBaseObject, Comparable<Gun> {

	private WeaponType type;
	private boolean hasIronSights;
	private int zoomLevel = 0;
	private Ammo ammotype;
	private double acc;
	private double swaymultiplier = 2;
	private int maxbull;
	@Deprecated
	private float damage;
	
	/** These values control the damage a gun can deal at various ranges - replaces the "damage" value entirely */
    private int rangeStart;
	private int rangeEnd;
    private float minDamage;
	private float maxDamage;
	
	private int durib = 1000;
	private boolean isAutomatic;
	boolean supports18 = false;
	boolean nightVisionOnScope = false;

	private double headshotMultiplier = 2;

	private boolean isPrimaryWeapon = true;

	private boolean useOffhandOverride = true;

	private List<String> weaponSounds;
	private double volume = 4;

	private double delayBetweenShots = 0.25;

	private int shotsPerBullet = 1;
	private int firerate = 1;

	private double reloadTime = 1.5;

	private ChargingHandler ch = null;
	private ReloadingHandler rh = null;

	private boolean enableSwayMovementModifier = true;
	private boolean enableSwaySneakModifier = true;
	private boolean enableSwayRunModifier = true;

	private int maxDistance = 150;

	private Particle particle = null;
	private double particle_r = 1;
	private double particle_g = 1;
	private double particle_b = 1;
	private Material particle_material = Material.COAL_BLOCK;

	private int lightl = 20;

	private boolean enableMuzzleSmoke = false;

	public ChatColor glowEffect = null;

	public boolean unlimitedAmmo = false;

	RealtimeCalculationProjectile customProjectile = null;
	double velocity = 2;
	double explosionRadius = 10;
	double recoil = 1;

	// This refers to the last time a gun was shot by a player, on a per-gun basis.
	// Doing this should prevent players from fast-switching to get around
	// bullet-delays
	public HashMap<UUID, Long> lastshot = new HashMap<>();

	public void copyFrom(Gun g) {
		this.setIngredientsRaw(g.getIngredientsRaw());
		this.type = g.type;
		this.hasIronSights = g.hasIronSights;
		this.zoomLevel = g.zoomLevel;
		this.ammotype = g.ammotype;
		this.acc = g.acc;
		this.swaymultiplier = g.swaymultiplier;
		this.maxbull = g.maxbull;
//		this.damage = g.damage;
		this.minDamage = g.minDamage;
		this.maxDamage = g.maxDamage;
		this.rangeStart = g.rangeStart;
		this.rangeEnd = g.rangeEnd;
		this.durib = g.durib;
		this.isAutomatic = g.isAutomatic;
		this.supports18 = g.supports18;
		this.nightVisionOnScope = g.nightVisionOnScope;
		this.headshotMultiplier = g.headshotMultiplier;
		this.isPrimaryWeapon = g.isPrimaryWeapon;
		this.explosionRadius = g.explosionRadius;
		this.setCustomLore(g.getCustomLore());
		this.weaponSounds = g.weaponSounds;
		this.setPrice(g.getPrice());
		this.delayBetweenShots = g.delayBetweenShots;
		this.shotsPerBullet = g.shotsPerBullet;
		this.firerate = g.firerate;
		this.ch = g.ch;
		this.rh = g.rh;
		this.maxDistance = g.maxbull;
		this.particle = g.particle;
		this.particle_r = g.particle_r;
		this.particle_g = g.particle_g;
		this.particle_b = g.particle_b;
		this.particle_material = g.particle_material;
		this.lightl = g.lightl;
		this.enableMuzzleSmoke = g.enableMuzzleSmoke;
		this.glowEffect = g.glowEffect;
		this.unlimitedAmmo = g.unlimitedAmmo;
		this.customProjectile = g.customProjectile;
		this.velocity = g.velocity;
		this.recoil = g.recoil;
	}

	@Deprecated
	public Gun(String name, MaterialStorage id, WeaponType type, boolean h, Ammo am, double acc, double swaymult,
			   int maxBullets, float damage, boolean isAutomatic, int durib, String ws, double cost,
			   ItemStack[] ing) {
		this(name, id, type, h, am, acc, swaymult, maxBullets, damage, isAutomatic, durib, ws,
				null, ChatColor.GOLD + name, cost, ing);
	}
	@Deprecated
	public Gun(String name, MaterialStorage id, WeaponType type, boolean h, Ammo am, double acc, double swaymult,
			int maxBullets, float damage, boolean isAutomatic, int durib, WeaponSounds ws, double cost,
			ItemStack[] ing) {
		this(name, id, type, h, am, acc, swaymult, maxBullets, damage, isAutomatic, durib, ws, null,
				ChatColor.GOLD + name, cost, ing);
	}

	@Deprecated
	public Gun(String name, MaterialStorage id, WeaponType type, boolean h, Ammo am, double acc, double swaymult,
			int maxBullets, float damage, boolean isAutomatic, int durib, WeaponSounds ws, List<String> extralore,
			String displayname, double cost, ItemStack[] ing) {
		this(name, id, type, h, am, acc, swaymult, maxBullets, damage, isAutomatic, durib, ws.getSoundName(),
				extralore, displayname, cost, ing);
	}

	@Deprecated
	public Gun(String name, MaterialStorage id, WeaponType type, boolean h, Ammo am, double acc, double swaymult,
			int maxBullets, float damage, boolean isAutomatic, int durib, String ws, List<String> extralore,
			String displayname, double cost, ItemStack[] ing) {
		super(name,id,ChatColor.translateAlternateColorCodes('&', displayname),extralore,true);
		this.type = type;
		this.hasIronSights = h;
		this.ammotype = am;
		this.setIngredients(ing);
		this.acc = acc;
		this.maxbull = maxBullets;
		this.damage = damage;
		this.durib = durib;
		this.swaymultiplier = swaymult;
		this.isAutomatic = isAutomatic;
		this.weaponSounds = new ArrayList<String>();
		this.weaponSounds.add(ws);

		this.setPrice(cost);

		//this.extralore = extralore;
		//this.displayname = ChatColor.translateAlternateColorCodes('&', displayname);
	}

	public Gun(String name, MaterialStorage id) {
		super(name,id,name,null,true);
	}


	public void setAutomatic(boolean automatic) {
		this.isAutomatic = automatic;
	}

	public void setHasIronsights(boolean b) {
		this.hasIronSights = b;
	}

	public void setDamage(float damage) {
		this.damage = damage;
	}
	
	public float getMaxDamage() {
        return maxDamage;
    }
	
	public void setMaxDamage(float maxDamage) {
        this.maxDamage = maxDamage;
    }
	
	public float getMinDamage() {
        return minDamage;
    }
	
	public void setMinDamage(float minDamage) {
        this.minDamage = minDamage;
    }
	
	public int getRangeStart() {
        return rangeStart;
    }
	
	public int getRangeEnd() {
        return rangeEnd;
    }
	
	public void setRangeStart(int rangeStart) {
        this.rangeStart = rangeStart;
    }
	
	public void setRangeEnd(int rangeEnd) {
        this.rangeEnd = rangeEnd;
    }

	public void setDuribility(int durib) {
		this.durib = durib;
	}

	public void setMaxBullets(int amount) {
		this.maxbull = amount;
	}

	public void setSway(double sway) {
		this.acc = sway;
	}

	public void setSwayMultiplier(double multiplier) {
		this.swaymultiplier = multiplier;
	}

	public void setHeadshotMultiplier(double dam) {
		headshotMultiplier = dam;
	}

	public double getHeadshotMultiplier() {
		return headshotMultiplier;
	}

	public ChatColor getGlow() {
		return glowEffect;
	}

	public void setLightOnShoot(int level) {
		lightl = level;
	}

	public int getLightOnShoot() {
		return lightl;
	}

	public double getRecoil() {
		return recoil;
	}

	public void setRecoil(double d) {
		this.recoil = d;
	}
	public void enableBetterAimingAnimations(boolean b){useOffhandOverride=b;}
	public boolean hasBetterAimingAnimations(){return useOffhandOverride;}

	public void setVolume(double f){this.volume = f;}

	public double getVolume(){return volume;}

	/**
	 * Sets the glow for the item. Null to disable the glow.
	 */
	public void setGlow(ChatColor glow) {
		this.glowEffect = glow;
	}

	public double getReloadTime() {
		return reloadTime;
	}

	public void setReloadingTimeInSeconds(double time) {
		this.reloadTime = time;
	}

	public void setBulletsPerShot(int i) {
		this.shotsPerBullet = i;
	}

	public void setNightVision(boolean nightVisionOnScope) {
		this.nightVisionOnScope = nightVisionOnScope;
	}

	public void setAmmo(Ammo ammo) {
		this.ammotype = ammo;
	}

	public boolean hasnightVision() {
		return nightVisionOnScope;
	}

	public boolean usesCustomProjctiles() {
		return customProjectile != null;
	}

	public void setCustomProjectile(String key) {
		this.customProjectile = ProjectileManager.getHandler(key);
	}

	public RealtimeCalculationProjectile getCustomProjectile() {
		return customProjectile;
	}

	public void setRealtimeVelocity(double velocity) {
		this.velocity = velocity;
	}

	public double getVelocityForRealtimeCalculations() {
		return velocity;
	}

	public double getExplosionRadius() {
		return explosionRadius;
	}

	public void setExplosionRadius(double d) {
		this.explosionRadius = d;
	}

	public int getBulletsPerShot() {
		return shotsPerBullet;
	}

	public void setZoomLevel(int zoom) {
		this.zoomLevel = zoom;
	}

	public int getZoomWhenIronSights() {
		return zoomLevel;
	}

	public void setFireRate(int firerate) {
		this.firerate = firerate;
	}

	public int getFireRate() {
		return firerate;
	}


	public WeaponType getWeaponType() {
		return type;
	}

	public boolean isPrimaryWeapon() {
		return isPrimaryWeapon;
	}

	public void setIsPrimary(boolean isPrimary) {
		this.isPrimaryWeapon = isPrimary;
	}

	public boolean isAutomatic() {
		return isAutomatic;
	}


	public void setUnlimitedAmmo(boolean b) {
		this.unlimitedAmmo = b;
	}

	public boolean shoot(Player player) {
		return Gun.USE_THIS_INSTEAD_OF_INDEVIDUAL_SHOOT_METHODS(this, player, getSway());
	}


	@SuppressWarnings("deprecation")
	public static boolean USE_THIS_INSTEAD_OF_INDEVIDUAL_SHOOT_METHODS(Gun g, Player player, double acc) {
		boolean offhand = QualityArmory.isIronSights(player.getInventory().getItemInHand());
		if ((!offhand && getAmount(player.getInventory().getItemInHand()) > 0)
				|| (offhand && Update19OffhandChecker.hasAmountOFfhandGreaterthan(player, 0))) {
			QAWeaponPrepareShootEvent shootevent = new QAWeaponPrepareShootEvent(player, g);
			Bukkit.getPluginManager().callEvent(shootevent);
			if (shootevent.isCanceled())
				return false;
			GunUtil.basicShoot(offhand, g, player, acc);
			return true;
		}
		return false;
	}

	public int getMaxBullets() {
		return maxbull;
	}

	public boolean playerHasAmmo(Player player) {
		if (player.getGameMode() == GameMode.CREATIVE)
			return true;
		if (hasUnlimitedAmmo())
			return true;
		if (getAmmoType() == null)
			return true;
		return GunUtil.hasAmmo(player, this);
	}

	public void setSound(WeaponSounds sound) {
		setSound(sound.getSoundName());
	}

	public void setSound(String sound) {
		this.weaponSounds.clear();
		this.weaponSounds.add(sound);
	}

	public void setSounds(List<String> sound) {
		this.weaponSounds = sound;
	}

	public void reload(Player player) {
		if (getChargingVal() == null || (getReloadingingVal() == null || !getReloadingingVal().isReloading(player)))
			GunUtil.basicReload(this, player, hasUnlimitedAmmo(), reloadTime);
	}

	public double getDamage() {
		return damage;
	}

	public int getDurability() {
		return this.durib;
	}

	public Ammo getAmmoType() {
		return ammotype;
	}

	public boolean hasIronSights() {
		return hasIronSights;
	}

	public boolean hasUnlimitedAmmo() {
		if (unlimitedAmmo)
			return true;
		return ammotype == null;
	}

	public double getSway() {
		// TODO Auto-generated method stub
		return acc;
	}

	public double getMovementMultiplier() {
		// TODO Auto-generated method stub
		return swaymultiplier;
	}

	@Deprecated
	public String getWeaponSound() {
		return weaponSounds.get(0);
	}

	public List<String> getWeaponSounds() {
		return weaponSounds;
	}

	public double getDelayBetweenShotsInSeconds() {
		return delayBetweenShots;
	}

	public void setDelayBetweenShots(double seconds) {
		this.delayBetweenShots = seconds;
	}

	public HashMap<UUID, Long> getLastShotForGun() {
		return lastshot;
	}

	public ChargingHandler getChargingVal() {
		return ch;
	}

	public void setChargingHandler(ChargingHandler ch) {
		this.ch = ch;
	}

	public ReloadingHandler getReloadingingVal() {
		return rh;
	}

	public void setReloadingHandler(ReloadingHandler rh) {
		this.rh = rh;
	}

	public int getMaxDistance() {
		return maxDistance;
	}

	public void setMaxDistance(int a) {
		this.maxDistance = a;
	}

	@Override
	public boolean is18Support() {
		return supports18;
	}

	@Override
	public void set18Supported(boolean b) {
		supports18 = b;
	}

	public Particle getParticle() {
		return particle;
	}

	public double getParticleR() {
		return particle_r;
	}

	public double getParticleG() {
		return particle_g;
	}

	public double getParticleB() {
		return particle_b;
	}

	public Material getParticleMaterial() {
		return this.particle_material;
	}

	public void setParticles(Particle p) {
		this.setParticles(p, 1, 1, 1, Material.COAL_BLOCK);
	}

	public void setParticles(Particle p, double r, double g, double b, Material m) {
		particle = p;
		this.particle_r = r;
		this.particle_g = g;
		this.particle_b = b;
		this.particle_material = m;
	}

	public boolean useMuzzleSmoke() {
		return enableMuzzleSmoke;
	}

	public void setUseMuzzleSmoke(boolean b) {
		this.enableMuzzleSmoke = b;
	}

	@Override
	public int compareTo(Gun arg0) {
		if (QAMain.orderShopByPrice) {
			return (int) (this.getPrice() - arg0.getPrice());
		}
		return this.getDisplayName().compareTo(arg0.getDisplayName());
	}

	@Override
	public boolean onRMB(Player e, ItemStack usedItem) {
		return onClick(e, usedItem, (QAMain.reloadOnFOnly || !QAMain.SWAP_RMB_WITH_LMB));
	}

	@Override
	public boolean onLMB(Player e, ItemStack usedItem) {
		return onClick(e, usedItem, QAMain.SWAP_RMB_WITH_LMB);
	}

	@Override
	public String getSoundOnEquip() {
		return null;
	}

	@Override
	public String getSoundOnHit() {
		return null;
	}
	
	public double calculateDamage(double shooterAndTargetDistance) {
	    // Fallback
	    if(getMaxDamage() == 0 && getMinDamage() == 0) {
	        return this.damage;
	    }
	    if(shooterAndTargetDistance <= getRangeStart()) {
	        return this.getMaxDamage();
	    }
	    if(shooterAndTargetDistance >= getRangeEnd()) {
	        return this.getMinDamage();
	    }
	    double dmgDropoffPerBlock = (getMaxDamage() - getMinDamage()) / (getRangeEnd() - getRangeStart());
	    return getMaxDamage() - dmgDropoffPerBlock * (shooterAndTargetDistance-getRangeStart());
	}

	@SuppressWarnings("deprecation")
	public boolean onClick(final Player e, ItemStack usedItem, boolean fire) {
		QAMain.DEBUG("CLICKED GUN " + getName());
		if (QAMain.requirePermsToShoot && !e.getPlayer().hasPermission("qualityarmory.usegun")) {
			e.getPlayer().sendMessage(QAMain.S_NOPERM);
			return true;
		}

		QAMain.DEBUG("Dups check");
		QAMain.checkforDups(e.getPlayer(), usedItem);

		ItemStack offhandItem = Update19OffhandChecker.getItemStackOFfhand(e.getPlayer());
		boolean offhand = offhandItem != null && offhandItem.equals(usedItem);

		QAMain.DEBUG("Made it to gun/attachment check : " + getName());
		try {
			if (QAMain.enableInteractChests) {
				Block b = e.getTargetBlock(null,6);
				if (b != null
						&& (b.getType() == Material.CHEST
						|| b.getType() == Material.TRAPPED_CHEST)
						|| b.getType() == Material.ENDER_CHEST) {
					QAMain.DEBUG("Chest interactable check has return true!");
					return true;
				}
			}
		}catch (Error|Exception e4){}

		if (fire) {
			QAMain.DEBUG("Fire mode called");
				if (e.getPlayer().getItemInHand().getItemMeta().getDisplayName().contains(QAMain.S_RELOADING_MESSAGE)) {
					if (!GunRefillerRunnable.hasItemReloaded(usedItem)) {
						ItemStack tempused = usedItem;
						ItemMeta im = tempused.getItemMeta();
						im.setDisplayName(getDisplayName());
						tempused.setItemMeta(im);
						if (offhand) {
							Update19OffhandChecker.setOffhand(e.getPlayer(), tempused);
							QAMain.DEBUG("odd. Reloading broke. Removing reloading message from offhand - firing");
						} else {
							e.getPlayer().setItemInHand(tempused);
							QAMain.DEBUG("odd. Reloading broke. Removing reloading message from mainhand - firing");
						}
					}
					QAMain.DEBUG("Reloading message 1!");
					return true;
				}
				if (!isAutomatic() && GunUtil.rapidfireshooters.containsKey(e.getPlayer().getUniqueId())) {
					GunUtil.rapidfireshooters.remove(e.getPlayer().getUniqueId()).cancel();
					if (QAMain.enableReloadWhenOutOfAmmo)
						if (getAmount(usedItem) <= 0) {
							if (offhand) {
								e.getPlayer().setItemInHand(e.getPlayer().getInventory().getItemInOffHand());
								e.getPlayer().getInventory().setItemInOffHand(null);
								usedItem = e.getPlayer().getItemInHand();
								offhand = false;
							}
							if (QAMain.allowGunReload) {
								QualityArmory.sendHotbarGunAmmoCount(e.getPlayer(), this, usedItem,	((getMaxBullets() != getAmount(usedItem))
												&& GunUtil.hasAmmo(e.getPlayer(), this)));
								if((getMaxBullets() != getAmount(usedItem))){
									QAMain.DEBUG("Ammo full");
								}else if (playerHasAmmo(e.getPlayer())) {
									QAMain.DEBUG("Trying to reload WITH AUTORELOAD. player has ammo");
									reload(e.getPlayer());
								} else {
									if (QAMain.showOutOfAmmoOnItem) {
										QAMain.DEBUG("UNSUPPORTED: Out of ammo displayed on item");
									}
									QAMain.DEBUG("Trying to reload WITH AUTORELOAD. player DOES NOT have ammo");
								}
							}
							return true;
						}
				} else {
					QAMain.DEBUG("About to fire single shot");
					if (QAMain.enableReloadWhenOutOfAmmo)
						if (getAmount(usedItem) <= 0) {
							if (offhand) {
								e.getPlayer().setItemInHand(e.getPlayer().getInventory().getItemInOffHand());
								e.getPlayer().getInventory().setItemInOffHand(null);
								usedItem = e.getPlayer().getItemInHand();
								offhand = false;
							}
							if (QAMain.allowGunReload) {
								QualityArmory.sendHotbarGunAmmoCount(e.getPlayer(), this, usedItem,
										((getMaxBullets() != getAmount(usedItem))
												&& GunUtil.hasAmmo(e.getPlayer(), this)));
								if (playerHasAmmo(e.getPlayer())) {
									QAMain.DEBUG("Trying to reload WITH AUTORELOAD. player has ammo");
									reload(e.getPlayer());

								} else {
									QAMain.DEBUG("Trying to reload WITH AUTORELOAD. player DOES NOT have ammo");
								}
							}
							return true;
						}
					shoot(e.getPlayer());
					if (QAMain.enableDurability) {
						if (QualityArmory.isIronSights(e.getPlayer().getItemInHand())) {
							//Update19OffhandChecker.setOffhand(e.getPlayer(), OLD_ItemFact.damage(this, usedItem));
						} else {
							//e.getPlayer().setItemInHand(OLD_ItemFact.damage(this, usedItem));
						}
					}

				}

				QualityArmory.sendHotbarGunAmmoCount(e.getPlayer(), this, usedItem, false);
				return true;

		} else {
			QAMain.DEBUG("Non-Fire mode activated");


			if (QAMain.enableIronSightsON_RIGHT_CLICK) {
				if (!Update19OffhandChecker.supportOffhand(e.getPlayer())) {
					QAMain.enableIronSightsON_RIGHT_CLICK = false;
					QAMain.DEBUG("Offhand checker returned false for the player. Disabling ironsights");
					return true;
				}
				// Rest should be okay
				if (hasIronSights()) {
					try {

						if (e.getPlayer().getItemInHand().getItemMeta().getDisplayName()
								.contains(QAMain.S_RELOADING_MESSAGE)) {
							if (!GunRefillerRunnable.hasItemReloaded(usedItem)) {
								ItemStack tempused = usedItem.clone();
								ItemMeta im = tempused.getItemMeta();
								im.setDisplayName(getDisplayName());
								tempused.setItemMeta(im);
								if (offhand) {
									Update19OffhandChecker.setOffhand(e.getPlayer(), tempused);
									QAMain.DEBUG(
											"odd. Reloading broke. Removing reloading message from offhand - reload");
								} else {
									e.getPlayer().setItemInHand(tempused);
									QAMain.DEBUG(
											"odd. Reloading broke. Removing reloading message from mainhand - reload");
								}
							}
							QAMain.DEBUG("Reloading message 1!");
							return true;
						}
						// ItemStack offhandItem =
						// Update19OffhandChecker.getItemStackOFfhand(e.getPlayer());
						if (offhandItem != null) {
							e.getPlayer().getInventory().addItem(offhandItem);
							Update19OffhandChecker.setOffhand(e.getPlayer(), null);
						}

						ItemStack tempremove = null;
						if (e.getPlayer().getInventory().getItemInOffHand() != null)
							tempremove = e.getPlayer().getInventory().getItemInOffHand();
						e.getPlayer().getInventory().setItemInOffHand(e.getPlayer().getInventory().getItemInMainHand());
						if (tempremove != null) {
							e.getPlayer().getInventory().setItemInMainHand(QualityArmory.getIronSightsItemStack());

							QAMain.toggleNightvision(e.getPlayer(), this, true);
							QAMain.DEBUG("Toggle Night vision on right click");
							final Gun checkTo = QualityArmory
									.getGun(Update19OffhandChecker.getItemStackOFfhand(e.getPlayer()));
							new BukkitRunnable() {

								@Override
								public void run() {
									if (!e.getPlayer().isOnline()) {
										QAMain.DEBUG("Canceling since player is offline");
										cancel();
										return;
									}
									Gun g = null;
									if (!QualityArmory.isIronSights(e.getPlayer().getItemInHand())
											|| (g = QualityArmory.getGun(
													Update19OffhandChecker.getItemStackOFfhand(e.getPlayer()))) == null
											|| g != checkTo) {
										QAMain.toggleNightvision(e.getPlayer(), checkTo, false);
										QAMain.DEBUG(
												"Removing nightvision since either the main hand is not ironsights/ offhand gun is null. : "
														+ (!QualityArmory.isIronSights(e.getPlayer().getItemInHand()))
														+ " "
														+ ((g = QualityArmory.getGun(Update19OffhandChecker
																.getItemStackOFfhand(e.getPlayer()))) == null)
														+ " " + (g != checkTo));
										cancel();
										return;
									}

								}
							}.runTaskTimer(QAMain.getInstance(), 20, 20);
						}

						QualityArmory.sendHotbarGunAmmoCount(e.getPlayer(), this, usedItem, false);
					} catch (Error e2) {
						Bukkit.broadcastMessage(QAMain.prefix
								+ "Ironsights not compatible for versions lower than 1.8. The server owner should set EnableIronSights to false in the plugin's config");
					}
				} else {
				}
				QAMain.DEBUG("Ironsights on RMB finished");
			} else {
				QAMain.DEBUG("Reload called");
				Block targetblock = e.getTargetBlock(null,5);
				if (targetblock!= null && QAMain.interactableBlocks.contains(targetblock.getType())) {
					QAMain.DEBUG("Canceled interact because block is "+targetblock.getType().name());
					return false;
				} else {
					if (QAMain.allowGunReload) {
						QualityArmory.sendHotbarGunAmmoCount(e.getPlayer(), this, usedItem,
								((getMaxBullets() != getAmount(usedItem))
										&& GunUtil.hasAmmo(e.getPlayer(), this)));
						if (playerHasAmmo(e.getPlayer())) {
							QAMain.DEBUG("Trying to reload. player has ammo");
							reload(e.getPlayer());
						} else {
							QAMain.DEBUG("Trying to reload. player DOES NOT have ammo");
						}
					}else{
						QAMain.DEBUG("Reloading has been disabled");
					}
				}
			}

		}
		QAMain.DEBUG("Reached end for gun-check!");
		return true;
	}

	@Override
	public ItemStack getItemStack() {
		return CustomItemManager.getItemFact("gun").getItem(this.getItemData(),1);
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
			}
		}
		return 0;
	}

	public static void updateAmmo(Gun g, ItemMeta current, int amount) {
		if(( (current==null ||   !current.hasLore()) ))
			return;
		List<String> lore = current.getLore();
		updateAmmo(g,lore,amount);
		current.setLore(lore);
		return;
	}
	public static void updateAmmo(Gun g, List<String> lore, int amount) {
		for(int i = 0; i < lore.size(); i++){
			if(lore.get(i).startsWith(QAMain.S_ITEM_BULLETS)){
				lore.set(i,QAMain.S_ITEM_BULLETS + ": " + (amount) + "/" + (g.getMaxBullets()));
				break;
			}
		}
		return;
	}


	public static List<String> getGunLore(Gun g, ItemStack current, int amount) {
		List<String> lore = (current!=null && current.hasItemMeta() && current.getItemMeta().hasLore()) ? current.getItemMeta().getLore() : new ArrayList<>();
		OLD_ItemFact.addVariantData(null, lore, g);
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

	private static final String CALCTEXT = ChatColor.DARK_GRAY + "qadata:";

	public static int getCalculatedExtraDurib(ItemStack is) {
		if(CustomItemManager.isUsingCustomData())
			return -1;
		if (!is.hasItemMeta() || !is.getItemMeta().hasLore() || is.getItemMeta().getLore().isEmpty())
			return -1;
		List<String> lore = is.getItemMeta().getLore();
		for (int i = 0; i < lore.size(); i++) {
			if (lore.get(i).startsWith(CALCTEXT))
				return Integer.parseInt(lore.get(i).split(CALCTEXT)[1]);
		}
		return -1;
	}

	public static ItemStack addCalulatedExtraDurib(ItemStack is, int number) {
		if(CustomItemManager.isUsingCustomData())
			return is;
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
		if(CustomItemManager.isUsingCustomData())
			return is;
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


	public boolean isEnableSwaySneakModifier() {
		return enableSwaySneakModifier;
	}

	public void setEnableSwaySneakModifier(boolean enableSwaySneakModifier) {
		this.enableSwaySneakModifier = enableSwaySneakModifier;
	}

	public boolean isEnableSwayMovementModifier() {
		return enableSwayMovementModifier;
	}

	public void setEnableSwayMovementModifier(boolean enableSwayMovementModifier) {
		this.enableSwayMovementModifier = enableSwayMovementModifier;
	}

	public boolean isEnableSwayRunModifier() {
		return enableSwayRunModifier;
	}

	public void setEnableSwayRunModifier(boolean enableSwayRunModifier) {
		this.enableSwayRunModifier = enableSwayRunModifier;
	}
}
