package me.avankziar.lly.general.objects.lottery.draw;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.logging.Level;

import me.avankziar.lly.general.database.MysqlBaseHandler;
import me.avankziar.lly.general.database.MysqlLottery;
import me.avankziar.lly.general.database.QueryType;
import me.avankziar.lly.general.objects.lottery.ClassicLotto;
import me.avankziar.lly.spigot.database.MysqlSetup;
import me.avankziar.lly.spigot.handler.lottery.LotteryHandler;

public class ClassicLottoDraw extends LotteryDraw implements MysqlLottery<ClassicLottoDraw>
{
	private boolean wasDrawn;
	private long drawTime;
	private LinkedHashSet<Integer> choosenNumbers = new LinkedHashSet<>();
	private double actualPot;
	
	/**
	 * <b>Only to call if the Mysql Setup is to do!</b>
	 * @param lotteryname
	 */
	public ClassicLottoDraw(String lotteryname)
	{
		super(0, lotteryname);
	}
	
	public ClassicLottoDraw(long id, String lotteryname,
			boolean wasDrawn, long drawTime,
			double actualPot,
			LinkedHashSet<Integer> choosenNumbers)
	{
		super(id, lotteryname);
		setWasDrawn(wasDrawn);
		setDrawTime(drawTime);
		setActualPot(actualPot);
		setChoosenNumbers(choosenNumbers);
	}

	public boolean wasDrawn()
	{
		return wasDrawn;
	}

	public void setWasDrawn(boolean wasDrawn)
	{
		this.wasDrawn = wasDrawn;
	}

	public long getDrawTime()
	{
		return drawTime;
	}

	public void setDrawTime(long drawTime)
	{
		this.drawTime = drawTime;
	}

	public double getActualPot() {
		return actualPot;
	}

	public void setActualPot(double actualPot) {
		this.actualPot = actualPot;
	}

	public LinkedHashSet<Integer> getChoosenNumbers()
	{
		return choosenNumbers;
	}

	public void setChoosenNumbers(LinkedHashSet<Integer> choosenNumbers)
	{
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
				+ " lottery_name text NOT NULL,"
				+ " was_drawn boolean,"
				+ " draw_time bigint,"
				+ " actual_pot double".replace("%%tablename%%", getMysqlTableName()));
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
					+ "`(`lottery_name`, `was_drawn`, `draw_time`, `actual_pot`");
			for(int i = 0; i < cl.getAmountOfChoosedNumber(); i++)
	        {
				sql.append(" ,`ball_"+i+"`");
	        }
			sql.append(") VALUES(?, ?, ?, ?");
			for(int i = 0; i < cl.getAmountOfChoosedNumber(); i++)
	        {
				sql.append(", ?");
	        }
			sql.append(")");
			PreparedStatement ps = conn.prepareStatement(sql.toString());
			ps.setString(1, getLotteryName());
	        ps.setBoolean(2, wasDrawn());
	        ps.setLong(3, getDrawTime());
	        ps.setDouble(4, getActualPot());
	        int c = 5;
	        for(int i = 0; i < cl.getAmountOfChoosedNumber(); i++)
	        {
	        	Iterator<Integer> iter = getChoosenNumbers().iterator();
	        	ps.setInt(c, iter.hasNext() ? iter.next() : 0);
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
				+ "` SET `lottery_name`, `was_drawn` = ?, `draw_time` = ?, `actual_pot` = ?");
			for(int i = 0; i < cl.getAmountOfChoosedNumber(); i++)
	        {
				sql.append(", `ball_"+i+"` = ?");
	        }
			sql.append(" WHERE "+whereColumn);
			PreparedStatement ps = conn.prepareStatement(sql.toString());
			ps.setString(1, getLotteryName());
	        ps.setBoolean(2, wasDrawn());
	        ps.setLong(3, getDrawTime());
	        ps.setDouble(4, getActualPot());
	        int c = 5;
	        for(int i = 0; i < cl.getAmountOfChoosedNumber(); i++)
	        {
	        	Iterator<Integer> iter = getChoosenNumbers().iterator();
	        	ps.setInt(c, iter.hasNext() ? iter.next() : 0);
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
	public ArrayList<ClassicLottoDraw> get(Connection conn, String orderby, String limit, String whereColumn, Object... whereObject)
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
			ArrayList<ClassicLottoDraw> al = new ArrayList<>();
			while (rs.next()) 
			{
			
				LinkedHashSet<Integer> set = new LinkedHashSet<>();
				for(int ii = 0; ii < cl.getAmountOfChoosedNumber(); ii++)
		        {
					set.add(rs.getInt("ball_"+ii));
		        }
				al.add(new ClassicLottoDraw(rs.getLong("id"),
						rs.getString("lottery_name"),
						rs.getBoolean("was_drawn"),
						rs.getLong("draw_time"),
						rs.getDouble("actual_pot"),
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