package engine.data.fetch;

import java.util.List;

import engine.caster.ObjectCaster;
import jep.JepException;
import jep.SharedInterpreter;

public class BacktestDataLoader {
	List<String> index;

	public BacktestDataLoader() {
	}

	public double[][][] getMOHLCV() {
		double[][][] output = null;

		try (SharedInterpreter interp = new SharedInterpreter()) {
			interp.eval("import sys");
			interp.eval("sys.path.insert(0, 'app/src/main/java/engine/data/fetch')");

			String pythonPath = "../tools/python/src/fetch/load_csv_data.py";
			interp.runScript(pythonPath);

			interp.invoke("prepareData");
			List<List<List<Double>>> data = (List<List<List<Double>>>) interp.invoke("get_ret_list");
			output = ObjectCaster.toDouble3D(data);

			List<Object> rawIndices = (List<Object>) interp.invoke("get_indices");
			this.index = (List<String>) rawIndices.get(0);

		} catch (JepException e) {
			e.printStackTrace();
			System.exit(500);
		}

		return output;
	}

	public List<String> getIndex() {
		return this.index;
	}
}
