disp('Extracting RVO2...')
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

    % IMPORTING DATA
    Px = importdata([fileNameFront 'Position_x.txt']);
    Py = importdata([fileNameFront 'Position_y.txt']);
    Vx = importdata([fileNameFront 'Velocity_x.txt']);
    Vy = importdata([fileNameFront 'Velocity_y.txt']);
    eval('cd ..');

    % SAVING MAT FILES
    save(['Data' num2str(filePathNumber) '.mat'],...
        'Scenario','Model','Nagent',...
        'Px','Py','Vx','Vy',...
        's');
end
eval('cd ..');
eval('cd ..');
eval('cd ..');



