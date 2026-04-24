package br.com.vini.formulas;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== ETAPA I - LÉXICO ===");
        System.out.println("Fórmula (A<B)^(C>D): " + Lexico.verificarFormula("(A<B)^(C>D)"));
        System.out.println("Fórmula ~(AvB): " + Lexico.verificarFormula("~(AvB)"));

        System.out.println("\n=== ETAPA II - SINTÁTICO ===");
        String[] formulas = {
                "(A>B)^(B>A)",
                "A^C",
                "(A>B))^(B>A)",
                "A)) ^^ > BC",
                "~A",
                "(~A>B)"
        };
        for (String f : formulas) {
            System.out.println(f + " → " + Sintatico.verificarFBF(f));
        }

        System.out.println("\n=== ETAPA III - TAUTOLOGIA ===");
        String formula = "(A>B)^(B>A)";
        boolean resultado = Tautologia.verificarTautologia(formula);
        System.out.println("A fórmula " + formula + " é tautologia? " + (resultado ? "Sim" : "Não"));
    }
}
