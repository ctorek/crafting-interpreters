package cmd;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Utility command-line program that generates Java for each type of expression in Lox.
 */
public class GenerateAst {
    public static void main(String[] args) throws IOException {
        // Exit if more or fewer args than needed are provided
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            // Exit code for command line usage error
            System.exit(64);
        }

        String outputDir = args[0];
        writeAst(outputDir, "Expression", Arrays.asList(
                // Types of expressions and how they are represented
                "Binary:Expression left,Token operator,Expression right",
                "Grouping:Expression expression",
                "Literal:Object value",
                "Unary:Token operator,Expression expression"
        ));
        System.out.println("Written to " + outputDir + "/Expression.java");

        writeAst(outputDir, "Statement", Arrays.asList(
                // Types of statements and their fields
                "Expr:Expression expression",
                "Print:Expression expression"
        ));
    }

    /**
     * Writes a Java representation of an expression to a file.
     * @param outputDir directory to output the finished code to
     * @param name name of file to output to without a file extension
     * @param types a list of types of expressions and the types they contain
     * @throws IOException
     */
    private static void writeAst(String outputDir, String name, List<String> types) throws IOException {
        String path = outputDir + "/" + name + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        // Imports and class definition
        writer.println("package jlox;\n");
        writer.println("import java.util.List;\n");
        writer.println("abstract class " + name + " {");

        // Visitor pattern
        defineVisitor(writer, name, types);

        // Types of expressions as subclasses
        for (String type: types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();

            // Create subclass
            defineType(writer, name, className, fields);

            // Newline after each
            writer.println();
        }

        // Abstract accept method used in visitor pattern
        writer.println();
        writer.println("\tabstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    /**
     * Writes a visitor interface and method for each subclass of the base class.
     * @param writer the PrintWriter being used to write to the file
     * @param name the name of the base class
     * @param types a list of types of expressions and their fields
     */
    private static void defineVisitor(PrintWriter writer, String name, List<String> types) {
        // Interface header
        writer.println("\tinterface Visitor<R> {");

        // Creating visit method for each type
        for (String type: types) {
            String typeName = type.split(":")[0].trim();
            // Method header
            writer.println("\t\tR visit" + typeName + name + "(" + typeName + " " + name.toLowerCase() + ");");
        }

        // Closing interface
        writer.println("\t}");
    }

    /**
     * Writes Java subclasses of the base Expression class and their fields.
     * @param writer the PrintWriter being used to write to the file
     * @param base the name of the abstract base class that this subclass extends from
     * @param name the name of the subclass being written
     * @param fields the fields that represent the data this expression contains
     */
    private static void defineType(PrintWriter writer, String base, String name, String fields) {
        // Class definition
        writer.println("\tstatic class " + name + " extends " + base + " {");

        // Class fields
        String[] fieldList = fields.split(",");
        for (String field: fieldList) {
            // Field already includes type and name so no splitting is necessary
            writer.println("\t\tfinal " + field.trim() + ";");
        }
        writer.println();

        // Constructor method header
        writer.println("\t\t" + name + "(" + fields + ") {");

        // Constructor body
        for (String field: fieldList) {
            // Field contains type and name so splitting is used to find name
            String fieldName = field.split(" ")[1];
            writer.println("\t\t\tthis." + fieldName + " = " + fieldName + ";");
        }

        // Closing constructor
        writer.println("\t\t}");

        // Override of abstract accept method for visitor pattern
        writer.println();
        writer.println("\t\t@Override");
        writer.println("\t\t<R> R accept(Visitor<R> visitor) {");
        writer.println("\t\t\treturn visitor.visit" + name + base + "(this);");
        writer.println("\t\t}");

        // Closing class
        writer.println("\t}");
    }
}
