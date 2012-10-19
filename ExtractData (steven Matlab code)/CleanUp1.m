Scenario = '1';
Model = 'RVO2';

NagentListR = [50 100 150 200 300 500 1000];
cd('~/Desktop/CrowdData/RESULTS/MAT FILES/');
    
for iR=1:length(NagentListR)
    load([Model Scenario '-' num2str(NagentListR(iR)) 'TrajDistAVG1.mat'])
    save([Model Scenario '-' num2str(NagentListR(iR)) 'TrajDistAVG.mat'],'cDist')
    delete([Model Scenario '-' num2str(NagentListR(iR)) 'TrajDistAVG1.mat']);
end
cd('..')
cd('..')
disp('habis')


