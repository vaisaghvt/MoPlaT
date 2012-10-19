% disp('Extracting Lattice...')
% Scenario = '1';
Model = 'RVO2';
% Nagent = 100;
% Nsim = 1;

goToPath(Model,Scenario,Nagent)

for s=33:33
    filePathNumber = num2str(s);
    
    disp(['Extracting ' Model '-' Scenario '-' num2str(Nagent) '-folder ' filePathNumber '...'])
    cd(filePathNumber);
    
    XX = importdata(['Scenario' Scenario '-' num2str(Nagent) '_RVO2_' filePathNumber '_Position_x.txt'],',');
    YY = importdata(['Scenario' Scenario '-' num2str(Nagent) '_RVO2_' filePathNumber '_Position_y.txt'],',');
    VX = importdata(['Scenario' Scenario '-' num2str(Nagent) '_RVO2_' filePathNumber '_Velocity_x.txt'],',');
    VY = importdata(['Scenario' Scenario '-' num2str(Nagent) '_RVO2_' filePathNumber '_Velocity_y.txt'],',');
    
    TT = XX(1);
    
    Px = reshape(XX(2:end),Nagent,TT)';
    Py = reshape(YY(2:end),Nagent,TT)';
    Vx = reshape(VX(2:end),Nagent,TT)';
    Vy = reshape(VY(2:end),Nagent,TT)';
    
    cd('..')
    save(['Data' filePathNumber '.mat'],'Px','Py','Vx','Vy');
end

eval('cd ..');
eval('cd ..');
eval('cd ..');