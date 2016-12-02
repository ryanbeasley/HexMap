package HexGrid;

import java.io.IOException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author rthec
 */
public class Controller{
    public static String createHexMesh() throws IOException{
    	HexGrid obj = new HexGrid(51, 51, 2, 7);
        obj.buildGrid();
        obj.buildContinent();
        obj.setHeights();
        obj.setMountains();
        obj.assignVertices();
        //obj.centerPlane();
        obj.computeNormals();
        obj.createBuffers();
        return obj.writeToJson();
        
    }
}
