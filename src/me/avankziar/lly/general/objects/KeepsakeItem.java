package me.avankziar.lly.general.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.avankziar.lly.general.assistance.ChatApi;
import me.avankziar.lly.general.assistance.ChatApiItem;
import me.avankziar.lly.spigot.LLY;

public class KeepsakeItem 
{
	private Material material;
	private int amount;
	private String displayname;
	private ArrayList<String> lore = new ArrayList<>();
	private LinkedHashMap<Enchantment, Integer> enchantments = new LinkedHashMap<>();
	private HashSet<ItemFlag> itemFlags = new HashSet<>();
	private Boolean enchantmentGlintOverride;
	
	public KeepsakeItem(Material material, int amount, String displayname,
			ArrayList<String> lore, Boolean enchantmentGlintOverride)
	{
		setMaterial(material);
		setAmount(amount);
		setDisplayname(displayname);
		setLore(lore);
		setEnchantmentGlintOverride(enchantmentGlintOverride);
	}
	
	public Material getMaterial() {
		return material;
	}
	
	public void setMaterial(Material material) {
		this.material = material;
	}
	
	public int getAmount() {
		return amount;
	}
	
	public void setAmount(int amount) {
		this.amount = amount;
	}
	
	public String getDisplayname() {
		return displayname;
	}
	
	public void setDisplayname(String displayname) {
		this.displayname = displayname;
	}
	
	public ArrayList<String> getLore() {
		return lore;
	}
	
	public void setLore(ArrayList<String> lore) {
		this.lore = lore;
	}
	
	public LinkedHashMap<Enchantment, Integer> getEnchantments() {
		return enchantments;
	}
	
	public void setEnchantments(LinkedHashMap<Enchantment, Integer> enchantments) {
		this.enchantments = enchantments;
	}
	
	public void addEnchantment(String enchantment, int lv)
	{
		Enchantment e = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(enchantment));
		if(e == null)
		{
			return;
		}
		enchantments.put(e, lv);
	}
	
	public HashSet<ItemFlag> getItemFlags() {
		return itemFlags;
	}
	
	public void setItemFlags(HashSet<ItemFlag> itemFlags) {
		this.itemFlags = itemFlags;
	}
	
	public void addItemFlag(ItemFlag itemflag)
	{
		if(itemFlags.contains(itemflag))
		{
			return;
		}
		itemFlags.add(itemflag);
	}
	
	public Boolean getEnchantmentGlintOverride() {
		return enchantmentGlintOverride;
	}

	public void setEnchantmentGlintOverride(Boolean enchantmentGlintOverride) {
		this.enchantmentGlintOverride = enchantmentGlintOverride;
	}

	public void sendItem(UUID uuid, String displayname, ArrayList<String> lore)
	{
		ItemStack is = new ItemStack(material, amount);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(ChatApiItem.tl(displayname));
		ArrayList<String> l = new ArrayList<>();
		lore.stream().forEach(x -> l.add(ChatApiItem.tl(x)));
		im.setLore(l);
		enchantments.keySet().forEach(x -> im.addEnchant(x, enchantments.get(x), true));
		itemFlags.stream().forEach(x -> im.addItemFlags(x));
		im.setEnchantmentGlintOverride(enchantmentGlintOverride);
		is.setItemMeta(im);
		Player player = Bukkit.getPlayer(uuid);
		if(player == null)
		{
			if(LLY.getPlugin().getParcel() == null)
			{
				return;
			}
			LLY.getPlugin().getParcel().sendParcel(uuid,
					LLY.getPlugin().getYamlHandler().getLang().getString("Parcel.Displayname"),
					LLY.getPlugin().getYamlHandler().getLang().getString("Parcel.Lore"),
					is);
			return;
		}
		HashMap<Integer, ItemStack> send = player.getInventory().addItem(is);
		if(!send.isEmpty())
		{
			player.getWorld().dropItem(player.getLocation(), is);
		}
		player.spigot().sendMessage(ChatApi.tl(
				LLY.getPlugin().getYamlHandler().getLang().getString("Parcel.Sended")));
	}
}
