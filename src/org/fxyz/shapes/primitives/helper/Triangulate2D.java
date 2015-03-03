/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.fxyz.shapes.primitives.helper;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.geometry.Point2D;
import org.fxyz.geometry.Point3D;

/**
 *
 * @author José Pereda Llamas
 * Created on 21-nov-2014 - 15:37:25
 */
public class Triangulate2D {
    
    private static final int MAXPOLY = 1200;
    private static final double EPSILON	= 0.00001;

    private final List<Point2D> points;
    private final int size;
    
    private int n;
    private final int[][] triangulation = new int[MAXPOLY][3];
    private final List<Integer> duplicates;
    
    public Triangulate2D(List<Point3D> points3D){
        this.points=points3D.stream().map(p->new Point2D(p.x,p.y)).collect(Collectors.toList());
        
        //check for duplicates or too close
        duplicates=IntStream.range(0, points.size()).boxed()
            .filter(i->points.get(i).distance(points.get(i==points.size()-1?0:i+1))<100*EPSILON)
            .map(i->i==points.size()-1?i:i+1).collect(Collectors.toList());
        duplicates.forEach(i->points.remove(i.intValue()));

        // close polygon
        points.add(points.get(0));
        
        double maxX = points.stream().mapToDouble(p->p.getX()).max().orElse(0d);
        double maxY = points.stream().mapToDouble(p->p.getY()).max().orElse(0d);
        double minX = points.stream().mapToDouble(p->p.getX()).min().orElse(0d);
        double minY = points.stream().mapToDouble(p->p.getY()).min().orElse(0d);

        // middle point of polygon
        Point2D mid = new Point2D((maxX+minX)/2d,(maxY+minY)/2d);
        points.add(mid);
            
        this.size=this.points.size();
//        points.forEach(System.out::println);
        
    }
    
    private class Triangle {
        private final Point2D a;
        private final Point2D b;
        private final Point2D c;
    
        public Triangle(Point2D a, Point2D b, Point2D c){
            this.a=a; this.b=b; this.c=c;
        }
        public double getSignedArea(){
            return ((a.getX()*b.getY()-a.getY()*b.getX()+
                     a.getY()*c.getX()-a.getX()*c.getY()+
                     b.getX()*c.getY()-c.getX()*b.getY())/2.0);
        }
        
        public double getArea() { return Math.abs(getSignedArea()); }
        
        public boolean cw(){
            return getSignedArea()<-EPSILON;
        }
        
        public boolean hasPointIn(Point2D p){
            if(new Triangle(a, b, p).cw()) return false;
            if(new Triangle(b, c, p).cw()) return false;
            return !new Triangle(c, a, p).cw();
        }
    }
    
    public boolean ear_Q(int a, int b, int c){
        Point2D pa=points.get(a), pb=points.get(b), pc=points.get(c);
        Triangle t=new Triangle(pa,pb,pc);
        if(t.cw()) return false;
        return points.stream()
                .filter(m->!m.equals(pa) && !m.equals(pb) && !m.equals(pc))
                .noneMatch(m->t.hasPointIn(m));
    }
    
    public void triangulate(){
        int l[] = new int[MAXPOLY];
        int r[] = new int[MAXPOLY];
        for(int i=0; i<size; i++){
            l[i] = (i-1+size)%size;
            r[i] = (i+1+size)%size;
        }
        
        n=0;
        int i = size-1;
        while(n < size - 2){
            i = r[i];
            if(ear_Q(l[i],i,r[i])){
                triangulation[n][0] = l[i];
                triangulation[n][1] = i;
                triangulation[n][2] = r[i];
                n++;
                l[r[i]] = l[i];
                r[l[i]] = r[i];
            }
        }
    }
    
    public double check(){
        double a0= getAreaPolygon();
        double a1= getAreaTriangulation();
//        System.out.println("a0: "+a0+", a1: "+a1+" , n: "+n);
        return a0-a1;
    }
    private double getAreaPolygon(){
        return IntStream.range(0, size).boxed().mapToDouble(i->{
            int j = (i+1)%size;
            return points.get(i).getX()*points.get(j).getY() - 
                    points.get(j).getX()*points.get(i).getY();
        }).sum()/2d;
    }

    private double getAreaTriangulation(){
        return IntStream.range(0, n).boxed().mapToDouble(i->{
            Triangle t= new Triangle(points.get(triangulation[i][0]),
                    points.get(triangulation[i][1]),points.get(triangulation[i][2]));
            return t.getArea();
        }).sum();
    }

    public int getSize() { return n; }
    
    public int[][] getTriangulation() { return triangulation; }
    
    public List<Point2D> getPoints() { return points; }
        
}
