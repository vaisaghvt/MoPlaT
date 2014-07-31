from PositionHeatMap import readMeanListSdListFromFile
import os
import math
from scipy import stats
import matplotlib.pyplot as plt
import matplotlib as mpl
from matplotlib import rc
import numpy as np

scenarioList = [1,2]
# scenarioList = [8]
for scenarioNumber in scenarioList:
    withDeviceFile = "HotSpotsOverTime-Scenario{0}-{1}".format(scenarioNumber, "with device")    
    withoutDeviceFile = "HotSpotsOverTime-Scenario{0}-{1}".format(scenarioNumber, "without device")    
    with open("{0}.csv".format(withDeviceFile)) as withFile:
        with open("{0}.csv".format(withoutDeviceFile)) as withoutFile:
            resultMean1, resultSError1 = readMeanListSdListFromFile("{0}.csv".format(withDeviceFile))
            resultMean2, resultSError2 = readMeanListSdListFromFile("{0}.csv".format(withoutDeviceFile))
            
            
            fig = plt.figure(num=None, figsize=(50,50), dpi=80)
            # ax2 = fig.add_subplot(111)
            x_axis = range(1, len(resultMean1)+1, 1)
            plt.errorbar(x=x_axis, y=resultMean1, yerr=resultSError1, fmt='r-', label="With Device")
            x_axis = range(1, len(resultMean2)+1, 1)
            plt.errorbar(x=x_axis, y=resultMean2, yerr=resultSError2, fmt='b-', label="Without Device")
            
            plt.ylim((0, 4))

            # plt.yticks(np.arange(0, 360, 80))
            # for tl in plt.get_yticklabels():
            #     tl.set_color('r')
            font = {'size': 60}


            rc('font', **font)
            # for tick in mpl.axis.Axis.get_major_ticks():
            #     tick.label.set_fontsize(60);
            # for tick in mpl.axis.YAxis.get_major_ticks():
            #     tick.label.set_fontsize(60);
            plt.tick_params(axis='both', which='major', labelsize=60)
            # handles, labels = plt.get_legend_handles_labels()

            # # reverse the order
            # plt.legend(handles[::-1], labels[::-1])

            # or sort them by labels
            
            plt.ylabel("Number of hotspots", 
               fontsize=60,
               verticalalignment='center',
               horizontalalignment='right',
               rotation='vertical' )
            plt.xlabel("Time in timestep (0.05s)", 
               fontsize=60)
         
            # print labels2
            # plt.legend(handles2, labels2, loc="upper right")
            plt.legend(loc="upper right")
            # plt.show()
            
            
            plt.savefig("HotSpots-Scenario{0}".format(scenarioNumber), pad_inches=0)
            plt.close()