close all;
clear;
clc;


% weight assignment
ownCPU = 10;
ownRAM = 5;
neiCPU = 5;
neiRAM = 3;

%cpu = [1, 1, 2, 4, 6, 8, 4];
%ram = [4, 4, 8, 8, 16, 12, 8];

cpu = [1, 1, 1, 1, 1, 1, 1, 1];
ram = [4, 4, 4, 4, 4, 4, 4, 4];

w = zeros(size(cpu, 2), size(cpu, 2));

for i=1:size(cpu, 2)
    for j=1:size(cpu, 2)
        % normalize value of CPU
        oCPU = normCPU(cpu(i));
        nCPU = normCPU(cpu(j));
        % normalize value of RAM
        oRAM = normRAM(ram(i));
        nRAM = normRAM(ram(j));

        w(i,j) = (oCPU * ownCPU) + (oRAM * ownRAM) + (nCPU * neiCPU) + (nRAM * neiRAM);
    end
end

figure, surf(w);

function a = normCPU(c)
    a = c/10;
end

function a = normRAM(r)
    a = r/20;
end