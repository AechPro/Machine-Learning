import numpy as np
from openpyxl import load_workbook
from scipy.optimize import fsolve
workingDirectory = "C:/Users/Matt/Desktop/abs to conc/1. Abs to Conc (for Matt)"

def padConcatenate(matrices,shapes):
    largestAxis = np.max(shapes)
    for matrix in matrices:
        if len(matrix) != largestAxis:
            for _ in range(abs(largestAxis - len(matrix))):
                matrix.append([np.nan for __ in range(len(matrix[0]))])
    concatenatedMatrix = np.concatenate(matrices,axis=1)
    return concatenatedMatrix
def load_values(directory, workbooks):
    matrices = []
    for entry in workbooks:
        workbook = load_workbook(''.join([directory, '/', '0. BT474_1 (abs).xlsx']))
        sheet = workbook[entry]
        matrix = []
        for row in sheet.rows:
            matrix.append([])
            for cell in row:
                if cell.value == None:
                    matrix[len(matrix) - 1].append(np.nan)
                else:
                    matrix[len(matrix) - 1].append(cell.value)
        matrices.append(matrix)
    return matrices

colors = ["dual", "red", "blue", "uns"]
matrices = load_values(workingDirectory,colors)
matrixLengths = np.asarray([(len(i[0]), len(i)) for i in matrices])
paddedMatrices = padConcatenate(matrices,matrixLengths)
matrices = np.asarray(matrices)
cell_bkgd_4_avg = np.nanmean(matrices[-1][:,0])
cell_bkgd_4_SD = np.nanstd(matrices[-1][:,0])
cell_bkgd_6_avg = np.nanmean(matrices[-1][:,1])
cell_bkgd_6_SD = np.nanstd(matrices[-1][:,1])
SD_multi = 1
cell_bkgd_4_cutoff = cell_bkgd_4_avg + SD_multi * cell_bkgd_4_SD
cell_bkgd_6_cutoff = cell_bkgd_6_avg + SD_multi * cell_bkgd_6_SD
absNoBackground = paddedMatrices.copy()
for i in range(0,2,8):
    absNoBackground[:,i] = paddedMatrices[:,i] - cell_bkgd_4_cutoff
    absNoBackground[:,i+1] = paddedMatrices[:,i+1] - cell_bkgd_6_cutoff

"""

% Convert Abs to Conc

for i = 1:4
    for j = 1:abs_length(i) 
        abs = abs_all_NObkgd(j,i+(i-1):i+(i-1)+1);
       % abs = abs_all(j,i+(i-1):i+(i-1)+1);
        if abs(1) ~= 'NaN'
            x0 = [0,1];
            x = fsolve(@(x)abs2conc(x,abs),x0);
            conc_all(j,i+(i-1):i+(i-1)+1) = x;
        end
    end
end

csvwrite('conc_all.xlsx',conc_all); 

return

function F = abs2conc(x,abs)
% fitted curves for red and blue dyes
% Y=Y0 + (Plateau-Y0)*(1-exp(-K*x))

%   [Red_470,     Red_625,    Blue_470,   Blue_625] HRP 03-20-17
Y0 =[0.04506        0.02659     0.0511      0.0199];
P = [0.719          0.3026      0.2012      0.7079];  
K = [3.597          4.145       1.474       4.393];

F(1) = abs(1) - (Y0(1) + (P(1)-Y0(1))*(1-exp(-K(1)*x(1)))) - (Y0(3) + (P(3)-Y0(3))*(1-exp(-K(3)*x(2)))) ;
F(2) = abs(2) - (Y0(2) + (P(2)-Y0(2))*(1-exp(-K(2)*x(1)))) - (Y0(4) + (P(4)-Y0(4))*(1-exp(-K(4)*x(2)))) ;

return
"""
def F(x,abs):
    Y0 = [0.04506,0.02659,0.0511,0.0199]
    P = [0.719,0.3026,0.2012,0.7079]
    K = [3.597,4.145,1.474,4.393]
    out = [0,0]
    out[0] = abs[0][0] - (Y0[0] + (P[0]-Y0[0])*(1-np.exp(-K[0]*x[0]))) - (Y0[2] + (P[2] - Y0[2])*(1-np.exp(-K[2]*x[1])))
    out[1] = abs[0][1] - (Y0[1] + (P[1]-Y0[1])*(1-np.exp(-K[1]*x[0]))) - (Y0[3] + (P[3] - Y0[3])*(1-np.exp(-K[3]*x[1])))
    return out
for i in range(4):
    for j in range(len(matrices[i])):
        abs = absNoBackground[j,i+i-1:i+i+1]
        if len(abs)>=1:
            if not np.isnan(abs[0]):
                x0 = np.asarray([0,1])
                x = fsolve(F,x0,args=[abs])
                print(x)