close all;
clear;
clc;

x = 1:10:100;
y = [20 30 45 40 60 65 80 75 95 90];
err = 8*ones(size(y));
errorbar(x,y,err)

% dmean = 12;
% dstd = 2;
% 
% a = [];
% 
% for i=1:1000
%     n = round((rand() * dstd));
%     if (rand() < 0.5) 
%         v = dmean - n;
%     else
%         v = dmean + n;
%     end
%     a = [a v];
% end
% 
% figure,
% plot(a)