/*
 * Copyright (C) 2014 F(Y)zx :
 * Authored by : Jason Pollastrini aka jdub1581, 
 * All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fxyz.utils;

import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import javafx.geometry.Point3D;
import javafx.scene.shape.TriangleMesh;

/**
 *  Just Testing the viability of creating a stream package for TriangleMeshs
 * @author Jason Pollastrini aka jdub1581
 */
public class MeshStreamer{
    private TriangleMesh mesh;
    private MeshStreamer(){}
    
    public MeshStreamer(TriangleMesh mesh){
        this.mesh = mesh;
    }
    /*
        Streams used for returning values
    */    
    private DoubleStream pointValueStream(TriangleMesh mesh){
        return IntStream.range(0, mesh.getPoints().size())
                //.peek(System.out::println)
                .mapToDouble((i) -> mesh.getPoints().get(i));
    }
    private DoubleStream texCoordValueStream(TriangleMesh mesh){
        return IntStream.range(0, mesh.getTexCoords().size())
                //.peek(System.out::println)
                .mapToDouble((i) -> mesh.getTexCoords().get(i));
    }
    private IntStream faceValueStream(TriangleMesh mesh){
        return IntStream.range(0, mesh.getPoints().size())
                //.peek(System.out::println)
                .map((i) -> mesh.getFaces().get(i));
    }
    
    public List<Point3D> getPointsAsList(){
        return null;
    }
    
    public List<Point3D> getTexCoordsAsList(){
        return null;
    }
    
    public List<Point3D> getFacesAsList(){
        return null;
    }
}
