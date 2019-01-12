package bulat.assembler;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Assembler {
    private static Pattern C_COMMAND = Pattern.compile("([AMD]{1,3}=)?(1|0|-1|D|[A|M]|!D|![A|M]|-D|-[A|M]|D\\+1|[A|M]\\+1|D-1|[A|M]-1|D+[A|M]|D-[A|M]|[A|M]-D|D&[A|M]|D\\|[A|M])" +
            "(;(JGT|JEQ|JGE|JLT|JNE|JLE|JMP))?");
    private static Pattern A_COMMAND = Pattern.compile("@(\\w+|\\d+)");
    private static Pattern LINK_NAME = Pattern.compile("\\w+");
    private static Pattern DIGITAL = Pattern.compile("\\d+");

    private static Map<String, Integer> links = new HashMap<>();
    private static Integer VARIABLE_RAM_INDEX = 16;

    private static Map<String, String> COMMAND_BINARY_MAP = new HashMap<>() {{
        put("0", "0101010");
        put("1", "0111111");
        put("-1", "0111010");
        put("D", "0001100");
        put("A", "0110000");put("M", "1110000");
        put("!D", "0001101");
        put("!A", "0110001");put("!M", "1110001");
        put("-D", "0001111");
        put("-A", "0110011");put("-M", "1110011");
        put("D+1", "0011111");
        put("A+1", "0110111");put("M+1", "1110111");
        put("D-1", "0001110");
        put("A-1", "0110010");put("M-1", "1110010");
        put("D+A", "0000010");put("D+M", "1000010");
        put("D-A", "0010011");put("D-M", "1010011");
        put("A-D", "0000111");put("M-D", "1000111");
        put("D&A", "0000000");put("D&M", "1000000");
        put("D|A", "0010101");put("D|M", "1010101");
    }};

    private static Map<String, String> JUMP_BINARY_MAPPING = new HashMap<>() {{
        put("JGT", "001");
        put("JEQ", "010");
        put("JGE", "011");
        put("JLT", "100");
        put("JNE", "101");
        put("JLE", "110");
        put("JMP", "111");
    }};

    public static void main(String[] args) {
        String binaryCode;
        fistTrace(INPUT);
        int lineCounter = 0, commandCounter = 0;
        for (String line : INPUT.split("\\r?\\n")) {
            lineCounter++;
            var trimed = pure(line);
            if (trimed.length() == 0) {
                continue;
            }
            if (trimed.startsWith("@")) {
                String symbol = trimed.substring(1);
                //A command
                if (!A_COMMAND.matcher(trimed).matches()) {
                    throw new RuntimeException("Incorrect syntax at line " + (lineCounter - 1));
                }
                if (LINK_NAME.matcher(symbol).matches()) {
                    if (links.containsKey(symbol)) {
                        binaryCode = "0" + String.format("%15s", Integer.toBinaryString(links.get(symbol))).replace(' ', '0');
                    } else {
                        links.put(symbol, VARIABLE_RAM_INDEX);
                        binaryCode = "0" + String.format("%15s", Integer.toBinaryString(VARIABLE_RAM_INDEX)).replace(' ', '0');
                        VARIABLE_RAM_INDEX++;
                    }
                } else if (DIGITAL.matcher(symbol).matches()) {
                    binaryCode = "0" + String.format("%15s", Integer.toBinaryString(Integer.parseInt(symbol))).replace(' ', '0');
                } else {
                    throw new RuntimeException("Error on naming links at line " + lineCounter);
                }
                commandCounter++;
                System.out.println(binaryCode);
            } else {
                //C command
                Matcher m = C_COMMAND.matcher(trimed);
                if (!m.matches()) {
                    continue;
                }
                String toStore = m.group(1);
                String toStoreBinary = "000";
                String jump = m.group(3);
                String jumpBinary = "000";
                String command = m.group(2);
                String commandBinary = COMMAND_BINARY_MAP.get(command);
                if (toStore != null) {
                    if (toStore.contains("A")) {
                        toStoreBinary = "1";
                    } else {
                        toStoreBinary = "0";
                    }
                    if (toStore.contains("D")) {
                        toStoreBinary += "1";
                    } else {
                        toStoreBinary += "0";
                    }
                    if (toStore.contains("M")) {
                        toStoreBinary += "1";
                    } else {
                        toStoreBinary += "0";
                    }
                }
                if (jump != null) {
                    jump = jump.substring(1);
                    jumpBinary = JUMP_BINARY_MAPPING.get(jump);
                }
                binaryCode = "111" + commandBinary + toStoreBinary + jumpBinary;
                commandCounter++;
                System.out.println(binaryCode);
            }

        }
    }

    private static void fistTrace(String in) {
        int lineCounter = 0, commandCounter = 0;
        for (String line : INPUT.split("\\r?\\n")) {
            lineCounter++;
            var trimed = pure(line);
            if (trimed.length() == 0) {
                continue;
            }
            if (trimed.startsWith("(") && trimed.endsWith(")")) {
                String linkName = trimed.substring(1, trimed.length() - 1);
                if (!LINK_NAME.matcher(linkName).matches()) {
                    throw new RuntimeException("At line " + (lineCounter - 1) + " there is incorrect link name");
                }
                links.put(linkName, commandCounter);
            } else {
                commandCounter++;
            }
        }
    }


    private static String pure(String in) {
        return in.split("//")[0].trim();
    }

    private static String INPUT = "// This file is part of www.nand2tetris.org\n" +
            "// and the book \"The Elements of Computing Systems\"\n" +
            "// by Nisan and Schocken, MIT Press.\n" +
            "// File name: projects/04/Fill.asm\n" +
            "\n" +
            "// Runs an infinite loop that listens to the keyboard input.\n" +
            "// When a key is pressed (any key), the program blackens the screen,\n" +
            "// i.e. writes \"black\" in every pixel;\n" +
            "// the screen should remain fully black as long as the key is pressed. \n" +
            "// When no key is pressed, the program clears the screen, i.e. writes\n" +
            "// \"white\" in every pixel;\n" +
            "// the screen should remain fully clear as long as no key is pressed.\n" +
            "\n" +
            "// Put your code here.\n" +
            "\n" +
            "//!!! Screen doesn't react if I comment this declaration\n" +
            "//@ones\n" +
            "(LOOP)\n" +
            "@somevar\n" +
            "\n" +
            "@KBD\n" +
            "D=M\n" +
            "\n" +
            "@SET_ONE\n" +
            "D;JNE\n" +
            "@R14\n" +
            "M=0\n" +
            "@END_SET\n" +
            "0;JMP\n" +
            "\n" +
            "(SET_ONE)\n" +
            "@R14\n" +
            "M=-1\n" +
            "(END_SET)\n" +
            "\n" +
            "@BLACK_SCREEN\n" +
            "0;JMP\n" +
            "\n" +
            "\n" +
            "//start of black screen\n" +
            "//@R14 which color\n" +
            "(BLACK_SCREEN)\n" +
            "    //i = 256\n" +
            "    @256\n" +
            "    D=A\n" +
            "    @i\n" +
            "    M=D\n" +
            "    \n" +
            "    //index = 0\n" +
            "    @index\n" +
            "    M=0\n" +
            "\n" +
            "    (EXTERNAL_LOOP)\n" +
            "\t// if(i==0) exit loop\n" +
            "\t@i\n" +
            "\tD=M\n" +
            "\t@END_EXT_LOOP\n" +
            "\tD;JEQ\n" +
            "\n" +
            "\t    //j = 32\n" +
            "\t    @32\n" +
            "\t    D=A\n" +
            "\t    @j\n" +
            "\t    M=D\n" +
            "\n" +
            "\t    (INNER_LOOP)\n" +
            "    \t        //if (j==0) goto END_INNER_LOOP\n" +
            "\t\t@j\n" +
            "\t\tD=M\n" +
            "\t\t@END_INNER_LOOP\n" +
            "\t\tD;JEQ\n" +
            "\t\t\n" +
            "\t\t//payload. Count index. Save to @temp. Retreive filling value.\n" +
            "\t\t@index\n" +
            "\t\tD=M\n" +
            "\t\t@SCREEN\n" +
            "\t\tA=A+D\n" +
            "\t\tD=A\n" +
            "\t\t@temp\n" +
            "\t\tM=D\n" +
            "\t\t\n" +
            "\n" +
            "\t\t@R14\n" +
            "\t\tD=M\n" +
            "\t\t@temp\n" +
            "\t\tA=M\n" +
            "\t\tM=D\n" +
            "\n" +
            "\t\t@j //j=j-1\n" +
            "\t\tM=M-1\n" +
            "\t\t@index //index++\n" +
            "\t\tM=M+1\n" +
            "\t\t\n" +
            "\t\t@INNER_LOOP\n" +
            "\t\t0;JMP\n" +
            "\t    (END_INNER_LOOP)\n" +
            "\n" +
            "\t//i = i-1; goto ext_loop\n" +
            "\t@i\n" +
            "\tM=M-1\n" +
            "\t@EXTERNAL_LOOP\n" +
            "\t0;JMP\n" +
            "\n" +
            "(END_EXT_LOOP)\n" +
            "//return to the main loop\n" +
            "@1\n" +
            "0;JMP";
}
