import com.sun.net.httpserver.HttpServer; // Webサーバーの機能を読み込みます。
import java.net.InetSocketAddress; // 待ち受ける番号を指定する機能を読み込みます。
import java.util.ArrayList; // Todoを入れるリストの機能を読み込みます。
import java.util.List; // リストの型を使えるようにします。

public class App { // プログラムの本体です。
    public static void main(String[] args) throws Exception { // ここから実行します。
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番で待つサーバーを用意します。
        server.createContext("/", exchange -> { // トップページへのアクセスを処理します。
            String path = exchange.getRequestURI().getPath(); // アクセスされたパスを取り出します。
            String message;
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // UTF-8の文字として返すことを伝えます。
            if (path.equals("/hello")) { // パスが /hello か比べます。
                String query = exchange.getRequestURI().getRawQuery(); // URLの ? より後ろを取り出します。
                String name = java.net.URLDecoder.decode(query.substring(5), "UTF-8"); // name= の後ろを切り出します。
                message = "こんにちは、" + name + "さん！"; // 名前をつないで返す文字を作ります。
            } else if (path.equals("/todos")) { // パスが /todos か比べます。
                List<String> todos = new ArrayList<>(); // Todoを入れるリストを作ります。
                todos.add("牛乳を買う"); // 1件目を入れます。
                todos.add("卵を買う"); // 2件目を入れます。
                todos.add("パンを買う"); // 3件目を入れます。
                todos.add("バターを買う"); // 4件目を入れます。
                String html = "<ul>"; // 一覧の始まりを用意します。
                for (String todo : todos) { // Todoを1件ずつ取り出します。
                    html += "<li>" + todo + "</li>"; // 一覧に1件を足します。
                }
                html += "</ul>"; // 一覧を閉じます。
                message = html; // 作ったHTMLを返す文字にします。
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); // HTMLとして返すことを伝えます。
            } else if (path.equals("/bye")) { // パスが /bye か比べます。
                message = "さようなら！";
            } else if (path.equals("/menu")) { // パスが /menu か比べます。
                message = "おいしい！";
            } else {
                message = "ページが見つかりません";
            }
            byte[] body = message.getBytes("UTF-8"); // 文字をUTF-8のデータに変えます。
            exchange.sendResponseHeaders(200, body.length); // 成功とデータの長さを伝えます。
            exchange.getResponseBody().write(body); // データをブラウザに送ります。
            exchange.getResponseBody().close(); // 送り終えたことを伝えます。
        }); // トップページの処理を終えます。
        server.start(); // サーバーを起動します。
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）"); // 起動したことを表示します。
    } // 実行する部分を終えます。
} // プログラムの本体を終えます。
