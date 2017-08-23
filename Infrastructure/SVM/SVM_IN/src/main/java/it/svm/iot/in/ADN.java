package it.svm.iot.in;

import java.util.ArrayList;
import java.util.Scanner;
import it.svm.iot.core.*;


/**
 * ADN for the SVM Infrastructure Node.
 * @author Paolo Sassi
 * @author Matteo Rotundo
 *
 */
public class ADN {
	
	/**
	 * Mca reference point for the IN.
	 */
	private static Mca IN_Mca = Mca.getInstance();
	
	/**
	 * Application Entity of the MN.
	 */
	private static AE IN_AE;
	
	/**
	 * List containing the registered containers.
	 */
	private static ArrayList<Container> containers = 
			new ArrayList<Container>();
	
	/**
	 * Discover all the useful resources on the MN.
	 * @param mn_cse URI of the MN
	 */
	private static void discover(String mn_cse) {
		String vm_cont_raw, res_cont_raw;
		String parent_cont = "";
		String[] vm_cont, res_cont, tmp;
		int i = 0, vm_pos = 0;
	
		/* Discover the VM containers on the MN */
		vm_cont_raw = IN_Mca.discoverResources(mn_cse, "?fu=1&rty=3&st=0");
		vm_cont = vm_cont_raw.split(" ");
		
		for (String vm : vm_cont) {
			
			tmp = vm.split("/");
			if (i == vm_pos) {
				/* Create the container for the VM */
				parent_cont = Constants.IN_CSE_URI + "/" + 
						IN_AE.getRn() + "/" + tmp[tmp.length - 1];
				containers.add(IN_Mca.createContainer(Constants.IN_CSE_URI + "/" + 
						IN_AE.getRn(), tmp[tmp.length - 1]));
				vm_pos += (Constants.NUM_RESOURCES + 1);
			} else {
				/* Create the container for the resource */
				containers.add(IN_Mca.createContainer(parent_cont,
						tmp[tmp.length - 1]));
			}
			i++;
		}
		
	}
	
	/**
	 * Private constructor for the ADN class.
	 */
	private ADN() {}

	/**
	 * Main method for the ADN on the IN side
	 * @param args Arguments for the ADN
	 */
	public static void main(String[] args) {
		Scanner keyboard = new Scanner(System.in);
		Boolean exit = false;
		String input;
		System.out.printf("********** Infrastructure Node ADN **********\n");
		IN_AE = IN_Mca.createAE(Constants.IN_CSE_URI, "SVM_Monitor");
		System.out.printf("AE registered on IN-CSE\n");
		
		discover(Constants.MN_CSE_SHORT_URI);
		
		System.out.println("Enter 'q' to quit");
		while(!exit) {
			/* Busy wait */
			input = keyboard.nextLine();
			if (input != null && input.equals("q"))
				exit = true;
		}
		keyboard.close();
	}
}
