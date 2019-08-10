package svrp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Random;
import java.util.Scanner;

import scala.actors.threadpool.Arrays;

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

public class ProcessOriginal
{
	public static final int MAX_NUM=1200;
	public static final int FLEET_VOLUME=45*5;
	public static final int []WEIGHTS={5,1};
	
	public static final double FLEET_SPEED=16;
	public static final double WALKING_SPEED=3;
	
	final double MAX_WALKING_TIME=1900;
	
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
		scanner=new Scanner(new FileInputStream(Common.PATH_ALL_STATIONS_NO_METRO));
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
		scanner=new Scanner(new FileInputStream(Common.PATH_MATRIX_DRIVING_NO_METRO));
		while(scanner.hasNextLine())
		{
			String line=scanner.nextLine().trim();
			if(line.equals(""))
				continue;
			String items[]=line.split("\t");
			//System.out.println(line);
			//System.out.println(items[0]+":"+items[1]);
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
		scanner=new Scanner(new FileInputStream(Common.PATH_MATRIX_WALKING_NO_METRO));
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
        
        //read original routines
        scanner=new Scanner(new FileInputStream(Common.PATH_ORIGINAL_ROUTINES));
		while(scanner.hasNextLine())
		{
			String line=scanner.nextLine();
			String[] items=line.split("\t");
			ArrayList<Integer> clusterStation=new ArrayList<Integer>();
			for(int i=0;i<items.length;i++)
			{
				for(int j=ac;j<all.size();j++)
				{
					if(all.get(j).equals(items[i]))
					{
						
						clusterStation.add(j);
						break;
					}
				}
			}
			clusterStationsPath2.add(clusterStation);
		}
		scanner.close();
	}
	
	private double getAdrToStaDis(int fromIndex, int toIndex)
	{
		return getAdrToStaDis(fromIndex, toIndex, MAX_WALKING_TIME);
	}
	
	private double getAdrToStaDis(int fromIndex, int toIndex, double time)
	{
		double tmp=matrixWalking[fromIndex][toIndex]/WALKING_SPEED;
		if(tmp<=time)
			return tmp;
		else
			return -1;
	}
	
	private void assign()
	{
		ArrayList<ArrayList<Integer> > clusters=new ArrayList<ArrayList<Integer> >();
		ArrayList<ArrayList<double[]> > clusterToStations2=new ArrayList<ArrayList<double[]> >();
		ArrayList<Double> clusterCosts=new ArrayList<Double>();
		
		ArrayList<Integer> adrs=new ArrayList<Integer>();
		for(int i=0;i<ac;i++)
			adrs.add(i);
		
		Hashtable<Integer,ArrayList<Integer> > ht=new Hashtable<Integer,ArrayList<Integer> >();
		for(int i=0;i<clusterStationsPath2.size();i++)
		{
			ArrayList<Integer> clusterStationPath=clusterStationsPath2.get(i);
			//System.out.println(clusterStationPath.size());
			for(int j=0;j<clusterStationPath.size();j++)
			{
				int station=clusterStationPath.get(j);
				ArrayList<Integer> paths=ht.get(station);
				if(paths==null)
				{
					paths=new ArrayList<Integer>();
					ht.put(station, paths);
					//System.out.println("sss:"+station);
				}
				paths.add(i);
				//System.out.println("sss: "+station);
			}
		}
		
		double minx=Double.MAX_VALUE;
		for(int h=0;h<1000;h++)
		{
			int count=0;	
			while(count<ac)
			{
				count=0;
				
				Random ran=new Random();
				ran.setSeed(System.currentTimeMillis());
				clusters=new ArrayList<ArrayList<Integer> >();
				clusterToStations2=new ArrayList<ArrayList<double[]> >();
				clusterCosts=new ArrayList<Double>();
				
				for(int i=0;i<clusterStationsPath2.size();i++)
				{
					clusters.add(new ArrayList<Integer>());
					clusterToStations2.add(new ArrayList<double[]>());
				}
				
				int [] volumes=new int[clusterStationsPath2.size()];
				
				for(int i=0;i<ac;i++)
				{
					int s=ran.nextInt(adrs.size());
					int adrIndex=adrs.get(s);
					
					int indices[]=new int[all.size()-ac];
					for(int j=0;j<indices.length;j++)
					{
						indices[j]=ac+j;
						//System.out.println("aaa:"+indices[j]);
					}
					
					int tmp;
					for(int j=0;j<indices.length;j++)
					{
						for(int k=indices.length-1;k>j;k--)
						{
							if(matrixWalking[adrIndex][indices[k]]<matrixWalking[adrIndex][indices[k-1]])
							{
								tmp=indices[k];
								indices[k]=indices[k-1];
								indices[k-1]=tmp;
							}
						}
					}
					
					boolean sign=true;
					for(int j=0;j<indices.length&&sign;j++)
					{//System.out.println(indices[j]);
						ArrayList<Integer> paths=ht.get(indices[j]);
						if(paths==null) System.out.println(indices[j]);
						for(int path:paths)
						{
							if(volumes[path]+weights.get(adrIndex)<=FLEET_VOLUME)
							{
								volumes[path]=volumes[path]+weights.get(adrIndex);
								clusters.get(path).add(adrIndex);
								clusterToStations2.get(path).add(new double[]{1,indices[j],matrixWalking[adrIndex][indices[j]]});
								count++;
								sign=false;
								break;
							}
						}
					}
					
					if(sign) break;
				}
				//System.out.println("count:"+count+", ac:"+ac);
			}
			
			double totalCost=0;
			for(int i=0;i<clusters.size();i++)
			{
				ArrayList<Integer> cluster=clusters.get(i);
				ArrayList<Integer> clusterPath=clusterStationsPath2.get(i);
				ArrayList<double[]> clusterToStation=clusterToStations2.get(i);
				
				double cost=evaluate(cluster, clusterPath, clusterToStation);
				this.clusterCosts.add(cost);
				totalCost+=cost;
			}
			if(totalCost<minx)
			{
				minx=totalCost;
				this.clusters=clusters;
				this.clusterToStations2=clusterToStations2;
				this.clusterCosts=clusterCosts;
			}
		
		}
		System.out.println("total1:"+minx/5.0/ac);
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
	
	protected void interAdjust()
	{
		final int NNN=2;
		
		int [][] adrPathMinAssigns = new int[ac][clusters.size()];
		for(int i=0;i<ac;i++)
		{
			for(int j=0;j<clusterStationsPath2.size();j++)
			{
				ArrayList<Integer> path=clusterStationsPath2.get(j);
				int minIndex=-1;
				for(int k=0;k<path.size();k++)
				{
					int station=path.get(k);
					if(minIndex==-1 || this.matrixWalking[i][station]<matrixWalking[i][minIndex])
						minIndex=station;
				}
				adrPathMinAssigns[i][j]=minIndex;
			}
		}
		
		for(int i=0;i<clusters.size();i++)
		{
			for(int j=i+1;j<clusters.size();j++)
			{
				ArrayList<Integer> cluster1=clusters.get(i);
				ArrayList<Integer> cluster2=clusters.get(j);
				ArrayList<Integer> clusterStation1=clusterStationsPath2.get(i);
				ArrayList<Integer> clusterStation2=clusterStationsPath2.get(j);
				ArrayList<double[]> clusterToStation1=clusterToStations2.get(i);
				ArrayList<double[]> clusterToStation2=clusterToStations2.get(j);
				
				for(int x=0;x<cluster1.size();x++)
				{
					for(int y=0;y<cluster2.size();y++)
					{
						int adr1=cluster1.get(x);
						int adr2=cluster2.get(y);
						
						if(weights.get(adr1)==weights.get(adr2))
						{
							if(matrixWalking[adr1][adrPathMinAssigns[adr1][j]]+matrixWalking[adr2][adrPathMinAssigns[adr2][i]]<
									matrixWalking[adr1][adrPathMinAssigns[adr1][i]]+matrixWalking[adr2][adrPathMinAssigns[adr2][j]])
							{
								cluster1.remove(x);
								cluster1.add(x, adr2);
								clusterToStation1.get(x)[1]=adrPathMinAssigns[adr2][i];
								clusterToStation1.get(x)[2]=matrixWalking[adr2][adrPathMinAssigns[adr2][i]];
								
								cluster2.remove(y);
								cluster2.add(y, adr1);
								clusterToStation2.get(y)[1]=adrPathMinAssigns[adr1][j];
								clusterToStation2.get(y)[2]=matrixWalking[adr1][adrPathMinAssigns[adr1][j]];
							}
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
			
			double cost=evaluate(cluster, clusterStationPath, clusterToStation);
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
		assign();
		interAdjust();
		output();
	}
	
	public void test()
	{
		
	}
	
	public static void main(String[] args) throws Exception
	{
		ProcessOriginal p=new ProcessOriginal();
		p.run("ALL");
		//p.run("JC");
		//p.run("JK");
		p.test();
	}
}
