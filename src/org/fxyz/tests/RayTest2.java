/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.fxyz.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import org.fxyz.cameras.CameraTransformer;
import org.fxyz.shapes.composites.PolyLine3D;
import org.fxyz.shapes.primitives.SpheroidMesh;

/**
 *
 * @author José Pereda Llamas
 * Created on 22-dic-2014 - 10:12:25
 */
public class RayTest2 extends Application {

    private final List<Integer> intersectedFaces = new ArrayList<>();

    class Ray {

        private final Point3D origin, direction;

        public Ray(Point3D origin, Point3D direction) {
            this.origin = origin;
            this.direction = direction;
        }

        public boolean intersects(int face, Point3D v0, Point3D v1, Point3D v2) {
           
            Point3D diff = origin.subtract(v0);
            Point3D edge1 = v1.subtract(v0);
            Point3D edge2 = v2.subtract(v0);
            Point3D norm = edge1.crossProduct(edge2);

            double dirDotNorm = direction.dotProduct(norm);
            double sign;
            if (dirDotNorm > 0.0000001) {
                sign = 1;
            } else if (dirDotNorm < -0.0000001) {
                sign = -1;
                dirDotNorm = -dirDotNorm;
            } else {
                return false;
            }

            double dirDotDiffxEdge2 = sign * direction.dotProduct(diff.crossProduct(edge2));
            if (dirDotDiffxEdge2 >= 0.0) {

                double dirDotEdge1xDiff = sign * direction.dotProduct(edge1.crossProduct(diff));

                if (dirDotEdge1xDiff >= 0.0) {
                    if (dirDotEdge1xDiff <= dirDotNorm) {
                        double diffDotNorm = -sign * diff.dotProduct(norm);
                        if (diffDotNorm >= 0.0) {
                            intersectedFaces.add(face);
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }
////////////////////////////////////////////////////////////////////////////////
    private PerspectiveCamera camera;
    private final CameraTransformer cameraTransform = new CameraTransformer();
    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    private double mouseDeltaX;
    private double mouseDeltaY;
    private PolyLine3D visual;
    private SpheroidMesh target;
    private Sphere origin;
    private Color highlight = Color.CHARTREUSE;
    private WritableImage img;
    private PhongMaterial hMat;
    private Group root3D = new Group();
    //==========================================================================

    @Override
    public void start(Stage stage) {
        img = new WritableImage(2,2);
        IntStream.range(0, 2).forEach(y->{
            for(int x = 0; x < 2; x++){
                if(x <= 1){
                    img.getPixelWriter().setColor(x, y, highlight);
                }else{
                    img.getPixelWriter().setColor(x, y, Color.RED);   
                }               
            }
        });
        hMat = new PhongMaterial();
        hMat.setDiffuseMap(img);
       
       
        camera = new PerspectiveCamera(true);
        cameraTransform.setTranslate(0, 0, 0);
        cameraTransform.getChildren().addAll(camera);
        camera.setNearClip(0.1);
        camera.setFarClip(1000000.0);
        camera.setFieldOfView(42);
        camera.setVerticalFieldOfView(true);
        camera.setTranslateZ(-100);

        target = new SpheroidMesh(32,100,100);
        target.setMaterial(hMat);
        target.setTranslateX(-500);
        target.setTranslateZ(500);
        target.setTranslateY(-500);
        target.setDrawMode(DrawMode.LINE);target.setCullFace(CullFace.NONE);

        origin = new Sphere(12.5);
        origin.setMaterial(new PhongMaterial(Color.BLUE));
        origin.setDrawMode(DrawMode.LINE);
        origin.setCullFace(CullFace.NONE);
       
        PointLight light2 = new PointLight(Color.GAINSBORO);
        light2.setTranslateZ(-1500);
        PointLight light = new PointLight(Color.AZURE);
        light.setTranslateZ(2500);
       
        root3D.getChildren().addAll(cameraTransform, target, origin, light, light2);

        StackPane root = new StackPane();
        root.getChildren().add(root3D);

        Scene scene = new Scene(root, 1200, 800, true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.DARKRED);
        scene.setCamera(camera);
        //First person shooter keyboard movement
        scene.setOnKeyPressed(event -> {
            double change = 10.0;
            //Add shift modifier to simulate "Running Speed"
            if (event.isShiftDown()) {
                change = 50.0;
            }
            //What key did the user press?
            KeyCode keycode = event.getCode();
            //Step 2c: Add Zoom controls
            if (keycode == KeyCode.W) {
                camera.setTranslateZ(camera.getTranslateZ() + change);
            }
            if (keycode == KeyCode.S) {
                camera.setTranslateZ(camera.getTranslateZ() - change);
            }
            //Step 2d: Add Strafe controls
            if (keycode == KeyCode.A) {
                camera.setTranslateX(camera.getTranslateX() - change);
            }
            if (keycode == KeyCode.D) {
                camera.setTranslateX(camera.getTranslateX() + change);
            }
            if(keycode == KeyCode.SPACE){
                checkIntersections();
            }

        });

        scene.setOnMousePressed((MouseEvent me) -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();

        });
        scene.setOnMouseDragged((MouseEvent me) -> {

            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseDeltaX = (mousePosX - mouseOldX);
            mouseDeltaY = (mousePosY - mouseOldY);

            double modifier = 10.0;
            double modifierFactor = 0.1;

            if (me.isControlDown()) {
                modifier = 0.1;
            }
            if (me.isShiftDown()) {
                modifier = 50.0;
            }
            if (me.isPrimaryButtonDown()) {
                cameraTransform.ry.setAngle(((cameraTransform.ry.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180); // +
                cameraTransform.rx.setAngle(((cameraTransform.rx.getAngle() - mouseDeltaY * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180); // -

            } else if (me.isSecondaryButtonDown()) {
                double z = camera.getTranslateZ();
                double newZ = z + mouseDeltaX * modifierFactor * modifier;
                camera.setTranslateZ(newZ);
            } else if (me.isMiddleButtonDown()) {
                cameraTransform.t.setX(cameraTransform.t.getX() + mouseDeltaX * modifierFactor * modifier * 0.3); // -
                cameraTransform.t.setY(cameraTransform.t.getY() + mouseDeltaY * modifierFactor * modifier * 0.3); // -
            }

        });
        stage.setTitle("Hello World!");
        stage.setScene(scene);
        stage.show();
    }

    private void checkIntersections(){
        if(visual != null){
            root3D.getChildren().remove(visual);
            visual = null;
        }
        TriangleMesh m = ((TriangleMesh)target.getMesh());
        System.err.println(" : " + m.getFaces().size());
        Transform a = target.getLocalToSceneTransform(),
                b = origin.getLocalToSceneTransform();
        Ray ray = new Ray(
                Point3D.ZERO.add(b.getTx(),b.getTy(),b.getTz()),
                new Point3D(
                        a.getTx() - b.getTx(),
                        a.getTx() - b.getTx(),
                        a.getTx() - b.getTx()
                ).normalize()
        );
        if(!intersectedFaces.isEmpty()){intersectedFaces.clear();}
       
        IntStream.range(0, m.getFaces().size()/6)
                .filter(i->{
                    //System.out.println(i);
                    int[] v = faceValues.apply(i, m);
                   
                    Point3D p1 = new Point3D(
                            m.getPoints().get(v[0]),
                            m.getPoints().get(v[0] + 1),
                            m.getPoints().get(v[0] + 2)
                    );
                    Point3D p2 = new Point3D(
                            m.getPoints().get(v[2]),
                            m.getPoints().get(v[2] + 1),
                            m.getPoints().get(v[2] + 2)
                    );
                    Point3D p3 = new Point3D(
                            m.getPoints().get(v[4]),
                            m.getPoints().get(v[4] + 1),
                            m.getPoints().get(v[4] + 2)
                    );
                   
                    return ray.intersects(i, p1, p2, p3);
                   
                })//.peek(System.out::println)
                .forEach(result->{
                    // handle the results
                });
                // show the ray
                if(!intersectedFaces.isEmpty()){
                    System.out.println("Intersected Faces : " + intersectedFaces.size());
                    List<org.fxyz.geometry.Point3D> l = new ArrayList<>();
                    l.add(new org.fxyz.geometry.Point3D((float)target.getTranslateX(), (float)target.getTranslateY(), (float)target.getTranslateZ()).multiply(1.5f));
                    l.add(new org.fxyz.geometry.Point3D((float)origin.getTranslateX(), (float)origin.getTranslateX(), (float)origin.getTranslateX()));
                    visual = new PolyLine3D(l, 1, highlight);
                    root3D.getChildren().add(visual);
                }
               
    }
    private final BiFunction<Integer, TriangleMesh, int[]> faceValues = (index, m) -> {
        if (index > ((m.getFaces().size()) - m.getFaceElementSize())) {
            return m.getFaces().toArray(index, null, 6);
        }
        if (index > 0) {            
            index = (index * 6);
            //System.err.println(index);
            return m.getFaces().toArray(index, null, 6);
        }
        // for 0 index
        return m.getFaces().toArray(index, null, index + 6);
    };
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
