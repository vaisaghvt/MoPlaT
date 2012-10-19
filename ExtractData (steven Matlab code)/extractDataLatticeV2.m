disp('Extracting Lattice...')
% Scenario = '1';
Model = 'Lattice';
% Nagent = 100;
Nsim = 1;

goToPath(Model,Scenario,Nagent)

for s=1:100
    filePathNumber = num2str(s);
    
    disp(['Extracting ' Model '-' Scenario '-' num2str(Nagent) '-folder ' filePathNumber '...'])
    
    fid = fopen(['Scenario' Scenario '-' num2str(Nagent) '_RVO2_' filePathNumber '_LatticeState.txt']);

    t=1;
    while (~feof(fid))
        Nt = cell2mat(textscan(fid,'%d',1));
        if Nt==0
            break
        end
        C = textscan(fid,'%d %d',Nt,'delimiter',',');
        Px{t} = C{1}';
        Py{t} = C{2}';
        t = t+1;
    end
    fclose(fid);

    save(['Data' filePathNumber '.mat'],'Px','Py');
end

eval('cd ..');
eval('cd ..');
eval('cd ..');