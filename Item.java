public class Item { // Itemという名前のクラス（プログラムの入れ物）を作ります。
    public static void main(String[] args) { // 実行すると最初に動く部分です。
        String title = "バナナを買う"; // String型（文字を入れる種類）の変数（値を入れる名前付きの箱）を作ります。
        String html = "<li>" + title + "</li>"; // +で文字をつなぎ、HTMLの1行を作ります。
        System.out.println(html);
        boolean done = false;
        int count = 3;
        System.out.println("いま" + count + "件"); // 作った文字をターミナル（文字で結果を見る画面）に出します。
    } // mainの終わりです。
} // Itemクラスの終わりです。
