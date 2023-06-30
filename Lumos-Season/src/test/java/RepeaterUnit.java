/**
 * @author Dioxide.CN
 * @date 2023/6/28
 * @since 1.0
 */
public class RepeaterUnit {

    public static void main(String[] args) {
        int space = 4;
        int starter = space * 73;
        for (int i = 0; i < 73; i++) {
            int right = starter - i * space;
            int left = right - 9 * space;
            if (left == 0) break;
            // time a - time b
            System.out.println(left + ".." + right);
        }
    }

}
