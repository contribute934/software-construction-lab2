/**
 * 三个数运算的练习题，是 {@link Exercise} 的子类。
 *
 * <p>支持五种题型：纯加法、纯减法、纯乘法、纯除法、加减乘除混合。
 * 生成时除约束“运算数、每一步中间结果、最终结果都在 (0,100) 内”外，
 * 还要求每一步除法都能整除。纯除法用构造法反推被除数，
 * 其余题型用拒绝采样；含乘除的题型把运算数采样范围收窄到 30 以内，
 * 提高一次采样通过约束检查的概率。</p>
 */
public class ThreeOperandExercise extends Exercise {

    /** 题型：纯加法。 */
    public static final int ADD = 1;
    /** 题型：纯减法。 */
    public static final int SUB = 2;
    /** 题型：纯乘法。 */
    public static final int MUL = 3;
    /** 题型：纯除法。 */
    public static final int DIV = 4;
    /** 题型：加减乘除混合（按运算优先级计算）。 */
    public static final int MIXED = 5;

    /** 四种运算符，混合题型从中随机抽取。 */
    private static final char[] ALL_OPS = {'+', '-', '*', '/'};

    /** 本题题型，取值为上面五个常量之一；用固定构造时为 0。 */
    private final int mode;

    public ThreeOperandExercise(int mode) {
        this.mode = mode;
    }

    /** 按给定的运算数和运算符直接构造（供单元测试使用）。 */
    public ThreeOperandExercise(int[] operands, char[] operators) {
        super(operands, operators);
        this.mode = 0;
    }

    /** 随机生成三个 1..bound 的运算数。 */
    private int[] randomOperands(int bound) {
        return new int[]{
                1 + RANDOM.nextInt(bound),
                1 + RANDOM.nextInt(bound),
                1 + RANDOM.nextInt(bound)};
    }

    @Override
    public void generate() {
        while (true) {
            int[] values;
            char[] ops;
            switch (mode) {
                case ADD:
                    values = randomOperands(99);
                    ops = new char[]{'+', '+'};
                    break;
                case SUB:
                    values = randomOperands(99);
                    ops = new char[]{'-', '-'};
                    break;
                case MUL:
                    values = randomOperands(30);
                    ops = new char[]{'*', '*'};
                    break;
                case DIV:
                    // 构造法：a ÷ b ÷ c，先定两个除数 b、c（均不小于 2，避免 ÷1 的无意义步骤）
                    // 与最终商 q，由第二步 mid = q*c 反推第一步，再由 a = mid*b 反推被除数
                    int b = 2 + RANDOM.nextInt(14);
                    int c = 2 + RANDOM.nextInt(14);
                    int q = 1 + RANDOM.nextInt(9);
                    int mid = q * c;
                    int a = mid * b;
                    values = new int[]{a, b, c};
                    ops = new char[]{'/', '/'};
                    break;
                case MIXED:
                    char o1 = ALL_OPS[RANDOM.nextInt(ALL_OPS.length)];
                    char o2 = ALL_OPS[RANDOM.nextInt(ALL_OPS.length)];
                    boolean hasMulDiv = o1 == '*' || o1 == '/' || o2 == '*' || o2 == '/';
                    values = randomOperands(hasMulDiv ? 30 : 99);
                    ops = new char[]{o1, o2};
                    break;
                default:
                    throw new IllegalStateException("固定构造的题目不能再调用 generate()");
            }
            operands = values;
            operators = ops;
            if (checkConstraints()) {
                answer = evaluate();
                return;
            }
        }
    }
}
