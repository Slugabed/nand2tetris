package bulat.assembler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Assembler {
    private static Pattern C_COMMAND = Pattern.compile("([AMD]{1,3}=)?(1|0|-1|D|[A|M]|!D|![A|M]|-D|-[A|M]|D\\+1|[A|M]\\+1|D-1|[A|M]-1|D\\+[A|M]|D-[A|M]|[A|M]-D|D&[A|M]|D\\|[A|M])" +
            "(;(JGT|JEQ|JGE|JLT|JNE|JLE|JMP))?");
    private static Pattern A_COMMAND = Pattern.compile("@((\\w.*)|\\d+)");
    private static Pattern LINK_NAME = Pattern.compile("\\w.*");
    private static Pattern DIGITAL = Pattern.compile("\\d+");
    private static Pattern RAM_REFERENCE = Pattern.compile("R(\\d{1,2})");

    private static Map<String, Integer> links = new HashMap<>();
    private static Integer VARIABLE_RAM_INDEX = 16;

    private static Map<String, String> COMMAND_BINARY_MAP = new HashMap<>() {{
        put("0", "0101010");
        put("1", "0111111");
        put("-1", "0111010");
        put("D", "0001100");
        put("A", "0110000");
        put("M", "1110000");
        put("!D", "0001101");
        put("!A", "0110001");
        put("!M", "1110001");
        put("-D", "0001111");
        put("-A", "0110011");
        put("-M", "1110011");
        put("D+1", "0011111");
        put("A+1", "0110111");
        put("M+1", "1110111");
        put("D-1", "0001110");
        put("A-1", "0110010");
        put("M-1", "1110010");
        put("D+A", "0000010");
        put("D+M", "1000010");
        put("D-A", "0010011");
        put("D-M", "1010011");
        put("A-D", "0000111");
        put("M-D", "1000111");
        put("D&A", "0000000");
        put("D&M", "1000000");
        put("D|A", "0010101");
        put("D|M", "1010101");
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

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Please pass asm file path");
            System.exit(-1);
        }
        Path inputPath = Path.of(args[0]);
        byte[] bytes = Files.readAllBytes(inputPath);
        String INPUT = new String(bytes, StandardCharsets.UTF_8);

        Path OUTPUT = Path.of("output.hack");
        try (BufferedWriter bw = Files.newBufferedWriter(OUTPUT)) {
            String binaryCode;
            fistPassage(INPUT);
            int lineCounter = 0, commandCounter = 0;
            for (String line : INPUT.split("\\r?\\n")) {
                lineCounter++;
                var trimed = pure(line);
                if (trimed.length() == 0) {
                    continue;
                }
                if (trimed.startsWith("@")) {
                    if (!A_COMMAND.matcher(trimed).matches()) {
                        throw new RuntimeException("Incorrect syntax at line " + (lineCounter - 1));
                    }

                    String symbol = trimed.substring(1);
                    //A command
                    if (symbol.equals("SP")) {
                        binaryCode = "0" + String.format("%15s", Integer.toBinaryString(0)).replace(' ', '0');
                    } else
                    if (symbol.equals("LCL")) {
                        binaryCode = "0" + String.format("%15s", Integer.toBinaryString(1)).replace(' ', '0');
                    } else
                    if (symbol.equals("ARG")) {
                        binaryCode = "0" + String.format("%15s", Integer.toBinaryString(2)).replace(' ', '0');
                    } else
                    if (symbol.equals("THIS")) {
                        binaryCode = "0" + String.format("%15s", Integer.toBinaryString(3)).replace(' ', '0');
                    } else
                    if (symbol.equals("THAT")) {
                        binaryCode = "0" + String.format("%15s", Integer.toBinaryString(4)).replace(' ', '0');
                    } else
                    if (symbol.equals("SCREEN")) {
                        binaryCode = "0" + String.format("%15s", Integer.toBinaryString(16384)).replace(' ', '0');
                    } else if (symbol.equals("KBD")) {
                        binaryCode = "0" + String.format("%15s", Integer.toBinaryString(24576)).replace(' ', '0');
                    } else if (DIGITAL.matcher(symbol).matches()) {
                        binaryCode = "0" + String.format("%15s", Integer.toBinaryString(Integer.parseInt(symbol))).replace(' ', '0');
                    } else if (LINK_NAME.matcher(symbol).matches()) {
                        Matcher remMatcher = RAM_REFERENCE.matcher(symbol);
                        if (remMatcher.matches()) {
                            binaryCode = "0" + String.format("%15s", Integer.toBinaryString(Integer.parseInt(remMatcher.group(1)))).replace(' ', '0');
                        } else if (links.containsKey(symbol)) {
                            binaryCode = "0" + String.format("%15s", Integer.toBinaryString(links.get(symbol))).replace(' ', '0');
                        } else {
                            links.put(symbol, VARIABLE_RAM_INDEX);
                            binaryCode = "0" + String.format("%15s", Integer.toBinaryString(VARIABLE_RAM_INDEX)).replace(' ', '0');
                            VARIABLE_RAM_INDEX++;
                        }
                    } else {
                        throw new RuntimeException("Error on naming links at line " + lineCounter);
                    }
                    commandCounter++;
                    bw.write(binaryCode);
                    bw.newLine();
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
                    bw.write(binaryCode);
                    bw.newLine();
                }

            }
        }
    }

    private static void fistPassage(String in) {
        int lineCounter = 0, commandCounter = 0;
        for (String line : in.split("\\r?\\n")) {
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
}
