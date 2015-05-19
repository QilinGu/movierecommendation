package models;



/**
 * This class creates a Movie Object which stores the movie and the svdvalue
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
	private double svdvalue;

	/**
	 * This constructor initializes the Movie Object
	 * 
	 */
	public Movie(int id,double svdvalue) {
		this.movieid = id;
		this.svdvalue = svdvalue;
	}

	/**
	 */
	public double getsvdvalue() {
		return this.svdvalue;
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

	public void setsvdvalue(double newsvdvalue) {
		this.svdvalue = newsvdvalue;
	}


	@Override
	public String toString() {

		return "Movie: " + movieid + " has a value of " + svdvalue;
	}



	public int compareTo(Movie arg0) {
		if (this.svdvalue > arg0.svdvalue)
			return -1;
		else if (this.svdvalue == arg0.svdvalue)
			return lastCompareTo(arg0);
		else
			return 1;
	}

	/**
	 * This method is used to compare different Movie Objects by their
	 * movieids
	 * 
	 * @param arg0
	 *            the other Movie Object being compared with
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