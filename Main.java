import java.util.ArrayList; // Todoを入れる一覧を作るために使います。
import java.util.List; // 一覧を表す型を使います。

class Todo { // Todoは、やること1件を表すクラス（データの設計図）です。
    String title; // titleは、やることの名前です。
    boolean done; // doneは、済んだかどうかを表します。

    Todo(String title, boolean done) { // 新しいTodoを作るときに値を受け取ります。
        this.title = title; // 受け取った名前を保存します。
        this.done = done; // 受け取った状態を保存します。
    } // 値の設定はここまでです。

    String toItem() { // Todoを1行の文字列に変えるメソッド（処理）です。
        if (done) { // 済んでいる場合です。
            return "<li>[OK!] " + title + "</li>"; // 「[済]」を付けて返します。
        } // 済んでいる場合の処理はここまでです。
        return "<li>" + title + "</li>"; // 未完了なら名前だけを入れて返します。
    } // toItemはここまでです。
} // Todoクラスはここまでです。

public class Main { // 実行を始めるMainクラスです。
    public static void main(String[] args) { // 実行すると最初に動く処理です。
        List<Todo> todos = new ArrayList<>(); // Todoを入れる一覧を作ります。
        todos.add(new Todo("牛乳を買う", false)); // 未完了のTodoを追加します。
        todos.add(new Todo("ゴミを出す", true));
        todos.add(new Todo("掃除をする", false)); // 完了したTodoを追加します。

        for (Todo todo : todos) { // 一覧のTodoを1件ずつ取り出します。
            System.out.println(todo.toItem()); // 変換した1行を表示します。
        } // 繰り返しはここまでです。
    } // mainはここまでです。
} // Mainクラスはここまでです。
