package me.avankziar.lly.general.objects.lotteryticket;

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
import me.avankziar.lly.general.database.MysqlLottery;
import me.avankziar.lly.general.database.QueryType;
import me.avankziar.lly.general.objects.lottery.ClassicLotto;
import me.avankziar.lly.spigot.database.MysqlSetup;
import me.avankziar.lly.spigot.handler.lottery.LotteryHandler;

public class ClassicLottoTicket extends LotteryTicket implements MysqlLottery, Ticketable
{
	private UUID lotteryPlayer;
	/**
	 * If the used numbers should set again in the next lottery.
	 */
	private boolean shouldRepeate;
	/**
	 * Boolean to set if the lotto was drawn and is finish.
	 */
	private boolean wasDrawn;
	private LinkedHashSet<Integer> choosenNumbers = new LinkedHashSet<>();
	
	/**
	 * <b>Only to call if the Mysql Setup is to do!</b>
	 * @param lotteryname
	 */
	public ClassicLottoTicket(String lotteryname)
	{
		super(0, 0, lotteryname);
	}
	
	public ClassicLottoTicket(long id, long drawid, String lotteryname, UUID lotteryPlayer, boolean shouldRepeate, boolean wasDrawn,
			LinkedHashSet<Integer> choosenNumbers)
	{
		super(0, drawid, lotteryname);
		setLotteryPlayer(lotteryPlayer);
		setShouldRepeate(shouldRepeate);
		setWasDrawn(wasDrawn);
		setChoosenNumbers(choosenNumbers);
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

	public boolean wasDrawn() {
		return wasDrawn;
	}

	public void setWasDrawn(boolean wasDrawn) {
		this.wasDrawn = wasDrawn;
	}

	public LinkedHashSet<Integer> getChoosenNumbers() {
		return choosenNumbers;
	}

	public void setChoosenNumbers(LinkedHashSet<Integer> choosenNumbers) {
		this.choosenNumbers = choosenNumbers;
	}
	
	public boolean setupMysql(MysqlSetup mysqlSetup)
	{
		Optional<ClassicLotto> ocl = LotteryHandler.getClassicLottery(getLotteryName());
		if(ocl.isEmpty())
		{
			return false;
		}
		ClassicLotto cl = ocl.get();
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE IF NOT EXISTS `%%tablename%%"
				+ "` (id bigint AUTO_INCREMENT PRIMARY KEY,"
				+ " draw_id bigint,"
				+ " lottery_name text NOT NULL,"
				+ " player_uuid char(36) NOT NULL,"
				+ " should_repeat boolean,"
				+ " was_drawn boolean".replace("%%tablename%%", getMysqlTableName()));
		for(int i = 0; i < cl.getAmountOfChoosedNumber(); i++)
        {
			sql.append(" ,`ball_"+i+"`");
        }
		sql.append(");");
		return mysqlSetup.baseSetup(getMysqlTableName());
	}

	@Override
	public boolean create(Connection conn)
	{
		try
		{
			Optional<ClassicLotto> ocl = LotteryHandler.getClassicLottery(getLotteryName());
			if(ocl.isEmpty())
			{
				return false;
			}
			ClassicLotto cl = ocl.get();
			String tablename = getMysqlTableName();
			StringBuilder sql = new StringBuilder();
			sql.append("INSERT INTO `" + tablename
					+ "`(`draw_id`, `lottery_name`, `player_uuid`, `should_repeat`, `was_drawn`");
			for(int i = 0; i < cl.getAmountOfChoosedNumber(); i++)
	        {
				sql.append(" ,`ball_"+i+"`");
	        }
			sql.append(") VALUES(?, ?, ?, ?, ?");
			for(int i = 0; i < cl.getAmountOfChoosedNumber(); i++)
	        {
				sql.append(", ?");
	        }
			sql.append(")");
			PreparedStatement ps = conn.prepareStatement(sql.toString());
			ps.setString(1, getLotteryName());
	        ps.setString(2, getLotteryPlayer().toString());
	        ps.setBoolean(3, shouldRepeate());
	        ps.setBoolean(4, wasDrawn());
	        int c = 5;
	        for(Iterator<Integer> iter = getChoosenNumbers().iterator(); iter.hasNext();)
	        {
	        	ps.setInt(c, iter.next());
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
			Optional<ClassicLotto> ocl = LotteryHandler.getClassicLottery(getLotteryName());
			if(ocl.isEmpty())
			{
				return false;
			}
			ClassicLotto cl = ocl.get();
			String tablename = getMysqlTableName();
			StringBuilder sql = new StringBuilder();
			sql.append("UPDATE `" + tablename
				+ "` SET `draw_id`, `lottery_name`, `player_uuid` = ?, `should_repeat` = ?, `was_drawn` = ?");
			for(int i = 0; i < cl.getAmountOfChoosedNumber(); i++)
	        {
				sql.append(", `ball_"+i+"` = ?");
	        }
			sql.append(" WHERE "+whereColumn);
			PreparedStatement ps = conn.prepareStatement(sql.toString());
			ps.setString(1, getLotteryName());
	        ps.setString(2, getLotteryPlayer().toString());
	        ps.setBoolean(3, shouldRepeate());
	        ps.setBoolean(4, wasDrawn());
	        int c = 5;
	        for(Iterator<Integer> iter = getChoosenNumbers().iterator(); iter.hasNext();)
	        {
	        	ps.setInt(c, iter.next());
	        	c++;
	        }
			int i = c+1;
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
	public ArrayList<Object> get(Connection conn, String orderby, String limit, String whereColumn, Object... whereObject)
	{
		try
		{
			Optional<ClassicLotto> ocl = LotteryHandler.getClassicLottery(getLotteryName());
			if(ocl.isEmpty())
			{
				return null;
			}
			ClassicLotto cl = ocl.get();
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
			ArrayList<Object> al = new ArrayList<>();
			while (rs.next()) 
			{
			
				LinkedHashSet<Integer> set = new LinkedHashSet<>();
				for(int ii = 0; ii < cl.getAmountOfChoosedNumber(); ii++)
		        {
					set.add(rs.getInt("ball_"+ii));
		        }
				al.add(new ClassicLottoTicket(rs.getLong("id"),
						rs.getLong("draw_id"),
						rs.getString("lottery_name"),
						UUID.fromString(rs.getString("player_uuid")),
						rs.getBoolean("should_repeat"),
						rs.getBoolean("was_drawn"),
						set));
			}
			return al;
		} catch (SQLException e)
		{
			this.log(MysqlBaseHandler.getLogger(), Level.WARNING, "SQLException! Could not get a "+this.getClass().getSimpleName()+" Object!", e);
		}
		return new ArrayList<>();
	}
	
	public static ArrayList<ClassicLottoTicket> convert(ArrayList<Object> arrayList)
	{
		ArrayList<ClassicLottoTicket> l = new ArrayList<>();
		for(Object o : arrayList)
		{
			if(o instanceof ClassicLottoTicket)
			{
				l.add((ClassicLottoTicket) o);
			}
		}
		return l;
	}
}