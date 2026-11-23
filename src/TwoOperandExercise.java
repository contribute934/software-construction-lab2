/**
 * 两个数运算的练习题，是 {@link Exercise} 的子类。
 *
 * <p>支持四种题型：纯加法、纯减法、纯乘法、纯除法。
 * 加、减、乘采用拒绝采样：随机生成后用基类的约束检查过滤；
 * 除法采用构造法：先随机定被除数，再从它的因数中随机选一个作除数，
 * 保证一定整除，且商的取值不至于总是 1。</p>
 */
public class TwoOperandExercise extends Exercise {

    /** 题型：纯加法。 */
    public static final int ADD = 1;
    /** 题型：纯减法。 */
    public static final int SUB = 2;
    /** 题型：纯乘法。 */
    public static final int MUL = 3;
    /** 题型：纯除法。 */
    public static final int DIV = 4;

    /** 本题题型，取值为上面四个常量之一；用固定构造时为 0。 */
    private final int mode;

    public TwoOperandExercise(int mode) {
        this.mode = mode;
    }

    /** 按给定的运算数和运算符直接构造（供单元测试使用）。 */
    public TwoOperandExercise(int[] operands, char[] operators) {
        super(operands, operators);
        this.mode = 0;
    }

    @Override
    public void generate() {
        while (true) {
            int a;
            int b;
            char op;
            switch (mode) {
                case ADD:
                    a = 1 + RANDOM.nextInt(99);
                    b = 1 + RANDOM.nextInt(99);
                    op = '+';
                    break;
                case SUB:
                    a = 1 + RANDOM.nextInt(99);
                    b = 1 + RANDOM.nextInt(99);
                    op = '-';
                    break;
                case MUL:
                    a = 1 + RANDOM.nextInt(99);
                    b = 1 + RANDOM.nextInt(99);
                    op = '*';
                    break;
                case DIV:
                    // 构造法：先定被除数 a，再从 a 的因数中随机选一个作除数 b，
                    // 则商 a/b 一定是正整数，且不会总是 1
                    a = 1 + RANDOM.nextInt(99);
                    java.util.List<Integer> divisors = new java.util.ArrayList<>();
                    for (int d = 1; d <= a; d++) {
                        if (a % d == 0) {
                            divisors.add(d);
                        }
                    }
                    b = divisors.get(RANDOM.nextInt(divisors.size()));
                    op = '/';
                    break;
                default:
                    throw new IllegalStateException("固定构造的题目不能再调用 generate()");
            }
            operands = new int[]{a, b};
            operators = new char[]{op};
            if (checkConstraints()) {
                answer = evaluate();
                return;
            }
        }
    }
}
