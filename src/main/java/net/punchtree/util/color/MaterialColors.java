package net.punchtree.util.color;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;

public class MaterialColors {

	public static class MaterialColor extends PunchTreeColor {
		private static final long serialVersionUID = 1L;
		
		public final Material material;
		
		private MaterialColor(int r, int g, int b, Material material) {
			super(r,g,b);
			this.material = material;
		}
	}
	
	public static final MaterialColor CONCRETE_WHITE = new MaterialColor(207,213,214, Material.WHITE_CONCRETE);
	public static final MaterialColor CONCRETE_ORANGE = new MaterialColor(224,97,1, Material.ORANGE_CONCRETE);
	public static final MaterialColor CONCRETE_MAGENTA = new MaterialColor(169,48,159, Material.MAGENTA_CONCRETE);
	public static final MaterialColor CONCRETE_LIGHT_BLUE = new MaterialColor(36,136,199, Material.LIGHT_BLUE_CONCRETE);
	public static final MaterialColor CONCRETE_YELLOW = new MaterialColor(241,175,21, Material.YELLOW_CONCRETE);
	public static final MaterialColor CONCRETE_LIME = new MaterialColor(94,169,25, Material.LIME_CONCRETE);
	public static final MaterialColor CONCRETE_PINK = new MaterialColor(214,101,143, Material.PINK_CONCRETE);
	public static final MaterialColor CONCRETE_DARK_GRAY = new MaterialColor(55,58,62, Material.GRAY_CONCRETE);
	public static final MaterialColor CONCRETE_LIGHT_GRAY = new MaterialColor(125,125,115, Material.LIGHT_GRAY_CONCRETE);
	public static final MaterialColor CONCRETE_CYAN = new MaterialColor(21,119,136, Material.CYAN_CONCRETE);
	public static final MaterialColor CONCRETE_PURPLE = new MaterialColor(100,32,156, Material.PURPLE_CONCRETE);
	public static final MaterialColor CONCRETE_BLUE = new MaterialColor(45,47,143, Material.BLUE_CONCRETE);
	public static final MaterialColor CONCRETE_BROWN = new MaterialColor(96,60,32, Material.BROWN_CONCRETE);
	public static final MaterialColor CONCRETE_GREEN = new MaterialColor(73,91,36, Material.GREEN_CONCRETE);
	public static final MaterialColor CONCRETE_RED = new MaterialColor(142,33,33, Material.RED_CONCRETE);
	public static final MaterialColor CONCRETE_BLACK = new MaterialColor(8,10,15, Material.BLACK_CONCRETE);
	
	private static final Map<String, MaterialColor> concretes;
	static{
		concretes = new HashMap<>();
		concretes.put("CONCRETE_WHITE", CONCRETE_WHITE);
		concretes.put("CONCRETE_ORANGE", CONCRETE_ORANGE);
		concretes.put("CONCRETE_MAGENTA", CONCRETE_MAGENTA);
		concretes.put("CONCRETE_LIGHT_BLUE", CONCRETE_LIGHT_BLUE);
		concretes.put("CONCRETE_YELLOW", CONCRETE_YELLOW);
		concretes.put("CONCRETE_LIME", CONCRETE_LIME);
		concretes.put("CONCRETE_PINK", CONCRETE_PINK);
		concretes.put("CONCRETE_DARK_GRAY", CONCRETE_DARK_GRAY);
		concretes.put("CONCRETE_LIGHT_GRAY", CONCRETE_LIGHT_GRAY);
		concretes.put("CONCRETE_CYAN", CONCRETE_CYAN);
		concretes.put("CONCRETE_PURPLE", CONCRETE_PURPLE);
		concretes.put("CONCRETE_BLUE", CONCRETE_BLUE);
		concretes.put("CONCRETE_BROWN", CONCRETE_BROWN);
		concretes.put("CONCRETE_GREEN", CONCRETE_GREEN);
		concretes.put("CONCRETE_RED", CONCRETE_RED);
		concretes.put("CONCRETE_BLACK", CONCRETE_BLACK);
	}
	
	public static Collection<MaterialColor> getConcretes() {
		return concretes.values();
	}
	
}
