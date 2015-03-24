package models;
import java.util.ArrayList;
import java.util.TreeMap;
import models.User;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.*;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;




public class AllUsers {
    
    /*TreeMap<Username, Userdata>*/
    TreeMap<String, User> allusers = new TreeMap<String, User>();
    /*ArrayList<MovieTitles> index would be movieID*/
    ArrayList<String> allmovies = new ArrayList<String>();
    /*ArrayList<genres> index would be movieID*/
    ArrayList<String> allgenres = new ArrayList<String>();
    
    public ArrayList<String> shortlist = new ArrayList<String>();
    
    public int getSizeOfAll(){
        return allusers.size();
    }
    
    
    public void movieParse(File moviefile) throws IOException{
		String line;
		BufferedReader input = null;

		try {
			input = new BufferedReader(new FileReader(moviefile));

			while((line = input.readLine()) != null) {
				String[] wordArray = line.split("[|]+");
				allmovies.add(wordArray[1]);
			}
			
		} catch(FileNotFoundException e) {
			System.out.println("File Not Found");

		} catch (IOException e) {
			System.out.println("File is not readable");
			
		} finally {
			input.close();
			
		}
	}
	
	public void userParse(File userfile) throws IOException{
	    String line;
		BufferedReader input = null;

		try {
			input = new BufferedReader(new FileReader(userfile));

			while((line = input.readLine()) != null) {
				String[] wordArray = line.split("[\t]+");
				String user = wordArray[0];
				allusers.put(user, new User());
				
				for(int i = 1; i < wordArray.length; i++){
					
						String[] rating = wordArray[i].split("[,]");
						allusers.get(user).userdata.put(Integer.parseInt(rating[0]), Integer.parseInt(rating[1]));
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
    
    public void addToAll(String username, User user){
        
        
        allusers.put(username, user);  
        System.out.println(user.userdata.toString());
        
        
    }
    
    public boolean hasUser(String username){
        if(allusers.containsKey(username)){
            return true;
        }
        return false;
    }
    
    public User getUserData(String username){
        return allusers.get(username);
    }
    
    public String findGenre(int id){
        return allgenres.get(id);
    }
    //Change by Daniel
    public String findMovie(int id){
        return allmovies.get(id-1);
    }
    
    public ArrayList<Integer> getTenRandomIDS(){
        ArrayList<Integer> random = new ArrayList<Integer>();
        for(int i = 0; i < 10; i++)
        {
          random.add((int)(Math.random() * allmovies.size()) + 1);  
        }
        makeRandomList(random);
        return random;
    }
    
    public void makeRandomList(ArrayList<Integer> movieIds){
        for(int i = 0; i < movieIds.size(); i++){
            shortlist.add(findMovie(movieIds.get(i)));
        }
    }
    
   // Added by Daniel  
    double xyStandarddeviation(double meanx,
			double meany, int moviesize, int usersize, String user, int i) {
		double sumx = 0;
		double sumy = 0;
		for (int j = 0; j < moviesize; j++) {
			int x = CheckUserID(user,j);
			int y = CheckUserID(Integer.toString(i),j);
			double xx =x  - meanx;
			sumx += xx * xx;
			double yy = y - meany;
			sumy += yy * yy;

		}
		sumx = sumx / (moviesize - 2);
		sumy = sumy / (moviesize - 2);
		sumx = Math.sqrt(sumx);
		sumy = Math.sqrt(sumy);
		//System.out.println("sx for user "+id + "is " + sumx  + "sy for user "+i +"is "+sumy );
		return sumx * sumy;

	}	
	
	// Added by Daniel  
      public int CheckUserID(String userid, int movieindex){
    	if (allusers.get(userid).userdata.get(movieindex) == null){
			return 0;
		}
    	 return allusers.get(userid).userdata.get(movieindex) ;
    }
    
    // Added by Daniel  
    public void suggestMovies(String user, String recommender,
			ArrayList<Integer> suggest,int random) {

		int size = allmovies.size();
		//System.out.println(size);
		for (int i = 1; i <= size; i++) {
			if (random >=2 && allusers.get(recommender).userdata.containsKey(i)
				//	&& auserIndex.movieIndex.get(id).get(i) == 0
					&& !allusers.get(user).userdata.containsKey(i)
					&& !suggest.contains(i)) {
				if (allusers.get(recommender).userdata.get(i) == 5){
				suggest.add(i);
				return;
				}
			}
		}for (int i = 1; i <= size; i++) {
			if (random >=2 && allusers.get(recommender).userdata.containsKey(i)
					//	&& auserIndex.movieIndex.get(id).get(i) == 0
						&& !allusers.get(user).userdata.containsKey(i)
						&& !suggest.contains(i)) {
					if (allusers.get(recommender).userdata.get(i) >3){
					suggest.add(i);
					return;
					}
				}
		}
	}

    
    
    
    // Added by Daniel  
    public void updateMovies(HashMap<String, MovieObject> pearsonmap,ArrayList<Integer> movies, String user){
    	 ArrayList<MovieObject> finalResults = new ArrayList<MovieObject>(
 				pearsonmap.values());
 		Collections.sort(finalResults);
        int t = 10;
        //System.out.println("Index 0 is " + finalResults.get(0).getID());
		for (int j = 1; j <= t; j++) {
			//Random rand = new Random();
			//int random = rand.nextInt(4);//incase we implement backward and forward traversal
			//random = 2;// debugging
			
			MovieObject i = finalResults.get(j);
	        suggestMovies(user, i.getID(), movies,2);
	        System.out.println(movies);
	        //Logger.debug("message: %s, movies);
		}

 	
    }
    
    // Added by Daniel  //implementing Apache
    public void checkForSimUsers(String user, ArrayList<Integer> movies){

    	HashMap<String, MovieObject> pearsonmap = new HashMap<String, MovieObject>();
		TreeMap<Integer, Integer> userMap = allusers.get(user).userdata;
		//System.out.println(userMap);
	    int usersize = allusers.size(); //neww
	    int moviesize = allmovies.size(); //neww
	    for (int i = 1; i <= usersize; i++) { //neww
			double sumofx = 0;
			double sumofy = 0;
			double xy = 0;
			for (int j = 0; j < moviesize; j++) {
				int x = CheckUserID(user,j);
				int y = CheckUserID(Integer.toString(i),j);
				sumofx += x;
				sumofy += y;
				xy += (x * y);
			}
			double meanx = sumofx / (moviesize - 1);
			double meany = sumofy / (moviesize - 1);
			double sd = xyStandarddeviation(meanx, meany,
					moviesize, usersize, user, i);
			double meanxy = meanx * meany;
			meanxy = (moviesize - 1) * meanxy;
			xy = xy - meanxy;
			xy = xy / (moviesize - 2);
			double pcc = xy / sd;
			pearsonmap.put(Integer.toString(i),
					new MovieObject(Integer.toString(i),pcc));	
		}
	     updateMovies(pearsonmap,movies, user);
	     
	     /*	try{
		 DataModel model = new FileDataModel(new File("data/dataset.csv")); //should be changed after every user submission
		UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
		UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
		UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
		//GenericUserBasedRecommender recommender = new GenericUserBasedRecommender(model,similarity);
		int ouruser = Integer.parseInt(user);
		List<RecommendedItem> recommendations = recommender.recommend(ouruser, 10);
		
		//long[] users = recommender.mostSimilarUserIDs(user, 5);
		for (RecommendedItem recommendation : recommendations) {
		  System.out.println(recommendation);
		  movies.add((int)recommendation.getItemID());
		  
		}
		}
		catch (IOException | TasteException e) {
			// TODO Auto-generated catch block
			System.out.println("I/O or Taste Exception");
		}
	     */
	     
    }
	

	private boolean checkList (ArrayList<String> userList, String user ) {
		
		for(int i = 0; i < userList.size(); i++) {
			
			if(userList.get(i).equals(user)) {
				
				return false;
			}
		}
		
		return true;
	}
    
    
}