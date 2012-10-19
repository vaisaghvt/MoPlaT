% disp('Extracting Lattice...')
% Scenario = '1';
Model = 'Lattice';
% Nagent = 100;
% Nsim = 1;

goToPath(Model,Scenario,Nagent)

for s=1:Nsim
    filePathNumber = num2str(s);
    
    disp(['Extracting ' Model '-' Scenario '-' num2str(Nagent) '-folder ' filePathNumber '...'])
    cd(filePathNumber);
    
    XX = importdata(['Scenario' Scenario '-' num2str(Nagent) '_RVO2_' filePathNumber '_LatticeState_x.txt'],',');
    YY = importdata(['Scenario' Scenario '-' num2str(Nagent) '_RVO2_' filePathNumber '_LatticeState_y.txt'],',');
    
    TT = XX(1);
    
    Px = reshape(XX(2:end),Nagent,TT)';
    Py = reshape(YY(2:end),Nagent,TT)';
    
    cd('..')
    save(['Data' filePathNumber '.mat'],'Px','Py');
end

eval('cd ..');
eval('cd ..');
eval('cd ..');