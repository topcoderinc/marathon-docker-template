package sample.submission;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

/*
 * A simple trainer, calculates linear regression parameters from the
 * training data and saves it as 'model'.
 */
public class Trainer {
	public static void main(String[] args) throws Exception {
		// arg0: truth file
		// arg1: output 'model' file
		String trainPath = null;
		String outPath = null;
		if (args.length != 2) {
			System.out.println("Usage: java sample.submission.Trainer <truth-path> <model-path>");
			System.exit(1);
		}
		else {
			trainPath = args[0];
			outPath = args[1];
		}
		
		List<Double> xs = new Vector<>();
		List<Double> ys = new Vector<>();
		
		File truthCsv = new File(trainPath);
		LineNumberReader lnr = new LineNumberReader(new FileReader(truthCsv));
		while (true) {
			String line = lnr.readLine();
			if (line == null) break;
			// name,height,weight
			// joe,1.80,75
			line = line.trim();
			if (line.isEmpty() || line.contains("name")) continue;
			String[] parts = line.split(",");
			xs.add(Double.parseDouble(parts[1]));
			ys.add(Double.parseDouble(parts[2]));
		}
		lnr.close();
		
		int n = xs.size();
		double sumX = 0.0, sumY = 0.0;
		for (int i = 0; i < n; i++) {
			double x = xs.get(i);
			double y = ys.get(i);
            sumX  += x;
            sumY  += y;
        }
        		
		double avgX = sumX / n;
        double avgY = sumY / n;

        double xx = 0.0, xy = 0.0;
        for (int i = 0; i < n; i++) {
        	double x = xs.get(i);
			double y = ys.get(i);
            xx += (x - avgX) * (x - avgX);
            xy += (x - avgX) * (y - avgY);
        }
        double a  = xy / xx;
        double b = avgY - a * avgX;
        
		PrintWriter out = new PrintWriter(new File(outPath));
		out.println("" + a);
		out.println("" + b);
		out.close();
		System.out.println("Training finished. Model written to " + outPath);
	}
}
