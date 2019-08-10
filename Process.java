package svrp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import com.graphhopper.jsprit.util.Examples;

public class Process 
{
	public static final int MAX_NUM=1200;
	public static final int FLEET_VOLUME=45*5;
	public static final int []WEIGHTS={5,1};
	
	public static final double FLEET_SPEED=16;
	public static final double WALKING_SPEED=3;
	public static final double METRO_COST=180;
	
	final double MAX_WALKING_TIME=900;
	
	private int ac=0;
	private int sc=0;
	
	private ArrayList<String> all=new ArrayList<String>();
	private ArrayList<String> uniques=new ArrayList<String>();
	private Hashtable<String,ArrayList<Integer> > allHt=new Hashtable<String, ArrayList<Integer> >();
	private Hashtable<String,Integer> uniqueHt=new Hashtable<String,Integer>();
	
	private ArrayList<Integer> weights=new ArrayList<Integer>();
	
	private ArrayList<ArrayList<String> > metros=new ArrayList<ArrayList<String>>();
	private Hashtable<String,ArrayList<int[]> > metroHt=new Hashtable<String,ArrayList<int[]>>();
	private ArrayList<String> oldStations=new ArrayList<String>();
	
	private ArrayList<String> startStations=new ArrayList<String>();
	private Hashtable<String,Integer> startCounts=new Hashtable<String,Integer>();
	private ArrayList<String> endStations=new ArrayList<String>();
	private Hashtable<String,Integer> endCounts=new Hashtable<String,Integer>();
	private ArrayList<int[]> startEnds=new ArrayList<int[]>();
	
	private double [][]matrixDriving=new double[MAX_NUM][MAX_NUM];
	private double [][]matrixWalking=new double[MAX_NUM][MAX_NUM];
	
	private ArrayList<ArrayList<Integer> > clusters=new ArrayList<ArrayList<Integer> >();
	private ArrayList<Integer > clusterVehicles=new ArrayList<Integer >();
	private ArrayList<ArrayList<Integer>[] > clusterAllPasses=new ArrayList<ArrayList<Integer>[] >();
	private ArrayList<ArrayList<Integer> > clusterStations=new ArrayList<ArrayList<Integer> >();
	private ArrayList<ArrayList<double[]> > clusterToStations=new ArrayList<ArrayList<double[]> >();
	private ArrayList<ArrayList<Integer> > clusterStationsPath=new ArrayList<ArrayList<Integer> >(); 
	
	private ArrayList<ArrayList<double[]> > clusterToStations2=new ArrayList<ArrayList<double[]> >();
	private ArrayList<ArrayList<Integer> > clusterStationsPath2=new ArrayList<ArrayList<Integer> >(); 
	private ArrayList<Double> clusterCosts=new ArrayList<Double>();
	
	private void read() throws Exception
	{	int uniqueCount=0;
		int allCount=0;
		//read address
		Scanner scanner=new Scanner(new FileInputStream(Common.PATH_ALL_ADDRESS));
		while(scanner.hasNextLine())
		{
			String line=scanner.nextLine();
			String items[]=line.split("\t")[0].split(":");
			all.add(items[0]);
			weights.add(WEIGHTS[Integer.parseInt(items[1])]);
			ac++;
			if(uniqueHt.get(items[0])==null)
			{
				uniqueHt.put(items[0], uniqueCount++);
				uniques.add(items[0]);
			}
			
			ArrayList<Integer> lst=allHt.get(items[0]);
			if(lst==null)
			{
				lst=new ArrayList<Integer>();
				allHt.put(items[0], lst);
			}
			lst.add(allCount++);
		}
		scanner.close();
		
		//read stations
		scanner=new Scanner(new FileInputStream(Common.PATH_ALL_STATIONS));
		while(scanner.hasNextLine())
		{
			String line=scanner.nextLine();
			String name=line.split("\t")[0];
			all.add(name);
			sc++;
			if(uniqueHt.get(name)==null)
			{
				uniqueHt.put(name, uniqueCount++);
				uniques.add(name);
			}
			
			ArrayList<Integer> lst=allHt.get(name);
			if(lst==null)
			{
				lst=new ArrayList<Integer>();
				allHt.put(name, lst);
			}
			lst.add(allCount++);
		}
		scanner.close();
		
		//read driving matrix
		double tmpMatrix[][]=new double[MAX_NUM][MAX_NUM];
		scanner=new Scanner(new FileInputStream(Common.PATH_MATRIX_DRIVING));
		while(scanner.hasNextLine())
		{
			String line=scanner.nextLine().trim();
			if(line.equals(""))
				continue;
			String items[]=line.split("\t");
			//System.out.println(line);
			int x=uniqueHt.get(items[0]);
			int y=uniqueHt.get(items[1]);
			if(items[2].endsWith("公里"))
				tmpMatrix[x][y]=1000*Double.parseDouble(items[2].substring(0, items[2].indexOf("公里")));
			else if(items[2].endsWith("米"))
				tmpMatrix[x][y]=Double.parseDouble(items[2].substring(0, items[2].indexOf("米")));
		}
		scanner.close();
		//System.out.println(all.size());
		for(int i=0;i<all.size();i++)
		{
			int x=uniqueHt.get(all.get(i));
			for(int j=0;j<all.size();j++)
			{
				int y=uniqueHt.get(all.get(j));
				matrixDriving[i][j]=tmpMatrix[x][y];
			}
		}
		
		for(int i=0;i<all.size();i++)
			for(int j=0;j<all.size();j++)
				if(matrixDriving[i][j]==0 && i!=j)
					matrixDriving[i][j]=matrixDriving[j][i];
		
		for(int i=0;i<all.size();i++)
			for(int j=0;j<all.size();j++)
				if(matrixDriving[i][j]==0)
				{
					matrixDriving[i][j]=Double.MAX_VALUE;
				}
		
		//read walking matrix
		tmpMatrix=new double[MAX_NUM][MAX_NUM];
		scanner=new Scanner(new FileInputStream(Common.PATH_MATRIX_WALKING));
		while(scanner.hasNextLine())
		{
			String line=scanner.nextLine().trim();
			if(line.equals(""))
				continue;
			String items[]=line.split("\t");
			int x=uniqueHt.get(items[0]);
			int y=uniqueHt.get(items[1]);
			if(items[2].endsWith("公里"))
				tmpMatrix[x][y]=1000*Double.parseDouble(items[2].substring(0, items[2].indexOf("公里")));
			else if(items[2].endsWith("米"))
				tmpMatrix[x][y]=Double.parseDouble(items[2].substring(0, items[2].indexOf("米")));
		}
		scanner.close();
		for(int i=0;i<ac;i++)
		{
			int x=uniqueHt.get(all.get(i));
			for(int j=ac;j<all.size();j++)
			{
				int y=uniqueHt.get(all.get(j));
				matrixWalking[i][j]=tmpMatrix[x][y];
				matrixWalking[j][i]=matrixWalking[i][j];
				if(matrixWalking[i][j]==0)
					matrixWalking[j][i]=matrixWalking[i][j]=Double.MAX_VALUE;
			}
		}
		
		//read metrolines
		scanner=new Scanner(new FileInputStream(Common.PATH_METRO_STATIONS));
		ArrayList<String> metro=null;
		int arrayIndex=-1,index=-1;
		while(scanner.hasNextLine())
		{
			String line=scanner.nextLine();
			String [] items=line.split(":");
			if(items[0].equals("line"))
			{
				metro=new ArrayList<String>();
				metros.add(metro);
				arrayIndex++;
				index=-1;
			}
			else
			{
				String sta=items[0].split("\t")[0];
				metro.add(sta);
				index++;
				ArrayList<int[]> lst=metroHt.get(sta);
				if(lst==null)
				{
					lst=new ArrayList<int[]>();
					metroHt.put(sta, lst);
				}
				lst.add(new int[]{arrayIndex,index});
			}
		}
		scanner.close();
		
		//read start and end stations
		scanner=new Scanner(new FileInputStream(Common.PATH_OLD_STATIONS));
		while(scanner.hasNextLine())
		{
			String line=scanner.nextLine();
			String name=line.split("\t")[0];
			String [] items=name.split(":");
			if(items[0].equals("起点"))
			{
				startStations.add(items[2]);
				startCounts.put(items[2], Integer.parseInt(items[1]));
				oldStations.add(items[2]);
			}
			else if(items[0].equals("终点"))
			{
				endStations.add(items[2]);
				endCounts.put(items[2], Integer.parseInt(items[1]));
				oldStations.add(items[2]);
			}
			else
				oldStations.add(items[0]);
		}
		scanner.close();
		
		for(int i=0,k=0;i<startStations.size();i++)
        {
        	String start=startStations.get(i);
        	int c=startCounts.get(start);
        	for(int j=0;j<c;j++,k++)
        	{
        		startEnds.add(new int[]{allHt.get(start).get(0), 0});
        	}
        }
        
        for(int i=0,k=0;i<endStations.size();i++)
        {
        	String end=endStations.get(i);
        	int c=endCounts.get(end);
        	for(int j=0;j<c;j++,k++)
        	{
        		startEnds.get(k)[1]=allHt.get(end).get(0);
        	}
        }
	}
	
	private void outputUnreachable() throws Exception
	{
		PrintWriter pw1=new PrintWriter(Common.PATH_UNREACHABLE_DRIVING);
		for(int i=0;i<all.size();i++)
			for(int j=0;j<all.size();j++)
				if(matrixDriving[i][j]==Double.MAX_VALUE)
				{
					System.out.println(i+":"+j);
					pw1.println(all.get(i)+"\t"+all.get(j));
				}
		pw1.close();
		
		PrintWriter pw2=new PrintWriter(Common.PATH_UNREACHABLE_WALKING);
		for(int i=0;i<all.size();i++)
			for(int j=0;j<all.size();j++)
				if(matrixWalking[i][j]==Double.MAX_VALUE)
				{
					pw2.println(all.get(i)+"\t"+all.get(j));
				}
		pw2.close();
	}
	
	private void cluster() throws FileNotFoundException
	{
		Examples.createOutputFolder();

        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, FLEET_VOLUME).setCostPerDistance(1).setCostPerTime(2).build();
        
        ArrayList<VehicleImpl.Builder> builders=new ArrayList<VehicleImpl.Builder>();
        for(int i=0;i<startEnds.size();i++)
        {
        	int[] startEnd=startEnds.get(i);
        	VehicleImpl.Builder builder = VehicleImpl.Builder.newInstance("vehicle"+i).setType(type);
        	builder.setType(type);
        	builder.setStartLocation(Location.newInstance(""+startEnd[0]));
        	builder.setEndLocation(Location.newInstance(""+startEnd[1]));
        	//System.out.println("start-end:"+startEnd[0]+"-"+startEnd[1]);
        	builders.add(builder);
        }
        /*for(int i=0,k=0;i<startStations.size();i++)
        {
        	String start=startStations.get(i);
        	int c=startCounts.get(start);
        	for(int j=0;j<c;j++,k++)
        	{
        		VehicleImpl.Builder builder = VehicleImpl.Builder.newInstance("vehicle"+k).setType(type);
        		builder.setType(type);
        		builder.setStartLocation(Location.newInstance(""+allHt.get(start).get(0)));
        		System.out.println("start:"+allHt.get(start).get(0));
        		builders.add(builder);
        	}
        }
        
        for(int i=0,k=0;i<endStations.size();i++)
        {
        	String end=endStations.get(i);
        	int c=endCounts.get(end);
        	for(int j=0;j<c;j++,k++)
        	{
        		VehicleImpl.Builder builder = builders.get(k);
        		builder.setType(type);
        		builder.setEndLocation(Location.newInstance(""+allHt.get(end).get(0)));
        		System.out.println("end:"+allHt.get(end).get(0));
        	}
        }*/
        
        VehicleImpl [] vehicles=new VehicleImpl[builders.size()];
        for(int i=0;i<builders.size();i++)
        {
        	vehicles[i]=builders.get(i).build();
        }
        //VehicleImpl vehicle1 = VehicleImpl.Builder.newInstance("vehicle1")
        //		.setStartLocation(Location.newInstance("0")).setEndLocation(Location.newInstance("1")).setType(type).build();

        Service [] servs=new Service[ac];
        for(int i=0;i<servs.length;i++)
        {
        	String adr=all.get(i);
        	servs[i]=Service.Builder.newInstance(""+i).addSizeDimension(0, weights.get(i)).setLocation(Location.newInstance(""+i)).build();
        }

        //define a matrix-builder building a symmetric matrix
        VehicleRoutingTransportCostsMatrix.Builder costMatrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        for(int i=0;i<all.size();i++)
        {
        	for(int j=0;j<all.size();j++)
        	{
        		costMatrixBuilder.addTransportDistance(""+i, ""+j, matrixDriving[i][j]);
            	costMatrixBuilder.addTransportTime(""+i, ""+j, matrixDriving[i][j]);
        	}
        }
        VehicleRoutingTransportCosts costMatrix = costMatrixBuilder.build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.setFleetSize(FleetSize.FINITE);
        
        for(int i=0;i<vehicles.length;i++)
        	vrpBuilder.addVehicle(vehicles[i]);
        
        for(int i=0;i<servs.length;i++)
        	vrpBuilder.setRoutingCost(costMatrix).addJob(servs[i]);
        //System.out.println(servs.length);
        
        VehicleRoutingProblem vrp = vrpBuilder.build();
        
        //VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().setFleetSize(FleetSize.INFINITE).setRoutingCost(costMatrix)
        //    .addVehicle(vehicle2).addVehicle(vehicle).addJob(s1).addJob(s2).addJob(s3).build();
        PrintStream print=new PrintStream(Common.PATH_CLUSTER);
		System.setOut(print);
        //System.out.println("1");
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        //System.out.println("2");
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        //System.out.println("3");
        
        SolutionPrinter.print(vrp, Solutions.bestOf(solutions), SolutionPrinter.Print.VERBOSE);

        //new Plotter(vrp, Solutions.bestOf(solutions)).plot("output/yo.png", "po");
	}
	
	private void readClusters() throws Exception
	{
		int go=0;
		ArrayList<Integer> lst=null;
		Scanner scanner=new Scanner(new FileInputStream(Common.PATH_CLUSTER));
		boolean sign=false;
		while(scanner.hasNextLine())
		{
			String line=scanner.nextLine();
			if(line.equals("+---------+----------------------+-----------------------+-----------------+-----------------+-----------------+-----------------+"))
			{
				go++;
				if(go>=2)
				{
					lst=new ArrayList<Integer>();
					clusters.add(lst);
					sign=true;
				}
			}
			else if(! line.equals("+--------------------------------------------------------------------------------------------------------------------------------+") && 
					go>=2)
			{
				String []items=line.split("\\|");
				
				try
				{
					lst.add(Integer.parseInt(items[4].trim()));
					//System.out.println(items[4].trim());
					if(sign)
					{
						//System.out.println(items[2].substring(items[2].indexOf("vehicle")+"vehicle".length()).trim());
						clusterVehicles.add(Integer.parseInt(items[2].substring(items[2].indexOf("vehicle")+"vehicle".length()).trim()));
						//System.out.println(Integer.parseInt(items[2].substring(items[2].indexOf("vehicle")+1)));
					}
					sign=false;
					//System.out.println(items[4]);
				}
				catch(NumberFormatException e)
				{
					//System.out.println(line);
					//e.printStackTrace();
				}
				catch(ArrayIndexOutOfBoundsException e)
				{
					//System.out.println(line);
				}
			}
		}
		scanner.close();
	}
	
	private double getAdrToStaDis(int fromIndex, int toIndex)
	{
		return getAdrToStaDis(fromIndex, toIndex, MAX_WALKING_TIME);
	}
	
	private double getAdrToStaDis(int fromIndex, int toIndex, double time)
	{
		String to=all.get(toIndex);
		if(metroHt.get(to)==null)
		{
			double tmp=matrixWalking[fromIndex][toIndex]/WALKING_SPEED;
			if(tmp<=time)
				return tmp;
			else
				return -1;
		}
		else
		{
			ArrayList<int[]> lst=metroHt.get(to);
			double cost=-1;
			for(int[]indices:lst)
			{
				ArrayList<String> metro=metros.get(indices[0]);
				for(int h=indices[1],c=0;h>=0;h--,c++)
				{
					int x=allHt.get(metro.get(h)).get(0);
					double tmp=matrixWalking[fromIndex][x]/WALKING_SPEED + c*METRO_COST;
					if(tmp<=time && (cost==-1 || cost>tmp))
					{
						cost=tmp;
					}
				}
				
				for(int h=indices[1]+1,c=0;h<metro.size();h++,c++)
				{
					int x=allHt.get(metro.get(h)).get(0);
					double tmp=matrixWalking[fromIndex][x]/WALKING_SPEED + c*METRO_COST;
					if(tmp<=time && (cost==-1 || cost>tmp))
					{
						cost=tmp;
					}
				}
			}
			return cost;
		}
	}
	
	private void assign()
	{
		int adrCount=ac;
		
		double sMatrix[][]=new double[MAX_NUM][MAX_NUM];
		for(int k=0;k<clusters.size();k++)
		{
			ArrayList<int[]> as=new ArrayList<int[]>();
			ArrayList<Integer> cluster=clusters.get(k);
			for(int i=0;i<cluster.size();i++)
			{
				int adrIndex=cluster.get(i);
				for(int j=adrCount;j<all.size();j++)
				{
					sMatrix[adrIndex][j]=getAdrToStaDis(adrIndex, j);
				}
			}
		}
		
		ArrayList<Integer>[] clusterAllPass=new ArrayList[MAX_NUM];
		for(int i=0;i<MAX_NUM;i++)
			clusterAllPass[i]=new ArrayList<Integer>();
		
		//ArrayList<ArrayList<Integer> > unsigns=new ArrayList<ArrayList<Integer> >();
		for(int k=0;k<clusters.size();k++)
		{
			clusterAllPasses.add(clusterAllPass);
			ArrayList<Integer> clusterStation=new ArrayList<Integer>();
			clusterStations.add(clusterStation);
			ArrayList<Integer> cluster=clusters.get(k);
			int[] cc=new int[MAX_NUM];
			//clusterAllStations.add(clusterAllPass);
			for(int i=0;i<cluster.size();i++)
			{
				int adrIndex=cluster.get(i);
				for(int j=adrCount;j<all.size();j++)
				{
					if(sMatrix[adrIndex][j]>-1)
					{
						cc[j]++;
						clusterAllPass[j].add(adrIndex);
					}
				}
			}
			
			//ArrayList<Integer> unsign=new ArrayList<Integer>();
			//unsigns.add(unsign);
			
			int []selected=new int[MAX_NUM];
			int count=cluster.size();
			int preCount=-1;
			while(count>0 && preCount!=count)
			{
				preCount=count;
				
				int maxIndex=-1;
				for(int j=adrCount;j<all.size();j++)
				{
					if(maxIndex==-1 || cc[maxIndex]<cc[j])
					{
						maxIndex=j;
						
					}
				}
				if(cc[maxIndex]==0)
				{
					//System.out.println(preCount+":"+count);
					for(int i=0;i<cluster.size();i++)
					{
						int adrIndex=cluster.get(i);
						if(selected[adrIndex]==0)
						{
							//unsign.add(adrIndex);
							//System.out.print("a: "+adrIndex+", ");
						}
					}
					
					//System.out.println();
					continue;
				}
				//System.out.println(maxIndex+":"+cc[maxIndex]);
				clusterStation.add(maxIndex);
				for(int i=0;i<cluster.size();i++)
				{
					int adrIndex=cluster.get(i);
					if(sMatrix[adrIndex][maxIndex]>-1 && selected[adrIndex]==0)
					{
						selected[adrIndex]=maxIndex;
						for(int j=adrCount;j<all.size();j++)
						{
							if(sMatrix[adrIndex][j]>-1)
								cc[j]--;
						}
						count--;
					}
				}
			}
			
			ArrayList<double[]> clusterToStation=new ArrayList<double[]>();
			clusterToStations.add(clusterToStation);
			for(int i=0;i<cluster.size();i++)
			{
				int adrIndex=cluster.get(i);
				int minIndex=-1;
				double minDis=-1;
				if(selected[adrIndex]>0)
				{
					for(int j=0;j<clusterStation.size();j++)
					{
						int index=clusterStation.get(j);
						double dis2=getAdrToStaDis(adrIndex, index);
						if(minIndex==-1 && dis2>-1 || minIndex>-1 && dis2>-1 && minDis>dis2)
						{
							minIndex=index;
							minDis=dis2;
						}
					}
					//System.out.println("0:"+minDis);
					clusterToStation.add(new double[]{0,minIndex,minDis});
				}
				else
				{
					for(int j=0;j<clusterStation.size();j++)
					{
						int index=clusterStation.get(j);
						double dis2=getAdrToStaDis(adrIndex, index, Double.MAX_VALUE);
						if(minIndex==-1 && dis2>-1 || minIndex>-1 && dis2>-1 && minDis>dis2)
						{
							minIndex=index;
							minDis=dis2;
						}
						//clusterAllPass[j].add(adrIndex);
					}
					
					System.out.println("1:"+minDis);
					clusterToStation.add(new double[]{1,minIndex,minDis});
				}
			}
			
			/*System.out.println("-------------------------------------------");
			for(int j=0;j<clusterStation.size();j++)
			{
				System.out.println(adrCount+":"+clusterStation.get(j)+":"+all.get(clusterStation.get(j)));
			}*/
		}
		
		/*for(int k=0;k<clusterToStations.size();k++)
		{
			ArrayList<double[]> list=clusterToStations.get(k);
			for(int i=0;i<list.size();i++)
			{
				double[] items=list.get(i);
				System.out.println(items[0]+":"+items[1]+":"+items[2]);
			}
			System.out.println("-------------------------------------------");
		}*/
	}
	
	private double evaluate(ArrayList<Integer> cluster, ArrayList<Integer> clusterStation, ArrayList<double[]> clusterToStation)
	{
		double walkingCost=0, drivingCost=0;
		
		Hashtable<Integer,Integer> stationIndices=new Hashtable<Integer,Integer>();
		Hashtable<Integer,ArrayList<Integer> > stationAssignNum=new Hashtable<Integer,ArrayList<Integer>>();
		for(int i=0;i<clusterStation.size();i++)
		{
			stationIndices.put((int)clusterStation.get(i), i);
			stationAssignNum.put((int)clusterStation.get(i), new ArrayList<Integer>());
		}
		
		for(int i=0;i<clusterToStation.size();i++)
		{
			double[] items=clusterToStation.get(i);
			int adrIndex=cluster.get(i);
			//System.out.println((int)items[1]);
			stationAssignNum.get((int)items[1]).add(adrIndex);
			//stationAssignNum.put((int)items[1],stationAssignNum.get((int)items[1])+1);
			walkingCost+=items[2] * weights.get(adrIndex);
		}
		
		int pathSize=clusterStation.size();
		double pathLength[]=new double[pathSize];
		pathLength[pathSize-1]=0;
		for(int i=clusterStation.size()-2;i>=0;i--)
		{
			int index1=clusterStation.get(i);
			int index2=clusterStation.get(i+1);
			pathLength[i]=matrixDriving[index1][index2]+pathLength[i+1];
		}
		
		for(int i=0;i<clusterStation.size();i++)
		{
			int index=clusterStation.get(i);
			ArrayList<Integer> ss=stationAssignNum.get(index);
			for(int j=0;j<ss.size();j++)
			{
				drivingCost+=weights.get(ss.get(j))*pathLength[i]/FLEET_SPEED;
			}
			
		}
		
		return walkingCost+drivingCost;
	}
	
	private double insertStationToPath(ArrayList<Integer> cluster, ArrayList<Integer> clusterStation, 
			ArrayList<double[]> clusterToStation, ArrayList<Integer>[] clusterPass,
			ArrayList<Integer> clusterStation2, ArrayList<double[]> clusterToStation2)
	{
		double minStationCost=Double.MAX_VALUE;
		int minStation=-1;
		ArrayList<Integer> minClusterStation=null;
		ArrayList<double[]> minClusterToStation=null;
		for(int station=1;station<clusterPass.length-1;station++)
		{
			if(clusterPass[station].size()<10)
				continue;
			
			int minIndex=-1;
			double minCost=Double.MAX_VALUE;
			//int stationIndex=clusterStation.get(index)
			
			ArrayList<Integer> tmpClusterStation=new ArrayList<Integer>();
			
			ArrayList<double[]> tmpClusterToStation=new ArrayList<double[]>();
			tmpClusterToStation.addAll(clusterToStation);
			
			ArrayList<Integer> passes=clusterPass[station];
			for(int i=0;i<passes.size();i++)
			{
				int pass=passes.get(i);
				int adrIndex=0;
				for(int k=0;k<cluster.size();k++)
				{
					if(cluster.get(k)==pass)
					{
						adrIndex=k;
						break;
					}
				}
				double dis=this.getAdrToStaDis(pass, station);
				double oldDis=tmpClusterToStation.get(adrIndex)[2];
				if(dis<oldDis)
				{
					tmpClusterToStation.remove(adrIndex);
					tmpClusterToStation.add(adrIndex,new double[]{1,station,dis});
				}
			}
			
			for(int i=0;i<clusterStation.size();i++)
			{
				ArrayList<Integer> cs=new ArrayList<Integer>();
				cs.addAll(clusterStation);
				cs.add(i,station);
				double cost=this.evaluate(cluster, cs, tmpClusterToStation);
				if(minCost>cost)
				{
					minCost=cost;
					minIndex=i;
				}
			}
			
			tmpClusterStation.addAll(clusterStation);
			tmpClusterStation.add(minIndex, station);
			
			if(minStationCost>minCost)
			{
				minStationCost=minCost;
				minStation=station;
				minClusterStation=tmpClusterStation;
				minClusterToStation=tmpClusterToStation;
			}
		}
		
		clusterStation2.addAll(minClusterStation);
		clusterToStation2.addAll(minClusterToStation);
		
		return minStationCost;
	}
	
	private void innerAdjust()
	{
		//System.out.println(clusterVehicles.size());
		for(int h=0;h<clusterStations.size();h++)
		{
			ArrayList<Integer> stations=clusterStations.get(h);
			int[] startEnd=startEnds.get(clusterVehicles.get(h));
			int startIndex=-1, endIndex=-1;
			for(int j=0;j<stations.size();j++)
			{
				int index=stations.get(j);
				if(all.get(index).equals(all.get(startEnd[0])))
					startIndex=index;
				if(all.get(index).equals(all.get(startEnd[1])))
					endIndex=index;
			}
			if(startIndex==-1)
				stations.add(0, startEnd[0]);
			else
			{
				stations.remove((Object)startIndex);
				stations.add(0, startEnd[0]);
			}
			if(endIndex==-1)
				stations.add(startEnd[1]);
			else
			{
				stations.remove((Object)endIndex);
				stations.add(startEnd[1]);
			}
			
			int n=stations.size();
			double[][] v=new double[n][n];
			for(int i=0;i<n;i++)
			{
				for(int j=0;j<n;j++)
				{
					v[i][j]=matrixDriving[stations.get(i)][stations.get(j)];
					System.out.print(v[i][j]+":"+all.get(stations.get(i))+"-"+all.get(stations.get(j))+"\t");
				}
				System.out.println();
			}
			System.out.println();
			System.out.println();
			System.out.println();
			double[][] f=new double[1<<n][n];
			for(int i=0;i<1<<n;i++)
				for(int j=0;j<n;j++)
					f[i][j]=Double.MAX_VALUE;
			
			f[1][0]=0;
			int[][] path=new int[1<<n][n];
			path[1][0]=0;
			for(int i = 1;i < 1 << n;i++)
			{
				for(int j = 0;j < n;j++)
				{
					if((i >> j & 1) == 1)
					{
						for(int k = 0;k < n;k++)
						{
							if((i - (1 << j) >> k & 1) == 1)
							{
								if(f[i][j]>f[i-(1<<j)][k] + v[k][j])
								{
									f[i][j] = f[i-(1<<j)][k] + v[k][j];
									path[i][j]=k;
								}
							}
						}
					}
				}
			}
			
			//cout<<f[(1<<n)-1][n-1]<<endl;
			ArrayList<Integer> stations2=new ArrayList<Integer>();
			clusterStationsPath.add(stations2);
			int i = (1 << n) - 1;
			int j=n-1;
			do
			{
				int k=j;
				j=path[i][j];
				stations2.add(0, stations.get(k));
				i=i-(1<<k);
				
			} while(i>0);
		}
		
		for(int i=0;i<clusterStations.size();i++)
		{
			ArrayList<Integer> cluster=clusters.get(i);
			ArrayList<Integer> stations=clusterStations.get(i);
			ArrayList<double[]> clusterToStation=clusterToStations.get(i);
			
			double cost=evaluate(cluster, stations, clusterToStation);
			
			for(int a:stations)
			{
				System.out.print(a+", ");
			}
			
			ArrayList<Integer> stations2=clusterStationsPath.get(i);
			//ArrayList<double[]> clusterToStation=clusterToStations.get(i);
			//double cost=evaluate(stations, clusterToStation);
			System.out.print(" || ");
			for(int a:stations2)
			{
				
				System.out.print(a+", ");
			}
			
			System.out.println();
		}
		
		
		for(int i=0;i<clusterStations.size();i++)
		{
			ArrayList<Integer> clusterStationPath2=new ArrayList<Integer>();
			ArrayList<double[]> clusterToStation2=new ArrayList<double[]>();
			ArrayList<Integer> cluster=clusters.get(i);
			ArrayList<Integer> clusterStationPath=clusterStationsPath.get(i);
			ArrayList<double[]> clusterToStation=clusterToStations.get(i);
			ArrayList<Integer>[] clusterPass=clusterAllPasses.get(i);
			//System.out.println("ddd:"+i);
			double cost=this.evaluate(cluster, clusterStationPath, clusterToStation);
			double cost2=insertStationToPath(cluster, clusterStationPath, clusterToStation, 
					clusterPass, clusterStationPath2, clusterToStation2);
			
			if(cost>cost2)
			{
				clusterStationsPath2.add(clusterStationPath2);
				clusterToStations2.add(clusterToStation2);
				clusterCosts.add(cost2);
				System.out.println("0:"+cost2);
			}
			else
			{
				clusterStationsPath2.add(clusterStationPath);
				clusterToStations2.add(clusterToStation);
				clusterCosts.add(cost);
				System.out.println("1:"+cost);
			}
		}
		
		double total=0;
		for(double cost:clusterCosts)
		{
			total+=cost;
		}
		System.out.println("total1:"+total/5.0/ac);
	}
	
	protected void interAdjust()
	{
		final int NNN=1;
		
		ArrayList<Hashtable<Integer,Integer> > stationAssignNums;
		
		for(int i=0;i<clusters.size();i++)
		{
			for(int j=i+1;j<clusters.size();j++)
			{
				stationAssignNums=new ArrayList<Hashtable<Integer,Integer> >();
				for(int k=0;k<clusters.size();k++)
				{
					ArrayList<Integer> cluster=clusters.get(k);
					ArrayList<Integer> clusterStation=clusterStationsPath2.get(k);
					ArrayList<double[]> clusterToStation=clusterToStations2.get(k);
					
					Hashtable<Integer,Integer> stationAssignNum=new Hashtable<Integer,Integer>();
					for(int h=0;h<clusterStation.size();h++)
						stationAssignNum.put((int)clusterStation.get(h), 0);
					
					for(int h=0;h<clusterToStation.size();h++)
					{
						double[] items=clusterToStation.get(h);
						int index=cluster.get(h);
						stationAssignNum.put((int)items[1],stationAssignNum.get((int)items[1])+weights.get(index));
					}
					
					stationAssignNums.add(stationAssignNum);
				}
				
				ArrayList<Integer> cluster1=clusters.get(i);
				ArrayList<Integer> cluster2=clusters.get(j);
				ArrayList<Integer> clusterStation1=clusterStationsPath2.get(i);
				ArrayList<Integer> clusterStation2=clusterStationsPath2.get(j);
				ArrayList<double[]> clusterToStation1=clusterToStations2.get(i);
				ArrayList<double[]> clusterToStation2=clusterToStations2.get(j);
				double cost1=clusterCosts.get(i);
				double cost2=clusterCosts.get(j);
				Hashtable<Integer,Integer> stationAssignNum1=stationAssignNums.get(i);
				Hashtable<Integer,Integer> stationAssignNum2=stationAssignNums.get(j);
				//System.out.println(""+i+"-"+j+":"+clusterToStation1.size()+","+cluster1.size()+";"+clusterToStation2.size()+","+cluster2.size());
				for(int x=1;x<clusterStation1.size()-1;x++)
				{
					for(int y=1;y<clusterStation2.size()-1;y++)
					{
						int index1=clusterStation1.get(x);
						int index2=clusterStation2.get(y);
						int assignNum1=stationAssignNum1.get(index1);
						int assignNum2=stationAssignNum2.get(index2);
						
						if(Math.abs(assignNum1-assignNum2)>NNN)
							continue;
						
						ArrayList<Integer> newCluster1=new ArrayList<Integer>();
						ArrayList<Integer> newCluster2=new ArrayList<Integer>();
						
						ArrayList<Integer> newClusterStation1=new ArrayList<Integer>();
						ArrayList<Integer> newClusterStation2=new ArrayList<Integer>();
						
						ArrayList<double[]> newClusterToStation1=new ArrayList<double[]>();
						ArrayList<double[]> newClusterToStation2=new ArrayList<double[]>();
						
						newCluster1.addAll(cluster1);
						newClusterStation1.addAll(clusterStation1);
						//newClusterToStation1.addAll(clusterToStation1);
						for(int k=0;k<clusterToStation1.size();k++)
							newClusterToStation1.add(clusterToStation1.get(k).clone());
						newClusterStation1.remove((Object)index1);
						
						newCluster2.addAll(cluster2);
						newClusterStation2.addAll(clusterStation2);
						for(int k=0;k<clusterToStation2.size();k++)
							newClusterToStation2.add(clusterToStation2.get(k).clone());
						newClusterStation2.remove((Object)index2);
						
						ArrayList<Integer> removeCluster1=new ArrayList<Integer>();
						ArrayList<double[]> removeClusterToStation1=new ArrayList<double[]>();
						for(int k=0;k<newClusterToStation1.size();)
						{
							double []tmp=newClusterToStation1.get(k).clone();
							if(tmp[1]==index1)
							{
								removeCluster1.add(cluster1.get(k));
								removeClusterToStation1.add(tmp);
								newCluster1.remove(k);
								newClusterToStation1.remove(k);
							}
							else
								k++;
						}
						
						ArrayList<Integer> removeCluster2=new ArrayList<Integer>();
						ArrayList<double[]> removeClusterToStation2=new ArrayList<double[]>();
						for(int k=0;k<newClusterToStation2.size();)
						{
							double []tmp=newClusterToStation2.get(k).clone();
							if(tmp[1]==index2)
							{
								removeCluster2.add(cluster2.get(k));
								removeClusterToStation2.add(tmp);
								newCluster2.remove(k);
								newClusterToStation2.remove(k);
							}
							else
								k++;
						}
						
						for(int k=0;k<removeClusterToStation2.size();k++)
						{
							int removeIndex=removeCluster2.get(k);
							double[] tmp=removeClusterToStation2.get(k);
							int stationIndex=-1;
							double dis=tmp[2];
							for(int h=0;h<newClusterStation1.size();h++)
							{
								double dis2=getAdrToStaDis(removeIndex, newClusterStation1.get(h), MAX_WALKING_TIME);
								if(dis2!=-1 && dis>dis2)
								{
									dis=dis2;
									stationIndex=newClusterStation1.get(h);
								}
							}
							if(stationIndex!=-1)
							{
								tmp[1]=stationIndex;
								tmp[2]=dis;
							}
						}
						
						
						for(int k=0;k<newClusterToStation1.size();k++)
						{
							double[] tmp=newClusterToStation1.get(k).clone();
							double dis2=getAdrToStaDis(newCluster1.get(k), index2, MAX_WALKING_TIME);
							if(dis2!=-1 && tmp[2]>dis2)
							{
								tmp[1]=index2;
								tmp[2]=dis2;
							}
						}
						
						newCluster1.addAll(removeCluster2);
						newClusterToStation1.addAll(removeClusterToStation2);
						//System.out.println("newCluster1:"+newCluster1.size()+",newClusterToStation1:"+newClusterToStation1.size());
						
						///////////////////////////////////////////////////////////////////////////////
						for(int k=0;k<removeClusterToStation1.size();k++)
						{
							int removeIndex=removeCluster1.get(k);
							double[] tmp=removeClusterToStation1.get(k).clone();
							int stationIndex=-1;
							double dis=tmp[2];
							for(int h=0;h<newClusterStation2.size();h++)
							{
								double dis2=getAdrToStaDis(removeIndex, newClusterStation2.get(h), MAX_WALKING_TIME);
								if(dis2>-1 && dis>dis2)
								{
									dis=dis2;
									stationIndex=newClusterStation2.get(h);
								}
							}
							if(stationIndex!=-1)
							{
								tmp[1]=stationIndex;
								tmp[2]=dis;
							}
						}
						
						for(int k=0;k<newClusterToStation2.size();k++)
						{
							double[] tmp=newClusterToStation2.get(k).clone();
							double dis2=getAdrToStaDis(newCluster2.get(k), index2, MAX_WALKING_TIME);
							if(dis2>-1 && tmp[2]>dis2)
							{
								tmp[1]=index2;
								tmp[2]=dis2;
							}
						}
						newCluster2.addAll(removeCluster1);
						newClusterToStation2.addAll(removeClusterToStation1);
						
						double newCost1=-1, newCost2=-1;
						int newStationIndex1=-1, newStationIndex2=-1;
						for(int k=1;k<newClusterStation1.size()-1;k++)
						{
							newClusterStation1.add(k,index2);
							double newc1=evaluate(newCluster1, newClusterStation1, newClusterToStation1);
							if(newCost1==-1 || newCost1>newc1)
							{
								newCost1=newc1;
								newStationIndex1=k;
							}
							newClusterStation1.remove(k);
						}
						newClusterStation1.add(newStationIndex1, index2);
						
						for(int k=1;k<newClusterStation2.size()-1;k++)
						{
							newClusterStation2.add(k,index1);
							double newc2=evaluate(newCluster2, newClusterStation2, newClusterToStation2);
							if(newCost2==-1 || newCost2>newc2)
							{
								newCost2=newc2;
								newStationIndex2=k;
							}
							newClusterStation2.remove(k);
						}
						newClusterStation2.add(newStationIndex2, index1);
						
						if(newCost1+newCost2<cost1+cost2)
						{
							clusters.remove(i);
							clusters.add(i,newCluster1);
							clusters.remove(j);
							clusters.add(j,newCluster2);
							
							clusterStationsPath2.remove(i);
							clusterStationsPath2.add(i,newClusterStation1);
							clusterStationsPath2.remove(j);
							clusterStationsPath2.add(j,newClusterStation2);
							
							clusterToStations2.remove(i);
							clusterToStations2.add(i,newClusterToStation1);
							clusterToStations2.remove(j);
							clusterToStations2.add(j,newClusterToStation2);
							
							clusterCosts.remove(i);
							clusterCosts.add(i,newCost1);
							clusterCosts.remove(j);
							clusterCosts.add(j,newCost2);
							
							//System.out.println("newCluster1:"+newCluster1.size()+",newClusterToStation1:"+newClusterToStation1.size());
						}
						
					}
				}
			}
		}
		
		double total=0;
		for(int i=0;i<clusterStationsPath2.size();i++)
		{
			ArrayList<Integer> cluster=clusters.get(i);
			ArrayList<Integer> clusterStationPath=clusterStationsPath2.get(i);
			ArrayList<double[]> clusterToStation=clusterToStations2.get(i);
			
			double cost=this.evaluate(cluster, clusterStationPath, clusterToStation);
			total+=cost;
		}
		System.out.println("total2:"+total/5.0/ac);
	}
	
	public void output()
	{
		double drivingTotal=0;
		double totalaaa=0;
		for(int i=0;i<clusterStationsPath2.size();i++)
		{
			ArrayList<Integer> path=clusterStationsPath2.get(i);
			double drivingCost=0;
			for(int j=0;j<path.size();j++)
			{
				int index=path.get(j);
				System.out.print(all.get(index)+", ");
			}
			for(int j=path.size()-2;j>=0;j--)
			{
				int index1=path.get(j);
				int index2=path.get(j+1);
				drivingCost+=matrixDriving[index1][index2];
				drivingTotal+=matrixDriving[index1][index2];
			}
			System.out.print(drivingCost/1000);
			
			ArrayList<Integer> cluster=clusters.get(i);
			ArrayList<Integer> clusterStationPath=clusterStationsPath2.get(i);
			ArrayList<double[]> clusterToStation=clusterToStations2.get(i);
			
			double cost=this.evaluate(cluster, clusterStationPath, clusterToStation);
			
			double aaa=0;
			for(int index:cluster)
			{
				aaa+=weights.get(index);
				totalaaa+=weights.get(index);
			}
			
			System.out.print(","+cost/60/5.0/cluster.size()+", "+aaa/5.0);
			
			System.out.println();
		}
		
		System.out.println("drivingTotal:"+drivingTotal/FLEET_SPEED/10+", "+drivingTotal/10000+", "+totalaaa/50);
	}
	
	public void run(String type) throws Exception
	{
		//preprocess();
		Common.setCommon(type);
		read();
		//outputUnreachable();
		//cluster();
		readClusters();
		assign();
		innerAdjust();
		interAdjust();
		output();
	}
	
	public void test()
	{
		
	}
	
	public static void main(String[] args) throws Exception
	{
		Process p=new Process();
		p.run("ALL");
		//p.run("JC");
		//p.run("JK");
		p.test();
	}
}
