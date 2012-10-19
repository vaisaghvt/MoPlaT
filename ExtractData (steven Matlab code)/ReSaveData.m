%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Measure the total distance traveled by agent (Average over simulations)
% Applied to Social Force and RVO2 only
%
%
% Author: Steven Lee Chong Eu
% Last Update: 29-May-2012
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

Name = 'ReSaveData';

filepath = ['DATA/Scenario' Scenario '-' num2str(Nagent) '/' Model '/'];
cd(filepath);

for s=Nsim:-1:50
    disp(['Smoothing ' Model '-' Scenario '-' num2str(Nagent) '-' Name '-Sim ' num2str(s) '...'])
    load(['Data' num2str(s) '.mat']);
    
    [Px,Py] = SmoothTrajMass(Px,Py);
    
    save(['Data' num2str(s) 's.mat'],'Px','Py')
    
end
eval('cd ..');
eval('cd ..');
eval('cd ..');