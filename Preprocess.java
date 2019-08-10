package svrp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Scanner;

public class Preprocess 
{
	public void mergeAdrs() throws Exception
	{
		String [][] nameAdrs={{"金城-1"},{"金肯-1"}};
		String [] path_Names={};
		for(int i=0;i<path_Names.length;i++)
		{
			PrintWriter pw=new PrintWriter(new FileOutputStream(path_Names[i]));
			for(int j=0;j<nameAdrs[i].length;j++)
			{
				Scanner sc=new Scanner(new FileInputStream(Common.PATH_HOME+"/"+nameAdrs[i][j]+"-out.txt"));
				
				while(sc.hasNextLine())
				{
					String line=sc.nextLine().trim();
					if(!line.equals(""))
						pw.println(line);
				}
				sc.close();
			}
			pw.close();
		}
	}
	
	public void mergeMatrix() throws Exception
	{
		String [] nameMatrix={"金城-1=金城-1=driving", "金城-1=金肯-1=driving", "金肯-1=金肯-1=driving"};
		PrintWriter pw=new PrintWriter(new FileOutputStream(Common.PATH_MATRIX_DRIVING));
		for(int i=0;i<nameMatrix.length;i++)
		{
			File dir=new File(Common.PATH_HOME+"/"+nameMatrix[i]);
			File []files=dir.listFiles();
			for(int j=0;j<files.length;j++)
			{
				Scanner sc=new Scanner(new FileInputStream(files[j]));
				while(sc.hasNextLine())
				{
					String line=sc.nextLine();
					if(! line.trim().equals(""))
						pw.println(line);
				}
				sc.close();
			}
		}
		pw.close();
	}
	
	public void splitJCJK() throws Exception
	{
		Scanner sc;
		PrintWriter pw1,pw2;
		
		Hashtable<String,Integer> htStationsJC=new Hashtable<String,Integer>();
		sc=new Scanner(new FileInputStream(Common.PATH_ALL_STATIONS_JC));
		while(sc.hasNextLine())
		{
			String s=sc.nextLine().split("\t")[0];
			if(htStationsJC.get(s)==null)
				htStationsJC.put(s, 0);
			htStationsJC.put(s,htStationsJC.get(s)+1);
		}
		sc.close();
		
		Hashtable<String,Integer> htStationsJK=new Hashtable<String,Integer>();
		sc=new Scanner(new FileInputStream(Common.PATH_ALL_STATIONS_JK));
		while(sc.hasNextLine())
		{
			String s=sc.nextLine().split("\t")[0];
			if(htStationsJK.get(s)==null)
				htStationsJK.put(s, 0);
			htStationsJK.put(s,htStationsJK.get(s)+1);
		}
		sc.close();
		
		sc=new Scanner(new FileInputStream(Common.PATH_JC));
		Hashtable<String,Integer> htJC=new Hashtable<String,Integer>();
		while(sc.hasNextLine())
		{
			String s=sc.nextLine();
			if(htJC.get(s)==null)
				htJC.put(s, 0);
			htJC.put(s, htJC.get(s)+1);
		}
		sc.close();
		
		sc=new Scanner(new FileInputStream(Common.PATH_JK));
		Hashtable<String,Integer> htJK=new Hashtable<String,Integer>();
		while(sc.hasNextLine())
		{
			String s=sc.nextLine();
			if(htJK.get(s)==null)
				htJK.put(s, 0);
			htJK.put(s, htJK.get(s)+1);
		}
		sc.close();
		
		sc=new Scanner(new FileInputStream(Common.PATH_ALL_ADDRESS));
		pw1=new PrintWriter(new FileOutputStream(Common.PATH_ALL_ADDRESS_JC));
		pw2=new PrintWriter(new FileOutputStream(Common.PATH_ALL_ADDRESS_JK));
		while(sc.hasNextLine())
		{
			String line=sc.nextLine();
			String[] items=line.split("\t");
			String[] items0=items[0].split(":");
			if(htJC.get(items0[0])!=null && htJC.get(items0[0])>0)
			{
				htJC.put(items0[0], htJC.get(items0[0])-1);
				pw1.println(line);
			}
			else if(htJK.get(items0[0])!=null && htJK.get(items0[0])>0)
			{
				htJK.put(items0[0], htJK.get(items0[0])-1);
				pw2.println(line);
			}
		}
		pw2.close();
		pw1.close();
		sc.close();
		
		sc=new Scanner(new FileInputStream(Common.PATH_MATRIX_DRIVING));
		pw1=new PrintWriter(new FileOutputStream(Common.PATH_MATRIX_DRIVING_JC));
		pw2=new PrintWriter(new FileOutputStream(Common.PATH_MATRIX_DRIVING_JK));
		while(sc.hasNextLine())
		{
			String line=sc.nextLine();
			String[] items=line.split("\t");
			if((htJC.get(items[0])!=null || htStationsJC.get(items[0])!=null) && (htJC.get(items[1])!=null || htStationsJC.get(items[1])!=null))
			{
				pw1.println(line);
			}
			if((htJK.get(items[0])!=null || htStationsJK.get(items[0])!=null) && (htJK.get(items[1])!=null || htStationsJK.get(items[1])!=null))
			{
				pw2.println(line);
			}
		}
		pw2.close();
		pw1.close();
		sc.close();
		
		sc=new Scanner(new FileInputStream(Common.PATH_MATRIX_WALKING));
		pw1=new PrintWriter(new FileOutputStream(Common.PATH_MATRIX_WALKING_JC));
		pw2=new PrintWriter(new FileOutputStream(Common.PATH_MATRIX_WALKING_JK));
		while(sc.hasNextLine())
		{
			String line=sc.nextLine();
			String[] items=line.split("\t");
			if((htJC.get(items[0])!=null || htStationsJC.get(items[0])!=null) && (htJC.get(items[1])!=null || htStationsJC.get(items[1])!=null))
			{
				pw1.println(line);
			}
			if((htJK.get(items[0])!=null || htStationsJK.get(items[0])!=null) && (htJK.get(items[1])!=null || htStationsJK.get(items[1])!=null))
			{
				pw2.println(line);
			}
		}
		pw2.close();
		pw1.close();
		sc.close();
		
		sc=new Scanner(new FileInputStream(Common.PATH_MATRIX_DRIVING_NO_METRO));
		pw1=new PrintWriter(new FileOutputStream(Common.PATH_MATRIX_DRIVING_NO_METRO_JC));
		pw2=new PrintWriter(new FileOutputStream(Common.PATH_MATRIX_DRIVING_NO_METRO_JK));
		while(sc.hasNextLine())
		{
			String line=sc.nextLine();
			String[] items=line.split("\t");
			if((htJC.get(items[0])!=null || htStationsJC.get(items[0])!=null) && (htJC.get(items[1])!=null || htStationsJC.get(items[1])!=null))
			{
				pw1.println(line);
			}
			if((htJK.get(items[0])!=null || htStationsJK.get(items[0])!=null) && (htJK.get(items[1])!=null || htStationsJK.get(items[1])!=null))
			{
				pw2.println(line);
			}
		}
		pw2.close();
		pw1.close();
		sc.close();
		
		sc=new Scanner(new FileInputStream(Common.PATH_MATRIX_WALKING_NO_METRO));
		pw1=new PrintWriter(new FileOutputStream(Common.PATH_MATRIX_WALKING_NO_METRO_JC));
		pw2=new PrintWriter(new FileOutputStream(Common.PATH_MATRIX_WALKING_NO_METRO_JK));
		while(sc.hasNextLine())
		{
			String line=sc.nextLine();
			String[] items=line.split("\t");
			if((htJC.get(items[0])!=null || htStationsJC.get(items[0])!=null) && (htJC.get(items[1])!=null || htStationsJC.get(items[1])!=null))
			{
				pw1.println(line);
			}
			if((htJK.get(items[0])!=null || htStationsJK.get(items[0])!=null) && (htJK.get(items[1])!=null || htStationsJK.get(items[1])!=null))
			{
				pw2.println(line);
			}
		}
		pw2.close();
		pw1.close();
		sc.close();
	}
	
	public void statistics() throws Exception
	{
		Scanner sc;
		
		int c0=0, c1=0;
		sc=new Scanner(new FileInputStream(Common.PATH_ALL_ADDRESS_JC));
		while(sc.hasNextLine())
		{
			String line=sc.nextLine();
			String[] items=line.split("\t");
			String[] items0=items[0].split(":");
			
			if(items0[1].equals("0"))
				c0++;
			else
				c1++;
		}
		sc.close();
		
		c0=0;
		c1=0;
		sc=new Scanner(new FileInputStream(Common.PATH_ALL_ADDRESS_JK));
		while(sc.hasNextLine())
		{
			String line=sc.nextLine();
			String[] items=line.split("\t");
			String[] items0=items[0].split(":");
			
			if(items0[1].equals("0"))
				c0++;
			else
				c1++;
		}
		sc.close();
	}
	
	public static void main(String args[]) throws Exception
	{
		Preprocess pp=new Preprocess();
		///pp.mergeMatrix();
		//pp.mergeAdrs();
		pp.splitJCJK();
		//pp.statistics();
	}
}
