package me.avankziar.lly.general.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;

import me.avankziar.lly.general.database.Language.ISO639_2B;
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
							case "</dark_blue>":
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
							case "<dark_blue>":
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
								r = "&6";
								break;
							case "<gray>":
								r = "&7";
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
								r = "&n";
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
		
		configKeys.put("EnableCommands.Base"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				true}));
		
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
		/*
		 * The "Stringlist" are define so.
		 */
		configKeys.put("GuiFlatFileNames"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				"guiOne",
				"guiTwo",}));
		/*
		 * If there was a second language, with also 2 entry, so would Entry 1 and two for the first and 3 and 4 four the second language.
		 */	
	}
	
	@SuppressWarnings("unused") //INFO:Commands
	public void initCommands()
	{
		comBypass();
		String path = "";
		commandsInput("path", "base", "perm.command.perm", 
				"/base [pagenumber]", "/base ", false,
				"<red>/base <white>| Infoseite für alle Befehle.",
				"<red>/base <white>| Info page for all commands.",
				"<aqua>Befehlsrecht für <white>/base",
				"<aqua>Commandright for <white>/base",
				"<yellow>Basisbefehl für das BaseTemplate Plugin.",
				"<yellow>Groundcommand for the BaseTemplate Plugin.");
		String basePermission = "perm.base.";
		argumentInput("base_argument", "argument", basePermission,
				"/base argument <id>", "/econ deletelog ", false,
				"<red>/base argument <white>| Ein Subbefehl",
				"<red>/base argument <white>| A Subcommand.",
				"<aqua>Befehlsrecht für <white>/base argument",
				"<aqua>Commandright for <white>/base argument",
				"<yellow>Basisbefehl für das BaseTemplate Plugin.",
				"<yellow>Groundcommand for the BaseTemplate Plugin.");
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
						"<yellow>=====&7[&6BungeeTeleportManager&7]<yellow>=====",
						"<yellow>=====&7[&6BungeeTeleportManager&7]<yellow>====="}));
		languageKeys.put("Next", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>&nnächste Seite <yellow>==>",
						"<yellow>&nnext page <yellow>==>"}));
		languageKeys.put("Past", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow><== &nvorherige Seite",
						"<yellow><== &nprevious page"}));
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
		languageKeys.put("WasntDrawn", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Ziehung ausstehend</red>",
						"<red>drawing pending</red>"}));
		initClassicLotto();
	}
	
	private void initClassicLotto()
	{
		String path = "ClassicLotto";
		languageKeys.put(path+".Draw.NoTicketAreBought", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<dark_blue>...}/=== <gold>%lotteryname% <white>Ziehung <dark_blue>===\\{...",
						"<gold>JackPot bis zu <white>%totalpot%<gold>! <aqua>Hauptgewinn <white>%highestwinningcatgeory%<aqua>!",
						"<yellow>Gezogene Nummer: <reset>%drawnnumber%",
						"<red>Keine Lose wurden verkauft! Ziehung ist ungültig",
						"<dark_blue>...}/=====  ^^^^^^^^^^  =====\\\\{...",
						"",
						"",
						""}));
		languageKeys.put(path+".Draw.RepeatTicket.Category", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"%lottoryname%-Ticket",
						"%lottoryname%-Ticket"}));
		languageKeys.put(path+".Draw.RepeatTicket.Comment", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"Lotterie Ticket wurde automatisch wiedergekauft.",
						"Lottery ticket was automatically repurchased."}));
		languageKeys.put(path+".Draw.WinningCategoryReplacer", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<gray>GW%level%: <white>%winneramount%",
						"<gray>GW%level%: <white>%winneramount%"}));
		languageKeys.put(path+".Draw.WinningCategoryReplacerSeperator", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<gray>, ",
						"<gray>, "}));
		languageKeys.put(path+".Draw.JackpotWasBreached", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<dark_blue>...}/=== <gold>%lotteryname% <white>Ziehung <dark_blue>===\\{...",
						"<yellow>Folgend Zahlen wurden gezogen: <white><bold>%drawnnumber%",
						"<gold>Gratulation an <white>%winners% <gold>zum knacken des Jackpots!",
						"<gold>Gewonnen wurden <red><bold>%payout% <reset><gold>verteilt auf alle Gewinner.",
						"<gray>Folgend die Anzahl der Gewinner in allen Gewinnkategorien:",
						"<gray>%winningcategorywinneramount%",
						"<yellow>Im nächsten Lotteriepot sind insgesamt bis zu satte ...",
						"<aqua><bold>%nexttotalpot% zu gewinnen!",
						"<gray>(%nextpot% + %amounttoaddpot%)",
						"<dark_blue>...}/=====  ^^^^^^^^^^  =====\\\\{...",
						""}));
		languageKeys.put(path+".Draw.JackpotIsUntouched", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<dark_blue>...}/=== <gold>%lotteryname% <white>Ziehung <dark_blue>===\\{...",
						"<yellow>Folgend Zahlen wurden gezogen: <white><bold>%drawnnumber%",
						"<gray><bold>Leider gab es keinen Hauptgewinner!",
						"<gray>Folgend die Anzahl der Gewinner in allen Gewinnkategorien:",
						"<gray>%winningcategorywinneramount%",
						"<yellow>Dem nächsten Lotteriepot:",
						"<yellow>Im nächsten Lotteriepot sind insgesamt bis zu satte ...",
						"<aqua><bold>%nexttotalpot% zu gewinnen!",
						"<gray>(%nextpot% + %amounttoaddpot%)",
						"<dark_blue>...}/=====  ^^^^^^^^^^  =====\\\\{...",
						""}));
		languageKeys.put(path+".Draw.Won", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"        <gold><bold>! GEWONNEN !",
						"<yellow>Du hattest %matchchoosennumberamount% richtige!",
						"<gray>Deine Nummern:<reset> %matchchoosennumber%",
						"<yellow>Dein Preis: %payout%",
						"        <gold><bold>! GEWONNEN !",
						""}));
		languageKeys.put(path+".Draw.NotWon", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Schade! Leider hast du nichts gewonnen.",
						"<yellow>Beim nächsten Mal!",
						""}));
		languageKeys.put(path+".Draw.Win.Category", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"%lottoryname%-Gewinn",
						"%lottoryname%-Prize"}));
		languageKeys.put(path+".Draw.Win.Comment", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"Gewonnen in der Gewinnkategory %level%.",
						"Won in the winning category %level%."}));
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
					+ "Du wählst dir 6 sich nicht wiederholende Zahlen aus 1 bis 49."
					+ "Die Gewinnchance ist 1:13.983.816.",
					"The classic lottery, but without Super Number/Super 6/Game 77/Glücksspirale,"
					+ " is a drawing of 6 out of 49. The process is quite simple."
					+ "You choose 6 non-repeating numbers from 1 to 49."
					+ "The chance of winning is 1:13,983,816."}));
		mapII.put("#Description", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Die Beschreibung sollte das Spiel in ein paar Sätzen erklären.",
					"",
					"The description should explain the game in a few sentences."}));
		mapII.put("StandartPot", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					1_000_000}));
		mapII.put("#StandartPot", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Die Mindestmenge an Geld im Jackpot.",
					"",
					"The minimum amount of money in the jackpot."}));
		mapII.put("MaximumPot", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					10_000_000}));
		mapII.put("#MaximumPot", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Die Menge an Geld wie hoch der Jackpot maximal werden kann.",
					"",
					"The amount of money the jackpot can reach at most"}));
		mapII.put("AmountToAddToThePotIfNoOneIsWinning", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					500_000}));
		mapII.put("#AmountToAddToThePotIfNoOneIsWinning", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Die Menge an Geld, die dem JackPot hinzugefügt wird, wenn keiner die höchste Gewinnklasse gewinnt.",
					"Dabei wird der Teil des Pots genommen, der nicht gewonnen wurde um dieser Menger addiert.",
					"",
					"The amount of money added to the jackpot if no one wins the highest prize category.",
					"The part of the pot that was not won is taken and added to this amount."}));
		mapII.put("CostPerTicket", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					2.5}));
		mapII.put("#CostPerTicket", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Die Menge an Geld, was ein Los dieser Lotterie kostet.",
					"",
					"The amount of money a ticket in this lottery costs."}));
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
		mapII.put("#DrawTime", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Deklariert, wann die Lotterie gezogen wird. Es können mehrere Zeiten festgelegt werden.",
					"Dabei wird zuerst immer der Wochentag in Großbuchstaben genannt, dann die Stunden und Minutenzahl.",
					"",
					"Declares when the lottery will be drawn. Multiple times can be specified.",
					"The day of the week is always given first in capital letters, followed by the hours and minutes."}));
		mapII.put("DrawTime", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					"WEDNESDAY-20-00",
					"SATURDAY-20-00"}));
		mapII.put("#DrawTime", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Deklariert, wann die Lotterie gezogen wird. Es können mehrere Zeiten festgelegt werden.",
					"Dabei wird zuerst immer der Wochentag in Großbuchstaben genannt, dann die Stunden und Minutenzahl.",
					"",
					"Declares when the lottery will be drawn. Multiple times can be specified.",
					"The day of the week is always given first in capital letters, followed by the hours and minutes."}));
		mapII.put("WinningCategory.1.PayoutPercentage", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					1.5625}));
		mapII.put("#WinningCategory.1.PayoutPercentage", new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
					"",
					"Lotto zahlt vom Jackpot immer eine gewissen Prozenzanteil an die Gewinner einer Gewinnkategorie aus.",
					"Dabei teilen sich alle Gewinner der Gleichen Gewinnkategorie diesen Anteil.",
					"Umso niedriger das Level der Gewinnkategorie umso weniger gewinnt man etwas.",
					"Die Prozentanteile aller Gewinnkategorien sollten nicht 100.0% übersteigen.",
					"Beim Klassischen Lotto ist die Anzahl der Gewinnkategory die gleiche, wie die Anzahl an Zahlen der Spieler wählen darf!",
					"",
					"Lotto always pays out a certain percentage of the jackpot to the winners of a prize category.",
					"All winners of the same prize category share this share.",
					"The lower the level of the prize category, the less you win.",
					"The percentages of all prize categories should not exceed 100.0%.",
					"In Classic Lotto, the number of winning categories is the same as the number of numbers the player can choose!"}));
		mapII.put("WinningCategory.2.PayoutPercentage", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					3.1255}));
		mapII.put("WinningCategory.3.PayoutPercentage", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					6.252}));
		mapII.put("WinningCategory.4.PayoutPercentage", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					12.56}));
		mapII.put("WinningCategory.5.PayoutPercentage", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					25.5}));
		mapII.put("WinningCategory.6.PayoutPercentage", new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					51.0}));
		mapI.put("Lotto", mapII);
		getLottery().put(type, mapI);
	}
}