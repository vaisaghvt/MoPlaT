disp('Extracting Lattice...')
% Scenario = '1';
Model = 'RVO2';
% Nagent = 100;
Nsim = 100;

goToPath(Model,Scenario,Nagent)
for s=1:Nsim
    disp(['Extracting ' Model '-' Scenario '-' num2str(Nagent) '-folder ' num2str(s) '...'])
    filePathNumber = num2str(s);
    eval(['cd ' filePathNumber])
    
    fileNameFront = ['Scenario' Scenario '-' num2str(Nagent) '_' Model '_' filePathNumber '_'];
    
    Ls = importdata([fileNameFront 'LatticeState.txt']);
    
    Tsim = size(Ls,1)/100;
    Px = cell(Tsim,1);
    Py = cell(Tsim,1);
    for t=1:Tsim
        simSnapShot = Ls(1+(t-1)*100:t*100,1:100);
        [py,px] = find(simSnapShot==1);
        Px{t}=px';
        Py{t}=py';
    end
    eval('cd ..');  % OUT TO SCENARIO FOLDER
    eval('cd ..');
    eval('cd Lattice/');
    
    save(['Data' num2str(s)],'Px','Py','s')
    
    eval('cd ..');  % OUT TO SCENARIO FOLDER
    eval(['cd ' Model])
end
eval('cd ..')
eval('cd Lattice/');
[Wy(t,:),Wx(t,:)] = find(simSnapShot==2);
save('LatticeDataMap','Wx','Wy')
eval('cd ..');

% for s=1:100
%     disp(['Clearing Ls' num2str(s)])
%     load(['Data' num2str(s) '.mat'])
%     clear Ls
%     save(['Data' num2str(s) '.mat']);
% end
eval('cd ..');
eval('cd ..');