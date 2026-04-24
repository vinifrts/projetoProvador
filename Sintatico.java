package br.com.vini.formulas;

import java.util.Stack;

public class Sintatico {
    private static final String OPERADORES = "~^v><";
    private static final String VARIAVEIS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String PARENTESES = "()";

    private static class ResultadoAnalise {
        boolean sucesso;
        int posicaoFinal;
        String mensagemErro;

        ResultadoAnalise(boolean sucesso, int posicaoFinal) {
            this.sucesso = sucesso;
            this.posicaoFinal = posicaoFinal;
            this.mensagemErro = "";
        }

        ResultadoAnalise(boolean sucesso, int posicaoFinal, String mensagemErro) {
            this.sucesso = sucesso;
            this.posicaoFinal = posicaoFinal;
            this.mensagemErro = mensagemErro;
        }
    }

    public static String verificarFBF(String formula) {
        formula = formula.replaceAll("\\s+", "");

        if (formula.isEmpty()) return "Não, porque a fórmula está vazia.";

        String caracterInvalido = verificarCaracteresValidos(formula);
        if (!caracterInvalido.isEmpty())
            return "Não, contém caractere inválido: '" + caracterInvalido + "'";

        String erroParenteses = verificarParentesesBalanceados(formula);
        if (!erroParenteses.isEmpty()) return "Não, " + erroParenteses;

        ResultadoAnalise resultado = analisarExpressao(formula, 0);
        if (!resultado.sucesso)
            return "Não, " + (resultado.mensagemErro.isEmpty()
                    ? "estrutura inválida" : resultado.mensagemErro);

        if (resultado.posicaoFinal < formula.length())
            return "Não, há símbolos extras após posição " + (resultado.posicaoFinal + 1);

        return "Sim, é uma FBF.";
    }

    private static String verificarParentesesBalanceados(String formula) {
        Stack<Integer> pilha = new Stack<>();
        for (int i = 0; i < formula.length(); i++) {
            char c = formula.charAt(i);
            if (c == '(') pilha.push(i);
            else if (c == ')') {
                if (pilha.isEmpty())
                    return "há um ')' sem abertura em " + (i + 1);
                pilha.pop();
            }
        }
        if (!pilha.isEmpty())
            return "há um '(' sem fechamento em " + (pilha.peek() + 1);
        return "";
    }

    private static String verificarCaracteresValidos(String formula) {
        for (char c : formula.toCharArray()) {
            if (VARIAVEIS.indexOf(c) == -1 && OPERADORES.indexOf(c) == -1 && PARENTESES.indexOf(c) == -1)
                return String.valueOf(c);
        }
        return "";
    }

    private static ResultadoAnalise analisarExpressao(String f, int p) {
        if (p >= f.length()) return new ResultadoAnalise(false, p, "termina abruptamente");
        ResultadoAnalise termo = analisarTermo(f, p);
        if (!termo.sucesso) return termo;

        if (termo.posicaoFinal < f.length() &&
                OPERADORES.indexOf(f.charAt(termo.posicaoFinal)) != -1 &&
                f.charAt(termo.posicaoFinal) != '~') {

            char op = f.charAt(termo.posicaoFinal);
            ResultadoAnalise prox = analisarExpressao(f, termo.posicaoFinal + 1);
            if (!prox.sucesso)
                return new ResultadoAnalise(false, termo.posicaoFinal, "após '" + op + "': " + prox.mensagemErro);
            return prox;
        }
        return termo;
    }

    private static ResultadoAnalise analisarTermo(String f, int p) {
        if (p >= f.length()) return new ResultadoAnalise(false, p, "termina abruptamente");
        char c = f.charAt(p);

        if (VARIAVEIS.indexOf(c) != -1) return new ResultadoAnalise(true, p + 1);
        if (c == '~') {
            if (p + 1 >= f.length())
                return new ResultadoAnalise(false, p, "~ não seguido de expressão");
            ResultadoAnalise r = analisarTermo(f, p + 1);
            return r.sucesso ? r : new ResultadoAnalise(false, p, "após ~: " + r.mensagemErro);
        }
        if (c == '(') {
            ResultadoAnalise e = analisarExpressao(f, p + 1);
            if (!e.sucesso) return new ResultadoAnalise(false, p, "erro nos parênteses: " + e.mensagemErro);
            if (e.posicaoFinal >= f.length() || f.charAt(e.posicaoFinal) != ')')
                return new ResultadoAnalise(false, p, "falta ')'");
            return new ResultadoAnalise(true, e.posicaoFinal + 1);
        }
        return new ResultadoAnalise(false, p, "'" + c + "' não inicia termo válido");
    }
}
