import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class LexicalAnalyzer {

    // Define sets for different token categories
    private static final Set<String> KEYWORDS = Set.of("if", "else", "return", "int", "float", "string", "boolean");
    private static final Set<String> OPERATORS = Set.of("==", "!=", ">", "<");
    private static final Set<String> ARITHMETIC_OPERATORS = Set.of("+", "-", "*", "/");
    private static final Set<String> SEPARATORS = Set.of("(", ")", "{", "}", ",", ";");

    // Define regex patterns for different token types
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9]*$");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^[0-9]+(\\.[0-9]+)?$");
    private static final Pattern STRING_PATTERN = Pattern.compile("^\".*\"$");  // Matches strings in quotes

    // Token pattern to split by spaces and also keep arithmetic operators separate
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\s+|(?=[+\\-*/(){};,])|(?<=[+\\-*/(){};,])");

    // Define a map to store variable types for type checking
    private Map<String, String> symbolTable = new HashMap<>();

    // Analyze the input code line by line
    public void analyze(String filePath) {
        try {
            Files.lines(Paths.get(filePath)).forEach(this::analyzeLine);
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    // Analyze each line individually
    private void analyzeLine(String line) {
        line = line.trim();
        if (line.isEmpty()) return;  // Skip empty lines

        // Analyze the line and handle variable declarations and assignments
        if (analyzeStatement(line)) {
            if (line.endsWith(";")) {
                System.out.println("(';', 'SEPARATOR')");
            }
        }
    }

    // Generalized helper for printing and identifying tokens
    private void processTokens(String expression) {
        String[] valueTokens = TOKEN_PATTERN.split(expression);
        for (String token : valueTokens) {
            token = token.trim();
            if (!token.isEmpty()) {
                identifyToken(token);
            }
        }
    }

    // Analyze individual statements to identify tokens and populate symbolTable
    private boolean analyzeStatement(String statement) {
        String[] tokens = TOKEN_PATTERN.split(statement);

        if (tokens.length >= 3 && KEYWORDS.contains(tokens[0]) && tokens[2].equals("=")) {
            // Declaration with assignment (e.g., "int x = 10")
            return processDeclaration(tokens, statement);
        } else if (tokens.length >= 3 && tokens[1].equals("=")) {
            // Assignment to an existing variable (e.g., "x = x + 5")
            return processAssignment(tokens, statement);
        } else {
            // For other statements, identify tokens normally
            processTokens(statement);
        }
        return true;
    }

    // Process declarations and populate symbol table
    private boolean processDeclaration(String[] tokens, String statement) {
        String varType = tokens[0];
        String varName = tokens[1];
        String value = statement.substring(statement.indexOf('=') + 1).trim().replace(";", "");

        if (!IDENTIFIER_PATTERN.matcher(varName).matches()) {
            System.out.println("Error: Invalid variable name '" + varName + "'");
            return false;
        }

        String valueType = determineType(value);

        // Check for type mismatch on declaration
        if (!valueType.equals(varType)) {
            System.out.println("Type mismatch: cannot assign value of type '" + valueType + "' to variable '" + varName + "' of type '" + varType + "'");
            return false;
        }

        // Print tokens and add variable to symbol table
        symbolTable.put(varName, varType);
        printToken(varType, "KEYWORD");
        printToken(varName, "IDENTIFIER");
        printToken("=", "OPERATOR");
        processTokens(value);

        return true;
    }

    // Process assignment statements and type-check them
    private boolean processAssignment(String[] tokens, String statement) {
        String varName = tokens[0];
        String value = statement.substring(statement.indexOf('=') + 1).trim().replace(";", "");

        if (!symbolTable.containsKey(varName)) {
            System.out.println("Error: Variable '" + varName + "' not declared before use.");
            return false;
        }

        String expressionType = determineType(value);

        // Check if the type of the expression matches the type of the variable
        if (!typeCheck(varName, expressionType)) {
            return false;
        }

        // Print tokens
        printToken(varName, "IDENTIFIER");
        printToken("=", "OPERATOR");
        processTokens(value);

        return true;
    }

    // Determine the type of an expression or variable
    private String determineType(String value) {
        if (IDENTIFIER_PATTERN.matcher(value).matches() && symbolTable.containsKey(value)) {
            return symbolTable.get(value);
        } else if (containsArithmeticOperator(value)) {
            return evaluateArithmeticExpressionType(value);
        } else {
            return getType(value);
        }
    }

    // Check if the expression contains any arithmetic operators
    private boolean containsArithmeticOperator(String expression) {
        return ARITHMETIC_OPERATORS.stream().anyMatch(expression::contains);
    }

    // Evaluate the type of an arithmetic expression
    private String evaluateArithmeticExpressionType(String expression) {
        String[] components = TOKEN_PATTERN.split(expression.trim());

        String resultType = null;
        for (String component : components) {
            component = component.trim();
            if (component.isEmpty() || ARITHMETIC_OPERATORS.contains(component)) continue;

            // Determine the type of the component (either a literal, variable, or unknown)
            String componentType;
            if (symbolTable.containsKey(component)) {
                componentType = symbolTable.get(component);  // Get type from symbol table if it's a variable
            } else {
                componentType = getType(component);  // Otherwise, determine the type directly
            }

            // Determine the resulting type based on the component's type
            if (resultType == null) {
                resultType = componentType;  // Set the initial type
            } else if ("float".equals(componentType) || "float".equals(resultType)) {
                resultType = "float";  // If any part is float, result is float
            } else if (!resultType.equals(componentType)) {
                System.out.println("Type mismatch within expression: incompatible types in '" + expression + "'");
                return "unknown";
            }
        }
        return resultType;
    }

    // Print token in the required format
    private void printToken(String token, String type) {
        System.out.println("('" + token + "', '" + type + "')");
    }

    // Identify and categorize each token and print it
    private void identifyToken(String token) {
        if (KEYWORDS.contains(token)) {
            printToken(token, "KEYWORD");
        } else if (ARITHMETIC_OPERATORS.contains(token)) {
            printToken(token, "ARITHMETIC_OPERATOR");
        } else if (OPERATORS.contains(token)) {
            printToken(token, "OPERATOR");
        } else if (SEPARATORS.contains(token)) {
            printToken(token, "SEPARATOR");
        } else if (NUMBER_PATTERN.matcher(token).matches()) {
            printToken(token, "NUMBER");
        } else if (STRING_PATTERN.matcher(token).matches()) {
            printToken(token, "STRING");
        } else if (IDENTIFIER_PATTERN.matcher(token).matches()) {
            printToken(token, "IDENTIFIER");
        } else {
            System.out.println("Error: Unrecognized token '" + token + "'");
        }
    }

    // Get type of a value (int, float, string, boolean)
    public static String getType(String s) {
        if (NUMBER_PATTERN.matcher(s).matches()) {
            return s.contains(".") ? "float" : "int";
        } else if (s.equals("True") || s.equals("False")) {
            return "boolean";
        } else if (STRING_PATTERN.matcher(s).matches()) {
            return "string";
        }
        return "unknown";
    }

    // Perform type checking on assignments; return true if types match, otherwise return false
    public boolean typeCheck(String variable, String expressionType) {
        String varType = symbolTable.get(variable);
        if (!varType.equals(expressionType)) {
            System.out.println("Type mismatch: cannot assign value of type '" + expressionType + "' to variable '" + variable + "' of type '" + varType + "'");
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        LexicalAnalyzer lexer = new LexicalAnalyzer();
        String filePath = "src/Sample.txt";  // Replace with your file path
        lexer.analyze(filePath);
    }
}
