package it.svm.iot;

/**
 * Class representing a product on a vending machine
 * @author Paolo Sassi
 * @author Matteo Rotundo
 *
 */

public class Product {
	private String name;
	private int qty;
	private float price;
	
	/**
	 * Constructor for class Product.
	 * @param n Name of the product
	 * @param q Initial quantity of the product
	 * @param p Initial price of the product
	 */
	public Product(String n, int q, float p) {
		name = n;
		qty = q;
		price = p;
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
		if (qty >= 0 && qty < Constants.MAX_PROD_QTY)
			this.qty = qty;
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
		if (price >= Constants.MIN_PRICE && price <= Constants.MAX_PRICE)
			this.price = price;
		else {
			System.err.printf("Product %s: Invalid price (%f).\n", name, price);
			System.exit(1);
		}
	}

}
