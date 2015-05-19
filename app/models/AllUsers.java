package models;

import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import java.math.*;

import models.Update;
import models.User;
import models.SQL;

import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.SingularValueDecomposition;
import org.apache.mahout.math.SparseMatrix;


/** 
 * ALLUSERS OPERATIONS
 *
 *
 *getMoviesize()
 *  returns the size of the movies
 *
 *updateNow()
 *  Sets the updating boolean to true which means we are updating svd
 *
 *
 *movieParse(moviefile)
 *  Parses the movie file and stores the results in an array
 *
 *
 *findMovie(id)
 *  returns the movie name with the specified id
 *
 * 
 *getTenRandomIDS(baddummymovies)
 *get ten random movies that are not empty movie slots in the grouplens dataset(dummymovies)
 * for the first register page
 *
 *getTenRandomIDS(username, baddummymovies)
 *gets ten random movies that the user hasn't rated on previous registration pages
 * (on the load-more pages)
 *
 * 
 *getLastTen(username)
 *  gets the ten most recent movies a user has seen
 *
 * 
 *makeRandomList(movieIds)
 *  makes random movies for the registeration page
 * 
 *
 startsWithNumber(first)
 *  movie parsing method that ensures a line starts with a number
 *
 **/




public class AllUsers {

	private TreeMap<String, User> allusers;
	
	public static ArrayList<String> allmovies;
	public ArrayList<String> shortlist;
	public static boolean updating;
	public static SQL sql = new SQL();

	public AllUsers() {

		/*TreeMap<Username, Userdata>*/
		allusers = new TreeMap<String, User>();

		/*ArrayList<MovieTitles> index would be movieID*/
		allmovies = new ArrayList<String>();

		//V = readMatrix("conf/Vmatrix3users.txt");
		shortlist = new ArrayList<String>();
		updating = false;
	}

	public static int getMoviesize() {
		return allmovies.size();
	}

	public static void updateNow() {
		updating = true;
	}

	public static void updateFinish() {
		updating = false;
	}

	public void movieParse(File moviefile) throws IOException {

		String line,finalline;
		BufferedReader input = null;
		int count = 1;
		try {

			input = new BufferedReader(new FileReader(moviefile));

			while ((line = input.readLine()) != null) {
				if (!(line.isEmpty()) && startsWithNumber(line)) {
					StringBuilder outLine = new StringBuilder();
					StringBuilder countindex = new StringBuilder();
					int begin = line.indexOf("::")+ 2;
					countindex.append(line, 0, begin-2);
					int filecount = Integer.parseInt(countindex.toString());
					while(count < filecount) {
						allmovies.add("Dummy Movie part "+count + " (2015)");	
						count++;
					}
					int end = line.lastIndexOf("::");
					while(begin < end) {
						outLine.append(line.charAt(begin++));
					}
					//finalline =removeYears(outLine.toString());
					finalline =outLine.toString();
					allmovies.add(finalline);
					count++;
				}
			}


		} catch(FileNotFoundException e) {

			System.out.println("File Not Found");

		} catch (IOException e) {

			System.out.println("File is not readable");

		} finally {

			input.close();
		}
	}

	public String findMovie(int id) {
	    
		return allmovies.get(id-1);
		
	}

	public ArrayList<Integer> getTenRandomIDS(ArrayList baddummymovies) {
	    
		ArrayList<Integer> random = new ArrayList<Integer>();
		int i = 0;
		int randy;
		
		while (i<10) {
		    
			randy = (int)(Math.random() * allmovies.size()) + 1;
			
			if (!baddummymovies.contains(randy)) {
				random.add(randy);
				i++;
			}
		}
		
		makeRandomList(random);
		return random;
	}

	public ArrayList<Integer> getTenRandomIDS(String username, ArrayList baddummymovies) {
		ArrayList<Integer> random = new ArrayList<Integer>();
		while(random.size() != 10) {
			int randomMovie = ((int)(Math.random() * allmovies.size()) + 1);
			if(!sql.checkUser(randomMovie, username) && !baddummymovies.contains(randomMovie)) {
				random.add(randomMovie); 
			}

		}
		makeRandomList(random);
		return random;
	}

	public ArrayList<String> getLastTen(String username) {
		ArrayList<Integer> lasttenID = sql.tableGetLastTen(username);
		ArrayList<String> lastten = new ArrayList<String>();
		for(int i = 0; i < lasttenID.size(); i++) {
			lastten.add(findMovie(lasttenID.get(i)));
		}
		return lastten;
	}

	public void makeRandomList(ArrayList<Integer> movieIds) {
		for(int i = 0; i < movieIds.size(); i++) {
			shortlist.add(findMovie(movieIds.get(i)));
		}
	}


	private static boolean startsWithNumber(String first) {
		first = String.valueOf(first.charAt(0));

		first = first.replaceAll("[0-9]", "X");
		if (first.charAt(0) == 'X') {
			return true;
		}
		return false;

	}
}