// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)

// Put your code here.
@R0
D=M
@first //@16
M=D

@R1
D=M
@second //@17
M=D

@sum //@18
M=0

(LOOP)
@second //if second <= 0 than goto end
D=M
@STORE_R
D;JLE

@sum //sum += first
D=M
@first
D=D+M
@sum
M=D

@second //second -= 1
M=M-1

@LOOP
0;JMP

    (STORE_R)
@sum
D=M
@R2
M=D

(END)
@END
0;JMP

//if (second>0) {
//    sum += first
//    second -= 1
//} else {
//    R2=sum
//}
