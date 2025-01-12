package me.avankziar.lly.general.assistance;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.ChatColor;

@SuppressWarnings("deprecation")
public class ChatApiItem
{	
	public static String tl(String s)
	{
		return parseHex(ChatColor.translateAlternateColorCodes('&', getKyoriAdventureConvertBukkitColor(s)));
	}
	
	private static String getKyoriAdventureConvertBukkitColor(String s)
	{
		String r = "";
		r = s.replace("<black>", "&0")
				.replace("<black>", "")
				.replace("<dark_blue>", "&1")
				.replace("</dark_blue>", "")
				.replace("<dark_green>", "&2")
				.replace("</dark_green>", "")
				.replace("<dark_aqua>", "&3")
				.replace("</dark_aqua>", "")
				.replace("<dark_red>", "&4")
				.replace("</dark_red>", "")
				.replace("<dark_purple>", "&5")
				.replace("</dark_purple>", "")
				.replace("<gold>", "&6")
				.replace("</gold>", "")
				.replace("<gray>", "&7")
				.replace("</gray>", "")
				.replace("<dark_gray>", "&8")
				.replace("</dark_gray>", "")
				.replace("<blue>", "&9")
				.replace("</blue>", "")
				.replace("<green>", "&a")
				.replace("</green>", "")
				.replace("<aqua>", "&b")
				.replace("</aqua>", "")
				.replace("<red>", "&c")
				.replace("</red>", "")
				.replace("<light_purple>", "&d")
				.replace("</light_purple>", "")
				.replace("<yellow>", "&e")
				.replace("</yellow>", "")
				.replace("<white>", "&f")
				.replace("</white>", "")
				.replace("<obf>", "&k")
				.replace("</obf>", "")
				.replace("<b>", "&l")
				.replace("</b>", "")
				.replace("<st>", "&m")
				.replace("</st>", "")
				.replace("<u>", "&n")
				.replace("</u>", "")
				.replace("<i>", "&o")
				.replace("</i>", "")
				.replace("<reset>", "&r")
				.replace("</reset>", "")
				//.replace("", "")
				//.replace("", "")
				;
		return r;
	}
	
	private static String parseHex(String text) 
	{
		supportsHex = checkHexSupport();
		return parseHexText(text, findHexIndexes(text));
	}
	
	private static List<Integer> findHexIndexes(String text) 
	{
        int index;
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        int i = 0;
        while ((index = text.indexOf("&#", i)) != -1) {
            indexes.add(index);
            ++i;
        }
        return indexes;
    }

    private static String parseHexText(String text, List<Integer> indexes) 
    {
        //int HEX_LENGTH = 7;
        StringBuilder newText = new StringBuilder();
        StringBuilder currentHex = new StringBuilder();
        boolean isInHex = false;
        for (int i = 0; i < text.length(); ++i) 
        {
            if (indexes.contains(i)) 
            {
                isInHex = true;
                continue;
            }
            if (isInHex) {
                currentHex.append(text.charAt(i));
                if (currentHex.length() != 7) continue;
                isInHex = false;
                newText.append(nearestColor(currentHex.toString()));
                currentHex.setLength(0);
                continue;
            }
            newText.append(text.charAt(i));
        }
        return newText.toString();
    }

    private static String nearestColor(String hex)
	{
		if (supportsHex())
		{
			if (hex.matches("[#][0-9a-fA-F]+"))
				return ChatColor.of(hex).toString();
			String newhex = "#";
			for (int j = 1; j < hex.length(); j++)
			{
				String s = String.valueOf(hex.charAt(j));
				if (!s.matches("[0-9a-fA-F]"))
					s = "0";
				newhex = String.valueOf(newhex) + s;
			}
			return ChatColor.of(newhex).toString();
		}
		Color awtColor = Color.decode(hex);
		ChatColor nearestColor = ChatColor.WHITE;
		double shorterDistance = -1.0D;
		byte b;
		int i;
		ChatColor[] arrayOfChatColor;
		for (i = (arrayOfChatColor = ChatColor.values()).length, b = 0; b < i;)
		{
			ChatColor chatColor = arrayOfChatColor[b];
			Color color = getChatColorPaint(chatColor, awtColor);
			if (color != null)
			{
				double distance = calcColorDistance(awtColor, color);
				if (shorterDistance == -1.0D || distance < shorterDistance)
				{
					nearestColor = chatColor;
					shorterDistance = distance;
				}
			}
			b++;
		}
		return nearestColor.toString();
	}

    private static Color getChatColorPaint(ChatColor chatColor, Color awtColor) 
    {
        if (awtColor.getRed() == awtColor.getBlue() && awtColor.getBlue() == awtColor.getGreen()) 
        {
            if (ChatColor.BLACK.equals((Object)chatColor)) 
            {
                return new Color( 0x000000 );
            }
            if (ChatColor.DARK_GRAY.equals((Object)chatColor)) 
            {
                return new Color( 0x555555 );
            }
            if (ChatColor.GRAY.equals((Object)chatColor)) 
            {
                return new Color( 0xAAAAAA );
            }
            if (ChatColor.WHITE.equals((Object)chatColor)) 
            {
                return new Color( 0xFFFFFF );
            }
        }
        if (ChatColor.AQUA.equals((Object)chatColor)) 
        {
            return new Color( 0x55FFFF );
        }
        if (ChatColor.BLUE.equals((Object)chatColor)) 
        {
            return new Color( 0x05555FF );
        }
        if (ChatColor.DARK_BLUE.equals((Object)chatColor)) 
        {
            return new Color( 0x0000AA );
        }
        if (ChatColor.DARK_AQUA.equals((Object)chatColor)) 
        {
            return new Color( 0x00AAAA );
        }
        if (ChatColor.GREEN.equals((Object)chatColor)) 
        {
            return new Color( 0x55FF55 );
        }
        if (ChatColor.DARK_GREEN.equals((Object)chatColor)) 
        {
            return new Color( 0x00AA00 );
        }
        if (ChatColor.DARK_PURPLE.equals((Object)chatColor)) 
        {
            return new Color( 0xAA00AA );
        }
        if (ChatColor.LIGHT_PURPLE.equals((Object)chatColor)) 
        {
            return new Color( 0xFF55FF );
        }
        if (ChatColor.RED.equals((Object)chatColor)) 
        {
            return new Color( 0xFF5555 );
        }
        if (ChatColor.DARK_RED.equals((Object)chatColor)) 
        {
            return new Color( 0xAA0000 );
        }
        if (ChatColor.YELLOW.equals((Object)chatColor))
        {
            return new Color( 0xFFFF55 );
        }
        if (ChatColor.GOLD.equals((Object)chatColor)) 
        {
            return new Color( 0xFFAA00 );
        }
        return null;
    }

    private static double calcColorDistance(Color awtColor, Color color) 
    {
        return Math.sqrt(Math.pow(awtColor.getRed() - color.getRed(), 2.0) + Math.pow(awtColor.getGreen() - color.getGreen(), 2.0) + Math.pow(awtColor.getBlue() - color.getBlue(), 2.0));
    }
    
    private static boolean supportsHex;
    
    private static boolean checkHexSupport() 
    {
        try 
        {
            ChatColor.of((Color)Color.BLACK);
            return true;
        }
        catch (NoSuchMethodError e) 
        {
            return false;
        }
    }

    public static boolean supportsHex() 
    {
        return supportsHex;
    }
}