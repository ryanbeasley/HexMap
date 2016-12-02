package HexGrid;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author rthec
 */
public class Vector3D {
    public double x;
    public double y;
    public double z;
    
    public Vector3D(){
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }
    
    public Vector3D(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public static Vector3D computeNormal(Hex v1, Vertex v2, Vertex v3){
        Vector3D u, v, out;
        u = new Vector3D();
        v = new Vector3D();
        out = new Vector3D();
        
        u.x = v2.x - v1.x;
        u.y = v2.y - v1.y;
        u.z = v2.z - v1.z;
        
        v.x = v3.x - v1.x;
        v.y = v3.y - v1.y;
        v.z = v3.z - v1.z;
        
        out.x = u.y*v.z - u.z*v.y;
        out.y = -(u.x*v.z - u.z*v.x);
        out.z = u.x*v.y-u.y*v.x;
        
        if (out.z < 0){
            out.x = -out.x;
            out.y = -out.y;
            out.z = -out.z;
        }
        return out;
    }
    
    public static Vector3D add(Vector3D v1, Vector3D v2){
        Vector3D out = new Vector3D();
        
        out.x = v1.x + v2.x;
        out.y = v1.y + v2.y;
        out.z = v1.z + v2.z;
        
        return out;
    }
    
    public static Vector3D normalize(Vector3D v){
        Vector3D out = new Vector3D(v.x, v.y, v.z);
        double r = 1/Math.sqrt(v.x*v.x + v.y*v.y + v.z*v.z);
        
        out.x *= r;
        out.y *= r;
        out.z *= r;
        
        return out;
    }
}
