package me.avankziar.lly.spigot.handler.lottery;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.scheduler.BukkitRunnable;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.avankziar.lly.general.assistance.Utility;
import me.avankziar.lly.general.cmdtree.CommandSuggest;
import me.avankziar.lly.general.objects.Advertising;
import me.avankziar.lly.general.objects.DrawTime;
import me.avankziar.lly.general.objects.KeepsakeItem;
import me.avankziar.lly.general.objects.PlayerData;
import me.avankziar.lly.general.objects.ScratchCardField;
import me.avankziar.lly.general.objects.lottery.Lottery.GameType;
import me.avankziar.lly.general.objects.lottery.ScratchCard;
import me.avankziar.lly.general.objects.lottery.ticket.ScratchCardTicket;
import me.avankziar.lly.spigot.LLY;
import me.avankziar.lly.spigot.handler.EconomyHandler;
import me.avankziar.lly.spigot.handler.MessageHandler;

public class ScratchCardHandler
{
	protected static Collection<ScratchCard> scratchcard = new HashSet<>();
	
	public static void initalizedYamls()
	{
		for(YamlDocument y : LLY.getPlugin().getYamlHandler().getScratchCard())
		{
			try
			{
				if(!y.contains("LotteryName") || !y.contains("Description")
						|| !y.contains("ScratchCardField.1.WinningAmount")
						|| !y.contains("ScratchCardField.1.Chance")
						|| !y.contains("ScratchCardField.1.Display"))
				{
					LLY.log.warning(
							y.getFile().getName()+".yaml is missing essential values! "
									+ "Please check these otherwise the lottery cannot be registered!");
					continue;
				}
				String lottoname = y.getString("LotteryName");
				String description = y.getString("Description");
				double costPerTicket = y.getDouble("CostPerTicket", 2.5);
				int amountOfField = y.getInt("AmountOfField");
				int amountOfSameFieldToWin = y.getInt("AmountOfSameFieldToWin");
				ArrayList<ScratchCardField> scratchCardField = new ArrayList<>();
				for(int i = 1; i <= 100; i++)
				{
					if(!y.contains("ScratchCardField."+i+".WinningAmount")
							|| !y.contains("ScratchCardField."+i+".Chance")
							|| !y.contains("ScratchCardField."+i+".Display"))
					{
						continue;
					}
					double winningAmount = y.getDouble("ScratchCardField."+i+".WinningAmount");
					if(winningAmount == -1.0 && amountOfField == 1)
					{
						//Joker cannot be used if amountOfField == 1
						continue;
					}
					double chance = y.getDouble("ScratchCardField."+i+".Chance");
					String display = y.getString("ScratchCardField."+i+".Display");
					KeepsakeItem ksi = null;
					if(y.contains("ScratchCardField."+i+".KeepsakeItem.Material")
							&& y.contains("ScratchCardField."+i+".KeepsakeItem.Amount")
							&& y.contains("ScratchCardField."+i+".KeepsakeItem.Displayname")
							&& y.contains("ScratchCardField."+i+".KeepsakeItem.EnchantmentGlintOverride"))
					{
						try
						{
							Material material = Material.valueOf(y.getString("ScratchCardField."+i+".KeepsakeItem.Material"));
							int amount = y.getInt("ScratchCardField."+i+".KeepsakeItem.Amount");
							amount = amount > 64 ? 64 : (amount < 1 ? 1 : amount);
							String displayname = y.getString("ScratchCardField."+i+".KeepsakeItem.Displayname");
							ArrayList<String> lore = new ArrayList<>();
							if(y.contains("ScratchCardField."+i+".Lore"))
							{
								lore = (ArrayList<String>) y.getStringList("ScratchCardField."+i+".KeepsakeItem.Lore");
							}
							String ego = y.getString("ScratchCardField."+i+".KeepsakeItem.EnchantmentGlintOverride");
							Boolean enchantmentGlintOverride = ego.equals("null") ? null : (ego.equals("true") ? true : false);
							ksi = new KeepsakeItem(material, amount, displayname, lore, enchantmentGlintOverride);
							if(y.contains("ScratchCardField."+i+".KeepsakeItem.Enchantment"))
							{
								for(String s : y.getStringList("ScratchCardField."+i+".KeepsakeItem.Enchantment"))
								{
									String[] sp = s.split(";");
									if(sp.length != 2)
									{
										LLY.log.warning(lottoname+" ScratchCardField number "+i+" is has a small issue! "+
												"KeepsakeItem Enchantment is skipt: "+s);
										continue;
									}
									ksi.addEnchantment(sp[0].toLowerCase(), Integer.valueOf(sp[1]));
								}
							}
							if(y.contains("ScratchCardField."+i+".KeepsakeItem.ItemFlag"))
							{
								for(String s : y.getStringList("ScratchCardField."+i+".KeepsakeItem.ItemFlag"))
								{
									ksi.addItemFlag(ItemFlag.valueOf(s));
								}
							}
						} catch(Exception e)
						{
							LLY.log.warning(lottoname+" ScratchCardField number "+i+" is has a small issue! "+
									"Keepsake Item was skipt");
							continue;
						}
					}
					ScratchCardField scf = new ScratchCardField(winningAmount, chance, display, ksi);
					scratchCardField.add(scf);
				}
				if(scratchCardField.size() < 3)
				{
					LLY.log.warning(lottoname+" ScratchCardField amount < 3! "+
							"ScratchCard will not be registered!");
					continue;
				}
				ArrayList<Advertising> advertising = new ArrayList<>();
				for(int i = 1; i < 100; i++)
				{
					if(!y.contains("Advertising."+i+".Message") || !y.contains("Advertising."+i+".Time"))
					{
						continue;
					}
					boolean active = y.getBoolean("Advertising."+i+".IsActive", false);
					boolean canIgnored = y.getBoolean("Advertising."+i+".CanIgnore", false);
					ArrayList<String> message = (ArrayList<String>) y.getStringList("Advertising."+i+".Message");
					ArrayList<DrawTime> time = new ArrayList<>();
					for(String s : y.getStringList("Advertising."+i+".Time"))
					{
						String[] a = s.split("-");
						if(a.length != 4)
						{
							LLY.log.warning(lottoname+" has by AdvertisingTime a Problem! "
											+ "AdvertisingTime "+s+" missed 3 of `-` Character! "
											+ "AdvertisingTime was not registered!");
							continue;
						}
						try
						{
							int weekOfMonth = Integer.valueOf(a[0]);
							weekOfMonth = weekOfMonth > 5 ? 5 
									: (weekOfMonth < 1 ? 1 : weekOfMonth);
							DayOfWeek dayOfWeek = DayOfWeek.valueOf(a[1]);
							int hour = Integer.valueOf(a[2]);
							hour = hour > 23 ? 23
									: (hour < 0 ? 0 : hour);
							int min = Integer.valueOf(a[3]);
							min = min > 59 ? 59
									: (min < 0 ? 0 : min);
							time.add(new DrawTime(weekOfMonth, dayOfWeek, hour, min));
						} catch(Exception e)
						{
							LLY.log.warning(lottoname+" has by AdvertisingTime a Problem! "
									+ "AdvertisingTime "+s+" is incorrect!");
							continue;
						}
					}
					advertising.add(new Advertising(active, canIgnored, message, time));
				}
				int fieldPerLine = y.getInt("Display.FieldPerLine");
				ArrayList<String> displayFieldUnscratched = new ArrayList<>();
				if(y.contains("Display.FieldUnscratched"))
				{
					y.getStringList("Display.FieldUnscratched").stream().forEach(x -> displayFieldUnscratched.add(x));
				}
				ArrayList<String> displayFieldScratched = new ArrayList<>();
				if(y.contains("Display.FieldScratched"))
				{
					y.getStringList("Display.FieldScratched").stream().forEach(x -> displayFieldScratched.add(x));
				}
				ScratchCard sc = new ScratchCard(lottoname, description, GameType.X_NUMBER_OF_FIELDS,
						costPerTicket, amountOfField, amountOfSameFieldToWin, scratchCardField, advertising,
						fieldPerLine, displayFieldUnscratched, displayFieldScratched);
				LLY.log.info("ScratchCard "+lottoname+" loaded!");
				scratchcard.add(sc);
				sc.getTicketMysql().setupMysql(LLY.getPlugin().getMysqlSetup(), me.avankziar.lly.general.database.ServerType.ALL);
			} catch(Exception e)
			{
				continue;
			}
		}
	}
	
	public static void initalizedDraws()
	{
		new BukkitRunnable() 
		{
			@Override
			public void run() 
			{
				asyncDraw();
			}
		}.runTaskTimer(LLY.getPlugin(), 0L, 60*20L);
	}
	
	private static void asyncDraw()
	{
		final LocalDateTime ldt = LocalDateTime.now();
		new BukkitRunnable() 
		{
			@Override
			public void run() 
			{	
				for(ScratchCard ls : LotteryHandler.getScratchCard())
				{
					for(Advertising ad : ls.getAdvertising())
					{
						if(!ad.isActive())
						{
							continue;
						}
						for(DrawTime dr : ad.getDrawTime())
						{
							if(dr.isNow(ldt))
							{
								advertise(ls, ad);
							}
						}
					}
				}
			}
		}.runTaskAsynchronously(LLY.getPlugin());
	}
	
	private static void advertise(final ScratchCard ls, Advertising ad)
	{
		String[] sa = replacer(ls, null, null, null, false,
				ad.getMessage()).toArray(new String[ad.getMessage().size()]);
		for(Player player : Bukkit.getOnlinePlayers())
		{
			PlayerData pd = LLY.getPlugin().getMysqlHandler().getData(new PlayerData(), "`player_uuid` = ?", player.getUniqueId().toString());
			if(pd == null || (ad.canIgnored() && pd.isIgnoreAdvertising()))
			{
				continue;
			}
			MessageHandler.sendMessage(player, sa);
		}
	}
	
	
	
	/**
	 * Replacer for message in the drawing of the lottery.
	 */
	public static ArrayList<String> replacer(ScratchCard sc, ScratchCardTicket lst,
			Double payout, String jackpotwinners, boolean wasJackpotBreached,
			ArrayList<String> list)
	{
		ArrayList<String> li = new ArrayList<>();
		for(String s : list)
		{
			String r = replacer(sc, lst, payout, jackpotwinners, wasJackpotBreached, s);
			li.add(r);
		}
		return li;
	}
	
	public static String replacer(ScratchCard sc, ScratchCardTicket lst,
			Double payout, String jackpotwinners, boolean wasJackpotBreached,
			String r)
	{
		String s = r;
		if(payout != null)
		{
			s = s.replace("%payout%", EconomyHandler.format(payout));
		} else
		{
			s = s.replace("%payout%", "/");
		}
		if(jackpotwinners != null)
		{
			s = s.replace("%winners%", jackpotwinners);
		} else
		{
			s = s.replace("%winners%", "/");
		}
		if(s.contains("%lottosupercmd%"))
		{
			s = s.replace("%lottosupercmd%", CommandSuggest.getCmdString(CommandSuggest.Type.LOTTOSUPER));
		}
		if(s.contains("%lottosuperbet%"))
		{
			s = s.replace("%lottosuperbet%", CommandSuggest.getCmdString(CommandSuggest.Type.LOTTOSUPER_PLAY));
		}
		if(sc != null)
		{
			s = s.replace("%lotteryname%", sc.getLotteryName())
					.replace("%description%", sc.getDescription())
					.replace("%amountofchoosennumber%", String.valueOf(sc.getAmountOfFields()))
					.replace("%additionalamountofchoosennumber%", String.valueOf(sc.getAmountOfSameFieldToWin()))
					.replace("%jackpotamount%", EconomyHandler.format(sc.getJackpot().getWinningAmount()))
					.replace("%costperticket%", EconomyHandler.format(sc.getCostPerTicket()))
					.replace("%winningchance%", sc.getWinningChance());
		} else
		{
			s = s.replace("%lotteryname%", "/")
					.replace("%description%", "/")
					.replace("%amountofchoosennumber%", "/")
					.replace("%additionalamountofchoosennumber%", "/")
					.replace("%jackpotamount%", "/")
					.replace("%costperticket%", "/")
					.replace("%winningchance%", "/");
		}
		if(lst != null)
		{
			s = s.replace("%lotteryname%", lst.getLotteryName())
					.replace("%ticketid%", String.valueOf(lst.getID()))
					.replace("%ticketplayer%", Utility.convertUUIDToName(lst.getLotteryPlayer().toString()));
		} else
		{
			s = s.replace("%lotteryname%", "/")
					.replace("%ticketid%", "/")
					.replace("%ticketplayer%", "/");
		}
		return s;
	}
}