for cluster in "c0-1 10 1" "c0-2 12 1" "c0-3 3 1" "c0-4 4 1" "c0-5 5 1" "c0-7 6 1" "c0-10 11 1" "c0-11 2 1" "c0-12 9 1" "c0-0 13 1" "c0-14 14 1"
do
	set -- $cluster
        ssh $1 "cd CrowdSimulation; nohup java -cp dist/CrowdSimulation.jar app.RVOModel -filename xml-resources/CrowdProperties/LatticeTestSettings_$2.xml -repeat 100 -seed $3 2> $2.log 1> $2_1.log </dev/null &"
        echo "assigned job $2 with seed $3 to $1"
done
