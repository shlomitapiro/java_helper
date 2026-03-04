
//3 isolated digits and in order
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







import java.util.*;

public class Solution {

    public static String findEquation(String digits, int target) {

        // ═══════════════════════════════════════════════════
        // בדיקות קלט
        // ═══════════════════════════════════════════════════
        if (digits == null || digits.trim().isEmpty())
            return "No input provided";

        String s = digits.trim();

        if (!s.matches("[0-9]+"))
            return "Invalid input: digits only";

        if (s.length() < 2)
            return "Need at least 2 digits";

        // ═══════════════════════════════════════════════════
        // שלב 1: יצירת כל המספרים האפשריים מהמחרוזת — O(n²)
        // "234510" -> [2, 23, 234, ..., 3, 34, ..., 4, 45, ...]
        // ═══════════════════════════════════════════════════
        List<Integer> allNumbers = new ArrayList<>();

        for (int i = 0; i < s.length(); i++) {
            for (int j = i + 1; j <= s.length(); j++) {
                String sub = s.substring(i, j);

                // מונעים leading zeros כמו "05", "034"
                if (sub.length() > 1 && sub.charAt(0) == '0') continue;

                allNumbers.add(Integer.parseInt(sub));
            }
        }

        // ═══════════════════════════════════════════════════
        // שלב 2: שמירת כל המספרים ב-HashSet לחיפוש O(1)
        // זה מה שמאפשר לנו להגיע ל-O(n⁴) במקום O(n⁶)
        // במקום לחפש את c בלולאה, נחשב מה c חייב להיות
        // ═══════════════════════════════════════════════════
        Set<Integer> numbersSet = new HashSet<>(allNumbers);

        String[] ops = {"+", "-", "*", "/"};

        // ═══════════════════════════════════════════════════
        // שלב 3: עבור כל זוג (a, b) — O(n⁴)
        // נחשב מה c חייב להיות ונבדוק אם הוא קיים ב-HashSet
        // ═══════════════════════════════════════════════════
        for (int a : allNumbers) {
            for (int b : allNumbers) {
                for (String op1 : ops) {
                    for (String op2 : ops) {

                        Double ab = applyOp((double) a, b, op1);
                        if (ab == null) continue;

                        // ─────────────────────────────────────
                        // אפשרות 1: (a op1 b) op2 c = target
                        // => c = reverseOp(target, ab, op2)
                        // ─────────────────────────────────────
                        Double c1 = reverseOp(target, ab, op2);
                        if (c1 != null && isWholeNumber(c1) && numbersSet.contains(c1.intValue())) {
                            return "(" + a + " " + op1 + " " + b + ") "
                                    + op2 + " " + c1.intValue() + " = " + target;
                        }

                        // ─────────────────────────────────────
                        // אפשרות 2: a op1 (b op2 c) = target
                        // => (b op2 c) = reverseOp(target, a, op1)
                        // => c = reverseOp(innerTarget, b, op2)
                        // ─────────────────────────────────────
                        Double innerTarget = reverseOp(target, a, op1);
                        if (innerTarget == null) continue;

                        Double c2 = reverseOp(innerTarget, b, op2);
                        if (c2 != null && isWholeNumber(c2) && numbersSet.contains(c2.intValue())) {
                            return a + " " + op1 + " (" + b + " " + op2
                                    + " " + c2.intValue() + ") = " + target;
                        }
                    }
                }
            }
        }

        return "No equation found for target: " + target;
    }


    // ═══════════════════════════════════════════════════
    // מחשבת את הפעולה ההפוכה כדי למצוא את c
    //
    // אם: result = x op c
    // אז: c = ?
    //
    // + : result = x + c  =>  c = result - x
    // - : result = x - c  =>  c = x - result
    // * : result = x * c  =>  c = result / x
    // / : result = x / c  =>  c = x / result
    // ═══════════════════════════════════════════════════
    private static Double reverseOp(double result, double x, String op) {
        switch (op) {
            case "+": return result - x;
            case "-": return x - result;
            case "*": return x == 0 ? null : result / x;
            case "/": return result == 0 ? null : x / result;
            default:  return null;
        }
    }


    // פעולה רגילה בין שני מספרים
    private static Double applyOp(double a, double b, String op) {
        switch (op) {
            case "+": return a + b;
            case "-": return a - b;
            case "*": return a * b;
            case "/": return b == 0 ? null : a / b;
            default:  return null;
        }
    }


    // בודק שהתוצאה היא מספר שלם ולא 3.7 למשל
    private static boolean isWholeNumber(double d) {
        return d == Math.floor(d) && !Double.isInfinite(d);
    }


    public static void main(String[] args) {
        System.out.println(findEquation("234510", 20));   // (2 + 3) * 4 = 20
        System.out.println(findEquation("999", 9));       // 9 * 9 / 9 = 9
        System.out.println(findEquation("12312", 3));     // 1 * 2 + 1 = 3
        System.out.println(findEquation("5", 10));        // Need at least 2 digits
        System.out.println(findEquation("9999", 999));    // No equation found
        System.out.println(findEquation(null, 5));        // No input provided
    }
}