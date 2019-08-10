package svrp;

public class Common 
{
	public static String PATH_HOME="E:/research/zxy/experiments";
	
	public static String PATH_JC=PATH_HOME+"/preprocessed/JC.txt";
	public static String PATH_JK=PATH_HOME+"/preprocessed/JK.txt";
	
	public static String PATH_ALL_ADDRESS=PATH_HOME+"/preprocessed/all-address.txt";
	public static String PATH_ALL_ADDRESS_JC=PATH_HOME+"/preprocessed/all-address-jc.txt";
	public static String PATH_ALL_ADDRESS_JK=PATH_HOME+"/preprocessed/all-address-jk.txt";
	
	public static String PATH_ALL_STATIONS=PATH_HOME+"/preprocessed/all-stations.txt";
	public static String PATH_ALL_STATIONS_JC=PATH_HOME+"/preprocessed/all-stations-jc.txt";
	public static String PATH_ALL_STATIONS_JK=PATH_HOME+"/preprocessed/all-stations-jk.txt";
	
	public static String PATH_OLD_STATIONS=PATH_HOME+"/preprocessed/old-stations.txt";
	public static String PATH_OLD_STATIONS_JC=PATH_HOME+"/preprocessed/old-stations-jc.txt";
	public static String PATH_OLD_STATIONS_JK=PATH_HOME+"/preprocessed/old-stations-jk.txt";
	
	public static String PATH_METRO_STATIONS=PATH_HOME+"/preprocessed/metro-stations.txt";
	
	public static String PATH_ALL_STATIONS_NO_METRO=PATH_HOME+"/preprocessed/all-stations-no-metro.txt";
	public static String PATH_ALL_STATIONS_NO_METRO_JC=PATH_HOME+"/preprocessed/all-stations-no-metro-jc.txt";
	public static String PATH_ALL_STATIONS_NO_METRO_JK=PATH_HOME+"/preprocessed/all-stations-no-metro-jk.txt";
	
	public static String PATH_MATRIX_DRIVING=PATH_HOME+"/preprocessed/matrix-driving.txt";
	public static String PATH_MATRIX_DRIVING_JC=PATH_HOME+"/preprocessed/matrix-driving-jc.txt";
	public static String PATH_MATRIX_DRIVING_JK=PATH_HOME+"/preprocessed/matrix-driving-jk.txt";
	
	public static String PATH_MATRIX_WALKING=PATH_HOME+"/preprocessed/matrix-walking.txt";
	public static String PATH_MATRIX_WALKING_JC=PATH_HOME+"/preprocessed/matrix-walking-jc.txt";
	public static String PATH_MATRIX_WALKING_JK=PATH_HOME+"/preprocessed/matrix-walking-jk.txt";
	
	public static String PATH_MATRIX_DRIVING_NO_METRO=PATH_HOME+"/preprocessed/matrix-driving-no-metro.txt";
	public static String PATH_MATRIX_DRIVING_NO_METRO_JC=PATH_HOME+"/preprocessed/matrix-driving-no-metro-jc.txt";
	public static String PATH_MATRIX_DRIVING_NO_METRO_JK=PATH_HOME+"/preprocessed/matrix-driving-no-metro-jk.txt";
	
	public static String PATH_MATRIX_WALKING_NO_METRO=PATH_HOME+"/preprocessed/matrix-walking-no-metro.txt";
	public static String PATH_MATRIX_WALKING_NO_METRO_JC=PATH_HOME+"/preprocessed/matrix-walking-no-metro-jc.txt";
	public static String PATH_MATRIX_WALKING_NO_METRO_JK=PATH_HOME+"/preprocessed/matrix-walking-no-metro-jk.txt";
	
	public static String PATH_CLUSTER=PATH_HOME+"/preprocessed/cluster.txt";
	public static String PATH_CLUSTER_JC=PATH_HOME+"/preprocessed/cluster-jc.txt";
	public static String PATH_CLUSTER_JK=PATH_HOME+"/preprocessed/cluster-jk.txt";
	
	public static String PATH_CLUSTER_NO_METRO=PATH_HOME+"/results/cluster-no-metro.txt";
	public static String PATH_CLUSTER_NO_METRO_JC=PATH_HOME+"/results/cluster-no-metro-jc.txt";
	public static String PATH_CLUSTER_NO_METRO_JK=PATH_HOME+"/results/cluster-no-metro-jk.txt";
	
	public static String PATH_ORIGINAL_ROUTINES=PATH_HOME+"/preprocessed/original-routines.txt";
	public static String PATH_ORIGINAL_ROUTINES_JC=PATH_HOME+"/preprocessed/original-routines-jc.txt";
	public static String PATH_ORIGINAL_ROUTINES_JK=PATH_HOME+"/preprocessed/original-routines-jk.txt";
	
	public static String PATH_UNREACHABLE_DRIVING=PATH_HOME+"/preprocessed/unreachable-driving.txt";
	public static String PATH_UNREACHABLE_WALKING=PATH_HOME+"/preprocessed/unreachable-walking.txt";
	
	public static String [] AKS = {"zNgfhmzqjS7LqPtwk46P9jOfDi4UVDuE", 
										 "BAOHOHwBwvLVj3i6TLfNihGkKnGnX7jR", 
										 "sP9mZurYaMFK9YixwRbdt4TZhR3KniBD"};
	
	public static void setCommon(String type)
	{
		if(type.equals("ALL"))
			;
		else if(type.equals("JC"))
		{
			PATH_ALL_ADDRESS=PATH_ALL_ADDRESS_JC;
			PATH_ALL_STATIONS=PATH_ALL_STATIONS_JC;
			PATH_OLD_STATIONS=PATH_OLD_STATIONS_JC;
			PATH_ALL_STATIONS_NO_METRO=PATH_ALL_STATIONS_NO_METRO_JC;
			PATH_MATRIX_DRIVING=PATH_MATRIX_DRIVING_JC;
			PATH_MATRIX_WALKING=PATH_MATRIX_WALKING_JC;
			PATH_MATRIX_DRIVING_NO_METRO=PATH_MATRIX_DRIVING_NO_METRO_JC;
			PATH_MATRIX_WALKING_NO_METRO=PATH_MATRIX_WALKING_NO_METRO_JC;
			PATH_CLUSTER=PATH_CLUSTER_JC;
			PATH_CLUSTER_NO_METRO=PATH_CLUSTER_NO_METRO_JC;
			PATH_ORIGINAL_ROUTINES=PATH_ORIGINAL_ROUTINES_JC;
		}
		else if(type.equals("JK"))
		{
			PATH_ALL_ADDRESS=PATH_ALL_ADDRESS_JK;
			PATH_ALL_STATIONS=PATH_ALL_STATIONS_JK;
			PATH_OLD_STATIONS=PATH_OLD_STATIONS_JK;
			PATH_ALL_STATIONS_NO_METRO=PATH_ALL_STATIONS_NO_METRO_JK;
			PATH_MATRIX_DRIVING=PATH_MATRIX_DRIVING_JK;
			PATH_MATRIX_WALKING=PATH_MATRIX_WALKING_JK;
			PATH_MATRIX_DRIVING_NO_METRO=PATH_MATRIX_DRIVING_NO_METRO_JK;
			PATH_MATRIX_WALKING_NO_METRO=PATH_MATRIX_WALKING_NO_METRO_JK;
			PATH_CLUSTER=PATH_CLUSTER_JK;
			PATH_CLUSTER_NO_METRO=PATH_CLUSTER_NO_METRO_JK;
			PATH_ORIGINAL_ROUTINES=PATH_ORIGINAL_ROUTINES_JK;
		}
	}
}
