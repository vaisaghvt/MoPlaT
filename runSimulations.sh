#!/bin/bash
# runSimulations = runsSimulations from inputs in file1

opath=$PATH
PATH=/bin:/usr/bin

case $# in
	0|1) echo 'Usage runSimulations settings File xmlFile' 1>&2; exit 1
esac


awk -v xmlFile=$2 '
BEGIN {totalCount=1
		startingPoint[1]=1}
{	
	model[NR] = $1
	startingPoint[NR+1] = startingPoint[NR]+NF-1
	for(i=2;i<=NF;i++){
		completeValuesList[totalCount] = $i
		totalCount++
	}

}
END {
	startingPoint[NR+1] = totalCount-1
	for (j=0; j<=NR; j++){
		indices[j] = 0
	}

	while(indices[0]!=1){
		timeNeeded=0

		for(j=1;j<=NR;j++){
			value[j] = completeValuesList[startingPoint[j]+indices[j]]
		 	command = "overwrite " xmlFile " xmlParser " model[j] " " value[j] " " xmlFile
		 	# print command
		 	system(command)		
		}
		testCommand = "grep FilePath " xmlFile;
		testCommand |getline filePathLine
		close(testCommand)

		c = "timeNeeded " filePathLine;
        c |getline runTime;
        close(c)

		javaCommand = "java -cp dist/CrowdSimulation.jar app.RVOModel -time 50 -for " runTime
		print javaCommand
		system(javaCommand)
		for(j=NR;j>=1;j--){
			if(startingPoint[j]+indices[j]==startingPoint[j+1]){
				indices[j]=0
				indices[j-1]++
			}else {
				if(j==NR){
					indices[j]++
				}
			}
		}
	}
}' $1