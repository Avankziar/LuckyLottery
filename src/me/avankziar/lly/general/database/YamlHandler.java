package me.avankziar.lly.general.database;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import me.avankziar.lly.general.database.Language.ISO639_2B;
import me.avankziar.lly.general.objects.lottery.Lottery.GameType;

public class YamlHandler implements YamlHandling
{	
	private String pluginname;
	private Path dataDirectory;
	private Logger logger;
	private String administrationLanguage = null;
	
	private YamlManager yamlManager;
	public YamlManager getYamlManager()
	{
		return yamlManager;
	}
	
	private GeneralSettings gsd = GeneralSettings.DEFAULT;
	private LoaderSettings lsd = LoaderSettings.builder().setAutoUpdate(true).build();
	private DumperSettings dsd = DumperSettings.DEFAULT;
	private UpdaterSettings usd = UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version"))
			.setKeepAll(true)
			.setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS).build();
	
	private String languages;
	
	private YamlDocument config;
	public YamlDocument getConfig()
	{
		return config;
	}
	
	private YamlDocument commands;
	public YamlDocument getCommands()
	{
		return commands;
	}
	
	private YamlDocument lang;
	public YamlDocument getLang()
	{
		return lang;
	}
	
	private YamlDocument mvelang;
	public YamlDocument getMVELang()
	{
		return mvelang;
	}
	
	private static ArrayList<YamlDocument> classicLotto = new ArrayList<>();
	public ArrayList<YamlDocument> getClassicLotto()
	{
		return classicLotto;
	}
	
	private static ArrayList<YamlDocument> lottoSuper = new ArrayList<>();
	public ArrayList<YamlDocument> getLottoSuper()
	{
		return lottoSuper;
	}
	
	public YamlHandler(YamlManager.Type type, String pluginname, Logger logger, Path directory, String administrationLanguage)
	{
		this.pluginname = pluginname;
		this.logger = logger;
		this.dataDirectory = directory;
		this.administrationLanguage = administrationLanguage;
		loadYamlHandler(type);
	}
	
	public boolean loadYamlHandler(YamlManager.Type type)
	{
		yamlManager = new YamlManager(type);
		if(!mkdirStaticFiles(type))
		{
			return false;
		}
		if(!mkdirDynamicFiles(type))
		{
			return false;
		}
		return true;
	}
	
	public boolean mkdirStaticFiles(YamlManager.Type type)
	{
		File directory = new File(dataDirectory.getParent().toFile(), "/"+pluginname+"/");
		if(!directory.exists())
		{
			directory.mkdirs();
		}
		String f = "config";
		try
	    {
			config = YamlDocument.create(new File(directory,"%f%.yml".replace("%f%", f)),
					getClass().getResourceAsStream("/default.yml"),gsd,lsd,dsd,usd);
			if(!setupStaticFile(f, config, yamlManager.getConfigKey()))
			{
				return false;
			}
			f = "commands";
			commands = YamlDocument.create(new File(directory,"%f%.yml".replace("%f%", f)),
					getClass().getResourceAsStream("/default.yml"),gsd,lsd,dsd,usd);
			if(!setupStaticFile(f, commands, yamlManager.getCommandsKey()))
			{
				return false;
			}
	    } catch (IOException e)
	    {
	    	logger.severe("Could not create/load config.yml file! Plugin will shut down!");
	    }
		return true;
	}
	
	private boolean mkdirDynamicFiles(YamlManager.Type type)
	{
		if(!mkdirLanguage(type))
		{
			return false;
		}
		if(!mkdirLotto())
		{
			return false;
		}
		return true;
	}
	
	private boolean mkdirLanguage(YamlManager.Type type)
	{
		String languageString = yamlManager.getLanguageType().toString().toLowerCase();
		File directory = new File(dataDirectory.getParent().toFile(), "/"+pluginname+"/Languages/");
		if(!directory.exists())
		{
			directory.mkdirs();
		}
		String f = languageString;
		try
	    {
			lang = YamlDocument.create(new File(directory,"%f%.yml".replace("%f%", f)),
					getClass().getResourceAsStream("/default.yml"),gsd,lsd,dsd,usd);
			if(!setupStaticFile(f, lang, yamlManager.getLanguageKey()))
			{
				return false;
			}
			mvelang = YamlDocument.create(new File(directory,"mve_%f%.yml".replace("%f%", f)),
					getClass().getResourceAsStream("/default.yml"),gsd,lsd,dsd,usd);
			if(!setupStaticFile(f, mvelang, yamlManager.getModifierValueEntryLanguageKey()))
			{
				return false;
			}
	    } catch (Exception e)
	    {
	    	logger.severe("Could not create/load %f%.yml file! Plugin will shut down!".replace("%f%", languageString));
	    	return false;
	    }
		return true;
	}
	
	private boolean mkdirLotto()
	{
		File lottery = new File(dataDirectory.getParent().toFile(), "/"+pluginname+"/Lottery/");
		if(!lottery.exists())
		{
			lottery.mkdirs();
		}
		if(yamlManager.getLottery().containsKey(GameType.X_FROM_Y))
		{
			File classicLottoFolder = new File(lottery, "/ClassicLotto/");
			if(!classicLottoFolder.exists())
			{
				classicLottoFolder.mkdirs();
				for(Entry<String, LinkedHashMap<String, Language>> entry : yamlManager.getLottery()
																			.get(GameType.X_FROM_Y).entrySet())
				{
					String cl = entry.getKey();
					LinkedHashMap<String, Language> map = entry.getValue();
					try
				    {
						YamlDocument y = YamlDocument.create(new File(classicLottoFolder,"%f%.yml".replace("%f%", cl)),
								getClass().getResourceAsStream("/default.yml"),gsd,lsd,dsd,usd);
						if(!setupStaticFile(cl, y, map))
						{
							return false;
						}
						classicLotto.add(y);
				    } catch (IOException e)
				    {
				    	logger.severe("Could not create/load config.yml file! Plugin will shut down!");
				    }
				}
			} else
			{
				loadFiles(classicLottoFolder, classicLotto);
			}
		}
		if(yamlManager.getLottery().containsKey(GameType.X_FROM_Y_AND_Z_FROM_U))
		{
			File lottoSuperFolder = new File(lottery, "/LottoSuper/");
			if(!lottoSuperFolder.exists())
			{
				lottoSuperFolder.mkdirs();
				for(Entry<String, LinkedHashMap<String, Language>> entry : yamlManager.getLottery()
																			.get(GameType.X_FROM_Y_AND_Z_FROM_U).entrySet())
				{
					String cl = entry.getKey();
					LinkedHashMap<String, Language> map = entry.getValue();
					try
				    {
						YamlDocument y = YamlDocument.create(new File(lottoSuperFolder,"%f%.yml".replace("%f%", cl)),
								getClass().getResourceAsStream("/default.yml"),gsd,lsd,dsd,usd);
						if(!setupStaticFile(cl, y, map))
						{
							return false;
						}
						lottoSuper.add(y);
				    } catch (IOException e)
				    {
				    	logger.severe("Could not create/load config.yml file! Plugin will shut down!");
				    }
				}
			} else
			{
				loadFiles(lottoSuperFolder, lottoSuper);
			}
		}		
		return true;
	}
	
	private void loadFiles(File folder, ArrayList<YamlDocument> ylA)
	{
		for(File f : folder.listFiles())
		{
			if(f.isDirectory())
			{
				continue;
			}
			try
			{
				YamlDocument y = YamlDocument
						.create(f, getClass().getResourceAsStream("/default.yml"),gsd,lsd,dsd,usd);
				ylA.add(y);
			} catch(Exception e)
			{
				continue;
			}
		}
	}
	
	private boolean setupStaticFile(String f, YamlDocument yd, LinkedHashMap<String, Language> map) throws IOException
	{
		yd.update();
		if(f.equals("config") && config != null)
		{
			//If Config already exists
			languages = administrationLanguage == null 
					? config.getString("Language", "ENG").toUpperCase() 
					: administrationLanguage;
			setLanguage();
		}
		for(String key : map.keySet())
		{
			Language languageObject = map.get(key);
			if(languageObject.languageValues.containsKey(yamlManager.getLanguageType()) == true)
			{
				yamlManager.setFileInput(yd, map, key, yamlManager.getLanguageType());
			} else if(languageObject.languageValues.containsKey(yamlManager.getDefaultLanguageType()) == true)
			{
				yamlManager.setFileInput(yd, map, key, yamlManager.getDefaultLanguageType());
			}
		}
		yd.save();
		if(f.equals("config") && config != null)
    	{
			//if Config was created the first time
			languages = administrationLanguage == null 
					? config.getString("Language", "ENG").toUpperCase() 
					: administrationLanguage;
			setLanguage();
    	}
		return true;
	}
	
	private void setLanguage()
	{
		List<Language.ISO639_2B> types = new ArrayList<Language.ISO639_2B>(EnumSet.allOf(Language.ISO639_2B.class));
		ISO639_2B languageType = ISO639_2B.ENG;
		for(ISO639_2B type : types)
		{
			if(type.toString().equals(languages))
			{
				languageType = type;
				break;
			}
		}
		yamlManager.setLanguageType(languageType);
	}
	
	@Override
	public String getCommandString(String s)
	{
		return getCommands().getString(s);
	}
	
	@Override
	public String getCommandString(String s, String defaults)
	{
		String r = getCommandString(s);
		return r != null ? r : defaults;
	}
}