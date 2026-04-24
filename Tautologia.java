import java.util.*;

public class Tautologia {

    static class Formula {
        char tipo;
        Formula esquerda;
        Formula direita;
        char variavel;
        boolean negada;

        public Formula(char variavel, boolean negada) {
            this.tipo = 'V';
            this.variavel = variavel;
            this.negada = negada;
            this.esquerda = null;
            this.direita = null;
        }

        public Formula(char tipo, Formula esquerda) {
            this.tipo = tipo;
            this.esquerda = esquerda;
            this.direita = null;
            this.negada = false;
        }

        public Formula(char tipo, Formula esquerda, Formula direita) {
            this.tipo = tipo;
            this.esquerda = esquerda;
            this.direita = direita;
            this.negada = false;
        }

        @Override
        public String toString() {
            if (tipo == 'V') {
                return (negada ? "∼" : "") + variavel;
            } else if (tipo == '∼') {
                return "∼(" + esquerda.toString() + ")";
            } else {
                String op = "";
                switch (tipo) {
                    case '^': op = " ∧ "; break;
                    case 'v': op = " v "; break;
                    case '>': op = " → "; break;
                    case '<': op = " ↔ "; break;
                }
                return "(" + esquerda.toString() + op + direita.toString() + ")";
            }
        }
    }

    static class No {
        List<Formula> formulas;
        No esquerdo;
        No direito;
        boolean fechado;

        public No(List<Formula> formulas) {
            this.formulas = formulas;
            this.esquerdo = null;
            this.direito = null;
            this.fechado = false;
        }
    }

    private static int pos;
    private static String formulaStr;

    public static Formula parseFormula(String input) {
        formulaStr = input.replaceAll("\\s+", "");
        formulaStr = formulaStr.replace('~', '∼');
        pos = 0;
        return parseExpressao();
    }

    private static Formula parseExpressao() {
        Formula formula = parseDisjuncao();
        return formula;
    }

    private static Formula parseDisjuncao() {
        Formula esquerda = parseConjuncao();
        while (pos < formulaStr.length() && formulaStr.charAt(pos) == 'v') {
            pos++;
            Formula direita = parseConjuncao();
            esquerda = new Formula('v', esquerda, direita);
        }
        return esquerda;
    }

    private static Formula parseConjuncao() {
        Formula esquerda = parseCondicional();
        while (pos < formulaStr.length() && formulaStr.charAt(pos) == '^') {
            pos++;
            Formula direita = parseCondicional();
            esquerda = new Formula('^', esquerda, direita);
        }
        return esquerda;
    }

    private static Formula parseCondicional() {
        Formula esquerda = parseBicondicional();
        while (pos < formulaStr.length() && formulaStr.charAt(pos) == '>') {
            pos++;
            Formula direita = parseBicondicional();
            esquerda = new Formula('>', esquerda, direita);
        }
        return esquerda;
    }

    private static Formula parseBicondicional() {
        Formula esquerda = parseNegacao();
        while (pos < formulaStr.length() && formulaStr.charAt(pos) == '<') {
            pos++;
            Formula direita = parseNegacao();
            esquerda = new Formula('<', esquerda, direita);
        }
        return esquerda;
    }

    private static Formula parseNegacao() {
        if (pos < formulaStr.length() && (formulaStr.charAt(pos) == '~' || formulaStr.charAt(pos) == '∼')) {
            pos++;
            if (pos < formulaStr.length() && isVariavel(formulaStr.charAt(pos))) {
                char variavel = formulaStr.charAt(pos);
                pos++;
                return new Formula(variavel, true);
            } else if (pos < formulaStr.length() && (formulaStr.charAt(pos) == '~' || formulaStr.charAt(pos) == '∼')) {
                Formula operando = parseNegacao();
                return new Formula('∼', operando);
            } else if (pos < formulaStr.length() && formulaStr.charAt(pos) == '(') {
                Formula operando = parseDirecional();
                return new Formula('∼', operando);
            } else {
                throw new RuntimeException("Expressão de negação inválida");
            }
        }
        return parseDirecional();
    }

    private static boolean isVariavel(char c) {
        return c >= 'A' && c <= 'Z';
    }

    private static Formula parseDirecional() {
        if (pos >= formulaStr.length()) throw new RuntimeException("Fórmula incompleta");
        char c = formulaStr.charAt(pos);
        if (isVariavel(c)) {
            pos++;
            return new Formula(c, false);
        }
        if (c == '(') {
            pos++;
            Formula formula = parseExpressao();
            if (pos >= formulaStr.length() || formulaStr.charAt(pos) != ')') {
                throw new RuntimeException("Parênteses não fechado");
            }
            pos++;
            return formula;
        }
        if (c == '~' || c == '∼') {
            return parseNegacao();
        }
        throw new RuntimeException("Símbolo inesperado: " + c);
    }

    public static boolean verificarTautologia(String formulaStr) {
        Formula formula = parseFormula(formulaStr);
        Formula negacao = new Formula('∼', formula);
        List<Formula> inicial = new ArrayList<>();
        inicial.add(negacao);
        No raiz = new No(inicial);
        expandirTableau(raiz);
        return todosRamosFechados(raiz);
    }

    private static void expandirTableau(No no) {
        if (no.fechado) return;
        if (temContradicao(no.formulas)) {
            no.fechado = true;
            return;
        }
        Optional<Formula> formulaParaExpandir = encontrarFormulaParaExpandir(no.formulas);
        if (!formulaParaExpandir.isPresent()) return;
        Formula formula = formulaParaExpandir.get();
        List<Formula> novasFormulas = new ArrayList<>(no.formulas);
        novasFormulas.remove(formula);

        if (formula.tipo == '∼') {
            if (formula.esquerda.tipo == '∼') {
                novasFormulas.add(formula.esquerda.esquerda);
                no.formulas = novasFormulas;
                expandirTableau(no);
            } else if (formula.esquerda.tipo == '^') {
                List<Formula> ramoEsquerdo = new ArrayList<>(novasFormulas);
                ramoEsquerdo.add(new Formula('∼', formula.esquerda.esquerda));
                List<Formula> ramoDireito = new ArrayList<>(novasFormulas);
                ramoDireito.add(new Formula('∼', formula.esquerda.direita));
                no.esquerdo = new No(ramoEsquerdo);
                no.direito = new No(ramoDireito);
                expandirTableau(no.esquerdo);
                expandirTableau(no.direito);
            } else if (formula.esquerda.tipo == 'v') {
                novasFormulas.add(new Formula('∼', formula.esquerda.esquerda));
                novasFormulas.add(new Formula('∼', formula.esquerda.direita));
                no.formulas = novasFormulas;
                expandirTableau(no);
            } else if (formula.esquerda.tipo == '>') {
                novasFormulas.add(formula.esquerda.esquerda);
                novasFormulas.add(new Formula('∼', formula.esquerda.direita));
                no.formulas = novasFormulas;
                expandirTableau(no);
            } else if (formula.esquerda.tipo == '<') {
                List<Formula> ramoEsquerdo = new ArrayList<>(novasFormulas);
                ramoEsquerdo.add(formula.esquerda.esquerda);
                ramoEsquerdo.add(new Formula('∼', formula.esquerda.direita));
                List<Formula> ramoDireito = new ArrayList<>(novasFormulas);
                ramoDireito.add(new Formula('∼', formula.esquerda.esquerda));
                ramoDireito.add(formula.esquerda.direita);
                no.esquerdo = new No(ramoEsquerdo);
                no.direito = new No(ramoDireito);
                expandirTableau(no.esquerdo);
                expandirTableau(no.direito);
            }
        } else if (formula.tipo == '^') {
            novasFormulas.add(formula.esquerda);
            novasFormulas.add(formula.direita);
            no.formulas = novasFormulas;
            expandirTableau(no);
        } else if (formula.tipo == 'v') {
            List<Formula> ramoEsquerdo = new ArrayList<>(novasFormulas);
            ramoEsquerdo.add(formula.esquerda);
            List<Formula> ramoDireito = new ArrayList<>(novasFormulas);
            ramoDireito.add(formula.direita);
            no.esquerdo = new No(ramoEsquerdo);
            no.direito = new No(ramoDireito);
            expandirTableau(no.esquerdo);
            expandirTableau(no.direito);
        } else if (formula.tipo == '>') {
            List<Formula> ramoEsquerdo = new ArrayList<>(novasFormulas);
            ramoEsquerdo.add(new Formula('∼', formula.esquerda));
            List<Formula> ramoDireito = new ArrayList<>(novasFormulas);
            ramoDireito.add(formula.direita);
            no.esquerdo = new No(ramoEsquerdo);
            no.direito = new No(ramoDireito);
            expandirTableau(no.esquerdo);
            expandirTableau(no.direito);
        } else if (formula.tipo == '<') {
            List<Formula> ramoEsquerdo = new ArrayList<>(novasFormulas);
            ramoEsquerdo.add(formula.esquerda);
            ramoEsquerdo.add(formula.direita);
            List<Formula> ramoDireito = new ArrayList<>(novasFormulas);
            ramoDireito.add(new Formula('∼', formula.esquerda));
            ramoDireito.add(new Formula('∼', formula.direita));
            no.esquerdo = new No(ramoEsquerdo);
            no.direito = new No(ramoDireito);
            expandirTableau(no.esquerdo);
            expandirTableau(no.direito);
        }
    }

    private static boolean temContradicao(List<Formula> formulas) {
        for (int i = 0; i < formulas.size(); i++) {
            Formula f1 = formulas.get(i);
            if (f1.tipo == 'V') {
                for (int j = 0; j < formulas.size(); j++) {
                    Formula f2 = formulas.get(j);
                    if (f2.tipo == '∼' && f2.esquerda.tipo == 'V' && f2.esquerda.variavel == f1.variavel) return true;
                    if (f2.tipo == 'V' && f2.variavel == f1.variavel && f1.negada != f2.negada) return true;
                }
            }
            if (f1.tipo == '∼' && f1.esquerda.tipo == 'V') {
                for (int j = 0; j < formulas.size(); j++) {
                    Formula f2 = formulas.get(j);
                    if (f2.tipo == 'V' && f2.variavel == f1.esquerda.variavel && !f2.negada) return true;
                }
            }
        }
        return false;
    }

    private static Optional<Formula> encontrarFormulaParaExpandir(List<Formula> formulas) {
        for (Formula f : formulas) {
            if (f.tipo != 'V') {
                if (f.tipo == '∼' && f.esquerda.tipo == 'V') continue;
                return Optional.of(f);
            }
        }
        return Optional.empty();
    }

    private static boolean todosRamosFechados(No no) {
        if (no == null) return true;
        if (no.fechado) return true;
        if (no.esquerdo == null && no.direito == null) return false;
        if (no.esquerdo != null && no.direito != null) return todosRamosFechados(no.esquerdo) && todosRamosFechados(no.direito);
        else if (no.esquerdo != null) return todosRamosFechados(no.esquerdo);
        else return todosRamosFechados(no.direito);
    }

    private static Set<Character> encontrarVariaveis(Formula formula) {
        Set<Character> variaveis = new HashSet<>();
        coletarVariaveis(formula, variaveis);
        return variaveis;
    }

    private static void coletarVariaveis(Formula formula, Set<Character> variaveis) {
        if (formula == null) return;
        if (formula.tipo == 'V') variaveis.add(formula.variavel);
        else {
            coletarVariaveis(formula.esquerda, variaveis);
            if (formula.direita != null) coletarVariaveis(formula.direita, variaveis);
        }
    }

    private static boolean avaliarFormula(Formula formula, Map<Character, Boolean> atribuicao) {
        if (formula == null) return false;
        if (formula.tipo == 'V') {
            boolean valor = atribuicao.getOrDefault(formula.variavel, false);
            return formula.negada ? !valor : valor;
        }
        if (formula.tipo == '∼') return !avaliarFormula(formula.esquerda, atribuicao);
        if (formula.tipo == '^') return avaliarFormula(formula.esquerda, atribuicao) && avaliarFormula(formula.direita, atribuicao);
        if (formula.tipo == 'v') return avaliarFormula(formula.esquerda, atribuicao) || avaliarFormula(formula.direita, atribuicao);
        if (formula.tipo == '>') {
            boolean antecedente = avaliarFormula(formula.esquerda, atribuicao);
            boolean consequente = avaliarFormula(formula.direita, atribuicao);
            return !antecedente || consequente;
        }
        if (formula.tipo == '<') {
            boolean esq = avaliarFormula(formula.esquerda, atribuicao);
            boolean dir = avaliarFormula(formula.direita, atribuicao);
            return esq == dir;
        }
        return false;
    }

    public static String classificarFormula(String formulaStr) {
        Formula formula = parseFormula(formulaStr);
        Set<Character> variaveis = encontrarVariaveis(formula);
        List<Character> variaveisOrdenadas = new ArrayList<>(variaveis);
        int numCombinacoes = (int) Math.pow(2, variaveis.size());
        boolean todasVerdadeiras = true;
        boolean todasFalsas = true;
        for (int i = 0; i < numCombinacoes; i++) {
            Map<Character, Boolean> atribuicao = new HashMap<>();
            for (int j = 0; j < variaveisOrdenadas.size(); j++) {
                char variavel = variaveisOrdenadas.get(j);
                boolean valor = ((i >> (variaveisOrdenadas.size() - j - 1)) & 1) == 1;
                atribuicao.put(variavel, valor);
            }
            boolean resultado = avaliarFormula(formula, atribuicao);
            if (!resultado) todasVerdadeiras = false;
            if (resultado) todasFalsas = false;
        }
        if (todasVerdadeiras) return "sim";
        else if (todasFalsas) return "não - é uma contradição";
        else return "não - é uma contingência";
    }
}
