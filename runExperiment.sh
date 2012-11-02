for cluster in "c0-11 14 1 25" "c0-12 14 26 25" "c0-0 14 51 25" "c0-14 14 76 25"
do
	set -- $cluster
        ssh $1 "cd CrowdSimulation; nohup java -cp dist/CrowdSimulation.jar app.RVOModel -filename xml-resources/CrowdProperties/LatticeTestSettings_$2.xml -repeat $4 -seed $3 2> $2.log 1> $2_1.log </dev/null &"
        echo "assigned job $2 with seed $3 to $1"
done
