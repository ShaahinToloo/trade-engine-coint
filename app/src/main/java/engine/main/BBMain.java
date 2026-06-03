package engine.main;

import engine.heads.BBBacktest;

public class BBMain extends Main {

	public BBMain() {
		super(false);
	}

	public static void main(String[] args) {

		BBMain app = new BBMain();

		BBBacktest backtest = new BBBacktest(app.A, app.B);

		System.out.println("Java --BackTest Started in 'BBMain'");

		backtest.start();

		backtest.getResults(
				true,
				"/home/arch/data");

		System.out.println("\n\t[--- Program Finished ---]");
	}
}