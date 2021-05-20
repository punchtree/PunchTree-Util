package net.punchtree.util.color;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;

@SuppressWarnings("serial")
public class PunchTreeColor extends Color {

	// Bukkit Colors:

		public static final PunchTreeColor WHITE = new PunchTreeColor(255, 255, 255, ChatColor.WHITE);
		public static final PunchTreeColor YELLOW = new PunchTreeColor(255, 255, 85, ChatColor.YELLOW);
		public static final PunchTreeColor LIGHT_PURPLE = new PunchTreeColor(255, 85, 255, ChatColor.LIGHT_PURPLE);
		public static final PunchTreeColor RED = new PunchTreeColor(255, 85, 85, ChatColor.RED);
		public static final PunchTreeColor AQUA = new PunchTreeColor(85, 255, 255, ChatColor.AQUA);
		public static final PunchTreeColor GREEN = new PunchTreeColor(85, 255, 85, ChatColor.GREEN);
		public static final PunchTreeColor BLUE = new PunchTreeColor(85, 85, 255, ChatColor.BLUE);
		public static final PunchTreeColor DARK_GRAY = new PunchTreeColor(85, 85, 85, ChatColor.DARK_GRAY);
		public static final PunchTreeColor GRAY = new PunchTreeColor(170, 170, 170, ChatColor.GRAY);
		public static final PunchTreeColor GOLD = new PunchTreeColor(255, 170, 0, ChatColor.GOLD);
		public static final PunchTreeColor DARK_PURPLE = new PunchTreeColor(170, 0, 170, ChatColor.DARK_PURPLE);
		public static final PunchTreeColor DARK_RED = new PunchTreeColor(170, 0, 0, ChatColor.DARK_RED);
		public static final PunchTreeColor DARK_AQUA = new PunchTreeColor(0, 170, 170, ChatColor.DARK_AQUA);
		public static final PunchTreeColor DARK_GREEN = new PunchTreeColor(0, 170, 0, ChatColor.DARK_GREEN);
		public static final PunchTreeColor DARK_BLUE = new PunchTreeColor(0, 0, 170, ChatColor.DARK_BLUE);
		public static final PunchTreeColor BLACK = new PunchTreeColor(0, 0, 0, ChatColor.BLACK);

		
		private static final Map<String, PunchTreeColor> defaults;
		static{
			defaults = new HashMap<String, PunchTreeColor>();
			defaults.put("WHITE", PunchTreeColor.WHITE);
			defaults.put("YELLOW", PunchTreeColor.YELLOW);
			defaults.put("LIGHT_PURPLE", PunchTreeColor.LIGHT_PURPLE);
			defaults.put("RED", PunchTreeColor.RED);
			defaults.put("AQUA", PunchTreeColor.AQUA);
			defaults.put("GREEN", PunchTreeColor.GREEN);
			defaults.put("BLUE", PunchTreeColor.BLUE);
			defaults.put("DARK_GRAY", PunchTreeColor.DARK_GRAY);
			defaults.put("GRAY", PunchTreeColor.GRAY);
			defaults.put("GOLD", PunchTreeColor.GOLD);
			defaults.put("DARK_PURPLE", PunchTreeColor.DARK_PURPLE);
			defaults.put("DARK_RED", PunchTreeColor.DARK_RED);
			defaults.put("DARK_AQUA", PunchTreeColor.DARK_AQUA);
			defaults.put("DARK_GREEN", PunchTreeColor.DARK_GREEN);
			defaults.put("DARK_BLUE", PunchTreeColor.DARK_BLUE);
			defaults.put("BLACK", PunchTreeColor.BLACK);
		}
		
		//old wool colors
//		public static final MinigameColor WOOL_WHITE = new MinigameColor(228,228,228);
//		public static final MinigameColor WOOL_ORANGE = new MinigameColor(234,126,53);
//		public static final MinigameColor WOOL_MAGENTA = new MinigameColor();
//		public static final MinigameColor WOOL_LIGHT_BLUE = new MinigameColor();
//		public static final MinigameColor WOOL_YELLOW = new MinigameColor(194,181,28);
//		public static final MinigameColor WOOL_LIME = new MinigameColor(57,186,46);
//		public static final MinigameColor WOOL_PINK = new MinigameColor();
//		public static final MinigameColor WOOL_DARK_GRAY = new MinigameColor(65,65,65);
//		public static final MinigameColor WOOL_LIGHT_GRAY = new MinigameColor(160,167,167);
//		public static final MinigameColor WOOL_CYAN = new MinigameColor();
//		public static final MinigameColor WOOL_PURPLE = new MinigameColor();
//		public static final MinigameColor WOOL_BLUE = new MinigameColor();
//		public static final MinigameColor WOOL_BROWN = new MinigameColor();
//		public static final MinigameColor WOOL_GREEN = new MinigameColor();
//		public static final MinigameColor WOOL_RED = new MinigameColor(158,43,39);
//		public static final MinigameColor WOOL_BLACK = new MinigameColor(24,20,20);
		
		// still old wool colors - better than wolves
//		public static final MinigameColor WOOL_WHITE = new MinigameColor(228,228,228);
//		public static final MinigameColor WOOL_ORANGE = new MinigameColor(234,126,53);
//		public static final MinigameColor WOOL_MAGENTA = new MinigameColor();
//		public static final MinigameColor WOOL_LIGHT_BLUE = new MinigameColor();
//		public static final MinigameColor WOOL_YELLOW = new MinigameColor(194,181,28);
//		public static final MinigameColor WOOL_LIME = new MinigameColor(57,186,46);
//		public static final MinigameColor WOOL_PINK = new MinigameColor();
//		public static final MinigameColor WOOL_DARK_GRAY = new MinigameColor(65,65,65);
//		public static final MinigameColor WOOL_LIGHT_GRAY = new MinigameColor(160,167,167);
//		public static final MinigameColor WOOL_CYAN = new MinigameColor();
//		public static final MinigameColor WOOL_PURPLE = new MinigameColor();
//		public static final MinigameColor WOOL_BLUE = new MinigameColor();
//		public static final MinigameColor WOOL_BROWN = new MinigameColor();
//		public static final MinigameColor WOOL_GREEN = new MinigameColor();
//		public static final MinigameColor WOOL_RED = new MinigameColor(158,43,39);
//		public static final MinigameColor WOOL_BLACK = new MinigameColor(24,20,20);
		

		
	
		
		/* 
		 * Wool & Clay Colors
		 * RED(), ORANGE(), YELLOW(), GREEN(), BLUE(), PURPLE(), 
		 * LIME(), MAGENTA(), LIGHTBLUE(),
		 * PINK(), CYAN(), BROWN(),
		 * WHITE(), LIGHTGRAY(), GRAY(), BLACK();
		 */
		
		public static Collection<PunchTreeColor> getDefaults(){
			return defaults.values();
		}
		
//		public static Collection<PunchTreeColor> getConcretes(){
//			return MaterialColors.concretes.values();
//		}
		
//		private static ChatColor
		public static Material getNearestConcrete(Color color) {
			return getNearestColor(color, MaterialColors.getConcretes()).material;
		}
		
		public static ChatColor getNearestChatColor(Color color) {
			return getNearestColor(color, defaults.values()).getChatColor();
		}
		
		// TODO idk this is only used by a constructor
		private static ChatColor getNearestChatColor(int red, int green, int blue) {
			return getNearestColor(red, green, blue, defaults.values()).getChatColor();
		}
		
		public static <TColor extends Color> TColor getNearestColor(Color color, Collection<TColor> colors) {
			return getNearestColor(color.getRed(), color.getGreen(), color.getBlue(), colors);
		}
		
		// This is a naive algorithm based on hue (since monochromatic teams are pretty deece)
		// For a more advanced algorithm google delta-e
		public static <TColor extends Color> TColor getNearestColor(int red, int green, int blue, Collection<TColor> colors) {
			
			float[] hsvSource = Color.RGBtoHSB(red, green, blue, null);
			float[] thisHsv = new float[3];
			
			double maxHueDistance = 1.1;
			double maxSBDistance = 2.1;
			TColor closest = null;
			for (TColor c : colors) {
				
				thisHsv = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), thisHsv);
				
				double hueDistance = Math.min(Math.abs(hsvSource[0]-thisHsv[0]), 1.-Math.abs(hsvSource[0]-thisHsv[0]));
				double brightnessDistance = Math.abs(hsvSource[2]-thisHsv[2]);
				double saturationDistance = Math.abs(hsvSource[2]-thisHsv[2]);
				double sbDistance = brightnessDistance + saturationDistance;
				
//				double newDistance = 
//						Math.sqrt(Math.pow((double) (red - c.getRed()), 2)
//								+ Math.pow((double) (green - c.getGreen()), 2)
//								+ Math.pow((double) (blue - c.getBlue()), 2));
				
				if (hueDistance < maxHueDistance || hueDistance == maxHueDistance && sbDistance < maxSBDistance) {
					maxHueDistance = hueDistance;
					maxSBDistance = sbDistance;
					closest = c;
				}
			}
			return closest;
		}
		
		public static PunchTreeColor valueOf(String colorName){
			for(String color : defaults.keySet()){
				if(colorName.equalsIgnoreCase(color)
				|| colorName.equalsIgnoreCase(color.replaceAll("_", ""))){
					return defaults.get(color);
				}
			}
			System.out.println("No color found: " + colorName);
			return PunchTreeColor.WHITE;
		}
	
		//------------------------------------------------------------------//

		private ChatColor chatColor;
		
		public PunchTreeColor(int red, int green, int blue){
			this(red, green, blue, getNearestChatColor(red, green, blue));
		}

		public PunchTreeColor(int red, int green, int blue, ChatColor chatColor){
			super(red, green, blue);
			this.chatColor = chatColor;
		}
		
		public ChatColor getChatColor(){
			return chatColor;
		}
		
		public void setChatColor(ChatColor chatColor){
			this.chatColor = chatColor;
		}
		
		public org.bukkit.Color getBukkitColor(){
			return org.bukkit.Color.fromRGB(this.getRed(), this.getGreen(), this.getBlue());
		}
		
		@Override
		public String toString() {
			return chatColor + "";
		}
	
}

