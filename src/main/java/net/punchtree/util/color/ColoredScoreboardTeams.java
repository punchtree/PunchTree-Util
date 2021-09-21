package net.punchtree.util.color;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import net.kyori.adventure.text.format.NamedTextColor;

public class ColoredScoreboardTeams {

	private static ScoreboardManager manager;
	private static Scoreboard scoreboard;
	
	public static Team AQUA_TEAM;
	public static Team BLACK_TEAM;
	public static Team BLUE_TEAM;
	public static Team DARK_AQUA_TEAM;
	public static Team DARK_BLUE_TEAM;
	public static Team DARK_GRAY_TEAM;
	public static Team DARK_GREEN_TEAM;
	public static Team DARK_PURPLE_TEAM;
	public static Team DARK_RED_TEAM;
	public static Team GOLD_TEAM;
	public static Team GRAY_TEAM;
	public static Team GREEN_TEAM;
	public static Team LIGHT_PURPLE_TEAM;
	public static Team RED_TEAM;
	public static Team WHITE_TEAM;
	public static Team YELLOW_TEAM;
	
	private static Map<NamedTextColor, Team> namedTextColorTeamMap = new HashMap<>();

	public static void initializeTeams() {
		manager = Bukkit.getScoreboardManager();
		scoreboard = manager.getMainScoreboard();
		if (AQUA_TEAM == null) {
			AQUA_TEAM = initializeTeam("aquaTeam", NamedTextColor.AQUA);
			BLACK_TEAM = initializeTeam("blackTeam", NamedTextColor.BLACK);
			BLUE_TEAM = initializeTeam("blueTeam", NamedTextColor.BLUE);
			DARK_AQUA_TEAM = initializeTeam("darkAquaTeam", NamedTextColor.DARK_AQUA);
			DARK_BLUE_TEAM = initializeTeam("darkBlueTeam", NamedTextColor.DARK_BLUE);
			DARK_GRAY_TEAM = initializeTeam("darkGrayTeam", NamedTextColor.DARK_GRAY);
			DARK_GREEN_TEAM = initializeTeam("darkGreenTeam", NamedTextColor.DARK_GREEN);
			DARK_PURPLE_TEAM = initializeTeam("darkPurpleTeam", NamedTextColor.DARK_PURPLE);
			DARK_RED_TEAM = initializeTeam("darkRedTeam", NamedTextColor.DARK_RED);
			GOLD_TEAM = initializeTeam("goldTeam", NamedTextColor.GOLD);
			GRAY_TEAM = initializeTeam("grayTeam", NamedTextColor.GRAY);
			GREEN_TEAM = initializeTeam("greenTeam", NamedTextColor.GREEN);
			LIGHT_PURPLE_TEAM = initializeTeam("lightPurpleTeam", NamedTextColor.LIGHT_PURPLE);
			RED_TEAM = initializeTeam("redTeam", NamedTextColor.RED);
			WHITE_TEAM = initializeTeam("whiteTeam", NamedTextColor.WHITE);
			YELLOW_TEAM = initializeTeam("yellowTeam", NamedTextColor.YELLOW);
		}
		namedTextColorTeamMap.put(NamedTextColor.AQUA, AQUA_TEAM);
		namedTextColorTeamMap.put(NamedTextColor.BLACK, BLACK_TEAM);
		namedTextColorTeamMap.put(NamedTextColor.BLUE, BLUE_TEAM);
		namedTextColorTeamMap.put(NamedTextColor.DARK_AQUA, DARK_AQUA_TEAM);
		namedTextColorTeamMap.put(NamedTextColor.DARK_BLUE, DARK_BLUE_TEAM);
		namedTextColorTeamMap.put(NamedTextColor.DARK_GRAY, DARK_GRAY_TEAM);
		namedTextColorTeamMap.put(NamedTextColor.DARK_GREEN, DARK_GREEN_TEAM);
		namedTextColorTeamMap.put(NamedTextColor.DARK_PURPLE, DARK_PURPLE_TEAM);
		namedTextColorTeamMap.put(NamedTextColor.DARK_RED, DARK_RED_TEAM);
		namedTextColorTeamMap.put(NamedTextColor.GOLD, GOLD_TEAM);
		namedTextColorTeamMap.put(NamedTextColor.GRAY, GRAY_TEAM);
		namedTextColorTeamMap.put(NamedTextColor.GREEN, GREEN_TEAM);
		namedTextColorTeamMap.put(NamedTextColor.LIGHT_PURPLE, LIGHT_PURPLE_TEAM);
		namedTextColorTeamMap.put(NamedTextColor.RED, RED_TEAM);
		namedTextColorTeamMap.put(NamedTextColor.WHITE, WHITE_TEAM);
		namedTextColorTeamMap.put(NamedTextColor.YELLOW, YELLOW_TEAM);
	}
	
	private static Team initializeTeam(String teamName, NamedTextColor chatColor) {
		Team team = scoreboard.getTeam(teamName);
		if (team == null) {				
			team = scoreboard.registerNewTeam(teamName);
		}
		team.color(chatColor);
		return team;
	}
	
	public static Team getGlowingTeamNearestColor(PunchTreeColor color) {
		return namedTextColorTeamMap.get(NamedTextColor.nearestTo(color));
	}
	
}
