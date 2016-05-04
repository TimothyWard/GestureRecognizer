package GestureRecognizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFileChooser;

public class DataConverter {

	private class Point {
		public double x1;
		public double x2;

		public Point(double r, double a) {
			x1 = r;
			x2 = a;
		}
	}

	private List<Point> dataPointsA;
	private List<Point> dataPointsB;
	private List<Integer> outputs;
	private String label;

	public DataConverter(File dataFile) {
		File newDataFile = new File(dataFile.getName());
		dataPointsA = new LinkedList<Point>();
		dataPointsB = new LinkedList<Point>();
		outputs = new LinkedList<Integer>();
		label = dataFile.getName().replace("_data.txt", "");
		try {
			PrintWriter output = new PrintWriter(newDataFile);
			String data = convertFile(dataFile);
			output.println(data);
			output.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		} 
	}

	private void constructOutputs(String line) {
		if(line.startsWith("Closed")) {
			outputs.add(1);
			label = label + "-Closed";
		}
		else {
			outputs.add(-1);
		}
		if(line.startsWith("Open")) {
			outputs.add(1);
			label = label + "-Open";
		}
		else {
			outputs.add(-1);
		}
		if(line.startsWith("Neither")) {
			label = label + "-Neither";
		}
	}

	public String convertFile(File originalData) throws FileNotFoundException {
		String line;
		Scanner input = new Scanner(originalData);
		line = input.nextLine();
		constructOutputs(line);
		double max = 0;
		List<Point> dataPoints = dataPointsA;
		while(input.hasNextLine()) {
			line = input.nextLine();
			if(line.startsWith("Concave")) {
				dataPoints = dataPointsB;
				continue;
			}
			else if(line.startsWith("Convex") || line.length() == 0) {
				continue;
			}
			else if(line.startsWith("Center")) {
				break;
			}
			String[] parts = line.split(",");
			double r = Double.parseDouble(parts[0]);
			max = Math.max(max, r);
			double a = Double.parseDouble(parts[1]);
			a = (a+180)/360.0; // Convert angle to within [0,1]
			dataPoints.add(new Point(r, a));
		}
		input.close();
		String lineA = "";
		String lineB = "";
		for(int i = 0; i < dataPointsA.size(); i++) {
			Point pA = dataPointsA.get(i);
			Point pB = dataPointsB.get(i);
			pA.x1 = pA.x1/max; // Normalize radius reduce effect of hand size
			lineA = lineA + pA.x1 + ", " + pA.x2 + 
					(i == dataPointsA.size()-1?"; ":", ");
			pB.x1 = pB.x1/max; // Normalize radius reduce effect of hand size
			lineB = lineB + pB.x1 + ", " + pB.x2 + 
					(i == dataPointsA.size()-1?"; ":", ");
		}
		line = lineA + lineB;
		for(int i = 0; i < outputs.size()-1; i++) {
			line = line + outputs.get(i) + ", ";
		}
		line = line + outputs.get(outputs.size()-1);
		line = line + ";" + label;
		return line;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File currDirectory = new File(".");
		if(currDirectory.exists()) {
			System.out.println("pwd: " + currDirectory.getAbsolutePath());
		}
		JFileChooser fileChoice = new JFileChooser();
		int returnVal = fileChoice.showOpenDialog(null);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			File directory =  fileChoice.getSelectedFile();
			if(!directory.isDirectory()) {
				directory = directory.getParentFile();
			}
			for(File data: directory.listFiles()) {
				if(data.getName().endsWith(".txt")) {
					new DataConverter(data);
				}
			}
		}
	}

}
