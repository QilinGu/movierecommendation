package models;



/**
 * This class creates a MovieObject which stores the user ID and the dotproduct
 * 
 * @author Daniel Obaseyi Buraimo
 * 
 */
public class Movie implements Comparable<Movie> {
	/**
	 * This is the id of the user
	 */
	private final int movieid;
	/**
	 * This is the dot product
	 */
	private double dotproduct;

	/**
	 * This constructor initializes the MovieObject
	 * 
	 */
	public Movie(int id,double product) {
		this.movieid = id;
		this.dotproduct = product;
	}

	/**
	 */
	public double getProduct() {
		return this.dotproduct;
	}
	
	/**
	 */
	public int getID() {
		return this.movieid;
	}

	/**
	 * Changes the initial position of a particular SearchResultObject
	 * 
	 * @param newPosition
	 *            the new initial position
	 */

	public void setProduct(double newProduct) {
		this.dotproduct = newProduct;
	}


	/**
	 * This method overrides the Object's toString method. It creates a format
	 * in which this SearchResultObject can be easily read. Either into the
	 * console or a file.
	 * 
	 * @return the format of the output of this SearchResultObject
	 */
	@Override
	public String toString() {

		return "Movie: " + movieid + " has a value of " + dotproduct;
	}



	public int compareTo(Movie arg0) {
		if (this.dotproduct > arg0.dotproduct)
			return -1;
		else if (this.dotproduct == arg0.dotproduct)
			return lastCompareTo(arg0);
		else
			return 1;
	}

	/**
	 * This method is used to compare different SerachResultObjects by their
	 * filename
	 * 
	 * @param arg0
	 *            the other SearchResultObject being compared with
	 * 
	 * @return the value of the comparisons
	 */
	public int lastCompareTo(Movie arg0) {
		if (this.movieid > arg0.movieid)
			return -1;
		else if (this.movieid == arg0.movieid)
			return 0;
		else
			return 1;
	}


}