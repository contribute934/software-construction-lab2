import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 练习题基类（抽象类）。
 *
 * <p>把所有练习题共有的属性和行为抽象出来：
 * 运算数 operands、运算符 operators 和参考答案 answer，
 * 以及约束检查、按运算优先级求值、题目文本拼装等公共方法。
 * 具体的题目生成规则由子类实现 {@link #generate()}。</p>
 *
 * <p>实验二在实验一（加、减）的基础上把运算符扩展为加、减、乘、除四种，
 * 求值遵循“先乘除、后加减”的运算优先级。全班统一的约束：
 * 参与运算的数据、运算结果以及三个数运算的每一步中间结果，
 * 都必须是大于 0、小于 100 的正整数，且除法必须能够整除。</p>
 */
public abstract class Exercise {

    /** 全班共用一个随机数发生器，避免短时间内连续 new Random() 取到相同种子。 */
    protected static final Random RANDOM = new Random();

    /** 参与运算的数据，如 {23, 4, 5}。 */
    protected int[] operands;

    /** 相邻两个数之间的运算符，长度 = operands.length - 1，取值为 '+','-','*','/'。 */
    protected char[] operators;

    /** 本题的参考答案（按运算优先级计算的结果）。 */
    protected int answer;

    /** 无参构造，供子类在 generate() 中自行赋值使用。 */
    protected Exercise() {
    }

    /**
     * 按给定的运算数和运算符直接构造一道题（主要供单元测试使用）。
     *
     * @param operands  运算数
     * @param operators 运算符，长度应为 operands.length - 1
     */
    public Exercise(int[] operands, char[] operators) {
        this.operands = operands.clone();
        this.operators = operators.clone();
        this.answer = evaluate();
    }

    /**
     * 生成一道满足约束的题目，由子类按各自题型实现。
     */
    public abstract void generate();

    /**
     * 判断一个数是否满足“&gt;0 且 &lt;100”的约束。
     *
     * @param value 待检查的数
     * @return 在 (0, 100) 开区间内返回 true
     */
    protected boolean inRange(int value) {
        return value > 0 && value < 100;
    }

    /**
     * 对两个数做一次四则运算。除法要求整除，由调用方保证。
     */
    protected int apply(int left, char op, int right) {
        switch (op) {
            case '+':
                return left + right;
            case '-':
                return left - right;
            case '*':
                return left * right;
            case '/':
                return left / right;
            default:
                throw new IllegalArgumentException("未知运算符：" + op);
        }
    }

    /**
     * 按“先乘除、后加减”的优先级求值，返回每一步运算的结果（按实际运算顺序排列）。
     *
     * <p>第一遍从左到右先把所有乘、除算完，得到各步的中间结果；
     * 第二遍再从左到右计算剩下的加、减。最后一个元素即本题答案。</p>
     *
     * @return 每一步运算结果组成的列表
     * @throws ArithmeticException 某一步除法不能整除时抛出
     */
    public List<Integer> stepResults() {
        List<Integer> steps = new ArrayList<>();
        // 第一遍：先处理乘除
        List<Integer> values = new ArrayList<>();
        List<Character> addSubOps = new ArrayList<>();
        values.add(operands[0]);
        for (int i = 0; i < operators.length; i++) {
            char op = operators[i];
            int right = operands[i + 1];
            if (op == '*' || op == '/') {
                int left = values.remove(values.size() - 1);
                if (op == '/' && (right == 0 || left % right != 0)) {
                    throw new ArithmeticException("除法不能整除：" + left + " / " + right);
                }
                int result = apply(left, op, right);
                steps.add(result);
                values.add(result);
            } else {
                addSubOps.add(op);
                values.add(right);
            }
        }
        // 第二遍：再处理加减
        int acc = values.get(0);
        for (int i = 0; i < addSubOps.size(); i++) {
            acc = apply(acc, addSubOps.get(i), values.get(i + 1));
            steps.add(acc);
        }
        return steps;
    }

    /**
     * 计算本题答案（即最后一步的运算结果）。
     */
    public int evaluate() {
        List<Integer> steps = stepResults();
        return steps.get(steps.size() - 1);
    }

    /**
     * 检查本题是否满足全部约束：运算数在 (0,100) 内；
     * 每一步中间结果（含除法整除）与最终结果都在 (0,100) 内。
     *
     * @return 全部满足返回 true
     */
    public boolean checkConstraints() {
        if (operands == null || operators == null
                || operands.length != operators.length + 1) {
            return false;
        }
        for (int operand : operands) {
            if (!inRange(operand)) {
                return false;
            }
        }
        try {
            for (int step : stepResults()) {
                if (!inRange(step)) {
                    return false;
                }
            }
        } catch (ArithmeticException e) {
            return false;
        }
        return true;
    }

    /** 把内部运算符映射为题目中显示的符号（* 显示为 ×，/ 显示为 ÷）。 */
    protected char symbol(char op) {
        if (op == '*') {
            return '×';
        }
        if (op == '/') {
            return '÷';
        }
        return op;
    }

    /**
     * 题目文本，如 "23 + 4 × 5 ="。
     */
    public String getQuestionText() {
        StringBuilder sb = new StringBuilder();
        sb.append(operands[0]);
        for (int i = 0; i < operators.length; i++) {
            sb.append(' ').append(symbol(operators[i])).append(' ').append(operands[i + 1]);
        }
        sb.append(" =");
        return sb.toString();
    }

    /**
     * 带答案的题目文本，如 "23 + 4 × 5 = 43"。
     */
    public String getTextWithAnswer() {
        return getQuestionText() + " " + answer;
    }

    @Override
    public String toString() {
        return getQuestionText();
    }

    public int[] getOperands() {
        return operands.clone();
    }

    public char[] getOperators() {
        return operators.clone();
    }

    public int getAnswer() {
        return answer;
    }
}
