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
public class Vertex {
    //Physical location
    double x;
    double y;
    double z;
    
    public int index;
    ArrayList<Hex> adjHexes = new ArrayList<>();
    Vector3D normal;
    
    public void computeCoord(){
        double sumx = 0;
        double sumy = 0;
        double sumz = 0;
        int size = adjHexes.size();
        
        for (int i = 0; i < size; i++){
            sumx += adjHexes.get(i).x;
            sumy += adjHexes.get(i).y;
            sumz += adjHexes.get(i).z;            
        }
        if (size == 3){
            this.x = sumx / 3;
            this.y = sumy / 3;
        }
        /*
        else if (size == 2){
            double x, y, xp, yp;
            x = adjHexes.get(0).x - adjHexes.get(1).x;
            y = adjHexes.get(0).y - adjHexes.get(1).y;
            
            xp = (double)(x*Math.cos(Math.PI/3) - y*Math.cos(Math.PI/3));
            yp = (double)(y*Math.cos(Math.PI/3) + x*Math.cos(Math.PI/3));
            
            sumx += xp;
            sumy += yp;
            
            this.x = sumx / 3;
            this.y = sumy / 3;
        }
        */
        else if (size <= 2 && size > 0){
            Hex hex = adjHexes.get(0);
            int i;
            for (i = 0; i < 6; i++){
                if (this.equals(hex.vertices[i])){
                    break;
                }
            }
            if (i == 6)
                System.out.println("Could not compute singleton vertex coord");
            else{
                double r = hex.radius;
                double dx = (double)(r*Math.cos(Math.PI/3));
                double dy = (double)(r*Math.sin(Math.PI/3));
                
                switch(i){
                    case 0:
                        this.x = hex.x - dx;
                        this.y = hex.y - dy;
                        break;
                    case 1:
                        this.x = hex.x + dx;
                        this.y = hex.y - dy;
                        break;
                    case 2:
                        this.x = hex.x + r;
                        this.y = hex.y;
                        break;
                    case 3:
                        this.x = hex.x + dx;
                        this.y = hex.y + dy;
                        break;
                    case 4:
                        this.x = hex.x - dx;
                        this.y = hex.y + dy;
                        break;
                    case 5:
                        this.x = hex.x - r;
                        this.y = hex.y;
                        break;
                }
            }
        }
        this.z = sumz / adjHexes.size();
    }
}
