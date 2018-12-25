package me.zombie_striker.qg;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class MaterialStorage {

	private static List<MaterialStorage> store = new ArrayList<>();

	public static MaterialStorage getMS(Material m, int d, int var) {
		return getMS(m, d, var, null);
	}

	public static MaterialStorage getMS(Material m, int d, int var, String extraValue) {
		return getMS(m, d, var, extraValue, null);
	}

	public static MaterialStorage getMS(Material m, int d, int var, String extraValue, String ev2) {
		for (MaterialStorage k : store) {
			if (matchesMaterials(k, m, d) && matchVarients(k, var) && matchHeads(k, extraValue, ev2))
				return k;
		}
		MaterialStorage mm = new MaterialStorage(m, d, var, extraValue, ev2);
		store.add(mm);
		return mm;
	}

	private static boolean matchesMaterials(MaterialStorage k, Material m, int d) {
		return (k.m == m && (k.d == d || k.d == -1));
	}

	public static boolean matchVarients(MaterialStorage k, int var) {
		return (k.varient == var || var == -1);
	}

	public static boolean matchHeads(MaterialStorage k, String ex1, String ex2) {
		boolean exb1 = (!k.hasSpecialValue() || k.hasSpecialValue2()
				|| (ex1 != null && (ex1.equals("-1") || k.getSpecialValue().equals(ex1))));
		boolean exb2 = (!k.hasSpecialValue2()
				|| (ex2 != null && (ex2.equals("-1") || k.getSpecialValue2().equals(ex2))));
		return exb1 && exb2;
	}

	@SuppressWarnings("deprecation")
	public static MaterialStorage getMS(ItemStack is) {
		Material skull_Compare = null;
		try {
			skull_Compare = Material.valueOf("SKULL_ITEM");
		} catch (Error | Exception e45) {
		}
		if (skull_Compare == null) {
			skull_Compare = Material.matchMaterial("PLAYER_HEAD");
		}
		return getMS(is.getType(), is.getDurability(), getVarient(is),
				is.getType() == skull_Compare ? ((SkullMeta) is.getItemMeta()).getOwner() : null);
	}

	private int d;
	private Material m;
	private int varient = 0;
	private String specialValues = null;
	private String specialValues2 = null;

	public int getData() {
		return d;
	}

	public boolean hasSpecialValue() {
		return specialValues != null;
	}

	public String getSpecialValue() {
		return specialValues;
	}

	public void setSpecialValue(String s) {
		this.specialValues = s;
	}

	public boolean hasSpecialValue2() {
		return specialValues2 != null;
	}

	public String getSpecialValue2() {
		return specialValues2;
	}

	public void setSpecialValue2(String s) {
		this.specialValues2 = s;
	}

	public Material getMat() {
		return m;
	}

	private MaterialStorage(Material m, int d) {
		this.m = m;
		this.d = d;
	}

	private MaterialStorage(Material m, int d, int var) {
		this(m, d, var, null);
	}

	private MaterialStorage(Material m, int d, int var, String extraData) {
		this(m, d, var, extraData, null);
	}

	private MaterialStorage(Material m, int d, int var, String extraData, String ed2) {
		this.m = m;
		this.d = d;
		this.varient = var;
		this.specialValues = extraData;
		this.specialValues2 = ed2;
	}

	public boolean isVarient() {
		return varient > 0;
	}

	public int getVarient() {
		return varient;
	}

	public static int getVarient(ItemStack is) {
		if (is != null)
			if (is.hasItemMeta() && is.getItemMeta().hasLore()) {
				for (String lore : is.getItemMeta().getLore()) {
					if (lore.startsWith(QAMain.S_ITEM_VARIENTS_NEW)) {
						try {
							int id = Integer.parseInt(lore.split(QAMain.S_ITEM_VARIENTS_NEW)[1].trim());
							return id;
						} catch (Error | Exception e4) {
							e4.printStackTrace();
							return 0;
						}
					} else if (lore.startsWith(QAMain.S_ITEM_VARIENTS_LEGACY)) {
						try {
							int id = Integer.parseInt(lore.split(QAMain.S_ITEM_VARIENTS_LEGACY)[1].trim());
							return id;
						} catch (Error | Exception e4) {
							e4.printStackTrace();
							return 0;
						}
					}
				}
			}
		return 0;
	}
}
