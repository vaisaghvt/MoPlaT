/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package motionPlanners.socialforce;

import agent.RVOAgent;
import app.PropertySet;
import javax.vecmath.Vector2d;
import motionPlanners.VelocityCalculator;
import sim.util.Bag;
import java.io.*;
import javax.vecmath.Point2d;

/**
 *
 * @author steven
 */
public class SocialForce implements VelocityCalculator {
    
    private double g(double x) {
        if (x >= 0) // Psudeo kernel 
        {
            return x;
        } else {
            return 0;
        }
    }

    // Bounding the force (Newton)
    private double forceBound(double x) {
        int var = 2;
        
        if (x > var)
            x = var;
        else if (x < -var)
            x = -var;
                
        return x;
    }

    @Override
    public Vector2d calculateVelocity(RVOAgent me, Bag neighbors, Bag obses,
            Vector2d preferredVelocity, double timeStep) {
//        final double p=0.7;     // Panic factor

        // Constant for agent-agent interaction
        final int Aj = 2000;       //Published value = 2000 N
        final double Bj = 0.08;    //Published value = 0.08 m
        final int kj = 12000;      //Published value = 120000 kg/s^2
        final int kappaj = 24000;  //Published value = 240000 kg/ms
        
        // Constant for agent-wall interaction
        final int Aw = 5000;       //
        final double Bw = 0.1;    //
        final int kw = 12000;      //
        final int kappaw = 24000;  //

        final double Rw = 0.3;  // Imaginary wall radius

        double Pxi = me.getCurrentPosition().getX();    // X-position
        double Pyi = me.getCurrentPosition().getY();    // Y-position
        double Vxi = me.getVelocity().getX();           // X-velocity
        double Vyi = me.getVelocity().getY();           // Y-velocity
        double Ri = me.getRadius();                     // Agent's radius
        double Mi = me.getMass();                       // Agent's mass

        // #####################################################################
        // INTERACTION FORCE
        // #####################################################################
        double fijx = 0;    //  X-direction interaction force initialization
        double fijy = 0;    //  Y-direction interaction force initialization
        
        // #####################################################################
        // PRECALCULATE WALL FORCE GRID
        // #####################################################################
        int N0x = 10*PropertySet.WORLDXSIZE;        // Total number of cell in X direction
        int N0y = 10*PropertySet.WORLDYSIZE;        // Total number of cell in Y direction
        double Xmin=0;                              // Minimum x point of terrain
        double Xmax=PropertySet.WORLDXSIZE;         // Maximum x point of terrain
        double Ymin=0;                              // Minimum y point of terrain
        double Ymax=PropertySet.WORLDYSIZE;         // Maximum y point of terrain
                
        double[] vertexx = new double[] {0,0,5,5};      // Vertex coordinate x
        double[] vertexy = new double[] {-5,0,0,-5};    // Vertex coordinate y
        
        double[][][] preFw = PrecalculateForces.PreCalcForce(N0x,N0y,vertexx,vertexy,Xmin,Xmax,Ymin,Ymax);
        double[][] Fw = preFw[0];
        double[][] Fwx = preFw[1];
        double[][] Fwy = preFw[2];
        
        //
        // Update everytime step for every neighbour
        //
        for (int i = 0; i < neighbors.size(); i++) {

            RVOAgent tempAgent = (RVOAgent) neighbors.get(i);

            if (tempAgent.equals(me)){
                continue;   // skip agent i itself for the calculation
            }
//            else if (tempAgent.isSocialForceObstacle()==true){
//                continue;
//            }
        //If this is real agent
                double Pxj = tempAgent.getCurrentPosition().getX();
                double Pyj = tempAgent.getCurrentPosition().getY();
                double Vxj = tempAgent.getVelocity().getX();
                double Vyj = tempAgent.getVelocity().getY();
                double Rj = tempAgent.getRadius();

                double Rij = Ri + Rj;   //System.out.println("Rij: "+Rij);
                double Dij = Math.sqrt((Pxi - Pxj) * (Pxi - Pxj) + (Pyi - Pyj) * (Pyi - Pyj));
//                if (Dij < 0.01) {
//                    System.out.println("ERR! Dij<0.01. Dij " + Dij + " Pxi: " + Pxi + " Pxj: " + Pxj);
//                    Dij = 1;
//                }
             
                double RD = Rij - Dij;
                double Nijx = (Pxi - Pxj) / Dij;    // Normal X-direction unit vector
                double Nijy = (Pyi - Pyj) / Dij;    // Normal Y-direction unit vector
                double Tijx = -Nijy;            // Tangential X-direction unit vector
                double Tijy = Nijx;             // Tangential Y-direction unit vector
                double DeltaVtji = (Vxj - Vxi) * Tijx + (Vyj - Vyi) * Tijy;
                
                // Social Repulsion force
                double fsr = Aj * Math.exp(RD / Bj);
                // Pushing force
                double fp = kj * g(RD);
                // Friction force
                double ff = kappaj * g(RD) * DeltaVtji;

                //System.out.println(fsr + " " + fp + " " + ff);
                
                fijx += (fsr + fp) * Nijx + ff * Tijx;
                fijy += (fsr + fp) * Nijy + ff * Tijy;
        }
        
        double Vxint = (fijx) / Mi * timeStep;
        double Vyint = (fijy) / Mi * timeStep;

//         #################################################################
//         BOUNDARY FORCE
//         #################################################################
        
//        double fiwx = 0;    //  X-direction interaction force initialization
//        double fiwy = 0;    //  Y-direction interaction force initialization
//
//        for (int i = 0; i < neighbors.size(); i++) {
//            RVOAgent tempObstacle = (RVOAgent) neighbors.get(i);
//            
//            if (tempObstacle.equals(me)){
//                continue;   // skip agent i itself for the calculation
//            }
////            else if (tempObstacle.isSocialForceObstacle()==false){
////                continue;
////            }
//
//            double Wx = tempObstacle.getX();
//            double Wy = tempObstacle.getY();
//            double Diw = Math.sqrt((Pxi - Wx) * (Pxi - Wx) + (Pyi - Wy) * (Pyi - Wy));
////            if (Diw < 0.01) {
////                //System.out.println("ERR! Diw<0.01. Diw " + Diw + " Pxi: " + Pxi + " Wx: " + Wx);
////                Diw = 1;
////            }
//
//            double RD = Rw - Diw;
//
//            double Niwx = (Pxi - Wx) / Diw;
//            double Niwy = (Pyi - Wy) / Diw;
//
//            double Tiwx = -Niwy;    //Tangential component vector 
//            double Tiwy = Niwx;
//
//            double DeltaVtji = Vxi * Tiwx + Vyi * Tiwy;
//
//            double fsr = Aw * Math.exp(RD / Bw);
//            double fp = kw * g(RD);
//            double ff = kappaw * g(RD) * DeltaVtji;
//
//            fiwx += (fsr + fp) * Niwx + ff * Tiwx;
//            fiwy += (fsr + fp) * Niwy + ff * Tiwy;
//
//        }
//        
//        
//        double Vxwall = (fiwx) / Mi * timeStep;
//        double Vywall = (fiwy) / Mi * timeStep;
        
        // #################################################################
        // BOUNDARY FORCE FIELD
        // #################################################################
        double[] Fwi = PrecalculateForces.averageSurroundForce(Pxi,Pyi,Xmax,Ymax,N0x,N0y,Fw,Fwx,Fwy);
        double fiwx = Fwi[0];
        double fiwy = Fwi[1];
        double Vxwall = (fiwx) / Mi * timeStep;
        double Vywall = (fiwy) / Mi * timeStep;

        double Vx = forceBound(preferredVelocity.x + Vxint + Vxwall);
        double Vy = forceBound(preferredVelocity.y + Vyint + Vywall);

        //System.out.println(Vxint);
        
        return new Vector2d(Vx, Vy);
    }
}