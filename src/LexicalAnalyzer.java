import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
public class LexicalAnalyzer {
    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
        "float", "int", "string", "boolean", "if", "else", "return"
    ));
    private static final Set<String> OPERATORS = new HashSet<>(Arrays.asList(
        "+", "-", "*", "/", "=", "==", "!=", ">","<"
    ));
    private static final Set<String> SEPARATORS = new HashSet<>(Arrays.asList(
        "(", ")", "{", "}", ",", ";"
    ));
    public static String getType(String s){
        if(s.matches("[+-]?\\d+")) return "int";
        if(s.matches("[+-]?(\\d+\\.\\d+|\\.\\d+|\\d+\\.)")) return "float";
        if(s.equals("True") || s.equals("False")) return "boolean";
        return "unknown";
    }
    public static Boolean checkSemiColon(String s){
        return s.charAt(s.length()-1) == ';';
    }
    public static void analyzeLine(String line){
        String[] tokens = line.split("\\s+");
        String keyword = tokens[0];
        String op = tokens[2];
        String type;

        if(KEYWORDS.contains(tokens[0])){
            if(keyword.equals("return")){

            }else if (keyword.equals("if")){

            }else{
                if(!checkSemiColon(tokens[3])) {
                    System.out.println("Missing semicolon.");
                    return;
                }
                System.out.println("('" + keyword + "', 'KEYWORD'");
                System.out.println("('" + tokens[1] + "', 'IDENTIFIER'");
                if(op.equals("=")){
                    System.out.println("('" + op + "', 'OPERATOR'");
                    type = getType(tokens[3].substring(0, tokens[3].length()-1));
                    if(type.equals(keyword)) System.out.println("('" + tokens[3].substring(0, tokens[3].length()-1) + "', '" + type + "')");
                }else{System.out.println("('" + op + "' is an invalid OPERATOR");}
            }
        }
        else{
            System.out.println("Keyword \'" + keyword + "\' not supported.");
        }
        }

}

