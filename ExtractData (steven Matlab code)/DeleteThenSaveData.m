Scenario = '3';
Model = 'RVO2';

Nagent = 200;
filepath = ['Scenario' Scenario '-' num2str(Nagent) '/' Model '/'];
eval(['cd ' filepath]);
    
for s=1:100
    load(['Data' num2str(s) '.mat'])
    clear kk
    save(['Data' num2str(s) '.mat'])
    clear
end
eval('cd ..');
eval('cd ..');
disp('habis')


