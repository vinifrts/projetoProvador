package br.com.vini.formulas;

public class Lexico {
    private static final String OPERADORES = "~^v<>";
    private static final String VARIAVEIS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String PARENTESES = "()";

    public static String verificarFormula(String formula) {
        for (char c : formula.toCharArray()) {
            if (Character.isWhitespace(c)) continue;

            if (OPERADORES.indexOf(c) == -1 && VARIAVEIS.indexOf(c) == -1 && PARENTESES.indexOf(c) == -1) {
                return "não";
            }
        }
        return "sim";
    }
}
