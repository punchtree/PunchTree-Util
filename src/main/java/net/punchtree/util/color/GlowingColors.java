package net.punchtree.util.color;

import org.bukkit.scoreboard.Team;

public class GlowingColors {

	public static class GlowingColor extends PunchTreeColor {
		private static final long serialVersionUID = 1L;
		
		public final Team scoreboardTeam;
		
		private GlowingColor(int r, int g, int b, Team scoreboardTeam) {
			super(r,g,b);
			this.scoreboardTeam = scoreboardTeam;
		}
	}
	
}
