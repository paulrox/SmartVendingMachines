package it.svm.iot.mn;

import it.svm.iot.core.*;

/**
 * ADN for the SVM Middle Node
 * @author Paolo Sassi
 * @author Matteo Rotundo
 *
 */
public class ADN {

	private static ADN instance = null;
	/**
	 * Mca reference point for the MN
	 */
	public static Mca MN_Mca;
	/**
	 * Application Entity of the MN
	 */
	public static AE MN_AE;
	
	/**
	 * Private constructor for the ADN class (singleton)
	 */
	private ADN() {
		MN_Mca = Mca.getInstance();	/* Get the Mca instance */
		AE MN_AE = MN_Mca.createAE(Constants.MN_CSE_URI, "SVM_Monitor");
		
	}
	
	public static ADN getInstance() {
		if (instance == null) {
			System.out.printf("Instanciating Middle Node ADN...");
			instance = new ADN();
			System.out.printf("OK\n");
		}
		return instance;
	}
	
	public static void main(String[] args) {
		
		ADN MN_ADN = ADN.getInstance();
		Container container = MN_Mca.createContainer("coap://127.0.0.1:5683/~/svm-mn-cse/svm-mn-name/SVM_Monitor", "DATA");
		MN_Mca.createContentInstance("coap://127.0.0.1:5683/~/svm-mn-cse/svm-mn-name/SVM_Monitor/DATA", "ciao");

	}

}
