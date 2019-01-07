// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

// Put your code here.

//!!! Screen doesn't react if I comment this declaration
//@ones
(LOOP)

@KBD
D=M

@SET_ONE
D;JNE
@R14
M=0
@END_SET
0;JMP

(SET_ONE)
@R14
M=-1
(END_SET)

@BLACK_SCREEN
0;JMP


//start of black screen
//@R14 which color
(BLACK_SCREEN)
    //i = 256
    @256
    D=A
    @i
    M=D
    
    //index = 0
    @index
    M=0

    (EXTERNAL_LOOP)
	// if(i==0) exit loop
	@i
	D=M
	@END_EXT_LOOP
	D;JEQ

	    //j = 32
	    @32
	    D=A
	    @j
	    M=D

	    (INNER_LOOP)
    	        //if (j==0) goto END_INNER_LOOP
		@j
		D=M
		@END_INNER_LOOP
		D;JEQ
		
		//payload. Count index. Save to @temp. Retreive filling value.
		@index
		D=M
		@SCREEN
		A=A+D
		D=A
		@temp
		M=D
		

		@R14
		D=M
		@temp
		A=M
		M=D

		@j //j=j-1
		M=M-1
		@index //index++
		M=M+1
		
		@INNER_LOOP
		0;JMP
	    (END_INNER_LOOP)

	//i = i-1; goto ext_loop
	@i
	M=M-1
	@EXTERNAL_LOOP
	0;JMP

(END_EXT_LOOP)
//return to the main loop
@1
0;JMP