package models;

import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.SingularValueDecomposition;
import org.apache.mahout.math.SparseMatrix;

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

import java.io.File;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;

import java.math.*;

import models.AllUsers;
import models.SQL;


public class Algorithms { 

	public static Matrix V;
	public static AllUsers aU;
	public static int moviesize;
	public static int vector;
	public static int grouplensUsers;
	public Algorithms(AllUsers allusers) {
		aU = allusers;
		V = readMatrix("conf/VMatrixMillion6040reduced.txt");
		vector = 1;
		grouplensUsers = 6040;
	}


	public static void setV(Matrix newV){
		V = newV;
	}

	public void recommendMovies(String user, ArrayList<Integer> movies,ArrayList baddummymovies){

		HashMap<String, MovieObject> pearsonmap = new HashMap<String, MovieObject>();
		TreeMap<Integer, Integer> userMap = aU.sql.tableGetMap(user);
		DenseMatrix q = new DenseMatrix(vector,aU.getMoviesize());
		for (Entry<Integer, Integer> t: userMap.entrySet()){
			q.setQuick(0,t.getKey()-1,t.getValue());
		}
		// int usersize = aU.allusers.size(); //neww
		// int moviesize = aU.allmovies.size(); //neww
		Matrix  qV = q.times(V);
		Matrix qVbyVT = qV.times(V.transpose());
		int columns = qVbyVT.columnSize();
		HashMap<Integer, Movie> map = new HashMap<Integer, Movie>();

		for(int j = 0; j<columns; j++){
			map.put(j, new Movie(j, qVbyVT.get(0, j)));
		}

		ArrayList<Movie> finalsvd = new ArrayList<Movie>(map.values());
		Collections.sort(finalsvd);
		int count = 0;
		int j = 0;
		while (count < 10){
			if (!userMap.containsKey(finalsvd.get(j).getID()+1) && !baddummymovies.contains(finalsvd.get(j).getID()+1)){
				movies.add(finalsvd.get(j).getID()+1);
				count++;
			}
			j++;
		}
	}        

	public static void writeMatrix(Matrix inputM, String filename, String title) throws IOException
	{
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, false)));

		int rows = inputM.numRows();
		int columns = inputM.numCols();
		double value;
		out.println(title);
		out.println(rows);
		out.println(columns);
		for (int i = 0; i<rows; i++){
			for (int j = 0; j < columns; j++){
				if ((value = inputM.getQuick(i, j)) !=0){
					out.println(i + "," + j + "," +value );
				}
			}
		}
		out.close();
	}

	public static Matrix readMatrix(String filename){
		String line;
		int rows;
		int columns;
		DenseMatrix newMatrix = null;

		try {
			BufferedReader input = null;

			try {
				input = new BufferedReader(new FileReader(filename));
				input.readLine();//done necessarily and intentionally to skip the one line documentation
				rows = Integer.parseInt(input.readLine());
				columns = Integer.parseInt(input.readLine());
				newMatrix = new DenseMatrix(rows,columns);
				while ((line = input.readLine()) != null) {
					if (!(line.isEmpty())) {
						String lineArray[] = line.split(",");
						if (lineArray.length == 3){
							newMatrix.set(Integer.parseInt(lineArray[0]), Integer.parseInt(lineArray[1]), Double.parseDouble(lineArray[2]));
						}
					}
				}
			}
			finally {

				input.close();

			}
		} catch (FileNotFoundException e) {
			System.out.println("Directory is invalid/Directory does not consist a file");
		} catch (IOException e) {
			System.out.println("Input/Output Exception");
		}
		catch(NullPointerException e){
			System.out.println("An Error occured");
		}
		return newMatrix;


	}

	public static ArrayList readArrayList(String filename){
		ArrayList<Integer> list = new ArrayList<Integer>();
		String line;


		try {
			BufferedReader input = null;

			try {
				input = new BufferedReader(new FileReader(filename));
				while ((line = input.readLine()) != null) {
					if (!(line.isEmpty())) {

						list.add(Integer.parseInt(line));
					}
				}
			}
			finally {

				input.close();

			}
		} catch (FileNotFoundException e) {
			System.out.println("Directory is invalid/Directory does not consist a file");
		} catch (IOException e) {
			System.out.println("Input/Output Exception");
		}
		catch(NullPointerException e){
			System.out.println("An Error occured");
		}


		return list;
	}

	public static DenseMatrix readM(String filename, int newrow,int newcolumn) {
		BufferedReader reader = null;
		DenseMatrix m = null;
		try {
			reader = new BufferedReader(new FileReader(filename));
			String line;
			try {
				int row = Integer.parseInt(reader.readLine());
				int column = Integer.parseInt(reader.readLine());
				m = new DenseMatrix(newrow, newcolumn);
				line = reader.readLine();
				int i = 0;
				while (line != null && i < newrow) {// don't forget to change this after debugging
					String[] numbers = line.split(",");
				for (int j = 0; j < numbers.length; j++) {
					m.setQuick(i, j, Double.parseDouble(numbers[j]));
				}

				line = reader.readLine();
				i++;
				}
			} catch (IOException e) {
				//ignored
			}
		} catch (FileNotFoundException e) {
			//ignored
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}   
		return m;
	}

	public static Matrix reduceMatrixV(String filename,double percentkept){
		String line;
		int rows;
		int columns;
		DenseMatrix newMatrix = null;
		try {
			BufferedReader input = null;

			try {
				input = new BufferedReader(new FileReader(filename));
				input.readLine();//done necessarily and intentionally to skip the one line documentation
				rows = Integer.parseInt(input.readLine());
				columns = Integer.parseInt(input.readLine());
				int newcolumns = (int) ((percentkept/100) * columns); 
				newMatrix = new DenseMatrix(rows,newcolumns);
				while ((line = input.readLine()) != null) {
					if (!(line.isEmpty())) {
						String lineArray[] = line.split(",");
						if (lineArray.length == 3 && Integer.parseInt(lineArray[1]) < newcolumns){
							newMatrix.set(Integer.parseInt(lineArray[0]), Integer.parseInt(lineArray[1]), Double.parseDouble(lineArray[2]));
						}
					}
				}
			}
			finally {

				input.close();

			}
		} catch (FileNotFoundException e) {
			System.out.println("Directory is invalid/Directory does not consist a file");
		} catch (IOException e) {
			System.out.println("Input/Output Exception");
		}
		catch(NullPointerException e){
			System.out.println("An Error occured");
		}

		return newMatrix;
	}


	public void updateSVD()throws IOException{

        Timer timer = new Timer();
		Calendar date = Calendar.getInstance();
		date.set(
				Calendar.DAY_OF_WEEK,
				Calendar.TUESDAY
				);
		date.set(Calendar.HOUR, 7);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		System.out.println(date.getTime());

        timer.schedule(
				new Update(),
				date.getTime(),
				1000 * 60 * 60 * 24 * 7
				);
	}
    
    /**
     * For Testing Purposes Only, Updates Small SVD File.
     * */
	public void updateSVDsmall()throws IOException{ 

		Timer timer = new Timer();
		Calendar date = Calendar.getInstance();
		date.set(
				Calendar.DAY_OF_WEEK,
				Calendar.MONDAY
				);
		date.set(Calendar.HOUR, 10);
		date.set(Calendar.MINUTE, 10);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		System.out.println(date.getTime());

		timer.schedule(
				new Update(),
				date.getTime(),
				1000 * 60 * 5
				);
	}

}
