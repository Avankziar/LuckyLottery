package me.avankziar.base.general.objects;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import me.avankziar.base.general.database.MysqlBaseHandler;
import me.avankziar.base.general.database.MysqlHandable;
import me.avankziar.base.general.database.QueryType;

/**
 * Example Object
 * @author User
 *
 */
public class PlayerData implements MysqlHandable
{
	private int id;
	private UUID uuid;
	private String name;
	private double balance;
	private boolean moneyPlayerFlow;
	private boolean moneyBankFlow;
	private boolean generalMessage;
	private String pendingInvite;
	private boolean frozen; //To Freeze a Playeraccount
	private List<String> bankAccountNumber;
	
	public PlayerData(){}
	
	public PlayerData(int id, UUID uuid, String name,
			double balance, List<String> bankAccountNumber,
			boolean moneyPlayerFlow, boolean moneyBankFlow, boolean generalMessage,
			String pendingInvite, boolean frozen)
	{
		setId(id);
		setUUID(uuid);
		setName(name);
		setBalance(balance);
		setBankAccountNumber(bankAccountNumber);
		setMoneyPlayerFlow(moneyPlayerFlow);
		setMoneyBankFlow(moneyBankFlow);
		setGeneralMessage(generalMessage);
		setPendingInvite(pendingInvite);
		setFrozen(frozen);
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}
	
	public UUID getUUID()
	{
		return uuid;
	}

	public void setUUID(UUID uuid)
	{
		this.uuid = uuid;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public double getBalance()
	{
		return balance;
	}

	public void setBalance(double balance)
	{
		this.balance = balance;
	}

	public List<String> getBankAccountNumber()
	{
		return bankAccountNumber;
	}

	public void setBankAccountNumber(List<String> bankAccountNumber)
	{
		this.bankAccountNumber = bankAccountNumber;
	}
	
	public boolean isMoneyPlayerFlow()
	{
		return moneyPlayerFlow;
	}

	public void setMoneyPlayerFlow(boolean moneyPlayerFlow)
	{
		this.moneyPlayerFlow = moneyPlayerFlow;
	}

	public boolean isMoneyBankFlow()
	{
		return moneyBankFlow;
	}

	public void setMoneyBankFlow(boolean moneyBankFlow)
	{
		this.moneyBankFlow = moneyBankFlow;
	}

	public boolean isGeneralMessage()
	{
		return generalMessage;
	}

	public void setGeneralMessage(boolean generalMessage)
	{
		this.generalMessage = generalMessage;
	}

	public String getPendingInvite()
	{
		return pendingInvite;
	}

	public void setPendingInvite(String pendingInvite)
	{
		this.pendingInvite = pendingInvite;
	}

	public boolean isFrozen()
	{
		return frozen;
	}

	public void setFrozen(boolean frozen)
	{
		this.frozen = frozen;
	}
	
	public PlayerData(ResultSet rs)
	{
		
	}

	@Override
	public boolean create(Connection conn, String tablename)
	{
		try
		{
			String sql = "INSERT INTO `" + tablename
					+ "`(`player_uuid`, `player_name`, `balance`, `bankaccountlist`,"
					+ " `moneyplayerflow`, `moneybankflow`, `generalmessage`, `pendinginvite`, `frozen`) " 
					+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
	        ps.setString(1, getUUID().toString());
	        ps.setString(2, getName());
	        ps.setDouble(3, getBalance());
	        ps.setString(4, String.join(";", getBankAccountNumber()));
	        ps.setBoolean(5, isMoneyBankFlow());
	        ps.setBoolean(6, isMoneyPlayerFlow());
	        ps.setBoolean(7, isGeneralMessage());
	        ps.setString(8, getPendingInvite());
	        ps.setBoolean(9, isFrozen());
	        int i = ps.executeUpdate();
	        MysqlBaseHandler.addRows(QueryType.INSERT, i);
	        return true;
		} catch (SQLException e)
		{
			this.log(MysqlBaseHandler.getLogger(), Level.WARNING, "SQLException! Could not create a "+this.getClass().getSimpleName()+" Object!", e);
		}
		return false;
	}

	@Override
	public boolean update(Connection conn, String tablename, String whereColumn, Object... whereObject)
	{
		try
		{
			String sql = "UPDATE `" + tablename
				+ "` SET `player_uuid` = ?, `player_name` = ?, `balance` = ?,"
				+ " `bankaccountlist` = ?, `moneyplayerflow` = ?, `moneybankflow` = ?, `generalmessage` = ?,"
				+ " `pendinginvite` = ?, `frozen` = ?" 
				+ " WHERE "+whereColumn;
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, getUUID().toString());
			ps.setString(2, getName());
			ps.setDouble(3, getBalance());
			ps.setString(4, String.join(";", getBankAccountNumber()));
			ps.setBoolean(5, isMoneyPlayerFlow());
			ps.setBoolean(6, isMoneyBankFlow());
			ps.setBoolean(7, isGeneralMessage());
			ps.setString(8, getPendingInvite());
			ps.setBoolean(9, isFrozen());
			int i = 10;
			for(Object o : whereObject)
			{
				ps.setObject(i, o);
				i++;
			}			
			int u = ps.executeUpdate();
			MysqlBaseHandler.addRows(QueryType.UPDATE, u);
			return true;
		} catch (SQLException e)
		{
			this.log(MysqlBaseHandler.getLogger(), Level.WARNING, "SQLException! Could not update a "+this.getClass().getSimpleName()+" Object!", e);
		}
		return false;
	}

	@Override
	public ArrayList<Object> get(Connection conn, String tablename, String orderby, String limit, String whereColumn, Object... whereObject)
	{
		try
		{
			String sql = "SELECT * FROM `" + tablename
				+ "` WHERE "+whereColumn+" ORDER BY "+orderby+limit;
			PreparedStatement ps = conn.prepareStatement(sql);
			int i = 1;
			for(Object o : whereObject)
			{
				ps.setObject(i, o);
				i++;
			}
			
			ResultSet rs = ps.executeQuery();
			MysqlBaseHandler.addRows(QueryType.READ, rs.getMetaData().getColumnCount());
			ArrayList<Object> al = new ArrayList<>();
			while (rs.next()) 
			{
				String bankacc = rs.getString("bankaccountlist");
				List<String> lists = new ArrayList<>();
				if(bankacc != null)
				{
					lists = Arrays.asList(rs.getString("bankaccountlist").split(";"));
				}
				al.add(new PlayerData(rs.getInt("id"),
						UUID.fromString(rs.getString("player_uuid")),
						rs.getString("player_name"),
						rs.getDouble("balance"),
						lists,
						rs.getBoolean("moneyplayerflow"),
						rs.getBoolean("moneybankflow"),
						rs.getBoolean("generalmessage"),
						rs.getString("pendinginvite"),
						rs.getBoolean("frozen")));
			}
			return al;
		} catch (SQLException e)
		{
			this.log(MysqlBaseHandler.getLogger(), Level.WARNING, "SQLException! Could not get a "+this.getClass().getSimpleName()+" Object!", e);
		}
		return new ArrayList<>();
	}
	
	public static ArrayList<PlayerData> convert(ArrayList<Object> arrayList)
	{
		ArrayList<PlayerData> l = new ArrayList<>();
		for(Object o : arrayList)
		{
			if(o instanceof PlayerData)
			{
				l.add((PlayerData) o);
			}
		}
		return l;
	}
}