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
    holdingProbFile = "HoldingProb-Scenario{0}".format(scenarioNumber)    
    
    resultMean1, resultSError1 = readMeanListSdListFromFile("{0}.csv".format(holdingProbFile))
    
    fig = plt.figure(num=None, figsize=(50, 12), dpi=80)
    # ax2 = fig.add_subplot(111)
    x_axis = range(0, 101, 10)
    plt.errorbar(x=x_axis, y=resultMean1, yerr=resultSError1, fmt='r-')  
    # plt.ylim((0, ))
    plt.xlim((0,100 ))

    # plt.yticks(np.arange(0, 360, 80))
    # for tl in plt.get_yticklabels():
    #     tl.set_color('r')
    font = {'size': 30}


    rc('font', **font)
    # for tick in mpl.axis.Axis.get_major_ticks():
    #     tick.label.set_fontsize(30);
    # for tick in mpl.axis.YAxis.get_major_ticks():
    #     tick.label.set_fontsize(30);
    plt.tick_params(axis='both', which='major', labelsize=30)
    # handles, labels = plt.get_legend_handles_labels()

    # # reverse the order
    # plt.legend(handles[::-1], labels[::-1])

    # or sort them by labels
    
    plt.ylabel("Total Number of Hotspots", 
       fontsize=30,
       verticalalignment='center',
       horizontalalignment='right',
       rotation='vertical' )
    plt.xlabel("Percentage Holding Device", 
       fontsize=30)
    # print labels2
    # plt.legend(handles2, labels2, loc="upper right")
    # plt.legend(loc="upper right")
    # plt.show()
    
    
    plt.savefig("HoldingProb-Scenario{0}".format(scenarioNumber), pad_inches=0)
    plt.close()