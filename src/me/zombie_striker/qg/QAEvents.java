package me.zombie_striker.qg;

import me.zombie_striker.qg.ammo.Ammo;
import me.zombie_striker.qg.ammo.AmmoUtil;
import me.zombie_striker.qg.api.QualityArmory;
import me.zombie_striker.qg.armor.ArmorObject;
import me.zombie_striker.qg.attachments.AttachmentBase;
import me.zombie_striker.qg.guns.Gun;
import me.zombie_striker.qg.guns.utils.GunRefillerRunnable;
import me.zombie_striker.qg.guns.utils.GunUtil;
import me.zombie_striker.qg.handlers.BulletWoundHandler;
import me.zombie_striker.qg.handlers.EconHandler;
import me.zombie_striker.qg.handlers.Update19OffhandChecker;
import me.zombie_striker.qg.miscitems.IronSightsToggleItem;
import me.zombie_striker.qg.miscitems.MeleeItems;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;

/**
 * @author Plajer
 * <p>
 * Created at 25.12.2018
 */
public class QAEvents implements Listener {

    public QAEvents(QAMain plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onHopperPickup(InventoryPickupItemEvent e) {
        if (e.isCancelled())
            return;
        if (e.getInventory().getType() == InventoryType.HOPPER)
            if (me.zombie_striker.qg.api.QualityArmory.isGun(e.getItem().getItemStack()))
                e.setCancelled(true);
    }

    @EventHandler
    public void onHopper(InventoryMoveItemEvent e) {
        if (e.isCancelled())
            return;
        if (e.getSource().getType() == InventoryType.HOPPER || e.getSource().getType() == InventoryType.DISPENSER
                || e.getSource().getType() == InventoryType.DROPPER)
            if (QualityArmory.isGun(e.getItem()))
                e.setCancelled(true);
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (e.isCancelled())
            return;
        if (e.getDamager() instanceof Player) {
            Player d = (Player) e.getDamager();
            if ((e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)
                    && QualityArmory.isMisc(d.getItemInHand())) {
                ArmoryBaseObject aa = QualityArmory.getMisc(d.getItemInHand());
                if (aa instanceof MeleeItems) {
                    QAMain.DEBUG("Setting damage for " + aa.getName() + " to be equal to " + ((MeleeItems) aa).getDamage()
                            + ". was " + e.getDamage());
                    e.setDamage(((MeleeItems) aa).getDamage());
                }
            }
            if (QualityArmory.isGun(d.getItemInHand()) || QualityArmory.isGunWithAttchments(d.getItemInHand())
                    || QualityArmory.isIronSights(d.getItemInHand()))
                QAMain.DEBUG("The player " + e.getEntity().getName() + " was shot with a gun! Damage=" + e.getDamage());
        }
    }

    @EventHandler
    public void onDeath(PlayerRespawnEvent e) {
        BulletWoundHandler.bleedoutMultiplier.remove(e.getPlayer().getUniqueId());
        BulletWoundHandler.bloodLevel.put(e.getPlayer().getUniqueId(), QAMain.bulletWound_initialbloodamount);
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onDrop(final PlayerDropItemEvent e) {
        if (e.isCancelled())
            return;
        if (QAMain.showOutOfAmmoOnItem) {
            if (QualityArmory.isGun(e.getItemDrop().getItemStack())) {
                if (e.getItemDrop().getItemStack().getItemMeta().getDisplayName().contains(QAMain.S_OUT_OF_AMMO)) {
                    ItemStack temp = e.getItemDrop().getItemStack();
                    ItemMeta im = temp.getItemMeta();
                    im.setDisplayName(QualityArmory.getGun(temp).getDisplayName());
                    temp.setItemMeta(im);
                    e.getItemDrop().setItemStack(temp);
                    QAMain.DEBUG("UNSUPPORTED: Dropping item with Out of ammo displayed");
                }
            }
        }

        if (QualityArmory.isIronSights(e.getItemDrop().getItemStack())) {
            e.setCancelled(true);
            if (!QAMain.enableIronSightsON_RIGHT_CLICK)
                return;
        }
        if (QualityArmory.isGun(e.getItemDrop().getItemStack())) {
            Gun g = QualityArmory.getGun(e.getItemDrop().getItemStack());
            if (QAMain.enableVisibleAmounts)
                if (QAMain.getInstance().isDuplicateGun(e.getItemDrop().getItemStack(), e.getPlayer())) {
                    QAMain.DEBUG("Dup gun");
                    e.setCancelled(true);
                    return;
                }
            if ((e.getItemDrop().getItemStack().getItemMeta().hasDisplayName()
                    && e.getItemDrop().getItemStack().getItemMeta().getDisplayName().contains(QAMain.S_RELOADING_MESSAGE))) {
                if (!GunRefillerRunnable.hasItemReloaded(e.getItemDrop().getItemStack())) {
                    if (g != null) {
                        ItemStack fix = e.getItemDrop().getItemStack();
                        ItemMeta temp = fix.getItemMeta();
                        temp.setDisplayName(g.getDisplayName());
                        fix.setItemMeta(temp);
                        e.getItemDrop().setItemStack(fix);
                        QAMain.DEBUG("Glitched gun. Allow drop");
                        e.setCancelled(false);
                        return;
                    }
                }
                // If the gun is glitched, allow dropps. If not, cancel it
                e.setCancelled(true);
                QAMain.DEBUG("Canceled thing because player tried to drop gun while reloading.");
                return;
            }
            if (g.getGlow() != null && QAMain.coloredGunScoreboard != null) {
                for (Scoreboard s : QAMain.coloredGunScoreboard)
                    if (s.getTeam("QA_" + g.getGlow().name() + "") != null)
                        s.getTeam("QA_" + g.getGlow().name() + "").addEntry(e.getItemDrop().getUniqueId().toString());
                QAMain.DEBUG("Added Glow");
                e.getItemDrop().setGlowing(true);
            }
        }
        QAMain.getInstance().checkforDups(e.getPlayer(), e.getItemDrop().getItemStack());

        if (QAMain.enableIronSightsON_RIGHT_CLICK) {
            if (e.getPlayer().getItemInHand() != null && (QualityArmory.isGun(e.getItemDrop().getItemStack())
                    || QualityArmory.isIronSights(e.getItemDrop().getItemStack()))) {
                if (e.getItemDrop().getItemStack().getAmount() == 1) {
                    try {
                        boolean dealtWithDrop = false;
                        if (QualityArmory.isIronSights(e.getItemDrop().getItemStack())) {
                            e.getItemDrop().setItemStack(e.getPlayer().getInventory().getItemInOffHand());
                            // e.getPlayer().setItemInHand(e.getPlayer().getInventory().getItemInOffHand());
                            e.getPlayer().getInventory().setItemInOffHand(null);
                            dealtWithDrop = true;
                        }
                        if (e.getPlayer().getItemInHand().getType() != Material.AIR && e.getPlayer().getItemInHand()
                                .getDurability() != e.getItemDrop().getItemStack().getDurability()) {
                            return;
                        }
                        Gun g = QualityArmory.getGun(e.getItemDrop().getItemStack());
                        if (g != null) {
                            QAMain.DEBUG("Dropped gun is a gun. Checking for has ammo");
                            e.setCancelled(true);
                            if (GunUtil.hasAmmo(e.getPlayer(), g)) {
                                if (!dealtWithDrop) {
                                    if (e.getPlayer().getItemInHand().getType() != Material.AIR) {
                                        if ((g.getMaxBullets() - 1) == ItemFact
                                                .getAmount(e.getPlayer().getItemInHand())) {
                                            QAMain.DEBUG("Player is full on ammo. Don't reload");
                                            return;
                                        }
                                    }
                                }
                                final Gun gk = g;
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        QAMain.DEBUG("Reloaded after one tick");
                                        GunUtil.basicReload(gk, e.getPlayer(), gk.hasUnlimitedAmmo());
                                    }
                                }.runTaskLater(QAMain.getInstance(), 1);
                            }
                        }

                    } catch (Error e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onShiftToggle(PlayerToggleSneakEvent e) {
        if (e.isCancelled())
            return;
        if (!QAMain.enableIronSightsON_RIGHT_CLICK) {
            ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
            if (QualityArmory.isIronSights(item))
                item = e.getPlayer().getInventory().getItemInOffHand();
            if (item != null && QualityArmory.isGun(item)) {
                Gun gun = QualityArmory.getGun(item);
                QAMain.toggleNightvision(e.getPlayer(), gun, true);
            }
            if (!e.isSneaking())
                QAMain.toggleNightvision(e.getPlayer(), null, false);
        }
    }

    @EventHandler
    public void onAnvilClick(final PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.ANVIL
                && QAMain.overrideAnvil && !e.getPlayer().isSneaking()) {
            QAMain.DEBUG("ANVIL InteractEvent Called");
            if (QAMain.shouldSend && !QAMain.resourcepackReq.contains(e.getPlayer().getUniqueId())) {
                QualityArmory.sendResourcepack(e.getPlayer(), true);
                e.setCancelled(true);
                QAMain.DEBUG("Resourcepack message being sent!");
                return;
            }
            if (!e.getPlayer().hasPermission("qualityarmory.craft")) {
                e.getPlayer().sendMessage(QAMain.prefix + ChatColor.RED + QAMain.S_ANVIL);
                return;
            }
            e.getPlayer().openInventory(QAMain.createCraft(0));
            e.setCancelled(true);
            QAMain.DEBUG("Opening crafting menu");
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onShift(final PlayerToggleSneakEvent e) {
        if (e.isCancelled())
            return;
        if (!QAMain.enableIronSightsON_RIGHT_CLICK) {
            QAMain.DEBUG("Sneak Toggle Called");
            try {
                if (e.isSneaking()) {
                    if (QualityArmory.isGun(e.getPlayer().getItemInHand())
                            || QualityArmory.isGunWithAttchments(e.getPlayer().getItemInHand())
                            && (!QualityArmory.isCustomItem(e.getPlayer().getInventory().getItemInOffHand()))) {
                        QAMain.DEBUG("Sneak Swapping start!");
                        Gun g = QualityArmory.getGun(e.getPlayer().getItemInHand());
                        if (g != null) {
                            QAMain.DEBUG("Gun used " + g.getName() + " attach?= " + (g instanceof AttachmentBase));
                            if (g.hasIronSights()) {
                                QAMain.DEBUG("Gun has ironsights");
                                try {

                                    if (!e.getPlayer().getItemInHand().hasItemMeta()
                                            || !e.getPlayer().getItemInHand().getItemMeta().hasDisplayName()
                                            || e.getPlayer().getItemInHand().getItemMeta().getDisplayName()
                                            .contains(QAMain.S_RELOADING_MESSAGE))
                                        return;
                                    if (Update19OffhandChecker.supportOffhand(e.getPlayer())) {
                                        ItemStack tempremove = null;
                                        if (e.getPlayer().getInventory().getItemInOffHand() != null)
                                            tempremove = e.getPlayer().getInventory().getItemInOffHand();
                                        e.getPlayer().getInventory()
                                                .setItemInOffHand(e.getPlayer().getInventory().getItemInMainHand());
                                        ItemStack ironsights = ItemFact.getIronSights();
                                        e.getPlayer().getInventory().setItemInMainHand(ironsights);
                                        QAMain.DEBUG("Swap gun  to off hand");
                                        if (tempremove != null && !QualityArmory.isGun(tempremove)) {
                                            e.getPlayer().getInventory().addItem(tempremove);
                                            QAMain.DEBUG("Added offhand back to inventory");
                                        }

                                        new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                Gun g1 = null;
                                                Gun g2 = null;
                                                if ((g1 = QualityArmory.getGun(e.getPlayer().getItemInHand())) != null
                                                        && (g2 = QualityArmory.getGun(e.getPlayer().getInventory()
                                                        .getItemInOffHand())) != null)
                                                    if (g1 == g2) {
                                                        e.getPlayer().getInventory().setItemInOffHand(null);
                                                        QAMain.DEBUG("Item Duped. Got Rid of using offhand override (code=1)");
                                                    }
                                            }
                                        }.runTaskLater(QAMain.getInstance(), 5);
                                    }
                                } catch (Error e2) {
                                    Bukkit.broadcastMessage(QAMain.prefix
                                            + "Ironsights not compatible for versions lower than 1.8. The server owner should set EnableIronSights to false in the plugin's config");
                                }
                            }
                        }
                    }
                } else {
                    if (QualityArmory.isIronSights(e.getPlayer().getItemInHand())) {

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Gun g1 = null;
                                Gun g2 = null;
                                if ((g1 = QualityArmory.getGun(e.getPlayer().getItemInHand())) != null
                                        && (g2 = QualityArmory
                                        .getGun(e.getPlayer().getInventory().getItemInOffHand())) != null)
                                    if (g1 == g2) {
                                        e.getPlayer().getInventory().setItemInOffHand(null);
                                        QAMain.DEBUG("Item Duped. Got Rid of using offhand override (code=2)");
                                    }
                            }
                        }.runTaskLater(QAMain.getInstance(), 5);

                        e.getPlayer().getInventory().setItemInMainHand(e.getPlayer().getInventory().getItemInOffHand());
                        e.getPlayer().getInventory().setItemInOffHand(null);
                        QAMain.DEBUG("Swap gun back to main hand");
                    }
                }
            } catch (Error | Exception e2) {
                QAMain.DEBUG("Failed to sneak and put gun in off hand.");
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled())
            return;
        if (e.getPlayer().getItemInHand() != null && (QualityArmory.isCustomItem(e.getPlayer().getItemInHand()))) {
            e.setCancelled(true);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreakMonitor(final BlockBreakEvent e) {
        if (e.isCancelled())
            return;
        int k = 0;
        if (e.getPlayer().getItemInHand() != null)
            if ((k = ItemFact.getCalculatedExtraDurib(e.getPlayer().getItemInHand())) != -1) {
                ItemStack hand = e.getPlayer().getItemInHand();
                e.getBlock().breakNaturally(hand);
                e.setCancelled(true);
                final ItemStack t;
                if (k > 0) {
                    t = ItemFact.decrementCalculatedExtra(hand);
                } else {
                    t = ItemFact.removeCalculatedExtra(hand);
                }
                new BukkitRunnable() {

                    @Override
                    public void run() {
                        e.getPlayer().setItemInHand(t);
                    }
                }.runTaskLater(QAMain.getInstance(), 1);

            }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        QAMain.recoilHelperMovedLocation.put(e.getPlayer().getUniqueId(), e.getTo());
    }

    @EventHandler
    public void onHeadPlace(BlockPlaceEvent e) {
        if (QualityArmory.isAmmo(e.getItemInHand()) || QualityArmory.isGun(e.getItemInHand()))
            e.setCancelled(true);
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent e) {
        if (QAMain.reloadingTasks.containsKey(e.getEntity().getUniqueId())) {
            for (BukkitTask r : QAMain.reloadingTasks.get(e.getEntity().getUniqueId())) {
                r.cancel();
                QAMain.DEBUG("Canceling reload task " + r.getTaskId());
            }
        }
        QAMain.reloadingTasks.remove(e.getEntity().getUniqueId());

        for (ItemStack is : new ArrayList<>(e.getDrops())) {
            if (QualityArmory.isIronSights(is)) {
                e.getDrops().remove(is);
                QAMain.DEBUG("Removing IronSights");
            }
        }

        if (e.getDeathMessage() != null && IronSightsToggleItem.getItemName() != null
                && e.getDeathMessage().contains(IronSightsToggleItem.getItemName())) {
            try {
                e.setDeathMessage(e.getDeathMessage().replace(IronSightsToggleItem.getItemName(),
                        e.getEntity().getKiller().getInventory().getItemInOffHand().getItemMeta().getDisplayName()));
                QAMain.DEBUG("Removing ironsights from death message and replaced with gun's name");
            } catch (Error | Exception e34) {
            }
        }
        BulletWoundHandler.bleedoutMultiplier.remove(e.getEntity().getUniqueId());
        BulletWoundHandler.bloodLevel.put(e.getEntity().getUniqueId(), QAMain.bulletWound_initialbloodamount);

        if (e.getEntity().getKiller() instanceof Player) {
            Player killer = e.getEntity().getKiller();
            if (QualityArmory.isGun(killer.getItemInHand()) || QualityArmory.isIronSights(killer.getItemInHand())) {
                QAMain.DEBUG("This player \"" + e.getEntity().getName() + "\" was killed by a player with a gun");
            } else if (QualityArmory.isCustomItem(e.getEntity().getItemInHand())) {
                QAMain.DEBUG("This player \"" + e.getEntity().getName() + "\" was killed by a player, but not with a gun");
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClickMONITOR(final PlayerInteractEvent e) {
        if (QAMain.ignoreSkipping)
            return;
        if (e.getPlayer().getItemInHand() != null && !QualityArmory.isCustomItem(e.getPlayer().getItemInHand())) {
            QAMain.DEBUG("Item is not any valid item - mainhand");
            if (QualityArmory.isCustomItemNextId(e.getPlayer().getItemInHand())) {
                QAMain.DEBUG("A player is using a non-gun item, but may reach the textures of one!");
                // If the item is not a gun, but the item below it is
                int safeDurib = QualityArmory.findSafeSpot(e.getPlayer().getItemInHand(), true, QAMain.overrideURL)
                        + (QAMain.overrideURL ? 0 : 3);

                // if (e.getItem().getDurability() == 1) {
                QAMain.DEBUG("Safe Durib= " + (safeDurib) + "! ORG " + e.getPlayer().getItemInHand().getDurability());
                ItemStack is = e.getPlayer().getItemInHand();
                is.setDurability((short) (safeDurib));
                is = ItemFact.addCalulatedExtraDurib(is, safeDurib - e.getPlayer().getItemInHand().getDurability());
                e.getPlayer().setItemInHand(is);
                // }
            }
        }
        if (e.getPlayer().getInventory().getItemInOffHand() != null
                && !QualityArmory.isCustomItem(e.getPlayer().getInventory().getItemInOffHand())) {
            QAMain.DEBUG("Item is not any valid item - offhand");
            if (QualityArmory.isCustomItemNextId(e.getPlayer().getInventory().getItemInOffHand())) {
                QAMain.DEBUG("A player is using a non-gun item, but may reach the textures of one!");
                // If the item is not a gun, but the item below it is
                int safeDurib = QualityArmory.findSafeSpot(e.getPlayer().getInventory().getItemInOffHand(), true,
                        QAMain.overrideURL) + (QAMain.overrideURL ? 0 : 3);

                // if (e.getItem().getDurability() == 1) {
                QAMain.DEBUG("Safe Durib= " + (safeDurib) + "! ORG "
                        + e.getPlayer().getInventory().getItemInOffHand().getDurability());
                ItemStack is = e.getPlayer().getInventory().getItemInOffHand();
                is.setDurability((short) (safeDurib));
                is = ItemFact.addCalulatedExtraDurib(is,
                        safeDurib - e.getPlayer().getInventory().getItemInOffHand().getDurability());
                e.getPlayer().getInventory().setItemInOffHand(is);
                // }
            }
        }

    }

    @SuppressWarnings({ "deprecation" })
    @EventHandler
    public void onClick(final PlayerInteractEvent e) {
        QAMain.DEBUG("InteractEvent Called");
        // Quick bugfix for specifically this item.

        try {
            if (QAMain.ITEM_enableUnbreakable && !QAMain.ignoreUnbreaking)
                if (QualityArmory.isCustomItem(e.getPlayer().getItemInHand())) {
                    if (!e.getPlayer().getItemInHand().getItemMeta().spigot().isUnbreakable()) {
                        QAMain.DEBUG("A player is using a breakable item that reached being a gun!");
                        // If the item is not a gun, but the item below it is
                        int safeDurib = QualityArmory.findSafeSpot(e.getPlayer().getItemInHand(), true, QAMain.overrideURL)
                                + (QAMain.overrideURL ? 0 : 3);

                        // if (e.getItem().getDurability() == 1) {
                        QAMain.DEBUG("Safe Durib= " + (safeDurib) + "! ORG "
                                + e.getPlayer().getItemInHand().getDurability());
                        ItemStack is = e.getPlayer().getItemInHand();
                        is.setDurability((short) (safeDurib));
                        e.getPlayer().setItemInHand(is);
                    }
                }
        } catch (Error | Exception e45) {
        }

        try {
            if (QAMain.ITEM_enableUnbreakable && !QAMain.ignoreUnbreaking)
                if (QualityArmory.isCustomItem(e.getPlayer().getInventory().getItemInOffHand())) {
                    if (!e.getPlayer().getInventory().getItemInOffHand().getItemMeta().spigot().isUnbreakable()) {
                        QAMain.DEBUG("A player is using a breakable item that reached being a gun!");
                        // If the item is not a gun, but the item below it is
                        int safeDurib = QualityArmory.findSafeSpot(e.getPlayer().getInventory().getItemInOffHand(),
                                true, QAMain.overrideURL) + (QAMain.overrideURL ? 0 : 3);

                        // if (e.getItem().getDurability() == 1) {
                        QAMain.DEBUG("Safe Durib= " + (safeDurib) + "! ORG "
                                + e.getPlayer().getInventory().getItemInOffHand().getDurability());
                        ItemStack is = e.getPlayer().getInventory().getItemInOffHand();
                        is.setDurability((short) (safeDurib));
                        e.getPlayer().getInventory().setItemInOffHand(is);
                    }
                }
        } catch (Error | Exception e45) {
        }

        if (!QualityArmory.isCustomItem(e.getPlayer().getItemInHand())) {
            ItemStack offhand = Update19OffhandChecker.getItemStackOFfhand(e.getPlayer());
            if (offhand == null || !QualityArmory.isCustomItem(offhand))
                return;
        }

        if (QAMain.kickIfDeniedRequest && QAMain.sentResourcepack.containsKey(e.getPlayer().getUniqueId())
                && System.currentTimeMillis() - QAMain.sentResourcepack.get(e.getPlayer().getUniqueId()) >= 3000) {
            // the player did not accept resourcepack, and got away with it
            e.setCancelled(true);
            e.getPlayer().kickPlayer(QAMain.S_KICKED_FOR_RESOURCEPACK);
            return;
        }

        if (e.getItem() != null) {
            final ItemStack origin = e.getItem();
            final int slot = e.getPlayer().getInventory().getHeldItemSlot();
            if (!QAMain.isVersionHigherThan(1, 9)) {
                ItemStack temp1 = null;
                try {
                    temp1 = e.getPlayer().getInventory().getItemInOffHand();
                } catch (Error | Exception re453) {
                }
                final ItemStack temp2 = temp1;

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (origin.getDurability() != e.getPlayer().getItemInHand().getDurability()
                                && slot == e.getPlayer().getInventory().getHeldItemSlot()
                                && (e.getPlayer().getItemInHand() != null
                                && e.getPlayer().getItemInHand().getType() == origin.getType())) {
                            try {
                                if (QualityArmory.isIronSights(e.getPlayer().getItemInHand())
                                        && origin.getDurability() == e.getPlayer().getInventory().getItemInOffHand()
                                        .getDurability())
                                    return;
                                if (temp2 != null
                                        && temp2.getDurability() == e.getPlayer().getItemInHand().getDurability())
                                    return;
                            } catch (Error | Exception re54) {
                            }
                            e.getPlayer().setItemInHand(origin);
                            QAMain.DEBUG("The item in the player's hand changed! Origin " + origin.getDurability() + " New "
                                    + e.getPlayer().getItemInHand().getDurability());
                        }

                    }
                }.runTaskLater(QAMain.getInstance(), 0);
            }

            ItemStack usedItem = e.getPlayer().getItemInHand();

            try {
                if (QAMain.AutoDetectResourcepackVersion && !QAMain.MANUALLYSELECT18) {
                    if (us.myles.ViaVersion.bukkit.util.ProtocolSupportUtil.getProtocolVersion(e.getPlayer()) < QAMain.ID18) {
                        Gun g = QualityArmory.getGun(usedItem);
                        if (g == null)
                            g = QAMain.gunRegister.get(QualityArmory.getGunWithAttchments(usedItem).getBase());

                        if (!g.is18Support()) {
                            for (Gun g2 : QAMain.gunRegister.values()) {
                                if (g2.is18Support()) {
                                    if (g2.getDisplayName().equals(g.getDisplayName())) {
                                        e.getPlayer().setItemInHand(ItemFact.getGun(g2));
                                        QAMain.DEBUG("Custom-validation check 1");
                                        return;
                                    }
                                }
                            }
                            // If there is no exact match for 1.8, get the closest gun that uses the same
                            // ammo type.
                            for (Gun g2 : QAMain.gunRegister.values()) {
                                if (g2.is18Support()) {
                                    if (g2.getAmmoType().equals(g.getAmmoType())) {
                                        e.getPlayer().setItemInHand(ItemFact.getGun(g2));
                                        QAMain.DEBUG("Custom-validation check 2");
                                        return;
                                    }
                                }
                            }
                        }
                    } else {
                        if (us.myles.ViaVersion.bukkit.util.ProtocolSupportUtil
                                .getProtocolVersion(e.getPlayer()) >= QAMain.ID18) {
                            Gun g = QualityArmory.getGun(usedItem);
                            if (g == null)
                                g = QAMain.gunRegister.get(QualityArmory.getGunWithAttchments(usedItem).getBase());
                            if (g.is18Support()) {
                                for (Gun g2 : QAMain.gunRegister.values()) {
                                    if (!g2.is18Support()) {
                                        if (g2.getDisplayName().equals(g.getDisplayName())) {
                                            e.getPlayer().setItemInHand(ItemFact.getGun(g2));
                                            QAMain.DEBUG("Custom-validation check 3");
                                            return;
                                        }
                                    }
                                }
                                // If there is no exact match for 1.8, get the closest gun that uses the same
                                // ammo type.
                                for (Gun g2 : QAMain.gunRegister.values()) {
                                    if (!g2.is18Support()) {
                                        if (g2.getAmmoType().equals(g.getAmmoType())) {
                                            e.getPlayer().setItemInHand(ItemFact.getGun(g2));
                                            QAMain.DEBUG("Custom-validation check 4");
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Error | Exception e4) {
            }

            /*
             * try {
             *
             * if (us.myles.ViaVersion.bukkit.util.ProtocolSupportUtil.getProtocolVersion(e.
             * getPlayer()) > ID18) { if (isIS(usedItem)) { try { usedItem =
             * e.getPlayer().getInventory().getItemInOffHand(); } catch (Error | Exception
             * e4) { } } if (getCustomItem(usedItem).is18Support()) { return; } } } catch
             * (Error | Exception e3) { }
             */

            // Sedn the resourcepack if the player does not have it.
            if (QAMain.shouldSend && !QAMain.resourcepackReq.contains(e.getPlayer().getUniqueId())) {
                QAMain.DEBUG("Player does not have resourcepack!");
                QualityArmory.sendResourcepack(e.getPlayer(), true);
            }

            if (QualityArmory.isIronSights(usedItem)) {
                if (Update19OffhandChecker.supportOffhand(e.getPlayer())) {
                    try {
                        if ((e.getAction() == Action.RIGHT_CLICK_AIR
                                || e.getAction() == Action.RIGHT_CLICK_BLOCK) == (QAMain.SWAP_RMB_WITH_LMB)) {
                            e.setCancelled(true);
                            Gun g = QualityArmory.getGun(Update19OffhandChecker.getItemStackOFfhand(e.getPlayer()));
                            if (!QAMain.enableIronSightsON_RIGHT_CLICK) {
                                if (!e.getPlayer().isSneaking() || (g != null && !g.isAutomatic())) {
                                    QAMain.DEBUG("Swapping " + g.getName() + " from offhand to main hand to reload!");
                                    e.getPlayer().getInventory()
                                            .setItemInMainHand(e.getPlayer().getInventory().getItemInOffHand());
                                    e.getPlayer().getInventory().setItemInOffHand(null);
                                    usedItem = e.getPlayer().getInventory().getItemInMainHand();
                                }
                            } else {
                                QAMain.DEBUG("Swapping " + g.getName() + " from offhand to main hand!");
                                e.getPlayer().getInventory()
                                        .setItemInMainHand(e.getPlayer().getInventory().getItemInOffHand());
                                e.getPlayer().getInventory().setItemInOffHand(null);
                                QAMain.toggleNightvision(e.getPlayer(), g, false);
                            }
                        }
                    } catch (Error e2) {
                    }
                }
                if (((e.getAction() == Action.LEFT_CLICK_AIR
                        || e.getAction() == Action.LEFT_CLICK_BLOCK) == QAMain.SWAP_RMB_WITH_LMB)) {
                    usedItem = e.getPlayer().getInventory().getItemInOffHand();
                }
            }

            ArmoryBaseObject qaItem = QualityArmory.getCustomItem(usedItem);
            if (qaItem != null) {
                QAMain.DEBUG(qaItem.getClass().getName() + " item is being used!");
                if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    qaItem.onLMB(e, usedItem);
                } else {
                    qaItem.onRMB(e, usedItem);
                }
            }
        }
    }

    @EventHandler
    public void swap(PlayerItemHeldEvent e) {
        if (e.isCancelled())
            return;
        ItemStack prev = e.getPlayer().getInventory().getItem(e.getPreviousSlot());
        if (QualityArmory.isIronSights(prev)) {
            try {
                e.getPlayer().getInventory().setItem(e.getPreviousSlot(),
                        e.getPlayer().getInventory().getItemInOffHand());
                e.getPlayer().getInventory().setItemInOffHand(null);
            } catch (Error e2) {
            }
        }
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent e) {
        QAMain.resourcepackReq.remove(e.getPlayer().getUniqueId());
        if (QAMain.reloadingTasks.containsKey(e.getPlayer().getUniqueId())) {
            for (BukkitTask r : QAMain.reloadingTasks.get(e.getPlayer().getUniqueId())) {
                r.cancel();
            }
        }
        QAMain.reloadingTasks.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent e) {
        if (/* Bukkit.getVersion().contains("1.8") || */ Bukkit.getVersion().contains("1.7")) {
            Bukkit.broadcastMessage(
                    QAMain.prefix + " QualityArmory does not support versions older than 1.9, and may crash clients");
            Bukkit.broadcastMessage(
                    "Since there is no reason to stay on outdated updates, (1.7 and 1.8 has quite a number of exploits) update your server.");
            if (QAMain.shouldSend) {
                QAMain.shouldSend = false;
                Bukkit.broadcastMessage(QAMain.prefix + ChatColor.RED + " Disabling resourcepack.");
            }
        }
        /*
         * if (guntype == Material.DIAMOND_HOE && !hideTextureWarnings) {
         * Bukkit.broadcastMessage(prefix +
         * " QA is now moving all items to a new Diamond_Axe system to prevent conflicts with other plugins."
         * ); Bukkit.broadcastMessage(prefix +
         * " Please delete the \"gunMaterialType\"value in the config or hide these warnings by setting \"hideTextureWarnings\" to true."
         * ); }
         */
        if (QAMain.addGlowEffects) {
            new BukkitRunnable() {

                @Override
                public void run() {
                    if (e.getPlayer().getScoreboard() != null
                            && !QAMain.coloredGunScoreboard.contains(e.getPlayer().getScoreboard())) {
                        QAMain.coloredGunScoreboard.add(QAMain.getInstance().registerGlowTeams(e.getPlayer().getScoreboard()));
                    }
                }
            }.runTaskLater(QAMain.getInstance(), 20 * 15);
        }

        if (QAMain.sendOnJoin) {
            QualityArmory.sendResourcepack(e.getPlayer(), QAMain.sendTitleOnJoin);
        } else {
            for (ItemStack i : e.getPlayer().getInventory().getContents()) {
                if (i != null && (QualityArmory.isGun(i) || QualityArmory.isAmmo(i) || QualityArmory.isMisc(i))) {
                    if (QAMain.shouldSend && !QAMain.resourcepackReq.contains(e.getPlayer().getUniqueId())) {
                        new BukkitRunnable() {

                            @Override
                            public void run() {
                                QualityArmory.sendResourcepack(e.getPlayer(), false);
                            }

                        }.runTaskLater(QAMain.getInstance(), 0);
                    }
                    break;
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    public void oninvClick(final InventoryClickEvent e) {
        if (e.isCancelled())
            return;
        String name = null;
        try {
            if (e.getClickedInventory() != null)
                name = e.getClickedInventory().getTitle();
        } catch (Error | Exception e4) {
            if (e.getInventory() != null)
                name = e.getInventory().getTitle();
        }

        if (name != null && (name.startsWith(QAMain.S_craftingBenchName) || name.startsWith(QAMain.S_shopName))) {
            QAMain.DEBUG("ClickedShop");

            boolean shop = (name.startsWith(QAMain.S_shopName));

            e.setCancelled(true);

            if (shop) {

                if (!QAMain.enableEconomy) {
                    e.getWhoClicked().closeInventory();
                    e.getWhoClicked().sendMessage(QAMain.prefix + QAMain.S_noEcon);
                    return;
                }

            }

            if (e.getCurrentItem() != null) {
                if (shop) {
                    if (e.getCurrentItem().isSimilar(QAMain.prevButton)) {
                        int page = Integer.parseInt(e.getInventory().getTitle().split(QAMain.S_shopName)[1]) - 1;
                        e.getWhoClicked().closeInventory();
                        e.getWhoClicked().openInventory(QAMain.createShop(Math.max(0, page)));
                        QAMain.DEBUG("Prev_Shop");
                        return;
                    }
                    if (e.getCurrentItem().isSimilar(QAMain.nextButton)) {
                        int page = Integer.parseInt(e.getInventory().getTitle().split(QAMain.S_shopName)[1]) + 1;
                        e.getWhoClicked().closeInventory();
                        e.getWhoClicked().openInventory(QAMain.createShop(Math.min(QualityArmory.getMaxPages(), page)));
                        QAMain.DEBUG("next_Shop");
                        return;
                    }
                } else {
                    if (e.getCurrentItem().isSimilar(QAMain.prevButton)) {
                        int page = Integer.parseInt(e.getInventory().getTitle().split(QAMain.S_craftingBenchName)[1]) - 1;
                        e.getWhoClicked().closeInventory();
                        e.getWhoClicked().openInventory(QAMain.createCraft(Math.max(0, page)));
                        QAMain.DEBUG("Prev_craft");
                        return;
                    }
                    if (e.getCurrentItem().isSimilar(QAMain.nextButton)) {
                        int page = Integer.parseInt(e.getInventory().getTitle().split(QAMain.S_craftingBenchName)[1]) + 1;
                        e.getWhoClicked().closeInventory();
                        e.getWhoClicked().openInventory(QAMain.createCraft(Math.min(QualityArmory.getMaxPages(), page)));
                        QAMain.DEBUG("next_craft");
                        return;
                    }
                }

                if (QualityArmory.isGun(e.getCurrentItem())) {
                    Gun g = QualityArmory.getGun(e.getCurrentItem());
                    if ((shop && EconHandler.hasEnough(g, (Player) e.getWhoClicked()))
                            || (!shop && QAMain.getInstance().lookForIngre((Player) e.getWhoClicked(), g))
                            || e.getWhoClicked().getGameMode() == GameMode.CREATIVE) {
                        if (shop) {
                            EconHandler.pay(g, (Player) e.getWhoClicked());
                            e.getWhoClicked().sendMessage(
                                    QAMain.S_BUYCONFIRM.replaceAll("%item%", ChatColor.stripColor(g.getDisplayName()))
                                            .replaceAll("%cost%", "" + g.cost()));
                        } else
                            QAMain.getInstance().removeForIngre((Player) e.getWhoClicked(), g);
                        ItemStack s = ItemFact.getGun(g);
                        s.setAmount(g.getCraftingReturn());
                        e.getWhoClicked().getInventory().addItem(s);
                        QAMain.getInstance().shopsSounds(e, shop);
                        QAMain.DEBUG("Buy-gun");
                    } else {
                        QAMain.DEBUG("Failed to buy/craft gun");
                        e.getWhoClicked().closeInventory();
                        if (shop)
                            e.getWhoClicked().sendMessage(QAMain.prefix + QAMain.S_noMoney);
                        else
                            e.getWhoClicked().sendMessage(QAMain.prefix + QAMain.S_missingIngredients);
                        try {
                            ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(),
                                    Sound.BLOCK_ANVIL_BREAK, 1, 1);
                        } catch (Error e2) {
                            ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(),
                                    Sound.valueOf("ANVIL_BREAK"), 1, 1);
                        }
                    }
                } /*
                 * else if (QualityArmory.isGunWithAttchments(e.getCurrentItem())) {
                 * AttachmentBase g = QualityArmory.getGunWithAttchments(e.getCurrentItem());
                 * Gun g2 = gunRegister.get(g.getBase()); if ((shop && EconHandler.hasEnough(g2,
                 * (Player) e.getWhoClicked())) || (!shop && lookForIngre((Player)
                 * e.getWhoClicked(), g)) || e.getWhoClicked().getGameMode() ==
                 * GameMode.CREATIVE) { if (shop) { EconHandler.pay(g2, (Player)
                 * e.getWhoClicked()); e.getWhoClicked().sendMessage(
                 * S_BUYCONFIRM.replaceAll("%item%", ChatColor.stripColor(g.getDisplayName()))
                 * .replaceAll("%cost%", "" + g2.cost())); } else removeForIngre((Player)
                 * e.getWhoClicked(), g); ItemStack s = ItemFact.getGun(g);
                 * s.setAmount(g2.getCraftingReturn());
                 * e.getWhoClicked().getInventory().addItem(s); shopsSounds(e, shop);
                 * DEBUG("Buy-attachment"); } else { DEBUG("Failed to buy/craft attachment");
                 * e.getWhoClicked().closeInventory(); if (shop)
                 * e.getWhoClicked().sendMessage(prefix + S_noMoney); else
                 * e.getWhoClicked().sendMessage(prefix + S_missingIngredients); try { ((Player)
                 * e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(),
                 * Sound.BLOCK_ANVIL_BREAK, 1, 1); } catch (Error e2) { ((Player)
                 * e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(),
                 * Sound.valueOf("ANVIL_BREAK"), 1, 1); } } }
                 */else if (QualityArmory.isAmmo(e.getCurrentItem())) {
                    Ammo g = QualityArmory.getAmmo(e.getCurrentItem());
                    if ((shop && EconHandler.hasEnough(g, (Player) e.getWhoClicked()))
                            || (!shop && QAMain.getInstance().lookForIngre((Player) e.getWhoClicked(), g))
                            || e.getWhoClicked().getGameMode() == GameMode.CREATIVE) {
                        if (shop) {
                            e.getWhoClicked().sendMessage(
                                    QAMain. S_BUYCONFIRM.replaceAll("%item%", ChatColor.stripColor(g.getDisplayName()))
                                            .replaceAll("%cost%", "" + g.cost()));
                            EconHandler.pay(g, (Player) e.getWhoClicked());
                        } else
                            QAMain.getInstance().removeForIngre((Player) e.getWhoClicked(), g);
                        AmmoUtil.addAmmo((Player) e.getWhoClicked(), g, g.getCraftingReturn());
                        QAMain.getInstance().shopsSounds(e, shop);
                        QAMain.DEBUG("Buy-ammo");
                    } else {
                        e.getWhoClicked().closeInventory();
                        QAMain.DEBUG("Failed to buy/craft ammo");
                        if (shop)
                            e.getWhoClicked().sendMessage(QAMain.prefix + QAMain.S_noMoney);
                        else
                            e.getWhoClicked().sendMessage(QAMain.prefix + QAMain.S_missingIngredients);
                        try {
                            ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(),
                                    Sound.BLOCK_ANVIL_BREAK, 1, 1);
                        } catch (Error e2) {
                            ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(),
                                    Sound.valueOf("ANVIL_BREAK"), 1, 1);
                        }
                    }
                } else if (QualityArmory.isMisc(e.getCurrentItem())) {
                    ArmoryBaseObject g = QualityArmory.getMisc(e.getCurrentItem());
                    if ((shop && EconHandler.hasEnough(g, (Player) e.getWhoClicked()))
                            || (!shop && QAMain.getInstance().lookForIngre((Player) e.getWhoClicked(), g))
                            || e.getWhoClicked().getGameMode() == GameMode.CREATIVE) {
                        if (shop) {
                            EconHandler.pay(g, (Player) e.getWhoClicked());
                            e.getWhoClicked().sendMessage(
                                    QAMain.S_BUYCONFIRM.replaceAll("%item%", ChatColor.stripColor(g.getDisplayName()))
                                            .replaceAll("%cost%", "" + g.cost()));
                        } else
                            QAMain.getInstance().removeForIngre((Player) e.getWhoClicked(), g);
                        ItemStack s = ItemFact.getObject(g);
                        s.setAmount(g.getCraftingReturn());
                        e.getWhoClicked().getInventory().addItem(s);
                        QAMain.getInstance().shopsSounds(e, shop);
                        QAMain.DEBUG("Buy-Misc");
                    } else {
                        QAMain.DEBUG("Failed to buy/craft misc");
                        e.getWhoClicked().closeInventory();
                        if (shop)
                            e.getWhoClicked().sendMessage(QAMain.prefix + QAMain.S_noMoney);
                        else
                            e.getWhoClicked().sendMessage(QAMain.prefix + QAMain.S_missingIngredients);
                        try {
                            ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(),
                                    Sound.BLOCK_ANVIL_BREAK, 1, 1);
                        } catch (Error e2) {
                            ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(),
                                    Sound.valueOf("ANVIL_BREAK"), 1, 1);
                        }
                    }
                } else if (QualityArmory.isArmor(e.getCurrentItem())) {
                    {
                        ArmorObject g = QualityArmory.getArmor(e.getCurrentItem());
                        if ((shop && EconHandler.hasEnough(g, (Player) e.getWhoClicked()))
                                || (!shop && QAMain.getInstance().lookForIngre((Player) e.getWhoClicked(), g))
                                || e.getWhoClicked().getGameMode() == GameMode.CREATIVE) {
                            if (shop) {
                                e.getWhoClicked()
                                        .sendMessage(QAMain.S_BUYCONFIRM
                                                .replaceAll("%item%", ChatColor.stripColor(g.getDisplayName()))
                                                .replaceAll("%cost%", "" + g.cost()));
                                EconHandler.pay(g, (Player) e.getWhoClicked());
                            } else
                                QAMain.getInstance().removeForIngre((Player) e.getWhoClicked(), g);
                            ItemStack s = ItemFact.getArmor(g);
                            s.setAmount(g.getCraftingReturn());
                            e.getWhoClicked().getInventory().addItem(s);
                            QAMain.getInstance().shopsSounds(e, shop);
                            QAMain.DEBUG("Buy-armor");
                        } else {
                            QAMain.DEBUG("Failed to buy/craft armor");
                            e.getWhoClicked().closeInventory();
                            if (shop)
                                e.getWhoClicked().sendMessage(QAMain.prefix + QAMain.S_noMoney);
                            else
                                e.getWhoClicked().sendMessage(QAMain.prefix + QAMain.S_missingIngredients);
                            try {
                                ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(),
                                        Sound.BLOCK_ANVIL_BREAK, 1, 1);
                            } catch (Error e2) {
                                ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(),
                                        Sound.valueOf("ANVIL_BREAK"), 1, 1);
                            }
                        }
                    }
                } else {
                    e.setCancelled(true);
                }
            }
            return;
        }

        // player inv

        if ((e.getCurrentItem() != null && QualityArmory.isIronSights(e.getCurrentItem()))) {
            e.setCancelled(true);
            return;
        }
        try {
            if (e.getSlot() == 40 && e.getClickedInventory() == e.getWhoClicked().getInventory()
                    && QualityArmory.isGun(e.getClickedInventory().getItem(40))) {
                e.setCancelled(true);
            }
        } catch (Error | Exception r5) {
            if (e.getSlot() == 40 && e.getInventory() == e.getWhoClicked().getInventory()
                    && QualityArmory.isGun(e.getInventory().getItem(40))) {
                e.setCancelled(true);
            }

        }

        if (QualityArmory.isAmmo(e.getCurrentItem())) {
            Ammo current = QualityArmory.getAmmo(e.getCurrentItem());
            if (e.getCursor() == null) {
                e.setCursor(e.getCurrentItem());
                e.setCurrentItem(null);
                QAMain.DEBUG("Clicked ammo: Swap: " + e.getSlot());
            } else if (QualityArmory.isAmmo(e.getCursor())) {
                Ammo cursor = QualityArmory.getAmmo(e.getCursor());
                if (current == cursor) {
                    if (e.getCurrentItem().getAmount() < current.getMaxAmount()) {
                        e.setCancelled(true);
                        ItemStack tempCur = e.getCurrentItem();
                        if (e.isLeftClick()) {
                            int required = current.getMaxAmount() - e.getCurrentItem().getAmount();
                            if (required <= e.getCursor().getAmount()) {
                                tempCur.setAmount(current.getMaxAmount());
                                ItemStack tempCurs = e.getCursor();
                                if (required == e.getCursor().getAmount()) {
                                    tempCurs = null;
                                } else {
                                    tempCurs.setAmount(e.getCursor().getAmount() - required);
                                }
                                e.getClickedInventory().setItem(e.getSlot(), tempCur);
                                e.getWhoClicked().setItemOnCursor(tempCurs);
                                QAMain.DEBUG("Clicked ammo: Half-Merger: " + e.getSlot());
                            } else {
                                tempCur.setAmount(tempCur.getAmount() + e.getCursor().getAmount());
                                e.getClickedInventory().setItem(e.getSlot(), tempCur);
                                e.getWhoClicked().setItemOnCursor(null);
                                QAMain.DEBUG("Clicked ammo: Full-Merger: " + e.getSlot());
                            }
                        } else if (e.isRightClick()) {
                            ItemStack tempCurs = e.getCursor();
                            tempCur.setAmount(tempCur.getAmount() + 1);
                            if (tempCurs.getAmount() == 1)
                                tempCurs = null;
                            else
                                tempCurs.setAmount(tempCurs.getAmount() - 1);
                            e.getClickedInventory().setItem(e.getSlot(), tempCur);
                            e.getWhoClicked().setItemOnCursor(tempCurs);
                            QAMain.DEBUG("Clicked ammo: Right click-Merger: " + e.getSlot());
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPickup(PlayerPickupItemEvent e) {
        if (e.isCancelled())
            return;
        if (QualityArmory.isCustomItem(e.getItem().getItemStack())) {
            if (QAMain.shouldSend && !QAMain.namesToBypass.contains(e.getPlayer().getName())
                    && !QAMain.resourcepackReq.contains(e.getPlayer().getUniqueId())) {
                QualityArmory.sendResourcepack(e.getPlayer(), true);
            }

            if (QualityArmory.isGun(e.getItem().getItemStack())) {
                Gun g = QualityArmory.getGun(e.getItem().getItemStack());
                try {
                    if (QAMain.AutoDetectResourcepackVersion && !QAMain.MANUALLYSELECT18) {
                        if (us.myles.ViaVersion.bukkit.util.ProtocolSupportUtil
                                .getProtocolVersion(e.getPlayer()) < QAMain.ID18) {
                            if (g == null)
                                g = QAMain.gunRegister
                                        .get(QualityArmory.getGunWithAttchments(e.getItem().getItemStack()).getBase());

                            if (!g.is18Support()) {
                                for (Gun g2 : QAMain.gunRegister.values()) {
                                    if (g2.is18Support()) {
                                        if (g2.getDisplayName().equals(g.getDisplayName())) {
                                            e.getItem().setItemStack(ItemFact.getGun(g2));
                                            QAMain.DEBUG("Custom-validation check 1");
                                            return;
                                        }
                                    }
                                }
                                // If there is no exact match for 1.8, get the closest gun that uses the same
                                // ammo type.
                                for (Gun g2 : QAMain.gunRegister.values()) {
                                    if (g2.is18Support()) {
                                        if (g2.getAmmoType().equals(g.getAmmoType())) {
                                            e.getItem().setItemStack(ItemFact.getGun(g2));
                                            QAMain.DEBUG("Custom-validation check 2");
                                            return;
                                        }
                                    }
                                }
                            }
                        } else {
                            if (us.myles.ViaVersion.bukkit.util.ProtocolSupportUtil
                                    .getProtocolVersion(e.getPlayer()) >= QAMain.ID18) {
                                if (g == null)
                                    g = QAMain.gunRegister.get(
                                            QualityArmory.getGunWithAttchments(e.getItem().getItemStack()).getBase());
                                if (g.is18Support()) {
                                    for (Gun g2 : QAMain.gunRegister.values()) {
                                        if (!g2.is18Support()) {
                                            if (g2.getDisplayName().equals(g.getDisplayName())) {
                                                e.getItem().setItemStack(ItemFact.getGun(g2));
                                                QAMain.DEBUG("Custom-validation check 3");
                                                return;
                                            }
                                        }
                                    }
                                    // If there is no exact match for 1.8, get the closest gun that uses the same
                                    // ammo type.
                                    for (Gun g2 : QAMain.gunRegister.values()) {
                                        if (!g2.is18Support()) {
                                            if (g2.getAmmoType().equals(g.getAmmoType())) {
                                                e.getItem().setItemStack(ItemFact.getGun(g2));
                                                QAMain.DEBUG("Custom-validation check 4");
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Error | Exception e4) {
                }
                QAMain.getInstance().checkforDups(e.getPlayer(), e.getItem().getItemStack());

                if (QAMain.enablePrimaryWeaponHandler)
                    if (QualityArmory.isOverLimitForPrimaryWeapons(g, e.getPlayer()))
                        e.setCancelled(true);
                return;
            }

            if (QualityArmory.isAmmo(e.getItem().getItemStack())) {
                AmmoUtil.addAmmo(e.getPlayer(), QAMain.ammoRegister.get(MaterialStorage.getMS(e.getItem().getItemStack())),
                        e.getItem().getItemStack().getAmount());
                e.setCancelled(true);
                e.getItem().remove();
                try {
                    e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.2f, 1);
                } catch (Error e2) {
                    e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.valueOf("CLICK"), 0.2f, 1);
                }
            }
        }
    }

}
