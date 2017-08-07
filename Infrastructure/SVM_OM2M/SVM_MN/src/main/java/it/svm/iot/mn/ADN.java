package it.svm.iot.mn;

import java.util.ArrayList;
import it.svm.iot.core.*;

/**
 * ADN for the SVM Middle Node
 * @author Paolo Sassi
 * @author Matteo Rotundo
 *
 */
public class ADN {

	/**
	 * Mca reference point for the MN.
	 */
	private static Mca MN_Mca = Mca.getInstance();
	/**
	 * Application Entity of the MN.
	 */
	private static AE MN_AE = MN_Mca.createAE(Constants.MN_CSE_URI,
			"SVM_Monitor");
	/**
	 * List containing the registered containers.
	 */
	private static ArrayList<Container> containers = 
			new ArrayList<Container>();
	
	/**
	 * List of mote addresses.
	 */
	private static ArrayList<String> mote_addr = new ArrayList<String>();
	
	/**
	 * Private constructor for the ADN class.
	 */
	private ADN() {}
	
	private static void getMoteAddresses(String br_addr) {
	
	private static void registerResources(String br_addr) {
		
	}
	
	public static void main(String[] args) {
		System.out.printf("********** Middle Node ADN **********\n");
		System.out.printf("AE registered on MN-CSE\n");
		Container container = MN_Mca.createContainer("coap://127.0.0.1:5683/~/svm-mn-cse/svm-mn-name/SVM_Monitor", "DATA");
		MN_Mca.createContentInstance("coap://127.0.0.1:5683/~/svm-mn-cse/svm-mn-name/SVM_Monitor/DATA", "ciao");
		
		System.out.printf("OK\n");
	}

}
