close all;
clear;
clc;

dmean = 12;
dstd = 2;

a = [];

for i=1:1000
    n = round((rand() * dstd));
    if (rand() < 0.5) 
        v = dmean - n;
    else
        v = dmean + n;
    end
    a = [a v];
end

figure,
plot(a)