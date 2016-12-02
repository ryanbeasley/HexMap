package HexGrid;
import java.util.*;
import java.io.*;
import java.text.DecimalFormat;

import simple.parser.*;
import simple.*;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author rthec
 */
public class HexGrid {
    public Hex[][] grid;
    public int numRows;
    public int numColumns;
    public double radius;
    public double apothem;
    public double maxHeight;
    public Vertex[][] vertices; 
    public ArrayList<Double> buffer;
    public ArrayList<Double> normBuffer;
    public ArrayList<Double> biomeBuffer;
    public ArrayList<Integer> indexBuffer;
    public ArrayList<Double> textureBuffer;
    public ArrayList<Double> noiseBuffer;
    public int index = 0;
    double mountainThresh;
    
    public HexGrid(int numColumns, int numRows, double radius, double maxHeight){
        this.grid = new Hex[numColumns][numRows];
        this.vertices = new Vertex[numColumns+1][2*numRows+2];
        this.numColumns = numColumns;
        this.numRows = numRows;
        this.radius = radius;
        this.apothem = (double) (Math.cos(Math.PI/6) * radius);
        this.maxHeight = maxHeight;
        this.mountainThresh = maxHeight * .55;
    }
    
    public void buildGrid(){
        for (int i = 0; i < numColumns; i++){
            for (int j = 0; j < numRows; j++){
                if ((i & 1) == 0)
                    grid[i][j] = new Hex(1.5*radius*i, 2*apothem*j, i, j, radius, apothem);
                else
                    grid[i][j] = new Hex(1.5*radius*i, apothem*(2*j + 1), i, j, radius, apothem);
            }
        }
        
        for (int i = 0; i < numColumns; i++){
            for (int j = 0; j < numRows; j++){
                Hex temp = grid[i][j];
                
                //Check these
                if ((i & 1) == 0){ 
                    addAdj(temp,i,j+1);
                    addAdj(temp,i+1,j);
                    addAdj(temp,i+1,j-1);
                    addAdj(temp,i,j-1);
                    addAdj(temp,i-1,j-1);
                    addAdj(temp,i-1,j);
                }
                else{
                    addAdj(temp,i,j+1);
                    addAdj(temp,i+1,j+1);
                    addAdj(temp,i+1,j);
                    addAdj(temp,i,j-1);
                    addAdj(temp,i-1,j);
                    addAdj(temp,i-1,j+1);
                }
                grid[i][j] = temp;
                grid[i][j].setCube(i-numColumns/2, j-numRows/2);
            }
        }
    }
    
    private void addAdj(Hex temp, int i, int j){
        if (i >= 0 && i < numColumns && j >= 0 && j < numRows){
            temp.adj.add(grid[i][j]);
        }
    }
    
    public void buildContinent(){
        boolean[][] visited = new boolean[numColumns][numRows];
        Stack<Hex> s = new Stack<>();
        int countLand = 0;        
        Hex center = grid[numColumns/2][numRows/2];
        double lakeHit;
        int numLakes = 0;
        boolean lake = true;
        int dist;
        
        s.push(center);
        
        while(!s.isEmpty()){
            countLand = 1;
            long seed = System.nanoTime();
            
            Hex temp = s.pop();
            visited[temp.i][temp.j] = true;
            Collections.shuffle(temp.adj, new Random(seed));
            
            for (int i = 0; i < temp.adj.size(); i++){
                if(!visited[temp.adj.get(i).i][temp.adj.get(i).j]){
                    s.add(temp.adj.get(i));
                }
                else {
                        //if land
                    if (temp.adj.get(i).biome[0] == 1.01 || temp.adj.get(i).biome[0] == 3.01){
                        countLand++;
                    }
                    //if water
                    else if (temp.adj.get(i).biome[0] == 2.01){
                        countLand--;
                    }
                }
            }
            
            dist = distance(temp, center);
            
            if (lake && dist > numRows / 2){
            	lake = false;
            }
            
            lakeHit = Math.random();
            double b;
            
            if (lakeHit < Math.pow(dist, 2) / (numRows * numColumns) && numLakes < 3 && lake){
            	numLakes++;
            	temp.biome[0] = 2.01;
            	temp.biome[1] = 2.01;
            	temp.biome[2] = 2.01;
            	generateLake(temp);
            }            
            else if (temp.biome[0] != 2.01){
            	if (countLand >= 0){
            		 b = (((Math.random() * 100 + Math.pow(countLand, 5)) / Math.pow((dist+1) * 20 / numRows, 2)) < 1) ? 2.01 : 1.01;
            	}
            	else {
            		 b = (((Math.random() * 100) / Math.pow((dist+1) * 20 / numRows, 2)) < 1) ? 2.01 : 1.01;
            	}
            	
                temp.biome[0] = b;
                temp.biome[1] = b;
                temp.biome[2] = b;
                    //if land
                if (temp.biome[0] == 1.01 && countLand < 0){
                    temp.biome[0] = 3.01; //temp is coast
                    temp.biome[1] = 3.01;
                    temp.biome[2] = 3.01;
                }
                else if (temp.biome[0] == 2.01 && countLand > 0){
                    for (int i = 0; i < temp.adj.size(); i++){
                        //if ith neighbor biome is land
                        if (temp.adj.get(i).biome[0] == 1.01){
                            //ith neighbor biome is coast
                            temp.adj.get(i).biome[0] = 3.01;
                            temp.adj.get(i).biome[0] = 3.01;
                            temp.adj.get(i).biome[0] = 3.01;
                        }
                    }
                }
            }
            
        }
        
        s = null;
        visited = null;
    }
    
    private void generateLake(Hex center){
    	boolean[][] visited = new boolean[numColumns][numRows];
        Stack<Hex> s = new Stack<>();
        int dist;
        
        
        s.push(center);
        
        while(!s.isEmpty()){
        	int numWater = 0;
            Hex temp = s.pop();
            visited[temp.i][temp.j] = true;
            
            dist = distance(temp, center);
            
            for (int i = 0; i < temp.adj.size(); i++){
                if(!visited[temp.adj.get(i).i][temp.adj.get(i).j] && dist < numRows / 10){
                    s.add(temp.adj.get(i));
                }                
                
                if (temp.adj.get(i).biome[0] == 2.01){
                	numWater++;
                }
            }
            
            if (dist < 2){
            	temp.biome[0] = 2.01;
            	temp.biome[1] = 2.01;
            	temp.biome[2] = 2.01;
            }
            else if (numWater != 0){
            	if (1 / Math.pow(dist, .5) > Math.random()){
            		temp.biome[0] = 2.01;
                	temp.biome[1] = 2.01;
                	temp.biome[2] = 2.01;
            	}
            }
        }
        
        s = null;
        visited = null;
    }
    public void setHeights(){
        boolean[][] visited = new boolean[numColumns][numRows];
        Stack<Hex> s = new Stack<>();
        Hex center = grid[numColumns/2][numRows/2];
        int count = 0;
        int scount = 1;
        int numMount = 0;
        s.push(center);
        
        while(!s.isEmpty()){
        	int adjMount = 0;
            
            long seed = System.nanoTime();
            
            count++;
            if (count == 6*scount){
            	count = 0;
            	numMount = 0;
            	scount++;
            }
            
            Hex temp = s.pop();
            visited[temp.i][temp.j] = true;
            Collections.shuffle(temp.adj, new Random(seed));
            
            for (int i = 0; i < temp.adj.size(); i++){
                if(!visited[temp.adj.get(i).i][temp.adj.get(i).j]){
                    s.push(temp.adj.get(i));
                }
                if (temp.adj.get(i).z > mountainThresh){
                	adjMount++;
                }
                if (temp.adj.get(i).biome[0] == 2.01 || temp.adj.get(i).biome[0] == 3.01){
            		adjMount = -6;		
            	}
            }
            
            //if temp is coast or water
            if (temp.biome[0] == 3.01){
                temp.z = maxHeight / 100;
            }
            else if(temp.biome[0] == 2.01){
                temp.z = 0;
            }
            else{
            	if (numMount > 6 && numMount > scount / 6){
            		temp.z = maxHeight * Math.random() / Math.pow(distance(temp, center)+1, 2);
            	}
            	else {
            		temp.z = maxHeight * Math.random() / Math.pow(distance(temp, center)+1, .25);
            	}
                
                if (temp.z > mountainThresh){
                	numMount++;
                }
            }
            
            if (adjMount < 3 && adjMount > 0 && temp.z < mountainThresh){
	        	double rando = Math.random() / Math.pow(numMount+1, .2);
	        	if (rando > .2){
	        		numMount++;
	        		temp.z = mountainThresh + rando * maxHeight / 10;
	        	}
	        }
            
            temp.z = Math.pow(temp.z / mountainThresh, 3.0) * mountainThresh;
        }
        
        s = null;
        visited = null;
    }
    
    public void setMountains(){
    	boolean[][] visited = new boolean[numColumns][numRows];
        Stack<Hex> s = new Stack<>();
        Hex center = grid[numColumns/2][numRows/2];
        ArrayList<Integer> index = new ArrayList<>();
        
        s.push(center);
        
        while(!s.isEmpty()){
        	int count = 0;
        	int range = 1;
        	long seed = System.nanoTime();
        	
        	Hex temp = s.pop();
	        visited[temp.i][temp.j] = true;
	        
	        Collections.shuffle(temp.adj, new Random(seed));
	        
	        for (int i = 0; i < temp.adj.size(); i++){
	        	if(!visited[temp.adj.get(i).i][temp.adj.get(i).j]){
                    s.push(temp.adj.get(i));
                }
	        	
	        }
	        
	        setMountain(temp);        
	        
        }
	        
        s = null;
        visited = null;
    }
    
    private void setMountain(Hex h){
    	if (h.z > mountainThresh){
            h.biome[0] = 4.01; // mountains
            h.biome[1] = 4.01;
            h.biome[2] = 4.01;
        }
    }
    
    public void assignVertices(){
        for (int i = 0; i < numColumns+1; i++){
            for (int j = 0; j < 2*numRows+2; j++){
                vertices[i][j] = new Vertex();
            }
        }
        
        for (int i = 0; i < numColumns+1; i++){
            for (int j = 0; j < 2*numRows+2; j++){
                //if i is even and j is even
                if((i & 1) == 0 && (j & 1) == 0){
                    if(addHex(vertices[i][j], i, j/2))
                        grid[i][j/2].vertices[0] = vertices[i][j];
                    //add to grid.verticees index 0
                    if(addHex(vertices[i][j], i-1, j/2-1))
                        grid[i-1][j/2-1].vertices[2] = vertices[i][j];
                    //add to grid.verticees index 2
                    if(addHex(vertices[i][j], i, j/2-1))
                        grid[i][j/2-1].vertices[4] = vertices[i][j];
                    //add to grid.verticees index 4
                }
                
                //if i is odd and j is even
                else if((i & 1) == 1 && (j & 1) == 0){
                    if(addHex(vertices[i][j], i-1, j/2))
                        grid[i-1][j/2].vertices[1] = vertices[i][j];
                    //add to grid.verticees index 1
                    if(addHex(vertices[i][j], i-1, j/2-1))
                        grid[i-1][j/2-1].vertices[3] = vertices[i][j];
                    //add to grid.verticees index 3
                    if(addHex(vertices[i][j], i, j/2-1))
                        grid[i][j/2-1].vertices[5] = vertices[i][j];
                    //add to grid.verticees index 5
                }
                
                //if i is even and j is odd
                else if((i & 1) == 0 && (j & 1) == 1){
                    if(addHex(vertices[i][j], i-1, (j-1)/2))
                        grid[i-1][(j-1)/2].vertices[1] = vertices[i][j];
                    //add to grid.verticees index 1
                    if(addHex(vertices[i][j], i-1, (j-1)/2-1))
                        grid[i-1][(j-1)/2-1].vertices[3] = vertices[i][j];
                    //add to grid.verticees index 3
                    if(addHex(vertices[i][j], i, (j-1)/2))
                        grid[i][(j-1)/2].vertices[5] = vertices[i][j];
                    //add to grid.verticees index 5
                }
                
                //if i is odd and j is odd
                else if((i & 1) == 1 && (j & 1) == 1){
                    if(addHex(vertices[i][j], i, (j-1)/2))
                        grid[i][(j-1)/2].vertices[0] = vertices[i][j];
                    //add to grid.verticees index 0
                    if(addHex(vertices[i][j], i-1, (j-1)/2))
                        grid[i-1][(j-1)/2].vertices[2] = vertices[i][j];
                    //add to grid.verticees index 2
                    if(addHex(vertices[i][j], i, (j-1)/2-1))
                        grid[i][(j-1)/2-1].vertices[4] = vertices[i][j];
                    //add to grid.verticees index 4
                }

                vertices[i][j].computeCoord();
            }
        }
    }
    
    public void assignCentroids(){
    	for (int i = 0; i < numColumns; i++){
    		for (int j = 0; j < numRows; j++){
    			for (int k = 0; k < 6; k++){
    				Hex h = grid[i][j];
    				Vertex temp = new Vertex();
    				Vertex v1 = h.vertices[k%6];
    				Vertex v2 = h.vertices[(k+1)%6];
    				
    				temp.x = (v1.x + v2.x + h.x) / 3.0;
    				temp.y = (v1.y + v2.y + h.y) / 3.0;
    				temp.z = (v1.z + v2.z + h.z) / 3.0;
    				
    				grid[i][j].centroids[k] = temp;
    			}
    		}
    	}
    }
    
    public void centerPlane(){
    	double centerX;
    	double centerY;
    	
    	centerX = grid[numColumns/2][numRows/2].x;
    	centerY = grid[numColumns/2][numRows/2].y;
    	
    	for (int i = 0; i < numColumns; i++){
    		for (int j = 0; j < numRows; j++){
    			Hex h = grid[i][j];
    			h.x = h.x - centerX;
    			h.y = h.y - centerY;
    			for (int k = 0; k < 6; k++){
    				/*
    				Vertex c = h.centroids[k];
    				c.x = c.x - centerX;
    				c.y = c.y - centerY;
    				*/
    			}
    		}
    	}
    	
    	for (int i = 0; i < numColumns + 1; i++){
    		for (int j = 0; j < 2*numRows+2; j++){
    			Vertex v1 = vertices[i][j];
    			v1.x = v1.x - centerX;
    			v1.y = v1.y - centerY;
    		}
    	}
    }
    
    private boolean addHex(Vertex v, int i, int j){
        if (i >= 0 && i < numColumns && j >= 0 && j < numRows){
            v.adjHexes.add(grid[i][j]);
            return true;
        }
        return false;
    }
    
    private int distance(Hex a, Hex b){
        return (Math.abs(a.r-b.r) + Math.abs(a.s-b.s) + Math.abs(a.t-b.t)) / 2;
    }
    
    public void computeNormals(){
        for (int i = 0; i < numColumns; i++){
            for (int j = 0; j < numRows; j++){
                Hex center = grid[i][j];
                Vector3D normal = new Vector3D();
                for (int k = 0; k < 6; k++){
                    Vertex v1 = center.vertices[k%6];
                    Vertex v2 = center.vertices[(k+1)%6];                   
                    
                    normal = Vector3D.add(normal, Vector3D.computeNormal(center, v1, v2));
                }
                normal = Vector3D.normalize(normal);
                center.normal = normal;
            }
        }
        
        for (int i = 0; i < numColumns+1; i++){
            for (int j = 0; j < 2*numRows+2; j++){
                Vector3D normal = new Vector3D();
                Vertex v = vertices[i][j];
                
                for (int k = 0; k < v.adjHexes.size(); k++){
                    normal = Vector3D.add(v.adjHexes.get(k).normal, normal);
                }
                
                v.normal = Vector3D.normalize(normal);
            }
        }
    }
    
    public void createBuffers(){
        buffer = new ArrayList<>();
        normBuffer = new ArrayList<>();
        biomeBuffer = new ArrayList<>();
        indexBuffer = new ArrayList<>();
        textureBuffer = new ArrayList<>();
        noiseBuffer = new ArrayList<>();
        
        for (int i = 0; i < numColumns; i++){
            for (int j = 0; j < numRows; j++){
                Hex center = grid[i][j];
                
                buffer.add(center.x);
                buffer.add(center.y);
                buffer.add(center.z);
                
                biomeBuffer.add(center.biome[0]);
                biomeBuffer.add(center.biome[1]);
                biomeBuffer.add(center.biome[2]);

                normBuffer.add(center.normal.x);
                normBuffer.add(center.normal.y);
                normBuffer.add(center.normal.z);
                
                noiseBuffer.add((Math.random()+7)/8);
                
                textureBuffer.add(0.5);
                textureBuffer.add(0.5);
                
                for (int k = 0; k < 6; k++){         
                    Vertex v1 = center.vertices[k%6];
                    
                    
                    for (int b = 0; b < v1.adjHexes.size(); b++){
                        biomeBuffer.add(v1.adjHexes.get(b).biome[0]);
                    }
                    if (v1.adjHexes.size() == 1)
                        biomeBuffer.add(center.biome[0]);
                    if (v1.adjHexes.size() <=2)
                        biomeBuffer.add(center.biome[0]);
                    
                    buffer.add(v1.x);
                    buffer.add(v1.y);
                    buffer.add(v1.z);
                    
                    normBuffer.add(v1.normal.x);
                    normBuffer.add(v1.normal.y);
                    normBuffer.add(v1.normal.z);
                    
                    double s = .5 * (1 + Math.cos((4+k)*Math.PI/3));
                    double t = .5 * (1 + Math.cos((4+k)*Math.PI/3));
                    textureBuffer.add(s);
                    textureBuffer.add(t);
                    
                    noiseBuffer.add((Math.random()+7)/8);
                    
                    //build a triangle                   
                    
                    indexBuffer.add(i*numRows*7+j*7);
                    indexBuffer.add(i*numRows*7+j*7+k%6+1);
                    indexBuffer.add(i*numRows*7+j*7+(k+1)%6+1);
                }
            }                   
        }
        
        grid = null;
        vertices = null;       
        
    }
    
    public void createBuffersWithCentroids(){
    	
    }
    
    public String writeToJson() throws IOException{
        JSONObject obj = new JSONObject();
        JSONArray list = new JSONArray();
        JSONArray nList = new JSONArray();
        JSONArray bioList = new JSONArray();
        JSONArray iList = new JSONArray();
        JSONArray textList = new JSONArray();
        JSONArray noiseList = new JSONArray();
        
        double temp;
        int holder;
        
        for(int i = 0; i < buffer.size(); i++){
        	temp = buffer.get(i);
        	temp = temp*10000;
        	holder = (int) temp;
        	temp = (double) holder;
        	temp = temp/10000;
            list.add(temp);                       
        }
        for (int i = 0; i < normBuffer.size(); i++){
        	temp = normBuffer.get(i);
        	temp = temp*10000;
        	holder = (int) temp;
        	temp = (double) holder;
        	temp = temp/10000;
            nList.add(temp); 
        }
        for (int i = 0; i < biomeBuffer.size(); i++){
        	temp = biomeBuffer.get(i);
        	temp = temp*10000;
        	holder = (int) temp;
        	temp = (double) holder;
        	temp = temp/10000;
            bioList.add(temp); 
        }
        for (int i = 0; i < noiseBuffer.size(); i++){
        	temp = noiseBuffer.get(i);
        	temp = temp*10000;
        	holder = (int) temp;
        	temp = (double) holder;
        	temp = temp/10000;
            noiseList.add(temp); 
        }
        
        for (int i = 0; i < indexBuffer.size(); i++){
            iList.add(indexBuffer.get(i)); 
        }
        for (int i = 0; i < textureBuffer.size(); i++){
        	temp = textureBuffer.get(i);
        	temp = temp*10000;
        	holder = (int) temp;
        	temp = (double) holder;
        	temp = temp/10000;
            textList.add(temp); 
        }
        obj.put("vertexPositions", list);
        obj.put("vertexNormals", nList);
        obj.put("biomeTypes", bioList);
        obj.put("indices", iList);
        obj.put("textCoord", textList);
        obj.put("noise", noiseList);
        
        File fp = new File("D:\\Dev\\apache-tomcat-9.0.0.M13\\wtpwebapps\\HexMap\\test.json");
        
        FileWriter file = new FileWriter(fp);
        file.write(obj.toJSONString());
        file.flush();
        file.close();
        
        return "hello";
        
    }
    
    
}
