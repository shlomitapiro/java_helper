public class ThreeDigitTargetSolver {

    public static String findEquation(String s, int target) {
        if (s == null || s.length() < 3) {
            return "No solution";
        }

        boolean[] seen = new boolean[10];
        int[] rightCount = new int[10];

        // Count all digits first
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (!Character.isDigit(ch)) {
                return "Invalid input";
            }
            rightCount[ch - '0']++;
        }

        // Try each position as the middle digit
        for (int j = 0; j < s.length(); j++) {
            int b = s.charAt(j) - '0';

            // Remove current digit from the right side
            rightCount[b]--;

            // Try every digit that exists on the left as 'a'
            for (int a = 0; a <= 9; a++) {
                if (!seen[a]) {
                    continue;
                }

                // Try every digit that exists on the right as 'c'
                for (int c = 0; c <= 9; c++) {
                    if (rightCount[c] <= 0) {
                        continue;
                    }

                    String equation = tryAllOperations(a, b, c, target);
                    if (equation != null) {
                        return equation;
                    }
                }
            }

            // Mark current digit as available on the left for future positions
            seen[b] = true;
        }

        return "No solution";
    }

    private static String tryAllOperations(int a, int b, int c, int target) {
        char[] ops = {'+', '-', '*', '/'};

        for (char op1 : ops) {
            for (char op2 : ops) {

                // Check: (a op1 b) op2 c
                Result first = apply(a, b, op1);
                if (first.valid) {
                    Result second = apply(first.value, c, op2);
                    if (second.valid && second.value == target) {
                        return "(" + a + " " + op1 + " " + b + ") " + op2 + " " + c + " = " + target;
                    }
                }

                // Check: a op1 (b op2 c)
                Result third = apply(b, c, op2);
                if (third.valid) {
                    Result fourth = apply(a, third.value, op1);
                    if (fourth.valid && fourth.value == target) {
                        return a + " " + op1 + " (" + b + " " + op2 + " " + c + ") = " + target;
                    }
                }
            }
        }

        return null;
    }

    private static Result apply(long x, long y, char op) {
        switch (op) {
            case '+':
                return new Result(true, x + y);
            case '-':
                return new Result(true, x - y);
            case '*':
                return new Result(true, x * y);
            case '/':
                if (y == 0) {
                    return new Result(false, 0);
                }
                if (x % y != 0) {
                    return new Result(false, 0);
                }
                return new Result(true, x / y);
            default:
                return new Result(false, 0);
        }
    }

    private static class Result {
        boolean valid;
        long value;

        Result(boolean valid, long value) {
            this.valid = valid;
            this.value = value;
        }
    }

    public static void main(String[] args) {
        System.out.println(findEquation("1234", 7));
        System.out.println(findEquation("731245", 14));
        System.out.println(findEquation("999", 27));
        System.out.println(findEquation("105", 5));
    }
}