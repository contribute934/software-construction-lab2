import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 练习题生成器（工厂类）。
 *
 * <p>对上层只暴露统一的接口：按题型编号生成一道题或批量生成一组不重复的题，
 * 上层不直接接触两个数题、三个数题两个子类，符合依赖倒转原则。
 * 题型编号 1..10，其中 10 表示从前 9 种题型中随机抽取。</p>
 */
public class ExerciseGenerator {

    /** 题型名称，索引 = 题型编号 - 1。 */
    public static final String[] TYPE_NAMES = {
            "两个数纯加法", "两个数纯减法", "两个数纯乘法", "两个数纯除法",
            "三个数纯加法", "三个数纯减法", "三个数纯乘法", "三个数纯除法",
            "三个数混合运算（加减乘除）", "随机混合（以上任意题型）"
    };

    /** 题型总数（含随机混合）。 */
    public static final int TYPE_COUNT = TYPE_NAMES.length;

    /** 生成器自己的随机源，只用于“随机混合”时抽取题型。 */
    private static final Random RANDOM = new Random();

    /**
     * 按题型编号生成一道满足约束的题目。
     *
     * @param type 题型编号 1..10
     * @return 生成好的题目
     */
    public Exercise createOne(int type) {
        Exercise exercise;
        switch (type) {
            case 1:
                exercise = new TwoOperandExercise(TwoOperandExercise.ADD);
                break;
            case 2:
                exercise = new TwoOperandExercise(TwoOperandExercise.SUB);
                break;
            case 3:
                exercise = new TwoOperandExercise(TwoOperandExercise.MUL);
                break;
            case 4:
                exercise = new TwoOperandExercise(TwoOperandExercise.DIV);
                break;
            case 5:
                exercise = new ThreeOperandExercise(ThreeOperandExercise.ADD);
                break;
            case 6:
                exercise = new ThreeOperandExercise(ThreeOperandExercise.SUB);
                break;
            case 7:
                exercise = new ThreeOperandExercise(ThreeOperandExercise.MUL);
                break;
            case 8:
                exercise = new ThreeOperandExercise(ThreeOperandExercise.DIV);
                break;
            case 9:
                exercise = new ThreeOperandExercise(ThreeOperandExercise.MIXED);
                break;
            case 10:
                return createOne(1 + RANDOM.nextInt(TYPE_COUNT - 1));
            default:
                throw new IllegalArgumentException("题型编号应在 1.." + TYPE_COUNT + " 之间");
        }
        exercise.generate();
        return exercise;
    }

    /**
     * 批量生成指定数量的题目，同一批内按题目文本去重。
     *
     * @param type  题型编号 1..10
     * @param count 需要的题目数量
     * @return 不重复的题目列表
     */
    public List<Exercise> generateBatch(int type, int count) {
        Set<String> seen = new LinkedHashSet<>();
        List<Exercise> result = new ArrayList<>();
        int attempts = 0;
        while (result.size() < count && attempts < count * 200) {
            attempts++;
            Exercise exercise = createOne(type);
            if (seen.add(exercise.getQuestionText())) {
                result.add(exercise);
            }
        }
        return result;
    }
}
