import com.sun.net.httpserver.HttpServer; // Webサーバーの機能を読み込みます。
import java.net.InetSocketAddress; // 待ち受ける番号を指定する機能を読み込みます。
import java.net.URLDecoder; // 送られたTodoの文字を元に戻す機能を読み込みます。
import java.nio.charset.StandardCharsets; // UTF-8を指定する機能を読み込みます。
import java.util.ArrayList; // Todoを入れるリストの機能を読み込みます。
import java.util.List; // リストの型を使えるようにします。

class Todo { // ★変更 Todo1件分のデータです。
    private final int id; // ★変更 何番かを保存します。
    private final String title; // ★変更 やることを保存します。
    private final String dueDate; // 期限日を保存します。未設定なら空文字です。
    private boolean done; // ★変更 終わったかを保存します。

    Todo(int id, String title, String dueDate) { // ★変更 新しいTodoを作ります。
        this.id = id; // ★変更 番号を入れます。
        this.title = title; // ★変更 やることを入れます。
        this.dueDate = dueDate; // 期限日を入れます。
        this.done = false; // ★変更 最初は未完了にします。
    } // ★変更

    int getId() { // ★変更 番号を読み出します。
        return id; // ★変更
    } // ★変更

    String getTitle() { // ★変更 やることを読み出します。
        return title; // ★変更
    } // ★変更

    String getDueDate() { // 期限日を読み出します。
        return dueDate;
    }

    boolean isDone() { // ★変更 終わったかを読み出します。
        return done; // ★変更
    } // ★変更

    void setDone(boolean done) { // ★変更 終わったかを書き換えます。
        this.done = done; // ★変更
    } // ★変更
} // ★変更

public class App { // プログラムの本体です。
    static List<Todo> todos = new ArrayList<>(); // ★変更 Todoを保存します。
    static int nextId = 1; // ★変更 次に振る番号です。

    public static void main(String[] args) throws Exception { // ここから実行します。
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番で待つサーバーを用意します。
        todos.add(new Todo(nextId++, "牛乳を買う", "")); // ★変更 1件目のサンプルを入れます。
        Todo egg = new Todo(nextId++, "卵を買う", ""); // ★変更 2件目のサンプルを作ります。
        egg.setDone(true); // ★変更 2件目を完了にします。
        todos.add(egg); // ★変更 2件目を入れます。
        server.createContext("/", exchange -> { // トップページへのアクセスを処理します。
            String path = exchange.getRequestURI().getPath(); // アクセスされたパスを取り出します。
            String method = exchange.getRequestMethod(); // GETかPOSTかを取り出します。
            String message;
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // UTF-8の文字として返すことを伝えます。
            if (path.equals("/add") && method.equals("POST")) { // Todoの追加かを調べます。
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); // 送られた内容を読み取ります。
                String title = "";
                String dueDate = "";
                for (String field : body.split("&")) { // 送られた項目を1つずつ読みます。
                    int equals = field.indexOf('=');
                    if (equals < 0) {
                        continue;
                    }
                    String name = URLDecoder.decode(field.substring(0, equals), StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(field.substring(equals + 1), StandardCharsets.UTF_8);
                    if (name.equals("todo")) {
                        title = value;
                    } else if (name.equals("dueDate")) {
                        dueDate = value;
                    }
                }
                if (!title.isEmpty()) { // ★変更 空の入力は追加しません。
                    todos.add(new Todo(nextId, title, dueDate)); // やることと期限日を一覧に追加します。
                    nextId++; // ★変更 次の番号に進めます。
                }
                exchange.getResponseHeaders().set("Location", "/"); // 戻り先をトップページにします。
                exchange.sendResponseHeaders(303, -1); // トップページへ移動するよう伝えます。
                exchange.close(); // 今回の通信を閉じます。
                return; // 追加の処理を終えます。
            } else if (path.equals("/done") && method.equals("GET")) { // ★追加 完了のアクセスかを調べます。
                String query = exchange.getRequestURI().getQuery(); // ★追加 URLのidを含む部分を取り出します。
                if (query != null && query.startsWith("id=") && query.length() > 3) { // ★追加 idが書かれているかを調べます。
                    try { // ★追加 数字に変えられるかを試します。
                        int id = Integer.parseInt(query.substring(3)); // ★追加 idを数字に変えます。
                        for (Todo todo : todos) { // ★追加 Todoを1件ずつ調べます。
                            if (todo.getId() == id) { // ★追加 idが一致するかを調べます。
                                todo.setDone(true); // ★追加 一致したTodoを完了にします。
                                break; // ★追加 ここで探すのを終えます。
                            } // ★追加
                        } // ★追加
                    } catch (NumberFormatException e) { // ★追加 数字でないidは何も変えません。
                    } // ★追加
                } // ★追加
                exchange.getResponseHeaders().set("Location", "/"); // ★追加 戻り先をトップページにします。
                exchange.sendResponseHeaders(303, -1); // ★追加 トップページへ移動するよう伝えます。
                exchange.close(); // ★追加 今回の通信を閉じます。
                return; // ★追加 完了の処理を終えます。
            } else if (path.equals("/delete") && method.equals("GET")) { // ★追加 削除のアクセスかを調べます。
                String query = exchange.getRequestURI().getQuery(); // ★追加 URLのidを含む部分を取り出します。
                if (query != null && query.startsWith("id=") && query.length() > 3) { // ★追加 idが書かれているかを調べます。
                    try { // ★追加 数字に変えられるかを試します。
                        int id = Integer.parseInt(query.substring(3)); // ★追加 idを数字に変えます。
                        todos.removeIf(todo -> todo.getId() == id); // ★追加 一致したTodoを削除します。
                    } catch (NumberFormatException e) { // ★追加 数字でないidは何も変えません。
                    } // ★追加
                } // ★追加
                exchange.getResponseHeaders().set("Location", "/"); // ★追加 戻り先をトップページにします。
                exchange.sendResponseHeaders(303, -1); // ★追加 トップページへ移動するよう伝えます。
                exchange.close(); // ★追加 今回の通信を閉じます。
                return; // ★追加 削除の処理を終えます。
            } else if (path.equals("/")) { // トップページかを調べます。
                String html = "<!doctype html><html lang='ja'><head><meta charset='UTF-8'><title>わたしのTodo</title>"
                        + "<style>body{max-width:40rem;margin:2rem auto;padding:0 1rem;font-size:1rem}h1{font-size:1.5rem}</style>"
                        + "</head><body><h1>わたしのTodo</h1>"
                        + "<form method='post' action='/add'><input name='todo'><input type='date' name='dueDate' aria-label='期限日'><button>追加</button></form>"; // やることと期限日の入力欄を用意します。
                if (todos.isEmpty()) {
                    html += "<p>やることは、いまゼロです</p>";
                }
                html += "<ul>"; // 一覧の始まりを用意します。
                for (Todo todo : todos) { // ★変更 Todoを1件ずつ取り出します。
                    String mark = ""; // ★変更 未完了なら印を付けません。
                    if (todo.isDone()) { // ★変更 完了しているかを調べます。
                        mark = " ✔"; // ★変更 完了したTodoに印を付けます。
                    } // ★変更
                    String dueDateText = todo.getDueDate().isEmpty() ? "" : " 期限: " + todo.getDueDate();
                    html += "<li>" + todo.getTitle() + dueDateText + mark + " <a href='/done?id=" + todo.getId()
                            + "'>完了</a> <a href='/delete?id=" + todo.getId() + "'>削除</a></li>"; // 期限日を完了の印より前に表示します。
                }
                html += "</ul></body></html>"; // 一覧とページを閉じます。
                message = html; // 作ったHTMLを返す文字にします。
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); // HTMLとして返すことを伝えます。
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
