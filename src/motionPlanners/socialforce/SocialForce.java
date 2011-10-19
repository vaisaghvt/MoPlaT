/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package motionPlanners.socialforce;

import agent.RVOAgent;
import javax.vecmath.Vector2d;
import motionPlanners.VelocityCalculator;
import sim.util.Bag;
import java.io.*;

/**
 *
 * @author steven
 */
public class SocialForce implements VelocityCalculator{
    private double g(double x){
        if(x>=0)    // Psudeo kernel 
            return x;
        else
            return 0;
    }
    @Override
    public Vector2d calculateVelocity(RVOAgent me, Bag neighbors, Bag obses, 
    Vector2d preferredVelocity, double timeStep) {
        final int A=2000;       //Published value = 2000 N
        final double B=0.08;    //Published value = 0.08 m
        final int k=12000;      //Published value = 120000 kg/s^2
        final int kappa=24000;  //Published value = 240000 kg/ms
        
        double Pxi = me.getCurrentPosition().getX();    // X-position
        double Pyi = me.getCurrentPosition().getY();    // Y-position
        double Vxi = me.getVelocity().getX();           // X-velocity
        double Vyi = me.getVelocity().getY();           // Y-velocity
        double Ri = me.getRadius();                     // Agent's radius
        double Mi = me.getMass();                       // Agent's mass
        
        double fijx = 0;    //  X-direction interaction force initialization
        double fijy = 0;    //  Y-direction interaction force initialization
        
        for(int i=0;i< neighbors.size();i++){
            RVOAgent tempAgent = (RVOAgent) neighbors.get(i);
            if (tempAgent.equals(me)){
                continue;   // skip agent i itself for the calculation
            }
            
            double Pxj = tempAgent.getCurrentPosition().getX();
            double Pyj = tempAgent.getCurrentPosition().getY();
            double Vxj = tempAgent.getVelocity().getX();
            double Vyj = tempAgent.getVelocity().getY();
            double Rj = tempAgent.getRadius();
            
            double Rij = Ri + Rj;   //System.out.println("Rij: "+Rij);
            double Dij = Math.sqrt((Pxi-Pxj)*(Pxi-Pxj)+(Pyi-Pyj)*(Pyi-Pyj));
            if (Dij<0.01){
                System.out.println("ERR! Dij<0.01. Dij "+Dij+" Pxi: "+Pxi+" Pxj: "+Pxj);
                Dij=1;
            }
            
            double RD = Rij - Dij;
            double Nijx = (Pxi-Pxj)/Dij;    // Normal X-direction unit vector
            double Nijy = (Pxi-Pxj)/Dij;    // Normal Y-direction unit vector
            double Tijx = -Nijy;            // Tangential X-direction unit vector
            double Tijy = Nijx;             // Tangential Y-direction unit vector
            double DeltaVtji = (Vxj-Vxi)*Tijx+(Vyj-Vyi)*Tijy;
            
            double fsr=A*Math.exp(RD/B);
            double fp=k*g(RD);
            double ff=kappa*g(RD)*DeltaVtji;

            fijx += (fsr+fp)*Nijx + ff*Tijx;
            fijy += (fsr+fp)*Nijy + ff*Tijy;
        }
        
        double Vx = fijx/Mi*timeStep + preferredVelocity.x;        //System.out.println("Vx: "+Vx);
        double Vy = fijy/Mi*timeStep + preferredVelocity.y;
        
        return new Vector2d(Vx,Vy);
    }
    
}