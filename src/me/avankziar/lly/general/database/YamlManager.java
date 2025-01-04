package me.avankziar.lly.general.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;

import me.avankziar.lly.general.database.Language.ISO639_2B;
import me.avankziar.lly.general.objects.WinningClass.PayoutType;
import me.avankziar.lly.general.objects.lottery.Lottery;
import me.avankziar.lly.general.objects.lottery.Lottery.GameType;
import me.avankziar.lly.spigot.ModifierValueEntry.Bypass;

public class YamlManager
{	
	public enum Type
	{
		BUNGEE, SPIGOT, VELO;
	}
	
	private ISO639_2B languageType = ISO639_2B.GER;
	//The default language of your plugin. Mine is german.
	private ISO639_2B defaultLanguageType = ISO639_2B.GER;
	private Type type;
	
	//Per Flatfile a linkedhashmap.
	private static LinkedHashMap<String, Language> configKeys = new LinkedHashMap<>();
	private static LinkedHashMap<String, Language> commandsKeys = new LinkedHashMap<>();
	private static LinkedHashMap<String, Language> languageKeys = new LinkedHashMap<>();
	private static LinkedHashMap<String, Language> mvelanguageKeys = new LinkedHashMap<>();
	private static 
	LinkedHashMap<Lottery.GameType, LinkedHashMap<String, LinkedHashMap<String, Language>>> lottery = new LinkedHashMap<>();
	/*
	 * Here are mutiplefiles in one "double" map. The first String key is the filename
	 * So all filename muss be predefine. For example in the config.
	 */
	private static LinkedHashMap<String, LinkedHashMap<String, Language>> guisKeys = new LinkedHashMap<>();
	
	public YamlManager(Type type)
	{
		this.type = type;
		initConfig();
		initCommands();
		initLanguage();
		initModifierValueEntryLanguage();
		initDefaultLottery();
	}
	
	public ISO639_2B getLanguageType()
	{
		return languageType;
	}

	public void setLanguageType(ISO639_2B languageType)
	{
		this.languageType = languageType;
	}
	
	public ISO639_2B getDefaultLanguageType()
	{
		return defaultLanguageType;
	}
	
	public LinkedHashMap<String, Language> getConfigKey()
	{
		return configKeys;
	}
	
	public LinkedHashMap<String, Language> getCommandsKey()
	{
		return commandsKeys;
	}
	
	public LinkedHashMap<String, Language> getLanguageKey()
	{
		return languageKeys;
	}
	
	public LinkedHashMap<String, Language> getModifierValueEntryLanguageKey()
	{
		return mvelanguageKeys;
	}
	
	public LinkedHashMap<String, LinkedHashMap<String, Language>> getGUIKey()
	{
		return guisKeys;
	}
	
	public LinkedHashMap<Lottery.GameType, LinkedHashMap<String, LinkedHashMap<String, Language>>> getLottery()
	{
		return lottery;
	}
	/*
	 * The main methode to set all paths in the yamls.
	 */
	public void setFileInput(dev.dejvokep.boostedyaml.YamlDocument yml,
			LinkedHashMap<String, Language> keyMap, String key, ISO639_2B languageType) throws org.spongepowered.configurate.serialize.SerializationException
	{
		if(!keyMap.containsKey(key))
		{
			return;
		}
		if(yml.get(key) != null)
		{
			return;
		}
		if(key.startsWith("#"))
		{
			//Comments
			String k = key.replace("#", "");
			if(yml.get(k) == null)
			{
				//return because no actual key are present
				return;
			}
			if(yml.getBlock(k) == null)
			{
				return;
			}
			if(yml.getBlock(k).getComments() != null && !yml.getBlock(k).getComments().isEmpty())
			{
				//Return, because the comments are already present, and there could be modified. F.e. could be comments from a admin.
				return;
			}
			if(keyMap.get(key).languageValues.get(languageType).length == 1)
			{
				if(keyMap.get(key).languageValues.get(languageType)[0] instanceof String)
				{
					String s = ((String) keyMap.get(key).languageValues.get(languageType)[0]).replace("\r\n", "");
					yml.getBlock(k).setComments(Arrays.asList(s));
				}
			} else
			{
				List<Object> list = Arrays.asList(keyMap.get(key).languageValues.get(languageType));
				ArrayList<String> stringList = new ArrayList<>();
				if(list instanceof List<?>)
				{
					for(Object o : list)
					{
						if(o instanceof String)
						{
							stringList.add(((String) o).replace("\r\n", ""));
						}
					}
				}
				yml.getBlock(k).setComments((List<String>) stringList);
			}
			return;
		}
		if(keyMap.get(key).languageValues.get(languageType).length == 1)
		{
			if(keyMap.get(key).languageValues.get(languageType)[0] instanceof String)
			{
				yml.set(key, convertMiniMessageToBungee(((String) keyMap.get(key).languageValues.get(languageType)[0]).replace("\r\n", "")));
			} else if(keyMap.get(key).languageValues.get(languageType)[0] instanceof Boolean)
			{
				yml.set(key, (boolean) keyMap.get(key).languageValues.get(languageType)[0]);
			} else if(keyMap.get(key).languageValues.get(languageType)[0] instanceof Integer)
			{
				yml.set(key, (int) keyMap.get(key).languageValues.get(languageType)[0]);
			} else if(keyMap.get(key).languageValues.get(languageType)[0] instanceof Long)
			{
				yml.set(key, (long) keyMap.get(key).languageValues.get(languageType)[0]);
			} else if(keyMap.get(key).languageValues.get(languageType)[0] instanceof Double)
			{
				yml.set(key, Double.valueOf((double) keyMap.get(key).languageValues.get(languageType)[0]));
			} else if(keyMap.get(key).languageValues.get(languageType)[0] instanceof Float)
			{
				yml.set(key, (float) keyMap.get(key).languageValues.get(languageType)[0]);
			} else
			{
				yml.set(key, keyMap.get(key).languageValues.get(languageType)[0]);
			}
		} else
		{
			List<Object> list = Arrays.asList(keyMap.get(key).languageValues.get(languageType));
			ArrayList<String> stringList = new ArrayList<>();
			if(list instanceof List<?>)
			{
				for(Object o : list)
				{
					if(o instanceof String)
					{
						stringList.add(convertMiniMessageToBungee(((String) o).replace("\r\n", "")));
					} else
					{
						stringList.add(o.toString().replace("\r\n", ""));
					}
				}
			}
			yml.set(key, (List<String>) stringList);
		}
	}
	
	private String convertMiniMessageToBungee(String s)
	{
		if(type != Type.BUNGEE)
		{
			//If Server is not Bungee, there is no need to convert.
			return s;
		}
		StringBuilder b = new StringBuilder();
		for(int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			if(c == '<' && i+1 < s.length())
			{
				char cc = s.charAt(i+1);
				if(cc == '#' && i+8 < s.length())
				{
					//Hexcolors
					//     i12345678
					//f.e. <#00FF00>
					String rc = s.substring(i, i+8);
					b.append(rc.replace("<#", "&#").replace(">", ""));
					i += 8;
				} else
				{
					//Normal Colors
					String r = null;
					StringBuilder sub = new StringBuilder();
					sub.append(c).append(cc);
					i++;
					for(int j = i+1; j < s.length(); j++)
					{
						i++;
						char jc = s.charAt(j);
						if(jc == '>')
						{
							sub.append(jc);
							switch(sub.toString())
							{
							case "</color>":
							case "</black>":
							case "</#d2b773>":
							case "</dark_green>":
							case "</dark_aqua>":
							case "</dark_red>":
							case "</dark_purple>":
							case "</gold>":
							case "</gray>":
							case "</dark_gray>":
							case "</blue>":
							case "</green>":
							case "</aqua>":
							case "</red>":
							case "</light_purple>":
							case "</yellow>":
							case "</white>":
							case "</obf>":
							case "</obfuscated>":
							case "</b>":
							case "</bold>":
							case "</st>":
							case "</strikethrough>":
							case "</u>":
							case "</underlined>":
							case "</i>":
							case "</em>":
							case "</italic>":
								r = "";
								break;
							case "<black>":
								r = "&0";
								break;
							case "<#d2b773>":
								r = "&1";
								break;
							case "<dark_green>":
								r = "&2";
								break;
							case "<dark_aqua>":
								r = "&3";
								break;
							case "<dark_red>":
								r = "&4";
								break;
							case "<dark_purple>":
								r = "&5";
								break;
							case "<gold>":
								r = "<gold>";
								break;
							case "<gray>":
								r = "<gray>";
								break;
							case "<dark_gray>":
								r = "&8";
								break;
							case "<blue>":
								r = "&9";
								break;
							case "<green>":
								r = "<green>";
								break;
							case "<aqua>":
								r = "<aqua>";
								break;
							case "<red>":
								r = "<red>";
								break;
							case "<light_purple>":
								r = "&d";
								break;
							case "<yellow>":
								r = "<yellow>";
								break;
							case "<white>":
								r = "<white>";
								break;
							case "<obf>":
							case "<obfuscated>":
								r = "&k";
								break;
							case "<b>":
							case "<bold>":
								r = "&l";
								break;
							case "<st>":
							case "<strikethrough>":
								r = "&m";
								break;
							case "<u>":
							case "<underlined>":
								r = "<underlined>";
								break;
							case "<i>":
							case "<em>":
							case "<italic>":
								r = "&o";
								break;
							case "<reset>":
								r = "&r";
								break;
							case "<newline>":
								r = "~!~";
								break;
							}
							b.append(r);
							break;
						} else
						{
							//Search for the color.
							sub.append(jc);
						}
					}
				}
			} else
			{
				b.append(c);
			}
		}
		return b.toString();
	}
	
	private void addComments(LinkedHashMap<String, Language> mapKeys, String path, Object[] o)
	{
		mapKeys.put(path, new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, o));
	}
	
	private void addConfig(String path, Object[] c, Object[] o)
	{
		configKeys.put(path, new Language(new ISO639_2B[] {ISO639_2B.GER}, c));
		addComments(configKeys, "#"+path, o);
	}
	
	public void initConfig() //INFO:Config
	{
		addConfig("useIFHAdministration",
				new Object[] {
				true},
				new Object[] {
				"Boolean um auf das IFH Interface Administration zugreifen soll.",
				"Wenn 'true' eingegeben ist, aber IFH Administration ist nicht vorhanden, so werden automatisch die eigenen Configwerte genommen.",
				"Boolean to access the IFH Interface Administration.",
				"If 'true' is entered, but IFH Administration is not available, the own config values are automatically used."});
		addConfig("IFHAdministrationPath", 
				new Object[] {
				"lly"},
				new Object[] {
				"",
				"Diese Funktion sorgt dafür, dass das Plugin auf das IFH Interface Administration zugreifen kann.",
				"Das IFH Interface Administration ist eine Zentrale für die Daten von Sprache, Servername und Mysqldaten.",
				"Diese Zentralisierung erlaubt für einfache Änderung/Anpassungen genau dieser Daten.",
				"Sollte das Plugin darauf zugreifen, werden die Werte in der eigenen Config dafür ignoriert.",
				"",
				"This function ensures that the plugin can access the IFH Interface Administration.",
				"The IFH Interface Administration is a central point for the language, server name and mysql data.",
				"This centralization allows for simple changes/adjustments to precisely this data.",
				"If the plugin accesses it, the values in its own config are ignored."});
		addConfig("ServerName",
				new Object[] {
				"hub"},
				new Object[] {
				"",
				"Der Server steht für den Namen des Spigotservers, wie er in BungeeCord/Waterfall/Velocity config.yml unter dem Pfad 'servers' angegeben ist.",
				"Sollte kein BungeeCord/Waterfall oder andere Proxys vorhanden sein oder du nutzt IFH Administration, so kannst du diesen Bereich ignorieren.",
				"",
				"The server stands for the name of the spigot server as specified in BungeeCord/Waterfall/Velocity config.yml under the path 'servers'.",
				"If no BungeeCord/Waterfall or other proxies are available or you are using IFH Administration, you can ignore this area."});
		addConfig("Language",
				new Object[] {
				"ENG"},
				new Object[] {
				"",
				"Die eingestellte Sprache. Von Haus aus sind 'ENG=Englisch' und 'GER=Deutsch' mit dabei.",
				"Falls andere Sprachen gewünsch sind, kann man unter den folgenden Links nachschauen, welchs Kürzel für welche Sprache gedacht ist.",
				"Siehe hier nach, sowie den Link, welche dort auch für Wikipedia steht.",
				"https://github.com/Avankziar/RootAdministration/blob/main/src/main/java/me/avankziar/roota/general/Language.java",
				"",
				"The set language. By default, ENG=English and GER=German are included.",
				"If other languages are required, you can check the following links to see which abbreviation is intended for which language.",
				"See here, as well as the link, which is also there for Wikipedia.",
				"https://github.com/Avankziar/RootAdministration/blob/main/src/main/java/me/avankziar/roota/general/Language.java"});
		addConfig("Mysql.Status",
				new Object[] {
				false},
				new Object[] {
				"",
				"'Status' ist ein simple Sicherheitsfunktion, damit nicht unnötige Fehler in der Konsole geworfen werden.",
				"Stelle diesen Wert auf 'true', wenn alle Daten korrekt eingetragen wurden.",
				"",
				"'Status' is a simple security function so that unnecessary errors are not thrown in the console.",
				"Set this value to 'true' if all data has been entered correctly."});
		addComments(configKeys, "#Mysql", 
				new Object[] {
				"",
				"Mysql ist ein relationales Open-Source-SQL-Databaseverwaltungssystem, das von Oracle entwickelt und unterstützt wird.",
				"'My' ist ein Namenkürzel und 'SQL' steht für Structured Query Language. Eine Programmsprache mit der man Daten auf einer relationalen Datenbank zugreifen und diese verwalten kann.",
				"Link https://www.mysql.com/de/",
				"Wenn du IFH Administration nutzt, kann du diesen Bereich ignorieren.",
				"",
				"Mysql is an open source relational SQL database management system developed and supported by Oracle.",
				"'My' is a name abbreviation and 'SQL' stands for Structured Query Language. A program language that can be used to access and manage data in a relational database.",
				"Link https://www.mysql.com",
				"If you use IFH Administration, you can ignore this section."});
		addConfig("Mysql.Host",
				new Object[] {
				"127.0.0.1"},
				new Object[] {
				"",
				"Der Host, oder auch die IP. Sie kann aus einer Zahlenkombination oder aus einer Adresse bestehen.",
				"Für den Lokalhost, ist es möglich entweder 127.0.0.1 oder 'localhost' einzugeben. Bedenke, manchmal kann es vorkommen,",
				"das bei gehosteten Server die ServerIp oder Lokalhost möglich ist.",
				"",
				"The host, or IP. It can consist of a number combination or an address.",
				"For the local host, it is possible to enter either 127.0.0.1 or >localhost<.",
				"Please note that sometimes the serverIp or localhost is possible for hosted servers."});
		addConfig("Mysql.Port",
				new Object[] {
				3306},
				new Object[] {
				"",
				"Ein Port oder eine Portnummer ist in Rechnernetzen eine Netzwerkadresse,",
				"mit der das Betriebssystem die Datenpakete eines Transportprotokolls zu einem Prozess zuordnet.",
				"Ein Port für Mysql ist standart gemäß 3306.",
				"",
				"In computer networks, a port or port number ",
				"is a network address with which the operating system assigns the data packets of a transport protocol to a process.",
				"A port for Mysql is standard according to 3306."});
		addConfig("Mysql.DatabaseName",
				new Object[] {
				"mydatabase"},
				new Object[] {
				"",
				"Name der Datenbank in Mysql.",
				"",
				"Name of the database in Mysql."});
		addConfig("Mysql.SSLEnabled",
				new Object[] {
				false},
				new Object[] {
				"",
				"SSL ist einer der drei Möglichkeiten, welcher, solang man nicht weiß, was es ist, es so lassen sollte wie es ist.",
				"",
				"SSL is one of the three options which, as long as you don't know what it is, you should leave it as it is."});
		addConfig("Mysql.AutoReconnect",
				new Object[] {
				true},
				new Object[] {
				"",
				"AutoReconnect ist einer der drei Möglichkeiten, welcher, solang man nicht weiß, was es ist, es so lassen sollte wie es ist.",
				"",
				"AutoReconnect is one of the three options which, as long as you don't know what it is, you should leave it as it is."});
		addConfig("Mysql.VerifyServerCertificate",
				new Object[] {
				false},
				new Object[] {
				"",
				"VerifyServerCertificate ist einer der drei Möglichkeiten, welcher, solang man nicht weiß, was es ist, es so lassen sollte wie es ist.",
				"",
				"VerifyServerCertificate is one of the three options which, as long as you don't know what it is, you should leave it as it is."});
		addConfig("Mysql.User",
				new Object[] {
				"admin"},
				new Object[] {
				"",
				"Der User, welcher auf die Mysql zugreifen soll.",
				"",
				"The user who should access the Mysql."});
		addConfig("Mysql.Password",
				new Object[] {
				"not_0123456789"},
				new Object[] {
				"",
				"Das Passwort des Users, womit er Zugang zu Mysql bekommt.",
				"",
				"The user's password, with which he gets access to Mysql."});
		addConfig("EnableMechanic.Modifier",
				new Object[] {
				true},
				new Object[] {
				"",
				"Ermöglicht TT die Benutzung von IFH Interface Modifier.",
				"Es erlaubt, dass externe Plugins oder per Befehl Zahlenmodifikatoren in bestimmte Werten einfließen.",
				"Bspw. könnte es dazu führen, dass die Spieler mehr regestrierte Öfen besitzen dürfen.",
				"",
				"Enables TT to use IFH interface modifiers.",
				"It allows external plugins or by command to include number modifiers in certain values.",
				"For example, it could lead to players being allowed to own more registered furnace."});
		addConfig("EnableMechanic.ValueEntry",
				new Object[] {
				true},
				new Object[] {
				"",
				"Ermöglicht TT die Benutzung von IFH Interface ValueEntry.",
				"Es erlaubt, dass externe Plugins oder per Befehl Werteeinträge vornehmen.",
				"Bspw. könnte man dadurch bestimmte Befehle oder Technologien für Spieler freischalten.",
				"",
				"Enables TT to use the IFH interface ValueEntry.",
				"It allows external plugins or commands to make value entries.",
				"For example, it could be used to unlock certain commands or technologies for players."});		
		addConfig("ValueEntry.OverrulePermission",
				new Object[] {
				false},
				new Object[] {
				"",
				"Sollte ValueEntry eingeschalten und installiert sein, so wird bei fast allen Permissionabfragen ValueEntry mit abgefragt.",
				"Fall 1: ValueEntry ist nicht vorhanden oder nicht eingschaltet. So wird die Permission normal abgefragt.",
				"Für alle weitern Fälle ist ValueEntry vorhanden und eingeschaltet.",
				"Fall 2: Der Werteeintrag für den Spieler für diesen abgefragten Wert ist nicht vorhanden,",
				"so wird wenn 'OverrulePermission'=true immer 'false' zurückgegeben.",
				"Ist 'OverrulePermission'=false wird eine normale Permissionabfrage gemacht.",
				"Fall 3: Der Werteeintrag für den Spieler für diesen abgefragten Wert ist vorhanden,",
				"so wird wenn 'OverrulePermission'=true der hinterlegte Werteeintrag zurückgegebn.",
				"Wenn 'OverrulePermission'=false ist, wird 'true' zurückgegeben wenn der hinterlegte Werteeintrag ODER die Permissionabfrage 'true' ist.",
				"Sollten beide 'false' sein, wird 'false' zurückgegeben.",
				"",
				"If ValueEntry is switched on and installed, ValueEntry is also queried for almost all permission queries.",
				"Case 1: ValueEntry is not present or not switched on. The permission is queried normally.",
				"For all other cases, ValueEntry is present and switched on.",
				"Case 2: The value entry for the player for this queried value is not available,",
				"so if 'OverrulePermission'=true, 'false' is always returned.",
				"If 'OverrulePermission'=false, a normal permission query is made.",
				"Case 3: The value entry for the player for this queried value exists,",
				"so if 'OverrulePermission'=true the stored value entry is returned.",
				"If 'OverrulePermission'=false, 'true' is returned if the stored value entry OR the permission query is 'true'.",
				"If both are 'false', 'false' is returned."});
		addConfig("DateTimeFormatter",
				new Object[] {
				"dd-MM-yyyy HH:mm"},
				new Object[] {
				"",
				"Zeitformattierung für die Lotterieziehungen.",
				"",
				"Time formatting for lottery draws."});
	}
	
	@SuppressWarnings("unused") //INFO:Commands
	public void initCommands()
	{
		comBypass();
		String path = "";
		commandsInput("lly", "lly", "lly.cmd", 
				"/lly [pagenumber]", "/lly ", false,
				"<red>/lly <white>| Infoseite für alle Befehle.",
				"<red>/lly <white>| Info page for all commands.",
				"<aqua>Befehlsrecht für <white>/lly",
				"<aqua>Commandright for <white>/lly",
				"<yellow>Basisbefehl für das LuckyLottery Plugin.",
				"<yellow>Groundcommand for the LuckyLottery Plugin.");
		String basePermission = "classiclotto";
		commandsInput("classiclotto", "classiclotto", "classiclotto.cmd", 
				"/classiclotto [lotteryname]", "/classiclotto ", false,
				"<red>/classiclotto [Lotteriename] <white>| Listet alle Klassischen Lotterien auf und bei optionalen Argument gibt detailierte Information.",
				"<red>/classiclotto [lotteryname] <white>| Lists all Classic Lotteries and with optional argument gives detailed information.",
				"<aqua>Befehlsrecht für <white>/classiclotto",
				"<aqua>Commandright for <white>/classiclotto",
				"<yellow>Befehl /classiclotto",
				"<yellow>Command /classiclotto");
		argumentInput("classiclotto_drawnow", "drawnow", basePermission,
				"/classiclotto drawnow <lotteryname> [numbers|reopen]", "/classiclotto drawnow ", false,
				"<red>/classiclotto drawnow <Lotteriename> [Nummer|reopen] <white>| Zieht die Lotterie jetzt. Optional können die geltenden Zahlen mitangegeben werden und oder Wiedereröffnung der Lotterie nach der Ziehung für maneuell gezogene Lotterien.",
				"<red>/classiclotto drawnow <lotteryname> [numbers|reopen] <white>| Draw the lottery now. Optionally, the applicable numbers can be specified and or reopening the lottery after the draw for manually drawn lotteries.",
				"<aqua>Befehlsrecht für <white>/classiclotto drawnow",
				"<aqua>Commandright for <white>/classiclotto drawnow",
				"<yellow>Befehl /classiclotto drawnow",
				"<yellow>Command /classiclotto drawnow");
		argumentInput("classiclotto_play", "play", basePermission,
				"/classiclotto play <lotteryname> [numbers] [boolean repeat] [confirm]", "/classiclotto play ", false,
				"<red>/classiclotto play <Lotteriename> [Nummer] [boolean Wiederholung] [bestätigen] <white>| Spielt in der angegeben Lotterie.",
				"<red>/classiclotto play <lotteryname> [numbers] [boolean repeat] [confirm] <white>| Play in the specified lottery.",
				"<aqua>Befehlsrecht für <white>/classiclotto play",
				"<aqua>Commandright for <white>/classiclotto play",
				"<yellow>Befehl /classiclotto play",
				"<yellow>Command /classiclotto play");
		argumentInput("classiclotto_addpot", "addpot", basePermission,
				"/classiclotto addpot <lotteryname> <AmountToAdd> [-broadcast]", "/classiclotto addpot ", false,
				"<red>/classiclotto addpot <Lotteriename> <MengeZumHinzufügen> [-broadcast] <white>| Fügt dem aktuellen Pot den angegebenen Betrag hinzu. Optional mit Broadcast für alle Onlinespieler.",
				"<red>/classiclotto addpot <lotteryname> <AmountToAdd> [-broadcast] <white>| Adds the specified amount to the current pot. Optionally with broadcast to all online players.",
				"<aqua>Befehlsrecht für <white>/classiclotto addpot",
				"<aqua>Commandright for <white>/classiclotto addpot",
				"<yellow>Befehl /classiclotto addpot",
				"<yellow>Command /classiclotto addpot");
		argumentInput("classiclotto_setpot", "setpot", basePermission,
				"/classiclotto setpot <lotteryname> <AmountToSet> [-broadcast]", "/classiclotto setpot ", false,
				"<red>/classiclotto setpot <Lotteriename> <MengeZumSetzten> [-broadcast] <white>| Setzt den Pot der Lotterie neu fest. Optional mit Broadcast für alle Onlinespieler.",
				"<red>/classiclotto setpot <lotteryname> <AmountToSet> [-broadcast] <white>| Sets the lottery pot. Optionally with broadcast to all online players.",
				"<aqua>Befehlsrecht für <white>/classiclotto setpot",
				"<aqua>Commandright for <white>/classiclotto setpot",
				"<yellow>Befehl /classiclotto setpot",
				"<yellow>Command /classiclotto setpot");
		argumentInput("classiclotto_giveticket", "giveticket", basePermission,
				"/classiclotto giveticket <lotteryname> [amountOfTicket]", "/classiclotto giveticket ", false,
				"<red>/classiclotto giveticket <Lotteriename> [amountOfTicket] <white>| Gibt allen online Spieler ein Ticket in der Lotterie. Optional mit Anzahl an zu vergebenden Tickets.",
				"<red>/classiclotto giveticket <lotteryname> [amountOfTicket] <white>| Gives all online players a ticket in the lottery. Optional with number of tickets to be allocated.",
				"<aqua>Befehlsrecht für <white>/classiclotto giveticket",
				"<aqua>Commandright for <white>/classiclotto giveticket",
				"<yellow>Befehl /classiclotto giveticket",
				"<yellow>Command /classiclotto giveticket");
		argumentInput("classiclotto_open", "open", basePermission,
				"/classiclotto open", "/classiclotto open ", false,
				"<red>/classiclotto open <white>| Öffnet eine Lotterie. Sofern die Lotterie manuell gezogen wird, muss dies dann auch über einen Befehl erfolgen.",
				"<red>/classiclotto open <white>| Opens a lottery. If the lottery is drawn manually, this must also be done using a command.",
				"<aqua>Befehlsrecht für <white>/classiclotto open",
				"<aqua>Commandright for <white>/classiclotto open",
				"<yellow>Befehl /classiclotto open",
				"<yellow>Command /classiclotto open");
		argumentInput("classiclotto_ticketlist", "ticketlist", basePermission,
				"/classiclotto ticketlist [page]", "/classiclotto ticketlist ", false,
				"<red>/classiclotto ticketlist [Seite] <white>| Listet die letzten 10 Tickets der angegebenen Lotteriea auf.",
				"<red>/classiclotto ticketlist [page] <white>| Lists the last 10 tickets of the specified lottery.",
				"<aqua>Befehlsrecht für <white>/classiclotto ticketlist",
				"<aqua>Commandright for <white>/classiclotto ticketlist",
				"<yellow>Befehl /classiclotto ticketlist",
				"<yellow>Command /classiclotto ticketlist");
		argumentInput("classiclotto_repeat", "repeat", basePermission,
				"/classiclotto repeat <lotteryname> <id>", "/classiclotto repeat ", false,
				"<red>/classiclotto repeat <lotteryname> <id> <white>| Setzt das Ticket auf wiederholung der Zahlen. Sollte die Ziehung schon abgelaufen sein, wird ein neues Ticket erstellt.",
				"<red>/classiclotto repeat <lotteryname> <id> <white>| Sets the ticket to repeat the numbers. If the draw has already expired, a new ticket is created.",
				"<aqua>Befehlsrecht für <white>/classiclotto repeat",
				"<aqua>Commandright for <white>/classiclotto repeat",
				"<yellow>Befehl /classiclotto repeat",
				"<yellow>Command /classiclotto repeat");
		argumentInput("classiclotto_nextdraws", "nextdraws", basePermission,
				"/classiclotto nextdraws <lotteryname>", "/classiclotto nextdraws ", false,
				"<red>/classiclotto nextdraws <lotteryname> <white>| Zeigt alle nächsten Ziehungen der Lotterie.",
				"<red>/classiclotto nextdraws <lotteryname> <white>| Shows all upcoming lottery draws.",
				"<aqua>Befehlsrecht für <white>/classiclotto nextdraws",
				"<aqua>Commandright for <white>/classiclotto nextdraws",
				"<yellow>Befehl /classiclotto nextdraws",
				"<yellow>Command /classiclotto nextdraws");
	}
	
	private void comBypass() //INFO:ComBypass
	{
		List<Bypass.Permission> list = new ArrayList<Bypass.Permission>(EnumSet.allOf(Bypass.Permission.class));
		for(Bypass.Permission ept : list)
		{
			commandsKeys.put("Bypass."+ept.toString().replace("_", ".")
					, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					"lly."+ept.toString().toLowerCase().replace("_", ".")}));
		}
		
		List<Bypass.Counter> list2 = new ArrayList<Bypass.Counter>(EnumSet.allOf(Bypass.Counter.class));
		for(Bypass.Counter ept : list2)
		{
			if(!ept.forPermission())
			{
				continue;
			}
			commandsKeys.put("Count."+ept.toString().replace("_", ".")
					, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					"lly."+ept.toString().toLowerCase().replace("_", ".")}));
		}
	}
	
	private void commandsInput(String path, String name, String basePermission, 
			String suggestion, String commandString, boolean putUpCmdPermToBonusMalusSystem,
			String helpInfoGerman, String helpInfoEnglish,
			String dnGerman, String dnEnglish,
			String exGerman, String exEnglish)
	{
		commandsKeys.put(path+".Name"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				name}));
		commandsKeys.put(path+".Permission"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				basePermission}));
		commandsKeys.put(path+".Suggestion"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				suggestion}));
		commandsKeys.put(path+".PutUpCommandPermToBonusMalusSystem"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				putUpCmdPermToBonusMalusSystem}));
		commandsKeys.put(path+".CommandString"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				commandString}));
		commandsKeys.put(path+".HelpInfo"
				, new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
				helpInfoGerman,
				helpInfoEnglish}));
		commandsKeys.put(path+".Displayname"
				, new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
				dnGerman,
				dnEnglish}));
		commandsKeys.put(path+".Explanation"
				, new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
				exGerman,
				exEnglish}));
	}
	
	private void argumentInput(String path, String argument, String basePermission, 
			String suggestion, String commandString, boolean putUpCmdPermToBonusMalusSystem,
			String helpInfoGerman, String helpInfoEnglish,
			String dnGerman, String dnEnglish,
			String exGerman, String exEnglish)
	{
		commandsKeys.put(path+".Argument"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				argument}));
		commandsKeys.put(path+".Permission"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				basePermission+"."+argument}));
		commandsKeys.put(path+".Suggestion"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				suggestion}));
		commandsKeys.put(path+".PutUpCommandPermToBonusMalusSystem"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				putUpCmdPermToBonusMalusSystem}));
		commandsKeys.put(path+".CommandString"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				commandString}));
		commandsKeys.put(path+".HelpInfo"
				, new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
				helpInfoGerman,
				helpInfoEnglish}));
		commandsKeys.put(path+".Displayname"
				, new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
				dnGerman,
				dnEnglish}));
		commandsKeys.put(path+".Explanation"
				, new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
				exGerman,
				exEnglish}));
	}
	
	public void initLanguage() //INFO:Languages
	{
		languageKeys.put("InputIsWrong",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Deine Eingabe ist fehlerhaft! Klicke hier auf den Text, um weitere Infos zu bekommen!",
						"<red>Your input is incorrect! Click here on the text to get more information!"}));
		languageKeys.put("NoPermission",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Du hast dafür keine Rechte!",
						"<red>You dont not have the rights!"}));
		languageKeys.put("NoPlayerExist",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Der Spieler existiert nicht!",
						"<red>The player does not exist!"}));
		languageKeys.put("NoNumber",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Das Argument <white>%value% <red>muss eine ganze Zahl sein.",
						"<red>The argument <white>%value% &must be an integer."}));
		languageKeys.put("NoDouble",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Das Argument <white>%value% <red>muss eine Gleitpunktzahl sein!",
						"<red>The argument <white>%value% &must be a floating point number!"}));
		languageKeys.put("IsNegativ",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Das Argument <white>%value% <red>muss eine positive Zahl sein!",
						"<red>The argument <white>%value% <red>must be a positive number!"}));
		languageKeys.put("GeneralHover",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Klick mich!",
						"<yellow>Click me!"}));
		languageKeys.put("Headline", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>=====<gray>[<gold>LuckyLottery<gray>]<yellow>=====",
						"<yellow>=====<gray>[<gold>LuckyLottery<gray>]<yellow>====="}));
		languageKeys.put("Next", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow><underlined>nächste Seite <yellow>==>",
						"<yellow><underlined>next page <yellow>==>"}));
		languageKeys.put("NextPast", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<white>| ",
						"<white>| "}));
		languageKeys.put("Past", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow><== <underlined>vorherige Seite",
						"<yellow><== <underlined>previous page"}));
		languageKeys.put("IsTrue", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<green>✔",
						"<green>✔"}));
		languageKeys.put("IsFalse", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>✖",
						"<red>✖"}));
		languageKeys.put("WasChoosen", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<green>%number%</green>",
						"<green>%number%</green>"}));
		languageKeys.put("WasntChoosen", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>%number%</red>",
						"<red>%number%</red>"}));
		languageKeys.put("WasntNeutralChoosen", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<gray>%number%</gray>",
						"<gray>%number%</gray>"}));
		languageKeys.put("WasntDrawn", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Ziehung ausstehend</red>",
						"<red>drawing pending</red>"}));
		languageKeys.put("NotEnoughMoney", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Du hast nicht genug Geld!</red>",
						"<red>You dont have enough money!</red>"}));
		languageKeys.put("Replacer.NoDraw", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"Keine Lotterieziehung",
						"No lottery drawing"}));
		initClassicLotto();
		initLottoSuper();
	}
	
	private void initClassicLotto()
	{
		String path = "ClassicLotto";
		languageKeys.put(path+".NoClassicLottoFound", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Die angegebene klassische Lotterie extiert nicht!",
						"<red>The specified classic lottery does not exist!"}));
		languageKeys.put(path+".NoClassicLottoDrawFound", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Für die angegebene Lotterie wird keine Ziehung erwartet! Fehler mit der Mysql!",
						"<red>No drawing is expected for the specified lottery! Error with MySQL!"}));
		languageKeys.put(path+".Draw.NoTicketAreBought", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<#d2b773>...}/=== <gold>%lotteryname% <white>Ziehung <#d2b773>===\\{...",
						"<gold>JackPot bis zu <white>%actualpot%<gold>! <aqua>Hauptgewinn <white>%highestwinningclass%<aqua>!",
						"<yellow>Gezogene Nummer: <reset>%drawnnumber%",
						"<red>Keine Lose wurden verkauft!",
						"<#d2b773>...}/=====  ^^^^^^^^^^  =====\\\\{...",
						"<#d2b773>...}/=== <gold>%lotteryname% <white>Draw <#d2b773>===\\\\\\\\{...",
						"<gold>JackPot up to <white>%actualpot%<gold>! <aqua>Main prize <white>%highestwinningclass%<aqua>!",
						"<yellow>Drawn number: <reset>%drawnnumber%",
						"<red>No tickets were sold!",
						"<#d2b773>...}/=====  ^^^^^^^^^^  =====\\\\{..."}));
		languageKeys.put(path+".Draw.RepeatTicket.Category", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"%lotteryname%-Ticket",
						"%lotteryname%-Ticket"}));
		languageKeys.put(path+".Draw.RepeatTicket.Comment", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"Lotterie Ticket wurde automatisch wiedergekauft.",
						"Lottery ticket was automatically repurchased."}));
		languageKeys.put(path+".Draw.WinningClassReplacer", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<gray>GW%level%: <white>%winneramount%",
						"<gray>GW%level%: <white>%winneramount%"}));
		languageKeys.put(path+".Draw.WinningClassReplacerSeperator", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<gray>, ",
						"<gray>, "}));
		languageKeys.put(path+".Draw.JackpotWasBreached", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<#d2b773>...}/=== <gold>%lotteryname% <white>Ziehung <#d2b773>===\\{...",
						"<yellow>Folgend Zahlen wurden gezogen: <white><bold>%drawnnumber%",
						"<gold>Gratulation an <white>%winners% <gold>zum knacken des Jackpots!",
						"<gold>Gewonnen wurden <red><bold>%highestwinningclass% <reset><gold>verteilt auf alle Hauptgewinner.",
						"<gray>Folgend die Anzahl der Gewinner in allen Gewinnklassen:",
						"<gray>%winningclasswinneramount%",
						"<yellow>Im nächsten Lotteriepot sind insgesamt bis zu satte ...",
						"<aqua><bold>%nextpot% zu gewinnen!",
						"<#d2b773>...}/=====  ^^^^^^^^^^  =====\\\\{...",
						"<#d2b773>...}/=== <gold>%lotteryname% <white>Draw <#d2b773>===\\\\\\\\{...",
						"<yellow>The following numbers were drawn: <white><bold>%drawnnumber%",
						"<gold>Congratulations to <white>%winners% <gold>for hitting the jackpot!",
						"<gold>The prize money won was <red><bold>%highestwinningclass% <reset><gold>distributed among all mainwinners.",
						"<gray>Following are the number of winners in all prize categories:",
						"<gray>%winningclasswinneramount%",
						"<yellow>The next lottery pot contains a total of up to ...",
						"<aqua><bold>%nextpot% to win!",
						"<#d2b773>...}/=====  ^^^^^^^^^^  =====\\\\\\\\{..."}));
		languageKeys.put(path+".Draw.JackpotIsUntouched", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<#d2b773>...}/=== <gold>%lotteryname% <white>Ziehung <#d2b773>===\\{...",
						"<yellow>Folgend Zahlen wurden gezogen: <white><bold>%drawnnumber%",
						"<gray><bold>Leider gab es keinen Hauptgewinner!",
						"<gray>Folgend die Anzahl der Gewinner in allen Gewinnklassen:",
						"<gray>%winningclasswinneramount%",
						"<yellow>Im nächsten Lotteriepot sind insgesamt bis zu satte ...",
						"<aqua><bold>%nextpot% zu gewinnen!",
						"<#d2b773>...}/=====  ^^^^^^^^^^  =====\\\\{...",
						"<#d2b773>...}/=== <gold>%lotteryname% <white>Draw <#d2b773>===\\\\{...",
						"<yellow>The following numbers were drawn: <white><bold>%drawnnumber%",
						"<gray><bold>Unfortunately there was no main winner!",
						"<gray>Following are the number of winners in all prize categories:",
						"<gray>%winningclasswinneramount%",
						"<yellow>The next lottery pot contains a total of up to ...",
						"<aqua><bold>%nextpot% to win!",
						"<#d2b773>...}/=====  ^^^^^^^^^^  =====\\\\\\\\{..."}));
		languageKeys.put(path+".Draw.Won", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"        <gold><bold>! GEWONNEN !",
						"<yellow>Du hattest %matchchoosennumberamount% richtige!",
						"<gray>Deine Nummern:<reset> %matchchoosennumber%",
						"<yellow>Dein Preis: %payout%",
						"        <gold><bold>! WON !",
						"<yellow>You had %matchchoosennumberamount% correct!",
						"<gray>Your numbers:<reset> %matchchoosennumber%",
						"<yellow>Your price: %payout%"}));
		languageKeys.put(path+".Draw.NotWon", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Schade! Nicht gewonnen. %matchchoosennumber%",
						"<yellow>Too bad! Not won. %matchchoosennumber%"}));
		languageKeys.put(path+".Draw.Win.Category", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"%lottoryname%-Gewinn",
						"%lottoryname%-Prize"}));
		languageKeys.put(path+".Draw.Win.Comment", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"Gewonnen in der Gewinnkategory %level%.",
						"Won in the winning category %level%."}));
		languageKeys.put(path+".Cmd.Headline", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<#d2b773>...}/=== <gold>Klassisches Lotto Info <#d2b773>===\\{...",
						"<#d2b773>...}/=== <gold>Klassisches Lotto Info <#d2b773>===\\{..."}));
		languageKeys.put(path+".Cmd.BottomLine", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<#d2b773>...}/=====  ^^^^^^^^^^  =====\\\\{...",
						"<#d2b773>...}/=====  ^^^^^^^^^^  =====\\\\{..."}));
		languageKeys.put(path+".Cmd.GeneralInfo", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<aqua><bold>%lotteryname%:</bold> <white>Ziehung: %nextdraw%",
						"<gray>%description%",
						"<red>Aktueller Pot: <white>%actualpot% | %costperticket% <red>pro Ticket.",
						"<click:run_command:'%classiclottocmd%%lotteryname%'><gray>Klicke {hier} für mehr Infos.</click>",
						"<aqua><bold>%lotteryname%:</bold> <white>Draw: %nextdraw%",
						"<gray>%description%",
						"<red>Current pot: <white>%actualpot% | %costperticket% <red>per ticket.",
						"<click:run_command:'%classiclottocmd%%lotteryname%'><gray>Click {here} for more info.</click>"}));
		languageKeys.put(path+".Cmd.DetailInfo", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<#d2b773>...}/=== <gold>Klassisches Lotto Info <#d2b773>===\\{...",
						"<aqua><bold>%lotteryname%:",
						"<gray>%description%",
						"<red>Nächste Ziehung: <white>%nextdraw%",
						"<red>Aktueller Pot: <white>%actualpot% | %costperticket% <red>pro Ticket.",
						"<red>Gewinnchance <white>1:%winningchance%",
						"<red>Mindest Pot: <white>%standartpot% | <red>Maximaler Pot: <white>%maximumpot%",
						"<red>Ehöhung des Pot, falls es kein Hauptgewinner gibt: <white>%amounttoaddpot%",
						"<click:run_command:'%classiclottobet%%lotteryname%'><gold>Klicke {hier} zum spielen.</click>",
						"<#d2b773>...}/=====  ^^^^^^^^^^  =====\\\\{...",
						"<#d2b773>...}/=== <gold>Klassisches Lotto Info <#d2b773>===\\{...",
						"<aqua><bold>%lotteryname%:",
						"<gray>%description%",
						"<red>Next draw: <white>%nextdraw%",
						"<red>Current pot: <white>%actualpot% | %costperticket% <red>per ticket.",
						"<red>Chance of winning <white>1:%winningchance%",
						"<red>Minimum pot: <white>%standardpot% | <red>Maximum Pot: <white>%maximumpot%",
						"<red>Increase the pot if there is no main winner: <white>%amounttoaddpot%",
						"<click:run_command:'%classiclottobet%%lotteryname%'><gold>Click {here} to play.</click>",
						"<#d2b773>...}/=====  ^^^^^^^^^^  =====\\\\{..."}));
		languageKeys.put(path+".Arg.Play.LotteryNotOpen", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Die Lotterie erlaubt zurzeit keinen Ticketkauf!",
						"<red>The lottery currently does not allow ticket purchases!"}));
		languageKeys.put(path+".Arg.Play.TooManyTickets", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Du hast schon %amount% Lose für die Lotterie %lotteryname%. Pro Ziehung sind nur maximal %maximum% Lose erlaubt.",
						"<red>You already have %amount% tickets for the lottery %lotteryname%. Only a maximum of %maximum% tickets are allowed per drawing."}));
		languageKeys.put(path+".Arg.Play.AlreadyChoosenNumber", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<aqua>Deine bisher gewählten Nummern: <white>%choosennumber%",
						"<aqua>Your previously selected numbers: <white>%choosennumber%"}));
		languageKeys.put(path+".Arg.Play.RandomChoose", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Klicke <red>{hier} <yellow>um deine Nummber <white>zufällig <yellow>auszuwählen!",
						"<yellow>Click <red>{here} <yellow>to select your Nummber <white>randomly <yellow>!"}));
		languageKeys.put(path+".Arg.Play.ShouldRepeat", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Soll dein Ticket sich nach einer Ziehung wiederholen: <reset>%shouldrepeat% <gray>(Klicke aufs Icon)",
						"<yellow>Should your ticket repeat after a draw: <reset>%shouldrepeat% <gray>(Click on icon)"}));
		languageKeys.put(path+".Arg.Play.ChooseNumber", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Bitte wähle deine gewünschten %amountofchoosennumber% Nummern aus:",
						"<yellow>Please select your desired %amountofchoosennumber% numbers:"}));
		languageKeys.put(path+".Arg.Play.Seperator", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						" ",
						" "}));
		languageKeys.put(path+".Arg.Play.Category", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"%lottoryname%-Ticket",
						"%lottoryname%-Ticket"}));
		languageKeys.put(path+".Arg.Play.Comment", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"Lotterie Ticket wurde gekauft.",
						"Lottery ticket was purchased."}));
		languageKeys.put(path+".Arg.Play.TicketBought", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Du hast ein Ticket gekauft! Viel Glück! Gewählte Zahlen: <white>%choosennumber%",
						"<yellow>You have bought a ticket! Good luck! Chosen numbers: <white>%choosennumber%"}));
		languageKeys.put(path+".Arg.Play.MayYouConfirm", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Gewählte Zahlen: <white>%choosennumber%",
						"<yellow>Die Kosten belaufen sich auf <white>%costperticket%",
						"<yellow>Möchtest du bestätigen? <red>Klicke {hier}!",
						"<yellow>Numbers chosen: <white>%choosennumber%",
						"<yellow>The costs amount to <white>%costperticket%",
						"<yellow>Would you like to confirm? <red>Click {here}!"}));
		languageKeys.put(path+".Arg.AddPot.Added", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Der Pot des klassischen Lottos %lotteryname% wurde von <white>%oldpot% <yellow>auf <white>%actualpot% <yellow>um <white>%added% <yellow>erhöht!",
						"<yellow>The pot of the classic lotto %lotteryname% has been increased from <white>%oldpot% <yellow>to <white>%actualpot% <yellow>by <white>%added% <yellow>!"}));
		languageKeys.put(path+".Arg.SetPot.Set", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Der Pot des klassischen Lottos %lotteryname% wurde von <white>%oldpot% <yellow>auf <white>%actualpot% <yellow>gesetzt!",
						"<yellow>The pot of the classic lotto %lotteryname% was set from <white>%oldpot% <yellow>to <white>%actualpot% <yellow>!"}));
		languageKeys.put(path+".Arg.GiveTicket.Give", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<gold>Gratulation! <yellow>Dir wurde von %player% ein Los in der Lotterie %lotteryname% mit folgenden Zahlen geschenkt: <white>%choosennumber%",
						"<gold>Congratulations! <yellow>You have been given a ticket in the lottery %lotteryname% by %player% with the following numbers: <white>%choosennumber%"}));
		languageKeys.put(path+".Arg.GiveTicket.Given", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Du hast <white>%amount% <yellow>Spielern <white>%value% <yellow>Ticket(s) in der Lotterie %lotteryname% geschenkt!",
						"<yellow>You have given <white>%amount% <yellow>players <white>%value% <yellow>ticket(s) in the %lotteryname% lottery!"}));
		languageKeys.put(path+".DrawNow.OnCooldown", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<white>Achtung! <red>Die Ziehung der Lotterie %lotteryname% besitzt eine Abklingzeit!",
						"<white>Attention! <red>The drawing of the lottery %lotteryname% has a cooldown period!"}));
		languageKeys.put(path+".Arg.Open.AlreayOpen", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Die Lotterie %lotteryname% ist schon eröffnet.",
						"<red>The lottery %lotteryname% is already open."}));
		languageKeys.put(path+".Arg.Open.Open", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Die Lotterie %lotteryname% ist nun eröffnet mit einem aktuellen Jackpot von bis zu %actualpot%<yellow>.",
						"<yellow>The lottery %lotteryname% is now open with a current jackpot of up to %actualpot%<yellow>."}));
		languageKeys.put(path+".Arg.TicketList.NoTickets", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Du hast in dieser Lotterie keine Tickets!",
						"<red>You have no tickets in this lottery!"}));
		languageKeys.put(path+".Arg.TicketList.Headline", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<gray>===== <yellow>Lotterie <gold>%lotteryname% <gray>=====",
						"<gray>===== <yellow>Lottery <gold>%lotteryname% <gray>====="}));
		languageKeys.put(path+".Arg.TicketList.Listed", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>ID: <white>%id% | <red>Status: <white>%status% %repeat%",
						"<red>ID: <white>%id% | <red>Status: <white>%status% %repeat%"}));
		languageKeys.put(path+".Arg.TicketList.StatusWon", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<green>Gewonnen! >> <white>%numbers% <gray>GW<white>%winningclass% : %prize%",
						"<green>Won! >> <white>%numbers% <gray>GW<white>%winningclass% : %prize%"}));
		languageKeys.put(path+".Arg.TicketList.StatusDontWon", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<gray>Verloren >> <white>%numbers%",
						"<gray>Lost >> <white>%numbers%"}));
		languageKeys.put(path+".Arg.TicketList.StatusPending", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Ziehung folgt >> <white>%numbers%",
						"<yellow>Drawing follows >> <white>%numbers%"}));
		languageKeys.put(path+".Arg.TicketList.Repeat", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<click:suggest_command:'%classiclottorepeat%%lotteryname% %id%'><gray>{Wiederholen?}</click>",
						"<click:suggest_command:'%classiclottorepeat%%lotteryname% %id%'><gray>{Repeat?}</click>"}));
		languageKeys.put(path+".Arg.TicketList.DontRepeat", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<click:suggest_command:'%classiclottorepeat%%lotteryname% %id%'><gray>{Nicht wiederholen?}</click>",
						"<click:suggest_command:'%classiclottorepeat%%lotteryname% %id%'><gray>{Not repeat?}</click>"}));
		languageKeys.put(path+".Arg.Repeat.DontExist", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red> Das Ticket %id% der Lotterie %lotteryname% existiert nicht!",
						"<red> The ticket %id% of the lottery %lotteryname% does not exist!"}));
		languageKeys.put(path+".Arg.Repeat.DontYours", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red> Das Ticket %id% der Lotterie %lotteryname% gehört dir nicht!",
						"<red> The ticket %id% of the lottery %lotteryname% does not belong to you!"}));
		languageKeys.put(path+".Arg.Repeat.TicketCreated", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow> Die Ziehung der Lotterie, welche das Ticket angehört, wurde schon gezogen! <green>Ticket für nächste Ziehung neu erstellt!",
						"<yellow> The lottery to which the ticket belongs has already been drawn! <green>New ticket created for next draw!"}));
		languageKeys.put(path+".Arg.Repeat.Repeat", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Das Ticket %id% der Lotterie %lotteryname% wurde wiederholt!",
						"<yellow>The ticket %id% of the lottery %lotteryname% was repeated!"}));
		languageKeys.put(path+".Arg.Repeat.DontRepeat", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Das Ticket %id% der Lotterie %lotteryname% wird <red>nicht <yellow>wiederholt!",
						"<yellow>The ticket %id% of the lottery %lotteryname% will <red>not <yellow>be repeated!"}));
		languageKeys.put(path+".Arg.NextDraw.NoDraws", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Keine nächste Ziehung! Lotterie wird manuell gezogen!",
						"<red>No next drawing! Lottery will be drawn manually!"}));
		languageKeys.put(path+".Arg.NextDraw.NextDraws", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Nächste Ziehung für <white>%lotteryname%<yellow>:",
						"<yellow>Next draw for <white>%lotteryname%<yellow>:"}));
		languageKeys.put(path+".Arg.NextDraw.NextDrawsReplacer", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>%next%",
						"<yellow>%next%"}));
		languageKeys.put(path+".Arg.NextDraw.SeperatorReplacer", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<white>, <yellow>",
						"<white>, <yellow>"}));
	}
	
	private void initLottoSuper()
	{
		String path = "LottoSuper";
		languageKeys.put(path+"", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"",
						""}));
	}
	
	private void initModifierValueEntryLanguage() //INFO:BonusMalusLanguages
	{
		mvelanguageKeys.put(Bypass.Permission.BASE.toString()+".Displayname",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Byasspermission für",
						"<yellow>Bypasspermission for"}));
		mvelanguageKeys.put(Bypass.Permission.BASE.toString()+".Explanation",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Byasspermission für",
						"<yellow>das Plugin BaseTemplate",
						"<yellow>Bypasspermission for",
						"<yellow>the plugin BaseTemplate"}));
		mvelanguageKeys.put(Bypass.Counter.BASE.toString()+".Displayname",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Zählpermission für",
						"<yellow>Countpermission for"}));
		mvelanguageKeys.put(Bypass.Counter.BASE.toString()+".Explanation",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Zählpermission für",
						"<yellow>das Plugin BaseTemplate",
						"<yellow>Countpermission for",
						"<yellow>the plugin BaseTemplate"}));
	}
	
	private void initDefaultLottery()
	{
		initDefaultClassicLotto();
		initDefaultLottoSuper();
	}
	
	private void initDefaultClassicLotto()
	{
		Lottery.GameType type = GameType.X_FROM_Y;
		LinkedHashMap<String, LinkedHashMap<String, Language>> mapI = new LinkedHashMap<>();
		LinkedHashMap<String, Language> mapII = new LinkedHashMap<>();
		mapII.put("LotteryName", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					"Lotto"}));
		mapII.put("#LotteryName", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Name der Lotterie. Dieser MUSS einzigartig sein!",
					"",
					"Name of the lottery. This MUST be unique!"}));
		mapII.put("Description", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"<yellow>Das klassische Lotto, aber ohne Superzahl/Super 6/Spiel 77/Glücksspirale"
					+ " ist eine Ziehung von 6 aus 49. Der Ablauf ist ziemlich einfach."
					+ " Du wählst dir 6 sich nicht wiederholende Zahlen aus 1 bis 49."
					+ " Die Gewinnchance ist 1:13.983.816.",
					"<yellow>The classic lottery, but without Super Number/Super 6/Game 77/Glücksspirale,"
					+ " is a drawing of 6 out of 49. The process is quite simple."
					+ " You choose 6 non-repeating numbers from 1 to 49."
					+ " The chance of winning is 1:13,983,816."}));
		mapII.put("#Description", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Die Beschreibung sollte das Spiel in ein paar Sätzen erklären.",
					"",
					"The description should explain the game in a few sentences."}));
		mapII.put("StandartPot", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					1000_000.0}));
		mapII.put("#StandartPot", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Die Mindestmenge an Geld im Jackpot.",
					"",
					"The minimum amount of money in the jackpot."}));
		mapII.put("MaximumPot", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					10000000.0}));
		mapII.put("#MaximumPot", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Die Menge an Geld wie hoch der Jackpot maximal werden kann.",
					"",
					"The amount of money the jackpot can reach at most"}));
		mapII.put("AmountToAddToThePotIfNoOneIsWinning", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					500000.0D}));
		mapII.put("#AmountToAddToThePotIfNoOneIsWinning", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Die Menge an Geld, die dem JackPot hinzugefügt wird, wenn keiner die höchste Gewinnklasse gewinnt.",
					"Dabei wird der Teil des Pots genommen, der nicht gewonnen wurde um dieser Menger addiert.",
					"",
					"The amount of money added to the jackpot if no one wins the highest prize class.",
					"The part of the pot that was not won is taken and added to this amount."}));
		mapII.put("CostPerTicket", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					2.5}));
		mapII.put("#CostPerTicket", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Die Menge an Geld, was ein Los dieser Lotterie kostet.",
					"",
					"The amount of money a ticket in this lottery costs."}));
		mapII.put("MaximalAmountOfTicketWhichCanAPlayerBuy", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					-1}));
		mapII.put("#MaximalAmountOfTicketWhichCanAPlayerBuy", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Die Menge an Lose ein Spieler für ein Lotteriedurchgang kaufen kann. -1 ist Unendlich.",
					"",
					"The number of tickets a player can buy for one lottery round. -1 is infinite."}));
		mapII.put("FristNumberToChooseFrom", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					1}));
		mapII.put("#FristNumberToChooseFrom", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Deklariert die Erste Zahl aus den zu wählenden Zahlen.",
					"",
					"Declares the first number from the numbers to be selected."}));
		mapII.put("LastNumberToChooseFrom", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					49}));
		mapII.put("#LastNumberToChooseFrom", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Deklariert die Letzte Zahl aus den zu wählenden Zahlen.",
					"",
					"Declares the last number from the numbers to be selected."}));
		mapII.put("AmountOfChoosedNumber", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					6}));
		mapII.put("#AmountOfChoosedNumber", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Deklariert wieviele Zahlen man aus den zu wählenden Zahlenbereich wählen darf.",
					"",
					"Declares how many numbers you can choose from the range of numbers to be selected."}));
		mapII.put("DrawOnServer", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					"hub"}));
		mapII.put("#DrawOnServer", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					"",
					"Der Server, wo die Ziehung erfolgen soll. Solltest nur ein Spigot/Paper/etc. laufen, kannste das ignorieren.",
					"",
					"The server where the draw should take place. If only one Spigot/Paper/etc. is running, you can ignore this."}));
		mapII.put("DrawManually", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					false}));
		mapII.put("#DrawManually", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					"",
					"Wenn `true`, dann wird das Lotto nur über einen Befehl aufgelöst und die Zahlen gezogen.",
					"",
					"If `true`, then the lottery will only be solved via one command and the numbers will be drawn."}));
		mapII.put("DrawTime", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					"1-WEDNESDAY-20-00",
					"1-SATURDAY-20-00",
					"2-WEDNESDAY-20-00",
					"2-SATURDAY-20-00",
					"3-WEDNESDAY-20-00",
					"3-SATURDAY-20-00",
					"4-WEDNESDAY-20-00",
					"4-SATURDAY-20-00",
					"5-WEDNESDAY-20-00",
					"5-SATURDAY-20-00"}));
		mapII.put("#DrawTime", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Deklariert, wann die Lotterie gezogen wird. Es können mehrere Zeiten festgelegt werden.",
					"Dabei wird zuerst immer die Woche des Monats als Zahl genannt, dann der Wochentag in Großbuchstaben, dann die Stunden und Minutenzahl.",
					"Bedenke, dass es auch Monate mit 5 Wochen gibt. Nach der ISO Regel ist aber bspw. der 1. Dezember 2024 zum 4. Woche des November gehörend.",
					"Da immer dort wo der erste Donnerstag im Monat ist, die erste Woche des Monats ist.",
					"",
					"Declares when the lottery will be drawn. Multiple times can be specified.",
					"The week of the momnth is always given first as number, than the day of the week given first in capital letters, followed by the hours and minutes.",
					"Remember that there are also months with 5 weeks. However, according to the ISO rule, for example, December 1, 2024 belongs to the 4th week of November.",
					"Because wherever the first Thursday of the month is, that is the first week of the month."}));
		// ----------
		mapII.put("WinningClass.1.PayoutType", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					PayoutType.PERCENTAGE.toString()}));
		mapII.put("#WinningClass.1.PayoutType", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Der PayoutType kann aus folgenden Werten bestehen: LUMP_SUM oder PERCENTAGE",
					"LUM_SUM gibt allen Gewinnern dieser Gewinnklasse den selben Pauschalbetrag.",
					"PERCENTAGE nimmt vom Gesamtpot den Prozentwert und teil diesen unter allen Gewinnern der Gewinnklasse.",
					"Was gilt wird in in dem Pfad 'WinningClass.Zahl.Payout' dann definiert.",
					"",
					"The PayoutType can consist of the following values: LUMP_SUM or PERCENTAGE",
					"LUM_SUM gives all winners in this prize category the same lump sum amount.",
					"PERCENTAGE takes the percentage value of the total pot and divides it among all winners in the prize category.",
					"What applies is then defined in the path 'WinningClass.Zahl.Payout'."}));
		mapII.put("WinningClass.1.Payout", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					51.0}));
		mapII.put("#WinningClass.1.Payout", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Umso niedriger das Level der Gewinnklasse umso weniger gewinnt man etwas.",
					"Beim Klassischen Lotto ist die Anzahl der Gewinnklasse die gleiche, wie die Anzahl an Zahlen der Spieler wählen darf!",
					"In der Realität ist der Hauptgewinn eine Lotterie immer um die 50%. Jedoch ist alles außer gleich oder weniger als 0 möglich.",
					"",
					"The lower the level of the prize class, the less you win.",
					"In Classic Lotto, the number of winning categories is the same as the number of numbers the player can choose!",
					"In reality, the main prize of a lottery is always around 40-50%. However, anything but equal to or less than 0 is possible."}));
		mapII.put("WinningClass.1.NumberMatchToWin", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					6}));
		mapII.put("#WinningClass.1.NumberMatchToWin", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Die Anzahl an Lottozahlen, die den vom Spieler gewählten Zahlen gleicht um den Preis in dieser Gewinnklasse zu erhalten.",
					"In der Realität kann es sein, dass wenn mehr als 8 Zahlen gezogen werden, dass meist in den schlechteren Gewinnklassen die ersten Richtigen übersprungen werden.",
					"Bedeutet, wenn man theoretisch 8 Zahlen ziehen würde könnte man nur 7 oder 6 Gewinnklassen machen, dass somit um einen Preis zu gewinnen zu können, man mindestens 3 richtige Zahlen haben muss.",
					"",
					"The number of lottery numbers that match the numbers chosen by the player to win the prize in that prize category.",
					"In reality, it is possible that if more than 8 numbers are drawn, the first correct numbers are usually skipped in the lower prize categories.",
					"This means that if you would theoretically draw 8 numbers you could only make 7 or 6 winning classes, so to win a prize you have to have at least 3 correct numbers."}));
		//------------
		mapII.put("WinningClass.2.PayoutType", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					PayoutType.PERCENTAGE.toString()}));
		mapII.put("WinningClass.2.Payout", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					25.5}));
		mapII.put("WinningClass.2.NumberMatchToWin", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					5}));
		//------------
		mapII.put("WinningClass.3.PayoutType", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					PayoutType.PERCENTAGE.toString()}));
		mapII.put("WinningClass.3.Payout", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					12.56}));
		mapII.put("WinningClass.3.NumberMatchToWin", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					4}));
		// ----------
		mapII.put("WinningClass.4.PayoutType", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					PayoutType.PERCENTAGE.toString()}));
		mapII.put("WinningClass.4.Payout", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					6.252}));
		mapII.put("WinningClass.4.NumberMatchToWin", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					5}));	
		// ----------
		mapII.put("WinningClass.5.PayoutType", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					PayoutType.LUMP_SUM.toString()}));
		mapII.put("WinningClass.5.Payout", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					3.1255}));
		mapII.put("WinningClass.5.NumberMatchToWin", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					2}));
		//----------
		mapII.put("WinningClass.6.PayoutType", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					PayoutType.LUMP_SUM.toString()}));
		mapII.put("WinningClass.6.Payout", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					1.5625}));
		mapII.put("WinningClass.6.NumberMatchToWin", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					1}));
		mapII.put("Advertising.1.IsActive", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					true}));
		mapII.put("#Advertising.1.IsActive", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Wenn 'true', dann wird die Nachricht gesendet.",
					"",
					"If 'true', the message is sent."}));
		mapII.put("Advertising.1.CanIgnore", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					false}));
		mapII.put("#Advertising.1.CanIgnore", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Der Boolean, welcher definiert, ob Spieler mit dem toggeln des Ingamebefehls diese Nachricht ausblenden kann oder nicht.",
					"",
					"The boolean that defines whether or not the player can hide this message by toggling the ingame command."}));
		mapII.put("Advertising.1.Message", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"<#d2b773>...}/=== <white>%lotteryname% <#d2b773>===\\{...",
					"<gold>Aktueller JackPot bis zu <white>%actualpot%<gold>!",
					"<aqua>Hauptgewinn <white>%highestwinningclass%<aqua>!",
					"<red>Heute schon Lotto gespielt? <click:run_command:'%classiclottobet%%lotteryname%'><white><yellow>Klicke {hier}</click>",
					"<#d2b773>...}/=====  ^^^^^^^^^^  =====\\\\{...",
					"<#d2b773>...}/=== <gold>%lotteryname% <#d2b773>===\\\\\\\\{...",
					"<gold>JackPot up to <white>%actualpot%<gold>!",
					"<aqua>Main prize <white>%highestwinningclass%<aqua>!",
					"<red>Have you played the lottery today? <click:run_command:'%classiclottobet%%lotteryname%'><white><yellow>Click {here}</click>",
					"<#d2b773>...}/=====  ^^^^^^^^^^  =====\\\\{..."}));
		mapII.put("#Advertising.1.Message", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Die Nachricht, welchen allen auf dem Server Spielern gesendet werden soll von dieser Lotterie.",
					"",
					"The message to be sent to all players on the server from this lottery."}));
		mapII.put("Advertising.1.Time", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					"1-MONDAY-14-00",
					"1-TUESDAY-14-00",
					"1-WEDNESDAY-14-00",
					"1-THURSDAY-14-00",
					"1-FRIDAY-14-00",
					"1-SATURDAY-14-00",
					"1-SUNDAY-14-00",
					"2-MONDAY-14-00",
					"2-TUESDAY-14-00",
					"2-WEDNESDAY-14-00",
					"2-THURSDAY-14-00",
					"2-FRIDAY-14-00",
					"2-SATURDAY-14-00",
					"2-SUNDAY-14-00",
					"3-MONDAY-14-00",
					"3-TUESDAY-14-00",
					"3-WEDNESDAY-14-00",
					"3-THURSDAY-14-00",
					"3-FRIDAY-14-00",
					"3-SATURDAY-14-00",
					"3-SUNDAY-14-00",
					"4-MONDAY-14-00",
					"4-TUESDAY-14-00",
					"4-WEDNESDAY-14-00",
					"4-THURSDAY-14-00",
					"4-FRIDAY-14-00",
					"4-SATURDAY-14-00",
					"4-SUNDAY-14-00",
					"5-MONDAY-14-00",
					"5-TUESDAY-14-00",
					"5-WEDNESDAY-14-00",
					"5-THURSDAY-14-00",
					"5-FRIDAY-14-00",
					"5-SATURDAY-14-00",
					"5-SUNDAY-14-00",}));
		mapII.put("#Advertising.1.Time", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Die Zeiten, wann diese Nachrichten den Spieler geschickt werden soll. Gleiches Prinzip wie bei 'DrawTime'.",
					"",
					"The times when these messages should be sent to the player. Same principle as for 'DrawTime'."}));
		mapI.put("Lotto", mapII);
		getLottery().put(type, mapI);
	}
	
	private void initDefaultLottoSuper()
	{
		Lottery.GameType type = GameType.X_FROM_Y_AND_Z_FROM_U;
		LinkedHashMap<String, LinkedHashMap<String, Language>> mapI = new LinkedHashMap<>();
		LinkedHashMap<String, Language> mapII = new LinkedHashMap<>();
		mapII.put("LotteryName", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					"PowerBall"}));
		mapII.put("#LotteryName", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Name der Lotterie. Dieser MUSS einzigartig sein!",
					"",
					"Name of the lottery. This MUST be unique!"}));
		mapII.put("Description", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"<yellow>Die Super Lotterie. Ausgewählt werden 5 Zahlen aus 69 sowie 1 Zahl aus 26. Gewinnchance ist 1:292.201.338",
					"<yellow>The Super Lottery. 5 numbers from 69 and 1 number from 26 are selected. Chance of winning is 1:292,201,338"}));
		mapII.put("#Description", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Die Beschreibung sollte das Spiel in ein paar Sätzen erklären.",
					"",
					"The description should explain the game in a few sentences."}));
		mapII.put("StandartPot", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				Double.valueOf(40_000_000.0)}));
		mapII.put("#StandartPot", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Die Mindestmenge an Geld im Jackpot.",
					"",
					"The minimum amount of money in the jackpot."}));
		mapII.put("MaximumPot", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				Double.valueOf(Double.MAX_VALUE)}));
		mapII.put("#MaximumPot", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Die Menge an Geld wie hoch der Jackpot maximal werden kann.",
					"",
					"The amount of money the jackpot can reach at most."}));
		mapII.put("AmountToAddToThePotIfNoOneIsWinning", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				Double.valueOf(10_000_000.0)}));
		mapII.put("#AmountToAddToThePotIfNoOneIsWinning", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Die Menge an Geld, die dem JackPot hinzugefügt wird, wenn keiner die höchste Gewinnklasse gewinnt.",
					"Dabei wird der Teil des Pots genommen, der nicht gewonnen wurde um dieser Menger addiert.",
					"",
					"The amount of money added to the jackpot if no one wins the highest prize class.",
					"The part of the pot that was not won is taken and added to this amount."}));
		mapII.put("CostPerTicket", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					2.0}));
		mapII.put("#CostPerTicket", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Die Menge an Geld, was ein Los dieser Lotterie kostet.",
					"",
					"The amount of money a ticket in this lottery costs."}));
		mapII.put("MaximalAmountOfTicketWhichCanAPlayerBuy", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					-1}));
		mapII.put("#MaximalAmountOfTicketWhichCanAPlayerBuy", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Die Menge an Lose ein Spieler für ein Lotteriedurchgang kaufen kann. -1 ist Unendlich.",
					"",
					"The number of tickets a player can buy for one lottery round. -1 is infinite."}));
		mapII.put("FristNumberToChooseFrom", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					1}));
		mapII.put("#FristNumberToChooseFrom", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Deklariert die Erste Zahl aus den zu wählenden Zahlen.",
					"",
					"Declares the first number from the numbers to be selected."}));
		mapII.put("LastNumberToChooseFrom", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					69}));
		mapII.put("#LastNumberToChooseFrom", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Deklariert die Letzte Zahl aus den zu wählenden Zahlen.",
					"",
					"Declares the last number from the numbers to be selected."}));
		mapII.put("AmountOfChoosedNumber", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					5}));
		mapII.put("#AmountOfChoosedNumber", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Deklariert wieviele Zahlen man aus den zu wählenden Zahlenbereich wählen darf.",
					"",
					"Declares how many numbers you can choose from the range of numbers to be selected."}));
		mapII.put("AdditionalFristNumberToChooseFrom", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					1}));
		mapII.put("#AdditionalFristNumberToChooseFrom", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Deklariert die Erste Zahl aus den zu wählenden additionalen Zahlen.",
					"",
					"Declares the first number from the additional numbers to be selected."}));
		mapII.put("AdditionalLastNumberToChooseFrom", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					26}));
		mapII.put("#AdditionalLastNumberToChooseFrom", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Deklariert die Letzte Zahl aus den zu wählenden additionalen Zahlen.",
					"",
					"Declares the last number from the additional numbers to be selected."}));
		mapII.put("AdditionalAmountOfChoosedNumber", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					1}));
		mapII.put("#AdditionalAmountOfChoosedNumber", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Deklariert wieviele Zahlen man aus den zu wählenden additionalen Zahlenbereich wählen darf.",
					"",
					"Declares how many numbers you can choose from the range of additional numbers to be selected."}));
		mapII.put("DrawOnServer", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					"hub"}));
		mapII.put("#DrawOnServer", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					"",
					"Der Server, wo die Ziehung erfolgen soll. Solltest nur ein Spigot/Paper/etc. laufen, kannste das ignorieren.",
					"",
					"The server where the draw should take place. If only one Spigot/Paper/etc. is running, you can ignore this."}));
		mapII.put("DrawManually", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					false}));
		mapII.put("#DrawManually", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					"",
					"Wenn `true`, dann wird das Lotto nur über einen Befehl aufgelöst und die Zahlen gezogen.",
					"",
					"If `true`, then the lottery will only be solved via one command and the numbers will be drawn."}));
		mapII.put("DrawTime", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					"1-MONDAY-22-59",
					"1-WEDNESDAY-22-59",
					"1-SATURDAY-22-59",
					"2-MONDAY-22-59",
					"2-WEDNESDAY-22-59",
					"2-SATURDAY-22-59",
					"3-MONDAY-22-59",
					"3-WEDNESDAY-22-59",
					"3-SATURDAY-22-59",
					"4-MONDAY-22-59",
					"4-WEDNESDAY-22-59",
					"4-SATURDAY-22-59",
					"5-MONDAY-22-59",
					"5-WEDNESDAY-22-59",
					"5-SATURDAY-22-59"}));
		mapII.put("#DrawTime", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Deklariert, wann die Lotterie gezogen wird. Es können mehrere Zeiten festgelegt werden.",
					"Dabei wird zuerst immer die Woche des Monats als Zahl genannt, dann der Wochentag in Großbuchstaben, dann die Stunden und Minutenzahl.",
					"",
					"Declares when the lottery will be drawn. Multiple times can be specified.",
					"The week of the momnth is always given first as number, than the day of the week given first in capital letters, followed by the hours and minutes."}));
		mapII.put("WinningClass.1.PayoutType", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					PayoutType.PERCENTAGE.toString()}));
		mapII.put("#WinningClass.1.PayoutType", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Der PayoutType kann aus folgenden Werten bestehen: LUMP_SUM oder PERCENTAGE",
					"LUM_SUM gibt allen Gewinnern dieser Gewinnklasse den selben Pauschalbetrag.",
					"PERCENTAGE nimt vom Gesamtpot den Prozentwert und teil diesen unter allen Gewinnern der Gewinnklasse.",
					"Was gilt wird in in dem Pfad 'WinningClass.Zahl.Payout' dann definiert.",
					"",
					"The PayoutType can consist of the following values: LUMP_SUM or PERCENTAGE",
					"LUM_SUM gives all winners in this prize category the same lump sum amount.",
					"PERCENTAGE takes the percentage value of the total pot and divides it among all winners in the prize category.",
					"What applies is then defined in the path 'WinningClass.Zahl.Payout'."}));
		mapII.put("WinningClass.1.Payout", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					45.0}));
		mapII.put("#WinningClass.1.Payout", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Lotto zahlt vom Jackpot immer eine gewissen Prozenzanteil an die Gewinner einer Gewinnklasse aus.",
					"Dabei teilen sich alle Gewinner der Gleichen Gewinnklasse diesen Anteil.",
					"Umso höher das Level der Gewinnklasse umso weniger gewinnt man etwas.",
					"",
					"Lotto always pays out a certain percentage of the jackpot to the winners of a prize class.",
					"All winners of the same prize class share this share.",
					"The higher the level of the prize class, the less you win."}));
		mapII.put("WinningClass.1.NumberMatchToWin", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					5}));
		mapII.put("#WinningClass.1.NumberMatchToWin", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Die Anzahl an Lottozahlen, die den vom Spieler gewählten Zahlen gleicht um den Preis in dieser Gewinnklasse zu erhalten.",
					"=> https://www.powerball.com/powerball-prize-chart",
					"",
					"The number of lottery numbers that match the numbers chosen by the player to win the prize in that prize category.",
					"=> https://www.powerball.com/powerball-prize-chart"}));
		mapII.put("WinningClass.1.AddtionalNumberMatchToWin", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					1}));
		mapII.put("#WinningClass.9.AddtionalNumberMatchToWin", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Die Anzahl an additionalen Lottozahlen (meist SuperZahlen genannt),",
					"die den vom Spieler gewählten additionalen Zahlen gleicht um den Preis in dieser Gewinnklasse zu erhalten. (In Kombination mit den normalen Zahlen)",
					"",
					"The number of lottery numbers that match the numbers chosen",
					"by the player to win the prize in that prize category. (In combination with the normal numbers)"}));
		mapII.put("WinningClass.2.PayoutType", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					PayoutType.PERCENTAGE.toString()}));
		mapII.put("WinningClass.2.Payout", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					22.0}));
		mapII.put("WinningClass.2.NumberMatchToWin", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					5}));
		mapII.put("WinningClass.2.AddtionalNumberMatchToWin", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					0}));
		mapII.put("WinningClass.3.PayoutType", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					PayoutType.PERCENTAGE.toString()}));
		mapII.put("WinningClass.3.Payout", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					12.0}));
		mapII.put("WinningClass.3.NumberMatchToWin", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					4}));
		mapII.put("WinningClass.3.AddtionalNumberMatchToWin", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					1}));
		mapII.put("WinningClass.4.PayoutType", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					PayoutType.PERCENTAGE.toString()}));
		mapII.put("WinningClass.4.Payout", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					9.0}));
		mapII.put("WinningClass.4.NumberMatchToWin", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					4}));
		mapII.put("WinningClass.4.AddtionalNumberMatchToWin", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					0}));
		mapII.put("WinningClass.5.PayoutType", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					PayoutType.PERCENTAGE.toString()}));
		mapII.put("WinningClass.5.Payout", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					5.0}));
		mapII.put("WinningClass.5.NumberMatchToWin", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					3}));
		mapII.put("WinningClass.5.AddtionalNumberMatchToWin", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					1}));
		mapII.put("WinningClass.6.PayoutType", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					PayoutType.PERCENTAGE.toString()}));
		mapII.put("WinningClass.6.Payout", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					4.0}));
		mapII.put("WinningClass.6.NumberMatchToWin", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					3}));
		mapII.put("WinningClass.6.AddtionalNumberMatchToWin", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					0}));
		mapII.put("WinningClass.7.PayoutType", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					PayoutType.PERCENTAGE.toString()}));
		mapII.put("WinningClass.7.Payout", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					3.0}));
		mapII.put("WinningClass.7.NumberMatchToWin", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					2}));
		mapII.put("WinningClass.7.AddtionalNumberMatchToWin", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					1}));
		mapII.put("WinningClass.8.PayoutType", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					PayoutType.LUMP_SUM.toString()}));
		mapII.put("WinningClass.8.Payout", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					5.0}));
		mapII.put("WinningClass.8.NumberMatchToWin", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					1}));
		mapII.put("WinningClass.8.AddtionalNumberMatchToWin", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					1}));
		mapII.put("WinningClass.9.PayoutType", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					PayoutType.LUMP_SUM.toString()}));
		mapII.put("WinningClass.9.Payout", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					2.5}));
		mapII.put("WinningClass.9.NumberMatchToWin", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					0}));
		mapII.put("WinningClass.9.AddtionalNumberMatchToWin", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					1}));
		mapI.put("PowerBall", mapII);
		getLottery().put(type, mapI);
	}
}