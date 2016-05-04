package GestureRecognizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;

public class TestData {

	private int outputs = 0;
	private DataSet dataSet;
	private List<String[]> data;
	private int inputs = 0;

	public TestData() {
		this(new File("convertedData"), 0);
	}

	public TestData(File data, int inputSize) {
		this.dataSet = null;
		this.data = new LinkedList<String[]>();
		inputs = inputSize;
		if(data != null && data.exists()) {
			if(data.isDirectory()) {
				for(File f: data.listFiles()) {
					if(f.getName().endsWith(".txt")) { inputFile(f); }
				}
			}
			else if(data.isFile()) {
				inputFile(data);
			}
		}
		System.out.println("Data Set Size: " + this.data.size());
	}

	private void inputFile(File d) {
		Scanner in;
		try {
			in = new Scanner(d);
			while(in.hasNextLine()) {
				String line = in.nextLine();
				String[] parts = line.split(";");
				String[] dataA = parts[0].split(",");
				String[] dataB = parts[1].split(",");
				if(dataA.length < 18 && dataA.length > 12) {
					if(dataA.length != dataB.length) {
						System.err.println("Different number of concave and convex points");
						System.exit(-1);
					}
					inputs = Math.max(inputs, dataA.length);
					data.add(parts);
				}
				String[] outputData = parts[2].split(",");
				if(outputs == 0) {
					outputs = outputData.length;
				}
				else if(outputs != outputData.length) {
					System.err.println("Differing number of outputs");
					System.exit(-1);
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("Unable to open " 
					+ d.getAbsolutePath() + " for reading.");
			System.exit(-1);
		}
	}

	public int getInputs() {
		return inputs*2;
	}

	public boolean generateDataSet() {
		if(data == null || data.isEmpty()) {
			dataSet = null;
			return false;
		}
		dataSet = new DataSet(2*inputs+1, outputs);
		for(String[] dataBits: data) {
			double[] dataIn = new double[2*inputs + 1]; // +1 for the bias
			double[] dataOut = new double[outputs];
			int i = 0;
			for(String point: dataBits[0].split(",")) {
				dataIn[i++] = Double.parseDouble(point);
			}
			for(; i < inputs; ++i) {
				dataIn[i] = 0.0;
			}
			for(String point: dataBits[1].split(",")) {
				dataIn[i++] = Double.parseDouble(point);
			}
			for(; i < 2*inputs; ++i) {
				dataIn[i] = 0.0;
			}
			dataIn[2*inputs] = 1.0; // bias
			i = 0;
			for(String point: dataBits[2].split(",")) {
				dataOut[i++] = Double.parseDouble(point);
			}
			DataSetRow dataSetRow = new DataSetRow(dataIn, dataOut);
			dataSetRow.setLabel(dataBits[3]);
			dataSet.addRow(dataSetRow);
		}
		return true;
	}

	public DataSet getDataSet() {
		if(dataSet == null) {
			generateDataSet();
		}
		return dataSet;
	}
}
