// Todoの一覧を表示するクラスです。
public class Items {
    // プログラムを実行すると最初に呼ばれる部分です。
    public static void main(String[] args) {
        // 表示するTodoを配列に入れます。
        String[] todos = { "牛乳を買う", "", "パンを買う", "掃除をする" };
        // 各Todoが済んだかどうかを同じ順番で入れます。
        boolean[] done = { true, false, false, false };
        // iを0から始め、配列の件数に達するまで1ずつ増やします。
        for (int i = 0; i < todos.length; i++) {
            // i番目のTodoが空の文字列でない場合だけ表示します。
            if (!todos[i].isEmpty()) {
                // 表示する文字列をi番目のTodoから作ります。
                String text = todos[i];
                // i番目のTodoが済んでいたら、先頭に印を付けます。
                if (done[i]) {
                    text = "[済] " + text;
                }
                // i番目のTodoを<li>と</li>で囲んで表示します。
                System.out.println("<li>" + text + "</li>");
            }
        }
    }
}
