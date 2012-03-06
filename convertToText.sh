#!/bin/bash
# convertToText= converts in place all the files in the specified folder to text

opath=$PATH
PATH=/bin:/usr/bin

case $# in
	0) echo 'convertToText folder' 1>&2; exit 1
esac

		 	


latticefiles=`find $1 \( ! -name "*.txt" \) -and \( ! -name "*.svn-base" \) -type f|grep -i lattice`

java -cp dist/CrowdSimulation.jar app.dataTracking.DataReader lattice $latticefiles

nonlatticefiles=`find $1 \( ! -name "*.txt" \) -and \( ! -name "*.svn-base" \) -type f|egrep '(Position)|(Velocity)'`

java -cp dist/CrowdSimulation.jar app.dataTracking.DataReader float $nonlatticefiles