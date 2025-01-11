package me.avankziar.lly.spigot;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.avankziar.ifh.general.modifier.ModificationType;
import me.avankziar.ifh.general.modifier.Modifier;
import me.avankziar.ifh.general.valueentry.ValueEntry;
import me.avankziar.ifh.spigot.administration.Administration;
import me.avankziar.ifh.spigot.economy.Economy;
import me.avankziar.ifh.spigot.interfaces.ProxyOnlinePlayers;
import me.avankziar.ifh.spigot.sendable.Parcel;
import me.avankziar.ifh.spigot.tovelocity.chatlike.MessageToVelocity;
import me.avankziar.ifh.spigot.tovelocity.commands.CommandToVelocity;
import me.avankziar.lly.general.assistance.Utility;
import me.avankziar.lly.general.cmdtree.ArgumentConstructor;
import me.avankziar.lly.general.cmdtree.BaseConstructor;
import me.avankziar.lly.general.cmdtree.CommandConstructor;
import me.avankziar.lly.general.cmdtree.CommandSuggest;
import me.avankziar.lly.general.database.ServerType;
import me.avankziar.lly.general.database.YamlHandler;
import me.avankziar.lly.general.database.YamlManager;
import me.avankziar.lly.general.objects.lottery.ClassicLotto;
import me.avankziar.lly.general.objects.lottery.LottoSuper;
import me.avankziar.lly.general.objects.lottery.ScratchCard;
import me.avankziar.lly.spigot.ModifierValueEntry.Bypass;
import me.avankziar.lly.spigot.assistance.BackgroundTask;
import me.avankziar.lly.spigot.cmd.ClassicLottoCommandExecutor;
import me.avankziar.lly.spigot.cmd.LottoSuperCommandExecutor;
import me.avankziar.lly.spigot.cmd.LuckyLotteryCommandExecutor;
import me.avankziar.lly.spigot.cmd.TabCompletion;
import me.avankziar.lly.spigot.cmdtree.ArgumentModule;
import me.avankziar.lly.spigot.database.MysqlHandler;
import me.avankziar.lly.spigot.database.MysqlSetup;
import me.avankziar.lly.spigot.handler.ConfigHandler;
import me.avankziar.lly.spigot.handler.lottery.LotteryHandler;
import me.avankziar.lly.spigot.listener.JoinLeaveListener;
import me.avankziar.lly.spigot.metric.Metrics;

public class LLY extends JavaPlugin
{
	public static Logger log;
	private static LLY plugin;
	public static String pluginname = "LuckyLottery";
	private YamlHandler yamlHandler;
	private YamlManager yamlManager;
	private MysqlSetup mysqlSetup;
	private MysqlHandler mysqlHandler;
	private Utility utility;
	private BackgroundTask backgroundTask;
	
	private Administration administrationConsumer;
	private ValueEntry valueEntryConsumer;
	private Modifier modifierConsumer;
	private MessageToVelocity mtvConsumer;
	private ProxyOnlinePlayers proxyOnlinePlayersConsumer;
	private Economy ecoConsumer;
	private CommandToVelocity commandToVelocityConsumer;
	private Parcel parcelConsumer;
	
	private net.milkbowl.vault.economy.Economy vEco;
	
	public void onEnable()
	{
		plugin = this;
		log = getLogger();
		
		//https://patorjk.com/software/taag/#p=display&f=ANSI%20Shadow&t=LLY
		log.info(" ██╗     ██╗  ██╗   ██╗ | API-Version: "+plugin.getDescription().getAPIVersion());
		log.info(" ██║     ██║  ╚██╗ ██╔╝ | Author: "+plugin.getDescription().getAuthors().toString());
		log.info(" ██║     ██║   ╚████╔╝  | Plugin Website: "+plugin.getDescription().getWebsite());
		log.info(" ██║     ██║    ╚██╔╝   | Depend Plugins: "+plugin.getDescription().getDepend().toString());
		log.info(" ███████╗███████╗██║    | SoftDepend Plugins: "+plugin.getDescription().getSoftDepend().toString());
		log.info(" ╚══════╝╚══════╝╚═╝    | LoadBefore: "+plugin.getDescription().getLoadBefore().toString());
		
		setupIFHAdministration();
		
		yamlHandler = 
		new YamlHandler(
				YamlManager.Type.SPIGOT,
				pluginname,
				log,
				plugin.getDataFolder().toPath(),
        		(plugin.getAdministration() == null ?
        				null :
        					plugin.getAdministration().getLanguage()));
        setYamlManager(yamlHandler.getYamlManager());
		
		String path = plugin.getYamlHandler().getConfig().getString("IFHAdministrationPath");
		boolean adm = plugin.getAdministration() != null 
				&& plugin.getYamlHandler().getConfig().getBoolean("useIFHAdministration")
				&& plugin.getAdministration().isMysqlPathActive(path);
		if(adm || yamlHandler.getConfig().getBoolean("Mysql.Status", false) == true)
		{
			mysqlSetup = new MysqlSetup(plugin, adm, path);
			mysqlHandler = new MysqlHandler(plugin);
		} else
		{
			log.severe("MySQL is not set in the Plugin " + pluginname + "!");
			Bukkit.getPluginManager().getPlugin(pluginname).getPluginLoader().disablePlugin(this);
			return;
		}
		
		BaseConstructor.init(yamlHandler);
		LotteryHandler.initalized();
		utility = new Utility(mysqlHandler);
		backgroundTask = new BackgroundTask(this);
		
		setupBypassPerm();
		setupCommandTree();
		setupListeners();
		setupIFHConsumer();
		setupBstats();
		
		LotteryHandler.initalizedDraws();
	}
	
	public void onDisable()
	{
		Bukkit.getScheduler().cancelTasks(this);
		HandlerList.unregisterAll(this);
		yamlHandler = null;
		yamlManager = null;
		mysqlSetup = null;
		mysqlHandler = null;
		if(getServer().getPluginManager().isPluginEnabled("InterfaceHub")) 
	    {
	    	getServer().getServicesManager().unregisterAll(plugin);
	    }
		log.info(pluginname + " is disabled!");
		log = null;
	}

	public static LLY getPlugin()
	{
		return plugin;
	}
	
	public static void shutdown()
	{
		LLY.getPlugin().onDisable();
	}
	
	public YamlHandler getYamlHandler() 
	{
		return yamlHandler;
	}
	
	public YamlManager getYamlManager()
	{
		return yamlManager;
	}

	public void setYamlManager(YamlManager yamlManager)
	{
		this.yamlManager = yamlManager;
	}
	
	public MysqlSetup getMysqlSetup() 
	{
		return mysqlSetup;
	}
	
	public MysqlHandler getMysqlHandler()
	{
		return mysqlHandler;
	}
	
	public Utility getUtility()
	{
		return utility;
	}
	
	public BackgroundTask getBackgroundTask()
	{
		return backgroundTask;
	}
	
	public String getServername()
	{
		return getPlugin().getAdministration() != null ? getPlugin().getAdministration().getSpigotServerName() 
				: getPlugin().getYamlHandler().getConfig().getString("ServerName");
	}
	
	private void setupCommandTree()
	{		
		LinkedHashMap<Integer, ArrayList<String>> classicLottoI = new LinkedHashMap<>();
		ArrayList<String> list = new ArrayList<>();
		for(ClassicLotto cl : LotteryHandler.getClassicLotto())
		{
			list.add(cl.getLotteryName());
		}
		classicLottoI.put(1, list);
		
		LinkedHashMap<Integer, ArrayList<String>> lottoSuperI = new LinkedHashMap<>();
		list = new ArrayList<>();
		for(LottoSuper ls : LotteryHandler.getLottoSuper())
		{
			list.add(ls.getLotteryName());
		}
		lottoSuperI.put(1, list);
		
		LinkedHashMap<Integer, ArrayList<String>> scratchCardI = new LinkedHashMap<>();
		list = new ArrayList<>();
		for(ScratchCard ls : LotteryHandler.getScratchCard())
		{
			list.add(ls.getLotteryName());
		}
		scratchCardI.put(1, list);
		
		TabCompletion tab = new TabCompletion();
		
		CommandConstructor lly = new CommandConstructor(CommandSuggest.Type.LLY, "lly", false, false);
		registerCommand(lly, new LuckyLotteryCommandExecutor(plugin, lly), tab);
		
		if(LotteryHandler.getClassicLotto().size() > 0)
		{
			String path = "classiclotto";
			ArgumentConstructor addpot = new ArgumentConstructor(CommandSuggest.Type.CLASSICLOTTO_ADDPOT, path+"_addpot",
					0, 2, 3, false, false, classicLottoI);
			ArgumentConstructor setpot = new ArgumentConstructor(CommandSuggest.Type.CLASSICLOTTO_SETPOT, path+"_setpot",
					0, 2, 3, false, false, classicLottoI);
			ArgumentConstructor drawnow = new ArgumentConstructor(CommandSuggest.Type.CLASSICLOTTO_DRAWNOW, path+"_drawnow",
					0, 1, 999, true, false, classicLottoI);
			ArgumentConstructor play = new ArgumentConstructor(CommandSuggest.Type.CLASSICLOTTO_PLAY, path+"_play",
					0, 1, 2, false, false, classicLottoI);
			ArgumentConstructor giveticket = new ArgumentConstructor(CommandSuggest.Type.CLASSICLOTTO_GIVETICKET, path+"_giveticket",
					0, 1, 2, false, false, classicLottoI);
			ArgumentConstructor open = new ArgumentConstructor(CommandSuggest.Type.CLASSICLOTTO_OPEN, path+"_open",
					0, 1, 1, true, false, classicLottoI);
			ArgumentConstructor ticketlist = new ArgumentConstructor(CommandSuggest.Type.CLASSICLOTTO_TICKETLIST, path+"_ticketlist",
					0, 1, 2, false, false, classicLottoI);
			ArgumentConstructor repeat = new ArgumentConstructor(CommandSuggest.Type.CLASSICLOTTO_REPEAT, path+"_repeat",
					0, 2, 2, false, false, classicLottoI);
			ArgumentConstructor nextdraws = new ArgumentConstructor(CommandSuggest.Type.CLASSICLOTTO_NEXTDRAWS, path+"_nextdraws",
					0, 1, 1, false, false, classicLottoI);
			CommandConstructor cl = new CommandConstructor(CommandSuggest.Type.CLASSICLOTTO, path, false, false,
					drawnow, play, addpot, setpot, giveticket, open, ticketlist, repeat, nextdraws);
			registerCommand(cl, new ClassicLottoCommandExecutor(plugin, cl), tab);
			new me.avankziar.lly.spigot.cmd.classiclotto.ARG_AddPot(addpot);
			new me.avankziar.lly.spigot.cmd.classiclotto.ARG_SetPot(setpot);
			new me.avankziar.lly.spigot.cmd.classiclotto.ARG_DrawNow(drawnow);
			new me.avankziar.lly.spigot.cmd.classiclotto.ARG_GiveTicket(giveticket);
			new me.avankziar.lly.spigot.cmd.classiclotto.ARG_NextDraws(nextdraws);
			new me.avankziar.lly.spigot.cmd.classiclotto.ARG_Open(open);
			new me.avankziar.lly.spigot.cmd.classiclotto.ARG_Play(play);
			new me.avankziar.lly.spigot.cmd.classiclotto.ARG_Ticketlist(ticketlist);
			new me.avankziar.lly.spigot.cmd.classiclotto.ARG_Repeat(repeat);	
		}
		
		if(LotteryHandler.getLottoSuper().size() > 0)
		{
			String path = "lottosuper";
			ArgumentConstructor addpot = new ArgumentConstructor(CommandSuggest.Type.LOTTOSUPER_ADDPOT, path+"_addpot",
					0, 2, 3, false, false, lottoSuperI);
			ArgumentConstructor setpot = new ArgumentConstructor(CommandSuggest.Type.LOTTOSUPER_SETPOT, path+"_setpot",
					0, 2, 3, false, false, lottoSuperI);
			ArgumentConstructor drawnow = new ArgumentConstructor(CommandSuggest.Type.LOTTOSUPER_DRAWNOW, path+"_drawnow",
					0, 1, 999, true, false, lottoSuperI);
			ArgumentConstructor giveticket = new ArgumentConstructor(CommandSuggest.Type.LOTTOSUPER_GIVETICKET, path+"_giveticket",
					0, 1, 2, false, false, lottoSuperI);
			ArgumentConstructor nextdraws = new ArgumentConstructor(CommandSuggest.Type.LOTTOSUPER_NEXTDRAWS, path+"_nextdraws",
					0, 1, 1, false, false, lottoSuperI);
			ArgumentConstructor open = new ArgumentConstructor(CommandSuggest.Type.LOTTOSUPER_OPEN, path+"_open",
					0, 1, 1, true, false, lottoSuperI);
			ArgumentConstructor play = new ArgumentConstructor(CommandSuggest.Type.LOTTOSUPER_PLAY, path+"_play",
					0, 1, 2, false, false, lottoSuperI);
			ArgumentConstructor repeat = new ArgumentConstructor(CommandSuggest.Type.LOTTOSUPER_REPEAT, path+"_repeat",
					0, 2, 2, false, false, lottoSuperI);
			ArgumentConstructor ticketlist = new ArgumentConstructor(CommandSuggest.Type.LOTTOSUPER_TICKETLIST, path+"_ticketlist",
					0, 1, 2, false, false, lottoSuperI);
			CommandConstructor ls = new CommandConstructor(CommandSuggest.Type.LOTTOSUPER, path, false, false,
					addpot, setpot, drawnow, giveticket, nextdraws, open, play, ticketlist, repeat);
			registerCommand(ls, new LottoSuperCommandExecutor(plugin, ls), tab);
			new me.avankziar.lly.spigot.cmd.lottosuper.ARG_AddPot(addpot);
			new me.avankziar.lly.spigot.cmd.lottosuper.ARG_SetPot(setpot);
			new me.avankziar.lly.spigot.cmd.lottosuper.ARG_DrawNow(drawnow);
			new me.avankziar.lly.spigot.cmd.lottosuper.ARG_GiveTicket(giveticket);
			new me.avankziar.lly.spigot.cmd.lottosuper.ARG_NextDraws(nextdraws);
			new me.avankziar.lly.spigot.cmd.lottosuper.ARG_Open(open);
			new me.avankziar.lly.spigot.cmd.lottosuper.ARG_Play(play);
			new me.avankziar.lly.spigot.cmd.lottosuper.ARG_Repeat(repeat);
			new me.avankziar.lly.spigot.cmd.lottosuper.ARG_Ticketlist(ticketlist);
		}
		
		if(LotteryHandler.getScratchCard().size() > 0)
		{
			String path = "scratchcard";
			ArgumentConstructor giveticket = new ArgumentConstructor(CommandSuggest.Type.SCRATCHCARD_GIVETICKET, path+"_giveticket",
					0, 1, 2, false, false, scratchCardI);
			ArgumentConstructor play = new ArgumentConstructor(CommandSuggest.Type.SCRATCHCARD_PLAY, path+"_play",
					0, 1, 2, false, false, scratchCardI);
			CommandConstructor sc = new CommandConstructor(CommandSuggest.Type.SCRATCHCARD, path, false, false,
					giveticket, play);
			registerCommand(sc, new LottoSuperCommandExecutor(plugin, sc), tab);
			new me.avankziar.lly.spigot.cmd.scratchcard.ARG_GiveTicket(giveticket);
			new me.avankziar.lly.spigot.cmd.scratchcard.ARG_Play(play);
		}
		
		//ArgumentConstructor add = new ArgumentConstructor(CommandSuggest.Type.FRIEND_ADD, "friend_add", 0, 1, 1, false, playerMapI);
		//CommandConstructor friend = new CommandConstructor(CommandSuggest.Type.FRIEND, "friend", false, add, remove);
		//registerCommand(friend, new FriendCommandExecutor(plugin, friend), tab);
		//new ARGAdd(plugin, add);
	}
	
	public void setupBypassPerm()
	{
		String path = "Count.";
		for(Bypass.Counter bypass : new ArrayList<Bypass.Counter>(EnumSet.allOf(Bypass.Counter.class)))
		{
			if(!bypass.forPermission())
			{
				continue;
			}
			Bypass.set(bypass, yamlHandler.getCommands().getString(path+bypass.toString()));
		}
		path = "Bypass.";
		for(Bypass.Permission bypass : new ArrayList<Bypass.Permission>(EnumSet.allOf(Bypass.Permission.class)))
		{
			Bypass.set(bypass, yamlHandler.getCommands().getString(path+bypass.toString()));
		}
	}
	
	public ArrayList<BaseConstructor> getHelpList()
	{
		return BaseConstructor.getHelpList();
	}
	
	public ArrayList<CommandConstructor> getCommandTree()
	{
		return BaseConstructor.getCommandTree();
	}
	
	public void registerCommand(CommandConstructor cc, CommandExecutor ce, TabCompletion tab)
	{
		registerCommand(cc.getPath(), cc.getName());
		getCommand(cc.getName()).setExecutor(ce);
		getCommand(cc.getName()).setTabCompleter(tab);
	}
	
	public void registerCommand(String... aliases) 
	{
		PluginCommand command = getCommand(aliases[0], plugin);
	 
		command.setAliases(Arrays.asList(aliases));
		getCommandMap().register(plugin.getDescription().getName(), command);
	}
	 
	private static PluginCommand getCommand(String name, LLY plugin) 
	{
		PluginCommand command = null;
		try 
		{
			Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			c.setAccessible(true);
	 
			command = c.newInstance(name, plugin);
		} catch (SecurityException e) 
		{
			e.printStackTrace();
		} catch (IllegalArgumentException e) 
		{
			e.printStackTrace();
		} catch (IllegalAccessException e) 
		{
			e.printStackTrace();
		} catch (InstantiationException e) 
		{
			e.printStackTrace();
		} catch (InvocationTargetException e) 
		{
			e.printStackTrace();
		} catch (NoSuchMethodException e) 
		{
			e.printStackTrace();
		}
	 
		return command;
	}
	 
	private static CommandMap getCommandMap() 
	{
		CommandMap commandMap = null;
	 
		try {
			if (Bukkit.getPluginManager() instanceof SimplePluginManager) 
			{
				Field f = SimplePluginManager.class.getDeclaredField("commandMap");
				f.setAccessible(true);
	 
				commandMap = (CommandMap) f.get(Bukkit.getPluginManager());
			}
		} catch (NoSuchFieldException e) 
		{
			e.printStackTrace();
		} catch (SecurityException e) 
		{
			e.printStackTrace();
		} catch (IllegalArgumentException e) 
		{
			e.printStackTrace();
		} catch (IllegalAccessException e) 
		{
			e.printStackTrace();
		}
	 
		return commandMap;
	}
	
	public LinkedHashMap<String, ArgumentModule> getArgumentMap()
	{
		return BaseConstructor.getArgumentMapSpigot();
	}
	
	public void setupListeners()
	{
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new JoinLeaveListener(), plugin);
	}
	
	public boolean reload() throws IOException
	{
		if(!yamlHandler.loadYamlHandler(YamlManager.Type.SPIGOT))
		{
			return false;
		}
		if(yamlHandler.getConfig().getBoolean("Mysql.Status", false))
		{
			if(!mysqlSetup.loadMysqlSetup(ServerType.SPIGOT))
			{
				return false;
			}
		} else
		{
			return false;
		}
		return true;
	}
	
	public boolean existHook(String externPluginName)
	{
		if(plugin.getServer().getPluginManager().getPlugin(externPluginName) == null)
		{
			return false;
		}
		log.info(pluginname+" hook with "+externPluginName);
		return true;
	}
	
	private void setupIFHAdministration()
	{ 
		if(!plugin.getServer().getPluginManager().isPluginEnabled("InterfaceHub")) 
	    {
	    	return;
	    }
		RegisteredServiceProvider<me.avankziar.ifh.spigot.administration.Administration> rsp = 
                getServer().getServicesManager().getRegistration(Administration.class);
		if (rsp == null) 
		{
		   return;
		}
		administrationConsumer = rsp.getProvider();
		log.info(pluginname + " detected InterfaceHub >>> Administration.class is consumed!");
	}
	
	public Administration getAdministration()
	{
		return administrationConsumer;
	}
	
	public void setupIFHConsumer()
	{
		setupIFHValueEntry();
		setupIFHModifier();
		setupIFHMessageToVelocity();
		setupIFHBungeeOnlinePlayers();
		setupIFHEconomy();
		setupIFHCommandToVelocity();
		setupIFHParcel();
	}
	
	public void setupIFHValueEntry()
	{
		if(!new ConfigHandler().isMechanicValueEntryEnabled())
		{
			return;
		}
		if(!plugin.getServer().getPluginManager().isPluginEnabled("InterfaceHub")) 
	    {
	    	return;
	    }
        new BukkitRunnable()
        {
        	int i = 0;
			@Override
			public void run()
			{
				try
				{
					if(i == 20)
				    {
						cancel();
				    	return;
				    }
					RegisteredServiceProvider<me.avankziar.ifh.general.valueentry.ValueEntry> rsp = 
                            getServer().getServicesManager().getRegistration(
                           		 me.avankziar.ifh.general.valueentry.ValueEntry.class);
				    if(rsp == null) 
				    {
				    	i++;
				        return;
				    }
				    valueEntryConsumer = rsp.getProvider();
				    log.info(pluginname + " detected InterfaceHub >>> ValueEntry.class is consumed!");
				    cancel();
				} catch(NoClassDefFoundError e)
				{
					cancel();
				}
				if(getValueEntry() != null)
				{
					//Command Bonus/Malus init
					for(BaseConstructor bc : getHelpList())
					{
						if(!bc.isPutUpCmdPermToValueEntrySystem())
						{
							continue;
						}
						if(getValueEntry().isRegistered(bc.getValueEntryPath(pluginname)))
						{
							continue;
						}
						String[] ex = {plugin.getYamlHandler().getCommands().getString(bc.getPath()+".Explanation")};
						getValueEntry().register(
								bc.getValueEntryPath(pluginname),
								plugin.getYamlHandler().getCommands().getString(bc.getPath()+".Displayname", "Command "+bc.getName()),
								ex);
					}
					//Bypass Perm Bonus/Malus init
					List<Bypass.Permission> list = new ArrayList<Bypass.Permission>(EnumSet.allOf(Bypass.Permission.class));
					for(Bypass.Permission ept : list)
					{
						if(getValueEntry().isRegistered(ept.getValueLable()))
						{
							continue;
						}
						List<String> lar = plugin.getYamlHandler().getMVELang().getStringList(ept.toString()+".Explanation");
						getValueEntry().register(
								ept.getValueLable(),
								plugin.getYamlHandler().getMVELang().getString(ept.toString()+".Displayname", ept.toString()),
								lar.toArray(new String[lar.size()]));
					}
				}
			}
        }.runTaskTimer(plugin, 0L, 20*2);
	}
	
	public ValueEntry getValueEntry()
	{
		return valueEntryConsumer;
	}
	
	private void setupIFHModifier() 
	{
		if(!new ConfigHandler().isMechanicModifierEnabled())
		{
			return;
		}
        if(Bukkit.getPluginManager().getPlugin("InterfaceHub") == null) 
        {
            return;
        }
        new BukkitRunnable()
        {
        	int i = 0;
			@Override
			public void run()
			{
				try
				{
					if(i == 20)
				    {
						cancel();
						return;
				    }
				    RegisteredServiceProvider<me.avankziar.ifh.general.modifier.Modifier> rsp = 
                            getServer().getServicesManager().getRegistration(
                           		 me.avankziar.ifh.general.modifier.Modifier.class);
				    if(rsp == null) 
				    {
				    	//Check up to 20 seconds after the start, to connect with the provider
				    	i++;
				        return;
				    }
				    modifierConsumer = rsp.getProvider();
				    log.info(pluginname + " detected InterfaceHub >>> Modifier.class is consumed!");
				    cancel();
				} catch(NoClassDefFoundError e)
				{
					cancel();
				}
				if(getModifier() != null)
				{
					//Bypass CountPerm init
					List<Bypass.Counter> list = new ArrayList<Bypass.Counter>(EnumSet.allOf(Bypass.Counter.class));
					for(Bypass.Counter ept : list)
					{
						if(getModifier().isRegistered(ept.getModification()))
						{
							continue;
						}
						ModificationType bmt = null;
						switch(ept)
						{
						case BASE:
							bmt = ModificationType.UP;
							break;
						}
						List<String> lar = plugin.getYamlHandler().getMVELang().getStringList(ept.toString()+".Explanation");
						getModifier().register(
								ept.getModification(),
								plugin.getYamlHandler().getMVELang().getString(ept.toString()+".Displayname", ept.toString()),
								bmt,
								lar.toArray(new String[lar.size()]));
					}
				}
			}
        }.runTaskTimer(plugin, 20L, 20*2);
	}
	
	public Modifier getModifier()
	{
		return modifierConsumer;
	}
	
	private void setupIFHMessageToVelocity() 
	{
        if(Bukkit.getPluginManager().getPlugin("InterfaceHub") == null) 
        {
            return;
        }
        new BukkitRunnable()
        {
        	int i = 0;
			@Override
			public void run()
			{
				try
				{
					if(i == 20)
				    {
						cancel();
						return;
				    }
				    RegisteredServiceProvider<me.avankziar.ifh.spigot.tovelocity.chatlike.MessageToVelocity> rsp = 
		                             getServer().getServicesManager().getRegistration(
		                            		 me.avankziar.ifh.spigot.tovelocity.chatlike.MessageToVelocity.class);
				    if(rsp == null) 
				    {
				    	i++;
				        return;
				    }
				    mtvConsumer = rsp.getProvider();
				    log.info(pluginname + " detected InterfaceHub >>> MessageToVelocity.class is consumed!");
				    cancel();
				} catch(NoClassDefFoundError e)
				{
					cancel();
				}			    
			}
        }.runTaskTimer(plugin, 20L, 20*2);
	}
	
	public MessageToVelocity getMtV()
	{
		return mtvConsumer;
	}
	
	private void setupIFHBungeeOnlinePlayers() 
	{
		if(!plugin.getServer().getPluginManager().isPluginEnabled("InterfaceHub")) 
	    {
	    	return;
	    }
        new BukkitRunnable()
        {
        	int i = 0;
			@Override
			public void run()
			{
				try
				{
					if(i == 20)
				    {
						cancel();
				    	return;
				    }
				    RegisteredServiceProvider<me.avankziar.ifh.spigot.interfaces.ProxyOnlinePlayers> rsp = 
		                             getServer().getServicesManager().getRegistration(
		                            		 me.avankziar.ifh.spigot.interfaces.ProxyOnlinePlayers.class);
				    if(rsp == null) 
				    {
				    	i++;
				        return;
				    }
				    proxyOnlinePlayersConsumer = rsp.getProvider();
				    log.info(pluginname + " detected InterfaceHub >>> BungeeOnlinePlayers.class is consumed!");
				    cancel();
				} catch(NoClassDefFoundError e)
				{
					cancel();
				}			    
			}
        }.runTaskTimer(plugin, 0L, 20*2);
	}
	
	public ProxyOnlinePlayers getProxyOnlinePlayers()
	{
		return proxyOnlinePlayersConsumer;
	}
	
	private void setupIFHEconomy()
    {
		if(!plugin.getServer().getPluginManager().isPluginEnabled("InterfaceHub")
				&& !plugin.getServer().getPluginManager().isPluginEnabled("Vault")) 
	    {
			log.severe("Plugin InterfaceHub or Vault are missing!");
			log.severe("Disable "+pluginname+"!");
			Bukkit.getPluginManager().getPlugin(pluginname).getPluginLoader().disablePlugin(plugin);
	    	return;
	    }
		if(plugin.getServer().getPluginManager().isPluginEnabled("InterfaceHub"))
		{
			RegisteredServiceProvider<me.avankziar.ifh.spigot.economy.Economy> rsp = 
	                getServer().getServicesManager().getRegistration(Economy.class);
			if (rsp == null) 
			{
				RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp2 = getServer()
		        		.getServicesManager()
		        		.getRegistration(net.milkbowl.vault.economy.Economy.class);
		        if (rsp2 == null) 
		        {
		        	log.severe("A economy plugin which supported InterfaceHub or Vault is missing!");
					log.severe("Disable "+pluginname+"!");
					Bukkit.getPluginManager().getPlugin(pluginname).getPluginLoader().disablePlugin(plugin);
		            return;
		        }
		        vEco = rsp2.getProvider();
		        log.info(pluginname + " detected Vault >>> Economy.class is consumed!");
				return;
			}
			ecoConsumer = rsp.getProvider();
			log.info(pluginname + " detected InterfaceHub >>> Economy.class is consumed!");
		} else
		{
			RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp = getServer()
	        		.getServicesManager()
	        		.getRegistration(net.milkbowl.vault.economy.Economy.class);
	        if (rsp == null) 
	        {
	        	log.severe("A economy plugin which supported Vault is missing!");
				log.severe("Disable "+pluginname+"!");
				Bukkit.getPluginManager().getPlugin(pluginname).getPluginLoader().disablePlugin(plugin);
	            return;
	        }
	        vEco = rsp.getProvider();
	        log.info(pluginname + " detected Vault >>> Economy.class is consumed!");
		}
        return;
    }
	
	public Economy getIFHEco()
	{
		return this.ecoConsumer;
	}
	
	public net.milkbowl.vault.economy.Economy getVaultEco()
	{
		return this.vEco;
	}
	
	private void setupIFHCommandToVelocity() 
	{
        if(Bukkit.getPluginManager().getPlugin("InterfaceHub") == null) 
        {
            return;
        }
        new BukkitRunnable()
        {
        	int i = 0;
			@Override
			public void run()
			{
				try
				{
					if(i == 20)
				    {
						cancel();
						return;
				    }
				    RegisteredServiceProvider<me.avankziar.ifh.spigot.tovelocity.commands.CommandToVelocity> rsp = 
		                             getServer().getServicesManager().getRegistration(
		                            		 me.avankziar.ifh.spigot.tovelocity.commands.CommandToVelocity.class);
				    if(rsp == null) 
				    {
				    	i++;
				        return;
				    }
				    commandToVelocityConsumer = rsp.getProvider();
				    log.info(pluginname + " detected InterfaceHub >>> CommandToVelocity.class is consumed!");
				    cancel();
				} catch(NoClassDefFoundError e)
				{
					cancel();
				}			    
			}
        }.runTaskTimer(plugin, 20L, 20*2);
	}
	
	public CommandToVelocity getCtV()
	{
		return commandToVelocityConsumer;
	}
	
	private void setupIFHParcel() 
	{
        if(Bukkit.getPluginManager().getPlugin("InterfaceHub") == null) 
        {
            return;
        }
        new BukkitRunnable()
        {
        	int i = 0;
			@Override
			public void run()
			{
				try
				{
					if(i == 20)
				    {
						cancel();
						return;
				    }
				    RegisteredServiceProvider<me.avankziar.ifh.spigot.sendable.Parcel> rsp = 
		                             getServer().getServicesManager().getRegistration(
		                            		 me.avankziar.ifh.spigot.sendable.Parcel.class);
				    if(rsp == null) 
				    {
				    	i++;
				        return;
				    }
				    parcelConsumer = rsp.getProvider();
				    log.info(pluginname + " detected InterfaceHub >>> Parcel.class is consumed!");
				    cancel();
				} catch(NoClassDefFoundError e)
				{
					cancel();
				}			    
			}
        }.runTaskTimer(plugin, 20L, 20*2);
	}
	
	public Parcel getParcel()
	{
		return parcelConsumer;
	}
	
	public void setupBstats()
	{
		int pluginId = 0;
        new Metrics(this, pluginId);
	}
}