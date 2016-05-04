package GestureRecognizer;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.util.TransferFunctionType;



public class Project {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestData testData = new TestData();
		int inputs = testData.getInputs();
		TestData falseData = new TestData(new File("convertedData2"), inputs/2);
		TestData altData = new TestData(new File("convertedData1"), inputs/2);
		System.err.println("# of inputs: " + inputs);
		List<Integer> netLayers = new LinkedList<Integer>();
		netLayers.add(inputs+1);
		netLayers.add(5);
		netLayers.add(2);
		System.err.println("Creating neural network...");
		MultiLayerPerceptron nnet = 
				new MultiLayerPerceptron(netLayers, TransferFunctionType.TANH);
		System.err.println("Neural Network created...");
//		for(Neuron output: nnet.getLayerAt(2).getNeurons()) {
//			output.setTransferFunction(new Sgn());
//		}
		System.err.println("Creating data set...");
		DataSet dataSet = testData.getDataSet();
		DataSet falseDataSet = falseData.getDataSet();
		DataSet altDataSet = altData.getDataSet();
		System.err.println("Saving data set...");
		dataSet.save("./fullDataSet.dat");
		System.err.println("Data set saved as fullDataSet.dat");
		DataSet[] sets = dataSet.createTrainingAndTestSubsets(80, 20);
		System.err.println("Beginning training...");
		nnet.learn(sets[0]);
		// Testing
		testNetwork(nnet, sets[1]);
		// Test on alternative data
		testNetwork(nnet, altDataSet);
		// Test on negative Data
		testNetwork(nnet, falseDataSet);
	}
	
	private static int misClassified = 0;
	private static int partialClassified = 0;
	private static double totalError = 0;
	private static String[] classifications = {"correct", "partially", "incorrect" };
	private static String[] labels = new String[3];
	private static int[][] counts = new int[3][3];

	private static void testNetwork(NeuralNetwork nnet, DataSet dataSet) {
		
		misClassified = 0;
		partialClassified = 0;
		totalError = 0;
		for(DataSetRow instance: dataSet.getRows()) {
			String label = instance.getLabel();
			nnet.setInput(instance.getInput());
			nnet.calculate();
			double[] actual = nnet.getOutput();
			double[] expected = instance.getDesiredOutput();
			double[] result = new double[2];
			int error = 0;
			int category = 2;
			int type = 0;
			for(int i = 0; i < 2; i++) {
				result[i] = actual[i] * expected[i];
				if(result[i] < .7) { 
					++error;
					totalError += Math.pow(.7-result[i], 2);
				}
				if(((int)expected[i]) == 1) {
					category = i;
				}
			}
			labels[category] = label.split("-")[1];
			type = error;
			counts[category][type]++;
			switch(error) {
			case 2: ++misClassified; break;
			case 1: ++partialClassified; break;
			}
			System.out.println(label + ": " + classifications[error] + "(" + result[0] + ", " + result[1] + ")");
		}
		
		System.out.println(" = = = = = = = = = = = = = = = = = = ");
		System.out.println(" =             Summary             = ");
		System.out.println(" = = = = = = = = = = = = = = = = = = ");
		int totalSets = dataSet.size();
		int correct = totalSets - misClassified - partialClassified;
		System.out.println("Correctly classified: " + correct);
		System.out.println("Partially classified: " + partialClassified);
		System.out.println("Misclassified:        " + misClassified);
		System.out.println(" - - - - - - - - - - - - - - - - - - ");
		System.out.println(labels[0] + ": " + Arrays.toString(counts[0]));
		System.out.println(labels[1] + ": " + Arrays.toString(counts[1]));
		System.out.println(labels[2] + ": " + Arrays.toString(counts[2]));
		System.out.println(" - - - - - - - - - - - - - - - - - - ");
		System.out.println("Mean Squared Error: " + (totalError/totalSets));
	}

}
