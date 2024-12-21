package me.avankziar.lly.general.objects.lottery;

public class Lottery 
{
	public enum GameType
	{
		/**
		 * The player choose a x amount of non repetitive numbers of a pool from y non repetitive numbers.<br>
		 * F.e.:<br>
		 * 6 from 49. And player choose 2, 17, 22, 29, 40, 42.<br>
		 * Reallife example: Lotto (Germany)
		 */
		X_FROM_Y,
		/**
		 * The player choose a x amount of non repetitive numbers of a pool from y non repetitive numbers
		 * and a z amount of non repetitive numbers of a pool from u non repetitive numbers.<br>
		 * F.e.:<br>
		 * 5 from 50 and 2 from 12. And the player choose 8, 11, 33, 35, 42 and 2, 10
		 * Reallife example: Eurojackpot (EU), Powerball (USA)
		 */
		X_FROM_Y_AND_Z_FROM_U,
		/**
		 * The player has x amount of 0-9 numbers to choose.<br>
		 * Every correct guessed number on the right spot increases the prize category.<br>
		 * F.e.:
		 * 7 Numbers to choose. Player choose 2446831 and the winning Number was 4440129.<br>
		 * So player has 2 Numbers right.
		 * Reallife example: Glücksspirale (Germany)
		 */
		X_OF_NUMBERS_ON_THE_RIGHT_SPOT,
		/**
		 * The player has x amount of Fields, where he can unravel the "prepinted" Winnings.<br>
		 * If y amount of the same winnings are shown in the field, the winnings are given to him.<br>
		 * A special Icon can be unravel to double the drawn winnings.<br>
		 * This type of lottery, will be played with oneself instead with more players.<br>
		 * Reallife example: Scratch cards
		 */
		X_NUMBER_OF_FIELDS,
		/**
		 * All player buys x amount of tickets. From all tickets are drawn the prize categorys.
		 * Reallife example: Tombola from streetsfeastivals
		 */
		TOMBOLA_WITHOUT_DUDS,
		/**
		 * Same as tombola but with duds. Meaning, for a bought ticket x amount of duds are placed in the tombola.
		 */
		TOMBOLA_WITH_DUDS,
		
	}
	
	/**
	 * The unique name of the lottery.
	 */
	private String lotteryName;
	/*
	 * Description of the game.
	 */
	private String description;
	/**
	 * The type of lottery.
	 */
	private GameType gameType;
	
	public Lottery(String lotteryName, String description, GameType gameType)
	{
		setLotteryName(lotteryName);
		setDescription(description);
		setGameType(gameType);
	}

	public String getLotteryName() {
		return lotteryName;
	}

	public void setLotteryName(String lotteryName) {
		this.lotteryName = lotteryName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public GameType getGameType() {
		return gameType;
	}

	public void setGameType(GameType gameType) {
		this.gameType = gameType;
	}
}
