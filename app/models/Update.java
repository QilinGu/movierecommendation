package models;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.SingularValueDecomposition;
import org.apache.mahout.math.SparseMatrix;
import models.AllUsers;
import java.io.File;
import java.io.IOException;


public class Update extends TimerTask {

  public void run() {
    try {
        DenseMatrix M = AllUsers.readM("conf/M.txt",30);
    
        System.out.println("SMALL Grouplens DONE");
    
        System.out.println("Calculating SVD");
	
	    long start_time = System.currentTimeMillis();
        SingularValueDecomposition t = new SingularValueDecomposition(M);
	
	    System.out.println("SVD Done");
	
	    long end_time = System.currentTimeMillis();
	    long time = end_time-start_time;
	    time = time/1000;

        System.out.println("The time of SVD in seconds is " + time);
	
	    AllUsers.writeMatrix(t.getV(),"conf/Vmatrix3usersfull.txt","This is a result of SVD 3 recalculation");
        Matrix newV = AllUsers.reduceMatrixV("conf/Vmatrix3usersfull.txt",100);
        File f = new File("conf/Vmatrix3usersfull.txt");
	
	    System.out.println("Was the file deleted? " + f.delete());//we need to delete the matrix because it is very large and useless at this point
	
	    AllUsers.writeMatrix(newV,"conf/Vmatrix3users.txt","(SVD Recalculation)This the reduced matrix of the original centered Million ratings");
        AllUsers.setV(AllUsers.readMatrix("conf/Vmatrix3users.txt"));	
	
	    System.out.println("SVD UPDATE IS COMPLETE"); 
	    
    } catch (IOException e){
        //do nothing
    }
  }

}