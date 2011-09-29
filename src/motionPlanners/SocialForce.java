/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package motionPlanners;

import java.util.ArrayList;
import java.util.List;
import java.lang.Math;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

/**
 *
 * @author steven
 */
public class SocialForce implements VelocityCalculator{
 
    public static double[] Px;      // Agent's position x
    public static double[] Py;      // Agent's position y
    public static double desx;      // Agent's destination x
    public static double desy;      // Agent's destination y
    
    public static double[] fpx;     // Preffered force
    public static double[] fpy;
    public static double[] fijx;    // Interactive force
    public static double[] fijy;
    public static double[] fiwx;    // Boundary force
    public static double[] fiwy;
    
    final double Rv=2;  //  The view range
    final double p=0.3; //  Panic factor
    
    public double[] PreferredForce(double[] Px, double[] Py,double[] desx, double[] desy, int N){
    
        /*
         * Initialize variable
         */
        double[][] Dij = new double[N][N];  // Inter-distance between i and j
        double[] Did = new double[N];       // Inter-distance between i and destination
        double[] Eix = new double[N];       // Preferred direction unit vector x
        double[] Eiy = new double[N];       // Preferred direction unit vector y
        double[] vecExpectedE0jx = new double[N];
        double[] vecExpectedE0jy = new double[N];
        double[] vecE0jx = new double[N];
        double[] vecE0jy = new double[N];
        
        for(int i=0; i<N; i++){
            vecExpectedE0jx[i]=0;
            vecExpectedE0jy[i]=0;
        }
        
        /*
         * Finding inter-distance Dij and Did, and the unit preferred direction
         * Eix and Eiy
         */
        for(int i=0; i<N; i++){
            for(int j=0; j<N; j++){
                Dij[i][j] = sqrt((Px[i]-Px[j])*(Px[i]-Px[j])+(Py[i]-Py[j])*(Py[i]-Py[j]));                    
            }
            
            Did[i] = sqrt((desx-Px[i])*(desx-Px[i])+(desy-Py[i])*(desy-Py[i]));
            
            Eix[i] = (desx-Px[i])/Did[i];
            Eiy[i] = (desx-Py[i])/Did[i];
        };
        
        /*
         * Find the mean of vecExpectedE0j by taking the mean of Ei which is 
         * within the range of Rv
         */
        for(int i=0; i<N; i++){
            int counter=0;
            for(int j=0; j<N; j++){
                if (Dij[i][j]<=Rv){
                    counter+=1;
                    vecExpectedE0jx[i]+=Eix[j];
                    vecExpectedE0jy[i]+=Eiy[j];
                }
            }
            vecExpectedE0jx[i]/=counter;
            vecExpectedE0jy[i]/=counter;
            
            vecE0jx[i]=(1-p)
            
        };
        
        
        
        
        return
                
    }
    
    private double InteractionForce(){
    
        return
    }
    
    private double BoundaryForce(){
    
        return
    }
    
}
