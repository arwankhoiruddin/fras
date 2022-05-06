close all;
clear;
clc;

default = readmatrix('makespanDefault.txt');
fras = readmatrix('makespanFRAS.txt');
fair = readmatrix('makespanFAIR.txt');

rerata = [];
standar = [];
reratafras = [];
standarfras = [];
reratafair = [];
standarfair = [];

uk = size(default, 2);

for i=1:uk
    rerata = [rerata mean(default(:, i))];
    standar = [standar std(default(:, i))];
   
    reratafras = [reratafras mean(fras(:, i))];
    standarfras = [standarfras std(fras(:, i))];

    reratafair = [reratafair mean(fair(:, i))];
    standarfair = [standarfair std(fair(:, i))];
end

x = 0:uk-1;

figure
xlabel('CPU standard deviation');
ylabel('Make Span');
hold on
errorbar(x, rerata, standar, 'r');
errorbar(x, reratafras, standarfras, 'g');
errorbar(x, reratafair, standarfair, 'b');
legend('FIFO', 'FRAS', 'FAIR');
hold off