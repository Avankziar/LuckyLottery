package me.avankziar.lly.general.objects.lottery.ticket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

import me.avankziar.lly.general.database.MysqlBaseHandler;
import me.avankziar.lly.general.database.MysqlBaseSetup;
import me.avankziar.lly.general.database.MysqlLottery;
import me.avankziar.lly.general.database.QueryType;
import me.avankziar.lly.general.database.ServerType;
import me.avankziar.lly.general.objects.lottery.LottoSuper;
import me.avankziar.lly.spigot.handler.lottery.LotteryHandler;

public class LottoSuperTicket extends LotteryTicket implements MysqlLottery<LottoSuperTicket>
{
	private UUID lotteryPlayer;
	/**
	 * If the used numbers should set again in the next lottery.
	 */
	private boolean shouldRepeate;
	private LinkedHashSet<Integer> choosenNumbers = new LinkedHashSet<>();
	private LinkedHashSet<Integer> additionalChoosenNumbers = new LinkedHashSet<>();
	private int winningClassLevel;
	private double winningPrize;
	
	/**
	 * <b>Only to call if the Mysql Setup is to do!</b>
	 * @param lotteryname
	 */
	public LottoSuperTicket(String lotteryname)
	{
		super(0, 0, lotteryname);
	}
	
	public LottoSuperTicket(long id, long drawid, String lotteryname,
			UUID lotteryPlayer, boolean shouldRepeate, int winningClassLevel, double winningPrize,
			LinkedHashSet<Integer> choosenNumbers, LinkedHashSet<Integer> additionalChoosenNumbers)
	{
		super(0, drawid, lotteryname);
		setLotteryPlayer(lotteryPlayer);
		setShouldRepeate(shouldRepeate);
		setChoosenNumbers(choosenNumbers);
		setWinningClassLevel(winningClassLevel);
		setWinningPrize(winningPrize);
		setAdditionalChoosenNumbers(additionalChoosenNumbers);
	}
	
	public UUID getLotteryPlayer() {
		return lotteryPlayer;
	}

	public void setLotteryPlayer(UUID lotteryPlayer) {
		this.lotteryPlayer = lotteryPlayer;
	}

	public boolean shouldRepeate() {
		return shouldRepeate;
	}

	public void setShouldRepeate(boolean shouldRepeate) {
		this.shouldRepeate = shouldRepeate;
	}

	public int getWinningClassLevel()
	{
		return winningClassLevel;
	}

	public void setWinningClassLevel(int winningClassLevel)
	{
		this.winningClassLevel = winningClassLevel;
	}

	public double getWinningPrize()
	{
		return winningPrize;
	}

	public void setWinningPrize(double winningPrize)
	{
		this.winningPrize = winningPrize;
	}

	public LinkedHashSet<Integer> getChoosenNumbers() {
		return choosenNumbers;
	}

	public void setChoosenNumbers(LinkedHashSet<Integer> choosenNumbers) {
		this.choosenNumbers = choosenNumbers;
	}
	
	public LinkedHashSet<Integer> getAdditionalChoosenNumbers() {
		return additionalChoosenNumbers;
	}

	public void setAdditionalChoosenNumbers(LinkedHashSet<Integer> additionalChoosenNumbers) {
		this.additionalChoosenNumbers = additionalChoosenNumbers;
	}
	
	public String getMysqlTableName()
	{
		return "lly"+getLotteryName()+"Ticket";
	}
	
	public boolean setupMysql(MysqlBaseSetup mysqlSetup, ServerType serverType)
	{
		Optional<LottoSuper> ocl = LotteryHandler.getLottoSuper(getLotteryName());
		if(ocl.isEmpty())
		{
			return false;
		}
		LottoSuper ls = ocl.get();
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE IF NOT EXISTS `"+getMysqlTableName()
				+ "` (id bigint AUTO_INCREMENT PRIMARY KEY,"
				+ " draw_id bigint,"
				+ " player_uuid char(36) NOT NULL,"
				+ " should_repeat boolean,"
				+ " winning_class_level int,"
				+ " winning_price double");
		for(int i = 1; i <= ls.getAmountOfChoosedNumber(); i++)
        {
			sql.append(" ,`ball_"+i+"`");
        }
		for(int i = 1; i <= ls.getAdditionalAmountOfChoosenNumber(); i++)
        {
			sql.append(" ,`super_ball_"+i+"`");
        }
		sql.append(");");
		return mysqlSetup.baseSetup(sql.toString());
	}

	@Override
	public boolean create(Connection conn)
	{
		try
		{
			Optional<LottoSuper> ocl = LotteryHandler.getLottoSuper(getLotteryName());
			if(ocl.isEmpty())
			{
				return false;
			}
			LottoSuper ls = ocl.get();
			String tablename = getMysqlTableName();
			StringBuilder sql = new StringBuilder();
			sql.append("INSERT INTO `" + tablename
					+ "`(`draw_id`, `player_uuid`, `should_repeat`");
			for(int i = 1; i <= ls.getAmountOfChoosedNumber(); i++)
	        {
				sql.append(" ,`ball_"+i+"`");
	        }
			for(int i = 1; i <= ls.getAdditionalAmountOfChoosenNumber(); i++)
	        {
				sql.append(" ,`super_ball_"+i+"`");
	        }
			sql.append(") VALUES(?, ?, ?");
			for(int i = 1; i <= ls.getAmountOfChoosedNumber(); i++)
	        {
				sql.append(", ?");
	        }
			for(int i = 1; i <= ls.getAdditionalAmountOfChoosenNumber(); i++)
	        {
				sql.append(", ?");
	        }
			sql.append(")");
			PreparedStatement ps = conn.prepareStatement(sql.toString());
			ps.setLong(1, getDrawID());
	        ps.setString(2, getLotteryPlayer().toString());
	        ps.setBoolean(3, shouldRepeate());
	        ps.setInt(4, getWinningClassLevel());
	        ps.setDouble(5, getWinningPrize());
	        int c = 6;
	        Iterator<Integer> iter = getChoosenNumbers().iterator();
	        for(int i = 1; i <= ls.getAmountOfChoosedNumber(); i++)
	        {
	        	ps.setInt(c, iter.hasNext() ? iter.next() : 0);
	        	c++;
	        }
	        Iterator<Integer> iters = getAdditionalChoosenNumbers().iterator();
	        for(int i = 1; i <= ls.getAdditionalAmountOfChoosenNumber(); i++)
	        {
	        	ps.setInt(c, iters.hasNext() ? iters.next() : 0);
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
			Optional<LottoSuper> ols = LotteryHandler.getLottoSuper(getLotteryName());
			if(ols.isEmpty())
			{
				return false;
			}
			LottoSuper ls = ols.get();
			String tablename = getMysqlTableName();
			StringBuilder sql = new StringBuilder();
			sql.append("UPDATE `" + tablename
				+ "` SET `draw_id` = ?, `player_uuid` = ?, `should_repeat` = ?");
			for(int i = 1; i <= ls.getAmountOfChoosedNumber(); i++)
	        {
				sql.append(", `ball_"+i+"` = ?");
	        }
			for(int i = 1; i <= ls.getAdditionalAmountOfChoosenNumber(); i++)
	        {
				sql.append(", `super_ball_"+i+"` = ?");
	        }
			sql.append(" WHERE "+whereColumn);
			PreparedStatement ps = conn.prepareStatement(sql.toString());
			ps.setLong(1, getDrawID());
	        ps.setString(2, getLotteryPlayer().toString());
	        ps.setBoolean(3, shouldRepeate());
	        ps.setInt(4, getWinningClassLevel());
	        ps.setDouble(5, getWinningPrize());
	        int c = 6;
	        Iterator<Integer> iter = getChoosenNumbers().iterator();
	        for(int i = 1; i <= ls.getAmountOfChoosedNumber(); i++)
	        {
	        	ps.setInt(c, iter.hasNext() ? iter.next() : 0);
	        	c++;
	        }
	        Iterator<Integer> iters = getAdditionalChoosenNumbers().iterator();
	        for(int i = 1; i <= ls.getAdditionalAmountOfChoosenNumber(); i++)
	        {
	        	ps.setInt(c, iters.hasNext() ? iters.next() : 0);
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
	public ArrayList<LottoSuperTicket> get(Connection conn, String orderby, String limit, String whereColumn, Object... whereObject)
	{
		try
		{
			Optional<LottoSuper> ols = LotteryHandler.getLottoSuper(getLotteryName());
			if(ols.isEmpty())
			{
				return null;
			}
			LottoSuper ls = ols.get();
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
			ArrayList<LottoSuperTicket> al = new ArrayList<>();
			while (rs.next()) 
			{
			
				LinkedHashSet<Integer> set = new LinkedHashSet<>();
				for(int ii = 0; ii < ls.getAmountOfChoosedNumber(); ii++)
		        {
					set.add(rs.getInt("ball_"+ii));
		        }
				LinkedHashSet<Integer> sets = new LinkedHashSet<>();
				for(int ii = 0; ii < ls.getAdditionalAmountOfChoosenNumber(); ii++)
		        {
					sets.add(rs.getInt("super_ball_"+ii));
		        }
				al.add(new LottoSuperTicket(rs.getLong("id"),
						rs.getLong("draw_id"),
						getLotteryName(),
						UUID.fromString(rs.getString("player_uuid")),
						rs.getBoolean("should_repeat"),
						rs.getInt("winning_class_level"),
						rs.getDouble("winning_price"),
						set,
						sets));
			}
			return al;
		} catch (SQLException e)
		{
			this.log(MysqlBaseHandler.getLogger(), Level.WARNING, "SQLException! Could not get a "+this.getClass().getSimpleName()+" Object!", e);
		}
		return new ArrayList<>();
	}
}