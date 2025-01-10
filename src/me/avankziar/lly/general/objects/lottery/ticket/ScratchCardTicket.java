package me.avankziar.lly.general.objects.lottery.ticket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

import me.avankziar.lly.general.database.MysqlBaseHandler;
import me.avankziar.lly.general.database.MysqlBaseSetup;
import me.avankziar.lly.general.database.MysqlLottery;
import me.avankziar.lly.general.database.QueryType;
import me.avankziar.lly.general.database.ServerType;
import me.avankziar.lly.general.objects.lottery.ScratchCard;
import me.avankziar.lly.spigot.handler.lottery.LotteryHandler;

public class ScratchCardTicket extends LotteryTicket implements MysqlLottery<ScratchCardTicket>
{
	private UUID lotteryPlayer;

	private double winningAmount;
	
	private ArrayList<Double> scratchfields = new ArrayList<>();
	
	/**
	 * <b>Only to call if the Mysql Setup is to do!</b>
	 * @param lotteryname
	 */
	public ScratchCardTicket(String lotteryname)
	{
		super(0, 0, lotteryname);
	}
	
	public ScratchCardTicket(long id, String lotteryname, UUID lotteryPlayer, double winningAmount, ArrayList<Double> scratchfields)
	{
		super(id, 0, lotteryname);
		setLotteryPlayer(lotteryPlayer);
		setWinningAmount(winningAmount);
		setScratchfields(scratchfields);
	}
	
	public UUID getLotteryPlayer()
	{
		return lotteryPlayer;
	}

	public void setLotteryPlayer(UUID lotteryPlayer)
	{
		this.lotteryPlayer = lotteryPlayer;
	}

	public double getWinningAmount()
	{
		return winningAmount;
	}

	public void setWinningAmount(double winningAmount)
	{
		this.winningAmount = winningAmount;
	}

	public ArrayList<Double> getScratchfields()
	{
		return scratchfields;
	}

	public void setScratchfields(ArrayList<Double> scratchfields)
	{
		this.scratchfields = scratchfields;
	}

	public String getMysqlTableName()
	{
		return "lly"+getLotteryName()+"Ticket";
	}
	
	public boolean setupMysql(MysqlBaseSetup mysqlSetup, ServerType serverType)
	{
		Optional<ScratchCard> osc = LotteryHandler.getScratchCard(getLotteryName());
		if(osc.isEmpty())
		{
			return false;
		}
		ScratchCard sc = osc.get();
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE IF NOT EXISTS `"+getMysqlTableName()
				+ "` (id bigint AUTO_INCREMENT PRIMARY KEY,"
				+ " player_uuid char(36) NOT NULL,"
				+ " winning_amount double,");
		for(int i = 1; i <= sc.getAmountOfFields(); i++)
        {
			sql.append(", `field_"+i+"` int");
        }
		sql.append(");");
		return mysqlSetup.baseSetup(sql.toString());
	}

	@Override
	public boolean create(Connection conn)
	{
		try
		{
			Optional<ScratchCard> osc = LotteryHandler.getScratchCard(getLotteryName());
			if(osc.isEmpty())
			{
				return false;
			}
			ScratchCard sc = osc.get();
			String tablename = getMysqlTableName();
			StringBuilder sql = new StringBuilder();
			sql.append("INSERT INTO `" + tablename
					+ "`(`player_uuid`, `winning_amount`");
			for(int i = 1; i <= sc.getAmountOfFields(); i++)
	        {
				sql.append(", `field_"+i+"`");
	        }
			sql.append(") VALUES(?, ?");
			for(int i = 1; i <= sc.getAmountOfFields(); i++)
	        {
				sql.append(", ?");
	        }
			sql.append(")");
			PreparedStatement ps = conn.prepareStatement(sql.toString());
			ps.setString(1, getLotteryPlayer().toString());
	        ps.setDouble(2, getWinningAmount());
	        int c = 3;
	        Iterator<Double> iter = getScratchfields().iterator();
	        for(int i = 1; i <= sc.getAmountOfFields(); i++)
	        {
	        	ps.setDouble(c, iter.hasNext() ? iter.next() : 0);
	        	c++;
	        }
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
	public boolean update(Connection conn, String whereColumn, Object... whereObject)
	{
		try
		{
			Optional<ScratchCard> osc = LotteryHandler.getScratchCard(getLotteryName());
			if(osc.isEmpty())
			{
				return false;
			}
			ScratchCard sc = osc.get();
			String tablename = getMysqlTableName();
			StringBuilder sql = new StringBuilder();
			sql.append("UPDATE `" + tablename
				+ "` SET `player_uuid` = ?, `winning_amount` = ?");
			for(int i = 1; i <= sc.getAmountOfFields(); i++)
	        {
				sql.append(", `field_"+i+"` = ?");
	        }
			sql.append(" WHERE "+whereColumn);
			PreparedStatement ps = conn.prepareStatement(sql.toString());
			ps.setString(1, getLotteryPlayer().toString());
	        ps.setDouble(2, getWinningAmount());
	        int c = 3;
	        Iterator<Double> iter = getScratchfields().iterator();
	        for(int i = 1; i <= sc.getAmountOfFields(); i++)
	        {	
	        	ps.setDouble(c, iter.hasNext() ? iter.next() : 0);
	        	c++;
	        }
			int i = c;
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
	public ArrayList<ScratchCardTicket> get(Connection conn, String orderby, String limit, String whereColumn, Object... whereObject)
	{
		try
		{
			Optional<ScratchCard> osc = LotteryHandler.getScratchCard(getLotteryName());
			if(osc.isEmpty())
			{
				return new ArrayList<>();
			}
			ScratchCard sc = osc.get();
			String tablename = getMysqlTableName();
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
			ArrayList<ScratchCardTicket> al = new ArrayList<>();
			while (rs.next()) 
			{
			
				ArrayList<Double> set = new ArrayList<>();
				for(int ii = 1; i <= sc.getAmountOfFields(); i++)
		        {
					set.add(rs.getDouble("field_"+ii));
		        }
				al.add(new ScratchCardTicket(rs.getLong("id"),
						getLotteryName(),
						UUID.fromString(rs.getString("player_uuid")),
						rs.getDouble("winning_amount"),
						set));
			}
			return al;
		} catch (SQLException e)
		{
			this.log(MysqlBaseHandler.getLogger(), Level.WARNING, "SQLException! Could not get a "+this.getClass().getSimpleName()+" Object!", e);
		}
		return new ArrayList<>();
	}
}