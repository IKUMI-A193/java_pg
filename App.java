import com.sun.net.httpserver.HttpServer; // Webサーバーの機能を読み込みます。
import java.net.InetSocketAddress; // 待ち受ける番号を指定する機能を読み込みます。

public class App { // プログラムの本体です。
    public static void main(String[] args) throws Exception { // ここから実行します。
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番で待つサーバーを用意します。
        server.createContext("/", exchange -> { // トップページへのアクセスを処理します。
            System.out.println("ハンドラが動いた"); // アクセス時にターミナルへ表示します。
            String message = "Hello, Server!はろはろり～ん"; // 返す文字を決めます。
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // UTF-8の文字として返すことを伝えます。
            byte[] body = message.getBytes("UTF-8"); // 文字をUTF-8のデータに変えます。
            exchange.sendResponseHeaders(200, body.length); // 成功とデータの長さを伝えます。
            exchange.getResponseBody().write(body); // データをブラウザに送ります。
            exchange.getResponseBody().close(); // 送り終えたことを伝えます。
        }); // トップページの処理を終えます。
        server.start(); // サーバーを起動します。
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）"); // 起動したことを表示します。
    } // 実行する部分を終えます。
} // プログラムの本体を終えます。
