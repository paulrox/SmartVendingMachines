package it.svm.iot.core;

/**
 * Class representing a product on a vending machine
 * @author Paolo Sassi
 * @author Matteo Rotundo
 *
 */

public class Product {
	public String name;
	public int qty;
	public float price;
	public Boolean is_new_price;
	public Boolean is_new_qty;
	/* Reference to the vm class*/
	public VendingMachine vm;
	
	/**
	 * Constructor for class Product.
	 * @param n Name of the product
	 */
	public Product(String n, VendingMachine vm) {
		name = n;
		is_new_price = false;
		is_new_qty = false;
		this.vm = vm;
	}
	
	/* Getter methods */
	
	/**
	 * Get the product name.
	 * @return Product name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Get the remaining amount of product.
	 * @return Quantity
	 */
	public int getQty() {
		return qty;
	}
	
	/**
	 * Get the actual product price.
	 * @return Product price
	 */
	public float getPrice() {
		return price;
	}
	
	/* Setter methods */
	
	/**
	 * Set the remaining amount of product.
	 * @param qty Product quantity
	 */
	public void setQty(int qty) {
		if (qty >= 0 && qty < Constants.MAX_PROD_QTY) {
			this.qty = qty;
			this.is_new_qty = true;
			this.vm.is_new = true;
		}
		else {
			System.err.printf("Product %s: Invalid product quantity (%f).\n", name, qty);
			System.exit(1);
		}
	}
	
	/**
	 * Set the actual product price.
	 * @param price Product price
	 */
	public void setPrice(float price) {
		if (price >= Constants.MIN_PRICE && price <= Constants.MAX_PRICE) {
			this.price = price;
			this.is_new_price = true;
			this.vm.is_new = true;
		}
		else {
			System.err.printf("Product %s: Invalid price (%f).\n", name, price);
			System.exit(1);
		}
	}

}
