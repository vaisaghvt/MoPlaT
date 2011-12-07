/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package motionPlanners.socialforce;

/**
 *
 * @author steven
 */
public class PrecalculateForces {
    
    //
    // Average over surrounding force
    //
    public static double[] averageSurroundForce(double Pxi, double Pyi, double Xmax, double Ymax, int N0x, int N0y, double[][] F, double[][] Fx, double[][] Fy) {
        int cellix = (int) (Pxi/Xmax)*N0x;
        int celliy = (int) (Pyi/Ymax)*N0y;
        
        int[] neighbouricelly;
        int[] neighbouricellx;
        
        // Check cell boundary condition
        if (cellix==1 && celliy==1){   // Left Top corner
            neighbouricelly = new int[] {2,1,2};
            neighbouricellx = new int[] {1,2,2};
        }
        else if (cellix==1 && celliy== N0y){     // Right top corner
            neighbouricelly = new int[] {N0y-1,N0y-1,N0y};
            neighbouricellx = new int[] {1,2,2};
        }
        else if (cellix==N0x && celliy== 1){     // Left bottom corner
            neighbouricelly = new int[] {1,2,2};
            neighbouricellx = new int[] {N0x-1,N0x,N0x-1};
        }
        else if (cellix==N0x && celliy== N0y){    // Right bottom corner
            neighbouricelly = new int[] {N0y-1,N0y-1,N0y-1};
            neighbouricellx = new int[] {N0x,N0x-1,N0x};
        }
        else if (cellix==1){    // Top row all column
            neighbouricellx = new int[] {1,1,2,2,2};
            neighbouricelly = new int[] {celliy-1,celliy+1,celliy-1,celliy,celliy+1};
        }
        else if (celliy==N0y){   // Right column all row
            neighbouricellx = new int[] {cellix-1,cellix+1,cellix-1,cellix,cellix+1};
            neighbouricelly = new int[] {N0y,N0y,N0y-1,N0y-1,N0y-1};
        }
        else if (cellix==N0x){   // Bottom row all column
            neighbouricellx = new int[] {N0x,N0x,N0x-1,N0x-1,N0x-1};
            neighbouricelly = new int[] {celliy-1,celliy+1,celliy-1,celliy,celliy+1};
        }
        else if (celliy==1){    // Left column all row
            neighbouricellx = new int[] {cellix-1,cellix+1,cellix-1,cellix,cellix+1};
            neighbouricelly = new int[] {1,1,2,2,2};
        }
        else{   // Otherwise
            neighbouricellx = new int[] {cellix,cellix,cellix+1,cellix+1,cellix+1,cellix-1,cellix-1,cellix-1};
            neighbouricelly = new int[] {celliy-1,celliy+1,celliy-1,celliy,celliy+1,celliy-1,celliy,celliy+1};
        }
        
        // Collecting neighbour Force magnitude and direction for averaging
        int Nneighbour = neighbouricellx.length;
        double[] FAvg = new double[Nneighbour+1];
        double[] FxAvg = new double[Nneighbour+1];
        double[] FyAvg = new double[Nneighbour+1];
        for (int i = 0; i<Nneighbour; i++){
            // Assigning value into buffer
            FAvg[i] = F[neighbouricelly[i]][neighbouricellx[i]];
            FxAvg[i] = Fx[neighbouricelly[i]][neighbouricellx[i]];
            FyAvg[i] = Fy[neighbouricelly[i]][neighbouricellx[i]];
        }
        FAvg[Nneighbour] = F[celliy][cellix];
        FxAvg[Nneighbour] = Fx[celliy][cellix];
        FyAvg[Nneighbour] = Fy[celliy][cellix];
        
        // Average over the force
        double meanFAvg = 0;
        double meanFxAvg = 0;
        double meanFyAvg = 0;
        for (int i = 0; i<Nneighbour+1; i++){
            meanFAvg += FAvg[i];
            meanFxAvg += FxAvg[i];
            meanFyAvg += FyAvg[i];
        }
        meanFAvg = meanFAvg/Nneighbour;
        meanFxAvg = meanFxAvg/Nneighbour;
        meanFyAvg = meanFyAvg/Nneighbour;

        double Fxi = -meanFxAvg*meanFAvg;
        double Fyi = -meanFyAvg*meanFAvg;

        return new double[]{Fxi,Fyi};
    }

    //
    // MATLAB linspace function
    //
    private static double[] linearSpaceVector(double a, double b, int c){
        // Create a vector of c elements starting with a, interval difference
        // of intervalValue until b.
        double intervalValue = (b-a)/(c-1);
        double[] resultVector = new double[c];
        
        resultVector[0] = a;
        
        for (int i=0; i<c-1; i++){
            resultVector[i+1] += resultVector[i]+intervalValue;
        }
        resultVector[c-1]=b;
        
        return resultVector;
    } 
    
    public static double[][][] PreCalcForce(int N0x, int N0y, double[] vertexx, double[] vertexy, double Xmin, double Xmax, double Ymin, double Ymax){
            
            double[] x = linearSpaceVector(Xmin,Xmax,N0x);
            double[] y = linearSpaceVector(Ymin,Ymax,N0y);

            // Exponential force constant
            double A_wall = 1;             //10  Diameter of the wall
            double B_wall = 1;             //10  Steepness of the wall
            double A_corner = A_wall;      //5 Diameter of the pole
            double B_corner = B_wall;	//5 Steepness of the pole
//
//            // The vertex in cartesian coordinate
//            double[] vertexx = new double[] {0,0,5,5};
//            double[] vertexy = new double[] {-5,0,0,-5};

            // Initialize every cell of force
            double[][] F = new double[N0y][N0x];
            double[][] Fx = new double[N0y][N0x];
            double[][] Fy = new double[N0y][N0x];
            for (int i=0; i<N0x; i++){
                for (int j=0; j<N0y; j++){
                    F[j][i] = 0;
                    Fx[j][i] = 0;
                    Fy[j][i] = 0;
                }
            }

            int Nvertex = vertexx.length;  // total number of vertex 

            for (int p=0; p<Nvertex; p++){
                // Vector point from vertex p+1 to p
                double bx = vertexx[p+1]-vertexx[p];
                double by = vertexy[p+1]-vertexy[p];
                double normb = Math.sqrt(bx*bx+by*by);
                double bxhat = bx/normb;
                double byhat = by/normb;

                for (int i=0; i<N0x; i++){
                    for (int j=0; j<N0y; j++){

                        ////////////////////////////////////////////////////////////
                        // Wall Potential //////////////////////////////////////////
                        ////////////////////////////////////////////////////////////
                        double a = (x[i]-vertexx[p])*bxhat+(y[j]-vertexy[p])*byhat;
                        if (a>=0 && a<=normb){
                            // Vector points on wall's line
                            double wx = a*bxhat + vertexx[p];
                            double wy = a*byhat + vertexy[p];
                            //double normw = Math.sqrt(wx*wx+wy*wy);

                            // Vector point perpendicular to wall's line
                            double rx = x[i]-wx;
                            double ry = y[j]-wy;
                            double normr = Math.sqrt(rx*rx+ry*ry);

                            // Speed magnitude (Exponential potential)
                            double F0 = A_wall*Math.exp(-B_wall*normr);
                            F[j][i] += F0;
                        }

                        ////////////////////////////////////////////////////////////
                        // Half pole at 1st (p) end of wall ////////////////////////
                        ////////////////////////////////////////////////////////////
                        double rx = x[i]-vertexx[p];
                        double ry = y[j]-vertexy[p];
                        double normr = Math.sqrt(rx*rx+ry*ry);

                        double ax = -bx;
                        double ay = -by;
                        double norma = Math.sqrt(ax*ax+ay*ay);

                        // Only calc for half a circle
                        if ((rx*ax+ry*ay)/(normr*norma)>Math.cos(Math.PI/2)){
                            // Speed magnitude (Exponential potential)
                            F[j][i] += A_corner*Math.exp(-B_corner*normr);
                        }

                        ////////////////////////////////////////////////////////////
                        // Half pole at 2nd (p+1) end of wall //////////////////////
                        ////////////////////////////////////////////////////////////
                        rx = x[i]-vertexx[p+1];
                        ry = y[j]-vertexy[p+1];
                        normr = Math.sqrt(rx*rx+ry*ry);

                        ax = bx;
                        ay = by;
                        norma = Math.sqrt(ax*ax+ay*ay);

                        if ((rx*ax+ry*ay)/(normr*norma)>Math.cos(Math.PI/2)){
                            // Speed magnitude (Exponential potential)
                            F[j][i] += A_corner*Math.exp(-B_corner*normr);
                        }

                        ////////////////////////////////////////////////////////////
                        // Pole deletion at intersection corner ////////////////////
                        ////////////////////////////////////////////////////////////
                        if ((p>1 && p<Nvertex) || (vertexx[1]==vertexx[Nvertex-1] && vertexy[1]==vertexy[Nvertex-1]) ){
                            rx = x[i]-vertexx[p];
                            ry = y[j]-vertexy[p];
                            normr = Math.sqrt(rx*rx+ry*ry);

                            // Speed magnitude (Exponential potential)
                            F[j][i] -= A_corner*Math.exp(-B_corner*normr);

                        }
                        
                    }//end for j loop
                }// end for i loop
            }//end for p loop

            ////////////////////////////////////////////////////////////////////////
            // Force field direction ///////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////////
            double deltay = y[2] - y[1];
            for (int i=0; i<N0x; i++){
                // First row
                Fy[0][i]=(F[1][i]-F[0][i])/deltay;
                // Last row
                Fy[N0y-1][i]=(F[N0y-1][i]-F[N0y-2][i])/deltay;
                // Middle row
                for (int j=1; j<=N0y-2; j++){
                    Fy[j][i]=(F[j+1][i]-F[j-1][i])/deltay;
                }
            }

            double deltax = x[2] - x[1];
            for (int j=0; j<N0y; j++){
                // First column
                Fx[j][0]=(F[j][1]-F[j][0])/deltax;
                // Last column
                Fy[j][N0x-1]=(F[j][N0x-1]-F[j][N0x-2])/deltax;
                // Middle row
                for (int i=1; i<=N0x-2; i++){
                    // CHCECK WHETHER IS 2 deltax or deltax!!!!!!!
                    Fy[j][i]=(F[j][i+1]-F[j][i-1])/deltax;
                }
            }

            // Dot product of the force field matrix
            for (int i=0; i<N0x; i++){
                for (int j=0; j<N0y; i++){
                    Fx[j][i] = Fx[j][i]*F[j][i];
                    Fy[j][i] = Fy[j][i]*F[j][i];
                }
            }
            
            // Prepare to output the 3 2D matrix
            double[][][] result = new double[3][][];
            result[0] = F;
            result[1] = Fx;
            result[2] = Fy;
            
            return result;
    }
}
