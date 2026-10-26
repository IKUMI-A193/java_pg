import com.sun.net.httpserver.HttpServer; // Webサーバーの機能を読み込みます。
import java.net.InetSocketAddress; // 待ち受ける番号を指定する機能を読み込みます。
import java.net.URLDecoder; // 送られたTodoの文字を元に戻す機能を読み込みます。
import java.nio.charset.StandardCharsets; // UTF-8を指定する機能を読み込みます。
import java.util.ArrayList; // Todoを入れるリストの機能を読み込みます。
import java.util.List; // リストの型を使えるようにします。

public class App { // プログラムの本体です。
    public static void main(String[] args) throws Exception { // ここから実行します。
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番で待つサーバーを用意します。
        // Todoを保存する空のリストを一度だけ作ります。
        List<String> todos = new ArrayList<>();
        server.createContext("/", exchange -> { // トップページへのアクセスを処理します。
            String path = exchange.getRequestURI().getPath(); // アクセスされたパスを取り出します。
            String method = exchange.getRequestMethod(); // GETかPOSTかを取り出します。
            String message;
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // UTF-8の文字として返すことを伝えます。
            if (path.equals("/add") && method.equals("POST")) { // Todoの追加かを調べます。
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); // 送られた内容を読み取ります。
                String value = body.substring(5); // 先頭の todo= を除きます。
                String todo = URLDecoder.decode(value, StandardCharsets.UTF_8); // 日本語を元に戻します。
                if (!todo.isEmpty()) { // 空の入力は追加しません。
                    todos.add(todo); // Todoを一覧に追加します。
                }
                exchange.getResponseHeaders().set("Location", "/"); // 戻り先をトップページにします。
                exchange.sendResponseHeaders(303, -1); // トップページへ移動するよう伝えます。
                exchange.close(); // 今回の通信を閉じます。
                return; // 追加の処理を終えます。
            } else if (path.equals("/")) { // トップページかを調べます。
                String html = "<form method='post' action='/add'><input name='todo'><button>追加</button></form><ul>"; // 入力フォームと一覧の始まりを用意します。
                for (String todo : todos) { // Todoを1件ずつ取り出します。
                    html += "<li>" + todo + "</li>"; // 一覧に1件を足します。
                }
                html += "</ul>"; // 一覧を閉じます。
                message = html; // 作ったHTMLを返す文字にします。
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); // HTMLとして返すことを伝えます。
            } else if (path.equals("/hello")) { // パスが /hello か比べます。
                String query = exchange.getRequestURI().getRawQuery(); // URLの ? より後ろを取り出します。
                String name = java.net.URLDecoder.decode(query.substring(5), "UTF-8"); // name= の後ろを切り出します。
                message = "こんにちは、" + name + "さん！"; // 名前をつないで返す文字を作ります。
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
