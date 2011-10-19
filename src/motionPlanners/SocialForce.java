/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package motionPlanners;

import agent.RVOAgent;
import javax.vecmath.Vector2d;
import sim.util.Bag;

/**
 *
 * @author steven
 */
public class SocialForce implements VelocityCalculator{
    private double g(double x){
        if(x>=0)
            return x;
        else
            return 0;
    }
    @Override
    public Vector2d calculateVelocity(RVOAgent me, Bag neighbors, Bag obses, 
    Vector2d preferredVelocity, double timeStep) {
        final int A=20000;       //N
        final double B=0.1;    //m
        final int k=120000;     //kg/s^2
        final int kappa=240000; //kg/ms
        //final int Va=181;   //degree (View angle)
        
        double Pxi = me.getCurrentPosition().getX();
        double Pyi = me.getCurrentPosition().getY();
        double Vxi = me.getVelocity().getX();
        double Vyi = me.getVelocity().getY();
        double Ri = me.getRadius();
        double Mi = me.getMass();
        
        double fijx = 0;
        double fijy = 0;
        
        for(int i=0;i< neighbors.size();i++){
            RVOAgent tempAgent = (RVOAgent) neighbors.get(i);
            
            double Pxj = tempAgent.getCurrentPosition().getX();
            double Pyj = tempAgent.getCurrentPosition().getY();
            double Vxj = tempAgent.getVelocity().getX();
            double Vyj = tempAgent.getVelocity().getY();
            double Rj = tempAgent.getRadius();
            
            double Rij = Ri + Rj;
            double Dij = Math.sqrt((Pxi-Pxj)*(Pxi-Pxj)+(Pyi-Pyj)*(Pyi-Pyj));
            System.out.println(Pxi+" "+Pxj+" "+Pyi+" "+Pyj);
            if (Dij<0.01){
                System.out.println(Dij);
                Dij=1;
            }
            
            double RD = Rij - Dij;
            
            double Nijx = (Pxi-Pxj)/Dij;    // Normal direction unit vector
            double Nijy = (Pxi-Pxj)/Dij;
            double Tijx = -Nijy;            // Tangential direction unit vector
            double Tijy = Nijx;

            
            double DeltaVtji = (Vxj-Vxi)*Tijx+(Vyj-Vyi)*Tijy;
            
            double fsr=A*Math.exp(RD/B);
            double fp=k*g(RD);
            double ff=kappa*g(RD)*DeltaVtji;

            //System.out.println(fsr+" "+fp+" "+ff);
            
            
            //double dotP = Vxi*Nijx + Vyi*Nijy;
            //double absV = Math.sqrt(Vxi*Vxi + Vyi*Vyi);
            //double absNij = Math.sqrt(Nijx*Nijx + Nijy*Nijy);
            
            fijx += (fsr+fp)*Nijx + ff*Tijx;
            fijy += (fsr+fp)*Nijy + ff*Tijy;
            
            // DIRECITON CONDITION
//            if(dotP <= absV*absNij*Math.cos(Va/180*Math.PI)){
//                fijx += (fsr+fp)*Nijx + ff*Tijx;
//                fijy += (fsr+fp)*Nijy + ff*Tijy;
//            }else{
//                fijx += 0;
//                fijy += 0;
//            }
            
            
            
        }
        
        //System.out.println("fijx: "+fijx+"; fijy: "+fijy);
        //System.out.println();
        
        double Vx = fijx/Mi*timeStep+preferredVelocity.x;
        double Vy = fijy/Mi*timeStep+preferredVelocity.y;
        
        return new Vector2d(Vx,Vy);
        
    }
    
}