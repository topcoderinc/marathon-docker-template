package sample.submission;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;

/*
 * A simple solution, works by doing linear regression with parameters
 * calculated previously by the Trainer.
 */
public class Tester {
	public static void main(String[] args) throws Exception {
		// arg0: test file path
		// arg1: output csv path
		String inPath = null;
		String outPath = null;
		String modelPath = null; 
		if (args.length != 3) {
			System.out.println("Usage: java sample.submission.Tester <test-path> <output-path> <model-path>");
			System.exit(1);
		}
		else {
			inPath = args[0];
			outPath = args[1];
			modelPath = args[2];
		}
		
		LineNumberReader lnr = new LineNumberReader(new FileReader(modelPath));
		double a = Double.parseDouble(lnr.readLine());
		double b = Double.parseDouble(lnr.readLine());
		lnr.close();
		
		PrintWriter out = new PrintWriter(new File(outPath));
		lnr = new LineNumberReader(new FileReader(inPath));
		while (true) {
			String line = lnr.readLine();
			if (line == null) break;
			// name,height
			// joe,1.80
			line = line.trim();
			if (line.isEmpty() || line.contains("name")) continue;
			String[] parts = line.split(",");
			String name = parts[0];
			double h = Double.parseDouble(parts[1]);
			double w = a * h + b;
			out.println(name + "," + w);
		}
		lnr.close();
		out.close();
	}
}
