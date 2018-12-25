package me.zombie_striker.qg;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

import me.zombie_striker.qg.ammo.*;
import me.zombie_striker.qg.api.QualityArmory;
import me.zombie_striker.qg.armor.*;
import me.zombie_striker.qg.config.*;
import me.zombie_striker.qg.guns.*;
import me.zombie_striker.qg.guns.projectiles.*;
import me.zombie_striker.qg.guns.projectiles.HomingRocketProjectile;
import me.zombie_striker.qg.guns.projectiles.MiniNukeProjectile;
import me.zombie_striker.qg.guns.projectiles.RocketProjectile;
import me.zombie_striker.qg.guns.utils.*;
import me.zombie_striker.qg.handlers.*;
import me.zombie_striker.qg.handlers.chargers.*;
import me.zombie_striker.qg.handlers.reloaders.*;
import me.zombie_striker.qg.miscitems.*;
import me.zombie_striker.qg.miscitems.ThrowableItems.ThrowableHolder;
import me.zombie_striker.qg.npcs.Gunner;
import me.zombie_striker.qg.npcs.GunnerTrait;
import me.zombie_striker.qg.npcs_sentinel.SentinelQAHandler;

import me.zombie_striker.qg.utils.GithubDependDownloader;
import me.zombie_striker.qg.utils.GithubUpdater;
import me.zombie_striker.qg.utils.Metrics;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.*;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.google.common.base.Charsets;

public class QAMain extends JavaPlugin {

	public static HashMap<MaterialStorage, Gun> gunRegister = new HashMap<>();
	public static HashMap<MaterialStorage, Ammo> ammoRegister = new HashMap<>();
	public static HashMap<MaterialStorage, ArmoryBaseObject> miscRegister = new HashMap<>();
	public static HashMap<MaterialStorage, ArmorObject> armorRegister = new HashMap<>();
	// public static HashMap<MaterialStorage, AttachmentBase> attachmentRegister =
	// new HashMap<>();

	public static HashMap<UUID, Location> recoilHelperMovedLocation = new HashMap<>();

	public static ArrayList<MaterialStorage> expansionPacks = new ArrayList<>();

	public static HashMap<UUID, List<BukkitTask>> reloadingTasks = new HashMap<>();

	public static HashMap<UUID, Long> sentResourcepack = new HashMap<>();

	public static ArrayList<UUID> resourcepackReq = new ArrayList<>();

	public static List<Gunner> gunners = new ArrayList<>();

	public static List<String> namesToBypass = new ArrayList<>();

	private static QAMain main;

	public static List<Material> interactableBlocks = new ArrayList<>();
	public static List<Material> destructableBlocks = new ArrayList<>();
	public static boolean enableInteractChests = false;

	private TreeFellerHandler tfh = null;

	public static QAMain getInstance() {
		return main;
	}

	public static boolean DEBUG = false;

	public static Object bulletTrail;

	public static boolean shouldSend = true;
	public static boolean sendOnJoin = false;
	public static boolean sendTitleOnJoin = false;
	public static double secondsTilSend = 0.0;

	public static boolean orderShopByPrice = false;

	public static boolean ignoreUnbreaking = false;
	public static boolean ignoreSkipping = false;

	public static boolean enableDurability = false;

	public static boolean enableArmorIgnore = false;

	public static boolean enableRecoil = true;

	public static double bulletStep = 0.10;
	public static double gravity = 0.05;

	public static double swayModifier_Ironsights = 0.8;
	public static double swayModifier_Sneak = 0.7;
	public static double swayModifier_Walk = 1.5;
	public static double swayModifier_Run = 2;

	public static boolean blockbullet_leaves = false;
	public static boolean blockbullet_halfslabs = false;
	public static boolean blockbullet_door = false;
	public static boolean blockbullet_water = false;
	public static boolean blockbullet_glass = false;

	public static boolean overrideAnvil = false;
	public static boolean supportWorldGuard = false;
	public static boolean enableIronSightsON_RIGHT_CLICK = false;
	public static boolean enableBulletTrails = true;
	public static boolean enableVisibleAmounts = false;
	public static boolean reloadOnF = true;
	public static boolean reloadOnFOnly = true;

	public static boolean disableHotBarMessageOnShoot = false;
	public static boolean disableHotBarMessageOnReload = false;
	public static boolean disableHotBarMessageOnOutOfAmmo = false;

	public static boolean enableExplosionDamage = false;
	public static boolean enableExplosionDamageDrop = false;

	public static boolean hideTextureWarnings = false;

	public static boolean enableEconomy = false;

	public static boolean allowGunReload = true;

	public static boolean ignoreArmorStands = false;

	public static boolean enableBleeding = false;
	public static double bulletWound_initialbloodamount = 1500;
	public static double bulletWound_BloodIncreasePerSecond = 0.01;
	public static double bulletWound_MedkitBloodlossHealRate = 0.05;

	public static String headshot_sound = WeaponSounds.XP_ORG_PICKUP.getSoundName();
	public static boolean HeadshotOneHit = true;
	public static boolean headshotPling = true;
	public static boolean headshotGoreSounds = true;

	public static boolean showOutOfAmmoOnItem = false;
	public static boolean showOutOfAmmoOnTitle = false;
	public static boolean showReloadOnTitle = false;

	public static boolean addGlowEffects = false;

	public static boolean enableReloadWhenOutOfAmmo = false;

	public static boolean overrideURL = false;
	public static boolean kickIfDeniedRequest = false;
	// public static String url19plus =
	// "https://www.dropbox.com/s/faufrgo7w2zpi3d/QualityArmoryv1.0.10.zip?dl=1";
	public static String url_newest = "https://www.dropbox.com/s/3mkoulo9tzdte8w/QualityArmoryv1.0.37.zip?dl=1";
	public static String url18 = "https://www.dropbox.com/s/odvle92e0fz0ezr/QualityArmory1.8v1.0.2.zip?dl=1";
	public static String url18New = "https://www.dropbox.com/s/ipza6fod5b2jub6/QualityArmory1.9PLUS%20v1.0.2.zip?dl=1";
	public static String url = url_newest;

	public static String S_NOPERM = "&c You do not have permission to do that.";
	public static String S_NORES1 = " &c&l Downloading Resourcepack...";
	public static String S_NORES2 = " &f Accept the resourcepack to see the custom items";
	public static String S_ANVIL = " &a You do not have permission to use this armory bench. Shift+Click to access anvil.";
	public static String S_ITEM_BULLETS = "&aBullets";
	public static String S_ITEM_DURIB = "Durability";
	public static String S_ITEM_DAMAGE = "&aDamage";
	public static String S_ITEM_AMMO = "&aAmmo";
	public static String S_ITEM_ING = "Ingredients";
	public static String S_ITEM_VARIENTS_LEGACY = "&7Varient:";
	public static String S_ITEM_VARIENTS_NEW = "&7Varient:";
	public static String S_ITEM_DPS = "&2DPS";
	public static String S_ITEM_COST = "&" + ChatColor.GOLD.getChar() + "Price: ";

	public static String S_KICKED_FOR_RESOURCEPACK = "&c You have been kicked because you did not accept the resourcepack. \n&f If you want to rejoin the server, edit the server entry and set \"Resourcepack Prompts\" to \"Accept\" or \"Prompt\"'";

	public static String S_LMB_SINGLE = ChatColor.DARK_GRAY + "[LMB] to use Single-fire mode";
	public static String S_LMB_FULLAUTO = ChatColor.DARK_GRAY + "[Sneak]+[LMB] to use Automatic-fire";
	public static String S_RMB_RELOAD = ChatColor.DARK_GRAY + "[RMB] to reload";
	public static String S_RMB_R1 = ChatColor.DARK_GRAY + "[DropItem] to reload";
	public static String S_RMB_R2 = ChatColor.DARK_GRAY + "[RMB] to reload";

	public static String S_RMB_A1 = ChatColor.DARK_GRAY + "[RMB] to open ironsights";
	public static String S_RMB_A2 = ChatColor.DARK_GRAY + "[Sneak] to open ironsights";

	public static String S_FULLYHEALED = "&fYou are fully healed. No need for this right now!";
	public static String S_MEDKIT_HEALING = "Healing";
	public static String S_MEDKIT_BLEEDING = "Blood-Loss Rate:";
	public static double S_MEDKIT_HEALDELAY = 6;
	public static double S_MEDKIT_HEAL_AMOUNT = 1;
	public static String S_MEDKIT_LORE_INFO = "&f[RMB] to heal yourself!";

	public static String S_BULLETPROOFSTOPPEDBLEEDING = "&aYour Kevlar vest protected you from that bullet!";
	public static String S_BLEEDOUT_STARTBLEEDING = "&cYOU ARE BLEEDING! FIND A MEDKIT TO STOP THE BLEEDING!";
	public static String S_BLEEDOUT_LOSINGconscious = "&cYOU ARE LOSING CONSCIOUSNESS DUE TO BLOODLOSS!";

	public static String S_BUYCONFIRM = "&aYour have purchased %item% for $%cost%!";

	public static String S_OUT_OF_AMMO = "[Out of Ammo]";
	public static String S_RELOADING_MESSAGE = "[Reloading...]";
	public static String S_MAX_FOUND = "[%total%]";
	public static String S_HOTBAR_FORMAT = "%name% = %amount%/%max% %state%";

	public static String S_RESOURCEPACK_HELP = "In case the resourcepack crashes your client, reject the request and use /qa getResourcepack to get the resourcepack.";
	public static String S_RESOURCEPACK_DOWNLOAD = "Download this resourcepack and enable it to see the custom items (Note that it may take some time to load)";
	public static String S_RESOURCEPACK_BYPASS = "By issuing this command, you are now added to a whitelist for the resourcepack. You should no longer see the prompt";
	public static String S_RESOURCEPACK_OPTIN = "By issuing this command, you have been removed from the whitelist. You will now recieve the resourcepack prompt";

	public static String S_GRENADE_PULLPIN = " Pull the pin first...";
	public static String S_GRENADE_PALREADYPULLPIN = "You already pulled the pin!";

	public static Material usedIronSightsMaterial = Material.DIAMOND_AXE;
	public static int usedIronSightsData = 21;

	public static boolean enableCrafting = true;
	public static boolean enableShop = true;

	public static double smokeSpacing = 0.5;

	public static boolean enablePrimaryWeaponHandler = false;
	public static int primaryWeaponLimit = 2;
	public static int secondaryWeaponLimit = 2;

	public static String prefix = ChatColor.GRAY + "[" + ChatColor.DARK_GREEN + "QualityArmory" + ChatColor.GRAY + "]"
			+ ChatColor.WHITE;

	// public Inventory craftingMenu;
	public static String S_craftingBenchName = "Armory Crafting-Bench Page:";
	public static String S_missingIngredients = "You do not have all the materials needed to craft this.";

	// public Inventory shopMenu;
	public static String S_shopName = "Weapons Shop Page:";
	public static String S_noMoney = "You do not have enough money to buy this.";
	public static String S_noEcon = "ECONOMY NOT ENABLED. REPORT THIS TO THE OWNER!";

	public static ItemStack prevButton;
	public static ItemStack nextButton;

	public static MessagesYML m;
	public static MessagesYML resourcepackwhitelist;

	public static boolean hasParties = false;
	public static boolean friendlyFire = false;

	public static boolean hasProtocolLib = false;

	private static final String SERVER_VERSION;

	public static boolean AUTOUPDATE = true;

	public static boolean SWAP_RMB_WITH_LMB = true;

	public static boolean ENABLE_LORE_INFO = true;
	public static boolean ENABLE_LORE_HELP = true;

	public static boolean AutoDetectResourcepackVersion = true;
	public static final int ID18 = 106;

	public static boolean ITEM_enableUnbreakable = false;// TODO :stuufff
	public static boolean MANUALLYSELECT18 = false;

	private FileConfiguration config;
	private File configFile;

	public static boolean unknownTranslationKeyFixer = false;

	public static boolean enableCreationOfFiles = true;

	public static List<Scoreboard> coloredGunScoreboard = new ArrayList<>();
	public static boolean blockBreakTexture = false;
	public static List<UUID> currentlyScoping = new ArrayList<>();

	static {
		String name = Bukkit.getServer().getClass().getName();
		name = name.substring(name.indexOf("craftbukkit.") + "craftbukkit.".length());
		name = name.substring(0, name.indexOf("."));
		SERVER_VERSION = name;
	}

	public static boolean isVersionHigherThan(int mainVersion, int secondVersion) {
		if (secondVersion >= 9 && Bukkit.getPluginManager().isPluginEnabled("ViaRewind"))
			return false;
		String firstChar = SERVER_VERSION.substring(1, 2);
		int fInt = Integer.parseInt(firstChar);
		if (fInt < mainVersion)
			return false;
		StringBuilder secondChar = new StringBuilder();
		for (int i = 3; i < 10; i++) {
			if (SERVER_VERSION.charAt(i) == '_' || SERVER_VERSION.charAt(i) == '.')
				break;
			secondChar.append(SERVER_VERSION.charAt(i));
		}

		int sInt = Integer.parseInt(secondChar.toString());
        return sInt >= secondVersion;
    }

	/**
	 * GUNLIST:
	 *
	 * 2: P30 3 PKP 4 MP5K 5 AK47 6: AK 7 M16 8 Remmington 9 FNFal 10 RPG 11 UMP 12
	 * SW1911 13 M40 14 Ammo 556 15 9mm 16 buckshot 17 rocketRPG 18 Enfield 19 Henry
	 * 20 MouserC96
	 *
	 * 22 Grenades
	 */

	@Override
	public void onDisable() {
		for (Entry<MaterialStorage, ArmoryBaseObject> misc : miscRegister.entrySet()) {
			if (misc instanceof ThrowableItems) {
				for (Entry<Entity, ThrowableHolder> e : ThrowableItems.throwItems.entrySet()) {
					if (e.getKey() instanceof Item)
						e.getKey().remove();
				}
			}
		}

		for (Scoreboard s : coloredGunScoreboard)
			for (Team t : s.getTeams())
				t.unregister();

		for (Gunner g : gunners) {
			g.dispose();
		}
	}

	private boolean saveTheConfig = false;

	public Object a(String path, Object def) {
		if (getConfig().contains(path)) {
			return getConfig().get(path);
		}
		getConfig().set(path, def);
		saveConfig();
		return def;
	}

	@Override
	public void onEnable() {
		main = this;
		supportWorldGuard = Bukkit.getPluginManager().isPluginEnabled("WorldGuard");
		if (Bukkit.getPluginManager().getPlugin("PluginConstructorAPI") == null)
			// new DependencyDownloader(this, 276723);
			GithubDependDownloader.autoUpdate(this,
					new File(getDataFolder().getParentFile(), "PluginConstructorAPI.jar"), "ZombieStriker",
					"PluginConstructorAPI", "PluginConstructorAPI.jar");
		try {
			ParticleHandlers.initValues();
		} catch (Error | Exception e5) {
		}
		DEBUG(ChatColor.RED + "NOTICE ME");
		reloadVals();
		new BukkitRunnable() {
			@Override
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers())
					resourcepackReq.add(player.getUniqueId());
			}
		}.runTaskLater(this, 1);

		// check if Citizens is present and enabled.

		if (getServer().getPluginManager().getPlugin("Citizens") == null
				|| getServer().getPluginManager().getPlugin("Citizens").isEnabled() == false) {
			getLogger().log(Level.SEVERE, "Citizens 2.0 not found or not enabled");
			// getServer().getPluginManager().disablePlugin(this);
		} else {
			// Register your trait with Citizens.
			net.citizensnpcs.api.CitizensAPI.getTraitFactory()
					.registerTrait(net.citizensnpcs.api.trait.TraitInfo.create(GunnerTrait.class));
		}

		new QAEvents(this);
		Bukkit.getPluginManager().registerEvents(new AimManager(), this);

		try {
			Bukkit.getPluginManager().registerEvents(new Update19resourcepackhandler(), this);
		} catch (Exception | Error e) {
			getLogger().info(prefix + " Resourcepack handler has been disabled due to the update being used.");
		}
		try {
			Bukkit.getPluginManager().registerEvents(new Update19Events(), this);
		} catch (Exception | Error e) {
		}

		try {
			/* final Updater updater = */// new Updater(this, 278412, true);
			/*
			 * new BukkitRunnable() { public void run() { // TODO: Works well. Make changes
			 * for the updaters of // PixelPrinter and Music later. if
			 * (updater.updaterActive) updater.download(false); }
			 * }.runTaskTimerAsynchronously(this, 20 /* * 60 *, 20 * 60 * 5);
			 */
			if (AUTOUPDATE)
				GithubUpdater.autoUpdate(this, "ZombieStriker", "QualityArmory", "QualityArmory.jar");
		} catch (Exception e) {
		}

		setupMetrics();

		new BukkitRunnable() {
			@SuppressWarnings("deprecation")
			public void run() {
				try {
					// Cheaty, hacky fix
					for (Player p : Bukkit.getOnlinePlayers()) {
						// if (p.getItemInHand().containsEnchantment(Enchantment.MENDING)) {
						if (p.getItemInHand() != null && p.getItemInHand().hasItemMeta())
							if (QualityArmory.isCustomItem(p.getItemInHand())) {
								if (ITEM_enableUnbreakable && (!p.getItemInHand().getItemMeta().spigot().isUnbreakable()
										&& !ignoreUnbreaking)) {
									ItemStack temp = p.getItemInHand();
									int j = QualityArmory.findSafeSpot(temp, false, overrideURL);
									temp.setDurability((short) Math.max(0, j - 1));
									temp = ItemFact.removeCalculatedExtra(temp);
									p.setItemInHand(temp);
								}
							}
						try {

							// if
							// (p.getInventory().getItemInOffHand().containsEnchantment(Enchantment.MENDING))
							// {
							if (p.getInventory().getItemInOffHand() != null
									&& p.getInventory().getItemInOffHand().hasItemMeta())
								if (QualityArmory.isCustomItem(p.getInventory().getItemInOffHand())) {
									if (ITEM_enableUnbreakable && (!p.getInventory().getItemInOffHand().getItemMeta()
											.spigot().isUnbreakable() && !ignoreUnbreaking)) {
										ItemStack temp = p.getInventory().getItemInOffHand();
										int j = QualityArmory.findSafeSpot(temp, false, overrideURL);
										temp.setDurability((short) Math.max(0, j - 1));
										temp = ItemFact.removeCalculatedExtra(temp);
										p.getInventory().setItemInOffHand(temp);
										return;
									}
								}
						} catch (Error | Exception e45) {
						}
					}
				} catch (Error | Exception catchy) {

				}
			}
		}.runTaskTimer(this, 20, 4);
	}

	private void setupMetrics() {
        Metrics metrics = new Metrics(this);

        // Optional: Add custom charts
        metrics.addCustomChart(new Metrics.SimplePie("GunCount", new java.util.concurrent.Callable<String>() {
            @Override
            public String call() throws Exception {
                return gunRegister.size() + "";
            }
        }));
        metrics.addCustomChart(
                new Metrics.SimplePie("uses_default_resourcepack", new java.util.concurrent.Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return overrideURL + "";
                    }
                }));
        metrics.addCustomChart(
                new Metrics.SimplePie("has_an_expansion_pack", new java.util.concurrent.Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return (expansionPacks.size() > 0) + "";
                    }
                }));
    }

	public static void toggleNightvision(Player player, Gun g, boolean add) {
		if (add) {
			if (g.getZoomWhenIronSights() > 0) {
				currentlyScoping.add(player.getUniqueId());
				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1200, g.getZoomWhenIronSights()));
			}
			if (g.hasnightVision()) {
				currentlyScoping.add(player.getUniqueId());
				player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 1200, 3));
			}
		} else {
			if (currentlyScoping.contains(player.getUniqueId())) {
				if (player.hasPotionEffect(PotionEffectType.SLOW) && (g == null || g.getZoomWhenIronSights() > 0))
					player.removePotionEffect(PotionEffectType.SLOW);
				if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION) && (g == null || g.hasnightVision())
						&& player.getPotionEffect(PotionEffectType.NIGHT_VISION).getAmplifier() == 3)
					player.removePotionEffect(PotionEffectType.NIGHT_VISION);
				currentlyScoping.remove(player.getUniqueId());
			}
		}

	}

	public static void DEBUG(String message) {
		if (DEBUG)
			Bukkit.broadcast(message, "qualityarmory.debugmessages");
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	public void reloadVals() {

		Material glass = null;
		try {
			glass = Material.matchMaterial("STAINED_GLASS_PANE");
		} catch (Error | Exception e45) {
		}
		if (glass == null)
			glass = Material.matchMaterial("BLUE_STAINED_GLASS_PANE");

		prevButton = new ItemStack(glass, 1, (short) 14);
		nextButton = new ItemStack(glass, 1, (short) 5);

		new BoltactionCharger();
		new BreakactionCharger();
		new PumpactionCharger();
		new RevolverCharger();
		new BurstFireCharger();
		new PushbackCharger();
		new RequireAimCharger();

		new PumpactionReloader();
		new SingleBulletReloader();

		new MiniNukeProjectile();
		new ExplodingRoundProjectile();
		new HomingRocketProjectile();
		new RocketProjectile();
		new FireProjectile();

		gunRegister.clear();
		ammoRegister.clear();
		miscRegister.clear();
		armorRegister.clear();
		interactableBlocks.clear();

		// attachmentRegister.clear();

		m = new MessagesYML(new File(getDataFolder(), "messages.yml"));
		S_ANVIL = ChatColor.translateAlternateColorCodes('&', (String) m.a("NoPermAnvilMessage", S_ANVIL));
		S_NOPERM = ChatColor.translateAlternateColorCodes('&', (String) m.a("NoPerm", S_NOPERM));
		S_shopName = (String) m.a("ShopName", S_shopName);
		S_craftingBenchName = (String) m.a("CraftingBenchName", S_craftingBenchName);
		S_missingIngredients = (String) m.a("Missing_Ingredients", S_missingIngredients);
		S_NORES1 = ChatColor.translateAlternateColorCodes('&', (String) m.a("NoResourcepackMessage1", S_NORES1));
		S_NORES2 = ChatColor.translateAlternateColorCodes('&', (String) m.a("NoResourcepackMessage2", S_NORES2));
		S_ITEM_AMMO = ChatColor.translateAlternateColorCodes('&', (String) m.a("Lore_Ammo", S_ITEM_AMMO));
		S_ITEM_BULLETS = ChatColor.translateAlternateColorCodes('&', (String) m.a("lore_bullets", S_ITEM_BULLETS));
		S_ITEM_DAMAGE = ChatColor.translateAlternateColorCodes('&', (String) m.a("Lore_Damage", S_ITEM_DAMAGE));
		S_ITEM_DURIB = ChatColor.translateAlternateColorCodes('&', (String) m.a("Lore_Durib", S_ITEM_DURIB));
		S_ITEM_ING = ChatColor.translateAlternateColorCodes('&', (String) m.a("Lore_ingredients", S_ITEM_ING));
		if (m.getConfig().contains("Lore_Varients"))
			S_ITEM_VARIENTS_LEGACY = ChatColor.translateAlternateColorCodes('&',
					(String) m.a("Lore_Varients", S_ITEM_VARIENTS_LEGACY));
		S_ITEM_VARIENTS_NEW = ChatColor.translateAlternateColorCodes('&',
				(String) m.a("Lore_Variants", S_ITEM_VARIENTS_NEW));
		S_ITEM_COST = ChatColor.translateAlternateColorCodes('&', (String) m.a("Lore_Price", S_ITEM_COST));
		S_ITEM_DPS = ChatColor.translateAlternateColorCodes('&', (String) m.a("Lore_DamagePerSecond", S_ITEM_DPS));

		S_RELOADING_MESSAGE = ChatColor.translateAlternateColorCodes('&',
				(String) m.a("Reloading_Message", S_RELOADING_MESSAGE));
		S_MAX_FOUND = ChatColor.translateAlternateColorCodes('&', (String) m.a("State_AmmoCount", S_MAX_FOUND));
		S_OUT_OF_AMMO = ChatColor.translateAlternateColorCodes('&', (String) m.a("State_OutOfAmmo", S_OUT_OF_AMMO));
		S_HOTBAR_FORMAT = ChatColor.translateAlternateColorCodes('&', (String) m.a("HotbarMessage", S_HOTBAR_FORMAT));

		S_KICKED_FOR_RESOURCEPACK = ChatColor.translateAlternateColorCodes('&',
				(String) m.a("Kick_message_if_player_denied_request", S_KICKED_FOR_RESOURCEPACK));

		S_LMB_SINGLE = (String) m.a("Lore-LMB-Single", S_LMB_SINGLE);
		S_LMB_FULLAUTO = (String) m.a("Lore-LMB-FullAuto", S_LMB_FULLAUTO);
		S_RMB_RELOAD = (String) m.a("Lore-RMB-Reload", S_RMB_RELOAD);
		S_RMB_A1 = (String) m.a("Lore-Ironsights-RMB", S_RMB_A1);
		S_RMB_A2 = (String) m.a("Lore-Ironsights-Sneak", S_RMB_A2);
		S_RMB_R1 = (String) m.a("Lore-Reload-Dropitem", S_RMB_R1);
		S_RMB_R2 = (String) m.a("Lore-Reload-RMB", S_RMB_R2);

		S_BUYCONFIRM = ChatColor.translateAlternateColorCodes('&', (String) m.a("Shop_Confirm", S_BUYCONFIRM));

		S_RESOURCEPACK_HELP = (String) m.a("Resourcepack_InCaseOfCrash", S_RESOURCEPACK_HELP);
		S_RESOURCEPACK_DOWNLOAD = (String) m.a("Resourcepack_Download", S_RESOURCEPACK_DOWNLOAD);
		S_RESOURCEPACK_BYPASS = (String) m.a("Resourcepack_NowBypass", S_RESOURCEPACK_BYPASS);
		S_RESOURCEPACK_OPTIN = (String) m.a("Resourcepack_NowOptIn", S_RESOURCEPACK_OPTIN);

		S_FULLYHEALED = ChatColor.translateAlternateColorCodes('&', (String) m.a("Medkit-FullyHealed", S_FULLYHEALED));
		S_MEDKIT_HEALING = ChatColor.translateAlternateColorCodes('&',
				(String) m.a("Medkit-Healing", S_MEDKIT_HEALING));
		S_MEDKIT_BLEEDING = ChatColor.translateAlternateColorCodes('&',
				(String) m.a("Medkit-Bleeding", S_MEDKIT_BLEEDING));

		S_MEDKIT_HEAL_AMOUNT = (double) m.a("Medkit-HEALING_HEARTS_AMOUNT", S_MEDKIT_HEAL_AMOUNT);

		S_MEDKIT_HEALDELAY = (double) m.a("Medkit-HEALING_WEAPPING_DELAY_IN_SECONDS", S_MEDKIT_HEALDELAY);

		S_MEDKIT_LORE_INFO = ChatColor.translateAlternateColorCodes('&',
				(String) m.a("Medkit-Lore_RMB", S_MEDKIT_LORE_INFO));

		S_BLEEDOUT_LOSINGconscious = ChatColor.translateAlternateColorCodes('&',
				(String) m.a("Bleeding.Losingconsciousness", S_BLEEDOUT_LOSINGconscious));
		S_BLEEDOUT_STARTBLEEDING = ChatColor.translateAlternateColorCodes('&',
				(String) m.a("Bleeding.StartBleeding", S_BLEEDOUT_STARTBLEEDING));
		S_BULLETPROOFSTOPPEDBLEEDING = ChatColor.translateAlternateColorCodes('&',
				(String) m.a("Bleeding.ProtectedByKevlar", S_BULLETPROOFSTOPPEDBLEEDING));

		resourcepackwhitelist = new MessagesYML(new File(getDataFolder(), "resourcepackwhitelist.yml"));
		namesToBypass = (List<String>) resourcepackwhitelist.a("Names_Of_players_to_bypass", namesToBypass);

		if (!new File(getDataFolder(), "config.yml").exists())
			saveDefaultConfig();
		reloadConfig();

		if (getServer().getPluginManager().isPluginEnabled("Parties"))
			hasParties = true;
		if (getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
			hasProtocolLib = true;
			ProtocolLibHandler.initRemoveArmswing();
		}

		if (getServer().getPluginManager().isPluginEnabled("Sentinel"))
			try {
				org.mcmonkey.sentinel.SentinelPlugin.integrations.add(new SentinelQAHandler());
			} catch (Error | Exception e4) {
			}

		DEBUG = (boolean) a("ENABLE-DEBUG", false);

		friendlyFire = (boolean) a("FriendlyFireEnabled", false);

		kickIfDeniedRequest = (boolean) a("KickPlayerIfDeniedResourcepack", false);
		shouldSend = (boolean) a("useDefaultResourcepack", true);

		enableDurability = (boolean) a("EnableWeaponDurability", false);

		bulletStep = (double) a("BulletDetection.step", 0.10);

		blockbullet_door = (boolean) a("BlockBullets.door", false);
		blockbullet_halfslabs = (boolean) a("BlockBullets.halfslabs", false);
		blockbullet_leaves = (boolean) a("BlockBullets.leaves", false);
		blockbullet_water = (boolean) a("BlockBullets.water", false);
		blockbullet_glass = (boolean) a("BlockBullets.glass", false);

		enableInteractChests = (boolean) a("enableInteract.Chests", false);

		overrideAnvil = (boolean) a("overrideAnvil", false);

		sendOnJoin = (boolean) a("sendOnJoin", true);
		sendTitleOnJoin = (boolean) a("sendTitleOnJoin", false);
		secondsTilSend = Double.valueOf(a("SecondsTillRPIsSent", 5.0) + "");

		enableBulletTrails = (boolean) a("enableBulletTrails", true);
		smokeSpacing = Double.valueOf(a("BulletTrailsSpacing", 0.5) + "");

		enableArmorIgnore = (boolean) a("enableIgnoreArmorProtection", enableArmorIgnore);
		ignoreUnbreaking = (boolean) a("enableIgnoreUnbreakingChecks", ignoreUnbreaking);
		ignoreSkipping = (boolean) a("enableIgnoreSkipForBasegameItems", ignoreSkipping);

		ITEM_enableUnbreakable = (boolean) a("Items.enableUnbreaking", ITEM_enableUnbreakable);

		// enableVisibleAmounts = (boolean) a("enableVisibleBulletCounts", false);
		reloadOnF = (boolean) a("enableReloadingWhenSwapToOffhand", true);
		reloadOnFOnly = (boolean) a("enableReloadOnlyWhenSwapToOffhand", false);

		// showOutOfAmmoOnItem = (boolean) a("showOutOfAmmoOnItem", false);
		showOutOfAmmoOnTitle = (boolean) a("showOutOfAmmoOnTitle", false);
		showReloadOnTitle = (boolean) a("showReloadingTitle", false);

		enableExplosionDamage = (boolean) a("enableExplosionDamage", false);
		enableExplosionDamageDrop = (boolean) a("enableExplosionDamageDrop", false);

		enablePrimaryWeaponHandler = (boolean) a("enablePrimaryWeaponLimiter", false);
		primaryWeaponLimit = (int) a("weaponlimiter_primaries", primaryWeaponLimit);
		secondaryWeaponLimit = (int) a("weaponlimiter_secondaries", secondaryWeaponLimit);

		enableCrafting = (boolean) a("enableCrafting", true);
		enableShop = (boolean) a("enableShop", true);

		AUTOUPDATE = (boolean) a("AUTO-UPDATE", true);
		SWAP_RMB_WITH_LMB = !(boolean) a("Swap-Reload-and-Shooting-Controls", false);

		orderShopByPrice = (boolean) a("Order-Shop-By-Price", orderShopByPrice);

		ENABLE_LORE_INFO = (boolean) a("enable_lore_gun-info_messages", true);
		ENABLE_LORE_HELP = (boolean) a("enable_lore_control-help_messages", true);

		usedIronSightsMaterial = Material
				.matchMaterial((String) a("Material_For_IronSightToggle_Item", usedIronSightsMaterial.name()));
		usedIronSightsData = (int) a("Data_For_IronSightToggle_Item", usedIronSightsData);

		HeadshotOneHit = (boolean) a("Enable_Headshot_Instantkill", HeadshotOneHit);
		headshotPling = (boolean) a("Enable_Headshot_Notification_Sound", headshotPling);
		headshot_sound = (String) a("Headshot_Notification_Sound", headshot_sound);
		headshotGoreSounds = (boolean) a("Enable_Headshot_Sounds", headshotGoreSounds);

		ignoreArmorStands = (boolean) a("ignoreArmorStands", false);

		gravity = (double) a("gravityConstantForDropoffCalculations", gravity);

		allowGunReload = (boolean) a("allowGunReload", allowGunReload);
		AutoDetectResourcepackVersion = (boolean) a("Auto-Detect-Resourcepack", AutoDetectResourcepackVersion);
		MANUALLYSELECT18 = (boolean) a("ManuallyOverrideTo_1_8_systems",
				Bukkit.getPluginManager().isPluginEnabled("WetSponge") ? true : MANUALLYSELECT18);

		unknownTranslationKeyFixer = (boolean) a("unknownTranslationKeyFixer", false);

		enableCreationOfFiles = (boolean) a("Enable_Creation_Of_Default_Files", true);

		addGlowEffects = (boolean) a("EnableGlowEffects", false);

		blockBreakTexture = (boolean) a("Break-Block-Texture-If-Shot", true);

		enableRecoil = (boolean) a("enableRecoil", true);

		bulletWound_initialbloodamount = (double) a("experimental.BulletWounds.InitialBloodLevel",
				bulletWound_initialbloodamount);
		bulletWound_BloodIncreasePerSecond = (double) a("experimental.BulletWounds.BloodIncreasePerSecond",
				bulletWound_BloodIncreasePerSecond);
		bulletWound_MedkitBloodlossHealRate = (double) a("experimental.BulletWounds.Medkit_Heal_Bloodloss_Rate",
				bulletWound_MedkitBloodlossHealRate);
		enableBleeding = (boolean) a("experimental.BulletWounds.enableBleeding", enableBleeding);

		disableHotBarMessageOnOutOfAmmo = (boolean) a("disableHotbarMessages.OutOfAmmo", false);
		disableHotBarMessageOnShoot = (boolean) a("disableHotbarMessages.Shoot", false);
		disableHotBarMessageOnReload = (boolean) a("disableHotbarMessages.Reload", false);

		enableReloadWhenOutOfAmmo = (boolean) a("automaticallyReloadGunWhenOutOfAmmo", false);

		swayModifier_Run = (double) a("generalModifiers.sway.Run", swayModifier_Run);
		swayModifier_Walk = (double) a("generalModifiers.sway.Walk", swayModifier_Walk);
		swayModifier_Ironsights = (double) a("generalModifiers.sway.Ironsights", swayModifier_Ironsights);
		swayModifier_Sneak = (double) a("generalModifiers.sway.Sneak", swayModifier_Sneak);

		try {
			enableEconomy = EconHandler.setupEconomy();
		} catch (Exception | Error e) {
		}

		overrideURL = (boolean) a("DefaultResourcepackOverride", false);

		if (overrideURL) {
			url = (String) a("DefaultResourcepack", url);
			url18 = (String) a("DefaultResourcepack_18", url18);
			url18New = (String) a("DefaultResourcepack_18_COMPATFOR_19PLUS", url18New);
		} else {
			// Make sure the user is always up to date if they don't override
			// the resoucepack
			if (!getConfig().contains("DefaultResourcepack")
					|| !url.equals(getConfig().getString("DefaultResourcepack"))) {
				getConfig().set("DefaultResourcepack", url);
				getConfig().set("DefaultResourcepack_18", url18);
				getConfig().set("DefaultResourcepack_18_COMPATFOR_19PLUS", url18New);
				saveTheConfig = true;
			}
		}
		enableIronSightsON_RIGHT_CLICK = (boolean) a("IronSightsOnRightClick", false);

		List<String> destarray = (List<String>) a("DestructableMaterials", Collections.singletonList("MATERIAL_NAME_HERE"));
		for (String s : destarray) {
			try {
				destructableBlocks.add(Material.getMaterial(s));
			} catch (Error | Exception e54) {
				try {
					// destructableBlocks.add(Material.getMaterial(Integer.parseInt(s.split(":")[0])));
				} catch (Error | Exception e5) {
				}
			}
		}

		for (Material m : Material.values())
			if (m.isBlock())
				if (m.name().contains("DOOR") || m.name().contains("TRAPDOOR") || m.name().contains("BUTTON")
						|| m.name().contains("LEVER"))
					interactableBlocks.add(m);
		if (saveTheConfig) {
			DEBUG(prefix + " Needed to save config: code=2");
			saveConfig();
		}

		if (enableCreationOfFiles) {

			List<String> stringsWoodRif = Arrays.asList(new String[] { getIngString(Material.IRON_INGOT, 0, 12),
					getIngString(MultiVersionLookup.getWood(), 0, 2), getIngString(Material.REDSTONE, 0, 5) });
			List<String> stringsGoldRif = Arrays.asList(new String[] { getIngString(Material.IRON_INGOT, 0, 12),
					getIngString(Material.GOLD_INGOT, 0, 2), getIngString(Material.REDSTONE, 0, 5) });
			List<String> stringsMetalRif = Arrays.asList(
					new String[] { getIngString(Material.IRON_INGOT, 0, 15), getIngString(Material.REDSTONE, 0, 5) });
			List<String> stringsPistol = Arrays.asList(
					new String[] { getIngString(Material.IRON_INGOT, 0, 5), getIngString(Material.REDSTONE, 0, 2) });
			List<String> stringsRPG = Arrays.asList(
					new String[] { getIngString(Material.IRON_INGOT, 0, 32), getIngString(Material.REDSTONE, 0, 10) });

			List<String> stringsHelmet = Arrays.asList(
					new String[] { getIngString(Material.IRON_INGOT, 0, 5), getIngString(Material.OBSIDIAN, 0, 1) });

			List<String> stringsGrenades = Arrays.asList(new String[] { getIngString(Material.IRON_INGOT, 0, 6),
					getIngString(MultiVersionLookup.getGunpowder(), 0, 10) });

			List<String> stringsAmmo = Arrays.asList(new String[] { getIngString(Material.IRON_INGOT, 0, 1),
					getIngString(MultiVersionLookup.getGunpowder(), 0, 1), getIngString(Material.REDSTONE, 0, 1) });
			List<String> stringsAmmoMusket = Arrays.asList(new String[] { getIngString(Material.IRON_INGOT, 0, 4),
					getIngString(MultiVersionLookup.getGunpowder(), 0, 3), });
			List<String> stringsAmmoRPG = Arrays.asList(new String[] { getIngString(Material.IRON_INGOT, 0, 4),
					getIngString(MultiVersionLookup.getGunpowder(), 0, 6), getIngString(Material.REDSTONE, 0, 1) });

			List<String> StringsWool = Arrays.asList(new String[] { getIngString(MultiVersionLookup.getWool(), 0, 8) });

			List<String> stringsHealer = Arrays.asList(new String[] { getIngString(MultiVersionLookup.getWool(), 0, 6),
					getIngString(Material.GOLDEN_APPLE, 0, 1) });
			if (MANUALLYSELECT18 || !isVersionHigherThan(1, 9)
					|| (AutoDetectResourcepackVersion && Bukkit.getPluginManager().isPluginEnabled("ViaRewind"))) {
				String additive = AutoDetectResourcepackVersion ? "_18" : "";
				{
					GunYMLCreator
							.createNewCustomGun(getDataFolder(), "default_1_8_p30", "p30" + additive, "P30", 1,
									stringsPistol, WeaponType.PISTOL, null, true, "9mm", 3, 12, 100)
							.setMaterial(Material.IRON_HOE).setOn18(true).setIsSecondaryWeapon(true).done();
					if (!AutoDetectResourcepackVersion)
						GunYMLCreator
								.createNewCustomGun(getDataFolder(), "default_1_8_pkp", "pkp" + additive, "PKP", 1,
										stringsMetalRif, WeaponType.RIFLE, WeaponSounds.GUN_BIG, true, "556", 4, 100,
										3000)
								.setMaterial(Material.DIAMOND_AXE).setOn18(true).setFullyAutomatic(3)
								.setBulletsPerShot(1).done();
					GunYMLCreator
							.createNewCustomGun(getDataFolder(), "default_1_8_mp5k", "mp5k" + additive, "MP5K", 1,
									stringsMetalRif, WeaponType.SMG, null, false, "9mm", 3, 32, 1000)
							.setMaterial(MultiVersionLookup.getGoldPick()).setOn18(true).setFullyAutomatic(3)
							.setBulletsPerShot(1).done();
					GunYMLCreator
							.createNewCustomGun(getDataFolder(), "default_1_8_ak47", "ak47" + additive, "AK47", 1,
									stringsMetalRif, WeaponType.RIFLE, null, false, "556", 3, 40, 1500)
							.setMaterial(MultiVersionLookup.getGoldShovel()).setOn18(true).setFullyAutomatic(2)
							.setBulletsPerShot(1).done();
					GunYMLCreator
							.createNewCustomGun(getDataFolder(), "default_1_8_m16", "m16", "M16" + additive, 1,
									stringsMetalRif, WeaponType.RIFLE, null, true, "556", 3, 30, 2000)
							.setMaterial(MultiVersionLookup.getIronShovel()).setOn18(true).setFullyAutomatic(2)
							.setBulletsPerShot(1).done();
					GunYMLCreator
							.createNewCustomGun(getDataFolder(), "default_1_8_fnfal", "fnfal" + additive, "FNFal", 1,
									stringsMetalRif, WeaponType.RIFLE, null, false, "556", 3, 32, 2000)
							.setMaterial(MultiVersionLookup.getGoldHoe()).setOn18(true).setFullyAutomatic(2)
							.setBulletsPerShot(1).done();
					GunYMLCreator
							.createNewCustomGun(getDataFolder(), "default_1_8_rpg", "rpg" + additive, "RPG", 1,
									stringsRPG, WeaponType.RPG, null, false, "rocket", 100, 1, 4000)
							.setMaterial(Material.DIAMOND_HOE).setOn18(true).setCustomProjectile(ProjectileManager.RPG)
							.setCustomProjectileExplosionRadius(10).setCustomProjectileVelocity(2)// .setChargingHandler(ChargingManager.RPG)
							.setReloadingHandler(ReloadingManager.SINGLERELOAD).setDistance(500)
							.setParticle("SMOKE_LARGE").done();

					// TODO: New guns for resourcepack

					GunYMLCreator
							.createNewCustomGun(getDataFolder(), "default_1_8_famas", "famas" + additive, "FAMAS-G2", 1,
									stringsMetalRif, WeaponType.RIFLE, null, false, "556", 3, 30, 4500)
							.setFullyAutomatic(3).setRecoil(2).setMaterial(Material.PRISMARINE_CRYSTALS).setOn18(true)
							.done();
					GunYMLCreator
							.createNewCustomGun(getDataFolder(), "default_1_8_m79", "m79" + additive,
									"&6M79 \"Thumper\"", 1, stringsWoodRif, WeaponType.RPG, WeaponSounds.WARHEAD_LAUNCH,
									false, "40mm", 100, 1, 5000)
							.setDelayShoot(1).setCustomProjectile(ProjectileManager.EXPLODINGROUND)
							.setCustomProjectileVelocity(2).setCustomProjectileExplosionRadius(6)// .setChargingHandler(ChargingManager.MININUKELAUNCHER)
							.setReloadingHandler(ReloadingManager.SINGLERELOAD).setDistance(500)
							.setParticle(0.001, 0.001, 0.001).setRecoil(10).setMaterial(Material.PRISMARINE_SHARD)
							.setOn18(true).done();
					GunYMLCreator
							.createNewCustomGun(getDataFolder(), "default_1_8_dp27", "dp27" + additive, "DP-27", 1,
									stringsMetalRif, WeaponType.RIFLE, WeaponSounds.GUN_BIG, false, "762", 3, 47, 3000)
							.setFullyAutomatic(2).setBulletsPerShot(1).setRecoil(2).setMaterial(Material.QUARTZ)
							.setOn18(true).done();
					GunYMLCreator
							.createNewCustomGun(getDataFolder(), "default_1_8_m40", "m40" + additive, "M40", 1,
									stringsWoodRif, WeaponType.SNIPER, null, false, "762", 10, 6, 2700)
							.setZoomLevel(9).setDelayShoot(0.7).setChargingHandler(ChargingManager.BOLT)
							.setSwayMultiplier(3).setDistance(280).setRecoil(5).setMaterial(Material.NETHER_BRICK)
							.setOn18(true).done();
					GunYMLCreator.createNewCustomGun(getDataFolder(), "default_1_8_uzi", "uzi" + additive, "UZI", 1,
							stringsMetalRif, WeaponType.SMG, WeaponSounds.GUN_SMALL_AUTO, false, "9mm", 2, 25, 2000)
							.setFullyAutomatic(3).setMaterial(Material.RABBIT_FOOT).setOn18(true).done();
					GunYMLCreator
							.createNewCustomGun(getDataFolder(), "default_1_8_aa12", "aa12" + additive, "AA-12", 26,
									stringsMetalRif, WeaponType.SHOTGUN, null, false, "shell", 2, 32, 4000)
							.setBulletsPerShot(10).setDistance(80).setFullyAutomatic(2).setRecoil(7)
							.setMaterial(MultiVersionLookup.getCarrotOnAStick()).setOn18(true).done();
					GunYMLCreator
							.createNewCustomGun(getDataFolder(), "default_1_8_spas12", "spas12" + additive, "Spas-12",
									1, stringsMetalRif, WeaponType.SHOTGUN, null, false, "shell", 2, 8, 1000)
							.setBulletsPerShot(20).setDistance(80).setRecoil(10).setMaterial(Material.RABBIT_HIDE)
							.setOn18(true).done();

				}

				ArmoryYML skullammo = GunYMLCreator.createSkullAmmo(false, getDataFolder(), false, "default18_ammo556",
						"556ammo", "&7 5.56x45mm NATO", null, MultiVersionLookup.getSkull(), 3, "cactus", null, 4, 1,
						50);
				skullammo.set(false, "skull_owner_custom_url_COMMENT",
						"Only specify the custom URL if the head does not use a player's skin, and instead sets the skin to a base64 value. If you need to get the head using a command, the URL should be set to the string of letters after \"Properties:{textures:[{Value:\"");
				skullammo.set(false, "skull_owner_custom_url",
						"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTg3ZmRmNDU4N2E2NDQ5YmZjOGJlMzNhYjJlOTM4ZTM2YmYwNWU0MGY2ZmFhMjc3ZDcxYjUwYmNiMGVhNjgzOCJ9fX0=");
				ArmoryYML skullammo2 = GunYMLCreator.createSkullAmmo(false, getDataFolder(), false, "default18_ammoRPG",
						"RPGammo", "&7 Rocket", null, MultiVersionLookup.getSkull(), 3, "cactus", null, 4, 1, 50);
				skullammo2.set(false, "skull_owner_custom_url_COMMENT",
						"Only specify the custom URL if the head does not use a player's skin, and instead sets the skin to a base64 value. If you need to get the head using a command, the URL should be set to the string of letters after \"Properties:{textures:[{Value:\"");
				skullammo2.set(false, "skull_owner_custom_url",
						"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTg3ZmRmNDU4N2E2NDQ5YmZjOGJlMzNhYjJlOTM4ZTM2YmYwNWU0MGY2ZmFhMjc3ZDcxYjUwYmNiMGVhNjgzOCJ9fX0=");

			}
			if (!MANUALLYSELECT18 && isVersionHigherThan(1, 9)) {
				GunYMLCreator.createAmmo(false, getDataFolder(), false, "9mm", "&f9mm", 15, stringsAmmo, 2, 0.7, 50,
						10);
				GunYMLCreator.createAmmo(false, getDataFolder(), false, "556", "&f5.56 NATO", 14, stringsAmmo, 4, 1, 50,
						5);
				GunYMLCreator.createAmmo(false, getDataFolder(), false, "762", "&f7.62x39mm", 79, stringsAmmo, 5, 1.2,
						50, 5);
				GunYMLCreator.createAmmo(false, getDataFolder(), false, "shell", "&fBuckshot", 16, stringsAmmo, 10, 0.5,
						8, 4);
				GunYMLCreator.createAmmo(false, getDataFolder(), false, "rocket", "&fRocket", 17, stringsAmmoRPG, 100,
						1000, 1);
				GunYMLCreator.createAmmo(false, getDataFolder(), false, "musketball", "&fMusket Ball", 51,
						stringsAmmoMusket, 1, 0.7, 32, 8);
				GunYMLCreator.createAmmo(false, getDataFolder(), false, "50bmg", "&f.50BMG", 90, stringsAmmo, 10, 3, 30,
						1);
				GunYMLCreator.createAmmo(false, getDataFolder(), false, "40mm", "&f40x46mm", 99, stringsAmmo, 30, 10,
						10, 1);
				GunYMLCreator
						.createAmmo(false, getDataFolder(), false, "default_flamerfuel", "fuel", "&fFlamerFuel", null,
								Material.BLAZE_POWDER, 0,
								Arrays.asList(new String[] { getIngString(Material.BLAZE_ROD, 0, 1), }), 1, 1, 64, 2)
						.setVariant(1).done();

				GunYMLCreator.createNewDefaultGun(getDataFolder(), "p30", "P30", 2, stringsPistol, WeaponType.PISTOL,
						null, true, "9mm", 3, 12, 700).setIsSecondaryWeapon(true).done();
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "pkp", "PKP", 3, stringsMetalRif, WeaponType.RIFLE,
								WeaponSounds.GUN_BIG, true, "762", 3, 100, 12000)
						.setFullyAutomatic(3).setBulletsPerShot(1).setRecoil(2).done();
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "mp5k", "MP5K", 4, stringsMetalRif, WeaponType.SMG,
								WeaponSounds.GUN_SMALL_AUTO, false, "9mm", 2, 32, 2500)
						.setFullyAutomatic(3).setBulletsPerShot(1).done();
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "ak47", "AK47", 5, stringsMetalRif, WeaponType.RIFLE,
								null, true, "762", 3, 40, 5000)
						.setSway(0.19).setFullyAutomatic(2).setBulletsPerShot(1).setRecoil(2).done();
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "ak47u", "AK47-U", 6, stringsMetalRif, WeaponType.RIFLE,
								null, true, "762", 3, 30, 5000)
						.setFullyAutomatic(2).setBulletsPerShot(1).setRecoil(2).done();
				GunYMLCreator.createNewDefaultGun(getDataFolder(), "m16", "M16", 7, stringsMetalRif, WeaponType.RIFLE,
						null, true, "556", 3, 30, 3600).setFullyAutomatic(2).setBulletsPerShot(1).setRecoil(2).done();
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "remington", "Remington", 8, stringsMetalRif,
								WeaponType.SHOTGUN, null, false, "shell", 3, 8, 1000)
						.setChargingHandler(ChargingManager.PUMPACTION)
						.setReloadingHandler(ReloadingManager.PUMPACTIONRELOAD).setBulletsPerShot(20).setDistance(70)
						.setRecoil(10).done();
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "fnfal", "FN Fal", 9, stringsMetalRif, WeaponType.RIFLE,
								null, false, "762", 3, 32, 3800)
						.setFullyAutomatic(2).setBulletsPerShot(1).setRecoil(2).done();
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "rpg", "RPG", 10, stringsRPG, WeaponType.RPG, null, false,
								"rocket", 100, 1, 4000)
						.setDelayShoot(1).setCustomProjectile(ProjectileManager.RPG)
						.setCustomProjectileExplosionRadius(10).setCustomProjectileVelocity(2)// .setChargingHandler(ChargingManager.RPG)
						.setReloadingHandler(ReloadingManager.SINGLERELOAD).setDistance(500).setParticle("SMOKE_LARGE")
						.setRecoil(15).done();
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "ump", "UMP", 11, stringsMetalRif, WeaponType.SMG,
								WeaponSounds.GUN_SMALL_AUTO, false, "9mm", 2, 32, 1700)
						.setFullyAutomatic(2).setBulletsPerShot(1).done();
				GunYMLCreator.createNewDefaultGun(getDataFolder(), "sw1911", "SW-1911", 12, stringsPistol,
						WeaponType.PISTOL, null, true, "9mm", 3, 12, 700).setIsSecondaryWeapon(true).done();
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "m40", "M40", 13, stringsWoodRif, WeaponType.SNIPER, null,
								true, "762", 10, 6, 2700)
						.setZoomLevel(9).setDelayShoot(0.7).setChargingHandler(ChargingManager.BOLT)
						.setSwayMultiplier(3).setDistance(280).setRecoil(5).done();
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "enfield", "Enfield", 18, stringsPistol,
								WeaponType.PISTOL, null, true, "9mm", 3, 6, 200)
						.setIsSecondaryWeapon(true).setChargingHandler(ChargingManager.REVOLVER)
						.setReloadingHandler(ReloadingManager.SINGLERELOAD).done();
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "henryrifle", "Henry Rifle", 19, stringsGoldRif,
								WeaponType.RIFLE, null, true, "556", 4, 6, 400)
						.setChargingHandler(ChargingManager.BREAKACTION)
						.setReloadingHandler(ReloadingManager.SINGLERELOAD).done();
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "mauser", "Mauser C96", 20, stringsPistol,
								WeaponType.PISTOL, null, true, "9mm", 3, 12, 700)
						.setSwayMultiplier(3).setIsSecondaryWeapon(true).done();

				ArmoryYML grenade = GunYMLCreator.createMisc(false, getDataFolder(), false, "default_grenade",
						"grenade", "&7Grenade",
						Arrays.asList(ChatColor.DARK_GRAY + "[LMB] to pull pin", ChatColor.DARK_GRAY + "[RMB] to throw",
								ChatColor.DARK_GRAY + "Grenades wait " + ChatColor.GRAY + "FIVE seconds"
										+ ChatColor.DARK_GRAY + " before exploding.",
								ChatColor.DARK_RED + "<!>Will Explode Even If Not Thrown<!>"),
						m(22), stringsGrenades, 100, WeaponType.GRENADES, 100, 1);
				grenade.set(false, "radius", 10);
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "dragunov", "Dragunov", 23, stringsMetalRif,
								WeaponType.SNIPER, null, true, "762", 7, 12, 2100)
						.setDelayShoot(0.4).setZoomLevel(9).setSwayMultiplier(3).setRecoil(5).done();
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "spas12", "Spas-12", 24, stringsMetalRif,
								WeaponType.SHOTGUN, null, false, "shell", 2, 8, 1000)
						.setBulletsPerShot(20).setDistance(80).setRecoil(10).done();
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "aa12", "AA-12", 26, stringsMetalRif, WeaponType.SHOTGUN,
								null, false, "shell", 2, 32, 4000)
						.setBulletsPerShot(10).setDistance(80).setFullyAutomatic(2).setRecoil(7).done();

				/**
				 * 27 - 36 taken for custom weapons
				 */
				GunYMLCreator.createMisc(false, getDataFolder(), false, "default_Medkit_camo", "medkitcamo", "&5Medkit",
						null, m(37), stringsHealer, 300, WeaponType.MEDKIT, 1, 1000);

				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "magnum", "Magnum", 38, stringsPistol, WeaponType.PISTOL,
								WeaponSounds.GUN_BIG, true, "9mm", 6, 6, 700)
						.setChargingHandler(ChargingManager.REVOLVER).setReloadingHandler(ReloadingManager.SINGLERELOAD)
						.setIsSecondaryWeapon(true).setRecoil(10).done();
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "awp", "AWP", 39, stringsMetalRif, WeaponType.SNIPER,
								WeaponSounds.GUN_BIG, true, "762", 10, 12, 3000)
						.setDelayShoot(0.8).setZoomLevel(9).setSwayMultiplier(3).setRecoil(5).done();

				ArmoryYML smokegrenade = GunYMLCreator.createMisc(false, getDataFolder(), false, "default_smokegrenade",
						"smokegrenade", "&7Smoke Grenade",
						Arrays.asList(ChatColor.DARK_GRAY + "[LMB] to pull pin", ChatColor.DARK_GRAY + "[RMB] to throw",
								ChatColor.DARK_GRAY + "Smoke Grenades wait " + ChatColor.GRAY + "FIVE seconds"
										+ ChatColor.DARK_GRAY + " before exploding.",
								ChatColor.DARK_RED + "<!>Will Explode Even If Not Thrown<!>"),
						m(40), stringsGrenades, 100, WeaponType.SMOKE_GRENADES, 100, 1);
				smokegrenade.set(false, "radius", 5);
				ArmoryYML flashbanggrenade = GunYMLCreator.createMisc(false, getDataFolder(), false,
						"default_flashbang", "flashbang", "&7FlashBang",
						Arrays.asList(ChatColor.DARK_GRAY + "[LMB] to pull pin", ChatColor.DARK_GRAY + "[RMB] to throw",
								ChatColor.DARK_GRAY + "Flashbangs wait " + ChatColor.GRAY + "FIVE seconds"
										+ ChatColor.DARK_GRAY + " before exploding.",
								ChatColor.DARK_RED + "<!>Will Explode Even If Not Thrown<!>"),
						m(41), stringsGrenades, 100, WeaponType.FLASHBANGS, 100, 1);
				flashbanggrenade.set(false, "radius", 5);

				GunYMLCreator
						.createAttachment(false, getDataFolder(), false, "default_p30_silencer", "p30silenced",
								ChatColor.GOLD + "P30[Silenced]", null, m(42), stringsPistol, 1000, "p30")
						.setWeaponSound(WeaponSounds.SILENCEDSHOT).dontVerify().done();
				GunYMLCreator
						.createAttachment(false, getDataFolder(), false, "default_awp_asiimov", "awpasiimov",
								ChatColor.GOLD + "AWP[Asiimov-skin]", null, m(43), stringsMetalRif, 1000, "awp")
						.dontVerify().done();

				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "m4a1s", "M4A1s", 44, stringsMetalRif, WeaponType.RIFLE,
								null, true, "556", 3, 30, 3600)
						.setFullyAutomatic(2).setBulletsPerShot(1).setRecoil(5).done();

				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "rpk", "RPK", 45, stringsWoodRif, WeaponType.RIFLE, null,
								false, "762", 3, 70, 7000)
						.setFullyAutomatic(3).setBulletsPerShot(1).setRecoil(2).done();

				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "sg553", "SG-553", 46, stringsMetalRif, WeaponType.RIFLE,
								null, true, "556", 3, 40, 3200)
						.setFullyAutomatic(2).setBulletsPerShot(1).setRecoil(2).done();
				GunYMLCreator.createNewDefaultGun(getDataFolder(), "fnfiveseven", "FN-Five-Seven", 47, stringsPistol,
						WeaponType.PISTOL, null, true, "9mm", 3, 12, 700).setIsSecondaryWeapon(true).done();

				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "dp27", "DP-27", 48, stringsMetalRif, WeaponType.RIFLE,
								WeaponSounds.GUN_BIG, true, "762", 3, 47, 3000)
						.setFullyAutomatic(2).setBulletsPerShot(1).setRecoil(2).done();

				ArmoryYML incedarygrenade = GunYMLCreator.createMisc(false, getDataFolder(), false,
						"default_incendarygrenade", "incendarygrenade", "&7Incendary Grenade",
						Arrays.asList(ChatColor.DARK_GRAY + "[LMB] to pull pin", ChatColor.DARK_GRAY + "[RMB] to throw",
								ChatColor.DARK_GRAY + "Incendary Grenades wait " + ChatColor.GRAY + "FIVE seconds"
										+ ChatColor.DARK_GRAY + " before exploding.",
								ChatColor.DARK_RED + "<!>Will Explode Even If Not Thrown<!>"),
						m(49), stringsGrenades, 100, WeaponType.INCENDARY_GRENADES, 100, 1);
				incedarygrenade.set(false, "radius", 5);
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "homingrpg", "&6Homing RPG Launcher", 50, stringsMetalRif,
								WeaponType.RPG, null, false, "rocket", 100, 1, 5000)
						.setDelayShoot(1).setCustomProjectile(ProjectileManager.HOMING_RPG)
						.setCustomProjectileExplosionRadius(10).setCustomProjectileVelocity(2)// .setChargingHandler(ChargingManager.HOMINGRPG)
						.setReloadingHandler(ReloadingManager.SINGLERELOAD).setDistance(800).setNightVisionOnScope(true)
						.setParticle("SMOKE_LARGE").setRecoil(10).done();

				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "flintlockpistol", "\"Harper's Ferry\" Flintlock Pistol",
								52, stringsMetalRif, WeaponType.RIFLE, WeaponSounds.GUN_AUTO, true, "musketball", 10, 1,
								100)
						.setSway(0.4).setDelayReload(4).setDelayShoot(1).setIsSecondaryWeapon(true).setRecoil(8).done();

				// Jump for armor

				/**
				 * (boolean forceUpdate, File dataFolder, boolean invalid, String filename,
				 * String name, String displayname, List<String> lore, int id, List<String>
				 * craftingRequirements, WeaponType weapontype, boolean enableIronSights, String
				 * ammotype, int damage, double sway, Material type, int maxBullets, int
				 * duribility, double delayReload, double delayShoot, int bulletspershot,
				 * boolean isAutomatic, int cost, ChargingHandlerEnum ch, int distance, int var,
				 * boolean version18, WeaponSounds ws, String particle, double particleR, double
				 * particleG, double particleB, boolean addMuzzleSmoke)
				 *
				 */

				List<String> stringsMini = Arrays.asList(
						new String[] { getIngString(Material.IRON_INGOT, 0, 10), getIngString(Material.TNT, 0, 16) });
				List<String> strings10mm = Arrays.asList(new String[] { getIngString(Material.IRON_INGOT, 0, 10),
						getIngString(Material.REDSTONE, 0, 4) });

				List<String> stringsFatman = Arrays.asList(new String[] { getIngString(Material.IRON_INGOT, 0, 32),
						getIngString(Material.REDSTONE, 0, 16), getIngString(Material.BLAZE_POWDER, 0, 8) });

				GunYMLCreator.createAmmo(true, getDataFolder(), false, "default_fusion_cell", "fusion_cell",
						"Fusion Cell", 53, strings10mm, 60, 0.2, 30);
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "lazerrifle", "&6Lazer Rifle", 54, stringsMetalRif,
								WeaponType.LAZER, WeaponSounds.LAZERSHOOT, false, "fusion_cell", 4, 20, 2000)
						.setAutomatic(true).setParticle(1, 0, 0).setDistance(150).setSwayMultiplier(3).setSway(0.2)
						.setRecoil(0).done();
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "fatman", "&6Fatman", 55, stringsFatman, WeaponType.RPG,
								WeaponSounds.WARHEAD_LAUNCH, false, "mininuke", 500, 1, 6000)
						.setDelayShoot(1).setCustomProjectile(ProjectileManager.MINI_NUKE)
						.setCustomProjectileExplosionRadius(10).setCustomProjectileVelocity(3)// .setChargingHandler(ChargingManager.MININUKELAUNCHER)
						.setReloadingHandler(ReloadingManager.SINGLERELOAD).setDistance(500).setParticle(0.3, 0.9, 0.3)
						.setRecoil(5).done();
				GunYMLCreator.createAmmo(true, getDataFolder(), false, "default_mininuke", "mininuke", "MiniNuke", 56,
						stringsMini, 3000, 100, 1);

				GunYMLCreator.createNewDefaultGun(getDataFolder(), "10mm", "&610mm Pistol", 57, strings10mm,
						WeaponType.PISTOL, null, true, "9mm", 3, 12, 700).setIsSecondaryWeapon(true).done();

				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "instituterifle", "&6Institute Rifle", 58,
								stringsMetalRif, WeaponType.LAZER, WeaponSounds.LAZERSHOOT, false, "fusion_cell", 4, 20,
								2000)
						.setAutomatic(true).setParticle(0.5, 0.9, 0.9).setDistance(150).setSwayMultiplier(3)
						.setSway(0.2).setRecoil(0).done();

				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "musket", "\"Brown Bess\" Musket", 63, stringsMetalRif,
								WeaponType.RIFLE, WeaponSounds.GUN_AUTO, true, "musketball", 10, 1, 100)
						.setSway(0.3).setDelayReload(5).setDelayShoot(1).setSwayMultiplier(3).setRecoil(3).done();

				List<String> stringsRifle = Arrays.asList(new String[] { getIngString(Material.IRON_INGOT, 0, 8),
						getIngString(Material.REDSTONE, 0, 3) });
				List<String> stringsLight = Arrays.asList(new String[] { getIngString(Material.IRON_INGOT, 0, 8),
						getIngString(Material.NETHER_STAR, 0, 1) });

				GunYMLCreator
						.createNewCustomGun(getDataFolder(), "default_aliensrifle", "m41pulserifle",
								ChatColor.GOLD + "M41PulseRifle", 64, stringsRifle, WeaponType.RIFLE,
								WeaponSounds.GUN_MEDIUM, false, "556", 4, 30, 5000)
						.setLore(Collections.singletonList("&fGame over, man. Game over!")).setFullyAutomatic(3)
						.setBulletsPerShot(1).setMuzzleSmoke(false).setRecoil(2).done();
				GunYMLCreator
						.createNewCustomGun(getDataFolder(), "default_auto9", "auto9", ChatColor.GOLD + "Auto9", 65,
								stringsPistol, WeaponType.PISTOL, WeaponSounds.GUN_DEAGLE, true, "556", 5, 12, 700)
						.setLore(Collections.singletonList("&fDead or alive, you're coming with me! ")).setZoomLevel(1).setRecoil(2)
						.done();
				GunYMLCreator
						.createNewCustomGun(getDataFolder(), "default_arcgun9", "arcgun9",
								ChatColor.GOLD + "The Arc-Gun-9", 66, strings10mm, WeaponType.LAZER,
								WeaponSounds.SHOCKWAVE, false, "fusion_cell", 0, 10, 2400)
						.setLore(Collections.singletonList("&fPushy!"))
						.setChargingHandler(ChargingManager.getHandler(ChargingManager.PUSHBACK)).done();
				GunYMLCreator
						.createNewCustomGun(getDataFolder(), "default_halorifle", "unscassaultrifle",
								ChatColor.GOLD + "UNSCAssaultRifle", 67, stringsRifle, WeaponType.RIFLE,
								WeaponSounds.GUN_MEDIUM, true, "556", 3, 32, 3800)
						.setFullyAutomatic(3).setBulletsPerShot(1)
						.setLore(Collections.singletonList("&fAlso known as the \"MA5B\"")).setRecoil(2).done();
				GunYMLCreator
						.createNewCustomGun(getDataFolder(), "default_haloalien", "alienneedler",
								ChatColor.GOLD + "\"Needler\"", 68, stringsRifle, WeaponType.PISTOL,
								WeaponSounds.GUN_NEEDLER, true, "fusion_cell", 1, 32, 2000)
						.setFullyAutomatic(4).setBulletsPerShot(1).setLore(Collections.singletonList("&fWarning: Sharp"))
						.setParticle("REDSTONE", 1, 0.1, 1).done();
				GunYMLCreator
						.createNewCustomGun(getDataFolder(), "default_thatgun", "thatgun",
								ChatColor.GOLD + "\"That Gun\"", 69, stringsRifle, WeaponType.PISTOL,
								WeaponSounds.GUN_DEAGLE, true, "556", 5, 12, 2000)
						.setLore(Collections.singletonList("&fAlso known as the \"LAPD 2019 Detective Special\"")).setRecoil(2)
						.done();
				GunYMLCreator
						.createNewCustomGun(getDataFolder(), "default_blaster", "blaster",
								ChatColor.GOLD + "\"Blaster\" Pistol", 72, stringsGoldRif, WeaponType.LAZER,
								WeaponSounds.GUN_STARWARS, false, "fusion_cell", 4, 20, 1600)
						.setFullyAutomatic(1).setBulletsPerShot(1).setMuzzleSmoke(false).setParticle(1, 0, 0)
						.setLore(Collections.singletonList("&fMiss all the shots you want!")).setRecoil(0).done();
				GunYMLCreator
						.createNewCustomGun(getDataFolder(), "default_hl2pulserifle", "pulserifle",
								ChatColor.GOLD + "Overwatch Pulse Rifle", 73, stringsGoldRif, WeaponType.LAZER,
								WeaponSounds.GUN_HALOLAZER, true, "fusion_cell", 4, 30, 5000)
						.setFullyAutomatic(3).setBulletsPerShot(1).setMuzzleSmoke(false)
						.setLore(Collections.singletonList("&fStardard Issue Rifles for Combie solders."))
						.setParticle(0.5, 0.99, 0.99).setRecoil(2).done();
				GunYMLCreator
						.createNewCustomGun(getDataFolder(), "default_vera", "vera", ChatColor.GOLD + "Vera", 74,
								stringsGoldRif, WeaponType.RIFLE, WeaponSounds.GUN_DEAGLE, true, "556", 3, 30, 3000)
						.setNightVisionOnScope(true).setZoomLevel(5)
						.setLore(Arrays.asList("&fThe Callahan Full-bore Auto-lock.", "&7\"Customized trigger, …",
								"&7…double cartridge thorough gauge.", "&7It is my very favorite gun …",
								"&7…This is the best gun made by man.", "&7 It has extreme sentimental value …",
								"&7…I call her Vera.\"-Jayne Cobb"))
						.setFullyAutomatic(2).setBulletsPerShot(1).setRecoil(2).done();

				GunYMLCreator.createMisc(false, getDataFolder(), false, "default_lightsaberblue", "LightSaberBlue",
						ChatColor.GOLD + "(Blue)LightSaber", Arrays.asList("&fMay The Force be with you", "&fAlways"),
						Material.DIAMOND_AXE, 70, stringsLight, 10000, WeaponType.MEELEE, 9, 1000);
				GunYMLCreator.createMisc(false, getDataFolder(), false, "default_lightsaberred", "LightSaberRed",
						ChatColor.GOLD + "(Red)LightSaber", Arrays.asList("&fMay The Force be with you", "&fAlways"),
						Material.DIAMOND_AXE, 71, stringsLight, 10000, WeaponType.MEELEE, 9, 1000);

				GunYMLCreator.createNewDefaultGun(getDataFolder(), "mac10", "Mac-10", 75, stringsMetalRif,
						WeaponType.SMG, WeaponSounds.GUN_SMALL_AUTO, true, "9mm", 2, 32, 2500).setFullyAutomatic(3)
						.done();
				GunYMLCreator.createNewDefaultGun(getDataFolder(), "uzi", "UZI", 76, stringsMetalRif, WeaponType.SMG,
						WeaponSounds.GUN_SMALL_AUTO, true, "9mm", 2, 25, 2000).setFullyAutomatic(3).done();
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "skorpion", "Skorpion vz.61", 77, stringsMetalRif,
								WeaponType.SMG, WeaponSounds.GUN_SMALL_AUTO, true, "9mm", 2, 20, 1600)
						.setFullyAutomatic(3).done();

				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "sks", "SKS-45", 78, stringsWoodRif, WeaponType.SNIPER,
								null, true, "762", 7, 10, 2000)
						.setDelayShoot(0.6).setZoomLevel(6).setDistance(290).setSwayMultiplier(3).setRecoil(8).done();

				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "barrett", "Barrett-M82", 91, stringsWoodRif,
								WeaponType.SNIPER, WeaponSounds.GUN_BIG, true, "50bmg", 17, 10, 4000)
						.setDelayShoot(1).setZoomLevel(6).setDistance(350).setDelayReload(2.5).setSwayMultiplier(3)
						.setNightVisionOnScope(true).setRecoil(15).done();

				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "makarov", "Makarov \"PM\"", 93, stringsPistol,
								WeaponType.PISTOL, WeaponSounds.GUN_SMALL_AUTO, true, "9mm", 3, 8, 700)
						.setIsSecondaryWeapon(true).done();

				GunYMLCreator.createNewDefaultGun(getDataFolder(), "ppsh41", "PPSh-41", 98, stringsWoodRif,
						WeaponType.RIFLE, null, true, "762", 3, 71, 7000).setFullyAutomatic(3).setRecoil(2).done();
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "m79", "&6M79 \"Thumper\"", 100, stringsFatman,
								WeaponType.RPG, WeaponSounds.WARHEAD_LAUNCH, true, "40mm", 100, 1, 5000)
						.setDelayShoot(1).setCustomProjectile(ProjectileManager.EXPLODINGROUND)
						.setCustomProjectileVelocity(2).setCustomProjectileExplosionRadius(6)// .setChargingHandler(ChargingManager.MININUKELAUNCHER)
						.setReloadingHandler(ReloadingManager.SINGLERELOAD).setDistance(500)
						.setParticle(0.001, 0.001, 0.001).setRecoil(10).done();
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "minigun", "Minigun", 101, stringsMetalRif,
								WeaponType.BIG_GUN, WeaponSounds.GUN_BIG, true, "556", 2, 200, 15000)
						.setFullyAutomatic(5).setBulletsPerShot(1).setChargingHandler(ChargingManager.REQUIREAIM)
						.setSway(0.5).setSwayMultiplier(2.4).setParticle(0.9, 0.9, 0.9).done();
				GunYMLCreator// TODO: MINIGUN RECOIL
						.createNewDefaultGun(getDataFolder(), "mk19", "Mk-19", 102, stringsMetalRif, WeaponType.BIG_GUN,
								WeaponSounds.WARHEAD_LAUNCH, true, "40mm", 50, 50, 20000)
						.setFullyAutomatic(1).setCustomProjectile(ProjectileManager.EXPLODINGROUND)
						.setCustomProjectileVelocity(4).setCustomProjectileExplosionRadius(5)
						.setChargingHandler(ChargingManager.REQUIREAIM).setSway(0.5).setSwayMultiplier(2.4)
						.setParticle(0.001, 0.001, 0.001).setRecoil(7).done();
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "asval", "AS-Val", 103, stringsMetalRif, WeaponType.RIFLE,
								WeaponSounds.SILENCEDSHOT, true, "762", 3, 30, 7000)
						.setSway(0.2).setFullyAutomatic(3).setRecoil(2).done();
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "fnp90", "FN-P90", 105, stringsMetalRif, WeaponType.SMG,
								WeaponSounds.SILENCEDSHOT, true, "556", 2, 50, 3000)
						.setDelayReload(2.5).setFullyAutomatic(4).setRecoil(2).done();
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "kar98k", "Kar-98K", 106, stringsWoodRif,
								WeaponType.SNIPER, null, true, "762", 10, 6, 2500)
						.setZoomLevel(2).setDelayShoot(0.7).setChargingHandler(ChargingManager.BOLT)
						.setSwayMultiplier(3).setDistance(280).setRecoil(7).done();
				GunYMLCreator.createNewDefaultGun(getDataFolder(), "mp40", "MP 40", 107, stringsMetalRif,
						WeaponType.SMG, WeaponSounds.GUN_SMALL, true, "9mm", 2, 32, 3800).setFullyAutomatic(3).done();
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "sturmgewehr44", "Sturmgewehr 44", 108, stringsMetalRif,
								WeaponType.SMG, WeaponSounds.GUN_AUTO, true, "762", 3, 30, 3800)
						.setFullyAutomatic(3).setRecoil(2).done();

				/**
				 * Variant systems
				 */
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "vz58", "VZ.58", 5, stringsMetalRif, WeaponType.RIFLE,
								null, true, "762", 3, 30, 4500)
						.setSway(0.2).setFullyAutomatic(2).setBulletsPerShot(1).setVariant(1).done();
				GunYMLCreator.createNewDefaultGun(getDataFolder(), "cz65", "CZ.75", 2, stringsPistol, WeaponType.PISTOL,
						null, true, "9mm", 3, 12, 700).setIsSecondaryWeapon(true).setVariant(1).done();

				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "sawedoffshotgun", "Sawed-off Shotgun", 109,
								stringsMetalRif, WeaponType.SHOTGUN, null, true, "shell", 2, 2, 1000)
						.setReloadingHandler(ReloadingManager.SINGLERELOAD).setDelayReload(1).setBulletsPerShot(20)
						.setDistance(80).setRecoil(11).done();
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "famas", "FAMAS-G2", 110, stringsMetalRif,
								WeaponType.RIFLE, null, true, "556", 3, 30, 4500)
						.setFullyAutomatic(3).setRecoil(2).done();

				GunYMLCreator.createDefaultArmor(getDataFolder(), false, "assaulthelmet", "Assault Helmet", null, 25,
						stringsHelmet, 3000, WeaponType.HELMET, 1.5, 2, true);
				GunYMLCreator.createDefaultArmor(getDataFolder(), false, "ncrhelmet", "NCR Ranger Helmet", null, 59,
						stringsHelmet, 5000, WeaponType.HELMET, 1.5, 2, true);
				GunYMLCreator.createDefaultArmor(getDataFolder(), false, "skimask", "Ski Mask", null, 60, StringsWool,
						50, WeaponType.HELMET, 1, 0, false);
				GunYMLCreator.createDefaultArmor(getDataFolder(), false, "ushanka", "Ushanka-Hat", null, 61,
						StringsWool, 50, WeaponType.HELMET, 1, 0, false);
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "flamer", "Flamer", 113, stringsMetalRif,
								WeaponType.FLAMER, WeaponSounds.HISS, false, "fuel", 1, 60, 8000)
						.setFullyAutomatic(5).setRecoil(0).setCustomProjectile(ProjectileManager.FIRE).setDistance(11)
						.done();
				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "glock", "Glock-17", 2, stringsPistol, WeaponType.PISTOL,
								null, true, "9mm", 3, 15, 1800)
						.setIsSecondaryWeapon(true).setFireRate(3).setVariant(2).done();

				GunYMLCreator
						.createNewDefaultGun(getDataFolder(), "sten", "STEN Gun", 117, stringsMetalRif,
								WeaponType.RIFLE, WeaponSounds.GUN_SMALL_AUTO, true, "9mm", 2, 32, 2500)
						.setFullyAutomatic(3).setRecoil(1).done();
			}

			// 80 81 82 83 84 86 87 88 89 92 94 95 96 97 104 111 112 114 115 116
			if (!overrideURL) {
				expansionPacks.add(MaterialStorage.getMS(Material.DIAMOND_AXE, 80, 0));
				expansionPacks.add(MaterialStorage.getMS(Material.DIAMOND_AXE, 81, 0));
				expansionPacks.add(MaterialStorage.getMS(Material.DIAMOND_AXE, 82, 0));
				expansionPacks.add(MaterialStorage.getMS(Material.DIAMOND_AXE, 83, 0));
				expansionPacks.add(MaterialStorage.getMS(Material.DIAMOND_AXE, 84, 0));
				expansionPacks.add(MaterialStorage.getMS(Material.DIAMOND_AXE, 85, 0));
				expansionPacks.add(MaterialStorage.getMS(Material.DIAMOND_AXE, 86, 0));
				expansionPacks.add(MaterialStorage.getMS(Material.DIAMOND_AXE, 87, 0));
				expansionPacks.add(MaterialStorage.getMS(Material.DIAMOND_AXE, 88, 0));
				expansionPacks.add(MaterialStorage.getMS(Material.DIAMOND_AXE, 89, 0));
				expansionPacks.add(MaterialStorage.getMS(Material.DIAMOND_AXE, 92, 0));
				expansionPacks.add(MaterialStorage.getMS(Material.DIAMOND_AXE, 94, 0));
				expansionPacks.add(MaterialStorage.getMS(Material.DIAMOND_AXE, 95, 0));
				expansionPacks.add(MaterialStorage.getMS(Material.DIAMOND_AXE, 96, 0));
				expansionPacks.add(MaterialStorage.getMS(Material.DIAMOND_AXE, 97, 0));
				expansionPacks.add(MaterialStorage.getMS(Material.DIAMOND_AXE, 104, 0));
				expansionPacks.add(MaterialStorage.getMS(Material.DIAMOND_AXE, 111, 0));
				expansionPacks.add(MaterialStorage.getMS(Material.DIAMOND_AXE, 112, 0));
				expansionPacks.add(MaterialStorage.getMS(Material.DIAMOND_AXE, 114, 0));
				expansionPacks.add(MaterialStorage.getMS(Material.DIAMOND_AXE, 115, 0));
				expansionPacks.add(MaterialStorage.getMS(Material.DIAMOND_AXE, 116, 0));
			}

			// GunYMLCreator.createNewGun(false, getDataFolder(), true, "ExampleGun",
			// "Examplegun",
			// ChatColor.GOLD + "Example", Arrays.asList("Hello", "more lines"), 0,
			// stringsGoldRif,
			// WeaponType.PISTOL, false, "556", 3, 0.3, Material.DIAMOND_AXE, 100, 1000,
			// 1.5, 0.25, 1, true,
			// 1000000, null, 120, false, WeaponSounds.GUN_AUTO);

			GunYMLCreator
					.createNewCustomGun(getDataFolder(), "example_gun", "ExampleGun", "&7ExampleGun", 35,
							stringsMetalRif, WeaponType.RIFLE, WeaponSounds.GUN_MEDIUM, true, "556", 4, 100, 69)
					.setInvalid(true).setDropGlow(ChatColor.GREEN).setLore(Arrays.asList("Hello", "more lines")).done();

			GunYMLCreator.createAmmo(false, getDataFolder(), true, "example_ammo", "example", "7fDisplayname",
					Arrays.asList("Example", "Lore"), Material.DIAMOND_AXE, 27, stringsAmmo, 1, 1.0, 16);

			GunYMLCreator.createMisc(false, getDataFolder(), true, "example_knife", "ExampleKnife", "&7Example Knife",
                    Collections.singletonList("Now, this is a knife!"), Material.IRON_SWORD, 1, stringsMetalRif, 100,
					WeaponType.MEELEE, 12, 100);
			GunYMLCreator.createAttachment(false, getDataFolder(), true, "example_attachment", "example_attachment",
					"Attachment For AK47", null, m(28), stringsMetalRif, 100, "AK47").dontVerify().done();

			ArmoryYML skullammo = GunYMLCreator.createSkullAmmo(false, getDataFolder(), true, "example_skullammo",
					"exampleSkullAmmo", "&7 Example Ammo", null, MultiVersionLookup.getSkull(), 3, "cactus", null, 4, 1,
					64);
			skullammo.set(false, "skull_owner_custom_url_COMMENT",
					"Only specify the custom URL if the head does not use a player's skin, and instead sets the skin to a base64 value. If you need to get the head using a command, the URL should be set to the string of letters after \"Properties:{textures:[{Value:\"");
			skullammo.set(false, "skull_owner_custom_url",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTg3ZmRmNDU4N2E2NDQ5YmZjOGJlMzNhYjJlOTM4ZTM2YmYwNWU0MGY2ZmFhMjc3ZDcxYjUwYmNiMGVhNjgzOCJ9fX0=");

		}
		// Skull texture
		GunYMLLoader.loadAmmo(this);
		GunYMLLoader.loadMisc(this);
		GunYMLLoader.loadGuns(this);
		GunYMLLoader.loadAttachments(this);
		GunYMLLoader.loadArmor(this);
		if (QAMain.enableBleeding)
			BulletWoundHandler.startTimer();

		if (tfh != null) {
			tfh = new TreeFellerHandler();
			Bukkit.getPluginManager().registerEvents(tfh, this);
		}
		if (addGlowEffects) {
			coloredGunScoreboard = new ArrayList<>();
			coloredGunScoreboard.add(registerGlowTeams(Bukkit.getScoreboardManager().getMainScoreboard()));
		}

	}

	public Scoreboard registerGlowTeams(Scoreboard sb) {
		if (sb.getTeam("QA_RED") == null) {
			for (ChatColor c : ChatColor.values()) {
				if (sb.getTeam("QA_" + c.name() + "") == null)
					sb.registerNewTeam("QA_" + c.name() + "").setPrefix(c + "");
			}
		}
		return sb;
	}

	@SuppressWarnings("deprecation")
	public ItemStack[] convertIngredients(List<String> e) {
		ItemStack[] list = new ItemStack[e.size()];
		for (int i = 0; i < e.size(); i++) {
			String[] k = e.get(i).split(",");

			ItemStack temp = null;
			try {
				temp = new ItemStack(Material.matchMaterial(k[0]));
			} catch (Exception e2) {
				// temp = new ItemStack(Integer.parseInt(k[0]));
			}
			if (k.length > 1)
				temp.setDurability(Short.parseShort(k[1]));
			if (k.length > 2)
				temp.setAmount(Integer.parseInt(k[2]));
			list[i] = temp;
		}
		return list;
	}

	public String getIngString(Material m, int durability, int amount) {
		return m.toString() + "," + durability + "," + amount;
	}

	public boolean b(String arg, String startsWith) {
		if (arg == null || startsWith == null)
			return false;
		return arg.toLowerCase().startsWith(startsWith.toLowerCase());
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 1) {
			List<String> s = new ArrayList<>();
			if (b("give", args[0]))
				s.add("give");
			if (b("getResourcepack", args[0]))
				s.add("getResourcepack");
			if (b("sendResourcepack", args[0]))
				s.add("sendResourcepack");
			if (b("version", args[0]))
				s.add("version");
			if (enableShop)
				if (b("shop", args[0]))
					s.add("shop");
			if (enableCrafting)
				if (b("craft", args[0]))
					s.add("craft");
			// if (b("getOpenGunSlot", args[0]))
			// s.add("getOpenGunSlot");
			if (sender.hasPermission("qualityarmory.reload"))
				if (b("reload", args[0]))
					s.add("reload");
			if (sender.hasPermission("qualityarmory.createnewitem")) {
				if (b("createNewAmmoGun", args[0]))
					s.add("createNewGun");
				if (b("createNewAmmo", args[0]))
					s.add("createNewAmmo");
			}

			return s;
		}
		if (args.length == 2) {
			List<String> s = new ArrayList<>();
			if (args[0].equalsIgnoreCase("give")) {
				for (Entry<MaterialStorage, Gun> e : gunRegister.entrySet())
					if (b(e.getValue().getName(), args[1]))
						s.add(e.getValue().getName());
				for (Entry<MaterialStorage, Ammo> e : ammoRegister.entrySet())
					if (b(e.getValue().getName(), args[1]))
						s.add(e.getValue().getName());
				for (Entry<MaterialStorage, ArmoryBaseObject> e : miscRegister.entrySet())
					if (b(e.getValue().getName(), args[1]))
						s.add(e.getValue().getName());
				for (Entry<MaterialStorage, ArmorObject> e : armorRegister.entrySet())
					if (b(e.getValue().getName(), args[1]))
						s.add(e.getValue().getName());
				/*
				 * for (Entry<MaterialStorage, AttachmentBase> e :
				 * attachmentRegister.entrySet()) if (b(e.getValue().getAttachmentName(),
				 * args[1])) s.add(e.getValue().getAttachmentName());
				 */

			}
			return s;
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("QualityArmory")) {
			if (args.length > 0) {

				if (args[0].equalsIgnoreCase("debug")) {
					Gunner.createGunner(((Entity) sender).getLocation(), "ak47");
				}
				if (args[0].equalsIgnoreCase("debug2")) {
					for (Gunner g : gunners) {
						g.dispose();
					}
				}

				if (args[0].equalsIgnoreCase("version")) {
					sender.sendMessage(prefix + ChatColor.WHITE + " This server is using version " + ChatColor.GREEN
							+ this.getDescription().getVersion() + ChatColor.WHITE + " of QualityArmory");
					sender.sendMessage("--==Changelog==--");
					InputStream in = getClass().getResourceAsStream("/changelog.txt");
					BufferedReader reader = new BufferedReader(new InputStreamReader(in));
					for (int i = 0; i < 7; i++) {
						try {
							String s = reader.readLine();
							if (s.length() <= 1)
								break;
							if (i == 6) {
								sender.sendMessage("......");
								break;
							}
							sender.sendMessage(s);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					return true;
				}
				/*
				 * if (args[0].equalsIgnoreCase("getOpenGunSlot")) { if
				 * (sender.hasPermission("qualityarmory.getopengunslot")) {
				 * List<MaterialStorage> getAllKeys = new ArrayList<>();
				 * getAllKeys.addAll(gunRegister.keySet());
				 * getAllKeys.addAll(ammoRegister.keySet());
				 * getAllKeys.addAll(miscRegister.keySet());
				 * getAllKeys.addAll(armorRegister.keySet()); int openID = 1; for
				 * (MaterialStorage i : getAllKeys) { if (i.getMat() == guntype) if (i.getData()
				 * > openID) openID = i.getData(); } sender.sendMessage(prefix +
				 * " The next open slot for \"" + guntype.name() + "\"-base-guns is " + (openID
				 * + 1)); return true; } else { sender.sendMessage(prefix + ChatColor.RED +
				 * S_NOPERM); return true; } }
				 */
				if (args[0].equalsIgnoreCase("createNewAmmo")) {
					if (sender.hasPermission("qualityarmory.createnewitem")) {
						if (args.length >= 2) {
							ItemStack itemInHand = ((Player) sender).getItemInHand();
							if (itemInHand == null) {
								sender.sendMessage(prefix + " You need to hold an item that will be used as ammo.");

								return true;
							}
							GunYMLCreator.createSkullAmmo(true, getDataFolder(), false, "custom_" + args[1], args[1],
									args[1], Collections.singletonList("Custom_item"), itemInHand.getType(),
									itemInHand.getDurability(),
									(itemInHand.getType() == MultiVersionLookup.getSkull()
											? ((SkullMeta) itemInHand.getItemMeta()).getOwner()
											: null),
									null, 100, 1, 64);
							sender.sendMessage(prefix + " A new ammo type has been created.");
							sender.sendMessage(
									prefix + " You will need to use /qa reload for the new ammo type to appear.");
						} else {
							sendHelp(sender);
						}
					} else {
						sender.sendMessage(prefix + ChatColor.RED + S_NOPERM);
						return true;
					}
					return true;
				}
				if (args[0].equalsIgnoreCase("sendResourcepack")) {
					Player player = null;
					if (args.length > 1) {
						player = Bukkit.getPlayer(args[1]);
						if (player == null) {
							sender.sendMessage(prefix + " This player does not exist.");
							return true;
						}
					} else {
						player = (Player) sender;
					}
					namesToBypass.remove(player.getName());
					resourcepackwhitelist.set("Names_Of_players_to_bypass", namesToBypass);
					sender.sendMessage(prefix + S_RESOURCEPACK_OPTIN);
					QualityArmory.sendResourcepack(player, true);
					return true;
				}
				if (args[0].equalsIgnoreCase("getResourcepack")) {
					Player player = null;
					if (args.length > 1) {
						player = Bukkit.getPlayer(args[1]);
						if (player == null) {
							sender.sendMessage(prefix + " This player does not exist.");
							return true;
						}
					} else {
						player = (Player) sender;
					}
					namesToBypass.add(player.getName());
					resourcepackwhitelist.set("Names_Of_players_to_bypass", namesToBypass);
					player.sendMessage(prefix + S_RESOURCEPACK_DOWNLOAD);
					if (AutoDetectResourcepackVersion && Bukkit.getPluginManager().isPluginEnabled("ViaRewind")) {
						if (MANUALLYSELECT18) {
							player.sendMessage("For 1.9+ : " + url18New);
							player.sendMessage("For 1.8  : " + url18);
						} else {
							player.sendMessage("For 1.9+ : " + url);
							player.sendMessage("For 1.8  : " + url18);
						}
					} else {
						player.sendMessage(url);
					}
					player.sendMessage(prefix + S_RESOURCEPACK_BYPASS);

					return true;
				}

				if (args[0].equalsIgnoreCase("createNewGun")) {
					if (sender.hasPermission("qualityarmory.createnewitem")) {
						if (args.length >= 2) {
							ItemStack itemInHand = ((Player) sender).getItemInHand();
							if (itemInHand == null) {
								sender.sendMessage(prefix + " You need to hold an item that will be used as gun");
								return true;
							}
							GunYMLCreator.createNewGun(false, getDataFolder(), false, "custom_" + args[1], args[1],
									args[1], Collections.singletonList("Custom_item"), itemInHand.getDurability(), null,
									WeaponType.RIFLE, false, "9mm", 1, 0.2, itemInHand.getType(), 64, 100, 1.5, 0.2, 1,
									false, 200, null, 120, 0, (AutoDetectResourcepackVersion), WeaponSounds.GUN_MEDIUM);
							GunYMLCreator.createSkullAmmo(true, getDataFolder(), true, "custom_" + args[1], args[1],
									args[1], Collections.singletonList("Custom_item"), itemInHand.getType(),
									itemInHand.getDurability(),
									(itemInHand.getType() == MultiVersionLookup.getSkull()
											? ((SkullMeta) itemInHand.getItemMeta()).getOwner()
											: null),
									null, 100, 1, 64);
							sender.sendMessage(prefix + " A new gun has been created.");
							sender.sendMessage(prefix + " You will need to use /qa reload for the new gun to appear.");
						} else {
							sendHelp(sender);
						}
					} else {
						sender.sendMessage(prefix + ChatColor.RED + S_NOPERM);
						return true;
					}
					return true;
				}

				if (args[0].equalsIgnoreCase("listItemIds")) {
					if (sender.hasPermission("qualityarmory.getmaterialsused")) {
						for (ArmoryBaseObject g : miscRegister.values())
							sender.sendMessage(ChatColor.GREEN + g.getName() + ": " + ChatColor.WHITE
									+ g.getItemData().getMat().name() + " : " + g.getItemData().getData());
						for (Gun g : gunRegister.values())
							sender.sendMessage(ChatColor.GOLD + g.getName() + ": " + ChatColor.WHITE
									+ g.getItemData().getMat().name() + " : " + g.getItemData().getData());
						for (Ammo g : ammoRegister.values())
							sender.sendMessage(ChatColor.AQUA + g.getName() + ": " + ChatColor.WHITE
									+ g.getItemData().getMat().name() + " : " + g.getItemData().getData());
					} else {
						sender.sendMessage(prefix + ChatColor.RED + S_NOPERM);
						return true;
					}
				}
				if (args[0].equalsIgnoreCase("debug")) {
					BulletWoundHandler.bulletHit((Player) sender, 1);
					return true;
				}
				if (args[0].equalsIgnoreCase("reload")) {
					if (sender.hasPermission("qualityarmory.reload")) {
						reloadConfig();
						reloadVals();
						sender.sendMessage(prefix + " Guns and configs have been reloaded.");
						return true;
					} else {
						sender.sendMessage(prefix + ChatColor.RED + S_NOPERM);
						return true;
					}
				}
				if (args[0].equalsIgnoreCase("give")) {
					if (!sender.hasPermission("qualityarmory.give")) {
						sender.sendMessage(prefix + ChatColor.RED + S_NOPERM);
						return true;
					}
					if (args.length == 1) {
						sender.sendMessage(prefix + " The item name is required");
						StringBuilder sb = new StringBuilder();
						sb.append("Valid items: ");
						for (Gun g : gunRegister.values()) {
							sb.append(g.getName() + ",");
						}
						sb.append(ChatColor.GRAY);
						for (Ammo g : ammoRegister.values()) {
							sb.append(g.getName() + ",");
						}
						sb.append(ChatColor.WHITE);
						for (ArmoryBaseObject g : miscRegister.values()) {
							sb.append(g.getName() + ",");
						}
						sb.append(ChatColor.GRAY);
						for (ArmorObject g : armorRegister.values()) {
							sb.append(g.getName() + ",");
						}
						sb.append(ChatColor.WHITE);
						// for (AttachmentBase g : attachmentRegister.values()) {
						// sb.append(g.getAttachmentName() + ",");
						// }
						sender.sendMessage(prefix + sb.toString());
						return true;
					}

					ArmoryBaseObject g = null;
					// for (int j = 1; j < args.length; j++) {
					// gunName.append(args[j]);
					// if (j != args.length - 1)
					// gunName.append(" ");
					// }
					// Check if it is a gun, then if it is ammo, then if it is misc
					for (Entry<MaterialStorage, Gun> e : gunRegister.entrySet())
						if (e.getValue().getName().equalsIgnoreCase(args[1])) {
							g = e.getValue();
							break;
						}
					if (g == null)
						for (Entry<MaterialStorage, Ammo> e : ammoRegister.entrySet())
							if (e.getValue().getName().equalsIgnoreCase(args[1])) {
								g = e.getValue();
								break;
							}
					if (g == null)
						for (Entry<MaterialStorage, ArmoryBaseObject> e : miscRegister.entrySet())
							if (e.getValue().getName().equalsIgnoreCase(args[1])) {
								g = e.getValue();
								break;
							}
					if (g == null)
						for (Entry<MaterialStorage, ArmorObject> e : armorRegister.entrySet())
							if (e.getValue().getName().equalsIgnoreCase(args[1])) {
								g = e.getValue();
								break;
							}
					/*
					 * if (g == null) for (Entry<MaterialStorage, AttachmentBase> e :
					 * attachmentRegister.entrySet()) if
					 * (e.getValue().getAttachmentName().equalsIgnoreCase(args[1])) { // g =
					 * e.getValue().getBase(); attachment = e.getValue(); g =
					 * gunRegister.get(attachment.getBase()); break; }
					 */
					if (g != null) {
						Player who = null;
						if (args.length > 2)
							who = Bukkit.getPlayer(args[2]);
						else if (sender instanceof Player)
							who = ((Player) sender);
						else {
							sender.sendMessage("A USER IS REQUIRED FOR CONSOLE. /QA give <gun> <player>");
						}
						if (who == null) {
							sender.sendMessage("That player is not online");
							return true;
						}

						ItemStack temp;

						if (g instanceof Gun) {
							temp = ItemFact.getGun((Gun) g);
							who.getInventory().addItem(temp);
						} else if (g instanceof Ammo) {
							// temp = ItemFact.getAmmo((Ammo) g);
							AmmoUtil.addAmmo(who, (Ammo) g, ((Ammo) g).getMaxAmount());
						} else {
							temp = ItemFact.getObject(g);
							temp.setAmount(g.getCraftingReturn());
							who.getInventory().addItem(temp);
						}

						sender.sendMessage(prefix + " Adding " + g.getName() + " to your inventory");
					} else {
						sender.sendMessage(prefix + " Could not find item \"" + args[1] + "\"");
					}
					return true;
				}
			}
			if (sender instanceof Player) {
				final Player player = (Player) sender;
				if (args.length >= 1 && args[0].equalsIgnoreCase("override")) {
					resourcepackReq.add(player.getUniqueId());
					sender.sendMessage(prefix + " Overriding resourcepack requirement.");
					return true;
				}
				if (shouldSend && !resourcepackReq.contains(player.getUniqueId())) {
					QualityArmory.sendResourcepack(((Player) sender), true);
					return true;
				}
				if (args.length == 0) {
					sendHelp(player);
					return true;
				}
				if (enableCrafting)
					if (args[0].equalsIgnoreCase("craft")) {
						if (!sender.hasPermission("qualityarmory.craft")) {
							sender.sendMessage(prefix + ChatColor.RED + S_NOPERM);
							return true;
						}
						player.openInventory(createCraft(0));
						return true;

					}
				if (enableShop)
					if (args[0].equalsIgnoreCase("shop")) {
						if (!sender.hasPermission("qualityarmory.shop")) {
							sender.sendMessage(prefix + ChatColor.RED + S_NOPERM);
							return true;
						}
						player.openInventory(createShop(0));
						return true;

					}
				// The user sent an invalid command/no args.
				sendHelp(player);
				return true;
			}
		}
		return true;
	}

	public void sendHelp(CommandSender sender) {
		sender.sendMessage(prefix + " Commands:");
		sender.sendMessage(ChatColor.GOLD + "/QA give <Item>:" + ChatColor.GRAY
				+ " Gives the sender the item specified (guns, ammo, misc.)");
		sender.sendMessage(ChatColor.GOLD + "/QA craft:" + ChatColor.GRAY + " Opens the crafting menu.");
		sender.sendMessage(ChatColor.GOLD + "/QA shop: " + ChatColor.GRAY + "Opens the shop menu");

		sender.sendMessage(ChatColor.GOLD + "/QA getResourcepack: " + ChatColor.GRAY
				+ "Sends the link to the resourcepack. Disables the resourcepack prompts until /qa sendResourcepack is used");
		sender.sendMessage(ChatColor.GOLD + "/QA sendResourcepack: " + ChatColor.GRAY
				+ "Sends the resourcepack prompt. Enables the resourcepack prompt. Enabled by default");

		// sender.sendMessage(
		// ChatColor.GOLD + "/QA listItemIds: " + ChatColor.GRAY + "Lists the materials
		// and data for all items.");
		if (sender.hasPermission("qualityarmory.reload"))
			sender.sendMessage(ChatColor.GOLD + "/QA reload: " + ChatColor.GRAY + "reloads all values in QA.");
		if (sender.hasPermission("qualityarmory.createnewitem")) {
			sender.sendMessage(ChatColor.GOLD + "/QA createNewGun <name>: " + ChatColor.GRAY
					+ "Creats a new gun using the item in your hand as a template.");
			sender.sendMessage(ChatColor.GOLD + "/QA createNewAmmo <name>: " + ChatColor.GRAY
					+ "Creats a new ammo type using the item in your hand as a template");
		}
	}

	public boolean isDuplicateGun(ItemStack is1, Player player) {
		for (ItemStack is : player.getInventory().getContents()) {
			if (is != null && is1 != null)
				if (ItemFact.sameGun(is, is1)) {
					return true;
				}
		}
		return false;
	}

	public void shopsSounds(InventoryClickEvent e, boolean shop) {

		if (shop) {
			try {
				((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.BLOCK_ANVIL_USE, 0.7f, 1);
			} catch (Error e2) {
				((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.valueOf("ANVIL_USE"),
						0.7f, 1);
			}
		} else {
			try {
				((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), MultiVersionLookup.getHarp(),
						0.7f, 1);
			} catch (Error e2) {
				((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.valueOf("NOTE_PIANO"),
						0.7f, 1);
			}
		}
	}

	public boolean lookForIngre(Player player, ArmoryBaseObject a) {
		return lookForIngre(player, a.getIngredients());
	}

	@SuppressWarnings("deprecation")
	public boolean lookForIngre(Player player, ItemStack[] ings) {
		if (ings == null)
			return true;
		boolean[] bb = new boolean[ings.length];
		for (ItemStack is : player.getInventory().getContents()) {
			if (is != null) {
				for (int i = 0; i < ings.length; i++) {
					if (bb[i])
						continue;
					if (is.getType() == ings[i].getType()
							&& (ings[i].getDurability() == 0 || is.getDurability() == ings[i].getDurability())) {
						if (is.getAmount() >= ings[i].getAmount())
							bb[i] = true;
						break;
					}
				}
			}
		}
		for (boolean b : bb) {
			if (!b)
				return false;
		}
		return true;
	}

	public boolean removeForIngre(Player player, ArmoryBaseObject a) {
		return removeForIngre(player, a.getIngredients());
	}

	@SuppressWarnings("deprecation")
	public boolean removeForIngre(Player player, ItemStack[] ings) {
		if (ings == null)
			return true;
		boolean[] bb = new boolean[ings.length];
		for (ItemStack is : player.getInventory().getContents()) {
			if (is != null) {
				for (int i = 0; i < ings.length; i++) {
					if (bb[i])
						continue;
					if (is.getType() == ings[i].getType() && is.getDurability() == ings[i].getDurability()) {
						if (is.getAmount() > ings[i].getAmount()) {
							bb[i] = true;
							int slot = player.getInventory().first(is);
							is.setAmount(is.getAmount() - ings[i].getAmount());
							player.getInventory().setItem(slot, is);
						} else if (is.getAmount() == ings[i].getAmount()) {
							bb[i] = true;
							int slot = player.getInventory().first(is);
							player.getInventory().setItem(slot, new ItemStack(Material.AIR));
						}
						break;
					}
				}
			}
		}
		for (boolean b : bb) {
			if (!b)
				return false;
		}
		return true;
	}

	public void checkforDups(Player p, ItemStack... curr) {
		for (ItemStack curs : curr)
			for (int i = 0; i < p.getInventory().getSize(); i++) {
				ItemStack cont = p.getInventory().getItem(i);
				if (cont != null)
					if (curs != null && ItemFact.sameGun(cont, curs))
						if (!cont.equals(curs))
							p.getInventory().setItem(i, null);

			}
	}

	public static MaterialStorage m(int d) {
		return MaterialStorage.getMS(Material.DIAMOND_AXE, d, 0);
	}

	@Override
	public void reloadConfig() {
		if (configFile == null) {
			configFile = new File(this.getDataFolder(), "config.yml");
		}
		config = CommentYamlConfiguration.loadConfiguration(configFile);
		InputStream defConfigStream = this.getResource("config.yml");
		if (defConfigStream != null) {
			config.setDefaults(
					YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));
		}
	}

	@Override
	public FileConfiguration getConfig() {
		if (this.config == null) {
			this.reloadConfig();
		}
		return this.config;
	}

	public static Inventory createShop(int page) {
		return createCustomInventory(page, true);
	}

	public static Inventory createCraft(int page) {
		return createCustomInventory(page, false);
	}

	public static Inventory createCustomInventory(int page, boolean shopping) {
		Inventory shopMenu = Bukkit.createInventory(null, 9 * 6, (shopping ? S_shopName : S_craftingBenchName) + page);
		List<Gun> gunslistr = new ArrayList<>(gunRegister.values());
		Collections.sort(gunslistr);
		int basei = 0;
		int index = (page * 9 * 5);
		int maxIndex = (index + (9 * 5));

		shopMenu.setItem((9 * 6) - 1 - 8, prevButton);
		shopMenu.setItem((9 * 6) - 1, nextButton);

		if (basei + gunslistr.size() < index)
			basei += gunslistr.size();
		else
			for (Gun g : gunslistr) {
				if (shopping && g.cost() < 0)
					continue;
				if (basei < index) {
					basei++;
					continue;
				}
				basei++;
				if (index >= maxIndex)
					break;
				index++;
				try {
					ItemStack is = ItemFact.getGun(g);
					ItemMeta im = is.getItemMeta();
					List<String> lore = ItemFact.getCraftingGunLore(g);
					im.setLore(lore);
					is.setItemMeta(im);
					if (enableVisibleAmounts)
						is.setAmount(g.getCraftingReturn());
					if (shopping)
						is = ItemFact.addShopLore(g, is.clone());
					shopMenu.addItem(is);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		/*
		 * List<AttachmentBase> attlistr = new
		 * ArrayList<AttachmentBase>(attachmentRegister.values());
		 * Collections.sort(attlistr); if (basei + gunslistr.size() < index) basei +=
		 * gunslistr.size(); else for (AttachmentBase g : attlistr) { Gun g2 =
		 * gunRegister.get(g.getBase()); if (shopping && g2.cost() < 0) continue; if
		 * (basei < index) { basei++; continue; } basei++; if (index >= maxIndex) break;
		 * index++;
		 *
		 * try { ItemStack is = ItemFact.getGun(g); ItemMeta im = is.getItemMeta();
		 * List<String> lore = ItemFact.getCraftingGunLore(g2, g); im.setLore(lore);
		 * is.setItemMeta(im); if (enableVisibleAmounts)
		 * is.setAmount(g2.getCraftingReturn()); if (shopping) is =
		 * ItemFact.addShopLore(g2, g, is.clone()); shopMenu.addItem(is); } catch
		 * (Exception e) { e.printStackTrace(); } }
		 */
		if (basei + gunslistr.size() < index)
			basei += gunslistr.size();
		else
			for (ArmoryBaseObject abo : miscRegister.values()) {
				if (shopping && abo.cost() < 0)
					continue;
				if (basei < index) {
					basei++;
					continue;
				}
				basei++;
				if (index >= maxIndex)
					break;
				index++;
				try {
					ItemStack is = ItemFact.getObject(abo);
					ItemMeta im = is.getItemMeta();
					List<String> lore = ItemFact.getCraftingLore(abo);
					im.setLore(lore);
					is.setItemMeta(im);
					if (enableVisibleAmounts)
						is.setAmount(abo.getCraftingReturn());
					if (shopping)
						is = ItemFact.addShopLore(abo, is.clone());
					shopMenu.addItem(is);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		if (basei + gunslistr.size() < index)
			basei += gunslistr.size();
		else
			for (Ammo ammo : ammoRegister.values()) {
				if (shopping && ammo.cost() < 0)
					continue;
				if (basei < index) {
					basei++;
					continue;
				}
				basei++;
				if (index >= maxIndex)
					break;
				index++;
				try {
					ItemStack is = ItemFact.getAmmo(ammo);
					ItemMeta im = is.getItemMeta();
					List<String> lore = ItemFact.getCraftingLore(ammo);
					im.setLore(lore);
					is.setItemMeta(im);
					is.setAmount(ammo.getCraftingReturn());
					if (shopping)
						is = ItemFact.addShopLore(ammo, is.clone());
					shopMenu.addItem(is);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		if (basei + gunslistr.size() < index)
			basei += gunslistr.size();
		else
			for (ArmorObject armor : armorRegister.values()) {
				if (shopping && armor.cost() < 0)
					continue;
				if (basei < index) {
					basei++;
					continue;
				}
				basei++;
				if (index >= maxIndex)
					break;
				index++;
				try {
					ItemStack is = ItemFact.getArmor(armor);
					ItemMeta im = is.getItemMeta();
					List<String> lore = ItemFact.getCraftingLore(armor);
					im.setLore(lore);
					is.setItemMeta(im);
					is.setAmount(armor.getCraftingReturn());
					if (shopping)
						is = ItemFact.addShopLore(armor, is.clone());
					shopMenu.addItem(is);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		return shopMenu;
	}
}
