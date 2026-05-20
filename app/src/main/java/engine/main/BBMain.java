package engine.main;

import engine.constants.BacktestConstants;
import engine.heads.Backtest;

public class BBMain extends Main {

	public BBMain() {
		super(false);
	}

	public static void main(String[] args) {

		BBMain app = new BBMain();

		Backtest backtest = new Backtest(app.A, app.B);

		System.out.println("Java --BackTest Started in 'BBMain'");

		backtest.start();

		backtest.getResults(
				true,
				"/home/arch/data",
				"BackTest/"+BacktestConstants.MAIN_FOLDER);

		System.out.println("\n\t[--- Program Finished ---]");
	}
}