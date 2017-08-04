package it.svm.iot;


public class ADN {
	
	public static void main(String[] args) {
		Mca MN_Mca = Mca.getInstance();
		AE ae = MN_Mca.createAE("coap://127.0.0.1:5683/~/svm-mn-cse", "SVM_Monitor");
		Container container = MN_Mca.createContainer("coap://127.0.0.1:5683/~/svm-mn-cse/svm-mn-name/SVM_Monitor", "DATA");
		MN_Mca.createContentInstance("coap://127.0.0.1:5683/~/svm-mn-cse/svm-mn-name/SVM_Monitor/DATA", "ciao");

	}

}
