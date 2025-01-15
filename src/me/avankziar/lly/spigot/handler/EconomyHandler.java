package me.avankziar.lly.spigot.handler;

import java.util.UUID;

import org.bukkit.Bukkit;

import me.avankziar.ifh.general.economy.account.AccountCategory;
import me.avankziar.ifh.general.economy.action.OrdererType;
import me.avankziar.ifh.general.economy.currency.CurrencyType;
import me.avankziar.ifh.spigot.economy.account.Account;
import me.avankziar.ifh.spigot.economy.currency.EconomyCurrency;
import me.avankziar.lly.spigot.LLY;

public class EconomyHandler 
{
	public static String format(double d)
	{
		if(LLY.getPlugin().getIFHEco() != null)
		{
			EconomyCurrency ec = LLY.getPlugin().getIFHEco().getDefaultCurrency(CurrencyType.DIGITAL);
			return LLY.getPlugin().getIFHEco().format(d, ec);
		}
		if(LLY.getPlugin().getVaultEco() != null)
		{
			return String.valueOf(d) + " " + LLY.getPlugin().getVaultEco().currencyNamePlural();
		}
		return "MISSING ECONOMY";
	}
	
	public static boolean hasBalance(UUID uuid, double d)
	{
		if(LLY.getPlugin().getIFHEco() != null)
		{
			Account ac = LLY.getPlugin().getIFHEco().getDefaultAccount(uuid, AccountCategory.MAIN,
					LLY.getPlugin().getIFHEco().getDefaultCurrency(CurrencyType.DIGITAL));
			return ac != null ? ac.getBalance() >= d : false;
		}
		if(LLY.getPlugin().getVaultEco() != null)
		{
			return LLY.getPlugin().getVaultEco().has(Bukkit.getOfflinePlayer(uuid), d);
		}
		return false;
	}
	
	public static void withdraw(UUID uuid, double d, String category, String comment)
	{
		if(LLY.getPlugin().getIFHEco() != null)
		{
			Account ac = LLY.getPlugin().getIFHEco().getDefaultAccount(uuid, AccountCategory.MAIN,
					LLY.getPlugin().getIFHEco().getDefaultCurrency(CurrencyType.DIGITAL));
			LLY.getPlugin().getIFHEco().withdraw(ac, d, OrdererType.PLAYER, uuid.toString(),
					category, comment);
		}
		if(LLY.getPlugin().getVaultEco() != null)
		{
			LLY.getPlugin().getVaultEco().withdrawPlayer(Bukkit.getPlayer(uuid), d);
		}
	}
	
	public static void deposit(UUID uuid, double d, String category, String comment)
	{
		if(LLY.getPlugin().getIFHEco() != null)
		{
			Account ac = LLY.getPlugin().getIFHEco().getDefaultAccount(uuid, AccountCategory.MAIN,
					LLY.getPlugin().getIFHEco().getDefaultCurrency(CurrencyType.DIGITAL));
			LLY.getPlugin().getIFHEco().deposit(ac, d, OrdererType.PLAYER, uuid.toString(),
					category, comment);
		}
		if(LLY.getPlugin().getVaultEco() != null)
		{
			LLY.getPlugin().getVaultEco().depositPlayer(Bukkit.getPlayer(uuid), d);
		}
	}
}