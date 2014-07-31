import os
import math
from scipy import stats
import matplotlib.pyplot as plt
import matplotlib as mpl
from matplotlib import rc
import numpy as np


class Point:

    """A point identified by (x,y) coordinates.

    supports: +, -, *, /, str, repr

    length  -- calculate length of vector to point from origin
    distance_to  -- calculate distance between two points
    as_tuple  -- construct tuple (x,y)
    clone  -- construct a duplicate
    integerize  -- convert x & y to integers
    floatize  -- convert x & y to floats
    move_to  -- reset x & y
    slide  -- move (in place) +dx, +dy, as spec'd by point
    slide_xy  -- move (in place) +dx, +dy
    rotate  -- rotate around the origin
    rotate_about  -- rotate around another point
    """

    def __init__(self, x=0.0, y=0.0):
        self.x = x
        self.y = y

    def __add__(self, p):
        """Point(x1+x2, y1+y2)"""
        return Point(self.x+p.x, self.y+p.y)

    def __sub__(self, p):
        """Point(x1-x2, y1-y2)"""
        return Point(self.x-p.x, self.y-p.y)

    def __mul__( self, scalar ):
        """Point(x1*x2, y1*y2)"""
        return Point(self.x*scalar, self.y*scalar)

    def __div__(self, scalar):
        """Point(x1/x2, y1/y2)"""
        return Point(self.x/scalar, self.y/scalar)

    def __str__(self):
        return "(%s, %s)" % (self.x, self.y)

    def __repr__(self):
        return "%s(%r, %r)" % (self.__class__.__name__, self.x, self.y)

    def length(self):
        return math.sqrt(self.x**2 + self.y**2)

    def distance_to(self, p):
        """Calculate the distance between two points."""
        return (self - p).length()

    def as_tuple(self):
        """(x, y)"""
        return (self.x, self.y)

    def clone(self):
        """Return a full copy of this point."""
        return Point(self.x, self.y)

    def integerize(self):
        """Convert co-ordinate values to integers."""
        self.x = int(self.x)
        self.y = int(self.y)

    def floatize(self):
        """Convert co-ordinate values to floats."""
        self.x = float(self.x)
        self.y = float(self.y)

    def move_to(self, x, y):
        """Reset x & y coordinates."""
        self.x = x
        self.y = y



class Rect:

    """A rectangle identified by two points.

    The rectangle stores left, top, right, and bottom values.

    Coordinates are based on screen coordinates.

    origin                               top
       +-----> x increases                |
       |                           left  -+-  right
       v                                  |
    y increases                         bottom

    set_points  -- reset rectangle coordinates
    contains  -- is a point inside?
    overlaps  -- does a rectangle overlap?
    top_left  -- get top-left corner
    bottom_right  -- get bottom-right corner
    expanded_by  -- grow (or shrink)
    """

    def __init__(self, pt1, pt2):
        """Initialize a rectangle from two points."""
        self.set_points(pt1, pt2)

    def set_points(self, pt1, pt2):
        """Reset the rectangle coordinates."""
        (x1, y1) = pt1.as_tuple()
        (x2, y2) = pt2.as_tuple()
        self.left = min(x1, x2)
        self.top = min(y1, y2)
        self.right = max(x1, x2)
        self.bottom = max(y1, y2)
        self.width = self.right- self.left
        self.height = self.bottom - self.top
        self.area = self.width * self.height

    def contains(self, pt):
        """Return true if a point is inside the rectangle."""
        x,y = pt.as_tuple()
        return (self.left <= x <= self.right and
                self.top <= y <= self.bottom)

    def overlaps(self, other):
        """Return true if a rectangle overlaps this rectangle."""
        return (self.right > other.left and self.left < other.right and
                self.top < other.bottom and self.bottom > other.top)

    def top_left(self):
        """Return the top-left corner as a Point."""
        return Point(self.left, self.top)

    def bottom_right(self):
        """Return the bottom-right corner as a Point."""
        return Point(self.right, self.bottom)

    def __str__( self ):
        return "<Rect (%s,%s)-(%s,%s)>" % (self.left,self.top,
                                           self.right,self.bottom)

    def __repr__(self):
        return "%s(%r, %r)" % (self.__class__.__name__,
                               Point(self.left, self.top),
                               Point(self.right, self.bottom))




def foldersAsList(path):
    folders=[]
    while 1:
        path,folder=os.path.split(path)

        if folder!="":
            folders.append(folder)
        else:
            if path!="":
                folders.append(path)

            break

    folders.reverse()
    return folders

def getPositionFromString(pointString):

    pointString = pointString.replace("(","").replace(")","")
    coordinates = pointString.split(",")
    return float(coordinates[0]), float(coordinates[1])



def positionListFromFile(fileName):
    finalResult = []
    with open(fileName,'r') as f:
        for lineno, line in enumerate(f):
            if lineno == 0:
                numberOfTimeSteps  = int(line.split(": ")[1].strip())
            elif lineno == 1:
                numberOfAgents = int(line.split(": ")[1].strip())
            else:
                positionListForTimeStep = line.split("\t")
                count = 0
                positionList = []
                for position in positionListForTimeStep:
                    if len(position.strip())>0:
                        count = count+1
                        x,y = getPositionFromString(position.strip())
                        positionDict = Point(x*10,y*10)
                        positionList.append(positionDict)
                finalResult.append(positionList)
                # print positionList
                if count!=numberOfAgents:
                    print "number of agents not right"+str(count)+"!="+str(numberOfAgents)
    return finalResult



def findGridOfPosition(gridList, position):
    for grid in gridList:
        if grid.contains(position):
            return grid


def countHotSpots(histogram):
    result  =0
    for grid in histogram.keys():
        numberOfPeople = histogram[grid]

        density = (numberOfPeople*100)/ grid.area
        if density >=5 :
            result = result +1

    return result

def getGridsForScenarioOne():
    entryRect = Rect(Point(138,30), Point(152,40))
    bigArea = Rect(Point(30,40), Point(260,250))
    grids = []
    for i in range(bigArea.left, bigArea.right, 10):
        for j in range(bigArea.top, bigArea.bottom, 10):
            grids.append(Rect(Point(i,j), Point(i+10, j+10)))
    grids.append(entryRect)
    return grids

def getGridsForScenarioTwo():
    tunnelArea = Rect(Point(130,20), Point(160,160))
    bigArea = Rect(Point(30,160), Point(260,330))
    grids = []
    for i in range(bigArea.left, bigArea.right, 10):
        for j in range(bigArea.top, bigArea.bottom, 10):
            grids.append(Rect(Point(i,j), Point(i+10, j+10)))
    for i in range(tunnelArea.left, tunnelArea.right, 10):
        for j in range(tunnelArea.top, tunnelArea.bottom, 10):
            grids.append(Rect(Point(i,j), Point(i+10, j+10)))
    # g''rids.append(entryRect)
    return grids



def getGridsForScenarioEight():
    tunnel1 = Rect(Point(110,47), Point(155,137))
    tunnel2 = Rect(Point(155,127), Point(170,137))
    tunnel3 = Rect(Point(170,127), Point(200,172))
    tunnel4 = Rect(Point(150,155), Point(170,172))
    tunnel5 = Rect(Point(110,155), Point(150,225))
    bigArea = Rect(Point(40,220), Point(250,350)) # actual should be 253

    grids = []
    for i in range(bigArea.left, bigArea.right, 10):
        for j in range(bigArea.top, bigArea.bottom, 10):
            grids.append(Rect(Point(i,j), Point(i+10, j+10)))

    for i in range(tunnel1.left, tunnel1.right, 15):
        for j in range(tunnel1.top, tunnel1.bottom, 10):
            grids.append(Rect(Point(i,j), Point(i+15, j+10)))

    grids.append(tunnel2)

    for i in range(tunnel3.left, tunnel3.right, 10):
        for j in range(tunnel3.top, tunnel3.bottom, 15):
            grids.append(Rect(Point(i,j), Point(i+10, j+15)))

    grids.append(tunnel4)

    for i in range(tunnel5.left, tunnel5.right, 10):
        for j in range(tunnel5.top, tunnel5.bottom, 10):
            grids.append(Rect(Point(i,j), Point(i+10, j+10)))
    for i in range(bigArea.left, bigArea.right, 10):
        for j in range(bigArea.top, bigArea.bottom, 10):
            grids.append(Rect(Point(i,j), Point(i+10, j+10)))
    return grids

def getGridsForScenario(scenarioNumber):
    if scenarioNumber == 1 :
        return getGridsForScenarioOne()
    elif scenarioNumber == 2 :
        return getGridsForScenarioTwo()
    # elif scenarioNumber == 3 :
        # return getGridsForScenarioThree()
    elif scenarioNumber == 8 :
        return getGridsForScenarioEight()


def getVisitHistogram(grids, positionList):
    histogram = {}
    for grid in grids:
        histogram[grid] = 0
    for position in positionList:
        locationGrid = findGridOfPosition(grids, position)
        if locationGrid is not None:
            histogram[locationGrid] = histogram[locationGrid] +1
    return histogram

'''
Total number of hotspots normalized by length of simulation
'''
def getTotalNumberOfHotSpots(grids, positionList):
    hotSpotsPerTimeStep = getHotSpotNumberPerTimeStep(grids, positionList)
    
    return reduce(lambda x, y: x+y,hotSpotsPerTimeStep)

def getHotSpotNumberPerTimeStep(grids, positionList):
    numberOfHotSpotsPerTimeStep = []
    for positionListForTimeStep in positionList:
        histogram = getVisitHistogram(grids, positionListForTimeStep)

        numberOfHotSpotsPerTimeStep.append(countHotSpots(histogram))

    return numberOfHotSpotsPerTimeStep


'''
Get density at each grid aggregated by aggregateFunction
'''
def getDensityMap(grids, positionList, aggregateFunction):
    densityMap = {}
    for grid in grids:
        densityMap[grid] = 0
    maxGridDensity = 0
    for positionListForTimeStep in positionList:
        histogram = getVisitHistogram(grids, positionListForTimeStep)

        for grid in grids:
            gridDensity = (histogram[grid] *100)/ grid.area
            maxGridDensity = max(gridDensity,maxGridDensity)
            densityMap[grid] = aggregateFunction(densityMap[grid],gridDensity)
    print maxGridDensity
    return densityMap

def getMeanAndSDOfTotalHotspots(fileList, scenarioNumber):

    totalNumberOfHotSpots = []
    for num,fileName in enumerate(fileList):
        print("processing run number {0}".format(num+1))
        positionList = positionListFromFile(fileName)
        numberOfHotSpots = getTotalNumberOfHotSpots(
                    getGridsForScenario(scenarioNumber),
                    positionList)
        totalNumberOfHotSpots.append(numberOfHotSpots)
    if(len(totalNumberOfHotSpots)>0):
        mean = np.mean(totalNumberOfHotSpots)
        sd = stats.sem(totalNumberOfHotSpots)
        return mean, sd
    else:
        return -1, -1

def readMeanListSdListFromFile(logFile):
    resultMean = []
    resultSError = []
    
    with open(logFile) as f:
        for lineno, line in enumerate(f):
            if lineno == 0:
                continue
            parts = line.split(",")
            resultMean.append(float(parts[0].strip()))
            resultSError.append(float(parts[1].strip()))

    return resultMean, resultSError

def plotLineGraph(meanList, sdList, x_axis, givenLabel, givenFileName):
    

    fig = plt.figure(num=None, figsize=(50, 12), dpi=80)
    # ax2 = fig.add_subplot(111)

    plt.errorbar(x=x_axis, y=meanList, yerr=sdList, fmt='r-', label=givenLabel)
    # font = {'size': 30}

    # rc('font', **font)
    #for tick in yaxis.get_major_ticks():
    #    tick.label.set_fontsize(30);
    #for tick in yaxis.get_major_ticks():
    #    tick.label.set_fontsize(30);

    
    plt.savefig(givenFileName, pad_inches=0)
    plt.close()

def writeMeanSdToFile(meanList, sErrorList, name):
    print "Writing to {0}.csv".format(name)
    # print meanList
    # print sErrorList
    with open("{0}.csv".format(name),'w') as logFile:
        toWrite = "{0}, {1}\n".format('mean', 'sd')
        logFile.write(toWrite)
        for mean,sd in zip(meanList, sErrorList): 
            # print mean
            toWrite = "{0}, {1}\n".format(mean,sd)
            logFile.write(toWrite)
    print "done"

def plotHotSpotsOverTime(fileList, scenarioNumber, dataType):
    resultFileName = "HotSpotsOverTime-Scenario{0}-{1}".format(scenarioNumber, dataType)    
    try:
        with open("{0}.csv".format(resultFileName)) as f:
            resultMean, resultSError = readMeanListSdListFromFile("{0}.csv".format(resultFileName))
            x_axis = range(1, len(resultMean)+1, 1)
            plotLineGraph(resultMean, resultSError,x_axis, "hot Spot Number Per TimeStep", "{0}.png".format(resultFileName))
    except IOError:
        hotSpotNumberPerTimeStepList = []
        grids = getGridsForScenario(scenarioNumber)
        maxTimeStep = 0
        for num,fileName in enumerate(fileList):
            positionList = positionListFromFile(fileName)
            hotSpotNumberPerTimeStep = getHotSpotNumberPerTimeStep(grids, positionList)
            hotSpotNumberPerTimeStepList.append(hotSpotNumberPerTimeStep)
            maxTimeStep = max(maxTimeStep, len(hotSpotNumberPerTimeStep))
        resultMean = []
        resultSError = []
        for timeStep in range(0, maxTimeStep):
            total  =0
            listOfValuesForTimeStep = []
            for hotSpotNumberPerTimeStep in hotSpotNumberPerTimeStepList:
                if timeStep < len(hotSpotNumberPerTimeStep):
                    listOfValuesForTimeStep.append(hotSpotNumberPerTimeStep[timeStep])
            mean = np.mean(listOfValuesForTimeStep)
            sd = stats.sem(listOfValuesForTimeStep)
            resultMean.append(mean)
            resultSError.append(sd)

        print "calculation done"
        
        print "saving to file"
        writeMeanSdToFile(resultMean, resultSError, resultFileName)
        print "plotting graph"
        resultMean, resultSError = readMeanListSdListFromFile("{0}.csv".format(resultFileName))
        x_axis = range(1, len(resultMean)+1, 1)
        plotLineGraph(resultMean, resultSError,x_axis, "hot Spot Number Per TimeStep", "{0}.png".format(resultFileName))
    

# def colorForValue(value):
#     cmap = plt.get_cmap('RdYlBu')
#     return cmap(1 - (value/7.0)) # 9 is about the max density that is possible in a grid

def displayHeatMap(gridDensityMap):
    plt.axes()
    value = -1
    for grid in gridDensityMap.keys():
        cell = plt.Rectangle(
            (grid.left, grid.top),
            grid.width,grid.height,
            fc=colorForValue(gridDensityMap[grid]))
        plt.gca().add_patch(cell)
        value = max(value, gridDensityMap[grid])
    plt.axis('scaled')
    plt.show()

def saveLogFile(gridDensityMap, name):
    with open("{0}.csv".format(name),'w') as logFile:
        toWrite = "{0}, {1} , {2}, {3}, {4}\n".format('left', 'top', 'width', 'height', 'value')
        logFile.write(toWrite)
        for grid in gridDensityMap.keys(): 
            toWrite = "{0}, {1} , {2}, {3}, {4}\n".format(grid.left, grid.top, grid.width, grid.height, gridDensityMap[grid])
            logFile.write(toWrite)

def saveHeatMap(name):
    plt.axes()

    min, max = (0, 7)
    step = 1

    # Setting up a colormap that's a simple transtion
    mymap = mpl.colors.LinearSegmentedColormap.from_list('mycolors',['blue','yellow', 'red'])

    # Using contourf to provide my colorbar info, then clearing the figure
    Z = [[0,0],[0,0]]
    levels = range(min,max+step,step)
    CS3 = plt.contourf(Z, levels, cmap=mymap)
    plt.clf()
    
    with open("{0}.csv".format(name)) as logFile:
        
        for lineno,line in enumerate(logFile):
            if lineno == 0:
                continue
            # toWrite = "{0}, {1} , {2}, {3}, {4}\n".format(grid.left, grid.top, grid.width, grid.height, gridDensityMap[grid])
            parts = line.split(',')
            cell = plt.Rectangle(
                (int(parts[0].strip()), int(parts[1].strip())),
                int(parts[2].strip()),int(parts[3].strip()),
                fc=mymap((int(parts[4].strip()))/7.0))
            plt.gca().add_patch(cell)
            
    plt.axis('scaled')
    plt.colorbar(CS3)
    font = {'size': 25}
    plt.xticks(np.arange(0, 400, 100))
    plt.yticks(np.arange(0, 400, 100))
    rc('font', **font)
    # plt.colorbar()

    
    plt.savefig("{0}.png".format(name), pad_inches=0)
    plt.close()


def saveMaxDensityHeatMapForEachRun(fileList, scenarioNumber, typeOfData):

    for num,fileName in enumerate(fileList):
        print("processing run number {0}".format(num+1))
        positionList = positionListFromFile(fileName)
        gridDensityMap = getDensityMap(
                    getGridsForScenario(scenarioNumber),
                    positionList, lambda x,y: max(x,y) )
        saveHeatMap(gridDensityMap, "max-{0}-{1}-{2}".format(typeOfData, scenarioNumber, num))

def saveMeanDensityHeatMapForEachRun(fileList, scenarioNumber, typeOfData):

    for num,fileName in enumerate(fileList):
        print("processing run number {0}".format(num+1))
        positionList = positionListFromFile(fileName)
        gridDensityMap = getDensityMap(
                    getGridsForScenario(scenarioNumber),
                    positionList, lambda x,y: x+y)
        for grid in gridDensityMap.keys():
            gridDensityMap[grid] = gridDensityMap[grid]/ len(positionList)
        saveHeatMap(gridDensityMap, "mean-{0}-{1}-{2}".format(typeOfData,scenarioNumber, num))

def averageDensityMapForList(grids, listOfDensities):
    result = {}
    for grid in grids:
        result[grid] = 0
    for densityMap in listOfDensities:
        for grid in densityMap.keys():
            result[grid] = result[grid] + densityMap[grid]
    for grid in result.keys():
        result[grid] = result[grid] / len(listOfDensities)

    return result


def saveMaxDensityHeatMapAverageForAll(fileList, scenarioNumber, typeOfData):
    listOfDensities = []
    
    resultFileName = "Average of max-{0} ({1})".format(scenarioNumber, typeOfData)
    try:
        with open("{0}.csv".format(resultFileName)) as f:
           saveHeatMap(resultFileName)
    except IOError:
        grids = getGridsForScenario(scenarioNumber)
        for num,fileName in enumerate(fileList):
            # print("processing run number {0}".format(num+1))
            positionList = positionListFromFile(fileName)
            gridDensityMap = getDensityMap(
                        grids,
                        positionList, lambda x,y: max(x,y) )
            listOfDensities.append(gridDensityMap)
        resultGridDensityMap = averageDensityMapForList(grids, listOfDensities)
        saveLogFile(resultGridDensityMap,resultFileName)
        saveHeatMap(resultFileName)

def saveMeanDensityHeatMapAverageForAll(fileList, scenarioNumber, typeOfData):
    listOfDensities = []
    grids = getGridsForScenario(scenarioNumber)
    for num,fileName in enumerate(fileList):
        print("processing run number {0}".format(num+1))
        positionList = positionListFromFile(fileName)
        gridDensityMap = getDensityMap(
                    grids,
                    positionList, lambda x,y: x+y)
        for grid in gridDensityMap.keys():
            gridDensityMap[grid] = gridDensityMap[grid]/ len(positionList)
        listOfDensities.append(gridDensityMap)
    resultGridDensityMap = averageDensityMapForList(grids, listOfDensities)
    saveHeatMap(resultGridDensityMap, "Average of mean-{0} ({1})".format(scenarioNumber, typeOfData))

def plotHistogramOfHotSpotValues(fileList, scenarioNumber, dataType):
    densityNumberMap = {}
    for i in range(0,10):
        densityNumberMap[i] = 0
    grids = getGridsForScenario(scenarioNumber)
    for num,fileName in enumerate(fileList):
        print("processing run number {0}".format(num+1))
        positionList = positionListFromFile(fileName)
        gridDensityMap = getDensityMap(
                    grids,
                    positionList, lambda x,y: max(x,y))
        #print gridDensityMap
        for grid in gridDensityMap.keys():
            densityValue = round(gridDensityMap[grid])

            densityNumberMap[densityValue]=densityNumberMap[densityValue]+1
    print densityNumberMap




def getAllSeedFileList(finalDataSet, holdingProbability, trust, scenarioNumber):
    finalList = []
    for attributeSet in finalDataSet:
        if attributeSet["holdingProbability"]== holdingProbability and attributeSet["trust"] ==trust and attributeSet["scenarioNumber"] == scenarioNumber:
            finalList.append(attributeSet["filePath"])
    return finalList


def loadDataSetDictionaryFromFolder(currentDir):
    rootFolderList = foldersAsList(currentDir)

    finalDataSet = []
    for root, dirs, files in os.walk(currentDir): # Walk directory tree
        folderListForFile = foldersAsList(root)
        folderListForFile = [folder for folder in folderListForFile if folder not in rootFolderList]
        if len(folderListForFile) ==3:
            scenarioFile = folderListForFile[0]
            holdingProbability = folderListForFile[1]
            trust = folderListForFile[2]
            totalNumberOfHotSpots = []
            for root1, dirs1, files1 in os.walk(os.path.join(root)):
                # print (scenarioFile +" : " + holdingProbability+" : " + trust )
                for fileName in files1:
                    parts = fileName.split("_")
                    dataType = parts[2].lower()
                    scenarioNumber = int(scenarioFile[len(scenarioFile)-1])
                    seed = parts[1]
                    if dataType == "position":
                        fullFilePath = os.path.join(root,fileName)
                        attributeSet = {}
                        attributeSet["scenarioNumber"] = scenarioNumber
                        attributeSet["seed"] = seed
                        attributeSet["holdingProbability"] = holdingProbability
                        attributeSet["trust"] = trust
                        attributeSet["filePath"] = fullFilePath
                        finalDataSet.append(attributeSet)
    return finalDataSet




def main():
    print "loading initial data"
    dataFolder = os.path.join(os.path.dirname(os.path.realpath(__file__)),"PosVelText")
    finalDataSet = loadDataSetDictionaryFromFolder(dataFolder)

    NUMBER_OF_SEEDS_TO_USE = 20

    scenarioList = [1,2]
    # scenarioList = [8]
    for scenarioNumber in scenarioList:
        print "Processing scenario number {0}".format(scenarioNumber)
        


        
        holdingProbability = "0.0"
        trust = "1.0"

        print "Without device"
        fileList = getAllSeedFileList(
            finalDataSet, holdingProbability, trust, scenarioNumber)[0:NUMBER_OF_SEEDS_TO_USE]

        
        print "loaded {0} files".format(len(fileList))

        # plotHistogramOfHotSpotValues(fileList, scenarioNumber, "without device")
        
        # print "Calculating max density heat map for each run"
        # saveMaxDensityHeatMapForEachRun(fileList, scenarioNumber, "without device")
        
        print "Calculating average of max density heat maps"
        saveMaxDensityHeatMapAverageForAll(fileList, scenarioNumber, "without device")
        print "Plotting unsafe regions over time"
        plotHotSpotsOverTime(fileList, scenarioNumber, "without device")

        holdingProbability = "1.0"
        trust = "1.0"
        print "With device"
        fileList = getAllSeedFileList(
                finalDataSet, holdingProbability, trust, scenarioNumber)[0:NUMBER_OF_SEEDS_TO_USE]
        print "loaded {0} files".format(len(fileList))

        


        # print "Calculating max density heat map for each run"
        # saveMaxDensityHeatMapForEachRun(fileList, scenarioNumber, "with device")
            
        
        print "Calculating average of max density heat maps"
        saveMaxDensityHeatMapAverageForAll(fileList, scenarioNumber, "with device")

        print "Plotting unsafe regions over time"
        plotHotSpotsOverTime(fileList, scenarioNumber, "with device")

       

        
        
        print "Calculating effect of percentage of people holding device"
        #HOLDING PROB PLOT
        meanList = []
        sdList = []
        HoldingProbFileName = "HoldingProb-Scenario{0}".format(scenarioNumber)
        try:
            with open("{0}.csv".format(HoldingProbFileName)) as f:
                meanList, sdList = readMeanListSdListFromFile("{0}.csv".format(HoldingProbFileName))
                
        except IOError:
            for holdingProbability in range(0,11,1):

                hp = str(float(holdingProbability)/10.0)
                print "calculating for hp={0}".format(hp)
                fileList = getAllSeedFileList(
                  finalDataSet, hp, "1.0", scenarioNumber)[0:NUMBER_OF_SEEDS_TO_USE]
                mean, sd = getMeanAndSDOfTotalHotspots(fileList, scenarioNumber)
                meanList.append(mean)
                sdList.append(sd)
            writeMeanSdToFile(meanList, sdList,HoldingProbFileName)

        print ("starting plotting")
        plotLineGraph(meanList, sdList,[float(i)/10.0 for i in range(0,len(meanList))],"total number of unsafe regions",HoldingProbFileName)

        print "Calculating effect of trust given everyone has device"
        #TRUST PLOT
        meanList = []
        sdList = []
        TrustProbFileName = "TrustProb-Scenario{0}".format(scenarioNumber)
        try:
            with open("{0}.csv".format(TrustProbFileName)) as f:
                meanList, sdList = readMeanListSdListFromFile("{0}.csv".format(TrustProbFileName))
                
        except IOError:
            for trustValue in range(0,11,1):

                trust = str(float(trustValue)/10.0)
                print "calculating for trust={0}".format(trust)
                fileList = getAllSeedFileList(
                  finalDataSet, "1.0", trust, scenarioNumber)[0:NUMBER_OF_SEEDS_TO_USE]
                mean, sd = getMeanAndSDOfTotalHotspots(fileList, scenarioNumber)
                meanList.append(mean)
                sdList.append(sd)
            writeMeanSdToFile(meanList, sdList,TrustProbFileName)
        print ("starting plotting")
        plotLineGraph(meanList, sdList,[float(i)/10.0 for i in range(0,len(meanList))],"total number of unsafe regions","Trust Prob-Scenario {0}".format(scenarioNumber))


if __name__ == '__main__':
    main()

