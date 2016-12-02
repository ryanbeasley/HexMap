package HexGrid;

import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author rthec
 */
public class Hex {
    public double radius;
    public double apothem;
    //Physical location
    public double x;
    public double y;
    public double z;
    
    //Location in off-set grid
    public int i;
    public int j;
    
    public int index;
    //Cube coordinates from center
    public int r;
    public int s;
    public int t;
    
    public double[] biome = new double[3];
    
    public ArrayList<Hex> adj = new ArrayList<>();
    public Vertex[] vertices = new Vertex[6];
    public Vertex[] centroids = new Vertex[6];
    public Vector3D normal;
    
    public Hex(double x, double y, int i, int j, double radius, double apothem){
        this.x = x;
        this.y = y;
        this.i = i;
        this.j = j;
        this.radius = radius;
        this.apothem = apothem;
        
    }

    public void setCube(int x, int y){
        r = x;
        t = y - (r - (r & 1))/2;
        s = -r - t;
    }
}
