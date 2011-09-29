/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package motionPlanners;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
    
    final double Rv=2;      // The view range
    final double p=0.3;     // Panic factor
    final double v0=1.0;    // Common velocity
    final double[] M= new double[N];    // Mass of human
    
    /*
    for(int i=0; i<N; i++){
        M[i]=50 + 20*Math.random();
    }
    * 
    */
    
    public double[] PreferredForce(double[] Px, double[] Py, double[] Vx, double[] Vy, double[] desx, double[] desy, int N){
    
        /*
         * Initialize variable
         */
        double[][] Dij = new double[N][N];  // Inter-distance between i and j
        double[] Did = new double[N];       // Inter-distance between i and destination
        double[] Eix = new double[N];       // Preferred direction unit vector x
        double[] Eiy = new double[N];       // Preferred direction unit vector y
        double[] vecExpectedE0jx = new double[N];
        double[] vecExpectedE0jy = new double[N];
        double[] vecE0ix = new double[N];
        double[] vecE0iy = new double[N];
        double[] absE0i = new double[N];
        double[] normE0ix = new double[N];
        double[] normE0iy = new double[N];
        
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
            
            vecE0ix[i]=(1-p)*Eix[i]+p*vecExpectedE0jx[i];
            vecE0iy[i]=(1-p)*Eiy[i]+p*vecExpectedE0jy[i];
            
            absE0i[i]=sqrt(vecE0ix[i]*vecE0ix[i]+vecE0iy[i]*vecE0iy[i]);
            
            normE0ix[i]=vecE0ix[i]/absE0i[i];
            normE0iy[i]=vecE0iy[i]/absE0i[i];
            
            fpx[i]=M[i]*(v0*normE0ix[i]-Vx[i])/tau;
            fpy[i]=M[i]*(v0*normE0iy[i]-Vy[i])/tau;
        };

        return fpx fpy;
                
    }
    
    private double InteractionForce(double[] Px, double[] Py, double[] Vx,double[] Vy){
    
        final int A=2000;       //N
        final double B=0.08;    //m
        final int k=120000;     //kg/s^2
        final int kappa=240000; //kg/ms
        
        double[][] Rij = new double[N][N];
        double[][] Dij = new double[N][N];
        double[][] RD = new double[N][N];
        double[][] Nijx = new double[N][N];
        double[][] Nijy = new double[N][N];
        double[][] Tijx = new double[N][N];
        double[][] Tijy = new double[N][N];
        double[][] DeltaVtji = new double[N][N];
        double[][] fsr = new double[N][N];
        double[][] fp = new double[N][N];
        double[][] ff = new double[N][N];
        
        for(int i=0; i<N; i++){
            for(int j=0; j<N; j++){
                Rij[i][j] = R[i]+R[j];
                
                if (i==j)
                    Dij[i][j]=1;    // eye (MATLAB)
                else
                    Dij[i][j] = sqrt((Px[i]-Px[j])*(Px[i]-Px[j])+(Py[i]-Py[j])*(Py[i]-Py[j]));                    
                
                RD[i][j]=Rij[i][j]-Dij[i][j];
                Nijx[i][j]=(Px[i]-Px[j])/Dij[i][j];
                Nijy[i][j]=(Py[i]-Py[j])/Dij[i][j];
                
                Tijx[i][j]=-Tijx[i][j];
                Tijy[i][j]=Tijy[i][j];
                
                DeltaVtji[j][i]=(Vx[j]-Vx[i]).*Tijx[i][j]+(Vy[j]-Vy[i]).*Tijy[i][j];
                
                fsr[i][j]=A*exp(RD[i][j]/B);
                fp[i][j]=k*g(RD[i][j]);
                ff[i][j]=kappa*g(RD[i][j])*DeltaVtji[j][i];
                
                fijx[i][j]=(fsr[i][j]+fp[i][j]).*Nijx[i][j] + ff[i][j].*Tijx[i][j];
                fijy[i][j]=(fsr[i][j]+fp[i][j]).*Nijy[i][j] + ff[i][j].*Tijy[i][j];
            }
        };
        
        
        return fijx fijy
    }
    
    private double BoundaryForce(){
    
        return
    }
    
    private double g(double x){
        if(x>=0)
            return x;
        else
            return 0;
        
    }
    
}
