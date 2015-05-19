package models;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Map.Entry;


import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.SingularValueDecomposition;
import org.apache.mahout.math.SparseMatrix;

import models.Algorithms;
import models.AllUsers;

import controllers.Javatar;

import java.io.File;
import java.io.IOException;


public class Update extends TimerTask {
    
    
  public void run() {

    try {
        AllUsers.updateNow();
	    int count = Algorithms.grouplensUsers;
		ArrayList<String> users = AllUsers.sql.loginGetUsers();
		int usercount = Algorithms.grouplensUsers + users.size();
		DenseMatrix M = Algorithms.readM("conf/M.txt",usercount, AllUsers.getMoviesize());
		System.out.println("Grouplens DONE");
		for (String user: users){
			TreeMap<Integer, Integer> userMap = AllUsers.sql.tableGetMap(user);
			for (Entry<Integer, Integer> t: userMap.entrySet()){
				M.setQuick(count,t.getKey()-1,t.getValue());
			}
			count++;
		}
		System.out.println("Calculating SVD");
		long start_time = System.currentTimeMillis();
		SingularValueDecomposition t = new SingularValueDecomposition(M);
		System.out.println("SVD Done");
		long end_time = System.currentTimeMillis();
		long time = end_time-start_time;
		time = time/1000;
		System.out.println("The time of SVD in seconds is " + time);

		Algorithms.writeMatrix(t.getV(),"conf/VmatrixMillion6040full.txt","This is a result of SVD recalculation");
		Matrix newV = Algorithms.reduceMatrixV("conf/VmatrixMillion6040full.txt",20);
		//we need to delete the matrix because it is very large and useless at this point
		File f = new File("conf/VmatrixMillion6040full.txt");
		System.out.println("Was the file deleted? " + f.delete());//we need to delete the matrix because it is very large and useless at this point
		Algorithms.writeMatrix(newV,"conf/VMatrixMillion6040reduced.txt","(SVD Recalculation)This the reduced matrix of the original centered Million ratings");
		Algorithms.setV(Algorithms.readMatrix("conf/VMatrixMillion6040reduced.txt"));
		System.out.println("SVD UPDATE IS COMPLETE");
	    AllUsers.updateFinish();
	    
    } catch (IOException e) {
        //do nothing
    }
  }

}